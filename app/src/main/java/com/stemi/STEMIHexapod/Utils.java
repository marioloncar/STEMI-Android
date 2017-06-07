package com.stemi.STEMIHexapod;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;

import com.stemi.STEMIHexapod.activities.ConnectingActivity;

/**
 * Created by Mario on 07/05/2017.
 */

public class Utils {

    public static void initActionBarWithTitle(AppCompatActivity activity, Context context, String title) {
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.navbar));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>" + title + "</font>", Html.FROM_HTML_MODE_LEGACY));
            } else {
                actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>" + title + "</font>"));
            }

            @SuppressLint("PrivateResource")
            final Drawable upArrow = ResourcesCompat.getDrawable(context.getResources(), R.drawable.abc_ic_ab_back_material, null);
            assert upArrow != null;
            upArrow.setColorFilter(ContextCompat.getColor(context, R.color.highlightColor), PorterDuff.Mode.SRC_ATOP);
            actionBar.setHomeAsUpIndicator(upArrow);
        }
    }

    public static Typeface getCustomTypeface(final Context context) {
        return Typeface.createFromAsset(context.getAssets(),
                "fonts/ProximaNova-Regular.otf");
    }

    public static void showConnectionDialog(final Context context, final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        activity.runOnUiThread(() -> {
            builder.setCancelable(false);
            builder.setTitle(R.string.connection_lost);
            builder.setMessage(R.string.check_connection);
            builder.setPositiveButton(R.string.ok, (dialog, id) -> {
                Intent i = new Intent(context, ConnectingActivity.class);
                context.startActivity(i);
                activity.finish();
            });
            builder.show();
        });
    }

    public static void showStandbyDialog(final Context context, final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        activity.runOnUiThread(() -> {
            builder.setCancelable(false);
            builder.setTitle(R.string.standby_mode_active);
            builder.setMessage(R.string.standby_mode_message);
            builder.setPositiveButton(R.string.ok, (dialog, id) -> dialog.dismiss());
            builder.show();
        });
    }
}
