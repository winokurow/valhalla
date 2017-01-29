package org.ilw.valhalla.common;

import android.util.Log;

/**
 * Created by Ilja.Winokurow on 23.01.2017.
 */

public class Utils {

    public static void sleep(int time)
    {
        try
        {
            Thread.sleep(time);
        } catch (Exception e)
        {
            Log.d("Utils", e.getMessage());
        }
    }
}
