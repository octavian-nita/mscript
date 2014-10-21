package com.mscript.parse;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;

public class FunctionRecognitionException extends RecognitionException {

    public FunctionRecognitionException(String message, @NotNull Parser recognizer, @NotNull Token offendingToken) {
        super(message, recognizer, recognizer.getInputStream(), recognizer.getContext());
        setOffendingToken(offendingToken);
    }
}
