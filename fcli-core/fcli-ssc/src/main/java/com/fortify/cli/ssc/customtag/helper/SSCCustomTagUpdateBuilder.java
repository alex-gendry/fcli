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
import com.fortify.cli.common.util.StringUtils;
import com.fortify.cli.ssc.customtag.domain.SSCCustomTagValueType;
import kong.unirest.UnirestInstance;
import org.codehaus.stax2.io.EscapingWriterFactory;
import org.yaml.snakeyaml.external.com.google.gdata.util.common.base.Escaper;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SSCCustomTagUpdateBuilder {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final SSCCustomTagHelper customTagHelper;
    private final Map<String, String> customTags = new LinkedHashMap<>();
    private ArrayNode preparedCustomTagUpdateData = null;

    public SSCCustomTagUpdateBuilder(UnirestInstance unirest, String appVersionId) {
        this(new SSCCustomTagHelper(unirest, appVersionId));
    }

    public SSCCustomTagUpdateBuilder(SSCCustomTagHelper customTagHelper) {
        this.customTagHelper = customTagHelper;
    }

    public SSCCustomTagUpdateBuilder add(Map<String, String> customTags) {
        if (customTags != null && !customTags.isEmpty()) {
            this.customTags.putAll(customTags);
        }
        return resetPreparedRequest();
    }

    public SSCCustomTagUpdateBuilder prepareAndCheckBody() {
        if (this.preparedCustomTagUpdateData == null) {
            this.preparedCustomTagUpdateData = prepareCustomTagUpdateData();
        }
        return this;
    }

    public ArrayNode buildUpdateBody() {
        prepareAndCheckBody();

        return this.preparedCustomTagUpdateData;
    }

    public Set<String> getCustomTagIds() {
        prepareAndCheckBody();
        return this.getCustomTagIds(this.preparedCustomTagUpdateData);
    }

    private ArrayNode prepareCustomTagUpdateData() {
        return prepareCustomTags().entrySet().stream()
                .map(this::createCustomTagUpdateNode)
                .collect(JsonHelper.arrayNodeCollector());
    }

    private Map<String, String> prepareCustomTags() {
        Map<String, String> preparedCustomTags = new LinkedHashMap<>();
        preparedCustomTags.putAll(this.customTags);
        return preparedCustomTags;
    }

    private ObjectNode createCustomTagUpdateNode(Map.Entry<String, String> customTagEntry) {
        return this.createCustomTagUpdateNode(customTagHelper, customTagEntry);
    }

    private SSCCustomTagUpdateBuilder resetPreparedRequest() {
        this.preparedCustomTagUpdateData = null;
        return this;
    }


    private static final Set<String> getCustomTagIds(ArrayNode customTags) {
        return JsonHelper.stream(customTags)
                .map(j -> j.get("customTagId"))
                .map(JsonNode::asText)
                .collect(Collectors.toSet());
    }


    private final ObjectNode createCustomTagUpdateNode(SSCCustomTagHelper helper, Map.Entry<String, String> customTagEntry) {
        String customTagNameOrId = customTagEntry.getKey();
        SSCCustomTagDescriptor descriptor = helper.getCustomTagDescriptor(customTagNameOrId);
        String customTagGuid = descriptor.getGuid();
        SSCCustomTagValueType valueType = descriptor.getValueType();
        String value = customTagEntry.getValue();

        ObjectNode customTagUpdateNode = objectMapper.createObjectNode();
        customTagUpdateNode.put("customTagGuid", customTagGuid);

        switch (valueType) {
            case TEXT:
                customTagUpdateNode.put("textValue",  value);
                break;
            case DATE:
                customTagUpdateNode.put("dateValue", getOptionDateValue(descriptor, value));
                break;
            case DECIMAL:
                customTagUpdateNode.put("decimalValue", getOptionDecimalValue(descriptor, value));
                break;
            case LIST:
                String index = "-1";
                try {
                    index = getValueIndex(helper, descriptor, value);
                } catch (SSCCustomTagHelper.InvalidCustomTagValueOrIndexException e) {
                    if (descriptor.isExtensible()) {
                        helper.createCustomTagValue(descriptor, value);
                        index = getValueIndex(helper, descriptor, value);
                    } else {
                        throw e;
                    }
                }
                customTagUpdateNode.put("newCustomTagIndex", index);
                break;
            default:
                throw new IllegalStateException("Unknown Value type " + valueType + " for Custom Tag " + descriptor.getName());
        }
        return customTagUpdateNode;
    }

    private static Float getOptionDecimalValue(SSCCustomTagDescriptor descriptor, String value) {
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Value for Custom Tag '" + descriptor.getName() + "' must be a decimal");
        }
    }

    private static String getOptionDateValue(SSCCustomTagDescriptor descriptor, String value) {
        if (!Pattern.matches("\\d{4}-\\d{2}-\\d{2}", value)) {
            throw new IllegalArgumentException("Value for Custom Tag '" + descriptor.getName() + "' must be specified as yyyy-MM-dd");
        }
        return value;
    }

    private static final String getValueIndex(SSCCustomTagHelper helper, SSCCustomTagDescriptor descriptor, String value) {
        ArrayNode valueIndexes = getValueIndexes(helper, descriptor, value);
        if (valueIndexes.size() > 1) {
            throw new IllegalArgumentException("Custom Tag '" + descriptor.getName() + "' can only contain a single value");
        }
        return valueIndexes.get(0).get("lookupIndex").textValue();
    }


    private static ArrayNode getValueIndexes(SSCCustomTagHelper helper, SSCCustomTagDescriptor descriptor, String value) {
        return Stream.of(value.split(","))
                .filter(StringUtils::isNotBlank)
                .map(val -> helper.getValueIndex(descriptor.getGuid(), val))
                .map(SSCCustomTagUpdateBuilder::createCustomTagValueNode)
                .collect(JsonHelper.arrayNodeCollector());
    }

    private static final ObjectNode createCustomTagValueNode(String valueLookupIndex) {
        return objectMapper.createObjectNode().put("lookupIndex", valueLookupIndex);
    }
}
