package io.flyingmongoose.brave.Fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseObject;
import com.parse.ParseUser;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.flyingmongoose.brave.Activity.ActivHome;
import io.flyingmongoose.brave.R;
import io.flyingmongoose.brave.Util.UtilParseAPI;
import io.flyingmongoose.brave.Util.UtilValidate;

/**
 * Created by wprenison on 2017/11/08.
 */

public class FragSettingsNew extends Fragment implements View.OnClickListener
{
    private ActivHome activity;
    private final String TAG = "FragSettingsNew";

    @BindView(R.id.tilSettingsName) TextInputLayout tilName;
    @BindView(R.id.etxtSettingsName) EditText etxtName;

    @BindView(R.id.tilSettingsEmail) TextInputLayout tilEmail;
    @BindView(R.id.etxtSettingsEmail) EditText etxtEmail;

    @BindView(R.id.tilSettingsCellNumber) TextInputLayout tilCellNumber;
    @BindView(R.id.etxtSettingsCellNumber) EditText etxtCellNumber;

    @BindView(R.id.swSettingsMuteNotifications) Switch swMuteNotifications;

    @BindView(R.id.swSettingsAlertConfirmation) Switch swAlertConfirmation;

    @BindView(R.id.btnSettingsSignOut) Button btnSignOut;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.fragment_settings_new, container, false);

        //Get Handle on views
        ButterKnife.bind(this, constructedView);

        return constructedView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        activity = (ActivHome) getActivity();

        //Init fields
        if(ActivHome.userFresh)
        {
            initFields(ActivHome.currentUser);
        }
        else
        {
            ActivHome.currentUser.fetchInBackground(new GetCallback<ParseObject>()
            {
                @Override
                public void done(ParseObject object, ParseException e)
                {
                    if(e == null)
                        initFields(ActivHome.currentUser);
                    else
                        Log.e(TAG, "Error while fetching user: " + e.getCode() + ": " + e.getMessage());
                }
            });
        }

        //sub to listeners
        btnSignOut.setOnClickListener(this);

        //Add validation listeners
        etxtName.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                //Validate
                UtilValidate.validateFullName(activity, editable.toString().trim(), tilName, true);

            }
        });

        etxtCellNumber.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                //validate
                UtilValidate.validatePhone(activity, editable.toString().trim(),  tilCellNumber, true);
            }
        });

        etxtEmail.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2)
            {

            }

            @Override
            public void afterTextChanged(Editable editable)
            {
                UtilValidate.validateEmail(activity, editable.toString().trim(), tilEmail, true);
            }
        });
    }

    /**
     * Inits all views using the current parse user object
     * ,obtained statically from the Home Activity, and Shared Prefs
     * @param currUser
     */
    private void initFields(ParseUser currUser)
    {
        etxtName.setText(currUser.getString(UtilParseAPI.name));
        etxtEmail.setText(currUser.getString(UtilParseAPI.email));
        etxtCellNumber.setText(currUser.getString(UtilParseAPI.cellNumber));

        //init allow notif
        swMuteNotifications.setChecked(!ParseInstallation.getCurrentInstallation().getBoolean(UtilParseAPI.allowNotif));

        //init alert delay
        swAlertConfirmation.setChecked(ActivHome.sharedPrefs.getBoolean(getString(R.string.prefAlertDelay), false));
    }

    @Override
    public void onClick(View view)
    {
        UtilParseAPI.signOutUser(activity);
    }

    @Override
    public void onPause()
    {
        super.onPause();

        updateDbFields();
    }

    /***
     * Updates the parse db fields with new data if needed
     */
    private void updateDbFields()
    {
        //Check if any updates where made, then persist them back to the db
        boolean userObjUpdated = false;

        //Check name
        String settingName = etxtName.getText().toString().trim();
        if(!ActivHome.currentUser.getString(UtilParseAPI.name).equals(settingName))
        {
            ActivHome.currentUser.put(UtilParseAPI.name, settingName);
            userObjUpdated = true;
        }

        //Check email
        String settingsEmail = etxtEmail.getText().toString().trim();
        if(!ActivHome.currentUser.getString(UtilParseAPI.email).equals(settingsEmail))
        {
            //TODO: Check that the new email does not already exist
            ActivHome.currentUser.put(UtilParseAPI.email, settingsEmail);
            userObjUpdated = true;
        }

        //Check Cell Number
        String settingCellNumber = etxtCellNumber.getText().toString().trim();
        if(!ActivHome.currentUser.equals(settingCellNumber))
        {
            ActivHome.currentUser.put(UtilParseAPI.cellNumber, settingCellNumber);
            userObjUpdated = true;
        }

        //Check allow notif
        boolean settingsAllowNotif = !swMuteNotifications.isChecked();
        ParseInstallation currInst = ParseInstallation.getCurrentInstallation();
        if(currInst.getBoolean(UtilParseAPI.allowNotif) != settingsAllowNotif)
        {
            currInst.put(UtilParseAPI.allowNotif, settingsAllowNotif);
            currInst.saveInBackground();
        }

        //Alert delay is only locally kept at the moment and is done on it's own listener

        if(userObjUpdated)
            ActivHome.currentUser.saveInBackground();
    }
}
