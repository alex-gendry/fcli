/*******************************************************************************
 * Copyright 2021, 2022 Open Text.
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
package com.fortify.cli.ssc._common.rest.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fortify.cli.common.json.JsonHelper;

public class SSCInputTransformer {
    public static final JsonNode getDataOrSelf(JsonNode json) {
        if(json.has("data") ) {
            return json.get("data");
        } else {
            switch(json.getNodeType()){
                case OBJECT:
                    ArrayNode data = JsonHelper.getObjectMapper().createArrayNode();
                    data.add(json);
                    return data;
                case ARRAY:
                default:
                    return json;
            }
        }
    }
}
