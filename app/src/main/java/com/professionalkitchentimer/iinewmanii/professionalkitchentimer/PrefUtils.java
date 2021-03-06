package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by IINEWMANII on 11/19/2016.
 * Class used to save to and retrieve from shared preferences.
 */

class PrefUtils {

    private final SharedPreferences timerPreferences;
    private static final String ACTIVE_TIMER = "activeTimer";
    private static final String START_TIME_ONE = "startTime";
    private static final String START_TIME_TWO = "startTimeTwo";
    private static final String START_TIME_THREE = "startTimeThree";
    private static final String START_TIME_FOUR = "startTimeFour";
    private static final String ORIGINAL_TIME_ONE = "millisToCount";
    private static final String ORIGINAL_TIME_TWO = "millisToCountTwo";
    private static final String ORIGINAL_TIME_THREE = "millisToCountThree";
    private static final String ORIGINAL_TIME_FOUR = "millisToCountFour";
    private static final String PREVIOUS_HOURS_ONE = "previousHoursOne";
    private static final String PREVIOUS_MINUTES_ONE = "previousMinutesOne";
    private static final String PREVIOUS_HOURS_TWO = "previousHoursTwo";
    private static final String PREVIOUS_MINUTES_TWO = "previousMinutesTwo";
    private static final String PREVIOUS_HOURS_THREE = "previousHoursThree";
    private static final String PREVIOUS_MINUTES_THREE = "previousMinutesThree";
    private static final String PREVIOUS_HOURS_FOUR = "previousHoursFour";
    private static final String PREVIOUS_MINUTES_FOUR = "previousMinutesFour";
    private static final String PAUSED_TIME_ONE = "pausedTimeOne";
    private static final String PAUSED_TIME_TWO = "pausedTimeTwo";
    private static final String PAUSED_TIME_THREE = "pausedTimeThree";
    private static final String PAUSED_TIME_FOUR = "pausedTimeFour";
    private static final String TIMER_ONE_STATE = "timerOneState";
    private static final String TIMER_TWO_STATE = "timerTwoState";
    private static final String TIMER_THREE_STATE = "timerThreeState";
    private static final String TIMER_FOUR_STATE = "timerFourState";
    private static final String WAKE_UP_TIME_ONE = "wakeUpTimeOne";
    private static final String WAKE_UP_TIME_TWO = "wakeUpTimeTwo";
    private static final String WAKE_UP_TIME_THREE = "wakeUpTimeThree";
    private static final String WAKE_UP_TIME_FOUR = "wakeUpTimeFour";
    private static final String TIMER_NOTIFICATION_RUNNING = "timerNotificationRunning";
    private static final String ORIGINAL_NOTIFICATION_VOLUME = "originalNotificationVolume";
    private static final String ALARM_VOLUME = "alarmVolume";
    private static final String WARNING_ALARM_MINUTE = "warningAlarmMinute";
    private static final String WARNING_ALARM = "warningAlarm";
    private static final String VIBRATE_SETTING = "vibrateSetting";
    private static final String KEEP_SCREEN_ON = "keepScreenOn";
    private static final String TIMER_NOTIFICATION_ALARM = "timerNotificationAlarm";

    PrefUtils(Context context) {
        timerPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    int getOriginalNotificationVolume() {return timerPreferences.getInt(ORIGINAL_NOTIFICATION_VOLUME, 0);}

    void setOriginalNotificationVolume(int originalNotificationVolume) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(ORIGINAL_NOTIFICATION_VOLUME, originalNotificationVolume);
        editor.apply();
    }

    boolean getTimerNotificationAlarm() {return timerPreferences.getBoolean(TIMER_NOTIFICATION_ALARM, false);}

    void setTimerNotificationAlarm(boolean timerNotificationAlarm) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putBoolean(TIMER_NOTIFICATION_ALARM, timerNotificationAlarm);
        editor.apply();
    }

    boolean getWarningAlarm() {return timerPreferences.getBoolean(WARNING_ALARM, false);}

    void setWarningAlarm(boolean warningAlarm) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putBoolean(WARNING_ALARM, warningAlarm);
        editor.apply();
    }

    int getWarningAlarmMinute() {return timerPreferences.getInt(WARNING_ALARM_MINUTE, 5);}

    void setWarningAlarmMinute(int warningAlarmMinute) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(WARNING_ALARM_MINUTE, warningAlarmMinute);
        editor.apply();
    }

    boolean getVibrateSetting() {return timerPreferences.getBoolean(VIBRATE_SETTING, true);}

    void setVibrateSetting(boolean vibrateSetting) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putBoolean(VIBRATE_SETTING, vibrateSetting);
        editor.apply();
    }

    boolean getKeepScreenOn() {
        return timerPreferences.getBoolean(KEEP_SCREEN_ON, false);
    }

    void setKeepScreenOn(boolean keepScreenOn) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putBoolean(KEEP_SCREEN_ON, keepScreenOn);
        editor.apply();
    }

    int getAlarmVolume() {
        return timerPreferences.getInt(ALARM_VOLUME, 1);
    }

    void setAlarmVolume(int alarmVolume) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(ALARM_VOLUME, alarmVolume);
        editor.apply();
    }

    boolean getTimerNotificationRunning() {
        return timerPreferences.getBoolean(TIMER_NOTIFICATION_RUNNING, false);
    }

    void setTimerNotificationRunning(boolean timerNotificationRunning) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putBoolean(TIMER_NOTIFICATION_RUNNING, timerNotificationRunning);
        editor.apply();
    }

    long getWakeUpTimeOne() {
        return timerPreferences.getLong(WAKE_UP_TIME_ONE, 0);
    }

    long getWakeUpTimeTwo() {
        return timerPreferences.getLong(WAKE_UP_TIME_TWO, 0);
    }

    long getWakeUpTimeThree() {
        return timerPreferences.getLong(WAKE_UP_TIME_THREE, 0);
    }

    long getWakeUpTimeFour() {
        return timerPreferences.getLong(WAKE_UP_TIME_FOUR, 0);
    }

    void setWakeUpTimeOne(long wakeUpTimeOne) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(WAKE_UP_TIME_ONE, wakeUpTimeOne);
        editor.apply();
    }

    void setWakeUpTimeTwo(long wakeUpTimeTwo) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(WAKE_UP_TIME_TWO, wakeUpTimeTwo);
        editor.apply();
    }

    void setWakeUpTimeThree(long wakeUpTimeThree) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(WAKE_UP_TIME_THREE, wakeUpTimeThree);
        editor.apply();
    }

    void setWakeUpTimeFour(long wakeUpTimeFour) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(WAKE_UP_TIME_FOUR, wakeUpTimeFour);
        editor.apply();
    }

    int getTimerOneState() {return timerPreferences.getInt(TIMER_ONE_STATE, 0);}

    int getTimerTwoState() {return timerPreferences.getInt(TIMER_TWO_STATE, 0);}

    int getTimerThreeState() {return timerPreferences.getInt(TIMER_THREE_STATE, 0);}

    int getTimerFourState() {return timerPreferences.getInt(TIMER_FOUR_STATE, 0);}

    void setTimerOneState(int timerOneState) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(TIMER_ONE_STATE, timerOneState);
        editor.apply();
    }

    void setTimerTwoState(int timerTwoState) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(TIMER_TWO_STATE, timerTwoState);
        editor.apply();
    }

    void setTimerThreeState(int timerThreeState) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(TIMER_THREE_STATE, timerThreeState);
        editor.apply();
    }

    void setTimerFourState(int timerFourState) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(TIMER_FOUR_STATE, timerFourState);
        editor.apply();
    }

    int getActiveTimer() {return timerPreferences.getInt(ACTIVE_TIMER, TimerState.NO_ACTIVE_TIMER);}

    void setActiveTimer(int activeTimer) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(ACTIVE_TIMER, activeTimer);
        editor.apply();
    }

    long getPausedTime() {
        return timerPreferences.getLong(PAUSED_TIME_ONE, 0);
    }

    long getPausedTimeTwo() {
        return timerPreferences.getLong(PAUSED_TIME_TWO, 0);
    }

    long getPausedTimeThree() {
        return timerPreferences.getLong(PAUSED_TIME_THREE, 0);
    }

    long getPausedTimeFour() {
        return timerPreferences.getLong(PAUSED_TIME_FOUR, 0);
    }

    void setPausedTime(long pausedTime) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(PAUSED_TIME_ONE, pausedTime);
        editor.apply();
    }

    void setPausedTimeTwo(long pausedTimeTwo) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(PAUSED_TIME_TWO, pausedTimeTwo);
        editor.apply();
    }

    void setPausedTimeThree(long pausedTimeThree) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(PAUSED_TIME_THREE, pausedTimeThree);
        editor.apply();
    }

    void setPausedTimeFour(long pausedTimeFour) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(PAUSED_TIME_FOUR, pausedTimeFour);
        editor.apply();
    }

    int getPreviousHoursOne() {return timerPreferences.getInt(PREVIOUS_HOURS_ONE, 0);}

    int getPreviousHoursTwo() {return timerPreferences.getInt(PREVIOUS_HOURS_TWO, 0);}

    int getPreviousHoursThree() {return timerPreferences.getInt(PREVIOUS_HOURS_THREE, 0);}

    int getPreviousHoursFour() {return timerPreferences.getInt(PREVIOUS_HOURS_FOUR, 0);}

    void setPreviousHoursOne(int previousHoursOne) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(PREVIOUS_HOURS_ONE, previousHoursOne);
        editor.apply();
    }

    void setPreviousHoursTwo(int previousHoursTwo) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(PREVIOUS_HOURS_TWO, previousHoursTwo);
        editor.apply();
    }

    void setPreviousHoursThree(int previousHoursThree) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(PREVIOUS_HOURS_THREE, previousHoursThree);
        editor.apply();
    }

    void setPreviousHoursFour(int previousHoursFour) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(PREVIOUS_HOURS_FOUR, previousHoursFour);
        editor.apply();
    }

    int getPreviousMinutesOne() {return timerPreferences.getInt(PREVIOUS_MINUTES_ONE, 0);}

    int getPreviousMinutesTwo() {return timerPreferences.getInt(PREVIOUS_MINUTES_TWO, 0);}

    int getPreviousMinutesThree() {return timerPreferences.getInt(PREVIOUS_MINUTES_THREE, 0);}

    int getPreviousMinutesFour() {return timerPreferences.getInt(PREVIOUS_MINUTES_FOUR, 0);}

    void setPreviousMinutesOne(int previousMinutesOne) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(PREVIOUS_MINUTES_ONE, previousMinutesOne);
        editor.apply();
    }

    void setPreviousMinutesTwo(int previousMinutesTwo) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(PREVIOUS_MINUTES_TWO, previousMinutesTwo);
        editor.apply();
    }

    void setPreviousMinutesThree(int previousMinutesThree) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(PREVIOUS_MINUTES_THREE, previousMinutesThree);
        editor.apply();
    }

    void setPreviousMinutesFour(int previousMinutesFour) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putInt(PREVIOUS_MINUTES_FOUR, previousMinutesFour);
        editor.apply();
    }

    long getOriginalTime() {
        return timerPreferences.getLong(ORIGINAL_TIME_ONE, 0);
    }

    long getOriginalTimeTwo() {
        return timerPreferences.getLong(ORIGINAL_TIME_TWO, 0);
    }

    long getOriginalTimeThree() {
        return timerPreferences.getLong(ORIGINAL_TIME_THREE, 0);
    }

    long getOriginalTimeFour() {
        return timerPreferences.getLong(ORIGINAL_TIME_FOUR, 0);
    }

    void setOriginalTime(long originalMillisToCount) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(ORIGINAL_TIME_ONE, originalMillisToCount);
        editor.apply();
    }

    void setOriginalTimeTwo(long originalMillisToCountTwo) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(ORIGINAL_TIME_TWO, originalMillisToCountTwo);
        editor.apply();
    }

    void setOriginalTimeThree(long originalMillisToCountThree) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(ORIGINAL_TIME_THREE, originalMillisToCountThree);
        editor.apply();
    }

    void setOriginalTimeFour(long originalMillisToCountFour) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(ORIGINAL_TIME_FOUR, originalMillisToCountFour);
        editor.apply();
    }

    long getStartTime() {
        return timerPreferences.getLong(START_TIME_ONE, 0);
    }

    long getStartTimeTwo() {
        return timerPreferences.getLong(START_TIME_TWO, 0);
    }

    long getStartTimeThree() {
        return timerPreferences.getLong(START_TIME_THREE, 0);
    }

    long getStartTimeFour() {
        return timerPreferences.getLong(START_TIME_FOUR, 0);
    }

    void setStartTime(long started) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(START_TIME_ONE, started);
        editor.apply();
    }

    void setStartTimeTwo(long startedTwo) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(START_TIME_TWO, startedTwo);
        editor.apply();
    }

    void setStartTimeThree(long startedThree) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(START_TIME_THREE, startedThree);
        editor.apply();
    }

    void setStartTimeFour(long startedFour) {
        SharedPreferences.Editor editor = timerPreferences.edit();
        editor.putLong(START_TIME_FOUR, startedFour);
        editor.apply();
    }
}
