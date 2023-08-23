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
package com.fortify.cli.fod.entitlement.cli.cmd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.output.cli.mixin.OutputHelperMixins;
import com.fortify.cli.common.output.transform.IRecordTransformer;
import com.fortify.cli.fod._common.output.cli.AbstractFoDJsonNodeOutputCommand;
import com.fortify.cli.fod.entitlement.cli.mixin.FoDEntitlementResolverMixin;
import com.fortify.cli.fod.entitlement.helper.FoDEntitlementHelper;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(name = OutputHelperMixins.Get.CMD_NAME)
public class FoDEntitlementGetCommand extends AbstractFoDJsonNodeOutputCommand implements IRecordTransformer {
    @Getter @Mixin private OutputHelperMixins.Get outputHelper;

    @Mixin private FoDEntitlementResolverMixin.PositionalParameter entitlementId;

    @Override
    public JsonNode getJsonNode(UnirestInstance unirest) {
        return FoDEntitlementHelper.getEntitlementDescriptor(unirest,
                entitlementId.getEntitlementId(), false).asJsonNode();
    }

    @Override
    public JsonNode transformRecord(JsonNode record) {
        return FoDEntitlementHelper.renameFields(record);
    }

    @Override
    public boolean isSingular() {
        return true;
    }

}
