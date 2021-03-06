package com.stemi.STEMIHexapod.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.stemi.STEMIHexapod.Constants;
import com.stemi.STEMIHexapod.R;
import com.stemi.STEMIHexapod.Utils;
import com.stemi.STEMIHexapod.helpers.SharedPreferencesHelper;
import com.stemi.STEMIHexapod.interfaces.DiscardCalibrationCallback;

import java.util.LinkedHashMap;
import java.util.Map;

import stemi.education.stemihexapod.Hexapod;
import stemi.education.stemihexapod.HexapodStatus;

/**
 * Created by Mario on 29/08/16.
 */
public class CalibrationActivity extends AppCompatActivity implements
        View.OnClickListener, HexapodStatus {

    private class Motor {
        int idx;
        ImageButton imageButton;

        Motor(int idx, ImageButton imageButton) {
            this.idx = idx;
            this.imageButton = imageButton;
        }
    }

    private Map<Integer, Motor> motorsMap;
    private int[] motorResIds = {R.id.ibMotor0, R.id.ibMotor1, R.id.ibMotor2, R.id.ibMotor3, R.id.ibMotor4,
            R.id.ibMotor5, R.id.ibMotor6, R.id.ibMotor7, R.id.ibMotor8, R.id.ibMotor9,
            R.id.ibMotor10, R.id.ibMotor11, R.id.ibMotor12, R.id.ibMotor13, R.id.ibMotor14,
            R.id.ibMotor15, R.id.ibMotor16, R.id.ibMotor17};

    private AlertDialog.Builder builder;
    private ImageView ivCircle;
    private TextView tvCalibValue, tvSelect;
    private ImageButton ibCalibUp, ibCalibD;
    private MediaPlayer movingSound, movingSoundShort;

    private byte[] calibrationValues = new byte[18];
    private byte[] changedCalibrationValues = new byte[18];

    private int index;
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    private final Handler repeatUpdateHandler = new Handler();
    private Hexapod hexapod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calibration);

        Utils.initActionBarWithTitle(CalibrationActivity.this, this, "Calibration");

        ivCircle = (ImageView) findViewById(R.id.ivCircle);
        tvCalibValue = (TextView) findViewById(R.id.tvCalibValue);
        tvSelect = (TextView) findViewById(R.id.tvSelect);
        ibCalibUp = (ImageButton) findViewById(R.id.ibCalibUp);
        ibCalibD = (ImageButton) findViewById(R.id.ibCalibDown);

        motorsMap = new LinkedHashMap<>(19);
        for (int i = 0; i < 18; i++) {
            motorsMap.put(motorResIds[i], new Motor(i, (ImageButton) findViewById(motorResIds[i])));
        }

        String savedIp = SharedPreferencesHelper.getSharedPreferencesString(this, SharedPreferencesHelper.Key.IP, null);

        tvSelect.setTypeface(Utils.getCustomTypeface(this));
        tvCalibValue.setTypeface(Utils.getCustomTypeface(this));

        movingSound = MediaPlayer.create(this, R.raw.moving_sound);
        movingSoundShort = MediaPlayer.create(this, R.raw.moving_sound_short);

        ibCalibUp.setEnabled(false);
        ibCalibD.setEnabled(false);

        builder = new AlertDialog.Builder(this);

        hexapod = new Hexapod(true);
        hexapod.setIpAddress(savedIp);
        hexapod.hexapodStatus = this;

        hexapod.connectWithCompletion(connected -> {
            if (connected) {
                try {
                    byte[] fetchedValues = hexapod.fetchDataFromHexapod();
                    if (fetchedValues != null) {
                        int n = fetchedValues.length;
                        System.arraycopy(fetchedValues, 0, calibrationValues, 0, n);
                        changedCalibrationValues = calibrationValues.clone();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        /* Calibration up listeners */
        ibCalibUp.setOnClickListener(v -> {
            increment();
            if (changedCalibrationValues[index] == 100)
                movingSoundShort.pause();
            else
                movingSoundShort.start();
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


        /* Calibration down listeners */
        ibCalibD.setOnClickListener(v -> {
            decrement();
            if (changedCalibrationValues[index] == 0)
                movingSoundShort.pause();
            else
                movingSoundShort.start();

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

    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }

    @Override
    protected void onStop() {
        super.onStop();
        hexapod.disconnect();
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        hexapod.disconnect();
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

    private void showSelectedMotor(int resId) {
        Motor m = motorsMap.get(resId);
        ibCalibD.setEnabled(true);
        ibCalibUp.setEnabled(true);
        index = m.idx;
        m.imageButton.setAlpha(1f);
        for (int key : motorsMap.keySet()) {
            if (key != resId) {
                motorsMap.get(key).imageButton.setAlpha(0f);
            }
        }
        tvSelect.setVisibility(View.INVISIBLE);
        ivCircle.setVisibility(View.VISIBLE);
        tvCalibValue.setVisibility(View.VISIBLE);
        String string = String.valueOf(changedCalibrationValues[m.idx]);
        tvCalibValue.setText(string);
    }

    @Override
    public void onClick(View view) {
        showSelectedMotor(view.getId());
    }

    private void showBackDialog() {
        builder.setTitle(R.string.warning);
        builder.setMessage(R.string.reset_to_initial);
        builder.setPositiveButton(R.string.yes, (dialog, id) -> {
            DiscardTask task = new DiscardTask(CalibrationActivity.this);
            task.execute();
        });
        builder.setNegativeButton(R.string.no, (dialog, id) -> dialog.dismiss());
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
            hexapod.writeDataToHexapod(saved -> {
                if (saved) {
                    finish();
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void connectionStatus(boolean isConnected) {
        if (!isConnected) {
            Utils.showConnectionDialog(this, CalibrationActivity.this);
        }
    }

    private class RepeatUpdater implements Runnable {
        public void run() {
            if (mAutoIncrement) {
                increment();
                repeatUpdateHandler.postDelayed(new RepeatUpdater(), Constants.REPEAT_DELAY);
            } else if (mAutoDecrement) {
                decrement();
                repeatUpdateHandler.postDelayed(new RepeatUpdater(), Constants.REPEAT_DELAY);
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

    private void discardValuesToInitial(final DiscardCalibrationCallback discardCalibrationCallback) {
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
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        discardCalibrationCallback.onDiscardedData(true);
    }

    private class DiscardTask extends AsyncTask<Void, Void, Void> {
        ProgressDialog progressDialog;

        public DiscardTask(Context context) {
            progressDialog = new ProgressDialog(context);
        }

        @Override
        protected void onPreExecute() {
            progressDialog.setMessage(getString(R.string.canceling));
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            discardValuesToInitial(finished -> {
                if (finished) {
                    finish();
                }
            });
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            progressDialog.dismiss();
        }
    }

}



