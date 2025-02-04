package com.micropay.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.popups.DialogUtils;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.R;
import com.micropay.utils.DATA_CONVERTER;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by micropay on 01/25/2021.
 */

public class ActivateAgent extends Fragment {

    private final String TAG = ActivateAgent.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private EditText phoneNo, pinNo;
    private Button next;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fverify_agent, container, false);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));

        cacheUtil = new CacheUtil(getActivity().getApplicationContext());
        cacheUtil.updateAll();
        phoneNo = rootView.findViewById(R.id.phoneNo);
        pinNo = rootView.findViewById(R.id.pinNo);
        next = rootView.findViewById(R.id.proceed);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidAgentCode())
                    callAgentVerificationApi();
            }
        });

        final TextView deviceId = rootView.findViewById(R.id.device_id);
        String deviceId1 = NetworkUtil.getDeviceId(getActivity().getApplication());
        deviceId.setText("Device ID: " + deviceId1);
        deviceId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkUtil.copyToClip(getActivity(), deviceId.getText().toString(), deviceId.getText().toString());
            }
        });

        setRetainInstance(true);
        return rootView;
    }

    private boolean isValidAgentCode() {
        try {
            if (TextUtils.isEmpty(phoneNo.getText())) {
                showAlertDialog(getString(R.string.invalid_phone_no));
                phoneNo.requestFocus();
                return false;
            }

            if (DATA_CONVERTER.getInternationalFormat(phoneNo.getText().toString()) == null) {
                showAlertDialog(getString(R.string.invalid_phone_no));
                phoneNo.requestFocus();
                return false;
            }
            if (phoneNo.getText().length() < 4) {
                showAlertDialog(getString(R.string.invalid_agent_code_length));
                phoneNo.requestFocus();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void callAgentVerificationApi() {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            NetworkUtil.hideSoftKeyboard(getActivity());
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/signIn", new JSONObject()
                        .put("deviceId", NetworkUtil.getDeviceId(getContext()))
                        .put("imeiNumber", NetworkUtil.getDeviceId(getContext()))
                        .put("deviceMake", NetworkUtil.getDeviceId(getContext()))
                        .put("deviceModel", NetworkUtil.getDeviceModel())
                        .put("newDeviceFlag", "Y")
                        .put("phoneNo", DATA_CONVERTER.getInternationalFormat(phoneNo.getText().toString()))
                        .put("pinNo", pinNo.getText().toString())
                        .put("channelCode", Constants.getChannelCode())
                        .put("activity", TAG),
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                    cacheUtil.putString("my_profile", response.toString());
                                    cacheUtil.putString("customerName", response.optString("customerName"));
                                    cacheUtil.putString("effectiveDate", response.optString("effectiveDate"));
                                    cacheUtil.putString("rimNo", response.optString("rimNo"));
                                    cacheUtil.putString("entityType", response.optString("entityType"));
                                    cacheUtil.putString("registeredPhone", DATA_CONVERTER.getInternationalFormat(phoneNo.getText().toString()));
                                    if (!response.optString("entityType").equals("CUSTOMER"))
                                        cacheUtil.putString("outletCode", response.optString("outletCode"));
                                    cacheUtil.putString("lockedFlag", response.optString("lockedFlag"));
                                    cacheUtil.putString("pinChangeFlag", response.optString("pinChangeFlag"));
                                    cacheUtil.putString("firstPinGenerated", response.optString("firstPinGenerated"));
                                    cacheUtil.putString("sessionId", NetworkUtil.getDeviceId(getActivity()));
                                    showConfirmationDialog(response);
                                } else {
                                    cacheUtil.remove(Constants.REGISTERED);
                                    showAlertDialog(response.optJSONObject("response").optString("responseMessage"));
                                }
                            } else
                                showAlertDialog(getActivity().getString(R.string.resp_timeout));
                            phoneNo.setText(null);
                            pinNo.setText(null);
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        error.printStackTrace();
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.dismiss();
                        showAlertDialog(NetworkUtil.getErrorDesc(error));
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
                NetworkUtil.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Verifying Details.", false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog("Cannot connect to server. No network available");
        }
    }

    private void performDevicePairing() {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            NetworkUtil.hideSoftKeyboard(getActivity());
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/generateDeviceActivationCode",
                        NetworkUtil.getBaseRequest(getActivity()),
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if ("0".equalsIgnoreCase(response.optString("responseCode"))) {
                                    goToAuthentication();
                                } else {
                                    cacheUtil.remove(Constants.REGISTERED);
                                    showAlertDialog(response.optString("responseMessage"));
                                }
                            } else
                                showAlertDialog(getActivity().getString(R.string.resp_timeout));
                            phoneNo.setText(null);
                            pinNo.setText(null);
                        }, error -> {
                            error.printStackTrace();
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            showAlertDialog(NetworkUtil.getErrorDesc(error));
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
                NetworkUtil.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Processing device activation.", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog("Cannot connect to server. No network available");
        }
    }


    private void showConfirmationDialog(JSONObject response) {
        if (getActivity() != null && response.length() > 0) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            builder.setMessage(response.optString("customerName"))
                    .setPositiveButton("CONTINUE",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                    performDevicePairing();
                                }
                            }).setNegativeButton("CANCEL",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                    getActivity().finish();
                                }
                            }).setCancelable(false);
            alertDialog = builder.create();
            if (alertDialog != null && !alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    public void showAlertDialog(String body) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            builder.setMessage(body)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                }
                            }).setCancelable(false);
            alertDialog = builder.create();
            if (alertDialog != null && !alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    public void goToAuthentication() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
        supportFragmentManager.beginTransaction()
                .replace(R.id.container, new DevicePairing()).commit();
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtil.getInstance(getActivity().getApplicationContext()).cancel(TAG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

}
