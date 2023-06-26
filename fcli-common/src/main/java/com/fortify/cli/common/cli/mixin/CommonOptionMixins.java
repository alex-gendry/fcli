package com.fortify.cli.common.cli.mixin;

import com.fortify.cli.common.util.PicocliSpecHelper;
import com.fortify.cli.common.util.StringUtils;

import lombok.Getter;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParameterException;

public class CommonOptionMixins {
    private CommonOptionMixins() {}
    
    public static class OptionalDestinationFile {
        @Option(names = {"-f", "--dest"}, descriptionKey = "fcli.destination-file")
        @Getter private String destination;
    }
    
    public static class RequireConfirmation {
        @Mixin private CommandHelperMixin commandHelper;
        @Option(names = {"-y", "--confirm"}, defaultValue = "false")
        private boolean confirmed;
        
        public void checkConfirmed(Object... promptArgs) {
            if (!confirmed) {
                CommandSpec spec = commandHelper.getCommandSpec();
                if ( System.console()==null ) {
                    throw new ParameterException(spec.commandLine(), "Missing option: Confirm operation with -y / --confirm (interactive prompt not available)");
                } else {
                    String expectedResponse = PicocliSpecHelper.getRequiredMessageString(spec, "expectedConfirmPromptResponse");
                    String response = System.console().readLine(getPrompt(promptArgs));
                    if ( response.equalsIgnoreCase(expectedResponse) ) {
                        return;
                    } else {
                        throw new IllegalStateException("Aborting: operation aborted by user");
                    }
                }
            }
        }
        
        private String getPrompt(Object... promptArgs) {
            CommandSpec spec = commandHelper.getCommandSpec();
            String promptFormat = PicocliSpecHelper.getMessageString(spec, "confirmPrompt");
            if ( StringUtils.isBlank(promptFormat) ) {
                String[] descriptionLines = spec.optionsMap().get("-y").description();
                if ( descriptionLines==null || descriptionLines.length<1 ) {
                    throw new RuntimeException("No proper description found for generating prompt for --confirm option");
                }
                promptFormat = spec.optionsMap().get("-y").description()[0].replaceAll("[. ]+$", "")+"?";
            }
            String prompt = String.format(promptFormat, promptArgs);
            String promptOptions = PicocliSpecHelper.getRequiredMessageString(spec, "confirmPromptOptions");
            return String.format("%s (%s) ", prompt, promptOptions);
        }
    }
}
