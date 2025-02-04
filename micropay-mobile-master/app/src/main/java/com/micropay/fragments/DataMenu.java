package com.micropay.fragments;

/**
 * Created by micropay on 01/25/2021.
 */

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.micropay.adaptor.MenuAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.DisplayUtils;
import com.micropay.api.GridSpaceDeco;
import com.micropay.api.ItemClickListener;
import com.micropay.fragments.transactions.TXNAirtelDataPurchaseAgent;
import com.micropay.fragments.transactions.TxnLycaMobileData;
import com.micropay.fragments.transactions.TxnLycaMobileDataAgent;
import com.micropay.fragments.transactions.TXNMTNData;
import com.micropay.fragments.transactions.TXNAirtelDataPurchase;
import com.micropay.fragments.transactions.TXNMTNDataAgent;
import com.micropay.fragments.transactions.TXNRokeTelcom;
import com.micropay.fragments.transactions.TXNRokeTelcomAgent;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DataMenu extends Fragment implements ItemClickListener {

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
        loanActivity.setTitle("Data Purchase");

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

        MenuAdaptor adapter = new MenuAdaptor(getActivity(), getMenu());
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

    private JSONArray getMenu() {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(0, new JSONObject().put("label", "Airtel")
                    .put("icon", R.drawable.airtel));
//            jsonArray.put(1, new JSONObject().put("label", "Africel")
//                    .put("icon", R.drawable.account));
            jsonArray.put(1, new JSONObject().put("label", "MTN")
                    .put("icon", R.drawable.mtn));
            jsonArray.put(2, new JSONObject().put("label", "Lycamobile")
                    .put("icon", R.drawable.lycamobile));
            jsonArray.put(3, new JSONObject().put("label", "Roke Data").put("sub_title", "Send Money")
                    .put("icon", R.drawable.roke));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public void onClick(View view, int position) {
        String entityType = cacheUtil.getString("entityType");
        Fragment fragment;
        switch (position) {
            case 0:
                cacheUtil.putString(Constants.MENU, "Airtel Data");
                fragment = entityType.equalsIgnoreCase("CUSTOMER") ? new TXNAirtelDataPurchase() : new TXNAirtelDataPurchaseAgent();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, fragment).addToBackStack(null)
                        .commit();
                break;
            case 1:
                //Customer
                cacheUtil.putString(Constants.MENU, "MTN Data");
                fragment = entityType.equalsIgnoreCase("CUSTOMER") ? new TXNMTNData() : new TXNMTNDataAgent();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, fragment).addToBackStack(null)
                        .commit();
                  break;
            case 2:
                //Customer
                cacheUtil.putString(Constants.MENU, "Lyka Mobile Data");
                fragment = entityType.equalsIgnoreCase("CUSTOMER") ? new TxnLycaMobileData() : new TxnLycaMobileDataAgent();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, fragment).addToBackStack(null)
                        .commit();
                break;

            case 3:
                //Customer
                cacheUtil.putString(Constants.MENU, "Roke_Data");
                fragment = entityType.equalsIgnoreCase("CUSTOMER") ? new TXNRokeTelcom() : new TXNRokeTelcomAgent();
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, fragment).addToBackStack(null)
                        .commit();
                break;
            default:
                //Toast.makeText(getActivity(), "This service is coming soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
