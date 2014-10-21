package com.mscript.parse;

import org.junit.Test;

import java.io.IOException;

public class MScriptParserAutoTest extends MScriptParserBaseTest {

    @Test
    public void testSimpleAssignments() throws IOException {
        parse("mscript/01-simple-assignments.mscript");
    }

    @Test
    public void testFunctionCalls() throws IOException {
        parse("mscript/02-function-calls.mscript");
    }

    @Test
    public void testFunctionCallsInText() throws IOException {
        parseText("$foo(1, 2, 3, 4); $foo('$bar.foo(1, 2)')");
    }
}
