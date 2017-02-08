package org.ilw.valhalla.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v7.widget.PopupMenu;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import org.ilw.valhalla.R;
import org.ilw.valhalla.activity.GameActivity;
import org.ilw.valhalla.data.Terrain;
import org.ilw.valhalla.dto.Cell;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.Point;
import org.ilw.valhalla.dto.Turn;

import java.util.Map;

import static android.graphics.Bitmap.createScaledBitmap;

public class GameView extends View {

    private Bitmap mBitmap1;
    private Canvas mCanvas;
    private Paint paint, mBitmapPaint;
    private float canvasSize;
    private final ScaleGestureDetector scaleGestureDetector;
    private final GestureDetector detector;
    private final int viewSize;
    private float mScaleFactor;

    public GameView(Context context, AttributeSet attrs) {
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

    public void prepareTurn()
    {
        for(Map.Entry<Integer,Turn> turn : ((GameActivity)getContext()).getTurns().entrySet()) {
            Turn value = turn.getValue();
            switch (value.getAction())
            {
                case "set":
                    String[] gladiators = value.getValue1().split(";");
                    for (String glad:gladiators) {
                        int xPos = Integer.parseInt(glad.split(":")[0]);
                        int yPos = Integer.parseInt(glad.split(":")[1]);
                        String gladiator = glad.split(":")[3];
                        int gladiatorDirection = Integer.parseInt(glad.split(":")[2]);
                        if (!(((GameActivity) getContext()).isFirstPlayer()))
                        {
                            yPos = ((GameActivity) getContext()).getField().length - yPos - 1;
                            xPos = ((GameActivity) getContext()).getField()[0].length - xPos - 1;
                            gladiatorDirection = (gladiatorDirection == 1)? 3:1;
                        }

                        ((GameActivity) getContext()).getField()[yPos][xPos].setGladiator(Integer.parseInt(gladiator));
                        ((GameActivity) getContext()).getField()[yPos][xPos].setGladiatorDirection(gladiatorDirection);
                        ((GameActivity) getContext()).getField()[yPos][xPos].setOwner(Integer.parseInt(value.getHost()));

                        ((GameActivity) getContext()).addQueue(0, new Point(xPos, yPos));
                    }
                    break;
            }
        }
    }

    public void drawField()
    {
        Cell[][] field = ((GameActivity) getContext()).getField();
        if (field != null) {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setColor(0xffbebebe);
            paint.setStrokeWidth(5f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            int xLength = field[0].length;
            int yLength = field.length;
            for (int i = 0; i < yLength; i++) {
                for (int j = 0; j < xLength; j++) {
                    float x1 = j * canvasSize / xLength;
                    float x2 = (j+ 1) * canvasSize / xLength;
                    float y1 = i * canvasSize / yLength;
                    float y2 = (i + 1) * canvasSize / yLength;

                    switch (field[i][j].getGround()) {
                        case 100:
                            paint.setStyle(Paint.Style.FILL);
                            paint.setColor(Color.DKGRAY);
                            mCanvas.drawRect(x1, y1, x2, y2, paint);
                            paint.setStyle(Paint.Style.STROKE);
                            paint.setColor(Color.BLACK);
                            mCanvas.drawRect(x1, y1, x2, y2, paint);
                            break;
                        case 99:
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
                    Point point = ((GameActivity) getContext()).getActivField();

                    if ((i==point.getY()) && (j==point.getX()))
                    {
                        float pixel = (viewSize / xLength)/100;
                        float x0 = (pixel*20) + (viewSize / xLength) * j;
                        float y0 = (pixel*65) + (viewSize / yLength) * i;
                        RectF rect = new RectF(x0,y0,x0+pixel*60,y0+pixel*30);
                        paint.setStyle(Paint.Style.FILL);
                        paint.setColor(Color.YELLOW);
                        mCanvas.drawOval(rect,paint);
                        paint.setStyle(Paint.Style.STROKE);
                        paint.setColor(Color.RED);
                        mCanvas.drawOval(rect,paint);
                    }
                    if (field[i][j].getGladiator() != -1)
                    {
                        float pixel = (viewSize / xLength)/100;
                        float x0 = (pixel*15 + (viewSize / xLength) * j);
                        float y0 = (pixel*10 + (viewSize / yLength) * i);

                        Bitmap bmp = ((GameActivity) getContext()).getmStore().get(String.format("glad%s_%s",field[i][j].getOwner(), field[i][j].getGladiatorDirection()));
                        Bitmap bmp1 = createScaledBitmap(bmp, (int) (bmp.getWidth() * 1.5), (int) (bmp.getHeight() * 1.5), false);
                        Paint paint = new Paint();
                        paint.setAntiAlias(true);
                        paint.setFilterBitmap(true);
                        mCanvas.drawBitmap(bmp1, x0, y0, paint);
                    }
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

  /*          //обрабатываем одиночный тап
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
            }*/

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

            //обрабатываем двойной тап
            @Override
            public boolean onDown(MotionEvent event){
                //получаем координаты ячейки, по которой тапнули
                int eventX=(int)((event.getX()+getScrollX())/mScaleFactor);
                int eventY=(int)((event.getY()+getScrollY())/mScaleFactor);
                Cell[][] field = ((GameActivity) getContext()).getField();
                int xLength = field[0].length;
                int yLength = field.length;

                int x = (int)(xLength *eventX/viewSize);
                int y = (int)(yLength *eventY/viewSize);
                StringBuffer text = new StringBuffer("Terrain:\n   ");
                text.append(Terrain.fromId(field[y][x].getGround()).getDescription());
                if (field[y][x].getGladiator() != -1)
                {
                    for (Gladiator gladiator:((GameActivity) getContext()).getGladiators())
                    {
                        if (gladiator.getId() == field[y][x].getGladiator())
                        {
                            text.append("\nGladiator:");
                            text.append("\n   Name: ");
                            text.append(gladiator.getName());
                            text.append("\n   Str: ");
                            text.append(gladiator.getStr());
                            text.append("\n   Dex: ");
                            text.append(gladiator.getDex());
                            text.append("\n   Spd: ");
                            text.append(gladiator.getSpd());
                            text.append("\n   Con: ");
                            text.append(gladiator.getCon());
                            text.append("\n   Martial Art: ");
                            text.append(gladiator.getMart_art());
                            break;
                        }
                    }
                }
                ((GameActivity) getContext()).getTextView().setText(text.toString());

                return true;
            }

            @Override
            public void onLongPress(MotionEvent event) {
                Log.d("KLICK", "Click 1");
                showContextMenu(GameView.this);
            }
        }

    // Display anchored popup menu based on view selected
    private void showContextMenu(View v) {
        Log.d("KLICK", "Click 2");
        PopupMenu popup = new PopupMenu(this.getContext(), v);
        Log.d("KLICK", "Click 3");
        // Inflate the menu from xml
        popup.getMenuInflater().inflate(R.menu.popup_commands, popup.getMenu());
        // Setup menu item selection
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_keyword:
                        //Toast.makeText(this), "Keyword!", Toast.LENGTH_SHORT).show();
                        return true;
                    case R.id.menu_popularity:
                        //Toast.makeText(MainActivity.this, "Popularity!", Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }
}