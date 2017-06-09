package za.co.oneohtwofour.brave;

import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by wprenison on 2017/06/04.
 */

public class ServiceFirebaseMessaging extends FirebaseMessagingService
{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);
    }

    @Override
    public void onDeletedMessages()
    {
        super.onDeletedMessages();
    }
}
