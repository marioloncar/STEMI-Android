package com.stemi.STEMIHexapod.activities;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stemi.STEMIHexapod.Constants;
import com.stemi.STEMIHexapod.R;
import com.stemi.STEMIHexapod.Utils;
import com.stemi.STEMIHexapod.helpers.SharedPreferencesHelper;

import stemi.education.stemihexapod.Hexapod;

/**
 * Created by Mario on 29/08/16.
 */
public class HeightActivity extends AppCompatActivity {

    private TextView tvHeightValue;
    private MediaPlayer movingSound, movingSoundShort;
    private final Handler repeatUpdateHandler = new Handler();

    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    private byte height = 50;

    private Hexapod hexapod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height);

        Utils.initActionBarWithTitle(HeightActivity.this, this, "Adjust height");

        ImageButton ibHeightUp = (ImageButton) findViewById(R.id.ibHeightUp);
        ImageButton ibHeightD = (ImageButton) findViewById(R.id.ibHeightD);
        tvHeightValue = (TextView) findViewById(R.id.tvHeightValue);

        movingSound = MediaPlayer.create(this, R.raw.moving_sound);
        movingSoundShort = MediaPlayer.create(this, R.raw.moving_sound_short);

        String s = String.valueOf(height);
        tvHeightValue.setText(s);

        height = (byte) SharedPreferencesHelper.getSharedPreferencesInt(this, SharedPreferencesHelper.Key.HEIGHT, 50);
        String savedIp = SharedPreferencesHelper.getSharedPreferencesString(this, SharedPreferencesHelper.Key.IP, null);

        tvHeightValue.setText(String.valueOf(height));
        tvHeightValue.setTypeface(Utils.getCustomTypeface(this));


        hexapod = new Hexapod();
        hexapod.setIpAddress(savedIp);
        hexapod.setHeight(height);
        hexapod.connect();


        /* Increase height listeners */
        ibHeightUp.setOnLongClickListener(v -> {
            movingSound.seekTo(0);
            movingSound.start();
            mAutoIncrement = true;
            repeatUpdateHandler.post(new RepeatUpdater());

            return true;
        });

        ibHeightUp.setOnTouchListener((v, event) -> {
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoIncrement) {
                mAutoIncrement = false;
                movingSound.pause();
            }
            return false;
        });

        ibHeightUp.setOnClickListener(v -> {
            increment();
            if (height == 100)
                movingSoundShort.pause();
            else
                movingSoundShort.start();
        });


        /* Decrease height listeners */
        ibHeightD.setOnLongClickListener(v -> {
            movingSound.seekTo(0);
            movingSound.start();
            mAutoDecrement = true;
            repeatUpdateHandler.post(new RepeatUpdater());
            return true;
        });

        ibHeightD.setOnTouchListener((v, event) -> {
            if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoDecrement) {
                mAutoDecrement = false;
                movingSound.pause();
            }
            return false;
        });

        ibHeightD.setOnClickListener(v -> {
            decrement();
            if (height == 0)
                movingSoundShort.pause();
            else
                movingSoundShort.start();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hexapod.disconnect();
    }

    @Override
    public void onStop() {
        super.onStop();
        hexapod.disconnect();
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
        if (!(height <= 0)) {
            height--;
            String sHeight = String.valueOf(height);
            tvHeightValue.setText(sHeight);
            SharedPreferencesHelper.putSharedPreferencesInt(this, SharedPreferencesHelper.Key.HEIGHT, height);
            hexapod.setHeight(height);
        } else
            stopSound();
    }

    private void increment() {
        if (!(height >= 100)) {
            height++;
            String sHeight = String.valueOf(height);
            tvHeightValue.setText(sHeight);
            SharedPreferencesHelper.putSharedPreferencesInt(this, SharedPreferencesHelper.Key.HEIGHT, height);
            hexapod.setHeight(height);
        } else
            stopSound();
    }

    private void stopSound() {
        movingSound.pause();
        movingSoundShort.pause();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }


}
