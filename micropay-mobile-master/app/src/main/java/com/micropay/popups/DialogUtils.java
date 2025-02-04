package com.micropay.popups;

import android.content.Context;
import android.content.DialogInterface;
import androidx.appcompat.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.micropay.micropay.R;

public class DialogUtils {

    public static AlertDialog showProgressDialog(Context context, String progress, boolean dismissable) {
        AlertDialog alertDialog = null;
        try {
            LayoutInflater li = LayoutInflater.from(context);
            View layoutView = li.inflate(R.layout.progress_dialog, null);
            androidx.appcompat.app.AlertDialog.Builder alertDialogBuilder = new androidx.appcompat.app.AlertDialog.Builder(
                    context);
            alertDialogBuilder.setView(layoutView);
            ((TextView) layoutView.findViewById(R.id.status_label)).setText(progress);
            alertDialogBuilder.setCancelable(false);
            alertDialog = alertDialogBuilder.create();
            alertDialog.setCancelable(dismissable);
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return alertDialog;
    }

    public static void showAlertDialog(Context context, String title, String bodyTxt, boolean dismissable) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.dialogTheme);
        if (title != null)
            builder.setTitle(title);
        builder.setMessage(bodyTxt)
                .setPositiveButton("OK",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                arg0.dismiss();
                            }
                        }).setCancelable(dismissable);
        AlertDialog alertDialog = builder.create();
        if (!alertDialog.isShowing()) {
            alertDialog.show();
        }
    }

}
