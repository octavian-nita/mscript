package com.mscript.parse;

import org.junit.Test;

import java.io.IOException;

public class MScriptParserAutoTest extends MScriptParserBaseTest {

    @Test
    public void testSimpleAssignments() throws IOException {
        parse("mscript/01-simple-assignments.mscript");
    }
}
