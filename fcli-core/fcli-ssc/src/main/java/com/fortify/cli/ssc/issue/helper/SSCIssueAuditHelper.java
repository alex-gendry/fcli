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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.ssc._common.rest.SSCUrls;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.*;

public final class SSCIssueAuditHelper {
    private final UnirestInstance unirest;
    private final Map<String, ObjectNode> issueNodesByInstanceId = new LinkedHashMap<>();
    private final Map<Number, ArrayNode> issueNodesByValuesHash = new LinkedHashMap<>();
    private final Map<Number, JsonNode> valuesByHash = new LinkedHashMap<>();
    private final HashSet<String> readOnlyCustomTagGuids = new HashSet<String>();

    @Getter
    private final ArrayNode issueNodes = JsonHelper.getObjectMapper().createArrayNode();

    public SSCIssueAuditHelper(UnirestInstance unirest, String applicationVersionId) {
        this.unirest = unirest;
        this.setReadOnlyGuids(unirest, applicationVersionId);

        JsonNode data = unirest.get(SSCUrls.PROJECT_VERSION_ISSUES(applicationVersionId))
                .queryString("fields", "id,issueInstanceId,projectVersionId,revision")
                .queryString("embed", "auditValues")
                .asObject(JsonNode.class).getBody()
                .get("data");
        data.forEach(this::processIssue);
    }

    private final void setReadOnlyGuids(UnirestInstance unirest, String applicationVersionId){
        JsonNode readOnlyCustomTags = unirest.get(SSCUrls.PROJECT_VERSION_CUSTOM_TAGS(applicationVersionId))
                .queryString("fields","guid,restrictionType")
                .queryString("q","restrictionType:READONLY")
                .queryString("includeall",true)
                .asObject(JsonNode.class).getBody()
                .get("data");

        for(JsonNode tag: readOnlyCustomTags){
            if(tag.get("restrictionType").textValue() != null) {
                if(tag.get("restrictionType").textValue().equals("READONLY")){
                    this.readOnlyCustomTagGuids.add(tag.get("guid").textValue());
                }
            }
        }
    }

    private final void excludeReadOnlyCustomTags(ArrayNode auditValues) {
        Iterator<JsonNode> iterator = auditValues.iterator();
        while(iterator.hasNext()) {
            JsonNode value = iterator.next();

            if(this.readOnlyCustomTagGuids.contains(value.get("customTagGuid").textValue())){
                iterator.remove();
            }
        }
    }

    private final void processIssue(JsonNode issue) {
        var newIssue = (ObjectNode) issue.deepCopy();
        ArrayNode auditValues =(ArrayNode)  newIssue.get("_embed").get("auditValues");
        excludeReadOnlyCustomTags(auditValues);
        newIssue.remove("_embed");
        int valueHash = auditValues.hashCode();
        this.issueNodesByValuesHash.computeIfAbsent(valueHash, (h) -> JsonHelper.getObjectMapper().createArrayNode()).add(newIssue);
        this.valuesByHash.computeIfAbsent(valueHash, (h) -> auditValues);

        issueNodesByInstanceId.put(newIssue.get("issueInstanceId").textValue(), newIssue);
    }


    public final ArrayNode transposeIssues(UnirestInstance unirest, String targetAppVersionId) {
        JsonNode issues = unirest.get(SSCUrls.PROJECT_VERSION_ISSUES(targetAppVersionId))
                .queryString("fields", "id,issueInstanceId,revision")
                .asObject(JsonNode.class).getBody()
                .get("data");

        ArrayNode transposedIssues = JsonHelper.getObjectMapper().createArrayNode();

        for (JsonNode targetIssue : issues) {
            var instanceId = targetIssue.get("issueInstanceId").textValue();
            if (this.issueNodesByInstanceId.containsKey(instanceId)) {
                this.issueNodesByInstanceId.get(instanceId).put("id", targetIssue.get("id").intValue());
                this.issueNodesByInstanceId.get(instanceId).put("revision", targetIssue.get("revision").intValue());
            }
        }

        return transposedIssues;
    }

    public final Set<Number> getValueHashes() {
        return this.valuesByHash.keySet();
    }

    public final ArrayNode getIssuesByValueHash(Number hash) {
        return this.issueNodesByValuesHash.get(hash);
    }

    public final JsonNode getValuesByHash(Number hash) {
        return this.valuesByHash.get(hash);
    }
}
