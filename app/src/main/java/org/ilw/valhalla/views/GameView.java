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
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.Toast;

import org.ilw.valhalla.R;
import org.ilw.valhalla.activity.GameActivity;
import org.ilw.valhalla.data.Terrain;
import org.ilw.valhalla.dto.Cell;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.Point;
import org.ilw.valhalla.dto.Turn;

import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

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
    private Point clickPoint;
    PopupMenu popup;

    public GameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        popup = new PopupMenu(this.getContext(), this);
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
        Cell[][] field = ((GameActivity) getContext()).getField();

        for(Map.Entry<Integer,Turn> turn : ((GameActivity)getContext()).getTurns().entrySet()) {
            Turn value = turn.getValue();

            if (value.getTurn() > ((GameActivity) getContext()).getTurnNumber()) {


                switch (value.getAction()) {
                    case "set":
                        if (value.getTurn()==-10) {
                            ((GameActivity) getContext()).setTurnNumber(0);
                            ((GameActivity) getContext()).addLogString("0: Start Game\n");
                        }
                        String[] gladiators = value.getValue1().split(";");
                        for (String glad : gladiators) {
                            int xPos = Integer.parseInt(glad.split(":")[0]);
                            int yPos = Integer.parseInt(glad.split(":")[1]);
                            String gladiator = glad.split(":")[3];
                            int gladiatorDirection = Integer.parseInt(glad.split(":")[2]);

                            ((GameActivity) getContext()).getField()[yPos][xPos].setGladiator(Integer.parseInt(gladiator));
                            ((GameActivity) getContext()).getField()[yPos][xPos].setGladiatorDirection(gladiatorDirection);
                            ((GameActivity) getContext()).getField()[yPos][xPos].setOwner(Integer.parseInt(value.getHost()));
                            ((GameActivity) getContext()).addQueue(0, new Point(xPos, yPos));

                            if (Integer.parseInt(value.getHost()) == 1) {
                                ((GameActivity) getContext()).setGladcountGamer1(gladiators.length);
                            } else {
                                ((GameActivity) getContext()).setGladcountGamer2(gladiators.length);
                            }
                        }
                        break;
                    case "skip":
                    {
                        TreeMap<Double, Point> queue = ((GameActivity) getContext()).getQueue();
                        Point point = queue.firstEntry().getValue();
                        field[point.getY()][point.getX()].setBlocked(false);
                        Gladiator gladiator = ((GameActivity) getContext()).getGladiatorById(field[point.getY()][point.getX()].getGladiator());

                        // progress
                        if (gladiator.getStamina_act() <5)
                        {
                            gladiator.setStamina_progress(gladiator.getStamina_progress()+1);
                        }

                        // stamina calculation
                        int stamina = gladiator.getStamina_act()+gladiator.getStamina()*2;
                        stamina = (stamina>gladiator.getStamina()*10) ? gladiator.getStamina()*10:stamina;
                        gladiator.setStamina_act(stamina);

                        queue.remove(queue.firstKey());
                        Double time = queue.firstKey();
                        ((GameActivity) getContext()).addQueue(time, point);
                        ((GameActivity) getContext()).setTurnNumber(value.getTurn());
                        ((GameActivity) getContext()).addLogString(value.getTurn() + ": "+gladiator.getName() + " skipped turn\n");


                        break;
                    }
                    case "block":
                    {
                        TreeMap<Double, Point> queue = ((GameActivity) getContext()).getQueue();
                        Point point = queue.firstEntry().getValue();
                        field[point.getY()][point.getX()].setBlocked(true);
                        Gladiator gladiator = ((GameActivity) getContext()).getGladiatorById(field[point.getY()][point.getX()].getGladiator());

                        queue.remove(queue.firstKey());
                        Double time = queue.firstKey();
                        double spd = gladiator.getSpd()/5.00;
                        time = time + 1 / spd;
                        ((GameActivity) getContext()).addQueue(time, point);
                        ((GameActivity) getContext()).setTurnNumber(value.getTurn());
                        ((GameActivity) getContext()).addLogString(value.getTurn() + ": "+gladiator.getName() + " set block\n");
                        break;
                    }
                    case "turnright":
                    {
                        TreeMap<Double, Point> queue = ((GameActivity) getContext()).getQueue();
                        int xPos1 = Integer.parseInt(value.getValue1().split(";")[0]);
                        int yPos1 = Integer.parseInt(value.getValue1().split(";")[1]);
                        int orient = field[yPos1][xPos1].getGladiatorDirection();
                        orient = (orient ==4)?1:orient+1;
                        field[yPos1][xPos1].setBlocked(false);
                        field[yPos1][xPos1].setGladiatorDirection(orient);
                        Gladiator gladiator = ((GameActivity) getContext()).getGladiatorById(field[yPos1][xPos1].getGladiator());
                        Point point = queue.firstEntry().getValue();
                        queue.remove(queue.firstKey());
                        Double time = queue.firstKey();
                        double spd = gladiator.getSpd()/5.00;
                        time = time + 2 / spd;
                        ((GameActivity) getContext()).addQueue(time, point);
                        ((GameActivity) getContext()).setTurnNumber(value.getTurn());
                        ((GameActivity) getContext()).addLogString(value.getTurn() + ": "+gladiator.getName() + " set block\n");
                        break;
                    }
                    case "turnleft":
                    {
                        TreeMap<Double, Point> queue = ((GameActivity) getContext()).getQueue();
                        int xPos1 = Integer.parseInt(value.getValue1().split(";")[0]);
                        int yPos1 = Integer.parseInt(value.getValue1().split(";")[1]);
                        int orient = field[yPos1][xPos1].getGladiatorDirection();
                        field[yPos1][xPos1].setBlocked(false);
                        orient = (orient ==1)?4:orient-1;
                        field[yPos1][xPos1].setGladiatorDirection(orient);
                        Gladiator gladiator = ((GameActivity) getContext()).getGladiatorById(field[yPos1][xPos1].getGladiator());
                        Point point = queue.firstEntry().getValue();
                        queue.remove(queue.firstKey());
                        Double time = queue.firstKey();
                        double spd = gladiator.getSpd()/5.00;
                        time = time + 2 / spd;
                        ((GameActivity) getContext()).addQueue(time, point);
                        ((GameActivity) getContext()).setTurnNumber(value.getTurn());
                        ((GameActivity) getContext()).addLogString(value.getTurn() + ": "+gladiator.getName() + " set block\n");
                        break;
                    }
                    case "getinfo":
                    {
                        TreeMap<Double, Point> queue = ((GameActivity) getContext()).getQueue();
                        Point point = queue.firstEntry().getValue();
                        field[point.getY()][point.getX()].setBlocked(false);
                        int xPos1 = Integer.parseInt(value.getValue1().split(";")[0]);
                        int yPos1 = Integer.parseInt(value.getValue1().split(";")[1]);
                        int xPos2 = Integer.parseInt(value.getValue2().split(";")[0]);
                        int yPos2 = Integer.parseInt(value.getValue2().split(";")[1]);
                        Gladiator gladiator2 = ((GameActivity) getContext()).getGladiatorById(field[yPos2][xPos2].getGladiator());
                        Gladiator gladiator1 = ((GameActivity) getContext()).getGladiatorById(field[yPos1][xPos1].getGladiator());

                        queue.remove(queue.firstKey());
                        Double time = queue.firstKey();
                        double intel = gladiator1.getIntel()/5.00;
                        time = time + 0.2 / intel;
                        ((GameActivity) getContext()).addQueue(time, point);
                        ((GameActivity) getContext()).setTurnNumber(value.getTurn());
                        ((GameActivity) getContext()).addLogString(value.getTurn() + ": "+gladiator1.getName() + " got info for "+gladiator2.getName() + "\n");
                        String info = logInfo(xPos2,yPos2, false);
                        ((GameActivity) getContext()).addLogString(info + "\n");

                        // progress
                        gladiator1.setIntel_progress(gladiator1.getIntel_progress()+1);
                        break;
                    }
                    case "kick":
                    {
                        TreeMap<Double, Point> queue = ((GameActivity) getContext()).getQueue();
                        Point point = queue.firstEntry().getValue();
                        field[point.getY()][point.getX()].setBlocked(false);
                        int xPos1 = Integer.parseInt(value.getValue1().split(";")[0]);
                        int yPos1 = Integer.parseInt(value.getValue1().split(";")[1]);
                        int xPos2 = Integer.parseInt(value.getValue2().split(";")[0]);
                        int yPos2 = Integer.parseInt(value.getValue2().split(";")[1]);
                        Gladiator gladiator2 = ((GameActivity) getContext()).getGladiatorById(field[yPos2][xPos2].getGladiator());
                        Gladiator gladiator1 = ((GameActivity) getContext()).getGladiatorById(field[yPos1][xPos1].getGladiator());

                        // health calculation
                        int precision = Integer.parseInt(value.getValue3().split(";")[0]);
                        int crit = Integer.parseInt(value.getValue3().split(";")[1]);
                        int popad = gladiator1.getDex() - gladiator2.getDex() + precision - 3;
                        int sila = 0;
                        int health = gladiator2.getHealth();
                        if (popad>0)
                        {
                            int defence = 0;
                            if (field[yPos2][xPos2].isBlocked())
                            {
                                defence = 3;
                            }
                            sila = gladiator1.getStr() + crit - 6 - defence;

                            if (sila>0)
                            {
                                health -= sila;
                                gladiator2.setHealth(health);

                                // progress
                                gladiator1.setMart_art_progress(gladiator1.getMart_art_progress()+1);
                                if (gladiator2.getHealth() <5)
                                {
                                    gladiator2.setCon_progress(gladiator2.getCon_progress()+1);
                                }
                            }
                        }
                        // stamina calculation
                        int stamina = gladiator1.getStamina_act()-3;
                        stamina = (stamina<0) ? 0:stamina;
                        gladiator1.setStamina_act(stamina);

                        queue.remove(queue.firstKey());

                            Double time = queue.firstKey();
                            double speed = gladiator1.getSpd() / 5.00;
                            time = time + 1.2 / speed;
                            ((GameActivity) getContext()).addQueue(time, point);

                        ((GameActivity) getContext()).setTurnNumber(value.getTurn());
                        ((GameActivity) getContext()).addLogString(value.getTurn() + ": "+gladiator1.getName() + " kicked "+ gladiator2.getName() + "\n");

                        Iterator it = queue.entrySet().iterator();
                        Double key = 0.00;
                        while (it.hasNext()) {
                            Map.Entry pair = (Map.Entry)it.next();
                            if (pair.getValue().equals(new Point (xPos2, yPos2))) {
                                key = (Double)pair.getKey();
                                break;
                            }
                        }
                        queue.remove(key);

                        if (popad>0) {
                            if (sila>0)
                            {
                                ((GameActivity) getContext()).addLogString("    " + gladiator2.getName() + " was wounded. -" + sila + " HP" +  "\n");
                            } else
                            {
                                ((GameActivity) getContext()).addLogString("    " + gladiator1.getName() + " kick was to light.\n");

                            }
                        } else
                        {
                            ((GameActivity) getContext()).addLogString("    " + gladiator2.getName() + " has dodged" + "\n");
                        }

                        if (health <= 0)
                        {
                            // Field update
                            field[yPos2][xPos2].setGladiator(-1);
                            field[yPos2][xPos2].setGladiatorDirection(-1);
                            field[yPos2][xPos2].setOwner(-1);
                            field[yPos2][xPos2].setBlocked(false);
                            ((GameActivity) getContext()).addLogString("    " + gladiator2.getName() + " has died." +  "\n");
                            if (value.getHost().equals("1"))
                                ((GameActivity) getContext()).setGladcountGamer2(((GameActivity) getContext()).getGladcountGamer2()-1);
                            else {
                                ((GameActivity) getContext()).setGladcountGamer1(((GameActivity) getContext()).getGladcountGamer1()-1);
                            }
                        }
                        // progress
                        if (crit >7)
                        {
                            gladiator1.setStr_progress(gladiator1.getStr_progress()+1);
                        }
                        if (precision >7)
                        {
                            gladiator1.setDex_progress(gladiator1.getDex_progress()+1);
                        }

                        break;
                    }
                    case "punch": {
                        TreeMap<Double, Point> queue = ((GameActivity) getContext()).getQueue();
                        Point point = queue.firstEntry().getValue();
                        field[point.getY()][point.getX()].setBlocked(false);
                        int xPos1 = Integer.parseInt(value.getValue1().split(";")[0]);
                        int yPos1 = Integer.parseInt(value.getValue1().split(";")[1]);
                        int xPos2 = Integer.parseInt(value.getValue2().split(";")[0]);
                        int yPos2 = Integer.parseInt(value.getValue2().split(";")[1]);
                        Gladiator gladiator2 = ((GameActivity) getContext()).getGladiatorById(field[yPos2][xPos2].getGladiator());
                        Gladiator gladiator1 = ((GameActivity) getContext()).getGladiatorById(field[yPos1][xPos1].getGladiator());

                        // health calculation
                        int precision = Integer.parseInt(value.getValue3().split(";")[0]);
                        int crit = Integer.parseInt(value.getValue3().split(";")[1]);
                        int popad = gladiator1.getDex() - gladiator2.getDex() + precision - 3 - 2;
                        int sila = 0;
                        int health = gladiator2.getHealth();
                        if (popad > 0) {
                            int defence = 0;
                            if (field[yPos2][xPos2].isBlocked()) {
                                defence = 1;
                            }
                            sila = gladiator1.getStr() + crit - 6 - defence + 2;
                            if (sila > 0) {
                                health -= sila;
                                gladiator2.setHealth(health);
                                // progress
                                gladiator1.setMart_art_progress(gladiator1.getMart_art_progress()+1);
                                if (gladiator2.getHealth() <5)
                                {
                                    gladiator2.setCon_progress(gladiator2.getCon_progress()+1);
                                }
                            }
                        }
                        // stamina calculation
                        int stamina = gladiator1.getStamina_act() - 2;
                        stamina = (stamina < 0) ? 0 : stamina;
                        gladiator1.setStamina_act(stamina);

                        queue.remove(queue.firstKey());

                        Double time = queue.firstKey();
                        double speed = gladiator1.getSpd() / 5.00;
                        time = time + 0.8 / speed;
                        ((GameActivity) getContext()).addQueue(time, point);

                        ((GameActivity) getContext()).setTurnNumber(value.getTurn());
                        ((GameActivity) getContext()).addLogString(value.getTurn() + ": "+gladiator1.getName() + " punched "+ gladiator2.getName() + "\n");
                        if (popad>0) {
                            if (sila>0)
                            {
                                ((GameActivity) getContext()).addLogString("    " + gladiator2.getName() + " was wounded. -" + sila + " HP" +  "\n");
                            } else
                            {
                                ((GameActivity) getContext()).addLogString("    " + gladiator1.getName() + " punch was to light.\n");

                            }
                        } else
                        {
                            ((GameActivity) getContext()).addLogString("    " + gladiator2.getName() + " has dodged" + "\n");
                        }
                        if (health <= 0)
                        {
                            // Field update
                            field[yPos2][xPos2].setGladiator(-1);
                            field[yPos2][xPos2].setGladiatorDirection(-1);
                            field[yPos2][xPos2].setOwner(-1);
                            field[yPos2][xPos2].setBlocked(false);

                            Iterator it = queue.entrySet().iterator();
                            Double key = 0.00;
                            while (it.hasNext()) {
                                Map.Entry pair = (Map.Entry)it.next();
                                if (pair.getValue().equals(new Point (xPos2, yPos2))) {
                                    key = (Double)pair.getKey();
                                    break;
                                }
                            }
                            queue.remove(key);
                            ((GameActivity) getContext()).addLogString("    " + gladiator2.getName() + " has died." +  "\n");
                            if (value.getHost().equals("1"))
                                ((GameActivity) getContext()).setGladcountGamer2(((GameActivity) getContext()).getGladcountGamer2()-1);
                            else {
                                ((GameActivity) getContext()).setGladcountGamer1(((GameActivity) getContext()).getGladcountGamer1()-1);
                            }
                        }

                        // progress
                        if (crit >7)
                        {
                            gladiator1.setStr_progress(gladiator1.getStr_progress()+1);
                        }
                        if (precision >7)
                        {
                            gladiator1.setDex_progress(gladiator1.getDex_progress()+1);
                        }
                        break;
                    }
                    case "walk":
                    {
                        TreeMap<Double, Point> queue = ((GameActivity) getContext()).getQueue();
                        Double time = queue.firstKey();
                        int xPos1 = Integer.parseInt(value.getValue1().split(";")[0]);
                        int yPos1 = Integer.parseInt(value.getValue1().split(";")[1]);
                        int xPos2 = Integer.parseInt(value.getValue2().split(";")[0]);
                        int yPos2 = Integer.parseInt(value.getValue2().split(";")[1]);

                        Gladiator gladiator = ((GameActivity) getContext()).getGladiatorById(field[yPos1][xPos1].getGladiator());

                        field[yPos2][xPos2].setGladiator(gladiator.getId());
                        field[yPos2][xPos2].setGladiatorDirection(field[yPos1][xPos1].getGladiatorDirection());
                        field[yPos2][xPos2].setOwner(field[yPos1][xPos1].getOwner());
                        field[yPos2][xPos2].setBlocked(false);

                        field[yPos1][xPos1].setGladiator(-1);
                        field[yPos1][xPos1].setGladiatorDirection(-1);
                        field[yPos1][xPos1].setOwner(-1);
                        field[yPos1][xPos1].setBlocked(false);

                        int speedMod = gladiator.getSpd();
                        double dist = 10;
                        if ((xPos1 != xPos2) && (yPos1 != yPos2))
                        {
                            dist = 14;
                        }

                        double speed = (Terrain.fromId(field[yPos1][xPos1].getGround()).getSpeed() + Terrain.fromId(field[yPos2][xPos2].getGround()).getSpeed())/2;
                        speed = speed*speedMod/5;
                        time = time + dist/speed;

                        Point point = queue.firstEntry().getValue();
                        queue.remove(queue.firstKey());

                        ((GameActivity) getContext()).addQueue(time, new Point(xPos2, yPos2));
                        ((GameActivity) getContext()).setTurnNumber(value.getTurn());

                        ((GameActivity) getContext()).addLogString(value.getTurn() + ": "+gladiator.getName() + " went to " + yPos2 + " " + xPos2 + "\n");
                        break;
                    }
                    case "run":
                    {
                        TreeMap<Double, Point> queue = ((GameActivity) getContext()).getQueue();
                        Double time = queue.firstKey();
                        int xPos1 = Integer.parseInt(value.getValue1().split(";")[0]);
                        int yPos1 = Integer.parseInt(value.getValue1().split(";")[1]);
                        int xPos2 = Integer.parseInt(value.getValue2().split(";")[0]);
                        int yPos2 = Integer.parseInt(value.getValue2().split(";")[1]);

                        Gladiator gladiator = ((GameActivity) getContext()).getGladiatorById(field[yPos1][xPos1].getGladiator());

                        field[yPos2][xPos2].setGladiator(gladiator.getId());
                        field[yPos2][xPos2].setGladiatorDirection(field[yPos1][xPos1].getGladiatorDirection());
                        field[yPos2][xPos2].setOwner(field[yPos1][xPos1].getOwner());
                        field[yPos2][xPos2].setBlocked(false);

                        field[yPos1][xPos1].setGladiator(-1);
                        field[yPos1][xPos1].setGladiatorDirection(-1);
                        field[yPos1][xPos1].setOwner(-1);
                        field[yPos1][xPos1].setBlocked(false);

                        int speedMod = gladiator.getSpd();
                        double dist = 10;
                        if ((xPos1 != xPos2) && (yPos1 != yPos2))
                        {
                            dist = 14;
                        }

                        double speed = (Terrain.fromId(field[yPos1][xPos1].getGround()).getSpeed() + Terrain.fromId(field[yPos2][xPos2].getGround()).getSpeed())/2;
                        speed = 2*speed*speedMod/5;
                        time = time + dist/speed;

                        // stamina calculation
                        int stamina = gladiator.getStamina_act()-4;
                        if (dist==14)
                        {
                            stamina=stamina-1;
                        }

                        stamina = (stamina<0) ? 0:stamina;
                        gladiator.setStamina_act(stamina);

                        Point point = queue.firstEntry().getValue();
                        queue.remove(queue.firstKey());

                        ((GameActivity) getContext()).addQueue(time, new Point(xPos2, yPos2));
                        ((GameActivity) getContext()).setTurnNumber(value.getTurn());

                        ((GameActivity) getContext()).addLogString(value.getTurn() + ": "+gladiator.getName() + " run to " + yPos2 + " " + xPos2 + "\n");

                        // progress
                        gladiator.setSpd_progress(gladiator.getSpd_progress()+1);
                    }
                }
            }
        }
    }

    public void drawField()
    {
        Cell[][] field = ((GameActivity) getContext()).getField();
        int xLength = field[0].length;
        int yLength = field.length;
        Point point = new Point(((GameActivity) getContext()).getActivField().getX(),((GameActivity) getContext()).getActivField().getY());
        if (!(((GameActivity) getContext()).isFirstPlayer()))
        {
            point.setX(xLength - point.getX() - 1);
            point.setY(yLength - point.getY() - 1);
        }

        if (field != null) {
            paint = new Paint();
            paint.setAntiAlias(true);
            paint.setDither(true);
            paint.setColor(0xffbebebe);
            paint.setStrokeWidth(5f);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeCap(Paint.Cap.ROUND);
            for (int i = 0; i < yLength; i++) {
                for (int j = 0; j < xLength; j++) {
                    int posX = j;
                    int posY = i;
                    if (!(((GameActivity) getContext()).isFirstPlayer()))
                    {
                        posX = xLength - posX - 1;
                        posY = yLength - posY - 1;
                    }

                    float x1 = j * canvasSize / xLength;
                    float x2 = (j+ 1) * canvasSize / xLength;
                    float y1 = i * canvasSize / yLength;
                    float y2 = (i + 1) * canvasSize / yLength;

                    switch (field[posY][posX].getGround()) {
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
                    if (field[posY][posX].getGladiator() != -1)
                    {
                        float pixel = (viewSize / xLength)/100;
                        float x0 = (pixel*15 + (viewSize / xLength) * j);
                        float y0 = (pixel*10 + (viewSize / yLength) * i);

                        int direction = field[posY][posX].getGladiatorDirection();

                        if (!(((GameActivity) getContext()).isFirstPlayer()))
                        {
                            switch (direction)
                            {
                                case 1:
                                    direction = 3;
                                    break;
                                case 2:
                                    direction = 4;
                                    break;
                                case 3:
                                    direction = 1;
                                    break;
                                case 4:
                                    direction = 2;
                                    break;
                            }
                        }
                        Bitmap bmp = ((GameActivity) getContext()).getmStore().get(String.format("glad%s_%s",field[posY][posX].getOwner(), direction));
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

                if (!(((GameActivity) getContext()).isFirstPlayer()))
                {
                    x = xLength - x - 1;
                    y = yLength - y - 1;
                }
                logInfo(x,y, true);
                return true;
            }

            @Override
            public void onLongPress(MotionEvent event) {
                //получаем координаты ячейки, по которой тапнули
                int eventX=(int)((event.getX()+getScrollX())/mScaleFactor);
                int eventY=(int)((event.getY()+getScrollY())/mScaleFactor);
                Cell[][] field = ((GameActivity) getContext()).getField();

                int xLength = field[0].length;
                int yLength = field.length;

                int x = (int)(xLength *eventX/viewSize);
                int y = (int)(yLength *eventY/viewSize);

                if (!(((GameActivity) getContext()).isFirstPlayer()))
                {
                    x = xLength - x - 1;
                    y = yLength - y - 1;
                }
                popup = new PopupMenu(getContext(), GameView.this);
                Point point = new Point(((GameActivity) getContext()).getActivField().getX(),((GameActivity) getContext()).getActivField().getY());

                int temp = (((GameActivity) getContext()).isFirstPlayer()) ? 1:2;
                if ((field[y][x].getGladiator()!=-1) && (field[y][x].getOwner()!=temp))
                {
                    popup.getMenu().add(1, R.id.menu_getinfo, 1, "get info");
                }
                if ((y==point.getY()) && (x==point.getX())) {
                    popup.getMenu().add(1, R.id.menu_skip, 1, "skip");
                    popup.getMenu().add(1, R.id.menu_block, 1, "block");
                    popup.getMenu().add(1, R.id.menu_turnleft, 1, "turn left");
                    popup.getMenu().add(1, R.id.menu_turnright, 1, "turn right");
                } else {
                    if ((Math.abs(point.getY()-y) <= 1) && (Math.abs(point.getX()-x) <= 1))
                    {
                        clickPoint = new Point(x,y);
                        if (field[y][x].getGladiator()==-1) {
                            popup.getMenu().add(1, R.id.menu_walk, 1, "walk");
                            popup.getMenu().add(1, R.id.menu_walk, 1, "run");
                        } else
                        {
                            if (field[y][x].getOwner()!=temp)
                            {
                                popup.getMenu().add(1, R.id.menu_kick, 1, "kick");
                                popup.getMenu().add(1, R.id.menu_punch, 1, "punch");
                            }

                        }
                    }

                }

                showContextMenu(GameView.this);
            }
        }

    // Display anchored popup menu based on view selected
    private void showContextMenu(View v) {
        // Inflate the menu from xml
        //popup.getMenuInflater().inflate(R.menu.popup_commands, popup.getMenu());
        // Setup menu item selection
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                Cell[][] field = ((GameActivity) getContext()).getField();
                Point point = new Point(((GameActivity) getContext()).getActivField().getX(),((GameActivity) getContext()).getActivField().getY());
                switch (item.getItemId()) {
                    case R.id.menu_turnleft:
                        ((GameActivity) getContext()).turnProcessing("turnleft", "" +((GameActivity) getContext()).getActivField().getX() +";"+((GameActivity) getContext()).getActivField().getY() + ";", "", "");
                        return true;
                    case R.id.menu_turnright:
                        ((GameActivity) getContext()).turnProcessing("turnright", "" +((GameActivity) getContext()).getActivField().getX() +";"+((GameActivity) getContext()).getActivField().getY() + ";", "", "");
                        return true;
                    case R.id.menu_skip:
                        ((GameActivity) getContext()).turnProcessing("skip", "" +((GameActivity) getContext()).getActivField().getX() +";"+((GameActivity) getContext()).getActivField().getY() + ";", "", "");
                        return true;
                    case R.id.menu_walk:
                        ((GameActivity) getContext()).turnProcessing("walk", "" +((GameActivity) getContext()).getActivField().getX() +";"+((GameActivity) getContext()).getActivField().getY() + ";", "" + clickPoint.getX() +";"+clickPoint.getY() + ";", "");
                        return true;
                    case R.id.menu_run:
                        Gladiator gladiator = ((GameActivity) getContext()).getGladiatorById(field[((GameActivity) getContext()).getActivField().getY()][((GameActivity) getContext()).getActivField().getX()].getGladiator());
                        if (gladiator.getStamina_act()>5) {
                            ((GameActivity) getContext()).turnProcessing("run", "" + ((GameActivity) getContext()).getActivField().getX() + ";" + ((GameActivity) getContext()).getActivField().getY() + ";", "" + clickPoint.getX() + ";" + clickPoint.getY() + ";", "");
                        } else {
                            Toast.makeText(getContext(), "Can't make - Stamina is low!",
                                    Toast.LENGTH_LONG).show();
                        }
                        return true;
                    case R.id.menu_block:
                        ((GameActivity) getContext()).turnProcessing("block", "" +((GameActivity) getContext()).getActivField().getX() +";"+((GameActivity) getContext()).getActivField().getY() + ";", "", "");
                        return true;
                    case R.id.menu_getinfo:
                        ((GameActivity) getContext()).turnProcessing("getinfo", "" +((GameActivity) getContext()).getActivField().getX() +";"+((GameActivity) getContext()).getActivField().getY() + ";", "" + clickPoint.getX() +";"+clickPoint.getY() + ";", "");
                        return true;
                    case R.id.menu_kick:
                        gladiator = ((GameActivity) getContext()).getGladiatorById(field[((GameActivity) getContext()).getActivField().getY()][((GameActivity) getContext()).getActivField().getX()].getGladiator());
                        if (gladiator.getStamina_act()>3) {
                            int randomNum1 = ThreadLocalRandom.current().nextInt(1, 10);
                            int randomNum2 = ThreadLocalRandom.current().nextInt(1, 10);
                            ((GameActivity) getContext()).turnProcessing("kick", "" + ((GameActivity) getContext()).getActivField().getX() + ";" + ((GameActivity) getContext()).getActivField().getY() + ";", "" + clickPoint.getX() + ";" + clickPoint.getY() + ";", "" + randomNum1 + ";" + randomNum2);
                        } else {
                            Toast.makeText(getContext(), "Can't make - Stamina is low!",
                                    Toast.LENGTH_LONG).show();
                        }
                        return true;
                    case R.id.menu_punch:
                        gladiator = ((GameActivity) getContext()).getGladiatorById(field[((GameActivity) getContext()).getActivField().getY()][((GameActivity) getContext()).getActivField().getX()].getGladiator());
                        if (gladiator.getStamina_act()>2) {
                        int randomNum1 = ThreadLocalRandom.current().nextInt(1, 10);
                        int randomNum2 = ThreadLocalRandom.current().nextInt(1, 10);
                            ((GameActivity) getContext()).turnProcessing("punch", "" +((GameActivity) getContext()).getActivField().getX() +";"+((GameActivity) getContext()).getActivField().getY() + ";", "" + clickPoint.getX() +";"+clickPoint.getY() + ";", ""+randomNum1+";"+randomNum2);

                        } else {
                            Toast.makeText(getContext(), "Can't make - Stamina is low!",
                                    Toast.LENGTH_LONG).show();
                        }
                            return true;
                    default:
                        return false;
                }
            }
        });
        popup.show();
    }

    public String logInfo(int x, int y, boolean isOwnerVerify)
    {
        Cell[][] field = ((GameActivity) getContext()).getField();

        StringBuffer text = new StringBuffer("Terrain:\n   ");
        text.append(Terrain.fromId(field[y][x].getGround()).getDescription());
        int temp = (((GameActivity) getContext()).isFirstPlayer()) ? 1:2;
        boolean isAllowed = true;
        if (isOwnerVerify)
        {
            isAllowed = (field[y][x].getOwner()==temp);
        }
        if ((isAllowed) &&(field[y][x].getGladiator() != -1))
        {
            for (Gladiator gladiator:((GameActivity) getContext()).getGladiators())
            {
                if (gladiator.getId() == field[y][x].getGladiator())
                {
                    text.append("\nGladiator:");
                    text.append("\n   Name: ");
                    text.append(gladiator.getName());
                    text.append("\n   Health Level: ");
                    text.append(gladiator.getHealth());
                    text.append("   Stamina Level: ");
                    text.append(gladiator.getStamina_act());
                    text.append("\n   Str: ");
                    text.append(gladiator.getStr());
                    text.append("   Dex: ");
                    text.append(gladiator.getDex());
                    text.append("\n   Spd: ");
                    text.append(gladiator.getSpd());
                    text.append("   Con: ");
                    text.append(gladiator.getCon());
                    text.append("\n   Int: ");
                    text.append(gladiator.getIntel());
                    text.append("   Stamina: ");
                    text.append(gladiator.getStamina());
                    text.append("\n   Martial Art: ");
                    text.append(gladiator.getMart_art());
                    break;
                }
            }
        }
        ((GameActivity) getContext()).getTextView().setText(text.toString());
        return text.toString();
    }
}