package com.tajidriver.service;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.tajidriver.R;
import com.tajidriver.configuration.TajiCabs;
import com.tajidriver.database.AppDatabase;
import com.tajidriver.database.RWServices;
import com.tajidriver.driver.DriverHome;
import com.tajidriver.global.Variables;
import com.tajidriver.home.Home;

import java.util.Map;

@SuppressLint("Registered")
public class MessagingServices extends FirebaseMessagingService implements IRequestListener  {
    private static final String TAG = MessagingServices.class.getName();

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.e(TAG, "===================Message data payload: " + remoteMessage.getData());

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob();
            } else {
                // Handle message within 10 seconds
                handleNow();
            }
        }

        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        Map<String, String> hashMap = remoteMessage.getData();

        String requestType = hashMap.get("request_type");

        assert requestType != null;
        switch (requestType) {
            case "701":
                sendNotification(remoteMessage.getData());
            break;

            case "702":
                // Ride Request
                requestNotification(remoteMessage.getData());
            break;
        }

    }

    @Override
    public void onNewToken(Context context, String firebaseToken) {
        // TODO: Implement this method to send token to your app server.
        // Register New Firebase Token to Database
        RegisterToken registerToken = new RegisterToken(context, this);
        registerToken.firebaseTokenRegistration(firebaseToken);
    }

    @Override
    public void onTokenUpdate(Context context, String firebaseToken) {
        // TODO: Implement this method to send token to your app server.
        // Update New Firebase Token for User
        RegisterToken registerToken = new RegisterToken(context, this);
        registerToken.updateFirebaseToken(firebaseToken);
    }

    /**
     * Schedule async work using WorkManager.
     */
    private void scheduleJob() {
        // [START dispatch_job]
        OneTimeWorkRequest work = new OneTimeWorkRequest.Builder(TajiWorker.class)
                .build();
        WorkManager.getInstance().beginWith(work).enqueue();
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private void handleNow() {
        Log.d(TAG, "Short lived task is done.");
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "Token registered successfully in the DB");

    }

    @Override
    public void onError(String message) {
        Log.d(TAG, "Error trying to register the token in the DB: " + message);
    }

    private void sendNotification(Map<String, String> hashMap) {
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // Message Breakdown
        String title = hashMap.get("title");
        String content = hashMap.get("content");
        String action = hashMap.get("action");

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.taji_icon)
                        .setContentTitle(title)
                        .setContentText("Welcome to Taji Driver")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(content))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }

    private void requestNotification(Map<String, String> hashMap) {
        Intent intent = new Intent(this, Home.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // Message Breakdown
        String title = hashMap.get("title");
        String content = hashMap.get("content");

        Variables.PASSENGER_NAME = hashMap.get("passengerName");
        Variables.PASSENGER_PHONE = hashMap.get("passengerPhone");
        Variables.TRIP_ID = hashMap.get("tripId");
        Variables.TRIP_COST = hashMap.get("tripCost");
        Variables.TRIP_DISTANCE = hashMap.get("tripDistance");

        Variables.REQUEST_ORIGIN_LAT = hashMap.get("originLat");
        Variables.REQUEST_ORIGIN_LNG = hashMap.get("originLng");
        Variables.REQUEST_ORIGIN_NAME = hashMap.get("originName");

        Variables.REQUEST_DESTINATION_LAT = hashMap.get("destinationLat");
        Variables.REQUEST_DESTINATION_LNG = hashMap.get("destinationLng");
        Variables.REQUEST_DESTINATION_NAME = hashMap.get("destinationName");

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.taji_icon)
                        .setContentTitle(title)
                        .setContentText("Trip Request")
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(content))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
    }
}