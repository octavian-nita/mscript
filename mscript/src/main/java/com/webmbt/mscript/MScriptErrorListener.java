package com.webmbt.mscript;

import com.webmbt.mscript.parse.MScriptRecognitionException;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.misc.Nullable;

import java.util.List;

/**
 * {@link org.antlr.v4.runtime.ANTLRErrorListener} that translates
 * {@link org.antlr.v4.runtime.RecognitionException}s to {@link MScriptError}s.
 */
class MScriptErrorListener extends BaseErrorListener {

    private final String mScript;

    private final List<MScriptError> mScriptErrors; // where to accumulate errors for later reporting

    public MScriptErrorListener(String mScript, List<MScriptError> mScriptErrors) {
        if (mScriptErrors == null) {
            throw new IllegalArgumentException("the list to accumulate MScript errors cannot be null");
        }

        this.mScript = mScript;
        this.mScriptErrors = mScriptErrors;
    }

    @Override
    public <T extends Token> void syntaxError(@NotNull Recognizer<T, ?> recognizer, @Nullable T offendingSymbol,
                                              int line, int charPositionInLine, @NotNull String message,
                                              @Nullable RecognitionException exception) {
        if (exception instanceof MScriptRecognitionException) {
            MScriptRecognitionException mScriptEx = (MScriptRecognitionException) exception;
            mScriptErrors.add(
                new MScriptError(mScript, offendingSymbol.getText(), line, charPositionInLine, mScriptEx.getErrorCode(),
                                 mScriptEx.getErrorArguments()));
        } else {
            mScriptErrors.add(new MScriptError(mScript, offendingSymbol.getText(), line, charPositionInLine, "E_PARSE",
                                               message == null ? exception.getMessage() : message));
        }
    }
}
