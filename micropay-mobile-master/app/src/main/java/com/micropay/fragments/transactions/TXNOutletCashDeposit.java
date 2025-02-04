package com.micropay.fragments.transactions;

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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

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
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.models.Receipt;
import com.micropay.models.Transaction;
import com.micropay.popups.DialogUtils;
import com.micropay.utils.DATA_CONVERTER;
import com.micropay.utils.NumberUtils;
import com.micropay.utils.PrinterUtils;
import com.micropay.utils.SpinnerUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TXNOutletCashDeposit extends Fragment {

    private static final String lineString = "------------------------------------------";
    private final String TAG = TXNOutletCashDeposit.class.getSimpleName();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    boolean printingCustomerCopy = true;
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private JSONObject requestObject;
    private TextView depositorLable;
    private Spinner myAccounts;
    private TextInputEditText tran_amount, outlet_code;
    private TextInputEditText pinNo;
    private String retrievalReference, customer_account_no = null, customerName = null;

    private Transaction transaction;
    private boolean isPrinterConfigured = true;
    private boolean isPrintingEnabled = true;


    private String customerAccount;
    private boolean printingEnabled = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.f_txn_outlet_cash_deposit, container, false);
        cacheUtil = new CacheUtil(requireActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) requireActivity();
        loanActivity.setTitle("Outlet Cash In");
        tran_amount = rootView.findViewById(R.id.tran_amount);
        outlet_code = rootView.findViewById(R.id.dep_outlet_code);
        myAccounts = rootView.findViewById(R.id.my_account);
        tran_amount.addTextChangedListener(NumberUtils.onTextChangedListener(tran_amount));
        Bundle arguments = getArguments();
        JSONObject response = null;
        try {
            response = new JSONObject(cacheUtil.getString("my_profile"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        populateMyAccounts(response.optJSONArray("accountList"));

        isPrintingEnabled = cacheUtil.getBoolean("enable_printing", false);
        if (isPrintingEnabled) {
            if (cacheUtil.getString("bluetooth_address").trim().length() <= 0) {
                isPrinterConfigured = false;
            }
        }

        rootView.findViewById(R.id.process_deposit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUserEntries();
            }
        });

        setRetainInstance(true);
        return rootView;
    }

    private void showToast(String text) {
        Toast.makeText(getActivity(), text, Toast.LENGTH_LONG).show();
    }


    private void populateMyAccounts(JSONArray accountsList) {
        if (accountsList != null && accountsList.length() > 0) {
            new SpinnerUtil().populateSpinner(accountsList, myAccounts, getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, "acctNo", "acctNo");
        }
    }


    private void callCashDepositApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getTransactionUrl() + "/outletCashIn", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                        //handle reset option here
                                        retrievalReference = response.optString("transId");
                                        displayCashDepositTransactionSuccess(response);
                                        //buildTransaction();
                                        //evaluatePrintingOptions("Customer");
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

    private void validateUserEntries() {
        try {
            if(1==2) { // (isPrintingEnabled && !isPrinterConfigured) {
                showAlertDialog("Printer is not configured");
                return;
            }

            if (SpinnerUtil.getSelectedValue(myAccounts).equalsIgnoreCase("0")) {
                showAlertDialog("Select float account to debit");
                return;
            }
            if (TextUtils.isEmpty(tran_amount.getText())) {
                showAlertDialog("Amount is required");
                return;
            }
            requestObject = new JSONObject();
            requestObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            requestObject.put("transAmt", NumberUtils.convertToBigDecimal(tran_amount.getText().toString()))
                    .put("currency", "UGX")
                    .put("transType", TransTypes.CASH_DEPOSIT)
                    .put("drAcctNo", SpinnerUtil.getSelectedValue(myAccounts))
                    .put("activity", TAG);
            callOutletInquiry();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callOutletInquiry() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/findOutletDetails", new JSONObject()
                        .put("outletCode", outlet_code.getText().toString()),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null && response.length() > 0) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode")))
                                        processOutletInquiryFeedback(response);
                                    else {
                                        showAlertDialog(response.optJSONObject("response").optString("responseMessage"));
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Validating voucher number.", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processOutletInquiryFeedback(JSONObject search_results) {
        try {
            if (search_results != null) {
                if (!"0".equalsIgnoreCase(search_results.optJSONObject("response").optString("responseCode"))) {
                    return;
                }
                customerName = search_results.optString("outletName");
                customer_account_no = search_results.optString("outletAccount");
                determineTransCharge();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void displayCashDepositTransactionSuccess(JSONObject response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        try {
            builder.setTitle(Html.fromHtml("<font color='#5a2069'><b>Success</b></font>"));
            builder.setMessage(Html.fromHtml(
                    "You have deposited UGX " + NumberUtils.toCurrencyFormat(NumberUtils.convertToBigDecimal(tran_amount.getText().toString())) + " to " + outlet_code.getText().toString() + "-" + customerName
                            + "<br>Trans ID: " + response.optString("transId")
                            + "<br>Charge: UGX" + NumberUtils.formatNumber(response.optString("chargeAmt"))
                            + "<br>Bal: UGX " + NumberUtils.formatNumber(response.optString("drAcctBal"))));
            builder.setPositiveButton("OK",
                    (arg0, arg1) -> {
                        arg0.dismiss();
                        getActivity().finish();
                    });
            AlertDialog diag = builder.create();
            diag.show();
            generateDepositPrintContent(response);
            printAndWait(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void determineTransCharge() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JSONObject transChargeRequest = new JSONObject();
                transChargeRequest.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
                requestObject.put("amount", tran_amount.getText().toString())
                        .put("transCode", TransTypes.SUPER_AGENT_TO_OUTLET)
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
                                        displayRetrievedAccountInformationDetails(response.optDouble("charge"));
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


    private void displayRetrievedAccountInformationDetails(Double chargeAmount) {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = (TextView) layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setText("Confirm cash deposit of " + NumberUtils.toCurrencyFormat(NumberUtils.convertToBigDecimal(tran_amount.getText().toString())) + " "
                        + Html.fromHtml(" to Outlet " + customer_account_no + " " + customerName + "<br>")
                        + Html.fromHtml("Charge " + NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(chargeAmount.toString())))
                );
                pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Confirm Cash Deposit</font>"));
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
                                                requestObject.put("crAcctNo", customer_account_no);
                                               // requestObject.put("customerPhoneNo", customer_phone.getText().toString());
                                                requestObject.put("customerName", customerName);
                                                System.out.println(requestObject);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            callCashDepositApi();
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


    private void renderDialog(AlertDialog.Builder builder) {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = builder.create();
        NetworkUtil.doKeepDialog(alertDialog);
        if (!alertDialog.isShowing()) {
            alertDialog.show();
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
    private void buildTransaction() {
        transaction = new Transaction();
        transaction.setAccount(requestObject.optString("crAcctNo"));
        transaction.setAgent(cacheUtil.getString("customerName"));
        transaction.setAmount(NumberUtils.formatNumber(requestObject.optString("transAmt")));
        transaction.setCurrency("Ugx.");
        transaction.setDate(new SimpleDateFormat("dd/MM/yyyy").format(new Date()) + " " + new SimpleDateFormat("HH:mm").format(new Date()));
        transaction.setOutlet(cacheUtil.getString("outletCode"));
        transaction.setReceiptTitle("Micropay Limited (U)");
        transaction.setReference(retrievalReference);
        transaction.setReceiptNo(DATA_CONVERTER.getReceiptNo());
        transaction.setTranStatus("Approved");
        transaction.setTranType("CASH DEPOSIT");
        transaction.setCustomer(customerName);
    }
    private List<Receipt> RECEIPT;
    void generateDepositPrintContent(JSONObject response) {
        RECEIPT = new ArrayList<>();
        String transDate = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss").format(new Date());

        Receipt data = new Receipt("Cash Deposit", null);
        RECEIPT.add(data);
        data = new Receipt("Receipt No:", DATA_CONVERTER.getReceiptNo());
        RECEIPT.add(data);
        data = new Receipt("Trans ID:", response.optString("transId"));
        RECEIPT.add(data);
        data = new Receipt("Trans Date:", transDate);
        RECEIPT.add(data);
        data = new Receipt("Trans Amount:", NumberUtils.formatNumber(requestObject.optString("transAmt")) + " UGX");
        RECEIPT.add(data);
        data = new Receipt(DATA_CONVERTER.capitalizeFirstLetter(NumberUtils.numberToWord(Long.parseLong(requestObject.optString("transAmt")))), null);
        RECEIPT.add(data);
        data = new Receipt("Trans Charge:", NumberUtils.formatNumber(response.optString("chargeAmt")) + " UGX");
        RECEIPT.add(data);
        data = new Receipt("Agent Code:", cacheUtil.getString("outletCode"));
        RECEIPT.add(data);
        data = new Receipt("Agent Name:", cacheUtil.getString("customerName"));
        RECEIPT.add(data);
        data = new Receipt("Agent Phone:", cacheUtil.getString("registeredPhone"));
        RECEIPT.add(data);
    }

    private void printAndWait(boolean exit) {

        if (!isPrintingEnabled) {
            return;
        }
        PrinterUtils printerUtils = new PrinterUtils(requireActivity(), RECEIPT, cacheUtil);
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
