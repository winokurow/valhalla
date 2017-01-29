package org.ilw.valhalla.org.ilw.valhalla.req;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import static android.content.ContentValues.TAG;

/**
 * Created by Ilja.Winokurow on 23.01.2017.
 */

public class CustomErrorListener implements Response.ErrorListener {

    Context context;

    public CustomErrorListener(Context context) {
        this.context = context;
    }


    @Override
    public void onErrorResponse(VolleyError error) {
        Log.e(TAG, "Login Error: " + error.getMessage());
        Toast.makeText(context,
                "Exception " + error.getMessage(), Toast.LENGTH_LONG)
                .show();
    }
}
