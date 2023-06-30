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
package com.fortify.cli.ssc.entity.appversion_artifact.cli.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.output.cli.cmd.IJsonNodeSupplier;
import com.fortify.cli.ssc.entity.appversion_artifact.cli.mixin.SSCAppVersionArtifactResolverMixin;
import com.fortify.cli.ssc.entity.appversion_artifact.helper.SSCAppVersionArtifactHelper;
import com.fortify.cli.ssc.output.cli.mixin.SSCOutputHelperMixins;

import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = SSCOutputHelperMixins.ArtifactApprove.CMD_NAME)
public class SSCAppVersionArtifactApproveCommand extends AbstractSSCAppVersionArtifactOutputCommand implements IJsonNodeSupplier {
    @Getter @Mixin private SSCOutputHelperMixins.ArtifactApprove outputHelper; 
    @Mixin private SSCAppVersionArtifactResolverMixin.PositionalParameter artifactResolver;
    @Option(names = {"-m", "--message"}, defaultValue = "Approved through fcli")
    private String message;
    
    @Override
    public JsonNode getJsonNode() {
        var unirest = getUnirestInstance();
        SSCAppVersionArtifactHelper.approve(unirest, artifactResolver.getArtifactId(unirest), message);
        return artifactResolver.getArtifactDescriptor(unirest).asJsonNode();
    }
    
    @Override
    public boolean isSingular() {
        return true;
    }
}
