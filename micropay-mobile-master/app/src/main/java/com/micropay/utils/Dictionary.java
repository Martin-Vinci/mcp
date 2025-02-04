package com.micropay.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Dictionary {
    public static JSONArray findTitles() throws JSONException {
        JSONArray list = new JSONArray();
        JSONObject item;
        item = new JSONObject().put("key", "10").put("value", "Dr.");
        list.put(item);
        item = new JSONObject().put("key", "17").put("value", "Hajati");
        list.put(item);
        item = new JSONObject().put("key", "16").put("value", "Haji");
        list.put(item);
        item = new JSONObject().put("key", "7").put("value", "Miss");
        list.put(item);
        item = new JSONObject().put("key", "2").put("value", "Mr.");
        list.put(item);
        item = new JSONObject().put("key", "8").put("value", "Mrs.");
        list.put(item);
        item = new JSONObject().put("key", "9").put("value", "Ms.");
        list.put(item);
        item = new JSONObject().put("key", "4").put("value", "Sir");
        list.put(item);
        return list;
    }
    public static JSONArray findIdTypes() throws JSONException {
        JSONArray list = new JSONArray();
        JSONObject item;
        item = new JSONObject().put("key", "1").put("value", "Drivers  License");
        list.put(item);
        item = new JSONObject().put("key", "3").put("value", "National ID");
        list.put(item);
        item = new JSONObject().put("key", "14").put("value", "NSSF No");
        list.put(item);
        item = new JSONObject().put("key", "2").put("value", "Passport");
        list.put(item);
        item = new JSONObject().put("key", "5").put("value", "Refugee ID");
        list.put(item);
        item = new JSONObject().put("key", "7").put("value", "Student ID Card");
        list.put(item);
        return list;
    }
    public static JSONArray findGenders() throws JSONException {
        JSONArray list = new JSONArray();
        JSONObject item;
        item = new JSONObject().put("key", "M").put("value", "Male");
        list.put(item);
        item = new JSONObject().put("key", "F").put("value", "Female");
        list.put(item);
        return list;
    }

    public static JSONArray findAirtimeRecipients() throws JSONException {
        JSONArray list = new JSONArray();
        JSONObject item;
        item = new JSONObject().put("key", "Self").put("value", "1-For myself");
        list.put(item);
        item = new JSONObject().put("key", "Other").put("value", "2-Another number");
        list.put(item);
        return list;
    }



}
