package com.stemi.STEMIHexapod;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;


/**
 * Created by Mario on 24/03/16.
 */


public class JoystickL extends LinearLayout {

    protected float positionX = 0; // Touch x position
    protected float positionY = 0; // Touch y position
    protected float centerX = 0; // Center view x position
    protected float centerY = 0; // Center view y position
    protected float joystickRadius;
    private double lastAngle = 0;

    public byte power = 0;
    public byte angle = 0;
    public static JoystickMovement leftJoystick;

    private int d;

    private ImageView path_JoyLeftUp, path_JoyLeftDown, path_JoyLeftLeft, path_JoyLeftRight, joystickView, joystickPlus, joystickPath;
    protected float topAlpha, bottomAlpha, leftAlpha, rightAlpha;




    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).layout(l, t, r, b);
        }

        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;

    }

    public JoystickL(Context context){
        super(context);

    }

    public JoystickL(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.joystick_l, this, true);
        path_JoyLeftDown = (ImageView) findViewById(R.id.ivJoystickLeft_Down);
        path_JoyLeftUp = (ImageView) findViewById(R.id.ivJoystickLeft_Up);
        path_JoyLeftLeft = (ImageView) findViewById(R.id.ivJoystickLeft_Left);
        path_JoyLeftRight = (ImageView) findViewById(R.id.ivJoystickLeft_Right);
        joystickPath = (ImageView) findViewById(R.id.ivJoystickLeft);

        joystickView = new ImageView(context);
        joystickPlus = new ImageView(context);
        joystickPlus.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        joystickPlus.setImageResource(R.drawable.center);
        this.addView(joystickPlus);

        joystickView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        joystickView.setImageResource(R.drawable.joystick);
        this.addView(joystickView);

        leftJoystick = new JoystickMovement() {
            @Override
            public void leftJoystickMoved(int power, int angle) {

            }

            @Override
            public void rightJoystickMoved(int rotation) {

            }
        };

    }


    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        // before measure, get the center of view
        positionX = getWidth() / 2;
        positionY = getWidth() / 2;
        d = Math.min(xNew, yNew);
        joystickRadius = (int) (d / 2 * 0.75);

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and height
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

        positionX = event.getX();
        positionY = event.getY();

        double bounds = Math.sqrt(Math.pow(positionX - centerX, 2) + Math.pow(positionY - centerY, 2));

        if (bounds > joystickRadius) {
            positionX = (float) ((positionX - centerX) * joystickRadius / bounds + centerX);
            positionY = (float) ((positionY - centerY) * joystickRadius / bounds + centerY);

        }

        double xDistanceTop = getWidth() / 2 - positionX;
        double yDistanceTop = (getWidth() / 2 - joystickRadius) - positionY;
        float distanceTop = (float) (Math.sqrt(Math.pow(xDistanceTop, 2) + Math.pow(yDistanceTop, 2)) / joystickRadius);

        double xDistanceRight = (getWidth() - (getWidth() / 2 - joystickRadius)) - positionX;
        double yDistanceRight = getHeight() / 2 - positionY;
        float distanceRight = (float) (Math.sqrt(Math.pow(xDistanceRight, 2) + Math.pow(yDistanceRight, 2)) / joystickRadius);

        double xDistanceBottom = getWidth() / 2 - positionX;
        double yDistanceBottom = (getHeight() - (getWidth() / 2 - joystickRadius)) - positionY;
        float distanceBottom = (float) (Math.sqrt(Math.pow(xDistanceBottom, 2) + Math.pow(yDistanceBottom, 2)) / joystickRadius);

        double xDistanceLeft = (getWidth() / 2 - joystickRadius) - positionX;
        double yDistanceLeft = getHeight() / 2 - positionY;
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
                joystickView.animate().setDuration(200).x(positionX - joystickView.getWidth() / 2).y(positionY - joystickView.getWidth() / 2).start();
                path_JoyLeftRight.animate().setDuration(200).alpha(rightAlpha);
                path_JoyLeftDown.animate().setDuration(200).alpha(bottomAlpha);
                path_JoyLeftLeft.animate().setDuration(200).alpha(leftAlpha);
                path_JoyLeftUp.animate().setDuration(200).alpha(topAlpha);

                break;

            case MotionEvent.ACTION_MOVE:
                joystickView.animate().setDuration(0).x(positionX - joystickView.getWidth() / 2).y(positionY - joystickView.getWidth() / 2).start();
                path_JoyLeftRight.animate().setDuration(0).alpha(rightAlpha);
                path_JoyLeftDown.animate().setDuration(0).alpha(bottomAlpha);
                path_JoyLeftLeft.animate().setDuration(0).alpha(leftAlpha);
                path_JoyLeftUp.animate().setDuration(0).alpha(topAlpha);

                break;

            case MotionEvent.ACTION_UP:

                positionX = centerX;
                positionY = centerY;
                rightAlpha = 0;
                leftAlpha = 0;
                topAlpha = 0;
                bottomAlpha = 0;

                joystickView.animate().setDuration(200).x(positionX - joystickView.getWidth() / 2).y(positionY - joystickView.getWidth() / 2).start();
                path_JoyLeftRight.animate().setDuration(200).alpha(rightAlpha);
                path_JoyLeftDown.animate().setDuration(200).alpha(bottomAlpha);
                path_JoyLeftLeft.animate().setDuration(200).alpha(leftAlpha);
                path_JoyLeftUp.animate().setDuration(200).alpha(topAlpha);


                break;
        }

        power = (byte) getPower();
        angle = (byte) (getAngle() / 2);

        leftJoystick.leftJoystickMoved(power, angle);

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
        return (100 * Math.sqrt(Math.pow(positionX - centerX, 2) + Math.pow(positionY - centerY, 2)) / joystickRadius);
    }
}