package com.micropay.api;

import android.util.Base64;
import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

/**
 * Created by developer on 9/19/18.
 */

public class Constants {

    public static final String REGISTERED = "IS_REGISTERED";
    public static final String KEY = "TARGET";
    public static final String MENU = "MENU";
    public static final String TITLE = "TITLE";
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MMM/yyyy");
    public static final Pattern EMAIL_ADDRESS_PATTERN = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
    );
    // broadcast receiver intent filters
    public static final String REGISTRATION_COMPLETE = "registrationComplete";
    public static final String PUSH_NOTIFICATION = "pushNotification";
    // id to handle the notification in the notification tray
    public static final int NOTIFICATION_ID = 100;
    public static final int NOTIFICATION_ID_BIG_IMAGE = 101;
    public static final String CRASH_REPORT = "CRASH_REPORT";
    public static String PUBLIC_HOST_NAME = "mb.micropay.co.ug";
    //private static String uatEndPoint = "http://10.0.2.2/micropay-api";
    private static String uatEndPoint = "https://mb.micropay.co.ug:9000/micropay-apis-uat";
    private static String prodEndPoint = "https://mb.micropay.co.ug:9000/mcp-gateway-prod";
    private static String mainEndPoint = uatEndPoint;

    private static String PUBLIC_URL_NONFINANCIAL,APP_VERSION_URL, PUBLIC_URL_FINANCIAL,PUBLIC_GATEWAY_URL, PUBLIC_URL_BILLPAY, PUBLIC_IP, BASIC_DATA, VENDOR_CODE, VENDOR_PASSWORD, CHANNEL_CODE;
    private static Long INSTITUTION_ID;

    static {
        PUBLIC_URL_NONFINANCIAL = mainEndPoint + "/api/EQuiWeb";
        PUBLIC_URL_FINANCIAL = mainEndPoint + "/api/Transaction";
        PUBLIC_URL_BILLPAY = mainEndPoint + "/api/BillPayment";
        PUBLIC_GATEWAY_URL = mainEndPoint + "/api/GateWay";
        APP_VERSION_URL = mainEndPoint + "/api/AppVersion";
        PUBLIC_IP = new String(Base64.decode("MTk3LjI0OC44OS4xOA==".getBytes(), Base64.NO_WRAP));
        BASIC_DATA = new String(Base64.decode("YWRtaW46YWRtaW4=".getBytes(), Base64.NO_WRAP));
        VENDOR_CODE = "micropay";
        VENDOR_PASSWORD = "micropay";
        INSTITUTION_ID = Long.valueOf(28);
        CHANNEL_CODE = "MOBILE";
    }

    public static String getBasicData() {
        return BASIC_DATA;
    }

    public static String getRawBasicData() {
        return "YWRtaW46YWRtaW4=";
    }

    public static String getAppVersionUrl() {
        return APP_VERSION_URL;
    }

    public static String getBaseIp() {
        return PUBLIC_IP;
    }

    public static String getBaseUrl() {
        return PUBLIC_URL_NONFINANCIAL;
    }

    public static String getTransactionUrl() {
        return PUBLIC_URL_FINANCIAL;
    }

    public static String getBillPayUrl() {
        return PUBLIC_URL_BILLPAY;
    }

    public static String getwayUrl() {
        return PUBLIC_GATEWAY_URL;
    }
    public static String getVendorCode() {
        return VENDOR_CODE;
    }

    public static String getVendorPassword() {
        return VENDOR_PASSWORD;
    }

    public static String getChannelCode() {
        return CHANNEL_CODE;
    }

    public static Long getInstitutionId() {
        return INSTITUTION_ID;
    }
}
