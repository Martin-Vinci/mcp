package com.micropay.adaptor;


import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.micropay.api.CacheUtil;
import com.micropay.micropay.AgentHomeActivity;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONObject;


/**
 * Created by micropay on 01/25/2021.
 */

public class RolesAdaptor extends RecyclerView.Adapter<RolesAdaptor.ViewHolder> {

    private final CacheUtil cacheUtil;
    private JSONArray rolesList;
    private Activity mCtx;

    public RolesAdaptor(JSONArray list, Activity mCtx) {
        this.mCtx = mCtx;
        this.rolesList = list;
        cacheUtil = new CacheUtil(mCtx.getApplicationContext());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.role_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RolesAdaptor.ViewHolder holder, int position) {
        final JSONObject jsonObject = rolesList.optJSONObject(position);
        holder.role_name.setText(jsonObject.optString("businessRoleName"));
        holder.business_unit.setText(jsonObject.optString("businessUnitName"));
    }

    private void showDetailsDialog(final String roleName, final String businessUnit, final long buId, final long buRoleId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle("Proceed with this role?");
        builder.setMessage(roleName + " AT " + businessUnit)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                if (cacheUtil != null) {
                                    cacheUtil.putString("roleName", roleName);
                                    cacheUtil.putString("businessUnit", businessUnit);
                                    cacheUtil.putLong("buId", buId);
                                    cacheUtil.putLong("buRoleId", buRoleId);
                                }
                                clearAndStartNewActivity(AgentHomeActivity.class);
                            }
                        })
                .setNegativeButton("CANCEL",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        }).setCancelable(false);
        AlertDialog alertDialog = builder.create();
        if (!mCtx.isFinishing()) {
            alertDialog.show();
        }
    }

    private void clearAndStartNewActivity(Class<?> clazz) {
        Intent intent = new Intent(mCtx.getApplicationContext(), clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        mCtx.startActivity(intent);
        mCtx.finish();
    }


    @Override
    public int getItemCount() {
        return rolesList.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView role_name, business_unit;
        public CardView card_view;

        public ViewHolder(View itemView) {
            super(itemView);
            role_name = itemView.findViewById(R.id.role_name);
            business_unit = itemView.findViewById(R.id.business_unit);
            card_view = itemView.findViewById(R.id.card_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final JSONObject jsonObject = rolesList.optJSONObject(getAdapterPosition());
            card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (getItemCount() == 1) {
                        showDetailsDialog(jsonObject.optString("businessRoleName"),
                                jsonObject.optString("businessUnitName"),
                                jsonObject.optLong("buId"), jsonObject.optLong("buRoleId"));
                    } else {
                        cacheUtil.putString("roleName", jsonObject.optString("businessRoleName"));
                        cacheUtil.putString("businessUnit", jsonObject.optString("businessUnitName"));
                        cacheUtil.putLong("buId", jsonObject.optLong("buId"));
                        cacheUtil.putLong("buRoleId", jsonObject.optLong("buRoleId"));
                        clearAndStartNewActivity(AgentHomeActivity.class);
                    }
                }
            });
        }
    }

}
