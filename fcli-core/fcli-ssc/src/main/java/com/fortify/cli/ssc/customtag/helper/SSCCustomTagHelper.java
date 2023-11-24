/*******************************************************************************
 * Copyright 2021, 2023 Open Text.
 *
 * The only warranties for products and services of Open Text 
 * and its affiliates and licensors ("Open Text") are as may 
 * be set forth in the express warranty statements accompanying 
 * such products and services. Nothing herein should be construed 
 * as constituting an additional warranty. Open Text shall not be 
 * liable for technical or editorial errors or omissions contained 
 * herein. The information contained herein is subject to change 
 * without notice.
 *******************************************************************************/
package com.fortify.cli.ssc.customtag.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.ssc._common.rest.SSCUrls;
import com.fortify.cli.ssc._common.rest.helper.SSCInputTransformer;
import kong.unirest.HttpRequest;
import kong.unirest.UnirestInstance;
import lombok.*;

import java.util.*;

public final class SSCCustomTagHelper {
    private final UnirestInstance unirest;
    private final Set<String> customTagDuplicateNames = new HashSet<>();
    private final Map<String, SSCCustomTagDescriptor> descriptorsById = new HashMap<>();
    private final Map<String, SSCCustomTagDescriptor> descriptorsByName = new HashMap<>();
    private final Map<String, SSCCustomTagDescriptor> descriptorsByGuid = new HashMap<>();
    private final Map<String, SSCCustomTagValueHelper> customTagValuesByIdMap = new HashMap<>();
    @Getter
    private final ArrayNode customTags;
    private final String appVersionId;

    /**
     * This constructor calls the SSC attributeDefinitions endpoint to retrieve attribute definition data,
     * then calls the {@link #processCustomTags(JsonNode)} method for each attribute definition
     * to collect the relevant details.
     *
     * @param unirest
     */
    public SSCCustomTagHelper(UnirestInstance unirest, String appVersionid) {
        this(unirest,
                getCustomTagsRequest(unirest, appVersionid)
                .asObject(ObjectNode.class).getBody(),
                appVersionid);
    }

    public SSCCustomTagHelper(UnirestInstance unirest, JsonNode ctTags, String appVersionId) {
        this.unirest = unirest;
        this.appVersionId = appVersionId;
        this.customTags = (ArrayNode) SSCInputTransformer.getDataOrSelf(ctTags);
        this.customTags.forEach(this::processCustomTags);
    }

    /**
     * Return an {@link HttpRequest} of which the response can be passed to
     * one of the constructors. This is useful for including the request in
     * an SSC bulk request.
     */
    public static final HttpRequest<?> getCustomTagsRequest(UnirestInstance unirest, String appVersionId) {
        return unirest.get(SSCUrls.PROJECT_VERSION_CUSTOM_TAGS(appVersionId));
    }

    /**
     * This method stores attribute definition data from the given {@link JsonNode}
     * (representing a single attribute definition) in various instance variables
     * for easy lookup.
     *
     * @param jsonNode representing a single Custom Tag
     */
    public SSCCustomTagDescriptor processCustomTags(JsonNode jsonNode) {
        SSCCustomTagDescriptor descriptor = JsonHelper.treeToValue(jsonNode, SSCCustomTagDescriptor.class);
        String id = descriptor.getId();
        String guid = descriptor.getGuid();
        String name = descriptor.getName();

        if (descriptorsByName.containsKey(name)) {
            this.customTagDuplicateNames.add(name); // SSC allows for having the same attribute name in different categories
        }

        this.descriptorsById.put(id, descriptor);
        this.descriptorsByGuid.put(guid, descriptor);
        this.descriptorsByName.put(name, descriptor);
        this.customTagValuesByIdMap.put(id, new SSCCustomTagValueHelper(descriptor.getValueListAsJson()));

        return descriptor;
    }

    /**
     * Get the attribute definition descriptor for the given attribute id, guid, or name.
     *
     * @param customTagIdOrGuidOrName
     * @return {@link SSCCustomTagDescriptor} instance
     */
    public SSCCustomTagDescriptor getCustomTagDescriptor(String customTagIdOrGuidOrName) {
        SSCCustomTagDescriptor descriptor = descriptorsById.get(customTagIdOrGuidOrName);
        if (descriptor == null) {
            descriptor = descriptorsByGuid.get(customTagIdOrGuidOrName);
        }
        if (descriptor == null && customTagDuplicateNames.contains(customTagIdOrGuidOrName)) {
            throw new IllegalArgumentException("Custom tag name '" + customTagIdOrGuidOrName + "' is not unique; Use the guid");
        }
        if (descriptor == null) {
            descriptor = descriptorsByName.get(customTagIdOrGuidOrName);
        }
        if (descriptor == null) {
            JsonNode systemCustomTags;

            systemCustomTags = this.unirest.get(SSCUrls.CUSTOM_TAGS).queryString("fields","id,guid,name")
                    .asObject(JsonNode.class).getBody().get("data");
            for(final JsonNode tempTag: systemCustomTags){
                if(customTagIdOrGuidOrName.equals(tempTag.get("id").textValue())
                || customTagIdOrGuidOrName.equals(tempTag.get("guid").textValue())
                || customTagIdOrGuidOrName.equals(tempTag.get("name").textValue())){
                    JsonNode customTag = unirest.post(SSCUrls.PROJECT_VERSION_CUSTOM_TAGS(this.appVersionId))
                            .body(JsonHelper.getObjectMapper().createObjectNode().set("guid", tempTag.get("guid")))
                            .asObject(JsonNode.class).getBody().get("data");

                    return this.processCustomTags(customTag);
                }
            }

            throw new IllegalArgumentException("Custom tag id, guid or name '" + customTagIdOrGuidOrName + "' does not exist");
        }
        return descriptor;
    }

    /**
     * Get the value index for the value or index, for the given
     * Custom Tag id, guid, or name.
     *
     * @param customTagIdOrGuidOrName
     * @param value
     * @return value index
     */
    public String getValueIndex(String customTagIdOrGuidOrName, String value) {
        return customTagValuesByIdMap.get(getCustomTagDescriptor(customTagIdOrGuidOrName).getId()).getValueIndex(value);
    }

    /**
     * Get the value for the given value or index, for the given
     * attribute id, guid, or name.
     *
     * @param customTagIdOrGuidOrName
     * @param valueOrIndex
     * @return value
     */
    public String getValue(String customTagIdOrGuidOrName, String valueOrIndex) {
        return customTagValuesByIdMap.get(getCustomTagDescriptor(customTagIdOrGuidOrName).getId()).getValue(valueOrIndex);
    }

    public static class InvalidCustomTagValueOrIndexException extends IllegalArgumentException {
        public InvalidCustomTagValueOrIndexException(String errorMessage) {
            super(errorMessage);
        }
    }

    public void createCustomTagValue(SSCCustomTagDescriptor customTagDescriptor, String value) {
        ObjectNode body = customTagDescriptor.asObjectNode();

        ObjectNode newValue = JsonHelper.getObjectMapper().createObjectNode();
        newValue.put("lookupValue", value);

        ArrayNode values = (ArrayNode) body.get("valueList");
        values.add(newValue);

        body.set("valueList", values);

        this.processCustomTags(this.unirest.put(SSCUrls.CUSTOM_TAG(customTagDescriptor.getId())).body(body)
                .asObject(JsonNode.class).getBody().get("data"));
    }


    /**
     * This class stores values data for a single custom tag, and provides
     * various methods for retrieving values properties based on the value or the lookup index.
     *
     * @author rsenden
     */
    private static final class SSCCustomTagValueHelper {
        private final Map<String, String> valueIndexesByIndex = new HashMap<>();
        private final Map<String, String> valueIndexesByName = new HashMap<>();
        private final Map<String, JsonNode> valuesByIndex = new HashMap<>();


        /**
         * This constructor takes a {@link JsonNode} representing a custom tag values array,
         * and calls the {@link #processValue(JsonNode)}
         * method for each value to collect the relevant data.
         *
         * @param valueList
         */
        public SSCCustomTagValueHelper(JsonNode valueList) {
            if (valueList != null && !valueList.isEmpty() && valueList.isArray()) {
                valueList.forEach(this::processValue);
            }
        }

        /**
         * This method stores values data from the given {@link JsonNode}
         * (representing a single value) in various instance variables
         * for easy lookup.
         *
         * @param jsonNode representing a single value
         */
        private void processValue(JsonNode jsonNode) {
            String lookupIndex = jsonNode.get("lookupIndex").asText();
            String lookupValue = jsonNode.get("lookupValue").asText();

            valueIndexesByIndex.put(lookupIndex, lookupIndex);
            valueIndexesByName.put(lookupValue, lookupIndex);
            valuesByIndex.put(lookupIndex, jsonNode);
        }

        /**
         * Get the value index for the given value.
         *
         * @param value
         * @return value index
         */
        public String getValueIndex(String value) {
            String lookupIndex = null;
            if (valueIndexesByName.containsKey(value)) {
                lookupIndex = valueIndexesByName.get(value);
            }
            if (lookupIndex == null) {
                throw new InvalidCustomTagValueOrIndexException("Custom Tag Value or Index '" + value + "' does not exist");
            }
            return lookupIndex;
        }

        /**
         * Get the value for the given value or index.
         *
         * @param index
         * @return value
         */
        public String getValue(String index) {
            return valuesByIndex.get(getValueIndex(index)).get("lookupValue").asText();
        }
    }

    public static final int DEFAULT_POLL_INTERVAL_SECONDS = 1;

    public static SSCCustomTagDescriptor getCustomTagDescriptor(UnirestInstance unirest, String customTagId) {
        return getDescriptor(getCustomTagJsonNode(unirest, customTagId));
    }

    private static JsonNode getCustomTagJsonNode(UnirestInstance unirest, String customTagId) {
        return unirest.get(SSCUrls.CUSTOM_TAG(customTagId)).asObject(JsonNode.class).getBody().get("data");
    }


    private static SSCCustomTagDescriptor getDescriptor(JsonNode scanNode) {
        return JsonHelper.treeToValue(scanNode, com.fortify.cli.ssc.customtag.helper.SSCCustomTagDescriptor.class);
    }

}
