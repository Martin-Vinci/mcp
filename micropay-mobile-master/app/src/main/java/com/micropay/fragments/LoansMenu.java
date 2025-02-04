package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.micropay.adaptor.MenuAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.DisplayUtils;
import com.micropay.api.GridSpaceDeco;
import com.micropay.api.ItemClickListener;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LoansMenu extends Fragment implements ItemClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private CacheUtil cacheUtil;
    private JSONArray tcInfo;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fcontent_list, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Loan Control");

        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
        recyclerView = rootView.findViewById(R.id.recyclerView);

        setupViews();

        setRetainInstance(true);
        return rootView;
    }


    private void setupViews() {

        GridSpaceDeco gridSpacingItemDecoration;
        GridLayoutManager mLayoutManager;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            mLayoutManager = new GridLayoutManager(getActivity(), 1);
            gridSpacingItemDecoration = new GridSpaceDeco(1,
                    DisplayUtils.dpToPx(1, getActivity()), true);
        } else {
            mLayoutManager = new GridLayoutManager(getActivity(), 4);
            gridSpacingItemDecoration = new GridSpaceDeco(4,
                    DisplayUtils.dpToPx(10, getActivity()), true);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(gridSpacingItemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.parseColor("#FFFFFF"));

        MenuAdaptor adapter = new MenuAdaptor(getActivity(), getLoanMenu());
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
        swipeContainer.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeContainer.setRefreshing(false);
            }
        });
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_dark);
    }

    private boolean enquiry, application, repayment;

    private JSONArray getLoanMenu() {
        JSONArray jsonArray = new JSONArray();
        try {
            tcInfo = getTcInfo();
            if (isTcAllowed(112) || isTcAllowed(512)) {
                //loan enquiry
                enquiry = true;
            }
            if (isTcAllowed(113) || isTcAllowed(513)) {
                //loan repayment
                repayment = true;
            }
            if (isTcAllowed(108) || isTcAllowed(508)) {
                //credit application
                application = true;
            }

            jsonArray.put(0, new JSONObject().put("label", "Enquiry")
                    .put("icon", R.drawable.qr_payment));

            jsonArray.put(1, new JSONObject().put("label", "Application")
                    .put("icon", R.drawable.qr_payment));

            jsonArray.put(2, new JSONObject().put("label", "Repayment")
                    .put("icon", R.drawable.qr_payment));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    private boolean isTcAllowed(long tcNo) {
        boolean isAllowed = false;
        for (int i = 0; i < tcInfo.length(); i++) {
            JSONObject jsonObject = tcInfo.optJSONObject(i);
            if (jsonObject.has("TC_NO") && jsonObject.optLong("TC_NO") == tcNo) {
                isAllowed = true;
                break;
            }
        }
        return isAllowed;
    }

    private JSONArray getTcInfo() {
        JSONArray jsonArray;
        try {
            jsonArray = new JSONArray(cacheUtil.getString("tc_info"));
        } catch (JSONException e) {
            jsonArray = new JSONArray();
        }
        return jsonArray;
    }

    @Override
    public void onClick(View view, int position) {
        switch (position) {
            case 0:
                //Credit
                if (enquiry) {
                    cacheUtil.putString(Constants.MENU, "Loan Enquiry");
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new CustomerSearch()).addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getActivity(), "You are not allowed to access this feature",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                //Customer
                if (application) {
                    cacheUtil.putString(Constants.MENU, "Credit Appln");
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new CustomerSearch()).addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getActivity(), "You are not allowed to access this feature",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 2:
                //Account
                if (repayment) {
                    cacheUtil.putString(Constants.MENU, "Group Repayment");
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new CustomerSearch()).addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getActivity(), "You are not allowed to access this feature",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                Toast.makeText(getActivity(), "This service is coming soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
