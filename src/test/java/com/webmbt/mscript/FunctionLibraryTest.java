package com.webmbt.mscript;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.webmbt.mscript.FunctionTest.FunctionTestPlugin;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 12, 2014
 */
public class FunctionLibraryTest {

    protected FunctionLibrary functionLibrary;

    @Before
    public void setUp() {
        functionLibrary = new FunctionLibrary();
    }

    @After
    public void tearDown() {
        functionLibrary = null;
    }

    @Test
    public void nullOrEmptyPluginNameAddsToSystemFunctions() throws NoSuchMethodException {
        functionLibrary.add(null, "foo1", FunctionTestPlugin.class.getMethod("f"));
        functionLibrary.add(" ", "foo2", FunctionTestPlugin.class.getMethod("f", String.class));
        functionLibrary.add("", "foo3", FunctionTestPlugin.class.getMethod("f", String.class, String.class));

        assertTrue(functionLibrary.library.containsKey(FunctionLibrary.SYSTEM_FUNCTIONS));
        assertEquals(3, functionLibrary.library.get(FunctionLibrary.SYSTEM_FUNCTIONS).size());
    }

    @Test
    public void addingImplementationsChangesArity() throws NoSuchMethodException {
        String pluginName = "bar", functionName = "foo";
        Class<FunctionTestPlugin> klass = FunctionTestPlugin.class;

        functionLibrary.add(null, functionName, klass.getMethod("f"));
        functionLibrary.add(pluginName, functionName, klass.getMethod("f", String.class));
        functionLibrary.add(pluginName, functionName, klass.getMethod("f", String.class, String.class));

        assertTrue(functionLibrary.library.containsKey(pluginName));
        assertEquals(1, functionLibrary.library.get(pluginName).size());

        Function foo = functionLibrary.library.get(pluginName).get(functionName);
        assertEquals(1, foo.getMinArity());
        assertEquals(2, foo.getMaxArity());
        assertEquals(pluginName, foo.getPluginName());
    }
}
