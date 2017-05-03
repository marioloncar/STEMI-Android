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
import android.util.Log;
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

import com.polidea.rxandroidble.RxBleConnection;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.utils.ConnectionSharingAdapter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.UUID;

import rx.Observable;
import rx.subjects.PublishSubject;

import com.trello.rxlifecycle.components.support.RxAppCompatActivity;
import static com.trello.rxlifecycle.android.ActivityEvent.PAUSE;

public class MainActivity extends RxAppCompatActivity implements View.OnClickListener, SensorEventListener {

    private static final String TAG = "MainActivity";

    int logCounter = 0;

    private class ToastText {
        String title;
        String message;

        ToastText(String t, String m) {
            title = t;
            message = m;
        }
    }

    private ImageButton ibStandby, ibMovement, ibRotation, ibOrientation;
    private Menu menu;
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

    private Observable<RxBleConnection> connectionObservable;
    private PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();
    private RxBleDevice rxBleDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String macAddress = getIntent().getStringExtra("ARDUINO_BT_MAC_ADDRESS");

        rxBleDevice = ConnectingActivity.getRxBleClient().getBleDevice(macAddress);
        connectionObservable = prepareConnectionObservable();

        menu = (Menu) findViewById(R.id.menu);
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

        Map<ImageButton, ToastText> menuButtons = new HashMap<>(7);
        menuButtons.put(ibMovement, new ToastText("Movement", getString(R.string.movement_hint)));
        menuButtons.put(ibRotation, new ToastText("Rotation", getString(R.string.rotation_hint)));
        menuButtons.put(ibOrientation, new ToastText("Orientation", getString(R.string.orientation_hint)));
        menuButtons.put(ibHeight, new ToastText("Height", getString(R.string.height_hint)));
        menuButtons.put(ibCalibration, new ToastText("Calibration", getString(R.string.calibration_hint)));
        menuButtons.put(ibWalkingStyle, new ToastText("Walk style", getString(R.string.walkstyle_hint)));

        /**** Listeners ****/
        for(ImageButton ib : menuButtons.keySet()) {
            ToastText toastText = menuButtons.get(ib);
            ib.setOnLongClickListener(v -> {
                showLongToast(toastText.title, toastText.message);
                return true;
            });
            ib.setOnTouchListener(this::hideToast);
        }
    }

    private boolean hideToast(View v, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Animation hide = new AlphaAnimation(1, 0);
            hide.setDuration(200);
            longToastBck.startAnimation(hide);
            lay.setVisibility(View.INVISIBLE);
        }
        return false;
    }

    private boolean isConnected() {
        return rxBleDevice.getConnectionState() == RxBleConnection.RxBleConnectionState.CONNECTED;
    }

    private Observable<RxBleConnection> prepareConnectionObservable() {
        return rxBleDevice
                .establishConnection(false)
                .takeUntil(disconnectTriggerSubject)
                .compose(bindUntilEvent(PAUSE))
                .compose(new ConnectionSharingAdapter());
    }

    public void sendCommandsOverWiFi() {
        connected = true;
        UUID characteristicUUID = UUID.fromString("19B10001-E8F2-537E-4F6C-D104768A1214");

        Thread t = new Thread() {
            public void run() {
                try {
                    while (connected) {
                        Thread.sleep(SLEEPING_INTERVAL);
                        logCounter++;
                        connectionObservable
                            .flatMap(rxBleConnection -> rxBleConnection.writeCharacteristic(characteristicUUID, bytesArray()))
                            .subscribe(
                                characteristicValue -> {
                                    if (logCounter % 10 == 0) Log.w(TAG, " Val confirmed > " + Arrays.toString(characteristicValue));
                                },
                                throwable -> {
                                    if (logCounter % 10 == 0) Log.w(TAG, " Char ERROR > " + throwable.toString());
                                }
                            );
                    }
                } catch (InterruptedException e) {
                    showConnectionDialog();
                } finally {
                    connected = false;
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
                if (menu.bMenu.isSelected()) {
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
        menu.openMenu();
        menu.bMenu.setSelected(true);
        vOverlay.setVisibility(View.VISIBLE);
    }

    private void closeMenu() {
        menu.closeMenu();
        menu.bMenu.setSelected(false);
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
        ConnectingActivity.triggerDisconnect();
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
        sendCommandsOverWiFi();

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



    private byte[] bytesArray() {

        JoystickL joyL = (JoystickL) findViewById(R.id.joyL);
        JoystickR joyR = (JoystickR) findViewById(R.id.joyR);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream() {
        };
        try {
            outputStream.write("P".getBytes());
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

    private void triggerDisconnect() {
        disconnectTriggerSubject.onNext(null);
    }
}
