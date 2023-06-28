package com.pdfx.logging;

import java.io.PrintWriter;
import java.io.StringWriter;

public class StackTraceUtil {

    /**
     * The method `getStackTrace` is used to retrieve the stack trace of a given throwable as a string.
     *
     * @param throwable The throwable object whose stack trace needs to be retrieved.
     *
     * @return A string representation of the stack trace of the throwable.
     */
    public static String getStackTrace(Throwable throwable) {

        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        throwable.printStackTrace(printWriter);
        return stringWriter.toString();
    }
}
