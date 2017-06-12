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
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
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
import java.util.Collection;
import java.util.List;


public class LoginActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener, TextView.OnEditorActionListener
{

    private final int REQ_CODE_REGISTER_USER = 1;
    private final LoginActivity thisActivity = this;
//    private ProgressBar progbLogin;
    private SwipeRefreshLayout srLayLogin;

    //If opened vai push notification
    private boolean openedVaiPush = false;
    private String jsonStringData;

    private EditText etxtUsername;
    private EditText etxtPassword;
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
        if(getIntent().hasExtra("com.parse.Data"))
        {
            openedVaiPush = true;
            jsonStringData = getIntent().getStringExtra("com.parse.Data");
            Log.d("Login", jsonStringData);
        }

        //check for already logged in user that is rembered
        ParseUser rememberedUser = ParseUser.getCurrentUser();

        if(rememberedUser != null)
            login();

        btnLogin = (TextView) findViewById(R.id.btnLogin);
        btnLoginFb = (LoginButton) findViewById(R.id.btnLoginFb);
        etxtUsername = (EditText) findViewById(R.id.etxtUsername);
        etxtPassword = (EditText) findViewById(R.id.etxtPassword);
        etxtPassword.setOnEditorActionListener(this);   //Listen for done clicked to start login

//       progbLogin = (ProgressBar) findViewById(R.id.progbLogin);
        srLayLogin = (SwipeRefreshLayout) findViewById(R.id.srLayLogin);
        initSwipeRefresh();
        initFbLogin();
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
                                //Launches register activity with limited pre-populated fields
                                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                                registerIntent.putExtra("regWithFacebook", true);
                                registerIntent.putExtra("authData", parseUser.getJSONObject("authData").toString());
                                startActivityForResult(registerIntent, REQ_CODE_REGISTER_USER);
                            }
                            else
                                login();
                        }
                        else
                        {
                            //Unsuccessful login read and display error
                            //Invalid login credentials code 101
                            //Check internet connection code 100

//                    progbLogin.setVisibility(View.GONE);
                            srLayLogin.setRefreshing(false);

                            if(e.getCode() == 100)
                                Snackbar.make(btnLoginFb, "Please check you internet connection and try again", Snackbar.LENGTH_LONG).show();
                            else
                                Snackbar.make(btnLoginFb, "Unsuccessful: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
//        btnLoginFb.registerCallback(callBackMang, new FacebookCallback<LoginResult>()
//        {
//            @Override
//            public void onSuccess(LoginResult loginResult)
//            {
//
//            }
//
//            @Override
//            public void onCancel()
//            {
//
//            }
//
//            @Override
//            public void onError(FacebookException error)
//            {
//
//            }
//        });
    }

    @Override
    public void onRefresh()
    {
        srLayLogin.setRefreshing(true);
        srLayLogin.setRefreshing(false);
    }

    public void onClickLogin(View view)
    {
//        progbLogin.setVisibility(View.VISIBLE);
        srLayLogin.setRefreshing(true);
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

//                    progbLogin.setVisibility(View.GONE);
                    srLayLogin.setRefreshing(false);

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
            intentLogin.putExtra("jsonPushData", jsonStringData);

        thisActivity.startActivity(intentLogin);
        thisActivity.finish();
    }

    public void onClickRegister(View view)
    {
        //Launches register activity
        Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivityForResult(registerIntent, REQ_CODE_REGISTER_USER);
    }

    public void subscribeToChannels(List<ParseObject> groups)
    {
        //sub to channel
        //Caps start of each word, uncaps every other
        List<String> channelNames = new ArrayList<>();

        channelNames.add("");   //Add broadcast channel

        for(int i = 0; i < groups.size(); i++)
            channelNames.add(groups.get(i).getString("name").replaceAll("\\s+", "").trim().toString());

        ParseInstallation.getCurrentInstallation().addAllUnique("channels", channelNames);
        ParseInstallation.getCurrentInstallation().saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                {
                    Log.i("Login", "Subscribed to all channels");
//                    progbLogin.setVisibility(View.GONE);
                    srLayLogin.setRefreshing(false);
                }
                else
                {
                    Log.e("Login", "Subscribe to channels failed: " + e.getMessage() + " Code: " + e.getCode());
//                    progbLogin.setVisibility(View.GONE);
                    srLayLogin.setRefreshing(false);
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
        if(requestCode == REQ_CODE_REGISTER_USER)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Intent homePanicIntent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(homePanicIntent);
                this.finish();
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
//                progbLogin.setVisibility(View.VISIBLE);
                srLayLogin.setRefreshing(true);

                final Toast msg = Toast.makeText(thisActivity, "", Toast.LENGTH_LONG);

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
//                            progbLogin.setVisibility(View.GONE);
                            srLayLogin.setRefreshing(false);
                            msg.setText("Reset Email Sent");
                        }
                        else
                        {
                            //Failed
//                            progbLogin.setVisibility(View.GONE);
                            srLayLogin.setRefreshing(false);

                            //No user found with that email address code 205
                            //Invalid email code 125
                            //Check internet connection code 100
                            if(e.getCode() == 205)
                                msg.setText("No account associated with that email");
                            else if(e.getCode() == 125)
                                msg.setText("Invalid email address");
                            else if(e.getCode() == 100)
                                msg.setText("Check your internet connection and try again");
                            else
                                msg.setText("Unsuccessful Email Reset: " + e.getMessage() + " Code: " + e.getCode());
                        }

                        msg.show();
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
