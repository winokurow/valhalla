package org.ilw.valhalla.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

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
    final Handler timerHandler = new Handler();
    Runnable timerWaitForTurnRunnable = new Runnable() {
        @Override
        public void run() {
            getTurnsFromServer();
            timerHandler.postDelayed(this, 2000);
        }
    };
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
                    Log.d("TAG", logString);
                    getTextView().setText(logString);
                    Log.d("TAG", "Textfeld0 " + getTextView().getText());
                    scrollInfo();
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

        addTurn(turn);
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

    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
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

}
