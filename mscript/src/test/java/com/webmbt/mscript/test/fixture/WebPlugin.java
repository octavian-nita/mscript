package com.webmbt.mscript.test.fixture;

import com.webmbt.plugin.PluginAncestor;

/** @author yxl01 */
public final class WebPlugin extends PluginAncestor {

    @Override
    public String getPluginID() { return "web"; }

    /**
     * Plugin function, called with $web.click('elem1') or $click('elem1').
     */
    @MSCRIPT_METHOD
    public void click(String location) {}

    @MSCRIPT_METHOD
    public void click(String location, String option) {}

    @MSCRIPT_METHOD
    public boolean isPresent(String location) { return ((int) (Math.random() * 10)) % 2 == 0; }

    @MSCRIPT_METHOD
    public String getValue(String location, String propName) { return "abc"; }

    @MSCRIPT_METHOD
    public String getTitle() { return "title"; }

    /**
     * Native function, not annotated with @MSCRIPT_METHOD; must be called with $web._myFunc1(), $web._myFunc2('p1').
     */
    public int nativeFunc1() { return 0; }

    public int nativeFunc1(String arg) { return 0; }
}
