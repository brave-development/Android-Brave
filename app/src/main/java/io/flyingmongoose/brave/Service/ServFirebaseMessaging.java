package io.flyingmongoose.brave.Service;

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

import org.greenrobot.eventbus.EventBus;

import java.util.Map;

import io.flyingmongoose.brave.Activity.ActivHome;
import io.flyingmongoose.brave.Event.EvtNewChatMsg;
import io.flyingmongoose.brave.R;

/**
 * Created by wprenison on 2017/06/04.
 */

public class ServFirebaseMessaging extends FirebaseMessagingService
{
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage)
    {
        super.onMessageReceived(remoteMessage);

        Map<String, String> data = remoteMessage.getData();

        if(data.get("type") != null)
            if(data.get("type").equals("newAlert"))
                newAlertReceived(data, remoteMessage);
            else if(data.get("type").equals("newMessage"))
                newChatReceived(remoteMessage);
            else
                newChatReceived(remoteMessage); //TODO: this should be an unknown message
        else
            newChatReceived(remoteMessage);    //TODO: this should be an unknown message

    }

    private void newChatReceived(RemoteMessage remoteMessage)
    {
        Log.d("DebugChat", "new chat  push received");

        String title = remoteMessage.getNotification().getTitle();
        String body = remoteMessage.getNotification().getBody();
        String alertId = "";

        Log.d("DebugChat", "Title: " + title + "\nbody: " + body);

        if(!title.equals(ActivHome.currentUser.getString("name")))  //TODO: change to check for user id
        {
            sendChatNotification(title, body, alertId);
            EventBus.getDefault().post(new EvtNewChatMsg("", title, body));
        }
    }

    private void newAlertReceived(Map<String, String> data, RemoteMessage remoteMessage)
    {

        Log.d("debugFbPush", "Message received: " + remoteMessage.getNotification().getBody()
                + "\nlat: " + data.get("lat") + " lng: " + data.get("lng"));

        sendAlertNotification(remoteMessage.getNotification().getTitle() ,remoteMessage.getNotification().getBody(), Double.parseDouble(data.get("lat")), Double.parseDouble(data.get("lng")));
    }

    private void sendChatNotification(String title, String messageBody, String alertId)
    {
        Intent intent = new Intent(this, ActivHome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 1 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Bundle extras = new Bundle();
        extras.putString("alertId", alertId);

        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.ic_chat)
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

        notificationManager.notify(1 /* ID of notification */, notificationBuilder.build());
    }

    private void sendAlertNotification(String title, String messageBody, Double lat, Double lng) {
        Intent intent = new Intent(this, ActivHome.class);
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
