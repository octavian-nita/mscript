package com.webmbt.mscript;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import static java.text.MessageFormat.format;
import static java.util.logging.Level.WARNING;

/**
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Mar 12, 2015
 */
public class MScriptError {

    public final String mScript;     // MScript source code

    public final String context;     // erroneous token, for parsing errors at least

    public final int lineNumber;

    public final int charNumber;

    public final String code;        // the error code (e.g. E_PLUGIN_NOT_FOUND)

    public final String description; // obtained from the resource bundle, the code being used as resource key

    /**
     * The error description is retrieved from a {@link ResourceBundle resource bundle} based on the error
     * <code>code</code> and can be parameterized ({@link java.text.MessageFormat} is used to format the arguments).
     */
    public MScriptError(String mScript, String context, int lineNumber, int charNumber, String code,
                        Object... arguments) {
        this.mScript = mScript;

        this.context = context;
        this.lineNumber = lineNumber;
        this.charNumber = charNumber;

        this.code = code == null ? "E_GENERIC" : code;
        String description;
        try {
            description = ERROR_MESSAGES.getString(this.code);
            if (arguments != null && arguments.length > 0) {
                description = format(description, arguments);
            }
        } catch (Throwable throwable) {
            LOG.log(WARNING, "Cannot format a full error description", throwable);
            description = "";
        }
        this.description = description;

        // Cache the toString() message:
        StringBuilder builder =
            new StringBuilder(this.context != null ? this.context + " @" : "@").append(this.lineNumber).append(':')
                                                                               .append(this.charNumber).append(' ')
                                                                               .append('[').append(this.code)
                                                                               .append(']');
        stringified = "".equals(this.description.trim()) ? builder.toString()
                                                         : builder.append(" ").append(this.description).toString();
    }

    @Override
    public String toString() { return stringified; }

    private final String stringified; // cached toString() message

    protected static final Logger LOG = Logger.getLogger(MScriptError.class.getName());

    protected static final ResourceBundle ERROR_MESSAGES =
        ResourceBundle.getBundle("com.webmbt.mscript.MScriptErrorMessages", Locale.getDefault());
}
