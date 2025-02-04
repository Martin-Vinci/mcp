package com.micropay.adaptor;

import android.app.Activity;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * Created by developer on 9/20/18.
 */

public class NotificationAdaptor extends RecyclerView.Adapter<NotificationAdaptor.ViewHolder> {

    private JSONArray notifications;
    private Activity mCtx;

    public NotificationAdaptor(JSONArray list, Activity mCtx) {
        this.notifications = list;
        this.mCtx = mCtx;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.notification_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        try {
            final JSONObject jsonObject = notifications.getJSONObject(position);

            if (jsonObject.has("customer")) {
                holder.title.setText(jsonObject.optString("title"));
                holder.description.setText(jsonObject.optString("message"));
                holder.item_date.setText(jsonObject.optString("date"));
                if (jsonObject.getBoolean("read")) {
                    holder.title.setTextColor(mCtx.getResources().getColor(R.color.colorRead));
                } else {
                    holder.title.setTextColor(mCtx.getResources().getColor(R.color.colorUnRead));
                }

                holder.customer_card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDetailsDialog(jsonObject.optString("title"), jsonObject.optString("message"));
                    }
                });

                Glide.with(mCtx)
                        .load(jsonObject.optInt("customer_photo"))
                        .fitCenter()
                        .placeholder(R.mipmap.ic_launcher)
                        .crossFade()
                        .into(holder.customer_photo);

                holder.customer_card.setVisibility(View.VISIBLE);
                holder.notification_card.setVisibility(View.GONE);

            } else {

                holder.title.setText(jsonObject.optString("title"));
                holder.description.setText(jsonObject.optString("message"));
                holder.item_date.setText(jsonObject.optString("date"));
                if (jsonObject.getBoolean("read")) {
                    holder.title.setTextColor(mCtx.getResources().getColor(R.color.colorRead));
                } else {
                    holder.title.setTextColor(mCtx.getResources().getColor(R.color.colorUnRead));
                }
                holder.notification_card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showDetailsDialog(jsonObject.optString("title"), jsonObject.optString("message"));
                    }
                });
                Glide.with(mCtx)
                        .load(jsonObject.optInt("icon_url"))
                        .fitCenter()
                        .placeholder(R.mipmap.ic_launcher)
                        .crossFade()
                        .into(holder.notification_image);

                holder.notification_card.setVisibility(View.VISIBLE);
                holder.customer_card.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return notifications.length();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public TextView title, description, item_date, customer_name, customer_activity, customer_progress, customer_date;
        public CardView notification_card, customer_card;
        public AppCompatImageView customer_photo, notification_image;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
            item_date = itemView.findViewById(R.id.item_date);

            customer_photo = itemView.findViewById(R.id.customer_photo);
            notification_image = itemView.findViewById(R.id.notification_image);

            customer_name = itemView.findViewById(R.id.customer_name);
            customer_activity = itemView.findViewById(R.id.customer_activity);
            customer_progress = itemView.findViewById(R.id.customer_progress);
            customer_date = itemView.findViewById(R.id.customer_date);

            notification_card = itemView.findViewById(R.id.notification_card);
            customer_card = itemView.findViewById(R.id.customer_card);
        }
    }

    private void showDetailsDialog(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mCtx);
        builder.setTitle(title);
        builder.setMessage(body)
                .setPositiveButton("OK",
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
}
