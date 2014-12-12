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
        public String getPluginID() {
            return "ft";
        }

        public String f() { return ""; }

        public String f(String param0) { return ""; }

        public void g(String param0) {}

        public void h(String param0, String param1) {}

        public void f1(String[] params) {}

        public void f2(String param0, String... params) {}
    }

    @Test
    public void nullOrEmptyPluginNameImpliesSystemFunction() {
        Function function = new Function("foo", 0);
        assertTrue(function.isSystemFunction());

        function = new Function("foo", null, 1);
        assertTrue(function.isSystemFunction());

        function = new Function("foo", "", 2, 3);
        assertTrue(function.isSystemFunction());

        function = new Function("foo", " ", 4, 4);
        assertTrue(function.isSystemFunction());

        function = new Function("foo", "bar", 10);
        assertFalse(function.isSystemFunction());
    }

    @Test
    public void implementationShouldAcceptStringArguments() throws NoSuchMethodException {
        Function function = new Function("foo", "bar");
        function.addImplementation(FunctionTestPlugin.class.getMethod("f"));
        function.addImplementation(FunctionTestPlugin.class.getMethod("f", String.class));
        function.addImplementation(FunctionTestPlugin.class.getMethod("g", String.class));
        function.addImplementation(FunctionTestPlugin.class.getMethod("h", String.class, String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void implementationShouldOnlyAcceptStringArguments() throws NoSuchMethodException {
        Function function = new Function("foo", "bar");
        function.addImplementation(FunctionTestPlugin.class.getMethod("f"));
        function.addImplementation(FunctionTestPlugin.class.getMethod("f1", String[].class));
    }

    @Test
    public void addingImplementationChangesArity() throws NoSuchMethodException {
        Function function = new Function("foo", "bar");
        assertTrue(function.getMinArity() == 0);
        assertTrue(function.getMaxArity() == 0);

        function.addImplementation(FunctionTestPlugin.class.getMethod("f"));
        assertTrue(function.getMinArity() == 0);
        assertTrue(function.getMaxArity() == 0);

        function.addImplementation(FunctionTestPlugin.class.getMethod("f", String.class));
        assertTrue(function.getMinArity() == 0);
        assertTrue(function.getMaxArity() == 1);
    }
}
