package com.micropay.fragments;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.marcoscg.fingerauth.FingerAuthDialog;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.micropay.AgentHomeActivity;
import com.micropay.micropay.CustomerHomeActivity;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.R;
import com.micropay.micropay.SuperAgentHomeActivity;
import com.micropay.popups.ChangePIN;
import com.micropay.popups.DialogUtils;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.HashMap;
import java.util.Map;

public class AgentAuthentication extends Fragment {

    private final String TAG = AgentAuthentication.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private EditText pinNo;
    private FingerAuthDialog fingerAuthDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fsignin_agent, container, false);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));

        cacheUtil = new CacheUtil(getActivity().getApplicationContext());
        cacheUtil.updateAll();
        //phoneNo = rootView.findViewById(R.id.phoneNo);
        pinNo = rootView.findViewById(R.id.pinNo);

//        Drawable[] compoundDrawables=pinNo.getCompoundDrawables();
//        Drawable drawableRight=compoundDrawables[2].mutate();
//        drawableRight.setColorFilter(new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN));

        //Check if the user has registered the outlet
        String registeredPhone = cacheUtil.getString("registeredPhone");
//        if (!"".equals(registeredPhone)) {
//            ((TextView) rootView.findViewById(R.id.outletLabel)).setText(cacheUtil.getString("registeredPhone"));
//            //if the outlet code is already verified. hide it.
//            phoneNo.setVisibility(View.GONE);
//        } else {
//            //check if at least the agent details are present
//            phoneNo.setVisibility(View.VISIBLE);
//        }

        String agent = capitalize(cacheUtil.getString("customerName").trim()
                .split(" ")[0].toLowerCase());

        if ("CUSTOMER".equals(cacheUtil.getString("entityType")))
            ((TextView) rootView.findViewById(R.id.welcomeLabel))
                    .setText("Welcome " + agent);
        else {
            String agentCode = cacheUtil.getString("outletCode");
            String welcomeText = "Welcome " + agent + "\n";
            welcomeText += "Outlet Code: " + agentCode;
            ((TextView) rootView.findViewById(R.id.welcomeLabel))
                    .setText(welcomeText);
        }

//        rootView.findViewById(R.id.locate_agent).setVisibility(View.GONE);
//        rootView.findViewById(R.id.forgot_pin).setVisibility(View.GONE);

        rootView.findViewById(R.id.authorize).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (validateCredentials()) {
                    try {
                        JSONObject baseRequest = NetworkUtil.getBaseRequest(getActivity());
//                        if (phoneNo.getVisibility() == View.VISIBLE) {
//                            baseRequest.put("phoneNo", phoneNo.getText().toString());
//                        } else
                        baseRequest.put("pinNo", pinNo.getText().toString());
                        baseRequest.put("activity", TAG);
                        callOutletAuthenticationApi(baseRequest);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        rootView.findViewById(R.id.device_id).setVisibility(View.GONE);
        setRetainInstance(true);

        return rootView;
    }


    private String capitalize(String string) {
        StringBuilder sb = new StringBuilder(string);
        sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        return sb.toString();
    }

    private boolean validateCredentials() {
//        if (phoneNo.getVisibility() == View.VISIBLE && TextUtils.isEmpty(phoneNo.getText())) {
//            showBasicAlertDialog(getString(R.string.invalid_outlet_code));
//            phoneNo.requestFocus();
//            return false;
//        }
        if (TextUtils.isEmpty(pinNo.getText())) {
            showBasicAlertDialog(getString(R.string.invalid_paswd));
            pinNo.requestFocus();
            return false;
        }
        if (pinNo.getText().length() < 4) {
            showBasicAlertDialog(getString(R.string.invalid_paswd_length));
            pinNo.requestFocus();
            return false;
        }
        return true;
    }

    private void callOutletAuthenticationApi(final JSONObject baseRequest) {

        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/signIn", baseRequest
                        .put("newDeviceFlag", "N"),
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            pinNo.setText(null);

                            if (response != null) {
                                if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                    cacheUtil.putString("pswd", pinNo.getText().toString());
                                    if ("OUTLET".equals(cacheUtil.getString("entityType")))
                                        clearAndStartNewActivity(AgentHomeActivity.class);
                                    if ("SUPER_AGENT".equals(cacheUtil.getString("entityType")))
                                        clearAndStartNewActivity(SuperAgentHomeActivity.class);
                                    if ("CUSTOMER".equals(cacheUtil.getString("entityType")))
                                        clearAndStartNewActivity(CustomerHomeActivity.class);
                                    cacheUtil.putString("sessionId", response.optString("sessionId"));

                                } else {
                                    cacheUtil.putBoolean("hasUsedPIN", false);
                                    showBasicAlertDialog(response.optJSONObject("response").optString("responseMessage"));
                                }
                            } else
                                showBasicAlertDialog(getActivity().getString(R.string.resp_timeout));
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.dismiss();
                        showBasicAlertDialog(NetworkUtil.getErrorDesc(error));
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Processing..", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showBasicAlertDialog(getString(R.string.unable_to_connect));
        }
    }

    public void showAlertDialogForExit(String title, String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            if (title != null)
                builder.setTitle(title);
            builder.setMessage(body)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                    getActivity().finish();
                                }
                            }).setCancelable(false);
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            alertDialog = builder.create();
            if (alertDialog != null && !alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private void showChangePasswordDialog(String outletCode) {
        try {
            Bundle bundle = new Bundle();
            bundle.putString(Constants.KEY, outletCode);
            ChangePIN roleSheet = new ChangePIN();
            roleSheet.setArguments(bundle);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, roleSheet).addToBackStack(null).commit();
        } catch (Exception e) {
            showBasicAlertDialog(e.getLocalizedMessage());
        }
    }

    public void showBasicAlertDialog(String body) {
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
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            alertDialog = builder.create();
            if (alertDialog != null && !alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private void clearAndStartNewActivity(Class<?> clazz) {
        Intent intent = new Intent(getActivity().getApplicationContext(), clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtil.getInstance(getActivity().getApplicationContext()).cancel(TAG);
    }

}
