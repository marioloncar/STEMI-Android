package com.loncar.stemi;

import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton ibMovement, ibRotation, ibOrientation, ibHeight, ibSettings;

    private int IP;

    public final String TAG = "MainActivity";

    public Boolean connected = false;
    public Boolean calibrationMode = false;
    public int sleepingInterval = 200; // interval between packets [ms]

    private byte staticTilt = 0;
    private byte movingTilt = 0;
    private byte onOff = 1;
    private byte accelerometer_x = 0;
    private byte accelerometer_y = 0;
    public byte[] sliders_array = {50, 25, 0, 0, 0, 50, 0, 0, 0, 0, 0};

    // bytes of the LIN (linearization) packets
    public Queue<byte[]> calibrationQueue;

    public MainActivity() {
        calibrationQueue = new LinkedList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        ibMovement = (ImageButton) findViewById(R.id.ibMovement);
        ibRotation = (ImageButton) findViewById(R.id.ibRotation);
        ibOrientation = (ImageButton) findViewById(R.id.ibOrientation);
        ibHeight = (ImageButton) findViewById(R.id.ibHeight);
        ibSettings = (ImageButton) findViewById(R.id.ibSettings);

        ibMovement.setSelected(true);


        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        IP = wifiManager.getDhcpInfo().gateway;

        String ipStr =
                String.format("%d.%d.%d.%d",
                        (IP & 0xff),
                        (IP >> 8 & 0xff),
                        (IP >> 16 & 0xff),
                        (IP >> 24 & 0xff));

        startSendingCommandsOverWiFi(ipStr);
    }

    public void startSendingCommandsOverWiFi(final String ip) {
        connected = true;

        Thread t = new Thread() {
            public void run() {
                try {
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


    private class CommandSender implements Runnable {

        OutputStream outputStream;
        Closeable socket;

        CommandSender(OutputStream outputStream, Closeable socket) {
            this.outputStream = outputStream;
            this.socket = socket;
        }

        @Override
        public void run() {
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Connection with robot established.");
            }
            try {
                BufferedOutputStream buffOutStream = new BufferedOutputStream(outputStream, 30);
                while (connected) {
                    Thread.sleep(sleepingInterval);

                    if (!calibrationMode) {
                        Joystick_L joyL = (Joystick_L) findViewById(R.id.joyL);
                        Joystick_R joyR = (Joystick_R) findViewById(R.id.joyR);

                        buffOutStream.write("PKT".getBytes());
                        buffOutStream.write(joyL.power);
                        buffOutStream.write(joyL.angle);
                        buffOutStream.write(joyR.rotation);
                        buffOutStream.write(staticTilt);
                        buffOutStream.write(movingTilt);
                        buffOutStream.write(onOff);
                        buffOutStream.write(accelerometer_x);
                        buffOutStream.write(accelerometer_y);
                        buffOutStream.write(sliders_array);
                        buffOutStream.flush();
                    } else {
                        if (!calibrationQueue.isEmpty()) {
                            buffOutStream.write("LIN".getBytes());
                            buffOutStream.write(calibrationQueue.remove());
                            buffOutStream.flush();
                        }
                    }
                }

                socket.close();

                if (BuildConfig.DEBUG) {
                    Log.d(TAG, "Connection with robot closed.");
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Socket IOExceptopn.");
            } catch (InterruptedException e) {
                e.printStackTrace();
                Log.e(TAG, "Socket InterruptedException.");
            } finally {
                connected = false;
            }
        }
    }


    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.ibMovement:
                ibMovement.setSelected(true);
                ibRotation.setSelected(false);
                ibOrientation.setSelected(false);

                break;
            case R.id.ibRotation:
                ibRotation.setSelected(true);
                ibMovement.setSelected(false);
                ibOrientation.setSelected(false);

                break;
            case R.id.ibOrientation:
                ibOrientation.setSelected(true);
                ibMovement.setSelected(false);
                ibRotation.setSelected(false);

                break;
            case R.id.ibHeight:
                if (ibHeight.isSelected()) {
                    ibHeight.setSelected(false);
                } else {
                    ibHeight.setSelected(true);
                }
                break;
            case R.id.ibSettings:

                break;
            default:
                System.out.println("Error!");
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            connected = false;
            finish();

        }
        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        connected = false;
        finish();

    }

    @Override
    public void onStop() {
        super.onStop();

        connected = false;

    }

}
