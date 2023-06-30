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
package com.fortify.cli.sc_sast.session.cli.mixin;

import com.fortify.cli.common.rest.unirest.config.IUserCredentialsConfig;

import lombok.Getter;
import picocli.CommandLine.Option;

public class SCSastUserCredentialOptions implements IUserCredentialsConfig {
    @Option(names = {"--ssc-user", "-u"}, required = true)
    @Getter private String user;
    
    @Option(names = {"--ssc-password", "-p"}, interactive = true, echo = false, arity = "0..1", required = true)
    @Getter private char[] password;
}
