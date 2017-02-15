package com.stemi.STEMIHexapod.activities;

import android.annotation.SuppressLint;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;

import com.stemi.STEMIHexapod.interfaces.DiscardCalibrationCallback;
import com.stemi.STEMIHexapod.R;

import mario.com.stemihexapod.ConnectingCompleteCallback;
import mario.com.stemihexapod.Hexapod;
import mario.com.stemihexapod.SavedCalibrationCallback;
import mario.com.stemihexapod.WalkingStyle;


public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        initActionBarWithTitle("Settings");

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.add(android.R.id.content, settingsFragment, "SETTINGS");
        fragmentTransaction.commit();

    }

    private void initActionBarWithTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>" + title + "</font>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>" + title + "</font>"));
        }

        @SuppressLint("PrivateResource")
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.abc_ic_ab_back_material, null);
        assert upArrow != null;
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.highlightColor), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(upArrow);
    }

    interface DiscardCallback {
        void onDiscardFinished(Boolean finished);
    }


    public static class SettingsFragment extends PreferenceFragment {

        static SharedPreferences prefs;
        String savedIp, hardwareVersion, stemiId;
        byte[] calibrationValues;
        byte[] currentCalibrationValues;
        private Hexapod hexapod;

        @Override
        public void onResume() {
            super.onResume();

            final Preference ipAddr = findPreference("ipAddress");
            savedIp = prefs.getString("ip", null);
            ipAddr.setSummary(savedIp);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            calibrationValues = new byte[]{50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
            currentCalibrationValues = new byte[18];

            prefs = this.getActivity().getSharedPreferences("myPref", Context.MODE_PRIVATE);

            addPreferencesFromResource(R.xml.settings);

            final Preference reset = findPreference("reset");
            final Preference id = findPreference("stemiId");
            final Preference hwVersion = findPreference("hardwareVersion");

            hardwareVersion = prefs.getString("version", null);
            stemiId = prefs.getString("stemiId", null);

            id.setSummary(stemiId);
            hwVersion.setSummary(hardwareVersion);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            reset.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    showResetDialog(builder);
                    return false;
                }
            });


        }


        private void showResetDialog(final AlertDialog.Builder builder) {
            builder.setTitle("Warning");
            builder.setMessage("Are you sure that you want to reset STEMI Hexapod legs to their initial positions?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    final ProgressDialog[] progress = new ProgressDialog[1];
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress[0] = new ProgressDialog(getActivity());
                            progress[0].setIndeterminate(true);
                            progress[0].setMessage("Resetting...");
                            progress[0].setCancelable(false);
                            progress[0].show();
                        }
                    });
                    Thread thread = new Thread() {
                        @Override
                        public void run() {

                            discardValuesToInitial(new DiscardCalibrationCallback() {
                                @Override
                                public void onDiscardedData(Boolean finished) {
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress[0].dismiss();
                                        }
                                    });
                                }
                            });
                        }

                    };
                    thread.start();

                }

            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                }
            });
            builder.show();
        }

        private void discardValuesToInitial(final DiscardCalibrationCallback discardCalibrationCallback) {
            hexapod = new Hexapod(true);
            hexapod.setIpAddress(savedIp);
            hexapod.connectWithCompletion(new ConnectingCompleteCallback() {
                @Override
                public void onConnectingComplete(boolean connected) {
                    if (connected) {
                        currentCalibrationValues = hexapod.fetchDataFromHexapod();
                        System.out.println("CALIBRATION VAL -> " + currentCalibrationValues[3]);
                    }
                }
            });

            discard(new DiscardCallback() {
                @Override
                public void onDiscardFinished(Boolean finished) {
                    if (finished) {
                        discardCalibrationCallback.onDiscardedData(true);
                    }

                }

            });


        }

        private void discard(final DiscardCallback discardCallback) {
            byte[] calculatingNumbers = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            for (int i = 0; i <= 10; i++) {
                for (int j = 0; j < calibrationValues.length; j++) {
                    if (i == 0) {
                        byte calc = (byte) (Math.abs(calibrationValues[j] - currentCalibrationValues[j]) / 10);
                        calculatingNumbers[j] = calc;
                    }
                    if (i < 10) {
                        if (currentCalibrationValues[j] < calibrationValues[j]) {
                            currentCalibrationValues[j] += calculatingNumbers[j];
                            try {
                                this.hexapod.setCalibrationValue(this.currentCalibrationValues[j], j);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if (currentCalibrationValues[j] > calibrationValues[j]) {
                            currentCalibrationValues[j] -= calculatingNumbers[j];
                            try {
                                this.hexapod.setCalibrationValue(this.currentCalibrationValues[j], j);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        currentCalibrationValues[j] = calibrationValues[j];
                        try {
                            this.hexapod.setCalibrationValue(this.currentCalibrationValues[j], j);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            this.hexapod.disconnect();
            try {
                this.hexapod.writeDataToHexapod(new SavedCalibrationCallback() {
                    @Override
                    public void onSavedData(Boolean saved) {
                        if (saved) {
                            currentCalibrationValues = new byte[]{};
                            hexapod = null;
                            prefs.edit().putString("walk", WalkingStyle.TRIPOD_GAIT.toString()).apply();
                            prefs.edit().putInt("height", 50).apply();
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            hexapod = new Hexapod(false);
                            hexapod.setIpAddress(savedIp);
                            int height = (byte) prefs.getInt("height", 50);
                            hexapod.setHeight(height);
                            String walkingStyle = prefs.getString("walk", WalkingStyle.TRIPOD_GAIT.toString());
                            hexapod.setWalkingStyle(WalkingStyle.valueOf(walkingStyle));
                            hexapod.connect();

                            try {
                                Thread.sleep(1000);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            hexapod.disconnect();

                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }

                            discardCallback.onDiscardFinished(true);
                        }
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
    }
}
