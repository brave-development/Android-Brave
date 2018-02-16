package io.flyingmongoose.brave;

import android.support.multidex.MultiDexApplication;
import android.util.Log;

import com.google.firebase.analytics.FirebaseAnalytics;
import com.parse.Parse;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;
import com.twitter.sdk.android.core.Twitter;

import io.flyingmongoose.brave.util.UtilNetInterceptor;
import okhttp3.OkHttpClient;

/**
 * Created by IC on 5/28/2015.
 */
public class BraveApplication extends MultiDexApplication
{
    public static final String PARSE_APP_ID = "PANICING-TURTLE";
    public static final String PARSE_API_KEY = "PANICINGTURTTLE3847TR386TB281XN1NY7YNXM";

    //End points
    public static final String API_BASE_URL = "http://panicing-turtle.herokuapp.com/parse/functions/";
    public static final String API_SEND_PUSH_FROM_ID = "pushFromId";
    public static final String API_NEW_ALERT_HOOK = "newAlertHook";
    public static final String API_GET_ACTIVE_ALERTS = "getActiveAlerts";

    public static FirebaseAnalytics fbAnalytics;
    public static boolean analyticsEnabled = true;
    private final String TAG = "braveApplication";

    @Override
    public void onCreate()
    {
        super.onCreate();
        initParse();
        Twitter.initialize(this);


            fbAnalytics = FirebaseAnalytics.getInstance(getApplicationContext());

//        try
//        {
//            FirebaseInstanceId.getInstance().deleteInstanceId();
//        }
//        catch(IOException ioe)
//        {
//            ioe.printStackTrace();
//        }
    }

    //Initialise PARSE
    private void initParse()
    {
//        Parse.setLogLevel(Parse.LOG_LEVEL_VERBOSE);
//        Parse.initialize(this, "qR8lv9KafH4E9CakPJfKSoDLGlOSPbPMbXVVdbBC", "EYia5DIkfo2OgYOkLJ5NxeUW8lqEUx1nRt2GeyTv"); Panic Dev db
//        Parse.initialize(this, "cBZmGCzXfaQAyxqnTh6eF2kIqCUnSm1ET8wYL5O7", "rno7DabpDMU293yi2TF4S3jKOlrZX2P27EW70C3G"); //Panic Live db db

        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder()
                .addInterceptor(new UtilNetInterceptor());

        //TODO: Live server detail
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("PANICING-TURTLE")
                .clientKey("PANICINGTURTLE3847TR386TB281XN1NY7YNXM")
                .server("http://panicing-turtle.herokuapp.com/parse")
//                .clientBuilder(clientBuilder)
//                .enableLocalDataStore()
                .build());

//        Parse.initialize(new Parse.Configuration.Builder(this)
//                .applicationId("PANICING-TORTOISE")
//                .clientKey("PANICINGTORTOISE3847TR386TB281XN1NY7YNXM")
//                .server("https://panicing-tortoise.herokuapp.com/parse")
//                .build()
//        );

        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                    Log.i(TAG, "Installation successfully initialized ObjectID: " + ParseInstallation.getCurrentInstallation().getObjectId());
                else
                    Log.e(TAG, "ERROR: Installation could not be initialized: " + e.getMessage() + " Code: " + e.getCode());
            }
        });

        ParseFacebookUtils.initialize(this);

        //Set default ACL settings
        ParseACL defaultACL = new ParseACL();
        defaultACL.setPublicReadAccess(true);
        ParseACL.setDefaultACL(defaultACL, true);

        //Set allow notifications
        if(!ParseInstallation.getCurrentInstallation().has("allowNotifications"))
        {
            ParseInstallation.getCurrentInstallation().put("allowNotifications", true);
            ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if(e == null)
                    {
                        Log.i(TAG, "allowNotifications saved and set to true on installation object");
                    }
                    else
                        Log.i(TAG, "Couldn't save allowNotifications to installation object: " + e.getCode() + ": " + e.getMessage());
                }
            });
        }

        Log.i(TAG, "init Parse complete");
        //Setup push notifications
        /*ParsePush.subscribeInBackground("Ab", new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                {
                    Log.i("Push Notification", "Successfully subscribed to the broadcast channel.");
                }
                else
                {
                    Log.e("Push Notification", "Unsuccessful subscribe to broadcast channel.");
                }
            }
        });*/

        //ParseAnalytics.trackAppOpened(getIntent());

        //Test that parse data is working
//        ParseObject testObject = new ParseObject("TestObject");
//        testObject.put("foo", "bar");
//        testObject.saveInBackground();

        /*Test Analytics
        Map<String, String> dimensions = new HashMap<String, String>();
         // What type of news is this?
        dimensions.put("category", "politics");
        // Is it a weekday or the weekend?
        dimensions.put("dayType", "weekday");
        // Send the dimensions to Parse along with the 'read' event

        ParseAnalytics.trackEvent("read", dimensions);*/
    }
}
