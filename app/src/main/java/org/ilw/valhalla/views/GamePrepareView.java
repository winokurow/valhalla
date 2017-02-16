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
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.ilw.valhalla.activity.PrepareActivity;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.Point;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static android.R.attr.direction;
import static android.graphics.Bitmap.createScaledBitmap;

public class GamePrepareView extends View {

    private Bitmap mBitmap1;
    private Canvas mCanvas;
    private Paint paint, mBitmapPaint;
    private float canvasSize;
    private int[][] cells;
    private final ScaleGestureDetector scaleGestureDetector;
    private final GestureDetector detector;
    private final int viewSize;
    private float mScaleFactor;
    public GamePrepareView(Context context, AttributeSet attrs) {
        super(context, attrs);


        viewSize=(int)convertDpToPixel(300, context);
        mScaleFactor=1f;//значение зума по умолчанию
        canvasSize=(int)(viewSize*mScaleFactor);//определяем размер канваса

        mBitmap1 = Bitmap.createBitmap((int) canvasSize, (int) canvasSize, Bitmap.Config.ARGB_8888);
        mCanvas = new Canvas(mBitmap1);
        mBitmapPaint = new Paint(Paint.DITHER_FLAG);
        scaleGestureDetector=new ScaleGestureDetector(context, new MyScaleGestureListener());
        detector=new GestureDetector(context, new MyGestureListener());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.save();
        canvas.scale(mScaleFactor, mScaleFactor);//зумируем канвас
        canvas.drawBitmap(mBitmap1, 0, 0, mBitmapPaint);
        canvas.restore();
    }

    //в случае касания пальем передаем управление MyScaleGestureListener
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        scaleGestureDetector.onTouchEvent(event);
        detector.onTouchEvent(event);
        return true;
    }

    public void drawField()
    {

        if (cells != null) {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setColor(0xffff0505);
            paint.setStrokeWidth(5f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            int xLength = (cells[0].length + 1) / 2;
            int yLength = (cells.length + 1) / 2;
            for (int i = 0; i < cells.length; i = i + 2) {
                for (int j = 0; j < cells[0].length; j = j + 2) {
                    float x1 = (j / 2) * canvasSize / xLength;
                    float x2 = ((j / 2) + 1) * canvasSize / xLength;
                    float y1 = (i / 2) * canvasSize / yLength;
                    float y2 = ((i / 2) + 1) * canvasSize / yLength;

                    switch (cells[i][j]) {
                        case 100:
                            paint.setStyle(Paint.Style.FILL);
                            paint.setColor(Color.DKGRAY);
                            mCanvas.drawRect(x1, y1, x2, y2, paint);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setColor(Color.BLACK);
                            mCanvas.drawRect(x1, y1, x2, y2, paint);
                            break;
                        case 99:
                            Log.d("TAG", new Float(x1).toString() + " " + new Float(x2).toString() + " " + new Float(y1).toString() + " " + new Float(y2).toString());
                            paint.setStyle(Paint.Style.FILL);
                            paint.setColor(Color.LTGRAY);
                            mCanvas.drawRect(x1, y1, x2, y2, paint);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setColor(Color.BLACK);
                            mCanvas.drawRect(x1, y1, x2, y2, paint);
                            break;
                        case 98:
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
            Point activePoint = ((PrepareActivity)getContext()).getActivePoint();
            Log.d("TAG", new Boolean(activePoint==null).toString());
            if (activePoint!=null)
            {
                float x1 = activePoint.getX() * canvasSize / xLength;
                float x2 = (activePoint.getX()+1) * canvasSize / xLength;
                float y1 = activePoint.getY() * canvasSize / yLength;
                float y2 = (activePoint.getY()+1) * canvasSize / yLength;
                paint.setStyle(Paint.Style.STROKE);
                paint.setColor(Color.YELLOW);
                mCanvas.drawRect(x1, y1, x2, y2, paint);
            }
            Map<Point, Gladiator> gladiators = ((PrepareActivity) getContext()).getGladiatorsSet();
            if (gladiators.size() > 0) {
                Iterator it = gladiators.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry pair = (Map.Entry) it.next();
                    Point point = (Point) pair.getKey();
                    //System.out.println(pair.getKey() + " = " + pair.getValue());
                    //it.remove(); // avoids a ConcurrentModificationException
                    //считаем координаты центра ячейки

                    float x0 = ((viewSize / (10 * xLength)) + (viewSize / xLength) * point.getX());
                    float y0 = ((viewSize / (10 * yLength)) + (viewSize / yLength) * point.getY());

                    Bitmap bmp = ((PrepareActivity) getContext()).getmStore().get("glad1");
                    Bitmap bmp1 = createScaledBitmap(bmp, (int) (bmp.getWidth() * 1.5), (int) (bmp.getHeight() * 1.5), false);
                    Paint paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setFilterBitmap(true);
                    mCanvas.drawBitmap(bmp1, x0, y0, paint);
                    bmp = ((PrepareActivity) getContext()).getmStore().get(String.format("pants_1", direction));
                    bmp1 = createScaledBitmap(bmp, (int) (bmp.getWidth() * 1.5), (int) (bmp.getHeight() * 1.5), false);
                    paint = new Paint();
                    paint.setAntiAlias(true);
                    paint.setFilterBitmap(true);
                    mCanvas.drawBitmap(bmp1, x0+50, y0+100, paint);
                }
            }
        }
    }


    //переводим dp в пиксели
    public float convertDpToPixel(float dp,Context context){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return dp * (metrics.densityDpi/160f);
    }

    public void setCells(int[][] cells) {
        this.cells = cells;
        drawField();
        invalidate();
        requestLayout();
    }

    //унаследовались от ScaleGestureDetector.SimpleOnScaleGestureListener, чтобы не писать пустую реализацию ненужных
    //методов интерфейса OnScaleGestureListener
    private class MyScaleGestureListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        //обрабатываем "щипок" пальцами
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            float scaleFactor = scaleGestureDetector.getScaleFactor();//получаем значение зума относительно предыдущего состояния
            //получаем координаты фокальной точки - точки между пальцами
            float focusX = scaleGestureDetector.getFocusX();
            float focusY = scaleGestureDetector.getFocusY();
            //следим чтобы канвас не уменьшили меньше исходного размера и не допускаем увеличения больше чем в 2 раза
            if (mScaleFactor * scaleFactor > 1 && mScaleFactor * scaleFactor < 2) {
                mScaleFactor *= scaleGestureDetector.getScaleFactor();
                canvasSize = viewSize * mScaleFactor;//изменяем хранимое в памяти значение размера канваса
                //используется при расчетах
                //по умолчанию после зума канвас отскролит в левый верхний угол.
                //Скролим канвас так, чтобы на экране оставалась
                //область канваса, над которой был жест зума
                //Для получения данной формулы достаточно школьных знаний математики (декартовы координаты).
                int scrollX = (int) ((getScrollX() + focusX) * scaleFactor - focusX);
                scrollX = Math.min(Math.max(scrollX, 0), (int) canvasSize - viewSize);
                int scrollY = (int) ((getScrollY() + focusY) * scaleFactor - focusY);
                scrollY = Math.min(Math.max(scrollY, 0), (int) canvasSize - viewSize);
                scrollTo(scrollX, scrollY);
            }
            //вызываем перерисовку принудительно
            invalidate();
            return true;
        }

    }

        //унаследовались от GestureDetector.SimpleOnGestureListener, чтобы не писать пустую
        //реализацию ненужных методов интерфейса OnGestureListener
        private class MyGestureListener extends GestureDetector.SimpleOnGestureListener
        {
            //обрабатываем скролл (перемещение пальца по экрану)
            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
            {

               //Log.d("TAG", new Float(getScrollX()).toString() +  " " + new Float(distanceX).toString());
                //не даем канвасу показать края по горизонтали
                if(getScrollX()+distanceX< canvasSize -viewSize && getScrollX()+distanceX>0){
                    scrollBy((int)distanceX, 0);
                }
                //не даем канвасу показать края по вертикали
                if(getScrollY()+distanceY< canvasSize -viewSize && getScrollY()+distanceY>0){
                    scrollBy(0, (int)distanceY);
                }
                return true;
            }

            //обрабатываем одиночный тап
            @Override
            public boolean onSingleTapConfirmed(MotionEvent event){
                List<Gladiator> gladiators = ((PrepareActivity)getContext()).getGladiatorsWait();

                //получаем координаты ячейки, по которой тапнули
                int eventX=(int)((event.getX()+getScrollX())/mScaleFactor);
                int eventY=(int)((event.getY()+getScrollY())/mScaleFactor);
                int xLength = (cells[0].length+1)/2;
                int yLength = (cells.length+1)/2;
                int x = (int)(xLength *eventX/viewSize);
                int y = (int)(yLength *eventY/viewSize);
                int active = ((PrepareActivity)getContext()).getActive();
                Point activePoint = ((PrepareActivity)getContext()).getActivePoint();
                Map<Point, Gladiator> gladiatorsSet = ((PrepareActivity)getContext()).getGladiatorsSet();

                if ((active>-1))
                {
                    if (((cells[y*2][x*2]==98) || (cells[y*2][x*2]==99)) && (!(gladiatorsSet.containsKey(new Point(x, y)))))
                    {
                    //Log.d("TAG", gladiatorsSet.toString());
                    gladiatorsSet.put(new Point(x,y), gladiators.get(active));
                    ((PrepareActivity)getContext()).setGladiatorsSet(gladiatorsSet);

                    drawField();
                    invalidate();
                    gladiators.remove(active);
                    ((PrepareActivity)getContext()).getView2().invalidate();
                    ((PrepareActivity)getContext()).setActive(-1);
                    }
                }

                else if (activePoint!=null){
                    if (activePoint.equals(new  Point(x, y)))
                    {
                        ((PrepareActivity)getContext()).setActivePoint(null);
                    } else
                    {
                        if (((cells[y*2][x*2]==98) || (cells[y*2][x*2]==99)) && (!(gladiatorsSet.containsKey(new Point(x, y))))) {
                            //Log.d("TAG", gladiatorsSet.toString());
                            gladiatorsSet.put(new Point(x, y), gladiatorsSet.get(activePoint));
                            gladiatorsSet.remove(activePoint);
                            ((PrepareActivity) getContext()).setGladiatorsSet(gladiatorsSet);
                            ((PrepareActivity)getContext()).setActivePoint(null);
                            ((PrepareActivity) getContext()).getView2().invalidate();
                        }
                    }
                    drawField();
                    invalidate();
                } else {
                    if (((gladiatorsSet.containsKey(new Point(x, y)))))
                    {
                        Point point = new Point(x,y);
                        activePoint = point;
                        Log.d("TAG", new Integer (activePoint.getX()).toString());
                        ((PrepareActivity)getContext()).setActivePoint(activePoint);
                        drawField();
                        invalidate();
                    }
                }
                return true;
            }

            //обрабатываем двойной тап
            @Override
            public boolean onDoubleTapEvent(MotionEvent event){
                //зумируем канвас к первоначальному виду
                mScaleFactor=1f;
                canvasSize =viewSize;
                scrollTo(0, 0);//скролим, чтобы не было видно краев канваса.
                invalidate();//перерисовываем канвас
                return true;
            }
        }
}