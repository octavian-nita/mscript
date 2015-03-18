package com.webmbt.mscript.test.fixture;

import com.webmbt.plugin.PluginAncestor;

/** @author yxl01 */
public final class ServicePlugin extends PluginAncestor {

    @Override
    public String getPluginID() { return "service"; }

    @MSCRIPT_METHOD
    public int dbCount(String dbID, String tableName) {
        return 10;
    }

    @MSCRIPT_METHOD
    public int dbCountDistinct(String dbID, String tableName, String columnName) {
        return 7;
    }
}
