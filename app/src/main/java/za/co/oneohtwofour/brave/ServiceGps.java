package za.co.oneohtwofour.brave;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.parse.GetCallback;
import com.parse.ParseACL;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.parse.SendCallback;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IC on 6/3/2015.
 */
public class ServiceGps extends Service implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener
{
    private final IBinder GpsBinder = new GpsLocalBinder();
    private LocationManager locMang;
    private LocationListener panicLocListner;
    private LocationListener slowTrackListner;
    private UserLocationListener userLocListner;
    private ParseObject panicUpdate;
    private boolean panicCanBeUpdated = true;
    private boolean wasGpsEnabledBefore = true;
    private boolean pushNotificationsSent = false;
    private String details = "";
    public Location prevLocation;

    //Google Play and last know location vars
    GoogleApiClient googApiClient;
    Location lastKnownLocation;

    private final String TAG = "ServiceGps";

    @Override
    public void onConnected(Bundle bundle)
    {
        Log.i(TAG, "Play services connected, checking for last known location");
        lastKnownLocation = LocationServices.FusedLocationApi.getLastLocation(googApiClient);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
        Log.i(TAG, "Play services connection suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult)
    {
        Log.i(TAG, "Play services failed to connect: " + connectionResult.getErrorCode());
    }

    //Creates binder for activity to access service methods
    public class GpsLocalBinder extends Binder
    {
        ServiceGps getService()
        {
            return ServiceGps.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return GpsBinder;
    }

    @Override
    public void onCreate()
    {
        //Connnect to play service to retrieve last known location
        buildGoogleApiClient();
        googApiClient.connect();
        //Get location manager
        locMang = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        //Setup location listener
         panicLocListner = new LocationListener()
        {
            @Override
            public void onLocationChanged(final Location location)
            {
                Log.i(TAG, "can update: " + panicCanBeUpdated);
                //Update online db if an update is not running
                if(panicCanBeUpdated)
                {
                    panicCanBeUpdated = false;

                    if(prevLocation != null)    //Update
                    {
                        //Send push notifications if they havent been sent before
                        if (!pushNotificationsSent)
                        {
                            sendPanicPushNotification(location);
                        }

                        //Add location
                        ParseGeoPoint currentLoc = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
                        panicUpdate.put("location", currentLoc);
                        panicUpdate.put("active", true);

                        panicUpdate.saveInBackground(new SaveCallback()
                        {
                            @Override
                            public void done(ParseException e)
                            {
                                panicCanBeUpdated = true;
                                if (e == null)
                                {
                                    Log.i(TAG, "Saving panic update updated Lat: " + location.getLatitude() + " , " + location.getLongitude());

                                } else
                                {
                                    Log.e(TAG, "Error: Unsuccessful panic update: " + e.getMessage() + " Code: " + e.getCode());
                                }
                            }
                        });

                        //Check for no of responders
                        panicUpdate.fetchInBackground(new GetCallback<ParseObject>()
                        {
                            @Override
                            public void done(ParseObject parseObject, ParseException e)
                            {
                                if (e == null)
                                {
                                    List<String> respondersList = parseObject.getList("responders");

                                    if (respondersList != null)
                                        if (respondersList.size() > 0)
                                            FragmentPanic.onUpdateResponder(respondersList.size());//Update responders in panic activity
                                }
                            }
                        });

                        prevLocation = location;
                    }
                    else
                    {
                        Log.i(TAG, "Last known location was null, so waited to use a gps location update before creating psnic");
                        createPanic(location);
                        prevLocation = location;
                    }
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras)
            {

            }

            @Override
            public void onProviderEnabled(String provider)
            {

            }

            @Override
            public void onProviderDisabled(String provider)
            {

            }
        };

        slowTrackListner = new LocationListener()
        {
            @Override
            public void onLocationChanged(Location location)
            {
                prevLocation = location;

                if(userLocListner != null)
                {
                    userLocListner.onLocationUpdate(location);
                    userLocListner = null;
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle)
            {

            }

            @Override
            public void onProviderEnabled(String s)
            {

            }

            @Override
            public void onProviderDisabled(String s)
            {

            }
        };
    }

    protected synchronized void buildGoogleApiClient()
    {
        googApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Log.i("Service GPS", "on start command");
        return START_STICKY;
    }

    @Override
    public boolean onUnbind(Intent intent)
    {
        googApiClient.disconnect();
        return super.onUnbind(intent);
    }

    @Override
    public void onLowMemory()
    {
        Log.i("Service GPS", "Low Memory");
        super.onLowMemory();
    }

    public void sendPanicPushNotification(Location location)
    {
        Log.d("Push", "Getting channels");
        List<String> channels = ParseInstallation.getCurrentInstallation().getList("channels");
        //Remove broadcast channel
        if(channels.indexOf("") != -1)
            channels.remove(channels.indexOf(""));


        ParsePush panicPush = new ParsePush();

        //Where
        //TODO: check if being sent to all channels not just some
        ParseQuery query = ParseInstallation.getQuery();
        query.whereContainedIn("channels", channels);
        query.whereNotEqualTo("objectId", ParseInstallation.getCurrentInstallation().getObjectId());
        panicPush.setQuery(query);
        Log.d("Push", "Set push query");

        //Data
        JSONStringer data = new JSONStringer();
        try
        {
            data.object().key("alert").value(HomeActivity.currentUser.get("name") + " needs help! Contact them on " + HomeActivity.currentUser.get("cellNumber") + " or view their location in the app.")
                    .key("badge").value("Increment").key("sound").value("default").key("panicId").value(panicUpdate.getObjectId())
                    .key("name").value(HomeActivity.currentUser.get("name")).key("cellNumber").value(HomeActivity.currentUser.get("cellNumber"))
                    .key("lat").value(location.getLatitude()).key("long").value(location.getLongitude()).endObject();

            Log.i("Panic Notification", "JSON Data: " + data.toString());

            panicPush.setData(new JSONObject(data.toString()));
            Log.d("Push", "Push data set");

        } catch (JSONException e)
        {
            e.printStackTrace();
        }

        Log.d("Push", "Sending push");
        panicPush.sendInBackground(new SendCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                    pushNotificationsSent = true;
                else
                    Log.e("Push", "Couldn't send push notification because: " + e.getMessage());
            }
        });
    }

    public void slowTrack(boolean enabled, int minTime, int minDistance)
    {
        if(enabled)
            locMang.requestLocationUpdates(LocationManager.GPS_PROVIDER, minTime, minDistance, slowTrackListner);
        else
            locMang.removeUpdates(slowTrackListner);
    }

    public void getUserLocation(UserLocationListener callback)
    {
        if(prevLocation == null)
            userLocListner = callback;
        else
            callback.onLocationUpdate(prevLocation);
    }

    public void onPanic(final boolean panic)
    {
        if(panic)
        {
            Log.i(TAG, "on Panic true");
            /*//Send some mock panics for regionPanic update testing
            List<ParseGeoPoint> listMockPoints = new ArrayList<ParseGeoPoint>();
            listMockPoints.add(new ParseGeoPoint(-33.917432, 18.420662));
            listMockPoints.add(new ParseGeoPoint(-33.915278, 18.418194));
            listMockPoints.add(new ParseGeoPoint(-33.914886, 18.425232));
            listMockPoints.add(new ParseGeoPoint(-33.927261, 18.431112));
            listMockPoints.add(new ParseGeoPoint(-33.908324, 18.472718));
            listMockPoints.add(new ParseGeoPoint(-33.945642, 18.466710));
            listMockPoints.add(new ParseGeoPoint(-33.952335, 18.459328));
            listMockPoints.add(new ParseGeoPoint(-33.875137, 18.578464));
            listMockPoints.add(new ParseGeoPoint(-33.890408, 18.667512));
            listMockPoints.add(new ParseGeoPoint(-33.879506, 18.676267));

            List<ParseObject> mockPanics = new ArrayList<ParseObject>();

            for(int i = 0; i < listMockPoints.size(); i++)
            {
                ParseObject mockPanic = new ParseObject("Panics");
                mockPanic.put("active", true);
                mockPanic.put("location", listMockPoints.get(i));
                mockPanic.put("user",HomeActivity.currentUser);
                mockPanics.add(mockPanic);
            }

            //send mock panics
            ParseObject.saveAllInBackground(mockPanics, new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if(e == null)
                        Log.i("Mock Panics", "Panics sent!");
                    else
                        Log.e("Mock Panics", "Panics FAILED: " + e.getMessage() + " Code: " + e.getCode());
                }
            });*/

            //Start panicing


            //Check if gps is enabled
            if(!locMang.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                //If gps is not enabled enable it
                wasGpsEnabledBefore = false;

                //at least prompt to turn on gps done in panic frag when clicking panic button
                /*//turn on gps
                Intent turnOnGpsIntent = new Intent("android.location.GPS_ENABLED_CHANGE");
                turnOnGpsIntent.putExtra("enabled", true);
                this.sendBroadcast(turnOnGpsIntent);

                final Intent poke = new Intent();
                poke.setClassName("com.android.settings", "com.android.settings.widget.SettingsAppWidgetProvider");
                poke.addCategory(Intent.CATEGORY_ALTERNATIVE);
                poke.setData(Uri.parse("3"));
                this.sendBroadcast(poke);*/
            }

            //Check if last know location is available
            if(lastKnownLocation != null)
            {
                Log.i(TAG, "Last known location: " + lastKnownLocation.getLatitude() + " , " + lastKnownLocation.getLongitude());
                createPanic(lastKnownLocation);
                prevLocation = lastKnownLocation;   //Set prevLocation so when relieving loc update, panic will be updated not recreated
            }
            else
                Log.i(TAG, "Last known location: was null");

            locMang.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, panicLocListner);
        }
        else
        {
            //Stop pannicing

            locMang.removeUpdates(panicLocListner);
//            Toast.makeText(getApplicationContext(), "Location updates removed", Toast.LENGTH_LONG).show();


            pushNotificationsSent = false;

            noLongerPanicking();

            //if gps was off before panacing change it back
            /*if(!wasGpsEnabledBefore)
            {
                Intent turnOffGpsIntent = new Intent("android.location.GPS_ENABLED_CHANGE");
                turnOffGpsIntent.putExtra("enabled", true);
                this.sendBroadcast(turnOffGpsIntent);
            }*/
        }
    }

    private void createPanic(Location location)
    {
        panicCanBeUpdated = false;
        panicUpdate = new ParseObject("Panics");

        //Set ACL for public read and write so responders can update object
        ParseACL allPublicAcl = new ParseACL();
        allPublicAcl.setPublicReadAccess(true);
        allPublicAcl.setPublicWriteAccess(true);

        panicUpdate.setACL(allPublicAcl);

        //Add user
        panicUpdate.put("user", HomeActivity.currentUser);
        panicUpdate.put("active", true);

        //Add location
        ParseGeoPoint geoLocation = new ParseGeoPoint(location.getLatitude(), location.getLongitude());
        panicUpdate.put("location", geoLocation);

        List<String> initRespondersColumn = new ArrayList<String>();
        panicUpdate.addAllUnique("responders", initRespondersColumn);

        panicUpdate.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                {
                    panicCanBeUpdated = true;
                    Log.i(TAG, "Panic created");
                }
                else
                {
                    if(e.getCode() == 100)
                        Toast.makeText(getApplicationContext(), R.string.error_100_no_internet, Toast.LENGTH_LONG).show();

                    Log.i("Error:", "Unsuccessful panic update: " + e.getMessage() + " Code: " + e.getCode());
                }
            }
        });
    }

    public void noLongerPanicking()
    {
        //Check if panic ever made it to db in case of slow connection
        //TODO: perhaps do async and retry every 5 till successfull or max amount of retries
        if(panicCanBeUpdated && panicUpdate != null)
        {
            panicUpdate.put("active", false);
            panicUpdate.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if (e == null)
                    {
                        Log.i(TAG, "Updating panic active = false");
                    } else
                        Log.e(TAG, "An error occurred while trying to deactivate panic on parse: " + e.getMessage() + " Code: " + e.getCode());
                }
            });
        }
    }

    public void onDetailsGiven(String details)
    {
        //Add details if any are given
        this.details = details;
        if(panicUpdate != null)
            panicUpdate.put("details", details);
    }

}
