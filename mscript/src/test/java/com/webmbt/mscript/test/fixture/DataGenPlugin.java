package com.webmbt.mscript.test.fixture;

import com.webmbt.plugin.PluginAncestor;

import java.util.Random;

/** @author yxl01 */
public final class DataGenPlugin extends PluginAncestor {

    private final Random random = new Random();

    @Override
    public String getPluginID() { return "dataGen"; }

    protected String dataGenUtility(String arg) {
        return "Should not be available / visible as an MScript function!";
    }

    @MSCRIPT_METHOD
    public String lowerCase(String inString) {
        return inString == null ? "" : inString.toLowerCase();
    }

    @MSCRIPT_METHOD
    public String upperCase(String inString) {
        return inString == null ? "" : inString.toUpperCase();
    }

    @MSCRIPT_METHOD
    public String randFromList(String list) {
        return randFromList(list, ",");
    }

    @MSCRIPT_METHOD
    public String randFromList(String list, String delimiter) {
        if (list == null) {
            return "";
        }

        String[] parts = list.split("\\Q" + delimiter + "\\E"); // quote the delimiter as it may contain regex chars
        return parts.length == 0 ? "" : parts[random.nextInt(parts.length)];
    }
}
