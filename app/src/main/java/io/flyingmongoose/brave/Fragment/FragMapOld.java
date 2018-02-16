package io.flyingmongoose.brave.fragment;

import android.app.Fragment;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.flyingmongoose.brave.activity.ActivHome;
import io.flyingmongoose.brave.R;

/**
 * Created by IC on 1/27/2015.
 */
public class FragMapOld extends Fragment
{
    private MapView mvMap;
    private GoogleMap googMap;
    private Marker trackPanicMarker;
    private Map<String, Marker> regionMarkers;  //Key is panic obj id
    private Map<String, ParseObject> regionPanics;  //Key is panic obj id
    private Map<Marker, ParseObject> markerPanicData; //Key is marker
    private boolean regionCanBeupdated = true;

    private Timer trackTimer;
    private Timer regionUpdateTimer;
    private final long TRACK_TIMER_START_DELAY = 0;
    private final long TRACK_TIMER_INTERVAL = 10000;
    private final long REGION_TIMER_START_DELAY = 0;
    private final long REGION_TIMER_INTERVAL = 10000;
    private final double REGION_PANIC_UPDATE_MAX_RANGE = 100;   //in km

    private String trackPanicId = "";
    private boolean trackPanicFirstFix = true;
    private boolean regionPanicFirstFix = true;
    private boolean trackPanicFollow = false;
    private JSONObject jsonObject = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_map_layout, container, false);

        mvMap = (MapView) view.findViewById(R.id.mvMap);
        mvMap.onCreate(savedInstanceState);

        Log.i("fragMap", "On Create called");

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mvMap.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mvMap.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mvMap.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mvMap.onLowMemory();
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);

        if(!hidden)
        {

            //Load a map
            mvMap.onResume();

            try
            {
                MapsInitializer.initialize(getActivity().getApplicationContext());
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }

//            googMap = mvMap.getMap();
            googMap.setMyLocationEnabled(true);

            //Check for data from possible push notification
            if (!ActivHome.jsonPushData.isEmpty())
            {
                Log.i("Service GPS", ActivHome.jsonPushData);
                //Use notification data to set init map

                try
                {
                    jsonObject = new JSONObject(ActivHome.jsonPushData);
                    final ParseGeoPoint defaultLocation = new ParseGeoPoint(jsonObject.getDouble("lat"), jsonObject.getDouble("lon"));

                    if(jsonObject.has("panicId"))
                    {
                        trackPanicId = jsonObject.getString("panicId");
                        final String name = jsonObject.getString("name");
                        final String cellNumber = jsonObject.getString("cellNumber");

                        //Update every 10 seconds track panic
                        trackTimer = new Timer();

                        trackTimer.scheduleAtFixedRate(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                //Retrieve panic obj and update map
                                updateTrackPanic(trackPanicId, name, cellNumber, defaultLocation);
                            }
                        }, TRACK_TIMER_START_DELAY, TRACK_TIMER_INTERVAL);
                    }
                    else
                    {
                        //Just animate camera to location like like iOS app
                        googMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(defaultLocation.getLatitude(), defaultLocation.getLongitude()), 12f));
                    }

                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

                //Update region panics every 10 sec within range km from camera location
                googMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener()
                {
                    @Override
                    public void onCameraChange(final CameraPosition cameraPosition)
                    {
                        if(regionUpdateTimer != null)
                            regionUpdateTimer.cancel();

                        regionUpdateTimer = new Timer();

                        regionUpdateTimer.scheduleAtFixedRate(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                updateRegionalPanics(new ParseGeoPoint(cameraPosition.target.latitude, cameraPosition.target.longitude));
                            }
                        }, REGION_TIMER_START_DELAY, REGION_TIMER_INTERVAL);
                    }
                });

            }
            else
            {
                //animate to user location
                LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
                Criteria criteria = new Criteria();

                Location lastLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
                if(lastLocation != null)
                    googMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lastLocation.getLatitude(), lastLocation.getLongitude()), 12f));

                //Update region panics every 10 sec within range km from camera location
                googMap.setOnCameraChangeListener(new GoogleMap.OnCameraChangeListener()
                {
                    @Override
                    public void onCameraChange(final CameraPosition cameraPosition)
                    {
                        if(regionUpdateTimer != null)
                            regionUpdateTimer.cancel();

                        regionUpdateTimer = new Timer();

                        regionUpdateTimer.scheduleAtFixedRate(new TimerTask()
                        {
                            @Override
                            public void run()
                            {
                                updateRegionalPanics(new ParseGeoPoint(cameraPosition.target.latitude, cameraPosition.target.longitude));
                            }
                        }, REGION_TIMER_START_DELAY, REGION_TIMER_INTERVAL);
                    }
                });

            }
        }
        else
        {
            if(trackTimer != null)
             trackTimer.cancel();

            if(regionUpdateTimer != null)
                regionUpdateTimer.cancel();
        }
    }

    private void updateRegionalPanics(ParseGeoPoint regionLocation)
    {
        Log.i("Update Region Panics", "Starting region panic update!");

        if(regionCanBeupdated)
        {
            regionCanBeupdated = false;
            //find all active panics in 100km radius from region location
            ParseQuery<ParseObject> queryRegionPanics = new ParseQuery<ParseObject>("Panics");
            queryRegionPanics.whereEqualTo("active", true);
            queryRegionPanics.whereWithinKilometers("location", regionLocation, REGION_PANIC_UPDATE_MAX_RANGE);

            if (!trackPanicId.isEmpty())
                queryRegionPanics.whereNotEqualTo("objectId", trackPanicId);

            queryRegionPanics.findInBackground(new FindCallback<ParseObject>()
            {
                @Override
                public void done(List<ParseObject> freshRegionPanics, ParseException e)
                {
                    if (e == null)
                    {

                        Log.i("Region Panics", "Region Panics found: " + freshRegionPanics.size());

                        if (regionPanicFirstFix)
                        {
                            //Setup panics
                            regionPanics = new HashMap<String, ParseObject>();
                            regionMarkers = new HashMap<String, Marker>();
                            markerPanicData = new HashMap<Marker, ParseObject>();

                            for (int i = 0; i < freshRegionPanics.size(); i++)
                            {
                                ParseObject panicObj = freshRegionPanics.get(i);
                                ParseGeoPoint markerLocation = panicObj.getParseGeoPoint("location");

                                Marker somePanicMarker = googMap.addMarker(new MarkerOptions().position(new LatLng(markerLocation.getLatitude(), markerLocation.getLongitude()))
                                        .title(panicObj.getString("name")).snippet(panicObj.getString("cellNumber") + "\n" + panicObj.getString("details"))
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.panic_pin)).anchor(0.5f, 0.5f));

                                //Add to regionPanics and regionMarkers (RegionMarkers used as key for Region Panics)
                                regionPanics.put(panicObj.getObjectId(), panicObj);
                                regionMarkers.put(panicObj.getObjectId(), somePanicMarker);
                                markerPanicData.put(somePanicMarker, panicObj);
                            }

                            //TODO: set widow adpater
                            /*infoWindowAdapter = new AdaptInfoWindow(getActivity(), markerPanicData, trackPanicId);
                            googMap.setInfoWindowAdapter(infoWindowAdapter);*/

                            regionPanicFirstFix = false;
                        }
                        else
                        {
                            //Update region panics
                            //Update each panic found, add ones not found
                            for (int i = 0; i < freshRegionPanics.size(); i++)
                            {
                                ParseObject aFreshPanic = freshRegionPanics.get(i);
                                String objectId = aFreshPanic.getObjectId();
                                ParseGeoPoint newLocation = aFreshPanic.getParseGeoPoint("location");
                                //Find stale panics and update them else add the new panic
                                if (regionPanics.containsKey(objectId))
                                {
                                    //Update if found

                                    regionMarkers.get(objectId).setPosition(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()));
                                    regionPanics.put(objectId, aFreshPanic);
                                    markerPanicData.put(regionMarkers.get(objectId), aFreshPanic);
                                }
                                else
                                {
                                    //Add if not found
                                    Marker somePanicMarker = googMap.addMarker(new MarkerOptions().position(new LatLng(newLocation.getLatitude(), newLocation.getLongitude()))
                                            .title(aFreshPanic.getString("name")).snippet(aFreshPanic.getString("cellNumber") + "\n" + aFreshPanic.getString("details"))
                                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.panic_pin)).anchor(0.5f, 0.5f));

                                    //Add to record keeping
                                    regionMarkers.put(objectId, somePanicMarker);
                                    regionPanics.put(objectId, aFreshPanic);
                                    markerPanicData.put(somePanicMarker, aFreshPanic);
                                }

                            }

                            //Remove panics that are not contained in the update list
                             Iterator<Map.Entry<String, ParseObject>> iter = regionPanics.entrySet().iterator();
                            while(iter.hasNext())
                            {
                                Map.Entry<String, ParseObject> entry = iter.next();

                                //Search trough freshPanics for key
                                boolean found = false;
                                for (int i = 0; i < freshRegionPanics.size(); i++)
                                {
                                    if (freshRegionPanics.get(i).getObjectId().contentEquals(entry.getKey()))
                                    {
//                                        Log.i("Remove Region Panics", "Panic found: " + entry.getKey());
                                        found = true;
                                        break;
                                    }
                                }

                                if(!found)
                                {
                                    //Remove panics not found in fresh list
//                                    Log.i("Remove Region Panics", "Panic Not FOUND!: " + entry.getKey());
                                    markerPanicData.remove(regionMarkers.get(entry.getKey()));
                                    regionMarkers.get(entry.getKey()).remove();
                                    regionMarkers.remove(entry.getKey());
                                    iter.remove();
                                }
                            }

                            //infoWindowAdapter.updateMarkerPanicData(markerPanicData);

                        }
                    }
                    else
                    {
                        Log.e("Region Panic", "An error occured while retrieving region panics to track: " + e.getMessage() + " Code: " + e.getCode());
                    }

                    regionCanBeupdated = true;
                }
            });
        }
    }

    private void updatePanicUserData()
    {

    }

    private void updateTrackPanic(final String trackFinalPanicId, final String name, final String cellNumber, final ParseGeoPoint defaultLocation)
    {


        ParseQuery<ParseObject> queryTrackPanic = new ParseQuery<ParseObject>("Panics");
        queryTrackPanic.whereEqualTo("objectId", trackFinalPanicId);
        queryTrackPanic.getFirstInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                ParseGeoPoint parsePanicLocation = null;

                if(e == null)
                {
                    //Get updated location
                     parsePanicLocation = parseObject.getParseGeoPoint("location");

                }
                else
                {
                    Log.e("Track Push Panic", "An error occured while retrieving panic object to track: " + e.getMessage() + " Code: " + e.getCode());
                }

                if(parsePanicLocation == null)
                    parsePanicLocation = defaultLocation;

                if(parseObject.getBoolean("active"))
                {
                    if (trackPanicFirstFix)
                    {
                        //set first fix with zoom animation and follow
                        trackPanicMarker = googMap.addMarker(new MarkerOptions().position(new LatLng(parsePanicLocation.getLatitude(), parsePanicLocation.getLongitude())).title("Help Me!").snippet(name + " " + cellNumber)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.panic_follow_pin)).anchor(0.5f, 0.5f));
                        googMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(parsePanicLocation.getLatitude(), parsePanicLocation.getLongitude()), 12f));
                        trackPanicFirstFix = false;
                    }
                    else
                    {
                        trackPanicMarker.setPosition(new LatLng(parsePanicLocation.getLatitude(), parsePanicLocation.getLongitude()));

                        if (trackPanicFollow)
                            googMap.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(parsePanicLocation.getLatitude(), parsePanicLocation.getLongitude())));
                    }
                }
                else
                {
                    if(trackPanicMarker != null)
                    {
                        //Reset all variables
                        trackPanicId = "";
                        trackPanicFirstFix = true;
                        trackPanicFollow = true;
                        trackPanicMarker.remove();
                    }

                    Toast.makeText(getActivity(), name + " is no longer panicing", Toast.LENGTH_LONG).show();
                    trackTimer.cancel();
                }
            }
        });
    }
}
