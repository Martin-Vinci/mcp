package com.micropay.adaptor;

import org.json.JSONObject;
import java.util.Comparator;

public class ItemIdComparator implements Comparator<JSONObject> {
    @Override
    public int compare(JSONObject item1, JSONObject item2) {
        return Integer.compare(item2.optInt("messageId"),
                item1.optInt("messageId"));
    }
}