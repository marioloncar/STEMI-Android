package com.loncar.stemi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.text.style.IconMarginSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by Mario on 29/08/16.
 */
public class HeightActivity extends AppCompatActivity {

    ImageButton ibHeightUp, ibHeightD;
    TextView tvHeightValue;
    public MediaPlayer moving;

    SharedPreferences prefs;

    private Handler repeatUpdateHandler = new Handler();
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    static int REPEAT_DELAY = 50;
    public byte height = 50;
    public Boolean connected;
    public final String TAG = "HeightActivity";
    public int sleepingInterval = 200;
    private byte walk = 30;
    public byte[] slidersArray = {0, 0, 0, 50, 0, 0, 0, 0, 0};


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.height_layout);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar));
        actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>Adjust height</font>"));

        ibHeightUp = (ImageButton) findViewById(R.id.ibHeightUp);
        ibHeightD = (ImageButton) findViewById(R.id.ibHeightD);
        tvHeightValue = (TextView) findViewById(R.id.tvHeightValue);
        moving = MediaPlayer.create(getApplicationContext(), R.raw.moving_sound);

        String s = String.valueOf(height);
        tvHeightValue.setText(s);

        prefs = getSharedPreferences("myPref", MODE_PRIVATE);
        if (prefs.contains("height")) {
            height = (byte) prefs.getInt("height", 0);
            tvHeightValue.setText(String.valueOf(height));
        } else {
            height = 50;
            tvHeightValue.setText(String.valueOf(height));
        }

        /*** Increase height listeners ***/
        ibHeightUp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mAutoIncrement = true;
                repeatUpdateHandler.post(new RepeatUpdater());

                return false;
            }
        });

        ibHeightUp.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoIncrement) {
                    mAutoIncrement = false;
                }
                return false;
            }
        });

        ibHeightUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                increment();
            }
        });
        /*** Increase height listeners end ***/


        /*** Decrease height listeners ***/
        ibHeightD.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mAutoDecrement = true;
                repeatUpdateHandler.post(new RepeatUpdater());
                return false;
            }
        });

        ibHeightD.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if ((event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) && mAutoDecrement) {
                    mAutoDecrement = false;
                }
                return false;
            }
        });

        ibHeightD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                decrement();
            }
        });
        /*** Decrease height listeners end ***/

        sendCommandsOverWiFi("192.168.4.1");

    }

    public void sendCommandsOverWiFi(final String ip) {
        connected = true;

        Thread t = new Thread() {
            public void run() {
                try {
                    //connect to IP address, port 80
                    Socket socket = new Socket(ip, 80);
                    OutputStream outputStream = socket.getOutputStream();
                    CommandSender wifiSender = new CommandSender(outputStream, socket);
                    Thread thread = new Thread(wifiSender);
                    thread.start();
                } catch (IOException e) {
                    Log.d(TAG, "TCP socket error: " + e.getMessage());
                    //e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public class CommandSender implements Runnable {

        OutputStream outputStream;
        Socket socket; // Closeable -> Socket (for API 17)


        CommandSender(OutputStream outputStream, Socket socket) {
            this.outputStream = outputStream;
            this.socket = socket;
        }


        @Override
        public void run() {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Connection with robot established.");
            }
            try {
                BufferedOutputStream buffer = new BufferedOutputStream(outputStream, 30);
                while (connected) {
                    Thread.sleep(sleepingInterval);
                    buffer.write("PKT".getBytes());
                    buffer.write(0); // power
                    buffer.write(0); // angle
                    buffer.write(0); // rotation
                    buffer.write(0); // rotation mode
                    buffer.write(0); // orientation mode
                    buffer.write(1); // standby mode
                    buffer.write(0); // accelerometerX
                    buffer.write(0); // accelerometerY
                    buffer.write(height);
                    buffer.write(walk);
                    buffer.write(slidersArray);
                    buffer.flush();
                }
                socket.close();
                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Connection with robot is closed.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Socket IOException.");
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "Socket InterruptedException.");
            } finally {
                connected = false;
            }
        }
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
        if (height > 0) {
            height--;
            String sHeight = String.valueOf(height);
            tvHeightValue.setText(sHeight);
            prefs.edit().putInt("height", height).apply();
        }
    }

    public void increment() {
        if (height < 100) {
            height++;
            String sHeight = String.valueOf(height);
            tvHeightValue.setText(sHeight);
            prefs.edit().putInt("height", height).apply();
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
}
