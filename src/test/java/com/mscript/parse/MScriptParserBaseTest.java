package com.mscript.parse;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.ParseTree;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import static org.junit.Assert.fail;

/**
 * Base class for MScript parser tests, encapsulating common functionality like exercising the SUT on a script, etc.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Sep 27, 2014
 */
public class MScriptParserBaseTest {

    /**
     * Simple {@link org.antlr.v4.runtime.ANTLRErrorListener} that handles a syntax error either by throwing the
     * passed {@link RecognitionException} if not null or otherwise by calling {@link org.junit.Assert#fail(String)}
     * with a proper message. The end goal is to cause an MScript parser test to fail upon syntax errors.
     */
    protected static class MScriptParserTestErrorListener extends BaseErrorListener {

        @Override
        public void syntaxError(@NotNull Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line,
                                int charPositionInLine, @NotNull String msg, @Nullable RecognitionException e) {
            if (e != null) {
                throw e; // in order to force a test to fail
            }
            fail(offendingSymbol + "@" + line + ":" + charPositionInLine + ": " + msg);
        }
    }

    /**
     * Equivalent to <code>parse(new File(filename))</code>.
     *
     * @see #parse(File)
     */
    protected ParseTree parse(String filename) throws IOException {
        return parse(new File(filename));
    }

    /**
     * Defines what it means to <a href="http://xunitpatterns.com/exercise%20SUT.html">exercise</a> the <a
     * href="http://xunitpatterns.com/SUT.html">system under test (SUT)</a> for simple {@link MScriptParser} tests, i.e.
     * parse an MScript file and return the resulting {@link ParseTree parse tree}. Outlines the code needed to call the
     * MScript parser in an application. If the parsing process fails, tests calling this method will also fail.
     *
     * @param file path to a <a href="http://xunitpatterns.com/test%20fixture%20-%20xUnit.html">fixture</a> MScript
     *             file to be parsed
     * @return the {@link ParseTree parse tree} resulting after the parsing process
     * @throws IOException if the specified file cannot be found or read
     */
    protected ParseTree parse(File file) throws IOException {

        ANTLRInputStream chars = new ANTLRInputStream(new FileReader(file));
        MScriptLexer mScriptLexer = new MScriptLexer(chars);

        CommonTokenStream tokens = new CommonTokenStream(mScriptLexer);
        MScriptParser mScriptParser = new MScriptParser(tokens);

        // Set the parser with an error listener that forces a test to fail upon the first parsing error:
        mScriptParser.addErrorListener(new MScriptParserTestErrorListener());

        return mScriptParser.script();
    }
}
