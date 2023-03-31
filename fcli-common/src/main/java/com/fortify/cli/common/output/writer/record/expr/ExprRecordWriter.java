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
package com.fortify.cli.common.output.writer.record.expr;

import org.springframework.expression.Expression;
import org.springframework.expression.common.TemplateParserContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fortify.cli.common.json.JsonHelper;
import com.fortify.cli.common.output.writer.record.AbstractRecordWriter;
import com.fortify.cli.common.output.writer.record.RecordWriterConfig;

import lombok.SneakyThrows;

public class ExprRecordWriter extends AbstractRecordWriter {
    private static final SpelExpressionParser parser = new SpelExpressionParser();
    private final Expression expression;
    
    public ExprRecordWriter(RecordWriterConfig config) {
        super(config);
        this.expression = parser.parseExpression(
                insertControlCharacters(config.getOptions()), 
                new TemplateParserContext("{", "}"));
    }

    @Override @SneakyThrows
    public void writeRecord(ObjectNode record) {
        getConfig().getWriter().write(JsonHelper.evaluateSpelExpression(record, expression, String.class));
    }

    private static final String insertControlCharacters(String s) {
        return s.replaceAll("\\\\t", "\t")
                .replaceAll("\\\\b", "\b")
                .replaceAll("\\\\n", "\n")
                .replaceAll("\\\\r", "\r")
                .replaceAll("\\\\f", "\f");
    }
    
    
}
