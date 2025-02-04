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

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.micropay.adaptor.MenuAdaptor;
import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.api.DisplayUtils;
import com.micropay.api.GridSpaceDeco;
import com.micropay.api.ItemClickListener;
import com.micropay.api.NetworkUtil;
import com.micropay.fragments.transactions.TXNCashDeposit;
import com.micropay.fragments.transactions.centenary.TxnAbcCashDepositAgent;
import com.micropay.fragments.transactions.centenary.TxnAbcCashDepositCustomer;
import com.micropay.micropay.ExceptionHandler;
import com.micropay.micropay.FragmentHandler;
import com.micropay.micropay.R;
import com.micropay.popups.DialogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BanksMenu extends Fragment implements ItemClickListener {

    private RecyclerView recyclerView;
    private final String TAG = TXNCashDeposit.class.getSimpleName();
    private SwipeRefreshLayout swipeContainer;
    private CacheUtil cacheUtil;
    private AlertDialog alertDialog;
    private String billerCode = "ABC_BANKS";
    private JSONArray menuList = new JSONArray();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(getActivity()));
        View rootView = inflater.inflate(R.layout.fcontent_list, container, false);
        cacheUtil = new CacheUtil(getActivity().getApplicationContext());

        FragmentHandler loanActivity = (FragmentHandler) getActivity();
        loanActivity.setTitle("Other Banks");

        getOtherBanks();

        swipeContainer = rootView.findViewById(R.id.swipeRefresh);
        recyclerView = rootView.findViewById(R.id.recyclerView);

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
                    DisplayUtils.dpToPx(1, getActivity()), true);
        }

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.addItemDecoration(gridSpacingItemDecoration);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setBackgroundColor(Color.parseColor("#FFFFFF"));

        MenuAdaptor adapter = new MenuAdaptor(getActivity(), menuList);
        recyclerView.setAdapter(adapter);
        adapter.setClickListener(this);
        swipeContainer.setOnRefreshListener(() -> swipeContainer.setRefreshing(false));
        swipeContainer.setColorSchemeResources(android.R.color.holo_blue_dark,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_red_dark);
    }

    private void getOtherBanks() {
        try {
            if (NetworkUtil.isNetworkAvailable(requireActivity().getApplicationContext())) {
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.POST,
                        Constants.getBillPayUrl() + "/findBillerProducts",
                        new JSONObject().put("billerCode", billerCode)
                                .put("channelSource", "MOBILE"),
                        response -> {
                            if (alertDialog != null && alertDialog.isShowing())
                                alertDialog.dismiss();
                            if (response != null) {
                                if (response.length() > 0) {
                                    if ("0".equalsIgnoreCase(response.optJSONObject("response").optString("responseCode"))) {
                                        JSONArray productList = response.optJSONArray("data");
                                        getMenu(productList);
                                    } else {
                                        showAlertDialog(response.optJSONObject("response").optString("responseMessage"));
                                    }
                                } else
                                    showAlertDialog(getActivity().getString(R.string.resp_timeout));
                            } else
                                showAlertDialog(getActivity().getString(R.string.resp_timeout));
                        }, error -> {
                    if (alertDialog != null && alertDialog.isShowing())
                        alertDialog.dismiss();
                    showAlertDialog(NetworkUtil.getErrorDesc(error));
                }) {
                    @Override
                    public Map<String, String> getHeaders() {
                        Map<String, String> headers = new HashMap<>();
                        headers.put("Authorization", "Basic " + Constants.getRawBasicData());
                        headers.put("sessionId", cacheUtil.getString("sessionId"));
                        return headers;
                    }
                };
                jsonObjectRequest.setTag(TAG);
                NetworkUtil.getInstance(requireActivity().getApplicationContext()).addToRequestQueue(jsonObjectRequest);
                alertDialog = DialogUtils.showProgressDialog(getActivity(), "Loading Banks", false);
            } else {
                showAlertDialog(getString(R.string.no_network));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showAlertDialog(String body) {
        if (getActivity() != null && !getActivity().isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.dialogTheme);
            builder.setMessage(body)
                    .setPositiveButton("OK",
                            (arg0, arg1) -> arg0.dismiss()).setCancelable(false);
            if (alertDialog != null && alertDialog.isShowing()) {
                alertDialog.dismiss();
            }
            alertDialog = builder.create();
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        }
    }

    private void getMenu(JSONArray productList) {
        menuList = new JSONArray();
        try {

            menuList.put(0, new JSONObject().put("label", "Centenary").put("code", "CENTENARY")
                    .put("icon", R.drawable.centenary_bank));

            int i = 1;

            for (int count = 0; count < productList.length(); count++) {
                JSONObject entry = productList.optJSONObject(count);
                String key = entry.optString("code");
                String description = entry.optString("description2");

                if (key.equals("ABSA")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.absa_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("BOA")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.boa_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("CAIRO")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.cairo_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("POST")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.postbank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("BARODA")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.bank_of_baroda));
                    i = i + 1;
                    continue;
                }
                if (key.equals("DFCU")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.dfcu_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("DTB")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.dtb_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("ECOBANK")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.ecobank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("EQUITY")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.equity_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("BANKOFINDIA")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.dtb_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("EXIM")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.exim_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("FTB")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.ftb));
                    i = i + 1;
                    continue;
                }
                if (key.equals("HFB")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.hfb));
                    i = i + 1;
                    continue;
                }
                if (key.equals("I&M")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.im_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("KCB")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.kcb_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("NCBA")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.ncba_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("OBUL")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.opportunity_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("ABC")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.abc_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("UGAFODE")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.ugafod));
                    i = i + 1;
                    continue;
                }
                if (key.equals("STANBIC")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.stanbic_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("UBA")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.uba_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("STANCHART")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.stanchart_bank));
                    i = i + 1;
                    continue;
                }
                if (key.equals("TROPICAL")) {
                    menuList.put(i, new JSONObject().put("label", description).put("code", key)
                            .put("icon", R.drawable.tropical_bank));
                    i = i + 1;
                    continue;
                }
            }

            List<JSONObject> jsonObjects = new ArrayList<>();
            for (int j = 0; j < menuList.length(); j++) {
                jsonObjects.add(menuList.getJSONObject(j));
            }
            Collections.sort(jsonObjects, new Comparator<JSONObject>() {
                @Override
                public int compare(JSONObject o1, JSONObject o2) {
                    return o1.optString("description").compareTo(o2.optString("description"));
                }
            });
            menuList = new JSONArray(jsonObjects);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        setupViews();
    }

    @Override
    public void onClick(View view, int position) throws JSONException {
        String entityType = cacheUtil.getString("entityType");
        Fragment fragment;
        JSONObject selectedData = menuList.getJSONObject(position);
        String code = selectedData.optString("code");
        String label = selectedData.optString("label");
        if (code.equals("CENTENARY")) {
            cacheUtil.putString(Constants.MENU, "Centenary");
            fragment = new CentenaryMenu();
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.container, fragment).addToBackStack(null)
                    .commit();
        } else {
            if ("CUSTOMER".equals(entityType))
                fragment = new TxnAbcCashDepositCustomer();
            else
                fragment = new TxnAbcCashDepositAgent();

            cacheUtil.putString(Constants.TITLE, label);
            cacheUtil.putString(Constants.KEY, code);
            getActivity().getSupportFragmentManager().beginTransaction()
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .replace(R.id.container, fragment).addToBackStack(null)
                    .commit();
        }
    }

}
