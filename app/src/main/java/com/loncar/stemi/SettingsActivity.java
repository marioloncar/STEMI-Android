package com.loncar.stemi;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;

import java.util.Objects;

public class SettingsActivity extends AppCompatActivity {

    public Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.settings_layout);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ActionBar actionBar = getSupportActionBar();
        context = this;

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar));
        actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>Settings</font>"));

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        SettingsFragment settingsFragment = new SettingsFragment();
        fragmentTransaction.add(android.R.id.content, settingsFragment, "SETTINGS");
        fragmentTransaction.commit();


    }

    public static class SettingsFragment extends PreferenceFragment {

        static SharedPreferences prefs;
        String savedIp, hardwareVersion, stemiId;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            addPreferencesFromResource(R.xml.settings);
            final Preference ipAddr = findPreference("ipAddress");
            final Preference id = findPreference("stemiId");
            final Preference hwVersion = findPreference("hardwareVersion");
            final SharedPreferences manualIP = PreferenceManager.getDefaultSharedPreferences(getActivity());

            prefs = getActivity().getSharedPreferences("myPref", MODE_PRIVATE);
            savedIp = prefs.getString("ip", null);
            hardwareVersion = prefs.getString("version", null);
            stemiId = prefs.getString("stemiId", null);
            if (!Objects.equals(savedIp, "")) {
                ipAddr.setSummary(savedIp);
            }

            id.setSummary(stemiId);
            hwVersion.setSummary(hardwareVersion);


        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
