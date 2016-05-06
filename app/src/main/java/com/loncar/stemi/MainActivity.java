package com.loncar.stemi;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;

import com.crashlytics.android.Crashlytics;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener{

    ImageButton ibMovement, ibRotation, ibOrientation, ibHeight, ibSettings;

    public final String TAG = "MainActivity";

    public Boolean connected = false;
    public Boolean calibrationMode = false;
    public int sleepingInterval = 200;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private byte onOff = 1;
    private byte accelerometerX = 0;
    private byte accelerometerY = 0;
    public byte[] slidersArray = {50, 25, 0, 0, 0, 50, 0, 0, 0, 0, 0};

    public Queue<byte[]> calibrationQueue;

    private int IP;

    public MainActivity() {
        calibrationQueue = new LinkedList<>();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        ibMovement = (ImageButton) findViewById(R.id.ibMovement);
        ibRotation = (ImageButton) findViewById(R.id.ibRotation);
        ibOrientation = (ImageButton) findViewById(R.id.ibOrientation);
        ibHeight = (ImageButton) findViewById(R.id.ibHeight);
        ibSettings = (ImageButton) findViewById(R.id.ibSettings);

        ibMovement.setSelected(true);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        IP = wifiManager.getDhcpInfo().gateway;

        //connect to IP address
        String ipAddr =
                String.format("%d.%d.%d.%d",
                        (IP & 0xff),
                        (IP >> 8 & 0xff),
                        (IP >> 16 & 0xff),
                        (IP >> 24 & 0xff));

        sendCommandsOverWiFi(ipAddr);
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

    private class CommandSender implements Runnable {

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

                    //send bytes
                    if (!calibrationMode) {
                        JoystickL joyL = (JoystickL) findViewById(R.id.joyL);
                        JoystickR joyR = (JoystickR) findViewById(R.id.joyR);

                        buffer.write("PKT".getBytes());
                        buffer.write(joyL.power);
                        buffer.write(joyL.angle);
                        buffer.write(joyR.rotation);
                        buffer.write(ibRotation.isSelected() ? 1 : 0); //static tilt
                        buffer.write(ibOrientation.isSelected() ? 1 : 0); //moving tilt
                        buffer.write(onOff);
                        buffer.write(accelerometerX);
                        buffer.write(accelerometerY);
                        buffer.write(slidersArray);
                        buffer.flush();
                    } else {
                        if (!calibrationQueue.isEmpty()) {
                            buffer.write("LIN".getBytes());
                            buffer.write(calibrationQueue.remove());
                            buffer.flush();
                        }
                    }
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
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            if (event.values[0] < -4) accelerometerX = -40;
            else if (event.values[0] > 4) accelerometerX = 40;
            else accelerometerX = (byte) (int) (event.values[0] * 10);

            if (event.values[1] < -4) accelerometerY = -40;
            else if (event.values[1] > 4) accelerometerY = 40;
            else accelerometerY = (byte) (int) (event.values[1] * 10);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }


    // buttons
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
                Log.d(TAG, "Default");
        }
    }

    // swipe to show notification bar and soft keys
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


    // back button
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
        sensorManager.unregisterListener(this);
        finish();
    }

    @Override
    public void onStop() {
        super.onStop();

        connected = false;

    }

    @Override
    protected void onResume() {
        super.onResume();

        connected = true;
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);
    }
}
