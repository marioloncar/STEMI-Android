package com.stemi.STEMIHexapod.activities;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.stemi.STEMIHexapod.R;
import com.stemi.STEMIHexapod.Utils;
import com.stemi.STEMIHexapod.helpers.SharedPreferencesHelper;
import com.stemi.STEMIHexapod.interfaces.DiscardCalibrationCallback;

import stemi.education.stemihexapod.Hexapod;
import stemi.education.stemihexapod.WalkingStyle;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        Utils.initActionBarWithTitle(SettingsActivity.this, this, "Settings");

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.add(android.R.id.content, settingsFragment, "SETTINGS");
        fragmentTransaction.commit();

    }

    interface DiscardCallback {
        void onDiscardFinished(Boolean finished);
    }


    public static class SettingsFragment extends PreferenceFragment {
        String savedIp, hardwareVersion, stemiId;

        byte[] calibrationValues;
        byte[] currentCalibrationValues;
        boolean standby;

        private Hexapod hexapod;

        @Override
        public void onResume() {
            super.onResume();

            final Preference ipAddr = findPreference("ipAddress");
            savedIp = SharedPreferencesHelper.getSharedPreferencesString(getActivity(), SharedPreferencesHelper.Key.IP, null);
            ipAddr.setSummary(savedIp);
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            calibrationValues = new byte[]{50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};
            currentCalibrationValues = new byte[18];

            addPreferencesFromResource(R.xml.settings);

            final Preference reset = findPreference("reset");
            final Preference id = findPreference("stemiId");
            final Preference hwVersion = findPreference("hardwareVersion");

            hardwareVersion = SharedPreferencesHelper.getSharedPreferencesString(getActivity(), SharedPreferencesHelper.Key.VERSION, null);
            stemiId = SharedPreferencesHelper.getSharedPreferencesString(getActivity(), SharedPreferencesHelper.Key.STEMI_ID, null);

            standby = getActivity().getIntent().getExtras().getBoolean("standby");

            id.setSummary(stemiId);
            hwVersion.setSummary(hardwareVersion);

            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            reset.setOnPreferenceClickListener(preference -> {
                if (standby) {
                    Utils.showStandbyDialog(getActivity(), SettingsFragment.this.getActivity());
                } else {
                    showResetDialog(builder);
                }
                return false;
            });
        }

        private void showResetDialog(final AlertDialog.Builder builder) {
            builder.setTitle(R.string.warning);
            builder.setMessage(R.string.reset_to_initial);
            builder.setPositiveButton(R.string.yes, (dialog, id) -> {
                final ProgressDialog[] progress = new ProgressDialog[1];
                getActivity().runOnUiThread(() -> {
                    progress[0] = new ProgressDialog(getActivity());
                    progress[0].setIndeterminate(true);
                    progress[0].setMessage(getString(R.string.resetting));
                    progress[0].setCancelable(false);
                    progress[0].show();
                });
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        discardValuesToInitial(finished -> getActivity().runOnUiThread(progress[0]::dismiss));
                    }
                };
                thread.start();
            });
            builder.setNegativeButton(R.string.no, (dialog, which) -> {
            });
            builder.show();
        }

        private void discardValuesToInitial(final DiscardCalibrationCallback discardCalibrationCallback) {
            hexapod = new Hexapod(true);
            hexapod.setIpAddress(savedIp);
            hexapod.connectWithCompletion(connected -> {
                if (connected) {
                    currentCalibrationValues = hexapod.fetchDataFromHexapod();
                }
            });
            discard(finished -> {
                if (finished) {
                    discardCalibrationCallback.onDiscardedData(true);
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
                this.hexapod.writeDataToHexapod(saved -> {
                    if (saved) {
                        currentCalibrationValues = new byte[]{};
                        hexapod = null;

                        SharedPreferencesHelper.putSharedPreferencesString(getActivity(), SharedPreferencesHelper.Key.WALK, WalkingStyle.TRIPOD_GAIT.toString());
                        SharedPreferencesHelper.putSharedPreferencesInt(getActivity(), SharedPreferencesHelper.Key.HEIGHT, 50);

                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        hexapod = new Hexapod(false);
                        hexapod.setIpAddress(savedIp);

                        int height = (byte) SharedPreferencesHelper.getSharedPreferencesInt(getActivity(), SharedPreferencesHelper.Key.HEIGHT, 50);
                        hexapod.setHeight(height);

                        String walkingStyle = SharedPreferencesHelper.getSharedPreferencesString(getActivity(), SharedPreferencesHelper.Key.WALK, WalkingStyle.TRIPOD_GAIT.toString());
                        SharedPreferencesHelper.putSharedPreferencesInt(getActivity(), SharedPreferencesHelper.Key.RB_SELECTED, R.id.rb1);
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

}
