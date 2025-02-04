package com.micropay.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by developer on 9/19/18.
 */

public class AgentPINReset extends Fragment {

    private final String TAG = AgentPINReset.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private TextInputEditText new_pin, confirm_pin;
    private JSONObject agentDetails;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dpin_change, container, false);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        new_pin = rootView.findViewById(R.id.outletPaswd);
        confirm_pin = rootView.findViewById(R.id.outletPaswd2);
        agentDetails = getAgentDetails();

        rootView.findViewById(R.id.confirm_changes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isValidInputSet())
                    callOutletPINResetApi();
            }
        });
        if ("STAFF AGENT".equalsIgnoreCase(cacheUtil.getString("agentType"))) {
            showSuccessAlertDialog("You can only change your password from the branch");
        }

        setRetainInstance(true);
        return rootView;
    }

    private boolean isValidInputSet() {
        if (TextUtils.isEmpty(new_pin.getText()) || new_pin.getText().length() < 5) {
            new_pin.requestFocus();
            showAlertDialog("Invalid new password length (expected 5 digit code)");
            return false;
        }
        if (TextUtils.isEmpty(confirm_pin.getText()) || confirm_pin.getText().length() < 5) {
            confirm_pin.requestFocus();
            showAlertDialog("Invalid new password confirmation length (expected 5 digit code)");
            return false;
        }
        if (!confirm_pin.getText().toString().equals(new_pin.getText().toString())) {
            confirm_pin.requestFocus();
            showAlertDialog("Password mismatch. New password and confirmation do not match.");
            return false;
        }
        return true;
    }

    private void callOutletPINResetApi() {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/resetOutletPIN",
                        NetworkUtil.getBaseRequest(getActivity())
                                .put("outletCode", cacheUtil.getString("OUTLET_CD"))
                                .put("agentNo", agentDetails.optString("AGENT_NO"))
                                .put("activity", TAG),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null)
                                    showSuccessAlertDialog(response.optString("responseTxt"));
                                else
                                    showAlertDialog(getActivity().getString(R.string.resp_timeout));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Authorizing...", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog(getActivity().getResources().getString(R.string.network_error));
        }
    }

    private JSONObject getAgentDetails() {
        try {
            return new JSONObject(cacheUtil.getString("self_service"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public void showAlertDialog(String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
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

    public void showSuccessAlertDialog(String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            builder.setMessage(body)
                    .setPositiveButton("OK",
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

    @Override
    public void onDestroy() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        super.onDestroy();
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
        NetworkUtil.getInstance(getActivity().getApplicationContext()).cancel(TAG);
    }
}
