package com.webmbt.mscript;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Dec 01, 2014
 */
public class MScriptError {

    private static final Map<String, String> ERROR_MESSAGES = new HashMap<>();
    static {
        // @TODO Load ERROR_MESSAGES from one or more Java properties files or hard-code more messages!
    }

    private String mscript;

    private int lineNumber;

    private int charNumber;

    private String contextText;

    private String code;

    private String description;

    public MScriptError(String mscript, int lineNumber, int charNumber, String contextText, String code) {
        this.mscript = mscript;
        this.lineNumber = lineNumber;
        this.charNumber = charNumber;
        this.contextText = contextText;
        this.code = code;
    }

    @Override
    public String toString() {
        return "'" + contextText + "'@" + lineNumber + ":" + charNumber + " " + code + " " + description;
    }
}
