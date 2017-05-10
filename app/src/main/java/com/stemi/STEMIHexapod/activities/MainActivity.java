package com.stemi.STEMIHexapod.activities;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.util.ArrayMap;
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

import com.stemi.STEMIHexapod.Menu;
import com.stemi.STEMIHexapod.R;
import com.stemi.STEMIHexapod.Utils;
import com.stemi.STEMIHexapod.helpers.SharedPreferencesHelper;
import com.stemi.STEMIHexapod.interfaces.JoystickMovement;
import com.stemi.STEMIHexapod.joysticks.JoystickL;
import com.stemi.STEMIHexapod.joysticks.JoystickR;

import java.util.Map;

import stemi.education.stemihexapod.Hexapod;
import stemi.education.stemihexapod.HexapodStatus;
import stemi.education.stemihexapod.WalkingStyle;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,
        SensorEventListener, HexapodStatus, JoystickMovement {

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
    private RelativeLayout lay;
    private ImageView longToastBck;

    private SensorManager sensorManager;
    private Sensor accelerometer;

    private byte accelerometerX = 0;
    private byte accelerometerY = 0;

    private Hexapod hexapod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_joystick);

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

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        ibMovement.setSelected(true);
        ibStandby.setSelected(true);

        /* Menu buttons */
        Map<ImageButton, ToastText> menuButtons = new ArrayMap<>(7);
        menuButtons.put(ibMovement, new ToastText(getString(R.string.movement), getString(R.string.movement_hint)));
        menuButtons.put(ibRotation, new ToastText(getString(R.string.rotation), getString(R.string.rotation_hint)));
        menuButtons.put(ibOrientation, new ToastText(getString(R.string.orientation), getString(R.string.orientation_hint)));
        menuButtons.put(ibHeight, new ToastText(getString(R.string.height), getString(R.string.height_hint)));
        menuButtons.put(ibCalibration, new ToastText(getString(R.string.calibration), getString(R.string.calibration_hint)));
        menuButtons.put(ibWalkingStyle, new ToastText(getString(R.string.walk_style), getString(R.string.walkstyle_hint)));

        /* Listeners */
        for (ImageButton ib : menuButtons.keySet()) {
            final ToastText toastText = menuButtons.get(ib);
            ib.setOnLongClickListener(view -> {
                showLongToast(toastText.title, toastText.message);
                return true;
            });
            ib.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    hideToast();
                return false;
            });
        }

        hexapod = new Hexapod();
        hexapod.hexapodStatus = this;

        JoystickL.leftJoystick = this;
        JoystickR.rightJoystick = this;

    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        String savedIp = SharedPreferencesHelper.getSharedPreferencesString(this, SharedPreferencesHelper.Key.IP, null);
        byte heightPref = (byte) SharedPreferencesHelper.getSharedPreferencesInt(this, SharedPreferencesHelper.Key.HEIGHT, 50);
        String walkingStyle = SharedPreferencesHelper.getSharedPreferencesString(this, SharedPreferencesHelper.Key.WALK, WalkingStyle.TRIPOD_GAIT.toString());

        hexapod.setIpAddress(savedIp);
        hexapod.setHeight(heightPref);
        hexapod.setWalkingStyle(WalkingStyle.valueOf(walkingStyle));
        hexapod.connect();

        // hide soft keys and status bar
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onPause() {
        super.onPause();
        hexapod.setMovementMode();
        hexapod.disconnect();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        hexapod.disconnect();
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
        this.hexapod.setAccelerometerX(this.accelerometerX);
        this.hexapod.setAccelerometerY(this.accelerometerY);
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
                    hexapod.turnOff();
                } else {
                    ibStandby.setSelected(true);
                    hexapod.turnOn();
                }
                break;

            case R.id.bMenu:
                if (menu.bMenu.isSelected()) closeMenu();
                else openMenu();
                break;

            case R.id.vOverlay:
                closeMenu();
                break;

            case R.id.ibMovement:
                if (!ibMovement.isSelected()) showShortToast(getString(R.string.movement_enabled));
                setSelectedButtons(true, false, false);
                hexapod.setMovementMode();
                closeMenu();
                break;

            case R.id.ibRotation:
                if (!ibRotation.isSelected()) showShortToast(getString(R.string.rotation_enabled));
                setSelectedButtons(false, true, false);
                hexapod.setRotationMode();
                closeMenu();
                break;

            case R.id.ibOrientation:
                if (!ibOrientation.isSelected())
                    showShortToast(getString(R.string.orientation_enabled));
                setSelectedButtons(false, false, true);
                hexapod.setOrientationMode();
                closeMenu();
                break;

            case R.id.ibHeight:
                closeMenu();
                if (hexapod.isInStandby()) {
                    Utils.showStandbyDialog(this, MainActivity.this);
                } else {
                    Intent intent = new Intent(MainActivity.this, HeightActivity.class);
                    startActivity(intent);
                }
                break;

            case R.id.ibCalibration:
                closeMenu();
                if (hexapod.isInStandby()) {
                    Utils.showStandbyDialog(this, MainActivity.this);
                } else {
                    Intent intent1 = new Intent(MainActivity.this, CalibrationActivity.class);
                    startActivity(intent1);
                }
                break;

            case R.id.ibWalkingStyle:
                closeMenu();
                Intent intent2 = new Intent(MainActivity.this, WalkingstyleActivity.class);
                startActivity(intent2);
                break;

            case R.id.ibSettings:
                closeMenu();
                Intent intent3 = new Intent(MainActivity.this, SettingsActivity.class);
                intent3.putExtra("standby", hexapod.isInStandby());
                startActivity(intent3);
                break;
        }
    }

    private void setSelectedButtons(boolean movement, boolean rotation, boolean orientation) {
        ibMovement.setSelected(movement);
        ibRotation.setSelected(rotation);
        ibOrientation.setSelected(orientation);
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
    public void onBackPressed() {
        super.onBackPressed();
        hexapod.disconnect();
        finish();
    }


    private void showShortToast(String message) {
        LayoutInflater inflater = getLayoutInflater();
        View rlToastShort = inflater.inflate(R.layout.toast_short,
                (ViewGroup) findViewById(R.id.rlToastShortRoot));

        TextView tvEnabled = (TextView) rlToastShort.findViewById(R.id.tvEnabled);

        int offset = Math.round(30 * this.getResources().getDisplayMetrics().density);

        tvEnabled.setText(message);
        tvEnabled.setTypeface(Utils.getCustomTypeface(this));
        Toast toast = new Toast(this);
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

        tvTitle.setTypeface(Utils.getCustomTypeface(this));
        tvDesc.setTypeface(Utils.getCustomTypeface(this));

    }

    private void hideToast() {
        Animation hide = new AlphaAnimation(1, 0);
        hide.setDuration(200);
        longToastBck.startAnimation(hide);
        lay.setVisibility(View.INVISIBLE);
    }

    @Override
    public void connectionStatus(boolean isConnected) {
        if (!isConnected) {
            Utils.showConnectionDialog(this, MainActivity.this);
        }

    }

    @Override
    public void leftJoystickMoved(int power, int angle) {
        hexapod.setJoystickParameters(power, angle);
    }

    @Override
    public void rightJoystickMoved(int rotation) {
        hexapod.setJoystickParameters(rotation);
    }
}
