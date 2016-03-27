package mario.com.stemi;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

/**
 * Created by Mario on 24/03/16.
 */
public class Joystick_L extends View {
    // Constants

    private final double RAD = 57.2957795;
    protected byte onTouchCounter = 0;
    protected byte invalidateFrequency = 2;

    // Variables
    protected int xPosition = 0; // Touch x position
    protected int yPosition = 0; // Touch y position
    protected double centerX = 0; // Center view x position
    protected double centerY = 0; // Center view y position
    protected Paint mainCircle;
    protected Paint backgroundCircle;
    protected Paint line;
    private Paint button;

    //Drawable d = ContextCompat.getDrawable(getContext(), R.drawable.joystick);
   // Bitmap src = BitmapFactory.decodeResource(getResources(), R.drawable.joystick);
  //  Bitmap bitmap = Bitmap.createScaledBitmap(src, 200, 200, true);
    protected int joystickRadius;
    protected int buttonRadius;
    private int lastAngle = 0;
    protected double sod = 0;
    protected double sod2 = 0;
    protected double sod3 = 0;

    public byte power = 0;
    public byte angle = 0;

    public Joystick_L(Context context) {
        super(context);
    }

    public Joystick_L(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJoystickView();
    }

    public Joystick_L(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initJoystickView();
    }

    protected void initJoystickView() {
        backgroundCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        backgroundCircle.setColor(Color.WHITE);
        backgroundCircle.setStyle(Paint.Style.FILL);

        mainCircle = new Paint(Paint.ANTI_ALIAS_FLAG);
        mainCircle.setColor(Color.rgb(132, 71, 142));
        mainCircle.setStrokeWidth(8);
        mainCircle.setStyle(Paint.Style.STROKE);

        button = new Paint(Paint.ANTI_ALIAS_FLAG);
        button.setColor(Color.rgb(53, 194, 240));
        button.setStyle(Paint.Style.FILL);


        line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setColor(Color.rgb(132, 71, 142));
        line.setStrokeWidth(8);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);


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
        //super.onDraw(canvas);
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;

        // drawing the move button
        canvas.drawCircle(xPosition, yPosition, buttonRadius, button);
       // canvas.drawBitmap(bitmap, xPosition, yPosition , button );
        //d.draw(canvas);

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

        return true;
    }

    protected int getAngle() {
        if (xPosition > centerX) {
            if (yPosition < centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY)
                        / (xPosition - centerX))
                        * RAD + 90);
            } else if (yPosition > centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY)
                        / (xPosition - centerX)) * RAD) + 90;
            } else {
                return lastAngle = 90;
            }
        } else if (xPosition < centerX) {
            if (yPosition < centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY)
                        / (xPosition - centerX))
                        * RAD - 90);
            } else if (yPosition > centerY) {
                return lastAngle = (int) (Math.atan((yPosition - centerY)
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

    protected int getPower() {
        return (int) (100 * Math.sqrt((xPosition - centerX)
                * (xPosition - centerX) + (yPosition - centerY)
                * (yPosition - centerY)) / joystickRadius);
    }

}
