package com.stemi.STEMIHexapod;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
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


/**
 * Created by Mario on 29/08/16.
 */
public class WalkstyleActivity extends AppCompatActivity {

    Typeface tf;
    TextView tvStyleHeader, tvWalkDesc;
    RadioGroup rgWalk;
    RadioButton rb1, rb2, rb3, rb4;

    public byte walkValue;
    public int rbStatus;

    SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walkstyle_layout);

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar));
        actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>Walking style</font>"));

        @SuppressLint("PrivateResource")
        final Drawable upArrow = ResourcesCompat.getDrawable(getResources(), R.drawable.abc_ic_ab_back_mtrl_am_alpha, null);
        assert upArrow != null;
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.highlightColor), PorterDuff.Mode.SRC_ATOP);
        actionBar.setHomeAsUpIndicator(upArrow);

        tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        tvStyleHeader = (TextView) findViewById(R.id.tvStyleHeader);
        tvWalkDesc = (TextView) findViewById(R.id.tvWalkDesc);
        rgWalk = (RadioGroup) findViewById(R.id.rgWalk);
        rb1 = (RadioButton) rgWalk.findViewById(R.id.rb1);
        rb2 = (RadioButton) rgWalk.findViewById(R.id.rb2);
        rb3 = (RadioButton) rgWalk.findViewById(R.id.rb3);
        rb4 = (RadioButton) rgWalk.findViewById(R.id.rb4);

        prefs = getSharedPreferences("myPref", MODE_PRIVATE);

        tvWalkDesc.setText(R.string.tripod_desc);
        tvWalkDesc.setTypeface(tf);
        rb1.setTypeface(tf);
        rb2.setTypeface(tf);
        rb3.setTypeface(tf);
        rb4.setTypeface(tf);

        rbStatus = prefs.getInt("rbSelected", R.id.rb1);
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
                        walkValue = 30;
                        prefs.edit().putInt("walk", walkValue).apply();
                        prefs.edit().putInt("rbSelected", R.id.rb1).apply();
                        break;
                    case R.id.rb2:
                        tvWalkDesc.setText(R.string.tripod_delayed_desc);
                        walkValue = 60;
                        prefs.edit().putInt("walk", walkValue).apply();
                        prefs.edit().putInt("rbSelected", R.id.rb2).apply();
                        break;
                    case R.id.rb3:
                        tvWalkDesc.setText(R.string.ripple_desc);
                        walkValue = 80;
                        prefs.edit().putInt("walk", walkValue).apply();
                        prefs.edit().putInt("rbSelected", R.id.rb3).apply();
                        break;
                    case R.id.rb4:
                        tvWalkDesc.setText(R.string.wave_desc);
                        walkValue = 100;
                        prefs.edit().putInt("walk", walkValue).apply();
                        prefs.edit().putInt("rbSelected", R.id.rb4).apply();
                        break;
                    default:
                        tvWalkDesc.setText(R.string.tripod_desc);
                        walkValue = 30;
                        prefs.edit().putInt("walk", walkValue).apply();
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
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }
    }

}