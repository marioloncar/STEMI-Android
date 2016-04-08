package com.loncar.stemi;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Mario on 24/03/16.
 */
public class Joystick_R extends LinearLayout {

    private final double RAD = 57.2957795;

    protected float xPosition = 0; // Touch x position
    protected float yPosition = 0; // Touch y position
    protected float centerX = 0; // Center view x position -> default: double
    protected float centerY = 0; // Center view y position -> default: double

    protected float joystickRadius;
    private double lastAngle = 0;

    public byte rotation = 0;

    int d;

    ImageView path_right_L, path_right_R, joystickView, plus, path;
    protected float leftAlpha, rightAlpha;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(l, t, r, b);
        }
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
    }

    public Joystick_R(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.joystick_r, this, true);
    }

    public Joystick_R(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.joystick_r, this, true);

        path_right_L = (ImageView) findViewById(R.id.ivJoystick_r_l);
        path_right_R = (ImageView) findViewById(R.id.ivJoystick_r_r);
        path = (ImageView) findViewById(R.id.ivJoystick_r);


        plus = new ImageView(context);
        plus.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        plus.setImageResource(R.drawable.joystick_center);
        this.addView(plus);

        joystickView = new ImageView(context);
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

    // Fixate yPosition
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        xPosition = event.getX();

        double bounds = Math.sqrt(Math.pow(xPosition - centerX, 2));

        if (bounds > joystickRadius) {
            xPosition = (float) ((xPosition - centerX) * joystickRadius / bounds + centerX);
        }

        if (getAngle() < 0) {
            leftAlpha = (float) (getPower() / 100);
            rightAlpha = 0;
        } else {
            leftAlpha = 0;
            rightAlpha = (float) (getPower() / 100);
        }

        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:

                joystickView.animate().setDuration(200).x(xPosition - joystickView.getWidth() / 2).start();
                path_right_R.animate().setDuration(200).alpha(rightAlpha);
                path_right_L.animate().setDuration(200).alpha(leftAlpha);

                break;

            case MotionEvent.ACTION_MOVE:

                joystickView.animate().setDuration(0).x(xPosition - joystickView.getWidth() / 2).start();
                path_right_R.animate().setDuration(0).alpha(rightAlpha);
                path_right_L.animate().setDuration(0).alpha(leftAlpha);

                break;

            case MotionEvent.ACTION_UP:

                xPosition = centerX;
                rightAlpha = 0;
                leftAlpha = 0;

                joystickView.animate().setDuration(200).x(xPosition - joystickView.getWidth() / 2).start();
                path_right_R.animate().setDuration(200).alpha(rightAlpha);
                path_right_L.animate().setDuration(200).alpha(leftAlpha);

                break;
        }

        rotation = (byte) (int) (Math.sin(Math.toRadians(getAngle())) * getPower());

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
        return (100 * Math.sqrt(Math.pow(xPosition - centerX, 2)) / joystickRadius);
    }

}
