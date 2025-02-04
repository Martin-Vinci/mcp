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
import java.util.HashMap;
import java.util.Map;

public class RequestChequeBook extends Fragment {

    private final String TAG = RequestChequeBook.class.getSimpleName();
    private CacheUtil cacheUtil;

    private AlertDialog alertDialog;

    private TextInputEditText acct_no, account_title, no_of_cheque;
    private Spinner type_list;

    private Long crncy_id, cust_id;
    private Map<Integer, Long> chequeMap = new HashMap<>();

    private JSONObject requestObject;
    private String cust_no;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));

        View rootView = inflater.inflate(R.layout.fcheque_request, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Cheque Request");

        acct_no = rootView.findViewById(R.id.acct_no);
        account_title = rootView.findViewById(R.id.account_title);
        no_of_cheque = rootView.findViewById(R.id.no_of_cheque);
        type_list = rootView.findViewById(R.id.type_list);

        try {
            Bundle arguments = getArguments();
            acct_no.setText(arguments.getString("ACCT_NO"));
            account_title.setText(arguments.getString("ACCT_NM"));
            crncy_id = arguments.getLong("CRNCY_ID");
            cust_id = arguments.getLong("PRIMARY_CUST_ID");
            this.cust_no = arguments.getString("CUST_NO");
            callProductChequesListApi(arguments.getLong("PROD_ID"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        rootView.findViewById(R.id.request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isValidChequeBody();
            }
        });

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

    private void callChequeBookRequestApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/chequeBookRequest", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        //handle reset option here cheque_books
                                        showAlertDialogAndExit(response.optString("responseTxt"));
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Authorizing...", false);
            } else {
                showAlertDialog("Cannot connect to server. No network available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callProductChequesListApi(long prod_id) {
        try {
            if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/searchChequeBookProducts", new JSONObject()
                        .put("outletCredentials", NetworkUtil.getBaseRequest(getActivity()))
                        .put("prodId", prod_id)
                        .put("activity", TAG),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        //handle reset option here cheque_books
                                        renderDisplay(response);
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
            } else {
                showAlertDialog("Cannot connect to server. No network available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderDisplay(JSONObject response) {
        JSONArray cheque_books = response.optJSONArray("cheque_books");
        if (cheque_books != null && cheque_books.length() > 0) {
            ArrayList<String> list = new ArrayList<String>();
            list.add(0, "Choose One");
            for (int i = 0; i < cheque_books.length(); i++) {
                JSONObject jsonObject = cheque_books.optJSONObject(i);
                list.add(jsonObject.optLong("REORDER_TRIGGER_LEVEL_NO") + "-" +
                        jsonObject.optString("CHQ_BOOK_TY") + " - " + jsonObject.optString("CHQ_BOOK_NM"));
                chequeMap.put(i, jsonObject.optLong("REORDER_TRIGGER_LEVEL_NO"));
            }
            ArrayAdapter<String> loanPurposesAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, list);
            type_list.setAdapter(loanPurposesAdapter);
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
                                getActivity().finish();
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (alertDialog != null && !alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    private void isValidChequeBody() {

        if (TextUtils.isEmpty(acct_no.getText())) {
            showAlertDialog("Please specify a valid account");
            return;
        }
        if (TextUtils.isEmpty(account_title.getText())) {
            showAlertDialog("Please specify a valid account title");
            return;
        }
        if (TextUtils.isEmpty(no_of_cheque.getText())) {
            showAlertDialog("Please specify a valid number of books");
            no_of_cheque.requestFocus();
            return;
        }

        if (type_list.getSelectedItemPosition() <= 0) {
            showAlertDialog("Please select a valid cheque book type");
            type_list.requestFocus();
            return;
        }
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
                        .put("agentNo", cacheUtil.getString("AGENT_NO"))
                        .put("buId", cacheUtil.getLong("OUTLET_BRANCH_ID", -99l));
            }

            outletCredentials.put("outletCode", cacheUtil.getString("OUTLET_CD"));
            requestObject = new JSONObject()
                    .put("outletCredentials", outletCredentials)
                    .put("chequeBookTypeCode", type_list.getSelectedItem().toString().split("-")[1].trim())
                    .put("reOrderLevel", Long.parseLong(type_list.getSelectedItem().toString().split("-")[0].trim()))
                    .put("accountTitle", account_title.getText().toString())
                    .put("accountNo", acct_no.getText().toString())
                    .put("customerId", cust_id)
                    .put("customerNo", cust_no)
                    .put("curencyId", crncy_id)
                    .put("numberOfCheques", Integer.valueOf(no_of_cheque.getText().toString()))
                    .put("activity", TAG);

            callChequeBookRequestApi();
        } catch (JSONException e) {
            e.printStackTrace();
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
