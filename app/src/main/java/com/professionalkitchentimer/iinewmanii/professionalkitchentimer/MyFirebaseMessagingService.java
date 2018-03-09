package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private final int firebaseMessageColor = Color.argb(255, 113, 132, 227);

    private static final String NOTIFICATION_CHANNEL_ID_FIREBASE_PUSH = "Push Notification";

    private NotificationManager notificationManager;

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel pushNotificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_FIREBASE_PUSH,
                    "Push Notification", NotificationManager.IMPORTANCE_DEFAULT);

            notificationManager.createNotificationChannel(pushNotificationChannel);
        }

        String messageType = remoteMessage.getData().get("type");

        String messageTitle = remoteMessage.getData().get("title");

        String messageBody = remoteMessage.getData().get("body");

        if (Objects.equals(messageType, "BigPicture")) {
            String imageUrl = remoteMessage.getData().get("image");

            Bitmap image = getBitmapfromUrl(imageUrl);

            firebaseBigPictureMessageFunction(messageTitle, messageBody, image);
        } else if (Objects.equals(messageType, "BigText")) {
            String bigText = remoteMessage.getData().get("bigtext");

            firebaseBigTextMessageFunction(messageTitle, messageBody, bigText);
        }
    }
    // [END receive_message]

    private void firebaseBigPictureMessageFunction(CharSequence messageTitle, CharSequence messageBody, Bitmap image) {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Bitmap FcmLargeIconBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_pkt_launcher);

        NotificationCompat.Builder bigPictureNotificationBuilder;

        bigPictureNotificationBuilder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_FIREBASE_PUSH) : new NotificationCompat.Builder(this);

        bigPictureNotificationBuilder.setSmallIcon(R.drawable.ic_pkt_icon_ticker)
                .setLargeIcon(FcmLargeIconBitmap)
                .setColor(firebaseMessageColor)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(image))/*Notification with image*/
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);

        if (notificationManager != null) {
            notificationManager.notify(0 /* ID of notification */, bigPictureNotificationBuilder.build());
        }
    }

    private void firebaseBigTextMessageFunction(CharSequence messageTitle, CharSequence messageBody, CharSequence bigText) {

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Bitmap FcmLargeIconBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_pkt_launcher);

        NotificationCompat.Builder bigTextNotificationBuilder;

        bigTextNotificationBuilder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
               new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID_FIREBASE_PUSH) : new NotificationCompat.Builder(this);

        bigTextNotificationBuilder.setSmallIcon(R.drawable.ic_pkt_icon_ticker)
                .setLargeIcon(FcmLargeIconBitmap)
                .setColor(firebaseMessageColor)
                .setContentTitle(messageTitle)
                .setContentText(messageBody)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(bigText))/*Notification with big text*/
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent)
                .setPriority(Notification.PRIORITY_HIGH);

        if (notificationManager != null) {
            notificationManager.notify(0 /* ID of notification */, bigTextNotificationBuilder.build());
        }
    }

    private static Bitmap getBitmapfromUrl(String imageUrl) {

        try {
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);

        } catch (Exception e) {
            e.printStackTrace();
            return null;

        }
    }
}




