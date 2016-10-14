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
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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
import java.util.Arrays;


public class SettingsActivity extends AppCompatActivity {

    SharedPreferences prefs = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_layout);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar));
        actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>Settings</font>"));

        @SuppressLint("PrivateResource")
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.abc_ic_ab_back_mtrl_am_alpha, null);
        assert upArrow != null;
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.highlightColor), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(upArrow);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.add(android.R.id.content, settingsFragment, "SETTINGS");
        fragmentTransaction.commit();

        prefs = getSharedPreferences("myPref", MODE_PRIVATE);


    }

    interface DiscardCalibInterface {
        void onDiscardedData(Boolean finished);
    }

    interface DiscardInterface {
        void onDiscardFinished(Boolean finished);
    }

    interface SavedCalibrationInterface {
        void onSavedData(Boolean saved);
    }


    public static class SettingsFragment extends PreferenceFragment {

        static SharedPreferences prefs;
        String savedIp, hardwareVersion, stemiId;
        byte[] calibrationArray;
        byte[] newCalibrationArray;
        boolean connected;
        int sleepingInterval = 100;
        int writeData;

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

            calibrationArray = new byte[]{50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50};

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

                            discardValuesToInitial(new DiscardCalibInterface() {
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

        private void discardValuesToInitial(final DiscardCalibInterface callback) {

            newCalibrationArray = fetchBin(savedIp);
            if (newCalibrationArray != null) {
                sendCommandsOverWiFi(savedIp);
            }

            discard(new DiscardInterface() {
                @Override
                public void onDiscardFinished(Boolean finished) {
                    if (finished) {
                        callback.onDiscardedData(true);
                    }

                }

            });


        }

        private void discard(final DiscardInterface callback) {


            byte[] calculatingNumbers = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
            for (int i = 0; i <= 10; i++) {
                for (int j = 0; j < calibrationArray.length; j++) {
                    if (i == 0) {
                        byte calc = (byte) (Math.abs(calibrationArray[j] - newCalibrationArray[j]) / 10);
                        calculatingNumbers[j] = calc;
                    }
                    if (i < 10) {
                        if (newCalibrationArray[j] < calibrationArray[j]) {
                            newCalibrationArray[j] += calculatingNumbers[j];
                        } else if (newCalibrationArray[j] > calibrationArray[j]) {
                            newCalibrationArray[j] -= calculatingNumbers[j];
                        }
                    } else {
                        newCalibrationArray[j] = calibrationArray[j];
                    }
                }
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            saveDataOverWiFi(savedIp, new SavedCalibrationInterface() {
                @Override
                public void onSavedData(Boolean saved) {
                    if (saved) {
                        callback.onDiscardFinished(true);
                    }
                }
            });

        }

        public void saveDataOverWiFi(final String ip, final SavedCalibrationInterface callback) {
            connected = false;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Thread t = new Thread() {
                public void run() {
                    try {
                        Socket socket = new Socket(ip, 80);
                        OutputStream outputStream = socket.getOutputStream();

//                    CommandSender wifiSender = new CommandSender(outputStream, socket);

                        try {
                            writeData = 1;
                            BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
                            Thread.sleep(sleepingInterval);
                            buffOutStream.write(bytesArray());
                            buffOutStream.flush();
                            socket.close();

                            Thread.sleep(500);

                            sendReturnCommandsOverWiFi(savedIp);
                            prefs.edit().putInt("walk", 30).apply();
                            prefs.edit().putInt("rbSelected", R.id.rb1).apply();
                            prefs.edit().putInt("height", 50).apply();

                            Thread.sleep(600);

                            callback.onSavedData(true);

                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            connected = false;
                        }

                    } catch (IOException ignored) {
                    }
                }
            };
            t.start();
        }

        public void sendCommandsOverWiFi(final String ip) {
            connected = true;

            Thread t = new Thread() {
                public void run() {
                    try {
                        Socket socket = new Socket(ip, 80);
                        OutputStream outputStream = socket.getOutputStream();

                        try {
                            BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
                            while (connected) {
                                Thread.sleep(sleepingInterval);
                                buffOutStream.write(bytesArray());
                                buffOutStream.flush();
                            }
                            socket.close();

                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        } finally {
                            connected = false;
                        }

                    } catch (IOException ignored) {
                    }
                }
            };
            t.start();
        }

        public void sendReturnCommandsOverWiFi(final String ip) {
            connected = true;

            Thread t = new Thread() {
                public void run() {
                    try {
                        Socket socket = new Socket(ip, 80);
                        OutputStream outputStream = socket.getOutputStream();

                        try {
                            BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
                            Thread.sleep(sleepingInterval);
                            buffOutStream.write(bytesArrayReturn());
                            buffOutStream.flush();
                            socket.close();

                        } catch (IOException | InterruptedException e) {
                            e.printStackTrace();
                        }

                    } catch (IOException ignored) {
                    }
                }
            };
            t.start();
        }

        private byte[] fetchBin(String params) {
            ByteArrayOutputStream baos = null;
            try {
                URL url = new URL("http://" + params + "/linearization.bin");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.connect();

                if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return new byte[0];
                }

                baos = new ByteArrayOutputStream();
                DataOutputStream dos = new DataOutputStream(baos);
                byte[] data = new byte[4096];
                int count = conn.getInputStream().read(data);
                while (count != -1) {
                    dos.write(data, 3, 18);
                    count = conn.getInputStream().read(data);

                }
            } catch (IOException e) {
                Log.d("TAG", "Getting calibration: " + e.getMessage());
            }
            return baos.toByteArray();
        }

        private byte[] bytesArray() {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
            };
            try {
                outputStream.write("LIN".getBytes());
                outputStream.write(newCalibrationArray);
                outputStream.write(writeData);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return outputStream.toByteArray();
        }

        private byte[] bytesArrayReturn() {
            byte[] slidersArray = {0, 0, 0, 50, 0, 0, 0, 0, 0};

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
            };
            try {
                outputStream.write("PKT".getBytes());
                outputStream.write(0);
                outputStream.write(0);
                outputStream.write(0);
                outputStream.write(0);
                outputStream.write(0);
                outputStream.write(1);
                outputStream.write(0);
                outputStream.write(0);
                outputStream.write(50);
                outputStream.write(30);
                outputStream.write(slidersArray);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return outputStream.toByteArray();
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
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}
