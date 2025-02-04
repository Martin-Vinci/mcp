package com.micropay.utils;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.micropay.models.Picklist;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class SpinnerUtil {
    static String response;

    public static String getSelectedValue(Spinner spinner) {
        try {
            Picklist object = (Picklist) spinner.getSelectedItem();
            response = object.getDescription();
        } catch (Exception ex) {
        }
        return response;
    }

    public void populateSpinner(JSONArray array, Spinner spinner, Context activity, int resource, String keyItem, String valueItem) {
        try {
            List<Picklist> itemList = new ArrayList<>();
            if (array.length() >= 1 && !keyItem.equals("acctNo"))
                itemList.add(new Picklist("Tap to select", "0"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject entry = array.optJSONObject(i);
                String key = entry.optString(keyItem);
                String value = array.getJSONObject(i).getString(valueItem);
                itemList.add(new Picklist(value, key));
            }
            ArrayAdapter<Picklist> spinnerAdapter = new ArrayAdapter<Picklist>(activity, resource, itemList);
            spinnerAdapter.setDropDownViewResource(resource);
            spinner.setAdapter(spinnerAdapter);

            if (array.length() == 1)
                spinner.setSelection(0);
        } catch (Exception ex) {

        }
    }

    public void populateSpinner(JSONArray array, Spinner spinner, Context activity, int resource, String keyItem,
                                String valueItem, String entityType) {
        try {
            List<Picklist> itemList = new ArrayList<>();
            if (array.length() > 1)
                itemList.add(new Picklist("Tap to select", "0"));
            for (int i = 0; i < array.length(); i++) {
                JSONObject entry = array.optJSONObject(i);
                String key = entry.optString(keyItem);
                String value = array.getJSONObject(i).getString(valueItem);
                if (entityType.equalsIgnoreCase("CUSTOMER") && value.toLowerCase().contains("withdraw"))
                    continue;

                itemList.add(new Picklist(value, key));
            }
            ArrayAdapter<Picklist> spinnerAdapter = new ArrayAdapter<Picklist>(activity, resource, itemList);
            spinnerAdapter.setDropDownViewResource(resource);
            spinner.setAdapter(spinnerAdapter);
        } catch (Exception ex) {

        }
    }


//    public void restoreSpinnerSelection(Spinner spinner, String myString) {
//        try {
//            int index = 0;
//            for (int i=0;i<spinner.getCount();i++){
//                Dictionary object = (Dictionary) spinner.getItemAtPosition(i);
//                response = object.getValue().toString();
//                if (object.getValue().toString().equals(myString)){
//                    index = i;
//                    spinner.setSelection(index);
//                }
//            }
//        } catch (Exception ex) {
//
//        }
//    }

    public static String getSelectedKey(Spinner spinner) {
        try {
            Picklist object = (Picklist) spinner.getSelectedItem();
            response = object.getCode().toString();
        } catch (Exception ex) {
        }
        return response;
    }


//    public String getSelectedValue(Spinner spinner) {
//        try {
//            Dictionary object = (Dictionary) spinner.getSelectedItem();
//            response = object.getValue().toString();
//        } catch (Exception ex) {
//        }
//        return response;
//    }

    public static void setSelection(Spinner spinner, String myString) {
        int index = 0;
        try {
            for (int i = 0; i < spinner.getCount(); i++) {
                if (spinner.getItemAtPosition(i).equals(myString)) {
                    index = i;
                    spinner.setSelection(index);
                }
            }
        } catch (Exception ex) {
        }
    }
}
