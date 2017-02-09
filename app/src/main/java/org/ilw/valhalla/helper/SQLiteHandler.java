package org.ilw.valhalla.helper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.ilw.valhalla.dto.Game;
import org.ilw.valhalla.dto.Gladiator;
import org.ilw.valhalla.dto.Turn;
import org.ilw.valhalla.dto.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SQLiteHandler extends SQLiteOpenHelper {

    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 58;

    // Database Name
    private static final String DATABASE_NAME = "android_api";

    // Login table name
    private static final String TABLE_USER = "user";

    // Game table name
    private static final String TABLE_GAME = "game";

    // Field table name
    private static final String TABLE_FIELD = "field";

    // Gladiator table name
    private static final String TABLE_GLADIATORS = "gladiators";

    // Turns table name
    private static final String TABLE_TURNS = "turns";

    // User Table Columns names
    private static final String KEY_USER_UID = "uid";
    private static final String KEY_USER_NAME = "name";
    private static final String KEY_USER_EMAIL = "email";
    private static final String KEY_USER_POINTS = "points";
    private static final String KEY_USER_LEVEL = "level";
    private static final String KEY_CREATED_AT = "created_at";
    private static final String KEY_UPDATED_AT = "updated_at";

    // Game Table Columns names
    private static final String KEY_GAME_ID = "id";
    private static final String KEY_GAME_GAMER1ID = "gamer1id";
    private static final String KEY_GAME_GAMER1NAME = "gamer1name";
    private static final String KEY_GAME_GAMER1POINTS = "gamer1points";
    private static final String KEY_GAME_GAMER2ID = "gamer2id";
    private static final String KEY_GAME_GAMER2NAME = "gamer2name";
    private static final String KEY_GAME_GAMER2POINTS = "gamer2points";
    private static final String KEY_GAME_STATUS = "status";
    private static final String KEY_GAME_FIELD = "field";

    // Gladiators Table Columns names
    private static final String KEY_GLADIATOR_ID = "id";
    private static final String KEY_GLADIATOR_USER_ID = "userid";
    private static final String KEY_GLADIATOR_NAME = "name";
    private static final String KEY_GLADIATOR_STR = "str";
    private static final String KEY_GLADIATOR_DEX = "dex";
    private static final String KEY_GLADIATOR_SPD = "spd";
    private static final String KEY_GLADIATOR_CON = "con";
    private static final String KEY_GLADIATOR_INT = "int";
    private static final String KEY_GLADIATOR_STAMINA = "stamina";
    private static final String KEY_GLADIATOR_MART_ART = "mart_art";
    private static final String KEY_GLADIATOR_STR_PROGRESS = "str_progress";
    private static final String KEY_GLADIATOR_DEX_PROGRESS = "dex_progress";
    private static final String KEY_GLADIATOR_SPD_PROGRESS = "spd_progress";
    private static final String KEY_GLADIATOR_CON_PROGRESS = "con_progress";
    private static final String KEY_GLADIATOR_INT_PROGRESS = "int_progress";
    private static final String KEY_GLADIATOR_STAMINA_PROGRESS = "stamina_progress";
    private static final String KEY_GLADIATOR_MART_ART_PROGRESS = "mart_art_progress";

    // Turns Table Columns names
    private static final String KEY_TURNS_GAME_ID = "gameid";
    private static final String KEY_TURNS_TURN = "turn";
    private static final String KEY_TURNS_HOST = "host";
    private static final String KEY_TURNS_ACTION = "action";
    private static final String KEY_TURNS_VALUE1 = "value1";
    private static final String KEY_TURNS_VALUE2 = "value2";
    private static final String KEY_TURNS_VALUE3 = "value3";

    // Field Table Columns names
    private static final String KEY_FIELD_CELLS = "cells";

    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_LOGIN_TABLE = "CREATE TABLE " + TABLE_USER + "("
                + KEY_USER_UID + " INTEGER PRIMARY KEY," + KEY_USER_NAME + " TEXT,"
                + KEY_USER_EMAIL + " TEXT UNIQUE,"
                + KEY_USER_POINTS + " INTEGER," + KEY_USER_LEVEL + " INTEGER," + KEY_CREATED_AT + " TEXT, " + KEY_UPDATED_AT + " TEXT "  +")";
        db.execSQL(CREATE_LOGIN_TABLE);

        String CREATE_GAME_TABLE = "CREATE TABLE " + TABLE_GAME + "("
                + KEY_GAME_ID + " STRING PRIMARY KEY," + KEY_GAME_GAMER1ID + " TEXT,"+ KEY_GAME_GAMER1NAME + " TEXT,"+ KEY_GAME_GAMER1POINTS + " TEXT,"
                + KEY_GAME_GAMER2ID + " TEXT,"+ KEY_GAME_GAMER2NAME + " TEXT,"+ KEY_GAME_GAMER2POINTS  + " TEXT,"
                + KEY_GAME_STATUS + " TEXT, " + KEY_GAME_FIELD + " TEXT, " + KEY_CREATED_AT + " TEXT, " + KEY_UPDATED_AT + " TEXT"+ ")";
        db.execSQL(CREATE_GAME_TABLE);

        String CREATE_GLADIATORS_TABLE = "CREATE TABLE " + TABLE_GLADIATORS + "("
                + KEY_GLADIATOR_ID + " STRING PRIMARY KEY," + KEY_GLADIATOR_USER_ID + " TEXT,"+ KEY_GLADIATOR_NAME + " TEXT,"+ KEY_GLADIATOR_STR + " TEXT," + KEY_GLADIATOR_STR_PROGRESS + " TEXT, "
                + KEY_GLADIATOR_DEX + " TEXT,"+ KEY_GLADIATOR_DEX_PROGRESS + " TEXT, "+ KEY_GLADIATOR_SPD + " TEXT,"+ KEY_GLADIATOR_SPD_PROGRESS + " TEXT, "+ KEY_GLADIATOR_CON  + " TEXT,"
                + KEY_GLADIATOR_CON_PROGRESS + " TEXT, "+ KEY_GLADIATOR_INT + " TEXT,"+ KEY_GLADIATOR_INT_PROGRESS + " TEXT, "+ KEY_GLADIATOR_STAMINA + " TEXT,"+ KEY_GLADIATOR_STAMINA_PROGRESS + " TEXT, " + KEY_GLADIATOR_MART_ART + " TEXT, "+ KEY_GLADIATOR_MART_ART_PROGRESS + " TEXT, " + KEY_CREATED_AT + " TEXT, " + KEY_UPDATED_AT + " TEXT"+ ")";
        db.execSQL(CREATE_GLADIATORS_TABLE);

        String CREATE_TURNS_TABLE = "CREATE TABLE " + TABLE_TURNS + "("
                + KEY_TURNS_GAME_ID + " STRING PRIMARY KEY," + KEY_TURNS_TURN + " TEXT,"+ KEY_TURNS_HOST + " TEXT,"+ KEY_TURNS_ACTION + " TEXT,"
                + KEY_TURNS_VALUE1 + " TEXT,"+ KEY_TURNS_VALUE2 + " TEXT,"+ KEY_TURNS_VALUE3  + " TEXT" + KEY_CREATED_AT + " TEXT, " + KEY_GAME_FIELD + " TEXT"+ ")";
        db.execSQL(CREATE_TURNS_TABLE);

        String CREATE_FIELD_TABLE = "CREATE TABLE " + TABLE_FIELD + "("
                + KEY_FIELD_CELLS  + " TEXT" + ")";
        db.execSQL(CREATE_FIELD_TABLE);

        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GAME);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FIELD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_GLADIATORS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TURNS);
        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addUser(User user) {
        ContentValues values = new ContentValues();
        values.put(KEY_USER_NAME, user.getName()); // Name
        values.put(KEY_USER_EMAIL, user.getEmail()); // Email
        values.put(KEY_USER_UID, user.getId()); // id
        values.put(KEY_USER_POINTS, user.getPoints()); // Points
        values.put(KEY_USER_LEVEL, user.getLevel()); // Level
        values.put(KEY_CREATED_AT, user.getCreated_at()); // Created At
        values.put(KEY_UPDATED_AT, user.getUpdated_at()); // Updated At

        insertData(TABLE_USER, values);
    }

    /**
     * Storing user details in database
     * */
    public void addGame(Game game) {
        ContentValues values = new ContentValues();
        Log.d(TAG, game.getId());
        values.put(KEY_GAME_ID, game.getId());
        values.put(KEY_GAME_GAMER1ID, game.getGamer1_id());
        values.put(KEY_GAME_GAMER1NAME, game.getGamer1_name());
        values.put(KEY_GAME_GAMER1POINTS, game.getGamer1_points());
        values.put(KEY_GAME_GAMER2ID, game.getGamer2_id());
        values.put(KEY_GAME_GAMER2NAME, game.getGamer2_name());
        values.put(KEY_GAME_GAMER2POINTS, game.getGamer2_points());
        values.put(KEY_GAME_STATUS, game.getStatus()); // Status
        values.put(KEY_GAME_FIELD, game.getField()); // Points
        values.put(KEY_CREATED_AT, game.getCreated_at()); // Created At
        values.put(KEY_UPDATED_AT, game.getUpdated_at()); // Updated At

        insertData(TABLE_GAME, values);
    }

    /**
     * Storing gladiators list in database
     * */
    public void addGladiator(List<Gladiator> gladiators) {
        for (Gladiator gladiator:gladiators) {
            ContentValues values = new ContentValues();
            values.put(KEY_GLADIATOR_ID, gladiator.getId());
            values.put(KEY_GLADIATOR_USER_ID, gladiator.getUserid());
            values.put(KEY_GLADIATOR_NAME, gladiator.getName());
            values.put(KEY_GLADIATOR_STR, gladiator.getStr());
            values.put(KEY_GLADIATOR_DEX, gladiator.getDex());
            values.put(KEY_GLADIATOR_SPD, gladiator.getSpd());
            values.put(KEY_GLADIATOR_CON, gladiator.getCon());
            values.put(KEY_GLADIATOR_INT, gladiator.getIntel());
            values.put(KEY_GLADIATOR_STAMINA, gladiator.getStamina());
            values.put(KEY_GLADIATOR_MART_ART, gladiator.getMart_art());
            values.put(KEY_GLADIATOR_STR_PROGRESS, gladiator.getStr_progress());
            values.put(KEY_GLADIATOR_DEX_PROGRESS, gladiator.getDex_progress());
            values.put(KEY_GLADIATOR_SPD_PROGRESS, gladiator.getSpd_progress());
            values.put(KEY_GLADIATOR_CON_PROGRESS, gladiator.getCon_progress());
            values.put(KEY_GLADIATOR_INT_PROGRESS, gladiator.getCon_progress());
            values.put(KEY_GLADIATOR_STAMINA_PROGRESS, gladiator.getCon_progress());
            values.put(KEY_GLADIATOR_MART_ART_PROGRESS, gladiator.getMart_art_progress());
            values.put(KEY_CREATED_AT, gladiator.getCreated_at()); // Created At
            values.put(KEY_UPDATED_AT, gladiator.getUpdated_at()); // Updated At

            insertData(TABLE_GLADIATORS, values);
        }
    }

    /**
     * Storing turns list in database
     * */
    public void addTurns(List<Turn> turns) {
        for (Turn turn:turns) {
            ContentValues values = new ContentValues();
            values.put(KEY_TURNS_GAME_ID, turn.getGamedid());
            values.put(KEY_TURNS_TURN, turn.getTurn());
            values.put(KEY_TURNS_HOST, turn.getHost());
            values.put(KEY_TURNS_ACTION, turn.getAction());
            values.put(KEY_TURNS_VALUE1, turn.getValue1());
            values.put(KEY_TURNS_VALUE2, turn.getValue2());
            values.put(KEY_TURNS_VALUE3, turn.getValue3());
            values.put(KEY_CREATED_AT, turn.getCreated_at()); // Created At
            values.put(KEY_UPDATED_AT, turn.getUpdated_at()); // Updated At
            insertData(TABLE_TURNS, values);
        }
    }

    /**
     * Getting user data from database
     * */
    public User getUserDetails() {
        String selectQuery = "SELECT * FROM " + TABLE_USER;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        User user = null;
        if (cursor.getCount() > 0) {
            user = new User (cursor.getString(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getString(5), cursor.getString(6));
            Log.d(TAG, "Fetching user from Sqlite: " + cursor.getString(1));
        }
        cursor.close();
        db.close();
        // return user


        return user;
    }

    /**
     * Delete game from DB
     * */
    public void setUserPoints(String id, String points, String level) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("points",points);
        cv.put("level",level);
        db.update(TABLE_USER, cv, KEY_USER_UID+"='"+id + "'", null);
        db.close();

        Log.d(TAG, "Update game in sqlite");
    }


    /**
     * Getting game data from database
     * */
    public Game getGameDetails() {
        String selectQuery = "SELECT * FROM " + TABLE_GAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        Game game = null;
        if (cursor.getCount() > 0) {
            Log.d(TAG, "1"+cursor.getString(0));
            Log.d(TAG, "2"+cursor.getString(1));
            game = new Game(cursor.getString(0), cursor.getString(1),cursor.getString(2),cursor.getInt(3),cursor.getString(4),cursor.getString(5),cursor.getInt(6),cursor.getString(7),cursor.getString(8),cursor.getString(9),cursor.getString(10));
            // return game
            Log.d(TAG, "Fetching game from Sqlite: " + game.toString());
        }
        cursor.close();
        db.close();
        return game;
    }


    /**
     * Delete game from DB
     * */
    public void setGameStatus(String status, String gameID) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("status",status);
        db.update(TABLE_GAME, cv, KEY_GAME_ID+"='"+gameID + "'", null);
        db.close();

        Log.d(TAG, "Update game in sqlite");
    }

    /**
     * Getting gladiators from database
     * */
    public List<Gladiator> getGladiatorsDetails() {
        String selectQuery = "SELECT * FROM " + TABLE_GLADIATORS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<Gladiator> gladiators = new ArrayList<>();
        // Move to first row
        if (cursor.moveToFirst()) {
            do {
                Gladiator gladiator = new Gladiator (cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getInt(3), cursor.getInt(4), cursor.getInt(5), cursor.getInt(6), cursor.getInt(7), cursor.getInt(8), cursor.getInt(9), cursor.getInt(10), cursor.getInt(11), cursor.getInt(12), cursor.getInt(13), cursor.getInt(14), cursor.getInt(15), cursor.getInt(16), cursor.getString(17), cursor.getString(18));
                gladiators.add(gladiator);
                // get the data into array, or class variable
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return gladiators;
    }

    /**
     * Getting turns from database
     * */
    public List<Turn> getTurnsDetails() {
        String selectQuery = "SELECT * FROM " + TABLE_TURNS;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        List<Turn> turns = new ArrayList<>();
        // Move to first row
        if (cursor.moveToFirst()) {
            do {
                Turn turn = new Turn (cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3), cursor.getString(4), cursor.getString(5), cursor.getString(6), cursor.getString(7), cursor.getString(8));
                turns.add(turn);
                // get the data into array, or class variable
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return turns;
    }

    /**
     * Insert field id
     * */
    public void setGameField(String gameid, String fieldId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(KEY_GAME_FIELD,fieldId);
        db.update(TABLE_GAME, cv, KEY_GAME_ID+"='"+gameid + "'", null);
        db.close();

        Log.d(TAG, "Update game in sqlite");
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
    public void deleteGladiators() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_GLADIATORS, null, null);
        db.close();

        Log.d(TAG, "Deleted all gladiators info from sqlite");
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteTurns() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_GLADIATORS, null, null);
        db.close();

        Log.d(TAG, "Deleted all turns info from sqlite");
    }

    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_USER, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

    public void deleteCells() {
        SQLiteDatabase db = this.getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE_FIELD, null, null);
        db.close();

        Log.d(TAG, "Deleted all user info from sqlite");
    }

    /**
     * Storing field cells in database
     * */
    public void addCells(String cells) {
        ContentValues values = new ContentValues();
        values.put(KEY_FIELD_CELLS, cells);

        insertData(TABLE_FIELD, values);
    }

    /**
     * Getting field cells from database
     * */
    public HashMap<String, String> getFieldCells() {
        HashMap<String, String> cells = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM " + TABLE_FIELD;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        // Move to first row
        cursor.moveToFirst();
        if (cursor.getCount() > 0) {
            cells.put(KEY_FIELD_CELLS, cursor.getString(0));
        }
        cursor.close();
        db.close();
        // return game
        Log.d(TAG, "Fetching game from Sqlite: " + cells.toString());

        return cells;
    }

    /**
     * Storing in database
     * */
    private void insertData(String table, ContentValues values) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Inserting Row
        long id = db.insert(table, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New data ("+table+")inserted into sqlite: " + id);
    }
}
