package io.flyingmongoose.brave;

import android.app.Activity;
import android.content.Intent;
import android.nfc.Tag;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.facebook.login.LoginManager;
import com.parse.FindCallback;
import com.parse.FunctionCallback;
import com.parse.ParseCloud;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by wprenison on 2017/10/17.
 * A class with all static parse API calls for the app in one place
 */

public class ParseAPIUtils
{
    //DB field names
    //Instalations table
    public static String allowNotif = "allowNotifications";

    //User Table
    public static String name = "name";
    public static String cellNumber = "cellNumber";
    public static String email = "email";



    private final static String TAG = "ParseApiUtils";


    /***
     * signs a user out and returns them to the login screen, clearing the channels their installation is subbed to and setting
     * @param activity
     */
    public static void signOutUser(Activity activity)
    {
        //unsub installation from push channels
        List<String> subbedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
        if(subbedChannels != null)  //User might not have joined a group yet
        {
            ParseInstallation.getCurrentInstallation().removeAll("channels", subbedChannels);
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }

        //check if it's a facebook login then logout of facebook as well
        if(ParseUser.getCurrentUser().getString("facebookId") != null)
            LoginManager.getInstance().logOut();

        ParseUser.logOut();

        ParsePush.subscribeInBackground("not_logged_in");
        Intent intentLogout = new Intent(activity, OnBoardingActivity.class);
        intentLogout.putExtra("initParse", false);
        activity.startActivity(intentLogout);
        activity.finish();
    }

    /**
     * Get group objects thhe user is subbed to
     * @param snackbarView view used to push feedback snackbar messages
     * @param groupNames a list oof strings representing each group's name
     * @param callback used to indicate loading status and return the list of group objects
     */
    public static void getGroupObjects(final View snackbarView, List<String> groupNames, final ParseApiInterface callback)
    {
        callback.onLoadingStatusChanged(true);

        //Query for group objects, ps only ones the user is subbed to
        ParseQuery<ParseObject> querySubbedGroups = new ParseQuery<ParseObject>("Groups");
        querySubbedGroups.whereContainedIn("name", groupNames);
        querySubbedGroups.findInBackground(new FindCallback<ParseObject>()
        {
            @Override
            public void done(List<ParseObject> list, ParseException e)
            {
                callback.onLoadingStatusChanged(false);
                if(e == null)
                {
                    callback.onParseObjectListResponse(list);
                }
                else
                {
                    if(e.getCode() == 100)
                        Snackbar.make(snackbarView, R.string.error_100_no_internet, Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(snackbarView, "Unsuccessful retrieval of group objects: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();

                }
            }
        });
    }

    public static void cloudCodeNewAlertHook(ParseObject panic, List<ParseObject> lstGroups, ParseUser user)
    {
        Log.i(TAG, "cloudCodeNewAlertHook: calling");
        HashMap<String, String> hashMap = new HashMap<>();

        try
        {
            //Create panic pointer
            JSONObject pointerPanic = new JSONObject();
            pointerPanic.put("__type", "Pointer");
            pointerPanic.put("className", panic.getClassName());
            pointerPanic.put("objectId", panic.getObjectId());
            hashMap.put("panic", pointerPanic.toString());

            //Create list of group pointers
            JSONArray pointerListGroups = new JSONArray();

            //Create pointer for each grouop and add it too the list
            for(int i = 0; i < lstGroups.size(); i++)
            {
                JSONObject pointerCurrGroup = new JSONObject();
                pointerCurrGroup.put("__type", "Pointer");
                pointerCurrGroup.put("className", lstGroups.get(i).getClassName());
                pointerCurrGroup.put("objectId", lstGroups.get(i).getObjectId());
                pointerListGroups.put(pointerCurrGroup);
            }

            hashMap.put("groups", pointerListGroups.toString());

            //Create user pointer
            JSONObject pointerUser = new JSONObject();
            pointerUser.put("__type", "Pointer");
            pointerUser.put("className", user.getClassName());
            pointerUser.put("objectId", user.getObjectId());
            hashMap.put("user", pointerUser.toString());

            ParseCloud.callFunctionInBackground(BraveApplication.API_NEW_ALERT_HOOK, hashMap, new FunctionCallback<Object>()
            {

                @Override
                public void done(Object object, ParseException e)
                {
                    Log.i(TAG, "cloudCodeNewAlertHook: completed");

                    if (e != null)
                        Log.e(TAG, "cloudCodeNewAlertHook: EXCEPTION - " + e.getCode() + ": " + e.getMessage());
                }
            });
        }
        catch(JSONException je)
        {
            je.printStackTrace();
        }
    }

    public static void cloudCodeGetActiveAlertsHttp(List<ParseObject> lstGroups, final ParseApiInterface callback)
    {
        try
        {
            callback.onLoadingStatusChanged(true);
            //Create list of group pointers
            JSONArray pointerListGroups = createGroupPointerList(lstGroups);

            JSONObject payload = new JSONObject();
            payload.put("groups", pointerListGroups);

            final OkHttpClient client = new OkHttpClient();
            Headers reqHeaders = new Headers.Builder()
                    .add("cache-control", "no-cache")
                    .add("content-type", "application/json")
                    .add("x-parse-application-id", BraveApplication.PARSE_APP_ID)
                    .add("x-parse-rest-api-key", BraveApplication.PARSE_API_KEY)
                    .build();

            RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), payload.toString());

            Request request = new Request.Builder().url(BraveApplication.API_BASE_URL + BraveApplication.API_GET_ACTIVE_ALERTS)
                    .headers(reqHeaders)
                    .post(formBody).build();

            client.newCall(request).enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    Log.e(TAG, "Failed to get active alerts: " + e.getMessage());
                    e.printStackTrace();
                    callback.onLoadingStatusChanged(false);
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException
                {
                    callback.onParseObjectListResponse(jsonAlertGroupListToObjectList(response.body().string()));
                    callback.onLoadingStatusChanged(false);
                }
            });

        }
        catch(JSONException je)
        {
            je.printStackTrace();
            Log.e(TAG, "JSONException occurred when creating payload for: cloudCodeGetActiveAlertsHttp");
        }

    }

    public static void cloudCodeNewAlertHookHttp(ParseObject panic, List<ParseObject> lstGroups, ParseUser user)
    {

        try
        {
            //Create panic pointer
            JSONObject pointerPanic = createPanicPointer(panic);

            //Create list of group pointers
            JSONArray pointerListGroups = createGroupPointerList(lstGroups);


            //Create user pointer
            JSONObject pointerUser = createUserPointer(user);

            final OkHttpClient client = new OkHttpClient();
            Headers reqHeaders = new Headers.Builder()
                    .add("cache-control", "no-cache")
                    .add("content-type", "application/json")
                    .add("x-parse-application-id", BraveApplication.PARSE_APP_ID)
                    .add("x-parse-rest-api-key", BraveApplication.PARSE_API_KEY)
                    .build();

            JSONObject payload = new JSONObject();
            payload.put("panic", pointerPanic);
            payload.put("groups", pointerListGroups);
            payload.put("user", pointerUser);

            Log.d(TAG, "payload - " + payload.toString());

            RequestBody formBody = RequestBody.create(MediaType.parse("application/json"), payload.toString());

            Log.d(TAG, "Body: " + formBody.toString());

            Request request = new Request.Builder().url(BraveApplication.API_BASE_URL + BraveApplication.API_NEW_ALERT_HOOK)
                    .headers(reqHeaders)
                    .post(formBody).build();

            Log.d(TAG, "Making post call new alert hook");
            client.newCall(request).enqueue(new Callback()
            {
                @Override
                public void onFailure(Call call, IOException e)
                {
                    Log.e(TAG, "Failed to create new PanicGroup: " + e.getMessage());
                    e.printStackTrace();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException
                {
                    Log.i(TAG, "Created PanicGroup: " + response.message());
                }
            });
        }
        catch(JSONException je)
        {
            je.printStackTrace();
            Log.e(TAG, "JSONException occurrred when creating the payload for: cloudCodeNewAlertHookHttp");
        }
    }

    private static JSONObject createPanicPointer(ParseObject panic)
    {
        try
        {
            JSONObject pointerPanic = new JSONObject();
            pointerPanic.put("__type", "Pointer");
            pointerPanic.put("className", panic.getClassName());
            pointerPanic.put("objectId", panic.getObjectId());

            return pointerPanic;
        }
        catch(JSONException je)
        {
            je.printStackTrace();
            Log.e(TAG, "JSONException occurred when creating a pointer of a panic object");
            return null;
        }
    }

    //Creates a JSON list of group object pointers
    private static JSONArray createGroupPointerList(List<ParseObject> lstGroups)
    {
        try
        {
            //Create list of group pointers
            JSONArray pointerListGroups = new JSONArray();

            //Create pointer for each grouop and add it too the list
            for (int i = 0; i < lstGroups.size(); i++)
            {
                JSONObject pointerCurrGroup = new JSONObject();
                pointerCurrGroup.put("__type", "Pointer");
                pointerCurrGroup.put("className", lstGroups.get(i).getClassName());
                pointerCurrGroup.put("objectId", lstGroups.get(i).getObjectId());
                pointerListGroups.put(pointerCurrGroup);
            }

            return pointerListGroups;
        }
        catch (JSONException je)
        {
            je.printStackTrace();
            Log.e(TAG, "JSONException occurred when creating a pointer list of group objects");
            return null;
        }

    }

    private static JSONObject createUserPointer(ParseUser user)
    {
        try
        {
            //Create user pointer
            JSONObject pointerUser = new JSONObject();
            pointerUser.put("__type", "Pointer");
            pointerUser.put("className", user.getClassName());
            pointerUser.put("objectId", user.getObjectId());

            return pointerUser;
        }
        catch(JSONException je)
        {
            je.printStackTrace();
            Log.e(TAG, "JSONException occurred when creating a pointer of a user object");
            return null;
        }
    }

    private static List<ParseObject> jsonAlertGroupListToObjectList(String jsonListString)
    {
        try
        {
            JSONObject jsonWrapper = new JSONObject(jsonListString);
            JSONArray lstJsonAlerts = jsonWrapper.getJSONArray("result");
            List<ParseObject> lstAlerts = new ArrayList<ParseObject>();

            for(int i = 0; i < lstJsonAlerts.length(); i++)
            {
                //Coonvert json to parse object
                JSONObject currJsonAlert = lstJsonAlerts.getJSONObject(i).getJSONObject("panic");
                ParseObject objALert = jsonAlertToParseObject(currJsonAlert);

                //Add object to list to return
                lstAlerts.add(objALert);
            }

            return lstAlerts;
        }
        catch(JSONException je)
        {
            je.printStackTrace();
            Log.e(TAG, "JSONException occurred when converting string json array to json array object: pointerAlertListToObjectList");
            return null;
        }
    }

    private static ParseObject jsonAlertToParseObject(JSONObject jsonAlert)
    {
        try
        {
            ParseObject objAlert = new ParseObject("Panics");

            //Set location
            JSONObject jsonLoc = jsonAlert.getJSONObject("location");
            objAlert.put("location", new ParseGeoPoint(jsonLoc.getDouble("latitude"), jsonLoc.getDouble("longitude")));

            //TODO: Set responders list

            //Set active
            objAlert.put("active", jsonAlert.getBoolean("active"));

            //Get user object
            ParseUser objUser = new ParseUser();
            objUser.setObjectId(jsonAlert.getJSONObject("user").getString("objectId"));
            objAlert.put("user", objUser);

            objAlert.setObjectId(jsonAlert.getString("objectId"));

            return objAlert;
        }
        catch(JSONException je)
        {
            je.printStackTrace();
            Log.e(TAG, "JSONException when converting json alert pointer to parse object");
            return null;
        }
    }
}