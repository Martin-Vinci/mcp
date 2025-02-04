package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.bumptech.glide.Glide;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.popups.DialogUtils;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CustomerInfo extends Fragment {

    private final String TAG = CustomerInfo.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;

    private TextView cust_nm, cust_no, registration_date,
            cust_gender, cust_address, cust_branch, cust_age, cust_status;

    private ImageView photo, signature;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fcust_details, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Customer Details");

        cust_nm = rootView.findViewById(R.id.cust_nm);
        cust_no = rootView.findViewById(R.id.cust_no);
        registration_date = rootView.findViewById(R.id.registration_date);
        cust_gender = rootView.findViewById(R.id.cust_gender);
        cust_address = rootView.findViewById(R.id.cust_address);
        cust_branch = rootView.findViewById(R.id.cust_branch);
        cust_age = rootView.findViewById(R.id.cust_age);
        cust_status = rootView.findViewById(R.id.cust_status);

        photo = rootView.findViewById(R.id.photo);
        signature = rootView.findViewById(R.id.signature);

        Bundle arguments = getArguments();
        if (arguments != null && arguments.getString("CUST_DETAILS") != null) {
            final JSONObject customer_details = getCustomerDetails(arguments.getString("CUST_DETAILS"));
            callCustomerImageApi(customer_details.optString("CUST_NO"));
            renderCustomerData(customer_details);
        }

        setRetainInstance(true);
        return rootView;
    }

    private void renderCustomerData(JSONObject customer_details) {

        cust_no.setText(customer_details.optString("CUST_NO"));
        cust_no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkUtil.copyToClip(getActivity(), "Customer Number Copied",
                        cust_no.getText().toString());
            }
        });

        cust_nm.setText(customer_details.optString("CUST_NM"));
        cust_nm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkUtil.copyToClip(getActivity(), "Customer Name Copied",
                        cust_nm.getText().toString());
            }
        });

        registration_date.setText(customer_details.optString("CREATE_DT"));
        cust_gender.setText(customer_details.optString("GENDER"));
        cust_address.setText(customer_details.optString("ADDR_LINE_1"));
        cust_address.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkUtil.copyToClip(getActivity(), "Customer Address Copied",
                        cust_address.getText().toString());
            }
        });

        cust_branch.setText(customer_details.optString("BU_NM"));
        cust_branch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NetworkUtil.copyToClip(getActivity(), "Customer Branch Copied",
                        cust_branch.getText().toString());
            }
        });

        cust_age.setText(String.valueOf(customer_details.optLong("AGE")));
        cust_status.setText(customer_details.optString("REC_ST"));
    }

    private JSONObject getCustomerDetails(String cust_details) {
        try {
            return new JSONObject(cust_details);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONObject();
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

    private void callCustomerImageApi(final String cust_no) {
        try {
            if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/getPhotoAndSignature", new JSONObject()
                        .put("outletCredentials", NetworkUtil.getBaseRequest(getActivity()))
                        .put("customerNo", cust_no),
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                if (alertDialog != null && alertDialog.isShowing())
                                    alertDialog.dismiss();
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        JSONArray search_results = response.optJSONArray("search_results");
                                        if (search_results != null && search_results.length() > 0)
                                            processImages(search_results);
                                        else
                                            showAlertDialog("Customer does not have any images");
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
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Basic " + Constants.getRawBasicData());
                        headers.put("sessionId", cacheUtil.getString("sessionId"));
                        return headers;
                    }
                };
                jsonObjectRequest.setTag(TAG);
                NetworkUtil.getInstance(getActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Retrieving Images...", false);
            } else {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.network_error), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processImages(JSONArray search_results) {
        for (int i = 0; i < search_results.length(); i++) {
            JSONObject jsonObject = search_results.optJSONObject(i);
            if (jsonObject.has("PHO") && jsonObject.optString("PHO") != null) {
                setImage(Base64.decode(jsonObject.optString("PHO"), Base64.DEFAULT), photo);
            } else if (jsonObject.has("SIG") && jsonObject.optString("SIG") != null) {
                setImage(Base64.decode(jsonObject.optString("SIG"), Base64.DEFAULT), signature);
            }
        }
    }

    private void setImage(byte[] data, ImageView imageView) {
        Glide.with(getActivity()).load(data).crossFade()
                .fitCenter().into(imageView);
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
