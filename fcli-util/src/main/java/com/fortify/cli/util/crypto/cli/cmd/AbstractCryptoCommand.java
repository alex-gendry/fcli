/*******************************************************************************
 * (c) Copyright 2021 Micro Focus or one of its affiliates
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
package com.fortify.cli.util.crypto.cli.cmd;

import java.util.Scanner;

import com.fortify.cli.common.cli.cmd.AbstractFortifyCLICommand;
import com.fortify.cli.common.cli.mixin.CommandHelperMixin;

import lombok.SneakyThrows;
import picocli.CommandLine.Mixin;

public abstract class AbstractCryptoCommand extends AbstractFortifyCLICommand implements Runnable {
    @Mixin private CommandHelperMixin commandHelper;
    
    @Override @SneakyThrows
    public final void run() {
        initMixins();
        String prompt = commandHelper.getMessageResolver().getMessageString("prompt")+" ";
        String value;
        if ( System.console()!=null ) {
            value = new String(System.console().readPassword(prompt));
        } else {
            try ( var scanner = new Scanner(System.in) ) {
                System.out.print(prompt);
                value = scanner.nextLine();
            }
        }
        System.out.println(process(value));
    }

    protected abstract String process(String value);
}
