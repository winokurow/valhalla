package org.ilw.valhalla.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
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
import org.ilw.valhalla.data.GameStatus;
import org.ilw.valhalla.dto.Game;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.User;
import org.ilw.valhalla.helper.SQLiteHandler;
import org.ilw.valhalla.helper.SessionManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends Activity {

    private TextView txtName;
    private TextView txtPoints;
    private Button btnLogout;
    private Button btnAddNewGame;
    private LinearLayout myLinearLayout;
    private LinearLayout waitviewLayout;
    private Context context;

    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private static final String TAG = MainActivity.class.getSimpleName();

    private List<Game> games;
    private List<Game> oldgames;
    private Game game;
    private String cells;
    private String gameid;
    private List<Gladiator> gladiators;
    private User user = null;
    private boolean isReady;
    private boolean isShown;


    private int active;

    final Handler timerHandler = new Handler();
    Runnable timerRefreshGamesRunnable = new Runnable() {
        @Override
        public void run() {
            getGames(GameStatus.WAITING.asString());
            timerHandler.postDelayed(this, 10000);
        }
    };
    Runnable timerWaitForPlayerRunnable = new Runnable() {
        @Override
        public void run() {
            waitForPlayer();
            timerHandler.postDelayed(this, 10000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = (TextView) findViewById(R.id.name);
        txtPoints = (TextView) findViewById(R.id.points);
        btnLogout = (Button) findViewById(R.id.btnLogout);

        btnAddNewGame = (Button) findViewById(R.id.btnAddGame);

        myLinearLayout = (LinearLayout) findViewById(R.id.gameslist);
        waitviewLayout = (LinearLayout) findViewById(R.id.waitview);
        context = this;
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);


        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            timerHandler.removeCallbacks(timerRefreshGamesRunnable);
            timerHandler.removeCallbacks(timerWaitForPlayerRunnable);
            logoutUser();
        }

        // Fetching user details from sqlite
        user = db.getUserDetails();

        String name = user.getName();
        int points = user.getPoints();

        // Displaying the user details on the screen
        txtName.setText(name);
        txtPoints.setText("Fans: " + points);


        // Add new Game
        btnAddNewGame.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                addNewGame(user.getId());
            }
        });

            // Logout button click event
            btnLogout.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    logoutUser();
                }
            });

            db.deleteGame();

            this.getGame(user.getId());

    }
    private void removeWaitView() {
        Log.d(TAG, "Remove View");
        btnAddNewGame.setEnabled(true);
        btnAddNewGame.setAlpha(1f);
        btnAddNewGame.setClickable(true);
        removeAllViews();
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        Log.d(TAG, "here");
        timerHandler.removeCallbacks(timerRefreshGamesRunnable);
        timerHandler.removeCallbacks(timerWaitForPlayerRunnable);
        Log.d(TAG, "there");
        session.setLogin(false);

        db.deleteUsers();
        db.deleteGame();
        // Launching the login activity
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * function to add new game
     * */
    private void addNewGame(final String user) {
        // Tag used to cancel the request
        String tag_string_req = "req_addNewGame";

        pDialog.setMessage("Adding Game ...");
        showDialog();

        StringRequest strReq = new StringRequest(Request.Method.POST,
                AppConfig.URL_CREATENEWGAME, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Create New Game Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // Now store the game in SQLite
                        JSONObject userReq = jObj.getJSONObject("data");
                        Gson gson = new GsonBuilder().create();
                        Game game = gson.fromJson(userReq.toString(), Game.class);

                        // Inserting row in users table
                        Log.d(TAG, "++++++++++++++" + game.getGamer1_id());
                        db.addGame(game);
                        gameid=game.getId();
                        timerHandler.removeCallbacks(timerRefreshGamesRunnable);
                        createWaitView(gameid);
                    } else {
                        // Error in game creation
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
                params.put("user", user);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * function to delete game
     * */
    private void deleteGame(final String gameid) {
        // Tag used to cancel the request
        String tag_string_req = "req_delGame";

        pDialog.setMessage("Deleting Game ...");
        showDialog();
        Log.d(TAG, gameid);
        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_CREATENEWGAME, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Delete Game Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        db.deleteGame();
                        Log.d(TAG,"Game is deleted");
                        timerHandler.postDelayed(timerRefreshGamesRunnable, 0);
                        removeWaitView();
                    } else {
                        // Error in game deletion
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
                params.put("uiid", gameid);
                params.put("status", "ENDED");
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * read all games
     * */
    private void getGames(final String status) {
        // Tag used to cancel the request
        String tag_string_req = "req_getGames";
        String uri = AppConfig.URL_CREATENEWGAME + "/games/"+status;

        Log.d(TAG, uri);
        StringRequest strReq = new StringRequest(Request.Method.GET,
                uri, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Get Games Response: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    games = new ArrayList<>();
                    // Check for error node in json
                    if (!error) {
                        // Now store the game in SQLite
                        JSONArray obj = jObj.getJSONArray("data");

                        if (!(obj.length() == 0)) {
                            Gson gson = new GsonBuilder().create();
                            games = gson.fromJson(obj.toString(), new TypeToken<ArrayList<Game>>() {}.getType());
                        }
                        } else {
                            // Error in login. Get the error message
                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getApplicationContext(),
                                    errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch(JSONException e){
                        // JSON error
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    if (!(games == null)) {
                        if (!(games.equals(oldgames))) {
                            createGamesView(games);
                            oldgames = games;
                        }

                    } else {
                        Log.d(TAG, "games is null");
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
                params.put("status", status);
                params.put("uiid", "");
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void startGame(final String gameID) {
        timerHandler.removeCallbacks(timerRefreshGamesRunnable);
        timerHandler.removeCallbacks(timerWaitForPlayerRunnable);
        startGameReq(gameID);
    }

    /**
     * function to start game
     * */
    private void startGameReq(final String gameid) {
        // Tag used to cancel the request
        String tag_string_req = "req_startGame";

        pDialog.setMessage("Starting Game ...");
        showDialog();
        Log.d(TAG, gameid);
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
                        db.setGameStatus(GameStatus.PREPARE.asString(), gameid);
                        removeWaitView();
                        Intent i = new Intent(getApplicationContext(),
                                PrepareActivity.class);
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
                params.put("uiid", gameid);
                params.put("status", "PREPARED");
                params.put("userid", user.getId());
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    /**
     * Wait for second player
     * */
    private void setField(final String gameid) {
        // Tag used to cancel the request
        String tag_string_req = "req_settingGameField";

        StringRequest strReq = new StringRequest(Request.Method.PUT,
                AppConfig.URL_FIELD, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Set Field: " + response.toString());
                isReady = false;
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        String field = jObj.getString("text");
                        Log.d(TAG, "field" + field);
                        db.setGameField(gameid, field);
                    } else {
                        // Error in response
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
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

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("gameid", gameid);
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
        Log.d(TAG, "+++++++++++++"+gameid);
        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_CREATENEWGAME + "/game/"+gameid, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Wait for players: " + response.toString());

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");
                    games = new ArrayList<>();
                    // Check for error node in json
                    if (!error) {
                        JSONObject gameObj = jObj.getJSONObject("data");

                        String status = gameObj.getString("status");
                        if (status.equals(GameStatus.PREPARE.asString()))
                        {
                            timerHandler.removeCallbacks(timerRefreshGamesRunnable);
                            timerHandler.removeCallbacks(timerWaitForPlayerRunnable);
                            db.setGameStatus(GameStatus.PREPARE.asString(), gameid);
                            db.setGameField(gameid, gameObj.getString("field"));
                            Game game = db.getGameDetails();

                            Intent i = new Intent(getApplicationContext(),
                                    PrepareActivity.class);
                            startActivity(i);
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
                params.put("status", "");
                params.put("uiid", gameid);
                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }

    private void removeAllViews()
    {
        if (((LinearLayout) myLinearLayout).getChildCount() > 0) {
            ((LinearLayout) myLinearLayout).removeAllViews();
        }
        if (((LinearLayout) waitviewLayout).getChildCount() > 0) {
            ((LinearLayout) waitviewLayout).removeAllViews();
        }


    }

    private void createWaitView(final String gameID) {
        removeAllViews();

        btnAddNewGame.setEnabled(false);
        btnAddNewGame.setAlpha(.5f);
        btnAddNewGame.setClickable(false);

        this.gameid = gameID;

        final LinearLayout newLinearLayout = new LinearLayout(context);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        buttonLayoutParams.setMargins(10, 10, 0, 0);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(5, 5, 5, 5);
        newLinearLayout.setOrientation(LinearLayout.VERTICAL);
        newLinearLayout.setLayoutParams(params);

        final TextView rowTextView = new TextView(context);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(5, 5, 5, 5);
        params.leftMargin = 15;
        rowTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
        rowTextView.setText("Awaiting for second player...");
        rowTextView.setLayoutParams(params);
        newLinearLayout.addView(rowTextView);

        final Button newButton = new Button(context);
        params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.setMargins(5, 5, 5, 5);
        params.leftMargin = 15;
        params.rightMargin = 15;
        params.topMargin = 30;
        params.bottomMargin = 30;
        newButton.setLayoutParams(params);
        newButton.setText("Remove game");
        newButton.setBackgroundResource(R.color.btn_join_bg);
        newButton.setTextColor(getResources().getColor(R.color.white));
        // Add new Game
        newButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                deleteGame(gameID);
            }
        });
        newLinearLayout.addView(newButton);

        waitviewLayout.addView(newLinearLayout);

        timerHandler.postDelayed(timerWaitForPlayerRunnable, 0);
        timerHandler.removeCallbacks(timerRefreshGamesRunnable);
    }

    private void createGamesView(final List<Game> games) {
        int id = 100;
        removeAllViews();

        for (final Game game : games) {
            final LinearLayout newLinearLayout = new LinearLayout(context);
            LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            buttonLayoutParams.setMargins(10, 10, 0, 0);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
            params.gravity = Gravity.TOP;
            params.setMargins(5, 5, 5, 5);
            newLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
            newLinearLayout.setBackgroundResource(R.drawable.border);
            newLinearLayout.setLayoutParams(params);

            final TextView rowTextView = new TextView(context);
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            params.setMargins(5, 5, 5, 5);
            params.leftMargin = 15;
            rowTextView.setText(game.getGamer1_name() + " (fans: " + game.getGamer1_points() + ")");
            rowTextView.setLayoutParams(params);
            newLinearLayout.addView(rowTextView);

            final Button newButton = new Button(context);
            params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            params.gravity = Gravity.CENTER;
            params.setMargins(5, 5, 5, 5);
            params.leftMargin = 15;
            params.rightMargin = 15;
            params.topMargin = 30;
            params.bottomMargin = 30;
            newButton.setLayoutParams(params);
            newButton.setText("Join");
            newButton.setBackgroundResource(R.color.btn_join_bg);
            newButton.setTextColor(getResources().getColor(R.color.white));
            // Add new Game
            newButton.setOnClickListener(new View.OnClickListener() {

                public void onClick(View view) {
                    startGame(game.getId());
                }
            });
            newLinearLayout.addView(newButton);

            myLinearLayout.addView(newLinearLayout);
            id++;
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

    public void getGame(final String uuid) {
        isReady = false;
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
                            db.addGame(game);
                            gameid = game.getId();
                            Log.d(TAG, "game is not empty empty");
                            switch (game.getStatus()) {
                                case "WAITING":
                                    createWaitView(game.getId());
                                    timerHandler.postDelayed(timerWaitForPlayerRunnable, 0);
                                    break;
                                case "PREPARED":
                                    Intent intent = new Intent(MainActivity.this, PrepareActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                                case "STARTED":
                                    intent = new Intent(MainActivity.this, PrepareActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                            }
                        }
                        else
                            {
                                Log.d(TAG, "game is empty");
                                timerHandler.postDelayed(timerRefreshGamesRunnable, 0);
                            }
                        }
                    }
                catch (JSONException e) {
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



}
