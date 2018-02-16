package io.flyingmongoose.brave.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;
import com.codemybrainsout.onboarder.OnAhoyListeners;
import com.codemybrainsout.onboarder.OnIntroListener;
import com.codemybrainsout.onboarder.OnTextInputProvidedListener;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.FindCallback;
import com.parse.GetCallback;
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
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import io.flyingmongoose.brave.R;
import io.flyingmongoose.brave.util.UtilAnalytics;
import io.flyingmongoose.brave.util.UtilFormating;

/**
 * Created by wprenison on 2017/08/02.
 */

public class ActivOnBoarding extends AhoyOnboarderActivity
{
    private static final String TAG = "OnBoarderActivity";
    private String password;

    private ActivOnBoarding thisActivity = this;

    //If opened vai push notification
    private boolean openedVaiPush = false;
    private String facebookId;

    private boolean isExistingUser = false;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //check if opened vai notification
        if(getIntent().hasExtra("google.message_id"))
            openedVaiPush = true;

        //Set basic page info
        AhoyOnboarderCard cardIntro = new AhoyOnboarderCard("The home of random acts of kindness", R.drawable.ic_huge_brave, AhoyOnboarderCard.OnboardType.INTRO);
        final AhoyOnboarderCard cardFullName = new AhoyOnboarderCard("What's your name, friend?", "Full Name", R.drawable.ic_fullname, R.drawable.ic_accept_fullname, R.drawable.ic_reject_fullname, AhoyOnboarderCard.OnboardType.TEXT_INUPT);
        AhoyOnboarderCard cardEmail = new AhoyOnboarderCard("Promise I don't spam...", "you@youremail.com", R.drawable.ic_email, R.drawable.ic_accept_email, R.drawable.ic_reject_email, AhoyOnboarderCard.OnboardType.TEXT_INUPT);
        final AhoyOnboarderCard cardPassword = new AhoyOnboarderCard("Sssshhh...", "Create a password", "Forgot Passowrd?", R.drawable.ic_password, R.drawable.ic_accept_password, R.drawable.ic_reject_password, AhoyOnboarderCard.OnboardType.TEXT_INUPT);
        final AhoyOnboarderCard cardPhone = new AhoyOnboarderCard("Call me maybe? ;)", "+27721234567", R.drawable.ic_phone, R.drawable.ic_accept_phone, R.drawable.ic_reject_phone, AhoyOnboarderCard.OnboardType.TEXT_INUPT);
        final AhoyOnboarderCard cardShare = new AhoyOnboarderCard("Got a referral code?", "Referral Code", R.drawable.ic_network, R.drawable.ic_accept_network, R.drawable.ic_reject_network, AhoyOnboarderCard.OnboardType.TEXT_INPUT_SHARE_OPTION);

        // You can define title and description colors (by default white)
        cardIntro.setPageName("Intro");
        cardIntro.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardIntro.setTitleTextSize(dpToPixels(10, this));
        cardIntro.setBackgroundColor(android.R.color.transparent);
        OnIntroListener introListener = new OnIntroListener()
        {
            @Override
            public void onIntroAnimationFinish()
            {
                goToNextPage();
            }
        };

        cardFullName.setPageName("FullName");
        cardFullName.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardFullName.setDescriptionColor(R.color.common_google_signin_btn_text_dark);
        cardFullName.setBackgroundColor(R.color.black_transparent);
        cardFullName.setTitleTextSize(dpToPixels(8, this));
        cardFullName.setDescriptionTextSize(dpToPixels(6, this));
        cardFullName.setInputType(InputType.TYPE_CLASS_TEXT, InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        final OnTextInputProvidedListener cardListenerFullName = new OnTextInputProvidedListener()
        {
            @Override
            public void onInputProvided(String textInput, TextInputLayout tillInput)
            {
                if(validateFullName(textInput, tillInput, false))
                {
                    showAcceptIcon();
                    tillInput.setError(null);
                }
                else
                    showEnterIcon();
            }

            @Override
            public void onValidate(String textInput, TextInputLayout tillInput, boolean fromScroll)
            {
                if(validateFullName(textInput, tillInput, true))
                {
                    if (!fromScroll)
                        goToNextPage();
                }
                else
                {
                    if(fromScroll)
                        goToPrevPage();

                    showRejectIcon();
                }
            }

            @Override
            public void onImeEnterPressed(TextInputLayout tillInput)
            {
                performNextClick();
            }

            @Override
            public void onLinkClick()
            {

            }

            @Override
            public void onFbClick()
            {

            }
        };


        cardPassword.setPageName("Password");
        cardPassword.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardPassword.setDescriptionColor(R.color.common_google_signin_btn_text_dark);
        cardPassword.setBackgroundColor(R.color.black_transparent);
        cardPassword.setTitleTextSize(dpToPixels(8, this));
        cardPassword.setDescriptionTextSize(dpToPixels(6, this));
        cardPassword.setInputType(InputType.TYPE_CLASS_TEXT, InputType.TYPE_TEXT_VARIATION_PASSWORD);
        OnTextInputProvidedListener cardListenerPassword = new OnTextInputProvidedListener()
        {
            @Override
            public void onInputProvided(String textInput, TextInputLayout tillInput)
            {
                if(validatePassword(textInput, tillInput, false))
                {
                    showAcceptIcon();
                    tillInput.setError(null);
                }
                else
                    showEnterIcon();
            }

            @Override
            public void onValidate(String textInput, TextInputLayout tillInput, boolean fromScroll)
            {
                if(validatePassword(textInput, tillInput, true))
                {
                    if(!fromScroll)
                        goToNextPage();

                    if(isExistingUser)
                    {
                        setLoading(true, 2);

                        String[] allInputData = getAllInputData();

                        //Attempt to login user
                        ParseUser.logInInBackground(allInputData[1], allInputData[2], new LogInCallback() {
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

                                    setLoading(false, 2);

                                    if(e.getCode() == 101)
                                        Snackbar.make(parentLayout, "Invalid Username or Password", Snackbar.LENGTH_LONG).show();
                                    else if(e.getCode() == 100)
                                        Snackbar.make(parentLayout, "Please check you internet connection and try again", Snackbar.LENGTH_LONG).show();
                                    else
                                        Snackbar.make(parentLayout, "Unsuccesfull: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                    }
                }
                else
                {
                    if(fromScroll)
                        goToPrevPage();

                    showRejectIcon();
                }
            }

            @Override
            public void onImeEnterPressed(TextInputLayout tillInput)
            {
                performNextClick();
            }

            @Override
            public void onLinkClick()
            {
                //Execute password reset
                buildResetDialog();
            }

            @Override
            public void onFbClick()
            {

            }
        };

        cardPhone.setPageName("Phone");
        cardPhone.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardPhone.setDescriptionColor(R.color.common_google_signin_btn_text_dark);
        cardPhone.setBackgroundColor(R.color.black_transparent);
        cardPhone.setTitleTextSize(dpToPixels(8, this));
        cardPhone.setDescriptionTextSize(dpToPixels(6, this));
        cardPhone.setInputType(InputType.TYPE_CLASS_PHONE, InputType.TYPE_NULL);
        final OnTextInputProvidedListener cardListenerPhone = new OnTextInputProvidedListener()
        {
            @Override
            public void onInputProvided(String textInput, TextInputLayout tillInput)
            {
                if(validatePhone(textInput, tillInput, false))
                {
                    showAcceptIcon();
                    tillInput.setError(null);
                }
                else
                    showEnterIcon();
            }

            @Override
            public void onValidate(String textInput, TextInputLayout tillInput, boolean fromScroll)
            {
                if(validatePhone(textInput, tillInput, true))
                {
                    if(!fromScroll)
                        goToNextPage();
                }
                else
                {
                    if(fromScroll)
                        goToPrevPage();

                    showRejectIcon();
                }
            }

            @Override
            public void onImeEnterPressed(TextInputLayout tillInput)
            {
                performNextClick();
            }

            @Override
            public void onLinkClick()
            {

            }

            @Override
            public void onFbClick()
            {

            }
        };

        cardShare.setPageName("Share");
        cardShare.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardShare.setDescriptionColor(R.color.common_google_signin_btn_text_dark);
        cardShare.setBackgroundColor(R.color.black_transparent);
        cardShare.setTitleTextSize(dpToPixels(8, this));
        cardShare.setDescriptionTextSize(dpToPixels(6, this));
        cardShare.setInputType(InputType.TYPE_CLASS_TEXT, InputType.TYPE_NULL);
        cardShare.setShareTitle("Share");
        cardShare.setShareTitleColor(R.color.common_google_signin_btn_text_dark);
        final OnTextInputProvidedListener cardListenerShare = new OnTextInputProvidedListener()
        {
            @Override
            public void onInputProvided(String textInput, TextInputLayout tillInput)
            {
                if(validateReferralCode(textInput, tillInput, false))
                {
                    showAcceptIcon();
                    tillInput.setError(null);
                }
                else
                    showEnterIcon();
            }

            @Override
            public void onValidate(String textInput, TextInputLayout tillInput, boolean fromScroll)
            {
                signUpNewUser(tillInput);
            }

            @Override
            public void onImeEnterPressed(TextInputLayout tillInput)
            {
                //Use below when adding a new page
//                    if(!fromScroll)
//                        goToNextPage();

                //Create user
                signUpNewUser(tillInput);
            }

            @Override
            public void onLinkClick()
            {

            }

            @Override
            public void onFbClick()
            {

            }
        };

        cardEmail.setPageName("Email");
        cardEmail.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardEmail.setDescriptionColor(R.color.common_google_signin_btn_text_dark);
        cardEmail.setBackgroundColor(R.color.black_transparent);
        cardEmail.setTitleTextSize(dpToPixels(8, this));
        cardEmail.setDescriptionTextSize(dpToPixels(6, this));
        cardEmail.setInputType(InputType.TYPE_CLASS_TEXT, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        cardEmail.setFbLogin(false);
        OnTextInputProvidedListener cardListenerEmail = new OnTextInputProvidedListener()
        {
            @Override
            public void onInputProvided(String textInput, TextInputLayout tillInput)
            {
                if(validateEmail(textInput, tillInput, false))
                {
                    showAcceptIcon();
                    tillInput.setError(null);
                }
                else
                    showEnterIcon();
            }

            @Override
            public void onValidate(String textInput, TextInputLayout tillInput, final boolean fromScroll)
            {
                if(validateEmail(textInput, tillInput, true))
                {
                    setLoading(true, 1);
                    //Check already if exists
                    ParseQuery<ParseUser> queryEmailExists = ParseUser.getQuery();
                    queryEmailExists.whereEqualTo("email", textInput);
                    queryEmailExists.getFirstInBackground(new GetCallback<ParseUser>()
                    {
                        @Override
                        public void done(ParseUser object, ParseException e)
                        {
                            setLoading(false, 1);

                            if(e == null)
                            {
                                //if user exists treat as login else as a register
                                if(object != null)
                                {
                                    //setup for login instead of reg
                                    removeOnboarderPage(5);
                                    removeOnboarderPage(4);
                                    removeOnboarderPage(3);

                                    //Display forget password and change behaviour for login instead
                                    setLinkVisible(true, 2);

                                    isExistingUser = true;
                                }
                            }
                            else
                            {
                                if(e.getCode() == 101)
                                {
//                                    setLinkVisible(false, 2);

                                    //Check if pages have been removed previously by coincidence and add them back if so
//                                    reAddOnboarderPage(cardFullName, cardListenerFullName, 3);
//                                    reAddOnboarderPage(cardPhone, cardListenerPhone, 4);
//                                    reAddOnboarderPage(cardShare, cardListenerShare, 5);

//                                    isExistingUser = false;

                                }
                                else
                                    Toast.makeText(getApplicationContext(), "Error connecting: " + e.getCode() + " " + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            if(!fromScroll)
                                goToNextPage();
                        }
                    });
                }
                else
                {
                    setLoading(false, 1);

                    if(fromScroll)
                        goToPrevPage();

                    showRejectIcon();
                }
            }

            @Override
            public void onImeEnterPressed(TextInputLayout tillInput)
            {
                performNextClick();
            }

            @Override
            public void onLinkClick()
            {

            }

            @Override
            public void onFbClick()
            {
                //Handle fb login
                final List<String> lstPerms = new ArrayList<>();
                lstPerms.add("email");
                lstPerms.add("public_profile");

                ParseFacebookUtils.logInWithReadPermissionsInBackground(thisActivity, lstPerms, new LogInCallback()
                {
                    @Override
                    public void done(ParseUser parseUser, ParseException e)
                    {
                        Log.d("DebugFb", "Response received from fb login");
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

                            if(e != null)   //Check for null user might have canceled fb login
                            {
                                if(e.getCode() == 100)
                                    Snackbar.make(parentLayout, getString(R.string.error_100_no_internet), Snackbar.LENGTH_LONG).show();
                                else
                                    Snackbar.make(parentLayout, "Unsuccessful: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    }
                });
            }
        };


        List<AhoyOnboarderCard> cards = new ArrayList<>();
        cards.add(cardIntro);
        cards.add(cardEmail);
        cards.add(cardPassword);
        cards.add(cardFullName);
        cards.add(cardPhone);
        cards.add(cardShare);

        List<OnAhoyListeners> listeners = new ArrayList<OnAhoyListeners>();
        listeners.add(introListener);
        listeners.add(cardListenerEmail);
        listeners.add(cardListenerPassword);
        listeners.add(cardListenerFullName);
        listeners.add(cardListenerPhone);
        listeners.add(cardListenerShare);


        setOnboardPages(cards, listeners);

        setGradientBackground();

        //Show/Hide navigation controls
        showNavigationControls(true);

        //Set pager indicator colors
        setInactiveIndicatorColor(R.color.grey_600);
        setActiveIndicatorColor(R.color.grey_300);

        //Set finish button text
        setFinishButtonTitle("Let's Go");

        //check for already logged in user that is rembered
        ParseUser rememberedUser = ParseUser.getCurrentUser();

        if(rememberedUser != null)
        {
//            //Check for incomplete fb login / register
//            if(rememberedUser.has("authData") && !rememberedUser.has("facebookId"))
//                regWithFacebook(rememberedUser);
//            else
                login();
        }


    }

    private void regWithFacebook(ParseUser parseUser)
    {
        //Launches register activity with limited pre-populated fields
        //get auth data
        String authDataString = parseUser.getJSONObject("authData").toString();

        //Make sure auth data is not null
        if(authDataString != null)
        {
            if(!authDataString.isEmpty())
            {
                try
                {
                    JSONObject jsonAuthData = new JSONObject(authDataString);
                    final JSONObject jsonFbData = jsonAuthData.getJSONObject("facebook");


                    try
                    {
                        //Get fb auth token to make details graph request
                        List<String> perms = new ArrayList<String>();
                        perms.add("email");
                        perms.add("public_profile");

                        AccessToken fbToken = new AccessToken(jsonFbData.getString("access_token"), getApplicationInfo().metaData.getString("com.facebook.sdk.ApplicationId"), jsonFbData.getString("id"), perms, null, null, null, null);

                        //Request needed info
                        GraphRequest request = GraphRequest.newMeRequest(fbToken, new GraphRequest.GraphJSONObjectCallback()
                        {
                            @Override
                            public void onCompleted(JSONObject object, GraphResponse response)
                            {
                                try
                                {
                                    Log.d(TAG, "DebugFb Graph response: " + object.toString());

                                    //Pre populate data
                                    if(object.has("email"))
                                        setInputText(object.getString("email"), 1);

                                    if(object.has("name"))
                                        setInputText(object.getString("name"), 3);

                                    if(object.has("id"))
                                        facebookId = object.getString("id");
                                }
                                catch(JSONException je)
                                {
                                    je.printStackTrace();
                                }
                            }
                        });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }
                    catch(JSONException je)
                    {
                        je.printStackTrace();
                    }
                }
                catch(JSONException je)
                {
                    je.printStackTrace();
                }
            }
            else
                Log.e(TAG, "User facebook auth data is empty");
        }
        else
            Log.e(TAG, "Could not find any facebook auth data in user record");
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

        final Intent intentLogin = new Intent(ActivOnBoarding.this, ActivHome.class);

        if(openedVaiPush)   //add data if opened vai push
            intentLogin.putExtras(getIntent().getExtras());

        this.startActivity(intentLogin);
        this.finish();
    }

    public void subscribeToChannels(List<ParseObject> groups)
    {
        //sub to channel
        List<String> channelNames = new ArrayList<>();

        channelNames.add("");   //Add broadcast channel

        for(int i = 0; i < groups.size(); i++)
            channelNames.add(UtilFormating.formatChannelName(groups.get(i).getString("name")));

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
//                loading(true);

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
//                            loading(false);
                            Snackbar.make(parentLayout, "Reset Email Sent", Snackbar.LENGTH_LONG).show();
                        }
                        else
                        {
                            //Failed
//                            loading(false);

                            //No user found with that email address code 205
                            //Invalid email code 125
                            //Check internet connection code 100
                            if(e.getCode() == 205)
                                Snackbar.make(parentLayout, "No account associated with that email", Snackbar.LENGTH_LONG).show();
                            else if(e.getCode() == 125)
                                Snackbar.make(parentLayout, "Invalid email address", Snackbar.LENGTH_LONG).show();
                            else if(e.getCode() == 100)
                                Snackbar.make(parentLayout, "Check your internet connection and try again", Snackbar.LENGTH_LONG).show();
                            else
                                Snackbar.make(parentLayout, "Unsuccessful Email Reset: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
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

    @Override
    public void onFinishButtonPressed()
    {
        validateFinalPage();
    }

    private boolean validateFullName(String inputText, TextInputLayout tillFullName, boolean showErrorMsgs)
    {
        boolean valid = true;

        if(inputText.isEmpty())
        {
            valid = false;

            if(showErrorMsgs)
            {
                tillFullName.setError(getString(R.string.error_cannot_be_empty));
            }
        }

        if(!inputText.contains(" "))
        {
            valid = false;

            if(showErrorMsgs)
            {
                tillFullName.setError("Please provide your last name as well");
            }
        }

        return valid;
    }

    private boolean validateEmail(String inputText, TextInputLayout tillEmail, boolean showErrorMsgs)
    {
        boolean valid = true;

        if(!inputText.contains("@"))
        {
            valid = false;

            if(showErrorMsgs)
                tillEmail.setError("Must contain @");
        }

        //Check for period after an @ sign
        String email[] = inputText.split("@");

        //check only one @ sign existed
        if(email.length > 2)
        {
            valid = false;

            if(showErrorMsgs)
                tillEmail.setError("Must only have one @");
        }
        else if(email.length == 2)
        {
            if(!email[1].contains("."))
            {
                valid = false;

                if(showErrorMsgs)
                    tillEmail.setError("Domain must have .");
            }
            else if((email[1].charAt(email[1].length() -1) + "").equals("."))
            {
                valid = false;

                if(showErrorMsgs)
                    tillEmail.setError("domain cannot end with a .");
            }
        }
        else
        {
            valid = false;

            if(showErrorMsgs)
                tillEmail.setError("Must have domain name ie @domainname.com");
        }

        return valid;
    }

    private boolean validatePassword(String inputText, TextInputLayout tillPassword, boolean showErrorMsgs)
    {
        boolean valid = true;

        if(inputText.length() < 6)
        {
            valid = false;

            if(showErrorMsgs)
                tillPassword.setError("Must be longer than 5 characters");
        }

        return valid;
    }

    private boolean validatePhone(String inputText, TextInputLayout tillPhone, boolean showErrorMsgs)
    {
        boolean valid = true;

        //Validation rules
        if(inputText.length() < 10)
        {
            valid = false;

            if(showErrorMsgs)
                tillPhone.setError("Must be 10 or more digits");
        }

        return valid;
    }

    private boolean validateReferralCode(String inputText, TextInputLayout tillReferralCode, boolean showErrorMsgs)
    {
        boolean valid = true;

        if(inputText.length() > 0 && inputText.length() < 3)
        {
            valid = false;

            if(showErrorMsgs)
                tillReferralCode.setError("Referral codes can't be less than 3 characters");
        }

        return valid;
    }

    private void signUpNewUser(final TextInputLayout tillLast)
    {
        String[] inputData = getAllInputData();

        for (int i = 0; i < inputData.length; i++)
            Log.d("debugRegister", "data " + i + ": " +
                    inputData[i]);

        final String referralCode = inputData[5];

        final ParseUser registerUser = new ParseUser();
        registerUser.put("name", inputData[3]);
        registerUser.setUsername(inputData[1]);
        registerUser.setEmail(inputData[1]);
        registerUser.setPassword(inputData[2]);
        registerUser.put("cellNumber", inputData[4]);

        registerUser.put("numberOfGroups", 100);

        if(referralCode.length() != 0)
        {
            //format // normalise referal code to a flat group name
            String formattedReferralCode = UtilFormating.formatGroupFlatName(referralCode);

            //Check already if exists
            ParseQuery<ParseObject> queryGroupExists = ParseQuery.getQuery("Groups");
            queryGroupExists.whereEqualTo("referralCode", formattedReferralCode);
            queryGroupExists.getFirstInBackground(new GetCallback<ParseObject>()
            {
                @Override
                public void done(ParseObject group, ParseException e)
                {
                    String msg = "";
                    if (e == null)
                    {
                        //An obj was found
                        registerUser.add("groups", group.getString("name"));

                        doSignUp(registerUser, tillLast, group);

                    } else
                    {
                        //Unsuccessful
                        //No obj found (Success)
                        //Check internet connection code 100
                        if (e.getCode() == 101)
                        {
                            showRejectIcon();
                            tillLast.setError("Referral Code does not exist");
                        }
                        else if (e.getCode() == 100)
                        {
                            animateLoading(false);
                            msg = "Check your internet connection and try again";
                        }
                        else
                        {
                            animateLoading(false);
                            msg = "Unsuccessful while checking if group name exists: " + e.getMessage() + " Code: " + e.getCode();
                        }
                    }

                    if (!msg.isEmpty())
                    {
                        Snackbar.make(parentLayout, msg, Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                signUpNewUser(tillLast);
                            }
                        }).show();
                    }
                }
            });
        }
        else
            doSignUp(registerUser, tillLast, null);
    }

    private void doSignUp(final ParseUser registerUser, final TextInputLayout tillLast, final ParseObject group)
    {
        //Loading animation
        animateLoading(true);
        registerUser.signUpInBackground(new SignUpCallback()
        {
            @Override
            public void done(ParseException e)
            {

                if (e == null)
                {
                    //init sharedPrefs
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
                    sharedPrefs.edit().putBoolean("panicDelay", true).commit();

                    Snackbar.make(parentLayout, "You have successfully been registered", Snackbar.LENGTH_LONG).show();

                    //Update group with parse user id if a referral code was used
                    if(group != null)
                        subUserToGroup(registerUser, group);
                    else    //Log user in via shared prefs
                    {
                        String[] allInputData = getAllInputData();

                        //Attempt to login user
                        ParseUser.logInInBackground(allInputData[1], allInputData[2], new LogInCallback() {
                            @Override
                            public void done(ParseUser parseUser, ParseException e)
                            {
                                animateLoading(false);
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

                                    if(e.getCode() == 101)
                                        Snackbar.make(parentLayout, "Invalid Username or Password", Snackbar.LENGTH_LONG).show();
                                    else if(e.getCode() == 100)
                                        Snackbar.make(parentLayout, "Please check you internet connection and try again", Snackbar.LENGTH_LONG).show();
                                    else
                                        Snackbar.make(parentLayout, "Unsuccesfull: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
                                }
                            }
                        });
                    }


                } else
                {
                    //Username already exists code 202
                    //Email already exists code 203
                    //Invalid Email code 125
                    //Interconnection down code 100
                    //Read exception
                    if (e.getCode() == 202)
                    {
                        TextInputLayout tillEmail = goToPage(1);
                        tillEmail.setError("An account with this email already exists");
                    }
                    else if (e.getCode() == 203)
                    {
                        TextInputLayout tillEmail = goToPage(1);
                        tillEmail.setError("An account with this email already exists");
                    }
                    else if (e.getCode() == 125)
                    {
                        TextInputLayout tillEmail = goToPage(1);
                        tillEmail.setError("The email you entered is invalid");
                    }
                    else if (e.getCode() == 100)
                    {
                        Snackbar.make(parentLayout, "Please check your internet connection and try again",Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                signUpNewUser(tillLast);
                            }
                        }).show();
                    }
                    else
                        Snackbar.make(parentLayout, "Registration was unsuccessful: " + e.getMessage() + " Code: " + e.getCode(),Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View view)
                            {
                                signUpNewUser(tillLast);
                            }
                        }).show();
                }
            }
        });
    }

    private void subUserToGroup(final ParseUser registerUser , final ParseObject group)
    {
        group.addUnique("subscriberObjects", registerUser.getObjectId());
        group.increment("subscribers");
        group.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if(e == null)
                {
//                    Intent result = new Intent();
//                    result.putExtra("email", registerUser.getEmail());
//                    result.putExtra("password", password);
//                    setResult(Activity.RESULT_OK, result);
//                    finish();
                    login();
                }
                else if (e.getCode() == 100)
                {
                    Snackbar.make(parentLayout, "Please check your internet connection and try again, Joining referral code's group was only partially completed",Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            subUserToGroup(registerUser, group);
                        }
                    }).show();
                }
                else
                    Snackbar.make(parentLayout, "Joining referral code's group was only partially completed: " + e.getMessage() + " Code: " + e.getCode(),Snackbar.LENGTH_INDEFINITE).setAction("Retry", new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            subUserToGroup(registerUser, group);
                        }
                    }).show();
            }
        });
    }

    int backPressedCount = 0;
    long lastBackPressedAt = 0;

    @Override
    public void onBackPressed()
    {
        if(backPressedCount > 0 && ((SystemClock.elapsedRealtime() - lastBackPressedAt) < 3000))
        {
            UtilAnalytics.logEventRegDropOff(getCurrentPageIndex() ,getCurrentPageName());
            finish();
        }
        else
        {
            backPressedCount++;
            lastBackPressedAt = SystemClock.elapsedRealtime();
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
        }

    }
}
