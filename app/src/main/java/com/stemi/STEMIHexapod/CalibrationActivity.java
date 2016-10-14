package com.stemi.STEMIHexapod;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import android.util.Log;
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

    ImageButton ibMotor0, ibMotor1, ibMotor2, ibMotor3, ibMotor4, ibMotor5, ibMotor6, ibMotor7,
            ibMotor8, ibMotor9, ibMotor10, ibMotor11, ibMotor12, ibMotor13, ibMotor14, ibMotor15, ibMotor16, ibMotor17, ibCalibUp, ibCalibD;
    ImageButton[] motors;
    AlertDialog.Builder builder;
    ImageView ivCircle;
    TextView tvCalibValue, tvSelect;
    private int sleepingInterval = 100;
    private Boolean connected;

    SharedPreferences prefs;
    Typeface tf;
    private MediaPlayer movingSound, movingSoundShort;

    // bytes of the LIN (linearization) packets
    private byte[] calibrationArray = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 0};
    private byte[] newCalibrationArray = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 0};
    private int index;
    private Handler repeatUpdateHandler = new Handler();
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    int REPEAT_DELAY = 50;
    public String savedIp;
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
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.abc_ic_ab_back_mtrl_am_alpha, null);
        assert upArrow != null;
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.highlightColor), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(upArrow);

        ibMotor0 = (ImageButton) findViewById(R.id.ibMotor0);
        ibMotor1 = (ImageButton) findViewById(R.id.ibMotor1);
        ibMotor2 = (ImageButton) findViewById(R.id.ibMotor2);
        ibMotor3 = (ImageButton) findViewById(R.id.ibMotor3);
        ibMotor4 = (ImageButton) findViewById(R.id.ibMotor4);
        ibMotor5 = (ImageButton) findViewById(R.id.ibMotor5);
        ibMotor6 = (ImageButton) findViewById(R.id.ibMotor6);
        ibMotor7 = (ImageButton) findViewById(R.id.ibMotor7);
        ibMotor8 = (ImageButton) findViewById(R.id.ibMotor8);
        ibMotor9 = (ImageButton) findViewById(R.id.ibMotor9);
        ibMotor10 = (ImageButton) findViewById(R.id.ibMotor10);
        ibMotor11 = (ImageButton) findViewById(R.id.ibMotor11);
        ibMotor12 = (ImageButton) findViewById(R.id.ibMotor12);
        ibMotor13 = (ImageButton) findViewById(R.id.ibMotor13);
        ibMotor14 = (ImageButton) findViewById(R.id.ibMotor14);
        ibMotor15 = (ImageButton) findViewById(R.id.ibMotor15);
        ibMotor16 = (ImageButton) findViewById(R.id.ibMotor16);
        ibMotor17 = (ImageButton) findViewById(R.id.ibMotor17);
        ivCircle = (ImageView) findViewById(R.id.ivCircle);
        tvCalibValue = (TextView) findViewById(R.id.tvCalibValue);
        tvSelect = (TextView) findViewById(R.id.tvSelect);
        ibCalibUp = (ImageButton) findViewById(R.id.ibCalibUp);
        ibCalibD = (ImageButton) findViewById(R.id.ibCalibDown);

        motors = new ImageButton[]{ibMotor0, ibMotor1, ibMotor2, ibMotor3, ibMotor4, ibMotor5, ibMotor6,
                ibMotor7, ibMotor8, ibMotor9, ibMotor10, ibMotor11, ibMotor12, ibMotor13, ibMotor14, ibMotor15, ibMotor16, ibMotor17};

        builder = new AlertDialog.Builder(this);

        tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        prefs = getSharedPreferences("myPref", MODE_PRIVATE);
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
                    sendCommandsOverWiFi(savedIp);
                }
            }
        };

        thread.start();
        /*** Calibration up listeners ***/
        ibCalibUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
                if (newCalibrationArray[index] == 100) {
                    movingSoundShort.pause();
                } else {
                    movingSoundShort.start();
                }
            }
        });

        ibCalibUp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                movingSound.seekTo(0);
                movingSound.start();
                mAutoIncrement = true;
                repeatUpdateHandler.post(new RepeatUpdater());
                return true;
            }
        });

        ibCalibUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoIncrement) {
                    mAutoIncrement = false;
                    movingSound.pause();
                }
                return false;
            }
        });

        /*** Calibration up listeners END***/


        /*** Calibration down listeners ***/
        ibCalibD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement();
                if (newCalibrationArray[index] == 0) {
                    movingSoundShort.pause();
                } else {
                    movingSoundShort.start();
                }
            }
        });

        ibCalibD.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                movingSound.seekTo(0);
                movingSound.start();
                mAutoDecrement = true;
                repeatUpdateHandler.post(new RepeatUpdater());

                return true;
            }
        });

        ibCalibD.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoDecrement) {
                    mAutoDecrement = false;
                    movingSound.pause();
                }
                return false;
            }
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
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                discardValuesToInitial(new DiscardCalibInterface() {
                    @Override
                    public void onDiscardedData(Boolean finished) {
                        if (finished) {
                            connected = false;
                            finish();

                        }
                    }
                });
            }

        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //dismiss dialog
            }
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
            saveDataOverWiFi(savedIp, new SavedCalibrationInterface() {
                @Override
                public void onSavedData(Boolean saved) {
                    if (saved) {
                        finish();
                    }
                }
            });
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibMotor0:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 0;
                ibMotor0.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor0) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                String string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor1:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 1;
                ibMotor1.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor1) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor2:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 2;
                ibMotor2.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor2) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor3:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 3;
                ibMotor3.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor3) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor4:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 4;
                ibMotor4.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor4) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor5:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 5;
                ibMotor5.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor5) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor6:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 6;
                ibMotor6.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor6) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor7:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 7;
                ibMotor7.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor7) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor8:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 8;
                ibMotor8.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor8) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor9:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 9;
                ibMotor9.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor9) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor10:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 10;
                ibMotor10.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor10) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor11:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 11;
                ibMotor11.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor11) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor12:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 12;
                ibMotor12.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor12) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor13:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 13;
                ibMotor13.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor13) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor14:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 14;
                ibMotor14.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor14) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor15:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 15;
                ibMotor15.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor15) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor16:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 16;
                ibMotor16.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor16) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor17:
                ibCalibD.setEnabled(true);
                ibCalibUp.setEnabled(true);
                index = 17;
                ibMotor17.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor17) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisibility();
                string = String.valueOf(newCalibrationArray[index]);
                tvCalibValue.setText(string);
                break;
        }
    }

    private void setVisibility() {
        tvSelect.setVisibility(View.INVISIBLE);
        ivCircle.setVisibility(View.VISIBLE);
        tvCalibValue.setVisibility(View.VISIBLE);
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
                            System.out.println("BYTES ARRAY -> " + Arrays.toString(bytesArray()));
                        }
                        socket.close();

                    } catch (IOException | InterruptedException e) {
                    } finally {
                        connected = false;
                    }


                } catch (IOException e) {

                }
            }
        };
        t.start();
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

                        callback.onSavedData(true);

                    } catch (IOException | InterruptedException e) {
                    } finally {
                        connected = false;
                    }

                } catch (IOException e) {
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
//

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

    class RepeatUpdater implements Runnable {
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

    public void decrement() {
        if (!(newCalibrationArray[index] <= 0)) {
            newCalibrationArray[index]--;
            String sCalib = String.valueOf(newCalibrationArray[index]);
            tvCalibValue.setText(sCalib);
        } else {
            movingSound.pause();
            movingSoundShort.pause();
        }
    }

    public void increment() {
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
            } catch (InterruptedException e) {
                e.printStackTrace();
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



