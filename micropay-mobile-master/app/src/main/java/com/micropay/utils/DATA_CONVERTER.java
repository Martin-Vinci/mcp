package com.micropay.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DATA_CONVERTER {
    public static String getInternationalFormat(String phoneNo)
    {
        String response = null;
        phoneNo = phoneNo.trim();

        if (phoneNo.isEmpty())
        {
            return null;
        }

        String phoneCode = phoneNo.substring(0, 2);
        if (phoneCode.equals("07"))   //Check whether Starting didgits are fine, Proceed and check the length
        {
            if (phoneNo.length() == 10)
            {
                response = "256" + phoneNo.substring(1);
                return response;
            }
        }
        else if (phoneCode.equals("25"))   //Check whether Starting didgits are fine, Proceed and check the length
        {
            if (phoneNo.length() == 12)
            {
                response = phoneNo;
                return response;
            }
        }
        return response;
    }

    public static String getReceiptNo(){
        final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        return dateFormat.format(new Date());
    }

    public static String capitalizeFirstLetter(String name){
        String firstLetter = name.substring(0, 1);
        String remainingLetters = name.substring(1, name.length());

        // change the first letter to uppercase
        firstLetter = firstLetter.toUpperCase();

        // join the two substrings
        name = firstLetter + remainingLetters;
        return name;
    }



}
