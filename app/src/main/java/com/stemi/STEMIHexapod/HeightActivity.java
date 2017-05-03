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
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

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
    private final static int REPEAT_DELAY = 50;
    private byte height = 50;
    private Boolean connected;
    private final static int SLEEPING_INTERVAL = 100;
    private byte[] slidersArray = {0, 0, 0, 50, 0, 0, 0, 0, 0};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.height_layout);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar));
        actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>Adjust height</font>"));

        @SuppressLint("PrivateResource")
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.abc_ic_ab_back_material, null);
        assert upArrow != null;
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.highlightColor), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(upArrow);

        movingSound = MediaPlayer.create(this, R.raw.moving_sound);
        movingSoundShort = MediaPlayer.create(this, R.raw.moving_sound_short);

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        ImageButton ibHeightUp = (ImageButton) findViewById(R.id.ibHeightUp);
        ImageButton ibHeightD = (ImageButton) findViewById(R.id.ibHeightD);
        tvHeightValue = (TextView) findViewById(R.id.tvHeightValue);
        String s = String.valueOf(height);
        tvHeightValue.setText(s);

        prefs = getSharedPreferences("myPref", MODE_PRIVATE);
        height = (byte) prefs.getInt("height", 0);
        tvHeightValue.setText(String.valueOf(height));

        tvHeightValue.setTypeface(tf);

        String savedIp = prefs.getString("ip", null);

        /*** Increase height listeners ***/
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

            if (height == 100) {
                movingSoundShort.pause();
            } else {
                movingSoundShort.start();
            }
        });

        /*** Increase height listeners END ***/


        /*** Decrease height listeners ***/
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
            if (height == 0) {
                movingSoundShort.pause();
            } else {
                movingSoundShort.start();
            }
        });
        /*** Decrease height listeners END ***/

        sendCommandsOverWiFi(savedIp);

    }


    private void sendCommandsOverWiFi(final String ip) {
        connected = true;

        Thread t = new Thread() {
            public void run() {
                try {
                    Socket socket = new Socket(ip, 80);
                    OutputStream outputStream = socket.getOutputStream();

                    try {
                        BufferedOutputStream buffer = new BufferedOutputStream(outputStream, 30);
                        while (connected) {
                            Thread.sleep(SLEEPING_INTERVAL);
                            buffer.write(bytesArray());
                            buffer.flush();

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
        if (!(height <= 0)) {
            height--;
            String sHeight = String.valueOf(height);
            tvHeightValue.setText(sHeight);
            prefs.edit().putInt("height", height).apply();
        } else {
            movingSound.pause();
            movingSoundShort.pause();
        }
    }

    private void increment() {
        if (!(height >= 100)) {
            height++;
            String sHeight = String.valueOf(height);
            tvHeightValue.setText(sHeight);
            prefs.edit().putInt("height", height).apply();
        } else {
            movingSound.pause();
            movingSoundShort.pause();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        connected = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        connected = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        connected = false;
    }

    private byte[] bytesArray() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
        };
        try {
            outputStream.write("PKT".getBytes());
            outputStream.write(0); // power
            outputStream.write(0); // angle
            outputStream.write(0); // rotation
            outputStream.write(0); // rotation mode
            outputStream.write(0); // orientation mode
            outputStream.write(1); // standby mode
            outputStream.write(0); // accelerometerX
            outputStream.write(0); // accelerometerY
            outputStream.write(height);
            byte walk = 30;
            outputStream.write(walk);
            outputStream.write(slidersArray);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }
}
