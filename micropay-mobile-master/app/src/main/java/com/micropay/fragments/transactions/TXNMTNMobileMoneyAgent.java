package com.micropay.fragments.transactions;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
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
import com.micropay.fragments.BillsMenuCustomer;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.models.Picklist;
import com.micropay.models.Receipt;
import com.micropay.popups.DialogUtils;
import com.micropay.utils.DATA_CONVERTER;
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

public class TXNMTNMobileMoneyAgent extends Fragment {
    private final String TAG = TXNCashDeposit.class.getSimpleName();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private JSONObject requestObject;
    private Spinner myAccounts, package_type;
    private TextInputEditText txt_otp, referenceNo, tran_amount;
    private TextView lbl_otp;
    private Button btn_process;
    private LinearLayout linear_customer_details;
    private View details_divider;
    private TextInputEditText pinNo;
    private String description = null;
    private String customerName = null;
    private BigDecimal transAmount;
    private String billerCode = "MTN_MOBILE_MONEY";
    private Integer transCode;
    private boolean isPrinterConfigured = true;
    private boolean isPrintingEnabled = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.f_txn_mtn_mm_agent, container, false);
        cacheUtil = new CacheUtil(requireActivity().getApplicationContext());
        FragmentHandler loanActivity = (FragmentHandler) requireActivity();
        loanActivity.setTitle("MTN MM-Agent");
        referenceNo = rootView.findViewById(R.id.referenceNo);
        linear_customer_details = rootView.findViewById(R.id.linear_customer_details);
        details_divider = rootView.findViewById(R.id.details_divider);
        txt_otp = rootView.findViewById(R.id.txt_otp);
        myAccounts = rootView.findViewById(R.id.my_account);
        tran_amount = rootView.findViewById(R.id.tran_amount);
        lbl_otp = rootView.findViewById(R.id.lbl_otp);
        package_type = rootView.findViewById(R.id.package_type);
        btn_process = rootView.findViewById(R.id.process_payment);
        tran_amount.addTextChangedListener(NumberUtils.onTextChangedListener(tran_amount));
        lbl_otp.setVisibility(View.GONE);
        txt_otp.setVisibility(View.GONE);

        JSONObject response = null;
        try {
            response = new JSONObject(cacheUtil.getString("my_profile"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        package_type.setOnItemSelectedListener(packageCategorySelectionListener);
        populateMyAccounts(response.optJSONArray("accountList"));
        callDataPackageApi();

        isPrintingEnabled = cacheUtil.getBoolean("enable_printing", false);
        if (isPrintingEnabled) {
            if (cacheUtil.getString("bluetooth_address").trim().length() <= 0) {
                isPrinterConfigured = false;
            }
        }

        btn_process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUtilityInquiry();
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

    private AdapterView.OnItemSelectedListener packageCategorySelectionListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            if (pos == 0) {
                //showToast("Invalid package category selected");
                return;
            }
            Picklist dictionary = (Picklist) package_type.getItemAtPosition(pos);
            String picklistCode = dictionary.getCode().toString();
            if (picklistCode.toLowerCase().contains("select")) return;
            JSONObject request = null;
            if (dictionary.getDescription().toLowerCase().contains("deposit")) {
                transCode = TransTypes.MTN_CASH_DEPOSIT;
                description = "MTN Cash Deposit";
            } else {
                transCode = TransTypes.MTN_CASH_WITHDRAW;
                description = "MTN Cash Withdraw";
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };

    private void callDataPackageApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/findBillerProducts",
                        new JSONObject().put("billerCode", billerCode)
                                .put("channelSource", "MOBILE"),
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if (response.length() > 0) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                        JSONArray areas = response.optJSONArray("data");
                                        if (areas != null && areas.length() > 0) {
                                            new SpinnerUtil().populateSpinner(areas, package_type, getActivity(),
                                                    android.R.layout.simple_spinner_dropdown_item, "code", "description", cacheUtil.getString("entityType"));
                                        }
                                    } else {
                                        showAlertDialog(response.optJSONObject("response").optString("responseMessage"));
                                    }
                                } else
                                    showAlertDialog(getActivity().getString(R.string.resp_timeout));
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Finding Packages", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callBillPaymentApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/processBillPayment", requestObject,
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                    displayTransactionSuccess(response);
                                } else {
                                    String errorMessage = response.optJSONObject("response").optString("responseMessage");
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

    private void validateUtilityInquiry() {
        try {
            if (SpinnerUtil.getSelectedValue(myAccounts).equalsIgnoreCase("0")) {
                showAlertDialog("Invalid float account selected");
                return;
            }
            if (SpinnerUtil.getSelectedValue(package_type).equalsIgnoreCase("0")) {
                showAlertDialog("Invalid transaction type selected");
                return;
            }
            if (TextUtils.isEmpty(referenceNo.getText())) {
                showAlertDialog("Mobile phone number is required");
                return;
            }
            if (TextUtils.isEmpty(tran_amount.getText())) {
                showAlertDialog("Amount is required");
                return;
            }

            transAmount = NumberUtils.convertToBigDecimal(tran_amount.getText().toString());
            requestObject = new JSONObject();
            requestObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            requestObject.put("mobilePhone", referenceNo.getText().toString())
                    .put("referenceNo", referenceNo.getText().toString()).put("billerCode", billerCode)
                    .put("paymentCode", SpinnerUtil.getSelectedKey(package_type))
                    .put("transAmt", transAmount.toString())
                    .put("currency", "UGX")
                    .put("tranCode", transCode)
                    .put("drAcctNo", SpinnerUtil.getSelectedValue(myAccounts))
                    .put("crAcctNo", SpinnerUtil.getSelectedValue(myAccounts))
                    .put("activity", TAG);
            callReferenceInquiry(requestObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callReferenceInquiry(JSONObject requestObject) {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/validateBillPayment", requestObject,
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null && response.length() > 0) {
                                if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode")))
                                    determineTransCharge(response);
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
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Basic " + Constants.getRawBasicData());
                        headers.put("sessionId", cacheUtil.getString("sessionId"));
                        return headers;
                    }
                };
                jsonObjectRequest.setTag(TAG);
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Validating Mobile phone number.", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void determineTransCharge(final JSONObject referenceInquiryResponse) {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JSONObject transChargeRequest = new JSONObject();
                transChargeRequest.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
                requestObject.put("amount", transAmount.toString())
                        .put("transCode", transCode)
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
                                        displayPostingConfirmationDetails(response.optDouble("charge"), referenceInquiryResponse);
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

    private void displayPostingConfirmationDetails(Double chargeAmount, JSONObject referenceInquiryResponse) {
        try {
            customerName = referenceInquiryResponse.optString("customerName");
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = (TextView) layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setText("Confirm " + description + " of " + NumberUtils.toCurrencyFormat(NumberUtils.convertToBigDecimal(tran_amount.getText().toString())) + " "
                        + Html.fromHtml(" to MTN Phone number: " + referenceNo.getText().toString() + ". Customer Name: " + referenceInquiryResponse.optString("customerName") + "<br>")
                        + Html.fromHtml("Charge " + (description.toLowerCase().contains("deposit") ? 0 : chargeAmount))
                );
                pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Confirm " + description + "</font>"));
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
                                                requestObject.put("authRequest", authRequest)
                                                        .put("customerName", customerName);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            callBillPaymentApi();
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
                    "You have processed " + description + " of UGX " + NumberUtils.toCurrencyFormat(NumberUtils.convertToBigDecimal(tran_amount.getText().toString())) + " for customer " + DATA_CONVERTER.getInternationalFormat(referenceNo.getText().toString()) + "-" + customerName
                            + "<br>Trans ID: " + response.optString("transId")
                            + "<br>Charge: UGX" + NumberUtils.formatNumber(response.optString("chargeAmt"))
                            + "<br>Bal: UGX " + (description.toLowerCase().contains("deposit") ? NumberUtils.formatNumber(response.optString("drAcctBal"))
                            : NumberUtils.formatNumber(response.optString("crAcctBal")))));
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                            Intent intent = new Intent(getActivity(), FragmentHandler.class);
                            startActivity(intent);
                        }
                    });
            AlertDialog diag = builder.create();
            diag.show();
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
    }


}
