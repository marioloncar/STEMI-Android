package com.stemi.STEMIHexapod;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;

/**
 * Created by Mario on 13/05/16.
 */
public class Menu extends RelativeLayout {

    public ImageButton ibMovement, ibRotation, ibOrientation, ibHeight, ibCalibration, ibStyles, ibSettings;
    public ImageView ivBck, ivMenuActive;
    public Button bMenu;
    private MediaPlayer puk;
    private MediaPlayer tiu;


    private static ImageButton[] buttons;

    public Menu(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.menu, this, true);
        initButtons(context);
    }

    public Menu(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.menu, this, true);
        initButtons(context);
    }

    private void initButtons(Context context) {
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

        puk = MediaPlayer.create(context, R.raw.puk);
        tiu = MediaPlayer.create(context, R.raw.tiu);
    }

    public void closeMenu() {
        tiu.start();

        AnimationSet close = new AnimationSet(false);

        Animation leftMove = new TranslateAnimation(0, -10, 10, 0); //lijevi pomak
        leftMove.setDuration(50);

        Animation rightMove = new TranslateAnimation(0, 20, 10, 0); //desni pomak
        rightMove.setStartOffset(50);
        rightMove.setDuration(50);

        Animation leftMove2 = new TranslateAnimation(0, -20, 10, 0); //lijevi pomak
        leftMove2.setStartOffset(100);
        leftMove2.setDuration(50);

        Animation jumpUp = new TranslateAnimation(0, 10, 10, -20); //skok prema gore
        jumpUp.setStartOffset(150);
        jumpUp.setDuration(50);

        Animation fallDown = new TranslateAnimation(0, 10, -20, 30); //spustanje prema dolje
        fallDown.setStartOffset(200);
        fallDown.setDuration(200);

        Animation fadeOut = new AlphaAnimation(1, 0); //nestajanje
        fadeOut.setStartOffset(200);
        fadeOut.setDuration(200);

        for (ImageButton button : buttons) {
            button.setAnimation(fadeOut);
            button.setVisibility(View.INVISIBLE);
        }

        close.addAnimation(leftMove);
        close.addAnimation(rightMove);
        close.addAnimation(leftMove2);
        close.addAnimation(jumpUp);
        close.addAnimation(fallDown);
        close.addAnimation(fadeOut);

        ivMenuActive.startAnimation(close);
        ivMenuActive.setVisibility(View.INVISIBLE);
        ivBck.setAnimation(fadeOut);
        ivBck.setVisibility(View.INVISIBLE);
    }

    public void openMenu() {
        long delay = 0;

        puk.start();

        ivMenuActive.setVisibility(View.VISIBLE);
        ivBck.setVisibility(View.VISIBLE);

        AnimationSet set = new AnimationSet(false);
        Animation animation = new TranslateAnimation(0, 0, 30, -20);
        animation.setFillAfter(true);
        animation.setDuration(200);

        Animation animation2 = new AlphaAnimation(0, 1);
        animation2.setDuration(200);

        set.addAnimation(animation);
        set.addAnimation(animation2);

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
