package com.micropay.micropay;

import android.app.Application;

import com.micropay.api.AESHandler;
import com.micropay.api.NetworkUtil;
import com.prowesspride.api.Setup;

import java.net.CookieHandler;
import java.net.CookiePolicy;

/**
 * Created by micropay on 1/31/19.
 */

public class MicropayMobile extends Application {

    public boolean connection = false;
    public static Setup setup;
    public static boolean isThermalActivated;

    public void onCreate() {
        super.onCreate();
        // enable cookies
        NetworkUtil.handleSSLHandshake();
        java.net.CookieManager cookieManager = new java.net.CookieManager();
        cookieManager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);
        CookieHandler.setDefault(cookieManager);
        String deviceId1 = NetworkUtil.getDeviceId(this);
        AESHandler.init(deviceId1);
    }

}
