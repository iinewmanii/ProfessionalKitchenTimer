package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.app.AlarmManager;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.messaging.FirebaseMessaging;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import android.support.annotation.IntDef;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.media.AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE;

//import android.util.Log;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener, View.OnTouchListener {

    private TextView inputHours,
            inputMinutes,
            inputSeconds,
            clockView1,
            clockView2,
            clockView3,
            clockView4,
            timerIndicator;
    private Button t1_button,
            t2_button,
            t3_button,
            t4_button,
            minute_plus_button,
            minute_minus_button,
            reset_button;
    private AdView mAdView;
    private String hms;
    private Handler repeatMinuteHandler;
    private Handler resetAllHandler;
    private PrefUtils timerPreferences;
    private int timerSecond;
    private int timerMinute;
    private int timerHour;
    private int originalVolume;
    private int warningAlarmStart;
    private int warningAlarmEnd;
    private boolean mAutoIncrement;
    private boolean mAutoDecrement;

    /** This is an IntDef to indicate which timer is the active timer **/
    @Retention(RetentionPolicy.SOURCE)

    @IntDef({TIMER_ONE,
            TIMER_TWO,
            TIMER_THREE,
            TIMER_FOUR,
            NO_ACTIVE_TIMER})

    @interface TimerActive {}

    public static final int NO_ACTIVE_TIMER = 0;
    public static final int TIMER_ONE = 1;
    public static final int TIMER_TWO = 2;
    public static final int TIMER_THREE = 3;
    public static final int TIMER_FOUR = 4;

    private int activeTimer;

    /** These are IntDefs to keep track of timer state **/
    @Retention(RetentionPolicy.SOURCE)

    @IntDef({INPUT, RUNNING, PAUSED})
    @interface TimerOneState {}

    @IntDef({INPUT, RUNNING, PAUSED})
    @interface TimerTwoState {}

    @IntDef({INPUT, RUNNING, PAUSED})
    @interface TimerThreeState {}

    @IntDef({INPUT, RUNNING, PAUSED})
    @interface TimerFourState {}

    public static final int INPUT = 0;
    public static final int RUNNING = 1;
    public static final int PAUSED = 2;

    private int timerOneState;
    private int timerTwoState;
    private int timerThreeState;
    private int timerFourState;

    private boolean timerNotificationRunning;
    private boolean timerWarning;
    private boolean timerAlarm;
    private boolean isLongPress;
    private CountTimer countDownTimer,
            countDownTimerTwo,
            countDownTimerThree,
            countDownTimerFour;
    private long pausedTime;
    private long pausedTimeTwo;
    private long pausedTimeThree;
    private long pausedTimeFour;
    private long millisToCount;
    private long millisToCountTwo;
    private long millisToCountThree;
    private long millisToCountFour;
    private long wakeUpTime;
    private long wakeUpTimeTwo;
    private long wakeUpTimeThree;
    private long wakeUpTimeFour;
    private MediaPlayer alarmPlayer;
    private AudioManager audioManager;
    private AudioManager.OnAudioFocusChangeListener afChangeListener;
    private Vibrator vibrator;

//    private static final String TAG = "NEWMAN";
    private static final String CREATE_NOTIFICATION_ACTION = "CREATE_NOTIFICATION";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
//        Log.v(TAG, "onCreate");

        setContentView(R.layout.activity_main);

        inputSeconds = (TextView) findViewById(R.id.time_input_seconds);
        inputMinutes = (TextView) findViewById(R.id.time_input_minutes);
        inputHours = (TextView) findViewById(R.id.time_input_hours);
        clockView1 = (TextView) findViewById(R.id.clock_text_view_1);
        clockView2 = (TextView) findViewById(R.id.clock_text_view_2);
        clockView3 = (TextView) findViewById(R.id.clock_text_view_3);
        clockView4 = (TextView) findViewById(R.id.clock_text_view_4);
        timerIndicator = (TextView) findViewById(R.id.timer_indicator);

        t1_button = (Button) findViewById(R.id.t1_button);
        t2_button = (Button) findViewById(R.id.t2_button);
        t3_button = (Button) findViewById(R.id.t3_button);
        t4_button = (Button) findViewById(R.id.t4_button);
        minute_plus_button = (Button) findViewById(R.id.minute_plus_button);
        minute_minus_button = (Button) findViewById(R.id.minute_minus_button);
        reset_button = (Button) findViewById(R.id.reset_button);

        if (getScreenWidthDp(this) >= 360) {
            MobileAds.initialize(this, getResources().getString(R.string.ad_unit_id));

            if (mAdView == null) {
                mAdView = (AdView) findViewById(R.id.adView);

                AdRequest adRequest = new AdRequest.Builder()
                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                        .addTestDevice("A95D2EF9F7259D162EA1A427BB252242")
                        .build();
                mAdView.loadAd(adRequest);
                mAdView.bringToFront();
            }
        }

        FirebaseMessaging.getInstance().subscribeToTopic("global");
//        FirebaseMessaging.getInstance().subscribeToTopic("test");
    }

    @Override
    public void onStart() {

        super.onStart();
//        Log.v(TAG, "onStart");

        if (repeatMinuteHandler == null) {
            repeatMinuteHandler = new Handler();
        }

        if (resetAllHandler == null) {
            resetAllHandler = new Handler();
        }

        t1_button.setOnLongClickListener(this);
        t2_button.setOnLongClickListener(this);
        t3_button.setOnLongClickListener(this);
        t4_button.setOnLongClickListener(this);
        minute_plus_button.setOnLongClickListener(this);
        minute_plus_button.setOnTouchListener(this);
        minute_minus_button.setOnLongClickListener(this);
        minute_minus_button.setOnTouchListener(this);
        reset_button.setOnTouchListener(this);

    }

    @Override
    public void onResume() {

        super.onResume();
//        Log.v(TAG, "onResume");

        timerPreferences = new PrefUtils(this);

        boolean keepScreenOn = timerPreferences.getKeepScreenOn();

        boolean timerNotificationAlarm = timerPreferences.getTimerNotificationAlarm();

        int originalNotificationVolume = timerPreferences.getOriginalNotificationVolume();

        if (keepScreenOn) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }

        setActiveTimer(timerPreferences.getActiveTimer());

        setTimerOneState(timerPreferences.getTimerOneState());
        setTimerTwoState(timerPreferences.getTimerTwoState());
        setTimerThreeState(timerPreferences.getTimerThreeState());
        setTimerFourState(timerPreferences.getTimerFourState());

        millisToCount = timerPreferences.getOriginalTime();
        millisToCountTwo = timerPreferences.getOriginalTimeTwo();
        millisToCountThree = timerPreferences.getOriginalTimeThree();
        millisToCountFour = timerPreferences.getOriginalTimeFour();
        pausedTime = timerPreferences.getPausedTime();
        pausedTimeTwo = timerPreferences.getPausedTimeTwo();
        pausedTimeThree = timerPreferences.getPausedTimeThree();
        pausedTimeFour = timerPreferences.getPausedTimeFour();
        timerNotificationRunning = timerPreferences.getTimerNotificationRunning();
        timerWarning = timerPreferences.getWarningAlarm();
        warningAlarmEnd = timerPreferences.getWarningAlarmMinute() * 60000;
        warningAlarmStart = warningAlarmEnd + 1000;

        if (timerNotificationAlarm) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, originalNotificationVolume, 0);
            }
            timerPreferences.setTimerNotificationAlarm(false);
        }

        if (activeTimer == NO_ACTIVE_TIMER) {
            setActiveTimer(TIMER_ONE);

            setInputVis();

            timerIndicator.setText(R.string.timer_one_indicator);
        }

    if ((timerOneState | timerTwoState | timerThreeState | timerFourState) == RUNNING) {
            removeAlarmManager();
            initTimer();
//            Log.v(TAG, "Alarm Manager Removed, Initialize Timer");
        }

        if ((timerOneState == PAUSED) && (pausedTime > 0)) {
            hms = timerFormat(pausedTime);
            clockView1.setText(hms);
        }

        if ((timerTwoState == PAUSED) && (pausedTimeTwo > 0)) {
            hms = timerFormat(pausedTimeTwo);
            clockView2.setText(hms);
        }

        if ((timerThreeState == PAUSED) && (pausedTimeThree > 0)) {
            hms = timerFormat(pausedTimeThree);
            clockView3.setText(hms);
        }

        if ((timerFourState == PAUSED) && (pausedTimeFour > 0)) {
            hms = timerFormat(pausedTimeFour);
            clockView4.setText(hms);
        }

        if ((activeTimer == TIMER_ONE) && ((pausedTime > 0) || (timerOneState == RUNNING))) {
            setClockViewOneVis();
            timerIndicator.setText(R.string.timer_one_indicator);
        } else if ((activeTimer == TIMER_ONE) && (pausedTime <= 0)) {
            setInputVis();
            timerIndicator.setText(R.string.timer_one_indicator);
        }

        if ((activeTimer == TIMER_TWO) && ((pausedTimeTwo > 0) || (timerTwoState == RUNNING))) {
            setClockViewTwoVis();
            timerIndicator.setText(R.string.timer_two_indicator);
        } else if ((activeTimer == TIMER_TWO) && (pausedTimeTwo <= 0)) {
            setInputVis();
            timerIndicator.setText(R.string.timer_two_indicator);
        }

        if ((activeTimer == TIMER_THREE) && ((pausedTimeThree > 0) || (timerThreeState == RUNNING))) {
            setClockViewThreeVis();
            timerIndicator.setText(R.string.timer_three_indicator);
        } else if ((activeTimer == TIMER_THREE) && (pausedTimeThree <= 0)) {
            setInputVis();
            timerIndicator.setText(R.string.timer_three_indicator);
        }

        if ((activeTimer == TIMER_FOUR) && ((pausedTimeFour > 0) || (timerFourState == RUNNING))) {
            setClockViewFourVis();
            timerIndicator.setText(R.string.timer_four_indicator);
        } else if ((activeTimer == TIMER_FOUR) && (pausedTimeFour <= 0)) {
            setInputVis();
            timerIndicator.setText(R.string.timer_four_indicator);
        }

        /*timerNotificationRunning = false;
        timerPreferences.setTimerNotificationRunning(false);*/

        if (timerNotificationRunning) {
            Intent timerNotifIntent = new Intent(this, TimerNotifyService.class);
            stopService(timerNotifIntent);
            timerPreferences.setTimerNotificationRunning(false);
            timerNotificationRunning = false;
        }

        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    public void onPause() {
//        Log.v(TAG, "onPause");

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        timerPreferences.setActiveTimer(activeTimer);

//        Log.v(TAG, "onPause pausedTime = " + pausedTime);

        if (timerOneState == RUNNING) {
            timerPreferences.setOriginalTime(millisToCount);
            timerPreferences.setPausedTime(pausedTime);
            timerPreferences.setTimerOneState(timerOneState);
//            Log.v(TAG, "timerPreferences timerOneRunning = true");
            countDownTimer.cancel();
            countDownTimer = null;
        } else if (timerOneState == INPUT) {
            millisToCount = 0;
            timerPreferences.setOriginalTime(0);
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        } else {
            timerPreferences.setTimerOneState(timerOneState);
            timerPreferences.setPausedTime(pausedTime);
            timerPreferences.setOriginalTime(millisToCount);
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        }

        if (timerTwoState == RUNNING) {
            timerPreferences.setOriginalTimeTwo(millisToCountTwo);
            timerPreferences.setPausedTimeTwo(pausedTimeTwo);
            timerPreferences.setTimerTwoState(timerTwoState);
            countDownTimerTwo.cancel();
            countDownTimerTwo = null;
        } else if (timerTwoState == INPUT) {
            millisToCountTwo = 0;
            timerPreferences.setOriginalTimeTwo(0);
            if (countDownTimerTwo != null) {
                countDownTimerTwo.cancel();
                countDownTimerTwo = null;
            }
        } else {
            timerPreferences.setTimerTwoState(timerTwoState);
            timerPreferences.setPausedTimeTwo(pausedTimeTwo);
            timerPreferences.setOriginalTimeTwo(millisToCountTwo);
            if (countDownTimerTwo != null) {
                countDownTimerTwo.cancel();
                countDownTimerTwo = null;
            }
        }

        if (timerThreeState == RUNNING) {
            timerPreferences.setOriginalTimeThree(millisToCountThree);
            timerPreferences.setPausedTimeThree(pausedTimeThree);
            timerPreferences.setTimerThreeState(timerThreeState);
            countDownTimerThree.cancel();
            countDownTimerThree = null;
        } else if (timerThreeState == INPUT) {
            millisToCountThree = 0;
            timerPreferences.setOriginalTimeThree(0);
            if (countDownTimerThree != null) {
                countDownTimerThree.cancel();
                countDownTimerThree = null;
            }
        } else {
            timerPreferences.setTimerThreeState(timerThreeState);
            timerPreferences.setPausedTimeThree(pausedTimeThree);
            timerPreferences.setOriginalTimeThree(millisToCountThree);
            if (countDownTimerThree != null) {
                countDownTimerThree.cancel();
                countDownTimerThree = null;
            }
        }

        if (timerFourState == RUNNING) {
            timerPreferences.setOriginalTimeFour(millisToCountFour);
            timerPreferences.setPausedTimeFour(pausedTimeFour);
            timerPreferences.setTimerFourState(timerFourState);
            countDownTimerFour.cancel();
            countDownTimerFour = null;
        } else if (timerFourState == INPUT) {
            millisToCountFour = 0;
            timerPreferences.setOriginalTimeFour(0);
            if (countDownTimerFour != null) {
                countDownTimerFour.cancel();
                countDownTimerFour = null;
            }
        } else {
            timerPreferences.setTimerFourState(timerFourState);
            timerPreferences.setPausedTimeFour(pausedTimeFour);
            timerPreferences.setOriginalTimeFour(millisToCountFour);
            if (countDownTimerFour != null) {
                countDownTimerFour.cancel();
                countDownTimerFour = null;
            }
        }

        setAlarmManager();

        timerPreferences.setWakeUpTimeOne(wakeUpTime);
        timerPreferences.setWakeUpTimeTwo(wakeUpTimeTwo);
        timerPreferences.setWakeUpTimeThree(wakeUpTimeThree);
        timerPreferences.setWakeUpTimeFour(wakeUpTimeFour);


        if (!timerNotificationRunning && ((timerOneState | timerTwoState | timerThreeState | timerFourState ) == RUNNING)) {
            Intent timerNotifIntent = new Intent(this, TimerNotifyService.class);
            startService(timerNotifIntent);
            timerPreferences.setTimerNotificationRunning(true);
//            Log.v(TAG, "Starting Timer notify Service");
        }

        if (mAdView != null) {
            mAdView.pause();
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
//        Log.v(TAG, "onStop");

        t1_button.setOnLongClickListener(null);
        t2_button.setOnLongClickListener(null);
        t3_button.setOnLongClickListener(null);
        t4_button.setOnLongClickListener(null);
        minute_plus_button.setOnLongClickListener(null);
        minute_plus_button.setOnTouchListener(null);
        minute_minus_button.setOnLongClickListener(null);
        minute_minus_button.setOnTouchListener(null);

        repeatMinuteHandler.removeCallbacksAndMessages(null);
        resetAllHandler.removeCallbacksAndMessages(null);

        timerPreferences = null;

        hms = null;

        super.onStop();
    }

    @Override
    public void onDestroy() {
//        Log.v(TAG, "onDestroy");

        if (mAdView != null) {
            mAdView.destroy();
            mAdView = null;
        }

        super.onDestroy();
    }

    @Override
    public boolean onLongClick(View view) {

        switch (view.getId()) {
            case R.id.minute_plus_button:
                mAutoIncrement = true;
                repeatMinuteHandler.post(new RptMinuteUpdater());
                break;

            case R.id.minute_minus_button:
                mAutoDecrement = true;
                repeatMinuteHandler.post(new RptMinuteUpdater());
                break;

            case R.id.t1_button:
                    if (((activeTimer == TIMER_ONE) && (timerOneState == INPUT)) ||
                            ((activeTimer == TIMER_TWO) && (timerTwoState == INPUT)) ||
                            ((activeTimer == TIMER_THREE) && (timerThreeState == INPUT)) ||
                            ((activeTimer == TIMER_FOUR) && (timerFourState == INPUT))) {
                        timerHour = timerPreferences.getPreviousHoursOne();
                        timerMinute = timerPreferences.getPreviousMinutesOne();
                        setInputText();

                        setInputVis();
                    }
                break;

            case R.id.t2_button:
                if (((activeTimer == TIMER_ONE) && (timerOneState == INPUT)) ||
                        ((activeTimer == TIMER_TWO) && (timerTwoState == INPUT)) ||
                        ((activeTimer == TIMER_THREE) && (timerThreeState == INPUT)) ||
                        ((activeTimer == TIMER_FOUR) && (timerFourState == INPUT))) {
                    timerHour = timerPreferences.getPreviousHoursTwo();
                    timerMinute = timerPreferences.getPreviousMinutesTwo();
                    setInputText();

                    setInputVis();
                }
                break;

            case R.id.t3_button:
                if (((activeTimer == TIMER_ONE) && (timerOneState == INPUT)) ||
                        ((activeTimer == TIMER_TWO) && (timerTwoState == INPUT)) ||
                        ((activeTimer == TIMER_THREE) && (timerThreeState == INPUT)) ||
                        ((activeTimer == TIMER_FOUR) && (timerFourState == INPUT))) {
                    timerHour = timerPreferences.getPreviousHoursThree();
                    timerMinute = timerPreferences.getPreviousMinutesThree();
                    setInputText();

                    setInputVis();
                }
                break;

            case R.id.t4_button:
                if (((activeTimer == TIMER_ONE) && (timerOneState == INPUT)) ||
                        ((activeTimer == TIMER_TWO) && (timerTwoState == INPUT)) ||
                        ((activeTimer == TIMER_THREE) && (timerThreeState == INPUT)) ||
                        ((activeTimer == TIMER_FOUR) && (timerFourState == INPUT))) {
                    timerHour = timerPreferences.getPreviousHoursFour();
                    timerMinute = timerPreferences.getPreviousMinutesFour();
                    setInputText();

                    setInputVis();
                }
                break;

            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {

        if ((view == minute_plus_button) && ((motionEvent.getAction() == MotionEvent.ACTION_UP) ||
                (motionEvent.getAction() == MotionEvent.ACTION_CANCEL)) && mAutoIncrement) {
            mAutoIncrement = false;
        } else if ((view == minute_minus_button) && ((motionEvent.getAction() == MotionEvent.ACTION_UP) ||
                (motionEvent.getAction() == MotionEvent.ACTION_CANCEL)) && mAutoDecrement) {
            mAutoDecrement = false;
        }

        if ((view == reset_button) && (motionEvent.getAction() == MotionEvent.ACTION_DOWN)) {
            isLongPress = true;
            resetAllHandler.postDelayed(new ResetAllRunnable(), 1500);
        } else if ((view == reset_button) && (motionEvent.getAction() == MotionEvent.ACTION_UP)) {
            resetAllHandler.removeCallbacksAndMessages(null);
        }

        return false;
    }

    public void timerOneButton(View view) {

        if (view.getId() == R.id.t1_button) {
            if ((activeTimer != TIMER_ONE) && (timerOneState != INPUT)) {
//            Log.v(TAG, "Timer One Selected");

                setActiveTimer(TIMER_ONE);

                setClockViewOneVis();

                timerIndicator.setText(R.string.timer_one_indicator);
            } else {
//            Log.v(TAG, "Timer One Selected");
                setActiveTimer(TIMER_ONE);

                resetTimerInputValues();

                setInputText();

                setInputVis();

                timerIndicator.setText(R.string.timer_one_indicator);
            }
        }
    }

    public void timerTwoButton(View view) {

        if (view.getId() == R.id.t2_button) {
            if ((activeTimer != TIMER_TWO) && (timerTwoState != INPUT)) {
//            Log.v(TAG, "Timer Two Selected");
                setActiveTimer(TIMER_TWO);

                setClockViewTwoVis();

                timerIndicator.setText(R.string.timer_two_indicator);
            } else {
//            Log.v(TAG, "Timer Two Selected");
                setActiveTimer(TIMER_TWO);

                resetTimerInputValues();

                setInputText();

                setInputVis();

                timerIndicator.setText(R.string.timer_two_indicator);
            }
        }
    }

    public void timerThreeButton(View view) {

        if (view.getId() == R.id.t3_button) {
            if ((activeTimer != TIMER_THREE) && (timerThreeState != INPUT)) {
//            Log.v(TAG, "Timer Three Selected");
                setActiveTimer(TIMER_THREE);

                setClockViewThreeVis();

                timerIndicator.setText(R.string.timer_three_indicator);
            } else {
//            Log.v(TAG, "Timer Three Selected");
                setActiveTimer(TIMER_THREE);

                resetTimerInputValues();

                setInputText();

                setInputVis();

                timerIndicator.setText(R.string.timer_three_indicator);
            }
        }
    }

    public void timerFourButton(View view) {

        if (view.getId() == R.id.t4_button) {
            if ((activeTimer != TIMER_FOUR) && (timerFourState != INPUT)) {
//            Log.v(TAG, "Timer Four Selected");
                setActiveTimer(TIMER_FOUR);

                setClockViewFourVis();

                timerIndicator.setText(R.string.timer_four_indicator);
            } else {
//            Log.v(TAG, "Timer Four Selected");
                setActiveTimer(TIMER_FOUR);

                resetTimerInputValues();

                setInputText();

                setInputVis();

                timerIndicator.setText(R.string.timer_four_indicator);
            }
        }
    }

    public void minutePlus(View view) {

        if (view.getId() == R.id.minute_plus_button) {
            if (timerMinute == 59) {
                timerMinute = 0;
                inputMinutes.setText(String.format(Locale.ENGLISH, ":%02d", timerMinute));

                timerHour += 1;
                inputHours.setText(String.format(Locale.ENGLISH, "%01d", timerHour));
            } else if (timerMinute < 59) {
//            Log.v(TAG, "Minute Plus");
                timerMinute += 1;
                inputMinutes.setText(String.format(Locale.ENGLISH, ":%02d", timerMinute));
            }
        }
    }

    public void minuteMinus(View view) {

        if (view.getId() == R.id.minute_minus_button) {
            if ((timerMinute == 0) && (timerHour > 0)) {
                timerHour -= 1;
                inputHours.setText(String.format(Locale.ENGLISH, "%01d", timerHour));

                timerMinute = 59;
                inputMinutes.setText(String.format(Locale.ENGLISH, ":%02d", timerMinute));

            } else if (timerMinute > 0) {
//            Log.v(TAG, "Minute Minus");
                timerMinute -= 1;
                inputMinutes.setText(String.format(Locale.ENGLISH, ":%02d", timerMinute));
            }
        }
    }

    public void hourPlus(View view) {

        if ((view.getId() == R.id.hour_plus_button) && (timerHour < 9)) {
//            Log.v(TAG, "Hour Plus");
            timerHour += 1;
            inputHours.setText(String.format(Locale.ENGLISH, "%01d", timerHour));
        }
    }

    public void hourMinus(View view) {

        if ((view.getId() == R.id.hour_minus_button) && (timerHour > 0)) {
//            Log.v(TAG, "Hour Minus");
            timerHour -= 1;
            inputHours.setText(String.format(Locale.ENGLISH, "%01d", timerHour));
        }
    }

    public void startButton(View view) {

        if (view.getId() == R.id.start_button) {
            if ((activeTimer == TIMER_ONE) && (timerOneState == INPUT) && ((timerHour > 0) || (timerMinute > 0))) {
                setClockViewOneVis();

                millisToCount = convertMillis();
                countDownTimer = new CountTimer(millisToCount, 1);
                hms = timerFormat(millisToCount);
                clockView1.setText(hms);
                timerPreferences.setStartTime(getNow());
                countDownTimer.start();
//            Log.v(TAG, "Timer Start");
                setTimerOneState(RUNNING);

                timerPreferences.setPreviousHoursOne(timerHour);
                timerPreferences.setPreviousMinutesOne(timerMinute);

                resetTimerInputValues();

                setInputText();
            } else if ((activeTimer == TIMER_ONE) && (timerOneState == PAUSED)) {
//            Log.v(TAG, "pausedTime = " + pausedTime);
                timerResume();
            }

            if ((activeTimer == TIMER_TWO) && (timerTwoState == INPUT) && ((timerHour > 0) || (timerMinute > 0))) {
                setClockViewTwoVis();

                millisToCountTwo = convertMillis();
                countDownTimerTwo = new CountTimer(millisToCountTwo, 2);
                hms = timerFormat(millisToCountTwo);
                clockView2.setText(hms);
                timerPreferences.setStartTimeTwo(getNow());
                countDownTimerTwo.start();
//            Log.v(TAG, "Timer Two Start");
                setTimerTwoState(RUNNING);

                timerPreferences.setPreviousHoursTwo(timerHour);
                timerPreferences.setPreviousMinutesTwo(timerMinute);

                resetTimerInputValues();

                setInputText();
            } else if ((activeTimer == TIMER_TWO) && (timerTwoState == PAUSED)) {
                timerResume();
            }

            if ((activeTimer == TIMER_THREE) && (timerThreeState == INPUT) && ((timerHour > 0) || (timerMinute > 0))) {
                setClockViewThreeVis();

                millisToCountThree = convertMillis();
                countDownTimerThree = new CountTimer(millisToCountThree, 3);
                hms = timerFormat(millisToCountThree);
                clockView3.setText(hms);
                timerPreferences.setStartTimeThree(getNow());
                countDownTimerThree.start();
//            Log.v(TAG, "Timer Three Start");
                setTimerThreeState(RUNNING);

                timerPreferences.setPreviousHoursThree(timerHour);
                timerPreferences.setPreviousMinutesThree(timerMinute);

                resetTimerInputValues();

                setInputText();
            } else if ((activeTimer == TIMER_THREE) && (timerThreeState == PAUSED)) {
                timerResume();
            }

            if ((activeTimer == TIMER_FOUR) && (timerFourState == INPUT) && ((timerHour > 0) || (timerMinute > 0))) {
                setClockViewFourVis();

                millisToCountFour = convertMillis();
                countDownTimerFour = new CountTimer(millisToCountFour, 4);
                hms = timerFormat(millisToCountFour);
                clockView4.setText(hms);
                timerPreferences.setStartTimeFour(getNow());
                countDownTimerFour.start();
//            Log.v(TAG, "Timer Four Start");
                setTimerFourState(RUNNING);

                timerPreferences.setPreviousHoursFour(timerHour);
                timerPreferences.setPreviousMinutesFour(timerMinute);

                resetTimerInputValues();

                setInputText();
            } else if ((activeTimer == TIMER_FOUR) && (timerFourState == PAUSED)) {
                timerResume();
            }
        }
    }

    public void stopButton(View view) {

        if (view.getId() == R.id.stop_button) {
            if ((timerOneState | timerTwoState | timerThreeState | timerFourState ) == RUNNING) {
                timerPause();
            } else if (timerAlarm) {
                stopAlarm();
                timerAlarm = false;
            }
        }
    }

    public void resetButton(View view) {

        if (view.getId() == R.id.reset_button) {
            if ((activeTimer == TIMER_ONE) && (timerOneState != RUNNING)) {
                resetTimerOne();
            }

            if ((activeTimer == TIMER_TWO) && (timerTwoState != RUNNING)) {
                resetTimerTwo();
            }

        if ((activeTimer == TIMER_THREE) && (timerThreeState != RUNNING)) {
                resetTimerThree();
            }

            if ((activeTimer == TIMER_FOUR) && (timerFourState != RUNNING)) {
                resetTimerFour();
            }
        }
    }

    private void timerPause() {

        if ((timerOneState == RUNNING) && (activeTimer == TIMER_ONE)) {
            countDownTimer.cancel();
            setTimerOneState(PAUSED);
        }

        if ((timerTwoState == RUNNING) && (activeTimer == TIMER_TWO)) {
            countDownTimerTwo.cancel();
            setTimerTwoState(PAUSED);
        }

        if ((timerThreeState == RUNNING) && (activeTimer == TIMER_THREE)) {
            countDownTimerThree.cancel();
            setTimerThreeState(PAUSED);
        }

        if ((timerFourState == RUNNING) && (activeTimer == TIMER_FOUR)) {
            countDownTimerFour.cancel();
            setTimerFourState(PAUSED);
        }
    }

    private void timerResume() {
//        Log.v(TAG, "pausedTime = " + pausedTime);

        if ((timerOneState == PAUSED) && (activeTimer == TIMER_ONE)) {
            countDownTimer = new CountTimer(pausedTime, 1);
            countDownTimer.start();
            setTimerOneState(RUNNING);
            timerPreferences.setTimerOneState(timerOneState);
//            Log.v(TAG, "timerResume");
        }

        if ((timerTwoState == PAUSED) && (activeTimer == TIMER_TWO)) {
            countDownTimerTwo = new CountTimer(pausedTimeTwo, 2);
            countDownTimerTwo.start();
            setTimerTwoState(RUNNING);
            timerPreferences.setTimerTwoState(timerTwoState);
//            Log.v(TAG, "timerTwoResume");
        }

        if ((timerThreeState == PAUSED) && (activeTimer == TIMER_THREE)) {
            countDownTimerThree = new CountTimer(pausedTimeThree, 3);
            countDownTimerThree.start();
            setTimerThreeState(RUNNING);
            timerPreferences.setTimerThreeState(timerThreeState);
//            Log.v(TAG, "timerThreeResume");
        }

        if ((timerFourState == PAUSED) && (activeTimer == TIMER_FOUR)) {
            countDownTimerFour = new CountTimer(pausedTimeFour, 4);
            countDownTimerFour.start();
            setTimerFourState(RUNNING);
            timerPreferences.setTimerFourState(timerFourState);
//            Log.v(TAG, "timerFourResume");
        }
    }

    private static long getNow() {

        return System.currentTimeMillis();
    }

    private static String timerFormat(long l) {

        return String.format(Locale.ENGLISH, "%01d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(l),
                TimeUnit.MILLISECONDS.toMinutes(l) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(l)),
                TimeUnit.MILLISECONDS.toSeconds(l) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(l)));
    }

    private long convertMillis() {

        return (long) ((timerHour * 3600000) + (timerMinute * 60000));
    }

    private void initTimer() {
//        Log.v(TAG, "initTimer()");

        long startTime = timerPreferences.getStartTime();
        long startTimeTwo = timerPreferences.getStartTimeTwo();
        long startTimeThree = timerPreferences.getStartTimeThree();
        long startTimeFour = timerPreferences.getStartTimeFour();

        if ((startTime > 0) && (timerOneState == RUNNING)) {
//            Log.v(TAG, "MillisToCalc " + millisToCount);
//            Log.v(TAG, "startTime = " + startTime);
            pausedTime = (millisToCount - (getNow() - startTime));
//            Log.v(TAG, "MillisToCount " + millisToCount);
//            Log.v(TAG, "pausedTime " + pausedTime);
            if (pausedTime <= 0) {
                setTimerOneState(INPUT);
            } else if (timerOneState == RUNNING) {
                setClockViewOneVis();

                countDownTimer = new CountTimer(pausedTime, 1);
                countDownTimer.start();
            }
        } else if ((startTime > 0) && (timerOneState == PAUSED)) {
            setTimerOneState(PAUSED);
        } else {
            setTimerOneState(INPUT);
        }

        if ((startTimeTwo > 0) && (timerTwoState == RUNNING)) {
//            Log.v(TAG, "MillisToCalcTwo " + millisToCountTwo);
            pausedTimeTwo = (millisToCountTwo - (getNow() - startTimeTwo));
//            Log.v(TAG, "MillisToCountTwo " + millisToCountTwo);
//            Log.v(TAG, "pausedTimeTwo " + pausedTimeTwo);
            if (pausedTimeTwo <= 0) {
                setTimerTwoState(INPUT);
            } else if (timerTwoState == RUNNING) {
                setClockViewTwoVis();

                countDownTimerTwo = new CountTimer(pausedTimeTwo, 2);
                countDownTimerTwo.start();
            }
        } else if ((startTimeTwo > 0) && (timerTwoState == PAUSED)) {
            setTimerTwoState(PAUSED);
        } else {
            setTimerTwoState(INPUT);
        }

        if ((startTimeThree > 0) && (timerThreeState == RUNNING)) {
//            Log.v(TAG, "MillisToCalcThree " + millisToCountThree);
            pausedTimeThree = (millisToCountThree - (getNow() - startTimeThree));
//            Log.v(TAG, "MillisToCountThree " + millisToCountThree);
//            Log.v(TAG, "pausedTimeThree " + pausedTimeThree);
            if (pausedTimeThree <= 0) {
                setTimerThreeState(INPUT);
            } else if (timerThreeState == RUNNING) {
                setClockViewThreeVis();

                countDownTimerThree = new CountTimer(pausedTimeThree, 3);
                countDownTimerThree.start();
            }
        } else if ((startTimeThree > 0) && (timerThreeState == PAUSED)) {
            setTimerThreeState(PAUSED);
        } else {
            setTimerThreeState(INPUT);
        }

        if ((startTimeFour > 0) && (timerFourState == RUNNING)) {
//            Log.v(TAG, "MillisToCalcFour " + millisToCountFour);
            pausedTimeFour = (millisToCountFour - (getNow() - startTimeFour));
//            Log.v(TAG, "MillisToCountFour " + millisToCountFour);
//            Log.v(TAG, "pausedTimeFour " + pausedTimeFour);
            if (pausedTimeFour <= 0) {
                setTimerFourState(INPUT);
            } else if (timerFourState == RUNNING) {
                setClockViewFourVis();

                countDownTimerFour = new CountTimer(pausedTimeFour, 4);
                countDownTimerFour.start();
            }
        } else if ((startTimeFour > 0) && (timerFourState == PAUSED)) {
            setTimerFourState(PAUSED);
        } else {
            setTimerFourState(INPUT);
        }
    }

    private void playAlarm() {

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        boolean vibrateSetting = timerPreferences.getVibrateSetting();
        long pattern[] = {200, 500, 500};
        int volume = timerPreferences.getAlarmVolume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        afChangeListener = new MainActOnAudioFocusChangeListener(volume);

        int result = audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);

            alarmPlayer = MediaPlayer.create(this, R.raw.alarm_clock_short);
            alarmPlayer.setLooping(true);
            alarmPlayer.start();

            if (vibrator.hasVibrator() && vibrateSetting) {
                vibrator.vibrate(pattern, 0);
            }
        }

        timerAlarm = true;
    }

    private void playWarningAlarm() {
//        Log.v(TAG, "Play warning alarm");

        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        boolean vibrateSetting = timerPreferences.getVibrateSetting();
        int volume = timerPreferences.getAlarmVolume();
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        originalVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        afChangeListener = new MainActOnAudioFocusChangeListener(volume);

        int result = audioManager.requestAudioFocus(afChangeListener, AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, FLAG_REMOVE_SOUND_AND_VIBRATE);

            alarmPlayer = MediaPlayer.create(this, R.raw.alarm_clock_short);
            alarmPlayer.setOnCompletionListener(new WarningAlarmCompletionListener());
            alarmPlayer.start();

            if (vibrator.hasVibrator() && vibrateSetting) {
                vibrator.vibrate(800);
            }
        }
    }

    private void stopAlarm() {

        if (vibrator != null) {
            vibrator.cancel();
        }

        if (alarmPlayer != null) {
            alarmPlayer.stop();
            alarmPlayer.reset();
            alarmPlayer.release();
            alarmPlayer = null;
        }

        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        if (audioManager != null) {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalVolume, 0);
            audioManager.abandonAudioFocus(afChangeListener);
            audioManager = null;
        }
    }

    /**
     * Sets alarm manager when onPause is called
     */
    private void setAlarmManager() {

        wakeUpTime = timerPreferences.getStartTime() + millisToCount;
        wakeUpTimeTwo = timerPreferences.getStartTimeTwo() + millisToCountTwo;
        wakeUpTimeThree = timerPreferences.getStartTimeThree() + millisToCountThree;
        wakeUpTimeFour = timerPreferences.getStartTimeFour() + millisToCountFour;

//        Log.v(TAG, "SAM millisToCount = " + millisToCount);
//        Log.v(TAG, "SAM pausedTime = " + pausedTime);

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.setAction(CREATE_NOTIFICATION_ACTION);
        Intent warningIntent = new Intent(this, WarningAlarmReceiver.class);

        if (millisToCount > 0) {
            alarmIntent.putExtra("timerNumberId", 1);
            PendingIntent sender = PendingIntent.getBroadcast(this, 1, alarmIntent, 0);
            if (Build.VERSION.SDK_INT < 23) {
                if (am != null) {
                    am.setExact(AlarmManager.RTC_WAKEUP, wakeUpTime, sender);
//                    Log.v(TAG, "Alarm Manager Set");
                }

                if (timerWarning && (pausedTime > warningAlarmStart)) {
                    long warningTime = wakeUpTime - warningAlarmStart;
                    warningIntent.putExtra("playWarningAlarm", true);
                    PendingIntent warningSender = PendingIntent.getBroadcast(this, 1, warningIntent, 0);
                    if (am != null) {
                        am.setExact(AlarmManager.RTC_WAKEUP, warningTime, warningSender);
                    }
                }
            } else {
                if (am != null) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeUpTime, sender);
//            Log.v(TAG, "Alarm Manager Set");
                }

                if (timerWarning && (pausedTime > warningAlarmStart)) {
                    long warningTime = wakeUpTime - warningAlarmStart;
                    warningIntent.putExtra("playWarningAlarm", true);
                    PendingIntent warningSender = PendingIntent.getBroadcast(this, 1, warningIntent, 0);
                    if (am != null) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, warningTime, warningSender);
                    }
                }
            }

        }

        if (millisToCountTwo > 0) {
            alarmIntent.putExtra("timerNumberId", 2);
            PendingIntent senderTwo = PendingIntent.getBroadcast(this, 2, alarmIntent, 0);
            if (Build.VERSION.SDK_INT < 23) {
                if (am != null) {
                    am.setExact(AlarmManager.RTC_WAKEUP, wakeUpTimeTwo, senderTwo);
//            Log.v(TAG, "Alarm Manager Two Set");
                }

                if (timerWarning && (pausedTimeTwo > warningAlarmStart)) {
                    long warningTimeTwo = wakeUpTimeTwo - warningAlarmStart;
                    warningIntent.putExtra("playWarningAlarm", true);
                    PendingIntent warningSenderTwo = PendingIntent.getBroadcast(this, 2, warningIntent, 0);
                    if (am != null) {
                        am.setExact(AlarmManager.RTC_WAKEUP, warningTimeTwo, warningSenderTwo);
                    }
                }
            } else {
                if (am != null) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeUpTimeTwo, senderTwo);
//            Log.v(TAG, "Alarm Manager Two Set");
                }

                if (timerWarning && (pausedTimeTwo > warningAlarmStart)) {
                    long warningTimeTwo = wakeUpTimeTwo - warningAlarmStart;
                    warningIntent.putExtra("playWarningAlarm", true);
                    PendingIntent warningSenderTwo = PendingIntent.getBroadcast(this, 2, warningIntent, 0);
                    if (am != null) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, warningTimeTwo, warningSenderTwo);
                    }
                }
            }

        }

        if (millisToCountThree > 0) {
            alarmIntent.putExtra("timerNumberId", 3);
            PendingIntent senderThree = PendingIntent.getBroadcast(this, 3, alarmIntent, 0);
            if (Build.VERSION.SDK_INT < 23) {
                if (am != null) {
                    am.setExact(AlarmManager.RTC_WAKEUP, wakeUpTimeThree, senderThree);
//            Log.v(TAG, "Alarm Manager Three Set");
                }

                if (timerWarning && (pausedTimeThree > warningAlarmStart)) {
                    long warningTimeThree = wakeUpTimeThree - warningAlarmStart;
                    warningIntent.putExtra("playWarningAlarm", true);
                    PendingIntent warningSenderThree = PendingIntent.getBroadcast(this, 3, warningIntent, 0);
                    if (am != null) {
                        am.setExact(AlarmManager.RTC_WAKEUP, warningTimeThree, warningSenderThree);
                    }
                }
            } else {
                if (am != null) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeUpTimeThree, senderThree);
//            Log.v(TAG, "Alarm Manager Three Set");
                }

                if (timerWarning && (pausedTimeThree > warningAlarmStart)) {
                    long warningTimeThree = wakeUpTimeThree - warningAlarmStart;
                    warningIntent.putExtra("playWarningAlarm", true);
                    PendingIntent warningSenderThree = PendingIntent.getBroadcast(this, 3, warningIntent, 0);
                    if (am != null) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, warningTimeThree, warningSenderThree);
                    }
                }
            }

        }

        if (millisToCountFour > 0) {
            alarmIntent.putExtra("timerNumberId", 4);
            PendingIntent senderFour = PendingIntent.getBroadcast(this, 4, alarmIntent, 0);
            if (Build.VERSION.SDK_INT < 23) {
                if (am != null) {
                    am.setExact(AlarmManager.RTC_WAKEUP, wakeUpTimeFour, senderFour);
//            Log.v(TAG, "Alarm Manager Four Set");
                }

                if (timerWarning && (pausedTimeFour > warningAlarmStart)) {
                    long warningTimeFour = wakeUpTimeFour - warningAlarmStart;
                    warningIntent.putExtra("playWarningAlarm", true);
                    PendingIntent warningSenderFour = PendingIntent.getBroadcast(this, 4, warningIntent, 0);
                    if (am != null) {
                        am.setExact(AlarmManager.RTC_WAKEUP, warningTimeFour, warningSenderFour);
                    }
                }
            } else {
                if (am != null) {
                    am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, wakeUpTimeFour, senderFour);
//            Log.v(TAG, "Alarm Manager Four Set");
                }

                if (timerWarning && (pausedTimeFour > warningAlarmStart)) {
                    long warningTimeFour = wakeUpTimeFour - warningAlarmStart;
                    warningIntent.putExtra("playWarningAlarm", true);
                    PendingIntent warningSenderFour = PendingIntent.getBroadcast(this, 4, warningIntent, 0);
                    if (am != null) {
                        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, warningTimeFour, warningSenderFour);
                    }
                }
            }

        }
    }

    private void removeAlarmManager() {

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent alarmIntent = new Intent(this, AlarmReceiver.class);
        alarmIntent.setAction(CREATE_NOTIFICATION_ACTION);
        Intent warningIntent = new Intent(this, WarningAlarmReceiver.class);

        alarmIntent.getIntExtra("timerNumberId", 1);
        PendingIntent sender = PendingIntent.getBroadcast(this, 1, alarmIntent, 0);

        warningIntent.getBooleanExtra("playWarningAlarm", true);
        PendingIntent warningSender = PendingIntent.getBroadcast(this, 1, warningIntent, 0);

        alarmIntent.getIntExtra("timerNumberId", 2);
        PendingIntent senderTwo = PendingIntent.getBroadcast(this, 2, alarmIntent, 0);

        warningIntent.getBooleanExtra("playWarningAlarm", true);
        PendingIntent warningSenderTwo = PendingIntent.getBroadcast(this, 2, warningIntent, 0);

        alarmIntent.getIntExtra("timerNumberId", 3);
        PendingIntent senderThree = PendingIntent.getBroadcast(this, 3, alarmIntent, 0);

        warningIntent.getBooleanExtra("playWarningAlarm", true);
        PendingIntent warningSenderThree = PendingIntent.getBroadcast(this, 3, warningIntent, 0);

        alarmIntent.getIntExtra("timerNumberId", 4);
        PendingIntent senderFour = PendingIntent.getBroadcast(this, 4, alarmIntent, 0);

        warningIntent.getBooleanExtra("playWarningAlarm", true);
        PendingIntent warningSenderFour = PendingIntent.getBroadcast(this, 4, warningIntent, 0);

        if (am != null) {
            am.cancel(sender);
            am.cancel(senderTwo);
            am.cancel(senderThree);
            am.cancel(senderFour);

            am.cancel(warningSender);
            am.cancel(warningSenderTwo);
            am.cancel(warningSenderThree);
            am.cancel(warningSenderFour);
        }

        sender.cancel();
        senderTwo.cancel();
        senderThree.cancel();
        senderFour.cancel();

        warningSender.cancel();
        warningSenderTwo.cancel();
        warningSenderThree.cancel();
        warningSenderFour.cancel();

//        Log.v(TAG, "Alarms Removed");
    }

    private class RptMinuteUpdater implements Runnable {

        private static final int REP_DELAY = 125;

        public void run() {

            if (mAutoIncrement) {
                minutePlus(minute_plus_button);
                repeatMinuteHandler.postDelayed(new RptMinuteUpdater(), REP_DELAY);
            } else if (mAutoDecrement) {
                minuteMinus(minute_minus_button);
                repeatMinuteHandler.postDelayed(new RptMinuteUpdater(), REP_DELAY);
            }
        }
    }

    /**
     * This runnable is used to prevent anonymous runnable
     */
    private class ResetAllRunnable implements Runnable {

        @Override
        public void run() {

            if (isLongPress) {
                DialogFragment resetAllTimersFragment = new ResetAllTimersFragment();
                resetAllTimersFragment.show(getFragmentManager(), "resetAllTimers");
            }
        }
    }

    /**
     * Inner class that stops all alarms and resets them to default values
     */
    public static class ResetAllTimersFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
            alertDialogBuilder.setMessage(R.string.reset_all_dialog_message)
                    .setPositiveButton(R.string.reset_all_positive_button, new ResetTimerDialogPositiveBtnListener())
                    .setNegativeButton(R.string.reset_all_negative_button, new ResetTimerDialogNegativeBtnListener());

            return alertDialogBuilder.create();
        }

        private static class ResetTimerDialogNegativeBtnListener implements DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialog, int which) {
//               Log.v(TAG, "Timers were not reset");
            }
        }

        private class ResetTimerDialogPositiveBtnListener implements DialogInterface.OnClickListener {

            @Override
            public void onClick(DialogInterface dialog, int which) {
//                            Log.v(TAG, "All Timers Reset");

                if (((MainActivity) getActivity()).countDownTimer != null) {
                    ((MainActivity) getActivity()).countDownTimer.cancel();
                    ((MainActivity) getActivity()).resetTimerOne();
                }

                if (((MainActivity) getActivity()).countDownTimerTwo != null) {
                    ((MainActivity) getActivity()).countDownTimerTwo.cancel();
                    ((MainActivity) getActivity()).resetTimerTwo();
                }

                if (((MainActivity) getActivity()).countDownTimerThree != null) {
                    ((MainActivity) getActivity()).countDownTimerThree.cancel();
                    ((MainActivity) getActivity()).resetTimerThree();
                }

                if (((MainActivity) getActivity()).countDownTimerFour != null) {
                    ((MainActivity) getActivity()).countDownTimerFour.cancel();
                    ((MainActivity) getActivity()).resetTimerFour();
                }

                Toast toast = Toast.makeText(getActivity(), "All timers have been reset", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }

    /**
     * Class to handle counting down time.
     */
    private class CountTimer extends CountDownTimer {

        private static final int countdownInterval = 125;
        final int timerNumber;

        CountTimer(long millisInFuture, int timerNumberInput) {

            super(millisInFuture, countdownInterval);

            timerNumber = timerNumberInput;
        }

        @Override
        public void onTick(long countTimerPausedTime) {

            switch (timerNumber) {
                case 1:
                    pausedTime = countTimerPausedTime;
                    hms = timerFormat(countTimerPausedTime);
                    clockView1.setText(hms);
                    break;

                case 2:
                    pausedTimeTwo = countTimerPausedTime;
                    hms = timerFormat(countTimerPausedTime);
                    clockView2.setText(hms);
                    break;

                case 3:
                    pausedTimeThree = countTimerPausedTime;
                    hms = timerFormat(countTimerPausedTime);
                    clockView3.setText(hms);
                    break;

                case 4:
                    pausedTimeFour = countTimerPausedTime;
                    hms = timerFormat(countTimerPausedTime);
                    clockView4.setText(hms);
                    break;

                default:
                    break;
            }

            if (timerWarning) {
                if (pausedTime > warningAlarmEnd) {
                    if (pausedTime < warningAlarmStart) {
                        playWarningAlarm();
                        timerWarning = false;
                    }
                }

                if (pausedTimeTwo > warningAlarmEnd) {
                    if (pausedTimeTwo < warningAlarmStart) {
                        playWarningAlarm();
                        timerWarning = false;
                    }
                }

                if (pausedTimeThree > warningAlarmEnd) {
                    if (pausedTimeThree < warningAlarmStart) {
                        playWarningAlarm();
                        timerWarning = false;
                    }
                }

                if (pausedTimeFour > warningAlarmEnd) {
                    if (pausedTimeFour < warningAlarmStart) {
                        playWarningAlarm();
                        timerWarning = false;
                    }
                }
            }
        }

        @Override
        public void onFinish() {

            if (!timerAlarm) {
                playAlarm();
            }

            switch (timerNumber) {
                case 1:
                    countDownTimer.cancel();
                    countDownTimer = null;
                    setTimerOneState(INPUT);
                    millisToCount = 0;

                    timerPreferences.setTimerOneState(timerOneState);
                    timerPreferences.setStartTime(0);
                    timerPreferences.setOriginalTime(0);
                    timerPreferences.setPausedTime(0);
                    break;

                case 2:
                    countDownTimerTwo.cancel();
                    countDownTimerTwo = null;
                    setTimerTwoState(INPUT);
                    millisToCountTwo = 0;

                    timerPreferences.setTimerTwoState(timerTwoState);
                    timerPreferences.setStartTimeTwo(0);
                    timerPreferences.setOriginalTimeTwo(0);
                    timerPreferences.setPausedTimeTwo(0);
                    break;

                case 3:
                    countDownTimerThree.cancel();
                    countDownTimerThree = null;
                    setTimerThreeState(INPUT);
                    millisToCountThree = 0;

                    timerPreferences.setTimerThreeState(timerThreeState);
                    timerPreferences.setStartTimeThree(0);
                    timerPreferences.setOriginalTimeThree(0);
                    timerPreferences.setPausedTimeThree(0);
                    break;

                case 4:
                    countDownTimerFour.cancel();
                    countDownTimerFour = null;
                    setTimerFourState(INPUT);
                    millisToCountFour = 0;

                    timerPreferences.setTimerFourState(timerFourState);
                    timerPreferences.setStartTimeFour(0);
                    timerPreferences.setOriginalTimeFour(0);
                    timerPreferences.setPausedTimeFour(0);
                    break;

                default:
                    break;
            }

            resetTimerInputValues();

            setInputVis();

            setInputText();
        }
    }

    /**
     * Inner class to handle change of audio focus when alarm is playing
     */
    private class MainActOnAudioFocusChangeListener implements AudioManager.OnAudioFocusChangeListener {

        private final int volume;

        MainActOnAudioFocusChangeListener(int vol) {

            volume = vol;
        }

        @Override
        public void onAudioFocusChange(int focusChange) {

            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_LOSS:
                    stopAlarm();
                    timerAlarm = false;
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    alarmPlayer.pause();
                    break;

                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 3, 0);
                    break;

                case AudioManager.AUDIOFOCUS_GAIN:
                    if (audioManager != null) {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                    }
                    break;

                default:
                    break;
            }
        }
    }

    /**
     * Inner class to listen for alarm to complete and then stop alarm and return volume to previous level
     */
    private class WarningAlarmCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
//            Log.v(TAG, "Warning alarm complete");
            stopAlarm();
            timerWarning = true;
        }
    }

    public void setActiveTimer(@TimerActive int activeTimer) {
        this.activeTimer = activeTimer;
    }

    public void setTimerOneState(@TimerOneState int timerOneState) { this.timerOneState = timerOneState; }

    public void setTimerTwoState(@TimerTwoState int timerTwoState) { this.timerTwoState = timerTwoState; }

    public void setTimerThreeState(@TimerThreeState int timerThreeState) { this.timerThreeState = timerThreeState; }

    public void setTimerFourState(@TimerFourState int timerFourState) { this.timerFourState = timerFourState; }

    private void setInputVis() {

        inputSeconds.setVisibility(View.VISIBLE);
        inputMinutes.setVisibility(View.VISIBLE);
        inputHours.setVisibility(View.VISIBLE);
        clockView1.setVisibility(View.GONE);
        clockView2.setVisibility(View.GONE);
        clockView3.setVisibility(View.GONE);
        clockView4.setVisibility(View.GONE);
    }

    private void setInputText() {

        inputHours.setText(String.format(Locale.ENGLISH, "%01d", timerHour));
        inputMinutes.setText(String.format(Locale.ENGLISH, ":%02d", timerMinute));
        inputSeconds.setText(String.format(Locale.ENGLISH, ":%02d", timerSecond));
    }

    private void resetTimerInputValues() {

        timerHour = 0;
        timerMinute = 0;
        timerSecond = 0;
    }

    private void setClockViewOneVis() {

        inputSeconds.setVisibility(View.INVISIBLE);
        inputMinutes.setVisibility(View.INVISIBLE);
        inputHours.setVisibility(View.INVISIBLE);
        clockView1.setVisibility(View.VISIBLE);
        clockView2.setVisibility(View.GONE);
        clockView3.setVisibility(View.GONE);
        clockView4.setVisibility(View.GONE);
    }

    private void setClockViewTwoVis() {

        inputSeconds.setVisibility(View.INVISIBLE);
        inputMinutes.setVisibility(View.INVISIBLE);
        inputHours.setVisibility(View.INVISIBLE);
        clockView1.setVisibility(View.GONE);
        clockView2.setVisibility(View.VISIBLE);
        clockView3.setVisibility(View.GONE);
        clockView4.setVisibility(View.GONE);
    }

    private void setClockViewThreeVis() {

        inputSeconds.setVisibility(View.INVISIBLE);
        inputMinutes.setVisibility(View.INVISIBLE);
        inputHours.setVisibility(View.INVISIBLE);
        clockView1.setVisibility(View.GONE);
        clockView2.setVisibility(View.GONE);
        clockView3.setVisibility(View.VISIBLE);
        clockView4.setVisibility(View.GONE);
    }

    private void setClockViewFourVis() {

        inputSeconds.setVisibility(View.INVISIBLE);
        inputMinutes.setVisibility(View.INVISIBLE);
        inputHours.setVisibility(View.INVISIBLE);
        clockView1.setVisibility(View.GONE);
        clockView2.setVisibility(View.GONE);
        clockView3.setVisibility(View.GONE);
        clockView4.setVisibility(View.VISIBLE);
    }

    private void resetTimerOne() {
//        Log.v(TAG, "Reset Timer One");
        countDownTimer = null;
        setTimerOneState(INPUT);

        resetTimerInputValues();

        millisToCount = 0;
        pausedTime = 0;

        timerPreferences.setStartTime(0);
        timerPreferences.setOriginalTime(0);
        timerPreferences.setTimerOneState(timerOneState);
        timerPreferences.setPausedTime(0);

        setInputVis();

        setInputText();
    }

    private void resetTimerTwo() {
//        Log.v(TAG, "Reset Timer Two");
        countDownTimerTwo = null;
        setTimerTwoState(INPUT);

        resetTimerInputValues();

        millisToCountTwo = 0;
        pausedTimeTwo = 0;

        timerPreferences.setStartTimeTwo(0);
        timerPreferences.setOriginalTimeTwo(0);
        timerPreferences.setTimerTwoState(timerTwoState);
        timerPreferences.setPausedTimeTwo(0);

        setInputVis();

        setInputText();
    }

    private void resetTimerThree() {
//        Log.v(TAG, "Reset Timer Four");
        countDownTimerThree = null;
        setTimerThreeState(INPUT);

        resetTimerInputValues();

        millisToCountThree = 0;
        pausedTimeThree = 0;

        timerPreferences.setStartTimeThree(0);
        timerPreferences.setOriginalTimeThree(0);
        timerPreferences.setTimerThreeState(timerThreeState);
        timerPreferences.setPausedTimeThree(0);

        setInputVis();

        setInputText();
    }

    private void resetTimerFour() {
//        Log.v(TAG, "Reset Timer Four");
        countDownTimerFour = null;
        setTimerFourState(INPUT);

        resetTimerInputValues();

        millisToCountFour = 0;
        pausedTimeFour = 0;

        timerPreferences.setStartTimeFour(0);
        timerPreferences.setOriginalTimeFour(0);
        timerPreferences.setTimerFourState(timerFourState);
        timerPreferences.setPausedTimeFour(0);

        setInputVis();

        setInputText();
    }

    public void openSettings(View view) {

        if (view.getId() == R.id.settings_button) {
            Intent intent = new Intent(this, AppPreferenceActivity.class);
            startActivity(intent);
        }
    }

    private static int getScreenWidthDp(Context context) {

        DisplayMetrics displayMetrics = new DisplayMetrics();

        WindowManager windowManager = (WindowManager) context.getSystemService(WINDOW_SERVICE);

        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        }

        return Math.round(displayMetrics.widthPixels / displayMetrics.density);
    }
}