package io.flyingmongoose.brave.dialog;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.github.clans.fab.FloatingActionButton;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.SaveCallback;

import io.flyingmongoose.brave.activity.ActivHome;
import io.flyingmongoose.brave.R;

/**
 * Created by wprenison on 2017/07/24.
 */

public class DiagFeedback extends DialogFragment
{
    private EditText etxtSubject;
    private EditText etxtMsg;
    private FloatingActionButton fabSend;
    private SwipeRefreshLayout srLayLoading;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)
    {
        View constructedView = inflater.inflate(R.layout.dialog_feedback, container, false);

        //Get handle on views
        etxtSubject = (EditText) constructedView.findViewById(R.id.diagFeedbackSubject);
        etxtMsg = (EditText) constructedView.findViewById(R.id.diagFeedbackMsg);
        fabSend = (FloatingActionButton) constructedView.findViewById(R.id.diagFeedbackFabSend);
        srLayLoading = (SwipeRefreshLayout) constructedView.findViewById(R.id.diagFeedbackProgCircle);

        return constructedView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        initSwipeRefresh();

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

        final String finalVersion = version;
        fabSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                srLayLoading.setRefreshing(true);

                //validate
                if(validate())
                {
                    //Get needed vars
                    String msg = etxtMsg.getText().toString().trim();

                    //Add additional info to msg
                    msg += "\n\nApp Version: " + finalVersion;

                    ParseObject feedbackObj = new ParseObject("Feedback");
                    feedbackObj.put("user", ActivHome.currentUser);
                    feedbackObj.put("subject", etxtSubject.getText().toString().trim());
                    feedbackObj.put("description", msg);
                    feedbackObj.put("appVersion", finalVersion);
                    feedbackObj.put("os", "Android " + Build.VERSION.RELEASE + " Api Level: " + Build.VERSION.SDK_INT);
                    feedbackObj.put("manufacturer", Build.MANUFACTURER);
                    feedbackObj.put("model", Build.MODEL);

                    feedbackObj.saveInBackground(new SaveCallback()
                    {
                        @Override
                        public void done(ParseException e)
                        {
                            srLayLoading.setRefreshing(false);

                            if(e == null)
                            {
                                dismiss();
                                Snackbar.make(ActivHome.fabMainAlert, getString(R.string.diagFeedbackSentSuccess), Snackbar.LENGTH_LONG).show();
                            }
                            else
                            {
                                if(e.getCode() == 100)
                                    Snackbar.make(fabSend, "Please check you internet connection and try again", Snackbar.LENGTH_LONG).show();
                                else
                                    Snackbar.make(fabSend, "Unsuccessful: " + e.getMessage() + " Code: " + e.getCode(), Snackbar.LENGTH_LONG).show();
                            }
                        }
                    });
                }
                else
                    srLayLoading.setRefreshing(false);
            }
        });
    }

    private void initSwipeRefresh()
    {
        srLayLoading.setEnabled(false);
        srLayLoading.setColorSchemeColors(getResources().getColor(R.color.FlatLightBlue), getResources().getColor(R.color.Red), getResources().getColor(R.color.SeaGreen));
        srLayLoading.setProgressBackgroundColor(R.color.CircleProgLoadingColor);
        srLayLoading.setProgressViewOffset(true, 0, 8);
    }

    private boolean validate()
    {
        boolean valid = true;

        if(etxtSubject.length() == 0)
        {
            valid = false;
            etxtSubject.setError(getString(R.string.error_cannot_be_empty));
        }

        if(etxtMsg.length() == 0)
        {
            valid = false;
            etxtMsg.setError(getString(R.string.error_cannot_be_empty));
        }

        return valid;
    }


}
