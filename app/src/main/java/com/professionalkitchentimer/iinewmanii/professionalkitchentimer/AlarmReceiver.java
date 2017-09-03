package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.NotificationCompat;

import static android.support.v4.app.NotificationCompat.CATEGORY_ALARM;
//import android.util.Log;

/**
 * Created by IINEWMANII on 2/10/2017.
 * Receive broadcast from alarm manager and build alarm notification.
 */

public class AlarmReceiver extends BroadcastReceiver {

    //    private static final String TAG = "NEWMAN";
    private final static String ALARM_NOTIFICATION_GROUP_KEY = "alarm_notification";
    private final int timerOneNotifColor = Color.argb(255, 239, 82, 79);
    private final int timerTwoNotifColor = Color.argb(255, 250, 225, 85);
    private final int timerThreeNotifColor = Color.argb(255, 94, 171, 92);
    private final int timerFourNotifColor = Color.argb(255, 250, 150, 27);
    private final int timerOneLightColor = Color.argb(255, 255, 0, 0);
    private final int timerTwoLightColor = Color.argb(255, 255, 255, 0);
    private final int timerThreeLightColor = Color.argb(255, 0, 255, 0);
    private final int timerFourLightColor = Color.argb(255, 255, 55, 0);
    private final long pattern[] = {200, 500, 500};

    /**
     * When broadcast is received determine which timer sent the broadcast
     * Then call alarmNotification and set the proper object values
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        PrefUtils timerPreferences = new PrefUtils(context);

        int timerNumberId = intent.getIntExtra("timerNumberId", 0);

        Intent timerNotifIntent = new Intent(context, TimerNotifyService.class);
        context.stopService(timerNotifIntent);

        if (timerNumberId == 1) {
            alarmNotification(1, context, timerOneNotifColor, timerOneLightColor, intent);
            timerPreferences.setTimerOneRunning(false);
            timerPreferences.setStartTime(0);
            timerPreferences.setOriginalTime(0);
            timerPreferences.setTimerPaused(false);
            timerPreferences.setPausedTime(0);
        }

        if (timerNumberId == 2) {
            alarmNotification(2, context, timerTwoNotifColor, timerTwoLightColor, intent);
            timerPreferences.setTimerTwoRunning(false);
            timerPreferences.setStartTimeTwo(0);
            timerPreferences.setOriginalTimeTwo(0);
            timerPreferences.setTimerTwoPaused(false);
            timerPreferences.setPausedTimeTwo(0);
        }

        if (timerNumberId == 3) {
            alarmNotification(3, context, timerThreeNotifColor, timerThreeLightColor, intent);
            timerPreferences.setTimerThreeRunning(false);
            timerPreferences.setStartTimeThree(0);
            timerPreferences.setOriginalTimeThree(0);
            timerPreferences.setTimerThreePaused(false);
            timerPreferences.setPausedTimeThree(0);
        }

        if (timerNumberId == 4) {
            alarmNotification(4, context, timerFourNotifColor, timerFourLightColor, intent);
            timerPreferences.setTimerFourRunning(false);
            timerPreferences.setStartTimeFour(0);
            timerPreferences.setOriginalTimeFour(0);
            timerPreferences.setTimerFourPaused(false);
            timerPreferences.setPausedTimeFour(0);
        }
    }

    /**
     * Builds a notification for an alarm and displays it in the notification tray
     */
    private void alarmNotification(int timerNumber, Context context, int notifColor, int lightColor, Intent intent) {
        PrefUtils timerPreferences = new PrefUtils(context);

        int timerNumberId = intent.getIntExtra("timerNumberId", 0);
        int alarmVolume = timerPreferences.getAlarmVolume();
        boolean vibrateSetting = timerPreferences.getVibrateSetting();

//        Log.v(TAG, "alarmVolume = " + alarmVolume);

        Uri notification = Uri.parse("android.resource://" + "com.professionalkitchentimer.iinewmanii.professionalkitchentimer" + '/' + R.raw.alarm_clock_short);

        AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        int originalNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        timerPreferences.setOriginalNotificationVolume(originalNotificationVolume);
        timerPreferences.setTimerNotificationAlarm(true);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, alarmVolume, 0);

        intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent sender = PendingIntent.getActivity(context, 1, intent, 0);
        PendingIntent senderTwo = PendingIntent.getActivity(context, 2, intent, 0);
        PendingIntent senderThree = PendingIntent.getActivity(context, 3, intent, 0);
        PendingIntent senderFour = PendingIntent.getActivity(context, 4, intent, 0);

        NotificationCompat.Builder timerNotificationBuilder = new NotificationCompat.Builder(context);

        final int lightFlashingInterval = 500;
        timerNotificationBuilder.setPriority(Notification.PRIORITY_MAX)
                .setColor(notifColor)
                .setSound(notification)
                .setLights(lightColor, lightFlashingInterval, lightFlashingInterval)
                .setContentTitle("Timer " + timerNumber + " has finished")
                .setContentText("Professional Kitchen Timer")
                .setSmallIcon(R.drawable.ic_pkt_alarm)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setAutoCancel(true);

        if (vibrateSetting) {
            timerNotificationBuilder.setVibrate(pattern);
        }

        int notifId = 0;
        if (timerNumberId == 1) {
            notifId = 1;
            timerNotificationBuilder.setContentIntent(sender);
        }

        if (timerNumberId == 2) {
            notifId = 2;
            timerNotificationBuilder.setContentIntent(senderTwo);
        }

        if (timerNumberId == 3) {
            notifId = 3;
            timerNotificationBuilder.setContentIntent(senderThree);
        }

        if (timerNumberId == 4) {
            notifId = 4;
            timerNotificationBuilder.setContentIntent(senderFour);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            timerNotificationBuilder.setCategory(CATEGORY_ALARM);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            timerNotificationBuilder.setGroup(ALARM_NOTIFICATION_GROUP_KEY);
        }

        Notification timerNotification = timerNotificationBuilder.build();
        timerNotification.flags |= Notification.FLAG_INSISTENT;
        NotificationManager alarmNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        alarmNotificationManager.notify(notifId, timerNotification);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            NotificationCompat.Builder summaryNotificationBuilder = new NotificationCompat.Builder(context);

            summaryNotificationBuilder.setSmallIcon(R.drawable.ic_pkt_alarm)
                    .setContentTitle("Multiple alarms have finished!")
                    .setContentText("Professional Kitchen Timer")
                    .setGroup(ALARM_NOTIFICATION_GROUP_KEY)
                    .setGroupSummary(true);

            Notification summaryNotification = summaryNotificationBuilder.build();
            final int summaryId = 0;
            alarmNotificationManager.notify(summaryId, summaryNotification);
        }
    }
}
