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
import android.view.View;

import static android.content.ContentValues.TAG;

public class GameView extends View {

    private Bitmap mBitmap;
    private Canvas mCanvas;
    private Paint paint, mBitmapPaint;
    private float canvasSize;

    private String cells = "";

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        canvasSize = (int) convertDpToPixel(300, context);

        mBitmap = Bitmap.createBitmap((int) canvasSize, (int) canvasSize, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        if (!(cells.isEmpty()))
        {
            String rows[] = cells.split(";");


            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setColor(0xffff0505);
            paint.setStrokeWidth(5f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);


            int[][] row = new int[rows.length][rows.length];
            for (int i = 0; i < rows.length; i++) {
                for (int j = 0; j < rows.length; j++) {
                    row[i][j] = Integer.parseInt(rows[i].split(",")[j]);
                    if ((i % 2 == 0) && (j % 2 == 0)) {
                        float x1 = j * canvasSize / (rows.length+1);
                        float x2 = (j + 2) * canvasSize / (rows.length+1);
                        float y1 = i * canvasSize / (rows.length+1);
                        float y2 = (i + 2) * canvasSize / (rows.length+1);

                        switch (row[i][j]) {
                            case 100:
                                paint.setStyle(Paint.Style.FILL);
                                paint.setColor(Color.LTGRAY);
                                mCanvas.drawRect(x1, y1, x2, y2, paint);
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setColor(Color.BLACK);
                                mCanvas.drawRect(x1, y1, x2, y2, paint);
                                break;
                        }
                    }
                }
            }

            canvas.drawBitmap(mBitmap, 0, 0, mBitmapPaint);
        }
    }

    //переводим dp в пиксели
    public float convertDpToPixel(float dp,Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi/160f);
    }

    public void setCells(String cells) {
        this.cells = cells;
        Log.d(TAG, cells);
        invalidate();
        requestLayout();
    }
}