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
package com.fortify.cli.ssc.customtag.cli.cmd;

import com.fortify.cli.common.output.cli.cmd.IBaseRequestSupplier;
import com.fortify.cli.common.output.cli.mixin.OutputHelperMixins;
import com.fortify.cli.common.rest.query.IServerSideQueryParamGeneratorSupplier;
import com.fortify.cli.common.rest.query.IServerSideQueryParamValueGenerator;
import com.fortify.cli.ssc._common.output.cli.cmd.AbstractSSCBaseRequestOutputCommand;
import com.fortify.cli.ssc._common.output.cli.cmd.AbstractSSCOutputCommand;
import com.fortify.cli.ssc._common.rest.SSCUrls;
import com.fortify.cli.ssc._common.rest.query.SSCQParamGenerator;
import com.fortify.cli.ssc._common.rest.query.SSCQParamValueGenerators;
import com.fortify.cli.ssc._common.rest.query.cli.mixin.SSCQParamMixin;
import com.fortify.cli.ssc.appversion.cli.mixin.SSCAppVersionResolverMixin;
import kong.unirest.HttpRequest;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = OutputHelperMixins.List.CMD_NAME)
public class SSCCustomTagListCommand extends AbstractSSCBaseRequestOutputCommand implements IServerSideQueryParamGeneratorSupplier {
    @Getter
    @Mixin
    private OutputHelperMixins.List outputHelper;
    @Mixin
    private SSCAppVersionResolverMixin.OptionalOption appVersionResolver;
    @Mixin
    private SSCQParamMixin qParamMixin;
    @Getter
    private IServerSideQueryParamValueGenerator serverSideQueryParamGenerator = new SSCQParamGenerator()
            .add("id", SSCQParamValueGenerators::plain)
            .add("guid", SSCQParamValueGenerators::wrapInQuotes)
            .add("name", SSCQParamValueGenerators::wrapInQuotes);

    @Override
    public HttpRequest<?> getBaseRequest(UnirestInstance unirest) {
        if (this.appVersionResolver.getAppVersionNameOrId() != null) {
            return getUnirestInstance().get(SSCUrls.PROJECT_VERSION_CUSTOM_TAGS(this.appVersionResolver.getAppVersionId(unirest)));
        } else {
            return getUnirestInstance().get(SSCUrls.CUSTOM_TAGS);
        }
    }

    @Override
    public boolean isSingular() {
        return false;
    }
}
