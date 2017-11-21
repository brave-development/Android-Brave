package io.flyingmongoose.brave;

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

/**
 * Created by wprenison on 2017/11/08.
 */

public class FragmentSettingsNew extends Fragment implements View.OnClickListener
{
    private HomeActivity activity;
    private final String TAG = "FragSettingsNew";

    @BindView(R2.id.tilSettingsName) TextInputLayout tilName;
    @BindView(R2.id.etxtSettingsName) EditText etxtName;

    @BindView(R2.id.tilSettingsEmail) TextInputLayout tilEmail;
    @BindView(R2.id.etxtSettingsEmail) EditText etxtEmail;

    @BindView(R2.id.tilSettingsCellNumber) TextInputLayout tilCellNumber;
    @BindView(R2.id.etxtSettingsCellNumber) EditText etxtCellNumber;

    @BindView(R2.id.swSettingsMuteNotifications) Switch swMuteNotifications;

    @BindView(R2.id.swSettingsAlertConfirmation) Switch swAlertConfirmation;

    @BindView(R2.id.btnSettingsSignOut) Button btnSignOut;

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

        activity = (HomeActivity) getActivity();

        //Init fields
        if(HomeActivity.userFresh)
        {
            initFields(HomeActivity.currentUser);
        }
        else
        {
            HomeActivity.currentUser.fetchInBackground(new GetCallback<ParseObject>()
            {
                @Override
                public void done(ParseObject object, ParseException e)
                {
                    if(e == null)
                        initFields(HomeActivity.currentUser);
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
                ValidateUtil.validateFullName(activity, editable.toString().trim(), tilName, true);

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
                ValidateUtil.validatePhone(activity, editable.toString().trim(),  tilCellNumber, true);
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
                ValidateUtil.validateEmail(activity, editable.toString().trim(), tilEmail, true);
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
        etxtName.setText(currUser.getString(ParseAPIUtils.name));
        etxtEmail.setText(currUser.getString(ParseAPIUtils.email));
        etxtCellNumber.setText(currUser.getString(ParseAPIUtils.cellNumber));

        //init allow notif
        swMuteNotifications.setChecked(!ParseInstallation.getCurrentInstallation().getBoolean(ParseAPIUtils.allowNotif));

        //init alert delay
        swAlertConfirmation.setChecked(HomeActivity.sharedPrefs.getBoolean(getString(R.string.prefAlertDelay), false));
    }

    @Override
    public void onClick(View view)
    {
        ParseAPIUtils.signOutUser(activity);
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
        if(!HomeActivity.currentUser.getString(ParseAPIUtils.name).equals(settingName))
        {
            HomeActivity.currentUser.put(ParseAPIUtils.name, settingName);
            userObjUpdated = true;
        }

        //Check email
        String settingsEmail = etxtEmail.getText().toString().trim();
        if(!HomeActivity.currentUser.getString(ParseAPIUtils.email).equals(settingsEmail))
        {
            //TODO: Check that the new email does not already exist
            HomeActivity.currentUser.put(ParseAPIUtils.email, settingsEmail);
            userObjUpdated = true;
        }

        //Check Cell Number
        String settingCellNumber = etxtCellNumber.getText().toString().trim();
        if(!HomeActivity.currentUser.equals(settingCellNumber))
        {
            HomeActivity.currentUser.put(ParseAPIUtils.cellNumber, settingCellNumber);
            userObjUpdated = true;
        }

        //Check allow notif
        boolean settingsAllowNotif = !swMuteNotifications.isChecked();
        ParseInstallation currInst = ParseInstallation.getCurrentInstallation();
        if(currInst.getBoolean(ParseAPIUtils.allowNotif) != settingsAllowNotif)
        {
            currInst.put(ParseAPIUtils.allowNotif, settingsAllowNotif);
            currInst.saveInBackground();
        }

        //Alert delay is only locally kept at the moment and is done on it's own listener

        if(userObjUpdated)
            HomeActivity.currentUser.saveInBackground();
    }
}
