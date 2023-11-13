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
package com.fortify.cli.ssc.issue.helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.common.util.StringUtils;
import com.fortify.cli.ssc._common.rest.SSCUrls;
import com.fortify.cli.ssc._common.rest.bulk.SSCBulkRequestBuilder;
import com.fortify.cli.ssc.customtag.domain.SSCCustomTagValueType;
import com.fortify.cli.ssc.customtag.helper.SSCCustomTagDescriptor;
import kong.unirest.HttpRequest;
import kong.unirest.UnirestInstance;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SSCIssueAuditBuilder {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final SSCIssueAuditHelper issueAuditHelper;
    private final Map<String, String> customTags = new LinkedHashMap<>();
    private ArrayNode preparedCustomTagUpdateData = null;

    public SSCIssueAuditBuilder(UnirestInstance unirest, String appVersionId) {
        this(new SSCIssueAuditHelper(unirest, appVersionId));
    }

    public SSCIssueAuditBuilder(SSCIssueAuditHelper issueAuditHelper) {
        this.issueAuditHelper = issueAuditHelper;
    }

    public final HttpRequest<?> buildRequest(UnirestInstance unirest, Number valueHash, String projectVersionId) {
        ObjectNode body = objectMapper.createObjectNode();
        body.set("issues", this.issueAuditHelper.getIssuesByValueHash(valueHash));
        body.set("customTagAudit", this.issueAuditHelper.getValuesByHash(valueHash));

        return unirest
                .post(SSCUrls.PROJECT_VERSION_ISSUES_ACTION_AUDIT(projectVersionId))
                .body(body);
    }

    public final SSCBulkRequestBuilder getBulkRequestsBuilder(UnirestInstance unirest, String appVersionId) {
        SSCBulkRequestBuilder builder = new SSCBulkRequestBuilder();

        for (Number valueHash : this.issueAuditHelper.getValueHashes()) {
            builder.request(valueHash.toString(), this.buildRequest(unirest, valueHash, appVersionId));
        }

        return builder;
    }
}
