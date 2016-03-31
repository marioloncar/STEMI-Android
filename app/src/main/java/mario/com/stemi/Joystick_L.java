package mario.com.stemi;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;


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
    protected Paint mainCircle;
    protected Paint backgroundCircle;
    protected Paint line;
    private Paint button;

    Bitmap src;
    Bitmap bitmap, levi;


    protected float joystickRadius;
    protected int buttonRadius;
    private double lastAngle = 0;
    protected double sod = 0;
    protected double sod2 = 0;
    protected double sod3 = 0;

    public byte power = 0;
    public byte angle = 0;

    ImageView path_left_Up, path_left_Down, path_left_L, path_left_R;


    public Joystick_L(Context context) {
        this(context, null);
        initJoystickView(context);
        System.out.println("PRVI KONSTRUKTOR");

    }

    public Joystick_L(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        initJoystickView(context);
        System.out.println("DRUGI KONSTRUKTOR");
    }

    public Joystick_L(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);

        initJoystickView(context);
        System.out.println("TRECI KONSTRUKTOR");
    }


    protected void initJoystickView(Context context) {


        final float scale = this.getResources().getDisplayMetrics().density;
        int p = (int) (60 * scale + 0.5f);


        src = BitmapFactory.decodeResource(getResources(), R.drawable.joystick);

        bitmap = Bitmap.createScaledBitmap(src, p, p, true);


        //  ((Activity)getContext()).getLayoutInflater().inflate(R.layout.joystick_l, this, true);


        path_left_Down = (ImageView) findViewById(R.id.ivJoystick_l_down);
        System.out.println("SIRINA KURCA  -> " + path_left_Down);


    }


    @Override
    protected void onSizeChanged(int xNew, int yNew, int xOld, int yOld) {
        super.onSizeChanged(xNew, yNew, xOld, yOld);
        // before measure, get the center of view
        xPosition = getWidth() / 2;
        yPosition = getWidth() / 2;
        int d = Math.min(xNew, yNew);
        buttonRadius = (int) (d / 2 * 0.25);
        joystickRadius = (int) (d / 2 * 0.75);

        // helper measures for drawing arrows
        sod = joystickRadius / 3.75; // sod - sixth of diagonal
        sod2 = sod * 2;
        sod3 = sod * 3;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // setting the measured values to resize the view to a certain width and
        // height
        int d = Math.min(measure(widthMeasureSpec), measure(heightMeasureSpec));

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
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;

        float bitmapWidth = bitmap.getWidth() / 2;
        float bitmapHeight = bitmap.getHeight() / 2;

        // drawing the move button
        //canvas.drawCircle(xPosition, yPosition, buttonRadius, button);
        canvas.drawBitmap(bitmap, xPosition - bitmapWidth, yPosition - bitmapHeight, null);
        //d.draw(canvas);
        //  canvas.draw(path_left_Down, 0, 0, null);


    }

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        xPosition = (int) event.getX();
        yPosition = (int) event.getY();
        double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (abs > joystickRadius) {
            xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
            yPosition = (int) ((yPosition - centerY) * joystickRadius / abs + centerY);
        }

        if (onTouchCounter % invalidateFrequency == 0) invalidate();
        onTouchCounter++;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            xPosition = (int) centerX;
            yPosition = (int) centerY;
            invalidate();
        }

//        power = (byte) getPower();
        //      angle = (byte) (getAngle() / 2);
        //path_left_Down.setAlpha(1.0f);
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
        return (100 * Math.sqrt((xPosition - centerX)
                * (xPosition - centerX) + (yPosition - centerY)
                * (yPosition - centerY)) / joystickRadius);
    }


}
