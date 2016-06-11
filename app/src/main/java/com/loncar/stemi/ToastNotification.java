package com.loncar.stemi;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

/**
 * Created by Mario on 23/05/16.
 */
public class ToastNotification extends RelativeLayout {

    public static TextView tvTitle, tvDesc;

    public ToastNotification(Context context) {
        super(context);
        System.out.println("KONTSTRUKTOR1");

    }

    public ToastNotification(Context context, AttributeSet attrs) {
        super(context, attrs);
        System.out.println("KONTSTRUKTOR2");
    }

    public ToastNotification(Context context, String title, String message) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.toast_long, this, true);
        System.out.println("KONTSTRUKTOR3");

        tvTitle = (TextView) findViewById(R.id.tvTitle);
        tvDesc = (TextView) findViewById(R.id.tvDesc);



    }

    public static void showLong(String title, String message) {
        tvTitle.setText(title);
        tvDesc.setText(message);
    }
}
