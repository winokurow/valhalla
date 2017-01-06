package org.ilw.valhalla.activity;

import android.app.Activity;
import android.os.Bundle;

import org.ilw.valhalla.R;
import org.ilw.valhalla.helper.SQLiteHandler;
import org.ilw.valhalla.helper.SessionManager;

/**
 * Created by Ilja.Winokurow on 05.01.2017.
 */

public class GameActivity extends Activity {
    private SQLiteHandler db;
    private SessionManager session;
    private static final String TAG = GameActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        String cells = db.getFieldCells().get("cells");

    }
}
