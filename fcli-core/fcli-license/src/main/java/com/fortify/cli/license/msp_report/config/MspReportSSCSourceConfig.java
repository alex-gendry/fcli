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
package com.fortify.cli.license.msp_report.config;

import com.formkiq.graalvm.annotations.Reflectable;
import com.fortify.cli.common.report.generator.IReportResultsGenerator;
import com.fortify.cli.common.rest.unirest.config.IUrlConfig;
import com.fortify.cli.license.msp_report.collector.MspReportResultsCollector;
import com.fortify.cli.license.msp_report.generator.ssc.MspReportSSCResultsGenerator;

import kong.unirest.Config;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * This SSC-specific configuration class defines an SSC source configuration,
 * holding SSC URL and credentials, and providing an {@link MspReportSSCResultsGenerator} 
 * instance based on this configuration.
 * 
 * @author rsenden
 */
@Reflectable @NoArgsConstructor 
@Data
public class MspReportSSCSourceConfig implements IMspReportSourceConfig, IUrlConfig {
    private String baseUrl;
    private String tokenExpression;
    private int connectTimeoutInMillis = Config.DEFAULT_CONNECT_TIMEOUT;
    private int socketTimeoutInMillis = Config.DEFAULT_SOCKET_TIMEOUT;
    private Boolean insecureModeEnabled;
    
    @Override
    public String getUrl() {
        return baseUrl;
    }
    
    public boolean hasUrlConfig() {
        return baseUrl!=null;
    }
    
    @Override
    public IReportResultsGenerator generator(MspReportResultsCollector resultsCollector) {
        return new MspReportSSCResultsGenerator(this, resultsCollector);
    }
}
