package com.micropay.api;

import android.view.View;

import org.json.JSONException;

/**
 * Created by developer on 9/20/18.
 */

public interface ItemClickListener {
    public void onClick(View view, int position) throws JSONException;
}
