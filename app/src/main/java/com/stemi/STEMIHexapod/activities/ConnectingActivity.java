package com.stemi.STEMIHexapod.activities;

import android.content.Intent;
import android.os.AsyncTask;
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

import com.stemi.STEMIHexapod.R;
import com.stemi.STEMIHexapod.Utils;
import com.stemi.STEMIHexapod.helpers.SharedPreferencesHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

import stemi.education.stemihexapod.WalkingStyle;

/**
 * Created by Mario on 24/08/16.
 */
public class ConnectingActivity extends AppCompatActivity {

    private TextView tvConnectingTitle, tvConnectingHint;
    private Button bConnect, bChangeIP;
    private ImageView ivStemiIcon, ivProgressPath, ivProgress;
    private String savedIp;

    @Override
    protected void onCreate(@Nullable final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connecting);

        tvConnectingTitle = (TextView) findViewById(R.id.tvConnectingTitle);
        tvConnectingHint = (TextView) findViewById(R.id.tvConnectingHint);
        bConnect = (Button) findViewById(R.id.bConnect);
        bChangeIP = (Button) findViewById(R.id.bChangeIP);
        ivStemiIcon = (ImageView) findViewById(R.id.ivStemiIcon);
        ivProgressPath = (ImageView) findViewById(R.id.ivProgressPath);
        ivProgress = (ImageView) findViewById(R.id.ivProgress);

        tvConnectingTitle.setTypeface(Utils.getCustomTypeface(this));
        tvConnectingHint.setTypeface(Utils.getCustomTypeface(this));
        bConnect.setTypeface(Utils.getCustomTypeface(this));

        bConnect.setOnClickListener(v -> {
            ConnectionTask connection = new ConnectionTask();
            connection.execute();
        });

        bChangeIP.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), IPActivity.class);
            startActivity(i);
        });
    }

    private void initDefaultValues(JSONObject jsonObject) throws JSONException, InterruptedException {
        String version = jsonObject.getString("version");
        String stemiId = jsonObject.getString("stemiID");

        SharedPreferencesHelper.putSharedPreferencesString(this, SharedPreferencesHelper.Key.VERSION, version);
        SharedPreferencesHelper.putSharedPreferencesString(this, SharedPreferencesHelper.Key.STEMI_ID, stemiId);
        SharedPreferencesHelper.putSharedPreferencesString(this, SharedPreferencesHelper.Key.WALK, WalkingStyle.TRIPOD_GAIT.toString());
        SharedPreferencesHelper.putSharedPreferencesInt(this, SharedPreferencesHelper.Key.RB_SELECTED, R.id.rb1);
        SharedPreferencesHelper.putSharedPreferencesInt(this, SharedPreferencesHelper.Key.HEIGHT, 50);

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

    private void showErrorUI() {
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
        bChangeIP.setTypeface(Utils.getCustomTypeface(this));

        tvConnectingTitle.setText(R.string.unable_to_connect);
        tvConnectingTitle.startAnimation(error);
        tvConnectingHint.setText(R.string.hint_noConnection);
    }

    private void openJoystickActivity() {
        finish();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        startActivity(i);
    }


    @Override
    protected void onResume() {
        super.onResume();
        boolean firstRun = SharedPreferencesHelper.getSharedPreferencesBoolean(this, SharedPreferencesHelper.Key.FIRST_RUN, true);
        if (firstRun) {
            SharedPreferencesHelper.putSharedPreferencesString(this, SharedPreferencesHelper.Key.IP, "192.168.4.1");
            SharedPreferencesHelper.putSharedPreferencesBoolean(this, SharedPreferencesHelper.Key.FIRST_RUN, false);
        } else {
            savedIp = SharedPreferencesHelper.getSharedPreferencesString(this, SharedPreferencesHelper.Key.IP, null);

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

    private class ConnectionTask extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected void onPreExecute() {
            animateUI();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            String jsonString = "";
            savedIp = SharedPreferencesHelper.getSharedPreferencesString(getApplicationContext(), SharedPreferencesHelper.Key.IP, null);
            jsonString = fetchJSON("http://" + savedIp + "/stemiData.json");
            // if jsonString object exists compare with one saved in SharedPreferences
            if (jsonString != null) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(jsonString);
                    String savedStemiID = SharedPreferencesHelper.getSharedPreferencesString(getApplicationContext(), SharedPreferencesHelper.Key.STEMI_ID, null);
                    if (Objects.equals(jsonObject.getString("stemiID"), savedStemiID)) {
                        Thread.sleep(2000);
                        return true;
                    } else {
                        // if IDs are not equal, set default STEMI values
                        initDefaultValues(jsonObject);
                        Thread.sleep(2000);
                        return true;
                    }
                } catch (JSONException | InterruptedException e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return false;
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean successful) {
            if (successful) {
                openJoystickActivity();
            } else {
                showErrorUI();
            }
        }
    }

}