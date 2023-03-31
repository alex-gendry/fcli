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
package com.fortify.cli.fod.entity.user.cli.cmd;

import java.util.ArrayList;

import com.fasterxml.jackson.databind.JsonNode;
import com.fortify.cli.common.output.cli.mixin.OutputHelperMixins;
import com.fortify.cli.common.output.transform.IActionCommandResultSupplier;
import com.fortify.cli.common.output.transform.IRecordTransformer;
import com.fortify.cli.fod.entity.app.helper.FoDAppHelper;
import com.fortify.cli.fod.entity.user.helper.FoDUserCreateRequest;
import com.fortify.cli.fod.entity.user.helper.FoDUserHelper;
import com.fortify.cli.fod.entity.user_group.helper.FoDUserGroupHelper;
import com.fortify.cli.fod.output.cli.AbstractFoDJsonNodeOutputCommand;

import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = OutputHelperMixins.Create.CMD_NAME)
public class FoDUserCreateCommand extends AbstractFoDJsonNodeOutputCommand implements IRecordTransformer, IActionCommandResultSupplier {
    @Getter @Mixin private OutputHelperMixins.Create outputHelper;

    @Parameters(index = "0", descriptionKey = "user-name")
    private String userName;
    @Option(names = {"--email"}, required = true)
    private String email;
    @Option(names = {"--firstname"}, required = true)
    private String firstName;
    @Option(names = {"--lastname"}, required = true)
    private String lastName;
    @Option(names = {"--phone", "--phone-number"})
    private String phoneNumber;
    @Option(names = {"--role"}, required = true)
    private String roleNameOrId;
    @Option(names = {"--groups"}, required = false, split = ",")
    private ArrayList<String> userGroups;
    @Option(names = {"--applications"}, required = false, split=",")
    private ArrayList<String> applications;

    @Override
    public JsonNode getJsonNode(UnirestInstance unirest) {
        validate();

        FoDUserCreateRequest userCreateRequest = new FoDUserCreateRequest()
                .setUserName(userName)
                .setEmail(email)
                .setFirstName(firstName)
                .setLastName(lastName)
                .setPhoneNumber(phoneNumber)
                .setRoleId(FoDUserHelper.getRoleId(unirest, roleNameOrId));

        if (userGroups != null && userGroups.size() > 0) {
            userCreateRequest.setUserGroupIds(FoDUserGroupHelper.getUserGroupsNode(unirest, userGroups));
        }
        if (applications != null && applications.size() > 0) {
            userCreateRequest.setApplicationIds(FoDAppHelper.getApplicationsNode(unirest, applications));
        }

        return FoDUserHelper.createUser(unirest, userCreateRequest).asJsonNode();
    }

    private void validate() {

    }

    @Override
    public JsonNode transformRecord(JsonNode record) {
        return FoDUserHelper.renameFields(record);
    }

    @Override
    public String getActionCommandResult() {
        return "CREATED";
    }

    @Override
    public boolean isSingular() {
        return true;
    }

}
