package io.flyingmongoose.brave;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.SignUpCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class RegisterActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener, TextView.OnEditorActionListener{

    //All fields required for registration
    private EditText etxtRegisterNameAndSurname;
    private FrameLayout fLayUsername;
    private EditText etxtRegisterUsername;
    private EditText etxtRegisterCellNumber;
    private FrameLayout fLayEmail;
    private EditText etxtRegisterEmail;
    private Spinner spnrRegisterCountry;
    boolean noCountrySelected;
    private Button btnChooseCountry;
    private FrameLayout fLayPassword;
    private EditText etxtRegisterPassword;
    private FrameLayout fLayRetypePassword;
    private EditText etxtRegisterRetypePassword;
    private CheckBox cbtermsAndConditions;
    private SwipeRefreshLayout srLayRegister;
    private ScrollView scrvRegisterDetails;

    private final RegisterActivity thisActivity = this;
    private final String TAG = "activityRegister";

    private final int NO_OF_FREE_GROUPS_ON_REGISTER = 3;
    private final String TERMS_AND_CONDITIONS_LINK = "http://www.panic-sec.org/terms/";
    private String facebookId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Initialise all fields
        etxtRegisterNameAndSurname = (EditText) findViewById(R.id.etxtRegisterNameAndSurname);
        fLayUsername = (FrameLayout) findViewById(R.id.fLayRegisterUsername);
        etxtRegisterUsername = (EditText) findViewById(R.id.etxtRegisterUsername);
        etxtRegisterCellNumber = (EditText) findViewById(R.id.etxtRegisterCellNumber);
        fLayEmail = (FrameLayout) findViewById(R.id.fLayRegisterEmail);
        etxtRegisterEmail = (EditText) findViewById(R.id.etxtRegisterEmail);

        noCountrySelected = true;
        spnrRegisterCountry = (Spinner) findViewById(R.id.spnrRegisterCountry);
        spnrRegisterCountry.setSelection(-1);
        spnrRegisterCountry.setOnItemSelectedListener(this);

        btnChooseCountry = (Button) findViewById(R.id.btnChooseCountry);

        fLayPassword = (FrameLayout) findViewById(R.id.fLayRegisterPassword);
        etxtRegisterPassword = (EditText) findViewById(R.id.etxtRegisterPassword);
        fLayRetypePassword = (FrameLayout) findViewById(R.id.fLayRegisterRetypePassword);
        etxtRegisterRetypePassword = (EditText) findViewById(R.id.etxtRegisterRetypePassword);
        etxtRegisterRetypePassword.setOnEditorActionListener(this);

        cbtermsAndConditions = (CheckBox) findViewById(R.id.cbTermsAndConditions);

        scrvRegisterDetails = (ScrollView) findViewById(R.id.scrvRegisterDetails);

        srLayRegister = (SwipeRefreshLayout) findViewById(R.id.srLayRegister);
        initSwipeRefresh();

        //Check to trigger select country dialog
        etxtRegisterEmail.setOnEditorActionListener(new TextView.OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent)
            {
                btnChooseCountry.performClick();
                return true;
            }
        });

        if(getIntent().getBooleanExtra("regWithFacebook", false))
        {
            Log.d("fbLogin", "init as reg with fb");
            initAsFbReg();
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
    {
        if(actionId == EditorInfo.IME_ACTION_DONE)
        {
            //Scroll to check box
            scrvRegisterDetails.post(new Runnable()
            {
                @Override
                public void run()
                {
                    scrvRegisterDetails.scrollTo(0, cbtermsAndConditions.getBottom());
                }
            });
        }

        return false;
    }

    private void initSwipeRefresh()
    {
        srLayRegister.setEnabled(false);
        srLayRegister.setOnRefreshListener(this);
        srLayRegister.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayRegister.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayRegister.setProgressViewOffset(true, 0, 8);
    }

    private void initAsFbReg()
    {
        //get auth data
        String authDataString = getIntent().getStringExtra("authData");

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
                                    if(object.has("name"))
                                        etxtRegisterNameAndSurname.setText(object.getString("name"));

                                    if(object.has("email"))
                                    {
                                        fLayUsername.setVisibility(View.GONE);
                                        fLayEmail.setVisibility(View.GONE);
                                        etxtRegisterEmail.setVisibility(View.INVISIBLE);
                                        etxtRegisterEmail.setText(object.getString("email"));
                                        etxtRegisterEmail.setEnabled(false);
                                        fLayPassword.setVisibility(View.GONE);
                                        etxtRegisterPassword.setVisibility(View.INVISIBLE);
                                        etxtRegisterPassword.setEnabled(false);
                                        fLayRetypePassword.setVisibility(View.GONE);
                                        etxtRegisterRetypePassword.setVisibility(View.INVISIBLE);
                                        etxtRegisterRetypePassword.setEnabled(false);
                                    }

                                    if(object.has("id"))
                                        facebookId = object.getString("id");

                                    etxtRegisterUsername.setVisibility(View.INVISIBLE);
                                    etxtRegisterUsername.setEnabled(false);

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

    @Override
    public void onRefresh()
    {
        srLayRegister.setRefreshing(true);
        srLayRegister.setRefreshing(false);
    }

    public void onClickChooseCountry(View view)
    {
        spnrRegisterCountry.performClick();
    }

    private boolean validate()
    {
        //Validation before registration here
        boolean valid = true;

        //Check ts and cs are agreed too
        if(!cbtermsAndConditions.isChecked())
        {
            valid = false;
            cbtermsAndConditions.setError("You must accept the t's abd c's to register an account");

            //Scroll to check box
            scrvRegisterDetails.post(new Runnable()
            {
                @Override
                public void run()
                {
                    scrvRegisterDetails.scrollTo(0, cbtermsAndConditions.getBottom());
                }
            });
        }

        //Check for blank values
        if(etxtRegisterNameAndSurname.getText().toString().isEmpty())
        {
            valid = false;
            etxtRegisterNameAndSurname.setError("Can't be blank");
        }

        if(etxtRegisterUsername.isEnabled())
        {
            if(etxtRegisterUsername.getText().toString().isEmpty())
            {
                valid = false;
                etxtRegisterUsername.setError("Can't be blank");
            } else if(etxtRegisterUsername.getText().toString().length() < 5)
            {
                valid = false;
                etxtRegisterUsername.setError("Must be at least 5 characters");
            }
        }

        if(etxtRegisterCellNumber.getText().toString().isEmpty())
        {
            valid = false;
            etxtRegisterCellNumber.setError("Can't be blank");
        }
        else if(etxtRegisterCellNumber.getText().toString().length() < 10 || etxtRegisterCellNumber.getText().toString().length() > 10)
        {
            valid = false;
            etxtRegisterCellNumber.setError("Number must be 10 digits");
        }

        if(etxtRegisterEmail.isEnabled())
        {
            if(etxtRegisterEmail.getText().toString().isEmpty())
            {
                valid = false;
                etxtRegisterEmail.setError("Can't be blank");
            }
        }

        if(btnChooseCountry.getText().toString().equalsIgnoreCase("Choose Country"))
        {
            valid = false;
            btnChooseCountry.setError("Have to choose");
        }

        if(etxtRegisterPassword.isEnabled())
        {
            if(etxtRegisterPassword.getText().toString().isEmpty())
            {
                valid = false;
                etxtRegisterPassword.setError("Can't be blank");
            }

            if(etxtRegisterRetypePassword.getText().toString().isEmpty())
            {
                valid = false;
                etxtRegisterRetypePassword.setError("Can't be blank");
            } else if(!etxtRegisterRetypePassword.getText().toString().equals(etxtRegisterPassword.getText().toString()))
            {
                valid = false;
                etxtRegisterRetypePassword.setError("Passwords don't match");
            }
        }

        return valid;
    }

    public void onClickSubmit(View view)
    {
        //Hide keyboard
        //clear all focus on views by requesting focus on root layout
        LinearLayout linLayRegisterRoot = (LinearLayout) findViewById(R.id.linLayRegisterRoot);
        linLayRegisterRoot.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etxtRegisterRetypePassword.getWindowToken(), 0);


        if(validate())
        {
            if(getIntent().getBooleanExtra("regWithFacebook", false))
                updateNewUser();
            else
                signUpNewUser();
        }
    }

    //User is already created when using facebook
    private void updateNewUser()
    {
        final ParseUser currUser = ParseUser.getCurrentUser();
        currUser.put("facebookId", facebookId);
        currUser.put("name", etxtRegisterNameAndSurname.getText().toString().trim());
        currUser.put("email", etxtRegisterEmail.getText().toString().trim());
        currUser.put("cellNumber", etxtRegisterCellNumber.getText().toString().trim());
        currUser.put("country", ((TextView) spnrRegisterCountry.getSelectedView()).getText().toString().trim());
        currUser.put("numberOfGroups", NO_OF_FREE_GROUPS_ON_REGISTER);
        currUser.add("groups", "");

        //Loading animation
        srLayRegister.setRefreshing(true);
        currUser.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e == null)
                {
                    //init sharedPrefs
                    SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
                    sharedPrefs.edit().putBoolean("panicDelay", true).commit();

                    srLayRegister.setRefreshing(false);
                    Snackbar.make(srLayRegister, "You have successfully been registered", Snackbar.LENGTH_LONG).show();

                    currUser.removeAll("groups", Arrays.asList(""));
                    currUser.saveInBackground();

                    thisActivity.setResult(Activity.RESULT_OK);
                    thisActivity.finish();
                } else
                {
                    //Username already exists code 202
                    //Email already exists code 203
                    //Invalid Email code 125
                    //Interconnection down code 100
                    //Read exception
                    //                    progbRegister.setVisibility(View.GONE);
                    srLayRegister.setRefreshing(false);
                    if (e.getCode() == 202)
                        etxtRegisterUsername.setError("An account with this username already exists");
                    else if (e.getCode() == 203)
                        etxtRegisterEmail.setError("An account with this email already exists");
                    else if (e.getCode() == 125)
                        etxtRegisterEmail.setError("The email you entered is invalid");    //UnsuccessfulRegisterMsg.setText("The email you entered is invalid");
                    else if (e.getCode() == 100)
                        Snackbar.make(srLayRegister, "Please check your internet connection and try again", Snackbar.LENGTH_LONG).show();
                    else if (e.getCode() == -1)
                        etxtRegisterUsername.setError("Username cannot be blank");
                    else
                        Snackbar.make(srLayRegister, "Registration was unsuccessful: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    private void signUpNewUser()
    {
        final ParseUser registerUser = new ParseUser();
        registerUser.put("name", etxtRegisterNameAndSurname.getText().toString().trim());
        registerUser.setUsername(etxtRegisterUsername.getText().toString().trim());
        registerUser.put("cellNumber", etxtRegisterCellNumber.getText().toString().trim());
        registerUser.setEmail(etxtRegisterEmail.getText().toString().trim());
        registerUser.put("country", ((TextView) spnrRegisterCountry.getSelectedView()).getText().toString().trim());
        registerUser.setPassword(etxtRegisterPassword.getText().toString());

        registerUser.put("numberOfGroups", NO_OF_FREE_GROUPS_ON_REGISTER);
        registerUser.add("groups", "");

        //Loading animation
        srLayRegister.setRefreshing(true);
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

                    srLayRegister.setRefreshing(false);
                    Snackbar.make(srLayRegister, "You have successfully been registered", Snackbar.LENGTH_LONG).show();

                    registerUser.removeAll("groups", Arrays.asList(""));
                    registerUser.saveInBackground();

                    thisActivity.setResult(Activity.RESULT_OK);
                    thisActivity.finish();
                } else
                {
                    //Username already exists code 202
                    //Email already exists code 203
                    //Invalid Email code 125
                    //Interconnection down code 100
                    //Read exception
                    srLayRegister.setRefreshing(false);
                    if (e.getCode() == 202)
                        etxtRegisterUsername.setError("An account with this username already exists");
                    else if (e.getCode() == 203)
                        etxtRegisterEmail.setError("An account with this email already exists");
                    else if (e.getCode() == 125)
                        etxtRegisterEmail.setError("The email you entered is invalid");    //UnsuccessfulRegisterMsg.setText("The email you entered is invalid");
                    else if (e.getCode() == 100)
                        Snackbar.make(srLayRegister, "Please check your internet connection and try again", Snackbar.LENGTH_LONG).show();
                    else if (e.getCode() == -1)
                        etxtRegisterUsername.setError("Username cannot be blank");
                    else
                        Snackbar.make(srLayRegister, "Registration was unsuccessful: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if(!noCountrySelected)  //Skip initial selected item
        {
            String countryText = spnrRegisterCountry.getSelectedItem().toString();
            btnChooseCountry.setTextColor(getResources().getColor(R.color.White));
            btnChooseCountry.setText(countryText);
            etxtRegisterPassword.requestFocus();
        }

        noCountrySelected = !noCountrySelected;
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {
        btnChooseCountry.setTextColor(getResources().getColor(R.color.HintWhite));
        btnChooseCountry.setText("Choose Country >");
    }

    public void onClickTermsAndConditionsLink(View view)
    {
        //Open link trough intent
        Intent openLinkIntent = new Intent(Intent.ACTION_VIEW);
        openLinkIntent.setData(Uri.parse(TERMS_AND_CONDITIONS_LINK));
        startActivity(openLinkIntent);
    }
}
