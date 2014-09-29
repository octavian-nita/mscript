package com.mscript.parse;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MScriptParserBaseTest {

    protected ParseTree parse(String filename) throws IOException {
        return parse(new File(filename));
    }

    protected ParseTree parse(File file) throws IOException {

        ANTLRInputStream chars = new ANTLRInputStream(new FileReader(file));
        MScriptLexer mScriptLexer = new MScriptLexer(chars);

        CommonTokenStream tokens = new CommonTokenStream(mScriptLexer);
        MScriptParser mScriptParser = new MScriptParser(tokens);

        return mScriptParser.script();
    }
}
