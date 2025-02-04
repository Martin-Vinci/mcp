package com.micropay.fragments.transactions;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;

import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentTransaction;

import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.api.TransTypes;
import com.micropay.fragments.AccountMenu;
import com.micropay.fragments.BillsMenuAgent;
import com.micropay.models.Receipt;
import com.micropay.popups.DialogUtils;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.models.Transaction;
import com.micropay.utils.DATA_CONVERTER;
import com.micropay.utils.NumberUtils;
import com.micropay.utils.PrinterUtils2;
import com.micropay.utils.SpinnerUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TxnNwscAgent extends Fragment {
    private final String TAG = TXNCashDeposit.class.getSimpleName();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private JSONObject requestObject;
    private Spinner myAccounts, customer_area, paymentType;
    private TextInputEditText customer_phone, referenceNo, tran_amount;
    private TextView lbl_customer_details, lbl_customer_phone, lbl_trans_amount, lbl_customer_name, lbl_bill_charge;
    private Button btn_process;
    private LinearLayout linear_customer_details;
    private View details_divider;
    private TextInputEditText pinNo;
    private String retrievalReference, receipient_phone = null, trans_reason = null;
    private BigDecimal transAmount;

    private Transaction transaction;
    private boolean isPrinterConfigured = true;
    private boolean isPrintingEnabled = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.f_txn_nwsc_pmt_agent, container, false);
        cacheUtil = new CacheUtil(requireActivity().getApplicationContext());
        FragmentHandler loanActivity = (FragmentHandler) requireActivity();
        loanActivity.setTitle("National Water Payment");
        referenceNo = rootView.findViewById(R.id.referenceNo);
        linear_customer_details = rootView.findViewById(R.id.linear_customer_details);
        lbl_customer_details = rootView.findViewById(R.id.lbl_customer_details);
        details_divider = rootView.findViewById(R.id.details_divider);
        customer_phone = rootView.findViewById(R.id.customer_phone);
        myAccounts = rootView.findViewById(R.id.my_account);
        paymentType = rootView.findViewById(R.id.paymentType);
        customer_area = rootView.findViewById(R.id.customer_area);
        tran_amount = rootView.findViewById(R.id.tran_amount);
        lbl_customer_phone = rootView.findViewById(R.id.lbl_customer_phone);
        lbl_trans_amount = rootView.findViewById(R.id.lbl_trans_amount);
        lbl_customer_name = rootView.findViewById(R.id.lbl_customer_name);
        lbl_bill_charge = rootView.findViewById(R.id.lbl_bill_charge);
        btn_process = rootView.findViewById(R.id.process_payment);
        tran_amount.addTextChangedListener(NumberUtils.onTextChangedListener(tran_amount));
        JSONObject areaRequest = new JSONObject();
        callCustomerAreaApi(areaRequest);
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

        btn_process.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btn_process.getText().toString().toUpperCase().equals("VALIDATE"))
                    validateUtilityInquiry();
                else
                    validateUserEntries();
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

        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(new JSONObject()
                    .put("value", "Postpaid")
                    .put("code", "POSTPAID"));
            jsonArray.put(new JSONObject()
                    .put("value", "New Connection")
                    .put("code", "NEWCONNECTION"));
            jsonArray.put(new JSONObject()
                    .put("value", "Check Balance")
                    .put("code", "BALANCEINQUIRY"));
            new SpinnerUtil().populateSpinner(jsonArray, paymentType, getActivity(),
                     android.R.layout.simple_spinner_dropdown_item, "code", "value");
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    private void callCustomerAreaApi(JSONObject areaRequest) {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/nwscfindCustomerAreas", areaRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if (response != null && response.length() > 0) {
                                        if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                            JSONArray areas = response.optJSONArray("data");
                                            if (areas != null && areas.length() > 0) {
                                                new SpinnerUtil().populateSpinner(areas, customer_area, getActivity(),
                                                        android.R.layout.simple_spinner_dropdown_item, "code", "description");
                                            }
                                        } else {
                                            showAlertDialog(response.optJSONObject("response").optString("responseMessage"));
                                        }
                                    } else
                                        showAlertDialog(getActivity().getString(R.string.resp_timeout));
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Finding areas", false);
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
        customer_phone.setVisibility(View.VISIBLE);
        tran_amount.setVisibility(View.VISIBLE);
        lbl_customer_phone.setVisibility(View.VISIBLE);
        lbl_trans_amount.setVisibility(View.VISIBLE);
        btn_process.setText("Submit");
        lbl_customer_name.setText(response.optString("customerName"));
        lbl_bill_charge.setText(response.optString("outStandingBal"));
    }

    public void displayAccountBalance(JSONObject response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        try {
            builder.setTitle(Html.fromHtml("<font color='#5a2069'><b>Nwsc Balance Details</b></font>"));
            builder.setMessage(Html.fromHtml(
                    "Account Name:  " + response.optString("customerName")
                            + "<br>Balance:  " + NumberUtils.formatNumber(response.optString("outStandingBal"))
                            + "<br>Charges: " + NumberUtils.formatNumber(response.optString("charge")) + "<br/>"));
            builder.setPositiveButton("OK",
                    (arg0, arg1) -> {
                        arg0.dismiss();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .replace(R.id.container, new BillsMenuAgent()).addToBackStack(null)
                                .commit();
                    }).setCancelable(false);
            AlertDialog diag = builder.create();
            diag.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callNWSCPaymentApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/nwscPayment", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                        retrievalReference = response.optString("transId");
                                        displayTransactionSuccess(response);
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
                showAlertDialog("Invalid float account selected");
                return;
            }
            if (TextUtils.isEmpty(customer_phone.getText())) {
                showAlertDialog("Customer phone is required");
                return;
            }
            if (TextUtils.isEmpty(referenceNo.getText())) {
                showAlertDialog("Meter number is required");
                return;
            }

            requestObject = new JSONObject();
            transAmount = NumberUtils.convertToBigDecimal(tran_amount.getText().toString());
            receipient_phone = DATA_CONVERTER.getInternationalFormat(customer_phone.getText().toString());
            if (receipient_phone == null) {
                showAlertDialog(getString(R.string.invalid_phone_no));
                customer_phone.requestFocus();
                return;
            }

            trans_reason = referenceNo.getText().toString() + "-" + lbl_customer_name.getText().toString();
            requestObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            requestObject.put("transAmt", transAmount)
                    .put("currency", "UGX")
                    .put("referenceNo", referenceNo.getText().toString())
                    .put("customerArea", SpinnerUtil.getSelectedKey(customer_area))
                    .put("customerType", SpinnerUtil.getSelectedKey(paymentType))
                    .put("drAcctNo", SpinnerUtil.getSelectedValue(myAccounts))
                    .put("tranCode", TransTypes.NWSC_AGENT)
                    .put("description", trans_reason)
                    .put("depositorPhoneNo", receipient_phone)
                    .put("customerName", lbl_customer_name.getText().toString())
                    .put("activity", TAG);
            determineTransCharge();
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
            if (customer_area.getSelectedItemPosition() <= 0) {
                showAlertDialog("Invalid customer area selected");
                return;
            }
            if (TextUtils.isEmpty(referenceNo.getText())) {
                showAlertDialog("Meter number is required");
                return;
            }
            requestObject = new JSONObject();
            requestObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            requestObject.put("receipientPhone", customer_phone.getText().toString())
                    .put("referenceNo", referenceNo.getText().toString())
                    .put("customerArea", SpinnerUtil.getSelectedKey(customer_area))
                    .put("drAcctNo", SpinnerUtil.getSelectedValue(myAccounts))
                    .put("activity", TAG);
            callReferenceInquiry();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callReferenceInquiry() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/nwscReferenceInquiry", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null && response.length() > 0) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode")))
                                        if (SpinnerUtil.getSelectedKey(paymentType).equals("BALANCEINQUIRY"))
                                            displayAccountBalance(response);
                                        else
                                            displayValidationDetails(response);
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Validating NWSC reference.", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // PRINTING STARTS HERE

    private void determineTransCharge() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JSONObject transChargeRequest = new JSONObject();
                transChargeRequest.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
                requestObject.put("amount", transAmount.toString())
                        .put("transCode", TransTypes.NWSC_AGENT)
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
                                        displayPostingConfirmationDetails(response.optDouble("charge"));
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

    private void displayPostingConfirmationDetails(Double chargeAmount) {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = (TextView) layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setText("Confirm National water payment of " + NumberFormat.getNumberInstance(Locale.US).format(transAmount) + ". "
                        + Html.fromHtml(" Reference number " + referenceNo.getText().toString() + " " + lbl_customer_name.getText().toString() + "<br>")
                        + Html.fromHtml("Charge " + NumberFormat.getNumberInstance(Locale.US).format(Double.parseDouble(chargeAmount.toString())))
                );
                pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Confirm NWSC Payment</font>"));
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
                                                requestObject.put("authRequest", authRequest);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            callNWSCPaymentApi();
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
                    "National water purchase completed of UGX " + NumberUtils.toCurrencyFormat(NumberUtils.convertToBigDecimal(tran_amount.getText().toString())) + " for customer " + DATA_CONVERTER.getInternationalFormat(customer_phone.getText().toString()) + "-" + lbl_customer_name.getText().toString()
                            + "<br>Trans ID: " + response.optString("transId")
                            + "<br>Charge: UGX" + NumberUtils.formatNumber(response.optString("chargeAmt"))
                            + "<br>Bal: UGX " + NumberUtils.formatNumber(response.optString("drAcctBal"))));
            builder.setPositiveButton("OK",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                            getActivity().finish();
                        }
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

    private void printAndWait(boolean exit) {
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
