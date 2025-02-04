package com.micropay.adaptor;
import android.app.Activity;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.micropay.micropay.R;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Comparator;
import java.util.List;

/**
 * Created by developer on 9/20/18.
 */

public class MessageAdaptor extends RecyclerView.Adapter<MessageAdaptor.ViewHolder> {

    private List<JSONObject> messageList;
    private Activity mCtx;

    public MessageAdaptor(List<JSONObject> list, Activity mCtx) {
        this.messageList = list;
        this.mCtx = mCtx;
    }





    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.inbox_entry, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        final JSONObject jsonObject = messageList.get(position);
        holder.description.setText(jsonObject.optString("smsText"));
        holder.item_date.setText(jsonObject.optString("createDate"));
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

        public ViewHolder(View itemView) {
            super(itemView);
            description = itemView.findViewById(R.id.description);
            item_date = itemView.findViewById(R.id.item_date);
            card_view = itemView.findViewById(R.id.card_view);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            final JSONObject jsonObject = messageList.get(getAdapterPosition());
            card_view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDetailsDialog(jsonObject.optString("title"), jsonObject.optString("message"));
                }
            });
        }
    }
}
