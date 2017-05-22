package za.co.oneohtwofour.brave;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

import java.util.Arrays;


public class RegisterActivity extends ActionBarActivity implements AdapterView.OnItemSelectedListener, SwipeRefreshLayout.OnRefreshListener, TextView.OnEditorActionListener{

    //All fields required for registration
    private EditText etxtRegisterNameAndSurname;
    private EditText etxtRegisterUsername;
    private EditText etxtRegisterCellNumber;
    private EditText etxtRegisterEmail;
    private Spinner spnrRegisterCountry;
    boolean noCountrySelected;
    private Button btnChooseCountry;
    private EditText etxtRegisterPassword;
    private EditText etxtRegisterRetypePassword;
    private CheckBox cbtermsAndConditions;
    private SwipeRefreshLayout srLayRegister;
    private ScrollView scrvRegisterDetails;

    private final RegisterActivity thisActivity = this;
    private final String TAG = "activityRegister";

    private final int NO_OF_FREE_GROUPS_ON_REGISTER = 3;
    private final String TERMS_AND_CONDITIONS_LINK = "http://www.panic-sec.org/terms/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //Hide status notification bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        //Initialise all fields
        etxtRegisterNameAndSurname = (EditText) findViewById(R.id.etxtRegisterNameAndSurname);
        etxtRegisterUsername = (EditText) findViewById(R.id.etxtRegisterUsername);
        etxtRegisterCellNumber = (EditText) findViewById(R.id.etxtRegisterCellNumber);
        etxtRegisterEmail = (EditText) findViewById(R.id.etxtRegisterEmail);

        noCountrySelected = true;
        spnrRegisterCountry = (Spinner) findViewById(R.id.spnrRegisterCountry);
        spnrRegisterCountry.setSelection(-1);
        spnrRegisterCountry.setOnItemSelectedListener(this);

        btnChooseCountry = (Button) findViewById(R.id.btnChooseCountry);

        etxtRegisterPassword = (EditText) findViewById(R.id.etxtRegisterPassword);
        etxtRegisterRetypePassword = (EditText) findViewById(R.id.etxtRegisterRetypePassword);
        etxtRegisterRetypePassword.setOnEditorActionListener(this);

        cbtermsAndConditions = (CheckBox) findViewById(R.id.cbTermsAndConditions);

        scrvRegisterDetails = (ScrollView) findViewById(R.id.scrvRegisterDetails);

        srLayRegister = (SwipeRefreshLayout) findViewById(R.id.srLayRegister);
        initSwipeRefresh();
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

    public void onClickSubmit(View view)
    {
       // String name = etxtUsername.getText().toString();
       //Toast.makeText(this, "Perfom submition procedures: " + etxtUsername.getText().toString() + " " + etxtPassword.getText().toString()
         //       + " " + etxtCellNumber.getText().toString() + " " + etxtNameAndSurname.getText().toString(), Toast.LENGTH_LONG).show();

        //Hide keyboard
        //clear all focus on views by requesting focus on root layout
        LinearLayout linLayRegisterRoot = (LinearLayout) findViewById(R.id.linLayRegisterRoot);
        linLayRegisterRoot.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(this.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(etxtRegisterRetypePassword.getWindowToken(), 0);

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

        if(etxtRegisterUsername.getText().toString().isEmpty())
        {
            valid = false;
            etxtRegisterUsername.setError("Can't be blank");
        }
        else if(etxtRegisterUsername.getText().toString().length() < 5)
        {
            valid = false;
            etxtRegisterUsername.setError("Must be at least 5 characters");
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

        if(etxtRegisterEmail.getText().toString().isEmpty())
        {
            valid = false;
            etxtRegisterEmail.setError("Can't be blank");
        }

        if(btnChooseCountry.getText().toString().equalsIgnoreCase("Choose Country"))
        {
            valid = false;
            btnChooseCountry.setError("Have to choose");
        }

        if(etxtRegisterPassword.getText().toString().isEmpty())
        {
            valid = false;
            etxtRegisterPassword.setError("Can't be blank");
        }

        if(etxtRegisterRetypePassword.getText().toString().isEmpty())
        {
            valid = false;
            etxtRegisterRetypePassword.setError("Can't be blank");
        }
        else if(!etxtRegisterRetypePassword.getText().toString().equals(etxtRegisterPassword.getText().toString()))
        {
            valid = false;
            etxtRegisterRetypePassword.setError("Passwords don't match");
        }

        if(valid)
        {
            final ParseUser registerUser = new ParseUser();
            registerUser.put("name", etxtRegisterNameAndSurname.getText().toString());
            registerUser.setUsername(etxtRegisterUsername.getText().toString());
            registerUser.put("cellNumber", etxtRegisterCellNumber.getText().toString());
            registerUser.setEmail(etxtRegisterEmail.getText().toString());
            registerUser.put("country", ((TextView) spnrRegisterCountry.getSelectedView()).getText().toString());
            registerUser.setPassword(etxtRegisterPassword.getText().toString());

            registerUser.put("numberOfGroups", NO_OF_FREE_GROUPS_ON_REGISTER);
            registerUser.add("groups", "");

            final Toast succesfulRegisterMsg = Toast.makeText(this, "You have successfully been registred", Toast.LENGTH_LONG);
            final Toast UnsuccessfulRegisterMsg = Toast.makeText(this, "", Toast.LENGTH_LONG);
            final Intent intentHome = new Intent(this, HomeActivity.class);

            //Loading animation
            //        final ProgressBar progbRegister = (ProgressBar) findViewById(R.id.progbRegister);
            //        progbRegister.setVisibility(View.VISIBLE);
            srLayRegister.setRefreshing(true);
            registerUser.signUpInBackground(new SignUpCallback()
            {
                @Override
                public void done(ParseException e)
                {
                    if (e == null)
                    {
                        //                    progbRegister.setVisibility(View.GONE);
                        //init sharedPrefs
                        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplication());
                        sharedPrefs.edit().putBoolean("panicDelay", true).commit();

                        srLayRegister.setRefreshing(false);
                        succesfulRegisterMsg.show();

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
                        //                    progbRegister.setVisibility(View.GONE);
                        srLayRegister.setRefreshing(false);
                        if (e.getCode() == 202)
                            etxtRegisterUsername.setError("An account with this username already exists");
                        else if (e.getCode() == 203)
                            etxtRegisterEmail.setError("An account with this email already exists");
                        else if (e.getCode() == 125)
                            etxtRegisterEmail.setError("The email you entered is invalid");    //UnsuccessfulRegisterMsg.setText("The email you entered is invalid");
                        else if (e.getCode() == 100)
                            UnsuccessfulRegisterMsg.setText("Please check your internet connection and try again");
                        else if (e.getCode() == -1)
                            etxtRegisterUsername.setError("Username cannot be blank");
                        else
                            UnsuccessfulRegisterMsg.setText("Registration was unsuccessful: " + e.getMessage() + " Code: " + e.getCode());

                        UnsuccessfulRegisterMsg.show();
                    }
                }
            });
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        if(!noCountrySelected)  //Skip initial selected item
        {
            String countryText = spnrRegisterCountry.getSelectedItem().toString();
            btnChooseCountry.setTextColor(getResources().getColor(R.color.White));
            btnChooseCountry.setText(countryText);
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
