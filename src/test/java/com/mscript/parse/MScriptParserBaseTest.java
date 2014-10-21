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

    import com.mscript.parse.MScriptLexer;
    import com.mscript.parse.MScriptParser;

/**
 * Base class for MScript parser tests, encapsulating common functionality like exercising the SUT on a script, etc.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Sep 27, 2014
 */
public class MScriptParserBaseTest {

    /**
     * Simple {@link org.antlr.v4.runtime.ANTLRErrorListener} that handles a syntax error by {@link
     * org.junit.Assert#fail(String) failing} an MScript parser test with a descriptive message.
     */
    protected static class MScriptParserTestErrorListener extends BaseErrorListener {

        @Override
        public void syntaxError(@NotNull Recognizer<?, ?> recognizer, @Nullable Object offendingSymbol, int line,
                                int charPositionInLine, @NotNull String msg, @Nullable RecognitionException e) {
            fail(offendingSymbol + "@" + line + ":" + charPositionInLine + ": " + msg +
                 (e != null && e.getMessage() != null ? " [" + e.getMessage() + "]" : ""));
        }
    }

    /**
     * Equivalent to <code>parse(new ANTLRInputStream(text))</code>.
     *
     * @see #parse(ANTLRInputStream)
     */
    protected ParseTree parseText(String text) {
        return parse(new ANTLRInputStream(text));
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
     * Equivalent to <code>parse(new ANTLRInputStream(new FileReader(file)))</code>.
     *
     * @see #parse(ANTLRInputStream)
     */
    protected ParseTree parse(File file) throws IOException {
        try (FileReader reader = new FileReader(file)) {
            return parse(new ANTLRInputStream(reader));
        }
    }

    /**
     * Defines what it means to <a href="http://xunitpatterns.com/exercise%20SUT.html">exercise</a> the <a
     * href="http://xunitpatterns.com/SUT.html">system under test (or SUT)</a> for simple {@link
     * com.mscript.parse.MScriptParser} tests, i.e. to parse streams containing MScript code and return resulting {@link
     * ParseTree parse tree}. Encapsulates the code required to call the MScript parser from an application. If the
     * parsing process fails, tests calling this method will also fail.
     *
     * @param chars <a href="http://xunitpatterns.com/test%20fixture%20-%20xUnit.html">fixture</a> {@link
     *              ANTLRInputStream input stream} to be parsed
     * @return the {@link ParseTree parse tree} resulting after the parsing process
     */
    protected ParseTree parse(ANTLRInputStream chars) {
        MScriptLexer mScriptLexer = new MScriptLexer(chars);

        CommonTokenStream tokens = new CommonTokenStream(mScriptLexer);
        MScriptParser mScriptParser = new MScriptParser(tokens);

        // Set up a custom error listener that forces a test to fail upon the first parsing error:
        mScriptParser.addErrorListener(new MScriptParserTestErrorListener());

        return mScriptParser.script();
    }
}
