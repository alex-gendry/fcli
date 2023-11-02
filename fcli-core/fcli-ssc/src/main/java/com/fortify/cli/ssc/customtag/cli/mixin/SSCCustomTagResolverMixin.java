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
package com.fortify.cli.ssc.customtag.cli.mixin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.cli.util.EnvSuffix;
import com.fortify.cli.ssc.customtag.helper.SSCCustomTagDescriptor;
import com.fortify.cli.ssc.customtag.helper.SSCCustomTagHelper;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Parameters;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SSCCustomTagResolverMixin {
    public static abstract class AbstractSSCAppVersionCustomTagResolverMixin {
        public abstract String getCustomTagId();

        public SSCCustomTagDescriptor getCustomTagDescriptor(UnirestInstance unirest){
            return SSCCustomTagHelper.getCustomTagDescriptor(unirest, getCustomTagId());
        }
        
        public String getCustomTagId(UnirestInstance unirest) {
            return getCustomTagDescriptor(unirest).getId();
        }
    }
    
    public static abstract class AbstractSSCAppVersionMultiCustomTagResolverMixin {
        public abstract String[] getCustomTagIds();

        public SSCCustomTagDescriptor[] getCustomTagDescriptors(UnirestInstance unirest){
            return Stream.of(getCustomTagIds()).map(id-> SSCCustomTagHelper.getCustomTagDescriptor(unirest, id)).toArray(SSCCustomTagDescriptor[]::new);
        }
        
        public Collection<JsonNode> getCustomTagDescriptorJsonNodes(UnirestInstance unirest){
            return Stream.of(getCustomTagDescriptors(unirest)).map(SSCCustomTagDescriptor::asJsonNode).collect(Collectors.toList());
        }
        
        public String[] getCustomTagIds(UnirestInstance unirest) {
            return Stream.of(getCustomTagDescriptors(unirest)).map(SSCCustomTagDescriptor::getId).toArray(String[]::new);
        }
    }
    
    public static class PositionalParameter extends AbstractSSCAppVersionCustomTagResolverMixin {
        @EnvSuffix("ARTIFACT") @Parameters(index = "0", arity = "1", paramLabel="customtag-id", descriptionKey = "fcli.ssc.customtag.resolver.id")
        @Getter private String customTagId;
    }
    
    public static class PositionalParameterMulti extends AbstractSSCAppVersionMultiCustomTagResolverMixin {
        @EnvSuffix("ARTIFACTS") @Parameters(index = "0", arity = "1..", paramLabel = "customtag-id's", descriptionKey = "fcli.ssc.customtag.resolver.ids")
        @Getter private String[] customTagIds;
    }
}
