package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class RequestCreditApplication extends Fragment implements AdapterView.OnItemSelectedListener {

    private final String TAG = RequestCreditApplication.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private TextInputEditText credit_amt, term_val;
    private Spinner repayment_acct, term_cd, credit_purpose, credit_portfolio, credit_type, credit_product;
    private JSONObject requestObject;
    private String cust_no;
    private long cust_id, cust_ty_id;
    private JSONObject response;
    private JSONObject outletCredentials;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fcredit_request, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Loan Application");

        repayment_acct = rootView.findViewById(R.id.repayment_acct);
        term_val = rootView.findViewById(R.id.term_val);
        term_cd = rootView.findViewById(R.id.term_cd);
        credit_purpose = rootView.findViewById(R.id.credit_purpose);
        credit_portfolio = rootView.findViewById(R.id.credit_portfolio);
        credit_product = rootView.findViewById(R.id.credit_product);

        credit_type = rootView.findViewById(R.id.credit_type);
        credit_amt = rootView.findViewById(R.id.credit_amt);

        rootView.findViewById(R.id.apply).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isValidCreditRequest();
            }
        });


        Bundle arguments = getArguments();
        if (arguments != null) {
            this.cust_no = arguments.getString("CUST_NO");
            this.cust_id = arguments.getLong("CUST_ID");
            this.cust_ty_id = arguments.getLong("CUST_TY_ID");
            callExtraLoanMetaDataApi();
        }

        setRetainInstance(true);
        return rootView;
    }

    private void callExtraLoanMetaDataApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {

                JSONObject put = new JSONObject()
                        .put("outletCredentials", NetworkUtil.getBaseRequest(getActivity()))
                        .put("customerNo", cust_no)
                        .put("customerId", cust_id)
                        .put("customerTypeId", cust_ty_id)
                        .put("activity", TAG);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/getCreditApplicationRequirements", put,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        //handle reset option here
                                        prepareViewsForDisplay(response);
                                        RequestCreditApplication.this.response = response;
                                    } else {
                                        showAlertDialogAndExit(response.optString("responseTxt"));
                                    }
                                } else
                                    showAlertDialogAndExit(getActivity().getString(R.string.resp_timeout));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.cancel();
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
                NetworkUtil.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Authorizing...", false);
            } else {
                showAlertDialogAndExit("Cannot connect to server. No network available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void prepareViewsForDisplay(final JSONObject response) {

        try {
            JSONArray account_list = response.optJSONArray("account_list");
            if (account_list != null && account_list.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < account_list.length(); i++) {
                    JSONObject jsonObject = account_list.optJSONObject(i);
                    list.add(jsonObject.optLong("ACCT_ID") + " - " + jsonObject.optString("ACCT_NO"));
                }
                Collections.sort(list);
                list.add(0, "Choose One");
                ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, list);
                repayment_acct.setAdapter(loanPurposesAdapter);
            } else {
                //customer does not have any repayment account
                showAlertDialog("No Deposit Accounts", "This customer does not have any deposit accounts on their profile");
            }

            JSONArray credit_purposes = response.optJSONArray("credit_purpose");
            if (credit_purposes != null && credit_purposes.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < credit_purposes.length(); i++) {
                    JSONObject jsonObject = credit_purposes.optJSONObject(i);
                    list.add(jsonObject.optLong("RSN_ID") + " - " + jsonObject.optString("RSN_DESC"));
                }
                Collections.sort(list);
                list.add(0, "Choose One");
                ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, list);
                credit_purpose.setAdapter(loanPurposesAdapter);
            }

            JSONArray credit_types = response.optJSONArray("credit_type");
            if (credit_types != null && credit_types.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < credit_types.length(); i++) {
                    JSONObject jsonObject = credit_types.optJSONObject(i);
                    list.add(jsonObject.optLong("CR_TY_ID") + " - " + jsonObject.optString("CR_TY_DESC"));
                }
                Collections.sort(list);
                list.add(0, "Choose One");
                ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, list);
                credit_type.setAdapter(loanPurposesAdapter);
                credit_type.setOnItemSelectedListener(this);
            }

            JSONArray credit_portfolios = response.optJSONArray("credit_portfolio");
            if (credit_portfolios != null && credit_portfolios.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                for (int i = 0; i < credit_portfolios.length(); i++) {
                    JSONObject jsonObject = credit_portfolios.optJSONObject(i);
                    list.add(jsonObject.optLong("PORTFOLIO_ID") + " - " + jsonObject.optString("PORTFOLIO_DESC"));
                }
                Collections.sort(list);
                list.add(0, "Choose One");
                ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, list);
                credit_portfolio.setAdapter(loanPurposesAdapter);
            }

            JSONArray term_codes = response.optJSONArray("term_codes");
            if (term_codes != null && term_codes.length() > 0) {
                ArrayList<String> list = new ArrayList<String>();
                list.add(0, "Choose One");
                for (int i = 0; i < term_codes.length(); i++) {
                    list.add(term_codes.optString(i));
                }
                ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                        android.R.layout.simple_spinner_dropdown_item, list);
                term_cd.setAdapter(loanPurposesAdapter);
            }

            credit_product.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    if (position > 0) {
                        String selectedProductType = String.valueOf(credit_product.getSelectedItem());
                        if (selectedProductType != null && selectedProductType.contains("-")) {
                            Long productId = Long.valueOf(selectedProductType.split("-")[0].trim());
                            JSONArray basic_info = response.optJSONArray("basic_info");
                            if (basic_info != null && basic_info.length() > 0) {
                                ArrayList<String> list = new ArrayList<String>();
                                for (int index = 0; index < basic_info.length(); index++) {
                                    JSONObject jsonObject = basic_info.optJSONObject(index);
                                    if (jsonObject.optLong("PROD_ID") == productId) {
                                        //set the term code and value
                                        min_term_val = jsonObject.optLong("MIN_TERM_VALUE");
                                        max_term_val = jsonObject.optLong("MAX_TERM_VALUE");

                                        max_term_amt = jsonObject.optLong("MAX_LOAN_AMT");
                                        min_term_amt = jsonObject.optLong("MIN_LOAN_AMT");

                                        min_term_code = jsonObject.optString("MIN_TERM_CD");
                                        max_term_code = jsonObject.optString("MAX_TERM_CD");

                                        term_cd.setSelection(getTermPosition(jsonObject.optString("MIN_TERM_CD")));
                                        term_val.setText(String.valueOf(jsonObject.optLong("MIN_TERM_VALUE")));
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int getTermPosition(String min_term_cd) {
        int position = 0;
        JSONArray term_codes = response.optJSONArray("term_codes");
        if (term_codes != null && term_codes.length() > 0) {
            for (int i = 0; i < term_codes.length(); i++) {
                if (term_codes.optString(i).startsWith(min_term_cd)) {
                    position = i + 1;
                    break;
                }
            }
        }
        return position;
    }

    private long max_term_amt, min_term_amt, min_term_val, max_term_val;
    private String min_term_code, max_term_code;

    public void showAlertDialog(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        if (title != null)
            builder.setTitle(title);
        builder.setMessage(body)
                .setNegativeButton("CLOSE",
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

    private void isValidCreditRequest() {
        try {
            if (credit_type.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Credit Type Selected");
                return;
            }
            if (credit_product.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Credit Product Selected");
                return;
            }
            if (credit_portfolio.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Credit Portfolio Level Selected");
                return;
            }
            if (credit_purpose.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Credit Purpose Selected");
                return;
            }
            if (TextUtils.isEmpty(term_val.getText())) {
                showAlertDialog("Invalid Account Opening Reason Selected");
                return;
            }
            if (term_cd.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Loan Term Selected");
                return;
            }
            if (TextUtils.isEmpty(credit_amt.getText())) {
                showAlertDialog("Invalid Credit Amount Selected");
                return;
            }
            if (repayment_acct.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid Repayment Account Selected");
                return;
            }
            if (Long.valueOf(credit_amt.getText().toString()) > max_term_amt) {
                showAlertDialog("Specified Credit Amount is above the required maximum for this product");
                return;
            }
            if (Long.valueOf(credit_amt.getText().toString()) < min_term_amt) {
                showAlertDialog("Specified Credit Amount is below the required minimum for this product");
                return;
            }

            outletCredentials = NetworkUtil.getBaseRequest(getActivity());
            if (cacheUtil.getBoolean("isTeller", false)) {
                outletCredentials.put("bankOfficerId", cacheUtil.getLong("userId", -99l))
                        .put("agentNo", cacheUtil.getString("userLoginId"))
                        .put("agentType", cacheUtil.getString("agentType"))
                        .put("buRoleId", cacheUtil.getLong("buRoleId", -99l))
                        .put("buId", cacheUtil.getLong("buId", -99l));
            } else {
                outletCredentials.put("agentAcctNo", cacheUtil.getString("OUTLET_FLOAT_ACCT"))
                        .put("agentType", cacheUtil.getString("agentType"))
                        .put("agentNo", cacheUtil.getString("AGENT_NO"));
            }

            requestObject = new JSONObject()
                    .put("outletCredentials", outletCredentials)
                    .put("loanAmount", new BigDecimal(credit_amt.getText().toString().trim()))
                    .put("creditPurposeId", Long.valueOf(credit_purpose.getSelectedItem().toString().split("-")[0].trim()))
                    .put("creditPortfolioId", Long.valueOf(credit_portfolio.getSelectedItem().toString().split("-")[0].trim()))
                    .put("settlementAcctId", Long.valueOf(repayment_acct.getSelectedItem().toString().split("-")[0].trim()))
                    .put("settlementAcctNo", repayment_acct.getSelectedItem().toString().split("-")[1].trim())
                    .put("customerId", cust_id)
                    .put("customerNo", cust_no)
                    .put("productId", Long.valueOf(credit_product.getSelectedItem().toString().split("-")[0].trim()))
                    .put("repayAcctId", Long.valueOf(repayment_acct.getSelectedItem().toString().split("-")[0].trim()))
                    .put("creditTypeId", Long.valueOf(credit_type.getSelectedItem().toString().split("-")[0].trim()))
                    .put("termCode", term_cd.getSelectedItem().toString().substring(0, 1))
                    .put("termValue", Long.valueOf(term_val.getText().toString().trim()))
                    .put("activity", TAG);

            callCreditApplicationApi(requestObject);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void callCreditApplicationApi(final JSONObject baseRequest) {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/loanApplication", baseRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        showAlertDialog("Application Status", response.optString("responseTxt"));
                                    } else {
                                        showAlertDialog(response.optString("responseTxt"));
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Processing...", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog("Cannot connect to server. No network available");
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
                                getActivity().getSupportFragmentManager().popBackStack();
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (alertDialog != null && !alertDialog.isShowing()) {
            alertDialog.show();
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
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if (position > 0) {
            String selectedCreditType = String.valueOf(credit_type.getSelectedItem());
            if (selectedCreditType != null && selectedCreditType.contains("-")) {
                Long creditTypeId = Long.valueOf(selectedCreditType.split("-")[0].trim());
                JSONArray products_list = response.optJSONArray("products_list");
                if (products_list != null && products_list.length() > 0) {
                    ArrayList<String> list = new ArrayList<String>();
                    for (int index = 0; index < products_list.length(); index++) {
                        JSONObject jsonObject = products_list.optJSONObject(index);
                        if (jsonObject.optLong("CR_TY_ID") == creditTypeId) {
                            list.add(jsonObject.optLong("PROD_ID") + " - " + jsonObject.optString("PROD_DESC"));
                        }
                    }
                    Collections.sort(list);
                    list.add(0, "Choose One");
                    ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                            android.R.layout.simple_spinner_dropdown_item, list);
                    credit_product.setAdapter(loanPurposesAdapter);
                }
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }
}
