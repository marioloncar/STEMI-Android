package com.stemi.STEMIHexapod;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Mario on 29/08/16.
 */
public class CalibrationActivity extends AppCompatActivity implements View.OnClickListener {

    interface SavedCalibrationInterface {
        void onSavedData(Boolean saved);
    }

    interface DiscardCalibInterface {
        void onDiscardedData(Boolean finished);
    }

    private ImageButton ibCalibUp, ibCalibD;

    private class Motor {
        int idx;
        ImageButton imageButton;

        Motor(int idx, ImageButton imageButton) {
            this.idx = idx;
            this.imageButton = imageButton;
        }
    }

    private Map<Integer, Motor> mMotorsMap;
    private int[] motorResIds = {R.id.ibMotor0, R.id.ibMotor1, R.id.ibMotor2, R.id.ibMotor3, R.id.ibMotor4,
                                 R.id.ibMotor5, R.id.ibMotor6, R.id.ibMotor7, R.id.ibMotor8, R.id.ibMotor9,
                                 R.id.ibMotor10, R.id.ibMotor11, R.id.ibMotor12, R.id.ibMotor13, R.id.ibMotor14,
                                 R.id.ibMotor15, R.id.ibMotor16, R.id.ibMotor17};
    private AlertDialog.Builder builder;
    private ImageView ivCircle;
    private TextView tvCalibValue, tvSelect;
    private final static int SLEEPING_INTERVAL = 100;
    private Boolean connected;

    private MediaPlayer movingSound, movingSoundShort;

    // bytes of the LIN (linearization) packets
    private byte[] calibrationArray = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 0};
    private byte[] newCalibrationArray = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 0};
    private int index;
    private final Handler repeatUpdateHandler = new Handler();
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    private static final int REPEAT_DELAY = 50;
    private String savedIp;
    private int writeData;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibration_layout);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar_landscape));
        actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>Calibration</font>"));

        @SuppressLint("PrivateResource")
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.abc_ic_ab_back_material, null);
        assert upArrow != null;
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.highlightColor), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(upArrow);

        mMotorsMap = new LinkedHashMap<>(19);

        for (int i = 0; i < 18; i++) {
            mMotorsMap.put(motorResIds[i], new Motor(i, (ImageButton) findViewById(motorResIds[i])));
        }

        ivCircle = (ImageView) findViewById(R.id.ivCircle);
        tvCalibValue = (TextView) findViewById(R.id.tvCalibValue);
        tvSelect = (TextView) findViewById(R.id.tvSelect);
        ibCalibUp = (ImageButton) findViewById(R.id.ibCalibUp);
        ibCalibD = (ImageButton) findViewById(R.id.ibCalibDown);

        builder = new AlertDialog.Builder(this);

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        SharedPreferences prefs = getSharedPreferences("myPref", MODE_PRIVATE);
        savedIp = prefs.getString("ip", null);
        writeData = 0;

        tvSelect.setTypeface(tf);
        tvCalibValue.setTypeface(tf);
        movingSound = MediaPlayer.create(this, R.raw.moving_sound);
        movingSoundShort = MediaPlayer.create(this, R.raw.moving_sound_short);

        ibCalibD.setEnabled(false);
        ibCalibUp.setEnabled(false);

        Thread thread = new Thread() {
            @Override
            public void run() {
                calibrationArray = fetchBin(savedIp);
                newCalibrationArray = calibrationArray.clone();
                if (calibrationArray != null) {
                    sendCommandsOverWiFi("");
                }
            }
        };

        thread.start();
        /*** Calibration up listeners ***/
        ibCalibUp.setOnClickListener(v -> {
            increment();
            if (newCalibrationArray[index] == 100) {
                movingSoundShort.pause();
            } else {
                movingSoundShort.start();
            }
        });

        ibCalibUp.setOnLongClickListener(v -> {
            movingSound.seekTo(0);
            movingSound.start();
            mAutoIncrement = true;
            repeatUpdateHandler.post(new RepeatUpdater());
            return true;
        });

        ibCalibUp.setOnTouchListener((v, event) -> {
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoIncrement) {
                mAutoIncrement = false;
                movingSound.pause();
            }
            return false;
        });

        /*** Calibration up listeners END***/


        /*** Calibration down listeners ***/
        ibCalibD.setOnClickListener(v -> {
            decrement();
            if (newCalibrationArray[index] == 0) {
                movingSoundShort.pause();
            } else {
                movingSoundShort.start();
            }
        });

        ibCalibD.setOnLongClickListener(v -> {
            movingSound.seekTo(0);
            movingSound.start();
            mAutoDecrement = true;
            repeatUpdateHandler.post(new RepeatUpdater());

            return true;
        });

        ibCalibD.setOnTouchListener((v, event) -> {
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoDecrement) {
                mAutoDecrement = false;
                movingSound.pause();
            }
            return false;
        });

        /*** Calibration down listeners END ***/
    }

    @Override
    public boolean onSupportNavigateUp() {
        showBackDialog();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        showBackDialog();
    }


    private void showBackDialog() {
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to reset STEMI Hexapod legs to their initial positions?");
        builder.setPositiveButton("Yes", (dialog, id) -> discardValuesToInitial(finished -> {
            if (finished) {
                connected = false;
                finish();

            }
        }));
        builder.setNegativeButton("No", (dialog, id) -> {
            //dismiss dialog
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.save_ip) {
            saveDataOverWiFi(savedIp, saved -> {
                if (saved) {
                    finish();
                }
            });
        }

        return super.onOptionsItemSelected(item);

    }

    private void showMotor(int resId) {
        Motor m = mMotorsMap.get(resId);
        ibCalibD.setEnabled(true);
        ibCalibUp.setEnabled(true);
        index = m.idx;
        m.imageButton.setAlpha(1f);
        for (int key : mMotorsMap.keySet()) {
            if(key != resId) {
                mMotorsMap.get(key).imageButton.setAlpha(0f);
            }
        }
        setVisibility();
        String string = String.valueOf(newCalibrationArray[m.idx]);
        tvCalibValue.setText(string);
    }

    @Override
    public void onClick(View v) {
        showMotor(v.getId());
    }

    private void setVisibility() {
        tvSelect.setVisibility(View.INVISIBLE);
        ivCircle.setVisibility(View.VISIBLE);
        tvCalibValue.setVisibility(View.VISIBLE);
    }


    private void sendCommandsOverWiFi(String ip) {
        connected = true;

        Thread t = new Thread() {
            public void run() {
                try {
                    Socket socket = new Socket(ip, 80);
                    OutputStream outputStream = socket.getOutputStream();

                    try {
                        BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
                        while (connected) {
                            Thread.sleep(SLEEPING_INTERVAL);
                            buffOutStream.write(bytesArray());
                            buffOutStream.flush();
                            System.out.println("BYTES ARRAY -> " + Arrays.toString(bytesArray()));
                        }
                        socket.close();

                    } catch (IOException | InterruptedException ignored) {
                    } finally {
                        connected = false;
                    }


                } catch (IOException ignored) {

                }
            }
        };
        t.start();
    }

    private void saveDataOverWiFi(final String ip, final SavedCalibrationInterface callback) {
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
                        Thread.sleep(SLEEPING_INTERVAL);
                        buffOutStream.write(bytesArray());
                        buffOutStream.flush();
                        socket.close();

                        callback.onSavedData(true);

                    } catch (IOException | InterruptedException ignored) {
                    } finally {
                        connected = false;
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
        } catch (IOException ignored) {
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
        } catch (IOException ignored) {
        }

        return outputStream.toByteArray();
    }

    private class RepeatUpdater implements Runnable {
        public void run() {
            if (mAutoIncrement) {
                increment();
                repeatUpdateHandler.postDelayed(new RepeatUpdater(), REPEAT_DELAY);
            } else if (mAutoDecrement) {
                decrement();
                repeatUpdateHandler.postDelayed(new RepeatUpdater(), REPEAT_DELAY);
            }
        }
    }

    private void decrement() {
        if (!(newCalibrationArray[index] <= 0)) {
            newCalibrationArray[index]--;
            String sCalib = String.valueOf(newCalibrationArray[index]);
            tvCalibValue.setText(sCalib);
        } else {
            movingSound.pause();
            movingSoundShort.pause();
        }
    }

    private void increment() {
        if (!(newCalibrationArray[index] >= 100)) {
            newCalibrationArray[index]++;
            String sCalib = String.valueOf(newCalibrationArray[index]);
            tvCalibValue.setText(sCalib);
        } else {
            movingSound.pause();
            movingSoundShort.pause();
        }
    }

    private void discardValuesToInitial(DiscardCalibInterface callback) {
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
            } catch (InterruptedException ignored) {
            }
        }
        callback.onDiscardedData(true);
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



