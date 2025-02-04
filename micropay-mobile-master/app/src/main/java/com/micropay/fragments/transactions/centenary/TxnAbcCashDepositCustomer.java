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
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
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

public class TxnAbcCashDepositCustomer extends Fragment {
    private final String TAG = TXNCashDeposit.class.getSimpleName();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private static CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private JSONObject requestObject;
    private TextInputEditText centenary_customer_account, tran_amount, depositor_name, depositor_phone;
    private Button btn_process;
    private TextInputEditText pinNo;
    private Spinner myAccounts, package_type;
    private TextView lbl_package_type;
    private String recipient_phone = null;
    private String trans_reason = null;
    private BigDecimal transAmount;

    private static String txnType;
    private final boolean isPrintingEnabled = true;
    private String bankCode = "ABC_BANKS";
    private String bankName = "ABC_BANKS";
    private final String billerCode = "ABC_BANKS";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.f_txn_corporate_cash_deposit, container, false);
        cacheUtil = new CacheUtil(requireActivity().getApplicationContext());
        FragmentHandler loanActivity = (FragmentHandler) requireActivity();
        loanActivity.setTitle("Cash In - " + cacheUtil.getString(Constants.TITLE));
        bankCode = cacheUtil.getString(Constants.KEY);
        bankName = cacheUtil.getString(Constants.TITLE);
        centenary_customer_account = rootView.findViewById(R.id.centenary_customer_account);
        depositor_name = rootView.findViewById(R.id.depositor_name);
        depositor_phone = rootView.findViewById(R.id.depositor_phone);
        myAccounts = rootView.findViewById(R.id.my_account);
        tran_amount = rootView.findViewById(R.id.tran_amount);

        package_type = rootView.findViewById(R.id.package_type);
        lbl_package_type = rootView.findViewById(R.id.lbl_package_type);

        package_type.setVisibility(View.GONE);
        lbl_package_type.setVisibility(View.GONE);

        btn_process = rootView.findViewById(R.id.btn_process);
        tran_amount.addTextChangedListener(NumberUtils.onTextChangedListener(tran_amount));

//        txnType =  cacheUtil.getString(Constants.MENU);
//        if (txnType.contains("OtherBanks")) {
//            loanActivity.setTitle("Cash In - Other Banks");
//            package_type.setVisibility(View.VISIBLE);
//            lbl_package_type.setVisibility(View.VISIBLE);
//        } else {
//            loanActivity.setTitle("Cash In - Centenary");
//            package_type.setVisibility(View.GONE);
//            lbl_package_type.setVisibility(View.GONE);
//        }

        //callPackageApi();
        JSONObject response = null;
        try {
            response = new JSONObject(cacheUtil.getString("my_profile"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        populateMyAccounts(response.optJSONArray("accountList"));
        btn_process.setOnClickListener(view -> validateUserEntries());
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
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if ("0".equalsIgnoreCase(response.optString("returnCode"))) {
                                    displayTransactionSuccess(response);
                                } else {
                                    String errorMessage = response.optString("returnMessage");
                                    showAlertDialog(errorMessage);
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
            if (SpinnerUtil.getSelectedValue(myAccounts).equalsIgnoreCase("0")) {
                showAlertDialog("Invalid float account selected");
                return;
            }
            if (TextUtils.isEmpty(centenary_customer_account.getText())) {
                showAlertDialog("Customer Account is required");
                return;
            }
            if (TextUtils.isEmpty(depositor_phone.getText())) {
                showAlertDialog("Depositor phone is required");
                return;
            }
            if (TextUtils.isEmpty(depositor_name.getText())) {
                showAlertDialog("Depositor name is required");
                return;
            }
            if (TextUtils.isEmpty(tran_amount.getText())) {
                showAlertDialog("Transaction amount is required");
                return;
            }
            requestObject = new JSONObject();
            transAmount = NumberUtils.convertToBigDecimal(tran_amount.getText().toString());
            recipient_phone = DATA_CONVERTER.getInternationalFormat(depositor_phone.getText().toString());
            if (recipient_phone == null) {
                showAlertDialog(getString(R.string.invalid_phone_no));
                centenary_customer_account.requestFocus();
                return;
            }

            trans_reason = centenary_customer_account.getText().toString() + "-" + "Cash deposit";
            requestObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            requestObject.put("transAmt", transAmount)
                    .put("currency", "UGX")
                    .put("referenceNo", centenary_customer_account.getText().toString())
                    .put("paymentCode", "ABC_CASHIN_VALIDATION")
                    .put("drAcctNo", SpinnerUtil.getSelectedValue(myAccounts))
                    .put("tranCode", TransTypes.ABC_CASHIN_CUSTOMER)
                    .put("description", trans_reason)
                    .put("bankName", bankName)
                    .put("bankCode", bankCode)
                    .put("billerCode", billerCode)
                    .put("depositorPhoneNo", depositor_phone.getText().toString())
                    .put("depositorName", depositor_name.getText().toString())
                    .put("activity", TAG);
            callCustomerInquiry();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callCustomerInquiry() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
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

    private void determineTransCharge(JSONObject jsonObject) {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JSONObject transChargeRequest = new JSONObject();
                transChargeRequest.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
                requestObject.put("amount", transAmount.toString())
                        .put("transCode", TransTypes.ABC_CASHIN_CUSTOMER)
                        .put("accountNo", SpinnerUtil.getSelectedValue(myAccounts))
                        .put("activity", TAG);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/findTransactionCharge", requestObject,
                        response -> {
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

    // PRINTING STARTS HERE
    private void displayPostingConfirmationDetails(Double chargeAmount, JSONObject validationJsonResponse) {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setText(validationJsonResponse.optString("returnMessage")
                );
                pinNo = layoutView.findViewById(R.id.confirmPin);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Confirm</font>"));
                //alertDialogBuilder.setMessage(customerName);
                alertDialogBuilder
                        .setCancelable(false)
                        .setPositiveButton("PROCEED",
                                (dialog, id) -> {
                                    dialog.cancel();
                                    if (TextUtils.isEmpty(pinNo.getText())) {
                                        pinNo.setError("PIN Number is required");
                                        pinNo.requestFocus();
                                    } else {
                                        try {
                                            JSONObject authRequest = NetworkUtil.getBaseRequest(getActivity());
                                            authRequest.put("pinNo", pinNo.getText());
                                            requestObject.put("authRequest", authRequest);
                                            requestObject.put("customerName", validationJsonResponse.optJSONObject("returnObject").optString("acctTitle"));
                                            requestObject.put("paymentCode", "ABC_CASHIN");
                                            requestObject.put("billerTransRef", validationJsonResponse.optString("progressIndicator"));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        processCorporateAgentService();
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
                    response.optString("returnMessage")
                            + "<br>Bal: UGX " + NumberUtils.formatNumber(response.optString("drAcctBal"))));
            builder.setPositiveButton("OK",
                    (arg0, arg1) -> {
                        arg0.dismiss();
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .replace(R.id.container, new CentenaryMenu()).addToBackStack(null)
                                .commit();
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
