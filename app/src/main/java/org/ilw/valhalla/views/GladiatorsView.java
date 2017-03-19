package org.ilw.valhalla.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import org.ilw.valhalla.activity.PrepareActivity;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.Point;

import java.util.List;
import java.util.Map;

import static android.R.attr.direction;

public class GladiatorsView extends View {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint paint, mBitmapPaint;
    private float canvasSize;
    private float canvasY1Size;
    private float canvasY2Size;

    public GladiatorsView(Context context, AttributeSet attrs) {
        super(context, attrs);
        canvasSize=(int)convertDpToPixel(300, context);
        canvasY1Size = (int) convertDpToPixel(20, context);
        canvasY2Size = (int) convertDpToPixel(80, context);
        mBitmap = Bitmap.createBitmap((int) canvasSize, (int) canvasSize, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);

        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int active = ((PrepareActivity)getContext()).getActive();
        List<Gladiator> gladiators = ((PrepareActivity)getContext()).getGladiatorsWait();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        mCanvas.drawRect(0f, 0, canvasSize, canvasY1Size, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        mCanvas.drawRect(0f, canvasY2Size, canvasSize, canvasSize, paint);
        Log.d("Tag", "count" + new Integer(gladiators.size()).toString());
        Log.d("Tag", "count" + ((PrepareActivity)getContext()).getGladiators().size());
        int count = gladiators.size();
        if ((gladiators.size()<5) && (gladiators.size()<((PrepareActivity)getContext()).getGladiators().size()))
        {
            count++;
        }
        Log.d("Tag", (new Integer(count)).toString());
        for (int i=0;i<5;i++)
        {
            if (i<count)
                    {
                if (i != active) {
                    drawRect(i * canvasSize / 5, canvasY1Size, (i + 1) * canvasSize / 5, canvasY2Size, paint, Color.WHITE);
                } else {
                    drawRect(i * canvasSize / 5, canvasY1Size, (i + 1) * canvasSize / 5, canvasY2Size, paint, Color.WHITE, Color.YELLOW, 3);
                }
                //Paint paint2 = new Paint();
                //paint2.setStyle(Paint.Style.FILL);
                //paint2.setColor(Color.BLACK);
                //paint2.setTextSize(20);
                //mCanvas.drawText(gladiators.get(i).getName(), i*canvasSize/5 + 5, canvasY1Size + 80, paint2);
                if (i<gladiators.size())
                {
                    Bitmap bmp = null;
                    bmp = ((PrepareActivity) getContext()).getmStore().get("glad1");
                    mCanvas.drawBitmap(bmp, i * canvasSize / 5 + 20, canvasY1Size + 20, null);
                    bmp = ((PrepareActivity) getContext()).getmStore().get(String.format("pants_1", direction));
                    mCanvas.drawBitmap(bmp, i * canvasSize / 5 + 52, canvasY1Size + 95, paint);
                    if ((active > -1) && (active < gladiators.size())) {
                        Paint paint2 = new Paint();
                        paint2.setStyle(Paint.Style.FILL);
                        paint2.setColor(Color.BLACK);
                        paint2.setTextSize(40);
                        int init = 50;
                        ((PrepareActivity) getContext()).setLogInfo(gladiators.get(active));
                        /*mCanvas.drawText("Name: " + gladiators.get(active).getName(), 30, canvasY2Size + init, paint2);
                        mCanvas.drawText("Str:  " + gladiators.get(active).getStr() + "    " + "Dex: " + gladiators.get(active).getDex(), 30, canvasY2Size + init + 50, paint2);
                        mCanvas.drawText("Spd:  " + gladiators.get(active).getSpd() + "  " + "Con: " + gladiators.get(active).getCon(), 30, canvasY2Size + init + 100, paint2);
                        mCanvas.drawText("Int: " + gladiators.get(active).getIntel() + "     " + "Stamina: " + gladiators.get(active).getStamina(), 30, canvasY2Size + init + 150, paint2);
                        mCanvas.drawText("Martial Art: " + gladiators.get(active).getMart_art(), 30, canvasY2Size + init + 250, paint2);
*/
                    }
                }
            } else {
                drawRect(i*canvasSize/5, canvasY1Size, (i+1)*canvasSize/5, canvasY2Size, paint, Color.DKGRAY);
            }


        }
        canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
    }

    public void setGladiators(List<Gladiator> gladiators) {
       // this.gladiators = gladiators;
        invalidate();
        requestLayout();
    }

    //переводим dp в пиксели
    public float convertDpToPixel(float dp,Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi/160f);
    }

    public void drawRect(float x1, float y1, float x2, float y2, Paint paint, int color) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        mCanvas.drawRect(x1, y1, x2, y2, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(Color.BLACK);
        mCanvas.drawRect(x1, y1, x2, y2, paint);
    }

    public void drawRect(float x1, float y1, float x2, float y2, Paint paint, int color, int bgColor, int border) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(color);
        mCanvas.drawRect(x1, y1, x2, y2, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setColor(bgColor);
        float borderOld = paint.getStrokeWidth();
        paint.setStrokeWidth(border);
        mCanvas.drawRect(x1, y1, x2, y2, paint);
        paint.setStrokeWidth(borderOld);
    }

    @Override
    public boolean onTouchEvent(final MotionEvent event) {
        if(event.getAction() == MotionEvent.ACTION_UP){
            int order = (int) (event.getX() / (canvasSize/5));
            List<Gladiator> gladiators = ((PrepareActivity)getContext()).getGladiators();
            int count = gladiators.size();
            if ((gladiators.size()<5) && (gladiators.size()<((PrepareActivity)getContext()).getGladiators().size()))
            {
                count++;
            }
            if ((order>-1) && (order<count))
            {
                if (((PrepareActivity) getContext()).getActive() != -1)
                {
                    ((PrepareActivity) getContext()).setActive(-1);
                    ((PrepareActivity) getContext()).setLogInfo(null);
                } else {
                    Point activePoint = ((PrepareActivity)getContext()).getActivePoint();
                    if (activePoint == null) {
                        ((PrepareActivity) getContext()).setActive(order);
                    } else {
                        Map<Point, Gladiator> gladiatorsSet = ((PrepareActivity)getContext()).getGladiatorsSet();

                        ((PrepareActivity) getContext()).getGladiatorsWait().add(gladiatorsSet.get(activePoint));
                        gladiatorsSet.remove(activePoint);
                        ((PrepareActivity)getContext()).setActivePoint(null);
                        ((PrepareActivity) getContext()).setLogInfo(null);
                        ((PrepareActivity)getContext()).getView().drawField();
                        ((PrepareActivity)getContext()).getView().invalidate();
                    }
                }
                this.invalidate();
            }
            return performClick();
        }
        return true;
    }

}