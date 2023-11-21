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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.common.output.cli.mixin.OutputHelperMixins;
import com.fortify.cli.common.output.query.QueryExpression;
import com.fortify.cli.common.output.query.QueryExpressionTypeConverter;
import com.fortify.cli.common.output.transform.IActionCommandResultSupplier;
import com.fortify.cli.common.output.transform.IRecordTransformer;
import com.fortify.cli.common.util.StringUtils;
import com.fortify.cli.ssc._common.output.cli.cmd.AbstractSSCJsonNodeOutputCommand;
import com.fortify.cli.ssc._common.rest.SSCUrls;
import com.fortify.cli.ssc._common.rest.bulk.SSCBulkRequestBuilder;
import com.fortify.cli.ssc._common.rest.bulk.SSCBulkRequestBuilder.SSCBulkResponse;
import com.fortify.cli.ssc.access_control.cli.mixin.SSCAppVersionUserMixin;
import com.fortify.cli.ssc.access_control.helper.SSCAppVersionUserUpdateBuilder;
import com.fortify.cli.ssc.appversion.cli.mixin.SSCAppVersionResolverMixin;
import com.fortify.cli.ssc.appversion.helper.SSCAppVersionDescriptor;
import com.fortify.cli.ssc.appversion.helper.SSCAppVersionHelper;
import com.fortify.cli.ssc.attribute.cli.mixin.SSCAttributeUpdateMixin;
import com.fortify.cli.ssc.attribute.helper.SSCAttributeUpdateBuilder;
import com.fortify.cli.ssc.customtag.cli.mixin.SSCCustomTagUpdateMixin;
import com.fortify.cli.ssc.customtag.helper.SSCCustomTagUpdateBuilder;
import com.fortify.cli.ssc.issue.cli.mixin.SSCIssueFilterSetResolverMixin;
import com.fortify.cli.ssc.issue.cli.mixin.SSCIssueTemplateResolverMixin;
import com.fortify.cli.ssc.issue.helper.SSCIssueFilterHelper;
import com.fortify.cli.ssc.issue.helper.SSCIssueFilterSetDescriptor;
import com.fortify.cli.ssc.issue.helper.SSCIssueTemplateDescriptor;
import kong.unirest.GetRequest;
import kong.unirest.HttpRequest;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import lombok.Setter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Command(name = OutputHelperMixins.Update.CMD_NAME)
public class SSCIssueUpdateCommand extends AbstractSSCJsonNodeOutputCommand implements IRecordTransformer, IActionCommandResultSupplier {
    @Getter @Setter
    private String actionCommandResult = "UPDATED";
    @Getter @Mixin private OutputHelperMixins.Update outputHelper;
    @Mixin private SSCAppVersionResolverMixin.RequiredOption appVersionResolver;
    @Mixin private SSCCustomTagUpdateMixin.RequiredCustomTagOption customTagsMixin;
    @Mixin private SSCIssueFilterSetResolverMixin.FilterSetOption filterSetResolver;
    @Option(names="--filter", required=false) private String filter;

    @Option(names = {"--query"}, converter = QueryExpressionTypeConverter.class, paramLabel = "<SpEL expression>")
    @Getter private QueryExpression query;
    
    @Override
    public JsonNode getJsonNode(UnirestInstance unirest) {
        SSCAppVersionDescriptor appVersionDescriptor = appVersionResolver.getAppVersionDescriptor(unirest);

        ArrayNode selectedIssues = getFilteredIssues(unirest, appVersionDescriptor.getVersionId());

        if(selectedIssues.size() > 0){
            SSCBulkResponse bulkResponse = new SSCBulkRequestBuilder()
                    .request("issueTag", getIssueTagRequest(unirest, appVersionDescriptor.getVersionId(), selectedIssues))
//            .request("attrUpdate", getAttrUpdateRequest(unirest, descriptor))
//            .request("userUpdate", getUserUpdateRequest(unirest, descriptor))
//            .request("updatedVersion", unirest.get(SSCUrls.PROJECT_VERSION(descriptor.getVersionId())))
                    .execute(unirest);
        } else {
            setActionCommandResult("NO_ISSUE_FOUND");
        }

        return selectedIssues;
    }
    
    @Override
    public JsonNode transformRecord(JsonNode input) {
    	return SSCAppVersionHelper.renameFields(input);
    }
    
    @Override
    public boolean isSingular() {
        return true;
    }

    private final ArrayNode getFilteredIssues(UnirestInstance unirest, String appVersionId){
        SSCIssueFilterSetDescriptor filterSetDescriptor = filterSetResolver.getFilterSetDescriptor(unirest, appVersionId);
        GetRequest request = unirest.get(SSCUrls.PROJECT_VERSION_ISSUES(appVersionId))
                .queryString("limit","-1")
                .queryString("qm", "issues");
        if ( filterSetDescriptor!=null ) {
            request.queryString("filterset", filterSetDescriptor.getGuid());
        }
        if ( filter!=null ) {
            request.queryString("filter", new SSCIssueFilterHelper(unirest, appVersionId).getFilter(filter));
        }
        ArrayNode issues = (ArrayNode) request.asObject(ObjectNode.class).getBody().get("data");

        if ( query!=null ) {
            Iterator<JsonNode> iter = issues.elements();
            while(iter.hasNext()){
                JsonNode issue = iter.next();

                if(!query.matches(issue)){
                    iter.remove();
                }
            }
        }

        return issues;
    }

    private final HttpRequest<?> getIssueTagRequest(UnirestInstance unirest, String appVersionId, ArrayNode selectedIssues) {
        Map<String, String> customTags = customTagsMixin.getCustomTags();
        SSCCustomTagUpdateBuilder customTagHelper = new SSCCustomTagUpdateBuilder(unirest,appVersionId).add(customTags);
        ArrayNode customTagsUpdate = customTagHelper.buildUpdateBody();
        ObjectNode body = JsonHelper.getObjectMapper().createObjectNode();
        body.set("issues",  selectedIssues);
        body.set("customTagAudit", customTagsUpdate);

        unirest.post(SSCUrls.PROJECT_VERSION_ISSUES_ACTION_AUDIT(appVersionId)).body(body)
                .asObject(JsonNode.class).getBody().get("data");

        return unirest.post(SSCUrls.PROJECT_VERSION_ISSUES_ACTION_AUDIT(appVersionId)).body(body);
    }
}
