package com.webmbt.mscript.test.fixture;

import com.webmbt.plugin.PluginAncestor;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author TestOptimal, LLC
 * @version 1.0, Mar 16, 2015
 */
public class FunctionsFixture {

    protected MbtScriptExecutor systemFunctions;

    public MbtScriptExecutor getSystemFunctions() { return systemFunctions; }

    public FunctionsFixture setSystemFunctions(MbtScriptExecutor systemFunctions) {
        this.systemFunctions = systemFunctions;
        return this;
    }

    protected List<PluginAncestor> availablePlugins;

    public List<PluginAncestor> getAvailablePlugins() { return availablePlugins; }

    public FunctionsFixture setAvailablePlugins(List<PluginAncestor> availablePlugins) {
        this.availablePlugins = availablePlugins;
        return this;
    }

    public FunctionsFixture() throws IllegalAccessException, InstantiationException {
        setUp();
    }

    public static final List<Class<? extends PluginAncestor>> DEFAULT_PLUGIN_CLASSES =
        asList(DataGenPlugin.class, ServicePlugin.class, WebPlugin.class);

    public FunctionsFixture setUp() throws IllegalAccessException, InstantiationException {
        systemFunctions = new MbtScriptExecutor();

        availablePlugins = new ArrayList<>(DEFAULT_PLUGIN_CLASSES.size());
        for (Class<? extends PluginAncestor> availablePluginClass : DEFAULT_PLUGIN_CLASSES) {
            availablePlugins.add(availablePluginClass.newInstance());
        }

        return this;
    }

    public void tearDown() {
        availablePlugins.clear();
        availablePlugins = null;

        systemFunctions = null;
    }
}
