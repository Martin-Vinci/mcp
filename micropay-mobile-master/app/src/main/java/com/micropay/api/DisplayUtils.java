package com.micropay.api;

import android.content.Context;
import android.content.res.Resources;
import android.text.method.KeyListener;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.widget.EditText;

/**
 * Created by developer on 9/20/18.
 */

public class DisplayUtils {

    public static float convertPixelsToDp(float px) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float dp = px / (metrics.densityDpi / 160f);
        return Math.round(dp);
    }

    public static EditText disableEditing(EditText editText) {
        editText.setTag(editText.getKeyListener());
        editText.setKeyListener(null);
        return editText;
    }

    public static void enableEditing(EditText editText) {
        editText.setKeyListener((KeyListener) editText.getTag());
    }

    public static float convertDpToPixel(float dp) {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        float px = dp * (metrics.densityDpi / 160f);
        return Math.round(px);
    }

    public static int convertDpToPx(int dp, Context ctx) {
        return Math.round(dp * (ctx.getResources().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));

    }

    public static int convertPxToDp(int px, Context ctx) {
        return Math.round(px / (Resources.getSystem().getDisplayMetrics().xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public static float dpFromPx(float px, Context ctx) {
        return px / ctx.getResources().getDisplayMetrics().density;
    }

    public static float pxFromDp(float dp, Context ctx) {
        return dp * ctx.getResources().getDisplayMetrics().density;
    }

    public static int dpToPx(int dp, Context ctx) {
        Resources r = ctx.getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }
}
