package com.webmbt.mscript;

import com.webmbt.plugin.PluginAncestor;
import org.junit.Test;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 12, 2014
 */
public class FunctionTest {

    public static class FunctionTestPlugin extends PluginAncestor {

        @Override
        public String getPluginID() { return "ft"; }

        public String f() { return "0"; }

        public String f(String param0) { return "1"; }

        public String f(String param0, String param1) { return "2"; }

        public void g(String param0, String param1, String param2) {}

        public void f1(String[] params) {}

        public void f2(String param0, String... params) {}
    }

    private FunctionTestPlugin functionTestPlugin = new FunctionTestPlugin();

    @Test
    public void nullOrEmptyPluginNameImpliesSystemFunction() {
        assertTrue(new Function("foo").isSystemFunction());
        assertTrue(new Function("foo", "").isSystemFunction());
        assertTrue(new Function("foo", " ").isSystemFunction());
        assertTrue(new Function("foo", null).isSystemFunction());
        assertFalse(new Function("foo", "b").isSystemFunction());
    }

    @Test
    public void implementationAcceptsStringArguments() throws NoSuchMethodException {
        Function function = new Function("foo", "bar");
        function.addImplementation(FunctionTestPlugin.class.getMethod("f"));
        function.addImplementation(FunctionTestPlugin.class.getMethod("f", String.class), functionTestPlugin);
        function.addImplementation(FunctionTestPlugin.class.getMethod("f", String.class, String.class));
        function.addImplementation(FunctionTestPlugin.class.getMethod("g", String.class, String.class, String.class),
                                   functionTestPlugin);
    }

    @Test(expected = IllegalArgumentException.class)
    public void implementationOnlyAcceptsStringArguments() throws NoSuchMethodException {
        Function function = new Function("foo", "bar");
        function.addImplementation(FunctionTestPlugin.class.getMethod("f", String.class), functionTestPlugin);
        function.addImplementation(FunctionTestPlugin.class.getMethod("f1", String[].class));
    }

    @Test
    public void addingImplementationsOverloadsFunction() throws NoSuchMethodException {
        Function function = new Function("foo", "bar");
        assertFalse(function.hasImplementations());
        assertFalse(function.hasImplementation(0));
        assertFalse(function.hasImplementation(1));

        function.addImplementation(FunctionTestPlugin.class.getMethod("f"));
        assertTrue(function.hasImplementations());
        assertTrue(function.hasImplementation(0));
        assertFalse(function.hasImplementation(1));

        function.addImplementation(FunctionTestPlugin.class.getMethod("f", String.class, String.class));
        assertFalse(function.hasImplementations());
        assertFalse(function.hasImplementation(0));
        assertFalse(function.hasImplementation(2));
    }
}
