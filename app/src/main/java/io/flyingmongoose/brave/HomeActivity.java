package io.flyingmongoose.brave;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.net.Uri;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
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

import java.util.List;


public class HomeActivity extends AppCompatActivity implements AdapterView.OnItemClickListener
{
    public static final String PARSE_APP_ID = "PANICING-TURTLE";
    public static final String PARSE_API_KEY = "PANICINGTURTLE3847TR386TB281XN1NY7YNXM";
    private final String LOG_TAG = "HomeActivity";
    private DrawerLayout drawLayNav;
    private ListView lstvNav;
    private ActionBarDrawerToggle drawerListener;   //This is used because it already implements drawer listener
    private NavAdapter navAdapter;      //Custom adapter for populating nav menu
    public static TextView txtvProfileName;

    //fragments
    public  FragmentPanic fragPanic = null;
    public  static FragmentMap fragMap = null;
    public FragmentGroups fragGroups = null;
    public  FragmentSettings fragSettings = null;
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
    public boolean userFresh = false;
    public static String jsonPushData = "";

    private static Menu mActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        currentUser = ParseUser.getCurrentUser();

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Set profile name in drawer
        txtvProfileName = (TextView) findViewById(R.id.txtvProfileName);

        if(currentUser != null)
            txtvProfileName.setText(currentUser.getString("name"));
        else
            txtvProfileName.setVisibility(View.INVISIBLE);

        //Update current user
        currentUser.fetchInBackground(new GetCallback<ParseObject>()
        {
            @Override
            public void done(ParseObject parseObject, ParseException e)
            {
                if(parseObject != null)
                {
                    userFresh = true;
                    txtvProfileName.setText(parseObject.getString("name"));
                    txtvProfileName.setVisibility(View.VISIBLE);
                }
            }
        });

        fLayBottomActionBar = (FrameLayout) findViewById(R.id.fragmentBottomActionBar);
        fabNeedleDrop = (FloatingActionButton) findViewById(R.id.fabNeedleDrop);

        //Initialise & populate nav drawer
        drawLayNav = (DrawerLayout)findViewById(R.id.drawLayNav);
        lstvNav = (ListView)findViewById(R.id.lstvNav);


        //Initialise custom adaptor
        navAdapter = new NavAdapter(this);
        lstvNav.setAdapter(navAdapter);
        lstvNav.setOnItemClickListener(this);

        drawerListener = new ActionBarDrawerToggle(this, drawLayNav, R.string.navDrawerOpen, R.string.navDrawerClosed)
        {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
               // Toast.makeText(HomeActivity.this, "Drawer Opened", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                //Toast.makeText(HomeActivity.this, "Drawer Closed", Toast.LENGTH_LONG).show();
            }
        };

        drawLayNav.setDrawerListener(drawerListener);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Init fragements
        fragPanic = new FragmentPanic();
        fragMap = new FragmentMap();
//        fragGroups = new FragmentGroupsOld();
//        fragSettings = new FragmentSettings();
//        fragHistory = new FragmentHistory();
        fragBottomActionBar = new FragmentBottomActionBar();
//        fragMangGroups = new FragmentManageGroups();


        //Add fragPanic and all other frags on start up

        fragManager = getSupportFragmentManager();
        fragTransaction = fragManager.beginTransaction();
        fragTransaction.add(R.id.fragmentBottomActionBar, fragBottomActionBar, TAG_FRAG_BOTTOM_ACTION_BAR);
        fragTransaction.add(R.id.HomeContentLayout, fragPanic, TAG_FRAG_PANIC);

        fragTransaction.add(R.id.HomeContentLayout, fragMap, TAG_FRAG_MAP);
        fragTransaction.hide(fragMap);

        /*fragTransaction.add(R.id.HomeContentLayout,fragGroups, TAG_FRAG_GROUPS);
        fragTransaction.hide(fragGroups);*/

        /*fragTransaction.add(R.id.HomeContentLayout, fragMangGroups, TAG_FRAG_MANG_GROUPS);
        fragTransaction.hide(fragMangGroups);*/

        /*fragTransaction.add(R.id.HomeContentLayout, fragHistory, TAG_FRAG_HISTORY);
        fragTransaction.hide(fragHistory);*/

        /*fragTransaction.add(R.id.HomeContentLayout, fragSettings, TAG_FRAG_SETTINGS);
        fragTransaction.hide(fragSettings);*/

        fragTransaction.commit();

        initFbPush();
    }

    private void initFbPush()
    {
        //Save firebase token to parse server if doesn't exist
        ParseInstallation instObj = ParseInstallation.getCurrentInstallation();
        if(instObj.getString("firebaseID") == null || instObj.getString("firebaseID").isEmpty())
        {
            instObj.put("firebaseID", FirebaseInstanceId.getInstance().getToken());
            instObj.saveInBackground(new SaveCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    Log.d("fbPushDebug", "Parse FB Push Token Refreshed: " + FirebaseInstanceId.getInstance().getToken());
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
        drawerListener.syncState();

        //Check if app opened vai push notification
        if(getIntent().hasExtra("jsonPushData"))
        {
            jsonPushData = getIntent().getStringExtra("jsonPushData");
            fragBottomActionBar.btnNavMap.performClick();
            Log.i("Home", "Data read in home activity: " + jsonPushData);
        }
    }

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
        mActionBar.findItem(0).setVisible(visible);
        mActionBar.findItem(1).setVisible(visible);
        this.invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {

        //Catch feed back button click
        if(item.getItemId() == 0)
        {
            //Start sharing intent with app url
            Intent share = new Intent(android.content.Intent.ACTION_SEND);
            share.setType("text/plain");
            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

            // Add data to the intent, the receiving app will decide
            // what to do with it.
            share.putExtra(Intent.EXTRA_SUBJECT, "Panic Crowd Sourced Security");
            share.putExtra(Intent.EXTRA_TEXT, "http://www.panic-sec.org/what/");

            startActivity(Intent.createChooser(share, "Share Panic"));
        }

        if(item.getItemId() == 1)
        {
            //Get app version
            PackageInfo pInfo = null;
            String version = "";
            try
            {
                pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
                version = pInfo.versionName;
            } catch (PackageManager.NameNotFoundException e)
            {
                e.printStackTrace();
            }

            //Present feedback e mail
            //Report user
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@panic-sec.org", null));
            intent.putExtra(Intent.EXTRA_SUBJECT, "Feedback on Panic v" + version );
            intent.putExtra(Intent.EXTRA_TEXT, "We would like to thank you for your support, your feedback is valuable to us. \nWe use it to enhance your experience, so feel free to make suggestions.\n\nSo what would you like to tell us?\n\n ");

            startActivity(Intent.createChooser(intent, "Send Feedback"));
        }

        //Make nav bar icon open or close nav drawer
        if(drawerListener.onOptionsItemSelected(item))
            return true;
        else
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

        if(locked)
        {
            drawLayNav.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            getSupportActionBar().setHomeButtonEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        else
        {
            drawLayNav.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            getSupportActionBar().setHomeButtonEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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
}

//Custom adaptor to populate navigation drawer
class NavAdapter extends BaseAdapter
{
    private Context context;
    private String[] navEntries;    //Entries for the navigation menu
    private TypedArray navIcons;     //Icon ids for navigation menu

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

        return navRow;
    }
}
