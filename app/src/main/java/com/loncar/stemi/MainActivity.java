package com.loncar.stemi;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    ImageButton ibStandby, ibMovement, ibRotation, ibOrientation, ibHeight, ibCalibration, ibWalkingStyle;
    View vOverlay;
    public final String TAG = "MainActivity";
    Typeface tf;
    RelativeLayout lay;
    ImageView longToastBck;

    SharedPreferences iPAddress;

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
        //Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_main);

        ibStandby = (ImageButton) findViewById(R.id.ibStandby);
        vOverlay = findViewById(R.id.vOverlay);
        ibMovement = (ImageButton) findViewById(R.id.ibMovement);
        ibRotation = (ImageButton) findViewById(R.id.ibRotation);
        ibOrientation = (ImageButton) findViewById(R.id.ibOrientation);
        ibHeight = (ImageButton) findViewById(R.id.ibHeight);
        ibCalibration = (ImageButton) findViewById(R.id.ibCalibration);
        ibWalkingStyle = (ImageButton) findViewById(R.id.ibWalkingStyle);
        lay = (RelativeLayout) findViewById(R.id.longToast);
        longToastBck = (ImageView) findViewById(R.id.ivToastLongBck);

        tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ibMovement.setSelected(true);
        ibStandby.setSelected(true);

        WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
        IP = wifiManager.getDhcpInfo().gateway;




//        final SharedPreferences manualIP = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        String ipAddr = manualIP.getString("address", "");

        //connect to IP address
        String ipAddr =
                String.format("%d.%d.%d.%d",
                        (IP & 0xff),
                        (IP >> 8 & 0xff),
                        (IP >> 16 & 0xff),
                        (IP >> 24 & 0xff));


        sendCommandsOverWiFi(ipAddr);

        /**** OnLongClick Listeners ****/
        ibMovement.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Movement", "Allows linear movements (left, right, back, forward). Tap to enable.");
                return true;

            }
        });

        ibRotation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Rotation", "Allows rotational movements with fixed stemi. Tap to enable.");
                return true;
            }
        });

        ibOrientation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Orientation", "Combination of movement and rotation. Tap to enable.");
                return true;
            }
        });

        ibHeight.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Height", "Tap to manually adjust the height of STEMI's body.");
                return true;
            }
        });

        ibCalibration.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Calibration", "Tap to manually adjust the position of each joint on each leg.");
                return true;
            }
        });

        ibWalkingStyle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Walk style", "Switch between different walk styes.");
                return true;
            }
        });


        /**** OnTouch Listeners ****/
        ibMovement.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hideToast();
                }
                return false;
            }
        });

        ibRotation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hideToast();
                }
                return false;
            }
        });

        ibOrientation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hideToast();
                }
                return false;
            }
        });

        ibHeight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hideToast();
                }
                return false;
            }
        });

        ibCalibration.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hideToast();
                }
                return false;
            }
        });

        ibWalkingStyle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    hideToast();
                }
                return false;
            }
        });
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
                        buffer.write(ibStandby.isSelected() ? 1 : 0);
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibStandby:
                if (ibStandby.isSelected()) {
                    ibStandby.setSelected(false);
                } else {
                    ibStandby.setSelected(true);
                }
                break;

            case R.id.bMenu:
                if (Menu.bMenu.isSelected()) {
                    Menu.closeMenu();
                    Menu.bMenu.setSelected(false);
                    vOverlay.setVisibility(View.INVISIBLE);
                } else {
                    Menu.openMenu();
                    Menu.bMenu.setSelected(true);
                    vOverlay.setVisibility(View.VISIBLE);
                }
                break;
            case R.id.vOverlay:
                Menu.closeMenu();
                Menu.bMenu.setSelected(false);
                vOverlay.setVisibility(View.INVISIBLE);
                break;

            case R.id.ibMovement:
                ibMovement.setSelected(true);
                ibRotation.setSelected(false);
                ibOrientation.setSelected(false);
                Menu.closeMenu();
                Menu.bMenu.setSelected(false);
                vOverlay.setVisibility(View.INVISIBLE);
                showShortToast("MOVEMENT ENABLED");
                break;

            case R.id.ibRotation:
                ibRotation.setSelected(true);
                ibMovement.setSelected(false);
                ibOrientation.setSelected(false);
                Menu.closeMenu();
                Menu.bMenu.setSelected(false);
                vOverlay.setVisibility(View.INVISIBLE);
                showShortToast("ROTATION ENABLED");
                break;

            case R.id.ibOrientation:
                ibOrientation.setSelected(true);
                ibMovement.setSelected(false);
                ibRotation.setSelected(false);
                Menu.closeMenu();
                Menu.bMenu.setSelected(false);
                vOverlay.setVisibility(View.INVISIBLE);
                showShortToast("ORIENTATION ENABLED");
                break;
            case R.id.ibHeight:
                Intent openHeight = new Intent(getApplicationContext(), HeightActivity.class);
                startActivity(openHeight);
                break;
            case R.id.ibCalibration:
                Intent openCalibration = new Intent(getApplicationContext(), CalibrationActivity.class);
                startActivity(openCalibration);
                break;
            case R.id.ibWalkingStyle:
                Intent openWalkstyle = new Intent(getApplicationContext(), WalkstyleActivity.class);
                startActivity(openWalkstyle);
                break;
            case R.id.ibSettings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);

                break;

            default:
                Log.d(TAG, "Default");
        }
    }


    // swipe to show notification bar and soft keys
    @TargetApi(Build.VERSION_CODES.KITKAT)
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

    private void showShortToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View rlToastShort = inflater.inflate(R.layout.toast_short,
                (ViewGroup) findViewById(R.id.rlToastShortRoot));

        TextView tvEnabled = (TextView) rlToastShort.findViewById(R.id.tvEnabled);

        int offset = Math.round(30 * getApplicationContext().getResources().getDisplayMetrics().density);

        tvEnabled.setText(message);
        tvEnabled.setTypeface(tf);
        Toast toast = new Toast(getApplicationContext());
        toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, offset, 0);
        toast.setDuration(Toast.LENGTH_SHORT);
        toast.setView(rlToastShort);
        toast.show();
    }

    private void showLongToast(String title, String message) {
        TextView tvTitle = (TextView) findViewById(R.id.tvTitle);
        TextView tvDesc = (TextView) findViewById(R.id.tvDesc);

        Animation show = new AlphaAnimation(0, 1);
        show.setDuration(150);
        longToastBck.setAnimation(show);
        lay.setVisibility(View.VISIBLE);

        tvTitle.setText(title);
        tvDesc.setText(message);

        tvTitle.setTypeface(tf);
        tvDesc.setTypeface(tf);

    }

    private void hideToast() {
        Animation hide = new AlphaAnimation(1, 0);
        hide.setDuration(200);
        longToastBck.setAnimation(hide);
        lay.setVisibility(View.INVISIBLE);
    }
}
