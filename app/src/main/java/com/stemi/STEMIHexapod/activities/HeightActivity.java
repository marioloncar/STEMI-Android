package com.stemi.STEMIHexapod.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.stemi.STEMIHexapod.R;

import mario.com.stemihexapod.Hexapod;

import static com.stemi.STEMIHexapod.Constants.REPEAT_DELAY;

/**
 * Created by Mario on 29/08/16.
 */
public class HeightActivity extends AppCompatActivity {

    private TextView tvHeightValue;
    private MediaPlayer movingSound, movingSoundShort;
    private SharedPreferences prefs;
    private final Handler repeatUpdateHandler = new Handler();
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    private byte height = 50;
    private Hexapod hexapod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_height);

        initActionBarWithTitle("Adjust height");

        ImageButton ibHeightUp = (ImageButton) findViewById(R.id.ibHeightUp);
        ImageButton ibHeightD = (ImageButton) findViewById(R.id.ibHeightD);
        tvHeightValue = (TextView) findViewById(R.id.tvHeightValue);

        movingSound = MediaPlayer.create(this, R.raw.moving_sound);
        movingSoundShort = MediaPlayer.create(this, R.raw.moving_sound_short);

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        String s = String.valueOf(height);
        tvHeightValue.setText(s);

        prefs = getSharedPreferences("myPref", MODE_PRIVATE);
        height = (byte) prefs.getInt("height", 0);
        tvHeightValue.setText(String.valueOf(height));

        tvHeightValue.setTypeface(tf);

        String savedIp = prefs.getString("ip", null);

        hexapod = new Hexapod();
        hexapod.setIpAddress(savedIp);
        hexapod.setHeight(height);
        hexapod.connect();

        /*** Increase height listeners ***/
        ibHeightUp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                movingSound.seekTo(0);
                movingSound.start();
                mAutoIncrement = true;
                repeatUpdateHandler.post(new RepeatUpdater());

                return true;
            }
        });

        ibHeightUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoIncrement) {
                    mAutoIncrement = false;
                    movingSound.pause();
                }
                return false;
            }
        });

        ibHeightUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
                if (height == 100)
                    movingSoundShort.pause();
                else
                    movingSoundShort.start();
            }
        });


        /*** Decrease height listeners ***/
        ibHeightD.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                movingSound.seekTo(0);
                movingSound.start();
                mAutoDecrement = true;
                repeatUpdateHandler.post(new RepeatUpdater());
                return true;
            }
        });

        ibHeightD.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoDecrement) {
                    mAutoDecrement = false;
                    movingSound.pause();
                }
                return false;
            }
        });

        ibHeightD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement();
                if (height == 0)
                    movingSoundShort.pause();
                else
                    movingSoundShort.start();
            }
        });
    }

    private void initActionBarWithTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
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

    // Handler for long click on increase/decrease value buttons
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

    // Decrements height value
    private void decrement() {
        if (!(height <= 0)) {
            height--;
            String sHeight = String.valueOf(height);
            tvHeightValue.setText(sHeight);
            prefs.edit().putInt("height", height).apply();
            hexapod.setHeight(height);
        } else
            stopSound();
    }

    // Increments height value
    private void increment() {
        if (!(height >= 100)) {
            height++;
            String sHeight = String.valueOf(height);
            tvHeightValue.setText(sHeight);
            prefs.edit().putInt("height", height).apply();
            hexapod.setHeight(height);
        } else
            stopSound();
    }

    private void stopSound() {
        movingSound.pause();
        movingSoundShort.pause();
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

    @Override
    public void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

}
