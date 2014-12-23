package com.webmbt.mscript;

import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static com.webmbt.plugin.MScriptInterface.MSCRIPT_METHOD;
import static java.lang.reflect.Modifier.isPublic;

/**
 * MScript function signatures are inferred by scanning {@link com.webmbt.plugin.MScriptInterface.MSCRIPT_METHOD
 * MSCRIPT_METHOD}-annotated Java methods.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 01, 2014
 */
public class MScriptEngine {

    private FunctionLibrary library = new FunctionLibrary();

    public MScriptEngine(Object systemFunctions, List<PluginAncestor> plugins) {
        this(systemFunctions == null ? null : systemFunctions.getClass(), plugins);
    }

    public MScriptEngine(Class<?> systemFunctions, List<PluginAncestor> plugins) {
        if (systemFunctions != null) {
            for (Method method : systemFunctions.getMethods()) {
                if (method.isAnnotationPresent(MSCRIPT_METHOD.class)) {
                    library.add(new Function(method.getName()).addImplementation(method));
                } else if (isPublic(method.getModifiers())) {
                    library.add(new Function("_" + method.getName()).addImplementation(method));
                }
            }
        }

        if (plugins != null) {
            for (PluginAncestor plugin : plugins) {
                for (Method method : plugin.getClass().getMethods()) {
                    if (method.isAnnotationPresent(MSCRIPT_METHOD.class)) {
                        library.add(new Function(method.getName(), plugin.getPluginID()).addImplementation(method));
                    } else if (isPublic(method.getModifiers())) {
                        library.add(new Function("_" + method.getName()).addImplementation(method));
                    }
                }
            }
        }
    }

    public List<MScriptError> checkMScript(String mScript, MbtScriptExecutor systemFunctions,
                                           List<PluginAncestor> plugins) {
        List<MScriptError> errors = new ArrayList<>();
        return errors;
    }
}
