/*******************************************************************************
 * (c) Copyright 2021 Micro Focus or one of its affiliates
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.cli.sc_dast.session.helper;

import java.time.OffsetDateTime;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.common.rest.unirest.config.IUrlConfig;
import com.fortify.cli.common.rest.unirest.config.IUserCredentialsConfig;
import com.fortify.cli.common.rest.unirest.config.UrlConfig;
import com.fortify.cli.common.session.helper.AbstractSessionDescriptor;
import com.fortify.cli.common.session.helper.SessionSummary;
import com.fortify.cli.common.util.DateTimePeriodHelper;
import com.fortify.cli.common.util.DateTimePeriodHelper.Period;
import com.fortify.cli.common.util.StringUtils;
import com.fortify.cli.sc_dast.util.SCDastConstants;
import com.fortify.cli.ssc.entity.token.helper.SSCTokenCreateRequest;
import com.fortify.cli.ssc.entity.token.helper.SSCTokenCreateResponse;
import com.fortify.cli.ssc.entity.token.helper.SSCTokenCreateResponse.SSCTokenData;
import com.fortify.cli.ssc.entity.token.helper.SSCTokenHelper;
import com.fortify.cli.ssc.session.helper.ISSCCredentialsConfig;
import com.fortify.cli.ssc.session.helper.ISSCUserCredentialsConfig;

import io.micronaut.core.annotation.ReflectiveAccess;
import kong.unirest.UnirestInstance;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data @EqualsAndHashCode(callSuper = true)  @ReflectiveAccess @JsonIgnoreProperties(ignoreUnknown = true)
public class SCDastSessionDescriptor extends AbstractSessionDescriptor {
    @JsonDeserialize(as = UrlConfig.class) private IUrlConfig sscUrlConfig;
    @JsonDeserialize(as = UrlConfig.class) private IUrlConfig scDastUrlConfig;
    private char[] predefinedToken;
    private SSCTokenCreateResponse cachedTokenResponse;
    
    protected SCDastSessionDescriptor() {}
    
    public SCDastSessionDescriptor(IUrlConfig sscUrlConfig, ISSCCredentialsConfig credentialsConfig) {
        this(sscUrlConfig, null, credentialsConfig);
    }
    
    public SCDastSessionDescriptor(IUrlConfig sscUrlConfig, IUrlConfig scDastUrlConfig, ISSCCredentialsConfig credentialsConfig) {
        this.sscUrlConfig = sscUrlConfig;
        this.predefinedToken = credentialsConfig.getPredefinedToken();
        this.cachedTokenResponse = generateToken(sscUrlConfig, credentialsConfig);
        char[] activeToken = getActiveToken();
        this.scDastUrlConfig = activeToken==null ? null : buildScDastUrlConfig(sscUrlConfig, scDastUrlConfig, activeToken);
    }

    @JsonIgnore
    public void logout(IUserCredentialsConfig userCredentialsConfig) {
        if ( cachedTokenResponse!=null && userCredentialsConfig!=null ) {
            SSCTokenHelper.deleteTokensById(getSscUrlConfig(), userCredentialsConfig, getTokenId());
        }
    }
    
    @Override @JsonIgnore
    public String getUrlDescriptor() {
        return String.format("SSC:     %s\nSC-DAST: %s", 
                sscUrlConfig==null || sscUrlConfig.getUrl()==null ? "unknown" : sscUrlConfig.getUrl(),
                scDastUrlConfig==null || scDastUrlConfig.getUrl()==null ? "unknown" : scDastUrlConfig.getUrl());
    }

    @JsonIgnore 
    public final char[] getActiveToken() {
        if ( hasActiveCachedTokenResponse() ) {
            return getCachedTokenResponseData().getToken();
        } else {
            return predefinedToken;
        }
    }
    
    @JsonIgnore
    public final boolean hasActiveCachedTokenResponse() {
        return getCachedTokenResponseData()!=null && getCachedTokenResponseData().getTerminalDate().after(new Date()); 
    }
    
    @JsonIgnore
    public Date getExpiryDate() {
        Date sessionExpiryDate = SessionSummary.EXPIRES_UNKNOWN;
        if ( getCachedTokenTerminalDate()!=null ) {
            sessionExpiryDate = getCachedTokenTerminalDate();
        }
        return sessionExpiryDate;
    }
    
    @JsonIgnore @Override
    public String getType() {
        return SCDastSessionHelper.instance().getType();
    }
    
    @JsonIgnore
    protected SSCTokenCreateResponse generateToken(IUrlConfig urlConfig, ISSCCredentialsConfig credentialsConfig) {
        if ( credentialsConfig.getPredefinedToken()==null ) {
            ISSCUserCredentialsConfig uc = credentialsConfig.getUserCredentialsConfig();
            if ( uc!=null && StringUtils.isNotBlank(uc.getUser()) && uc.getPassword()!=null ) {
                SSCTokenCreateRequest tokenCreateRequest = SSCTokenCreateRequest.builder()
                        .description("Auto-generated by fcli session login command")
                        .terminalDate(getExpiresAt(uc.getExpiresAt())) 
                        .type("CIToken")
                        .build();
                return SSCTokenHelper.createToken(urlConfig, uc, tokenCreateRequest, SSCTokenCreateResponse.class);
            }
        }
        return null;
    }
    
    private OffsetDateTime getExpiresAt(OffsetDateTime expiresAt) {
        return expiresAt!=null 
            ? expiresAt 
            : DateTimePeriodHelper.byRange(Period.MINUTES, Period.DAYS).getCurrentOffsetDateTimePlusPeriod(SCDastConstants.DEFAULT_TOKEN_EXPIRY);
    }

    @JsonIgnore 
    private final String getTokenId() {
        if ( hasActiveCachedTokenResponse() ) {
            return getCachedTokenResponseData().getId();
        } else {
            return null;
        }
    }
    
    @JsonIgnore
    private Date getCachedTokenTerminalDate() {
        return getCachedTokenResponseData()==null ? null : getCachedTokenResponseData().getTerminalDate();
    }
    
    @JsonIgnore
    private SSCTokenData getCachedTokenResponseData() {
        return cachedTokenResponse==null || cachedTokenResponse.getData()==null 
                ? null
                : cachedTokenResponse.getData();
    }
    
    private static final IUrlConfig buildScDastUrlConfig(IUrlConfig sscUrlConfig, IUrlConfig scDastUrlConfig, char[] activeToken) {
        String scDastUrl = scDastUrlConfig!=null && StringUtils.isNotBlank(scDastUrlConfig.getUrl())
                ? scDastUrlConfig.getUrl()
                : getScDastUrl(sscUrlConfig, activeToken);
        UrlConfig.UrlConfigBuilder builder = UrlConfig.builderFrom(sscUrlConfig, scDastUrlConfig);
        builder.url(scDastUrl);
        return builder.build();
    }

    private static String getScDastUrl(IUrlConfig sscUrlConfig, char[] activeToken) {
        return SSCTokenHelper.run(sscUrlConfig, activeToken, SCDastSessionDescriptor::getScDastUrl);
    }

    private static final String getScDastUrl(UnirestInstance unirest) {
        ArrayNode properties = getScDastConfigurationProperties(unirest);
        checkScDastIsEnabled(properties);
        String scDastUrl = getScDastUrlFromProperties(properties);
        return normalizeScDastUrl(scDastUrl);
    }
    
    private static final ArrayNode getScDastConfigurationProperties(UnirestInstance sscUnirest) {
        ObjectNode configData = sscUnirest.get("/api/v1/configuration?group=edast")
                .asObject(ObjectNode.class)
                .getBody(); 
        
        return JsonHelper.evaluateSpelExpression(configData, "data.properties", ArrayNode.class);
    }
    
    private static final void checkScDastIsEnabled(ArrayNode properties) {
        boolean scDastEnabled = JsonHelper.evaluateSpelExpression(properties, "^[name=='edast.enabled']?.value=='true'", Boolean.class);
        if (!scDastEnabled) {
            throw new IllegalStateException("ScanCentral DAST must be enabled in SSC");
        }
    }
    
    private static final String getScDastUrlFromProperties(ArrayNode properties) {
        String scDastUrl = JsonHelper.evaluateSpelExpression(properties, "^[name=='edast.server.url']?.value", String.class);
        if ( scDastUrl.isEmpty() ) {
            throw new IllegalStateException("SSC returns an empty ScanCentral DAST URL");
        }
        return scDastUrl;
    }
    
    private static final String normalizeScDastUrl(String scDastUrl) {
        // We remove '/api' and any trailing slashes from the URL as most users will specify relative URL's starting with /api/v2/...
        return scDastUrl.replaceAll("/api/?$","").replaceAll("/+$", "");
    }
}
