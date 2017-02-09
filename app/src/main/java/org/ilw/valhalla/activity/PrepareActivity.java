package org.ilw.valhalla.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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
import org.ilw.valhalla.data.GameStatus;
import org.ilw.valhalla.dto.Game;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.Point;
import org.ilw.valhalla.dto.User;
import org.ilw.valhalla.helper.SQLiteHandler;
import org.ilw.valhalla.helper.SessionManager;
import org.ilw.valhalla.views.GamePrepareView;
import org.ilw.valhalla.views.GladiatorsView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by Ilja.Winokurow on 05.01.2017.
 */
public class PrepareActivity extends Activity {
    private Button btnStartGame;
    private Button btnCancelGame;

    private SQLiteHandler db;
    private SessionManager session;
    private ProgressDialog pDialog;
    private static final String TAG = PrepareActivity.class.getSimpleName();

    protected Game game;
    protected User user;
    protected String fields;

    protected List<Gladiator> gladiators;
    protected List<Gladiator> gladiatorsWait;
    protected Map<Point, Gladiator> gladiatorsSet = new HashMap<>();

    protected boolean isFirstPlayer;

    protected GamePrepareView view;

    protected GladiatorsView view2;
    protected int active = -1;

    protected Point activePoint;
    protected boolean isPrepared;

    private HashMap<String, Bitmap> mStore = new HashMap<String, Bitmap>();

    int[][] preparedCells;

    final Handler timerHandler = new Handler();
    Runnable timerWaitForPlayerRunnable = new Runnable() {
        @Override
        public void run() {
            waitForPlayer();
            timerHandler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        timerHandler.removeCallbacks(timerWaitForPlayerRunnable);

        super.onCreate(savedInstanceState);
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        // SqLite database handler
        Bitmap bmp = BitmapFactory.decodeResource(getResources(),
                R.drawable.man);
        mStore.put("glad1", bmp);
        db = new SQLiteHandler(getApplicationContext());
        user = db.getUserDetails();
        game = db.getGameDetails();
        if ((game!=null) && (game.getStatus().equals("PREPARED_WAITING")))
        {
            isPrepared = true;
        }
        Log.d("ddf", new Boolean(isPrepared).toString());
        getGame(user.getId());

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

                            getGladiators1(game.getGamer1_id());
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

    public void getGladiators1(final String uuid) {

        // Tag used to cancel the request
        String tag_string_req = "get_Gladiators";
        String uri = AppConfig.URL_GLADIATOR + "/userid/" + uuid;
        Log.d(TAG, "here" + uri);
        StringRequest strReq = new StringRequest(Method.GET,
                uri, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                List<Gladiator> returnValue = null;
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        JSONArray obj = jObj.getJSONArray("data");
                        Gson gson = new GsonBuilder().create();
                        returnValue = gson.fromJson(obj.toString(), new TypeToken<ArrayList<Gladiator>>() {}.getType());
                        db.deleteGladiators();
                        db.addGladiator(returnValue);
                        if (isFirstPlayer)
                        {
                            gladiators = returnValue;
                            Log.d(TAG, gladiators.toString());
                            gladiatorsWait = cloneList(returnValue);
                        }
                        getGladiators2(game.getGamer2_id());
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
                hideDialog();
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d(TAG, "ERROR");
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

    public void getGladiators2(final String uuid) {

        // Tag used to cancel the request
        String tag_string_req = "get_Gladiators";
        String uri = AppConfig.URL_GLADIATOR + "/userid/" + uuid;
        Log.d(TAG, uri);
        StringRequest strReq = new StringRequest(Method.GET,
                uri, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                List<Gladiator> returnValue = null;
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        JSONArray userReq = jObj.getJSONArray("data");
                        Gson gson = new GsonBuilder().create();
                        returnValue = gson.fromJson(userReq.toString(), new TypeToken<ArrayList<Gladiator>>() {}.getType());
                        db.addGladiator(returnValue);
                        if (!isFirstPlayer)
                        {
                            gladiators = returnValue;
                            Log.d(TAG, gladiators.toString());
                            gladiatorsWait = cloneList(returnValue);
                        }
                        getFields(game.getField());
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());

                }
                hideDialog();
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

    /*public void getTurns(final String uuid, final String turnNumber) {
        isReady = false;
        // Tag used to cancel the request
        String tag_string_req = "get_Gladiators";
        String uri = AppConfig.URL_TURNS + "/game/" + uuid + "/current/" + turnNumber;

        StringRequest strReq = new StringRequest(Method.GET,
                uri, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                List<Turn> returnValue = null;
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        JSONObject userReq = jObj.getJSONObject("data");
                        Gson gson = new GsonBuilder().create();
                        returnValue = gson.fromJson(userReq.getString("data"), new TypeToken<ArrayList<Turn>>() {}.getType());
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
                turns = returnValue;
                isReady = true;
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
                isReady = true;
            }
        }) {

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }*/

    public void getFields(final String uuid) {
        // Tag used to cancel the request
        String tag_string_req = "get_Fields";
        String uri = AppConfig.URL_FIELD + "/" + uuid;

        StringRequest strReq = new StringRequest(Method.GET,
                uri, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, response);
                String returnValue = null;
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        returnValue = jObj.getString("data");
                        db.deleteCells();
                        db.addCells(returnValue);
                        fields = returnValue;
                        setContentView(R.layout.activity_prepare);
                        view = (GamePrepareView) findViewById(R.id.game_view);

                        if (!(isFirstPlayer))
                        {
                            fields = fields.replaceAll("098", "100");
                        } else
                        {
                            fields = fields.replaceAll("099", "100");
                        }
                        preparedCells = getIntFields();
                        // Add new Game

                        view.setCells(preparedCells);
                        view2 = (GladiatorsView) findViewById(R.id.gladiators_view);
                        if (isPrepared)
                        {
                            hideDialog();
                            pDialog.setMessage("Waiting for second player ...");
                            showDialog();
                            timerHandler.postDelayed(timerWaitForPlayerRunnable, 0);
                        } else {
                            btnCancelGame = (Button) findViewById(R.id.btnCancelGame);
                            btnStartGame = (Button) findViewById(R.id.btnStartGame);
                            // Cancel Game
                            if (user.getPoints() > 5) {
                                btnCancelGame.setOnClickListener(new View.OnClickListener() {

                                    public void onClick(View view) {
                                        cancelGame();
                                    }
                                });
                            } else {
                                btnCancelGame.setEnabled(false);
                                btnCancelGame.setAlpha(.5f);
                                btnCancelGame.setClickable(false);
                            }

                            btnStartGame.setOnClickListener(new View.OnClickListener() {

                                public void onClick(View view) {
                                    if (gladiatorsSet.size() == 0) {
                                        Toast.makeText(getApplicationContext(),
                                                "Please set the gladiators on field.", Toast.LENGTH_LONG).show();
                                    } else {

                                        getGameStatus();
                                    }
                                }
                            });
                            //view2.setGladiators(gladiators);

                            hideDialog();
                        }
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                    hideDialog();
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
                params.put("uuid", uuid);
                return params;
            }
        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    private int[][] getIntFields() {
        int[][] returnValue = null;
        if (!(fields.isEmpty())) {
            String rows[] = fields.split(";");
            int rowLength = rows[0].split(",").length;
            returnValue = new int[rows.length][rowLength];
            if (isFirstPlayer) {
                for (int i = 0; i < rows.length; i++) {
                    for (int j = 0; j < rowLength; j++) {
                        Log.d("here", rows[i].split(",")[j]);
                        returnValue[i][j] = Integer.parseInt(rows[i].split(",")[j]);
                    }
                }
            } else {
                for (int i = 0; i < rows.length; i++) {
                    for (int j = 0; j < rowLength; j++) {
                        returnValue[rows.length-i-1][rowLength-j-1] = Integer.parseInt(rows[i].split(",")[j]);
                    }
                }
            }
        }
        return returnValue;
    }

    public int getActive() {
        return active;
    }

    public void setActive(int active) {
        this.active = active;
    }

    public HashMap<String, Bitmap> getmStore() {
        return mStore;
    }

    public void setmStore(HashMap<String, Bitmap> mStore) {
        this.mStore = mStore;
    }

    public List<Gladiator> getGladiators() {
        return gladiators;
    }

    public void setGladiators(List<Gladiator> gladiators) {
        this.gladiators = gladiators;
    }

    public List<Gladiator> getGladiatorsWait() {
        return gladiatorsWait;
    }

    public void setGladiatorsWait(List<Gladiator> gladiatorsWait) {
        this.gladiatorsWait = gladiatorsWait;
    }

    public GladiatorsView getView2() {
        return view2;
    }

    public void setView2(GladiatorsView view2) {
        this.view2 = view2;
    }

    public Map<Point, Gladiator> getGladiatorsSet() {
        return gladiatorsSet;
    }

    public void setGladiatorsSet(Map<Point, Gladiator> gladiatorsSet) {
        this.gladiatorsSet = gladiatorsSet;
    }

    public Point getActivePoint() {
        return activePoint;
    }

    public void setActivePoint(Point activePoint) {
        this.activePoint = activePoint;
    }

    public static List<Gladiator> cloneList(List<Gladiator> list) {
        List<Gladiator> clone = new ArrayList<Gladiator>(list.size());
        for (Gladiator item : list) clone.add(item);
        return clone;
    }

    public GamePrepareView getView() {
        return view;
    }

    public void setView(GamePrepareView view) {
        this.view = view;
    }

    /**
     * function to cancel a game
     * */
    private void cancelGame() {
        // Tag used to cancel the request
        String tag_string_req = "req_gameGame";

        pDialog.setMessage("Canceling the Game ...");
        showDialog();
        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_CREATENEWGAME, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Cancel Game Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        db.setGameStatus(GameStatus.ENDED.asString(), game.getId());
                        setUserPoints();
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
                params.put("userid", game.getGamer2_id());
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

    /**
     * function to start game
     * */
    private void setUserPoints() {
        // Tag used to cancel the request
        String tag_string_req = "req_setPoints";

        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_USER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Set Points Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        db.setUserPoints(user.getId(), new Integer(user.getPoints()-5).toString(), new Integer(user.getLevel()).toString());
                        Intent intent = new Intent(PrepareActivity.this, MainActivity.class);
                        startActivity(intent);
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
                params.put("uiid", user.getId());
                params.put("points", new Integer(user.getPoints()-5).toString());
                params.put("level", new Integer(user.getLevel()).toString());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Wait for second player
     * */
    private void waitForPlayer() {
        // Tag used to cancel the request
        String tag_string_req = "req_getGame";
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_CREATENEWGAME + "/game/"+game.getId(), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get Status: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        JSONObject gameObj = jObj.getJSONObject("data");

                        String status = gameObj.getString("status");

                        if (status.equals(GameStatus.ENDED.asString())) {
                            timerHandler.removeCallbacks(timerWaitForPlayerRunnable);
                            Toast.makeText(getApplicationContext(),
                                    "Second user has canceled the game.", Toast.LENGTH_LONG).show();
                            db.deleteGame();
                            Intent intent = new Intent(PrepareActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                        if (status.equals(GameStatus.STARTED.asString())) {
                            timerHandler.removeCallbacks(timerWaitForPlayerRunnable);
                            db.setGameStatus(GameStatus.STARTED.asString(), game.getId());
                            Intent intent = new Intent(PrepareActivity.this, GameActivity.class);
                            startActivity(intent);
                            finish();
                        }
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
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * function to start game
     * */
    private void startGameReq() {
        // Tag used to cancel the request
        String tag_string_req = "req_startGame";

        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_CREATENEWGAME, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Start Game Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        db.setGameStatus(GameStatus.STARTED.asString(), game.getId());
                        addTurn2();
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
                params.put("status", "STARTED");
                params.put("userid", game.getGamer2_id());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Read status
     * */
    private void getGameStatus() {
        // Tag used to cancel the request
        String tag_string_req = "req_getGameStatus";
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_CREATENEWGAME + "/game/"+game.getId(), new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get Status: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        JSONObject gameObj = jObj.getJSONObject("data");
                        String status = gameObj.getString("status");
                        if (status.equals("PREPARED_WAITING"))
                        {
                            startGameReq();
                        }
                        else {
                            setStatusPrepareWaiting();
                        }
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
                params.put("gameid", game.getId());
                params.put("host", "ENDED");
                params.put("userid", game.getGamer2_id());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * function to start game
     * */
    private void setStatusPrepareWaiting() {
        // Tag used to cancel the request
        String tag_string_req = "req_startGame";

        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_CREATENEWGAME, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Start Game Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        db.setGameStatus(GameStatus.PREPARE_WAITING.asString(), game.getId());
                        game = db.getGameDetails();
                        Log.d("ddf", "STATUS" + game.getStatus());
                        pDialog.setMessage("Waiting for second player ...");
                        showDialog();
                        addTurn1();

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
                params.put("status", "PREPARED_WAITING");
                params.put("userid", game.getGamer2_id());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * function to add turn
     * */
    private void addTurn1() {
        // Tag used to cancel the request
        String tag_string_req = "req_addTurn1";

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
                        timerHandler.postDelayed(timerWaitForPlayerRunnable, 0);
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
                params.put("gameid", game.getId());
                if (isFirstPlayer)
                {
                    params.put("host", "1");
                } else {
                    params.put("host", "2");
                }
                params.put("turn", "-11");
                params.put("action", "set");
                String glad = "";
                Iterator it = gladiatorsSet.entrySet().iterator();
                while (it.hasNext()) {

                    Map.Entry pair = (Map.Entry)it.next();
                    if (isFirstPlayer) {
                        glad = String.format("%s:%s:%s:%s;", ((Point) pair.getKey()).getX(), ((Point) pair.getKey()).getY(), "1", ((Gladiator) pair.getValue()).getId());
                    } else {
                        int xLength = (preparedCells[0].length + 1) / 2;
                        int yLength = (preparedCells.length + 1) / 2;
                        glad = String.format("%s:%s:%s:%s;", xLength-((Point) pair.getKey()).getX() - 1, yLength - ((Point) pair.getKey()).getY() - 1, "3", ((Gladiator) pair.getValue()).getId());
                    }
                    it.remove(); // avoids a ConcurrentModificationException
                }
                params.put("value1", glad);
                    return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * function to add turn
     * */
    private void addTurn2() {
        // Tag used to cancel the request
        String tag_string_req = "req_addTurn2";

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
                        Intent intent = new Intent(PrepareActivity.this, GameActivity.class);
                        startActivity(intent);
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

                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("gameid", game.getId());
                if (isFirstPlayer)
                {
                    params.put("host", "1");
                } else {
                    params.put("host", "2");
                }
                params.put("turn", "-10");
                params.put("action", "set");
                String glad = "";
                Iterator it = gladiatorsSet.entrySet().iterator();
                while (it.hasNext()) {

                    Map.Entry pair = (Map.Entry)it.next();
                    if (isFirstPlayer) {
                        glad = String.format("%s:%s:%s:%s;", ((Point) pair.getKey()).getX(), ((Point) pair.getKey()).getY(), "1", ((Gladiator) pair.getValue()).getId());
                    } else {
                        int xLength = (preparedCells[0].length + 1) / 2;
                        int yLength = (preparedCells.length + 1) / 2;
                        glad = String.format("%s:%s:%s:%s;", xLength-((Point) pair.getKey()).getX() - 1, yLength - ((Point) pair.getKey()).getY() - 1, "3", ((Gladiator) pair.getValue()).getId());
                    }
                    it.remove(); // avoids a ConcurrentModificationException
                }
                params.put("value1", glad);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }
}
