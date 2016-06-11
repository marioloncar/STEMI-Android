package com.loncar.stemi;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by Mario on 13/05/16.
 */
public class Menu extends RelativeLayout {

    public static ImageButton ibMovement, ibRotation, ibOrientation, ibHeight, ibCalibration, ibStyles, ibSettings;
    public static ImageView ivBck, ivMenuActive;
    public static Button bMenu;

    public static ImageButton[] buttons;

    public Menu(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.menu, this, true);
    }

    public Menu(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.menu, this, true);

        ibMovement = (ImageButton) findViewById(R.id.ibMovement);
        ibRotation = (ImageButton) findViewById(R.id.ibRotation);
        ibOrientation = (ImageButton) findViewById(R.id.ibOrientation);
        ibHeight = (ImageButton) findViewById(R.id.ibHeight);
        ibCalibration = (ImageButton) findViewById(R.id.ibCalibration);
        ibStyles = (ImageButton) findViewById(R.id.ibWalkingStyle);
        ibSettings = (ImageButton) findViewById(R.id.ibSettings);
        ivBck = (ImageView) findViewById(R.id.ivBck);
        ivMenuActive = (ImageView) findViewById(R.id.ivMenuActive);
        bMenu = (Button) findViewById(R.id.bMenu);

        buttons = new ImageButton[]{ibMovement, ibRotation, ibOrientation, ibHeight, ibCalibration, ibStyles, ibSettings};

    }

    public static void closeMenu() {
        Animation fadeOut = new AlphaAnimation(1, 0);
        fadeOut.setInterpolator(new AccelerateInterpolator());
        fadeOut.setDuration(200);
        for (ImageButton button : buttons) {
            button.setAnimation(fadeOut);
            button.setVisibility(View.INVISIBLE);
        }
        ivMenuActive.setAnimation(fadeOut);
        ivMenuActive.setVisibility(View.INVISIBLE);
        ivBck.setAnimation(fadeOut);
        ivBck.setVisibility(View.INVISIBLE);
    }

    public static void openMenu() {
        long delay = 0;
        ivMenuActive.setVisibility(View.VISIBLE);
        ivBck.setVisibility(View.VISIBLE);

        AnimationSet set = new AnimationSet(false);//false means don't share interpolators
        Animation animation = new TranslateAnimation(0, 0, 30, -20);
        animation.setFillAfter(true);
        animation.setDuration(1000);

        Animation animation2 = new AlphaAnimation(0, 1);
        animation2.setDuration(1000);

//        Animation animation3 = new TranslateAnimation(0, 0, -20, 0);
//        animation3.setDuration(1000);
//        animation3.setFillAfter(true);

        set.addAnimation(animation);
        set.addAnimation(animation2);
//        set.addAnimation(animation3);

        ivMenuActive.startAnimation(set);


        for (ImageButton button : buttons) {
            Animation fadeIn = new AlphaAnimation(0, 1);
            fadeIn.setInterpolator(new DecelerateInterpolator());
            fadeIn.setDuration(500);
            fadeIn.setStartOffset(delay);
            button.setAnimation(fadeIn);
            button.setVisibility(View.VISIBLE);
            delay += 50;

        }
    }

}
