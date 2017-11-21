package io.flyingmongoose.brave;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.PersistableBundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.google.firebase.iid.FirebaseInstanceId;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wooplr.spotlight.SpotlightView;
import com.wooplr.spotlight.prefs.PreferencesManager;
import com.wooplr.spotlight.utils.SpotlightListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.sephiroth.android.library.bottomnavigation.BottomNavigation;


public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    private final int REQ_PERM_LOC = 100;
    private final String LOG_TAG = "HomeActivity";
    private DrawerLayout drawLayNav;
    private ListView lstvNav;
    private ActionBarDrawerToggle drawerListener;   //This is used because it already implements drawer listener
    private NavAdapter navAdapter;      //Custom adapter for populating nav menu
    public static TextView txtvProfileName;
    private View vDummyGroupTut;

    //fragments
    public  FragmentPanic fragPanic = null;
    public  static FragmentMap fragMap = null;
    public FragmentGroups fragGroups = null;
    public  FragmentSettings fragSettings = null;
    public FragmentSettingsNew fragSettingsNew = null;
    public  FragmentHistory fragHistory = null;
    public  FragmentBottomActionBar fragBottomActionBar = null;
    public  static FragmentManager fragManager;
    public FragmentTransaction fragTransaction;
    public  FragmentManageGroups fragMangGroups;

    //Fragment tags
    public static final String TAG_FRAG_PANIC = "fragPanic";
    public static final String TAG_FRAG_MAP = "fragMap";
    public static final String TAG_FRAG_BOTTOM_ACTION_BAR = "fragBottomActionBar";
    public static final String TAG_FRAG_GROUPS = "fragGroups";
    public static final String TAG_FRAG_MANG_GROUPS = "fragMangGroups";
    public static final String TAG_FRAG_HISTORY = "fragHistory";
    public static final String TAG_FRAG_SETTINGS = "fragSettings";
    public final String TAG = "HomeActivity";

    public FrameLayout fLayBottomActionBar;
    public FloatingActionButton fabNeedleDrop;

    private String navSelectedEntryTag = "Home";

    //Group manage vars
    private Intent intent;
    private String searchQuery;

    private SlidingTabLayout tabs;
    private ViewPager vpGroupContent;
    private CustomViewPagerAdapterGroups pagerAdapter;
    private CharSequence titles[] = {"Public", "Private", "New"};
    private int numberOfTabs = 3;

    private int backPressedCount = 0;
    private long lastBackPressedAt;

    public static ParseUser currentUser = ParseUser.getCurrentUser();
    public static SharedPreferences sharedPrefs;
    public static boolean userFresh = false;
    public static String jsonPushData = "";
    public static double pushLat;
    public static double pushLng;
    public static String pushPanicObjectId;

    private static Menu mActionBar;

    private SpotlightView spotvDrawer;
    private SpotlightView spotvGroups;

    //New Bottom Nav
    private int prevMenuItem = 0;
    private BottomNavigation bottomNavigation;
    private BottomNavigation.OnMenuItemSelectionListener mnuListener;
    public static FloatingActionButton fabMainAlert;

    private float btnMainPos1X, btnMainPos2X, btnMainPos3X, btnMainPos4X, btnMainPos5X;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        currentUser = ParseUser.getCurrentUser();
        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Set profile name in drawer
//        txtvProfileName = (TextView) findViewById(R.id.txtvProfileName);

//        if(currentUser != null)
//            txtvProfileName.setText(currentUser.getString("name"));
//        else
//            txtvProfileName.setVisibility(View.INVISIBLE);

        //Update current user
        currentUser.fetchInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                if(parseObject != null)
                {
                    userFresh = true;
//                    txtvProfileName.setText(parseObject.getString("name"));
//                    txtvProfileName.setVisibility(View.VISIBLE);
                }
            }
        });

        fLayBottomActionBar = (FrameLayout) findViewById(R.id.fragmentBottomActionBar);
        fabNeedleDrop = (FloatingActionButton) findViewById(R.id.fabNeedleDrop);
        bottomNavigation = (BottomNavigation) findViewById(R.id.BottomNavigation);
        fabMainAlert = (FloatingActionButton) findViewById(R.id.fabMainAlert);
        vDummyGroupTut = findViewById(R.id.vDummyGroupTut);
                //Initialise & populate nav drawer
//        drawLayNav = (DrawerLayout)findViewById(R.id.drawLayNav);
//        lstvNav = (ListView)findViewById(R.id.lstvNav);


        //Initialise custom adaptor
//        navAdapter = new NavAdapter(this);
//        lstvNav.setAdapter(navAdapter);
//        lstvNav.setOnItemClickListener(this);

//        drawerListener = new ActionBarDrawerToggle(this, drawLayNav, R.string.navDrawerOpen, R.string.navDrawerClosed)
//        {
//            @Override
//            public void onDrawerOpened(View drawerView) {
//                super.onDrawerOpened(drawerView);
//               // Toast.makeText(HomeActivity.this, "Drawer Opened", Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onDrawerClosed(View drawerView) {
//                super.onDrawerClosed(drawerView);
//                //Toast.makeText(HomeActivity.this, "Drawer Closed", Toast.LENGTH_LONG).show();
//            }
//        };

//        drawLayNav.setDrawerListener(drawerListener);
//        getSupportActionBar().setHomeButtonEnabled(true);
//        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mnuListener = new BottomNavigation.OnMenuItemSelectionListener()
        {
            @Override
            public void onMenuItemSelect(@IdRes int i, int position, boolean b)
            {

                //Calculate positions if any are 0
                if(btnMainPos1X == 0 || btnMainPos2X == 0 || btnMainPos3X == 0 || btnMainPos4X == 0 || btnMainPos5X == 0)
                    calcMainBtnScreenPositions();

                FragmentTransaction fragTrans = fragManager.beginTransaction();

                //Check position clicked
                switch(position)
                {
                    case 0:
                        translateViewToXPos(fabMainAlert, calcRelatveXPositionChange(fabMainAlert, btnMainPos1X));

                        //Swap fire drawable
                        fabMainAlert.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fire_color1));

                        //Pull in new relevant fragment
                        if(fragGroups == null)
                            fragGroups = new FragmentGroups();


                        //Set animation based on previously selected item, Have to set animation first otherwise it's not shown
                        switch(prevMenuItem)
                        {
                            case 1:
                                fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_left);
                                break;

                            case 2:
                                fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_push_out_down);
                                break;

                            case 3:
                            case 4:
                                fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
                                break;
                        }

                        fragTrans.replace(R.id.HomeContentLayout, fragGroups, TAG_FRAG_GROUPS);
                        fragTrans.commit();
                        prevMenuItem = 0;
                        break;

                    case 1:
                        translateViewToXPos(fabMainAlert, calcRelatveXPositionChange(fabMainAlert, btnMainPos2X));

                        //Swap fire drawable
                        fabMainAlert.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fire_color2));

                        //Pull in new relevant fragment
                        if(fragMap == null)
                            fragMap = new FragmentMap();


                        //Set animation based on previously selected item, Have to set animation first otherwise it's not shown
                        switch(prevMenuItem)
                        {
                            case 0:
                                fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_left);
                                break;

                            case 2:
                                fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_push_out_down);
                                break;

                            case 3:
                            case 4:
                                fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
                                break;
                        }

                        fragTrans.replace(R.id.HomeContentLayout, fragMap, TAG_FRAG_MAP);
                        fragTrans.commit();
                        prevMenuItem = 1;
                        break;

                    case 2:
                        translateViewToXPos(fabMainAlert, calcRelatveXPositionChange(fabMainAlert, btnMainPos3X));

                        //Swap fire drawable
                        fabMainAlert.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fire_color3));

                        //Pull in new relevant fragment
                        if(fragPanic == null)
                            fragPanic = new FragmentPanic();


                        //Set animation based on previously selected item, Have to set animation first otherwise it's not shown
                        switch(prevMenuItem)
                        {
                            case 0:
                            case 1:
                                fragTrans.setCustomAnimations(R.anim.anim_push_in_up, R.anim.anim_slide_out_to_left);
                                break;

                            case 3:
                            case 4:
                                fragTrans.setCustomAnimations(R.anim.anim_push_in_up, R.anim.anim_slide_out_to_right);
                                break;
                        }

                        fragTrans.replace(R.id.HomeContentLayout, fragPanic, TAG_FRAG_PANIC);
                        fragTrans.commit();
                        prevMenuItem = 2;
                        break;

                    case 3:
                        translateViewToXPos(fabMainAlert, calcRelatveXPositionChange(fabMainAlert, btnMainPos4X));

                        //Swap fire drawable
                        fabMainAlert.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fire_color4));

                        //Pull in new relevant fragment
                        if(fragHistory == null)
                            fragHistory = new FragmentHistory();

                        fragTrans = fragManager.beginTransaction();

                        //Set animation based on previously selected item, Have to set animation first otherwise it's not shown
                        switch(prevMenuItem)
                        {
                            case 0:
                            case 1:
                                fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                                break;

                            case 2:
                                fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_right, R.anim.anim_push_out_down);
                                break;

                            case 4:
                                fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_right);
                                break;
                        }

                        fragTrans.replace(R.id.HomeContentLayout, fragHistory, TAG_FRAG_HISTORY);
                        fragTrans.commit();
                        prevMenuItem = 3;
                        break;

                    case 4:
                        translateViewToXPos(fabMainAlert, calcRelatveXPositionChange(fabMainAlert, btnMainPos5X));

                        //Swap fire drawable
                        fabMainAlert.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fire_color5));

                        //Pull in new relevant fragment
                        if(fragSettingsNew == null)
                            fragSettingsNew = new FragmentSettingsNew();

                        fragTrans = fragManager.beginTransaction();

                                //Set animation based on previously selected item, Have to set animation first otherwise it's not shown
                                switch(prevMenuItem)
                                {
                                    case 0:
                                    case 1:
                                        fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_left);
                                        break;

                                    case 2:
                                        fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_right, R.anim.anim_push_out_down);
                                        break;

                                    case 3:
                                        fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_right, R.anim.anim_slide_out_to_right);
                                        break;
                                }

                        fragTrans.replace(R.id.HomeContentLayout, fragSettingsNew, TAG_FRAG_SETTINGS);
                        fragTrans.commit();
                        prevMenuItem = 4;
                        break;
                }
            }

            @Override
            public void onMenuItemReselect(@IdRes int i, int position, boolean b)
            {

            }
        };

        bottomNavigation.setOnMenuItemClickListener(mnuListener);

        fabMainAlert.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                bottomNavigation.setSelectedIndex(2, true);
            }
        });

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            initFragments();
        else
            reqRunTimePerms();

        bottomNavigation.setSelectedIndex(2, false);

        updateUserCountry();
    }

    private void initBtnMainAlertPosition()
    {
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float screenWidth = size.x;
        float screenCenterWidth = screenWidth / 2;
        float xWeightFactor = screenWidth / 5;   //This is the size of one fith of the screen's width

        fabMainAlert.setX(screenCenterWidth + xWeightFactor);
    }

    //Calculate Main middle button positions acording to screen size
    private void calcMainBtnScreenPositions()
    {
        //Get screen x size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        float screenWidth = size.x;
        float screenCenterWidth = screenWidth / 2;
//        Log.d("debugBottomNav", "screenWidth: " + screenWidth);
//        Log.d("debugBottomNav", "screenCenterWidth: " + screenCenterWidth);

        //Get button width
        float btnWidth = fabMainAlert.getWidth();
        float btnInitialPosX = fabMainAlert.getX();
        float btnCenterOffsetFactor = btnWidth / 2;   //This is the factor to subtract from an x value to place the view's center on the given x co-ordinate
//        Log.d("debugBottomNav", "btnWidth: " + btnWidth);
//        Log.d("debugBottomNav", "btnInitialPosX: " + btnInitialPosX);
//        Log.d("debugBottomNav", "btnCenterOffsetWidth: " + btnCenterOffsetFactor);

        //Divide the screen in 5 since there is 5 buttons on the nav bar
        float xWeightFactor = screenWidth / 6;   //This is the size of one fith of the screen's width
//        Log.d("debugBottomNav", "xWeight5th: " + xWeightFactor);

        //Calculate position 1 this is when item 1 on the menu is selected
        //So we need to place the view the furthest past it's center position that we are willing to go
        //The view needs to be pushed to the right
        btnMainPos1X = (screenCenterWidth + (xWeightFactor / 2.5f)) - btnCenterOffsetFactor;
//        Log.d("debugBottomNav", "Position1X: " + btnMainPos1X);

        //Calculate position 2, this would be the second most pushed to the right
        btnMainPos2X = (screenCenterWidth + (xWeightFactor / 4f)) - btnCenterOffsetFactor;
//        Log.d("debugBottomNav", "Position2X: " + btnMainPos2X);

        //Calculate position 3, this would essentially place the butotn in the center
        btnMainPos3X = screenCenterWidth - btnCenterOffsetFactor;
//        Log.d("debugBottomNav", "Position3X: " + btnMainPos3X);

        //Calculate position 4, this is when the 4th button is selected and the second most pushed to the left
        btnMainPos4X = (screenCenterWidth - (xWeightFactor / 4.2f)) - btnCenterOffsetFactor;
//        Log.d("debugBottomNav", "Position4X: " + btnMainPos4X);

        //Calculate position 5, this is when the 5th button is selected and the most pushed to the left
        btnMainPos5X = (screenCenterWidth - (xWeightFactor / 2.8f)) - btnCenterOffsetFactor;
//        Log.d("debugBottomNav", "Position5X: " + btnMainPos5X);
    }

    private float calcRelatveXPositionChange(View view , float absolutePosition)
    {
        float relativePosition = view.getX();
        return absolutePosition - relativePosition;
    }

    private void translateViewToXPos(View view, float byX)
    {
        view.animate().translationXBy(byX).setInterpolator(new AccelerateDecelerateInterpolator()).setDuration(150).start();
    }

    private void initFragments()
    {
        //Init fragements
        fragPanic = new FragmentPanic();
//        fragMap = new FragmentMap();
//        fragGroups = new FragmentGroupsOld();
//        fragSettings = new FragmentSettings();
//        fragHistory = new FragmentHistory();
//        fragBottomActionBar = new FragmentBottomActionBar();
//        fragMangGroups = new FragmentManageGroups();


        //Add fragPanic and all other frags on start up

        fragManager = getSupportFragmentManager();
        fragTransaction = fragManager.beginTransaction();
//        fragTransaction.add(R.id.fragmentBottomActionBar, fragBottomActionBar, TAG_FRAG_BOTTOM_ACTION_BAR);
        fragTransaction.add(R.id.HomeContentLayout, fragPanic, TAG_FRAG_PANIC);

//        fragTransaction.add(R.id.HomeContentLayout, fragMap, TAG_FRAG_MAP);
//        fragTransaction.hide(fragMap);

        /*fragTransaction.add(R.id.HomeContentLayout,fragGroups, TAG_FRAG_GROUPS);
        fragTransaction.hide(fragGroups);*/

        /*fragTransaction.add(R.id.HomeContentLayout, fragMangGroups, TAG_FRAG_MANG_GROUPS);
        fragTransaction.hide(fragMangGroups);*/

        /*fragTransaction.add(R.id.HomeContentLayout, fragHistory, TAG_FRAG_HISTORY);
        fragTransaction.hide(fragHistory);*/

        /*fragTransaction.add(R.id.HomeContentLayout, fragSettings, TAG_FRAG_SETTINGS);
        fragTransaction.hide(fragSettings);*/

        fragTransaction.commitAllowingStateLoss();

        //initFbPush();
    }

    private void reqRunTimePerms()
    {
        // Should we show an explanation?
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION))
        {
            // Show an explanation to the user
            //Explain to user that loc is required to use the app
            buildLocPermExplainDiag();
        }
        else
        {
            // No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERM_LOC);
        }
    }

    private void buildLocPermExplainDiag()
    {
        final Activity activity = this;
        final android.support.v7.app.AlertDialog.Builder diagbuilder = new android.support.v7.app.AlertDialog.Builder(this);
        diagbuilder.setTitle("Location Permissions");
        diagbuilder.setMessage("Your location is central to the way Brave works and notifies others of your emergencies.");
        diagbuilder.setNegativeButton("Quit", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                //Close app if user won't grant location permissions
                finish();
            }
        });
        diagbuilder.setPositiveButton("Got it", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
                dialogInterface.dismiss();

                // Request permission
                ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQ_PERM_LOC);
            }
        });
        diagbuilder.create().show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        switch(requestCode)
        {
            case REQ_PERM_LOC:
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, init any functionality that require loc perms
                    initFragments();
                }
                else
                {
                    // permission denied, app can;t function without loc perm so prompt should close?
                    buildLocPermExplainDiag();
                }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

//    public void showDrawTut()
//    {
////        //Open drawer
////        drawLayNav.openDrawer(Gravity.LEFT);
//
//        spotvDrawer =  new SpotlightView.Builder(this)
//                .introAnimationDuration(400)
//                .enableRevealAnimation(true)
//                .performClick(true)
//                .fadeinTextDuration(400)
//                .headingTvColor(ContextCompat.getColor(this, R.color.SeaGreen))
//                .headingTvSize(24)
//                .headingTvText("Menu")
//                .subHeadingTvColor(ContextCompat.getColor(this, R.color.White))
//                .subHeadingTvSize(14)
//                .subHeadingTvText("Tap here to access the menu, or swipe from the left edge of the screen")
//                .maskColor(Color.parseColor("#dc000000"))
//                .targetPadding(165)
//                .target()
//                .lineAnimDuration(400)
//                .lineAndArcColor(ContextCompat.getColor(this, R.color.SeaGreen))
//                .usageId(navAdapter.getGroupView().getId() + "") //UNIQUE ID
//                .dismissOnBackPress(true)
//                .dismissOnTouch(true)
//                .show();
//
//        final HomeActivity activity = this;
//
//        spotvDrawer.setListener(new SpotlightListener()
//        {
//            @Override
//            public void onUserClicked(String s)
//            {
//                spotvGroups =  new SpotlightView.Builder(activity)
//                        .introAnimationDuration(400)
//                        .enableRevealAnimation(true)
//                        .performClick(true)
//                        .fadeinTextDuration(400)
//                        .headingTvColor(ContextCompat.getColor(activity, R.color.SeaGreen))
//                        .headingTvSize(24)
//                        .headingTvText("Groups")
//                        .subHeadingTvColor(ContextCompat.getColor(activity, R.color.White))
//                        .subHeadingTvSize(14)
//                        .subHeadingTvText("Tap groups to join or create groups to be notified of your emergencies")
//                        .maskColor(Color.parseColor("#dc000000"))
//                        .target(lstvNav)
//                        .lineAnimDuration(400)
//                        .lineAndArcColor(ContextCompat.getColor(activity, R.color.SeaGreen))
//                        .targetPadding(-125)
//                        .usageId(lstvNav.getId() + "") //UNIQUE ID
//                        .dismissOnBackPress(true)
//                        .dismissOnTouch(true)
//                        .show();
//            }
//        });
//    }

    public void showGroupTut()
    {
//        Open drawer
//        drawLayNav.openDrawer(Gravity.LEFT);

        spotvGroups =  new SpotlightView.Builder(this)
                .introAnimationDuration(400)
                .enableRevealAnimation(true)
                .performClick(true)
                .fadeinTextDuration(400)
                .headingTvColor(ContextCompat.getColor(this, R.color.SeaGreen))
                .headingTvSize(24)
                .headingTvText("Groups")
                .subHeadingTvColor(ContextCompat.getColor(this, R.color.White))
                .subHeadingTvSize(14)
                .subHeadingTvText("Tap groups to join or create groups to be notified of your emergencies")
                .maskColor(Color.parseColor("#dc000000"))
                .target(vDummyGroupTut)
                .lineAnimDuration(400)
                .lineAndArcColor(ContextCompat.getColor(this, R.color.SeaGreen))
                .usageId(vDummyGroupTut.getId() + "") //UNIQUE ID
                .dismissOnBackPress(true)
                .dismissOnTouch(true)
                .show();
    }

    private void initFbPush()
    {
        boolean forceRefresh = false;
        //Save firebase token to parse server if doesn't exist
        ParseInstallation instObj = ParseInstallation.getCurrentInstallation();
        if(instObj.getString("firebaseID") == null || instObj.getString("firebaseID").isEmpty() || forceRefresh)
        {
            instObj.put("firebaseID", FirebaseInstanceId.getInstance().getToken());
            instObj.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if(e == null)
                    {
                        Log.d("fbPushDebug", "Parse FB Push Token Refreshed: " + FirebaseInstanceId.getInstance().getToken());
                    }
                    else
                    {
                        Log.d("fbPushDebug", "Parse FB Push Token Refresh FAILED: " + e.getCode() + " Messages: " + e.getMessage());
                    }
                }
            });
        }
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        if(Intent.ACTION_SEARCH.equals(intent.getAction()))
        {
            searchQuery = intent.getStringExtra(SearchManager.QUERY);
            //do search
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState)
    {
        super.onPostCreate(savedInstanceState);
//        drawerListener.syncState();

//        //Check if app opened vai push notification
//        if(getIntent().hasExtra("jsonPushData"))
//        {
//            jsonPushData = getIntent().getStringExtra("jsonPushData");
////            fragBottomActionBar.btnNavMap.performClick();
////            bottomNavigation.setSelectedIndex(1, true);
////            mnuListener.onMenuItemSelect(0, 1, true);
//            Log.i("Home", "Data read in home activity: " + jsonPushData);
//        }

        Intent startingIntent = getIntent();
        if (startingIntent != null)
        {
            Log.d("debug", "Trying to set lat and lng from notif: " + startingIntent.hasExtra("lat"));

            Bundle bundle = startingIntent.getExtras();
//            if (bundle != null) {
//                for (String key : bundle.keySet()) {
//                    Object value = bundle.get(key);
//                    Log.d(TAG, String.format("%s %s (%s)", key,
//                            value.toString(), value.getClass().getName()));
//                }
//            }
            if(!bundle.isEmpty())
            {
                pushPanicObjectId = bundle.getString("objectId", "");
                if(!pushPanicObjectId.isEmpty())
                {
                    pushLat = Double.parseDouble(bundle.getString("lat", "0"));
                    pushLng = Double.parseDouble(bundle.getString("lng", "0"));


                    Log.d("debug", "Managed to set cords from notif");
                    bottomNavigation.setSelectedIndex(1, false);

                    FragmentTransaction fragTrans = fragManager.beginTransaction();

                    translateViewToXPos(fabMainAlert, calcRelatveXPositionChange(fabMainAlert, btnMainPos2X));

                    //Swap fire drawable
                    fabMainAlert.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_fire_color2));

                    //Pull in new relevant fragment
                    if (fragMap == null)
                        fragMap = new FragmentMap();


                    //Set animation based on previously selected item, Have to set animation first otherwise it's not shown
                    switch (prevMenuItem)
                    {
                        case 0:
                            fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_left);
                            break;

                        case 2:
                            fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_push_out_down);
                            break;

                        case 3:
                        case 4:
                            fragTrans.setCustomAnimations(R.anim.anim_slide_in_from_left, R.anim.anim_slide_out_to_right);
                            break;
                    }

                    fragTrans.replace(R.id.HomeContentLayout, fragMap, TAG_FRAG_MAP);
                    fragTrans.commit();
                    prevMenuItem = 1;
                }

            }
        }
    }

    //TODO: Uncomment to get share and feedback functionallity back
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //Add share button to action bar
        menu.add(Menu.NONE, 0, 0, "Share").setIcon(R.drawable.abc_ic_menu_share_mtrl_alpha).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        //Add feedback button
        menu.add(Menu.NONE, 1, 1, "Feedback").setIcon(R.drawable.ic_feedback).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        mActionBar = menu;

        return true;
    }

    public void adjustActionBarButtonsVisibility(boolean visible)
    {
//        mActionBar.findItem(0).setVisible(visible);
//        mActionBar.findItem(1).setVisible(visible);
//        this.invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        //Catch feed back button click
        if(item.getItemId() == 0)
        {
            //Start sharing intent with app url
//            Intent share = new Intent(android.content.Intent.ACTION_SEND);
//            share.setType("text/plain");
//            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//
//            // Add data to the intent, the receiving app will decide
//            // what to do with it.
//            share.putExtra(Intent.EXTRA_SUBJECT, "Panic Crowd Sourced Security");
//            share.putExtra(Intent.EXTRA_TEXT, "http://www.panic-sec.org/what/");
//
//            startActivity(Intent.createChooser(share, "Share Panic"));
            FragmentDialogShareApp diagShareApp = new FragmentDialogShareApp();
            diagShareApp.show(getSupportFragmentManager(), "diagShareApp");
        }

        if(item.getItemId() == 1)
        {
            FragmentDialogFeedback diagFeedback = new FragmentDialogFeedback();
            diagFeedback.show(getSupportFragmentManager(), "diagFeedback");
        }

//        //Make nav bar icon open or close nav drawer
//        if(drawerListener.onOptionsItemSelected(item))
//            return true;
//        else
            return super.onOptionsItemSelected(item);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        super.onConfigurationChanged(newConfig);
        drawerListener.onConfigurationChanged(newConfig);   //Informs drawer listener of config changes like screen size
    }

    public void onClickPublicHistory(View view)
    {
        Toast.makeText(this, "Show public history", Toast.LENGTH_LONG).show();
    }

    public void onClickTelephone(View view)
    {
        Toast.makeText(this, "Show emergancy dailer maybe???...", Toast.LENGTH_LONG).show();
    }

    public void onClickLogout(View view)
    {
        //unsub installation from push channels
        List<String> subbedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
        ParseInstallation.getCurrentInstallation().removeAll("channels", subbedChannels);
        ParseInstallation.getCurrentInstallation().saveInBackground();

        ParseUser.logOut();
        Intent intentLogout = new Intent(this, LoginActivity.class);
        intentLogout.putExtra("initParse", false);
        startActivity(intentLogout);
        this.finish();
    }

    public void onClickManageGroups(View view)
    {

        //Check no of groups remaining
//        if(Integer.parseInt(FragmentGroupsOld.txtvGroupsRemainingValue.getText().toString()) > 0)
//        {
//
//            //        if(fragMangGroups == null) //Sliding tab does not display correctly if fragment is not recreated for some reason
//            fragMangGroups = new FragmentManageGroups();
//
//            fragTransaction = fragManager.beginTransaction();
//            fragTransaction.hide(fragGroups);
//            fragTransaction.add(R.id.HomeContentLayout, fragMangGroups, TAG_FRAG_MANG_GROUPS);
//            fragTransaction.addToBackStack(null);
//            fragTransaction.commit();
//
//            //Setup group management frag
//            /*intent = getIntent();
//
//            if(Intent.ACTION_SEARCH.equals(intent.getAction()))
//            {
//                searchQuery = intent.getStringExtra(SearchManager.QUERY);
//                //do search
//            }*/
//        }
//        else
//        {
//            //allow for purchase here
//            noGroupsLeftDialog();
//        }
    }

    public void noGroupsLeftDialog()
    {
        //Prompt user if they are sure
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You have no more groups left, please leave a group your are subscribed too.\n\nPurchasing of extra groups coming soon.").setCancelable(false)
                .setTitle("No more groups left")
                .setNegativeButton("Got It", new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                    {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed()
    {
        if(fragManager.getBackStackEntryCount() > 0)
        {
            fragManager.popBackStack();
            backPressedCount = 0;
        }
        else if(backPressedCount > 0 && ((SystemClock.elapsedRealtime() - lastBackPressedAt) < 3000))
        {
            finish();
        }
        else
        {
            backPressedCount++;
            lastBackPressedAt = SystemClock.elapsedRealtime();
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }

    }

    public void lockDrawer(boolean locked)
    {

//        if(locked)
//        {
//            drawLayNav.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
//            getSupportActionBar().setHomeButtonEnabled(false);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//        }
//        else
//        {
//            drawLayNav.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
//            getSupportActionBar().setHomeButtonEnabled(true);
//            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id)
    {
        if(view.getTag().toString().equalsIgnoreCase("Home"))
        {
            fragTransaction = fragManager.beginTransaction();

            if(fragManager.findFragmentByTag(TAG_FRAG_MANG_GROUPS) != null)
            {
//                fragManager.popBackStack(); //pop back stack if mang groups was opened otherwise blank frag is shown when returning to groups if naving straight from mang groups else where
                fragTransaction.remove(fragMangGroups);
            }

            if(fragManager.findFragmentByTag(TAG_FRAG_GROUPS) != null)
                fragTransaction.remove(fragGroups);

            if(fragManager.findFragmentByTag(TAG_FRAG_HISTORY) != null)
                fragTransaction.remove(fragHistory);

            if(fragManager.findFragmentByTag(TAG_FRAG_SETTINGS) != null)
                fragTransaction.remove(fragSettings);

            fragTransaction.show(fragPanic);
            fragTransaction.hide(fragMap);
            fragTransaction.show(fragBottomActionBar);
            fragTransaction.commit();

            //Reset bottom action bar button highlights
            fragBottomActionBar.resetButtons();

            fLayBottomActionBar.setVisibility(View.VISIBLE);

            this.setTitle("Home");

            //Deal with selected color
            if(!navSelectedEntryTag.equalsIgnoreCase("Home"))
            {
                //Set selected item color
                TextView txtvEntry = (TextView) view.findViewById(R.id.txtvEntry);
                txtvEntry.setTextColor(getResources().getColor(R.color.SelectedWhite));

                //Reset previous selected color
                View prevSelected = (View) parent.findViewWithTag(navSelectedEntryTag);
                TextView txtvPrevSelectedEntry = (TextView) prevSelected.findViewById(R.id.txtvEntry);
                txtvPrevSelectedEntry.setTextColor(getResources().getColor(R.color.HintGrey));

                navSelectedEntryTag = "Home";
            }

            drawLayNav.closeDrawers();
        }
        else if(view.getTag().toString().equalsIgnoreCase("Groups"))
        {
            //Deal with selected color & do not re add frag if it is already added and visible
            if(!navSelectedEntryTag.equalsIgnoreCase("Groups"))
            {
                if(fragGroups == null)
                {
                    Log.d(TAG, "Fragment groups was null and is being created now");
                    fragGroups = new FragmentGroups();
                }

                fragTransaction = fragManager.beginTransaction();

               /* fragTransaction.show(fragGroups);
                fragTransaction.hide(fragMangGroups);
                fragTransaction.hide(fragHistory);
                fragTransaction.hide(fragSettings);
                fragTransaction.hide(fragPanic);
                fragTransaction.hide(fragMap);*/
    //            fragTransaction.hide(fragBottomActionBar);
                fragTransaction.add(R.id.HomeContentLayout, fragGroups, TAG_FRAG_GROUPS);   //Add frag to display
                //Hide Home frags
                fragTransaction.hide(fragPanic);
                fragTransaction.hide(fragMap);
                //Remove other possible frags
                if(fragManager.findFragmentByTag(TAG_FRAG_MANG_GROUPS) != null)
                    fragTransaction.remove(fragMangGroups);

                if(fragManager.findFragmentByTag(TAG_FRAG_HISTORY) != null)
                    fragTransaction.remove(fragHistory);

                if(fragManager.findFragmentByTag(TAG_FRAG_SETTINGS) != null)
                    fragTransaction.remove(fragSettings);

                fragTransaction.commit();

                fLayBottomActionBar.setVisibility(View.GONE);

                this.setTitle("Groups");


                //Set selected item color
                TextView txtvEntry = (TextView) view.findViewById(R.id.txtvEntry);
                txtvEntry.setTextColor(getResources().getColor(R.color.SelectedWhite));

                //Reset previous selected color
                View prevSelected = (View) parent.findViewWithTag(navSelectedEntryTag);
                TextView txtvPrevSelectedEntry = (TextView) prevSelected.findViewById(R.id.txtvEntry);
                txtvPrevSelectedEntry.setTextColor(getResources().getColor(R.color.HintGrey));

                navSelectedEntryTag = "Groups";
            }

            drawLayNav.closeDrawers();
        }
        else if(view.getTag().toString().equalsIgnoreCase("History"))
        {
            //Deal with selected color & do not re add frag if it is already added and visible
            if(!navSelectedEntryTag.equalsIgnoreCase("History"))
            {
    //            if(fragHistory == null)   //Sliding tab does not display correctly if fragment is not recreated for some reason
                    fragHistory = new FragmentHistory();

                fragTransaction = fragManager.beginTransaction();
               /* fragTransaction.hide(fragGroups);
                fragTransaction.hide(fragMangGroups);
                fragTransaction.show(fragHistory);
                fragTransaction.hide(fragSettings);
                fragTransaction.hide(fragPanic);
                fragTransaction.hide(fragMap);
                fragTransaction.hide(fragBottomActionBar);*/

                //Add frag to view
                fragTransaction.add(R.id.HomeContentLayout, fragHistory, TAG_FRAG_HISTORY);
                //Hide home frags
                fragTransaction.hide(fragPanic);
                fragTransaction.hide(fragMap);
                //Remove other possible frags

                if(fragManager.findFragmentByTag(TAG_FRAG_MANG_GROUPS) != null)
                {
//                    fragManager.popBackStack(); //pop back stack if mang groups was opened otherwise blank frag is shown when returning to groups if naving straight from mang groups else where
                    fragTransaction.remove(fragMangGroups);
                }

                if(fragManager.findFragmentByTag(TAG_FRAG_GROUPS) != null)
                    fragTransaction.remove(fragGroups);

                if(fragManager.findFragmentByTag(TAG_FRAG_SETTINGS) != null)
                    fragTransaction.remove(fragSettings);

                fragTransaction.commit();

                fLayBottomActionBar.setVisibility(View.GONE);

                this.setTitle("History");


                //Set selected item color
                TextView txtvEntry = (TextView) view.findViewById(R.id.txtvEntry);
                txtvEntry.setTextColor(getResources().getColor(R.color.SelectedWhite));

                //Reset previous selected color
                View prevSelected = (View) parent.findViewWithTag(navSelectedEntryTag);
                TextView txtvPrevSelectedEntry = (TextView) prevSelected.findViewById(R.id.txtvEntry);
                txtvPrevSelectedEntry.setTextColor(getResources().getColor(R.color.HintGrey));

                navSelectedEntryTag = "History";
            }

            drawLayNav.closeDrawers();
        }
        else if(view.getTag().toString().equalsIgnoreCase("Settings"))
        {
            //Deal with selected color & do not re add frag if it is already added and visible
            if(!navSelectedEntryTag.equalsIgnoreCase("Settings"))
            {
                if(fragSettings == null)
                    fragSettings = new FragmentSettings();

                fragTransaction = fragManager.beginTransaction();
                /*fragTransaction.hide(fragGroups);
                fragTransaction.hide(fragMangGroups);
                fragTransaction.hide(fragHistory);
                fragTransaction.show(fragSettings);
                fragTransaction.hide(fragPanic);
                fragTransaction.hide(fragMap);
                fragTransaction.hide(fragBottomActionBar);*/

                //Add frag to view
                fragTransaction.add(R.id.HomeContentLayout, fragSettings, TAG_FRAG_SETTINGS);
                //Hide home frags
                fragTransaction.hide(fragPanic);
                fragTransaction.hide(fragMap);
                //Remove possible existing frags
                if(fragManager.findFragmentByTag(TAG_FRAG_MANG_GROUPS) != null)
                {
//                    fragManager.popBackStack(); //pop back stack if mang groups was opened otherwise blank frag is shown when returning to groups if naving straight from mang groups else where
                    fragTransaction.remove(fragMangGroups);
                }

                if(fragManager.findFragmentByTag(TAG_FRAG_GROUPS) != null)
                    fragTransaction.remove(fragGroups);

                if(fragManager.findFragmentByTag(TAG_FRAG_HISTORY) != null)
                    fragTransaction.remove(fragHistory);

                fragTransaction.commit();

                fLayBottomActionBar.setVisibility(View.GONE);

                this.setTitle("Settings");

                //Set selected item color
                TextView txtvEntry = (TextView) view.findViewById(R.id.txtvEntry);
                txtvEntry.setTextColor(getResources().getColor(R.color.SelectedWhite));

                //Reset previous selected color
                View prevSelected = (View) parent.findViewWithTag(navSelectedEntryTag);
                TextView txtvPrevSelectedEntry = (TextView) prevSelected.findViewById(R.id.txtvEntry);
                txtvPrevSelectedEntry.setTextColor(getResources().getColor(R.color.HintGrey));

                navSelectedEntryTag = "Settings";
            }

            drawLayNav.closeDrawers();
        }
        else
            Toast.makeText(this, "Nav button clicked: " + view.getTag().toString(), Toast.LENGTH_LONG).show();

    }

    //Updates the user's country if needed
    private void updateUserCountry()
    {
        //Check the users country if it has changed update their settings
        String currUserCountry = currentUser.getString("country");

        //Get device's country
        String currPhoneCountryCode = getPhoneCountry(this);
        String currPhoneCountry = new Locale("", currPhoneCountryCode).getDisplayName();
        if(currUserCountry == null || !currUserCountry.equalsIgnoreCase(currPhoneCountry))
        {
            //Update country
            currentUser.put("country", currPhoneCountry);
            currentUser.saveInBackground();
        }
    }

    /**
     * Get ISO 3166-1 alpha-2 country code for this device (or null if not available)
     * @param context Context reference to get the TelephonyManager instance from
     * @return country code or null
     */
    public static String getPhoneCountry(Context context)
    {
        try
        {
            final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            final String simCountry = tm.getSimCountryIso();
            if (simCountry != null && simCountry.length() == 2)
            { // SIM country code is available
                return simCountry.toLowerCase(Locale.US);
            }
            else if (tm.getPhoneType() != TelephonyManager.PHONE_TYPE_CDMA)
            { // device is not 3G (would be unreliable)
                String networkCountry = tm.getNetworkCountryIso();
                if (networkCountry != null && networkCountry.length() == 2)
                { // network country code is available
                    return networkCountry.toLowerCase(Locale.US);
                }
            }
        }
        catch (Exception e) { }
        return null;
    }
}

//Custom adaptor to populate navigation drawer
class NavAdapter extends BaseAdapter
{
    private Context context;
    private String[] navEntries;    //Entries for the navigation menu
    private TypedArray navIcons;     //Icon ids for navigation menu
    private View vGroupsRef;

    public NavAdapter(Context context)
    {
        this.context = context;
        navEntries = context.getResources().getStringArray(R.array.navEntries);
        navIcons = context.getResources().obtainTypedArray(R.array.navIcons);
    }

    @Override
    public int getCount() {
        return navEntries.length;
    }

    @Override
    public Object getItem(int position) {
        return navEntries[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View navRow = null;

        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);

            navRow = inflater.inflate(R.layout.custom_nav_drawer_row, parent, false);
        }
        else
        {
            navRow = convertView;
        }

        TextView txtvEntry = (TextView) navRow.findViewById(R.id.txtvEntry);
        ImageView imgvIcon = (ImageView) navRow.findViewById(R.id.imgvIcon);

        txtvEntry.setText(navEntries[position]);

        //If Home set as selected
        if(navEntries[position].equalsIgnoreCase("Home"))
            txtvEntry.setTextColor(context.getResources().getColor(R.color.SelectedWhite));
        else
            txtvEntry.setTextColor(context.getResources().getColor(R.color.HintGrey));

        imgvIcon.setImageResource(navIcons.getResourceId(position, -1));

        navRow.setTag(navEntries[position]);

        //Get ref to groups view will need it for tut purposes later
        if(position == 1)
            vGroupsRef = navRow;

        return navRow;
    }

    public View getGroupView(){return vGroupsRef;}
}
