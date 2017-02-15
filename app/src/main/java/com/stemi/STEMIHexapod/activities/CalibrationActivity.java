package com.stemi.STEMIHexapod.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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

import com.stemi.STEMIHexapod.interfaces.DiscardCalibrationCallback;
import com.stemi.STEMIHexapod.R;

import java.util.Arrays;

import mario.com.stemihexapod.ConnectingCompleteCallback;
import mario.com.stemihexapod.Hexapod;
import mario.com.stemihexapod.SavedCalibrationCallback;

import static com.stemi.STEMIHexapod.Constants.REPEAT_DELAY;

/**
 * Created by Mario on 29/08/16.
 */
public class CalibrationActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton ibMotor0, ibMotor1, ibMotor2, ibMotor3, ibMotor4, ibMotor5, ibMotor6, ibMotor7,
            ibMotor8, ibMotor9, ibMotor10, ibMotor11, ibMotor12, ibMotor13, ibMotor14, ibMotor15, ibMotor16,
            ibMotor17, ibCalibUp, ibCalibD;
    private ImageButton[] motors;
    private AlertDialog.Builder builder;
    private ImageView ivCircle;
    private TextView tvCalibValue, tvSelect;
    private MediaPlayer movingSound, movingSoundShort;
    private byte[] calibrationValues;
    private byte[] changedCalibrationValues;
    private int index;
    private final Handler repeatUpdateHandler = new Handler();
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    private String savedIp;
    private Hexapod hexapod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        initActionBarWithTitle("Calibration");

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
                ibMotor7, ibMotor8, ibMotor9, ibMotor10, ibMotor11, ibMotor12, ibMotor13, ibMotor14, ibMotor15,
                ibMotor16, ibMotor17};

        builder = new AlertDialog.Builder(this);

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        SharedPreferences prefs = getSharedPreferences("myPref", MODE_PRIVATE);
        savedIp = prefs.getString("ip", null);

        tvSelect.setTypeface(tf);
        tvCalibValue.setTypeface(tf);

        movingSound = MediaPlayer.create(this, R.raw.moving_sound);
        movingSoundShort = MediaPlayer.create(this, R.raw.moving_sound_short);

        setButtonsEnabled(false, false);

        calibrationValues = new byte[18];
        changedCalibrationValues = new byte[18];

        hexapod = new Hexapod(true);
        hexapod.setIpAddress(savedIp);

        hexapod.connectWithCompletion(new ConnectingCompleteCallback() {
            @Override
            public void onConnectingComplete(boolean connected) {
                if (connected) {
                    try {
                        calibrationValues = hexapod.fetchDataFromHexapod();
                        if (calibrationValues != null) {
                            System.arraycopy(calibrationValues, 0, changedCalibrationValues, 0, 18);
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });


        /*** Calibration up listeners ***/
        ibCalibUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
                if (changedCalibrationValues[index] == 100)
                    movingSoundShort.pause();
                else
                    movingSoundShort.start();
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


        /*** Calibration down listeners ***/
        ibCalibD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement();
                if (changedCalibrationValues[index] == 0)
                    movingSoundShort.pause();
                else
                    movingSoundShort.start();

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
                discardValuesToInitial(new DiscardCalibrationCallback() {
                    @Override
                    public void onDiscardedData(Boolean finished) {
                        if (finished) {
                            finish();
                        }
                    }
                });
            }

        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
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
        if (id == R.id.save) {
            writeData();
        }
        return super.onOptionsItemSelected(item);

    }

    private void writeData() {
        try {
            hexapod.writeDataToHexapod(new SavedCalibrationCallback() {
                @Override
                public void onSavedData(Boolean saved) {
                    if (saved) {
                        finish();
                    }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibMotor0:
                setButtonsEnabled(true, true);
                index = 0;
                ibMotor0.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor0)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                String string = String.valueOf(changedCalibrationValues[index]);
                tvCalibValue.setText(string);
                break;
            case R.id.ibMotor1:
                setButtonsEnabled(true, true);
                index = 1;
                ibMotor1.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor1)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor2:
                setButtonsEnabled(true, true);
                index = 2;
                ibMotor2.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor2)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor3:
                setButtonsEnabled(true, true);
                index = 3;
                ibMotor3.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor3)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor4:
                setButtonsEnabled(true, true);
                index = 4;
                ibMotor4.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor4)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor5:
                setButtonsEnabled(true, true);
                index = 5;
                ibMotor5.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor5)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor6:
                setButtonsEnabled(true, true);
                index = 6;
                ibMotor6.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor6)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor7:
                setButtonsEnabled(true, true);
                index = 7;
                ibMotor7.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor7)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor8:
                setButtonsEnabled(true, true);
                index = 8;
                ibMotor8.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor8)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor9:
                setButtonsEnabled(true, true);
                index = 9;
                ibMotor9.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor9)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor10:
                setButtonsEnabled(true, true);
                index = 10;
                ibMotor10.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor10)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor11:
                setButtonsEnabled(true, true);
                index = 11;
                ibMotor11.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor11)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor12:
                setButtonsEnabled(true, true);
                index = 12;
                ibMotor12.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor12)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor13:
                setButtonsEnabled(true, true);
                index = 13;
                ibMotor13.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor13)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor14:
                setButtonsEnabled(true, true);
                index = 14;
                ibMotor14.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor14)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor15:
                setButtonsEnabled(true, true);
                index = 15;
                ibMotor15.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor15)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor16:
                setButtonsEnabled(true, true);
                index = 16;
                ibMotor16.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor16)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
            case R.id.ibMotor17:
                setButtonsEnabled(true, true);
                index = 17;
                ibMotor17.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor17)
                        motors[i].setAlpha(0f);
                }
                setVisibility();
                setCalibrationValueText();
                break;
        }
    }

    private void setCalibrationValueText() {
        String calibValue;
        calibValue = String.valueOf(changedCalibrationValues[index]);
        tvCalibValue.setText(calibValue);
    }

    private void setButtonsEnabled(boolean up, boolean down) {
        ibCalibUp.setEnabled(up);
        ibCalibD.setEnabled(down);

    }

    private void setVisibility() {
        tvSelect.setVisibility(View.INVISIBLE);
        ivCircle.setVisibility(View.VISIBLE);
        tvCalibValue.setVisibility(View.VISIBLE);
    }


    @Override
    protected void onStop() {
        super.onStop();
        hexapod.disconnect();
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
        if (!(changedCalibrationValues[index] <= 0)) {
            changedCalibrationValues[index]--;
            String sCalib = String.valueOf(changedCalibrationValues[index]);
            tvCalibValue.setText(sCalib);
            hexapod.decreaseCalibrationValueAtIndex(index);
        } else {
            movingSound.pause();
            movingSoundShort.pause();
        }
    }

    private void increment() {
        if (!(changedCalibrationValues[index] >= 100)) {
            changedCalibrationValues[index]++;
            String sCalib = String.valueOf(changedCalibrationValues[index]);
            tvCalibValue.setText(sCalib);
            hexapod.increaseCalibrationValueAtIndex(index);
        } else {
            movingSound.pause();
            movingSoundShort.pause();
        }
    }

    private void discardValuesToInitial(DiscardCalibrationCallback discardCalibrationCallback) {
        byte[] calculatingNumbers = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        for (int i = 0; i <= 10; i++) {
            int n = calibrationValues.length;
            for (int j = 0; j < n; j++) {
                if (i == 0) {
                    byte calc = (byte) (Math.abs(calibrationValues[j] - changedCalibrationValues[j]) / 10);
                    calculatingNumbers[j] = calc;
                }

                if (i < 10) {
                    if (changedCalibrationValues[j] < calibrationValues[j]) {
                        changedCalibrationValues[j] += calculatingNumbers[j];
                        try {
                            hexapod.setCalibrationValue(changedCalibrationValues[j], j);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }

                    } else if (changedCalibrationValues[j] > calibrationValues[j]) {
                        changedCalibrationValues[j] -= calculatingNumbers[j];
                        try {
                            hexapod.setCalibrationValue(changedCalibrationValues[j], j);
                        } catch (IndexOutOfBoundsException e) {
                            e.printStackTrace();
                        }
                    }

                } else {
                    changedCalibrationValues[j] = calibrationValues[j];
                    try {
                        hexapod.setCalibrationValue(calibrationValues[j], j);
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
        discardCalibrationCallback.onDiscardedData(true);
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

}



