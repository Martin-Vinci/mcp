package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.DialogInterface;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.adaptor.DepositAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class AccountDisplay extends Fragment {

    private final String TAG = AccountDisplay.class.getSimpleName();
    private RecyclerView recyclerView;

    private DepositAdaptor adapter;
    private SwipeRefreshLayout swipeContainer;

    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private String custNo, specifiedKey;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fcontent_list, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Deposit Accounts");

        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
        recyclerView = rootView.findViewById(R.id.recyclerView);

        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        Bundle arguments = getArguments();
        if (arguments != null) {
            if (arguments.containsKey("OUTLET_FUND") && arguments.getBoolean("OUTLET_FUND")) {
                loanActivity.setTitle("Agent Accounts");
            }
            custNo = arguments.getString("CUST_NO");
            swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    callCustomerAccountsApi(custNo);
                }
            });
            swipeContainer.setColorSchemeResources(android.R.color.holo_blue_light,
                    android.R.color.holo_green_light,
                    android.R.color.holo_orange_light,
                    android.R.color.holo_red_light);
            callCustomerAccountsApi(custNo);
            specifiedKey = arguments.getString(Constants.KEY);
        }
        setRetainInstance(true);
        return rootView;
    }

    private void callCustomerAccountsApi(String cust_no) {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JSONObject customerRequest = new JSONObject()
                        .put("outletCredentials", NetworkUtil.getBaseRequest(getActivity()))
                        .put("customerNo", cust_no)
                        .put("activity", TAG);
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/searchDepositAccounts", customerRequest,
                        new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                swipeContainer.setRefreshing(false);
                                if (response != null) {
                                    if ("00".equalsIgnoreCase(response.optString("responseCode"))) {
                                        JSONArray search_results = response.optJSONArray("search_results");
                                        if (search_results != null) {
                                            //render results on screen
                                            renderResultsToScreen(search_results);
                                        } else {
                                            showBasicAlertDialog(null, "Search did not yield any results");
                                        }
                                    } else if ("21".equalsIgnoreCase(response.optString("responseCode"))) {
                                        showNoCustomerAccountsDialog();
                                    } else {
                                        showBasicAlertDialog("", response.optString("responseTxt"));
                                    }
                                } else
                                    showBasicAlertDialog(null, getActivity().getString(R.string.resp_timeout));
                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        swipeContainer.setRefreshing(false);
                        showBasicAlertDialog("Network Error", NetworkUtil.getErrorDesc(error));
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
                swipeContainer.setRefreshing(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            showBasicAlertDialog("Connection Unavailable", getActivity().getResources().getString(R.string.network_error));
        }
    }

    private void renderResultsToScreen(JSONArray search_results) {
        adapter = new DepositAdaptor(search_results, getActivity(), specifiedKey);
        recyclerView.setAdapter(adapter);
        swipeContainer.setRefreshing(false);
    }

    public void showBasicAlertDialog(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        if (title != null)
            builder.setTitle(title);
        builder.setMessage(body)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                goBackToPreviousFragment();
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (alertDialog != null && !alertDialog.isShowing())
            alertDialog.show();
    }

    public void showNoCustomerAccountsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        builder.setMessage("The selected customer does not have any active deposit accounts")
                .setNegativeButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                goBackToPreviousFragment();
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (alertDialog != null && !alertDialog.isShowing())
            alertDialog.show();
    }

    private void goBackToPreviousFragment() {
        FragmentManager supportFragmentManager = getActivity().getSupportFragmentManager();
        if (supportFragmentManager != null && supportFragmentManager.getBackStackEntryCount() > 0) {
            supportFragmentManager.popBackStackImmediate();
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
