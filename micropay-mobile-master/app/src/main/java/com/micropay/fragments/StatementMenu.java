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
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StatementMenu extends Fragment implements ItemClickListener {

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
        loanActivity.setTitle("Statement Control");

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
            jsonArray.put(0, new JSONObject().put("label", "Mini Statement")
                    .put("icon", R.drawable.mini_statement));

            jsonArray.put(1, new JSONObject().put("label", "Full Statement")
                    .put("icon", R.drawable.statement));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonArray;
    }

    @Override
    public void onClick(View view, int position) {
        switch (position) {
            case 0:
                cacheUtil.putString(Constants.MENU, "MiniStatement");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new MiniStatement()).addToBackStack(null)
                        .commit();
                break;
            case 1:
                //Customer
                cacheUtil.putString(Constants.MENU, "FullStatement");
                getActivity().getSupportFragmentManager().beginTransaction()
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .replace(R.id.container, new FullStatement()).addToBackStack(null)
                        .commit();
                break;
            default:
                //Toast.makeText(getActivity(), "This service is coming soon!", Toast.LENGTH_SHORT).show();
                break;
        }
    }

}
