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
import com.micropay.fragments.transactions.TXNAzamTVPayment;
import com.micropay.fragments.transactions.TXNDSTVPayment;
import com.micropay.fragments.transactions.TxnGoTvPayment;
import com.micropay.fragments.transactions.TxnNwscCustomer;
import com.micropay.fragments.transactions.TXNStarTimes;
import com.micropay.fragments.transactions.TXNTugende;
import com.micropay.fragments.transactions.TxnUmemeCustomer;
import com.micropay.fragments.transactions.TXNURA;
import com.micropay.fragments.transactions.TXNWENRECO;
import com.micropay.fragments.transactions.TXNZuku;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BillsMenuCustomer extends Fragment implements ItemClickListener {

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
        loanActivity.setTitle("Pay Services and Utilities");
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
            jsonArray.put(0, new JSONObject().put("label", "DStv").put("sub_title", "Send Money")
                    .put("icon", R.drawable.dstv));
            jsonArray.put(1, new JSONObject().put("label", "GOtv").put("sub_title", "Send Money")
                    .put("icon", R.drawable.gotv));
            jsonArray.put(2, new JSONObject().put("label", "StarTimes").put("sub_title", "Send Money")
                    .put("icon", R.drawable.startimes));
            jsonArray.put(3, new JSONObject().put("label", "AZAM TV").put("sub_title", "Send Money")
                    .put("icon", R.drawable.azam));
            jsonArray.put(4, new JSONObject().put("label", "Zuku TV").put("sub_title", "Send Money")
                    .put("icon", R.drawable.zuku));
            jsonArray.put(5, new JSONObject().put("label", "National Water").put("sub_title", "Send Money")
                    .put("icon", R.drawable.nwsc));
            jsonArray.put(6, new JSONObject().put("label", "Umeme").put("sub_title", "Send Money")
                    .put("icon", R.drawable.umeme_yaka));
            jsonArray.put(7, new JSONObject().put("label", "WENRECo").put("sub_title", "Send Money")
                    .put("icon", R.drawable.wenreco));
            jsonArray.put(8, new JSONObject().put("label", "Tugende").put("sub_title", "Send Money")
                    .put("icon", R.drawable.tugende));
            jsonArray.put(9, new JSONObject().put("label", "URA").put("sub_title", "Send Money")
                    .put("icon", R.drawable.ura));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public void onClick(View view, int position) {
        switch (position) {
            case 0:
                cacheUtil.putString(Constants.MENU, "Customer_DSTV");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNDSTVPayment()).addToBackStack(null)
                        .commit();
                break;
            case 1:
                cacheUtil.putString(Constants.MENU, "Customer_GOtv");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TxnGoTvPayment()).addToBackStack(null)
                        .commit();
                break;
            case 2:
                //Customer
                cacheUtil.putString(Constants.MENU, "StartTimesCustomer");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNStarTimes()).addToBackStack(null)
                        .commit();
                break;
            case 3:
                //Customer
                cacheUtil.putString(Constants.MENU, "AZAMCustomer");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNAzamTVPayment()).addToBackStack(null)
                        .commit();
                break;
            case 4:
                //Customer
                cacheUtil.putString(Constants.MENU, "ZUKUCustomer");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNZuku()).addToBackStack(null)
                        .commit();
                break;
            case 5:
                //Customer
                cacheUtil.putString(Constants.MENU, "NWSCCustomer");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TxnNwscCustomer()).addToBackStack(null)
                        .commit();
                break;
            case 6:
                //Customer
                cacheUtil.putString(Constants.MENU, "YakaCustomer");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TxnUmemeCustomer()).addToBackStack(null)
                        .commit();
                break;
            case 7:
                //Customer
                cacheUtil.putString(Constants.MENU, "WENRECoCustomer");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNWENRECO()).addToBackStack(null)
                        .commit();
                break;
            case 8:
                //Customer
                cacheUtil.putString(Constants.MENU, "TugendeCustomer");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNTugende()).addToBackStack(null)
                        .commit();
                break;
            case 9:
                //Customer
                cacheUtil.putString(Constants.MENU, "URA");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNURA()).addToBackStack(null)
                        .commit();
                break;
            default:
                //Toast.makeText(getActivity(), "This service is coming soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
