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
package com.fortify.cli.ssc.issue.cli.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.common.output.cli.cmd.IJsonNodeSupplier;
import com.fortify.cli.common.output.cli.mixin.OutputHelperMixins;
import com.fortify.cli.common.output.transform.IActionCommandResultSupplier;
import com.fortify.cli.common.output.transform.IRecordTransformer;
import com.fortify.cli.ssc._common.output.cli.cmd.AbstractSSCJsonNodeOutputCommand;
import com.fortify.cli.ssc._common.output.cli.cmd.AbstractSSCOutputCommand;
import com.fortify.cli.ssc._common.rest.SSCUrls;
import com.fortify.cli.ssc.appversion.cli.mixin.SSCAppVersionResolverMixin;
import com.fortify.cli.ssc.customtag.cli.mixin.SSCCustomTagUpdateMixin;
import com.fortify.cli.ssc.customtag.helper.SSCCustomTagUpdateBuilder;
import com.fortify.cli.ssc.issue.cli.mixin.SSCIssueFilterSetResolverMixin;
import com.fortify.cli.ssc.issue.helper.SSCIssueFilterHelper;
import com.fortify.cli.ssc.issue.helper.SSCIssueFilterSetDescriptor;
import kong.unirest.GetRequest;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.Map;

@Command(name = "tag")
public class SSCIssueTagCommand extends AbstractSSCOutputCommand implements IJsonNodeSupplier, IActionCommandResultSupplier {
    @Getter @Mixin private OutputHelperMixins.TableWithQuery outputHelper; 
    @Mixin private SSCAppVersionResolverMixin.RequiredOption parentResolver;
    @Mixin private SSCCustomTagUpdateMixin.RequiredCustomTagOption customTagsMixin;
    @Mixin private SSCIssueFilterSetResolverMixin.FilterSetOption filterSetResolver;
    @Option(names="--filter", required=false) private String filter;
    @Option(names="--issues-query", required=false) private String query;

    // TODO Include options for includeRemoved/Hidden/Suppressed?

    @Override
    public JsonNode getJsonNode() {
        var unirest = getUnirestInstance();
        String appVersionId = parentResolver.getAppVersionId(unirest);
        SSCIssueFilterSetDescriptor filterSetDescriptor = filterSetResolver.getFilterSetDescriptor(unirest, appVersionId);
        GetRequest request = unirest.get(SSCUrls.PROJECT_VERSION_ISSUES(appVersionId))
                .queryString("limit","-1")
                .queryString("qm", "issues")
                .queryString("fields", "id,revision");
        if ( filterSetDescriptor!=null ) {
            request.queryString("filterset", filterSetDescriptor.getGuid());
        }
        if ( filter!=null ) {
            request.queryString("filter", new SSCIssueFilterHelper(unirest, appVersionId).getFilter(filter));
        }
        if ( query!=null ) {
            request.queryString("q",query);
        }

        JsonNode issuesResponse = request.asObject(ObjectNode.class).getBody();
        if(issuesResponse.get("count").intValue() > 0){
            JsonNode issuesData = issuesResponse.get("data");
            Map<String, String> customTags = customTagsMixin.getCustomTags();
            SSCCustomTagUpdateBuilder customTagHelper = new SSCCustomTagUpdateBuilder(unirest,appVersionId).add(customTags);
            ArrayNode customTagsUpdate = customTagHelper.buildUpdateBody();
            ObjectNode body = JsonHelper.getObjectMapper().createObjectNode()
                    .set("issues", issuesData);
            body.set("issues", issuesData);
            body.set("customTagAudit", customTagsUpdate);

            return unirest.post(SSCUrls.PROJECT_VERSION_ISSUES_ACTION_AUDIT(appVersionId)).body(body)
                    .asObject(JsonNode.class).getBody().get("data");
        }
        return issuesResponse;
    }
    
    @Override
    public boolean isSingular() {
        return false;
    }



    @Override
    public String getActionCommandResult() {
        return "TAG_SUCCESSFUL";
    }

}
