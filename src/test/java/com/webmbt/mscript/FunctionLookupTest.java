package com.webmbt.mscript;

import org.junit.After;
import org.junit.Before;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 12, 2014
 */
public class FunctionLookupTest {

    protected Functions functions;

    @Before
    public void setUp() {
        functions = new Functions();
    }

    @After
    public void tearDown() {
        functions = null;
    }

    // @Test
    // public void nullOrEmptyPluginNameAddsToSystemFunctions() throws NoSuchMethodException {
    //     functions.add(null, "foo1", FunctionTestPlugin.class.getMethod("f"));
    //     functions.add(" ", "foo2", FunctionTestPlugin.class.getMethod("f", String.class));
    //     functions.add("", "foo3", FunctionTestPlugin.class.getMethod("f", String.class, String.class));
    //
    //     assertTrue(functions.library.containsKey(Functions.SYSTEM_FUNCTIONS));
    //     assertEquals(3, functions.library.get(Functions.SYSTEM_FUNCTIONS).size());
    // }
    //
    // @Test
    // public void addingImplementationsChangesArity() throws NoSuchMethodException {
    //     String pluginName = "bar", functionName = "foo";
    //     Class<FunctionTestPlugin> klass = FunctionTestPlugin.class;
    //
    //     functions.add(null, functionName, klass.getMethod("f"));
    //     functions.add(pluginName, functionName, klass.getMethod("f", String.class));
    //     functions.add(pluginName, functionName, klass.getMethod("f", String.class, String.class));
    //
    //     assertTrue(functions.library.containsKey(pluginName));
    //     assertEquals(1, functions.library.get(pluginName).size());
    //
    //     Function foo = functions.library.get(pluginName).get(functionName);
    //     assertEquals(1, foo.getMinArity());
    //     assertEquals(2, foo.getMaxArity());
    //     assertEquals(pluginName, foo.getPluginName());
    // }
}
