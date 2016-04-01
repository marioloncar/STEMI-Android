package com.loncar.stemi;

import android.app.Activity;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.loncar.stemi.R;


/**
 * Created by Mario on 24/03/16.
 */

public class Joystick_L extends RelativeLayout {
    // Constants


    private final double RAD = 57.2957795;
    protected byte onTouchCounter = 0;
    protected byte invalidateFrequency = 2;

    // Variables
    protected float xPosition = 0; // Touch x position
    protected float yPosition = 0; // Touch y position
    protected float centerX = 0; // Center view x position -> default: double
    protected float centerY = 0; // Center view y position -> default: double

    protected float joystickRadius;
    private double lastAngle = 0;

    int d;

    ImageView path_left_Up, path_left_Down, path_left_L, path_left_R, joystickView, plus, path;
    protected  float topAlpha, bottomAlpha, leftAlpha, rightAlpha;


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        for(int i = 0 ; i < getChildCount() ; i++){
            getChildAt(i).layout(l, t, r, b);
        }

        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;

    }

    public Joystick_L(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.joystick_l, this, true);
    }

    public Joystick_L(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.joystick_l, this, true);

        path_left_Down = (ImageView) findViewById(R.id.ivJoystick_l_down);
        path_left_Up = (ImageView) findViewById(R.id.ivJoystick_l_up);
        path_left_L = (ImageView) findViewById(R.id.ivJoystick_l_l);
        path_left_R = (ImageView) findViewById(R.id.ivJoystick_l_r);
        path = (ImageView) findViewById(R.id.ivJoystick_l);

        joystickView = new ImageView(context);
        plus = new ImageView(context);
        plus.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        plus.setImageResource(R.drawable.joystick_center);
        this.addView(plus);

        joystickView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        joystickView.setImageResource(R.drawable.joystick);
        this.addView(joystickView);




    }

    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        // before measure, get the center of view
        xPosition = getWidth() / 2;
        yPosition = getWidth() / 2;
        d = Math.min(xNew, yNew);
        joystickRadius = (int) (d / 2 * 0.8);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and
        // height
        d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));

        setMeasuredDimension(d, d);

    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    private int measure(int measureSpec) {
        int result;

        // Decode the measurement specifications.
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.UNSPECIFIED) {
            // Return a default size of 200 if no bounds are specified.
            result = 200;
        } else {
            // As you want to fill the available space
            // always return the full available bounds.
            result = specSize;
        }
        return result;
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {

        xPosition = event.getX();
        yPosition = event.getY();
        float abs = (float) Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (abs > joystickRadius) {
            xPosition = ((xPosition - centerX) * joystickRadius / abs + centerX);
            yPosition = ((yPosition - centerY) * joystickRadius / abs + centerY);
        }

        if (onTouchCounter % invalidateFrequency == 0) invalidate();
        onTouchCounter++;

        if (event.getAction() == MotionEvent.ACTION_UP) {

            //Pocetak bloka za animaciju
            xPosition = centerX;
            yPosition = centerY;
            path_left_R.setAlpha(0f);
            path_left_Down.setAlpha(0f);
            path_left_L.setAlpha(0f);
            path_left_Up.setAlpha(0f);
            //Kraj bloka za animaciju
            invalidate();
        }

        double xDistanceTop = getWidth() / 2 - xPosition;
        double yDistanceTop = (getWidth()/2 - joystickRadius) - yPosition;
        float distanceTop = (float)(Math.sqrt(Math.pow(xDistanceTop,2) + Math.pow(yDistanceTop,2)) / joystickRadius);

        double xDistanceRight = (getWidth() - (getWidth()/2 - joystickRadius)) - xPosition;
        double yDistanceRight = getHeight()/2 - yPosition;
        float distanceRight = (float)(Math.sqrt(Math.pow(xDistanceRight,2) + Math.pow(yDistanceRight,2)) / joystickRadius);

        double xDistanceBottom = getWidth()/2 - xPosition;
        double yDistanceBottom = (getHeight() - (getWidth()/2 - joystickRadius)) - yPosition;
        float distanceBottom = (float)(Math.sqrt(Math.pow(xDistanceBottom,2) + Math.pow(yDistanceBottom,2)) / joystickRadius);

        double xDistanceLeft = (getWidth()/2 - joystickRadius) - xPosition;
        double yDistanceLeft = getHeight()/2 - yPosition;
        float distanceLeft = (float)(Math.sqrt(Math.pow(xDistanceLeft,2) + Math.pow(yDistanceLeft,2)) / joystickRadius);

        if (distanceTop > 1) {
            distanceTop = 1;
        }
        else if (distanceRight > 1) {
            distanceRight = 1;
        }
        else if (distanceBottom > 1) {
            distanceBottom = 1;
        }
        else if (distanceLeft > 1){
            distanceLeft = 1;
        }


        topAlpha = 1 - distanceTop;
        rightAlpha = 1 - distanceRight;
        bottomAlpha = 1 - distanceBottom;
        leftAlpha = 1 - distanceLeft;

        //Pocetak bloka za animaciju
        joystickView.setX(xPosition - joystickView.getWidth() / 2);
        joystickView.setY(yPosition - joystickView.getWidth() / 2);
        path_left_R.setAlpha(rightAlpha);
        path_left_Down.setAlpha(bottomAlpha);
        path_left_L.setAlpha(leftAlpha);
        path_left_Up.setAlpha(topAlpha);
        //Kraj bloka za animaciju


        return true;
    }

    protected double getAngle() {
        if (xPosition > centerX) {
            if (yPosition < centerY) {
                return lastAngle = (Math.atan((yPosition - centerY)
                        / (xPosition - centerX))
                        * RAD + 90);
            } else if (yPosition > centerY) {
                return lastAngle = (Math.atan((yPosition - centerY)
                        / (xPosition - centerX)) * RAD) + 90;
            } else {
                return lastAngle = 90;
            }
        } else if (xPosition < centerX) {
            if (yPosition < centerY) {
                return lastAngle = (Math.atan((yPosition - centerY)
                        / (xPosition - centerX))
                        * RAD - 90);
            } else if (yPosition > centerY) {
                return lastAngle = (Math.atan((yPosition - centerY)
                        / (xPosition - centerX)) * RAD) - 90;
            } else {
                return lastAngle = -90;
            }
        } else {
            if (yPosition <= centerY) {
                return lastAngle = 0;
            } else {
                if (lastAngle < 0) {
                    return lastAngle = -180;
                } else {
                    return lastAngle = 180;
                }
            }
        }
    }

    protected double getPower() {
        return (100 * Math.sqrt(Math.pow(xPosition - centerX,2) + Math.pow(yPosition - centerY,2)) / joystickRadius);
    }



}