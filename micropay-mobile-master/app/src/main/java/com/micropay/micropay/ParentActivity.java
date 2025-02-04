package com.micropay.micropay;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PersistableBundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.micropay.api.CacheUtil;

/**
 * Created by developer on 9/23/18.
 */

public class ParentActivity extends AppCompatActivity {

    public static long DISCONNECT_TIMEOUT = 120000;
    private AlertDialog alertDialog;

    private Handler disconnectHandler = new Handler() {
        public void handleMessage(Message msg) {
        }
    };

    private Runnable disconnectCallback = () -> showAlertDialog("Your session has expired", "Your current session has expired due to inactivity.");
    private CacheUtil cacheUtil;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(this));
        cacheUtil = new CacheUtil(getApplicationContext());
        DISCONNECT_TIMEOUT = cacheUtil.getInt("SESSION_TIMEOUT", 120000);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void showAlertDialog(String title, String body) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.dialogTheme);
        if (title != null)
            builder.setTitle(title);
        builder.setMessage(body)
                .setPositiveButton("DISMISS",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                                clearAndStartNewActivity(LoginActivity.class);
                            }
                        }).setCancelable(false);
        alertDialog = builder.create();
        if (!isFinishing() && !alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

    private void clearAndStartNewActivity(Class<?> clazz) {
        Intent intent = new Intent(getApplicationContext(), clazz);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    public void resetDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
        disconnectHandler.postDelayed(disconnectCallback, DISCONNECT_TIMEOUT);
    }

    public void stopDisconnectTimer() {
        disconnectHandler.removeCallbacks(disconnectCallback);
    }

    @Override
    public void onUserInteraction() {
        resetDisconnectTimer();
    }

    @Override
    public void onResume() {
        super.onResume();
        resetDisconnectTimer();
    }

    @Override
    public void onStop() {
        super.onStop();
        stopDisconnectTimer();
    }

}
