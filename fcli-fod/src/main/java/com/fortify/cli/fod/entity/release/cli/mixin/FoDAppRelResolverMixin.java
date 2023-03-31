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

package com.fortify.cli.fod.entity.release.cli.mixin;

import com.fortify.cli.fod.entity.release.helper.FoDAppRelDescriptor;
import com.fortify.cli.fod.entity.release.helper.FoDAppRelHelper;

import kong.unirest.UnirestInstance;
import lombok.Getter;
import picocli.CommandLine.Mixin;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

//TODO Change description keys to be more like picocli convention
public class FoDAppRelResolverMixin {
    public static abstract class AbstractFoDAppRelResolverMixin {
        @Mixin private FoDDelimiterMixin delimiterMixin;
        public abstract String getAppRelNameOrId();

        public FoDAppRelDescriptor getAppRelDescriptor(UnirestInstance unirest, String... fields){
            return FoDAppRelHelper.getRequiredAppRel(unirest, getAppRelNameOrId(), delimiterMixin.getDelimiter(), fields);
        }

        public String getAppRelId(UnirestInstance unirest) {
            return String.valueOf(getAppRelDescriptor(unirest, "id").getReleaseId());
        }

        public FoDAppAndRelNameDescriptor getAppAndRelName() {
            return FoDAppAndRelNameDescriptor.fromCombinedAppAndRelName(getAppRelNameOrId(), delimiterMixin.getDelimiter());
        }
    }

    public static class RequiredOption extends AbstractFoDAppRelResolverMixin {
        @Option(names = {"--rel", "--release"}, required = true, descriptionKey = "ApplicationReleaseMixin")
        @Getter private String appRelNameOrId;
    }

    public static class PositionalParameter extends AbstractFoDAppRelResolverMixin {
        @Parameters(index = "0", descriptionKey = "ApplicationReleaseMixin")
        @Getter private String appRelNameOrId;
    }
}
