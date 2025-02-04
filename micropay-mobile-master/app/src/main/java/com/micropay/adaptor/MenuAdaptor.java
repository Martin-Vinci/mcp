package com.micropay.adaptor;

import android.content.Context;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.micropay.api.ItemClickListener;
import com.micropay.micropay.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by developer on 9/20/18.
 */

public class MenuAdaptor extends RecyclerView.Adapter<MenuAdaptor.MenuViewHolder> {

    private Context mContext;
    private JSONArray menuListing;
    private ItemClickListener clickListener;

    public MenuAdaptor(Context mContext, JSONArray albumList) {
        this.mContext = mContext;
        this.menuListing = albumList;
    }

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

    @Override
    public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);
        return new MenuViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MenuViewHolder holder, int position) {
        final JSONObject jsonObject = menuListing.optJSONObject(position);
        if (jsonObject != null) {
            holder.title.setText(jsonObject.optString("label"));
            //holder.sub_title.setText(jsonObject.optString("sub_title"));
            Glide.with(mContext)
                    .load(jsonObject.optInt("icon"))
                    .fitCenter()
                    .placeholder(R.mipmap.ic_launcher)
                    .crossFade()
                    .into(holder.thumbnail);
        }
    }

    @Override
    public int getItemCount() {
        return menuListing.length();
    }

    public class MenuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public TextView title;
        public ImageView thumbnail;
        public CardView cardView;

        public MenuViewHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.label);
            thumbnail = (ImageView) view.findViewById(R.id.img_view);
            //sub_title = (TextView) view.findViewById(R.id.sub_title);
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
