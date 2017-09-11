package io.flyingmongoose.brave;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by wprenison on 2017/06/04.
 */

public class ServiceFirebaseMessaging extends FirebaseMessagingService
{

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        Log.d("debugFbPush", "Message received: " + remoteMessage.getNotification().getBody()
        + "\nlat: " + data.get("lat") + " lng: " + data.get("lng"));

        sendNotification(remoteMessage.getNotification().getTitle() ,remoteMessage.getNotification().getBody(), Double.parseDouble(data.get("lat")), Double.parseDouble(data.get("lng")));
    }

    private void sendNotification(String title, String messageBody, Double lat, Double lng) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bundle extras = new Bundle();
        extras.putDouble("lat", lat);
        extras.putDouble("lng", lng);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_stat_healing)
                .setColor(getColor(R.color.bravely))
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher))
                .setPriority(Notification.PRIORITY_MAX)
                .setContentTitle(title)
                .setContentText(messageBody)
                .setExtras(extras)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    @Override
    public void onDeletedMessages()
    {
        super.onDeletedMessages();
    }
}
