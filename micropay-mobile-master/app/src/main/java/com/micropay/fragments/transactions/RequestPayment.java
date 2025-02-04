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
import android.widget.TextView;

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
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.models.Receipt;
import com.micropay.models.Transaction;
import com.micropay.popups.DialogUtils;
import com.micropay.utils.DATA_CONVERTER;
import com.micropay.utils.NumberUtils;
import com.micropay.utils.PrinterUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

public class RequestPayment extends Fragment {

    private static final String lineString = "------------------------------------------";
    private final String TAG = TXNCashDeposit.class.getSimpleName();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    boolean printingCustomerCopy = true;
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private JSONObject requestObject;
    private TextInputEditText from_phone, from_amount, from_reason;
    private TextInputEditText pinNo;
    private String retrievalReference, customer_account_no = null, customerName = null;
    private boolean isPrinterConfigured = true;
    private boolean isPrintingEnabled = true;
    private Transaction transaction;
    private String customerAccount;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.f_txn_request_payment, container, false);
        cacheUtil = new CacheUtil(requireActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) requireActivity();
        loanActivity.setTitle("Request Payment");
        from_phone = rootView.findViewById(R.id.from_phone);
        from_amount = rootView.findViewById(R.id.request_amount);
        from_reason = rootView.findViewById(R.id.request_reason);
        rootView.findViewById(R.id.process_deposit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUserEntries();
            }
        });

        setRetainInstance(true);
        return rootView;
    }

    private void callPaymentRequestAPI() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getTransactionUrl() + "/sendPaymentRequest", requestObject,
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if ("0".equalsIgnoreCase(response.optString("responseCode"))) {
                                    displayTransactionSuccess(response);
                                } else {
                                    String errorMessage = response.optString("responseMessage");
                                    showAlertDialog(errorMessage);
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
                    public Map<String, String> getHeaders() {
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
            if (TextUtils.isEmpty(from_phone.getText())) {
                showAlertDialog("From phone is required");
                return;
            }
            if (TextUtils.isEmpty(from_amount.getText())) {
                showAlertDialog("Amount required");
                return;
            }
            if (TextUtils.isEmpty(from_reason.getText())) {
                showAlertDialog("Reason required");
                return;
            }

            requestObject = new JSONObject();
            requestObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            requestObject.put("fromPhone", from_phone.getText().toString())
                    .put("requesterPhone", cacheUtil.getString("registeredPhone"))
                    .put("amount", NumberUtils.convertToBigDecimal(from_amount.getText().toString()))
                    .put("requesterReason", from_reason.getText().toString())
                    .put("activity", TAG);
            callAccountResponseByPhoneNo();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callAccountResponseByPhoneNo() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/accountResponseByPhoneNo", new JSONObject()
                        .put("phoneNo", from_phone.getText().toString()),
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null && response.length() > 0) {
                                if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode")))
                                    processAccountResponseByPhoneFeedback(response);
                                else if ("404".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode")))
                                    showAlertDialog(response.optJSONObject("response").optString("responseMessage"));
                                else {
                                    showAlertDialog(response.optJSONObject("response").optString("responseMessage"));
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Validating transaction.", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processAccountResponseByPhoneFeedback(JSONObject search_results) {
        try {
            if (search_results != null) {
                if ("0".equalsIgnoreCase(search_results.optJSONObject("response").optString("responseCode"))) {
                    customerName = search_results.optString("customerName");
                } else {
                    showAlertDialog(search_results.optJSONObject("response").optString("responseMessage"));
                }
                JSONArray account_list = search_results.optJSONArray("accountList");
                if (account_list != null)
                    for (int i = 0; i < account_list.length(); i++) {
                        JSONObject jsonObject = account_list.optJSONObject(i);
                        customer_account_no = jsonObject.optString("acctNo");
                        break;
                    }
            }
            if (customer_account_no != null && customerName != null) {
                displayRetrievedAccountInformationDetails(customerName);
            } else {
                displayInvalidAccountMessage();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayInvalidAccountMessage() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.dialogTheme);
            builder.setMessage("Phone number specified is not registered on agent banking. Send as Non-registered to generate Voucher code")
                    .setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                }
                            }).setNegativeButton("CANCEL",
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


    private void displayRetrievedAccountInformationDetails(final String customerName) {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = (TextView) layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setText("Confirm payment request of " + NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(from_amount.getText().toString())) + " "
                        + Html.fromHtml(" from customer " + customerName));
                pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Authentication</font>"));
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
                                                requestObject.put("customerName", customerName);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            callPaymentRequestAPI();
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
                    "You have requested for UGX " + NumberUtils.formatNumber(from_amount.getText().toString()) + " from " + DATA_CONVERTER.getInternationalFormat(from_phone.getText().toString()) + "-" + customerName
                            + "<br>Reason: " + from_reason.getText().toString()
                            + "<br>Expires on " + response.optString("expiryDate")
            ));
            builder.setPositiveButton("OK",
                    (arg0, arg1) -> {
                        arg0.dismiss();
                        getActivity().finish();
                    });
            AlertDialog diag = builder.create();
            diag.show();
            generatePrintContent(response);
            printAndWait(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /*BUILTIN PRINTER OPTIONS*/

    private void showAlertDialogAndExit(String responseTxt) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.dialogTheme);
            builder.setMessage(responseTxt)
                    .setPositiveButton("CLOSE",
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

    private void submitJob(Runnable runnable) {
        try {
            threadPool.submit(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
    void generatePrintContent(JSONObject response) {
        RECEIPT = new ArrayList<>();
        String transDate = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss").format(new Date());

        Receipt data = new Receipt("Cash Withdraw", null);
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
