package com.webmbt.mscript;

import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Mar 12, 2015
 */
public class MScriptError {

    protected static final ResourceBundle ERROR_MESSAGES =
        ResourceBundle.getBundle("com.webmbt.mscript.MScriptErrorMessages", Locale.getDefault());

    public final String mScript;

    public final String context;   // erroneous token

    public final int lineNumber;

    public final int charNumber;

    public final String code;      // e.g. E_WRONG_NUMBER_OF_ARGUMENTS

    public final String description;

    private final String toString; // cached toString() message

    public MScriptError(String mScript, String context, int lineNumber, int charNumber, String code) {
        this(mScript, context, lineNumber, charNumber, code, null);
    }

    /**
     * There may be situations where one might need to override eventual error descriptions coming from the default
     * resource bundles or even provide non-existent ones.
     */
    public MScriptError(String mScript, String context, int lineNumber, int charNumber, String code,
                        String description) {
        this.mScript = mScript;
        this.context = context == null ? "<error context not available>" : context;
        this.lineNumber = lineNumber;
        this.charNumber = charNumber;

        this.code = code == null ? "E_MSCRIPT_GENERIC" : code;
        if (description == null) { // i.e. we're not trying to override what's in the resource bundle
            try {
                description = ERROR_MESSAGES.getString(this.code);
            } catch (MissingResourceException mre) {
                description = "";
            }
        }
        this.description = description;

        // Cache the toString message:
        StringBuilder builder =
            new StringBuilder("'").append(this.context).append("'@").append(this.lineNumber).append(':')
                                  .append(this.charNumber).append(' ').append(this.code);
        toString = "".equals(this.description.trim()) ? builder.toString()
                                                      : builder.append(": ").append(this.description).toString();
    }

    @Override
    public String toString() { return toString; }
}
