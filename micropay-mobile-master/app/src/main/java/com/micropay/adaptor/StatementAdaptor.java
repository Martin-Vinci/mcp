package com.micropay.adaptor;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.micropay.api.Constants;
import com.micropay.models.StudentDetails;
import com.micropay.popups.StatementDetail;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by developer on 9/20/18.
 */

public class StatementAdaptor extends RecyclerView.Adapter<StatementAdaptor.ViewHolder> {

    private JSONArray accountHistory;
    private Activity mCtx;
    private NumberFormat formatter = NumberFormat.getInstance(Locale.ENGLISH);

    public StatementAdaptor(JSONArray list, Activity mCtx) {
        this.accountHistory = list;
        this.mCtx = mCtx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.stmt_item_layout, parent, false);
        return new ViewHolder(v);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final JSONObject jsonObject = accountHistory.optJSONObject(position);
        //holder.itemView.setOutlineSpotShadowColor(Color.YELLOW);
        if(position %2 == 1)
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#ffffff"));
            //  holder.imageView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        }
        else
        {
            holder.itemView.setBackgroundColor(Color.parseColor("#f9e6ff"));
            //  holder.imageView.setBackgroundColor(Color.parseColor("#FFFAF8FD"));
        }
        double credit= jsonObject.optDouble("credit");
        double debit= jsonObject.optDouble("debit");
        String amount = jsonObject.optString("txnAmount");
        holder.tran_date.setText(jsonObject.optString("effectiveDate"));
        if (debit > 0 && credit <= 0)
            amount = "-" + amount;
        holder.amount.setText(formatter.format(getAbs(amount)) + " " + jsonObject.optString("UGX"));
        holder.narration.setText(jsonObject.optString("description"));
        holder.balance.setText(formatter.format(getAbs(jsonObject.optString("closing"))));

//        holder.amount.setText(formatter.format(getAbs(amount)) + " " + jsonObject.optString("UGX"));
//        if (amount.startsWith("-")) {
//            holder.amount.setTextColor(mCtx.getResources().getColor(android.R.color.holo_red_dark));
//        } else {
//            holder.amount.setTextColor(mCtx.getResources().getColor(android.R.color.holo_green_dark));
//        }
//        holder.card_view.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                try {
//                    StatementDetail roleSheet = new StatementDetail();
//                    Bundle bundle = new Bundle();
//                    bundle.putString(Constants.KEY, jsonObject.toString());
//                    roleSheet.setArguments(bundle);
//                    roleSheet.show(((FragmentActivity) mCtx).getSupportFragmentManager(), "Statement Detail");
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private BigDecimal getAbs(String defaultValue) {
        if (defaultValue != null || "null".equalsIgnoreCase(defaultValue)) {
            try {
                return new BigDecimal(defaultValue).abs();
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        return BigDecimal.ZERO;
    }

    @Override
    public int getItemCount() {
        return accountHistory.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView tran_date,amount,narration,balance;

        public TextView tran_desc;
        public CardView card_view;

        public ViewHolder(View itemView) {
            super(itemView);
            tran_date = itemView.findViewById(R.id.stmnt_tranDate);
            amount = itemView.findViewById(R.id.stmnt_amount);
            narration = itemView.findViewById(R.id.stmnt_narration);
            balance = itemView.findViewById(R.id.stmnt_balance);
            card_view = itemView.findViewById(R.id.card_view);
        }
    }

}
