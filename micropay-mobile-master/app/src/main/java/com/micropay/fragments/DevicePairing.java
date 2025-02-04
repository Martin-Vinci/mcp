package com.micropay.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.R;
import com.micropay.popups.DialogUtils;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DevicePairing extends Fragment {

    private final String TAG = DevicePairing.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private EditText activationCode;
    private Button next;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_device_pairing, container, false);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));

        cacheUtil = new CacheUtil(getActivity().getApplicationContext());
        cacheUtil.updateAll();
        activationCode = rootView.findViewById(R.id.activationCode);
        next = rootView.findViewById(R.id.proceed);

        next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidAgentCode())
                    performDevicePairing();
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
            if (TextUtils.isEmpty(activationCode.getText())) {
                showAlertDialog("Invalid Activation code specified");
                activationCode.requestFocus();
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void performDevicePairing() {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            NetworkUtil.hideSoftKeyboard(getActivity());
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/performDevicePairing",
                        NetworkUtil.getBaseRequest(getActivity())
                                .put("deviceActivationCode", activationCode.getText().toString())
                                .put("channelCode", Constants.getChannelCode())
                                .put("activity", TAG),
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if ("0".equalsIgnoreCase(response.optString("responseCode"))) {
                                    cacheUtil.putString(Constants.REGISTERED, "VERIFIED");
                                    proceedToLogin();
                                } else {
                                    cacheUtil.remove(Constants.REGISTERED);
                                    showAlertDialog(response.optString("responseMessage"));
                                }
                            } else
                                showAlertDialog(getActivity().getString(R.string.resp_timeout));
                            activationCode.setText(null);
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Processing device pairing.", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog("Cannot connect to server. No network available");
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

    public void proceedToLogin() {
        FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
        if (supportFragmentManager != null) {
            if (supportFragmentManager.getBackStackEntryCount() > 0) {
                FragmentManager.BackStackEntry first = supportFragmentManager.getBackStackEntryAt(0);
                supportFragmentManager.popBackStack(first.getId(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            }
            alertDialog = DialogUtils.showProgressDialog(getActivity(), "Please wait while the app analyses device.", false);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    goToAuthentication();
                }
            }, 5000);   //7 seconds
        }
    }

    public void goToAuthentication() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = DialogUtils.showProgressDialog(getActivity(), "Activating app on the device.", false);
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
                supportFragmentManager.beginTransaction()
                        .replace(R.id.container, new AgentAuthentication()).commit();

                showAlertDialog("Device activation completed. You can now sign in with your current PIN");
            }
        }, 7000);   //7 seconds
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