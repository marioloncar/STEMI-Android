package com.stemi.STEMIHexapod.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.stemi.STEMIHexapod.R;

/**
 * Created by Mario on 11/08/16.
 */
public class IPActivity extends AppCompatActivity {

    private EditText et1, et2, et3, et4;
    private MenuItem menuItem;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ip);

        initActionBarWithTitle("IP address");

        et1 = (EditText) findViewById(R.id.et1);
        et2 = (EditText) findViewById(R.id.et2);
        et3 = (EditText) findViewById(R.id.et3);
        et4 = (EditText) findViewById(R.id.et4);
        TextView textView = (TextView) findViewById(R.id.textView);
        TextView textView2 = (TextView) findViewById(R.id.textView2);
        Button bResetIp = (Button) findViewById(R.id.bResetIp);


        Typeface tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        textView.setTypeface(tf);
        textView2.setTypeface(tf);
        bResetIp.setTypeface(tf);

        showSoftKeyboard();

        //Postavljanje polja na vrijednosti spremljenog IP-a
        prefs = getSharedPreferences("myPref", MODE_PRIVATE);
        String savedIp = prefs.getString("ip", null);

        et1.setTypeface(tf);
        et2.setTypeface(tf);
        et3.setTypeface(tf);
        et4.setTypeface(tf);

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
                menuItem.setEnabled(true);
                et1.setText("192");
                et2.setText("168");
                et3.setText("4");
                et4.setText("1");
                et4.requestFocus();
                et4.setSelection(et4.getText().length());
            }
        });

        // Change focus on fields
        et1.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                menuItem.setEnabled(true);
                if (et1.getText().toString().length() == 3)     //size as per your requirement
                    et2.requestFocus();

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
                menuItem.setEnabled(true);
                if (et2.getText().toString().length() == 3)     //size as per your requirement
                    et3.requestFocus();

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
                menuItem.setEnabled(true);
                if (et3.getText().toString().length() == 3)     //size as per your requirement
                    et4.requestFocus();

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
                // TODO Auto-generated method stub

            }

            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
            }

        });
        et4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                menuItem.setEnabled(true);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

    @Override
    public boolean onSupportNavigateUp() {
        hideSoftKeyboard();
        finish();
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        hideSoftKeyboard();
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(android.view.Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bar, menu);
        menuItem = menu.findItem(R.id.save);
        menuItem.setEnabled(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.save) {

            String ip1 = et1.getText().toString();
            String ip2 = et2.getText().toString();
            String ip3 = et3.getText().toString();
            String ip4 = et4.getText().toString();

            if ((ip1.trim().length() == 0) || (ip2.trim().length() == 0) || (ip3.trim().length() == 0) || (ip4.trim().length() == 0))
                showIPAlert();
            else {
                int field1 = Integer.parseInt(ip1);
                int field2 = Integer.parseInt(ip2);
                int field3 = Integer.parseInt(ip3);
                int field4 = Integer.parseInt(ip4);

                if ((field1 >= 0 && field1 <= 255) && (field2 >= 0 && field2 <= 255) && (field3 >= 0 && field3 <= 255) && (field4 >= 0 && field4 <= 255)) {
                    String customIp = ip1 + "." + ip2 + "." + ip3 + "." + ip4;
                    hideSoftKeyboard();
                    prefs.edit().putString("ip", customIp).apply();
                    finish();
                } else
                    showIPAlert();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void hideSoftKeyboard() {
        if (getCurrentFocus() != null) {
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
                .setMessage("IP fields must be in range 0-255")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .show();
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
