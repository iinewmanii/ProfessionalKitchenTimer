package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v7.app.NotificationCompat;
//import android.util.Log;
import android.widget.RemoteViews;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.support.v7.app.NotificationCompat.VISIBILITY_PUBLIC;

/**
 * Created by IINEWMANII on 12/28/2016.
 * Service to update timer notification in a thread.
 */

public class TimerNotifyService extends Service {

//    private static final String TAG = "NEWMAN";

    private HandlerThread handlerThread;

    private Handler timerNotificationHandler;

    private NotificationCompat.Builder timerNotificationBuilder;

    private Notification timerNotification;

    private NotificationManager timerNotificationManager;

    private PrefUtils timerPreferences;

    private PowerManager powerManager;

    private final Runnable timerNotificationUpdate = new TimerNotificationUpdateRunnable();

    private static final int TIMER_NOTIFICATION_ID = 13;

    private static final int TIMER_NOTIF_UPDATE_DELAY = 1000;

    private static final String NOT_SET = "0:00:00";

    private static final String PACKAGE_NAME = "com.professionalkitchentimer.iinewmanii.professionalkitchentimer";

    private boolean timerNotificationRunning;
    private boolean timerPaused;
    private boolean timerTwoPaused;
    private boolean timerThreePaused;
    private boolean timerFourPaused;

    private long pausedTime;
    private long pausedTimeTwo;
    private long pausedTimeThree;
    private long pausedTimeFour;

    private long wakeUpTime;
    private long wakeUpTimeTwo;
    private long wakeUpTimeThree;
    private long wakeUpTimeFour;

    private long timerNotifTimeOne;
    private long timerNotifTimeTwo;
    private long timerNotifTimeThree;
    private long timerNotifTimeFour;

    public void onCreate() {
        super.onCreate();
//        Log.v(TAG, "Timer Notify Service Created");

        handlerThread = new HandlerThread("TimerNotificationHandler");

        handlerThread.start();

        timerNotificationHandler = new Handler(handlerThread.getLooper());

        timerNotificationBuilder = new NotificationCompat.Builder(this);

        timerNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        timerPreferences = new PrefUtils(this);

        powerManager = (PowerManager) getSystemService(POWER_SERVICE);

        wakeUpTime = timerPreferences.getWakeUpTimeOne();
        wakeUpTimeTwo = timerPreferences.getWakeUpTimeTwo();
        wakeUpTimeThree = timerPreferences.getWakeUpTimeThree();
        wakeUpTimeFour = timerPreferences.getWakeUpTimeFour();

        timerPaused = timerPreferences.getTimerPaused();
        timerTwoPaused = timerPreferences.getTimerTwoPaused();
        timerThreePaused = timerPreferences.getTimerThreePaused();
        timerFourPaused = timerPreferences.getTimerFourPaused();

        pausedTime = timerPreferences.getPausedTime();
        pausedTimeTwo = timerPreferences.getPausedTimeTwo();
        pausedTimeThree = timerPreferences.getPausedTimeThree();
        pausedTimeFour = timerPreferences.getPausedTimeFour();

        sendNotification();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        timerNotificationHandler.post(timerNotificationUpdate);

        return START_STICKY;
    }

/**
 * Post initial timer notification
*/
    private void sendNotification() {
        timerNotifTimeOne = timerNotifTime(wakeUpTime);
        timerNotifTimeTwo = timerNotifTime(wakeUpTimeTwo);
        timerNotifTimeThree = timerNotifTime(wakeUpTimeThree);
        timerNotifTimeFour = timerNotifTime(wakeUpTimeFour);

//        Log.v(TAG, "timerNotifTimeOne = " + timerNotifTimeOne);

        Intent timerNotifIntent = new Intent(this, MainActivity.class);
        timerNotifIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingTimerNotifIntent = PendingIntent.getActivity(this,
                TIMER_NOTIFICATION_ID, timerNotifIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        RemoteViews remoteTimerViews = new RemoteViews(PACKAGE_NAME, R.layout.timer_notification);

        if (timerNotifTimeOne > 0 && !timerPaused) {
            String timeOne = timerFormat(timerNotifTimeOne);

            remoteTimerViews.setTextViewText(R.id.timer_one_notification_view, timeOne);
        } else if (timerPaused) {
            String pTimeOne = timerFormat(pausedTime);

            remoteTimerViews.setTextViewText(R.id.timer_one_notification_view, pTimeOne);
        } else {
            remoteTimerViews.setTextViewText(R.id.timer_one_notification_view, NOT_SET);
        }

        if (timerNotifTimeTwo > 0 && !timerTwoPaused) {
            String timeTwo = timerFormat(timerNotifTimeTwo);

            remoteTimerViews.setTextViewText(R.id.timer_two_notification_view, timeTwo);
        } else if (timerTwoPaused) {
            String pTimeTwo = timerFormat(pausedTimeTwo);

            remoteTimerViews.setTextViewText(R.id.timer_two_notification_view, pTimeTwo);
        } else {
            remoteTimerViews.setTextViewText(R.id.timer_two_notification_view, NOT_SET);
        }

        if (timerNotifTimeThree > 0 && !timerThreePaused) {
            String timeThree = timerFormat(timerNotifTimeThree);

            remoteTimerViews.setTextViewText(R.id.timer_three_notification_view, timeThree);
        } else if (timerThreePaused) {
            String pTimeThree = timerFormat(pausedTimeThree);

            remoteTimerViews.setTextViewText(R.id.timer_three_notification_view, pTimeThree);
        } else {
            remoteTimerViews.setTextViewText(R.id.timer_three_notification_view, NOT_SET);
        }

        if (timerNotifTimeFour > 0 && !timerFourPaused) {
            String timeFour = timerFormat(timerNotifTimeFour);

            remoteTimerViews.setTextViewText(R.id.timer_four_notification_view, timeFour);
        } else if (timerFourPaused) {
            String pTimeFour = timerFormat(pausedTimeFour);

            remoteTimerViews.setTextViewText(R.id.timer_four_notification_view, pTimeFour);
        }
        else {
            remoteTimerViews.setTextViewText(R.id.timer_four_notification_view, NOT_SET);
        }

        timerNotificationBuilder.setPriority(Notification.PRIORITY_HIGH)
                .setAutoCancel(false)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.ic_pkt_icon_ticker)
                .setContentIntent(pendingTimerNotifIntent)
                .setCustomContentView(remoteTimerViews)
                .setCustomBigContentView(remoteTimerViews)
                .setVisibility(VISIBILITY_PUBLIC);

        timerNotification = timerNotificationBuilder.build();
        timerNotification.flags |= Notification.FLAG_ONGOING_EVENT;

        startForeground(TIMER_NOTIFICATION_ID, timerNotification);
    }

/**
 * Update timer notification once per second
*/
    private void updateNotification() {
        timerNotificationRunning = timerPreferences.getTimerNotificationRunning();

        if (powerManager.isInteractive() && timerNotificationRunning) {
//            Log.v(TAG, "updateNotification");

            timerNotifTimeOne = timerNotifTime(wakeUpTime);
            timerNotifTimeTwo = timerNotifTime(wakeUpTimeTwo);
            timerNotifTimeThree = timerNotifTime(wakeUpTimeThree);
            timerNotifTimeFour = timerNotifTime(wakeUpTimeFour);

            RemoteViews remoteTimerViews = new RemoteViews(PACKAGE_NAME, R.layout.timer_notification);

            if (timerNotifTimeOne > 0 && !timerPaused) {
                String timeOne = timerFormat(timerNotifTimeOne);

                remoteTimerViews.setTextViewText(R.id.timer_one_notification_view, timeOne);
            }

            if (timerNotifTimeTwo > 0 && !timerTwoPaused) {
                String timeTwo = timerFormat(timerNotifTimeTwo);

                remoteTimerViews.setTextViewText(R.id.timer_two_notification_view, timeTwo);
            }

            if (timerNotifTimeThree > 0 && !timerThreePaused) {
                String timeThree = timerFormat(timerNotifTimeThree);

                remoteTimerViews.setTextViewText(R.id.timer_three_notification_view, timeThree);
            }

            if (timerNotifTimeFour > 0 && !timerFourPaused) {
                String timeFour = timerFormat(timerNotifTimeFour);

                remoteTimerViews.setTextViewText(R.id.timer_four_notification_view, timeFour);
            }

            timerNotificationBuilder.setCustomContentView(remoteTimerViews)
                    .setCustomBigContentView(remoteTimerViews);

            timerNotification = timerNotificationBuilder.build();
            timerNotification.flags |= Notification.FLAG_ONGOING_EVENT;

            timerNotificationManager.notify(TIMER_NOTIFICATION_ID, timerNotification);
        }
    }

/**
 * Format the timer value so it is easier for the user to read
*/
    private static String timerFormat(long l) {
        return String.format(Locale.ENGLISH, "%01d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(l),
                TimeUnit.MILLISECONDS.toMinutes(l) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(l)),
                TimeUnit.MILLISECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)));
    }

/**
 * Method for calculating time left on timer
*/
    private static long timerNotifTime(long l) {
        return l - System.currentTimeMillis();
    }

/**
 * This is to prevent having an anonymous inner class and an abstract runnable
*/
    private class TimerNotificationUpdateRunnable implements Runnable {
        @Override
        public void run() {
            if (timerNotificationHandler != null) {
                updateNotification();

                timerNotificationHandler.postDelayed(this, TIMER_NOTIF_UPDATE_DELAY);
            }
        }
    }

/**
 * Nullify objects to free memory
*/
    private void quitNotification() {
        if (timerNotificationManager != null) {
            timerNotificationManager.cancel(TIMER_NOTIFICATION_ID);
            timerNotificationManager = null;
        }

        if (timerNotification != null) {
            timerNotification = null;
        }

        if (timerNotificationBuilder != null) {
            timerNotificationBuilder = null;
        }

        if (handlerThread != null) {
            handlerThread.quitSafely();
            handlerThread = null;
        }

        if (timerNotificationHandler != null) {
            timerNotificationHandler = null;
        }

        if (powerManager != null) {
            powerManager = null;
        }

        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        timerNotificationHandler.removeCallbacks(timerNotificationUpdate);

        quitNotification();

        super.onDestroy();
    }
}
