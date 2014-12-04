package com.webmbt.mscript.parse;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;

/**
 * Thrown by the {@link com.mscript.parse.MScriptParser MScript parser} when (MScript-) specific parse errors occur
 * (e.g. a function call is successfully matched but either the function is not defined or the number of arguments
 * passed is wrong; currently, function existence is checked at parse time as opposed to the usual case in which a
 * semantic analysis phase would handle it).
 *
 * @author Octavian Theodor Nita (https://github.com/octavian-nita)
 * @version 1.0, Oct 21, 2014
 */
public class MScriptRecognitionException extends RecognitionException {

    /**
     * @param offendingToken sometimes the recognizer's current token might not be the best suited to be reported as
     *                       offending token; e.g. a function call is normally made up of several tokens and usually
     *                       the token at the beginning of the call better indicates the error location in code
     */
    public MScriptRecognitionException(String message, @NotNull Parser recognizer, @NotNull Token offendingToken) {
        super(message, recognizer, recognizer.getInputStream(), recognizer.getContext());
        setOffendingToken(recognizer, offendingToken);
    }

    public MScriptRecognitionException(String message, @NotNull Parser recognizer) {
        this(message, recognizer, recognizer.getCurrentToken());
    }
}
