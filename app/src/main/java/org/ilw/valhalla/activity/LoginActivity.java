package org.ilw.valhalla.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request.Method;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.ilw.valhalla.R;
import org.ilw.valhalla.app.AppConfig;
import org.ilw.valhalla.app.AppController;
import org.ilw.valhalla.dto.Game;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.Turn;
import org.ilw.valhalla.dto.User;
import org.ilw.valhalla.helper.SQLiteHandler;
import org.ilw.valhalla.helper.SessionManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends Activity {
    private static final String TAG = LoginActivity.class.getSimpleName();
    private Button btnLogin;
    private Button btnLinkToRegister;
    private EditText inputEmail;
    private EditText inputPassword;
    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    private User user;
    private Game game;
    private List<Gladiator> gladiators;
    private List<Turn> turns;
    private String cells;
    private boolean isReady;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        inputEmail = (EditText) findViewById(R.id.email);
        inputPassword = (EditText) findViewById(R.id.password);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLinkToRegister = (Button) findViewById(R.id.btnLinkToRegisterScreen);

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String email = inputEmail.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                // Check for empty data in the form
                if (!email.isEmpty() && !password.isEmpty()) {
                    // login user
                    checkLogin(email, password);
                } else {
                    // Prompt user to enter credentials
                    Toast.makeText(getApplicationContext(),
                            "Please enter the credentials!", Toast.LENGTH_LONG)
                            .show();
                }
            }

        });

        // Link to Register Screen
        btnLinkToRegister.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(),
                        RegisterActivity.class);
                startActivity(i);
                finish();
            }
        });

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        user = db.getUserDetails();
        if (user == null)
        {
            session.setLogin(false);
        }
        if (session.isLoggedIn()) {
            pDialog.setMessage("Starting ...");
            pDialog.show();
            // get current data
            user = db.getUserDetails();


            if (this.user == null)
            {
                session.setLogin(false);
            } else {
                getUser(user.getId());
            }
        }

    }

    /**
     * function to verify login details in mysql db
     */
    private void checkLogin(final String email, final String password) {
        // Tag used to cancel the request
        String tag_string_req = "req_login";

        pDialog.setMessage("Logging in ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.POST,
                AppConfig.URL_USER, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + response.toString());
                hideDialog();

                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);

                        // Now store the user in SQLite
                        JSONObject userReq = jObj.getJSONObject("data");
                        Gson gson = new GsonBuilder().create();
                        User user = gson.fromJson(userReq.toString(), User.class);

                        // Inserting row in users table
                        Log.d(TAG, "email" + email);
                        db.addUser(user);

                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
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
                isReady = true;
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("email", email);
                params.put("password", password);

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

    public void getUser(final String uuid) {
        isReady = false;
        // Tag used to cancel the request
        String tag_string_req = "get_User";
        String uri = AppConfig.URL_USER + "/uuid/" + uuid;

        StringRequest strReq = new StringRequest(Method.GET,
                uri, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                User returnValue = null;
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        // Now store the user in SQLite
                        JSONObject userReq = jObj.getJSONObject("data");
                        Gson gson = new GsonBuilder().create();
                        returnValue = gson.fromJson(userReq.toString(), User.class);
                        db.deleteUsers();

                        db.addUser(user);
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();

                        /*if ((game==null) || game.getStatus().equals("WAITING"))
                        {
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            db.addGame(game);
                            this.getGladiators(game.getGamer1_id());
                            while (!(isReady))
                            {
                                Utils.sleep(100);
                            }
                            db.addGladiator(gladiators);
                            this.getGladiators(game.getGamer2_id());
                            while (!(isReady))
                            {
                                Utils.sleep(100);
                            }
                            db.addGladiator(gladiators);
                            this.getFields(game.getField());
                            while (!(isReady))
                            {
                                Utils.sleep(100);
                            }
                            db.addCells(cells);

                            switch (game.getStatus()) {
                                case "PREPARED":
                                    Intent intent = new Intent(LoginActivity.this, PrepareActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                                case "STARTED":
                                    this.getTurns(game.getId(), "-1");
                                    while (!(isReady))
                                    {
                                        Utils.sleep(100);
                                    }
                                    db.addTurns(turns);
                                    intent = new Intent(LoginActivity.this, GameActivity.class);
                                    startActivity(intent);
                                    finish();
                                    break;
                            }*/
                    }
                    hideDialog();
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
                user = returnValue;

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
/*
    public void getGame(final String uuid) {
        isReady = false;
        // Tag used to cancel the request
        String tag_string_req = "get_Game";
        String uri = AppConfig.URL_CREATENEWGAME + "/userid/" + uuid;

        StringRequest strReq = new StringRequest(Method.GET,
                uri, new Response.Listener<String>() {

            @Override
            public void onResponse(String response) {
                Game returnValue = null;
                try {
                    JSONObject jObj = new JSONObject(response);
                    boolean error = jObj.getBoolean("error");

                    // Check for error node in json
                    if (!error) {
                        JSONObject userReq = jObj.getJSONObject("data");
                        Gson gson = new GsonBuilder().create();
                        returnValue = gson.fromJson(userReq.toString(), Game.class);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.getMessage());
                }
                game = returnValue;
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


}
