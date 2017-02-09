package org.ilw.valhalla.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

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
                            getTurns(game.getId());
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

    public void getTurns(final String gameId) {
        // Tag used to cancel the request
        String tag_string_req = "get_Game";
        String uri = AppConfig.URL_TURNS + "/game/" + gameId + "/current/-12";
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
                    Point point = getActivField();

                    int host = field[point.getY()][point.getX()].getOwner();

                    int temp = isFirstPlayer ? 1:2;
                    if (!(temp == host))
                    {
                        pDialog.setMessage("Waiting for second player ...");
                        showDialog();
                    }
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

    public Map<Double, Point> getQueue() {
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
}
