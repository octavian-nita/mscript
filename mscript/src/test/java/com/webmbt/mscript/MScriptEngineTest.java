package com.webmbt.mscript;

import com.webmbt.mscript.test.fixture.FunctionsFixture;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author TestOptimal, LLC
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
    public void givenFunctionCallWhenNonexistentPluginIsUsedThenPluginNotFoundErrorIsCollected() {
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
    public void givenFunctionCallWhenNonexistentFunctionIsCalledThenFunctionNotFoundErrorIsCollected() {
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
    public void givenFunctionCallWhenWrongNumberOfArgumentsThenWrongNumberOfArgumentsErrorIsCollected() {
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
    public void givenSysCallExpressionWhenFunctionExistsThenEvaluationSucceeds() throws Throwable {
        String result = mScriptEngine.executeMScript("$g('whatever')", functionsFixture.getSystemFunctions(),
                                                     functionsFixture.getAvailablePlugins());
        assertEquals(functionsFixture.getSystemFunctions().g("whatever"), result);
    }

    @Test
    public void givenInterpolatedStringExpressionThenEvaluationSucceeds() throws Throwable {
        functionsFixture.getSystemFunctions().setVar("var", "1");
        String result = mScriptEngine.executeMScript("'ab$g('1$g('34')2[var]')'", functionsFixture.getSystemFunctions(),
                                                     functionsFixture.getAvailablePlugins());
        assertEquals("ab13421", result);
    }

    @Test
    public void givenParenthesisedAndNegationExpressionThenEvaluationSucceeds() throws Throwable {
        functionsFixture.getSystemFunctions().setVar("var", "1");
        String result = mScriptEngine.executeMScript("var", functionsFixture.getSystemFunctions(),
                                                     functionsFixture.getAvailablePlugins());
        assertEquals("1", result);
    }
}
