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

import com.micropay.adaptor.MenuAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.DisplayUtils;
import com.micropay.api.GridSpaceDeco;
import com.micropay.api.ItemClickListener;
import com.micropay.fragments.transactions.TXNAirtelPurchase;
import com.micropay.fragments.transactions.TXNAirtelPurchaseAgent;
import com.micropay.fragments.transactions.TxnLycaMobileAirtime;
import com.micropay.fragments.transactions.TxnLycaMobileAirtimeAgent;
import com.micropay.fragments.transactions.TXNMTNAirtime;
import com.micropay.fragments.transactions.TXNMTNAirtimeAgent;
import com.micropay.fragments.transactions.TXNUTLAirtime;
import com.micropay.fragments.transactions.TXNUTLAirtimeAgent;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AirtimeMenu extends Fragment implements ItemClickListener {

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
        loanActivity.setTitle("Airtime Purchase");

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
            jsonArray.put(3, new JSONObject().put("label", "UTL Airtime").put("sub_title", "Send Money")
                    .put("icon", R.drawable.utcl));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public void onClick(View view, int position) {
        String entityType = cacheUtil.getString("entityType");
        switch (position) {
            case 0:
                cacheUtil.putString(Constants.MENU, "Customer_AirtelAirtime");
                if (entityType.equalsIgnoreCase("CUSTOMER"))
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new TXNAirtelPurchase()).addToBackStack(null)
                            .commit();
                else {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new TXNAirtelPurchaseAgent()).addToBackStack(null)
                            .commit();
                }
                break;
            case 1:
                //Customer
                cacheUtil.putString(Constants.MENU, "Customer_MTNAirtime");
                if (entityType.equalsIgnoreCase("CUSTOMER"))
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new TXNMTNAirtime()).addToBackStack(null)
                            .commit();
                else {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new TXNMTNAirtimeAgent()).addToBackStack(null)
                            .commit();
                }
                break;
            case 2:
                cacheUtil.putString(Constants.MENU, "Customer_LycamobileAirtime");
                if (entityType.equalsIgnoreCase("CUSTOMER"))
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new TxnLycaMobileAirtime()).addToBackStack(null)
                            .commit();
                else {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new TxnLycaMobileAirtimeAgent()).addToBackStack(null)
                            .commit();
                }
                break;


            case 11:
                cacheUtil.putString(Constants.MENU, "UTL");
                if (entityType.equalsIgnoreCase("CUSTOMER"))
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new TXNUTLAirtime()).addToBackStack(null)
                            .commit();
                else {
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new TXNUTLAirtimeAgent()).addToBackStack(null)
                            .commit();
                }
                break;
            default:
                //Toast.makeText(getActivity(), "This service is coming soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
