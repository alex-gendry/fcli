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
package com.fortify.cli.ssc.appversion.cli.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.common.output.cli.cmd.IJsonNodeSupplier;
import com.fortify.cli.common.output.cli.mixin.OutputHelperMixins;
import com.fortify.cli.common.output.transform.IActionCommandResultSupplier;
import com.fortify.cli.common.output.transform.IRecordTransformer;
import com.fortify.cli.ssc._common.output.cli.cmd.AbstractSSCJsonNodeOutputCommand;
import com.fortify.cli.ssc._common.rest.bulk.SSCBulkRequestBuilder;
import com.fortify.cli.ssc.appversion.cli.mixin.SSCAppAndVersionNameResolverMixin;
import com.fortify.cli.ssc.appversion.cli.mixin.SSCCopyFromAppVersionResolverMixin;
import com.fortify.cli.ssc.appversion.helper.SSCAppVersionDescriptor;
import com.fortify.cli.ssc.appversion.helper.SSCAppVersionHelper;
import com.fortify.cli.ssc.issue.helper.SSCIssueAuditBuilder;
import com.fortify.cli.ssc.issue.helper.SSCIssueAuditHelper;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = "copy-audit")
public class SSCAppVersionCopyAuditCommand extends AbstractSSCJsonNodeOutputCommand implements IJsonNodeSupplier, IRecordTransformer, IActionCommandResultSupplier {
    @Getter
    @Mixin
    private OutputHelperMixins.TableNoQuery outputHelper;
    @Mixin
    private SSCAppAndVersionNameResolverMixin.PositionalParameter sscAppAndVersionNameResolver;
    @Mixin
    private SSCCopyFromAppVersionResolverMixin.RequiredOption fromAppVersionResolver;

    private static final JsonNode copyAudit(UnirestInstance unirest, SSCAppVersionDescriptor sourceAppVersion, SSCAppVersionDescriptor targetAppVersion) {
        SSCIssueAuditHelper sourceCustomTagHelper = new SSCIssueAuditHelper(unirest, sourceAppVersion.getVersionId());

        SSCIssueAuditHelper issueAuditHelper = new SSCIssueAuditHelper(unirest, sourceAppVersion.getVersionId());
        issueAuditHelper.transposeIssues(unirest, targetAppVersion.getVersionId());

        SSCBulkRequestBuilder builder = new SSCIssueAuditBuilder(issueAuditHelper).getBulkRequestsBuilder(unirest, targetAppVersion.getVersionId());
        SSCBulkRequestBuilder.SSCBulkResponse bulkResponse = builder.execute(unirest);

        String action = "COPY_COMPLETED";
        for (JsonNode response : bulkResponse.fullBody()) {
            for (JsonNode subResponse : response.get("responses")) {
                JsonNode body = subResponse.get("body");
                if (body.has("responseCode")) {
                    if (body.get("responseCode").intValue() != 200) {
                        action = "COPY_COMPLETED_WITH_ERRORS";
                    }
                } else {
                    action = "COPY_COMPLETED_WITH_ERRORS";
                }
            }
        }

        //previousProjectVersionId,projectVersionId,processedIssues,failedIssues
        return JsonHelper.getObjectMapper().createObjectNode().put("previousProjectVersionId", sourceAppVersion.getVersionId()).put("projectVersionId", targetAppVersion.getVersionId()).put(IActionCommandResultSupplier.actionFieldName, action);
    }

    @Override
    public JsonNode getJsonNode(UnirestInstance unirest) {
        ObjectMapper mapper = new ObjectMapper();
        SSCAppVersionDescriptor targetAppVersionDescriptor = SSCAppVersionHelper.getRequiredAppVersion(unirest, sscAppAndVersionNameResolver.getAppAndVersionName(), sscAppAndVersionNameResolver.getDelimiter());
        SSCAppVersionDescriptor sourceAppVersionDescriptor = fromAppVersionResolver.getAppVersionDescriptor(unirest, sscAppAndVersionNameResolver.getDelimiter());

        return mapper.valueToTree(copyAudit(unirest, sourceAppVersionDescriptor, targetAppVersionDescriptor));

    }

    @Override
    public JsonNode transformRecord(JsonNode record) {
        return SSCAppVersionHelper.renameFields(record);
    }

    @Override
    public String getActionCommandResult() {
        return "COPY_COMPLETED";
    }

    @Override
    public boolean isSingular() {
        return true;
    }

}
