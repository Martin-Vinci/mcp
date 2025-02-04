package com.micropay.micropay;

import android.content.Context;
import android.util.Base64;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;

import org.json.JSONObject;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by micropay on 2/2/19.
 */

public class AppCrashHandler implements Thread.UncaughtExceptionHandler, Response.ErrorListener, Response.Listener<JSONObject> {

    private static Context ctx;

    /**
     * Storage for the original default crash handler.
     */
    private Thread.UncaughtExceptionHandler defaultHandler;

    /**
     * Simple string for category log
     */
    private static final String TAG = "AppCrashHandler";

    /**
     * Installs a new exception handler.
     *
     * @param applicationContext
     */
    public static void installHandler(Context applicationContext) {
        if (!(Thread.getDefaultUncaughtExceptionHandler() instanceof AppCrashHandler)) {
            Thread.setDefaultUncaughtExceptionHandler(new AppCrashHandler());
            ctx = applicationContext;
        }
    }

    private AppCrashHandler() {
        this.defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
    }

    /**
     * Called when there is an uncaught exception elsewhere in the code.
     *
     * @param t the thread that caused the error
     * @param e the exception that caused the error
     */
    @Override
    public void uncaughtException(Thread t, Throwable e) {
        try {
            if (NetworkUtil.isNetworkAvailable(ctx)) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/reportException", NetworkUtil.getBaseRequest(ctx)
                        .put("exception", e.toString())
                        .put("stackTrace", getStackTrace(e))
                        .put("activity", TAG), this, this) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Basic "
                                + Base64.encodeToString("admin:admin".getBytes(),
                                Base64.NO_WRAP));
                        return headers;
                    }
                };
                jsonObjectRequest.setTag(TAG);
                NetworkUtil.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
            }
        } catch (Exception ex) {
        }
        // Call the default handler
        defaultHandler.uncaughtException(t, e);
    }

    /**
     * Convert an exception into a printable stack trace.
     *
     * @param e the exception to convert
     * @return the stack trace
     */
    private String getStackTrace(Throwable e) {
        final Writer sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        String stacktrace = sw.toString();
        pw.close();
        return stacktrace;
    }

    @Override
    public void onErrorResponse(VolleyError error) {

    }

    @Override
    public void onResponse(JSONObject response) {

    }
}
