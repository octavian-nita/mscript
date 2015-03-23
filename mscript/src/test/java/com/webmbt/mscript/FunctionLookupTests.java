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
public class FunctionLookupTests {

    /** The <a href="http://xunitpatterns.com/SUT.html">system under test <em>(SUT)</em></a>. */
    protected Functions functions;

    protected FunctionsFixture fixture;

    @Before
    public void setUp() throws IllegalAccessException, InstantiationException {
        functions = new Functions();
        fixture = new FunctionsFixture();
    }

    @After
    public void tearDown() {
        fixture.tearDown();
        fixture = null;
        functions.clearCache();
        functions = null;
    }

    @Test
    public void givenAnnotatedMethodInSystemFunctionsThenResultIsFound() {
        Lookup lookup = functions.lookup(null, "f", 0, fixture.getSystemFunctions(), fixture.getAvailablePlugins());

        assertEquals(Lookup.Result.FOUND, lookup.result);

        assertNotNull(lookup.function);
        assertNull(lookup.function.getPluginName());
        assertEquals("f", lookup.function.getName());
    }

    @Test
    public void givenAnnotatedMethodInPluginsThenResultIsFound() {
        Lookup lookup =
            functions.lookup("dataGen", "lowerCase", 1, fixture.getSystemFunctions(), fixture.getAvailablePlugins());

        assertEquals(Lookup.Result.FOUND, lookup.result);

        assertNotNull(lookup.function);
        assertEquals("dataGen", lookup.function.getPluginName());
        assertEquals("lowerCase", lookup.function.getName());
    }

    @Test
    public void givenAnnotatedMethodInPluginsWhenNoPluginNameIsSpecifiedThenResultIsFound() {
        Lookup lookup =
            functions.lookup(null, "lowerCase", 1, fixture.getSystemFunctions(), fixture.getAvailablePlugins());

        assertEquals(Lookup.Result.FOUND, lookup.result);

        assertNotNull(lookup.function);
        assertEquals("dataGen", lookup.function.getPluginName());
        assertEquals("lowerCase", lookup.function.getName());
    }

    @Test
    public void givenPublicMethodWhenUnderscorePrefixedNameIsProvidedThenResultIsFound() {
        Lookup lookup =
            functions.lookup("web", "_myFunc1", 0, fixture.getSystemFunctions(), fixture.getAvailablePlugins());

        assertEquals(Lookup.Result.FOUND, lookup.result);

        assertNotNull(lookup.function);
        assertEquals("web", lookup.function.getPluginName());
        assertEquals("_myFunc1", lookup.function.getName());
    }

    @Test
    public void givenMissingPluginThenResultIsPluginNotFound() {
        List<PluginAncestor> availablePlugins = fixture.getAvailablePlugins();

        PluginAncestor unavailablePlugin = availablePlugins.remove(0);
        Lookup lookup =
            functions.lookup(unavailablePlugin.getPluginID(), "f", 0, fixture.getSystemFunctions(), availablePlugins);

        assertEquals(Lookup.Result.E_PLUGIN_NOT_FOUND, lookup.result);
        assertNull(lookup.function);
    }

    @Test
    public void givenMissingFunctionThenResultIsFunctionNotFound() {
        Lookup lookup =
            functions.lookup("dataGen", "fooBar", 0, fixture.getSystemFunctions(), fixture.getAvailablePlugins());

        assertEquals(Lookup.Result.E_FUNCTION_NOT_FOUND, lookup.result);
        assertNull(lookup.function);
    }

    @Test
    public void givenDifferentArgsNumberThenFunctionFoundButResultIsWrongNumberOfArguments() {
        Lookup lookup =
            functions.lookup("dataGen", "lowerCase", 0, fixture.getSystemFunctions(), fixture.getAvailablePlugins());

        assertNotNull(lookup.function);
        assertEquals("dataGen", lookup.function.getPluginName());
        assertEquals("lowerCase", lookup.function.getName());

        assertEquals(Lookup.Result.E_WRONG_NUMBER_OF_ARGUMENTS, lookup.result);
    }

    // TODO: Verify caching behaviour using Mockito!
    // (http://site.mockito.org/mockito/docs/current/org/mockito/Mockito.html)
}
