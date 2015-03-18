package com.webmbt.mscript;

import com.webmbt.mscript.test.fixture.FunctionsFixture;
import com.webmbt.plugin.PluginAncestor;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.webmbt.mscript.Functions.Lookup;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 2.0, Mar 14, 2015
 */
public class FunctionLookupTest {

    /** The <a href="http://xunitpatterns.com/SUT.html">system under test <em>(SUT)</em></a>. */
    protected Functions functions;

    protected FunctionsFixture functionsFixture;

    @Before
    public void setUp() throws IllegalAccessException, InstantiationException {
        functions = new Functions();
        functionsFixture = new FunctionsFixture().setUp();
    }

    @Test
    public void pluginNotFoundForUnavailablePlugins() {
        List<PluginAncestor> availablePlugins = functionsFixture.getAvailablePlugins();

        PluginAncestor unavailablePlugin = availablePlugins.remove(0);
        Lookup lookup = functions
            .lookup(unavailablePlugin.getPluginID(), "f", 0, functionsFixture.getSystemFunctions(), availablePlugins);

        assertEquals(Lookup.Result.E_PLUGIN_NOT_FOUND, lookup.result);
        assertNull(lookup.function);
    }

    @Test
    public void pluginFunctionNotFoundForNonexistentFunctions() {
        Lookup lookup = functions.lookup("dataGen", "fooBar", 0, functionsFixture.getSystemFunctions(),
                                         functionsFixture.getAvailablePlugins());

        assertEquals(Lookup.Result.E_FUNCTION_NOT_FOUND, lookup.result);
        assertNull(lookup.function);
    }

    @Test
    public void testWrongNumberOfArguments() {
        Lookup lookup = functions.lookup("dataGen", "lowerCase", 0, functionsFixture.getSystemFunctions(),
                                         functionsFixture.getAvailablePlugins());

        assertEquals(Lookup.Result.E_WRONG_NUMBER_OF_ARGUMENTS, lookup.result);

        assertNotNull(lookup.function);
        assertEquals("dataGen", lookup.function.getPluginName());
        assertEquals("lowerCase", lookup.function.getName());
    }

    @After
    public void tearDown() {
        functionsFixture.tearDown();
        functionsFixture = null;
        functions.clearCache();
        functions = null;
    }
}
