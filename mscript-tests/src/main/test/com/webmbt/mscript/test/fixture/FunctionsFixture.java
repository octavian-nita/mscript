package com.webmbt.mscript.test.fixture;

import com.webmbt.plugin.MbtScriptExecutor;
import com.webmbt.plugin.PluginAncestor;

import java.util.ArrayList;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Mar 16, 2015
 */
public class FunctionsFixture {

    protected MbtScriptExecutor systemFunctions;

    protected List<PluginAncestor> availablePlugins;

    public MbtScriptExecutor getSystemFunctions() { return systemFunctions; }

    public List<PluginAncestor> getAvailablePlugins() { return availablePlugins; }

    public void setSystemFunctions(MbtScriptExecutor systemFunctions) { this.systemFunctions = systemFunctions; }

    public void setAvailablePlugins(List<PluginAncestor> availablePlugins) { this.availablePlugins = availablePlugins; }

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
