package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import com.google.android.material.textfield.TextInputEditText;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.models.StudentDetails;
import com.morpho.android.usb.USBManager;
import com.morpho.morphosmart.sdk.CallbackMessage;
import com.morpho.morphosmart.sdk.Coder;
import com.morpho.morphosmart.sdk.CompressionAlgorithm;
import com.morpho.morphosmart.sdk.DetectionMode;
import com.morpho.morphosmart.sdk.EnrollmentType;
import com.morpho.morphosmart.sdk.ErrorCodes;
import com.morpho.morphosmart.sdk.LatentDetection;
import com.morpho.morphosmart.sdk.MorphoDevice;
import com.morpho.morphosmart.sdk.MorphoImage;
import com.morpho.morphosmart.sdk.StrategyAcquisitionMode;
import com.morpho.morphosmart.sdk.Template;
import com.morpho.morphosmart.sdk.TemplateFVPType;
import com.morpho.morphosmart.sdk.TemplateList;
import com.morpho.morphosmart.sdk.TemplateType;
import com.micropay.adaptor.StatementAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.popups.DialogUtils;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.models.Transaction;
import com.micropay.utils.BiometricUtils;
import com.micropay.utils.NumberUtils;
import com.micropay.utils.ThermalStmt;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import uk.me.hardill.volley.multipart.MultipartRequest;
import wangpos.sdk4.libbasebinder.Printer;

public class StatementListing extends Fragment {

    private final String TAG = StatementListing.class.getSimpleName();
    private RecyclerView recyclerView;
    private StatementAdaptor adapter;

    private SwipeRefreshLayout swipeContainer;
    private CacheUtil cacheUtil;

    private AlertDialog alertDialog;
    private String acct_no, cust_nm;

    private JSONArray search_results;
    private ExecutorService service = Executors.newFixedThreadPool(2);

    private String cust_no, strMessage;
    private MorphoDevice morphoDevice;
    private Handler fingerprintHandler = new Handler();
    private Activity mCtx;
    public StatementListing(JSONArray list, Activity mCtx) {
        this.search_results = list;
        this.mCtx = mCtx;
        cacheUtil = new CacheUtil(mCtx);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.nav, menu);
        menu.findItem(R.id.action_signout).setVisible(false);
        menu.findItem(R.id.action_print).setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                stepBack();
                return true;
            case R.id.action_print:
                if (printingEnabled)
                    evaluatePrintingOptions();
                else
                    showAlertDialog("The printer was not configured.");
                return true;
        }
        return false;
    }

    public void stepBack() {
        if (getFragmentManager().getBackStackEntryCount() <= 0) {
            getActivity().onBackPressed();
        } else {
            getFragmentManager().popBackStack();
        }
        getActivity().overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private boolean printingEnabled, scannerEnabled;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.stmt_content_layout, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());
        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Account Statement");
        recyclerView = rootView.findViewById(R.id.recyclerView);
        fillRecyclerView();


        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
//        recyclerView = rootView.findViewById(R.id.recyclerView);
//        recyclerView.setHasFixedSize(true);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
//        recyclerView.setItemAnimator(new DefaultItemAnimator());
//        renderStatement();
//        Bundle arguments = getArguments();
//        if (arguments != null) {
//            this.acct_no = arguments.getString("ACCT_NO");
//            this.cust_nm = arguments.getString("CUST_NM");
//            this.cust_no = arguments.getString("CUST_NO");
//        }

//        if (cacheUtil.getBoolean("builtin_printer", false))
//            submitJob(printerInit);
//        else if (cacheUtil.getString("bluetooth_address").trim().length() <= 0) {
//            printingEnabled = false;
//        }
//        scannerEnabled = cacheUtil.getBoolean("builtin_scanner", false);
//
//        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_light,
//                android.R.color.holo_green_light,
//                android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);

        setHasOptionsMenu(true);
        setRetainInstance(true);
        return rootView;
    }

    public void fillRecyclerView(){
        adapter = new StatementAdaptor(search_results, getActivity());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);

    }

    public void showAlertDialog(String title, String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            if (title != null)
                builder.setTitle(title);
            builder.setMessage(body)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                    stepBack();
                                }
                            }).setCancelable(false);
            renderDialog(builder);
        }
    }

    private void submitJob(Runnable runnable) {
        try {
            service.submit(runnable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlertDialogForExit(String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
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
            renderDialog(builder);
        }
    }
    public void showAlertDialog(String body) {
        if (getActivity() != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            builder.setMessage(body)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                    stepBack();
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
    private Runnable printerInit = new Runnable() {
        @Override
        public void run() {
            try {
                buildInPrinter = new Printer(getActivity().getApplicationContext());
                buildInPrinter.setPrintFontType(getActivity(), "");
                isPrinterConfigured = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable builtInPrinterActivity = new Runnable() {
        @Override
        public void run() {
            try {
                if (isPrinterConfigured) {
                    printCopy();
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(getActivity(), "Printer not ready", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            getActivity().runOnUiThread(notifymainthread);
        }

        private void printCopy() throws RemoteException {
            buildInPrinter.printInit();
            buildInPrinter.clearPrintDataCache();
            loadBankLogo();
            appendToBuildInPrinterBuffer();
            buildInPrinter.printPaper_trade(2, 50);
            buildInPrinter.printFinish();
        }

    };

    private void loadBankLogo() {
        try {
            InputStream is = getActivity().getAssets().open("white_logo.png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            buildInPrinter.printImageBase(bitmap, 100, 100, Printer.Align.CENTER, 0);
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    private Runnable notifymainthread = new Runnable() {
        @Override
        public void run() {
            if (getActivity() != null && !getActivity().isFinishing()) getActivity().finish();
        }
    };


    public void appendToBuildInPrinterBuffer() {

        try {
            buildInPrinter.printString("MICROPAY (U) LTD", 30,
                    Printer.Align.CENTER, true, false);

            buildInPrinter.printString(transaction.getTranType(), 25,
                    Printer.Align.CENTER, false, false);

            buildInPrinter.printString(transaction.getCustomer(), 22,
                    Printer.Align.CENTER, false, false);

            buildInPrinter.printString(transaction.getAccount(), 22,
                    Printer.Align.CENTER, false, false);
            buildInPrinter.printString(transaction.getDate(), 22, Printer.Align.CENTER, false, false);

            buildInPrinter.printString(transaction.getTranStatus(), 22,
                    Printer.Align.CENTER, false, false);
            buildInPrinter.printString(lineString, 30, Printer.Align.CENTER,
                    false, false);

            JSONObject jsonObject = null;
            for (int i = 0; i < 5; i++) {
                jsonObject = transaction.getJsonArray().optJSONObject(i);

                buildInPrinter.print2StringInLine("RRN", jsonObject.optString("TRAN_JOURNAL_ID"),
                        1.0f, Printer.Font.MONOSPACE, 20, Printer.Align.LEFT, false, false,
                        false);
                buildInPrinter.print2StringInLine("Date", jsonObject.optString("TRAN_DT"),
                        1.0f, Printer.Font.MONOSPACE, 20, Printer.Align.LEFT, false,
                        false, false);

                buildInPrinter.print2StringInLine("Amount", "Ksh." +
                                NumberUtils.formatNumber(jsonObject.optLong("AMOUNT")), 1.0f,
                        Printer.Font.MONOSPACE, 20, Printer.Align.LEFT, false, false, false);

                String desc = jsonObject.optString("TRAN_DESC");
                if (desc != null && desc.length() > 32) {
                    buildInPrinter.printString(desc.substring(0, 32),
                            20, Printer.Align.LEFT,
                            false, false);
                } else
                    buildInPrinter.printString(desc.substring(0, 20),
                            20, Printer.Align.LEFT,
                            false, false);
            }
            if (jsonObject != null) {
                buildInPrinter.print2StringInLine("Closing Bal", "Ksh." +
                                NumberUtils.formatNumber(jsonObject.optString("LEDGER_BAL")),
                        1.0f, Printer.Font.MONOSPACE, 25, Printer.Align.LEFT, false,
                        false, false);
            }
            buildInPrinter.print2StringInLine("Merchant", transaction.getAgent(), 1.0f,
                    Printer.Font.MONOSPACE, 25, Printer.Align.LEFT, false, false, false);

            buildInPrinter.print2StringInLine("Outlet", transaction.getOutlet(),
                    1.0f, Printer.Font.MONOSPACE, 25, Printer.Align.LEFT, false,
                    false, false);

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

    private void buildTransaction() {
        transaction = new Transaction();
        transaction.setJsonArray(search_results);

        //transaction.setCustomer(cust_nm.toUpperCase());
        //transaction.setAccount(acct_no.toUpperCase().replace(acct_no.substring(3, 9), "xxxxxx"));

        transaction.setTranStatus("Approved");
        transaction.setTranType("STATEMENT");
        transaction.setOutlet(cacheUtil.getString("outletCode"));
        transaction.setReceiptTitle("MICROPAY (U) LTD");
        transaction.setAgent(cacheUtil.getString("outletCode"));
    }

    private void evaluatePrintingOptions() {
        if (search_results == null || search_results.length() <= 0) {
            showAlertDialogForExit("No statement available for printing.");
            return;
        }
        if (cacheUtil.getBoolean("builtin_printer", false)) {
            showPrinterOptionDialog("Proceed to print a copy for the customer?", true);
        } else if (cacheUtil.getString("bluetooth_address").trim().length() > 0) {
            showPrinterOptionDialog("Proceed to print a copy for the customer?", false);
        } else {
            showAlertDialog("Unfortunately you have not configured any printer option");
        }
    }

    private void showPrinterOptionDialog(String strMessage, final boolean builtInOption) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            builder.setMessage(strMessage)
                    .setPositiveButton("YES",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                    if (builtInOption)
                                        submitJob(builtInPrinterActivity);
                                    else
                                        callBluetoothPrinterAPI(cacheUtil.getString("bluetooth_address").trim());
                                }
                            }).setNegativeButton("CANCEL",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            arg0.dismiss();
                            stepBack();
                        }
                    }).setCancelable(false);
            renderDialog(builder);
        }
    }

    private boolean isPrinterConfigured = false;
    private Printer buildInPrinter;
    private static final String lineString = "------------------------------------------";

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


    @Override
    public void onDestroy() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        try {
            service.shutdown();
            service.awaitTermination(1, TimeUnit.SECONDS);
            if (morphoDevice != null) {
                morphoDevice.closeDevice();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    /*THERMAL PRINTER*/

    private Transaction transaction;

    private void callBluetoothPrinterAPI(String bluetooth_address) {
        ThermalStmt thermalUtils = new ThermalStmt(getActivity(), transaction);
        AsyncTask<String, Void, Integer> execute = thermalUtils.execute();
        try {
            execute.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } finally {
            getActivity().finish();
        }
    }

    private String chargeAccount;

    public void showBasicAlertDialog(final String body, final boolean showAccountListPrompt) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            builder.setMessage(body)
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                }
                            }).setCancelable(false);
            renderDialog(builder);
        }
    }

}
