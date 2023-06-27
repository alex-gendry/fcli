/*******************************************************************************
 * Copyright 2021, 2023 Open Text.
 *
 * The only warranties for products and services of Open Text 
 * and its affiliates and licensors (“Open Text”) are as may 
 * be set forth in the express warranty statements accompanying 
 * such products and services. Nothing herein should be construed 
 * as constituting an additional warranty. Open Text shall not be 
 * liable for technical or editorial errors or omissions contained 
 * herein. The information contained herein is subject to change 
 * without notice.
 *******************************************************************************/
package com.fortify.cli.app;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;

import org.graalvm.nativeimage.hosted.Feature;
import org.jasypt.normalization.Normalizer;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.app.i18n.I18nParameterExceptionHandler;
import com.fortify.cli.common.cli.util.FortifyCLIInitializerRunner;
import com.fortify.cli.common.cli.util.IFortifyCLIInitializer;
import com.fortify.cli.common.rest.unirest.GenericUnirestFactory;
import com.fortify.cli.common.variable.FcliVariableHelper;
import com.oracle.svm.core.annotate.AutomaticFeature;

import io.micronaut.configuration.picocli.MicronautFactory;
import io.micronaut.configuration.picocli.PicocliRunner;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.TypeHint;
import picocli.CommandLine;

/**
 * <p>This class provides the {@link #main(String[])} entrypoint into the application. 
 * It first configures logging and then loads the {@link PicocliRunner} class to
 * actually execute commands based on provided command line arguments.</p>
 * 
 * <p>This class is also responsible for registering some GraalVM features, allowing
 * the application to run properly as GraalVM native images.</p>
 * 
 * @author Ruud Senden
 */
public class FortifyCLI {
	private static final Boolean JANSI_DISABLE = Boolean.getBoolean("jansi.disable");

    /**
     * This is the main entry point for executing the Fortify CLI.
     * @param args Command line options passed to Fortify CLI
     */
    public static void main(String[] args) {
        System.exit(execute(args));
    }

    /**
     * This method starts the Micronaut {@link ApplicationContext}, then invokes all beans that implement the
     * {@link IFortifyCLIInitializer} interface prior to executing {@link CommandLine#execute(String...)}.
     * @param args Command line options passed to Fortify CLI
     * @return exit code
     */
    private static int execute(String[] args) {
    	String[] resolvedArgs = FcliVariableHelper.resolveVariables(args);
        try (ApplicationContext applicationContext = ApplicationContext.builder(FortifyCLI.class, Environment.CLI).start()) {
            try ( MicronautFactory micronautFactory = new MicronautFactory(applicationContext) ) {
            	installAnsiConsole();
                FortifyCLIInitializerRunner.initialize(resolvedArgs, micronautFactory);
                CommandLine commandLine = new CommandLine(FCLIRootCommands.class, micronautFactory);
                return commandLine
                    .setParameterExceptionHandler(new I18nParameterExceptionHandler(commandLine.getParameterExceptionHandler()))
                    .execute(resolvedArgs);
            } finally {
                GenericUnirestFactory.shutdown();
            	uninstallAnsiConsole();
            }
        }
    }
    
    private static final void installAnsiConsole() {
    	tryInvokeAnsiConsoleMethod("systemInstall");
    }
    
    private static final void uninstallAnsiConsole() {
    	tryInvokeAnsiConsoleMethod("systemUninstall");
    }
    
    private static final void tryInvokeAnsiConsoleMethod(String methodName) {
    	if ( !JANSI_DISABLE ) {
	    	try {
	    		// AnsiConsole performs eager initialization in a static block, so
	    		// referencing the class directly would initialize Jansi even if
	    		// isJansiEnabled() returns false. As such, we use reflection to 
	    		// only load the AnsiConsole class if Jansi is enabled, and then
	    		// invoke the specified method. Note that in order for this to work, 
	    		// we have a reflect-config.json file to allow reflective access to
	    		// AnsiConsole.
	    		Class.forName("org.fusesource.jansi.AnsiConsole")
	    			.getMethod(methodName).invoke(null);
	    	} catch ( Throwable t ) {
	    		t.printStackTrace();
	    	}
    	}
    }
    
/**
 * Register classes for runtime reflection in GraalVM native images. The
 * {@link TypeHint} annotation is used to generate reflect-config.json 
 * for some standard Java data classes on which we may want to reflectively 
 * invoke methods from SpEL expressions. Alternatively, we could look into 
 * creating a custom MethodResolver that covers all of these classes and 
 * potentially doesn't require reflective access.
 */
 @TypeHint(
         value = {
             Boolean.class,
             Double.class,
             Float.class,
             Integer.class,
             Long.class,
             Short.class,
             String.class,
             ObjectNode.class,
             ArrayNode.class,
             ArrayList.class,
             LinkedHashMap.class,
             HashSet.class
         },
         accessType = TypeHint.AccessType.ALL_PUBLIC_METHODS
     )
    @AutomaticFeature
    public static final class RuntimeReflectionRegistrationFeature implements Feature {
        public void beforeAnalysis(BeforeAnalysisAccess access) {
            // This jasypt class uses reflection, so we perform a dummy operation to have GraalVM native image generation detect this
            Normalizer.normalizeToNfc("dummy");
        }
    }
}
