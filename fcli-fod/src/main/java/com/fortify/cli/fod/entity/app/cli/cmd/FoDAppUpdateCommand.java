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
package com.fortify.cli.fod.entity.app.cli.cmd;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.validation.ValidationException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fortify.cli.common.output.cli.mixin.OutputHelperMixins;
import com.fortify.cli.common.output.transform.IActionCommandResultSupplier;
import com.fortify.cli.common.output.transform.IRecordTransformer;
import com.fortify.cli.common.util.DisableTest;
import com.fortify.cli.fod.entity.app.cli.mixin.FoDAppResolverMixin;
import com.fortify.cli.fod.entity.app.cli.mixin.FoDCriticalityTypeOptions;
import com.fortify.cli.fod.entity.app.helper.FoDAppDescriptor;
import com.fortify.cli.fod.entity.app.helper.FoDAppHelper;
import com.fortify.cli.fod.entity.app.helper.FoDAppUpdateRequest;
import com.fortify.cli.fod.entity.attribute.cli.mixin.FoDAttributeUpdateOptions;
import com.fortify.cli.fod.entity.attribute.helper.FoDAttributeDescriptor;
import com.fortify.cli.fod.entity.attribute.helper.FoDAttributeHelper;
import com.fortify.cli.fod.entity.microservice.cli.mixin.FoDAppMicroserviceUpdateOptions;
import com.fortify.cli.fod.output.cli.AbstractFoDJsonNodeOutputCommand;

import io.micronaut.core.util.StringUtils;
import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;

import static com.fortify.cli.common.util.DisableTest.TestType.MULTI_OPT_PLURAL_NAME;

@Command(name = OutputHelperMixins.Update.CMD_NAME)
public class FoDAppUpdateCommand extends AbstractFoDJsonNodeOutputCommand implements IRecordTransformer, IActionCommandResultSupplier {
    @Getter @Mixin private OutputHelperMixins.Update outputHelper;
    @Mixin private FoDAppResolverMixin.PositionalParameter appResolver;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Option(names = {"--name", "-n"})
    private String applicationNameUpdate;
    @Option(names = {"--description", "-d"})
    private String descriptionUpdate;
    @DisableTest(MULTI_OPT_PLURAL_NAME)
    @Option(names = {"--notify"}, required = false, split=",")
    private ArrayList<String> notificationsUpdate;
    @Mixin
    FoDAppMicroserviceUpdateOptions.AddMicroserviceOption addMicroservices;
    @Mixin
    FoDAppMicroserviceUpdateOptions.DeleteMicroserviceOption deleteMicroservices;
    @Mixin
    FoDAppMicroserviceUpdateOptions.RenameMicroserviceOption renameMicroservices;
    @Mixin
    private FoDCriticalityTypeOptions.OptionalOption criticalityTypeUpdate;
    @Mixin
    private FoDAttributeUpdateOptions.OptionalAttrOption appAttrsUpdate;

    // TODO Method too long; separate into multiple methods.
    @Override
    public JsonNode getJsonNode(UnirestInstance unirest) {

        // current values of app being updated
        FoDAppDescriptor appDescriptor = FoDAppHelper.getAppDescriptor(unirest, appResolver.getAppNameOrId(), true);
        ArrayList<FoDAttributeDescriptor> appAttrsCurrent = appDescriptor.getAttributes();

        // new values to replace
        FoDCriticalityTypeOptions.FoDCriticalityType appCriticalityNew = criticalityTypeUpdate.getCriticalityType();
        Map<String, String> attributeUpdates = appAttrsUpdate.getAttributes();
        JsonNode jsonAttrs = objectMapper.createArrayNode();
        if (attributeUpdates != null && !attributeUpdates.isEmpty()) {
            jsonAttrs = FoDAttributeHelper.mergeAttributesNode(unirest, appAttrsCurrent, attributeUpdates);
        } else {
            jsonAttrs = FoDAttributeHelper.getAttributesNode(appAttrsCurrent);
        }
        String appEmailListNew = FoDAppHelper.getEmailList(notificationsUpdate);

        FoDAppUpdateRequest appUpdateRequest = new FoDAppUpdateRequest()
                .setApplicationName(StringUtils.isNotEmpty(applicationNameUpdate) ? applicationNameUpdate : appDescriptor.getApplicationName())
                .setApplicationDescription(StringUtils.isNotEmpty(descriptionUpdate) ? descriptionUpdate : appDescriptor.getApplicationDescription())
                .setBusinessCriticalityType(appCriticalityNew != null ? String.valueOf(appCriticalityNew) : appDescriptor.getBusinessCriticalityType())
                .setEmailList(StringUtils.isNotEmpty(appEmailListNew) ? appEmailListNew : appDescriptor.getEmailList())
                .setAttributes(jsonAttrs);

        // TODO Compiler warns about unlikely argument to contains(); warning seems correct, you likely want to use Collections.disjoint() instead
        // TODO As a best practice, statements in if-blocks should always be enclosed in curly braces
        // TODO Avoid repetitive get*()-calls; assign to local variable instead
        // TODO Avoid repetitive null-checks, potentially change mixin definitions
        //      such that the mixin itself and the collection are initialized to non-null value
        //      to avoid null-checks
        // TODO Some of the checks are performed twice; i.e. if both add and delete options specified,
        //      then we're checking twice whether they do not overlap
        if (addMicroservices != null && addMicroservices.getMicroservices() != null) {
            if (addMicroservices.getMicroservices().contains(deleteMicroservices.getMicroservices()))
                throw new ValidationException("The --add-microservice and --delete-microservice cannot both contain the same microservice");
            appUpdateRequest.setAddMicroservices(addMicroservices.getMicroservices());
        }

        if (deleteMicroservices != null && deleteMicroservices.getMicroservices() != null) {
            if (deleteMicroservices.getMicroservices().contains(addMicroservices.getMicroservices()))
                throw new ValidationException("The --add-microservice and --delete-microservice cannot both contain the same microservice");
            appUpdateRequest.setDeleteMicroservices(deleteMicroservices.getMicroservices());
        }

        if (renameMicroservices != null && renameMicroservices.getMicroservices() != null) {
            List<String> msNames = new ArrayList<>(renameMicroservices.getMicroservices().keySet());
            if (msNames.contains(addMicroservices.getMicroservices()) || msNames.contains(deleteMicroservices.getMicroservices()))
                throw new ValidationException("The --update-microservice and --add-microservice or --delete-microservice cannot both contain the same microservice");
            appUpdateRequest.setRenameMicroservices(renameMicroservices.getMicroservices());
        }

        return FoDAppHelper.updateApp(unirest, appDescriptor.getApplicationId(), appUpdateRequest).asJsonNode();
    }

    @Override
    public JsonNode transformRecord(JsonNode record) {
        return FoDAppHelper.renameFields(record);
    }

    @Override
    public String getActionCommandResult() {
        return "UPDATED";
    }

    @Override
    public boolean isSingular() {
        return true;
    }

}
