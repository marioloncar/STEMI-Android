package com.stemi.STEMIHexapod;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.stemi.STEMIHexapod.activities.ConnectingActivity;

/**
 * Created by Mario on 07/05/2017.
 */

public class Utils {

    public static void showConnectionDialog(final Context context, final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setCancelable(false);
                builder.setTitle(R.string.connection_lost);
                builder.setMessage(R.string.check_connection);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(context, ConnectingActivity.class);
                        context.startActivity(i);
                        activity.finish();
                    }
                });
                builder.show();
            }
        });
    }

    public static void showStandbyDialog(final Context context, final Activity activity) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setCancelable(false);
                builder.setTitle(R.string.standby_mode_active);
                builder.setMessage(R.string.standby_mode_message);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }
}
