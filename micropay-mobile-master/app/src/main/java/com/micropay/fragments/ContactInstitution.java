package com.micropay.fragments;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.popups.DialogUtils;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by developer on 9/19/18.
 */

public class ContactInstitution extends Fragment implements PermissionListener {

    private final String TAG = ContactInstitution.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;

    private TextInputEditText text_msg, phone_no, email_address, subject;
    private Button send_message;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fcontact_request, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("User Feedback");

        subject = rootView.findViewById(R.id.subject);
        text_msg = rootView.findViewById(R.id.text_msg);
        phone_no = rootView.findViewById(R.id.phone_no);
        email_address = rootView.findViewById(R.id.email_address);

        send_message = rootView.findViewById(R.id.send_msg);
        send_message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateInput();
            }
        });

        rootView.findViewById(R.id.call_us).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                placePhoneCall();
            }
        });

        setRetainInstance(true);
        return rootView;
    }

    private void placePhoneCall() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.CALL_PHONE)
                .withListener(this)
                .onSameThread()
                .check();
    }

    private void validateInput() {
        if (!checkEmail(email_address.getText().toString())) {
            email_address.requestFocus();
            showAlertDialog("Invalid email address specified");
            return;
        }
        if (TextUtils.isEmpty(subject.getText())) {
            subject.requestFocus();
            showAlertDialog("Please enter a valid message subject");
            return;
        }
        if (TextUtils.isEmpty(text_msg.getText()) || text_msg.getText().length() < 10) {
            text_msg.requestFocus();
            showAlertDialog("Please enter a valid message content");
            return;
        }
        callContactUsApi();
    }

    private boolean checkEmail(String email) {
        return Constants.EMAIL_ADDRESS_PATTERN.matcher(email).matches();
    }

    private void callContactUsApi() {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/contactInstitution",
                        NetworkUtil.getBaseRequest(getActivity())
                                .put("phone", phone_no.getText().toString())
                                .put("messageTxt", text_msg.getText().toString())
                                .put("email", email_address.getText().toString())
                                .put("subject", subject.getText().toString())
                                .put("activity", TAG),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null)
                                    showAlertDialog(response.optString("responseTxt"));
                                else
                                    showAlertDialog(getActivity().getString(R.string.resp_timeout));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.dismiss();
                        showAlertDialog(getString(R.string.no_network));
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Forwarding...", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog(getActivity().getResources().getString(R.string.network_error));
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
                                    getActivity().getSupportFragmentManager().popBackStack();
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

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {
        try {
            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + cacheUtil.getString("contact_phone")));
            startActivity(intent);
        } catch (Exception e) {
        }
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {
        showAlertDialog("Micropay mobile requires permission to access your phone call manager in order to allow you place a call");
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
        token.continuePermissionRequest();
    }
}
