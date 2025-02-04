package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.Manifest;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.textfield.TextInputEditText;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;

import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.popups.DialogUtils;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.utils.Dictionary;
import com.micropay.utils.DATA_CONVERTER;
import com.micropay.utils.SignatureView;
import com.micropay.utils.SpinnerUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import pl.aprilapps.easyphotopicker.EasyImage;

public class CreateCustomer extends Fragment implements PermissionListener {

    private final String TAG = CreateCustomer.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;

    private TextInputEditText first_nm, phone_no, last_nm, birth_date, national_id_no,
            nationalIdIssueDate, address_line_1, pinNo;
    private ImageView photo, signature;

    private Spinner cust_title, gender, idType;

    final Calendar myCalendar = Calendar.getInstance();
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private String userData;

    private JSONObject requestData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.f_customer_create, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Account Opening Form");

        birth_date = rootView.findViewById(R.id.birth_date);
        first_nm = rootView.findViewById(R.id.first_nm);
        last_nm = rootView.findViewById(R.id.last_nm);
        national_id_no = rootView.findViewById(R.id.id_no);
        phone_no = rootView.findViewById(R.id.phone_no);
        address_line_1 = rootView.findViewById(R.id.address_line_1);
        idType = rootView.findViewById(R.id.idType);
        cust_title = rootView.findViewById(R.id.cust_title);
        gender = rootView.findViewById(R.id.gender);
        nationalIdIssueDate = rootView.findViewById(R.id.issueDate);

        photo = rootView.findViewById(R.id.photo);
        signature = rootView.findViewById(R.id.signature);

        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.CAMERA}, CAMERA_CAPTURE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, SIGNATURE_CAPTURE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, SIGNATURE_CAPTURE);
        }
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_NETWORK_STATE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_NETWORK_STATE}, SIGNATURE_CAPTURE);
        }
        try {
            populateOptions();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        final DatePickerDialog.OnDateSetListener birthDatePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                birth_date.setText(dateFormat.format(myCalendar.getTime()));
            }
        };


        birth_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), birthDatePicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        final DatePickerDialog.OnDateSetListener idPicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                nationalIdIssueDate.setText(dateFormat.format(myCalendar.getTime()));
            }
        };

        nationalIdIssueDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), idPicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPhoto = true;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                selectPhoto();
            }
        });

        signature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isPhoto = false;
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                selectSignature();
            }
        });

        rootView.findViewById(R.id.submit_customer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                processEntries();
            }
        });
        setRetainInstance(true);
        return rootView;
    }

    private void populateOptions() throws JSONException {
        new SpinnerUtil().populateSpinner(Dictionary.findTitles(), cust_title, getActivity(),
                android.R.layout.simple_spinner_dropdown_item, "key", "value");
        new SpinnerUtil().populateSpinner(Dictionary.findGenders(), gender, getActivity(),
                android.R.layout.simple_spinner_dropdown_item, "key", "value");
        new SpinnerUtil().populateSpinner(Dictionary.findIdTypes(), idType, getActivity(),
                android.R.layout.simple_spinner_dropdown_item, "key", "value");
    }

    public static final int PHOTO_CONSTANT = 1, GALLERY_CONSTANT = 2;
    final int SIGNATURE_CAPTURE = 1;
    final int CAMERA_CAPTURE = 0, CAMERA_FILE = 4;
    private boolean isPhoto = false, useSignaturePad;
    private SignatureView mSig;
    private LinearLayout mContent;
    static final int REQUEST_IMAGE_CAPTURE = 1, SELECT_FILE = 2;
    private boolean uploaded = false;

    private boolean isCameraAvailable() {
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            // You have camera in your device
            return true;
        }
        return false;
    }

    private void selectPhoto() {
        try {
            final CharSequence[] items = {"Capture Photo", "Choose from Gallery"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals("Capture Photo")) {
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, CAMERA_CAPTURE);
                    } else if (items[item].equals("Choose from Gallery")) {
                        Intent intent = new Intent(Intent.ACTION_PICK,
                                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Choose a Photo"), CAMERA_FILE);
                    } else if (items[item].equals("Cancel Operation")) {
                        dialog.dismiss();
                    }

                }
            });
            builder.show();
        } catch (Exception e) {

        }
    }

    private void selectSignature() {
        try {
            final CharSequence[] items = {"Capture Photo", "Choose from Gallery"};

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setItems(items, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int item) {
                    if (items[item].equals("Capture Photo")) {

                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, REQUEST_IMAGE_CAPTURE);
                    } else if (items[item].equals("Choose from Gallery")) {
                        Intent intent = new Intent(Intent.ACTION_PICK,
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        intent.setType("image/*");
                        startActivityForResult(Intent.createChooser(intent, "Choose a Photo"), SELECT_FILE);
                    } else if (items[item].equals("Cancel Operation")) {
                        dialog.dismiss();
                    }

                }
            });
            builder.show();
        } catch (Exception e) {

        }
    }

    private void validatePermissions() {
        Dexter.withActivity(getActivity())
                .withPermission(Manifest.permission.CAMERA)
                .withListener(this)
                .onSameThread()
                .check();
    }

    @Override
    public void onPermissionGranted(PermissionGrantedResponse response) {
        EasyImage.openCamera(CreateCustomer.this, PHOTO_CONSTANT);
    }

    @Override
    public void onPermissionDenied(PermissionDeniedResponse response) {
        showPermissionDeniedDialog();
    }

    @Override
    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
        token.continuePermissionRequest();
    }

    protected void showPermissionDeniedDialog() {
        if (getActivity() != null && !getActivity().isFinishing()) {
            alertDialog = new AlertDialog.Builder(getActivity(), R.style.dialogTheme).create();
            alertDialog.setCancelable(false);
            alertDialog.setTitle("Permission Denied");
            alertDialog.setMessage("Camera/gallery access required to retrieve and or capture photo/signature");
            alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "CANCEL",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            getActivity().getSupportFragmentManager().popBackStack();
                        }
                    });
            alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "GRANT",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            validatePermissions();
                        }
                    });
            alertDialog.show();
        }
    }

    private static Bitmap b_imgPhoto, b_imgSign;

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (requestCode == CAMERA_CAPTURE && resultCode == getActivity().RESULT_OK) {
                b_imgPhoto = (Bitmap) data.getExtras().get("data");
                b_imgPhoto = Bitmap.createScaledBitmap(b_imgPhoto, 180, 220, false);
                photo.setImageBitmap(b_imgPhoto);
                photo.setScaleType(ImageView.ScaleType.FIT_XY);
                //appDb.putString("b_imgPhoto", encodeBitmapToBase64(b_imgPhoto));
            } else if (requestCode == CAMERA_FILE && resultCode == getActivity().RESULT_OK) {
                final Uri imageUri = data.getData();
                final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                b_imgPhoto = BitmapFactory.decodeStream(imageStream);
                b_imgPhoto = Bitmap.createScaledBitmap(b_imgPhoto, 180, 220, false);
                photo.setImageBitmap(b_imgPhoto);
                photo.setScaleType(ImageView.ScaleType.FIT_XY);
                // appDb.putString("b_imgPhoto", encodeBitmapToBase64(b_imgPhoto));
            } else if (requestCode != CAMERA_CAPTURE && requestCode != CAMERA_FILE) {
                //getActivity();
                if (resultCode == Activity.RESULT_OK) {
                    if (requestCode == REQUEST_IMAGE_CAPTURE) {
                        try {
                            b_imgSign = (Bitmap) data.getExtras().get("data");
                            b_imgSign = Bitmap.createScaledBitmap(b_imgSign, 180, 220, false);
                            signature.setImageBitmap(b_imgSign);
                            signature.setScaleType(ImageView.ScaleType.FIT_XY);
                            //appDb.putString("b_imgSign", encodeBitmapToBase64(b_imgSign));
                            uploaded = false;
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else if (requestCode == SELECT_FILE) {
                        final Uri imageUri = data.getData();
                        final InputStream imageStream = getActivity().getContentResolver().openInputStream(imageUri);
                        b_imgSign = BitmapFactory.decodeStream(imageStream);
                        b_imgSign = Bitmap.createScaledBitmap(b_imgSign, 180, 220, false);
                        signature.setImageBitmap(b_imgSign);
                        signature.setScaleType(ImageView.ScaleType.FIT_XY);
                        // appDb.putString("b_imgPhoto", encodeBitmapToBase64(b_imgPhoto));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        setRetainInstance(true);
    }

    public static String encodeBitmapToBase64(Bitmap image) {
        String signResponse = "";
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] b = baos.toByteArray();
            signResponse = Base64.encodeToString(b, Base64.DEFAULT);
        } catch (Exception ex) {

        }
        return signResponse;
    }


    private void processEntries() {
        requestData = new JSONObject();
        try {
            if (TextUtils.isEmpty(first_nm.getText())) {
                showAlertDialog("First name is required");
                first_nm.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(last_nm.getText())) {
                showAlertDialog("Last name is required");
                last_nm.requestFocus();
                return;
            }
            if (TextUtils.isEmpty(phone_no.getText())) {
                showAlertDialog("Mobile phone is required");
                phone_no.requestFocus();
                return;
            }
            if (DATA_CONVERTER.getInternationalFormat(phone_no.getText().toString()) == null) {
                showAlertDialog(getString(R.string.invalid_phone_no));
                phone_no.requestFocus();
                return;
            }

            if (SpinnerUtil.getSelectedValue(gender).equalsIgnoreCase("0")) {
                showAlertDialog("Gender is required");
                gender.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(birth_date.getText())) {
                showAlertDialog("Date of birth is required");
                birth_date.requestFocus();
                return;
            }

            if (SpinnerUtil.getSelectedValue(idType).equalsIgnoreCase("0")) {
                showAlertDialog("ID Type is required");
                idType.requestFocus();
                return;
            }

            if (TextUtils.isEmpty(national_id_no.getText())) {
                showAlertDialog("ID value is required");
                national_id_no.requestFocus();
                return;
            }

            if (b_imgPhoto == null) {
                showAlertDialog("Photo is not a valid image. Try capturing again");
                return;
            }
            if (b_imgSign == null) {
                showAlertDialog("Signature is not a valid image. Try capturing again");
                return;
            }

            requestData.put("titleId", SpinnerUtil.getSelectedKey(cust_title));
            requestData.put("firstName", first_nm.getText().toString());
            requestData.put("surName", last_nm.getText().toString());
            requestData.put("mobilePhone", DATA_CONVERTER.getInternationalFormat(phone_no.getText().toString()));
            requestData.put("gender", SpinnerUtil.getSelectedKey(gender));
            requestData.put("dateOfBirth", birth_date.getText().toString());
            requestData.put("idType", SpinnerUtil.getSelectedKey(idType));
            requestData.put("idNumber", national_id_no.getText().toString());
            requestData.put("idIssueDate", nationalIdIssueDate.getText().toString());
            requestData.put("addressLine1", address_line_1.getText().toString());
            requestData.put("customerPhoto", encodeBitmapToBase64(b_imgPhoto));
            requestData.put("customerSign", encodeBitmapToBase64(b_imgSign));
            //requestData.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            confirmAuthentication();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmAuthentication() {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = (TextView) layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setText("Confirm account opening");
                pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Confirm Outlet Transfer</font>"));
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
                                                requestData.put("authRequest", authRequest);
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            callCustomerCreateApi();
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

    private void callCustomerCreateApi() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/createCustomer", requestData,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                        displayCustomerDetails(response);
                                    } else {
                                        showAlertDialog("Response", response.optJSONObject("response").optString("responseMessage"));
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


    public void displayCustomerDetails(JSONObject response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        try {
            builder.setTitle(Html.fromHtml("<font color='#5a2069'><b>Success</b></font>"));
            builder.setMessage(Html.fromHtml(
                    "Customer No:       " + response.optString("customerCode")
                            + "<br>Account No:  " + response.optString("accountNo")));
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void showAlertDialogAndExit(String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
            builder.setMessage(body)
                    .setPositiveButton("OK",
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
                                }
                            }).setCancelable(false);
            alertDialog = builder.create();
            if (alertDialog != null && !alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
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
            if (alertDialog != null && !alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    @Override
    public void onDestroy() {
        if (alertDialog != null && alertDialog.isShowing())
            alertDialog.dismiss();
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
        super.onStop();
        NetworkUtil.getInstance(getActivity().getApplicationContext()).cancel(TAG);
    }

}
