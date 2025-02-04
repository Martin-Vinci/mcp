package com.micropay.adaptor;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.micropay.api.ItemClickListener;
import com.micropay.micropay.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by developer on 9/20/18.
 */

public class ReceiptAdaptor extends RecyclerView.Adapter<ReceiptAdaptor.ViewHolder> {

    private List<JSONObject> messageList;
    private Activity mCtx;
    private ItemClickListener clickListener;

    public ReceiptAdaptor(List<JSONObject> list, Activity mCtx , ItemClickListener clickListener) {
        this.messageList = list;
        this.mCtx = mCtx;
        this.clickListener = clickListener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.receipt_entry, parent, false);
        return new ViewHolder(v, clickListener);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final JSONObject jsonObject = messageList.get(position);
        holder.description.setText(jsonObject.optString("receiptData"));
        holder.item_date.setText("Print");
//        if (jsonObject.optBoolean("read", false)) {
//            holder.title.setTextColor(mCtx.getResources().getColor(R.color.colorRead));
//        } else {
//            holder.title.setTextColor(mCtx.getResources().getColor(R.color.colorUnRead));
//        }
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

    @Override
    public int getItemCount() {
        return messageList.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView description, item_date;
        public CardView card_view;
        private ItemClickListener clickListener;
        public ViewHolder(View itemView, ItemClickListener clickListener) {
            super(itemView);
            description = itemView.findViewById(R.id.description);
            item_date = itemView.findViewById(R.id.item_date);
            card_view = itemView.findViewById(R.id.card_view);

            this.clickListener = clickListener;
            itemView.setOnClickListener(this);

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
