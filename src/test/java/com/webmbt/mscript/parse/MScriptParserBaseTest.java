package com.webmbt.mscript.parse;

import com.webmbt.mscript.Functions;
import com.webmbt.plugin.DataGenPlugin;
import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;
import com.webmbt.plugin.ServicePlugin;
import com.webmbt.plugin.WebPlugin;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.fail;

/**
 * Base class for MScript parser tests, encapsulating common functionality like exercising the SUT on a script, etc.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Sep 27, 2014
 */
public class MScriptParserBaseTest {

    protected Functions functions;

    protected MbtScriptExecutor systemFunctions;

    protected List<PluginAncestor> availablePlugins;

    protected List<Class<? extends PluginAncestor>> availablePluginClasses =
        asList(DataGenPlugin.class, ServicePlugin.class, WebPlugin.class);

    @Before
    public void setUp() throws IllegalAccessException, InstantiationException {
        functions = new Functions();

        systemFunctions = new MbtScriptExecutor();

        availablePlugins = new ArrayList<>(availablePluginClasses.size());
        for (Class<? extends PluginAncestor> availablePluginClass : availablePluginClasses) {
            availablePlugins.add(availablePluginClass.newInstance());
        }
    }

    @After
    public void tearDown() {
        availablePlugins.clear();
        availablePlugins = null;

        systemFunctions = null;

        functions.clearCache();
        functions = null;
    }

    /**
     * Simple {@link org.antlr.v4.runtime.ANTLRErrorListener} that handles a syntax error by {@link
     * org.junit.Assert#fail(String) failing} an MScript parser test with a descriptive message.
     */
    public static class MScriptParserTestErrorListener extends BaseErrorListener {

        @Override
        public <T extends Token> void syntaxError(@NotNull Recognizer<T, ?> recognizer, @Nullable T offendingSymbol,
                                                  int line, int charPositionInLine, @NotNull String message,
                                                  @Nullable RecognitionException exception) {
            fail(offendingSymbol + " @" + line + ":" + charPositionInLine + ": " + message);
        }
    }

    /**
     * Equivalent to <code>parse(new File(filename))</code>.
     *
     * @see #parse(File)
     */
    protected ParseTree parseFile(String filename) throws IOException {
        return parse(new File(filename));
    }

    /**
     * Equivalent to <code>parse(new ANTLRInputStream(text))</code>.
     *
     * @see #parse(ANTLRInputStream)
     */
    protected ParseTree parse(String text) {
        return parse(new ANTLRInputStream(text));
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
     * com.webmbt.mscript.parse.MScriptParser} tests i.e. to parse streams containing MScript code and return resulting
     * {@link ParseTree parse tree}. Encapsulates the code required to call the MScript parser from any application. If
     * the parsing process fails, tests calling this method will also fail.
     *
     * @param chars <a href="http://xunitpatterns.com/test%20fixture%20-%20xUnit.html">fixture</a> {@link
     *              ANTLRInputStream input stream} to be parsed
     * @return the {@link ParseTree parse tree} resulting from the parsing process
     */
    protected ParseTree parse(ANTLRInputStream chars) {
        MScriptLexer mScriptLexer = new MScriptLexer(chars);

        CommonTokenStream tokens = new CommonTokenStream(mScriptLexer);
        com.webmbt.mscript.parse.MScriptParser mScriptParser =
            new com.webmbt.mscript.parse.MScriptParser(tokens, systemFunctions, availablePlugins);

        // Set up a custom error listener that forces a test to fail upon the first parsing error:
        mScriptParser.addErrorListener(new MScriptParserTestErrorListener());

        return mScriptParser.script();
    }
}
