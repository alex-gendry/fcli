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
package com.fortify.cli.ssc.access_control.cli.mixin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.cli.util.EnvSuffix;
import com.fortify.cli.ssc.access_control.helper.SSCUserHelper;

import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Parameters;

public class SSCUserResolverMixin {
    private static abstract class AbstractSSCAuthEntityResolverMixin {
        public abstract String getAuthEntitySpec();
        
        public JsonNode getAuthEntityJsonNode(UnirestInstance unirest) {
            return new SSCUserHelper(unirest).getAuthEntities(false, true, getAuthEntitySpec());
        }
    }
    
    public static class PositionalParameterSingle extends AbstractSSCAuthEntityResolverMixin {
        @EnvSuffix("USER") @Parameters(index = "0", arity = "1", descriptionKey = "fcli.ssc.access-control.user.resolver.spec")
        @Getter private String authEntitySpec;
    }
}
