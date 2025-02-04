package com.micropay.micropay;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.popups.DialogUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


public class Crash_Activity extends AppCompatActivity {

    AlertDialog alertDialog;
    private final String TAG = Crash_Activity.class.getSimpleName();
    private CacheUtil cacheUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        setContentView(R.layout.crash_activity_layout);
        cacheUtil = new CacheUtil(getApplicationContext());

        Intent intent = getIntent();
        if (intent != null) {
            String crashReport = intent.getStringExtra(Constants.CRASH_REPORT);
            callErrorHandlerApi(crashReport);
        } else {
            showAlertDialogAndExit(getString(R.string.crash_report_failed));
        }
    }


    private void callErrorHandlerApi(String crashError) {
        try {
            if (NetworkUtil.isNetworkAvailable(getApplicationContext())) {

                JSONObject custRequest = new JSONObject()
                        .put("outletCredentials", NetworkUtil.getBaseRequest(this))
                        .put("crashReport", crashError)
                        .put("activity", TAG);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/logApplicationError", custRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("0".equalsIgnoreCase(response.optString("responseCode"))) {
                                        showAlertDialogAndExit("Application has crushed");
                                    } else {
                                        showAlertDialogAndExit(response.optString("responseMessage"));
                                    }
                                } else
                                    showAlertDialogAndExit("An error occurred while processing your request.");
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.dismiss();
                        showAlertDialogAndExit(NetworkUtil.getErrorDesc(error));
                    }
                }) {
                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Basic " + Constants.getRawBasicData());
                        headers.put("sessionId", cacheUtil.getString("sessionId"));
                        return headers;
                    }
                };
                jsonObjectRequest.setTag(TAG);
                NetworkUtil.getInstance(getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(this, "Uploading crash report...", false);
            } else {
                showAlertDialogAndExit("Cannot connect to server. No network available");
            }
        } catch (Exception e) {
            clearAndStartNewActivity(LoginActivity.class);
        }
    }

    private void clearAndStartNewActivity(Class<?> clazz) {
        Intent intent = new Intent(getApplicationContext(), clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }


    private void showAlertDialogAndExit(String responseTxt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialogTheme);
        builder.setMessage(responseTxt)
                .setPositiveButton("CLOSE",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                clearAndStartNewActivity(LoginActivity.class);
                            }
                        }).setCancelable(false);
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = builder.create();
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    @Override
    public void onPause() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtil.getInstance(getApplicationContext()).cancel(TAG);
    }

}