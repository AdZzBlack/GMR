package com.adzzblack.gmr;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Shader;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static android.R.attr.x;
import static android.R.attr.y;

/**
 * Created by ADI on 4/18/2017.
 */

public class DrawTest extends Fragment {
    protected AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
    protected View v;
    protected ViewGroup c;
    private Paint mPaint;
    float[] x = new float[8];
    float[] y = new float[8];

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.main_fragment, container, false);
        getActivity().setTitle("testing");

        setMap();

        fadeIn.setDuration(1400);
        fadeIn.setFillAfter(true);

        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(Color.GREEN);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(12);

        float ctrX = (float) -8;
        float ctrY = (float) 30;

        x[0] = (float) 727 + ctrX;
        x[1] = (float) 797 + ctrX;
        x[2] = (float) 802 + ctrX;
        x[3] = (float) 725 + ctrX;
        x[4] = (float) 725 + ctrX;
        x[5] = (float) 650 + ctrX;
        x[6] = (float) 660 + ctrX;
        x[7] = (float) 727 + ctrX;

        y[0] = (float) 217.93333435058594 + ctrY;
        y[1] = (float) 219.93333435058594 + ctrY;
        y[2] = (float) 307.93333435058594 + ctrY;
        y[3] = (float) 302.93333435058594 + ctrY;
        y[4] = (float) 370.93333435058594 + ctrY;
        y[5] = (float) 375.93333435058594 + ctrY;
        y[6] = (float) 157.93333435058594 + ctrY;
        y[7] = (float) 217.93333435058594 + ctrY;

        return v;
    }

    public void setMap()
    {
        RelativeLayout rela = (RelativeLayout) v.findViewById(R.id.tes);
        rela.addView(new MyView(getActivity()));

//        relativeLayout.removeAllViews();
    }

    public class MyView extends View
    {
        private Canvas  mCanvas;
        Context context;
        private Paint   mBitmapPaint;
        private Bitmap mBitmap;

        Paint paint = null;
        public MyView(Context c)
        {
            super(c);
            paint = new Paint();

            context=c;

            mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        }
//
//        @Override
//        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//            super.onSizeChanged(w, h, oldw, oldh);
//
//            mBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
//            mCanvas = new Canvas(mBitmap);
//        }

        @Override
        protected void onDraw(Canvas canvas)
        {
            super.onDraw(canvas);

            Paint wallpaintbase = new Paint();
            wallpaintbase.setColor(Color.RED);
            wallpaintbase.setStrokeWidth(5);
            wallpaintbase.setStyle(Paint.Style.STROKE);

            Path wallpathbase = new Path();
            wallpathbase.reset();
            wallpathbase.moveTo(x[0], y[0]);
            for(int i=1;i<x.length;i++)
            {
                wallpathbase.lineTo(x[i], y[i]);
            }
            canvas.drawPath(wallpathbase, wallpaintbase);

            drawPercentage(canvas,60, 1);
//            drawPercentage(canvas,40, 2);

        }
    }

    public void drawPercentage(Canvas canvas, int percentage, int color)
    {
        Paint wallpaint = new Paint();
        if(color==1) wallpaint.setColor(Color.YELLOW);
        else if(color==2) wallpaint.setColor(Color.BLUE);
        wallpaint.setAlpha(100);
        wallpaint.setStyle(Paint.Style.FILL);

        Path wallpath = new Path();
        wallpath.reset(); // only needed when reusing this path for a new build

        float yMin = (float) 10000;
        float yMax = (float) 0;
        float xBefore = (float) 0;
        float yBefore = (float) 0;
        for(int i=0;i<x.length;i++)
        {
            if(y[i]<yMin) yMin = y[i];
            if(y[i]>yMax) yMax = y[i];
        }
        float diff = yMax-yMin;
        Log.d("ymax", yMax + "");
        Log.d("ymin", yMin + "");
        Log.d("diffbefore", diff + "");
        diff = diff * percentage /100;
        yMax = yMin + diff;
        Log.d("diff", diff + "");

        boolean isStarted = false;
        float xStart = 0;
        float yStart = 0;

        for(int i=0;i<x.length;i++)
        {
            if(y[i]>yMax)
            {
                if(xBefore>0 && yBefore>0)
                {
                    float m =(y[i] - yBefore) / (x[i] - xBefore);
                    float xNew = (yMax - yBefore + (m * xBefore)) / m;

                    float tempXMin;
                    float tempXMax;
                    if(x[i]<=xBefore)
                    {
                        tempXMin = x[i];
                        tempXMax = xBefore;
                    }
                    else
                    {
                        tempXMin = xBefore;
                        tempXMax = x[i];
                    }

                    float tempYMin;
                    float tempYMax;
                    if(y[i]<=yBefore)
                    {
                        tempYMin = y[i];
                        tempYMax = yBefore;
                    }
                    else
                    {
                        tempYMin = yBefore;
                        tempYMax = y[i];
                    }

                    if(tempXMin==tempXMax)
                    {
                        if(yMax>=tempYMin && yMax<=tempYMax)
                        {
                            Log.d("xy1", tempXMin + ", " + yMax);
                            if(!isStarted)
                            {
                                xStart = tempXMin;
                                yStart = yMax;
                                isStarted = true;
                            }
                            wallpath.lineTo(tempXMin, yMax);
                        }
                    }
                    else if(xNew>=tempXMin && xNew<=tempXMax)
                    {
                        Log.d("xy4", xNew + ", " + yMax);
                        if(!isStarted)
                        {
                            xStart = xNew;
                            yStart = yMax;
                            isStarted = true;
                        }
                        wallpath.lineTo(xNew, yMax);
                    }
                }
            }
            else
            {
                if(yBefore>yMax)
                {
                    float m =(yBefore - y[i]) / (xBefore - x[i]);
                    float xNew = (yMax - y[i] + (m * x[i])) / m;

                    float tempXMin;
                    float tempXMax;
                    if(x[i]<=xBefore)
                    {
                        tempXMin = x[i];
                        tempXMax = xBefore;
                    }
                    else
                    {
                        tempXMin = xBefore;
                        tempXMax = x[i];
                    }
                    if(xNew>=tempXMin && xNew<=tempXMax)
                    {
                        Log.d("xy3", xNew + ", " + yMax);
                        wallpath.lineTo(xNew, yMax);
                        if(!isStarted)
                        {
                            xStart = xNew;
                            yStart = yMax;
                            isStarted = true;
                        }
                    }
                }

                Log.d("xy2", x[i] + ", " + y[i]);
                wallpath.lineTo(x[i], y[i]);
                if(!isStarted)
                {
                    xStart = x[i];
                    yStart = y[i];
                    isStarted = true;
                }
            }

            xBefore = x[i];
            yBefore = y[i];
        }
        wallpath.lineTo(xStart, yStart);

        canvas.drawPath(wallpath, wallpaint);
    }
}
