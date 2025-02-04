package com.micropay.fragments.transactions;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.micropay.adaptor.PaymentRequestAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.DisplayUtils;
import com.micropay.api.GridSpaceDeco;
import com.micropay.api.ItemClickListener;
import com.micropay.api.NetworkUtil;
import com.micropay.api.TransTypes;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.popups.DialogUtils;
import com.micropay.utils.DATA_CONVERTER;
import com.micropay.utils.NumberUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class ApprovePayments extends Fragment implements ItemClickListener {

    private RecyclerView recyclerView;
    private final String TAG = TXNCashDeposit.class.getSimpleName();
    private SwipeRefreshLayout swipeContainer;
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private TextInputEditText pinNo;
    private JSONArray menuList = new JSONArray();
    private String requesterPhone, customerName;
    private Integer requestId;
    private Double requestedAmount;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fcontent_list, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Approve Payments");

        findPendingPaymentRequests();

        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
        recyclerView = rootView.findViewById(R.id.recyclerView);

        setRetainInstance(true);

        return rootView;
    }


    private void setupViews() {
        GridSpaceDeco gridSpacingItemDecoration;
        GridLayoutManager mLayoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager = new GridLayoutManager(getActivity(), 1);
            gridSpacingItemDecoration = new GridSpaceDeco(1, DisplayUtils.dpToPx(1, getActivity()), true);
        } else {
            mLayoutManager = new GridLayoutManager(getActivity(), 4);
            gridSpacingItemDecoration = new GridSpaceDeco(4, DisplayUtils.dpToPx(1, getActivity()), true);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(gridSpacingItemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.parseColor("#FFFFFF"));

        PaymentRequestAdaptor adapter = new PaymentRequestAdaptor(getActivity(), menuList);
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
        swipeContainer.setOnRefreshListener(() -> swipeContainer.setRefreshing(false));
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_dark, android.R.color.holo_green_dark, android.R.color.holo_orange_dark, android.R.color.holo_red_dark);
    }


    private void getMenu(JSONArray productList) {
        menuList = new JSONArray();
        try {
            for (int i = 0; i < productList.length(); i++) {
                JSONObject json = productList.optJSONObject(i);
                menuList.put(i, json);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        setupViews();
    }

    @Override
    public void onClick(View view, int position) throws JSONException {
        JSONObject selectedData = menuList.getJSONObject(position);
        requesterPhone = selectedData.getString("requesterPhone");
        customerName = selectedData.getString("customerName");
        requestId = selectedData.optInt("requestId");
        requestedAmount = selectedData.optDouble("amount");
        showDialog();
    }

    private void showDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_custom, null);
        builder.setView(dialogView);

        // Set the dialog title and message
        String message = "Do you want to approve or decline this payment request?\n\n" +
                "Amount: " + requestedAmount + "\n" +
                "Phone Number: " + requesterPhone + "\n" +
                "Customer Name: " + customerName;
        builder.setTitle("Payment Request");
        builder.setMessage(message);

        builder.setPositiveButton("Approve", (dialog, which) -> {
            // Handle approve action
            callAccountResponseByPhoneNo();
        });

        builder.setNegativeButton("Decline", (dialog, which) -> {
            // Handle decline action
            declinePayment();
        });

        builder.setNeutralButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }


    private void findPendingPaymentRequests() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, Constants.getTransactionUrl() + "/findPendingRequest",
                        new JSONObject()
                                .put("authRequest", NetworkUtil.getBaseRequest(getActivity()))
                                .put("fromPhone", cacheUtil.getString("registeredPhone"))
                                .put("channelSource", "MOBILE"), response -> {
                    if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
                    if (response != null) {
                        if (response.length() > 0) {
                            if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                JSONArray productList = response.optJSONArray("data");
                                getMenu(productList);
                            } else {
                                showAlertDialog(response.optJSONObject("response").optString("responseMessage"));
                            }
                        } else showAlertDialog(getActivity().getString(R.string.resp_timeout));
                    } else showAlertDialog(getActivity().getString(R.string.resp_timeout));
                }, error -> {
                    if (alertDialog != null && alertDialog.isShowing()) alertDialog.dismiss();
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Loading Pending Payments", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlertDialog(String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.dialogTheme);
            builder.setMessage(body).setPositiveButton("OK", (arg0, arg1) -> arg0.dismiss()).setCancelable(false);
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private void callAccountResponseByPhoneNo() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/accountResponseByPhoneNo", new JSONObject()
                        .put("phoneNo", requesterPhone),
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
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Validating customer details.", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void determineTransCharge(final JSONObject customer) {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JSONObject transChargeRequest = new JSONObject();
                transChargeRequest.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
                transChargeRequest.put("amount", requestedAmount)
                        .put("transCode", TransTypes.FUNDS_TRANSFER)
                        .put("activity", TAG);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/findTransactionCharge", transChargeRequest,
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                    JSONArray account_list = customer.optJSONArray("accountList");
                                    String acctNo = null;
                                    if (account_list != null)
                                        for (int i = 0; i < account_list.length(); i++) {
                                            JSONObject jsonObject = account_list.optJSONObject(i);
                                            acctNo = jsonObject.optString("acctNo");
                                            break;
                                        }
                                    displayRetrievedAccountInformationDetails(customer.optString("customerName"),
                                            acctNo,
                                            requestedAmount.toString(),
                                            response.optDouble("charge"));
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
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Basic " + Constants.getRawBasicData());
                        headers.put("sessionId", cacheUtil.getString("sessionId"));
                        return headers;
                    }
                };
                jsonObjectRequest.setTag(TAG);
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Validating Transaction...", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void displayRetrievedAccountInformationDetails(final String customerName, String acctNo,
                                                           final String txnAmount, final Double chargeAmount) {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setText("Confirm funds transfer of " + NumberUtils.toCurrencyFormat(NumberUtils.convertToBigDecimal(txnAmount)) + " "
                        + Html.fromHtml(" to customer " + acctNo + " " + customerName + "<br>")
                        + Html.fromHtml("Charge " + chargeAmount)
                );
                pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Confirm Funds transfer</font>"));
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
                                            JSONObject request = new JSONObject();
                                            request.put("authRequest", authRequest);
                                            request.put("fromPhone", requesterPhone);
                                            request.put("requestId", requestId);
                                            request.put("action", "Approved");
                                            callPaymentRequestAPI(request);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                        .setNegativeButton("CANCEL",
                                (dialog, id) -> dialog.cancel());
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


    private void declinePayment() {
        try {
            if (getActivity() != null && !getActivity().isFinishing()) {
                LayoutInflater li = LayoutInflater.from(requireActivity());
                View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
                TextView customerDetails = layoutView.findViewById(R.id.txtAccountDetails);
                customerDetails.setVisibility(View.GONE);
                pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);
                androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(
                        requireActivity());
                alertDialogBuilder.setView(layoutView);
                alertDialogBuilder.setTitle(Html.fromHtml("<font color='#3E2723'>Confirm Pin</font>"));
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
                                            JSONObject request = new JSONObject();
                                            request.put("authRequest", authRequest);
                                            request.put("fromPhone", requesterPhone);
                                            request.put("requestId", requestId);
                                            request.put("action", "Declined");
                                            callPaymentRequestAPI(request);
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                })
                        .setNegativeButton("CANCEL",
                                (dialog, id) -> dialog.cancel());
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


    private void callPaymentRequestAPI(JSONObject request) {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getTransactionUrl() + "/reviewPaymentRequest", request,
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

    public void displayTransactionSuccess(JSONObject response) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        try {
            builder.setTitle(Html.fromHtml("<font color='#5a2069'><b>Success</b></font>"));
            builder.setMessage(Html.fromHtml(
                    "You have transferred UGX " + NumberUtils.toCurrencyFormat(NumberUtils.convertToBigDecimal(requestedAmount.toString())) + " to " + DATA_CONVERTER.getInternationalFormat(requesterPhone.toString()) + "-" + customerName
                            + "<br>Trans ID: " + response.optString("transId")
                            + "<br>Charge: UGX" + NumberUtils.formatNumber(response.optString("chargeAmt"))
                            + "<br>Bal: UGX " + NumberUtils.formatNumber(response.optString("drAcctBal"))));
            builder.setPositiveButton("OK",
                    (arg0, arg1) -> {
                        arg0.dismiss();
                        findPendingPaymentRequests();
                    });
            AlertDialog diag = builder.create();
            diag.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
