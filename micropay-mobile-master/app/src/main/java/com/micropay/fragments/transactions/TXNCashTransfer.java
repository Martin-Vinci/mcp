package com.micropay.fragments.transactions;

/**
 * Created by micropay on 01/25/2021.
 */

import android.annotation.SuppressLint;
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
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.models.Receipt;
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
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.DisplayUtils;
import com.micropay.api.NetworkUtil;
import com.micropay.popups.DialogUtils;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.models.Transaction;
import com.micropay.utils.BiometricUtils;
import com.micropay.utils.NumberUtils;
import com.micropay.utils.PrinterUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
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

public class TXNCashTransfer extends Fragment implements Observer {

    private final String TAG = TXNCashTransfer.class.getSimpleName();
    private CacheUtil cacheUtil;

    private AlertDialog alertDialog;

    private TextInputEditText tran_reason, tran_amount, source_acct, recipient_acct;
    private String cust_no;

    private JSONObject requestObject, outletCredentials;
    private byte[] imageData;

    private MorphoDevice morphoDevice;
    private String strMessage;

    private Handler fingerprintHandler = new Handler();

    private ImageView finger_img;
    private TextView status_label;

    private String cust_nm;
    private String crnry, retrievalReference;

    boolean useBiometrics, skipCharge = false, printerEnabled = true;

    private ExecutorService threadPool = Executors.newFixedThreadPool(4);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fagent_transfer, container, false);
        cacheUtil = new CacheUtil(requireActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) requireActivity();
        loanActivity.setTitle(getActivity().getString(R.string.funds_transfer));

        source_acct = rootView.findViewById(R.id.source_acct);
        recipient_acct = rootView.findViewById(R.id.recipient_acct);
        tran_amount = rootView.findViewById(R.id.tran_amount);
        tran_reason = rootView.findViewById(R.id.tran_reason);
        charge_acct = rootView.findViewById(R.id.charge_acct);
        tran_amount.addTextChangedListener(NumberUtils.onTextChangedListener(tran_amount));

        DisplayUtils.disableEditing(source_acct);
        Bundle arguments = getArguments();

        if (arguments != null) {
            source_acct.setText(arguments.getString("ACCT_NO"));
            this.cust_no = arguments.getString("CUST_NO");
            this.cust_nm = arguments.getString("CUST_NM");
            this.crnry = arguments.getString("CRNCY_SYM");
        }

        rootView.findViewById(R.id.process_txn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validateUserEntries();
            }
        });

        if (cacheUtil.getBoolean("builtin_printer", false))
            executeRunnable(printerInitThread);
        else if (cacheUtil.getString("bluetooth_address").trim().length() <= 0) {
            printerEnabled = false;
        }

        if (cacheUtil.getBoolean("builtin_scanner", false))
            initializeBiometricScanner();
        else {
            useBiometrics = false;
        }

        setupBaseObject();
        refreshAccountList();

        setRetainInstance(true);
        return rootView;
    }

    private void showAlertDialogAndExit(String responseTxt) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.dialogTheme);
            builder.setMessage(responseTxt)
                    .setPositiveButton("CLOSE",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                    requireActivity().finish();
                                }
                            }).setCancelable(false);
            renderDialog(builder);
        }
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
            alertDialog = builder.create();
            if (alertDialog != null && !alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private void executeRunnable(Runnable runnable) {
        try {
            threadPool.submit(runnable);
        } catch (Exception e) {
            e.printStackTrace();
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
            alertDialog = builder.create();
            if (alertDialog != null && !alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private void callFundsTransferApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/depositFundsTransfer", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        retrievalReference = response.optString("reference_no");
                                        buildTransaction();
                                        //evaluatePrintingOptions("Customer");
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
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Authorizing.", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void validateUserEntries() {
        try {
            if (TextUtils.isEmpty(source_acct.getText())) {
                showAlertDialog("Invalid source account");
                return;
            }
            if (TextUtils.isEmpty(recipient_acct.getText())) {
                showAlertDialog("Invalid recipient account");
                return;
            }
            if (TextUtils.isEmpty(tran_amount.getText())) {
                showAlertDialog("Invalid transaction amount");
                return;
            }
            if (TextUtils.isEmpty(tran_reason.getText())) {
                showAlertDialog("Invalid transaction narration");
                return;
            }
            if (charge_acct.getSelectedItemPosition() < 1) {
                showAlertDialog("Invalid transaction fee account selected");
                return;
            }

            requestObject = new JSONObject()
                    .put("outletCredentials", outletCredentials)
                    .put("accountNumber1", source_acct.getText().toString())
                    .put("accountNumber2", recipient_acct.getText().toString())
                    .put("tranAmount", NumberUtils.convertToBigDecimal(tran_amount.getText().toString()))
                    .put("description", tran_reason.getText().toString())
                    .put("referenceNo", String.valueOf(System.nanoTime()))
                    .put("chargeAccount", charge_acct.getSelectedItem().toString())
                    .put("skipCharge", skipCharge)
                    .put("activity", TAG);

            showAuthenticationOptions();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showAuthenticationOptions() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            CharSequence options[] = new CharSequence[]{"Use Biometrics", "Use OTP Validation"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            builder.setCancelable(false);
            builder.setTitle("Authorization Options");

            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    switch (which) {
                        case 0:
                            showBiometricVerificationDialog();
                            break;
                        case 1:
                            callInitiateOtpRequest();
                            break;
                    }
                }
            });
            builder.setNegativeButton("DISMISS", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog alertDialogObject = builder.create();
            ListView listView = alertDialogObject.getListView();
            listView.setDivider(new ColorDrawable(Color.BLACK)); // set color
            listView.setDividerHeight(2); // set height
            alertDialogObject.show();
        }
    }

    private void showOTPDialogPrompt() {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.dotp_form, null);
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder =
                        new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle("Enter OTP To Verify");
                final TextInputEditText otpTextView = layoutView.findViewById(R.id.otpCode);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("PROCEED",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                callOTPValidationAPI(otpTextView.getText().toString());
                            }
                        });
                alertDialogBuilder.setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        });
                renderDialog(alertDialogBuilder);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

    private String phoneNo;

    private void callInitiateOtpRequest() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/initiateOtpRequest", new JSONObject()
                        .put("outletCredentials", outletCredentials)
                        .put("requestType", "Customer")
                        .put("requestTypeDesc", "Funds Transfer")
                        .put("customerNo", cust_no != null ? cust_no : "")
                        .put("accountNo", source_acct.getText().toString())
                        .put("transType", 999),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        phoneNo = response.optString("phone_no");
                                        showOTPDialogPrompt();
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
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Requesting OTP.", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void callOTPValidationAPI(String otpValue) {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/verifyOtpValidity", new JSONObject()
                        .put("outletCredentials", outletCredentials)
                        .put("otp", otpValue)
                        .put("phone", phoneNo)
                        .put("transType", 999)
                        .put("accountNo", source_acct.getText().toString()),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        showConfirmationDialog();
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
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Authorizing.", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showBiometricVerificationDialog() {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.biometric_dialog, null);
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder =
                        new androidx.appcompat.app.AlertDialog.Builder(requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle("Verify Biometric");
                finger_img = layoutView.findViewById(R.id.finger_img);
                status_label = layoutView.findViewById(R.id.status_label);
                alertDialogBuilder.setCancelable(false);
                alertDialogBuilder.setPositiveButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        });
                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                alertDialog = alertDialogBuilder.create();
                if (!alertDialog.isShowing()) {
                    alertDialog.show();
                    threadPool.submit(new BiometricService());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initializeBiometricScanner() {
        try {
            USBManager.getInstance().initialize(requireActivity(),
                    "com.agentbanking.micropay.USB_ACTION", true);
            if (USBManager.getInstance().isDevicesHasPermission()) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (morphoDevice != null) {
                            morphoDevice.closeDevice();
                            morphoDevice = null;
                        }
                        morphoDevice = new MorphoDevice();
                        morphoDevice.rebootSoft(20, TXNCashTransfer.this);
                        checkAttachedActiveUsbDevices();
                        useBiometrics = true;
                    }
                }, TimeUnit.SECONDS.toMillis(3));
            } else {
                showAlertDialog("USB permission is required to access fingerprint device");
            }
        } catch (Exception e) {

        }
    }

    protected void checkAttachedActiveUsbDevices() {
        try {
            Integer nbUsbDevice = new Integer(0);
            int usbDeviceResp = morphoDevice.initUsbDevicesNameEnum(nbUsbDevice);
            if (usbDeviceResp == ErrorCodes.MORPHO_OK) {
                if (nbUsbDevice > 0) {
                    morphoDevice.openUsbDevice(morphoDevice.getUsbDeviceName(0), 0);
                } else {
                    showFingerPrintAlert("Biometric Scanner Alert",
                            "Unable to detect the biometric device. Ensure the device is activated and access permission granted");
                }
            } else {
                showAlertDialog("Biometric Scanner Alert", ErrorCodes.getError(usbDeviceResp, morphoDevice.getInternalError()));
            }
        } catch (Exception e) {

        }
    }

    public void showFingerPrintAlert(String title, String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            if (title != null)
                builder.setTitle(title);
            builder.setMessage(body)
                    .setPositiveButton("RETRY",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface listener, int arg1) {
                                    listener.dismiss();
                                    initializeBiometricScanner();
                                }
                            }).setNegativeButton("CANCEL",
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

    private class BiometricService implements Runnable {
        @Override
        public void run() {
            int timeout = 0;
            int acquisitionThreshold = 0;
            int advancedSecurityLevelsRequired = 0xFF;
            int maxSizeTemplate = 255;
            int nbFinger = 1;

            final TemplateList templateList = new TemplateList();
            TemplateType templateType = TemplateType.MORPHO_PK_ISO_FMR;
            TemplateFVPType templateFVPType = TemplateFVPType.MORPHO_NO_PK_FVP;

            EnrollmentType enrollType = EnrollmentType.ONE_ACQUISITIONS;
            LatentDetection latentDetection = LatentDetection.LATENT_DETECT_ENABLE;

            Coder coderChoice = Coder.MORPHO_DEFAULT_CODER;
            int detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
            detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();

            int ret = morphoDevice.setStrategyAcquisitionMode(StrategyAcquisitionMode.MORPHO_ACQ_EXPERT_MODE);
            if (ret == 0) {
                ret = morphoDevice.capture(timeout, acquisitionThreshold, advancedSecurityLevelsRequired,
                        nbFinger, templateType, templateFVPType, maxSizeTemplate, enrollType,
                        latentDetection, coderChoice, detectModeChoice, CompressionAlgorithm.MORPHO_NO_COMPRESS,
                        0, templateList, BiometricUtils.callbackCmd, TXNCashTransfer.this);
            }

            final StringBuilder file_name = new StringBuilder("TemplateFP_");
            try {
                if (ret == ErrorCodes.MORPHO_OK) {
                    for (int i = 0; i < templateList.getNbTemplate(); i++) {
                        file_name.append(templateType.getExtension());
                        Template t = templateList.getTemplate(i);
                        imageData = t.getData();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            fingerprintHandler.post(new Runnable() {
                @Override
                public synchronized void run() {
                    if (imageData != null)
                        callUploadTemplateForVerificationApi(imageData, file_name.toString());
                }
            });
        }
    }

    private void callUploadTemplateForVerificationApi(byte[] templateData, String fileName) {
        try {
            MultipartRequest request = new MultipartRequest(Constants.getBaseUrl() + "/verifyBiometrics",
                    null,
                    new com.android.volley.Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            try {
                                JSONObject jsonObject = new JSONObject(
                                        new String(response.data, HttpHeaderParser.parseCharset(response.headers)));
                                if ("00".equals(jsonObject.optString("responseCode"))) {
                                    showConfirmationDialog();
                                } else if ("25".equals(jsonObject.optString("responseCode"))) {
                                    showAlertDialog("Fingerprint verification failed");
                                } else {
                                    showAlertDialog(jsonObject.optString("responseTxt"));
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new com.android.volley.Response.ErrorListener() {
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
            request.addPart(new MultipartRequest.FormPart("customerNo", cust_no));
            request.addPart(new MultipartRequest.FilePart("template", "*/*", fileName, templateData));
            request.setTag(TAG);
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(request);
            alertDialog = DialogUtils.showProgressDialog(getActivity(), "Verifying biometric.", false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            imageData = null;
        }
    }

    private void showConfirmationDialog() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.dialogTheme);
            builder.setMessage(getActivity().getString(R.string.cust_verified))
                    .setPositiveButton("PROCEED",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface arg0, int arg1) {
                                    arg0.dismiss();
                                    callFundsTransferApi();
                                }
                            }).setNegativeButton("CLOSE",
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

    @Override
    public void onResume() {
        try {
            if (morphoDevice != null) {
                morphoDevice.resumeConnection(30, this);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onResume();
    }

    @Override
    public void onDestroy() {
        closeAllDialogs();
        try {
            if (morphoDevice != null) {
                int i = morphoDevice.closeDevice();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            threadPool.shutdown();
            threadPool.awaitTermination(1, TimeUnit.SECONDS);
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

    private void closeAllDialogs() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
    }

    @Override
    public void update(Observable observable, Object arg) {
        try {
            // convert the object to a callback back message.
            if (arg instanceof CallbackMessage) {
                CallbackMessage message = (CallbackMessage) arg;
                int type = message.getMessageType();
                switch (type) {
                    case 1:
                        // message is a command.
                        Integer command = (Integer) message.getMessage();
                        // Analyze the command.
                        switch (command) {
                            case 0:
                                strMessage = "Place finger on sensor";
                                break;
                            case 1:
                                strMessage = "Move finger up";
                                break;
                            case 2:
                                strMessage = "Move finger down";
                                break;
                            case 3:
                                strMessage = "Move finger to left";
                                break;
                            case 4:
                                strMessage = "Move finger to right";
                                break;
                            case 5:
                                strMessage = "Press-harder";
                                break;
                            case 6:
                                strMessage = "Move latent";
                                break;
                            case 7:
                                strMessage = "Remove finger";
                                break;
                            case 8:
                                strMessage = "Fingerprint Captured";
                                break;
                        }

                        fingerprintHandler.post(new Runnable() {
                            @Override
                            public synchronized void run() {
                                status_label.setText(strMessage);
                            }
                        });

                        break;
                    case 2:
                        // message is a low resolution image, display it.
                        byte[] image = (byte[]) message.getMessage();
                        MorphoImage morphoImage = MorphoImage.getMorphoImageFromLive(image);
                        int imageRowNumber = morphoImage.getMorphoImageHeader().getNbRow();
                        int imageColumnNumber = morphoImage.getMorphoImageHeader().getNbColumn();
                        final Bitmap imageBmp = Bitmap.createBitmap(imageColumnNumber, imageRowNumber, Bitmap.Config.ALPHA_8);
                        imageBmp.copyPixelsFromBuffer(ByteBuffer.wrap(morphoImage.getImage(), 0,
                                morphoImage.getImage().length));
                        fingerprintHandler.post(new Runnable() {
                            @Override
                            public synchronized void run() {
                                updateImage(imageBmp);
                            }
                        });
                        break;
                    case 3:
                        // message is the coded image quality.
                        final Integer quality = (Integer) message.getMessage();
                        fingerprintHandler.post(new Runnable() {
                            @Override
                            public synchronized void run() {
                                updateImageBackground(quality);
                            }
                        });
                        break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void updateImageBackground(int level) {
        Drawable drawable;
        if (level <= 25) {
            drawable = getResources().getDrawable(R.drawable.red_border);
        } else if (level <= 75) {
            drawable = getResources().getDrawable(R.drawable.yellow_border);
        } else {
            drawable = getResources().getDrawable(R.drawable.green_border);
        }
        finger_img.setBackground(drawable);
    }

    private void updateImage(Bitmap bitmap) {
        try {
            finger_img.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*BUILTIN PRINTER SETUP OPTIONS*/

    private Runnable printerInitThread = new Runnable() {
        @Override
        public void run() {
            try {
                builtInPrinter = new Printer(requireActivity().getApplicationContext());
                isPrinterConfigured = builtInPrinter != null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    boolean isPrintingCustomerCopy = true;
    private void loadBankLogo() {
        try {
            InputStream is = requireActivity().getAssets().open("white_logo.png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            builtInPrinter.printImageBase(bitmap, 100, 100, Printer.Align.CENTER, 0);
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    public void addLineToBuffer() {
        try {
            builtInPrinter.printString(transaction.getReceiptTitle(), 25, Printer.Align.CENTER, true, false);
            builtInPrinter.printString(transaction.getTranType(), 25, Printer.Align.CENTER, false, false);
            builtInPrinter.printString(transaction.getTranStatus(), 22, Printer.Align.CENTER, false, false);
            builtInPrinter.printString(lineString, 30, Printer.Align.CENTER, false, false);
            builtInPrinter.print2StringInLine("Account", transaction.getAccount(),
                    1.0f, Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false, false,
                    false);
            builtInPrinter.print2StringInLine("Recipient", transaction.getContraAccount(),
                    1.0f, Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false, false,
                    false);
            builtInPrinter.print2StringInLine("Amount", "Ksh." +
                            transaction.getAmount(),
                    1.0f, Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false,
                    false, false);
            builtInPrinter.print2StringInLine("Agent", transaction.getAgent(), 1.0f,
                    Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false, false, false);
            builtInPrinter.print2StringInLine("Outlet", transaction.getOutlet(),
                    1.0f, Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false,
                    false, false);
            builtInPrinter.print2StringInLine("Date", transaction.getDate(),
                    1.0f, Printer.Font.MONOSPACE, 22, Printer.Align.LEFT, false,
                    false, false);
            builtInPrinter.print2StringInLine("RRN", transaction.getReference(), 1.0f, Printer.Font.MONOSPACE,
                    22, Printer.Align.LEFT, false, false, false);
            builtInPrinter.print2StringInLine("Customer", transaction.getCustomer(), 1.0f, Printer.Font.MONOSPACE,
                    22, Printer.Align.LEFT, false, false, false);
            builtInPrinter.print2StringInLine("Reason", transaction.getReason(), 1.0f, Printer.Font.MONOSPACE,
                    22, Printer.Align.LEFT, false, false, false);
            builtInPrinter.printString(lineString, 30, Printer.Align.CENTER, false, false);
            builtInPrinter.printString("Your Success, Our Success", 22, Printer.Align.CENTER, false,
                    true);
            builtInPrinter.printString("Thank you for banking with us!", 22, Printer.Align.CENTER,
                    false, false);
            builtInPrinter.printString(lineString, 30, Printer.Align.CENTER, false, false);
            builtInPrinter.printString("", 30, Printer.Align.CENTER, false, false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private boolean isPrinterConfigured = false;
    private Printer builtInPrinter;
    private static final String lineString = "------------------------------------------";


    /*THERMAL PRINTING SETUP OPTIONS*/

    private Transaction transaction;

    private void buildTransaction() {
        transaction = new Transaction();
        transaction.setAccount(requestObject.optString("accountNumber1")
                .replace(requestObject.optString("accountNumber1").substring(3, 9), "xxxxxx"));

        transaction.setContraAccount(requestObject.optString("accountNumber2")
                .replace(requestObject.optString("accountNumber2").substring(3, 9), "xxxxxx"));

        transaction.setAgent(cacheUtil.getString("AGENT_NO"));
        transaction.setAmount(NumberUtils.formatNumber(requestObject.optString("tranAmount")));

        transaction.setCurrency(crnry != null ? crnry : "Ksh.");
        transaction.setDate(cacheUtil.getString("processingDate") + " " +
                new SimpleDateFormat("HH:mm").format(new Date()));

        transaction.setOutlet(cacheUtil.getString("OUTLET_CD"));
        transaction.setReceiptTitle("MICROPAY (U) LTD");

        transaction.setReference(retrievalReference);
        transaction.setTranStatus("Approved");

        transaction.setTranType("CASH TRANSFER");
        transaction.setReason(tran_reason.getText().toString());

        String[] split = cust_nm.split(" ");
        if (split != null) {
            if (split.length > 2)
                transaction.setCustomer(split[0] + " " + split[1]);
            else
                transaction.setCustomer(cust_nm);
        } else
            transaction.setCustomer(cust_nm);

    }

     private void printAndWait(List<Receipt> trans_string, boolean exit) {
        PrinterUtils printerUtils = new PrinterUtils(requireActivity(), trans_string, cacheUtil);
        AsyncTask<String, Void, Integer> execute = printerUtils.execute();
        try {
            execute.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        if (exit)
            getActivity().finish();
    }

    private void refreshAccountList() {
        try {
            if (response != null) {
                updateChargeAccountList(response);
                return;
            }
            if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {

                JSONObject requestObject = new JSONObject()
                        .put("outletCredentials", outletCredentials)
                        .put("accountNo", source_acct.getText().toString())
                        .put("activity", TAG);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/getCustomerChargeAccounts", requestObject,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        TXNCashTransfer.response = response;
                                        updateChargeAccountList(response);
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
                            alertDialog.cancel();
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Updating accounts......", false);
            } else {
                showAlertDialog(getActivity().getResources().getString(R.string.network_error));
            }
        } catch (Exception e) {

        }
    }

    private void updateChargeAccountList(JSONObject response) {
        JSONArray account_list = response.optJSONArray("account_list");
        if (account_list == null || account_list.length() <= 0) {
            showAlertDialog("Unable to fetch customer fee account list");
            return;
        }
        try {
            ArrayList<String> list = new ArrayList<String>();
            for (int i = 0; i < account_list.length(); i++) {
                JSONObject jsonObject = account_list.optJSONObject(i);
                list.add(jsonObject.optString("ACCT_NO"));
            }
            list.add(0, "Select Account");
            ArrayAdapter<String> chargeAccountAdaptor = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_dropdown_item, list);
            charge_acct.setAdapter(chargeAccountAdaptor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static JSONObject response;
    private Spinner charge_acct;

    private void setupBaseObject() {
        try {
            outletCredentials = NetworkUtil.getBaseRequest(getActivity())
                    .put("agentAcctNo", cacheUtil.getString("OUTLET_FLOAT_ACCT"))
                    .put("agentType", cacheUtil.getString("agentType"))
                    .put("agentNo", cacheUtil.getString("AGENT_NO"))
                    .put("outletCode", cacheUtil.getString("OUTLET_CD"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
