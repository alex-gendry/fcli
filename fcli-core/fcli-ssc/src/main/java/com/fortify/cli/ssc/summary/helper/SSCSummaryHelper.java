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
package com.fortify.cli.ssc.summary.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.ssc._common.rest.helper.SSCInputTransformer;
import com.fortify.cli.ssc.attribute.helper.SSCAttributeDefinitionHelper;
import kong.unirest.HttpRequest;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 *  
 * @author agendry
 *
 */
@Setter
@Getter
public class SSCSummaryHelper {
    public String header(String text, Integer level){
        String headerLevel = "";
        for(Integer i = 0 ; i < level; i++ ) { headerLevel += "#";}

        return String.format("%s %s\n", headerLevel, text);
    }

    public String bold(String text) {
        return String.format("**%s**", text);
    }

    public String image(String altText, String path) {
        return String.format("[%s](%s)", altText, path);
    }

    public String rule() {
        return "---\n";
    }
}
