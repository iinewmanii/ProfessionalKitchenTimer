package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import static android.media.AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE;

//import android.util.Log;

/**
 * Created by IINEWMANII on 7/19/2017.
 * Class to play warning sound.
 */

// TODO: 1/29/18 Intermittent timers settings option. 

public class WarningAlarmReceiver extends BroadcastReceiver {

//    private final static String TAG = "NEWMAN";

    private AudioManager audioManager;

    private MediaPlayer mediaPlayer;

    private Vibrator vibrator;

    private int originalVolume;

    @Override
    public void onReceive(Context context, Intent intent) {

        boolean warningAlarmSet = intent.getBooleanExtra("playWarningAlarm", false);

        if (warningAlarmSet) {
//            Log.v(TAG, "* ALERT Warning Alarm ALERT *");
            playWarningAlarm(context);
        }
    }

    /** Method to play a warning alarm and vibrate **/
    private void playWarningAlarm(Context context) {

        PrefUtils timerPreferences = new PrefUtils(context);

        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        boolean vibrateSetting = timerPreferences.getVibrateSetting();
        int volume = timerPreferences.getAlarmVolume();
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, FLAG_REMOVE_SOUND_AND_VIBRATE);

        mediaPlayer = MediaPlayer.create(context, R.raw.alarm_clock_short);
        mediaPlayer.setOnCompletionListener(new WarningAlarmCompletionListener());
        mediaPlayer.start();

        if (vibrator.hasVibrator() && vibrateSetting) {
            vibrator.vibrate(800);
        }
    }

    /** Method to stop alarm and return volume level to previous value **/
    private void stopAlarm() {

        if (vibrator != null) {
            vibrator.cancel();
        }

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.reset();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
            audioManager = null;
        }
    }

    /** Inner class to listen for completion of alarm and call stop alarm once audio completes **/
    private class WarningAlarmCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
//            Log.v(TAG, "Warning alarm complete");
            stopAlarm();
        }
    }
}
