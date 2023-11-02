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

import lombok.Getter;
import picocli.CommandLine.Option;

import java.util.Map;

public class SSCCustomTagUpdateMixin {
        private static final String PARAM_LABEL = "CUSTOM_TAG_NAME_OR_ID";
    public static abstract class AbstractSSCAppVersionCustomTagUpdateMixin {
        public abstract Map<String,String> getCustomTags();

    }
    
    public static class OptionalCustomTagOption extends AbstractSSCAppVersionCustomTagUpdateMixin {
        @Option(names = {"--cts", "--customtags"}, required = false, split = ",", paramLabel = PARAM_LABEL, descriptionKey = "fcli.ssc.customtag.update.option")
        @Getter private Map<String,String> customTags;
    }
    
    public static class RequiredCustomTagOption extends AbstractSSCAppVersionCustomTagUpdateMixin {
        @Option(names = {"--cts", "--customtags"}, required = true, split = ",", paramLabel = PARAM_LABEL, descriptionKey = "fcli.ssc.customtag.update.option")
        @Getter private Map<String,String> customTags;
    }
}
