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

        public String f() { return "0"; }

        public String f(String param0) { return "1"; }

        public String f(String param0, String param1) { return "2"; }

        public void g(String param0, String param1, String param2) {}

        public void f1(String[] params) {}

        public void f2(String param0, String... params) {}
    }

    @Test
    public void defaultNullOrEmptyPluginNameImpliesSystemFunction() {
        assertTrue(new Function("foo").isSystemFunction());
        assertTrue(new Function("foo", null, 1).isSystemFunction());
        assertTrue(new Function("foo", "", 2, 3).isSystemFunction());
        assertTrue(new Function("foo", " ", 4, 4).isSystemFunction());
        assertFalse(new Function("foo", "bar", 10).isSystemFunction());
    }

    @Test
    public void implementationAcceptsStringArguments() throws NoSuchMethodException {
        Function function = new Function("foo", "bar");
        function.addImplementation(FunctionTestPlugin.class.getMethod("f"));
        function.addImplementation(FunctionTestPlugin.class.getMethod("f", String.class));
        function.addImplementation(FunctionTestPlugin.class.getMethod("f", String.class, String.class));
        function.addImplementation(FunctionTestPlugin.class.getMethod("g", String.class, String.class, String.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void implementationOnlyAcceptsStringArguments() throws NoSuchMethodException {
        Function function = new Function("foo", "bar");
        function.addImplementation(FunctionTestPlugin.class.getMethod("f", String.class));
        function.addImplementation(FunctionTestPlugin.class.getMethod("f1", String[].class));
    }

    @Test
    public void addingImplementationsChangesArity() throws NoSuchMethodException {
        Function function = new Function("foo", "bar");
        assertTrue(function.getMinArity() == 0);
        assertTrue(function.getMaxArity() == 0);

        function.addImplementation(FunctionTestPlugin.class.getMethod("f"));
        assertTrue(function.getMinArity() == 0);
        assertTrue(function.getMaxArity() == 0);

        function.addImplementation(FunctionTestPlugin.class.getMethod("f", String.class, String.class));
        assertTrue(function.getMinArity() == 0);
        assertTrue(function.getMaxArity() == 2);
    }
}
