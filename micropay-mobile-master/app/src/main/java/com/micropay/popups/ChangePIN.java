package com.micropay.popups;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import com.micropay.api.ServiceManager;
import com.micropay.micropay.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by micropay on 01/25/2021.
 */

public class ChangePIN extends Fragment {

    private final String TAG = ChangePIN.class.getSimpleName();
    private EditText outletPaswd, outletPaswd2;
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private String outletCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.dpin_change, container, false);

        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        final TextView outletLabel = rootView.findViewById(R.id.welcome_txt);
        outletLabel.setText(cacheUtil.getString("outletCode"));

        outletPaswd = rootView.findViewById(R.id.outletPaswd);
        outletPaswd2 = rootView.findViewById(R.id.outletPaswd2);

        Bundle arguments = getArguments();
        outletCode = arguments.getString(Constants.KEY);

        rootView.findViewById(R.id.change_password).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Change the password here.
                if (validateCredentials()) {
                    try {
                        JSONObject baseRequest = NetworkUtil.getBaseRequest(getActivity());
                        baseRequest.put("outletCode", outletCode);
                        baseRequest.put("outletPaswd", ServiceManager.encouple(outletPaswd.getText().toString()))
                                .put("activity", TAG)
                                .put("agentNo", cacheUtil.getString("agentNo"));
                        callOutletPINChangeApi(baseRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        return rootView;
    }

    private boolean validateCredentials() {
        if (TextUtils.isEmpty(outletPaswd.getText())) {
            showAlertDialog("Please enter a valid password");
            outletPaswd.requestFocus();
            return false;
        }
        if (outletPaswd.getText().length() < 5) {
            showAlertDialog("Invalid password length (5+)");
            outletPaswd.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(outletPaswd2.getText())) {
            showAlertDialog("Please retype the same password");
            outletPaswd2.requestFocus();
            return false;
        }
        if (outletPaswd.getText().toString().equals(outletCode)) {
            showAlertDialog("You cannot use the same password...");
            return false;
        }
        if (!outletPaswd.getText().toString().equals(outletPaswd2.getText().toString())) {
            showAlertDialog("Passwords not matching...");
            return false;
        }
        return true;
    }

    private void callOutletPINChangeApi(final JSONObject baseRequest) {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/changeOutletPIN", baseRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.cancel();
                                outletPaswd.setText(null);
                                outletPaswd2.setText(null);
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        showLoginWindowDialog(response.optString("responseTxt"));
                                    } else {
                                        cacheUtil.putBoolean("hasUsedPIN", false);
                                        showAlertDialog(response.optString("responseTxt"));
                                    }
                                } else
                                    showAlertDialog(getString(R.string.network_error));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.cancel();
                        outletPaswd.setText(null);
                        outletPaswd2.setText(null);
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Processing Details.", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog(getString(R.string.unable_to_connect));
        }
    }

    public void showLoginWindowDialog(String body) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            builder.setMessage(body)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                    getActivity().getSupportFragmentManager().popBackStack();
                                }
                            }).setCancelable(false);
            alertDialog = builder.create();
            if (!getActivity().isFinishing()) {
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
            if (!getActivity().isFinishing()) {
                alertDialog.show();
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtil.getInstance(getActivity().getApplicationContext()).cancel(TAG);
    }

}
