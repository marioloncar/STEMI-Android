package com.stemi.STEMIHexapod;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
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
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.polidea.rxandroidble.RxBleClient;
import com.polidea.rxandroidble.RxBleDevice;
import com.polidea.rxandroidble.exceptions.BleScanException;
import com.polidea.rxandroidble.internal.RxBleLog;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import io.fabric.sdk.android.Fabric;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;


/**
 * Created by Mario on 24/08/16.
 */
public class ConnectingActivity extends AppCompatActivity {

    private static final String TAG = "ConnectingActivity";

    private TextView tvConnectingTitle, tvConnectingHint;
    private Button bConnect, bChangeIP;
    private Typeface tf;
    private ImageView ivStemiIcon, ivProgressPath, ivProgress;
    private SharedPreferences prefs = null;
    private static Subscription scanSubscription;
    private static RxBleClient rxBleClient;
    private static RxBleDevice rxBleDevice;
    private static PublishSubject<Void> disconnectTriggerSubject = PublishSubject.create();

    public static RxBleClient getRxBleClient() {
        return rxBleClient;
    }

    public static void triggerDisconnect() {
        disconnectTriggerSubject.onNext(null);
    }

    private boolean noLocationPermission() {
        String permission = "android.permission.ACCESS_COARSE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res != PackageManager.PERMISSION_GRANTED);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics(), new Answers());
        rxBleClient = RxBleClient.create(this);
        RxBleClient.setLogLevel(RxBleLog.DEBUG);
        setContentView(R.layout.connecting_layout);

        if (noLocationPermission()) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs location access to use Bluetooth Low Energy.");
            builder.setMessage("Please grant location access so this app can detect beacons.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(dialog -> {
                if(Build.VERSION.SDK_INT >= 23) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                }
            });
            builder.show();
        }

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

        bConnect.setOnClickListener(v -> {
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

            Thread connectionThread = new Thread(new ConnectionRunnable());

            Log.w(TAG, " Start BLE scan!");

            scanSubscription = rxBleClient.scanBleDevices()
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(ConnectingActivity::clearSubscription)
                    .subscribe(rxBleScanResult -> {
                                rxBleDevice = rxBleScanResult.getBleDevice();
                                Log.w(TAG, " BLE SCAN > " + rxBleDevice.getMacAddress());
                                if(rxBleDevice.getMacAddress().equals("98:4F:EE:0C:FA:4F")) {  // Arduino 101 MAC
                                    connectionThread.interrupt();
                                    openMainActivity("98:4F:EE:0C:FA:4F");
                                }
                            },
                            throwable -> {
                                Log.w(TAG, " BLE SCAN ERROR > " + throwable.toString());
                                onScanFailure(throwable);
                            });

            connectionThread.start();
        });

        bChangeIP.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), IPActivity.class);
            startActivity(i);
        });
    }

    private void openMainActivity(String macAddress) {
        finish();
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.putExtra("ARDUINO_BT_MAC_ADDRESS", macAddress);
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
            prefs.edit().putBoolean("firstrun", false).apply();
        }
    }

    private boolean isScanning() {
        return scanSubscription != null;
    }

    @Override
    public void onPause() {
        super.onPause();

        if (isScanning()) {
            scanSubscription.unsubscribe();
        }
    }

    private static void clearSubscription() {
        scanSubscription = null;
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
            String line;

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

    private class ConnectionRunnable implements Runnable {
        @Override
        public void run() {
            try {
                Thread.sleep(8000);
                runOnUiThread(new ConnectionFailedRunnable());
            } catch (InterruptedException e) {
            }
        }
    }

    private void onScanFailure(Throwable throwable) {
        if (throwable instanceof BleScanException) {
            handleBleScanException((BleScanException) throwable);
        }
    }

    private void handleBleScanException(BleScanException bleScanException) {

        switch (bleScanException.getReason()) {
            case BleScanException.BLUETOOTH_NOT_AVAILABLE:
                Toast.makeText(ConnectingActivity.this, "Bluetooth is not available", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_DISABLED:
                Toast.makeText(ConnectingActivity.this, "Enable bluetooth and try again", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_PERMISSION_MISSING:
                Toast.makeText(ConnectingActivity.this,
                        "On Android 6.0 location permission is required. Implement Runtime Permissions", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.LOCATION_SERVICES_DISABLED:
                Toast.makeText(ConnectingActivity.this, "Location services needs to be enabled on Android 6.0", Toast.LENGTH_SHORT).show();
                break;
            case BleScanException.BLUETOOTH_CANNOT_START:
            default:
                Toast.makeText(ConnectingActivity.this, "Unable to start scanning", Toast.LENGTH_SHORT).show();
                break;
        }
    }


    private class ConnectionFailedRunnable implements Runnable {
        @Override
        public void run() {
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
    }

}