package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.material.textfield.TextInputEditText;
import com.micropay.adaptor.StatementAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.popups.DialogUtils;
import com.micropay.utils.SpinnerUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class IssuedReceiptSearch extends Fragment {

    private final String TAG = IssuedReceiptSearch.class.getSimpleName();
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;

    private TextInputEditText trans_id;
    private TextInputEditText from_date, to_date;
    private JSONObject requestObject;

    private RecyclerView recyclerView;
    private StatementAdaptor adapter;
    private SwipeRefreshLayout swipeContainer;

    private TextInputEditText pinNo;

    final Calendar myCalendar = Calendar.getInstance();
    final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.f_receipts_request, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Search Receipts");

        LayoutInflater li = LayoutInflater.from(requireActivity());
        View layoutView = li.inflate(R.layout.confirm_transaction_dialog, null);
        pinNo = (TextInputEditText) layoutView.findViewById(R.id.confirmPin);

        trans_id = rootView.findViewById(R.id.trans_id);
        from_date = rootView.findViewById(R.id.from_date);
        to_date = rootView.findViewById(R.id.to_date);

        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
        recyclerView = rootView.findViewById(R.id.recyclerView);

        final DatePickerDialog.OnDateSetListener fromDatePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                from_date.setText(dateFormat.format(myCalendar.getTime()));
            }
        };

        from_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), fromDatePicker, myCalendar
                        .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                        myCalendar.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });

        final DatePickerDialog.OnDateSetListener toDatePicker = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {
                myCalendar.set(Calendar.YEAR, year);
                myCalendar.set(Calendar.MONTH, monthOfYear);
                myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                to_date.setText(dateFormat.format(myCalendar.getTime()));
            }
        };

        to_date.setOnClickListener(view -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), toDatePicker, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                    myCalendar.get(Calendar.DAY_OF_MONTH));
            datePickerDialog.show();
        });

        rootView.findViewById(R.id.request).setOnClickListener(view -> processUserEntries());

        setRetainInstance(true);
        return rootView;
    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void processUserEntries() {
        try {
            requestObject = new JSONObject();
            requestObject.put("authRequest", NetworkUtil.getBaseRequest(getActivity()));
            requestObject
                    .put("txnId", trans_id.getText().toString())
                    .put("fromDate", from_date.getText().toString())
                    .put("toDate", to_date.getText().toString())
                    .put("activity", TAG);
            findAccountStatementAPI();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void findAccountStatementAPI() {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JSONObject messageRequest = requestObject
                        .put("activity", TAG);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/findIssuedReceipt", messageRequest,
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                    Fragment fragment = null;
                                    JSONArray tran_list = response.optJSONArray("data");
                                    fragment = new IssuedTransReceiptView(tran_list, getActivity());
                                    getActivity().getSupportFragmentManager().beginTransaction()
                                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                            .replace(R.id.container, fragment).addToBackStack(null)
                                            .commit();

                                } else {
                                    showAlertDialog(null, response.optJSONObject("response").optString("responseMessage"));
                                }
                            } else
                                showAlertDialog(null, getActivity().getString(R.string.resp_timeout));
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        if (alertDialog != null && alertDialog.isShowing())
                            alertDialog.dismiss();
                        showAlertDialog(null, NetworkUtil.getErrorDesc(error));
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
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showAlertDialog("Connection Unavailable", "Cannot connect to server. No network available");
        }
    }
    private void showAlertDialogAndExit(String responseTxt) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        builder.setMessage(responseTxt)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                getActivity().finish();
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (alertDialog != null && !alertDialog.isShowing())
            alertDialog.show();
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
        if (alertDialog != null && !alertDialog.isShowing())
            alertDialog.show();
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
