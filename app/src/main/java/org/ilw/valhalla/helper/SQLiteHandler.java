package org.ilw.valhalla.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 12;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_USER = "user";

    // Game table name
    private static final String TABLE_GAME = "game";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_UID = "uid";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_POINTS = "points";
    // Game Table Columns names
    private static final String KEY_GAME_ID = "id";
    private static final String KEY_GAME_GAMER1ID = "gamer1id";
    private static final String KEY_GAME_GAMER1NAME = "gamer1name";
    private static final String KEY_GAME_GAMER1POINTS = "gamer1points";
    private static final String KEY_GAME_GAMER2ID = "gamer2id";
    private static final String KEY_GAME_GAMER2NAME = "gamer2name";
    private static final String KEY_GAME_GAMER2POINTS = "gamer2points";
    private static final String KEY_GAME_UID = "uid";
    private static final String KEY_GAME_CREATED_AT = "created_at";
    private static final String KEY_GAME_STATUS = "status";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_ID + " INTEGER PRIMARY KEY," + KEY_NAME + " TEXT,"
                + KEY_EMAIL + " TEXT UNIQUE," + KEY_UID + " TEXT,"
                + KEY_CREATED_AT + " TEXT, " + KEY_POINTS + " INTEGER" +")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_GAME_TABLE = "CREATE TABLE " + TABLE_GAME + "("
                + KEY_GAME_ID + " STRING PRIMARY KEY," + KEY_GAME_GAMER1ID + " TEXT,"+ KEY_GAME_GAMER1NAME + " TEXT,"+ KEY_GAME_GAMER1POINTS + " TEXT,"
                + KEY_GAME_GAMER2ID + " TEXT,"+ KEY_GAME_GAMER2NAME + " TEXT,"+ KEY_GAME_GAMER2POINTS  + " TEXT," + KEY_GAME_UID + " TEXT,"
                + KEY_GAME_CREATED_AT + " TEXT, " + KEY_GAME_STATUS + " INTEGER"+ ")";
        db.execSQL(CREATE_GAME_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME);
        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(String name, String email, String uid, String created_at, int points) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_EMAIL, email); // Email
        values.put(KEY_UID, uid); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_POINTS, points); // Points
        // Inserting Row
        long id = db.insert(TABLE_USER, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New user inserted into sqlite: " + id);
    }

    /**
     * Storing user details in database
     * */
    public void addGame(String gameid, String gamer1id, String gamer1name, String gamer1points, String gamer2id, String gamer2name, String gamer2points, String uid, String created_at, String status) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        Log.d(TAG, gameid);

        values.put(KEY_GAME_ID, gameid);
        values.put(KEY_GAME_GAMER1ID, gamer1id);
        values.put(KEY_GAME_GAMER1NAME, gamer1name);
        values.put(KEY_GAME_GAMER1POINTS, gamer1points);
        values.put(KEY_GAME_GAMER2ID, gamer2id);
        values.put(KEY_GAME_GAMER2NAME, gamer2name);
        values.put(KEY_GAME_GAMER2POINTS, gamer2points);

        values.put(KEY_UID, uid); // Email
        values.put(KEY_CREATED_AT, created_at); // Created At
        values.put(KEY_GAME_STATUS, status); // Points

        // Inserting Row
        long id = db.insert(TABLE_GAME, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New game inserted into sqlite: " + id);
    }

    /**
     * Getting user data from database
     * */
    public HashMap<String, String> getUserDetails() {
        HashMap<String, String> user = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            user.put("name", cursor.getString(1));
            user.put("email", cursor.getString(2));
            user.put("uid", cursor.getString(3));
            user.put("created_at", cursor.getString(4));

            user.put("points", Integer.toString(cursor.getInt(5)));
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching user from Sqlite: " + user.toString());

        return user;
    }

    /**
     * Getting game data from database
     * */
    public HashMap<String, String> getGameDetails() {
        HashMap<String, String> game = new HashMap<String, String>();
        String selectQuery = "SELECT  * FROM " + TABLE_GAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            game.put("id", cursor.getString(0));
            game.put("gamer1id", cursor.getString(1));
            game.put("gamer1name", cursor.getString(2));
            game.put("gamer1points", cursor.getString(3));
            game.put("gamer2id", cursor.getString(4));
            game.put("gamer2name", cursor.getString(5));
            game.put("gamer2points", cursor.getString(6));
            game.put("created_at", cursor.getString(7));
            game.put("status", cursor.getString(8));
        }
        cursor.close();
        db.close();
        // return game
        Log.d(TAG, "Fetching game from Sqlite: " + game.toString());

        return game;
    }

    /**
     * Delete game from DB
     * */
    public void deleteGame() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_GAME, null, null);
        db.close();

        Log.d(TAG, "Delete game info from sqlite");
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

}
