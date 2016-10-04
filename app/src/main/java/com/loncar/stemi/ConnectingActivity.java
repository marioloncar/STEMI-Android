package com.loncar.stemi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;
import java.util.logging.Handler;


/**
 * Created by Mario on 24/08/16.
 */
public class ConnectingActivity extends AppCompatActivity {

    TextView tvConnectingTitle, tvConnectingHint;
    Button bConnect, bChangeIP;
    Typeface tf;
    ImageView ivStemiIcon, ivProgressPath, ivProgress;
    SharedPreferences prefs = null;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
                bConnect.setText(R.string.pairing);

                Animation animation = new AlphaAnimation(0.5f, 1.0f);
                animation.setDuration(700);
                animation.setStartOffset(20);
                animation.setRepeatMode(Animation.REVERSE);
                animation.setRepeatCount(Animation.INFINITE);

                RotateAnimation rotateAnimation = new RotateAnimation(0, 360f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                rotateAnimation.setInterpolator(new LinearInterpolator());
                rotateAnimation.setDuration(1000);
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
                bChangeIP.setVisibility(View.INVISIBLE);

                /*** Connect to STEMI Hexapod ***/
                Thread thread = new Thread() {
                    @Override
                    public void run() {
                        String jsonString = "";
                        jsonString = fetchJSON("http://192.168.4.1/stemiData.json");
                        try {
                            // if jsonString object exists compare with one saved in SharedPrefs
                            if (jsonString != null) {
                                JSONObject jsonObject = new JSONObject(jsonString);
                                if (Objects.equals(jsonObject.getString("stemiID"), prefs.getString("stemiId", null))) {
                                    Log.d("USPOREDBA", "ID su jednaki");
                                    synchronized (this) {
                                        wait(5000);
                                    }
                                    openMainActivity();
                                } else {
                                    // if IDs are not equal, set default STEMI values
                                    Log.d("USPOREDBA", "ID nisu jednaki");
                                    String version = jsonObject.getString("version");
                                    String stemiId = jsonObject.getString("stemiID");

                                    prefs.edit().putString("version", version).apply();
                                    prefs.edit().putString("stemiId", stemiId).apply();
                                    prefs.edit().putInt("walk", 30).apply();
                                    prefs.edit().putInt("rbSelected", R.id.rb1).apply();
                                    prefs.edit().putInt("height", 50).apply();

                                    synchronized (this) {
                                        wait(5000);
                                    }
                                    openMainActivity();
                                }
                            } else {
                                // if connection is not established show different UI
                                synchronized (this) {
                                    wait(5000);
                                }
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        AnimationSet error = new AnimationSet(false);

                                        Animation leftMove = new TranslateAnimation(0, -10, 0, 0);
                                        leftMove.setDuration(50);
                                        Animation rightMove = new TranslateAnimation(0, 20, 0, 0);
                                        rightMove.setStartOffset(50);
                                        rightMove.setDuration(50);

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
                                        bConnect.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_connecting, null));
                                        bChangeIP.setVisibility(View.VISIBLE);
                                        bChangeIP.setTypeface(tf);
                                        tvConnectingTitle.setText(R.string.unable_to_connect);
                                        tvConnectingTitle.startAnimation(error);
                                        tvConnectingHint.setText(R.string.hint_noConnection);
                                    }
                                });
                            }
                        } catch (JSONException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                };
                thread.start();
                /*** Connect to STEMI Hexapod end ***/

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
            String restoredIp = prefs.getString("ip", null);
            if (restoredIp != null) {
                String newIp = prefs.getString("ip", "STEMI IP");
                System.out.println("IP -> " + newIp);
            }
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
                Log.d("Response: ", "> " + line);

            }

            return buffer.toString();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (reader != null) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}