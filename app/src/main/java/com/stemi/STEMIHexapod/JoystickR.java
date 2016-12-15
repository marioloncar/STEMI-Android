package com.stemi.STEMIHexapod;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * Created by Mario on 24/03/16.
 */
public class JoystickR extends LinearLayout {

    protected float positionX = 0; // Touch x position
    protected float positionY = 0; // Touch y position
    protected float centerX = 0; // Center view x position
    protected float centerY = 0; // Center view y position

    protected float joystickRadius;
    private double lastAngle = 0;

    public byte rotation = 0;
    public static JoystickMovement rightJoystick;

    private ImageView path_JoyRightLeft, path_JoyRightRight, joystickView, joystickPlus, joystickPath;
    protected float leftAlpha, rightAlpha;

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(l, t, r, b);
        }
        centerX = getWidth() / 2;
        centerY = getHeight() / 2;
    }


    public JoystickR(Context context, AttributeSet attrs) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.joystick_r, this, true);

        path_JoyRightLeft = (ImageView) findViewById(R.id.ivJoystickRight_Left);
        path_JoyRightRight = (ImageView) findViewById(R.id.ivJoystickRight_Right);
        joystickPath = (ImageView) findViewById(R.id.ivJoystickRight);

        joystickPlus = new ImageView(context);
        joystickPlus.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        joystickPlus.setImageResource(R.drawable.center);
        this.addView(joystickPlus);

        joystickView = new ImageView(context);
        joystickView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        joystickView.setImageResource(R.drawable.joystick);
        this.addView(joystickView);

        rightJoystick = new JoystickMovement() {
            @Override
            public void leftJoystickMoved(int power, int angle) {

            }

            @Override
            public void rightJoystickMoved(int rotation) {

            }
        };

    }


    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        // before measure, get the center of view

        positionX = getWidth() / 2;
        positionY = getHeight() / 2;

        int d = Math.max(xNew, yNew);

        joystickRadius = (int) (d / 2 * 0.75);

    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and height
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        positionX = event.getX();

        double bounds = Math.sqrt(Math.pow(positionX - centerX, 2));

        if (bounds > joystickRadius) {
            positionX = (float) ((positionX - centerX) * joystickRadius / bounds + centerX);
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

                joystickView.animate().setDuration(200).x(positionX - joystickView.getWidth() / 2).start();
                path_JoyRightRight.animate().setDuration(200).alpha(rightAlpha);
                path_JoyRightLeft.animate().setDuration(200).alpha(leftAlpha);

                break;

            case MotionEvent.ACTION_MOVE:

                joystickView.animate().setDuration(0).x(positionX - joystickView.getWidth() / 2).start();
                path_JoyRightRight.animate().setDuration(0).alpha(rightAlpha);
                path_JoyRightLeft.animate().setDuration(0).alpha(leftAlpha);

                break;

            case MotionEvent.ACTION_UP:

                positionX = centerX;
                rightAlpha = 0;
                leftAlpha = 0;

                joystickView.animate().setDuration(200).x(positionX - joystickView.getWidth() / 2).start();
                path_JoyRightRight.animate().setDuration(200).alpha(rightAlpha);
                path_JoyRightLeft.animate().setDuration(200).alpha(leftAlpha);

                break;
        }

        rotation = (byte) (Math.sin(Math.toRadians(getAngle())) * getPower());

        rightJoystick.rightJoystickMoved(rotation);

        this.invalidate();

        return true;
    }

    protected double getAngle() {
        double radian = 57.2957795;
        if (positionX > centerX) {
            if (positionY < centerY) {
                return lastAngle = (Math.atan((positionY - centerY)
                        / (positionX - centerX))
                        * radian + 90);
            } else if (positionY > centerY) {
                return lastAngle = (Math.atan((positionY - centerY)
                        / (positionX - centerX)) * radian) + 90;
            } else {
                return lastAngle = 90;
            }
        } else if (positionX < centerX) {
            if (positionY < centerY) {
                return lastAngle = (Math.atan((positionY - centerY)
                        / (positionX - centerX))
                        * radian - 90);
            } else if (positionY > centerY) {
                return lastAngle = (Math.atan((positionY - centerY)
                        / (positionX - centerX)) * radian) - 90;
            } else {
                return lastAngle = -90;
            }
        } else {
            if (positionY <= centerY) {
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
        return (100 * Math.sqrt(Math.pow(positionX - centerX, 2)) / joystickRadius);
    }



}
