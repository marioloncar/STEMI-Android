package com.loncar.stemi;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;


/**
 * Created by Mario on 29/08/16.
 */
public class WalkstyleActivity extends AppCompatActivity {

    ListView lvStyles;
    Typeface tf;
    TextView tvStyleHeader;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.walkstyle_layout);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.navbar));
        actionBar.setTitle(Html.fromHtml("<font color='#24A8E0'>WALKING STYLE</font>"));

        tf = Typeface.createFromAsset(getAssets(),
                "fonts/ProximaNova-Regular.otf");

        lvStyles = (ListView) findViewById(R.id.lvStyles);
        tvStyleHeader = (TextView) findViewById(R.id.tvStyleHeader);

        String[] styleNames = new String[] { "STIL 1",
                "STIL 2",
                "STIL 3",
                "STIL 4"
        };

        tvStyleHeader.setTypeface(tf);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, android.R.id.text1, styleNames);
        lvStyles.setAdapter(adapter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        Intent goBack = new Intent(this, MainActivity.class);
        this.finish();
        startActivity(goBack);
        return super.onSupportNavigateUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent goBack = new Intent(this, MainActivity.class);
        this.finish();
        startActivity(goBack);
    }
}
