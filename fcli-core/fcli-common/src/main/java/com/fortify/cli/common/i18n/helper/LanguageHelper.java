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
package com.fortify.cli.common.i18n.helper;

import java.nio.file.Path;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.formkiq.graalvm.annotations.Reflectable;
import com.fortify.cli.common.json.JsonNodeHolder;
import com.fortify.cli.common.util.FcliDataHelper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

public final class LanguageHelper {
    // TODO Any way we can dynamically determine available languages?
    // TODO Re-add NL and other languages once resource bundles are up to date
    private static final String[] supportedLanguages = {"en", "nl"};
    private LanguageHelper() {}
    
    public static final Stream<LanguageDescriptor> getSupportedLanguageDescriptorsStream() {
        return Stream.of(supportedLanguages)
            .map(LanguageDescriptor::new);
    }
    
    public static final LanguageDescriptor getConfiguredLanguageDescriptor() {
        Path languageConfigPath = getLanguageConfigPath();
        LanguageConfigDescriptor configDescriptor = !FcliDataHelper.exists(languageConfigPath) 
                ? new LanguageConfigDescriptor() 
                : FcliDataHelper.readFile(languageConfigPath, LanguageConfigDescriptor.class, true);
        return configDescriptor.getLanguageDescriptor();
    }
    
    public static final LanguageDescriptor setConfiguredLanguage(LanguageDescriptor descriptor) {
        Path languageConfigPath = getLanguageConfigPath();
        FcliDataHelper.saveFile(languageConfigPath, new LanguageConfigDescriptor(descriptor), true);
        return descriptor;
    }
    
    public static final LanguageDescriptor setConfiguredLanguage(String language) {
        return setConfiguredLanguage(new LanguageDescriptor(language));
    }
    
    public static final void clearLanguageConfig() {
        FcliDataHelper.deleteFile(getLanguageConfigPath(), true);
    }
    
    private static final Path getLanguageConfigPath() {
        return FcliDataHelper.getFcliConfigPath().resolve("i18n/language.json");
    }
    
    @Data @EqualsAndHashCode(callSuper = false) 
    @Reflectable @NoArgsConstructor @AllArgsConstructor
    private static final class LanguageConfigDescriptor extends JsonNodeHolder {
        private String language = "en";
        
        public LanguageConfigDescriptor(LanguageDescriptor languageDescriptor) {
            this.language = languageDescriptor.getLanguage();
        }
        
        @JsonIgnore
        public LanguageDescriptor getLanguageDescriptor() {
            return new LanguageDescriptor(language);
        }
    }
}
