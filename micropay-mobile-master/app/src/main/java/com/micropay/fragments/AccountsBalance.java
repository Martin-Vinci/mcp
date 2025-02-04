package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.adaptor.StatementAdaptor;
import com.micropay.utils.NumberUtils;
import com.micropay.utils.SpinnerUtil;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AccountsBalance extends Fragment {

    private final String TAG = FullStatement.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;

    private Spinner account_list;
    private JSONObject requestObject;

    private RecyclerView recyclerView;
    private StatementAdaptor adapter;
    private SwipeRefreshLayout swipeContainer;
    private TextInputEditText pinNo;


    final Calendar myCalendar = Calendar.getInstance();
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.f_accounts_balance, container, false);
        LayoutInflater li = LayoutInflater.from(requireActivity());
        View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);

        cacheUtil = new CacheUtil(getActivity().getApplicationContext());
        pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Balance Inquiry");

        account_list = rootView.findViewById(R.id.account_list);
        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
        recyclerView = rootView.findViewById(R.id.recyclerView);

        JSONObject myAcct = null;
        try {
            myAcct = new JSONObject(cacheUtil.getString("my_profile"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        populateMyAccounts(myAcct.optJSONArray("accountList"));

        rootView.findViewById(R.id.request).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processUserEntries();
            }
        });

        setRetainInstance(true);
        return rootView;
    }

    private void populateMyAccounts(JSONArray accountsList) {
        if (accountsList != null && accountsList.length() > 0) {
            new SpinnerUtil().populateSpinner(accountsList, account_list, getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, "acctNo", "acctNo");
        }
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void processUserEntries() {
        try {
            if (SpinnerUtil.getSelectedValue(account_list).equalsIgnoreCase("0")) {
                showAlertDialog(null, "Please select an account");
                return;
            }
            requestObject = new JSONObject();
            requestObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            requestObject.put("accountNo", SpinnerUtil.getSelectedValue(account_list))
                    .put("entityType", cacheUtil.getString("entityType"))
                    .put("activity", TAG);
            authenticateInquiry();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void authenticateInquiry() {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = (TextView) layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setText("Confirm account Inquiry");
                pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Enter your Pin</font>"));
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("PROCEED",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                        if (TextUtils.isEmpty(pinNo.getText())) {
                                            pinNo.setError("PIN Number is required");
                                            pinNo.requestFocus();
                                        } else {
                                            try {
                                                JSONObject authRequest = NetworkUtil.getBaseRequest(getActivity());
                                                authRequest.put("pinNo", pinNo.getText());
                                                requestObject.put("authRequest", authRequest);
                                                System.out.println(requestObject);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            findAccountStatementAPI();
                                        }
                                    }
                                })
                        .setNegativeButton("CANCEL",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                alertDialog = alertDialogBuilder.create();
                NetworkUtil.doKeepDialog(alertDialog);
                if (alertDialog != null && !alertDialog.isShowing()) {
                    alertDialog.show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void findAccountStatementAPI() {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/findAccountBalance", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                        displayAccountBalance(response);
                                    } else {
                                        showAlertDialog(null, response.optJSONObject("response").optString("responseMessage"));
                                    }
                                } else
                                    showAlertDialog(null, getActivity().getString(R.string.resp_timeout));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.dismiss();
                        showAlertDialog(null, NetworkUtil.getErrorDesc(error));
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
            showAlertDialog("Connection Unavailable", "Cannot connect to server. No network available");
        }
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
        if (alertDialog != null && !alertDialog.isShowing())
            alertDialog.show();
    }

    public void displayAccountBalance(JSONObject response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        try {
            builder.setTitle(Html.fromHtml("<font color='#5a2069'><b>Account Details</b></font>"));
            builder.setMessage(Html.fromHtml(
                    "Account Name:  " + response.optString("accountTitle")
                    + "<br>Branch Name:       " + response.optString("branchName")
                    + "<br>Account Status:  " + response.optString("accountStatus")
                    + "<br>Available Balance: " + NumberUtils.formatNumber(response.optString("availableBalance")) + "<br/>"));
            builder.setPositiveButton("OK",
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
            AlertDialog diag = builder.create();
            diag.show();
        } catch (Exception e) {
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
