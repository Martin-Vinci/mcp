package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.marcoscg.fingerauth.FingerAuth;
import com.morpho.android.usb.USBManager;
import com.morpho.morphosmart.sdk.CallbackMask;
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
import com.micropay.api.NetworkUtil;
import com.micropay.api.ServiceManager;
import com.micropay.popups.DialogUtils;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.LoginActivity;
import com.micropay.micropay.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import wangpos.sdk4.libbasebinder.Printer;

public class AppSecurity extends Fragment implements Observer {

    private final String TAG = AppSecurity.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private MorphoDevice morphoDevice;

    private int callbackCmd = CallbackMask.MORPHO_CALLBACK_IMAGE_CMD.getValue()
            | CallbackMask.MORPHO_CALLBACK_ENROLLMENT_CMD.getValue()
            | CallbackMask.MORPHO_CALLBACK_COMMAND_CMD.getValue()
            | CallbackMask.MORPHO_CALLBACK_CODEQUALITY.getValue()
            | CallbackMask.MORPHO_CALLBACK_DETECTQUALITY.getValue();

    private Handler mHandler = new Handler();
    private JSONObject agentDetails;

    private CheckBox printer_integrated, biometrics_integrated;
    Button mScan;
    BluetoothAdapter mBluetoothAdapter;

    private ExecutorService service = Executors.newFixedThreadPool(4);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fsec_settings, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Security Settings");

        agentDetails = getAgentDetails();

        rootView.findViewById(R.id.clear_device_info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPasswordConfirmationDialog(null);
            }
        });

        TextView change_password = rootView.findViewById(R.id.change_password);
        if ("STAFF".equalsIgnoreCase(cacheUtil.getString("agentType"))) {
            change_password.setVisibility(View.GONE);
        } else {
            change_password.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new PINChange()).commit();
                }
            });
        }

        CheckBox login_with_fp = rootView.findViewById(R.id.login_with_fp);
        if (FingerAuth.hasFingerprintSupport(getActivity())) {
            login_with_fp.setChecked(cacheUtil.getBoolean("fp_login_allowed", false));
            login_with_fp.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                    cacheUtil.putBoolean("fp_login_allowed", checked);
                }
            });
        } else {
            login_with_fp.setVisibility(View.GONE);
        }

        try {
            service.submit(printerInit);
        } catch (Exception e) {
            e.printStackTrace();
        }


        printer_integrated = rootView.findViewById(R.id.printer_integrated);
        printer_integrated.setChecked(cacheUtil.getBoolean("builtin_printer", false));
        printer_integrated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cacheUtil.putBoolean("builtin_printer", checked);
                if (checked)
                    testIntegratedPrinter();
            }
        });

        biometrics_integrated = rootView.findViewById(R.id.biometrics_integrated);
        biometrics_integrated.setChecked(cacheUtil.getBoolean("builtin_scanner", false));
        biometrics_integrated.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                cacheUtil.putBoolean("builtin_scanner", checked);
                if (checked)
                    setupAndTestBiometricScanner();
            }
        });

        TextView add_printer = rootView.findViewById(R.id.add_printer);
        if (printer_integrated.isChecked()) {
            add_printer.setVisibility(View.GONE);
        } else {
            add_printer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container, new DeviceList()).addToBackStack(null)
                            .commit();
                }
            });
        }
        setRetainInstance(true);
        return rootView;
    }

    private void setupAndTestBiometricScanner() {
        USBManager.getInstance().initialize(getActivity(), "com.agentbanking.micropay.USB_ACTION",
                true);
        if (USBManager.getInstance().isDevicesHasPermission()) {
            morphoDevice = new MorphoDevice();
            morphoDevice.rebootSoft(30, this);
            checkAttachedActiveUsbDevices();
        } else {
            cacheUtil.putBoolean("builtin_scanner", false);
            biometrics_integrated.setChecked(false);
            Toast.makeText(getActivity(), "Biometric Scanner Inaccessible", Toast.LENGTH_SHORT).show();
        }
    }

    protected void checkAttachedActiveUsbDevices() {
        Integer nbUsbDevice = new Integer(0);
        int usbDeviceResp = morphoDevice.initUsbDevicesNameEnum(nbUsbDevice);
        if (usbDeviceResp == ErrorCodes.MORPHO_OK) {
            if (nbUsbDevice > 0) {
                int i = morphoDevice.openUsbDevice(morphoDevice.getUsbDeviceName(0), 0);
                if (i == 0)
                    //try capturing device from here
                    showBiometricDialog();
            } else {
                cacheUtil.putBoolean("builtin_scanner", false);
                biometrics_integrated.setChecked(false);
                showAlertDialog("Biometric Scanner Alert",
                        "Unable to detect biometric device. Ensure device is activated and access permission granted");
            }
        } else {
            cacheUtil.putBoolean("builtin_scanner", false);
            biometrics_integrated.setChecked(false);
            showAlertDialog("Biometric Scanner Alert",
                    ErrorCodes.getError(usbDeviceResp, morphoDevice.getInternalError()));
        }
    }

    private ImageView finger_img;
    private TextView status_label;

    private void showBiometricDialog() {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(getActivity());
                View layoutView = li.inflate(R.layout.biometric_dialog, null);
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder =
                        new androidx.appcompat.app.AlertDialog.Builder(getActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle("Biometric Verification");
                finger_img = layoutView.findViewById(R.id.finger_img);
                status_label = layoutView.findViewById(R.id.status_label);
                alertDialogBuilder
                        .setCancelable(false)
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
                    try {
                        service.submit(commandJob);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private byte[] imageData = null;

    private Runnable commandJob = new Runnable() {
        @Override
        public void run() {
            final TemplateList templateList = new TemplateList();
            TemplateType templateType = TemplateType.MORPHO_PK_ISO_FMR;
            TemplateFVPType templateFVPType = TemplateFVPType.MORPHO_NO_PK_FVP;

            EnrollmentType enrollType = EnrollmentType.ONE_ACQUISITIONS;
            LatentDetection latentDetection = LatentDetection.LATENT_DETECT_ENABLE;

            int detectModeChoice = DetectionMode.MORPHO_ENROLL_DETECT_MODE.getValue();
            detectModeChoice |= DetectionMode.MORPHO_FORCE_FINGER_ON_TOP_DETECT_MODE.getValue();

            int ret = morphoDevice.setStrategyAcquisitionMode(StrategyAcquisitionMode.MORPHO_ACQ_EXPERT_MODE);
            if (ret == 0) {
                ret = morphoDevice.capture(0, 0, 0xFF,
                        1, templateType, templateFVPType, 255, enrollType,
                        latentDetection, Coder.MORPHO_DEFAULT_CODER, detectModeChoice, CompressionAlgorithm.MORPHO_NO_COMPRESS,
                        0, templateList, callbackCmd, AppSecurity.this);
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

            mHandler.post(new Runnable() {
                @Override
                public synchronized void run() {
                    if (imageData != null) {
                        cacheUtil.putBoolean("builtin_scanner", true);
                        biometrics_integrated.setChecked(true);
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.cancel();
                        showAlertDialog("Fingerprint scanner setup successfully");
                    } else {
                        cacheUtil.putBoolean("builtin_scanner", false);
                        biometrics_integrated.setChecked(false);
                        showAlertDialog("Fingerprint scanner setup failed");
                    }
                }
            });
        }
    };


    private void testIntegratedPrinter() {
        if (isPrinterConfigured) {
            progressDialog = ProgressDialog.show(getActivity(), "", "Checking Device...");
            service.submit(printerTestJob);
        } else {
            cacheUtil.putBoolean("builtin_printer", false);
            printer_integrated.setChecked(false);
            showAlertDialog("The integrated printer is not configured properly.");
        }
    }

    private Runnable printerInit = new Runnable() {
        @Override
        public void run() {
            try {
                mPrinter = new Printer(getActivity().getApplicationContext());
                mPrinter.setPrintFontType(getActivity(), "");
                isPrinterConfigured = true;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private void loadBankLogo() {
        try {
            InputStream is = getActivity().getAssets().open("white_logo.png");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            mPrinter.printImageBase(bitmap, 100, 100, Printer.Align.CENTER, 0);
            bitmap.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    public void addLineToBuffer() {
        try {
            mPrinter.printString("Micropay (U) Ltd", 25, Printer.Align.CENTER,
                    true, false);
            mPrinter.printString("Printer Testing", 25, Printer.Align.CENTER,
                    false, false);
            mPrinter.printString(lineString, 30, Printer.Align.CENTER, false, false);
            mPrinter.print2StringInLine("Date", cacheUtil.getString("processingDate"),
                    1.0f, Printer.Font.DEFAULT, 25, Printer.Align.LEFT,
                    false, false, false);
            mPrinter.printString(lineString, 30, Printer.Align.CENTER, false,
                    false);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private Runnable printerTestJob = new Runnable() {
        @Override
        public void run() {
            if (isPrinterConfigured) {
                try {
                    if (mPrinter != null) {
                        mPrinter.printInit();
                        mPrinter.clearPrintDataCache();
                        loadBankLogo();
                        addLineToBuffer();
                        mPrinter.printPaper_trade(2, 50);
                        mPrinter.printFinish();
                    } else {
                        Toast.makeText(getActivity(), "Printer not ready", Toast.LENGTH_SHORT).show();
                    }
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            } else {
                showAlertDialog("Printer Not Reachable", "The attached printer is not reachable");
            }
            notifyCompletion();
        }
    };

    private void notifyCompletion() {
        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private boolean isPrinterConfigured = false;
    private Printer mPrinter;
    private static final String lineString = "------------------------------------------";


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
                                }
                            }).setCancelable(false);
            alertDialog = builder.create();
            if (alertDialog != null && !alertDialog.isShowing())
                alertDialog.show();
        }
    }

    private void showPasswordConfirmationDialog(String errorMsg) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            LayoutInflater li = LayoutInflater.from(getActivity());
            View promptsView = li.inflate(R.layout.dpin_form, null);
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                    getActivity());
            alertDialogBuilder.setView(promptsView);
            alertDialogBuilder.setTitle("Authentication Required");
            ((TextInputLayout) promptsView.findViewById(R.id.pin_layout)).setError(errorMsg);
            final TextInputEditText pin_edit = (TextInputEditText) promptsView.findViewById(R.id.pin_edit);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("VERIFY",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    dialog.dismiss();
                                    if (TextUtils.isEmpty(pin_edit.getText()) || pin_edit.getText().toString().length() < 5) {
                                        showPasswordConfirmationDialog(getString(R.string.invalid_password));
                                    } else {
                                        try {
                                            JSONObject baseRequest = NetworkUtil.getBaseRequest(getActivity());
                                            baseRequest.put("outletCode", cacheUtil.getString("OUTLET_CD"));
                                            baseRequest.put("outletPaswd", ServiceManager.encouple(pin_edit.getText().toString()))
                                                    .put("activity", TAG)
                                                    .put("agentType", agentDetails.optString("AGENT_TY_CD"))
                                                    .put("agentNo", agentDetails.optString("AGENT_NO"));
                                            callUserAuthenticationApi(baseRequest);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
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
            if (alertDialog != null && !alertDialog.isShowing())
                alertDialog.show();
        }
    }

    private void callUserAuthenticationApi(final JSONObject baseRequest) {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/outletLogin", baseRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        //handle reset option here
                                        cacheUtil.clear();
                                        clearAndStartNewActivity(LoginActivity.class);
                                    } else {
                                        cacheUtil.putBoolean("hasUsedPIN", false);
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Authenticating.", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog("Cannot connect to server. No network available");
        }
    }

    private JSONObject getAgentDetails() {
        try {
            return new JSONObject(cacheUtil.getString("self_service"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
    }

    public void showAlertDialog(String body) {
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
            alertDialog = builder.create();
            if (alertDialog != null && !alertDialog.isShowing())
                alertDialog.show();
        }
    }

    private void clearAndStartNewActivity(Class<?> clazz) {
        Intent intent = new Intent(getActivity().getApplicationContext(), clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        getActivity().finish();
    }

    @Override
    public void onDestroy() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        try {
            service.shutdown();
            service.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
        super.onPause();
    }

    @Override
    public void onStop() {
        NetworkUtil.getInstance(getActivity().getApplicationContext()).cancel(TAG);
        super.onStop();
    }

    private String strMessage;

    @Override
    public void update(Observable observable, Object o) {
        try {
            // convert the object to a callback back message.
            CallbackMessage message = (CallbackMessage) o;
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
                            strMessage = "Configuration Successful";
                            break;
                    }

                    mHandler.post(new Runnable() {
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
                    mHandler.post(new Runnable() {
                        @Override
                        public synchronized void run() {
                            updateImage(imageBmp);
                        }
                    });
                    break;
                case 3:
                    // message is the coded image quality.
                    final Integer quality = (Integer) message.getMessage();
                    mHandler.post(new Runnable() {
                        @Override
                        public synchronized void run() {

                            updateImageBackground(quality);
                        }
                    });
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void updateImageBackground(int level) {
        Drawable drawable = null;
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
}
