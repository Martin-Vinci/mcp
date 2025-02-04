package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.RemoteException;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

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
import com.micropay.models.Transaction;
import com.micropay.utils.NumberUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import wangpos.sdk4.libbasebinder.Printer;

public class LoanRepayment extends Fragment {

    private final String TAG = LoanRepayment.class.getSimpleName();
    private CacheUtil cacheUtil;

    private AlertDialog alertDialog;

    private EditText repayment_acct, repayment_amt, repayment_rsn;

    private JSONObject requestObject, drawer_info, outletCredentials;
    private String cust_no, retrievalReference, cust_nm;
    private String loanAccount;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);
    private boolean printingEnabled = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fcredit_deposit, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Loan Repayment");

        repayment_acct = rootView.findViewById(R.id.repayment_acct);
        repayment_amt = rootView.findViewById(R.id.repayment_amt);
        repayment_rsn = rootView.findViewById(R.id.repayment_rsn);
        repayment_rsn.setText("LOAN REPAYMENT");

        Bundle arguments = getArguments();
        if (arguments != null) {
//            repayment_acct.setText(arguments.getString("REPAY_ACCT"));
            disAllowEdits(repayment_acct);
            this.cust_no = arguments.getString("CUST_NO");
            this.cust_nm = arguments.getString("CUST_NM");
            this.loanAccount = arguments.getString("ACCT_NO");
            repayment_acct.setText(loanAccount);
            if (cacheUtil.getBoolean("builtin_printer", false))
                submitJob(initBuiltInPrinter);
            else
                printingEnabled = cacheUtil.getString("bluetooth_address").trim().length() > 5;
        }

        rootView.findViewById(R.id.process).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processPayment();
            }
        });

        if (cacheUtil.getBoolean("isTeller", false))
            callUserDrawerValidationApi();

        setRetainInstance(true);
        return rootView;
    }

    private void disAllowEdits(EditText editText) {
        editText.setTag(editText.getKeyListener());
        editText.setKeyListener(null);
    }

    private void callUserDrawerValidationApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/verifyDrawerDetails", NetworkUtil.getBaseRequest(getActivity())
                        .put("agentNo", cacheUtil.getString("userLoginId"))
                        .put("buId", cacheUtil.getLong("buId", 0))
                        .put("activity", TAG),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode")) &&
                                            response.optJSONArray("drawer_info").length() >= 1) {
                                        //save the drawer information
                                        drawer_info = response.optJSONArray("drawer_info").optJSONObject(0);
                                    } else
                                        showAlertDialogAndExit(response.optString("responseTxt"));
                                } else
                                    showAlertDialogAndExit(getActivity().getString(R.string.resp_timeout));
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
                NetworkUtil.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Verifying drawer...", false);
            } else {
                showAlertDialog("Cannot connect to server. No network available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callLoanRepaymentApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/loanRepaymentByCash", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        retrievalReference = response.optString("reference_no");
                                        buildTransaction();
                                        //showAlertAndPrintOption(response.optString("responseTxt"));
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
                NetworkUtil.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Authorizing...", false);
            } else {
                showAlertDialog("Cannot connect to server. No network available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showImageViewOptionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        builder.setTitle("VIEW CUSTOMER IMAGE");
        builder.setMessage("This will allow you verify this customer's image.")
                .setPositiveButton("VIEW PHOTO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                callCustomerImagesApi();
                            }
                        }).setNegativeButton("Cancel",
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

    private void callCustomerImagesApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/getPhotoAndSignature", new JSONObject()
                        .put("outletCredentials", outletCredentials)
                        .put("customerNo", cust_no),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        JSONArray search_results = response.optJSONArray("search_results");

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
                NetworkUtil.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Retrieving photo...", false);
            } else {
                showAlertDialog("Cannot connect to server. No network available");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void showContinueAnywayDialog(String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        builder.setMessage(body)
                .setPositiveButton("PROCEED ANYWAY",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                showConfirmationDialog();
                            }
                        }).setNeutralButton("RETRY",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {
                        arg0.dismiss();
                        //retry image retrieval
                        callCustomerImagesApi();
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

    private void showConfirmationDialog() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            builder.setMessage(getActivity().getString(R.string.cust_verified))
                    .setPositiveButton("PROCEED",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                    callLoanRepaymentApi();
                                }
                            }).setNegativeButton("CANCEL",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                        }
                    }).setCancelable(false);
            renderDialog(builder);
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


    private void processPayment() {
        try {
            if (TextUtils.isEmpty(repayment_acct.getText())) {
                repayment_acct.requestFocus();
                showAlertDialog("Please specify a valid repayment account");
                return;
            }
            if (TextUtils.isEmpty(repayment_amt.getText())) {
                repayment_amt.requestFocus();
                showAlertDialog("Please specify a valid repayment amount");
                return;
            }
            if (TextUtils.isEmpty(repayment_rsn.getText())) {
                repayment_rsn.requestFocus();
                showAlertDialog("Please specify a valid repayment reason");
                return;
            }

            outletCredentials = NetworkUtil.getBaseRequest(getActivity());
            if (cacheUtil.getBoolean("isTeller", false)) {
                outletCredentials.put("bankOfficerId", drawer_info.optLong("SYSUSER_ID", -99l))
                        .put("agentNo", cacheUtil.getString("userLoginId"))
                        .put("agentType", cacheUtil.getString("agentType"))
                        .put("buRoleId", cacheUtil.getLong("buRoleId", -99l))
                        .put("buId", cacheUtil.getLong("buId", -99l))
                        .put("drawerUserRoleId", drawer_info.optLong("USER_ROLE_ID", -99l))
                        .put("userRoleId", drawer_info.optLong("USER_ROLE_ID", -99l))
                        .put("drawerNo", drawer_info.optString("DRAWER_NO"))
                        .put("drawerCrncyId", drawer_info.optLong("LOCAL_CRNCY_ID", 840l));
            } else {
                outletCredentials.put("agentAcctNo", cacheUtil.getString("OUTLET_FLOAT_ACCT"))
                        .put("agentType", cacheUtil.getString("agentType"))
                        .put("agentNo", cacheUtil.getString("AGENT_NO"));
            }
            requestObject = new JSONObject()
                    .put("outletCredentials", outletCredentials)
                    .put("accountNumber1", cacheUtil.getString("OUTLET_FLOAT_ACCT"))
                    .put("accountNumber2", repayment_acct.getText().toString())
                    .put("tranAmount", new BigDecimal(repayment_amt.getText().toString()))
                    .put("description", repayment_rsn.getText().toString() + "[" + loanAccount + "]")
                    .put("activity", TAG);

            showImageViewOptionDialog();

        } catch (Exception e) {
            e.printStackTrace();
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

    private void showAlertDialogAndExit(String responseTxt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        builder.setMessage(responseTxt)
                .setPositiveButton("DISMISS",
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


    private void submitJob(Runnable runnable) {
        try {
            threadPool.submit(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*BUILTIN PRINTER OPTIONS*/

    private Thread initBuiltInPrinter = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                buildInPrinter = new Printer(getActivity().getApplicationContext());
                isPrinterConfigured = true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    });

    private Thread printReceipt = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                if (isPrinterConfigured && buildInPrinter != null) {
                    buildInPrinter.printInit();
                    buildInPrinter.clearPrintDataCache();
                    addBankLogo();
                    addTransactionContent();
                    buildInPrinter.printPaper_trade(2, 50);
                    buildInPrinter.printFinish();
                } else {
                    Toast.makeText(getActivity(), "Printer not ready", Toast.LENGTH_SHORT).show();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                notifyCompletion();
            }
        }
    });

    private void notifyCompletion() {
        getActivity().finish();
    }

    private void addBankLogo() {
        try {
            InputStream is = getActivity().getAssets().open("white_logo.png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            buildInPrinter.printImageBase(bitmap, 150, 150, Printer.Align.CENTER, 0);
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    public void addTransactionContent() {
        try {
            buildInPrinter.printString("MICROPAY (U) LTD", 30, Printer.Align.CENTER, true, false);
            buildInPrinter.printString("LOAN REPAYMENT", 25, Printer.Align.CENTER, false, false);
            buildInPrinter.printString("Approved", 22, Printer.Align.CENTER, false, false);
            buildInPrinter.printString(lineString, 30, Printer.Align.CENTER, false, false);
            buildInPrinter.print2StringInLine("Account", requestObject.optString("accountNumber2")
                            .replace(requestObject.optString("accountNumber2").substring(3, 9), "xxxxxx"),
                    1.0f, Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false, false,
                    false);
            buildInPrinter.print2StringInLine("Amount", "Ksh." +
                            NumberUtils.formatNumber(requestObject.optString("tranAmount")),
                    1.0f, Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false,
                    false, false);

            buildInPrinter.print2StringInLine("Merchant", cacheUtil.getString("AGENT_NO"), 1.0f,
                    Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false, false, false);
            buildInPrinter.print2StringInLine("Outlet", cacheUtil.getString("OUTLET_CD"),
                    1.0f, Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false,
                    false, false);
            buildInPrinter.print2StringInLine("Date", cacheUtil.getString("processingDate"),
                    1.0f, Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false,
                    false, false);
            buildInPrinter.print2StringInLine("RRN", retrievalReference, 1.0f, Printer.Font.MONOSPACE,
                    22, Printer.Align.LEFT, false, false, false);
            String[] split = cust_nm.split(" ");
            if (split != null) {
                if (split.length > 2)
                    buildInPrinter.print2StringInLine("Customer", split[0] + " " + split[1],
                            1.0f, Printer.Font.MONOSPACE,
                            22, Printer.Align.LEFT, false, false, false);
                else
                    buildInPrinter.print2StringInLine("Customer", cust_nm, 1.0f, Printer.Font.MONOSPACE,
                            22, Printer.Align.LEFT, false, false, false);
            } else
                buildInPrinter.print2StringInLine("Customer", cust_nm, 1.0f, Printer.Font.MONOSPACE,
                        22, Printer.Align.LEFT, false, false, false);

            buildInPrinter.print2StringInLine("Dpd By", split[0] + " " + split[1], 1.0f,
                    Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false, false, false);
            buildInPrinter.printString(lineString, 30, Printer.Align.CENTER, false, false);
            buildInPrinter.printString("Your Success, Our Success", 22, Printer.Align.CENTER, false,
                    true);
            buildInPrinter.printString("Thank you for banking with us!", 22, Printer.Align.CENTER,
                    false, false);

            buildInPrinter.printString(lineString, 30, Printer.Align.CENTER, false, false);
            buildInPrinter.printString("", 30, Printer.Align.CENTER, false, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean isPrinterConfigured = false;
    private Printer buildInPrinter;
    private static final String lineString = "------------------------------------------";

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

    private void closeAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
    }

    @Override
    public void onPause() {
        closeAllDialogs();
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        NetworkUtil.getInstance(getActivity().getApplicationContext()).cancel(TAG);
    }


    private Transaction transaction;

    /*THERMAL PRINTER OPTIONS*/
    private void buildTransaction() {
        transaction = new Transaction();
        transaction.setAccount(requestObject.optString("accountNumber2")
                .replace(requestObject.optString("accountNumber2").substring(3, 9),
                        "XXXXXX"));
        transaction.setAgent(cacheUtil.getString("AGENT_NO"));
        transaction.setAmount(NumberUtils.formatNumber(requestObject.optString("tranAmount")));
        transaction.setCurrency("Ksh.");
        transaction.setDate(cacheUtil.getString("processingDate") + " " +
                new SimpleDateFormat("HH:mm").format(new Date()));
        transaction.setOutlet(cacheUtil.getString("OUTLET_CD"));
        transaction.setReceiptTitle("MICROPAY (U) LTD");
        transaction.setReference(retrievalReference);
        transaction.setTranStatus("Approved");
        transaction.setTranType("LOAN REPAYMENT");
        String[] split = cust_nm.split(" ");
        if (split != null) {
            transaction.setCustomer(split.length > 2 ? split[0] + " " + split[1] : cust_nm);
        } else
            transaction.setCustomer(cust_nm);
    }



}
