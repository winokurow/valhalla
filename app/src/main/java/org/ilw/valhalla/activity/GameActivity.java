package org.ilw.valhalla.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.ilw.valhalla.R;
import org.ilw.valhalla.app.AppConfig;
import org.ilw.valhalla.app.AppController;
import org.ilw.valhalla.dto.Cell;
import org.ilw.valhalla.dto.Game;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.Point;
import org.ilw.valhalla.dto.Turn;
import org.ilw.valhalla.dto.User;
import org.ilw.valhalla.helper.SQLiteHandler;
import org.ilw.valhalla.helper.SessionManager;
import org.ilw.valhalla.views.GameView;
import org.ilw.valhalla.views.QueueView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Ilja.Winokurow on 05.01.2017.
 */
public class GameActivity extends Activity {
    private SQLiteHandler db;
    private SessionManager session;
    private Cell[][] field;
    private Map<Integer, Turn> turns;
    private TreeMap<Double, Point> queue;
    private static final String TAG = GameActivity.class.getSimpleName();
    protected boolean isFirstPlayer;
    protected User user;
    private ProgressDialog pDialog;
    private Game game;
    protected GameView gameView;
    protected QueueView queueView;
    protected TextView textView;
    protected List<Gladiator> gladiators;
    private HashMap<String, Bitmap> mStore = new HashMap<String, Bitmap>();
    private int turnNumber=-20;
    private Gladiator activeGladiator;
    private String logString = "";
    public boolean isLocked;
    int gladcountGamer1;
    int gladcountGamer2;
    PopupWindow popupWindow;
    TextView textOut;

    final Handler timerHandler = new Handler();
    Runnable timerWaitForTurnRunnable = new Runnable() {
        @Override
        public void run() {
            if (!(isLocked))
            {
                getTurnsFromServer();
            }
            timerHandler.postDelayed(this, 2000);
        }
    };

    public Context getContext() {
        return (Context)this;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);


        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        timerHandler.removeCallbacks(timerWaitForTurnRunnable);

        gameView = (GameView) findViewById(R.id.game_view);
        queueView = (QueueView) findViewById(R.id.queue_view);

        textView = (TextView) findViewById(R.id.text_id);
        queue = new TreeMap <>();

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        user = db.getUserDetails();
        getGame(user.getId());
        //gameView.setCells(cells);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.man_1_1);
        mStore.put("glad1_1", bmp);
        bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.man_1_2);
        mStore.put("glad1_2", bmp);
        bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.man_1_3);
        mStore.put("glad1_3", bmp);
        bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.man_1_4);
        mStore.put("glad1_4", bmp);
        bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.man_2_1);
        mStore.put("glad2_1", bmp);
        bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.man_2_2);
        mStore.put("glad2_2", bmp);
        bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.man_2_3);
        mStore.put("glad2_3", bmp);
        bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.man_2_4);
        mStore.put("glad2_4", bmp);
    }

    public void getGame(final String uuid) {
        // Tag used to cancel the request
        String tag_string_req = "get_Game";
        String uri = AppConfig.URL_CREATENEWGAME + "/userid/" + uuid;
        Log.i(TAG, uri);
        StringRequest strReq = new StringRequest(Method.GET,
                uri, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Game returnValue = null;
                try {
                    Log.d(TAG, response);
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        if (!(jObj.isNull("data")))
                        {
                            JSONObject userReq = jObj.getJSONObject("data");
                            Gson gson = new GsonBuilder().create();
                            returnValue = gson.fromJson(userReq.toString(), Game.class);
                            game = returnValue;
                            db.deleteGame();
                            db.addGame(game);
                            isFirstPlayer = (user.getId().equals(game.getGamer1_id())) ? true : false;
                            getIntFields();
                            getTurnsFromServer();
                            gladiators = db.getGladiatorsDetails();
                        }
                    }
                }
                catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    public void getTurnsFromServer() {
        isLocked = true;
        // Tag used to cancel the request
        String tag_string_req = "get_Game";
        String uri = AppConfig.URL_TURNS + "/game/" + game.getId() + "/current/" + turnNumber;
        Log.i(TAG, uri);
        StringRequest strReq = new StringRequest(Method.GET,
                uri, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {

                try {
                    Log.d(TAG, response);
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        if (!(jObj.isNull("data")))
                        {
                            JSONArray obj = jObj.getJSONArray("data");
                            Gson gson = new GsonBuilder().create();
                            List<Turn> returnValue = gson.fromJson(obj.toString(), new TypeToken<ArrayList<Turn>>() {}.getType());
                            turns = new TreeMap<Integer, Turn>();
                            for (Turn turn:returnValue)
                            {
                                turns.put(turn.getTurn(),turn);
                            }
                        }
                    }
                    gameView.prepareTurn();
                    gameView.drawField();
                    gameView.invalidate();
                    queueView.invalidate();
                    getTextView().setText(logString);
                    scrollInfo();
                    gameEndVerification();
                    isLocked = false;
                    //hideDialog();
                }
                catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                //hideDialog();
            }
        }) {
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
    public Point getActivField() {
        return queue.firstEntry().getValue();
    }

    private void getIntFields() {
        int[][] returnValue = null;
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        String cells = db.getFieldCells().get("cells");
        Log.d(TAG, "Cells" + cells);
        if (!(cells.isEmpty())) {
            String rows[] = cells.split(";");
            int rowLength = rows[0].split(",").length;
            int yLength = (rows.length+1)/2;
            int xLength = (rowLength+1)/2;
            field = new Cell[yLength][xLength];
                for (int i = 0; i < yLength; i++) {
                    for (int j = 0; j < xLength; j++) {
                        int iTemp = Integer.parseInt(rows[i*2].split(",")[j*2].replaceAll("^[0]", ""));
                        field[i][j] = new Cell();
                        field[i][j].setGround(iTemp);
                    }
                }
        }
    }

    public void turnProcessing(String action, String value1, String value2, String value3)
    {
        isLocked = true;
        Turn turn;
        switch(action)
        {
            case "walk":


                break;
        }
        String host = isFirstPlayer?"1":"2";
        turn = new Turn (Integer.parseInt(game.getId()), turnNumber+1, host, action, value1, value2, value3, "", "");
        turns.put(turn.getTurn(), turn);
        gameView.prepareTurn();
        gameView.drawField();
        gameView.invalidate();
        queueView.invalidate();
        getTextView().setText(logString);
        scrollInfo();
        gameEndVerification();
        addTurn(turn);
        isLocked = false;
    }

    /**
     * function to add turn
     * */
    private void addTurn(final Turn turn) {
        // Tag used to cancel the request
        String tag_string_req = "req_addTurn";

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_TURNS, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Add Turn Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        //timerHandler.postDelayed(timerWaitForPlayerRunnable, 0);
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {

                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("gameid", new Integer(turn.getGamedid()).toString());
                params.put("host", turn.getHost());
                params.put("turn", new Integer(turn.getTurn()).toString());
                params.put("action", turn.getAction());
                params.put("value1", turn.getValue1());
                params.put("value2", turn.getValue2());
                params.put("value3", turn.getValue3());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * function to add turn
     * */
    private void gameEndVerification() {
        if ((gladcountGamer1==0) || (gladcountGamer2==0))
        {
            timerHandler.removeCallbacks(timerWaitForTurnRunnable);
            endGame();
        }
    }

    /**
     * method to end game
     * */
    private void endGame() {
        // Tag used to cancel the request
        String tag_string_req = "req_endGame";

        pDialog.setMessage("Ending Game ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_CREATENEWGAME, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "End Game Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {

                        boolean isWin = true;
                        if ((gladcountGamer1==0) && (isFirstPlayer)) {
                            isWin = false;
                        }
                        if ((gladcountGamer2==0) && (!(isFirstPlayer))) {
                            isWin = false;
                        }

                        String text = "You won!";
                        if (!(isWin)) {
                            text = "You lose!";
                        }

                        if (isWin)
                        {
                            if ((turnNumber>9) && (turnNumber<=18)) {
                                user.setPoints(user.getPoints() + turnNumber-18);
                                text += "\nYou have " + (turnNumber-18) + "more fans";
                            }
                        }
                        for (Gladiator gladiator:gladiators)
                        {
                            if (gladiator.getUserid() == new Integer(user.getId()))
                            {
                                String text1 = "";
                                if (gladiator.getStr_progress()>(int)Math. pow(2, gladiator.getStr()))
                                {
                                    text1 +="\nStrength +1";
                                    gladiator.setStr_progress(0);
                                    gladiator.setStr(gladiator.getStr()+1);
                                }
                                if (gladiator.getIntel_progress()>(int)Math. pow(2, gladiator.getIntel()))
                                {
                                    text1 +="\nIntellect +1";
                                    gladiator.setIntel_progress(0);
                                    gladiator.setIntel(gladiator.getIntel()+1);
                                }
                                if (gladiator.getSpd_progress()>(int)Math. pow(2, gladiator.getSpd()))
                                {
                                    text1 +="\nSpeed +1";
                                    gladiator.setSpd_progress(0);
                                    gladiator.setSpd(gladiator.getSpd()+1);
                                }
                                if (gladiator.getDex_progress()>(int)Math. pow(2, gladiator.getDex()))
                                {
                                    text1 +="\nDexterity +1";
                                    gladiator.setDex_progress(0);
                                    gladiator.setDex(gladiator.getDex()+1);
                                }
                                if (gladiator.getStamina_progress()>(int)Math. pow(2, gladiator.getStamina()))
                                {
                                    text1 +="\nStamina +1";
                                    gladiator.setStamina_progress(0);
                                    gladiator.setStamina(gladiator.getStamina()+1);
                                }
                                if (gladiator.getCon_progress()>(int)Math. pow(2, gladiator.getCon()))
                                {
                                    text1 +="\nConstitution +1";
                                    gladiator.setCon_progress(0);
                                    gladiator.setCon(gladiator.getCon()+1);
                                }
                                if (gladiator.getMart_art()>(int)Math. pow(2, gladiator.getMart_art()*5))
                                {
                                    text1 +="\nMartial arts +1";
                                    gladiator.setMart_art_progress(0);
                                    gladiator.setMart_art(gladiator.getMart_art()+1);
                                }

                                if (!(text1.isEmpty()))
                                {
                                    text1 = "\n" + gladiator.getName() + text1;
                                }
                                text += text1;
                            }
                        }



                        //Update TextView in PopupWindow dynamically
                        LayoutInflater layoutInflater =
                                (LayoutInflater)getBaseContext()
                                        .getSystemService(LAYOUT_INFLATER_SERVICE);
                        View popupView = layoutInflater.inflate(R.layout.popup, null);
                        popupWindow = new PopupWindow(
                                popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        textOut = (TextView)popupView.findViewById(R.id.textout);

                        Button btnDismiss = (Button)popupView.findViewById(R.id.dismiss);

                        btnDismiss.setOnClickListener(new Button.OnClickListener(){

                            @Override
                            public void onClick(View v) {
                                updateGladiator(0);

                            }});

                        textOut.setText(text);

                        popupWindow.showAtLocation(gameView, Gravity.CENTER, 0, 0);
                    } else {
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("uiid", game.getId());
                params.put("status", "ENDED");
                params.put("userid", user.getId());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * method to end game
     * */
    private void updateGladiator(int start) {
        // Tag used to cancel the request
        String tag_string_req = "req_updateGladiator";
        if (start == gladiators.size())
        {
            updateUser();
            return;
        }
        for (int i=start;i<gladiators.size();i++) {

            if (gladiators.get(i).getUserid() == new Integer(user.getId())) {
                final int count = i;
                StringRequest strReq = new StringRequest(Request.Method.PUT,
                        AppConfig.URL_GLADIATOR, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "End Game Response: " + response.toString());
                        hideDialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");

                            // Check for error node in json
                            if (!error) {
                                updateGladiator(count + 1);
                                return;
                            } else {
                                String errorMsg = jObj.getString("error_msg");
                                Toast.makeText(getApplicationContext(),
                                        errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Login Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_LONG).show();
                        hideDialog();
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("id", "" + gladiators.get(count).getId());
                        params.put("name", "" + gladiators.get(count).getName());
                        params.put("str", "" + gladiators.get(count).getStr());
                        params.put("str_progress", "" + gladiators.get(count).getStr_progress());
                        params.put("dex", "" + gladiators.get(count).getDex());
                        params.put("dex_progress", "" + gladiators.get(count).getDex_progress());
                        params.put("spd", "" + gladiators.get(count).getSpd());
                        params.put("spd_progress", "" + gladiators.get(count).getSpd_progress());
                        params.put("con", "" + gladiators.get(count).getCon());
                        params.put("con_progress", "" + gladiators.get(count).getCon_progress());
                        params.put("int", "" + gladiators.get(count).getIntel());
                        params.put("int_progress", "" + gladiators.get(count).getIntel_progress());
                        params.put("stamina", "" + gladiators.get(count).getStamina());
                        params.put("stamina_progress", "" + gladiators.get(count).getStamina_progress());
                        params.put("mart_art", "" + gladiators.get(count).getMart_art());
                        params.put("mart_art_progress", "" + gladiators.get(count).getMart_art_progress());
                        return params;
                    }

                };

                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

                break;
            }
        }
    }

    /**
     * method to end game
     * */
    private void updateUser() {
        // Tag used to cancel the request
        String tag_string_req = "req_updateUser";

                StringRequest strReq = new StringRequest(Request.Method.PUT,
                        AppConfig.URL_USER, new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {
                        Log.d(TAG, "End Game Response: " + response.toString());
                        hideDialog();

                        try {
                            JSONObject jObj = new JSONObject(response);
                            boolean error = jObj.getBoolean("error");

                            // Check for error node in json
                            if (!error) {
                                closeDialog();
                                db.deleteCells();
                                db.deleteCells();
                                db.deleteGladiators();
                                db.deleteTurns();
                                Intent i = new Intent(getApplicationContext(),
                                        MainActivity.class);
                                startActivity(i);
                                finish();
                            } else {
                                String errorMsg = jObj.getString("error_msg");
                                Toast.makeText(getApplicationContext(),
                                        errorMsg, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            // JSON error
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e(TAG, "Login Error: " + error.getMessage());
                        Toast.makeText(getApplicationContext(),
                                error.getMessage(), Toast.LENGTH_LONG).show();
                        hideDialog();
                    }
                }) {

                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> params = new HashMap<String, String>();
                        params.put("uiid", "" + user.getId());
                        params.put("points", "" + user.getPoints());
                        params.put("level", "" + user.getLevel());
                        return params;
                    }

                };

                // Adding request to request queue
                AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private void closeDialog() {
        if (popupWindow.isShowing())
            popupWindow.dismiss();
    }

    public Map<Integer, Turn> getTurns() {
        return turns;
    }

    public void setTurns(Map<Integer, Turn> turns) {
        this.turns = turns;
    }

    public Cell[][] getField() {
        return field;
    }

    public void setField(Cell[][] field) {
        this.field = field;
    }

    public HashMap<String, Bitmap> getmStore() {
        return mStore;
    }

    public void setmStore(HashMap<String, Bitmap> mStore) {
        this.mStore = mStore;
    }

    public boolean isFirstPlayer() {
        return isFirstPlayer;
    }

    public void setFirstPlayer(boolean firstPlayer) {
        isFirstPlayer = firstPlayer;
    }

    public TreeMap<Double, Point> getQueue() {
        return queue;
    }

    public void setQueue(TreeMap<Double, Point> queue) {
        this.queue = queue;
    }

    public void addQueue(double time, Point cell) {
        double key = time;
        while (this.queue.containsKey(key))
        {
            key = key + 0.000000000001;
        }
        this.queue.put(key, cell);

        Point point = getActivField();
        int host = field[point.getY()][point.getX()].getOwner();

        int temp = isFirstPlayer ? 1:2;
        if (!(temp == host))
        {
            pDialog.setMessage("Waiting for second player ...");
            showDialog();
            timerHandler.postDelayed(timerWaitForTurnRunnable, 0);
        } else
        {
            hideDialog();
            timerHandler.removeCallbacks(timerWaitForTurnRunnable);
        }
    }

    public TextView getTextView() {
        return textView;
    }

    public void setTextView(TextView textView) {
        this.textView = textView;
    }

    public List<Gladiator> getGladiators() {
        return gladiators;
    }

    public void setGladiators(List<Gladiator> gladiators) {
        this.gladiators = gladiators;
    }


    public int getTurnNumber() {
        return turnNumber;
    }

    public void setTurnNumber(int turnNumber) {
        this.turnNumber = turnNumber;
    }

    public Gladiator getActiveGladiator() {
        return activeGladiator;
    }

    public void setActiveGladiator(Gladiator activeGladiator) {
        this.activeGladiator = activeGladiator;
    }

    public String getLogString() {
        return logString;
    }

    public void addLogString(String logString) {
        this.logString += logString;
    }

    public Gladiator getGladiatorById(int id) {
        for (Gladiator glad : gladiators) {
            if (glad.getId() == id) {
                return glad;
            }
        }
        return null;
    }
public void scrollInfo() {
    final ScrollView scroll = (ScrollView) this.findViewById(R.id.SCROLLER_ID);
    scroll.post(new Runnable() {
        @Override
        public void run() {
            scroll.fullScroll(ScrollView.FOCUS_DOWN);
        }
    });
}

    public int getGladcountGamer1() {
        return gladcountGamer1;
    }

    public void setGladcountGamer1(int gladcountGamer1) {
        this.gladcountGamer1 = gladcountGamer1;
    }

    public int getGladcountGamer2() {
        return gladcountGamer2;
    }

    public void setGladcountGamer2(int gladcountGamer2) {
        this.gladcountGamer2 = gladcountGamer2;
    }
}
