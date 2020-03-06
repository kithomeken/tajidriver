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
import com.tajidriver.driver.DriverHome;

import java.util.Map;

@SuppressLint("Registered")
public class MessagingServices extends FirebaseMessagingService implements IRequestListener  {
    private static final String TAG = MessagingServices.class.getName();

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
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

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.e(TAG, "====================Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        Map<String, String> hashMap = remoteMessage.getData();

        String requestType = hashMap.get("request_type");

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

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token, Context context) {
        Log.d(TAG, "Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token, context);
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

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param firebaseToken The new token.
     */
    private void sendRegistrationToServer(String firebaseToken, Context context) {
        // TODO: Implement this method to send token to your app server.
        // Add custom implementation, as needed.

        RegisterToken registerToken = new RegisterToken(context, this);
        registerToken.firebaseTokenRegistration(firebaseToken);
    }

    @Override
    public void onComplete() {
        Log.d(TAG, "=====================================Token registered successfully in the DB");

    }

    @Override
    public void onError(String message) {
        Log.d(TAG, "======================================Error trying to register the token in the DB: " + message);
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param hashMap FCM message body received.
     */
    private void sendNotification(Map<String, String> hashMap) {
        Intent intent = new Intent(this, DriverHome.class);
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
                        .setContentText(content)
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

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param hashMap FCM message body received.
     */
    private void requestNotification(Map<String, String> hashMap) {
        Intent intent = new Intent(this, DriverHome.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        // Message Breakdown
        String title = hashMap.get("title");
        String content = hashMap.get("content");
        TajiCabs.RQ_NAME = hashMap.get("name");
        TajiCabs.RQ_DEST = hashMap.get("destination");
        TajiCabs.RQ_ORIG = hashMap.get("origin");
        TajiCabs.RQ_PHONE = hashMap.get("phone_number");
        TajiCabs.RQ_COST = hashMap.get("cost");

        TajiCabs.RQ_ORIG_NAME = hashMap.get("orig_name");
        TajiCabs.RQ_DEST_NAME = hashMap.get("dest_name");
        TajiCabs.RQ_DISTANCE = hashMap.get("distance");

        String channelId = getString(R.string.default_notification_channel_id);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.taji_icon)
                        .setContentTitle(title)
                        .setContentText(content)
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

        // Build Request UI

    }
}