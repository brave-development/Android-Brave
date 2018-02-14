package io.flyingmongoose.brave.Util;

import android.os.Bundle;
import android.util.Log;

import io.flyingmongoose.brave.BraveApplication;

/**
 * Created by Acinite on 2018/02/13.
 */

public class UtilAnalytics
{
    private static final String TAG = "UtilAnalytics";

    public static void logEventScreenViewed(String screenName)
    {
        if(BraveApplication.analyticsEnabled)
        {
            Bundle params = new Bundle();
            params.putString("screen_name", screenName);

            BraveApplication.fbAnalytics.logEvent("screen_viewed", params);
            Log.i(TAG, "screen_viewed analytic event logged, screen name: " + screenName);
        }
        else
            Log.i(TAG, "screen_viewed analytic event NOT logged");
    }
}
