package com.webmbt.mscript.parse;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;

/**
 * Thrown by the {@link com.webmbt.mscript.parse.MScriptParser MScript parser} when MScript-specific parse errors occur
 * (e.g. function call is successfully matched but either the function is not defined or the number of passed arguments
 * is wrong; currently, function invocation is checked at parse time as opposed to the more common case in which a
 * semantic analysis phase would validate it).
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.1, Mar 13, 2014
 */
public class MScriptRecognitionException extends RecognitionException {

    protected Object[] errorArguments; // for (eventually) parameterized error descriptions

    public Object[] getErrorArguments() { return errorArguments; }

    public String getErrorCode() { return getMessage(); }

    /**
     * @param offendingToken sometimes the recognizer's current token might not be the best suited to be reported as
     *                       offending token; e.g. a function call is normally made up of several tokens and usually
     *                       the token at the beginning of the call better indicates the error location in code
     */
    public MScriptRecognitionException(Parser recognizer, Token offendingToken, String errorCode,
                                       Object... errorArguments) {
        super(errorCode, recognizer, recognizer.getInputStream(), recognizer.getContext());
        this.errorArguments = errorArguments;
        if (offendingToken != null) {
            setOffendingToken(recognizer, offendingToken);
        }
    }

    public MScriptRecognitionException(Parser recognizer, String errorCode, Object... errorArguments) {
        this(recognizer, null, errorCode, errorArguments);
    }
}
