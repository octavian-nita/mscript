package com.webmbt.mscript;

import com.webmbt.mscript.test.fixture.FunctionsFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 2.0, Mar 30, 2015
 */
public class MScriptEngineTest {

    protected FunctionsFixture functionsFixture; // provides test system functions and plugins

    protected MScriptEngine mScriptEngine;

    @Before
    public void setUp() throws IllegalAccessException, InstantiationException {
        functionsFixture = new FunctionsFixture();
        mScriptEngine = new MScriptEngine();
    }

    @After
    public void tearDown() {
        mScriptEngine.clearFunctionCache();
        mScriptEngine = null;
        functionsFixture.tearDown();
        functionsFixture = null;
    }

    @Test
    public void givenErroneousMScriptWhenGenericSyntaxErrorIsEncounteredThenGenericParseErrorIsCollected() {
        String mScript = "v %% 10; // one-line, erroneous MScript";

        List<MScriptError> errors = mScriptEngine
            .checkMScript(mScript, functionsFixture.getSystemFunctions(), functionsFixture.getAvailablePlugins());

        assertEquals("The error list should contain one error", 1, errors.size());

        MScriptError error = errors.get(0);

        assertEquals("The error script should be the same as the input script", mScript, error.mScript);
        assertEquals("The error context should point to the erroneous token", "%", error.context);
        assertEquals("The error line number should point to the erroneous line", 1, error.lineNumber);
        assertEquals("The error char number should point to the erroneous token", 2, error.charNumber);
        assertEquals("The error code should point to a generic parse error message", "E_PARSE", error.code);
    }

    @Test
    public void givenMScriptFunctionCallWhenNonexistentPluginIsUsedThenPluginNotFoundErrorIsCollected() {
        String mScript = "$foo.bar()";

        List<MScriptError> errors = mScriptEngine
            .checkMScript(mScript, functionsFixture.getSystemFunctions(), functionsFixture.getAvailablePlugins());

        assertEquals("The error list should contain one error", 1, errors.size());

        MScriptError error = errors.get(0);

        assertEquals("The error script should be the same as the input script", mScript, error.mScript);
        assertEquals("The error context should point to the erroneous token", "foo", error.context);
        assertEquals("The error line number should point to the erroneous line", 1, error.lineNumber);
        assertEquals("The error char number should point to the erroneous token", 1, error.charNumber);
        assertEquals("The error code should point to a specific error message", "E_PLUGIN_NOT_FOUND", error.code);
    }

    @Test
    public void givenMScriptFunctionCallWhenNonexistentFunctionIsCalledThenFunctionNotFoundErrorIsCollected() {
        String mScript = "$web.bar()";

        List<MScriptError> errors = mScriptEngine
            .checkMScript(mScript, functionsFixture.getSystemFunctions(), functionsFixture.getAvailablePlugins());

        assertEquals("The error list should contain one error", 1, errors.size());

        MScriptError error = errors.get(0);

        assertEquals("The error script should be the same as the input script", mScript, error.mScript);
        assertEquals("The error context should point to the erroneous token", "bar", error.context);
        assertEquals("The error line number should point to the erroneous line", 1, error.lineNumber);
        assertEquals("The error char number should point to the erroneous token", 5, error.charNumber);
        assertEquals("The error code should point to a specific error message", "E_FUNCTION_NOT_FOUND", error.code);
    }

    @Test
    public void givenMScriptFunctionCallWhenWrongNumberOfArgumentsThenWrongNumberOfArgumentsErrorIsCollected() {
        String mScript = "$web._nativeFunc1(1, 2)";

        List<MScriptError> errors = mScriptEngine
            .checkMScript(mScript, functionsFixture.getSystemFunctions(), functionsFixture.getAvailablePlugins());

        assertEquals("The error list should contain one error", 1, errors.size());

        MScriptError error = errors.get(0);

        assertEquals("The error script should be the same as the input script", mScript, error.mScript);
        assertEquals("The error context should point to the erroneous token", "_nativeFunc1", error.context);
        assertEquals("The error line number should point to the erroneous line", 1, error.lineNumber);
        assertEquals("The error char number should point to the erroneous token", 5, error.charNumber);
        assertEquals("The error code should point to a specific error message", "E_WRONG_NUMBER_OF_ARGUMENTS",
                     error.code);
    }

    @Test
    @Ignore
    public void testMScriptExecution() throws Throwable {
        new MScriptEngine()
            .executeMScript("1 < '2'", functionsFixture.getSystemFunctions(), functionsFixture.getAvailablePlugins());
    }
}
