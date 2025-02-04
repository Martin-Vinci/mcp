package com.micropay.adaptor;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.micropay.api.ItemClickListener;
import com.micropay.micropay.R;
import com.micropay.fragments.CreditInfo;
import com.micropay.fragments.LoanRepayment;
import com.micropay.utils.NumberUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;


/**
 * Created by developer on 9/20/18.
 */

public class PaymentRequestAdaptor extends RecyclerView.Adapter<PaymentRequestAdaptor.MenuViewHolder> {

    private Context mContext;
    private JSONArray menuListing;
    private ItemClickListener clickListener;

    public PaymentRequestAdaptor(Context mContext, JSONArray albumList) {
        this.mContext = mContext;
        this.menuListing = albumList;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public PaymentRequestAdaptor.MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.payment_requests, parent, false);
        return new PaymentRequestAdaptor.MenuViewHolder(itemView);
    }


    @Override
    public void onBindViewHolder(final PaymentRequestAdaptor.MenuViewHolder holder, int position) {
        final JSONObject jsonObject = menuListing.optJSONObject(position);
        if (jsonObject != null) {
            String amount = NumberUtils.toCurrencyFormat(NumberUtils.convertToBigDecimal(jsonObject.optString("amount")));
            holder.amountTextView.setText("Amount: " + amount);
            holder.phoneNumberTextView.setText("Phone Number: " + jsonObject.optString("requesterPhone"));
            holder.reasonTextView.setText("Reason: " + jsonObject.optString("requesterReason"));
            holder.requestDateTextView.setText("Request Date: " + jsonObject.optString("createDate"));
            Glide.with(mContext)
                    .load(jsonObject.optInt("icon"))
                    .fitCenter()
                    .placeholder(R.mipmap.ic_launcher)
                    .crossFade();
        }
    }

    @Override
    public int getItemCount() {
        return menuListing.length();
    }

    public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView amountTextView;
        TextView phoneNumberTextView;
        TextView reasonTextView;
        TextView requestDateTextView;
        public CardView cardView;

        public MenuViewHolder(View view) {
            super(view);

            amountTextView = itemView.findViewById(R.id.amountTextView);
            phoneNumberTextView = itemView.findViewById(R.id.phoneNumberTextView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
            requestDateTextView = itemView.findViewById(R.id.requestDateTextView);
            cardView = view.findViewById(R.id.card_view);
            itemView.setOnClickListener(this); // bind the listener
        }

        @Override
        public void onClick(View view) {
            if (clickListener != null) {
                try {
                    clickListener.onClick(view, getAdapterPosition());
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
