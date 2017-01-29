package org.ilw.valhalla.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
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
import org.ilw.valhalla.dto.Game;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.User;
import org.ilw.valhalla.helper.SQLiteHandler;
import org.ilw.valhalla.helper.SessionManager;
import org.ilw.valhalla.views.GameView;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Ilja.Winokurow on 05.01.2017.
 */
public class PrepareActivity extends Activity {
    private SQLiteHandler db;
    private SessionManager session;
    private ProgressDialog pDialog;
    private static final String TAG = PrepareActivity.class.getSimpleName();

    protected Game game;
    protected User user;
    protected String fields;
    protected List<Gladiator> gladiators;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "BLYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA");
        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);
        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        user = db.getUserDetails();
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
                    Log.d(TAG, "here-2");
                    if (!error) {
                        if (!(jObj.isNull("data")))
                        {
                            JSONObject userReq = jObj.getJSONObject("data");
                            Gson gson = new GsonBuilder().create();
                            returnValue = gson.fromJson(userReq.toString(), Game.class);
                            game = returnValue;
                            Log.d(TAG, game.getId());
                            db.deleteGame();
                            Log.d(TAG, "here-1");
                            db.addGame(game);
                            Log.d(TAG, "here1");
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
                    Log.d(TAG, "here");
                    JSONObject jObj = new JSONObject(response);
                    Log.d(TAG, "there");
                    boolean error = jObj.getBoolean("error");
                    // Check for error node in json
                    if (!error) {
                        JSONArray obj = jObj.getJSONArray("data");
                        Log.d(TAG, new Integer (obj.length()).toString());
                        Gson gson = new GsonBuilder().create();
                        returnValue = gson.fromJson(obj.toString(), new TypeToken<ArrayList<Gladiator>>() {}.getType());
                        db.deleteGladiators();
                        db.addGladiator(returnValue);
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
                        setContentView(R.layout.activity_game);
                        GameView view = (GameView) findViewById(R.id.game_view);


                        String cells = db.getFieldCells().get("cells");
                        Log.d(TAG, "Cells" + cells);
                        view.setCells(cells);
                        hideDialog();
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
}
