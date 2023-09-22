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
package com.fortify.cli.sc_dast.sensor.cli.mixin;

import com.fortify.cli.common.cli.util.EnvSuffix;
import com.fortify.cli.sc_dast.sensor.helper.SCDastSensorDescriptor;
import com.fortify.cli.sc_dast.sensor.helper.SCDastSensorHelper;

import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Parameters;

public class SCDastSensorResolverMixin {
    
    public static abstract class AbstractSSCDastSensorResolverMixin {
        public abstract String getSensorNameOrId();

        public SCDastSensorDescriptor getSensorDescriptor(UnirestInstance unirest){
            return SCDastSensorHelper.getSensorDescriptor(unirest, getSensorNameOrId());
        }
        
        public String getSensorId(UnirestInstance unirest) {
            return getSensorDescriptor(unirest).getId();
        }
    }
    
    public static class PositionalParameter extends AbstractSSCDastSensorResolverMixin {
        @EnvSuffix("SENSOR") @Parameters(index = "0", arity = "1", descriptionKey = "fcli.sc-dast.sensor.resolver.nameOrId")
        @Getter private String sensorNameOrId;
    }
}
