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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

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
import com.micropay.models.Transaction;
import com.micropay.popups.DialogUtils;
import com.micropay.utils.DATA_CONVERTER;
import com.micropay.utils.Dictionary;
import com.micropay.utils.NumberUtils;
import com.micropay.utils.PrinterUtils;
import com.micropay.utils.SpinnerUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TXNAirtelDataPurchase extends Fragment {

    private static final String lineString = "------------------------------------------";
    private final String TAG = TXNCashDeposit.class.getSimpleName();
    private final ExecutorService threadPool = Executors.newFixedThreadPool(1);
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private JSONObject requestObject = new JSONObject();
    private TextView recipient_phone_lbl;
    private Spinner myAccounts, airtime_recipeient, package_category, package_type;
    private TextInputEditText recipient_phone;
    private TextInputEditText pinNo;
    private List<Receipt> RECEIPT;
    private String billerCode = "AIRTEL_DATA";
    private String customer_account_no = null;
    private String trans_reason = null;
    private String recipientPhoneNo = null;
    String referenceNo = null, requestId = null, billerTransRef = null;
    private Transaction transaction;
    private boolean isPrinterConfigured = true;
    private boolean isPrintingEnabled = true;

    private Double transAmount;
    private Map<String, JSONObject> products = new HashMap<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.f_mtn_data, container, false);
        cacheUtil = new CacheUtil(requireActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) requireActivity();
        loanActivity.setTitle("Airtel Data-Customer");

        recipient_phone = rootView.findViewById(R.id.recipient_phone);
        myAccounts = rootView.findViewById(R.id.my_account);
        airtime_recipeient = rootView.findViewById(R.id.airtime_recipeient);
        package_type = rootView.findViewById(R.id.package_type);
        package_category = rootView.findViewById(R.id.package_category);
        recipient_phone_lbl = rootView.findViewById(R.id.recipient_phone_lbl);
        try {
            callDataCategoryApi(new JSONObject().put("billerCode", billerCode));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        package_category.setOnItemSelectedListener(packageCategorySelectionListener);
        package_type.setOnItemSelectedListener(billerProductSelectionListener);

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

        try {
            new SpinnerUtil().populateSpinner(Dictionary.findAirtimeRecipients(), airtime_recipeient, getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, "key", "value");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        airtime_recipeient.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View arg1, int position, long arg3) {
                String airTimeOption = parent.getItemAtPosition(position).toString().split("-")[0];
                if (!airTimeOption.toLowerCase().contains("select")) {
                    try {
                        if ("2".equals(airTimeOption)) {
                            recipient_phone_lbl.setVisibility(View.VISIBLE);
                            recipient_phone.setVisibility(View.VISIBLE);
                        } else {
                            recipient_phone_lbl.setVisibility(View.GONE);
                            recipient_phone.setVisibility(View.GONE);
                        }
                    } catch (Exception ex) {

                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub
            }
        });
        rootView.findViewById(R.id.process_deposit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUserEntries();
            }
        });
        setRetainInstance(true);
        return rootView;
    }

    protected void showToast(String message) {
        Toast.makeText(this.getActivity(), message, Toast.LENGTH_LONG).show();
    }

    private AdapterView.OnItemSelectedListener packageCategorySelectionListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            if (pos == 0) {
                //showToast("Invalid package category selected");
                return;
            }
            Picklist dictionary = (Picklist) package_category.getItemAtPosition(pos);
            String picklistCode = dictionary.getCode().toString();
            if (picklistCode.toLowerCase().contains("select")) return;
            JSONObject request = null;
            try {
                request = new JSONObject()
                        .put("authRequest", NetworkUtil.getBaseRequest(getActivity()))
                        .put("billerCode", billerCode)
                        .put("categoryCode", picklistCode);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            callDataPackageApi(request);
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };


    private void populateMyAccounts(JSONArray accountsList) {
        if (accountsList != null && accountsList.length() > 0) {
            new SpinnerUtil().populateSpinner(accountsList, myAccounts, getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, "acctNo", "acctNo");
        }
    }

    private void callDataCategoryApi(JSONObject request) {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/findBillerProductCategories", request,
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if (response != null && response.length() > 0) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                        JSONArray areas = response.optJSONArray("data");
                                        if (areas != null && areas.length() > 0) {
                                            new SpinnerUtil().populateSpinner(areas, package_category, getActivity(),
                                                    android.R.layout.simple_spinner_dropdown_item, "code", "description");
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
                    public Map<String, String> getHeaders() throws AuthFailureError {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Basic " + Constants.getRawBasicData());
                        headers.put("sessionId", cacheUtil.getString("sessionId"));
                        return headers;
                    }
                };
                jsonObjectRequest.setTag(TAG);
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Finding data categories", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callDataPackageApi(JSONObject request) {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/findBillerProducts2", request,
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if (response != null && response.length() > 0) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                        JSONArray jsonArray = response.optJSONArray("data");
                                        try {
                                            renderProducts(jsonArray);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        showAlertDialog(response.optJSONObject("response").optString("responseMessage"));
                                    }
                                } else
                                    showAlertDialog(getActivity().getString(R.string.resp_timeout));
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Finding Packages", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    protected void renderProducts(JSONArray adaptorInfo) throws JSONException {
        if (adaptorInfo != null && adaptorInfo.length() > 0) {
            JSONObject item;
            String value;
            for (int i = 0; i < adaptorInfo.length(); i++) {
                item = adaptorInfo.optJSONObject(i);
                value = item.optString("code");
                products.put(value, item);
                transAmount = item.optDouble("amount");
            }
            new SpinnerUtil().populateSpinner(adaptorInfo, package_type, getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, "code", "description");

        }
    }


    private AdapterView.OnItemSelectedListener billerProductSelectionListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
            if (pos == 0) {
                //showToast("Invalid residence country selected");
                return;
            }
            Picklist dictionary = (Picklist) package_type.getItemAtPosition(pos);
            if (dictionary.getCode().toString().toLowerCase().contains("select")) return;
            if (products != null && products.size() > 0) {
                try {
                    for (Map.Entry<String, JSONObject> entry : products.entrySet()) {
                        JSONObject asJsonObject = entry.getValue();
                        if (!asJsonObject.optString("code").equals(dictionary.getCode().toString()))
                            continue;
                        transAmount = asJsonObject.optDouble("amount");
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
        }
    };


    private void callBillPaymentApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/processBillPayment2", requestObject,
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
        try {
            if (1 == 2) { // (isPrintingEnabled && !isPrinterConfigured) {
                showAlertDialog("Printer is not configured");
                return;
            }
            if (SpinnerUtil.getSelectedValue(myAccounts).equalsIgnoreCase("0")) {
                showAlertDialog("Select float account to debit");
                return;
            }
            if (airtime_recipeient.getSelectedItemPosition() <= 0) {
                showAlertDialog("Select recipient option");
                return;
            }
            if (SpinnerUtil.getSelectedValue(package_type).equalsIgnoreCase("0")) {
                showAlertDialog("Select data package");
                return;
            }

//            if (TextUtils.isEmpty(tran_amount.getText())) {
//                showAlertDialog("Amount is required");
//                return;
//            }

            if (recipient_phone.getVisibility() == View.VISIBLE) {
                if (TextUtils.isEmpty(recipient_phone.getText())) {
                    showAlertDialog("Recipient phone is required");
                    return;
                }
            }

            if (recipient_phone.getVisibility() == View.VISIBLE)
                recipientPhoneNo = DATA_CONVERTER.getInternationalFormat(recipient_phone.getText().toString());
            else
                recipientPhoneNo = DATA_CONVERTER.getInternationalFormat(cacheUtil.getString("registeredPhone"));
            trans_reason = "Airtime purchase" + "-" + recipientPhoneNo;
            requestObject = new JSONObject();
            requestObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            requestObject.put("currency", "UGX")
                    .put("tranCode", TransTypes.AIRTEL_DATA_CUSTOMER)
                    .put("transAmt", transAmount)
                    .put("drAcctNo", SpinnerUtil.getSelectedValue(myAccounts))
                    .put("paymentCode", SpinnerUtil.getSelectedKey(package_type))
                    .put("description", trans_reason)
                    .put("referenceNo", recipientPhoneNo)
                    .put("billerCode", "AIRTEL_MONEY")
                    .put("paymentCode", "21")
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
                requestObject.put("amount", transAmount)
                        .put("transCode", TransTypes.AIRTEL_AIRTIME_AGENT)
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
                                        displayCashDepositTransactionSuccess(referenceInquiryResponse);
                                    } else {
                                        String errorMessage = response.optJSONObject("response").optString("responseMessage");
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

    public void displayCashDepositTransactionSuccess(JSONObject referenceInquiryResponse) {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = (TextView) layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setText("Confirm data bundle purchase of " + SpinnerUtil.getSelectedValue(package_type) + ", " + "Customer Name: " + referenceInquiryResponse.optString("customerName") + " " + Html.fromHtml(" to phone number " + recipientPhoneNo));
                pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Confirm Data purchase</font>"));
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
                                            requestObject.put("crAcctNo", customer_account_no)
                                                    .put("paymentCode", SpinnerUtil.getSelectedKey(package_type));
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        callBillPaymentApi();
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
                    "You have bought data bundles of " + SpinnerUtil.getSelectedValue(package_type) + " to phone number " + DATA_CONVERTER.getInternationalFormat(recipientPhoneNo)
                            + "<br>Trans ID: " + response.optString("transId")
                            + "<br>Charge: UGX" + NumberUtils.formatNumber(response.optString("chargeAmt"))
                            + "<br>Bal: UGX " + NumberUtils.formatNumber(response.optString("drAcctBal"))
            ));
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

    void generatePrintContent(JSONObject response) {
        RECEIPT = new ArrayList<>();
        String transDate = new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss").format(new Date());

        Receipt data = new Receipt("Lyca Data Purchase", null);
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
        data = new Receipt("Customer Phone:", recipientPhoneNo);
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
