/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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

package com.fortify.cli.fod.entity.scan_sast.cli.cmd;

import java.io.File;
import java.util.Properties;

import javax.validation.ValidationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.output.transform.IActionCommandResultSupplier;
import com.fortify.cli.common.output.transform.IRecordTransformer;
import com.fortify.cli.common.progress.cli.mixin.ProgressHelperFactoryMixin;
import com.fortify.cli.common.progress.helper.IProgressHelperI18n;
import com.fortify.cli.common.util.FcliBuildPropertiesHelper;
import com.fortify.cli.fod.entity.release.cli.mixin.FoDAppMicroserviceRelResolverMixin;
import com.fortify.cli.fod.entity.scan.cli.mixin.FoDEntitlementPreferenceTypeOptions;
import com.fortify.cli.fod.entity.scan.cli.mixin.FoDInProgressScanActionTypeOptions;
import com.fortify.cli.fod.entity.scan.cli.mixin.FoDRemediationScanPreferenceTypeOptions;
import com.fortify.cli.fod.entity.scan.cli.mixin.FoDScanFormatOptions;
import com.fortify.cli.fod.entity.scan.helper.FoDAssessmentTypeDescriptor;
import com.fortify.cli.fod.entity.scan.helper.FoDScanHelper;
import com.fortify.cli.fod.entity.scan_sast.helper.FoDSastScanHelper;
import com.fortify.cli.fod.entity.scan_sast.helper.FoDSastScanSetupDescriptor;
import com.fortify.cli.fod.entity.scan_sast.helper.FoDStartSastScanRequest;
import com.fortify.cli.fod.output.cli.AbstractFoDJsonNodeOutputCommand;
import com.fortify.cli.fod.output.mixin.FoDOutputHelperMixins;
import com.fortify.cli.fod.util.FoDConstants;
import com.fortify.cli.fod.util.FoDEnums;

import io.micronaut.core.util.StringUtils;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

@Command(name = FoDOutputHelperMixins.StartSast.CMD_NAME)
public class FoDSastScanStartCommand extends AbstractFoDJsonNodeOutputCommand implements IRecordTransformer, IActionCommandResultSupplier {
    @Getter @Mixin private FoDOutputHelperMixins.StartSast outputHelper;
    @Mixin
    private FoDAppMicroserviceRelResolverMixin.PositionalParameter appMicroserviceRelResolver;
    @Option(names = {"--entitlement-id"})
    private Integer entitlementId;
    @Option(names = {"--purchase-entitlement"})
    private final Boolean purchaseEntitlement = false;
    @Option(names = {"--notes"})
    private String notes;
    @Option(names = {"--chunk-size"})
    private int chunkSize = FoDConstants.DEFAULT_CHUNK_SIZE;
    @CommandLine.Option(names = {"-f", "--file"}, required = true)
    private File scanFile;

    @Mixin
    private FoDEntitlementPreferenceTypeOptions.OptionalOption entitlementType;
    @Mixin
    private FoDRemediationScanPreferenceTypeOptions.OptionalOption remediationScanType;
    @Mixin
    private FoDInProgressScanActionTypeOptions.OptionalOption inProgressScanActionType;

    @Mixin private ProgressHelperFactoryMixin progressHelperFactory;

    // TODO: refactor use of FoDEnums and use @JsonValues as per: https://github.com/fortify/fcli/issues/279
    @Override
    public JsonNode getJsonNode(UnirestInstance unirest) {
        try ( var progressHelper = progressHelperFactory.createProgressHelper() ) {
            Properties fcliProperties = FcliBuildPropertiesHelper.getBuildProperties();
            String relId = appMicroserviceRelResolver.getAppMicroserviceRelId(unirest);
    
            // get current setup and check if its valid
            FoDSastScanSetupDescriptor currentSetup = FoDSastScanHelper.getSetupDescriptor(unirest, relId);
            if (currentSetup.getTechnologyStack() == null || StringUtils.isEmpty(currentSetup.getTechnologyStack())) {
                throw new ValidationException("The static scan configuration for release with id '" + relId +
                        "' has not been setup correctly - 'Technology Stack/Language Level' is missing or empty.");
            }
    
            // get entitlement to use
            FoDAssessmentTypeDescriptor entitlementToUse = getEntitlementToUse(unirest, progressHelper, relId, currentSetup);
    
            FoDStartSastScanRequest startScanRequest = new FoDStartSastScanRequest()
                    .setPurchaseEntitlement(purchaseEntitlement)
                    .setInProgressScanActionType(inProgressScanActionType.getInProgressScanActionType() != null ?
                            inProgressScanActionType.getInProgressScanActionType().name() : FoDEnums.InProgressScanActionType.Queue.name())
                    .setScanMethodType("Other")
                    .setNotes(notes != null && !notes.isEmpty() ? notes : "")
                    .setScanTool(fcliProperties.getProperty("projectName", "fcli"))
                    .setScanToolVersion(fcliProperties.getProperty("projectVersion", "unknown"));
    
            if (entitlementId != null && entitlementId > 0) {
                startScanRequest.setEntitlementId(entitlementToUse.getEntitlementId());
            } else if (entitlementType.getEntitlementPreferenceType() != null) {
                startScanRequest.setEntitlementPreferenceType(entitlementType.getEntitlementPreferenceType().name());
            } else {
                startScanRequest.setEntitlementPreferenceType(String.valueOf(FoDEnums.EntitlementPreferenceType.SubscriptionFirstThenSingleScan));
            }
    
            return FoDSastScanHelper.startScan(unirest, relId, startScanRequest, scanFile, chunkSize).asJsonNode();
        }
    }

    @Override
    public JsonNode transformRecord(JsonNode record) {
        return FoDScanHelper.renameFields(record);
    }

    @Override
    public String getActionCommandResult() {
        return "STARTED";
    }

    @Override
    public boolean isSingular() {
        return true;
    }

    private FoDAssessmentTypeDescriptor getEntitlementToUse(UnirestInstance unirest, IProgressHelperI18n progressHelper, String relId, FoDSastScanSetupDescriptor currentSetup) {
        FoDAssessmentTypeDescriptor entitlementToUse = new FoDAssessmentTypeDescriptor();

        /**
         * Logic for finding/using "entitlement" and "remediation" scanning is as follows:
         *  - if "entitlement id" is specified directly then use it
         *  - if "remediation" scan specified make sure it is valid and available
         *  - if an "entitlement type" (Single/Subscription) then pass to the API to use
         */
        if (entitlementId != null && entitlementId > 0) {
            entitlementToUse.setEntitlementId(entitlementId);
        }
        if (remediationScanType.getRemediationScanPreferenceType() != null && (remediationScanType.getRemediationScanPreferenceType() == FoDEnums.RemediationScanPreferenceType.RemediationScanOnly)) {
            // if requesting a remediation scan make we have one available
            FoDSastScanHelper.validateRemediationEntitlement(unirest, progressHelper, relId,
                    currentSetup.getEntitlementId(), FoDScanFormatOptions.FoDScanType.Static).getEntitlementId();
        }
        return entitlementToUse;
    }
}
