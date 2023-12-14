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
package com.fortify.cli.ssc.summary.cli.cmd;

import com.fortify.cli.common.cli.cmd.AbstractRunnableCommand;
import com.fortify.cli.common.output.writer.output.standard.StandardOutputWriter;
import com.fortify.cli.common.variable.FcliVariableHelper;
import com.fortify.cli.ssc.summary.helper.SSCSummaryHelper;
import picocli.AutoComplete;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Command;
import picocli.CommandLine.Model.CommandSpec;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author agendry
 *
 */
@Command(name = "generate")
public class SSCSummaryGenerateCommand extends AbstractRunnableCommand implements Runnable {
    @Spec
    CommandSpec spec;

    public void run() {
        String summary = "";

        SSCSummaryHelper hlpr = new SSCSummaryHelper();

        summary += hlpr.image("OpenText Fortify","https://cdn.asp.events/CLIENT_CloserSt_D86EA381_5056_B739_5482D50A1A831DDD/sites/CSWA-2023/media/libraries/exhibitors/Ezone-cover.png/fit-in/1500x9999/filters:no_upscale()") + "\n";
        summary += hlpr.header("Fortify AST Results", 1);
        summary += hlpr.rule();
        summary += ":date: Summary date: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));

        spec.commandLine().getOut().print(summary);
        spec.commandLine().getOut().print("\n");
        spec.commandLine().getOut().flush();
    }

}
