package com.micropay.api;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.collection.LruCache;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by developer on 9/19/18.
 */

public class NetworkUtil {
    private static NetworkUtil mInstance;
    private static Context mCtx;
    private RequestQueue mRequestQueue;
    private ImageLoader mImageLoader;
    private static CacheUtil cacheUtil;

    private NetworkUtil(Context context) {
        mCtx = context;
        mRequestQueue = getRequestQueue();
        mImageLoader = new ImageLoader(mRequestQueue,
                new ImageLoader.ImageCache() {
                    private final LruCache<String, Bitmap>
                            cache = new LruCache<String, Bitmap>(20);

                    @Override
                    public Bitmap getBitmap(String url) {
                        return cache.get(url);
                    }

                    @Override
                    public void putBitmap(String url, Bitmap bitmap) {
                        cache.put(url, bitmap);
                    }
                });
    }

    public static synchronized NetworkUtil getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new NetworkUtil(context);
        }
        return mInstance;
    }

    private static SSLSocketFactory getSocketFactory() {

        try {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] victimizedManager = new TrustManager[]{

                    new X509TrustManager() {

                        public X509Certificate[] getAcceptedIssuers() {

                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];

                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                            if (chain == null || chain.length == 0)
                                throw new IllegalArgumentException("Certificate is null or empty");
                            if (authType == null || authType.length() == 0)
                                throw new IllegalArgumentException("Authtype is null or empty");
                            if (!authType.equalsIgnoreCase("ECDHE_RSA") &&
                                    !authType.equalsIgnoreCase("ECDHE_ECDSA") &&
                                    !authType.equalsIgnoreCase("RSA") &&
                                    !authType.equalsIgnoreCase("ECDSA"))
                                throw new CertificateException("Certificate is not trust");
//                            try {
//                                chain[0].checkValidity();
//                            } catch (Exception e) {
//                                throw new CertificateException("Certificate is not valid or trusted");
//                            }
                        }
                    }
            };

            // Install the all-trusting trust manager
            final SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, victimizedManager, new java.security.SecureRandom());
            // Create an ssl socket factory with our all-trusting manager
            final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

            return sslSocketFactory;
        } catch (KeyManagementException | NoSuchAlgorithmException e) {
            return null;
        }

    }

    @SuppressLint("TrulyRandom")
    public static void handleSSLHandshake() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @Override
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                @Override
                public void checkServerTrusted(X509Certificate[] chain, String authType)  throws CertificateException {
                    if (chain == null || chain.length == 0)
                        throw new IllegalArgumentException("Certificate is null or empty");
                    if (authType == null || authType.length() == 0)
                        throw new IllegalArgumentException("Authtype is null or empty");
                    if (!authType.equalsIgnoreCase("ECDHE_RSA") &&
                            !authType.equalsIgnoreCase("ECDHE_ECDSA") &&
                            !authType.equalsIgnoreCase("RSA") &&
                            !authType.equalsIgnoreCase("ECDSA"))
                        throw new CertificateException("Certificate is not trust");
                }
            }};

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> {
                if (Constants.PUBLIC_HOST_NAME.equalsIgnoreCase(hostname)) {
                    return true;
                } else {
                    return false;
                }
            });
        } catch (Exception ignored) {
        }
    }


    public static String getErrorDesc(VolleyError error) {
        error.printStackTrace();

        if (error instanceof NetworkError) {
            return "A network related error occurred while processing your request";
        } else if (error.getCause() instanceof MalformedURLException) {
            return "Your request is invalid. (Bad Request)";
        } else if (error instanceof ServerError) {
            return "The server failed to respond correctly";
        } else if (error instanceof AuthFailureError) {
            return "The server failed to authenticate your request";
        } else if (error instanceof ParseError) {
            return "We were unable to interpret the response from the server.";
        } else if (error instanceof NoConnectionError) {
            return "Unfortunately, there is no valid connection to the provider.";
        } else if (error instanceof TimeoutError) {
            return "Unfortunately your request timed out";
        } else if (error.getCause() instanceof OutOfMemoryError) {
            return "Unfortunately your device is running low on memory";
        } else if (error.getCause() instanceof Exception) {
            return error.getLocalizedMessage();
        }
        if (error == null || error.networkResponse == null) {
            return "Something happened to your connection";
        }
        //get response body and parse with appropriate encoding
        try {
            //get status code here
            return new String(error.networkResponse.data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // exception
        }
        return "Something happened to your connection";
    }

    public static JSONObject getBaseRequest(Context activity) {
        JSONObject jsonObject = new JSONObject();
        try {
            cacheUtil = new CacheUtil(activity.getApplicationContext());
            jsonObject.put("channelCode", Constants.getChannelCode());
            jsonObject.put("outletCode", cacheUtil.getString("outletCode"));
            jsonObject.put("phoneNo", cacheUtil.getString("registeredPhone"));
            jsonObject.put("entityName", cacheUtil.getString("customerName"));
            jsonObject.put("deviceId", getDeviceId(activity));
            jsonObject.put("deviceModel", getDeviceModel());
        } catch (JSONException ex) {
        }
        return jsonObject;
    }

    public static String getDeviceId(Context ctx) {
        String result;
        String deviceId = Settings.Secure.getString(ctx.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        result = deviceId;
        return result;
    }

    public static String getDeviceModel() {
        return Build.MANUFACTURER
                + " " + Build.MODEL + " " + Build.VERSION.RELEASE
                + " " + Build.VERSION_CODES.class.getFields()[Build.VERSION.SDK_INT].getName();
    }

//    public static String getImeiNumber(Context ctx){
//        TelephonyManager tManager = (TelephonyManager) ctx
//                .getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceIMEI = tManager.getDeviceId();
//
//        return  deviceIMEI;
//    }


    public static void copyToClip(Context mContext, String subject, String linkTxt) {

        try {
            ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText(subject, linkTxt);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(mContext, subject, Toast.LENGTH_SHORT).show();
        } catch (Exception x) {
        }
    }

    public static void shareTextUrl(Context mContext, String subject, String linkTxt) {
        Intent share = new Intent(Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        share.putExtra(Intent.EXTRA_SUBJECT, subject);
        share.putExtra(Intent.EXTRA_TEXT, linkTxt);
        mContext.startActivity(Intent.createChooser(share, "Share link!"));
    }

    public static boolean isNetworkAvailable(Context ctx) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static String md5Hash(String s) {
        MessageDigest m = null;
        try {
            m = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        m.update(s.getBytes(), 0, s.length());
        String hash = new BigInteger(1, m.digest()).toString(16);
        return hash.toUpperCase();
    }

    public static void hideSoftKeyboard(Activity ctx) {
        try {
            if (ctx.getCurrentFocus() != null) {
                InputMethodManager inputMethodManager = (InputMethodManager) ctx
                        .getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(ctx
                        .getCurrentFocus().getWindowToken(), 0);
            }
        } catch (Exception e) {
        }
    }

    public static void openGooglePlay(Activity ctx, String pkgName) {
        try {
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + pkgName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            ctx.startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=" + pkgName)));
        }
    }

    public static void doKeepDialog(Dialog dialog) {
        try {
            WindowManager.LayoutParams layoutManager = new WindowManager.LayoutParams();
            layoutManager.copyFrom(dialog.getWindow().getAttributes());
            layoutManager.width = WindowManager.LayoutParams.WRAP_CONTENT;
            layoutManager.height = WindowManager.LayoutParams.WRAP_CONTENT;
            dialog.getWindow().setAttributes(layoutManager);
        } catch (Exception e) {
        }
    }

    public static void renderDialog(AlertDialog alertDialog, AlertDialog.Builder builder) {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = builder.create();
        NetworkUtil.doKeepDialog(alertDialog);
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null && mCtx != null) {
            mRequestQueue = Volley.newRequestQueue(mCtx.getApplicationContext(),
                    new HurlStack(null, getSocketFactory()));
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        if (!isNetworkAvailable(mCtx)) {
            Toast.makeText(mCtx, "Your device is offline. Please check your connection before trying again",
                    Toast.LENGTH_SHORT).show();
        } else {
            req.setRetryPolicy(new DefaultRetryPolicy(
                    0,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            getRequestQueue().add(req);
        }
    }

    public void cancel(String tagLabel) {
        RequestQueue requestQueue = getRequestQueue();
        if (requestQueue != null)
            requestQueue.cancelAll(tagLabel);
    }

}
