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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.media.AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE;

import android.util.Log;

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

    private final TimerState timerState = new TimerState();

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

    private static final String TAG = "NEWMAN";
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

        timerState.setActiveTimer(timerPreferences.getActiveTimer());

        Log.v(TAG, "Timer prefs active timer = " + timerPreferences.getActiveTimer());

        timerState.setTimerOneState(timerPreferences.getTimerOneState());
        timerState.setTimerTwoState(timerPreferences.getTimerTwoState());
        timerState.setTimerThreeState(timerPreferences.getTimerThreeState());
        timerState.setTimerFourState(timerPreferences.getTimerFourState());

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

        if (timerState.getActiveTimer() == TimerState.NO_ACTIVE_TIMER) {
            timerState.setActiveTimer(TimerState.TIMER_ONE);

            setInputVis(R.string.timer_one_indicator);

            timerIndicator.setText(R.string.timer_one_indicator);
        }

    if ((timerState.getTimerOneState() |
            timerState.getTimerTwoState() |
            timerState.getTimerThreeState() |
            timerState.getTimerFourState()) == TimerState.RUNNING) {
            removeAlarmManager();
            initTimer();
        }

        if ((timerState.getTimerOneState() == TimerState.PAUSED) && (pausedTime > 0)) {
            hms = timerFormat(pausedTime);
            clockView1.setText(hms);
        }

        if ((timerState.getTimerTwoState() == TimerState.PAUSED) && (pausedTimeTwo > 0)) {
            hms = timerFormat(pausedTimeTwo);
            clockView2.setText(hms);
        }

        if ((timerState.getTimerThreeState() == TimerState.PAUSED) && (pausedTimeThree > 0)) {
            hms = timerFormat(pausedTimeThree);
            clockView3.setText(hms);
        }

        if ((timerState.getTimerFourState() == TimerState.PAUSED) && (pausedTimeFour > 0)) {
            hms = timerFormat(pausedTimeFour);
            clockView4.setText(hms);
        }

        if (timerState.getActiveTimer() == TimerState.TIMER_ONE) {
            if ((timerState.getTimerOneState() == TimerState.RUNNING) | (timerState.getTimerOneState() == TimerState.PAUSED)) {
                Log.v(TAG, "setClockViewOneVis");
                setClockViewOneVis();
            } else if (timerState.getTimerOneState() == TimerState.INPUT) {
                setInputVis(R.string.timer_one_indicator);
            }
        }

        if (timerState.getActiveTimer() == TimerState.TIMER_TWO) {
            if ((timerState.getTimerTwoState() == TimerState.RUNNING) | (timerState.getTimerTwoState() == TimerState.PAUSED)) {
                setClockViewTwoVis();
            } else if (timerState.getTimerTwoState() == TimerState.INPUT) {
                setInputVis(R.string.timer_two_indicator);
            }
        }

        if (timerState.getActiveTimer() == TimerState.TIMER_THREE) {
            if ((timerState.getTimerThreeState() == TimerState.RUNNING) | (timerState.getTimerThreeState() == TimerState.PAUSED)) {
                setClockViewThreeVis();
            } else if (timerState.getTimerThreeState() == TimerState.INPUT) {
                setInputVis(R.string.timer_three_indicator);
            }
        }

        if (timerState.getActiveTimer() == TimerState.TIMER_FOUR) {
            if ((timerState.getTimerFourState() == TimerState.RUNNING) | (timerState.getTimerFourState() == TimerState.PAUSED)) {
                setClockViewFourVis();
            } else if (timerState.getTimerFourState() == TimerState.INPUT) {
                setInputVis(R.string.timer_four_indicator);
            }
        }

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

        timerPreferences.setActiveTimer(timerState.getActiveTimer());

//        Log.v(TAG, "onPause pausedTime = " + pausedTime);

        if (timerState.getTimerOneState() == TimerState.RUNNING) {
            timerPreferences.setOriginalTime(millisToCount);
            timerPreferences.setPausedTime(pausedTime);
            timerPreferences.setTimerOneState(timerState.getTimerOneState());
//            Log.v(TAG, "timerPreferences timerOneRunning = true");
            countDownTimer.cancel();
            countDownTimer = null;
        } else if (timerState.getTimerOneState() == TimerState.INPUT) {
            millisToCount = 0;
            timerPreferences.setOriginalTime(0);
            timerPreferences.setTimerOneState(timerState.getTimerOneState());
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        } else {
            timerPreferences.setTimerOneState(timerState.getTimerOneState());
            timerPreferences.setPausedTime(pausedTime);
            timerPreferences.setOriginalTime(millisToCount);
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        }

        if (timerState.getTimerTwoState() == TimerState.RUNNING) {
            timerPreferences.setOriginalTimeTwo(millisToCountTwo);
            timerPreferences.setPausedTimeTwo(pausedTimeTwo);
            timerPreferences.setTimerTwoState(timerState.getTimerTwoState());
            countDownTimerTwo.cancel();
            countDownTimerTwo = null;
        } else if (timerState.getTimerTwoState() == TimerState.INPUT) {
            millisToCountTwo = 0;
            timerPreferences.setOriginalTimeTwo(0);
            timerPreferences.setTimerTwoState(timerState.getTimerTwoState());
            if (countDownTimerTwo != null) {
                countDownTimerTwo.cancel();
                countDownTimerTwo = null;
            }
        } else {
            timerPreferences.setTimerTwoState(timerState.getTimerTwoState());
            timerPreferences.setPausedTimeTwo(pausedTimeTwo);
            timerPreferences.setOriginalTimeTwo(millisToCountTwo);
            if (countDownTimerTwo != null) {
                countDownTimerTwo.cancel();
                countDownTimerTwo = null;
            }
        }

        if (timerState.getTimerThreeState() == TimerState.RUNNING) {
            timerPreferences.setOriginalTimeThree(millisToCountThree);
            timerPreferences.setPausedTimeThree(pausedTimeThree);
            timerPreferences.setTimerThreeState(timerState.getTimerThreeState());
            countDownTimerThree.cancel();
            countDownTimerThree = null;
        } else if (timerState.getTimerThreeState() == TimerState.INPUT) {
            millisToCountThree = 0;
            timerPreferences.setOriginalTimeThree(0);
            timerPreferences.setTimerThreeState(timerState.getTimerThreeState());
            if (countDownTimerThree != null) {
                countDownTimerThree.cancel();
                countDownTimerThree = null;
            }
        } else {
            timerPreferences.setTimerThreeState(timerState.getTimerThreeState());
            timerPreferences.setPausedTimeThree(pausedTimeThree);
            timerPreferences.setOriginalTimeThree(millisToCountThree);
            if (countDownTimerThree != null) {
                countDownTimerThree.cancel();
                countDownTimerThree = null;
            }
        }

        if (timerState.getTimerFourState() == TimerState.RUNNING) {
            timerPreferences.setOriginalTimeFour(millisToCountFour);
            timerPreferences.setPausedTimeFour(pausedTimeFour);
            timerPreferences.setTimerFourState(timerState.getTimerFourState());
            countDownTimerFour.cancel();
            countDownTimerFour = null;
        } else if (timerState.getTimerFourState() == TimerState.INPUT) {
            millisToCountFour = 0;
            timerPreferences.setOriginalTimeFour(0);
            timerPreferences.setTimerFourState(timerState.getTimerFourState());
            if (countDownTimerFour != null) {
                countDownTimerFour.cancel();
                countDownTimerFour = null;
            }
        } else {
            timerPreferences.setTimerFourState(timerState.getTimerFourState());
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


        if (!timerNotificationRunning && ((timerState.getTimerOneState() |
                timerState.getTimerTwoState() |
                timerState.getTimerThreeState() |
                timerState.getTimerOneState() ) == TimerState.RUNNING)) {
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
                    if (((timerState.getActiveTimer() == TimerState.TIMER_ONE) && (timerState.getTimerOneState() == TimerState.INPUT)) |
                            ((timerState.getActiveTimer() == TimerState.TIMER_TWO) && (timerState.getTimerTwoState() == TimerState.INPUT)) |
                            ((timerState.getActiveTimer() == TimerState.TIMER_THREE) && (timerState.getTimerThreeState() == TimerState.INPUT)) |
                            ((timerState.getActiveTimer() == TimerState.TIMER_FOUR) && (timerState.getTimerFourState() == TimerState.INPUT))) {
                        timerHour = timerPreferences.getPreviousHoursOne();
                        timerMinute = timerPreferences.getPreviousMinutesOne();
                        setInputText();

//                        setInputVis();
                    }
                break;

            case R.id.t2_button:
                if (((timerState.getActiveTimer() == TimerState.TIMER_ONE) && (timerState.getTimerOneState() == TimerState.INPUT)) |
                        ((timerState.getActiveTimer() == TimerState.TIMER_TWO) && (timerState.getTimerTwoState() == TimerState.INPUT)) |
                        ((timerState.getActiveTimer() == TimerState.TIMER_THREE) && (timerState.getTimerThreeState() == TimerState.INPUT)) |
                        ((timerState.getActiveTimer() == TimerState.TIMER_FOUR) && (timerState.getTimerFourState() == TimerState.INPUT))) {
                    timerHour = timerPreferences.getPreviousHoursTwo();
                    timerMinute = timerPreferences.getPreviousMinutesTwo();
                    setInputText();

//                    setInputVis();
                }
                break;

            case R.id.t3_button:
                if (((timerState.getActiveTimer() == TimerState.TIMER_ONE) && (timerState.getTimerOneState() == TimerState.INPUT)) |
                        ((timerState.getActiveTimer() == TimerState.TIMER_TWO) && (timerState.getTimerTwoState() == TimerState.INPUT)) |
                        ((timerState.getActiveTimer() == TimerState.TIMER_THREE) && (timerState.getTimerThreeState() == TimerState.INPUT)) |
                        ((timerState.getActiveTimer() == TimerState.TIMER_FOUR) && (timerState.getTimerFourState() == TimerState.INPUT))) {
                    timerHour = timerPreferences.getPreviousHoursThree();
                    timerMinute = timerPreferences.getPreviousMinutesThree();
                    setInputText();

//                    setInputVis();
                }
                break;

            case R.id.t4_button:
                if (((timerState.getActiveTimer() == TimerState.TIMER_ONE) && (timerState.getTimerOneState() == TimerState.INPUT)) |
                        ((timerState.getActiveTimer() == TimerState.TIMER_TWO) && (timerState.getTimerTwoState() == TimerState.INPUT)) |
                        ((timerState.getActiveTimer() == TimerState.TIMER_THREE) && (timerState.getTimerThreeState() == TimerState.INPUT)) |
                        ((timerState.getActiveTimer() == TimerState.TIMER_FOUR) && (timerState.getTimerFourState() == TimerState.INPUT))) {
                    timerHour = timerPreferences.getPreviousHoursFour();
                    timerMinute = timerPreferences.getPreviousMinutesFour();
                    setInputText();

//                    setInputVis();
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
            if (timerState.getTimerOneState() == TimerState.INPUT) {
//            Log.v(TAG, "Timer One Selected");
                timerState.setActiveTimer(TimerState.TIMER_ONE);

                resetTimerInputValues();

                setInputText();

                setInputVis(R.string.timer_one_indicator);
            } else {
//            Log.v(TAG, "Timer One Selected");

                timerState.setActiveTimer(TimerState.TIMER_ONE);

                setClockViewOneVis();
            }
        }
    }

    public void timerTwoButton(View view) {

        if (view.getId() == R.id.t2_button) {
            if (timerState.getTimerTwoState() == TimerState.INPUT) {
//            Log.v(TAG, "Timer Two Selected");
                timerState.setActiveTimer(TimerState.TIMER_TWO);

                resetTimerInputValues();

                setInputText();

                setInputVis(R.string.timer_two_indicator);
            } else {
//            Log.v(TAG, "Timer Two Selected");
                timerState.setActiveTimer(TimerState.TIMER_TWO);

                setClockViewTwoVis();
            }
        }
    }

    public void timerThreeButton(View view) {

        if (view.getId() == R.id.t3_button) {
            if (timerState.getTimerThreeState() == TimerState.INPUT) {
//            Log.v(TAG, "Timer Three Selected");
                timerState.setActiveTimer(TimerState.TIMER_THREE);

                resetTimerInputValues();

                setInputText();

                setInputVis(R.string.timer_three_indicator);
            } else {
//            Log.v(TAG, "Timer Three Selected");
                timerState.setActiveTimer(TimerState.TIMER_THREE);

                setClockViewThreeVis();
            }
        }
    }

    public void timerFourButton(View view) {

        if (view.getId() == R.id.t4_button) {
            if (timerState.getTimerFourState() == TimerState.INPUT) {
//            Log.v(TAG, "Timer Four Selected");
                timerState.setActiveTimer(TimerState.TIMER_FOUR);

                resetTimerInputValues();

                setInputText();

                setInputVis(R.string.timer_four_indicator);
            } else {
//            Log.v(TAG, "Timer Four Selected");
                timerState.setActiveTimer(TimerState.TIMER_FOUR);

                setClockViewFourVis();
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
            if ((timerState.getActiveTimer() == TimerState.TIMER_ONE) && (timerState.getTimerOneState() == TimerState.INPUT) && ((timerHour > 0) | (timerMinute > 0))) {
                setClockViewOneVis();

                millisToCount = convertMillis();
                countDownTimer = new CountTimer(millisToCount, 1);
                hms = timerFormat(millisToCount);
                clockView1.setText(hms);
                timerPreferences.setStartTime(getCurrentTimeMillis());
                countDownTimer.start();
//            Log.v(TAG, "Timer Start");
                timerState.setTimerOneState(TimerState.RUNNING);

                timerPreferences.setPreviousHoursOne(timerHour);
                timerPreferences.setPreviousMinutesOne(timerMinute);

                resetTimerInputValues();

                setInputText();
            } else if ((timerState.getActiveTimer() == TimerState.TIMER_ONE) && (timerState.getTimerOneState() == TimerState.PAUSED)) {
//            Log.v(TAG, "pausedTime = " + pausedTime);
                timerResume();
            }

            if ((timerState.getActiveTimer() == TimerState.TIMER_TWO) && (timerState.getTimerTwoState() == TimerState.INPUT) && ((timerHour > 0) | (timerMinute > 0))) {
                setClockViewTwoVis();

                millisToCountTwo = convertMillis();
                countDownTimerTwo = new CountTimer(millisToCountTwo, 2);
                hms = timerFormat(millisToCountTwo);
                clockView2.setText(hms);
                timerPreferences.setStartTimeTwo(getCurrentTimeMillis());
                countDownTimerTwo.start();
//            Log.v(TAG, "Timer Two Start");
                timerState.setTimerTwoState(TimerState.RUNNING);

                timerPreferences.setPreviousHoursTwo(timerHour);
                timerPreferences.setPreviousMinutesTwo(timerMinute);

                resetTimerInputValues();

                setInputText();
            } else if ((timerState.getActiveTimer() == TimerState.TIMER_TWO) && (timerState.getTimerTwoState() == TimerState.PAUSED)) {
                timerResume();
            }

            if ((timerState.getActiveTimer() == TimerState.TIMER_THREE) && (timerState.getTimerThreeState() == TimerState.INPUT) && ((timerHour > 0) | (timerMinute > 0))) {
                setClockViewThreeVis();

                millisToCountThree = convertMillis();
                countDownTimerThree = new CountTimer(millisToCountThree, 3);
                hms = timerFormat(millisToCountThree);
                clockView3.setText(hms);
                timerPreferences.setStartTimeThree(getCurrentTimeMillis());
                countDownTimerThree.start();
//            Log.v(TAG, "Timer Three Start");
                timerState.setTimerThreeState(TimerState.RUNNING);

                timerPreferences.setPreviousHoursThree(timerHour);
                timerPreferences.setPreviousMinutesThree(timerMinute);

                resetTimerInputValues();

                setInputText();
            } else if ((timerState.getActiveTimer() == TimerState.TIMER_THREE) && (timerState.getTimerThreeState() == TimerState.PAUSED)) {
                timerResume();
            }

            if ((timerState.getActiveTimer() == TimerState.TIMER_FOUR) && (timerState.getTimerFourState() == TimerState.INPUT) && ((timerHour > 0) | (timerMinute > 0))) {
                setClockViewFourVis();

                millisToCountFour = convertMillis();
                countDownTimerFour = new CountTimer(millisToCountFour, 4);
                hms = timerFormat(millisToCountFour);
                clockView4.setText(hms);
                timerPreferences.setStartTimeFour(getCurrentTimeMillis());
                countDownTimerFour.start();
//            Log.v(TAG, "Timer Four Start");
                timerState.setTimerFourState(TimerState.RUNNING);

                timerPreferences.setPreviousHoursFour(timerHour);
                timerPreferences.setPreviousMinutesFour(timerMinute);

                resetTimerInputValues();

                setInputText();
            } else if ((timerState.getActiveTimer() == TimerState.TIMER_FOUR) && (timerState.getTimerFourState() == TimerState.PAUSED)) {
                timerResume();
            }
        }
    }

    public void stopButton(View view) {

        if (view.getId() == R.id.stop_button) {
            if ((timerState.getTimerOneState() |
                    timerState.getTimerTwoState() |
                    timerState.getTimerThreeState() |
                    timerState.getTimerFourState() ) == TimerState.RUNNING) {
                timerPause();
            } else if (timerAlarm) {
                stopAlarm();
                timerAlarm = false;
            }
        }
    }

    public void resetButton(View view) {

        if (view.getId() == R.id.reset_button) {
            if ((timerState.getActiveTimer() == TimerState.TIMER_ONE) && (timerState.getTimerOneState() != TimerState.RUNNING)) {
                resetTimerOne();
            }

            if ((timerState.getActiveTimer() == TimerState.TIMER_TWO) && (timerState.getTimerTwoState() != TimerState.RUNNING)) {
                resetTimerTwo();
            }

        if ((timerState.getActiveTimer() == TimerState.TIMER_THREE) && (timerState.getTimerThreeState() != TimerState.RUNNING)) {
                resetTimerThree();
            }

            if ((timerState.getActiveTimer() == TimerState.TIMER_FOUR) && (timerState.getTimerFourState() != TimerState.RUNNING)) {
                resetTimerFour();
            }
        }
    }

    private void timerPause() {

        if ((timerState.getTimerOneState() == TimerState.RUNNING) && (timerState.getActiveTimer() == TimerState.TIMER_ONE)) {
            countDownTimer.cancel();
            timerState.setTimerOneState(TimerState.PAUSED);
        }

        if ((timerState.getTimerTwoState() == TimerState.RUNNING) && (timerState.getActiveTimer() == TimerState.TIMER_TWO)) {
            countDownTimerTwo.cancel();
            timerState.setTimerTwoState(TimerState.PAUSED);
        }

        if ((timerState.getTimerThreeState() == TimerState.RUNNING) && (timerState.getActiveTimer() == TimerState.TIMER_THREE)) {
            countDownTimerThree.cancel();
            timerState.setTimerThreeState(TimerState.PAUSED);
        }

        if ((timerState.getTimerFourState() == TimerState.RUNNING) && (timerState.getActiveTimer() == TimerState.TIMER_FOUR)) {
            countDownTimerFour.cancel();
            timerState.setTimerFourState(TimerState.PAUSED);
        }
    }

    private void timerResume() {
//        Log.v(TAG, "pausedTime = " + pausedTime);

        if ((timerState.getTimerOneState() == TimerState.PAUSED) && (timerState.getActiveTimer() == TimerState.TIMER_ONE)) {
            countDownTimer = new CountTimer(pausedTime, 1);
            countDownTimer.start();
            timerState.setTimerOneState(TimerState.RUNNING);
            timerPreferences.setTimerOneState(timerState.getTimerOneState());
//            Log.v(TAG, "timerResume");
        }

        if ((timerState.getTimerTwoState() == TimerState.PAUSED) && (timerState.getActiveTimer() == TimerState.TIMER_TWO)) {
            countDownTimerTwo = new CountTimer(pausedTimeTwo, 2);
            countDownTimerTwo.start();
            timerState.setTimerTwoState(TimerState.RUNNING);
            timerPreferences.setTimerTwoState(timerState.getTimerTwoState());
//            Log.v(TAG, "timerTwoResume");
        }

        if ((timerState.getTimerThreeState() == TimerState.PAUSED) && (timerState.getActiveTimer() == TimerState.TIMER_THREE)) {
            countDownTimerThree = new CountTimer(pausedTimeThree, 3);
            countDownTimerThree.start();
            timerState.setTimerThreeState(TimerState.RUNNING);
            timerPreferences.setTimerThreeState(timerState.getTimerThreeState());
//            Log.v(TAG, "timerThreeResume");
        }

        if ((timerState.getTimerFourState() == TimerState.PAUSED) && (timerState.getActiveTimer() == TimerState.TIMER_FOUR)) {
            countDownTimerFour = new CountTimer(pausedTimeFour, 4);
            countDownTimerFour.start();
            timerState.setTimerFourState(TimerState.RUNNING);
            timerPreferences.setTimerFourState(timerState.getTimerFourState());
//            Log.v(TAG, "timerFourResume");
        }
    }

    private static long getCurrentTimeMillis() {

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

    private static long recalcPausedTime(long millis, long startTime) {
        return millis - (getCurrentTimeMillis() - startTime);
    }

    private void initTimer() {
//        Log.v(TAG, "initTimer()");

        long startTime = timerPreferences.getStartTime();
        long startTimeTwo = timerPreferences.getStartTimeTwo();
        long startTimeThree = timerPreferences.getStartTimeThree();
        long startTimeFour = timerPreferences.getStartTimeFour();

        if ((startTime > 0) && (timerState.getTimerOneState() == TimerState.RUNNING)) {
            pausedTime = recalcPausedTime(millisToCount, startTime);

            if (pausedTime > 0) {
                countDownTimer = new CountTimer(pausedTime, 1);
                countDownTimer.start();
            }
        } else if ((startTime > 0) && (timerState.getTimerOneState() == TimerState.PAUSED)) {
            timerState.setTimerOneState(TimerState.PAUSED);
        } else if (startTime <= 0) {
            timerState.setTimerOneState(TimerState.INPUT);
        }

        if ((startTimeTwo > 0) && (timerState.getTimerTwoState() == TimerState.RUNNING)) {
            pausedTimeTwo = recalcPausedTime(millisToCountTwo, startTimeTwo);

            if (pausedTimeTwo >0 ) {
                countDownTimerTwo = new CountTimer(pausedTimeTwo, 2);
                countDownTimerTwo.start();
            }
        } else if ((startTimeTwo > 0) && (timerState.getTimerTwoState() == TimerState.PAUSED)) {
            timerState.setTimerTwoState(TimerState.PAUSED);
        } else if (startTimeTwo <= 0) {
            timerState.setTimerTwoState(TimerState.INPUT);
        }

        if ((startTimeThree > 0) && (timerState.getTimerThreeState() == TimerState.RUNNING)) {
            pausedTimeThree = recalcPausedTime(millisToCountThree, startTimeThree);

            if (pausedTimeThree > 0) {
                countDownTimerThree = new CountTimer(pausedTimeThree, 3);
                countDownTimerThree.start();
            }
        } else if ((startTimeThree > 0) && (timerState.getTimerThreeState() == TimerState.PAUSED)) {
            timerState.setTimerThreeState(TimerState.PAUSED);
        } else if (startTimeThree <= 0){
            timerState.setTimerThreeState(TimerState.INPUT);
        }

        if ((startTimeFour > 0) && (timerState.getTimerFourState() == TimerState.RUNNING)) {
            pausedTimeFour = recalcPausedTime(millisToCountFour, startTimeFour);

            if (pausedTimeFour > 0) {
                countDownTimerFour = new CountTimer(pausedTimeFour, 4);
                countDownTimerFour.start();
            }
        } else if ((startTimeFour > 0) && (timerState.getTimerFourState() == TimerState.PAUSED)) {
            timerState.setTimerFourState(TimerState.PAUSED);
        } else if (startTimeFour <= 0){
            timerState.setTimerFourState(TimerState.INPUT);
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
                    ((MainActivity) getActivity()).resetTimerOne();
                }

                if (((MainActivity) getActivity()).countDownTimerTwo != null) {
                    ((MainActivity) getActivity()).resetTimerTwo();
                }

                if (((MainActivity) getActivity()).countDownTimerThree != null) {
                    ((MainActivity) getActivity()).resetTimerThree();
                }

                if (((MainActivity) getActivity()).countDownTimerFour != null) {
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
                    timerState.setTimerOneState(TimerState.INPUT);
                    millisToCount = 0;

                    timerPreferences.setTimerOneState(timerState.getTimerOneState());
                    timerPreferences.setStartTime(0);
                    timerPreferences.setOriginalTime(0);
                    timerPreferences.setPausedTime(0);

                    resetTimerInputValues();

                    setInputVis(R.string.timer_one_indicator);

                    break;

                case 2:
                    countDownTimerTwo.cancel();
                    countDownTimerTwo = null;
                    timerState.setTimerTwoState(TimerState.INPUT);
                    millisToCountTwo = 0;

                    timerPreferences.setTimerTwoState(timerState.getTimerTwoState());
                    timerPreferences.setStartTimeTwo(0);
                    timerPreferences.setOriginalTimeTwo(0);
                    timerPreferences.setPausedTimeTwo(0);

                    resetTimerInputValues();

                    setInputVis(R.string.timer_two_indicator);

                    break;

                case 3:
                    countDownTimerThree.cancel();
                    countDownTimerThree = null;
                    timerState.setTimerThreeState(TimerState.INPUT);
                    millisToCountThree = 0;

                    timerPreferences.setTimerThreeState(timerState.getTimerThreeState());
                    timerPreferences.setStartTimeThree(0);
                    timerPreferences.setOriginalTimeThree(0);
                    timerPreferences.setPausedTimeThree(0);

                    resetTimerInputValues();

                    setInputVis(R.string.timer_three_indicator);

                    break;

                case 4:
                    countDownTimerFour.cancel();
                    countDownTimerFour = null;
                    timerState.setTimerFourState(TimerState.INPUT);
                    millisToCountFour = 0;

                    timerPreferences.setTimerFourState(timerState.getTimerFourState());
                    timerPreferences.setStartTimeFour(0);
                    timerPreferences.setOriginalTimeFour(0);
                    timerPreferences.setPausedTimeFour(0);

                    resetTimerInputValues();

                    setInputVis(R.string.timer_four_indicator);

                    break;

                default:
                    break;
            }

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

    private void setInputVis(int indicator) {

        inputSeconds.setVisibility(View.VISIBLE);
        inputMinutes.setVisibility(View.VISIBLE);
        inputHours.setVisibility(View.VISIBLE);
        clockView1.setVisibility(View.GONE);
        clockView2.setVisibility(View.GONE);
        clockView3.setVisibility(View.GONE);
        clockView4.setVisibility(View.GONE);

        timerIndicator.setText(indicator);
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

        timerIndicator.setText(R.string.timer_one_indicator);
    }

    private void setClockViewTwoVis() {

        inputSeconds.setVisibility(View.INVISIBLE);
        inputMinutes.setVisibility(View.INVISIBLE);
        inputHours.setVisibility(View.INVISIBLE);
        clockView1.setVisibility(View.GONE);
        clockView2.setVisibility(View.VISIBLE);
        clockView3.setVisibility(View.GONE);
        clockView4.setVisibility(View.GONE);

        timerIndicator.setText(R.string.timer_two_indicator);
    }

    private void setClockViewThreeVis() {

        inputSeconds.setVisibility(View.INVISIBLE);
        inputMinutes.setVisibility(View.INVISIBLE);
        inputHours.setVisibility(View.INVISIBLE);
        clockView1.setVisibility(View.GONE);
        clockView2.setVisibility(View.GONE);
        clockView3.setVisibility(View.VISIBLE);
        clockView4.setVisibility(View.GONE);

        timerIndicator.setText(R.string.timer_three_indicator);
    }

    private void setClockViewFourVis() {

        inputSeconds.setVisibility(View.INVISIBLE);
        inputMinutes.setVisibility(View.INVISIBLE);
        inputHours.setVisibility(View.INVISIBLE);
        clockView1.setVisibility(View.GONE);
        clockView2.setVisibility(View.GONE);
        clockView3.setVisibility(View.GONE);
        clockView4.setVisibility(View.VISIBLE);

        timerIndicator.setText(R.string.timer_four_indicator);
    }

    private void resetTimerOne() {
//        Log.v(TAG, "Reset Timer One");
        countDownTimer.cancel();
        countDownTimer = null;
        timerState.setTimerOneState(TimerState.INPUT);

        resetTimerInputValues();

        millisToCount = 0;
        pausedTime = 0;

        timerPreferences.setStartTime(0);
        timerPreferences.setOriginalTime(0);
        timerPreferences.setTimerOneState(timerState.getTimerOneState());
        timerPreferences.setPausedTime(0);

        setInputVis(R.string.timer_one_indicator);

        setInputText();
    }

    private void resetTimerTwo() {
//        Log.v(TAG, "Reset Timer Two");
        countDownTimerTwo.cancel();
        countDownTimerTwo = null;
        timerState.setTimerTwoState(TimerState.INPUT);

        resetTimerInputValues();

        millisToCountTwo = 0;
        pausedTimeTwo = 0;

        timerPreferences.setStartTimeTwo(0);
        timerPreferences.setOriginalTimeTwo(0);
        timerPreferences.setTimerTwoState(timerState.getTimerTwoState());
        timerPreferences.setPausedTimeTwo(0);

        setInputVis(R.string.timer_two_indicator);

        setInputText();
    }

    private void resetTimerThree() {
//        Log.v(TAG, "Reset Timer Four");
        countDownTimerThree.cancel();
        countDownTimerThree = null;
        timerState.setTimerThreeState(TimerState.INPUT);

        resetTimerInputValues();

        millisToCountThree = 0;
        pausedTimeThree = 0;

        timerPreferences.setStartTimeThree(0);
        timerPreferences.setOriginalTimeThree(0);
        timerPreferences.setTimerThreeState(timerState.getTimerThreeState());
        timerPreferences.setPausedTimeThree(0);

        setInputVis(R.string.timer_three_indicator);

        setInputText();
    }

    private void resetTimerFour() {
//        Log.v(TAG, "Reset Timer Four");
        countDownTimerFour.cancel();
        countDownTimerFour = null;
        timerState.setTimerFourState(TimerState.INPUT);

        resetTimerInputValues();

        millisToCountFour = 0;
        pausedTimeFour = 0;

        timerPreferences.setStartTimeFour(0);
        timerPreferences.setOriginalTimeFour(0);
        timerPreferences.setTimerFourState(timerState.getTimerFourState());
        timerPreferences.setPausedTimeFour(0);

        setInputVis(R.string.timer_four_indicator);

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