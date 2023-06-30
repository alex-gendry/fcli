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
package com.fortify.cli.fod.entity.app.helper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fortify.cli.common.json.JsonNodeHolder;
import com.fortify.cli.fod.entity.app.attr.cli.helper.FoDAttributeDescriptor;

import io.micronaut.core.annotation.ReflectiveAccess;
import lombok.Data;
import lombok.EqualsAndHashCode;

@ReflectiveAccess
@Data
@EqualsAndHashCode(callSuper = true)
public class FoDAppDescriptor extends JsonNodeHolder {
    private Integer applicationId;
    private String applicationName;
    private String applicationDescription;
    private String businessCriticalityType;
    private ArrayList<FoDAttributeDescriptor> attributes;
    private String emailList;
    private Integer releaseId;
    private Integer microserviceId;

    public Map<Integer, String> attributesAsMap() {
        Map<Integer, String> attrMap = new HashMap<>();
        for (FoDAttributeDescriptor attr : attributes) {
            attrMap.put(attr.getId(), attr.getValue());
        }
        return  attrMap;
    }

}
