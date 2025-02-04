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
import com.micropay.fragments.transactions.TXNAzamAgent;
import com.micropay.fragments.transactions.TXNDSTVPaymentAgent;
import com.micropay.fragments.transactions.TxnGoTvPaymentAgent;
import com.micropay.fragments.transactions.TxnNwscAgent;
import com.micropay.fragments.transactions.TXNRokeTelcomAgent;
import com.micropay.fragments.transactions.TXNStarTimesAgent;
import com.micropay.fragments.transactions.TXNTugendeAgent;
import com.micropay.fragments.transactions.TXNURAAgent;
import com.micropay.fragments.transactions.TxnUmemeAgent;
import com.micropay.fragments.transactions.TXNWENRECoAgent;
import com.micropay.fragments.transactions.TXNZukuAgent;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BillsMenuAgent extends Fragment implements ItemClickListener {

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
        loanActivity.setTitle("Bill Payments");

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
            jsonArray.put(0, new JSONObject().put("label", "DStv").put("sub_title", "Send Money")
                    .put("icon", R.drawable.dstv));
            jsonArray.put(1, new JSONObject().put("label", "GOtv").put("sub_title", "Send Money")
                    .put("icon", R.drawable.gotv));
            jsonArray.put(2, new JSONObject().put("label", "StarTimes").put("sub_title", "Send Money")
                    .put("icon", R.drawable.startimes));
            jsonArray.put(3, new JSONObject().put("label", "AZAM TV").put("sub_title", "Send Money")
                    .put("icon", R.drawable.azam));
            jsonArray.put(4, new JSONObject().put("label", "ZUKU TV").put("sub_title", "Send Money")
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
            jsonArray.put(10, new JSONObject().put("label", "Roke Data").put("sub_title", "Send Money")
                    .put("icon", R.drawable.roke));

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public void onClick(View view, int position) {
        switch (position) {
            case 0:
                cacheUtil.putString(Constants.MENU, "DSTV");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNDSTVPaymentAgent()).addToBackStack(null)
                        .commit();
                break;
            case 1:
                cacheUtil.putString(Constants.MENU, "GOTV");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TxnGoTvPaymentAgent()).addToBackStack(null)
                        .commit();
                break;
            case 2:

                cacheUtil.putString(Constants.MENU, "StarTimes");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNStarTimesAgent()).addToBackStack(null)
                        .commit();
                break;
            case 3:

                cacheUtil.putString(Constants.MENU, "AZAMTV");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNAzamAgent()).addToBackStack(null)
                        .commit();
                break;
            case 4:

                cacheUtil.putString(Constants.MENU, "ZUKUTV");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNZukuAgent()).addToBackStack(null)
                        .commit();
                break;
            case 5:

                cacheUtil.putString(Constants.MENU, "Agent_NWSC");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TxnNwscAgent()).addToBackStack(null)
                        .commit();
                break;
            case 6:

                cacheUtil.putString(Constants.MENU, "Agent_Umeme_Yaka");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TxnUmemeAgent()).addToBackStack(null)
                        .commit();
                break;
            case 7:

                cacheUtil.putString(Constants.MENU, "WENRECoCustomer");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNWENRECoAgent()).addToBackStack(null)
                        .commit();
                break;
            case 8:

                cacheUtil.putString(Constants.MENU, "TugendeCustomer");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNTugendeAgent()).addToBackStack(null)
                        .commit();
                break;
            case 9:

                cacheUtil.putString(Constants.MENU, "URA");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNURAAgent()).addToBackStack(null)
                        .commit();
                break;
            case 10:

                cacheUtil.putString(Constants.MENU, "Roke_Data");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new TXNRokeTelcomAgent()).addToBackStack(null)
                        .commit();
                break;


            default:
                //Toast.makeText(getActivity(), "This service is coming soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
