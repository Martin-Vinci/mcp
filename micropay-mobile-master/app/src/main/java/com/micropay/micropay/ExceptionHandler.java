package com.micropay.micropay;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import com.micropay.api.Constants;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ExceptionHandler implements Thread.UncaughtExceptionHandler {
    Activity myContext;

    String LINE_SEPARATOR = "\n";

    public ExceptionHandler(Activity context) {
        myContext = context;
    }

    public void uncaughtException(Thread thread, Throwable exception) {
        StringWriter stackTrace = new StringWriter();
        StringWriter causeStackTrace = new StringWriter();

        StackTraceElement[] stackTraceElements = exception.getStackTrace();
        for (int i = 0; i < stackTraceElements.length; i++) {
            causeStackTrace.append(" Class: " + stackTraceElements[i].getClassName()
                    + " Method: " + stackTraceElements[i].getMethodName()
                    + " Line: " + stackTraceElements[i].getLineNumber());
        }
        exception.printStackTrace(new PrintWriter(stackTrace));
        StringBuilder errorReport = new StringBuilder();

        errorReport.append("************ LOCATION OF ERROR ************\n");
        errorReport.append(myContext.getClass().getSimpleName());
        errorReport.append("\n************ CAUSE OF ERROR ************\n");
        errorReport.append(stackTrace.toString());
        errorReport.append("************ CAUSE OF ERROR STACK LINE ************\n");
        errorReport.append(causeStackTrace.toString());
        errorReport.append("\n************ DEVICE INFORMATION ***********\n");
        errorReport.append("Brand: ").append("[" + Build.BRAND + "] ").append("Device: ").append("[" + Build.DEVICE + "] ")
                .append("Model: ").append("[" + Build.MODEL + "] ").append("Id: ").append("[" + Build.ID + "] ")
                .append("Product: ").append("[" + Build.PRODUCT + "]");
        errorReport.append("\n************ FIRMWARE ************\n");
        errorReport.append("SDK: ").append("[" + Build.VERSION.SDK + "] ").append("Release: ").append("[" + Build.VERSION.RELEASE + "] ")
                .append("Incremental: ").append("[" + Build.VERSION.INCREMENTAL + "] ");
        
//
////      *********************** To restart the same activity ************************
////      Intent intent = new Intent(myContext, myContext.getClass());
//
////      *************** To display a custom predefined screen to the user ***********
        Intent intent = new Intent(myContext, Crash_Activity.class);
        intent.putExtra(Constants.CRASH_REPORT, errorReport.toString());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        myContext.startActivity(intent);
        myContext.finish();

        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(10);
    }
}
