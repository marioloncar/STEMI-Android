package com.stemi.STEMIHexapod;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import io.fabric.sdk.android.Fabric;
import mario.com.stemihexapod.WalkingStyle;

/**
 * Created by Mario on 24/08/16.
 */
public class ConnectingActivity extends AppCompatActivity {

    private TextView tvConnectingTitle, tvConnectingHint;
    private Button bConnect, bChangeIP;
    private Typeface tf;
    private ImageView ivStemiIcon, ivProgressPath, ivProgress;
    private SharedPreferences prefs = null;
    private String savedIp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        Fabric.with(this, new Crashlytics(), new Answers());
        setContentView(R.layout.connecting_layout);

        tvConnectingTitle = (TextView) findViewById(R.id.tvConnectingTitle);
        tvConnectingHint = (TextView) findViewById(R.id.tvConnectingHint);
        bConnect = (Button) findViewById(R.id.bConnect);
        bChangeIP = (Button) findViewById(R.id.bChangeIP);
        ivStemiIcon = (ImageView) findViewById(R.id.ivStemiIcon);
        ivProgressPath = (ImageView) findViewById(R.id.ivProgressPath);
        ivProgress = (ImageView) findViewById(R.id.ivProgress);

        tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        tvConnectingTitle.setTypeface(tf);
        tvConnectingHint.setTypeface(tf);
        bConnect.setTypeface(tf);

        prefs = getSharedPreferences("myPref", MODE_PRIVATE);

        bConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateUI();
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String jsonString = "";
                            jsonString = fetchJSON("http://" + savedIp + "/stemiData.json");
                            // if jsonString object exists compare with one saved in SharedPreferences
                            if (jsonString != null) {
                                JSONObject jsonObject = new JSONObject(jsonString);
                                if (Objects.equals(jsonObject.getString("stemiID"), prefs.getString("stemiId", null))) {
                                    synchronized (this) {
                                        wait(2000);
                                    }
                                    openMainActivity();
                                } else {
                                    // if IDs are not equal, set default STEMI values
                                    initializeDefaultValues(jsonObject);
                                }
                            } else {
                                // if connection is not established show different UI
                                synchronized (this) {
                                    wait(5000);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        changeUI();
                                    }
                                });
                            }
                        } catch (JSONException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }

                    private void initializeDefaultValues(JSONObject jsonObject) throws JSONException, InterruptedException {
                        String version = jsonObject.getString("version");
                        String stemiId = jsonObject.getString("stemiID");

                        prefs.edit().putString("version", version).apply();
                        prefs.edit().putString("stemiId", stemiId).apply();
                        prefs.edit().putString("walk", WalkingStyle.TripodGait.toString()).apply();
                        prefs.edit().putInt("rbSelected", R.id.rb1).apply();
                        prefs.edit().putInt("height", 50).apply();
                        synchronized (this) {
                            wait(2000);
                        }
                        openMainActivity();
                    }

                };
                thread.start();
            }
        });

        bChangeIP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), IPActivity.class);
                startActivity(i);
            }
        });
    }

    private void animateUI() {
        bConnect.setText(R.string.pairing);

        Animation animation = new AlphaAnimation(0.5f, 1.0f);
        animation.setDuration(700);
        animation.setStartOffset(20);
        animation.setRepeatMode(Animation.REVERSE);
        animation.setRepeatCount(Animation.INFINITE);

        RotateAnimation rotateAnimation = new RotateAnimation(0, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        rotateAnimation.setInterpolator(new LinearInterpolator());
        rotateAnimation.setDuration(1300);
        rotateAnimation.setRepeatCount(Animation.INFINITE);

        tvConnectingHint.setVisibility(View.INVISIBLE);
        tvConnectingTitle.setVisibility(View.INVISIBLE);
        ivStemiIcon.setVisibility(View.VISIBLE);
        ivProgress.setVisibility(View.VISIBLE);
        ivProgressPath.setVisibility(View.VISIBLE);

        ivProgress.startAnimation(rotateAnimation);
        ivStemiIcon.startAnimation(animation);
        bConnect.setBackground(null);
        bConnect.setEnabled(false);
        bConnect.setTextSize(20);
        bConnect.setAlpha(0.6f);
        bChangeIP.setVisibility(View.INVISIBLE);
    }

    private void changeUI() {
        AnimationSet error = new AnimationSet(false);
        Animation leftMove = new TranslateAnimation(0, -10, 0, 0);
        leftMove.setDuration(50);
        Animation rightMove = new TranslateAnimation(0, 20, 0, 0);
        rightMove.setStartOffset(50);
        rightMove.setDuration(50);

        Animation fadeIn = new AlphaAnimation(0f, 1.0f);
        fadeIn.setDuration(500);

        error.addAnimation(leftMove);
        error.addAnimation(rightMove);

        ivProgress.clearAnimation();
        ivStemiIcon.clearAnimation();
        ivProgressPath.setVisibility(View.INVISIBLE);
        ivProgress.setVisibility(View.INVISIBLE);
        ivStemiIcon.setVisibility(View.INVISIBLE);
        tvConnectingHint.setVisibility(View.VISIBLE);
        tvConnectingTitle.setVisibility(View.VISIBLE);

        bConnect.setText(R.string.try_again);
        bConnect.setEnabled(true);
        bConnect.setTextSize(16);
        bConnect.setAlpha(1f);
        bConnect.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_connecting, null));
        bConnect.startAnimation(fadeIn);

        bChangeIP.setVisibility(View.VISIBLE);
        bChangeIP.setTypeface(tf);

        tvConnectingTitle.setText(R.string.unable_to_connect);
        tvConnectingTitle.startAnimation(error);
        tvConnectingHint.setText(R.string.hint_noConnection);
    }

    private void openMainActivity() {
        finish();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (prefs.getBoolean("firstrun", true)) {
            prefs.edit().putString("ip", "192.168.4.1").apply();
            prefs.edit().putBoolean("firstrun", false).apply();
        } else {
            SharedPreferences prefs = getSharedPreferences("myPref", MODE_PRIVATE);
            savedIp = prefs.getString("ip", null);
            prefs.edit().putBoolean("firstrun", false).apply();

        }
    }

    private String fetchJSON(String params) {
        HttpURLConnection connection = null;
        BufferedReader reader = null;

        try {
            URL url = new URL(params);
            connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.setUseCaches(false);
            connection.connect();

            InputStream stream = connection.getInputStream();
            reader = new BufferedReader(new InputStreamReader(stream));

            StringBuilder buffer = new StringBuilder();
            String line = "";
            while ((line = reader.readLine()) != null) {
                buffer.append(line).append("\n");
            }

            return buffer.toString();

        } catch (IOException ignored) {
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException ignored) {
            }
        }
        return null;
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