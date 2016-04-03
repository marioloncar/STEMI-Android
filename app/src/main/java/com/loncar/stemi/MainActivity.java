package com.loncar.stemi;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton ibMovement, ibRotation, ibOrientation, ibHeight, ibSettings;

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

        LinearLayout left_joystick = (LinearLayout) findViewById(R.id.llLeft_joystick);
        assert left_joystick != null;
        left_joystick.addView(new Joystick_L(this));

        LinearLayout right_joystick = (LinearLayout) findViewById(R.id.llRight_joystick);
        assert right_joystick != null;
        right_joystick.addView(new Joystick_R(this));

        ibMovement.setSelected(true);
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

}
