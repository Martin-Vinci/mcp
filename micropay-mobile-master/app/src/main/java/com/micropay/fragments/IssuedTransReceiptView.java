package com.micropay.fragments;

import android.app.Activity;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.adaptor.ItemIdComparator;
import com.micropay.adaptor.ReceiptAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.ItemClickListener;
import com.micropay.api.NetworkUtil;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.models.Receipt;
import com.micropay.utils.PrinterUtils2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Created by developer on 9/19/18.
 */

public class IssuedTransReceiptView extends Fragment implements ItemClickListener {

    private final String TAG = IssuedTransReceiptView.class.getSimpleName();
    private RecyclerView recyclerView;
    private ReceiptAdaptor adapter;
    List<JSONObject> jsonObjectList;
    private SwipeRefreshLayout swipeContainer;
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;

    private JSONArray search_results;
    private Activity mCtx;
    public IssuedTransReceiptView(JSONArray list, Activity mCtx) {
        this.search_results = list;
        this.mCtx = mCtx;
        cacheUtil = new CacheUtil(mCtx);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fcontent_list, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Receipts View");

        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
        recyclerView = rootView.findViewById(R.id.recyclerView);
        LinearLayoutManager linearLayout = new LinearLayoutManager(getActivity());
        linearLayout.setReverseLayout(true);
        linearLayout.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(linearLayout);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        if (search_results != null) {
            //render results on screen
            renderResultsToScreen(search_results);
        } else {
            showAlertDialog("Search did not yield any results", true);
        }

        setRetainInstance(true);

        return rootView;
    }

    @Override
    public void onClick(View view, int position) {
        // Handle the click event for the clicked row
        JSONObject clickedData = jsonObjectList.get(position);

        Thread t = new Thread() {
            public void run() {
                try {

                    List<Receipt> RECEIPT = new ArrayList<>();
                    String printData = clickedData.optString("receiptData");
                    Receipt data = new Receipt("", printData);
                    RECEIPT.add(data);

                    PrinterUtils2 printerUtils = new PrinterUtils2(getContext(), RECEIPT, cacheUtil);
                    AsyncTask<String, Void, Integer> execute = printerUtils.execute();
                    try {
                        execute.get();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    Log.e("Main", "Exe ", e);
                }
            }
        };

        t.start();
    }

    private void renderResultsToScreen(JSONArray tran_list) {
        if (tran_list != null) {
            jsonObjectList = new ArrayList<>();
            for (int i = 0; i < tran_list.length(); i++) {
                try {
                    jsonObjectList.add(tran_list.getJSONObject(i));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Collections.sort(jsonObjectList, new ItemIdComparator());
            adapter = new ReceiptAdaptor(jsonObjectList, getActivity(), this);
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
