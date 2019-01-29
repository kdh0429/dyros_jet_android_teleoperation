package org.ros.android.android_tutorial_teleop;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

class MyView extends View {

    private int step_number;
    public int current_step;
    private float[] array_0;
    private List<Float> list = new ArrayList<>();
    private int len;
    private boolean isfirst = true;

    private int i;

    private float cor_x, cor_y;
    private float a;
    private float b;

    private ScaleGestureDetector mScaleDetector;
    private float mScaleFactor = 1.f;
    private float scale;

    private float X;
    private float Y;
    private float mPosX;
    private float mPosY;
    private float mLastTouchX;
    private float mLastTouchY;
    private static final int INVALID_POINTER_ID = -1;
    // The ‘active pointer’ is the one currently moving our object.
    private int mActivePointerId = INVALID_POINTER_ID;
    private boolean isdrawfirst=true;
    private boolean isrefresh;


    private float minScaleFactor=0.1f;
    private float maxScaleFactor=5.0f;


    public MyView(Context context) { super(context);}

    public MyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyView(Context context, AttributeSet attrs, int defStyle) { super(context, attrs, defStyle); }

    public void SetData(int step_number, float[] array) {

        isfirst = false;
        isrefresh = false;

        this.step_number = step_number;
        len = array.length;

        list.clear();
        for (int j = 0; j < len; j++) {
            list.add(array[j]);
        }

    }

    public void StepRefresh(int cur_step)
    {
        isrefresh = true;
        current_step=cur_step;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Let the ScaleGestureDetector inspect all events.
        mScaleDetector.onTouchEvent(event);

        final int action = event.getAction();
        switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN: {
                final float x = event.getX();
                final float y = event.getY();

                mLastTouchX = x;
                mLastTouchY = y;
                mActivePointerId = event.getPointerId(0);
                break;
            }

            case MotionEvent.ACTION_MOVE: {
                final int pointerIndex = event.findPointerIndex(mActivePointerId);
                final float x = event.getX(pointerIndex);
                final float y = event.getY(pointerIndex);

                // Only move if the ScaleGestureDetector isn't processing a gesture.
                if (!mScaleDetector.isInProgress()) {
                    final float dx = x - mLastTouchX;
                    final float dy = y - mLastTouchY;

                    mPosX += dx;
                    mPosY += dy;

                    invalidate();
                }

                mLastTouchX = x;
                mLastTouchY = y;

                break;
            }

            case MotionEvent.ACTION_UP: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_CANCEL: {
                mActivePointerId = INVALID_POINTER_ID;
                break;
            }

            case MotionEvent.ACTION_POINTER_UP: {
                final int pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = event.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mLastTouchX = event.getX(newPointerIndex);
                    mLastTouchY = event.getY(newPointerIndex);
                    mActivePointerId = event.getPointerId(newPointerIndex);
                }
                break;
            }
        }

        return true;
    }



    //@RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onDraw(Canvas c) {

        if(isdrawfirst)
        {
            mScaleDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
            isdrawfirst = false;
        }


        Paint black = new Paint();
        black.setColor(Color.BLACK);
        black.setTextSize(25.0f);

        Paint black_axis = new Paint();
        black_axis.setColor(Color.BLACK);
        black_axis.setStrokeWidth(3.0f);

        Paint black_subaxis = new Paint();
        black_subaxis.setColor(Color.BLACK);
        black_subaxis.setStrokeWidth(1.0f);

        Paint white = new Paint();
        white.setColor(Color.WHITE);

        Paint red = new Paint();
        red.setColor(Color.RED);
        red.setTextSize((float)15*mScaleFactor);

        Paint blue = new Paint();
        blue.setColor(Color.BLUE);
        blue.setTextSize((float)15*mScaleFactor);

        if (isfirst) {
            array_0 = new float[]{2, (float) 0.0, (float) 0.0, (float) 0.0, (float) 0.0};
            len = array_0.length;
            for (int j = 0; j < len; j++) {
                list.add(array_0[j]);
            }
        }

        a = this.getWidth();     //screen 가로
        b = this.getHeight();      //screen 세로

        super.onDraw(c);

        c.save();

        scale = 10;
        scale = scale/(mScaleFactor);
        float as = (b/2)/scale;      //scaling     10mx10m
        float bs = (b/2)/scale;     //scaling
        c.translate(mPosX, mPosY);

        c.drawRect(-a*maxScaleFactor, -b*maxScaleFactor, a*maxScaleFactor, b*maxScaleFactor, white);
        c.drawLine(-a*maxScaleFactor,b/2,a*maxScaleFactor,b/2,black_axis);
        c.drawLine(a/2,-b*maxScaleFactor,a/2,b*maxScaleFactor,black_axis);

        for (int k =-(int)a*(int)maxScaleFactor; k<(int)a*(int)maxScaleFactor; k++)
        {
            c.drawLine(a/2+k*as,-b*maxScaleFactor,a/2+k*as,b*maxScaleFactor,black_subaxis);
        }

        for (int k =-(int)b*(int)maxScaleFactor; k<(int)b*(int)maxScaleFactor; k++)
        {
            c.drawLine(-a*maxScaleFactor,b/2+k*as,a*maxScaleFactor,b/2+k*as,black_subaxis);
        }


        for (i =current_step; i<step_number; i++)
        {
            if (i%2 == 0)
                c.drawCircle((a / 2) - list.get(2*i + 2) * bs, (b / 2) - list.get(2*i+1) *as, 3, red);
            else
                c.drawCircle((a / 2) - list.get(2*i + 2) * bs, (b / 2) - list.get(2*i+1) *as, 3, blue);
        }

        if (step_number %2 ==0) {
            c.drawText(Float.toString(list.get(len - 4)) + ", " + Float.toString(list.get(len - 3)), (a / 2)-40*mScaleFactor - list.get(len - 3) * as, (b / 2) - 25*mScaleFactor - list.get(len - 4) * bs, red);
            c.drawText(Float.toString(list.get(len - 2)) + ", " + Float.toString(list.get(len - 1)), (a / 2)-40*mScaleFactor - list.get(len - 1) * as, (b / 2) - 10*mScaleFactor - list.get(len - 2) * bs, blue);
        }
        else {
            c.drawText(Float.toString(list.get(len - 4)) + ", " + Float.toString(list.get(len - 3)), (a / 2)-40*mScaleFactor - list.get(len - 3) * as, (b / 2) - 25*mScaleFactor - list.get(len - 4) * bs, blue);
            c.drawText(Float.toString(list.get(len - 2)) + ", " + Float.toString(list.get(len - 1)), (a / 2)-40*mScaleFactor - list.get(len - 1) * as, (b / 2) - 10*mScaleFactor - list.get(len - 2) * bs, red);
        }

        c.drawText("Zoom: X "+Float.toString(mScaleFactor), 30-mPosX, 40-mPosY, black);
        c.restore();

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            mScaleFactor *= detector.getScaleFactor();

            // Don't let the object get too small or too large.
            mScaleFactor = Math.max(minScaleFactor, Math.min(mScaleFactor, maxScaleFactor));

            invalidate();
            return true;
        }
    }

}
