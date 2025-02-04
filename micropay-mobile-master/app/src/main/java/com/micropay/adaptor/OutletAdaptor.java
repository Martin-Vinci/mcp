package com.micropay.adaptor;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.micropay.api.CacheUtil;
import com.micropay.api.Constants;
import com.micropay.micropay.R;
import com.micropay.fragments.AccountDisplay;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;


/**
 * Created by developer on 9/20/18.
 */

public class OutletAdaptor extends RecyclerView.Adapter<OutletAdaptor.ViewHolder> {

    private JSONArray accountList;
    private Activity mCtx;
    private CacheUtil cacheUtil;
    private NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);

    public OutletAdaptor(JSONArray list, Activity mCtx) {
        this.accountList = list;
        this.mCtx = mCtx;
        cacheUtil = new CacheUtil(mCtx.getApplicationContext());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.outlet_item, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final JSONObject jsonObject = accountList.optJSONObject(position);

        holder.outletName.setText(jsonObject.optString("OUTLET_NM"));
        holder.outletCode.setText(jsonObject.optString("OUTLET_CD"));
        holder.outletBranch.setText(jsonObject.optString("BRANCH_NAME"));
        holder.accountNo.setText(jsonObject.optString("ACCT_NO"));
        holder.accountBalance.setText(String.format(Locale.ENGLISH, "%s %s",
                formatter.format(getAbs(jsonObject.optDouble("AVAIL_BAL"))), jsonObject.optString("CRNCY_CD")));


        holder.accountBalance.setTextColor(Color.parseColor(jsonObject.optString("COLOR")));
        holder.card_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cacheUtil.putString("OUTLET_FUND_ACCT", jsonObject.optString("ACCT_NO"));
                Fragment fragment = new AccountDisplay();
                Bundle bundle = new Bundle();
                bundle.putString("CUST_NO", jsonObject.optString("CUST_NO"));
                bundle.putBoolean("OUTLET_FUND", true);
                bundle.putString(Constants.KEY, "AgentTransfer");
                if (mCtx != null && !mCtx.isFinishing()) {
                    fragment.setArguments(bundle);
                    ((FragmentActivity) mCtx).getSupportFragmentManager().beginTransaction()
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .replace(R.id.container, fragment).addToBackStack(null)
                            .commit();
                }
            }
        });
        holder.app_menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popup = new PopupMenu(mCtx, holder.app_menu);
                popup.inflate(R.menu.account_menu);
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if (R.id.fund_outlet == item.getItemId()) {
                            cacheUtil.putString("OUTLET_FUND_ACCT", jsonObject.optString("ACCT_NO"));
                            Fragment fragment = new AccountDisplay();
                            Bundle bundle = new Bundle();
                            bundle.putString("CUST_NO", jsonObject.optString("CUST_NO"));
                            bundle.putBoolean("OUTLET_FUND", true);
                            bundle.putString(Constants.KEY, "AgentTransfer");
                            if (mCtx != null && !mCtx.isFinishing()) {
                                fragment.setArguments(bundle);
                                ((FragmentActivity) mCtx).getSupportFragmentManager().beginTransaction()
                                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                        .replace(R.id.container, fragment).addToBackStack(null)
                                        .commit();
                            }
                        }
                        return false;
                    }
                });
                if (mCtx != null) {
                    popup.show();
                }
            }
        });
    }

    private BigDecimal getAbs(double defaultValue) {
        try {
            return new BigDecimal(defaultValue).abs();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }

    @Override
    public int getItemCount() {
        return accountList.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView outletName, outletCode, outletBranch, accountNo, accountBalance;
        public ImageButton app_menu;
        public CardView card_view;

        public ViewHolder(View itemView) {
            super(itemView);

            outletName = itemView.findViewById(R.id.outletName);
            outletCode = itemView.findViewById(R.id.outletCode);
            outletBranch = itemView.findViewById(R.id.outletBranch);
            accountNo = itemView.findViewById(R.id.accountNo);
            accountBalance = itemView.findViewById(R.id.accountBalance);

            app_menu = itemView.findViewById(R.id.app_menu);
            card_view = itemView.findViewById(R.id.card_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
        }
    }

    private final String TAG = OutletAdaptor.class.getSimpleName();
}
