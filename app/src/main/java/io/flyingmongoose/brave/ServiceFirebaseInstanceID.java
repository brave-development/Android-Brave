package io.flyingmongoose.brave;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.SaveCallback;

/**
 * Created by wprenison on 2017/06/04.
 */

public class ServiceFirebaseInstanceID extends FirebaseInstanceIdService
{

    @Override
    public void onTokenRefresh()
    {
        super.onTokenRefresh();

        final String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Log.d("fbPushDebug", "FB Push Token Refreshed: " + refreshedToken);

        //Save firebase token to parse server
        ParseInstallation instObj = ParseInstallation.getCurrentInstallation();
        instObj.put("firebaseID", refreshedToken);
        instObj.saveInBackground(new SaveCallback()
        {
            @Override
            public void done(ParseException e)
            {
                Log.d("fbPushDebug", "Parse FB Push Token Refreshed: " + refreshedToken);
            }
        });

    }
}
