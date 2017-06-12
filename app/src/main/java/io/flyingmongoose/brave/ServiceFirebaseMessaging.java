package io.flyingmongoose.brave;

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
