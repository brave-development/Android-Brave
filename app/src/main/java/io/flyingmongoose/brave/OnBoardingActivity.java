package io.flyingmongoose.brave;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import com.codemybrainsout.onboarder.AhoyOnboarderActivity;
import com.codemybrainsout.onboarder.AhoyOnboarderCard;
import com.codemybrainsout.onboarder.OnTextInputProvidedListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wprenison on 2017/08/02.
 */

public class OnBoardingActivity extends AhoyOnboarderActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        //Set basic page info
        AhoyOnboarderCard cardFullName = new AhoyOnboarderCard("What's your name, friend?", "Full Name", R.drawable.ic_fullname, R.drawable.ic_accept_fullname, R.drawable.ic_reject_fullname, AhoyOnboarderCard.OnboardType.TEXT_INUPT);
        AhoyOnboarderCard cardEmail = new AhoyOnboarderCard("Promise I don't spam...", "you@youremail.com", R.drawable.ic_email, R.drawable.ic_accept_email, R.drawable.ic_reject_email, AhoyOnboarderCard.OnboardType.TEXT_INUPT);
        AhoyOnboarderCard cardPassword = new AhoyOnboarderCard("Sssshhh...", "Create a password", R.drawable.ic_password, R.drawable.ic_accept_password, R.drawable.ic_reject_password, AhoyOnboarderCard.OnboardType.TEXT_INUPT);
        AhoyOnboarderCard cardPhone = new AhoyOnboarderCard("Call me maybe? ;)", "+27721234567", R.drawable.ic_phone, R.drawable.ic_accept_phone, R.drawable.ic_reject_phone, AhoyOnboarderCard.OnboardType.TEXT_INUPT);
        AhoyOnboarderCard cardShare = new AhoyOnboarderCard("Got a referral code?", "Referral Code", R.drawable.ic_network, R.drawable.ic_accept_network, R.drawable.ic_reject_network, AhoyOnboarderCard.OnboardType.TEXT_INPUT_SHARE_OPTION);

        // You can define title and description colors (by default white)
        cardFullName.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardFullName.setDescriptionColor(R.color.common_google_signin_btn_text_dark);
        cardFullName.setBackgroundColor(R.color.black_transparent);
        cardFullName.setTitleTextSize(dpToPixels(10, this));
        cardFullName.setDescriptionTextSize(dpToPixels(8, this));
        cardFullName.setInputType(InputType.TYPE_CLASS_TEXT, InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        OnTextInputProvidedListener listener1 = new OnTextInputProvidedListener()
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
        };


        cardEmail.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardEmail.setDescriptionColor(R.color.common_google_signin_btn_text_dark);
        cardEmail.setBackgroundColor(R.color.black_transparent);
        cardEmail.setTitleTextSize(dpToPixels(10, this));
        cardEmail.setDescriptionTextSize(dpToPixels(8, this));
        cardEmail.setInputType(InputType.TYPE_CLASS_TEXT, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        OnTextInputProvidedListener listener2 = new OnTextInputProvidedListener()
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
            public void onValidate(String textInput, TextInputLayout tillInput, boolean fromScroll)
            {
                if(validateEmail(textInput, tillInput, true))
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
        };

        cardPassword.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardPassword.setDescriptionColor(R.color.common_google_signin_btn_text_dark);
        cardPassword.setBackgroundColor(R.color.black_transparent);
        cardPassword.setTitleTextSize(dpToPixels(10, this));
        cardPassword.setDescriptionTextSize(dpToPixels(8, this));
        cardPassword.setInputType(InputType.TYPE_CLASS_TEXT, InputType.TYPE_TEXT_VARIATION_PASSWORD);
        OnTextInputProvidedListener listener3 = new OnTextInputProvidedListener()
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
        };

        cardPhone.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardPhone.setDescriptionColor(R.color.common_google_signin_btn_text_dark);
        cardPhone.setBackgroundColor(R.color.black_transparent);
        cardPhone.setTitleTextSize(dpToPixels(10, this));
        cardPhone.setDescriptionTextSize(dpToPixels(8, this));
        cardPhone.setInputType(InputType.TYPE_CLASS_PHONE, InputType.TYPE_NULL);
        OnTextInputProvidedListener listener4 = new OnTextInputProvidedListener()
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
        };

        cardShare.setTitleColor(R.color.common_google_signin_btn_text_dark);
        cardShare.setDescriptionColor(R.color.common_google_signin_btn_text_dark);
        cardShare.setBackgroundColor(R.color.black_transparent);
        cardShare.setTitleTextSize(dpToPixels(10, this));
        cardShare.setDescriptionTextSize(dpToPixels(8, this));
        cardShare.setInputType(InputType.TYPE_CLASS_TEXT, InputType.TYPE_NULL);
        cardShare.setShareTitle("Share");
        cardShare.setShareTitleColor(R.color.common_google_signin_btn_text_dark);
        OnTextInputProvidedListener listener5 = new OnTextInputProvidedListener()
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
        };


        List<AhoyOnboarderCard> cards = new ArrayList<>();
        cards.add(cardFullName);
        cards.add(cardEmail);
        cards.add(cardPassword);
        cards.add(cardPhone);
        cards.add(cardShare);

        List<OnTextInputProvidedListener> listeners = new ArrayList<OnTextInputProvidedListener>();
        listeners.add(listener1);
        listeners.add(listener2);
        listeners.add(listener3);
        listeners.add(listener4);
        listeners.add(listener5);


        setOnboardPages(cards, listeners);

        setGradientBackground();

        //Show/Hide navigation controls
        showNavigationControls(true);

        //Set pager indicator colors
        setInactiveIndicatorColor(R.color.grey_600);
        setActiveIndicatorColor(R.color.grey_300);

        //Set finish button text
        setFinishButtonTitle("Get Started");
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

        final String referralCode = inputData[4];

        final ParseUser registerUser = new ParseUser();
        registerUser.put("name", inputData[0]);
        registerUser.setUsername(inputData[1]);
        registerUser.setEmail(inputData[1]);
        registerUser.setPassword(inputData[2]);
        registerUser.put("cellNumber", inputData[3]);

        registerUser.put("numberOfGroups", 100);

        if(referralCode.length() != 0)
        {
            //format // normalise referal code to a flat group name
            String formattedReferralCode = FormatHelper.formatGroupFlatName(referralCode);

            //Check already if exists
            ParseQuery<ParseObject> queryGroupExists = ParseQuery.getQuery("Groups");
            queryGroupExists.whereEqualTo("flatValue", formattedReferralCode);
            queryGroupExists.getFirstInBackground(new GetCallback<ParseObject>()
            {
                @Override
                public void done(ParseObject group, ParseException e)
                {
                    String msg = "";
                    if (e == null)
                    {
                        //An obj was found
                        registerUser.add("Groups", group);

                        doSignUp(registerUser, tillLast);

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
            doSignUp(registerUser, tillLast);
    }

    private void doSignUp(ParseUser registerUser, final TextInputLayout tillLast)
    {
        //Loading animation
        animateLoading(true);
        registerUser.signUpInBackground(new SignUpCallback()
        {
            @Override
            public void done(ParseException e)
            {
                animateLoading(false);

                if (e == null)
                {
                    //init sharedPrefs
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
                    sharedPrefs.edit().putBoolean("panicDelay", true).commit();

                    Snackbar.make(parentLayout, "You have successfully been registered", Snackbar.LENGTH_LONG).show();

                    finish();

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
}
