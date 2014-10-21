package com.mscript.parse;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * Thrown by the {@link com.mscript.parse.MScriptParser MScript parser} when a function call is successfully matched but
 * either the function is not defined or the number of arguments passed is wrong.
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Oct 21, 2014
 */
public class FunctionRecognitionException extends RecognitionException {

    /**
     * @param offendingToken since a function call is normally made up of several tokens, the recognizer's current token
     *                       might not be the best suited to be reported as offending token; usually, the token at the
     *                       beginning of the function call better indicates the error location in code
     */
    public FunctionRecognitionException(String message, @NotNull Parser recognizer, @NotNull Token offendingToken) {
        super(message, recognizer, recognizer.getInputStream(), recognizer.getContext());
        setOffendingToken(offendingToken);
    }
}
