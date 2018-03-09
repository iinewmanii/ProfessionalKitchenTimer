package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

import static android.support.v4.app.NotificationCompat.CATEGORY_ALARM;
import static android.support.v4.app.NotificationCompat.PRIORITY_HIGH;
//import android.util.Log;

/**
 * Created by IINEWMANII on 2/10/2017.
 * Receive broadcast from alarm manager and build alarm notification.
 */

public class AlarmReceiver extends BroadcastReceiver {

    //    private static final String TAG = "NEWMAN";
    private static final String ALARM_NOTIFICATION_GROUP_KEY = "ALARM_NOTIFICATION";
    private static final String NOTIFICATION_DELETED_ACTION = "NOTIFICATION_DELETED";
    private static final String CREATE_NOTIFICATION_ACTION = "CREATE_NOTIFICATION";
    private static final String NOTIFICATION_CHANNEL_ID_ALARM = "ALARM_NOTIFICATION";
    private PrefUtils timerPreferences;
    private AudioManager audioManager;
    private final int timerNotificationSummaryColor = Color.rgb(113, 132, 227);
    private final int timerOneNotifColor = Color.rgb(239, 82, 79);
    private final int timerTwoNotifColor = Color.rgb(250, 225, 85);
    private final int timerThreeNotifColor = Color.rgb(94, 171, 92);
    private final int timerFourNotifColor = Color.rgb(250, 150, 27);
    private final int timerOneLightColor = Color.rgb(255, 0, 0);
    private final int timerTwoLightColor = Color.rgb(255, 255, 0);
    private final int timerThreeLightColor = Color.rgb(0, 255, 0);
    private final int timerFourLightColor = Color.rgb(255, 55, 0);
    private int originalNotificationVolume;
    private final long pattern[] = {200, 500, 500};

    /**
     * When broadcast is received determine which timer sent the broadcast
     * Then call alarmNotification and set the proper object values
     */
    @Override
    public void onReceive(Context context, Intent intent) {

        timerPreferences = new PrefUtils(context);

        String action = intent.getAction();

        if (CREATE_NOTIFICATION_ACTION.equals(action)) {
            int timerNumberId = intent.getIntExtra("timerNumberId", 0);

            Intent timerNotifIntent = new Intent(context, TimerNotifyService.class);
            context.stopService(timerNotifIntent);

            switch (timerNumberId) {
                case 1:
                    alarmNotification(timerNumberId, context, timerOneNotifColor, timerOneLightColor, intent);
                    timerPreferences.setTimerOneState(TimerState.INPUT);
                    timerPreferences.setStartTime(0);
                    timerPreferences.setOriginalTime(0);
                    timerPreferences.setPausedTime(0);
                    break;

                case 2:
                    alarmNotification(timerNumberId, context, timerTwoNotifColor, timerTwoLightColor, intent);
                    timerPreferences.setTimerTwoState(TimerState.INPUT);
                    timerPreferences.setStartTimeTwo(0);
                    timerPreferences.setOriginalTimeTwo(0);
                    timerPreferences.setPausedTimeTwo(0);
                    break;

                case 3:
                    alarmNotification(timerNumberId, context, timerThreeNotifColor, timerThreeLightColor, intent);
                    timerPreferences.setTimerThreeState(TimerState.INPUT);
                    timerPreferences.setStartTimeThree(0);
                    timerPreferences.setOriginalTimeThree(0);
                    timerPreferences.setPausedTimeThree(0);
                    break;

                case 4:
                    alarmNotification(timerNumberId, context, timerFourNotifColor, timerFourLightColor, intent);
                    timerPreferences.setTimerFourState(TimerState.INPUT);
                    timerPreferences.setStartTimeFour(0);
                    timerPreferences.setOriginalTimeFour(0);
                    timerPreferences.setPausedTimeFour(0);
                    break;

                default:
                    break;
            }
        } else if (NOTIFICATION_DELETED_ACTION.equals(action)) {
            long millisToCount = timerPreferences.getOriginalTime();
            long millisToCountTwo = timerPreferences.getOriginalTimeTwo();
            long millisToCountThree = timerPreferences.getOriginalTimeThree();
            long millisToCountFour = timerPreferences.getOriginalTimeFour();

            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            originalNotificationVolume = timerPreferences.getOriginalNotificationVolume();
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, originalNotificationVolume, 0);

            if ((millisToCount | millisToCountTwo | millisToCountThree | millisToCountFour) > 0) {
                Intent timerNotifIntent = new Intent(context, TimerNotifyService.class);
                context.startService(timerNotifIntent);
            }
        }
    }

    /**
     * Builds a notification for an alarm and displays it in the notification tray
     */
    private void alarmNotification(int timerNumber, Context context, int notifColor, int lightColor, Intent intent) {

        int timerNumberId = intent.getIntExtra("timerNumberId", 0);
        int alarmVolume = timerPreferences.getAlarmVolume();
        boolean vibrateSetting = timerPreferences.getVibrateSetting();

//        Log.v(TAG, "alarmVolume = " + alarmVolume);

        /* Build a Uri to send our alarm sound file to a remote object */
        Uri alarmSound = Uri.parse("android.resource://" + context.getPackageName() + '/' + R.raw.alarm_clock_short);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            originalNotificationVolume = audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION);
        }
        timerPreferences.setOriginalNotificationVolume(originalNotificationVolume);
        timerPreferences.setTimerNotificationAlarm(true);
        audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, alarmVolume, 0);

        /* Create Intents for when the user taps on an alarm and swipes to delete alarm */
        Intent alarmTappedIntent = new Intent(context, MainActivity.class);
        alarmTappedIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent senderTapped = PendingIntent.getActivity(context, timerNumber, alarmTappedIntent, 0);

        Intent alarmSwipedIntent = new Intent(context.getApplicationContext(), AlarmReceiver.class);
        alarmSwipedIntent.setAction(NOTIFICATION_DELETED_ACTION);
        PendingIntent senderSwiped = PendingIntent.getBroadcast(context.getApplicationContext(), 0, alarmSwipedIntent, 0);

        /* Create alarm notification */
        NotificationManager alarmNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder timerNotificationBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            timerNotificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_ALARM);

            NotificationChannel alarmNotificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID_ALARM,
                    "Alarm Notification", NotificationManager.IMPORTANCE_HIGH);

            AudioAttributes alarmAudioAttr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();

            alarmNotificationChannel.setSound(alarmSound, alarmAudioAttr);
            if (alarmNotificationManager != null) {
                alarmNotificationManager.createNotificationChannel(alarmNotificationChannel);
            }
        } else {
            timerNotificationBuilder = new NotificationCompat.Builder(context);
        }

        int lightFlashingInterval = 500;
        timerNotificationBuilder.setPriority(PRIORITY_HIGH)
                .setColor(notifColor)
                .setSound(alarmSound)
                .setLights(lightColor, lightFlashingInterval, lightFlashingInterval)
                .setContentTitle("Timer " + timerNumber + " has finished")
                .setContentText("Swipe or Tap to cancel alarm")
                .setContentIntent(senderTapped)
                .setCategory(CATEGORY_ALARM)
                .setGroup(ALARM_NOTIFICATION_GROUP_KEY)
                .setSmallIcon(R.drawable.ic_pkt_alarm)
                .setWhen(System.currentTimeMillis())
                .setShowWhen(true)
                .setAutoCancel(true)
                .setDeleteIntent(senderSwiped);

        if (vibrateSetting) {
            timerNotificationBuilder.setVibrate(pattern);
        }

        Notification timerNotification = timerNotificationBuilder.build();
        timerNotification.flags |= Notification.FLAG_INSISTENT;

        if (alarmNotificationManager != null) {
            alarmNotificationManager.notify(timerNumberId, timerNotification);
        }

        // TODO: 3/8/18 Summary kind of works could probably be made better.
        /* Create summary alarmSound */
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            StatusBarNotification[] activeNotifications = alarmNotificationManager.getActiveNotifications();

            int numberOfNotifications = activeNotifications.length;

            if (numberOfNotifications > 1) {
                NotificationCompat.Builder summaryNotificationBuilder;

                summaryNotificationBuilder = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                        new NotificationCompat.Builder(context, NOTIFICATION_CHANNEL_ID_ALARM) : new NotificationCompat.Builder(context);

                summaryNotificationBuilder.setSmallIcon(R.drawable.ic_pkt_alarm)
                        .setColor(timerNotificationSummaryColor)
                        .setContentTitle("Multiple alarms have finished!")
                        .setContentText("PKT")
                        .setContentIntent(senderTapped)
                        .setSound(alarmSound)
                        .setGroup(ALARM_NOTIFICATION_GROUP_KEY)
                        .setGroupSummary(true)
                        .setAutoCancel(true)
                        .setDeleteIntent(senderSwiped);

                if (vibrateSetting) {
                    summaryNotificationBuilder.setVibrate(pattern);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    summaryNotificationBuilder.setCategory(CATEGORY_ALARM);
                }

                Notification summaryNotification = summaryNotificationBuilder.build();
                summaryNotification.flags |= Notification.FLAG_INSISTENT;
                int summaryId = 0;
                alarmNotificationManager.notify(summaryId, summaryNotification);
            }
        }

    }
}