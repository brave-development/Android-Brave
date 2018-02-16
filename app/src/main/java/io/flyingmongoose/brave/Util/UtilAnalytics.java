package io.flyingmongoose.brave.util;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import io.flyingmongoose.brave.BraveApplication;

/**
 * Created by Acinite on 2018/02/13.
 */

public class UtilAnalytics
{
    private static final String TAG = "UtilAnalytics";

    public static void logEventScreenView(Activity activity, String screenName, String className)
    {
        if(BraveApplication.analyticsEnabled)
        {
            BraveApplication.fbAnalytics.setCurrentScreen(activity, screenName, className);
            Log.i(TAG, "screen_viewed analytic event logged, screen name: " + screenName);
        }
        else
            Log.i(TAG, "screen_viewed analytic event NOT logged");
    }

    public static void logEventRegDropOff(int screenId, String screenName)
    {
        if(BraveApplication.analyticsEnabled)
        {
            Bundle params = new Bundle();
            params.putInt("item_id", screenId);
            params.putString("reg_screen", screenName);

            BraveApplication.fbAnalytics.logEvent("reg_drop_off", params);
            Log.i(TAG, "reg_drop_off analytic event logged");
        }
        else
            Log.i(TAG, "reg_drop_off analytic event NOT logged");
    }
}
