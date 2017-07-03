package io.flyingmongoose.brave;

import android.*;
import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by !Aries! on 2015-06-27.
 */
public class FragmentMap extends Fragment implements GoogleMap.OnInfoWindowClickListener, GoogleMap.OnMapClickListener, CompoundButton.OnCheckedChangeListener, SwipeRefreshLayout.OnRefreshListener
{

    private final String TAG = "FragmentMap";
    private final int REQ_PERM_LOC = 100;

    private final FragmentMap thisFrag = this;

    //Map
    private MapView mvMap;
    private View vMapPopupAnchor;
    //Map Constants
    private final float MAP_ZOOM_LEVEL = 12.0f;

    //Push notification data
    private String trackPanicId = "";
    private String trackPanicName = "The user";
    private ParseGeoPoint trackPanicLastKnownLocation;
    private JSONObject jsonObjPushData;
    private boolean trackPanicFollow = true;
    private Marker trackPanicMarker;
    //JSON Key names
    private final String PANIC_ID = "panicId";
    private final String PANIC_NAME = "name";
    private final String LAT = "lat";
    private final String LON = "long";

    //Panic data
    private boolean panicUpdateLocked = false;
    private Map<String, Marker> panicMarkers = new HashMap<String, Marker>();   //Uses panic object id as key
    private Map<String, ParseObject> panicObjs = new HashMap<String, ParseObject>(); //Uses marker id as key
    //Panic data Constants
    private final double PANIC_UPDATE_RANGE = 100; //in km distance from camera to query for active panics
    private final float PANIC_UPDATE_CAMERA_DISTANCE = 20; //in km amount camera has to move before a force update is done even though timer is not ready for an update
    private final int PANIC_MIN_TIME_INTERVAL_BEFORE_UPDATE = 3000; //in milli seconds minimum time interval before accepting a new camera location and trigering an update of panic data
    private final int PANIC_UPDATE_INTERVAL = 5000;    //in milliseconds (NEEDS TO BE HALF OF ACTUAL INTENDED) time between panic data updates if an update hasn't been trigger by the camera in this

    //Panic update data
    private LatLng camPrevLocation = null;
    private long lastUpdatedAt;
    private Timer panicUpdateTimer;

    private PopupWindow respondPopupWindow;
    private boolean displayingRespondWindow = false;    //Used to ensure the user can't open 2 respond windows because of laggyness
    private ToggleButton tbtnTrack;

    private SwipeRefreshLayout srLayMapLoading;
    private boolean refreshingForMap = false;   //Used soo that update of panics don't clear prog cricle when it is being used for other things like dialogs ect
    private boolean reafreshingForRepondDialog = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_map_layout, container, false);

        mvMap = (MapView) view.findViewById(R.id.mvMap);
        mvMap.onCreate(savedInstanceState);

        vMapPopupAnchor = view.findViewById(R.id.vMapPopupAnchor);
        tbtnTrack = (ToggleButton) view.findViewById(R.id.tbtnTrack);

        srLayMapLoading = (SwipeRefreshLayout) view.findViewById(R.id.srLayMapLoading);
        srLayMapLoading.setOnRefreshListener(this);

        Log.i(TAG, "Fragment Map onCreateView");

        return view;
    }

    private void initSwipeRefresh()
    {
        srLayMapLoading.setEnabled(false);
        srLayMapLoading.setOnRefreshListener(this);
        srLayMapLoading.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayMapLoading.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayMapLoading.setProgressViewOffset(true, 0, 5);
    }

    @Override
    public void onRefresh()
    {
        srLayMapLoading.setRefreshing(true);
        srLayMapLoading.setRefreshing(false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        Log.i(TAG, "Fragment Map onActivityCreated");

        //Initialize and get a handle on map obj
        initMap();

        //Check if push notification data exists
        checkPushNotificationData();
    }

    @Override
    public void onDetach()
    {
        super.onDetach();

        //if respond dialog is exists dismiss it
        if(respondPopupWindow != null)
            respondPopupWindow.dismiss();

        Log.i(TAG, "Fragment Map onDetach");
    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);

        if(!hidden)
        {
            Log.i(TAG, "Fragment Map Becoming visible");

            //Check if gps is on
            GpsHelper gpsHelper = new GpsHelper(getActivity());

            if(!gpsHelper.isGpsOn())
                gpsHelper.showDialog("Your GPS is disabled, please enable it to see your location");

            initSwipeRefresh();

            srLayMapLoading.setRefreshing(true);
            refreshingForMap = true;
            //Initialize and get a handle on map obj
            initMap();

            //Check if push notification data exists
            checkPushNotificationData();
        } else
        {
            //if respond dialog is exists dismiss it
            if(respondPopupWindow != null)
                respondPopupWindow.dismiss();

            //Clean up
            //Remove intervalUpdates
            if(panicUpdateTimer != null)
                panicUpdateTimer.cancel();

            Log.i(TAG, "Fragment Map Becoming invisible");
        }

        /*if(hidden)
        {
            //if respond dialog is exists dismiss it
            if(respondPopupWindow != null)
                respondPopupWindow.dismiss();

            Log.i(TAG, "Fragment Map Becoming invisible");
        }*/
    }

    private void initMap()
    {
        //Get ref to google map obj
        mvMap.onResume();

        MapsInitializer.initialize(getActivity().getApplicationContext());

        mvMap.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap googleMap)
            {
                googleMap.setMyLocationEnabled(true);
                googleMap.setPadding(calcLeftPadding(), 0, 0, calcBottom());
                googleMap.setOnInfoWindowClickListener(thisFrag);
                googleMap.setOnMapClickListener(thisFrag);

                //Animate camera to initial location
                initCamera(googleMap);

                //Init camPrevLocation
                CameraPosition tempCamPos = googleMap.getCameraPosition();
                camPrevLocation = new LatLng(tempCamPos.target.latitude, tempCamPos.target.longitude);

                //Load initial panics, build house keeping and begin updates
                requestPanicUpdates(googleMap);
            }
        });

        Log.i(TAG, "Map Initialized");
    }

    private int calcBottom()
    {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        int bottomPadding = height / 12;

        return bottomPadding;
    }

    private int calcLeftPadding()
    {
        Display display = getActivity().getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;

        int leftPadding = width / 6;

        return leftPadding;
    }

    private void checkPushNotificationData()
    {
        Log.i(TAG, "Fragment Map checkPushNotificationData");
        //Check if push notification data exists and perform house keeping
        if(!HomeActivity.jsonPushData.isEmpty())
        {
            //Convert json string to json object
            try
            {
                jsonObjPushData = new JSONObject(HomeActivity.jsonPushData);

                //There is push data, check for backwards compat if a panic Id was sent
                if(jsonObjPushData.has(PANIC_ID))
                {
                    trackPanicId = jsonObjPushData.getString(PANIC_ID);
                    //Make follow button visible and as active, TODO: can animate here as well
                    tbtnTrack.setVisibility(View.VISIBLE);
                    tbtnTrack.setChecked(true);
                    tbtnTrack.setOnCheckedChangeListener(this);
                }

                //Check and retrieve name if available
                if(jsonObjPushData.has(PANIC_NAME))
                    trackPanicName = jsonObjPushData.getString(PANIC_NAME);

                //get last know location data
                trackPanicLastKnownLocation = new ParseGeoPoint(jsonObjPushData.getDouble(LAT), jsonObjPushData.getDouble(LON));

            } catch(JSONException e)
            {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        trackPanicFollow = isChecked;
    }

    //Animate camera to initial location
    private void initCamera(final GoogleMap googMap)
    {
        //Check if push data exists and that a last know panic location was relieved
        if(jsonObjPushData != null && trackPanicLastKnownLocation.getLatitude() != 0)
        {
            googMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(trackPanicLastKnownLocation.getLatitude(), trackPanicLastKnownLocation.getLongitude()), MAP_ZOOM_LEVEL));
        } else if(HomeActivity.pushLat != 0 && HomeActivity.pushLng != 0)
        {
            Log.d("debug", "Used notif loc to init map cam");
            googMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(HomeActivity.pushLat, HomeActivity.pushLng), MAP_ZOOM_LEVEL));
        } else
        {
            //find user last known location and animate to that if it exists
            LocationManager locMang = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
            Location userLastKnownLocation = locMang.getLastKnownLocation(locMang.getBestProvider(new Criteria(), false));

            //Check if a last known location was available, if so animate to it
            if(userLastKnownLocation != null)
            {
                googMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(userLastKnownLocation.getLatitude(), userLastKnownLocation.getLongitude()), MAP_ZOOM_LEVEL));
            } else
            {
                //listen for a location and animate as soon as found, then stop listening
                googMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener()
                {
                    @Override
                    public void onMyLocationChange(Location location)
                    {
                        googMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), MAP_ZOOM_LEVEL));
                        googMap.setOnMyLocationChangeListener(null);
                    }
                });

            }

        }

        Log.i(TAG, "Camera initialized");
    }

    private void requestPanicUpdates(final GoogleMap googMap)
    {
        //Load all panics within 100km of the camera
        //Get camera location for query
        googMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener()
        {

            @Override
            public void onCameraIdle()
            {
                CameraPosition cameraPosition = googMap.getCameraPosition();

                Log.d(TAG, "New Camera location: " + cameraPosition.target.latitude + " : " + cameraPosition.target.longitude);

                //Check if a prev loc exists
                if(camPrevLocation != null)
                {
                    //Check if position has change and more than 3 sec have passed
                    LatLng newCamLocation = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                    long timeNow = SystemClock.elapsedRealtime();

                    if((!camPrevLocation.equals(newCamLocation)) && (timeNow - lastUpdatedAt) > PANIC_MIN_TIME_INTERVAL_BEFORE_UPDATE)
                    {
                        updatePanics(googMap, new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude));
                        camPrevLocation = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                        lastUpdatedAt = SystemClock.elapsedRealtime();
                        Log.i(TAG, "Panics updated by cam location");
                    }
                } else
                {
                    updatePanics(googMap, new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude));
                    camPrevLocation = new LatLng(cameraPosition.target.latitude, cameraPosition.target.longitude);
                    lastUpdatedAt = SystemClock.elapsedRealtime();
                    Log.i(TAG, "Panics updated by cam location");
                }
            }
        });

        //Set panic updates to check every 10 seconds if last update is older than 10 secs, update if it is
        intervalUpdates(googMap);

        Log.i(TAG, "Panic Updates Requested");
    }

    //Set panic updates to check every 10 seconds if last update is older than 10 secs, update if it is
    private void intervalUpdates(final GoogleMap googMap)
    {
        panicUpdateTimer = new Timer();

        panicUpdateTimer.scheduleAtFixedRate(new TimerTask()
        {
            @Override
            public void run()
            {
                long timeNow = SystemClock.elapsedRealtime();

                if((timeNow - lastUpdatedAt) >= PANIC_UPDATE_INTERVAL)
                {
                    updatePanics(googMap, camPrevLocation);  //THis is actual most recently known location, cant access map camera from a thread other than the main thread
                    lastUpdatedAt = SystemClock.elapsedRealtime();
                    Log.i(TAG, "Panics updated by interval");
                }
            }
        }, PANIC_UPDATE_INTERVAL, PANIC_UPDATE_INTERVAL);
    }

    public void updatePanics(final GoogleMap googMap, LatLng camLocation)
    {
        Log.i(TAG, "Fragment Map updatePanics");
        //Check if update is locked
        if(!panicUpdateLocked)
        {
            //Lock panic updates
            panicUpdateLocked = true;

            //Query all active panics within 100km of cam loc
            ParseQuery<ParseObject> queryActivePanics = new ParseQuery<ParseObject>("Panics");
            queryActivePanics.whereEqualTo("active", true);
            queryActivePanics.whereWithinKilometers("location", new ParseGeoPoint(camLocation.latitude, camLocation.longitude), PANIC_UPDATE_RANGE);
            queryActivePanics.include("user");

            queryActivePanics.findInBackground(new FindCallback<ParseObject>()
            {
                @Override
                public void done(List<ParseObject> freshPanics, ParseException e)
                {
                    if(e == null)
                    {

                        //Cycle trough fresh panics add and update
                        for(int i = 0; i < freshPanics.size(); i++)
                        {
                            String freshPanicObjectId = freshPanics.get(i).getObjectId();

                            //Check if object id exist in current panic data
                            if(panicMarkers.containsKey(freshPanicObjectId))
                            {
                                //update panic data with fresh coordinates
                                Marker panicMarker = panicMarkers.get(freshPanicObjectId);

                                //Update panic obj
                                panicObjs.put(panicMarker.getId(), freshPanics.get(i));
                                //Move pin
                                ParseGeoPoint newLoc = freshPanics.get(i).getParseGeoPoint("location");
                                panicMarker.setPosition(new LatLng(newLoc.getLatitude(), newLoc.getLongitude()));
                            } else
                            {
                                //add new panic
                                //Check if pin is to be tracked
                                if(trackPanicId.toString().equals(freshPanicObjectId))
                                {
                                    //add a tracked panic
                                    //add tracked pin
                                    ParseGeoPoint panicLoc = freshPanics.get(i).getParseGeoPoint("location");
                                    Marker panicMarker = googMap.addMarker(new MarkerOptions().position(new LatLng(panicLoc.getLatitude(), panicLoc.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.panic_follow_pin)));

                                    //If follow is enabled follow pin by animating camera
                                    trackPanicMarker = panicMarker;

                                    if(trackPanicFollow)
                                        googMap.animateCamera(CameraUpdateFactory.newLatLngZoom(trackPanicMarker.getPosition(), MAP_ZOOM_LEVEL));


                                    //House keeping
                                    panicMarkers.put(freshPanicObjectId, panicMarker);
                                    panicObjs.put(panicMarker.getId(), freshPanics.get(i));
                                } else
                                {
                                    //add normal pin
                                    ParseGeoPoint panicLoc = freshPanics.get(i).getParseGeoPoint("location");
                                    Marker panicMarker = googMap.addMarker(new MarkerOptions().position(new LatLng(panicLoc.getLatitude(), panicLoc.getLongitude())).icon(BitmapDescriptorFactory.fromResource(R.drawable.panic_pin)));

                                    //House keeping
                                    panicMarkers.put(freshPanicObjectId, panicMarker);
                                    panicObjs.put(panicMarker.getId(), freshPanics.get(i));
                                }
                            }
                        }

                        //Remove stale panics
                        Iterator<Map.Entry<String, Marker>> iterator = panicMarkers.entrySet().iterator();
                        boolean trackedPanicFound = false;
                        while(iterator.hasNext())
                        {
                            //Iterate trough searching update list for current entry (key is panic obj id)
                            Map.Entry<String, Marker> entry = iterator.next();

                            boolean found = false;
                            for(int i = 0; i < freshPanics.size(); i++)
                            {
                                //Check if track pin is found in update, else clear notification data and track pin setting
                                if(!trackPanicId.isEmpty())
                                    if(freshPanics.get(i).getObjectId().equals(trackPanicId))
                                        trackedPanicFound = true;

                                if(freshPanics.get(i).getObjectId().equals(entry.getKey()))
                                {
                                    found = true;
                                    break;
                                }
                            }

                            if(!found)
                            {
                                /*ParseObject panicObjNotFounInUpdate = panicObjs.get(entry.getValue().getId());

                                //check if the pin being tracked is removed and notify user
                                if(panicObjNotFounInUpdate.getObjectId().equals(trackPanicId))
                                {
                                    ParseUser userObj = panicObjNotFounInUpdate.getParseUser("user");



                                }*/

                                //remove from panic data
                                panicObjs.remove(entry.getValue().getId());
                                entry.getValue().remove();  //removes pin from map
                                iterator.remove();
                            }
                        }

                        /*//Reset panicUserData & Get user data for info window details
                        //Build list of users to fetch and marker id for keys
                        Iterator<Map.Entry<String, ParseObject>> iteratorObj = panicObjs.entrySet().iterator();

                        while (iteratorObj.hasNext())
                        {
                            Map.Entry<String, ParseObject> entry = iteratorObj.next();

                            panicUsers.put(entry.getKey(), entry.getValue().getParseUser("user"));
                        }*/

                        //set info window adapter
                        googMap.setInfoWindowAdapter(new CustomInfoWindowAdapter(getActivity(), panicObjs));

                        //Only check if panic was found if a track panic id was set
                        if(!trackPanicId.isEmpty())
                        {
                            if(!trackedPanicFound)
                            {
                                trackPanicFollow = false;
                                tbtnTrack.setVisibility(View.GONE);

                                Toast.makeText(getActivity(), trackPanicName + " is no longer panicking", Toast.LENGTH_LONG).show();

                                //Remove json data so when naving out of map and back in track panic is not registered again
                                HomeActivity.jsonPushData = "";
                                trackPanicId = "";
                                trackPanicName = "The user";
                            }
                        }

                        //Update track panic camera if there is a panic to track
                        if(trackPanicMarker != null)
                            if(trackPanicFollow)
                                googMap.animateCamera(CameraUpdateFactory.newLatLngZoom(trackPanicMarker.getPosition(), MAP_ZOOM_LEVEL));

                        //Unlock for updating again
                        panicUpdateLocked = false;
                        if(srLayMapLoading.isRefreshing() && refreshingForMap)
                        {
                            srLayMapLoading.setRefreshing(false);
                            refreshingForMap = false;
                        }

                        Log.i(TAG, "Completed update panics");

                    } else
                    {
                        if(srLayMapLoading.isRefreshing() && refreshingForMap)
                        {
                            srLayMapLoading.setRefreshing(false);
                            refreshingForMap = false;
                        }

                        if(e.getCode() == 100)
                            Toast.makeText(getActivity(), R.string.error_100_no_internet, Toast.LENGTH_LONG).show();
                        else
                            Toast.makeText(getActivity(), "An error occurred while updating panics: " + e.getMessage() + " Code: " + e.getCode(), Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onInfoWindowClick(final Marker marker)
    {
        srLayMapLoading.setRefreshing(true);
        reafreshingForRepondDialog = true;
        if(!displayingRespondWindow)    //Used to make sure user can't create 2 of these windows
            displayRespondWindow(marker);//Display dialogue window with reverse geo address

        Log.i(TAG, "Info Window click completed");
    }

    private void displayRespondWindow(final Marker marker)
    {
        displayingRespondWindow = true;

        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        final View popupWindow = layoutInflater.inflate(R.layout.pop_up_repond, null);

        respondPopupWindow = new PopupWindow(popupWindow, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        TextView txtvName = (TextView) popupWindow.findViewById(R.id.txtvPopUpRespondName);
        txtvName.setText(panicObjs.get(marker.getId()).getParseUser("user").getString("name"));

        ImageView closeRespondWindow = (ImageView) popupWindow.findViewById(R.id.imgvPopUpRespondClose);
        closeRespondWindow.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                respondPopupWindow.dismiss();
                displayingRespondWindow = false;
            }
        });

        TextView txtvCellNumber = (TextView) popupWindow.findViewById(R.id.txtvPopUpRespondCellNumber);
        txtvCellNumber.setText(panicObjs.get(marker.getId()).getParseUser("user").getString("cellNumber"));

        TextView txtvDetails = (TextView) popupWindow.findViewById(R.id.txtvPopUpRespondDetails);

        String details = panicObjs.get(marker.getId()).getString("details");

        if(details != null)
            if(!details.isEmpty())
                txtvDetails.setText(details);

        final ProgressBar progBarLoadingAddress = (ProgressBar) popupWindow.findViewById(R.id.progbPopUpRespondLoadingAddress);
        final TextView txtvAddress = (TextView) popupWindow.findViewById(R.id.txtvPopUpRespondAddress);

        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                String address = reverseGeoLocation(marker.getPosition());

                txtvAddress.setText(address);
                txtvAddress.setVisibility(View.VISIBLE);
                progBarLoadingAddress.setVisibility(View.GONE);
            }
        }).run();

        Button btnCall = (Button) popupWindow.findViewById(R.id.btnPopUpRespondCall);
        btnCall.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Call
                String uri = "tel:" + panicObjs.get(marker.getId()).getParseUser("user").getString("cellNumber").trim();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
                respondPopupWindow.dismiss();
                displayingRespondWindow = false;
            }
        });

        Button btnRespond = (Button) popupWindow.findViewById(R.id.btnPopUpRespondRespond);
        btnRespond.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //Respond
                ParseObject panicObj = panicObjs.get(marker.getId());
                panicObj.addUnique("responders", HomeActivity.currentUser.getObjectId());
                panicObj.saveInBackground(new SaveCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        if(e == null)
                        {
                            Toast.makeText(getActivity(), "The user will be notified, you are responding", Toast.LENGTH_LONG).show();
                        } else
                            Toast.makeText(getActivity(), "An error occurred while responding: " + e.getMessage() + " Code: " + e.getCode(), Toast.LENGTH_LONG).show();
                    }
                });
                respondPopupWindow.dismiss();
                displayingRespondWindow = false;
            }
        });

        //Touch button feedback
        View.OnTouchListener onTouchRespondButton = new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                Button btnTouched = (Button) v;

                if(event.getAction() == MotionEvent.ACTION_DOWN)
                    btnTouched.setBackgroundColor(getResources().getColor(R.color.LightHintGrey));
                else if(event.getAction() == MotionEvent.ACTION_UP)
                    btnTouched.setBackground(null);
                return false;
            }
        };

        btnCall.setOnTouchListener(onTouchRespondButton);
        btnRespond.setOnTouchListener(onTouchRespondButton);

        respondPopupWindow.showAsDropDown(vMapPopupAnchor, 30, 200);

        if(srLayMapLoading.isRefreshing() && reafreshingForRepondDialog)
        {
            srLayMapLoading.setRefreshing(false);
            reafreshingForRepondDialog = false;
        }
        Log.i(TAG, "Popup Window completed");
    }

    private String reverseGeoLocation(LatLng location)
    {
        Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
        String formattedAddress = "";
        try
        {
            List<Address> addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1);

            //Format address
            Address resultAddress = addresses.get(0);
            formattedAddress += resultAddress.getAddressLine(0);
            formattedAddress += "\n" + resultAddress.getAddressLine(1);
            formattedAddress += "\n" + resultAddress.getCountryName();
            formattedAddress += "\n" + resultAddress.getPostalCode();

        } catch(IOException e)
        {
            e.printStackTrace();
        }


        Log.i(TAG, "Finished reverse geocoding: \n" + formattedAddress);
        return formattedAddress;
    }

    @Override
    public void onMapClick(LatLng latLng)
    {
        if(respondPopupWindow != null)
            respondPopupWindow.dismiss();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        mvMap.onResume();
        Log.i(TAG, "Fragment Map onResume");
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mvMap.onPause();
        Log.i(TAG, "Fragment Map onPause");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mvMap.onDestroy();
        Log.i(TAG, "Fragment Map onDestroy");
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mvMap.onLowMemory();
        Log.i(TAG, "Fragment Map onLowMemory");
    }
}
