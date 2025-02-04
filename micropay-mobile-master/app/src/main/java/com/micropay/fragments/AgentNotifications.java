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

import com.micropay.adaptor.NotificationAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by developer on 9/19/18.
 */

public class AgentNotifications extends Fragment {

    private RecyclerView recyclerView;
    private NotificationAdaptor adapter;
    private JSONArray notificationList = new JSONArray();
    private SwipeRefreshLayout swipeContainer;
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fcontent_list, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        FragmentHandler fragmentHandler = (FragmentHandler) getActivity();
        fragmentHandler.setTitle("Notifications");

        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
        recyclerView = rootView.findViewById(R.id.recyclerView);

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loanCustomerNotification(null);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        loanCustomerNotification(null);
        setRetainInstance(true);
        return rootView;
    }

    private JSONArray getNotificationList() {
        JSONArray jsonArray = null;
        try {
            jsonArray = new JSONArray(cacheUtil.getString("notifications"));
        } catch (JSONException e) {
            e.printStackTrace();
            jsonArray = new JSONArray();
        }
        return jsonArray;
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void loanCustomerNotification(JSONObject jsonObject) {
        try {
            notificationList = getNotificationList();
            if (notificationList.length() <= 0) {
                showBasicAlertDialog("No new notifications");
            } else {
                JSONArray jsonArray = new JSONArray(cacheUtil.getString("notifications"));
                for (int i = 0; i < jsonArray.length(); i++) {
                    try {
                        notificationList.put(0, jsonArray.optJSONObject(i));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
            adapter = new NotificationAdaptor(notificationList, getActivity());
            recyclerView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            swipeContainer.setRefreshing(false);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void showBasicAlertDialog(String body) {
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
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }
}
