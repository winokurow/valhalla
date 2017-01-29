package org.ilw.valhalla.activity;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import org.ilw.valhalla.R;
import org.ilw.valhalla.helper.SQLiteHandler;
import org.ilw.valhalla.helper.SessionManager;
import org.ilw.valhalla.views.GameView;

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
        GameView view = (GameView) findViewById(R.id.game_view);

        // SqLite database handler
        db = new SQLiteHandler(getApplicationContext());
        String cells = db.getFieldCells().get("cells");
        Log.d(TAG, "Cells" + cells);
        view.setCells(cells);
    }
}
