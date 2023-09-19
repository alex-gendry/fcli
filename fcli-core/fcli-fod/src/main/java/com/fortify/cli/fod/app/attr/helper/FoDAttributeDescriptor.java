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
package com.fortify.cli.fod.app.attr.helper;

import java.util.ArrayList;

import com.formkiq.graalvm.annotations.Reflectable;
import com.fortify.cli.common.json.JsonNodeHolder;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data @EqualsAndHashCode(callSuper = true)
@Reflectable @NoArgsConstructor
public class FoDAttributeDescriptor extends JsonNodeHolder {
    private Integer id;
    private String name;
    private Integer attributeTypeId;
    private String attributeType;
    private Integer attributeDataTypeId;
    private String attributeDataType;
    private Boolean isRequired;
    private Boolean isRestricted;
    private ArrayList<FoDPickListDescriptor> picklistValues;
    private String value;
}
