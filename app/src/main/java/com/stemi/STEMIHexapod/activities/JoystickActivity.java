package com.stemi.STEMIHexapod.activities;

import android.content.DialogInterface;
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

import com.stemi.STEMIHexapod.interfaces.JoystickMovement;
import com.stemi.STEMIHexapod.Menu;
import com.stemi.STEMIHexapod.R;
import com.stemi.STEMIHexapod.joysticks.JoystickL;
import com.stemi.STEMIHexapod.joysticks.JoystickR;

import mario.com.stemihexapod.Hexapod;
import mario.com.stemihexapod.HexapodStatus;
import mario.com.stemihexapod.WalkingStyle;

import static com.stemi.STEMIHexapod.Menu.bMenu;

public class JoystickActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener, HexapodStatus, JoystickMovement {

    private ImageButton ibStandby, ibMovement, ibRotation, ibOrientation;
    private View vOverlay;
    private Typeface tf;
    private RelativeLayout lay;
    private ImageView longToastBck;
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private byte accelerometerX = 0;
    private byte accelerometerY = 0;
    private AlertDialog.Builder builder;
    private SharedPreferences prefs;
    private Hexapod hexapod;

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

        hexapod = new Hexapod();
        hexapod.hexapodStatus = this;

        // interface methods
        JoystickL.leftJoystick = this;
        JoystickR.rightJoystick = this;


        /**** OnLongClick Listeners ****/
        ibMovement.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Movement", getString(R.string.movement_hint));
                return true;

            }
        });

        ibRotation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Rotation", getString(R.string.rotation_hint));
                return true;
            }
        });

        ibOrientation.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Orientation", getString(R.string.orientation_hint));
                return true;
            }
        });

        ibHeight.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Height", getString(R.string.height_hint));
                return true;
            }
        });

        ibCalibration.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Calibration", getString(R.string.calibration_hint));
                return true;
            }
        });

        ibWalkingStyle.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                showLongToast("Walk style", getString(R.string.walkstyle_hint));
                return true;
            }
        });


        /**** OnTouch Listeners ****/
        ibMovement.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    hideToast();
                return false;
            }
        });

        ibRotation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    hideToast();
                return false;
            }
        });

        ibOrientation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    hideToast();
                return false;
            }
        });

        ibHeight.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    hideToast();
                return false;
            }
        });

        ibCalibration.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    hideToast();
                return false;
            }
        });

        ibWalkingStyle.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP)
                    hideToast();
                return false;
            }
        });

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

    // onClick listeners for buttons on screen
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
                setSelectedButtons(true, false, false);
                hexapod.setMovementMode();
                closeMenu();
                break;

            case R.id.ibRotation:
                if (!ibRotation.isSelected()) {
                    showShortToast("ROTATION ENABLED");
                }
                setSelectedButtons(false, true, false);
                hexapod.setRotationMode();
                closeMenu();
                break;

            case R.id.ibOrientation:
                if (!ibOrientation.isSelected()) {
                    showShortToast("ORIENTATION ENABLED");
                }
                setSelectedButtons(false, false, true);
                hexapod.setOrientationMode();
                closeMenu();
                break;

            case R.id.ibHeight:
                closeMenu();
                Intent intent = new Intent(JoystickActivity.this, HeightActivity.class);
                startActivity(intent);
                break;

            case R.id.ibCalibration:
                closeMenu();
                Intent intent1 = new Intent(JoystickActivity.this, CalibrationActivity.class);
                startActivity(intent1);
                break;

            case R.id.ibWalkingStyle:
                closeMenu();
                Intent intent2 = new Intent(JoystickActivity.this, WalkingstyleActivity.class);
                startActivity(intent2);
                break;

            case R.id.ibSettings:
                closeMenu();
                Intent intent3 = new Intent(JoystickActivity.this, SettingsActivity.class);
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
    public void onBackPressed() {
        super.onBackPressed();
        hexapod.disconnect();
        finish();
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
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);

        String savedIp = prefs.getString("ip", null);
        byte heightPref = (byte) prefs.getInt("height", 50);
        String walkingStyle = prefs.getString("walk", WalkingStyle.TRIPOD_GAIT.toString());

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

    private void showConnectionDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                builder.setCancelable(false);
                builder.setTitle("Connection lost");
                builder.setMessage("Please check connection with your STEMI and try again.");
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent i = new Intent(JoystickActivity.this, ConnectingActivity.class);
                        startActivity(i);
                        finish();
                    }
                });
                builder.show();
            }
        });

    }

    @Override
    public void connectionStatus(boolean isConnected) {
        if (!isConnected) {
            showConnectionDialog();
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
