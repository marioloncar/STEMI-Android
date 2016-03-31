package mario.com.stemi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.Image;
import android.support.annotation.NonNull;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

/**
 * Created by Mario on 24/03/16.
 */
public class Joystick_R extends View {

    private Paint background;

    private Path mPath;
    private RectF mRectF;
    public byte rotation = 0;
    final float[] RADII = {32.0f, 32.0f, 32.0f, 32.0f, 32.0f, 32.0f, 32.0f, 32.0f};
    protected float xPosition = 0; // Touch x position
    protected float yPosition = 0; // Touch y position
    protected float centerX = 0; // Center view x position -> default: double
    protected float centerY = 0; // Center view y position -> default: double
    protected Paint mainCircle;
    protected Paint backgroundCircle;
    protected Paint line;
    private Paint button;
    protected byte onTouchCounter = 0;
    protected byte invalidateFrequency = 2;

    private final double RAD = 57.2957795;

    Bitmap src;
    Bitmap bitmap;


    protected float joystickRadius;
    protected int buttonRadius;
    private double lastAngle = 0;
    protected double sod = 0;
    protected double sod2 = 0;
    protected double sod3 = 0;

    public byte power = 0;
    public byte angle = 0;


    ImageView botunL;

    public Joystick_R(Context context) {
        super(context);

    }

    public Joystick_R(Context context, AttributeSet attrs) {
        super(context, attrs);
        initJoystickView();
    }

    public Joystick_R(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
        initJoystickView();
    }


    protected void initJoystickView() {


        line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setColor(Color.rgb(132, 71, 142));
        line.setStrokeWidth(8);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);

        final float scale = this.getResources().getDisplayMetrics().density;
        int p = (int) (60 * scale + 0.5f);

       // src = BitmapFactory.decodeResource(getResources(), R.drawable.joystick);

      //  bitmap = Bitmap.createScaledBitmap(src, p, p, true);

        background = new Paint(Paint.ANTI_ALIAS_FLAG);
        background.setColor(Color.WHITE);
        background.setStyle(Paint.Style.FILL);

        button = new Paint(Paint.ANTI_ALIAS_FLAG);
        button.setColor(Color.rgb(53, 194, 240));
        button.setStyle(Paint.Style.FILL);

        mPath = new Path();
        mRectF = new RectF();

        botunL = (ImageView) findViewById(R.id.ivJoystick_r_l);
        botunL.setImageAlpha(255);


    }

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;


        float bitmapWidth = bitmap.getWidth() / 2;
        float bitmapHeight = bitmap.getHeight() / 2;

/*
        // draw background and its rectangle
        mRectF.set((float) (centerX * 0.15), (float) (centerY * 0.70), (float) (centerX * 1.85),
                (float) (centerY * 1.30));
        mPath.addRoundRect(mRectF, RADII, Path.Direction.CCW);

        canvas.drawPath(mPath, background);
        canvas.drawPath(mPath, line);
        mPath.rewind();

        //   LEFT
        canvas.drawLine((float) (centerX - sod3), (float) centerY,
                (float) (centerX - sod2), (float) (centerY - sod), line);
        canvas.drawLine((float) (centerX - sod3), (float) centerY,
                (float) (centerX - sod2), (float) (centerY + sod), line);
        //    RIGHT
        canvas.drawLine((float) (centerX + sod3), (float) centerY,
                (float) (centerX + sod2), (float) (centerY - sod), line);
        canvas.drawLine((float) (centerX + sod3), (float) centerY,
                (float) (centerX + sod2), (float) (centerY + sod), line);

        canvas.drawCircle(xPosition, yPosition, buttonRadius, button);
        */

        canvas.drawBitmap(bitmap, xPosition - bitmapWidth, yPosition - bitmapHeight, null);

    }

    // Fixate yPosition
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        xPosition = event.getX();
        double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (abs > joystickRadius) {
            xPosition = (float) ((xPosition - centerX) * joystickRadius / abs + centerX);
        }

        if (onTouchCounter % invalidateFrequency == 0) invalidate();
        onTouchCounter++;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            xPosition = centerX;
            invalidate();
        }

        rotation = (byte) (Math.sin(Math.toRadians(getAngle())) * getPower());
/*
        if (getAngle() < 0f) {
            pathL.setAlpha((float) getPower() / 100);
            pathR.setAlpha(0f);
        } else {
            pathR.setAlpha((float) getPower() / 100);
            pathL.setAlpha(0f);
        }
        System.out.println("SNAGA!!!! -> " + getPower());
*/
        return true;
    }

    protected double getPower() {
        return (100 * Math.sqrt((xPosition - centerX)
                * (xPosition - centerX)) / joystickRadius);
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


}
