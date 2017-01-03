package org.ilw.valhalla.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.ilw.valhalla.R;
import org.ilw.valhalla.app.AppConfig;
import org.ilw.valhalla.app.AppController;
import org.ilw.valhalla.dto.Game;
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
    private Context context;

    private ProgressDialog pDialog;
    private SQLiteHandler db;
    private SessionManager session;
    private static final String TAG = MainActivity.class.getSimpleName();
    List<Game> games;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        txtName = (TextView) findViewById(R.id.name);
        txtPoints = (TextView) findViewById(R.id.points);
        btnLogout = (Button) findViewById(R.id.btnLogout);
        btnAddNewGame = (Button) findViewById(R.id.btnAddGame);
        myLinearLayout = (LinearLayout) findViewById(R.id.gameslist);
        context = this;
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // session manager
        session = new SessionManager(getApplicationContext());

        if (!session.isLoggedIn()) {
            logoutUser();
        }

        // Fetching user details from sqlite
        final HashMap<String, String> user = db.getUserDetails();

        String name = user.get("name");
        String points = user.get("points");

        // Displaying the user details on the screen
        txtName.setText(name);
        txtPoints.setText("Fans: " + points);

        getGames("WAITING");

        // Add new Game
        btnAddNewGame.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Log.d(TAG, "UUID" + user.get("uid"));
                addNewGame(user.get("uid"));
            }
        });

        // Logout button click event
        btnLogout.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                logoutUser();
            }
        });
    }

    /**
     * Logging out the user. Will set isLoggedIn flag to false in shared
     * preferences Clears the user data from sqlite users table
     * */
    private void logoutUser() {
        session.setLogin(false);

        db.deleteUsers();

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
                        JSONObject game = jObj.getJSONObject("game");
                        String gamer1id = game.getString("gamer1_id");
                        String gamer1name = game.getString("gamer1_name");
                        String gamer1points = game.getString("gamer1_points");
                        String gamer2id = game.getString("gamer2_id");
                        String gamer2name = game.getString("gamer2_name");
                        String gamer2points = game.getString("gamer2_points");
                        String created_at = game.getString("created_at");
                        String uid = game.getString("id");
                        String status = game.getString("status");

                        // Inserting row in users table
                        db.addGame(gamer1id, gamer1name, gamer1points, gamer2id, gamer2name, gamer2points, uid, created_at, status);

                    } else {
                        // Error in login. Get the error message
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
     * read all games
     * */
    private void getGames(final String status) {
        // Tag used to cancel the request
        String tag_string_req = "req_getGames";

        StringRequest strReq = new StringRequest(Request.Method.GET,
                AppConfig.URL_CREATENEWGAME, new Response.Listener<String>() {

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
                        JSONArray obj = jObj.getJSONArray("game");

                        for (int i = 0; i < obj.length(); i++) {
                            JSONObject game = obj.getJSONObject(i);
                            String gamer1id = game.getString("gamer1_id");
                            String gamer1name = game.getString("gamer1_name");
                            String gamer1points = game.getString("gamer1_points");
                            String gamer2id = game.getString("gamer2_id");
                            String gamer2name = game.getString("gamer2_name");
                            String gamer2points = game.getString("gamer2_points");
                            String created_at = game
                                    .getString("created_at");
                            String uid = game
                                    .getString("id");
                            String status = game.getString("status");
                            games.add(new Game(uid, gamer1id, gamer2id, status, created_at));
                            Log.d(TAG, Integer.toString(games.size()));
                        }

                    } else {
                        // Error in login. Get the error message
                        String errorMsg = jObj.getString("error_msg");
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }
                } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }

                if (!(games==null)) {
                    int id = 100;
                    for (Game game : games) {
                        Log.d(TAG, "1");
                        final LinearLayout newLinearLayout = new LinearLayout(context);
                        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
                        buttonLayoutParams.setMargins(10, 10, 0, 0);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
                        params.gravity = Gravity.CENTER;
                        params.setMargins(5,5,5,5);
                        newLinearLayout.setLayoutParams(buttonLayoutParams);
                        newLinearLayout.setOrientation(LinearLayout.HORIZONTAL);
                        newLinearLayout.setBackgroundResource(R.drawable.border);

                        final TextView rowTextView = new TextView(context);
                        rowTextView.setText(game.getGamer1());
                        rowTextView.setLayoutParams(params);
                        newLinearLayout.addView(rowTextView);

                        final Button newButton = new Button(context);
                        newButton.setLayoutParams(params);
                        newButton.setText("Join");
                        newButton.setBackgroundResource(R.color.btn_join_bg);
                        newButton.setTextColor(getResources().getColor(R.color.white));
                        newLinearLayout.addView(newButton);

                        myLinearLayout.addView(newLinearLayout);
                        id++;
                    }
                } else
                {
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
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("status", status);

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
}
