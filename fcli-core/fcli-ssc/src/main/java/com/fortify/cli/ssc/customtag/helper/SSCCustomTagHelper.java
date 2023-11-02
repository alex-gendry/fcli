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

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.formkiq.graalvm.annotations.Reflectable;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.ssc._common.rest.SSCUrls;
import kong.unirest.UnirestInstance;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

public final class SSCCustomTagHelper {
    public static final int DEFAULT_POLL_INTERVAL_SECONDS = 1;

    private SSCCustomTagHelper() {
    }

    public static SSCCustomTagDescriptor getCustomTagDescriptor(UnirestInstance unirest, String customTagId) {
        return getDescriptor(getCustomTagJsonNode(unirest, customTagId));
    }

    private static JsonNode getCustomTagJsonNode(UnirestInstance unirest, String customTagId) {
        return unirest.get(SSCUrls.CUSTOM_TAG(customTagId)).asObject(JsonNode.class).getBody().get("data");
    }


    private static SSCCustomTagDescriptor getDescriptor(JsonNode scanNode) {
        return JsonHelper.treeToValue(scanNode, SSCCustomTagDescriptor.class);
    }

}
