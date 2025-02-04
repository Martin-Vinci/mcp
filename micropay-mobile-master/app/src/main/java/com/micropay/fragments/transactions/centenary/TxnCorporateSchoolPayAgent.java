package com.micropay.fragments.transactions.centenary;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.api.TransTypes;
import com.micropay.fragments.CentenaryMenu;
import com.micropay.fragments.transactions.TXNCashDeposit;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.models.Receipt;
import com.micropay.models.Transaction;
import com.micropay.popups.DialogUtils;
import com.micropay.utils.NumberUtils;
import com.micropay.utils.PrinterUtils2;
import com.micropay.utils.SpinnerUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TxnCorporateSchoolPayAgent extends Fragment {
    private final String TAG = TXNCashDeposit.class.getSimpleName();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private static CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private JSONObject requestObject;
    private Spinner myAccounts;
    private TextInputEditText customer_phone, referenceNo, tran_amount;
    private TextView lbl_customer_details, lbl_customer_phone, lbl_trans_amount,
            lbl_customer_name, lbl_umeme_charge;
    private Button btn_process;
    private LinearLayout linear_customer_details;
    private View details_divider;
    private TextInputEditText pinNo;
    private String customerName;
    private String retrievalReference;
    private BigDecimal outstandingAmount;

    private Transaction transaction;
    private boolean isPrinterConfigured = true;
    private boolean isPrintingEnabled = true;
    private static boolean isPrinterConnected = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.f_txn_coporate_billpay_agent, container, false);
        cacheUtil = new CacheUtil(requireActivity().getApplicationContext());
        FragmentHandler loanActivity = (FragmentHandler) requireActivity();
        loanActivity.setTitle("School Pay-Centenary");
        referenceNo = rootView.findViewById(R.id.referenceNo);
        myAccounts = rootView.findViewById(R.id.my_account);
        btn_process = rootView.findViewById(R.id.btn_process);

        linear_customer_details = rootView.findViewById(R.id.linear_customer_details);
        lbl_customer_details = rootView.findViewById(R.id.lbl_customer_details);
        details_divider = rootView.findViewById(R.id.details_divider);
        customer_phone = rootView.findViewById(R.id.customer_phone);
        myAccounts = rootView.findViewById(R.id.my_account);
        tran_amount = rootView.findViewById(R.id.tran_amount);
        lbl_customer_phone = rootView.findViewById(R.id.lbl_customer_phone);
        lbl_trans_amount = rootView.findViewById(R.id.lbl_trans_amount);
        lbl_customer_name = rootView.findViewById(R.id.lbl_customer_name);
        lbl_umeme_charge = rootView.findViewById(R.id.lbl_umeme_charge);
        tran_amount.addTextChangedListener(NumberUtils.onTextChangedListener(tran_amount));

        //displayValidationDetails(null);
        Bundle arguments = getArguments();
        JSONObject response = null;
        try {
            response = new JSONObject(cacheUtil.getString("my_profile"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        populateMyAccounts(response.optJSONArray("accountList"));
        btn_process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn_process.getText().toString().toUpperCase().equals("VALIDATE"))
                    validateUserEntries();
                else
                    callCustomerInquiry();
            }
        });
        setRetainInstance(true);
        return rootView;
    }

    private void populateMyAccounts(JSONArray accountsList) {
        if (accountsList != null && accountsList.length() > 0) {
            new SpinnerUtil().populateSpinner(accountsList, myAccounts, getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, "acctNo", "acctNo");
        }
    }

    private void processCorporateAgentService() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/processCorporateAgentService", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("0".equalsIgnoreCase(response.optString("returnCode"))) {
                                        retrievalReference = response.optString("transId");
                                        displayTransactionSuccess(response);
                                    } else {
                                        String errorMessage = response.optString("returnMessage");
                                        showAlertDialog(errorMessage);
                                    }
                                } else
                                    showAlertDialog(getActivity().getString(R.string.resp_timeout));
                            }
                        }, error -> {
                    if (alertDialog != null && alertDialog.isShowing())
                        alertDialog.dismiss();
                    showAlertDialog(NetworkUtil.getErrorDesc(error));
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
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Processing...", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validateUserEntries() {
        if (SpinnerUtil.getSelectedValue(myAccounts).equalsIgnoreCase("0")) {
            showAlertDialog("Invalid float account selected");
            return;
        }
        if (TextUtils.isEmpty(referenceNo.getText())) {
            showAlertDialog("Customer number is required");
            return;
        }

        requestObject = new JSONObject();
        try {
            //transAmount = NumberUtils.convertToBigDecimal(tran_amount.getText().toString());
            String trans_reason = referenceNo.getText().toString() + "-" + "School Pay";
            requestObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            requestObject.put("currency", "UGX")
                    .put("referenceNo", referenceNo.getText().toString())
                    .put("billerCode", "SCHOOLPAY")
                    .put("paymentCode", "VALIDATE_BILL_PAYMENT")
                    .put("drAcctNo", SpinnerUtil.getSelectedValue(myAccounts))
                    .put("tranCode", TransTypes.CENTENARY_SCHOOL_PAY_AGENT)
                    .put("description", trans_reason)
                    .put("activity", TAG);
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/processCorporateAgentService", requestObject,
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null && response.length() > 0) {
                                if ("0".equalsIgnoreCase(response.optString("returnCode")))
                                    displayValidationDetails(response);
                                else {
                                    showAlertDialog(response.optString("returnMessage"));
                                }
                            } else
                                showAlertDialog(getActivity().getString(R.string.resp_timeout));
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
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Validating Customer reference.", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callCustomerInquiry() {
        try {
            if (SpinnerUtil.getSelectedValue(myAccounts).equalsIgnoreCase("0")) {
                showAlertDialog("Invalid float account selected");
                return;
            }
            if (TextUtils.isEmpty(referenceNo.getText())) {
                showAlertDialog("Customer number is required");
                return;
            }

            if (TextUtils.isEmpty(tran_amount.getText())) {
                showAlertDialog("Transaction amount is required");
                return;
            }

            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                requestObject
                        .put("paymentCode", "INITIATE_BILL_PAYMENT")
                        .put("transAmt", NumberUtils.convertToBigDecimal(tran_amount.getText().toString()))
                        .put("outstandingAmount", outstandingAmount);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/processCorporateAgentService", requestObject,
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null && response.length() > 0) {
                                if ("0".equalsIgnoreCase(response.optString("returnCode")))
                                    displayPostingConfirmationDetails(0d, response);
                                    //determineTransCharge(response);
                                else {
                                    showAlertDialog(response.optString("returnMessage"));
                                }
                            } else
                                showAlertDialog(getActivity().getString(R.string.resp_timeout));
                        }, error -> {
                    if (alertDialog != null && alertDialog.isShowing())
                        alertDialog.dismiss();
                    showAlertDialog(NetworkUtil.getErrorDesc(error));
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
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Validating Customer Information.", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    void displayValidationDetails(JSONObject response) {
        lbl_customer_details.setVisibility(View.VISIBLE);
        linear_customer_details.setVisibility(View.VISIBLE);
        details_divider.setVisibility(View.VISIBLE);
        tran_amount.setVisibility(View.VISIBLE);
        //lbl_customer_phone.setVisibility(View.VISIBLE);
        //customer_phone.setVisibility(View.VISIBLE);
        lbl_trans_amount.setVisibility(View.VISIBLE);
        btn_process.setText("Submit");
        JSONObject customerData = response.optJSONObject("returnObject").optJSONObject("customerData");
        customerName = customerData.optString("firstName") + " "
                + customerData.optString("lastName") + "\n";
        String customerName2 = customerName + customerData.optString("schoolName");
        lbl_customer_name.setText(customerName2);
        lbl_umeme_charge.setText(customerData.optString("outstandingAmount"));
        outstandingAmount = new BigDecimal(customerData.optString("outstandingAmount"));
    }

    private void determineTransCharge(JSONObject jsonObject) {
        try {
            Double transAmount = 0D;
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JSONObject transChargeRequest = new JSONObject();
                transChargeRequest.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
                requestObject.put("amount", transAmount.toString())
                        .put("transCode", TransTypes.CENTENARY_SCHOOL_PAY_AGENT)
                        .put("accountNo", SpinnerUtil.getSelectedValue(myAccounts))
                        .put("activity", TAG);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/findTransactionCharge", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                        displayPostingConfirmationDetails(response.optDouble("charge"), jsonObject);
                                    } else {
                                        String errorMessage = response.optJSONObject("response").optString("responseMessage");
                                        showAlertDialog(errorMessage);
                                    }
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
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Processing...", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // PRINTING STARTS HERE
    private void displayPostingConfirmationDetails(Double chargeAmount, JSONObject validationJsonResponse) {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = (TextView) layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setText(validationJsonResponse.optString("returnMessage")
                );
                pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Confirm</font>"));
                //alertDialogBuilder.setMessage(customerName);
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

                                                requestObject.put("schoolName", validationJsonResponse.optJSONObject("returnObject").optString("schoolName"));
                                                requestObject.put("crAcctNo", validationJsonResponse.optJSONObject("returnObject").optString("schoolAcctNo"));
                                                requestObject.put("className", validationJsonResponse.optJSONObject("returnObject").optString("studentClass"));
                                                requestObject.put("customerName", validationJsonResponse.optJSONObject("returnObject").optString("studentFullName"));
                                                requestObject.put("surCharge", validationJsonResponse.optJSONObject("returnObject")
                                                        .optJSONObject("chargesData")
                                                        .optString("totalCharge"));
                                                requestObject.put("paymentCode", "CONFIRM_BILL_PAYMENT");
                                                requestObject.put("billerTransRef", validationJsonResponse.optString("progressIndicator"));
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            processCorporateAgentService();
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

    public void displayTransactionSuccess(JSONObject response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        try {
            builder.setTitle(Html.fromHtml("<font color='#5a2069'><b>Success</b></font>"));

            builder.setMessage(Html.fromHtml(
                    "You have processed school fees payment of UGX " + NumberUtils.toCurrencyFormat(NumberUtils.convertToBigDecimal(tran_amount.getText().toString())) + " for student " + customerName
                            + "<br>Trans ID: " + response.optString("transId")
                            + "<br>Balance: UGX " + NumberUtils.formatNumber(response.optString("drAcctBal"))));
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                            getActivity().getSupportFragmentManager().beginTransaction()
                                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                    .replace(R.id.container, new CentenaryMenu()).addToBackStack(null)
                                    .commit();
                        }
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
            generatePrintContent(response);
            printAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*BUILTIN PRINTER OPTIONS*/
    private void closeAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
    }

    public void showAlertDialog(String title, String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.dialogTheme);
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
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    public void showAlertDialog(String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.dialogTheme);
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
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    public void showAlertDialogAndExit(String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.dialogTheme);
            builder.setMessage(body)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    getActivity().finish();
                                }
                            }).setCancelable(false);
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        closeAllDialogs();
        try {
            if (threadPool != null) {
                threadPool.shutdown();
                threadPool.awaitTermination(1, TimeUnit.SECONDS);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        closeAllDialogs();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtil.getInstance(requireActivity().getApplicationContext()).cancel(TAG);
    }

    /*THERMAL PRINTER OPTIONS*/
    private List<Receipt> RECEIPT;

    void generatePrintContent(JSONObject response) {
        RECEIPT = new ArrayList<>();
        Receipt data = new Receipt("", response.optString("printData"));
        RECEIPT.add(data);
    }

    private void printAndWait() {

        if (!isPrintingEnabled) {
            return;
        }
        PrinterUtils2 printerUtils = new PrinterUtils2(requireActivity(), RECEIPT, cacheUtil);
        AsyncTask<String, Void, Integer> execute = printerUtils.execute();
        try {
            execute.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
//        if (exit)
//            getActivity().finish();
    }
}
