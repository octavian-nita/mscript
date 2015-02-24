package com.webmbt.plugin;

/**
 * Plugins may be stateful but care must be taken when using them in a concurrent environment like a web application
 * context (from a thread-safety point of view, consider using immutable plugin instances, limiting synchronization on
 * shared result, etc.).
 *
 * @author yxl01
 */
public abstract class PluginAncestor implements MScriptInterface {

    public abstract String getPluginID();
}
