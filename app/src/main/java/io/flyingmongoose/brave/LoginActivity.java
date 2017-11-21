package io.flyingmongoose.brave;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.CallbackManager;
import com.facebook.login.widget.LoginButton;
import com.parse.FindCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParsePush;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.RequestPasswordResetCallback;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;


public class LoginActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener, TextView.OnEditorActionListener
{

    private final int REQ_CODE_REGISTER_USER_FB = 1;
    private final int REQ_CODE_REGISTER_USER = 2;
    private final LoginActivity thisActivity = this;
//    private ProgressBar progbLogin;
    private SwipeRefreshLayout srLayLogin;

    //If opened vai push notification
    private boolean openedVaiPush = false;
    private String jsonStringData;

    private LinearLayout linLayUsername;
    private EditText etxtUsername;
    private LinearLayout linLayPassword;
    private EditText etxtPassword;
    private LinearLayout linLayActionButtons;
    private TextView btnLogin;
    private LoginButton btnLoginFb;

    private CallbackManager callBackMang;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        callBackMang = CallbackManager.Factory.create();

        //check if opened vai notification
        if(getIntent().hasExtra("google.message_id"))
            openedVaiPush = true;

//        if(getIntent().hasExtra("com.parse.Data"))
//        {
//            openedVaiPush = true;
//            jsonStringData = getIntent().getStringExtra("com.parse.Data");
//            Log.d("Login", jsonStringData);
//        }

        linLayActionButtons = (LinearLayout) findViewById(R.id.linLayLoginActionButtons);
        btnLogin = (TextView) findViewById(R.id.btnLogin);
        btnLoginFb = (LoginButton) findViewById(R.id.btnLoginFb);
        linLayUsername = (LinearLayout) findViewById(R.id.linLayUsername);
        etxtUsername = (EditText) findViewById(R.id.etxtUsername);
        linLayPassword = (LinearLayout) findViewById(R.id.linLayPassword);
        etxtPassword = (EditText) findViewById(R.id.etxtPassword);
        etxtPassword.setOnEditorActionListener(this);   //Listen for done clicked to start login
        srLayLogin = (SwipeRefreshLayout) findViewById(R.id.srLayLogin);

        //check for already logged in user that is rembered
        ParseUser rememberedUser = ParseUser.getCurrentUser();

        if(rememberedUser != null)
        {
            //Check for incomplete fb login / register
            if(rememberedUser.has("authData") && !rememberedUser.has("facebookId"))
                regWithFacebook(rememberedUser);
            else
                login();
        }

        initSwipeRefresh();
        initFbLogin();
    }

    public void register()
    {
        Intent boardingIntent = new Intent(this, OnBoardingActivity.class);
        startActivityForResult(boardingIntent, REQ_CODE_REGISTER_USER);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
    {
        if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
        {
            btnLogin.performClick();
        }
        return false;
    }

    private void initSwipeRefresh()
    {
        srLayLogin.setEnabled(false);
        srLayLogin.setOnRefreshListener(this);
        srLayLogin.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayLogin.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayLogin.setProgressViewOffset(true, 0, 8);
    }

    @Override
    public void onRefresh()
    {
        //Does nothing because srLay is not used as a swipe to refresh view only as a loading view
    }

    private void loading(boolean visible)
    {
        if(visible)
        {
            //Shows loading animation whilst animating other views to hide
            //Do needed animations
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_hide);
            linLayUsername.startAnimation(anim);
            linLayPassword.startAnimation(anim);
            linLayActionButtons.startAnimation(anim);
            btnLoginFb.startAnimation(anim);

            //Hide actual views
            linLayUsername.setVisibility(View.INVISIBLE);
            linLayPassword.setVisibility(View.INVISIBLE);
            linLayActionButtons.setVisibility(View.INVISIBLE);
            btnLoginFb.setVisibility(View.INVISIBLE);

            //Make loading animation visible
            srLayLogin.setRefreshing(true);
        }
        else
        {
            //Hide loading animation
            srLayLogin.setRefreshing(false);

            //Animate other views to visible
            Animation anim = AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_show);
            linLayUsername.startAnimation(anim);
            linLayPassword.startAnimation(anim);
            linLayActionButtons.startAnimation(anim);
            btnLoginFb.startAnimation(anim);

            //Make other views visible
            linLayUsername.setVisibility(View.VISIBLE);
            linLayPassword.setVisibility(View.VISIBLE);
            linLayActionButtons.setVisibility(View.VISIBLE);
            btnLoginFb.setVisibility(View.VISIBLE);
        }


    }

    private void initFbLogin()
    {
        btnLoginFb.setReadPermissions("email");

        final List<String> lstPerms = new ArrayList<>();
        lstPerms.add("email");
        lstPerms.add("public_profile");

        btnLoginFb.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                loading(true);
                ParseFacebookUtils.logInWithReadPermissionsInBackground(thisActivity, lstPerms, new LogInCallback()
                {
                    @Override
                    public void done(ParseUser parseUser, ParseException e)
                    {
                        if(parseUser != null)
                        {
                            //Check if parse user's fb id is set if not this is a new reg
                            if(parseUser.getString("facebookId") == null)
                            {
                                regWithFacebook(parseUser);
                            }
                            else
                                login();
                        }
                        else
                        {
                            //Unsuccessful login read and display error
                            //Invalid login credentials code 101
                            //Check internet connection code 100

                            loading(false);

                            if(e != null)   //Check for null user might have canceled fb login
                            {
                                if(e.getCode() == 100)
                                    Snackbar.make(btnLoginFb, getString(R.string.error_100_no_internet), Snackbar.LENGTH_LONG).show();
                                else
                                    Snackbar.make(btnLoginFb, "Unsuccessful: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        });
    }

    public void onClickLogin(View view)
    {
        loading(true);
        //Attempt to login user
        ParseUser.logInInBackground(etxtUsername.getText().toString(), etxtPassword.getText().toString(), new LogInCallback() {
            @Override
            public void done(ParseUser parseUser, ParseException e)
            {
                if(parseUser != null)
                {
                    //Login successful
                    login();
                }
                else
                {
                    //Unsuccessful login read and display error
                    //Invalid login credentials code 101
                    //Check internet connection code 100

                    loading(false);

                    if(e.getCode() == 101)
                        Snackbar.make(btnLogin, "Invalid Username or Password", Snackbar.LENGTH_LONG).show();
                    else if(e.getCode() == 100)
                        Snackbar.make(btnLogin, "Please check you internet connection and try again", Snackbar.LENGTH_LONG).show();
                    else
                        Snackbar.make(btnLogin, "Unsuccesfull: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void regWithFacebook(ParseUser parseUser)
    {
        //Launches register activity with limited pre-populated fields
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        registerIntent.putExtra("regWithFacebook", true);
        registerIntent.putExtra("authData", parseUser.getJSONObject("authData").toString());
        startActivityForResult(registerIntent, REQ_CODE_REGISTER_USER_FB);
    }

    public void login()
    {
        //TODO: animate login to home screen
        ParsePush.unsubscribeInBackground("not_logged_in");

        if(ParseUser.getCurrentUser() != null)
        {
            //Subscribe installation to channels
            //Get subbed group ids
            List<String> subbedGroupsId = ParseUser.getCurrentUser().getList("groups");

            if(subbedGroupsId != null)
            {
                //Get subbed groups
                ParseQuery<ParseObject> getGroups = ParseQuery.getQuery("Groups");
                getGroups.whereContainedIn("objectId", subbedGroupsId);
                getGroups.findInBackground(new FindCallback<ParseObject>()
                {
                    @Override
                    public void done(List<ParseObject> parseObjects, ParseException e)
                    {
                        if (e == null)
                        {
                            if (!parseObjects.isEmpty())
                                subscribeToChannels(parseObjects);
                            else
                                ParsePush.subscribeInBackground("");
                        } else
                        {
                            Log.e("Login", "Retrieving of subbed groups Failed: " + e.getMessage() + " Code: " + e.getCode());
                        }
                    }
                });
            }
            else
                ParsePush.subscribeInBackground("");
        }

        final Intent intentLogin = new Intent(LoginActivity.this, HomeActivity.class);

        if(openedVaiPush)   //add data if opened vai push
            intentLogin.putExtras(getIntent().getExtras());

        thisActivity.startActivity(intentLogin);
        thisActivity.finish();
    }

    public void onClickRegister(View view)
    {
        //Launches register activity
//        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
//        startActivityForResult(registerIntent, REQ_CODE_REGISTER_USER);
        register();
    }

    public void subscribeToChannels(List<ParseObject> groups)
    {
        //sub to channel
        List<String> channelNames = new ArrayList<>();

        channelNames.add("");   //Add broadcast channel

        for(int i = 0; i < groups.size(); i++)
            channelNames.add(FormatUtil.formatChannelName(groups.get(i).getString("name")));

        ParseInstallation.getCurrentInstallation().addAllUnique("channels", channelNames);
        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                {
                    Log.i("Login", "Subscribed to all channels");
                }
                else
                {
                    Log.e("Login", "Subscribe to channels failed: " + e.getMessage() + " Code: " + e.getCode());
                }
            }
        });
    }

    public void onClickResetPassword(View view)
    {
        buildResetDialog();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        ParseFacebookUtils.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQ_CODE_REGISTER_USER_FB)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Intent homePanicIntent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(homePanicIntent);
                this.finish();
            }
        }
        else if(requestCode == REQ_CODE_REGISTER_USER)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                String userEmail = data.getStringExtra("email");
                String userPassword = data.getStringExtra("password");

                if(userEmail.length() != 0 && userPassword.length() != 0)
                {
                    etxtUsername.setText(userEmail);
                    etxtPassword.setText(userPassword);
                    btnLogin.performClick();
                }
            }
        }
    }

    private void buildResetDialog()
    {
        //Inflate custom view
        LayoutInflater inflater = LayoutInflater.from(this);
        View resetDialogView = inflater.inflate(R.layout.dialog_reset_password, null);
        final EditText etxtDialogResetPasswordEmail = (EditText) resetDialogView.findViewById(R.id.etxtDialogResetPasswordEmail);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setView(resetDialogView);

        dialogBuilder.setPositiveButton("Reset", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //Loading animation
                loading(true);

                //Clean up email
                String email = etxtDialogResetPasswordEmail.getText().toString();
                email = email.trim().toLowerCase();
                //Reset user password
                ParseUser.requestPasswordResetInBackground(email, new RequestPasswordResetCallback()
                {
                    @Override
                    public void done(ParseException e)
                    {
                        if(e == null)
                        {
                            //Successfull
                            loading(false);
                            Snackbar.make(srLayLogin, "Reset Email Sent", Snackbar.LENGTH_LONG).show();
                        }
                        else
                        {
                            //Failed
                            loading(false);

                            //No user found with that email address code 205
                            //Invalid email code 125
                            //Check internet connection code 100
                            if(e.getCode() == 205)
                                Snackbar.make(srLayLogin, "No account associated with that email", Snackbar.LENGTH_LONG).show();
                            else if(e.getCode() == 125)
                                Snackbar.make(srLayLogin, "Invalid email address", Snackbar.LENGTH_LONG).show();
                            else if(e.getCode() == 100)
                                Snackbar.make(srLayLogin, "Check your internet connection and try again", Snackbar.LENGTH_LONG).show();
                            else
                                Snackbar.make(srLayLogin, "Unsuccessful Email Reset: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        dialogBuilder.create().show();
    }
}
