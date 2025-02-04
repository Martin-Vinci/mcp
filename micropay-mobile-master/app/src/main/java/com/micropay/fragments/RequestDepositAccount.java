package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RequestDepositAccount extends Fragment {

    private final String TAG = RequestDepositAccount.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private Spinner risk_class, source_of_funds, marketing_reasons, opening_reason, product_list;
    private JSONObject requestObject;

    private long custId, customerAge, customerTypeId;
    private String custNo, custNm;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fragment_account_request, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Account Creation");

        risk_class = rootView.findViewById(R.id.risk_class);
        source_of_funds = rootView.findViewById(R.id.source_of_funds);
        marketing_reasons = rootView.findViewById(R.id.marketing_reasons);
        opening_reason = rootView.findViewById(R.id.opening_reason);
        product_list = rootView.findViewById(R.id.product_list);

        rootView.findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isValidAccountRequest();
            }
        });

        Bundle arguments = getArguments();
        if (arguments != null) {
            custId = arguments.getLong("CUST_ID");
            custNo = arguments.getString("CUST_NO");
            customerAge = arguments.getLong("AGE");
            customerTypeId = arguments.getLong("CUST_TY_ID");
            this.custNm = arguments.getString("CUST_NM");
            callAccountOpeningRequirementsApi();
        }
        
        setRetainInstance(true);
        return rootView;
    }

    public void showAlertDialog(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        if (title != null)
            builder.setTitle(title);
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

    private void callAccountOpeningRequirementsApi() {
        try {

            JSONObject outletCredentials = NetworkUtil.getBaseRequest(getActivity());
            if (cacheUtil.getBoolean("isTeller", false)) {
                outletCredentials.put("bankOfficerId", cacheUtil.getLong("userId", -99l))
                        .put("agentNo", cacheUtil.getString("userLoginId"))
                        .put("agentType", cacheUtil.getString("agentType"))
                        .put("buRoleId", cacheUtil.getLong("buRoleId", -99l))
                        .put("buId", cacheUtil.getLong("buId", -99l));
            } else {
                outletCredentials.put("agentAcctNo", cacheUtil.getString("OUTLET_FLOAT_ACCT"))
                        .put("agentType", cacheUtil.getString("agentType"))
                        .put("buId", cacheUtil.getLong("OUTLET_BRANCH_ID", -99l))
                        .put("agentNo", cacheUtil.getString("AGENT_NO"));
            }

            JSONObject request = new JSONObject()
                    .put("outletCredentials", outletCredentials)
                    .put("customerTypeId", customerTypeId)
                    .put("customerId", custId)
                    .put("customerAge", customerAge)
                    .put("activity", TAG);

            if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/getAccountOpeningRequirements", request,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        //handle reset option here
                                        prepareViewsForDisplay(response);
                                    } else {
                                        showAlertDialogAndExit(response.optString("responseTxt"));
                                    }
                                } else
                                    showAlertDialog(getActivity().getString(R.string.resp_timeout));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.cancel();
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Verifying Customer", false);
            } else {
                showAlertDialog("Cannot connect to server. No network available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareViewsForDisplay(JSONObject response) {

        try {
            JSONArray risk_class_list = response.optJSONArray("risk_class");
            if (risk_class_list != null && risk_class_list.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < risk_class_list.length(); i++) {
                    JSONObject jsonObject = risk_class_list.optJSONObject(i);
                    list.add(jsonObject.optLong("RISK_ID") + " - " + jsonObject.optString("RISK_DESC"));
                }
                Collections.sort(list);
                list.add(0, "Choose One");
                ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, list);
                risk_class.setAdapter(loanPurposesAdapter);
            }

            JSONArray campaign_list = response.optJSONArray("campaign_list");
            if (campaign_list != null && campaign_list.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < campaign_list.length(); i++) {
                    JSONObject jsonObject = campaign_list.optJSONObject(i);
                    list.add(jsonObject.optLong("CAMPAIGN_ID") + " - " + jsonObject.optString("CAMPAIGN_DESC"));
                }
                Collections.sort(list);
                list.add(0, "Choose One");
                ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, list);
                marketing_reasons.setAdapter(loanPurposesAdapter);
            }

            JSONArray opening_reasons = response.optJSONArray("opening_reasons");
            if (opening_reasons != null && opening_reasons.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < opening_reasons.length(); i++) {
                    JSONObject jsonObject = opening_reasons.optJSONObject(i);
                    list.add(jsonObject.optLong("RSN_ID") + " - " + jsonObject.optString("RSN_DESC"));
                }
                Collections.sort(list);
                list.add(0, "Choose One");
                ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, list);
                opening_reason.setAdapter(loanPurposesAdapter);
            }

            JSONArray dep_product_list = response.optJSONArray("product_list");
            if (dep_product_list != null && dep_product_list.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < dep_product_list.length(); i++) {
                    JSONObject jsonObject = dep_product_list.optJSONObject(i);
                    list.add(jsonObject.optLong("PROD_ID") + " - " + jsonObject.optString("PROD_DESC"));
                }
                Collections.sort(list);
                list.add(0, "Choose One");
                ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, list);
                product_list.setAdapter(loanPurposesAdapter);
            }

            JSONArray funds_source = response.optJSONArray("source_of_funds");
            if (funds_source != null && funds_source.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < funds_source.length(); i++) {
                    JSONObject jsonObject = funds_source.optJSONObject(i);
                    list.add(jsonObject.optLong("SRC_OF_FUNDS_ID") + " - " + jsonObject.optString("SRC_OF_FUNDS_DESC"));
                }
                Collections.sort(list);
                list.add(0, "Choose One");
                ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, list);
                source_of_funds.setAdapter(loanPurposesAdapter);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlertDialog(String body) {
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

    public void showAlertDialogAndExit(String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        builder.setMessage(body)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                goBackToPreviousFragment();
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (alertDialog != null && !alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    private void goBackToPreviousFragment() {
        FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
        if (supportFragmentManager != null && supportFragmentManager.getBackStackEntryCount() > 0) {
            boolean done = supportFragmentManager.popBackStackImmediate();
        }
    }

    public void showAccountCreatedDialog(String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        builder.setMessage(body)
                .setPositiveButton("PRINT RECEIPT",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        })
                .setPositiveButton("CLOSE",
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

    private void isValidAccountRequest() {
        try {
            if (product_list.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Product Selected");
                return;
            }
            if (opening_reason.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Account Opening Reason Selected");
                return;
            }
            if (marketing_reasons.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Marketing Campaign Reason Selected");
                return;
            }
            if (source_of_funds.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Source of Funds Selected");
                return;
            }
            if (risk_class.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Risk Class Level Selected");
                return;
            }


            JSONObject outletCredentials = NetworkUtil.getBaseRequest(getActivity());

            if (cacheUtil.getBoolean("isTeller", false)) {
                outletCredentials.put("bankOfficerId", cacheUtil.getLong("userId", -99l))
                        .put("agentNo", cacheUtil.getString("userLoginId"))
                        .put("agentType", cacheUtil.getString("agentType"))
                        .put("buRoleId", cacheUtil.getLong("buRoleId", -99l))
                        .put("buId", cacheUtil.getLong("buId", -99l));
            } else {
                outletCredentials.put("agentAcctNo", cacheUtil.getString("OUTLET_FLOAT_ACCT"))
                        .put("agentType", cacheUtil.getString("agentType"))
                        .put("agentNo", cacheUtil.getString("AGENT_NO"))
                        .put("buId", cacheUtil.getLong("OUTLET_BRANCH_ID", -99l));
            }
            outletCredentials.put("outletCode", cacheUtil.getString("outletCode"));

            requestObject = new JSONObject()
                    .put("outletCredentials", outletCredentials)
                    .put("sourceOfFundsId", Long.valueOf(source_of_funds.getSelectedItem().toString().split("-")[0].trim()))
                    .put("productId", Long.valueOf(product_list.getSelectedItem().toString().split("-")[0].trim()))
                    .put("campaignId", Long.valueOf(marketing_reasons.getSelectedItem().toString().split("-")[0].trim()))
                    .put("openingReasonId", Long.valueOf(opening_reason.getSelectedItem().toString().split("-")[0].trim()))
                    .put("riskClassId", Long.valueOf(risk_class.getSelectedItem().toString().split("-")[0].trim()))
                    .put("customerId", custId)
                    .put("accountTitle", custNm)
                    .put("openingDate", cacheUtil.getLong("AGE", 15))
                    .put("activity", TAG);

            callAccountCreationApi(requestObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void callAccountCreationApi(final JSONObject custRequest) {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/createDepositAccount", custRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (progressDialog != null && progressDialog.isShowing())
                                    progressDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        //show account created dialog and exit
                                        showAccountCreatedDialog(response.optString("responseTxt"));
                                    } else {
                                        showAlertDialog(response.optString("responseTxt"));
                                    }
                                } else
                                    showAlertDialog(getActivity().getString(R.string.resp_timeout));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.cancel();
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
                progressDialog = ProgressDialog.show(getActivity(), "", "Authorizing");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog("Cannot connect to server. No network available");
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
