package io.flyingmongoose.brave;

import android.*;
import android.Manifest;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.TransitionDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.SaveCallback;
import com.wooplr.spotlight.SpotlightView;
import com.wooplr.spotlight.utils.SpotlightListener;

import java.text.Normalizer;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by IC on 1/27/2015.
 */
public class FragmentPanic extends Fragment implements View.OnClickListener, View.OnFocusChangeListener, TextView.OnEditorActionListener
{
    private Context context;
    private ImageButton ibtnPanic;
    private FloatingActionButton fabNeedleDrop;
    private FloatingActionButton fabTestPush;
    private EditText etxtPanicDesc;
    private LinearLayout linLayRespondes;
    public RelativeLayout relLayPanicRoot;
    private boolean panicing = false;
    public ServiceGps gpsService;
    private boolean isServiceConnected = false;
    private boolean GpsServiceIsBound = false;
    public static String panicDetails = "";
    private TransitionDrawable transPanicBtn;
    private int TRANSITION_TIME = 500;
    private TextView txtvNoOfResponders;
    private SpotlightView spotvPanicButton;
    private SpotlightView spotvNeedleDropButton;

    private ScheduledExecutorService worker = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?>  delayPanicTimer;
    private boolean skipTurnOffPanicService = false;
    private final long PANIC_TIMER_START_DELAY = 0;
    private final int REQ_PERM_LOC = 100;
    private boolean awaitingNeedleDropCallback = false;

    private final String TAG = "FragmentPanic";
    private HomeActivity activity;

    public static ParseObject panicObj;

    //Required to bind to service, allows access to service methods
    private ServiceConnection myServiceConnection = new ServiceConnection()
    {

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            GpsServiceIsBound = false;
            isServiceConnected = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            Log.i("Connecting service: ", "attempting to connect");
            ServiceGps.GpsLocalBinder binder = (ServiceGps.GpsLocalBinder) service;
            gpsService = binder.getService();
            GpsServiceIsBound = true;
            isServiceConnected = true;
            gpsService.slowTrack(true, 5000, 10);
            Log.i("Connecting service: ", "Service connected");
        }
    };

    public FragmentPanic()
    {
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_panic_layout, container, false);

        ibtnPanic = (ImageButton) view.findViewById(R.id.ibtnPanic);
        ibtnPanic.setOnClickListener(this);

        fabNeedleDrop = (FloatingActionButton) view.findViewById(R.id.fabNeedleDrop);
        fabNeedleDrop.setOnClickListener(this);

        fabTestPush = (FloatingActionButton) view.findViewById(R.id.fabTestPush);
        fabTestPush.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
//                gpsService.sendTestFbPush();

                //test share group
//                FragmentDialogShareGroup diagShareGroup = new FragmentDialogShareGroup();
//
//                Bundle args = new Bundle();
//                args.putBoolean("privateGroup", false);
//                args.putString("groupName", "Yea");
//                args.putString("groupCode", "TheBestCode");
//                diagShareGroup.setArguments(args);
//
//                diagShareGroup.show(getFragmentManager(), "testShare");

                //Test onboarding
                OnBoardingActivity testBoaringActv = new OnBoardingActivity();
                Intent boardingIntent = new Intent(activity, OnBoardingActivity.class);
                startActivity(boardingIntent);
            }
        });

        etxtPanicDesc = (EditText) view.findViewById(R.id.etxtPanicDesc);
        etxtPanicDesc.setOnFocusChangeListener(this);
        etxtPanicDesc.setOnEditorActionListener(this);
        etxtPanicDesc.setImeOptions(EditorInfo.IME_ACTION_DONE);

        linLayRespondes = (LinearLayout) view.findViewById(R.id.linLayResponders);

        txtvNoOfResponders = (TextView) view.findViewById(R.id.txtvNoOfRespondersValue);

        relLayPanicRoot = (RelativeLayout) view.findViewById(R.id.relLayPanicRoot);
        relLayPanicRoot.setOnClickListener(this);

        transPanicBtn = (TransitionDrawable) ibtnPanic.getBackground();
        transPanicBtn.setCrossFadeEnabled(true);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        context = getActivity();
        super.onActivityCreated(savedInstanceState);

        activity = (HomeActivity) getActivity();

        //Start gps service
        Intent startGpsService = new Intent(context, ServiceGps.class);
        context.startService(startGpsService);

        context.bindService(startGpsService, myServiceConnection, Context.BIND_ABOVE_CLIENT);

        //Check if gps is on
        GpsHelper gpsHelper = new GpsHelper(getActivity());

        if(!gpsHelper.isGpsOn())
            gpsHelper.showDialog("Your GPS is disabled, no one will be able to respond to your emergency. We strongly recommend leaving it on to ensure your safety.\n\nPlease enable your GPS now?");


        initTut(true);
    }

    private void initTut(boolean showTut)
    {
        if(showTut)
        {
           spotvPanicButton =  new SpotlightView.Builder(activity)
                    .introAnimationDuration(400)
                    .enableRevealAnimation(true)
                    .performClick(false)
                    .fadeinTextDuration(400)
                    .headingTvColor(ContextCompat.getColor(activity, R.color.SeaGreen))
                    .headingTvSize(24)
                    .headingTvText("Emergency")
                    .subHeadingTvColor(ContextCompat.getColor(activity, R.color.White))
                    .subHeadingTvSize(14)
                    .subHeadingTvText("Tap here to activate an emergency")
                    .maskColor(Color.parseColor("#dc000000"))
                    .target(ibtnPanic)
                    .lineAnimDuration(400)
                    .lineAndArcColor(ContextCompat.getColor(activity, R.color.SeaGreen))
                    .usageId(ibtnPanic.getId() + "") //UNIQUE ID
                    .show();

            spotvPanicButton.setListener(new SpotlightListener()
            {
                @Override
                public void onUserClicked(String s)
                {
                    //Create next one
                    spotvNeedleDropButton =  new SpotlightView.Builder(activity)
                            .introAnimationDuration(400)
                            .enableRevealAnimation(true)
                            .performClick(false)
                            .fadeinTextDuration(400)
                            .headingTvColor(ContextCompat.getColor(activity, R.color.SeaGreen))
                            .headingTvSize(24)
                            .headingTvText("Needle Drop")
                            .subHeadingTvColor(ContextCompat.getColor(activity, R.color.White))
                            .subHeadingTvSize(14)
                            .subHeadingTvText("Tap here to mark the location of a needle found")
                            .maskColor(Color.parseColor("#dc000000"))
                            .target(fabNeedleDrop)
                            .lineAnimDuration(400)
                            .lineAndArcColor(ContextCompat.getColor(activity, R.color.SeaGreen))
                            .usageId(fabNeedleDrop.getId() + "") //UNIQUE ID
                            .show();

                    spotvNeedleDropButton.setListener(new SpotlightListener()
                    {
                        @Override
                        public void onUserClicked(String s)
                        {
                            activity.showGroupTut();
                        }
                    });
                }
            });
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
    {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        return false;
    }

    @Override
    public void onDetach()
    {
        //Disconnect service
        //Start gps service
        Intent stopGpsService = new Intent(context, ServiceGps.class);
        context.stopService(stopGpsService);

        if(isServiceConnected)
            context.unbindService(myServiceConnection);

        super.onDetach();
    }

    @Override
    public void onClick(View v)
    {
        if(v == ibtnPanic)
        {
            Log.d(TAG, "Panic button WAS CLICKED");
            panicing = !panicing;   //swap panicing status

            if(panicing)
            {
                Log.d(TAG, "Panic button set to activate");
                //Check Gps is on
                GpsHelper gpsHelper = new GpsHelper(getActivity());
                if(gpsHelper.isGpsOn())
                {
                    //Lock drawer
                    ((HomeActivity) getActivity()).lockDrawer(true);

                    //Reveal desc and responders views
                    final Animation fadeIn = (Animation) AnimationUtils.loadAnimation(context, R.anim.abc_fade_in);

                    etxtPanicDesc.startAnimation(fadeIn);
                    linLayRespondes.startAnimation(fadeIn);


                    etxtPanicDesc.setVisibility(View.VISIBLE);
                    linLayRespondes.setVisibility(View.VISIBLE);

                    //Hide bottom action bar
                    //animate exit here
                    final Animation slideDown = (Animation) AnimationUtils.loadAnimation(context, R.anim.abc_slide_out_bottom);
                    activity.fLayBottomActionBar.startAnimation(slideDown);

                    activity.fLayBottomActionBar.setVisibility(View.GONE);

                           /* HomeActivity.fragManager.beginTransaction()
                    .setCustomAnimations(R.animator.animator_slide_down, 0, R.animator.animator_slide_up, 0)
                            .hide(HomeActivity.fragBottomActionBar)
                            .addToBackStack("HideBottomActionBar")
                    .commit();*/

                    //Animate and change image btn
                    transPanicBtn.startTransition(TRANSITION_TIME);

                    activity.adjustActionBarButtonsVisibility(false);

                    Log.i(TAG, "GpsServiceActivation" + isServiceConnected + "");

                    //Check if panic delay is enabled
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
                    boolean panicDelay = sharedPrefs.getBoolean("panicDelay", true);

                    if(panicDelay)
                    {
                        worker = Executors.newSingleThreadScheduledExecutor();

                        Runnable panicDelayedTask = new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                Looper.prepare();

                                //Activate panic immediate
                                gpsService.onPanic(true, new OnPanicCreatedListener()
                                {
                                    @Override
                                    public void onPanicCreated(ParseObject objPanic)
                                    {
                                        panicObj = objPanic;
                                        FragmentDialogPanicDescription fragPanicMsg = new FragmentDialogPanicDescription();
                                        fragPanicMsg.show(getFragmentManager(), "fragDiagPanicMsg");
                                    }
                                }, new OnResponderListener()
                                {
                                    @Override
                                    public void onResponderUpdate(int noOfResponders)
                                    {
                                        txtvNoOfResponders.setText(noOfResponders + "");
                                    }
                                }, new OtePanicListener()
                                {
                                    @Override
                                    public void onDisableOtePanic()
                                    {
                                        //Have to run on ui thread for ui to update
                                        activity.runOnUiThread(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                ibtnPanic.performClick();
                                            }
                                        });
                                    }
                                });

                                Looper.loop();
                            }
                        };

                        delayPanicTimer = worker.schedule(panicDelayedTask, PANIC_TIMER_START_DELAY, TimeUnit.MILLISECONDS);
                    }
                    else
                    {
                        //Activate panic immediately after confirmation dialog
                        panicConfirmationDialog();
                    }
                }
                else
                {
                    panicing = !panicing;   //Swop pannicing back since user can't panic without gps
                    gpsHelper.showDialog("Your GPS is disabled, please turn it on now and try again.");
                }

            }
            else
            {
                Log.d(TAG, "Panic button set to deactivate");
                //Cancel delayed panic
                if(delayPanicTimer != null)
                    delayPanicTimer.cancel(true);

                //UnLock drawer
                ((HomeActivity)getActivity()).lockDrawer(false);

                //Hide desc and responders views
                Animation fadeOut = (Animation) AnimationUtils.loadAnimation(context, R.anim.abc_fade_out);

                etxtPanicDesc.startAnimation(fadeOut);
                linLayRespondes.startAnimation(fadeOut);

                etxtPanicDesc.setVisibility(View.INVISIBLE);
                linLayRespondes.setVisibility(View.INVISIBLE);

                //Show bottom action bar
                final Animation slideUp = (Animation) AnimationUtils.loadAnimation(context, R.anim.abc_slide_in_bottom);
                activity.fLayBottomActionBar.startAnimation(slideUp);

                activity.fLayBottomActionBar.setVisibility(View.VISIBLE);

                //Animate and change image btn
                transPanicBtn.reverseTransition(TRANSITION_TIME);

                ((HomeActivity)getActivity()).adjustActionBarButtonsVisibility(false);

                if (gpsService != null)
                    gpsService.onPanic(false, null, null, null); //Kill gps service

                /*if(!skipTurnOffPanicService)
                {
                    if (gpsService != null)

                }
                else
                    skipTurnOffPanicService = false;    //Reset skip*/

                //Reset values
                etxtPanicDesc.setText("");
                txtvNoOfResponders.setText("0");
            }
        }
        else if(v == relLayPanicRoot)
        {
            //close keyboard
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(getActivity().INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

            //Fire on focus changed listener to send details for update to db
            etxtPanicDesc.clearFocus();
            relLayPanicRoot.requestFocus();
        }
        else if(v == fabNeedleDrop)
        {
            //Build a prompt first
            if(!awaitingNeedleDropCallback)
                buildNeedleDropConfirmation();
            else
                Snackbar.make(fabNeedleDrop, "Waiting for pending needle drop", Snackbar.LENGTH_LONG).show();
        }
    }

    public void panicConfirmationDialog()
    {
        //Prompt user if they are sure
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Activate Panic and send notifications?").setCancelable(false)
                .setTitle("Activate?")
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                    {
                        dialog.cancel();
                        skipTurnOffPanicService = true;
                        ibtnPanic.performClick();   //Revert animation and layout changes
                    }
                })
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                    {
                        //Activate panic immediate
                        gpsService.onPanic(true, new OnPanicCreatedListener()
                        {
                            @Override
                            public void onPanicCreated(ParseObject objPanic)
                            {
                                panicObj = objPanic;
                                FragmentDialogPanicDescription fragPanicMsg = new FragmentDialogPanicDescription();
                                fragPanicMsg.show(getFragmentManager(), "fragDiagPanicMsg");
                            }
                        }, new OnResponderListener()
                        {
                            @Override
                            public void onResponderUpdate(int noOfResponders)
                            {
                                txtvNoOfResponders.setText(noOfResponders + "");
                            }
                        }, new OtePanicListener()
                        {
                            @Override
                            public void onDisableOtePanic()
                            {
                                ibtnPanic.performClick();
                            }
                        });
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    private void buildNeedleDropConfirmation()
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage("Are you sure you want to drop a needle at your location?").setCancelable(false).setPositiveButton("Yes", new DialogInterface.OnClickListener()
        {
            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
            {
                awaitingNeedleDropCallback = true;

                //Get most recent location
                gpsService.getUserLocation(new UserLocationListener()
                {
                    @Override
                    public void onLocationUpdate(Location newLocation)
                    {
                        //Add new needle drop to parse
                        ParseObject needleDrop = new ParseObject("Needles");
                        needleDrop.put("location", new ParseGeoPoint(newLocation.getLatitude(), newLocation.getLongitude()));
                        needleDrop.put("user", HomeActivity.currentUser);
                        needleDrop.saveInBackground(new SaveCallback()
                        {
                            @Override
                            public void done(ParseException e)
                            {
                                awaitingNeedleDropCallback = false;
                                String msg;

                                if(e == null)
                                {
                                    msg = "Needle Drooped Successfully";
                                }
                                else
                                {
                                    //TODO Check errors
                                    int errorCode = e.getCode();
                                    msg = "Couldn't drop needle: " + e.getMessage();
                                }

                                Snackbar.make(fabNeedleDrop, msg, Snackbar.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }

        }).setNegativeButton("No", new DialogInterface.OnClickListener()
        {
            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
            {
                dialog.dismiss();
            }

        });

        final AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        if(v == etxtPanicDesc)
        {
            if(!hasFocus && !skipTurnOffPanicService && !etxtPanicDesc.getText().toString().isEmpty())
            {
                gpsService.onDetailsGiven(etxtPanicDesc.getText().toString());
            }
        }
    }

    public void onUpdateResponder(int noOfResponders)
    {
        txtvNoOfResponders.setText(noOfResponders + "");
    }

    public boolean isPanicing(){return panicing;}
}
