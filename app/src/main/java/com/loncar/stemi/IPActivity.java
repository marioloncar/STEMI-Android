package com.loncar.stemi;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Objects;
import java.util.Set;

/**
 * Created by Mario on 11/08/16.
 */
public class IPActivity extends AppCompatActivity {

    EditText et1, et2, et3, et4;
    String ip1, ip2, ip3, ip4, customIp, savedIp;
    int field1, field2, field3, field4;
    Typeface tf;
    TextView textView, textView2;
    Button bResetIp;
    SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ip_layout);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar));
        actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>IP address</font>"));

        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);
        et3 = (EditText) findViewById(R.id.et3);
        et4 = (EditText) findViewById(R.id.et4);
        textView = (TextView) findViewById(R.id.textView);
        textView2 = (TextView) findViewById(R.id.textView2);
        bResetIp = (Button) findViewById(R.id.bResetIp);

        tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        textView.setTypeface(tf);
        textView2.setTypeface(tf);

        showSoftKeyboard();

        //Postavljanje polja na vrijednosti spremljenog IP-a
        prefs = getSharedPreferences("myPref", MODE_PRIVATE);
        savedIp = prefs.getString("ip", null);

        if (savedIp != null) {
            String[] parts = savedIp.split("\\.");
            String part1 = parts[0];
            String part2 = parts[1];
            String part3 = parts[2];
            String part4 = parts[3];
            et1.setText(part1);
            et2.setText(part2);
            et3.setText(part3);
            et4.setText(part4);
            et4.requestFocus();
        }

        bResetIp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et1.setText("192");
                et2.setText("168");
                et3.setText("4");
                et4.setText("1");
                et4.requestFocus();
            }
        });

        // izmjena fokusa na poljima
        et1.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (et1.getText().toString().length() == 3)     //size as per your requirement
                {
                    et2.requestFocus();
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub

            }

            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }

        });

        et2.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (et2.getText().toString().length() == 3)     //size as per your requirement
                {
                    et3.requestFocus();
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub

            }

            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }

        });
        et3.addTextChangedListener(new TextWatcher() {

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                if (et3.getText().toString().length() == 3)     //size as per your requirement
                {
                    et4.requestFocus();
                }
            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub

            }

            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }

        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        hideSoftKeyboard();
        this.finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

            ip1 = et1.getText().toString();
            ip2 = et2.getText().toString();
            ip3 = et3.getText().toString();
            ip4 = et4.getText().toString();

            if (Objects.equals(ip1, "") || Objects.equals(ip2, "") || Objects.equals(ip3, "") || Objects.equals(ip4, "")) {
                field1 = 0;
                field2 = 0;
                field3 = 0;
                field4 = 0;
            } else {
                field1 = Integer.parseInt(ip1);
                field2 = Integer.parseInt(ip2);
                field3 = Integer.parseInt(ip3);
                field4 = Integer.parseInt(ip4);
            }
            if ((field1 >= 1 && field1 <= 255) && (field2 >= 1 && field2 <= 255) && (field3 >= 1 && field3 <= 255) && (field4 >= 1 && field4 <= 255)) {
                customIp = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
                hideSoftKeyboard();
                prefs.edit().putString("ip", customIp).apply();
                finish();
            } else {
                showIPAlert();

            }
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    public void hideSoftKeyboard() {
        if(getCurrentFocus()!=null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }
    private void showSoftKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
    }

    private void showIPAlert() {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage("IP fields must be in range 1-255")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
    }
}
