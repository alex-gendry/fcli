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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.formkiq.graalvm.annotations.Reflectable;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.common.json.JsonNodeHolder;
import com.fortify.cli.ssc.customtag.domain.SSCCustomTagRestrictionType;
import com.fortify.cli.ssc.customtag.domain.SSCCustomTagType;
import com.fortify.cli.ssc.customtag.domain.SSCCustomTagValueType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

@Reflectable @NoArgsConstructor
@Data @EqualsAndHashCode(callSuper=true)
public class SSCCustomTagDescriptor extends JsonNodeHolder {
    private String id;
    private String guid;
    private String name;
    private String description;
    private SSCCustomTagType type;
    private SSCCustomTagValueType valueType;
    private boolean hidden;
    private boolean inUse;
    private boolean extensible;
    private int objectVersion;
    private boolean requiresComment;
    private boolean restriction;
    private SSCCustomTagRestrictionType restrictionType;
    private Map<String, SSCCustomTagValueDescriptor> valuesByValue = new LinkedHashMap<>();
    private Map<String, SSCCustomTagValueDescriptor> valuesByIndex = new LinkedHashMap<>();

    @JsonProperty("valueList")
    public void setValueList(ArrayNode valuesNode) {
        if ( valuesNode!=null ) {
            JsonHelper.stream(valuesNode)
                .map(o->JsonHelper.treeToValue(o, SSCCustomTagValueDescriptor.class))
                .forEach(this::addValue);
        }
    }

    private void addValue(SSCCustomTagValueDescriptor descriptor) {
        valuesByValue.put(descriptor.getLookupValue(), descriptor);
        valuesByIndex.put(descriptor.getLookupIndex(), descriptor);
    }

    public JsonNode getValueListAsJson() {
        return asJsonNode().get("valueList");
    }

//    public String getFullName() {
//        return category+":"+name;
//    }

    public String getValueTypeName() {
        return valueType.name();
    }

    public String getRestrictionTypeName() {
        return restrictionType.name();
    }

    public String getTypeName() {
        return type.name();
    }

    public boolean checkCommentIsRequired() {
        return requiresComment;
    }


//    public void checkOptionNames(String... requiredNames) {
//        var names = optionsByName.keySet();
//        var requiredNamesList = Arrays.asList(requiredNames);
//        if (optionsByName.keySet().size() != requiredNames.length || !names.containsAll(requiredNamesList)) {
//            throw new IllegalStateException("SSC attribute " + name + " must be configured to have exactly these options: " + requiredNamesList);
//        }
//    }

//    public void check(boolean required, SSCAttributeDefinitionType requiredType, String... requiredOptionNames) {
//        if (required) {
//            checkIsRequired();
//        }
//        if (requiredType != null) {
//            checkType(requiredType);
//        }
//        if (requiredOptionNames != null) {
//            checkOptionNames(requiredOptionNames);
//        }
//    }
}
