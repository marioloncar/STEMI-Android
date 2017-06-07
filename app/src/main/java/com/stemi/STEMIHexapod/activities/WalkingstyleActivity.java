package com.stemi.STEMIHexapod.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.stemi.STEMIHexapod.R;
import com.stemi.STEMIHexapod.Utils;
import com.stemi.STEMIHexapod.helpers.SharedPreferencesHelper;

import stemi.education.stemihexapod.WalkingStyle;

/**
 * Created by Mario on 29/08/16.
 */
public class WalkingstyleActivity extends AppCompatActivity {

    private TextView tvWalkDesc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkingstyle);

        Utils.initActionBarWithTitle(WalkingstyleActivity.this, this, "Walking style");

        tvWalkDesc = (TextView) findViewById(R.id.tvWalkDesc);
        RadioGroup rgWalk = (RadioGroup) findViewById(R.id.rgWalk);
        RadioButton rb1 = (RadioButton) rgWalk.findViewById(R.id.rb1);
        RadioButton rb2 = (RadioButton) rgWalk.findViewById(R.id.rb2);
        RadioButton rb3 = (RadioButton) rgWalk.findViewById(R.id.rb3);
        RadioButton rb4 = (RadioButton) rgWalk.findViewById(R.id.rb4);

        tvWalkDesc.setText(R.string.tripod_desc);
        tvWalkDesc.setTypeface(Utils.getCustomTypeface(this));
        rb1.setTypeface(Utils.getCustomTypeface(this));
        rb2.setTypeface(Utils.getCustomTypeface(this));
        rb3.setTypeface(Utils.getCustomTypeface(this));
        rb4.setTypeface(Utils.getCustomTypeface(this));

        int rbStatus = SharedPreferencesHelper.getSharedPreferencesInt(this, SharedPreferencesHelper.Key.RB_SELECTED, R.id.rb1);
        if (rbStatus > 0) {
            RadioButton rbtn = (RadioButton) rgWalk.findViewById(rbStatus);
            rbtn.setChecked(true);
        }

        rgWalk.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.rb1:
                    tvWalkDesc.setText(R.string.tripod_desc);
                    SharedPreferencesHelper.putSharedPreferencesString(this, SharedPreferencesHelper.Key.WALK, WalkingStyle.TRIPOD_GAIT.toString());
                    SharedPreferencesHelper.putSharedPreferencesInt(this, SharedPreferencesHelper.Key.RB_SELECTED, R.id.rb1);
                    break;
                case R.id.rb2:
                    tvWalkDesc.setText(R.string.tripod_delayed_desc);
                    SharedPreferencesHelper.putSharedPreferencesString(this, SharedPreferencesHelper.Key.WALK, WalkingStyle.TRIPOD_GAIT_ANGLED.toString());
                    SharedPreferencesHelper.putSharedPreferencesInt(this, SharedPreferencesHelper.Key.RB_SELECTED, R.id.rb2);
                    break;
                case R.id.rb3:
                    tvWalkDesc.setText(R.string.ripple_desc);
                    SharedPreferencesHelper.putSharedPreferencesString(this, SharedPreferencesHelper.Key.WALK, WalkingStyle.TRIPOD_GAIT_STAR.toString());
                    SharedPreferencesHelper.putSharedPreferencesInt(this, SharedPreferencesHelper.Key.RB_SELECTED, R.id.rb3);
                    break;
                case R.id.rb4:
                    tvWalkDesc.setText(R.string.wave_desc);
                    SharedPreferencesHelper.putSharedPreferencesString(this, SharedPreferencesHelper.Key.WALK, WalkingStyle.WAVE_GAIT.toString());
                    SharedPreferencesHelper.putSharedPreferencesInt(this, SharedPreferencesHelper.Key.RB_SELECTED, R.id.rb4);

                    break;
                default:
                    tvWalkDesc.setText(R.string.tripod_desc);
                    SharedPreferencesHelper.putSharedPreferencesString(this, SharedPreferencesHelper.Key.WALK, WalkingStyle.TRIPOD_GAIT.toString());
                    SharedPreferencesHelper.putSharedPreferencesInt(this, SharedPreferencesHelper.Key.RB_SELECTED, R.id.rb1);
                    break;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
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
}
