package mario.com.stemi;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by Mario on 24/03/16.
 */
public class Joystick_R extends Joystick_L{
    Paint line;
    Paint background;
    Paint button;
    Path mPath;
    RectF mRectF;
    public byte rotation = 0;
    final float[] RADII = {32.0f, 32.0f, 32.0f, 32.0f, 32.0f, 32.0f, 32.0f, 32.0f};

    public Joystick_R(Context context) {
        super(context);
    }

    public Joystick_R(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Joystick_R(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
    }

    @Override
    protected void initJoystickView() {
        line = new Paint(Paint.ANTI_ALIAS_FLAG);
        line.setColor(Color.rgb(132, 71, 142));
        line.setStrokeWidth(8);
        line.setStyle(Paint.Style.STROKE);
        line.setStrokeCap(Paint.Cap.ROUND);

        background = new Paint(Paint.ANTI_ALIAS_FLAG);
        background.setColor(Color.WHITE);
        background.setStyle(Paint.Style.FILL);

        button = new Paint(Paint.ANTI_ALIAS_FLAG);
        button.setColor(Color.rgb(53, 194, 240));
        button.setStyle(Paint.Style.FILL);

        mPath = new Path();
        mRectF = new RectF();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // super.onDraw(canvas);
        centerX = (getWidth()) / 2;
        centerY = (getHeight()) / 2;
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
*/
        canvas.drawCircle(xPosition, yPosition, buttonRadius, button);
    }

    // Fixate yPosition
    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        xPosition = (int) event.getX();
        //yPosition = (int) event.getY();
        double abs = Math.sqrt((xPosition - centerX) * (xPosition - centerX)
                + (yPosition - centerY) * (yPosition - centerY));
        if (abs > joystickRadius) {
            xPosition = (int) ((xPosition - centerX) * joystickRadius / abs + centerX);
        }

        if(onTouchCounter % invalidateFrequency == 0) invalidate();
        onTouchCounter++;

        if (event.getAction() == MotionEvent.ACTION_UP) {
            xPosition = (int) centerX;
            invalidate();
        }

        rotation = (byte) (int) (Math.sin(Math.toRadians(getAngle())) * getPower());

        return true;
    }
}
