package com.micropay.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
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
import com.micropay.adaptor.ItemIdComparator;
import com.micropay.adaptor.MessageAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.NetworkUtil;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by developer on 9/19/18.
 */

public class AgentMessages extends Fragment {

    private final String TAG = AgentMessages.class.getSimpleName();
    private RecyclerView recyclerView;
    private MessageAdaptor adapter;
    private SwipeRefreshLayout swipeContainer;
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fcontent_list, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Inbox");

        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                callAgentTxnListingApi();
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        callAgentTxnListingApi();
        setRetainInstance(true);

        return rootView;
    }

    private void callAgentTxnListingApi() {
        if (NetworkUtil.isNetworkAvailable(getActivity().getApplicationContext())) {
            try {
                JSONObject messageRequest = NetworkUtil.getBaseRequest(getActivity())
                        .put("phoneNo", cacheUtil.getString("registeredPhone"))
                        .put("activity", TAG);

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBaseUrl() + "/findMessages", messageRequest,
                        response -> {
                            swipeContainer.setRefreshing(false);
                            if (response != null) {
                                if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                    JSONArray tran_list = response.optJSONArray("data");
                                    if (tran_list != null) {
                                        //render results on screen
                                        renderResultsToScreen(tran_list);
                                    } else {
                                        showAlertDialog("Search did not yield any results", true);
                                    }
                                } else {
                                    showAlertDialog(response.optJSONObject("response").optString("responseMessage"), true);
                                }
                            } else
                                showAlertDialog(getActivity().getString(R.string.resp_timeout), true);
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        swipeContainer.setRefreshing(false);
                        showAlertDialog(NetworkUtil.getErrorDesc(error), true);
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
            showAlertDialog(getString(R.string.unable_to_connect), true);
        }
    }

    private void renderResultsToScreen(JSONArray tran_list) {
        if (tran_list != null) {
            List<JSONObject> jsonObjectList = new ArrayList<>();
            for (int i = 0; i < tran_list.length(); i++) {
                try {
                    jsonObjectList.add(tran_list.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Collections.sort(jsonObjectList, new ItemIdComparator());
            adapter = new MessageAdaptor(jsonObjectList, getActivity());
            adapter.notifyDataSetChanged();
            recyclerView.setAdapter(adapter);
            swipeContainer.setRefreshing(false);
        } else
            showAlertDialog("", true);
    }

    private void showAlertDialog(String body, final boolean exit) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.dialogTheme);
        builder.setMessage(body)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                if (exit)
                                    getActivity().finish();
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

}
