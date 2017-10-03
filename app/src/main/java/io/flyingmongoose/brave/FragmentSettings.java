package io.flyingmongoose.brave;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SwitchCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.List;


public class FragmentSettings extends Fragment implements View.OnFocusChangeListener, TextView.OnEditorActionListener, View.OnClickListener, View.OnTouchListener, AdapterView.OnItemSelectedListener, CompoundButton.OnCheckedChangeListener
{
    private String TAG = "FragmentSettings";

    private EditText etxtName;
    private EditText etxtCellNumber;
    private EditText etxtEmail;
    private Spinner spnrCountry;
    private Button btnLogout;
    private Button btnDelete;
    private TextView txtvTutorial;
    private TextView txtvReportBug;
    private TextView txtvReportUser;
    private TextView txtvCountry;
    private ImageButton ibtnSettingsHelpPanicConfirmation;
    private SwitchCompat sSettingsPanicConfirmation;
    private SwitchCompat sSettingsNotifications;

    private boolean startUpHideComplete = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        etxtName = (EditText) view.findViewById(R.id.etxtSettingsName);
        etxtCellNumber = (EditText) view.findViewById(R.id.etxtSettingsCellNumber);
        etxtEmail = (EditText) view.findViewById(R.id.etxtSettingsEmail);

        spnrCountry = (Spinner) view.findViewById(R.id.spnrSettingsCountry);
        spnrCountry.setOnItemSelectedListener(this);

        txtvCountry = (TextView) view.findViewById(R.id.txtvSettingsChooseCountry);
        txtvCountry.setOnClickListener(this);
        txtvCountry.setOnTouchListener(this);

        btnLogout = (Button) view.findViewById(R.id.btnSettingsLogout);
        btnLogout.setOnClickListener(this);
        btnLogout.setOnTouchListener(this);

        btnDelete = (Button) view.findViewById(R.id.btnSettingsDeleteAccount);
        btnDelete.setOnClickListener(this);
        btnDelete.setOnTouchListener(this);

        txtvTutorial = (TextView) view.findViewById(R.id.txtvSettingsTutorial);
        txtvTutorial.setOnClickListener(this);
        txtvTutorial.setOnTouchListener(this);

        txtvReportBug = (TextView) view.findViewById(R.id.txtvSettingsReportBug);
        txtvReportBug.setOnClickListener(this);
        txtvReportBug.setOnTouchListener(this);

        txtvReportUser = (TextView) view.findViewById(R.id.txtvSettingsReportUser);
        txtvReportUser.setOnClickListener(this);
        txtvReportUser.setOnTouchListener(this);

        ibtnSettingsHelpPanicConfirmation = (ImageButton) view.findViewById(R.id.ibtnSettingsHelpPanicConfirmation);
        ibtnSettingsHelpPanicConfirmation.setOnClickListener(this);

        sSettingsPanicConfirmation = (SwitchCompat) view.findViewById(R.id.sSettingsPanicConfirmation);
        sSettingsPanicConfirmation.setOnCheckedChangeListener(this);

        sSettingsNotifications = (SwitchCompat) view.findViewById(R.id.sSettingsNotifications);
        sSettingsNotifications.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
    {
        if(buttonView == sSettingsPanicConfirmation)
        {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

            if(isChecked)
                sharedPref.edit().putBoolean("panicDelay", false).commit();
            else
                sharedPref.edit().putBoolean("panicDelay", true).commit();
        }
        else if(buttonView == sSettingsNotifications)
        {
            ParseInstallation currInst = ParseInstallation.getCurrentInstallation();

            if(isChecked)
                currInst.put("allowNotifications", false);
            else
                currInst.put("allowNotifications", true);

            currInst.saveInBackground();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        initUserDetails();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        saveChanges();
    }

    public void initUserDetails()
    {
        etxtName.setHint(HomeActivity.currentUser.getString("name"));
        etxtName.setOnEditorActionListener(this);
        etxtName.setOnFocusChangeListener(this);

        etxtCellNumber.setHint(HomeActivity.currentUser.getString("cellNumber"));
        etxtCellNumber.setOnEditorActionListener(this);
        etxtCellNumber.setOnFocusChangeListener(this);

        etxtEmail.setHint(HomeActivity.currentUser.getString("email"));
        etxtEmail.setOnEditorActionListener(this);
        etxtEmail.setOnFocusChangeListener(this);

//        selectCountry();

        //init Notifications switch
        sSettingsNotifications.setChecked(!ParseInstallation.getCurrentInstallation().getBoolean("allowNotifications"));

        //init Panic confirmation switch
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sSettingsPanicConfirmation.setChecked(!sharedPrefs.getBoolean("panicDelay", true));

        Log.d(TAG, "Init User details");

    }

    public void selectCountry()
    {

        //Get the array of countries
        String [] countries = getResources().getStringArray(R.array.arrayCountries);

        for(int i = 0; i < countries.length; i++)
            if(countries[i].equalsIgnoreCase(HomeActivity.currentUser.getString("country").trim()))
                spnrCountry.setSelection(i);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus)
    {
        EditText etxtFocusChanged = (EditText) v;

        if(hasFocus)
        {

        }
        else
        {
            if(etxtFocusChanged.getText().toString().isEmpty())
            {

            }
            else
            {
                //TODO: validate

                String enteredText = etxtFocusChanged.getText().toString();

                etxtFocusChanged.setHint(enteredText);
                etxtFocusChanged.setText("");
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
    {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        if(actionId == EditorInfo.IME_ACTION_DONE)
        {
            if(v == etxtCellNumber)
                if(v.getText().toString().length() > 10 || v.getText().toString().length() < 10)
                {
                    etxtCellNumber.setError("Number must be 10 digits");
                    etxtCellNumber.setText("");
                }

            if(!v.getText().toString().isEmpty())
            {
                v.setHint(v.getText().toString());
                v.setText("");
                v.clearFocus();
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
            else
            {
                v.setText("");
                v.clearFocus();
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }

            return true;
        }

        return false;
    }

    private void saveChanges()
    {
        final String name = etxtName.getHint().toString();
        String cellNumber = etxtCellNumber.getHint().toString();
        String email = etxtEmail.getHint().toString();

        if (!name.equals(HomeActivity.currentUser.getString("name")) && !name.isEmpty())
            HomeActivity.currentUser.put("name", name);

        if (!cellNumber.equals(HomeActivity.currentUser.getString("cellNumber")) && !cellNumber.isEmpty())
            HomeActivity.currentUser.put("cellNumber", cellNumber);

        if (!email.equals(HomeActivity.currentUser.getString("email")) && !!email.isEmpty())
            HomeActivity.currentUser.put("email", email);

        if(!txtvCountry.getText().toString().equalsIgnoreCase(HomeActivity.currentUser.getString("country")))
            HomeActivity.currentUser.put("country", txtvCountry.getText().toString());

        HomeActivity.currentUser.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                if (e == null)
                {
//                    HomeActivity.txtvProfileName.setText(name);
                }
                else
                    Toast.makeText(getActivity(), "Changes aborted: " + e.getMessage() + " Code: " + e.getCode(), Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    public void onHiddenChanged(boolean hidden)
    {
        super.onHiddenChanged(hidden);

        if(hidden)
        {
            if(startUpHideComplete) //skip first hide
            {
               // saveChanges();
            }
            else
            {
                if(startUpHideComplete)
                    initUserDetails();

                startUpHideComplete = true;
            }
        }
    }

    @Override
    public void onClick(View v)
    {

        if(v == btnLogout)
        {
            //Logout user
            logoutUser();
        }
        else if(v == btnDelete)
        {

            //Prompt user if they are sure
            deleteUserAccount();
        }
        else if(v == txtvTutorial)
        {
            //Start tutorial
            tutorialComingSoon();
        }
        else if(v == txtvReportBug)
        {
            //Send email
            reportBug();
        }
        else if(v == txtvReportUser)
        {
            //Give report instructions
            howToReportUserDialogue();

        }
        else if(v == txtvCountry)
        {
            //Open countries spinner
            spnrCountry.performClick();
        }
        else if(v == ibtnSettingsHelpPanicConfirmation)
        {
            //Explain to user no 5 sec delay
            panicConfirmationDialog();
        }
    }

    public void panicConfirmationDialog()
    {
        //Prompt user if they are sure
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Enabling this will remove the 5 second delay before sending notifications, however you will have to manually select 'Yes' each time you activate Panic.").setCancelable(false)
                .setTitle("Panic Confirmation")
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

    public void logoutUser()
    {
        //unsub installation from push channels
        List<String> subbedChannels = ParseInstallation.getCurrentInstallation().getList("channels");
        if(subbedChannels != null)  //User might not have joined a group yet
        {
            ParseInstallation.getCurrentInstallation().removeAll("channels", subbedChannels);
            ParseInstallation.getCurrentInstallation().saveInBackground();
        }

        //check if it's a facebook login then logout of facebook as well
        if(ParseUser.getCurrentUser().getString("facebookId") != null)
            LoginManager.getInstance().logOut();

        ParseUser.logOut();

        ParsePush.subscribeInBackground("not_logged_in");
        Intent intentLogout = new Intent(getActivity(), LoginActivity.class);
        intentLogout.putExtra("initParse", false);
        startActivity(intentLogout);
        getActivity().finish();
    }

    public void deleteUserAccount()
    {
        //Prompt user if they are sure
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Are you sure you would like to completely delete your panic account?").setCancelable(false)
                .setTitle("Delete account?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                    {
                        //delete account & logout user
                        ParseUser.getCurrentUser().deleteInBackground();
                        logoutUser();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener()
                {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id)
                    {
                        dialog.cancel();
                    }
                });
        final AlertDialog alert = builder.create();
        alert.show();
    }

    public void reportBug()
    {
        //Get app version
        PackageInfo pInfo = null;
        String version = "";
        try
        {
            pInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
            version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }


        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts("mailto", "support@panic-sec.org", null));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Panic v" + version + " Android bug report");
        intent.putExtra(Intent.EXTRA_TEXT, "In your bug report please inform us:\n\nWhat device brand and model you are using?\n\n\nWhat version of android you are running?\n\n\nWhat you where trying to do when you encountered the bug?\n\n\nA description of the bug?\n\n\n\n\nIt is ok if you do not know the answers to all of the above questions, just complete what you can.\n\nWe thank you for your support and feedback.");

        startActivity(Intent.createChooser(intent, "Report Bug"));
    }

    public void howToReportUserDialogue()
    {
        //Prompt user if they are sure
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Go to history and open a panic you would like to report. Then click report in the upper right corner.").setCancelable(false)
                .setTitle("How to report a user")
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

    public void tutorialComingSoon()
    {
        //Prompt user if they are sure
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Coming soon").setCancelable(false)
                .setTitle("Tutorial")
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
    public boolean onTouch(View v, MotionEvent event)
    {
        if(v == btnLogout)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                btnLogout.setBackgroundColor(getResources().getColor(R.color.BluePressed));
            else if (event.getAction() == MotionEvent.ACTION_UP)
                btnLogout.setBackgroundColor(getResources().getColor(R.color.Blue));
        }
        else if (v == btnDelete)
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                btnDelete.setBackgroundColor(getResources().getColor(R.color.RedPressed));
            else if(event.getAction() == MotionEvent.ACTION_UP)
                btnDelete.setBackgroundColor(getResources().getColor(R.color.Red));
        }
        else if(v == txtvTutorial || v == txtvReportBug ||  v == txtvReportUser || v == txtvCountry )
        {
            if(event.getAction() == MotionEvent.ACTION_DOWN)
                ((TextView)v).setTextColor(getResources().getColor(R.color.FlatLightBluePressed));
            else if(event.getAction() == MotionEvent.ACTION_UP)
                ((TextView)v).setTextColor(getResources().getColor(R.color.FlatLightBlue));
        }

        return false;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        //Set btn test to selected country
        txtvCountry.setText(spnrCountry.getSelectedItem().toString());
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}
