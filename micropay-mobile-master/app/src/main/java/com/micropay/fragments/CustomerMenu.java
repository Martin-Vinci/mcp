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

public class CustomerMenu extends Fragment implements ItemClickListener {

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
        loanActivity.setTitle("Customer Control");

        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
        recyclerView = rootView.findViewById(R.id.recyclerView);

        setupSubMenu();

        setRetainInstance(true);
        return rootView;
    }


    private void setupSubMenu() {
        try {
            GridSpaceDeco gridSpacingItemDecoration;
            GridLayoutManager mLayoutManager;
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                mLayoutManager = new GridLayoutManager(getActivity(), 1);
                gridSpacingItemDecoration = new GridSpaceDeco(1, DisplayUtils.dpToPx(1,
                        getActivity()), true);
            } else {
                mLayoutManager = new GridLayoutManager(getActivity(), 4);
                gridSpacingItemDecoration = new GridSpaceDeco(4, DisplayUtils.dpToPx(10,
                        getActivity()), true);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean enquiry, application;

    private JSONArray getLoanMenu() {
        JSONArray jsonArray = new JSONArray();
        try {
            tcInfo = getTcInfo();
            if (isTcAllowed(114) || isTcAllowed(514)) {
                //customer enquiry
                enquiry = true;
            }
            if (isTcAllowed(106) || isTcAllowed(506)) {
                //customer creation
                application = true;
            }
            jsonArray.put(1, new JSONObject().put("label", "Creation")
                    .put("icon", R.drawable.account));

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
                    cacheUtil.putString(Constants.MENU, "Customer Enquiry");
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new CustomerSearch()).addToBackStack(null)
                            .commit();
                } else {
                    Toast.makeText(getActivity(), getString(R.string.feature_denied),
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case 1:
                //Customer
                if (application) {
                    if (cacheUtil.getBoolean("builtin_scanner", false)) {
                        cacheUtil.putString(Constants.MENU, "Create Customer");
                        getActivity().getSupportFragmentManager().beginTransaction()
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .replace(R.id.container, new CreateCustomer()).addToBackStack(null)
                                .commit();
                    } else {
                        Toast.makeText(getActivity(), "This feature requires a biometric scanner",
                                Toast.LENGTH_SHORT).show();
                    }
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
