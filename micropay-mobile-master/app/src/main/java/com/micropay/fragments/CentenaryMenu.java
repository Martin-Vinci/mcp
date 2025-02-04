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
import com.micropay.fragments.transactions.centenary.TxnCashDepositAgent;
import com.micropay.fragments.transactions.centenary.TxnCashDepositCustomer;
import com.micropay.fragments.transactions.centenary.TxnCorporateCashWithdrawAgent;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CentenaryMenu extends Fragment implements ItemClickListener {

    private RecyclerView recyclerView;
    private SwipeRefreshLayout swipeContainer;
    private CacheUtil cacheUtil;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fcontent_list, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Centenary Bank");
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
            gridSpacingItemDecoration = new GridSpaceDeco(1, DisplayUtils.dpToPx(1, getActivity()), true);
        } else {
            mLayoutManager = new GridLayoutManager(getActivity(), 4);
            gridSpacingItemDecoration = new GridSpaceDeco(4, DisplayUtils.dpToPx(10, getActivity()), false);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(gridSpacingItemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.parseColor("#FFFFFF"));

        MenuAdaptor adapter = new MenuAdaptor(getActivity(), getMenu());
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
        swipeContainer.setOnRefreshListener(() -> swipeContainer.setRefreshing(false));
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_dark);
    }

    private JSONArray getMenu() {
        JSONArray jsonArray = new JSONArray();
        try {
            jsonArray.put(0, new JSONObject().put("label", "Cash Deposit").put("sub_title", "Cash Deposit")
                    .put("icon", R.drawable.cash_deposit));
//
//            jsonArray.put(1, new JSONObject().put("label", "School Pay").put("sub_title", "School Pay")
//                    .put("icon", R.drawable.schoolpay));

            if ("OUTLET".equals(cacheUtil.getString("entityType")))
                jsonArray.put(1, new JSONObject().put("label", "Cash Withdraw").put("sub_title", "Cash Withdraw")
                        .put("icon", R.drawable.withdraw));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public void onClick(View view, int position) {
        Fragment fragment;
        switch (position) {
            case 0:
                if ("CUSTOMER".equals(cacheUtil.getString("entityType")))
                    fragment = new TxnCashDepositCustomer();
                else
                    fragment = new TxnCashDepositAgent();

                cacheUtil.putString(Constants.MENU, "CentenaryCashDeposit");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, fragment).addToBackStack(null)
                        .commit();
                break;
//            case 1:
//                if ("CUSTOMER".equals(cacheUtil.getString("entityType")))
//                    fragment = new TxnCorporateSchoolPayCustomer();
//                else
//                    fragment = new TxnCorporateSchoolPayAgent();
//
//                cacheUtil.putString(Constants.MENU, "CentenarySchoolPay");
//                getActivity().getSupportFragmentManager().beginTransaction()
//                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
//                        .replace(R.id.container, fragment).addToBackStack(null)
//                        .commit();
//                break;
            case 1:
                if ("OUTLET".equals(cacheUtil.getString("entityType"))) {
                    cacheUtil.putString(Constants.MENU, "CentenaryCashWithdraw");
                    getActivity().getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, new TxnCorporateCashWithdrawAgent()).addToBackStack(null)
                            .commit();
                }
                break;
            default:
                //Toast.makeText(getActivity(), "This service is coming soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
