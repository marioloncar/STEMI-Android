package com.loncar.stemi;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Mario on 24/08/16.
 */
public class ConnectingActivity extends AppCompatActivity {

    TextView tvWelcome;
    Button bConnect, bChangeIP;
    Typeface tf;
    ProgressBar progressBar;
    Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.connecting_layout);

        tvWelcome = (TextView) findViewById(R.id.tvWelcome);
        bConnect = (Button) findViewById(R.id.bConnect);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        tvWelcome.setTypeface(tf);
        bConnect.setTypeface(tf);

        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bConnect.setText("");
                progressBar.setVisibility(View.VISIBLE);
                tvWelcome.setText(R.string.stemi_connecting);

                Animation anim = new AlphaAnimation(0.5f, 1.0f);
                anim.setDuration(700); //You can manage the time of the blink with this parameter
                anim.setStartOffset(20);
                anim.setRepeatMode(Animation.REVERSE);
                anim.setRepeatCount(Animation.INFINITE);
                tvWelcome.startAnimation(anim);



                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        // Do something after 5s = 5000ms
                        progressBar.setVisibility(View.INVISIBLE);
                        tvWelcome.clearAnimation();
                        Intent i = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(i);
                    }
                }, 5000);


            }
        });

//        bChangeIP.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent openIPActivity = new Intent(getApplicationContext(), IPActivity.class);
//                startActivity(openIPActivity);
//            }
//        });
    }
}
