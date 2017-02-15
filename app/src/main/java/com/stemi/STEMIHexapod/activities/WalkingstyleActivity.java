package com.stemi.STEMIHexapod.activities;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.stemi.STEMIHexapod.R;

import mario.com.stemihexapod.WalkingStyle;


/**
 * Created by Mario on 29/08/16.
 */
public class WalkingstyleActivity extends AppCompatActivity {

    private TextView tvWalkDesc;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walkingstyle);

        initActionBarWithTitle("Walking style");

        tvWalkDesc = (TextView) findViewById(R.id.tvWalkDesc);
        RadioGroup rgWalk = (RadioGroup) findViewById(R.id.rgWalk);
        RadioButton rb1 = (RadioButton) rgWalk.findViewById(R.id.rb1);
        RadioButton rb2 = (RadioButton) rgWalk.findViewById(R.id.rb2);
        RadioButton rb3 = (RadioButton) rgWalk.findViewById(R.id.rb3);
        RadioButton rb4 = (RadioButton) rgWalk.findViewById(R.id.rb4);

        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        prefs = getSharedPreferences("myPref", MODE_PRIVATE);

        tvWalkDesc.setText(R.string.tripod_desc);
        tvWalkDesc.setTypeface(tf);
        rb1.setTypeface(tf);
        rb2.setTypeface(tf);
        rb3.setTypeface(tf);
        rb4.setTypeface(tf);

        int rbStatus = prefs.getInt("rbSelected", R.id.rb1);
        if (rbStatus > 0) {
            RadioButton rbtn = (RadioButton) rgWalk.findViewById(rbStatus);
            rbtn.setChecked(true);
        }

        rgWalk.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rb1:
                        tvWalkDesc.setText(R.string.tripod_desc);
                        prefs.edit().putString("walk", WalkingStyle.TRIPOD_GAIT.toString()).apply();
                        prefs.edit().putInt("rbSelected", R.id.rb1).apply();
                        break;
                    case R.id.rb2:
                        tvWalkDesc.setText(R.string.tripod_delayed_desc);
                        prefs.edit().putString("walk", WalkingStyle.TRIPOD_GAIT_ANGLED.toString()).apply();
                        prefs.edit().putInt("rbSelected", R.id.rb2).apply();
                        break;
                    case R.id.rb3:
                        tvWalkDesc.setText(R.string.ripple_desc);
                        prefs.edit().putString("walk", WalkingStyle.TRIPOD_GAIT_STAR.toString()).apply();
                        prefs.edit().putInt("rbSelected", R.id.rb3).apply();
                        break;
                    case R.id.rb4:
                        tvWalkDesc.setText(R.string.wave_desc);
                        prefs.edit().putString("walk", WalkingStyle.WAVE_GAIT.toString()).apply();
                        prefs.edit().putInt("rbSelected", R.id.rb4).apply();
                        break;
                    default:
                        tvWalkDesc.setText(R.string.tripod_desc);
                        prefs.edit().putString("walk", WalkingStyle.TRIPOD_GAIT.toString()).apply();
                        prefs.edit().putInt("rbSelected", R.id.rb1).apply();
                        break;
                }
            }
        });
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

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

    private void initActionBarWithTitle(String title) {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>" + title + "</font>", Html.FROM_HTML_MODE_LEGACY));
        } else {
            actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>" + title + "</font>"));
        }

        @SuppressLint("PrivateResource")
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.abc_ic_ab_back_material, null);
        assert upArrow != null;
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.highlightColor), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(upArrow);
    }

}
