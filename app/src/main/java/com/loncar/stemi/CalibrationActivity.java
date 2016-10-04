package com.loncar.stemi;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Queue;

/**
 * Created by Mario on 29/08/16.
 */
public class CalibrationActivity extends AppCompatActivity implements View.OnClickListener {

    ImageButton ibMotor0, ibMotor1, ibMotor2, ibMotor3, ibMotor4, ibMotor5, ibMotor6, ibMotor7,
            ibMotor8, ibMotor9, ibMotor10, ibMotor11, ibMotor12, ibMotor13, ibMotor14, ibMotor15, ibMotor16, ibMotor17;
    ImageButton[] motors;
    Intent goBack;
    AlertDialog.Builder builder;
    ImageView ivCircle;
    TextView tvCalibValue;
    static int REPEAT_DELAY = 50;
    public int sleepingInterval = 200;

    // bytes of the LIN (linearization) packets
    public byte[] calibrationArray = {50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 0};
    public Queue<byte[]> calibrationQueue;
    private final String CALIBRATION_FILENAME = "linearization.bin";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.calibration_layout);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar_landscape));
        actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>Calibration</font>"));

        ibMotor0 = (ImageButton) findViewById(R.id.ibMotor0);
        ibMotor1 = (ImageButton) findViewById(R.id.ibMotor1);
        ibMotor2 = (ImageButton) findViewById(R.id.ibMotor2);
        ibMotor3 = (ImageButton) findViewById(R.id.ibMotor3);
        ibMotor4 = (ImageButton) findViewById(R.id.ibMotor4);
        ibMotor5 = (ImageButton) findViewById(R.id.ibMotor5);
        ibMotor6 = (ImageButton) findViewById(R.id.ibMotor6);
        ibMotor7 = (ImageButton) findViewById(R.id.ibMotor7);
        ibMotor8 = (ImageButton) findViewById(R.id.ibMotor8);
        ibMotor9 = (ImageButton) findViewById(R.id.ibMotor9);
        ibMotor10 = (ImageButton) findViewById(R.id.ibMotor10);
        ibMotor11 = (ImageButton) findViewById(R.id.ibMotor11);
        ibMotor12 = (ImageButton) findViewById(R.id.ibMotor12);
        ibMotor13 = (ImageButton) findViewById(R.id.ibMotor13);
        ibMotor14 = (ImageButton) findViewById(R.id.ibMotor14);
        ibMotor15 = (ImageButton) findViewById(R.id.ibMotor15);
        ibMotor16 = (ImageButton) findViewById(R.id.ibMotor16);
        ibMotor17 = (ImageButton) findViewById(R.id.ibMotor17);
        ivCircle = (ImageView) findViewById(R.id.ivCircle);
        tvCalibValue = (TextView) findViewById(R.id.tvCalibValue);

        motors = new ImageButton[]{ibMotor0, ibMotor1, ibMotor2, ibMotor3, ibMotor4, ibMotor5, ibMotor6,
                ibMotor7, ibMotor8, ibMotor9, ibMotor10, ibMotor11, ibMotor12, ibMotor13, ibMotor14, ibMotor15, ibMotor16, ibMotor17};

        goBack = new Intent(this, MainActivity.class);
        builder = new AlertDialog.Builder(this);

    }

    @Override
    public boolean onSupportNavigateUp() {
        showBackDialog();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        showBackDialog();
    }


    private void showBackDialog() {
        builder.setTitle("Warning");
        builder.setMessage("Are you sure you want to reset STEMI Hexapod legs to their initial positions?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                finish();
                startActivity(goBack);

            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                //dismiss dialog
            }
        });
        builder.show();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save_ip) {
            finish();
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ibMotor0:
                ibMotor0.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor0) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor1:
                ibMotor1.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor1) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor2:
                ibMotor2.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor2) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor3:
                ibMotor3.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor3) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor4:
                ibMotor4.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor4) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor5:
                ibMotor5.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor5) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor6:
                ibMotor6.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor6) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor7:
                ibMotor7.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor7) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor8:
                ibMotor8.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor8) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor9:
                ibMotor9.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor9) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor10:
                ibMotor10.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor10) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor11:
                ibMotor11.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor11) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor12:
                ibMotor12.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor12) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor13:
                ibMotor13.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor13) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor14:
                ibMotor14.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor14) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor15:
                ibMotor15.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor15) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor16:
                ibMotor16.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor16) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
            case R.id.ibMotor17:
                ibMotor17.setAlpha(1f);
                for (int i = 0; i < motors.length; i++) {
                    if (motors[i] != ibMotor17) {
                        motors[i].setAlpha(0f);
                    }
                }
                setVisible();
                break;
        }
    }

    private void setVisible() {
        ivCircle.setVisibility(View.VISIBLE);
    }


}



