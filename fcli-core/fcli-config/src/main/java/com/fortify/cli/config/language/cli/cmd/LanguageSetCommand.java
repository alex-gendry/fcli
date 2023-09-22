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
package com.fortify.cli.config.language.cli.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.cli.util.EnvSuffix;
import com.fortify.cli.common.i18n.helper.LanguageHelper;
import com.fortify.cli.common.output.cli.cmd.AbstractOutputCommand;
import com.fortify.cli.common.output.cli.cmd.IJsonNodeSupplier;
import com.fortify.cli.common.output.cli.mixin.OutputHelperMixins;

import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Parameters;

@Command(name = OutputHelperMixins.Set.CMD_NAME)
public class LanguageSetCommand extends AbstractOutputCommand implements IJsonNodeSupplier {
    @Mixin @Getter private OutputHelperMixins.Set outputHelper;
    @EnvSuffix("LANGUAGE") @Parameters(index = "0", descriptionKey = "fcli.config.language.set.language")
    private String language;
    
    @Override
    public JsonNode getJsonNode() {
        return LanguageHelper.setConfiguredLanguage(language).asObjectNode();
    }
    
    @Override
    public boolean isSingular() {
        return true;
    }
}
