package com.loncar.stemi;

import android.view.LayoutInflater;


import android.content.Context;

import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

import android.widget.ImageView;
import android.widget.LinearLayout;


/**
 * Created by Mario on 24/03/16.
 */

public class Joystick_L extends LinearLayout {

    private final double RAD = 57.2957795;

    protected float xPosition = 0; // Touch x position
    protected float yPosition = 0; // Touch y position
    protected float centerX = 0; // Center view x position -> default: double
    protected float centerY = 0; // Center view y position -> default: double

    protected float joystickRadius;
    private double lastAngle = 0;

    public byte power = 0;
    public byte angle = 0;


    int d;

    ImageView path_left_Up, path_left_Down, path_left_L, path_left_R, joystickView, plus, path;
    protected float topAlpha, bottomAlpha, leftAlpha, rightAlpha;


    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        for (int i = 0; i < getChildCount(); i++) {
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

        float bounds = (float) Math.sqrt(Math.pow(xPosition - centerX, 2) + Math.pow(yPosition - centerY, 2));

        if (bounds > joystickRadius) {
            xPosition = ((xPosition - centerX) * joystickRadius / bounds + centerX);
            yPosition = ((yPosition - centerY) * joystickRadius / bounds + centerY);

        }

        double xDistanceTop = getWidth() / 2 - xPosition;
        double yDistanceTop = (getWidth() / 2 - joystickRadius) - yPosition;
        float distanceTop = (float) (Math.sqrt(Math.pow(xDistanceTop, 2) + Math.pow(yDistanceTop, 2)) / joystickRadius);

        double xDistanceRight = (getWidth() - (getWidth() / 2 - joystickRadius)) - xPosition;
        double yDistanceRight = getHeight() / 2 - yPosition;
        float distanceRight = (float) (Math.sqrt(Math.pow(xDistanceRight, 2) + Math.pow(yDistanceRight, 2)) / joystickRadius);

        double xDistanceBottom = getWidth() / 2 - xPosition;
        double yDistanceBottom = (getHeight() - (getWidth() / 2 - joystickRadius)) - yPosition;
        float distanceBottom = (float) (Math.sqrt(Math.pow(xDistanceBottom, 2) + Math.pow(yDistanceBottom, 2)) / joystickRadius);

        double xDistanceLeft = (getWidth() / 2 - joystickRadius) - xPosition;
        double yDistanceLeft = getHeight() / 2 - yPosition;
        float distanceLeft = (float) (Math.sqrt(Math.pow(xDistanceLeft, 2) + Math.pow(yDistanceLeft, 2)) / joystickRadius);

        if (distanceTop > 1) {
            distanceTop = 1;
        } else if (distanceRight > 1) {
            distanceRight = 1;
        } else if (distanceBottom > 1) {
            distanceBottom = 1;
        } else if (distanceLeft > 1) {
            distanceLeft = 1;
        }


        topAlpha = 1 - distanceTop;
        rightAlpha = 1 - distanceRight;
        bottomAlpha = 1 - distanceBottom;
        leftAlpha = 1 - distanceLeft;


        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                joystickView.animate().setDuration(200).x(xPosition - joystickView.getWidth() / 2).y(yPosition - joystickView.getWidth() / 2).start();
                path_left_R.animate().setDuration(200).alpha(rightAlpha);
                path_left_Down.animate().setDuration(200).alpha(bottomAlpha);
                path_left_L.animate().setDuration(200).alpha(leftAlpha);
                path_left_Up.animate().setDuration(200).alpha(topAlpha);

                break;

            case MotionEvent.ACTION_MOVE:

                joystickView.animate().setDuration(0).x(xPosition - joystickView.getWidth() / 2).y(yPosition - joystickView.getWidth() / 2).start();
                path_left_R.animate().setDuration(0).alpha(rightAlpha);
                path_left_Down.animate().setDuration(0).alpha(bottomAlpha);
                path_left_L.animate().setDuration(0).alpha(leftAlpha);
                path_left_Up.animate().setDuration(0).alpha(topAlpha);

                break;

            case MotionEvent.ACTION_UP:

                xPosition = centerX;
                yPosition = centerY;
                rightAlpha = 0;
                leftAlpha = 0;
                topAlpha = 0;
                bottomAlpha = 0;

                joystickView.animate().setDuration(200).x(xPosition - joystickView.getWidth() / 2).y(yPosition - joystickView.getWidth() / 2).start();
                path_left_R.animate().setDuration(200).alpha(rightAlpha);
                path_left_Down.animate().setDuration(200).alpha(bottomAlpha);
                path_left_L.animate().setDuration(200).alpha(leftAlpha);
                path_left_Up.animate().setDuration(200).alpha(topAlpha);

                break;
        }

        power = (byte) getPower();
        angle = (byte) (getAngle() / 2);

        this.invalidate();


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
        return (100 * Math.sqrt(Math.pow(xPosition - centerX, 2) + Math.pow(yPosition - centerY, 2)) / joystickRadius);
    }


}