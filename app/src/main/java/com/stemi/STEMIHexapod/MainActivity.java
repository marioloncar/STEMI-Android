package com.stemi.STEMIHexapod;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import static com.stemi.STEMIHexapod.Menu.bMenu;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener {

    private ImageButton ibStandby, ibMovement, ibRotation, ibOrientation;
    private View vOverlay;
    private Typeface tf;
    private RelativeLayout lay;
    private ImageView longToastBck;
    private Boolean connected;
    private final static int SLEEPING_INTERVAL = 100;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private byte accelerometerX = 0;
    private byte accelerometerY = 0;
    private byte heightPref = 50;
    private byte walk = 30;
    public byte[] slidersArray = {0, 0, 0, 50, 0, 0, 0, 0, 0};

    private AlertDialog.Builder builder;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        ibStandby = (ImageButton) findViewById(R.id.ibStandby);
        vOverlay = findViewById(R.id.vOverlay);
        ibMovement = (ImageButton) findViewById(R.id.ibMovement);
        ibRotation = (ImageButton) findViewById(R.id.ibRotation);
        ibOrientation = (ImageButton) findViewById(R.id.ibOrientation);
        ImageButton ibHeight = (ImageButton) findViewById(R.id.ibHeight);
        ImageButton ibCalibration = (ImageButton) findViewById(R.id.ibCalibration);
        ImageButton ibWalkingStyle = (ImageButton) findViewById(R.id.ibWalkingStyle);
        lay = (RelativeLayout) findViewById(R.id.longToast);
        longToastBck = (ImageView) findViewById(R.id.ivToastLongBck);

        tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        prefs = getSharedPreferences("myPref", MODE_PRIVATE);


        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ibMovement.setSelected(true);
        ibStandby.setSelected(true);

        builder = new AlertDialog.Builder(this);


        /**** OnLongClick Listeners ****/
        ibMovement.setOnLongClickListener(v -> {
            showLongToast("Movement", getString(R.string.movement_hint));
            return true;

        });

        ibRotation.setOnLongClickListener(v -> {
            showLongToast("Rotation", getString(R.string.rotation_hint));
            return true;
        });

        ibOrientation.setOnLongClickListener(v -> {
            showLongToast("Orientation", getString(R.string.orientation_hint));
            return true;
        });

        ibHeight.setOnLongClickListener(v -> {
            showLongToast("Height", getString(R.string.height_hint));
            return true;
        });

        ibCalibration.setOnLongClickListener(v -> {
            showLongToast("Calibration", getString(R.string.calibration_hint));
            return true;
        });

        ibWalkingStyle.setOnLongClickListener(v -> {
            showLongToast("Walk style", getString(R.string.walkstyle_hint));
            return true;
        });


        /**** OnTouch Listeners ****/
        ibMovement.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                hideToast();
            }
            return false;
        });

        ibRotation.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                hideToast();
            }
            return false;
        });

        ibOrientation.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                hideToast();
            }
            return false;
        });

        ibHeight.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                hideToast();
            }
            return false;
        });

        ibCalibration.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                hideToast();
            }
            return false;
        });

        ibWalkingStyle.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                hideToast();
            }
            return false;
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

                    try {
                        BufferedOutputStream buffer = new BufferedOutputStream(outputStream, 30);
                        while (connected) {
                            Thread.sleep(SLEEPING_INTERVAL);
                            buffer.write(bytesArray());
                            buffer.flush();
                        }
                        socket.close();

                    } catch (IOException | InterruptedException e) {
                        showConnectionDialog();
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
                if (bMenu.isSelected()) {
                    closeMenu();
                } else {
                    openMenu();
                }
                break;
            case R.id.vOverlay:
                closeMenu();
                break;

            case R.id.ibMovement:
                if (!ibMovement.isSelected()) {
                    showShortToast("MOVEMENT ENABLED");
                }
                ibMovement.setSelected(true);
                ibRotation.setSelected(false);
                ibOrientation.setSelected(false);
                closeMenu();
                break;

            case R.id.ibRotation:
                if (!ibRotation.isSelected()) {
                    showShortToast("ROTATION ENABLED");
                }
                ibRotation.setSelected(true);
                ibMovement.setSelected(false);
                ibOrientation.setSelected(false);
                closeMenu();
                break;

            case R.id.ibOrientation:
                if (!ibOrientation.isSelected()) {
                    showShortToast("ORIENTATION ENABLED");
                }
                ibOrientation.setSelected(true);
                ibMovement.setSelected(false);
                ibRotation.setSelected(false);
                closeMenu();
                break;
            case R.id.ibHeight:
                closeMenu();
                Intent intent = new Intent(MainActivity.this, HeightActivity.class);
                startActivity(intent);

                break;
            case R.id.ibCalibration:
                closeMenu();
                Intent intent1 = new Intent(MainActivity.this, CalibrationActivity.class);
                startActivity(intent1);

                break;
            case R.id.ibWalkingStyle:
                closeMenu();
                Intent intent2 = new Intent(MainActivity.this, WalkstyleActivity.class);
                startActivity(intent2);

                break;
            case R.id.ibSettings:
                closeMenu();
                Intent intent3 = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent3);

                break;
        }
    }

    private void openMenu() {
        Menu.openMenu();
        bMenu.setSelected(true);
        vOverlay.setVisibility(View.VISIBLE);
    }

    private void closeMenu() {
        Menu.closeMenu();
        bMenu.setSelected(false);
        vOverlay.setVisibility(View.INVISIBLE);
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


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        connected = false;
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        connected = false;
        sensorManager.unregisterListener(this);
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
        String savedIp = prefs.getString("ip", null);
        heightPref = (byte) prefs.getInt("height", 0);
        walk = (byte) prefs.getInt("walk", 30);
        sendCommandsOverWiFi(savedIp);

        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
        show.setDuration(200);
        longToastBck.startAnimation(show);
        lay.setVisibility(View.VISIBLE);

        tvTitle.setText(title);
        tvDesc.setText(message);

        tvTitle.setTypeface(tf);
        tvDesc.setTypeface(tf);

    }

    private void hideToast() {
        Animation hide = new AlphaAnimation(1, 0);
        hide.setDuration(200);
        longToastBck.startAnimation(hide);
        lay.setVisibility(View.INVISIBLE);
    }

    private byte[] bytesArray() {

        JoystickL joyL = (JoystickL) findViewById(R.id.joyL);
        JoystickR joyR = (JoystickR) findViewById(R.id.joyR);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
        };
        try {
            outputStream.write("PKT".getBytes());
            outputStream.write(joyL.power);
            outputStream.write(joyL.angle);
            outputStream.write(joyR.rotation);
            outputStream.write(ibRotation.isSelected() ? 1 : 0);
            outputStream.write(ibOrientation.isSelected() ? 1 : 0);
            outputStream.write(ibStandby.isSelected() ? 1 : 0);
            outputStream.write(accelerometerX);
            outputStream.write(accelerometerY);
            outputStream.write(heightPref);
            outputStream.write(walk);
            outputStream.write(slidersArray);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }

    private void showConnectionDialog() {
        runOnUiThread(() -> {
            builder.setCancelable(false);
            builder.setTitle("Connection lost");
            builder.setMessage("Please check connection with your STEMI and try again.");
            builder.setPositiveButton("Ok", (dialog, id) -> {
                Intent i = new Intent(MainActivity.this, ConnectingActivity.class);
                startActivity(i);
                finish();
            });
            builder.show();
        });

    }
}
