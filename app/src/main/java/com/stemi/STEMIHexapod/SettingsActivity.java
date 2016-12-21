package com.stemi.STEMIHexapod;

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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;

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
            actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>"+title+"</font>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>"+title+"</font>"));
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

    interface SavedCallback {
        void onSavedData(Boolean saved);
    }


    public static class SettingsFragment extends PreferenceFragment {

        static SharedPreferences prefs;
        String savedIp, hardwareVersion, stemiId;
        byte[] calibrationValues;
        byte[] currentCalibrationValues;
        boolean connected;
        final static int SLEEPING_INTERVAL = 100;
        int writeData;
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

            writeData = 0;

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
            // DODATI THREAD
            hexapod = new Hexapod(true);
            hexapod.setIpAddress(savedIp);
            hexapod.connectWithCompletion(new ConnectingCompleteCallback() {
                @Override
                public void onConnectingComplete(boolean connected) {
                    if (connected) {
                        currentCalibrationValues = hexapod.fetchDataFromHexapod();
                    }
                }
            });

//            currentCalibrationValues = fetchBin(savedIp);
//            if (currentCalibrationValues != null) {
//                sendCommandsOverWiFi(savedIp);
//            }

            discard(new DiscardCallback() {
                @Override
                public void onDiscardFinished(Boolean finished) {
                    if (finished) {
                        discardCalibrationCallback.onDiscardedData(true);
                    }

                }

            });


        }

        private void discard(final DiscardCallback callback) {
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
                                this.hexapod.setValue(this.currentCalibrationValues[j], j);
                            } catch (Exception e) {
                                Log.e("SettingsActivity", "Exception", e);
                            }
                        } else if (currentCalibrationValues[j] > calibrationValues[j]) {
                            currentCalibrationValues[j] -= calculatingNumbers[j];
                            try {
                                this.hexapod.setValue(this.currentCalibrationValues[j], j);
                            } catch (Exception e) {
                                Log.e("SettingsActivity", "Exception", e);
                            }
                        }
                    } else {
                        currentCalibrationValues[j] = calibrationValues[j];
                        try{
                            this.hexapod.setValue(this.currentCalibrationValues[j], j);
                        }
                        catch(Exception e){
                            Log.e("SettingsActivity", "Exception", e);
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
                        if (saved){
                            currentCalibrationValues = new byte[]{};
                            hexapod = null;
                            prefs.edit().putString("walk", WalkingStyle.TripodGait.toString()).apply();
                            prefs.edit().putInt("height", 50).apply();
                            hexapod = new Hexapod(false);
                            hexapod.setIpAddress(savedIp);
                            int height = (byte) prefs.getInt("height", 0);
                            hexapod.setHeight(height);
                            // DOVRŠITI

                        }
                    }
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

//            saveDataOverWiFi(savedIp, new SavedCallback() {
//                @Override
//                public void onSavedData(Boolean saved) {
//                    if (saved) {
//                        callback.onDiscardFinished(true);
//                    }
//                }
//            });

        }

//        public void saveDataOverWiFi(final String ip, final SavedCallback callback) {
//            connected = false;
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            Thread t = new Thread() {
//                public void run() {
//                    try {
//                        Socket socket = new Socket(ip, 80);
//                        OutputStream outputStream = socket.getOutputStream();
//
////                    CommandSender wifiSender = new CommandSender(outputStream, socket);
//
//                        try {
//                            writeData = 1;
//                            BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
//                            Thread.sleep(SLEEPING_INTERVAL);
//                            buffOutStream.write(bytesArray());
//                            buffOutStream.flush();
//                            socket.close();
//
//                            Thread.sleep(500);
//
//                            sendReturnCommandsOverWiFi(savedIp);
//                            prefs.edit().putInt("walk", 30).apply();
//                            prefs.edit().putInt("rbSelected", R.id.rb1).apply();
//                            prefs.edit().putInt("height", 50).apply();
//
//                            Thread.sleep(600);
//
//                            callback.onSavedData(true);
//
//                        } catch (IOException | InterruptedException e) {
//                            e.printStackTrace();
//                        } finally {
//                            connected = false;
//                        }
//
//                    } catch (IOException ignored) {
//                    }
//                }
//            };
//            t.start();
//        }

//        public void sendCommandsOverWiFi(final String ip) {
//            connected = true;
//
//            Thread t = new Thread() {
//                public void run() {
//                    try {
//                        Socket socket = new Socket(ip, 80);
//                        OutputStream outputStream = socket.getOutputStream();
//
//                        try {
//                            BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
//                            while (connected) {
//                                Thread.sleep(SLEEPING_INTERVAL);
//                                buffOutStream.write(bytesArray());
//                                buffOutStream.flush();
//                            }
//                            socket.close();
//
//                        } catch (IOException | InterruptedException e) {
//                            e.printStackTrace();
//                        } finally {
//                            connected = false;
//                        }
//
//                    } catch (IOException ignored) {
//                    }
//                }
//            };
//            t.start();
//        }

//        public void sendReturnCommandsOverWiFi(final String ip) {
//            connected = true;
//
//            Thread t = new Thread() {
//                public void run() {
//                    try {
//                        Socket socket = new Socket(ip, 80);
//                        OutputStream outputStream = socket.getOutputStream();
//
//                        try {
//                            BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
//                            Thread.sleep(SLEEPING_INTERVAL);
//                            buffOutStream.write(bytesArrayReturn());
//                            buffOutStream.flush();
//                            socket.close();
//
//                        } catch (IOException | InterruptedException e) {
//                            e.printStackTrace();
//                        }
//
//                    } catch (IOException ignored) {
//                    }
//                }
//            };
//            t.start();
//        }
//
//        private byte[] fetchBin(String params) {
//            ByteArrayOutputStream baos = null;
//            try {
//                URL url = new URL("http://" + params + "/linearization.bin");
//                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//                conn.connect();
//
//                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK)
//                    return new byte[0];
//
//                baos = new ByteArrayOutputStream();
//                DataOutputStream dos = new DataOutputStream(baos);
//
//                byte[] data = new byte[4096];
//                int count = conn.getInputStream().read(data);
//                while (count != -1) {
//                    dos.write(data, 3, 18);
//                    count = conn.getInputStream().read(data);
//
//                }
//            } catch (IOException e) {
//                Log.d("TAG", "Getting calibration: " + e.getMessage());
//            }
//            return baos.toByteArray();
//        }
//
//        private byte[] bytesArray() {
//
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
//            };
//            try {
//                outputStream.write("LIN".getBytes());
//                outputStream.write(currentCalibrationValues);
//                outputStream.write(writeData);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return outputStream.toByteArray();
//        }
//
//        private byte[] bytesArrayReturn() {
//            byte[] slidersArray = {0, 0, 0, 50, 0, 0, 0, 0, 0};
//
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
//            };
//            try {
//                outputStream.write("PKT".getBytes());
//                outputStream.write(0);
//                outputStream.write(0);
//                outputStream.write(0);
//                outputStream.write(0);
//                outputStream.write(0);
//                outputStream.write(1);
//                outputStream.write(0);
//                outputStream.write(0);
//                outputStream.write(50);
//                outputStream.write(30);
//                outputStream.write(slidersArray);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//
//            return outputStream.toByteArray();
//        }


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
