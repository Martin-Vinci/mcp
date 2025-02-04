package com.micropay.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;

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
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by developer on 9/19/18.
 */

public class PINChange extends Fragment {

    private final String TAG = PINChange.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private TextInputEditText current_pin, new_pin, confirm_pin;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fpin_change, container, false);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());
        current_pin = rootView.findViewById(R.id.current_pin);
        new_pin = rootView.findViewById(R.id.new_pin);
        confirm_pin = rootView.findViewById(R.id.confirm_pin);

        if (getActivity() instanceof FragmentHandler) {
            FragmentHandler loanActivity = (FragmentHandler) getActivity();
            loanActivity.setTitle("PIN Change");
        }
        rootView.findViewById(R.id.confirm_changes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isValidInputSet();
            }
        });

        if (cacheUtil.isStaffAgent()) {
            showSuccessAlertDialog("You can only change your password from the branch");
        }

        setRetainInstance(true);
        return rootView;
    }

    private void isValidInputSet() {
        if (TextUtils.isEmpty(current_pin.getText()) || current_pin.getText().length() < 4) {
            current_pin.requestFocus();
            showAlertDialog("PIN should be 4 digits");
            return;
        }
        if (TextUtils.isEmpty(new_pin.getText()) || new_pin.getText().length() < 4) {
            new_pin.requestFocus();
            showAlertDialog("PIN should be 4 digits");
            return;
        }
        if (TextUtils.isEmpty(confirm_pin.getText()) || confirm_pin.getText().length() < 4) {
            confirm_pin.requestFocus();
            showAlertDialog("PIN should be 4 digits");
            return;
        }
        if (!confirm_pin.getText().toString().equals(new_pin.getText().toString())) {
            confirm_pin.requestFocus();
            showAlertDialog("PIN mismatch. New PIN and Confirmation do not match.");
            return;
        }
        jsonObject = new JSONObject();
        try {
            jsonObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            jsonObject
                    .put("oldPin", current_pin.getText().toString())
                    .put("newPin", new_pin.getText().toString())
                    .put("confirmPin", confirm_pin.getText().toString())
                    .put("activity", TAG);
            callOutletPINChangeApi(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private JSONObject jsonObject;

    private void callOutletPINChangeApi(JSONObject jsonObject) {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/changePIN", jsonObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("0".equals(response.optString("responseCode")))
                                        showSuccessAlertDialog("PIN Change completed");
                                    else
                                        showAlertDialog(response.optString("responseMessage"));
                                } else
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Authorizing.",
                        false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                            .replace(R.id.container, new AccountMenu()).addToBackStack(null)
                                            .commit();
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
