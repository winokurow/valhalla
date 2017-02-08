package org.ilw.valhalla.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;

import org.ilw.valhalla.activity.GameActivity;
import org.ilw.valhalla.dto.Cell;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.Point;

import java.util.List;
import java.util.Map;

public class QueueView extends View {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint paint, mBitmapPaint;
    private float canvasSize;
    private float canvasY1Size;
    private float canvasY2Size;

    public QueueView(Context context, AttributeSet attrs) {
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
        this.setBackgroundColor(Color.LTGRAY);
        Map<Double, Point> queue = ((GameActivity)getContext()).getQueue();
        Cell[][] field = ((GameActivity) getContext()).getField();
        paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        mCanvas.drawRect(0f, 0, canvasSize, canvasY1Size, paint);
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        mCanvas.drawRect(0f, canvasY2Size, canvasSize, canvasSize, paint);
        int i = 0;
        for(Map.Entry<Double, Point> entry : queue.entrySet()) {
            drawRect(i * canvasSize / 5, canvasY1Size, (i + 1) * canvasSize / 5, canvasY2Size, paint, Color.WHITE);

            Bitmap bmp = ((GameActivity) getContext()).getmStore().get(String.format("glad%s_%s",field[entry.getValue().getY()][entry.getValue().getX()].getOwner(), field[entry.getValue().getY()][entry.getValue().getX()].getGladiatorDirection()));
            mCanvas.drawBitmap(bmp, i * canvasSize / 5 + 20, canvasY1Size + 20, null);
            if (i==4)
            {
                break;
            }
            i++;
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

}