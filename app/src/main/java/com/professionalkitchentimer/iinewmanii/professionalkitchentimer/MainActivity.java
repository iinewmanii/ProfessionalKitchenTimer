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
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Vibrator;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.NativeExpressAdView;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
import static android.media.AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE;

//import android.util.Log;

public class MainActivity extends AppCompatActivity implements View.OnLongClickListener, View.OnTouchListener {

    private static final int REP_DELAY = 125;
    private static final int countdownInterval = 125;
    private static final int warningAlarmStart = 301000;
    private static final int warningAlarmEnd = 300000;
    private final Handler repeatMinuteHandler = new Handler();
    private final Handler resetAllHandler = new Handler();
//    private static final String TAG = "NEWMAN";
    private Button minute_plus_button,
            minute_minus_button,
            start_button,
            reset_button;
    private TextView inputHours,
            inputMinutes,
            inputSeconds,
            clockView1,
            clockView2,
            clockView3,
            clockView4,
            timerIndicator;
    private boolean mAutoIncrement = false;
    private boolean mAutoDecrement = false;
    private NativeExpressAdView mAdView;
    private PrefUtils timerPreferences;
    private AudioManager audioManager;
    private String hms;
    private int timerSecond = 0;
    private int timerMinute = 0;
    private int timerHour = 0;
    private int originalVolume;
    private boolean timerOneActive = false;
    private boolean timerTwoActive = false;
    private boolean timerThreeActive = false;
    private boolean timerFourActive = false;
    private boolean timerRunning = false;
    private boolean timerTwoRunning = false;
    private boolean timerThreeRunning = false;
    private boolean timerFourRunning = false;
    private boolean timerPaused = false;
    private boolean timerTwoPaused = false;
    private boolean timerThreePaused = false;
    private boolean timerFourPaused = false;
    private boolean timerNotificationRunning = false;
    private boolean timerWarning;
    private boolean timerAlarm = false;
    private boolean isLongPress = false;
    private CountTimer countDownTimer = null;
    private CountTimer countDownTimerTwo = null;
    private CountTimer countDownTimerThree = null;
    private CountTimer countDownTimerFour = null;
    private long pausedTime = 0;
    private long pausedTimeTwo = 0;
    private long pausedTimeThree = 0;
    private long pausedTimeFour = 0;
    private long millisToCount = 0;
    private long millisToCountTwo = 0;
    private long millisToCountThree = 0;
    private long millisToCountFour = 0;
    private long wakeUpTime = 0;
    private long wakeUpTimeTwo = 0;
    private long wakeUpTimeThree = 0;
    private long wakeUpTimeFour = 0;
    private MediaPlayer alarmPlayer;
    private AudioManager.OnAudioFocusChangeListener afChangeListener = null;
    private Vibrator vibrator = null;

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

        minute_plus_button = (Button) findViewById(R.id.minute_plus_button);
        minute_minus_button = (Button) findViewById(R.id.minute_minus_button);
        start_button = (Button) findViewById(R.id.start_button);
        reset_button = (Button) findViewById(R.id.reset_button);

        FirebaseMessaging.getInstance().subscribeToTopic("global");
//        FirebaseMessaging.getInstance().subscribeToTopic("test");

        if (mAdView == null) {
            mAdView = (NativeExpressAdView) findViewById(R.id.adView);

            AdRequest adRequest = new AdRequest.Builder()
//                .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                .addTestDevice("A95D2EF9F7259D162EA1A427BB252242")
                    .build();
            mAdView.loadAd(adRequest);
            mAdView.bringToFront();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
//        Log.v(TAG, "onStart");

//        timerPreferences = new PrefUtils(this);

        minute_plus_button.setOnLongClickListener(this);
        minute_plus_button.setOnTouchListener(this);
        minute_minus_button.setOnLongClickListener(this);
        minute_minus_button.setOnTouchListener(this);
        start_button.setOnLongClickListener(this);
        start_button.setOnTouchListener(this);
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

        timerOneActive = timerPreferences.getTimerOneActive();
        timerTwoActive = timerPreferences.getTimerTwoActive();
        timerThreeActive = timerPreferences.getTimerThreeActive();
        timerFourActive = timerPreferences.getTimerFourActive();
        millisToCount = timerPreferences.getOriginalTime();
        millisToCountTwo = timerPreferences.getOriginalTimeTwo();
        millisToCountThree = timerPreferences.getOriginalTimeThree();
        millisToCountFour = timerPreferences.getOriginalTimeFour();
        pausedTime = timerPreferences.getPausedTime();
        pausedTimeTwo = timerPreferences.getPausedTimeTwo();
        pausedTimeThree = timerPreferences.getPausedTimeThree();
        pausedTimeFour = timerPreferences.getPausedTimeFour();
        timerPaused = timerPreferences.getTimerPaused();
        timerTwoPaused = timerPreferences.getTimerTwoPaused();
        timerThreePaused = timerPreferences.getTimerThreePaused();
        timerFourPaused = timerPreferences.getTimerFourPaused();
        timerRunning = timerPreferences.getTimerOneRunning();
        timerTwoRunning = timerPreferences.getTimerTwoRunning();
        timerThreeRunning = timerPreferences.getTimerThreeRunning();
        timerFourRunning = timerPreferences.getTimerFourRunning();
        timerNotificationRunning = timerPreferences.getTimerNotificationRunning();
        timerWarning = timerPreferences.getWarningAlarm();

//        Log.v(TAG, "timerRunning = " + timerRunning);

        if (timerNotificationAlarm) {
            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION, originalNotificationVolume, 0);
            timerPreferences.setTimerNotificationAlarm(false);
        }

        if (!timerOneActive && !timerTwoActive && !timerThreeActive && !timerFourActive) {
            timerOneActive = true;
            timerTwoActive = false;
            timerThreeActive = false;
            timerFourActive = false;

            setInputVis();

            timerIndicator.setText(R.string.timer_one_indicator);
        }

        if (timerRunning || timerTwoRunning || timerThreeRunning || timerFourRunning) {
            removeAlarmManager();
            initTimer();
//            Log.v(TAG, "Alarm Manager Removed, Initialize Timer");
        }

        if (timerPaused && pausedTime > 0) {
            hms = timerFormat(pausedTime);
            clockView1.setText(hms);
        }

        if (timerTwoPaused && pausedTimeTwo > 0) {
            hms = timerFormat(pausedTimeTwo);
            clockView2.setText(hms);
        }

        if (timerThreePaused && pausedTimeThree > 0) {
            hms = timerFormat(pausedTimeThree);
            clockView3.setText(hms);
        }

        if (timerFourPaused && pausedTimeFour > 0) {
            hms = timerFormat(pausedTimeFour);
            clockView4.setText(hms);
        }

        if (timerOneActive && (pausedTime > 0 || timerRunning)) {
            setClockViewOneVis();
            timerIndicator.setText(R.string.timer_one_indicator);
        } else if (timerOneActive && pausedTime <= 0) {
            setInputVis();
            timerIndicator.setText(R.string.timer_one_indicator);
        }

        if (timerTwoActive && (pausedTimeTwo > 0 || timerTwoRunning)) {
            setClockViewTwoVis();
            timerIndicator.setText(R.string.timer_two_indicator);
        } else if (timerTwoActive && pausedTimeTwo <= 0) {
            setInputVis();
            timerIndicator.setText(R.string.timer_two_indicator);
        }

        if (timerThreeActive && (pausedTimeThree > 0 || timerThreeRunning)) {
            setClockViewThreeVis();
            timerIndicator.setText(R.string.timer_three_indicator);
        } else if (timerThreeActive && pausedTimeThree <= 0) {
            setInputVis();
            timerIndicator.setText(R.string.timer_three_indicator);
        }

        if (timerFourActive && (pausedTimeFour > 0 || timerFourRunning)) {
            setClockViewFourVis();
            timerIndicator.setText(R.string.timer_four_indicator);
        } else if (timerFourActive && pausedTimeFour <= 0) {
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
    }

    @Override
    public void onPause() {
//        Log.v(TAG, "onPause");

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        timerPreferences.setTimerOneActive(timerOneActive);
        timerPreferences.setTimerTwoActive(timerTwoActive);
        timerPreferences.setTimerThreeActive(timerThreeActive);
        timerPreferences.setTimerFourActive(timerFourActive);

//        Log.v(TAG, "onPause pausedTime = " + pausedTime);

        if (!timerPaused && timerRunning) {
            timerPreferences.setOriginalTime(millisToCount);
            timerPreferences.setPausedTime(pausedTime);
            timerPreferences.setTimerPaused(false);
            timerPreferences.setTimerOneRunning(true);
//            Log.v(TAG, "timerPreferences timerOneRunning = true");
            countDownTimer.cancel();
            countDownTimer = null;
        } else if (!timerPaused) {
            millisToCount = 0;
            timerPreferences.setOriginalTime(0);
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        } else {
            timerPreferences.setTimerOneRunning(false);
//            Log.v(TAG, "timerPreferences timerOneRunning set to false");
            timerPreferences.setPausedTime(pausedTime);
            timerPreferences.setTimerPaused(timerPaused);
            timerPreferences.setOriginalTime(millisToCount);
            if (countDownTimer != null) {
                countDownTimer.cancel();
                countDownTimer = null;
            }
        }

        if (!timerTwoPaused && timerTwoRunning) {
            timerPreferences.setOriginalTimeTwo(millisToCountTwo);
            timerPreferences.setPausedTimeTwo(pausedTimeTwo);
            timerPreferences.setTimerTwoPaused(false);
            timerPreferences.setTimerTwoRunning(true);
            countDownTimerTwo.cancel();
            countDownTimerTwo = null;
        } else if (!timerTwoPaused) {
            millisToCountTwo = 0;
            timerPreferences.setOriginalTimeTwo(0);
            if (countDownTimerTwo != null) {
                countDownTimerTwo.cancel();
                countDownTimerTwo = null;
            }
        } else {
            timerPreferences.setTimerTwoRunning(false);
            timerPreferences.setPausedTimeTwo(pausedTimeTwo);
            timerPreferences.setTimerTwoPaused(timerTwoPaused);
            timerPreferences.setOriginalTimeTwo(millisToCountTwo);
            if (countDownTimerTwo != null) {
                countDownTimerTwo.cancel();
                countDownTimerTwo = null;
            }
        }

        if (!timerThreePaused && timerThreeRunning) {
            timerPreferences.setOriginalTimeThree(millisToCountThree);
            timerPreferences.setPausedTimeThree(pausedTimeThree);
            timerPreferences.setTimerThreePaused(false);
            timerPreferences.setTimerThreeRunning(true);
            countDownTimerThree.cancel();
            countDownTimerThree = null;
        } else if (!timerThreePaused) {
            millisToCountThree = 0;
            timerPreferences.setOriginalTimeThree(0);
            if (countDownTimerThree != null) {
                countDownTimerThree.cancel();
                countDownTimerThree = null;
            }
        } else {
            timerPreferences.setTimerThreeRunning(false);
            timerPreferences.setPausedTimeThree(pausedTimeThree);
            timerPreferences.setTimerThreePaused(timerThreePaused);
            timerPreferences.setOriginalTimeThree(millisToCountThree);
            if (countDownTimerThree != null) {
                countDownTimerThree.cancel();
                countDownTimerThree = null;
            }
        }

        if (!timerFourPaused && timerFourRunning) {
            timerPreferences.setOriginalTimeFour(millisToCountFour);
            timerPreferences.setPausedTimeFour(pausedTimeFour);
            timerPreferences.setTimerFourPaused(false);
            timerPreferences.setTimerFourRunning(true);
            countDownTimerFour.cancel();
            countDownTimerFour = null;
        } else if (!timerFourPaused) {
            millisToCountFour = 0;
            timerPreferences.setOriginalTimeFour(0);
            if (countDownTimerFour != null) {
                countDownTimerFour.cancel();
                countDownTimerFour = null;
            }
        } else {
            timerPreferences.setTimerFourRunning(false);
            timerPreferences.setPausedTimeFour(pausedTimeFour);
            timerPreferences.setTimerFourPaused(timerFourPaused);
            timerPreferences.setOriginalTimeFour(millisToCountFour);
            if (countDownTimerFour != null) {
                countDownTimerFour.cancel();
                countDownTimerFour = null;
            }
        }

        setAlarmManager();

//        Log.v(TAG, "wakeUpTime = " + wakeUpTime);
//        Log.v(TAG, "wakeUpTimeTwo = " + wakeUpTimeTwo);
//        Log.v(TAG, "wakeUpTimeThree = " + wakeUpTimeThree);
//        Log.v(TAG, "wakeUpTimeFour = " + wakeUpTimeFour);

        timerPreferences.setWakeUpTimeOne(wakeUpTime);
        timerPreferences.setWakeUpTimeTwo(wakeUpTimeTwo);
        timerPreferences.setWakeUpTimeThree(wakeUpTimeThree);
        timerPreferences.setWakeUpTimeFour(wakeUpTimeFour);


        if (!timerNotificationRunning && (timerRunning || timerTwoRunning || timerThreeRunning || timerFourRunning)) {
            Intent timerNotifIntent = new Intent(this, TimerNotifyService.class);
            startService(timerNotifIntent);
            timerPreferences.setTimerNotificationRunning(true);
//            Log.v(TAG, "Starting Timer notify Service");
        }

        super.onPause();
    }

    @Override
    protected void onStop() {
//        Log.v(TAG, "onStop");

        minute_plus_button.setOnLongClickListener(null);
        minute_plus_button.setOnTouchListener(null);
        minute_minus_button.setOnLongClickListener(null);
        minute_minus_button.setOnTouchListener(null);
        start_button.setOnLongClickListener(null);
        start_button.setOnTouchListener(null);

        repeatMinuteHandler.removeCallbacksAndMessages(null);
        resetAllHandler.removeCallbacksAndMessages(null);

        timerPreferences = null;

        hms = null;

        super.onStop();
    }

    @Override
    public void onDestroy() {
//        Log.v(TAG, "onDestroy");

        mAdView.destroy();
        mAdView = null;

        super.onDestroy();
    }

    @Override
    public boolean onLongClick(View view) {
        if (view == minute_plus_button) {
            mAutoIncrement = true;
            repeatMinuteHandler.post(new RptMinuteUpdater());
        }

        if (view == minute_minus_button) {
            mAutoDecrement = true;
            repeatMinuteHandler.post(new RptMinuteUpdater());
        }

        if (view == start_button && timerOneActive && !timerRunning && !timerPaused) {
            timerHour = timerPreferences.getPreviousHoursOne();
            timerMinute = timerPreferences.getPreviousMinutesOne();

//            Log.v(TAG, "timerHour = " + timerHour);
//            Log.v(TAG, "timerMinute = " + timerMinute);

            setInputText();

            setInputVis();
        }

        if (view == start_button && timerTwoActive && !timerTwoRunning && !timerTwoPaused) {
            timerHour = timerPreferences.getPreviousHoursTwo();
            timerMinute = timerPreferences.getPreviousMinutesTwo();

            setInputText();

            setInputVis();
        }

        if (view == start_button && timerThreeActive && !timerThreeRunning && !timerThreePaused) {
            timerHour = timerPreferences.getPreviousHoursThree();
            timerMinute = timerPreferences.getPreviousMinutesThree();

            setInputText();

            setInputVis();
        }

        if (view == start_button && timerFourActive && !timerFourRunning && !timerFourPaused) {
            timerHour = timerPreferences.getPreviousHoursFour();
            timerMinute = timerPreferences.getPreviousMinutesFour();

            setInputText();

            setInputVis();
        }

        return true;
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if (view == minute_plus_button && (motionEvent.getAction() == MotionEvent.ACTION_UP ||
                motionEvent.getAction() == MotionEvent.ACTION_CANCEL) && mAutoIncrement) {
            mAutoIncrement = false;
        } else if (view == minute_minus_button && (motionEvent.getAction() == MotionEvent.ACTION_UP ||
                motionEvent.getAction() == MotionEvent.ACTION_CANCEL) && mAutoDecrement) {
            mAutoDecrement = false;
        }

        if (view == reset_button && motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
            isLongPress = true;
            resetAllHandler.postDelayed(new ResetAllRunnable(), 1500);
        } else if (view == reset_button && motionEvent.getAction() == MotionEvent.ACTION_UP) {
            resetAllHandler.removeCallbacksAndMessages(null);
        }

        return false;
    }

    public void timerOneButton(View t1_button) {
        if (!timerOneActive && (timerRunning || timerPaused)) {
//            Log.v(TAG, "Timer One Selected");
            timerOneActive = true;
            timerTwoActive = false;
            timerThreeActive = false;
            timerFourActive = false;

            setClockViewOneVis();

            timerIndicator.setText(R.string.timer_one_indicator);
        } else {
//            Log.v(TAG, "Timer One Selected");
            timerOneActive = true;
            timerTwoActive = false;
            timerThreeActive = false;
            timerFourActive = false;

            timerHour = 0;
            timerMinute = 0;

            setInputText();

            setInputVis();

            timerIndicator.setText(R.string.timer_one_indicator);
        }
    }

    public void timerTwoButton(View t2_button) {
        if (!timerTwoActive && (timerTwoRunning || timerTwoPaused)) {
//            Log.v(TAG, "Timer Two Selected");
            timerOneActive = false;
            timerTwoActive = true;
            timerThreeActive = false;
            timerFourActive = false;

            setClockViewTwoVis();

            timerIndicator.setText(R.string.timer_two_indicator);
        } else {
//            Log.v(TAG, "Timer Two Selected");
            timerOneActive = false;
            timerTwoActive = true;
            timerThreeActive = false;
            timerFourActive = false;

            timerHour = 0;
            timerMinute = 0;

            setInputText();

            setInputVis();

            timerIndicator.setText(R.string.timer_two_indicator);
        }
    }

    public void timerThreeButton(View t3_button) {
        if (!timerThreeActive && (timerThreeRunning || timerThreePaused)) {
//            Log.v(TAG, "Timer Three Selected");
            timerOneActive = false;
            timerTwoActive = false;
            timerThreeActive = true;
            timerFourActive = false;

            setClockViewThreeVis();

            timerIndicator.setText(R.string.timer_three_indicator);
        } else {
//            Log.v(TAG, "Timer Three Selected");
            timerOneActive = false;
            timerTwoActive = false;
            timerThreeActive = true;
            timerFourActive = false;

            timerHour = 0;
            timerMinute = 0;

            setInputText();

            setInputVis();

            timerIndicator.setText(R.string.timer_three_indicator);
        }
    }

    public void timerFourButton(View t4_button) {
        if (!timerFourActive && (timerFourRunning || timerFourPaused)) {
//            Log.v(TAG, "Timer Four Selected");
            timerOneActive = false;
            timerTwoActive = false;
            timerThreeActive = false;
            timerFourActive = true;

            setClockViewFourVis();

            timerIndicator.setText(R.string.timer_four_indicator);
        } else {
//            Log.v(TAG, "Timer Four Selected");
            timerOneActive = false;
            timerTwoActive = false;
            timerThreeActive = false;
            timerFourActive = true;

            timerHour = 0;
            timerMinute = 0;

            setInputText();

            setInputVis();

            timerIndicator.setText(R.string.timer_four_indicator);
        }
    }

    public void minutePlus(View minute_plus_button) {
        if (timerMinute == 59) {
            timerMinute = 0;
            inputMinutes.setText(String.format(Locale.ENGLISH, ":%02d", timerMinute));

            timerHour = timerHour + 1;
            inputHours.setText(String.format(Locale.ENGLISH, "%01d", timerHour));
        }

        if (timerMinute < 59) {
//            Log.v(TAG, "Minute Plus");
            timerMinute = timerMinute + 1;
            inputMinutes.setText(String.format(Locale.ENGLISH, ":%02d", timerMinute));
        }
    }

    public void minuteMinus(View minute_minus_button) {
        if (timerMinute == 0 && timerHour > 0) {
            timerHour = timerHour - 1;
            inputHours.setText(String.format(Locale.ENGLISH, "%01d", timerHour));

            timerMinute = 59;
            inputMinutes.setText(String.format(Locale.ENGLISH, ":%02d", timerMinute));

        }

        if (timerMinute > 0) {
//            Log.v(TAG, "Minute Minus");
            timerMinute = timerMinute - 1;
            inputMinutes.setText(String.format(Locale.ENGLISH, ":%02d", timerMinute));
        }
    }

    public void hourPlus(View hour_plus_button) {
        if (timerHour < 9) {
//            Log.v(TAG, "Hour Plus");
            timerHour = timerHour + 1;
            inputHours.setText(String.format(Locale.ENGLISH, "%01d", timerHour));
        }
    }

    public void hourMinus(View hour_minus_button) {
        if (timerHour > 0) {
//            Log.v(TAG, "Hour Minus");
            timerHour = timerHour - 1;
            inputHours.setText(String.format(Locale.ENGLISH, "%01d", timerHour));
        }
    }

    public void startButton(View start_button) {
        if (timerOneActive && !timerRunning && !timerPaused && (timerHour > 0 || timerMinute > 0)) {
            setClockViewOneVis();

            millisToCount = convertMillis();
            countDownTimer = new CountTimer(millisToCount, 1);
            hms = timerFormat(millisToCount);
            clockView1.setText(hms);
            timerPreferences.setStartTime(getNow());
            countDownTimer.start();
//            Log.v(TAG, "Timer Start");
            timerRunning = true;

            timerPreferences.setPreviousHoursOne(timerHour);
            timerPreferences.setPreviousMinutesOne(timerMinute);

            resetTimerInputValues();

            setInputText();
        } else if (timerOneActive && timerPaused) {
//            Log.v(TAG, "pausedTime = " + pausedTime);
            timerResume();
        }

        if (timerTwoActive && !timerTwoRunning && !timerTwoPaused && (timerHour > 0 || timerMinute > 0)) {
            setClockViewTwoVis();

            millisToCountTwo = convertMillis();
            countDownTimerTwo = new CountTimer(millisToCountTwo, 2);
            hms = timerFormat(millisToCountTwo);
            clockView2.setText(hms);
            timerPreferences.setStartTimeTwo(getNow());
            countDownTimerTwo.start();
//            Log.v(TAG, "Timer Two Start");
            timerTwoRunning = true;

            timerPreferences.setPreviousHoursTwo(timerHour);
            timerPreferences.setPreviousMinutesTwo(timerMinute);

            resetTimerInputValues();

            setInputText();
        } else if (timerTwoActive && timerTwoPaused) {
            timerResume();
        }

        if (timerThreeActive && !timerThreeRunning && !timerThreePaused && (timerHour > 0 || timerMinute > 0)) {
            setClockViewThreeVis();

            millisToCountThree = convertMillis();
            countDownTimerThree = new CountTimer(millisToCountThree, 3);
            hms = timerFormat(millisToCountThree);
            clockView3.setText(hms);
            timerPreferences.setStartTimeThree(getNow());
            countDownTimerThree.start();
//            Log.v(TAG, "Timer Three Start");
            timerThreeRunning = true;

            timerPreferences.setPreviousHoursThree(timerHour);
            timerPreferences.setPreviousMinutesThree(timerMinute);

            resetTimerInputValues();

            setInputText();
        } else if (timerThreeActive && timerThreePaused) {
            timerResume();
        }

        if (timerFourActive && !timerFourRunning && !timerFourPaused && (timerHour > 0 || timerMinute > 0)) {
            setClockViewFourVis();

            millisToCountFour = convertMillis();
            countDownTimerFour = new CountTimer(millisToCountFour, 4);
            hms = timerFormat(millisToCountFour);
            clockView4.setText(hms);
            timerPreferences.setStartTimeFour(getNow());
            countDownTimerFour.start();
//            Log.v(TAG, "Timer Four Start");
            timerFourRunning = true;

            timerPreferences.setPreviousHoursFour(timerHour);
            timerPreferences.setPreviousMinutesFour(timerMinute);

            resetTimerInputValues();

            setInputText();
        } else if (timerFourActive && timerFourPaused) {
            timerResume();
        }
    }

    public void stopButton(View stop_button) {
        if (timerRunning || timerTwoRunning || timerThreeRunning || timerFourRunning) {
            timerPause();
        } else if (timerAlarm) {
            stopAlarm();
            timerAlarm = false;
        }
    }

    public void resetButton(View reset_button) {
        if (timerOneActive && (timerPaused || !timerRunning)) {
            resetTimerOne();
        }

        if (timerTwoActive && (timerTwoPaused || !timerTwoRunning)) {
            resetTimerTwo();
        }

        if (timerThreeActive && (timerThreePaused || !timerThreeRunning)) {
            resetTimerThree();
        }

        if (timerFourActive && (timerFourPaused || !timerFourRunning)) {
            resetTimerFour();
        }
    }

    private void timerPause() {
        if (timerRunning && timerOneActive) {
            countDownTimer.cancel();
            countDownTimer = null;
            timerPaused = true;
            timerRunning = false;
//            Log.v(TAG, "timerPause");
        }

        if (timerTwoRunning && timerTwoActive) {
            countDownTimerTwo.cancel();
            countDownTimerTwo = null;
            timerTwoPaused = true;
            timerTwoRunning = false;
//            Log.v(TAG, "timerTwoPause");
        }

        if (timerThreeRunning && timerThreeActive) {
            countDownTimerThree.cancel();
            countDownTimerThree = null;
            timerThreePaused = true;
            timerThreeRunning = false;
//            Log.v(TAG, "timerThreePause");
        }

        if (timerFourRunning && timerFourActive) {
            countDownTimerFour.cancel();
            countDownTimerFour = null;
            timerFourPaused = true;
            timerFourRunning = false;
//            Log.v(TAG, "timerFourPause");
        }
    }

    private void timerResume() {
//        Log.v(TAG, "pausedTime = " + pausedTime);

        if (timerPaused && timerOneActive) {
            countDownTimer = new CountTimer(pausedTime, 1);
            countDownTimer.start();
            timerPaused = false;
            timerRunning = true;
            timerPreferences.setTimerPaused(false);
//            Log.v(TAG, "timerResume");
        }

        if (timerTwoPaused && timerTwoActive) {
            countDownTimerTwo = new CountTimer(pausedTimeTwo, 2);
            countDownTimerTwo.start();
            timerTwoPaused = false;
            timerTwoRunning = true;
            timerPreferences.setTimerTwoPaused(false);
//            Log.v(TAG, "timerTwoResume");
        }

        if (timerThreePaused && timerThreeActive) {
            countDownTimerThree = new CountTimer(pausedTimeThree, 3);
            countDownTimerThree.start();
            timerThreePaused = false;
            timerThreeRunning = true;
            timerPreferences.setTimerThreePaused(false);
//            Log.v(TAG, "timerThreeResume");
        }

        if (timerFourPaused && timerFourActive) {
            countDownTimerFour = new CountTimer(pausedTimeFour, 4);
            countDownTimerFour.start();
            timerFourPaused = false;
            timerFourRunning = true;
            timerPreferences.setTimerFourPaused(false);
//            Log.v(TAG, "timerFourResume");
        }
    }

    private static long getNow() {return System.currentTimeMillis();}

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

        if (startTime > 0 && !timerPaused) {
//            Log.v(TAG, "MillisToCalc " + millisToCount);
//            Log.v(TAG, "startTime = " + startTime);
            pausedTime = (millisToCount - (getNow() - startTime));
//            Log.v(TAG, "MillisToCount " + millisToCount);
//            Log.v(TAG, "pausedTime " + pausedTime);
            if (pausedTime <= 0) {
                timerRunning = false;
                timerPaused = false;
            } else if (!timerPaused) {
                setClockViewOneVis();

                countDownTimer = new CountTimer(pausedTime, 1);
                countDownTimer.start();
                timerRunning = true;
                timerPaused = false;
            }
        } else if (startTime > 0 && timerPaused) {
            timerRunning = false;
            timerPaused = true;
        } else {
            timerRunning = false;
            timerPaused = false;
        }

        if (startTimeTwo > 0 && !timerTwoPaused) {
//            Log.v(TAG, "MillisToCalcTwo " + millisToCountTwo);
            pausedTimeTwo = (millisToCountTwo - (getNow() - startTimeTwo));
//            Log.v(TAG, "MillisToCountTwo " + millisToCountTwo);
//            Log.v(TAG, "pausedTimeTwo " + pausedTimeTwo);
            if (pausedTimeTwo <= 0) {
                timerTwoRunning = false;
                timerTwoPaused = false;
            } else if (!timerTwoPaused) {
                setClockViewTwoVis();

                countDownTimerTwo = new CountTimer(pausedTimeTwo, 2);
                countDownTimerTwo.start();
                timerTwoRunning = true;
                timerTwoPaused = false;
            }
        } else if (startTimeTwo > 0 && timerTwoPaused) {
            timerTwoRunning = false;
            timerTwoPaused = true;
        } else {
            timerTwoRunning = false;
            timerTwoPaused = false;
        }

        if (startTimeThree > 0 && !timerThreePaused) {
//            Log.v(TAG, "MillisToCalcThree " + millisToCountThree);
            pausedTimeThree = (millisToCountThree - (getNow() - startTimeThree));
//            Log.v(TAG, "MillisToCountThree " + millisToCountThree);
//            Log.v(TAG, "pausedTimeThree " + pausedTimeThree);
            if (pausedTimeThree <= 0) {
                timerThreeRunning = false;
                timerThreePaused = false;
            } else if (!timerThreePaused) {
                setClockViewThreeVis();

                countDownTimerThree = new CountTimer(pausedTimeThree, 3);
                countDownTimerThree.start();
                timerThreeRunning = true;
                timerThreePaused = false;
            }
        } else if (startTimeThree > 0 && timerThreePaused) {
            timerThreeRunning = false;
            timerThreePaused = true;
        } else {
            timerThreeRunning = false;
            timerThreePaused = false;
        }

        if (startTimeFour > 0 && !timerFourPaused) {
//            Log.v(TAG, "MillisToCalcFour " + millisToCountFour);
            pausedTimeFour = (millisToCountFour - (getNow() - startTimeFour));
//            Log.v(TAG, "MillisToCountFour " + millisToCountFour);
//            Log.v(TAG, "pausedTimeFour " + pausedTimeFour);
            if (pausedTimeFour <= 0) {
                timerFourRunning = false;
                timerFourPaused = false;
            } else if (!timerFourPaused) {
                setClockViewFourVis();

                countDownTimerFour = new CountTimer(pausedTimeFour, 4);
                countDownTimerFour.start();
                timerFourRunning = true;
                timerFourPaused = false;
            }
        } else if (startTimeFour > 0 && timerFourPaused) {
            timerFourRunning = false;
            timerFourPaused = true;
        } else {
            timerFourRunning = false;
            timerFourPaused = false;
        }
    }

    private void playAlarm() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        boolean vibrateSetting = timerPreferences.getVibrateSetting();
        long pattern[] = {200, 500, 500};
        final int volume = timerPreferences.getAlarmVolume();
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
        if (timerWarning) {
//            Log.v(TAG, "Play warning alarm");

            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

            audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

            boolean vibrateSetting = timerPreferences.getVibrateSetting();
            final int volume = timerPreferences.getAlarmVolume();
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

        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        Intent warningIntent = new Intent(this, WarningAlarmReceiver.class);

        if (millisToCount > 0) {
            intent.putExtra("timerNumberId", 1);
            PendingIntent sender = PendingIntent.getBroadcast(this, 1, intent, 0);
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(wakeUpTime, sender), sender);
//            Log.v(TAG, "Alarm Manager Set");

            if (timerWarning && millisToCount > warningAlarmStart) {
                long warningTime = wakeUpTime - warningAlarmStart;
                warningIntent.putExtra("playWarningAlarm", true);
                PendingIntent warningSender = PendingIntent.getBroadcast(this, 1, warningIntent, 0);
                am.setAlarmClock(new AlarmManager.AlarmClockInfo(warningTime, warningSender), warningSender);
            }
        }

        if (millisToCountTwo > 0) {
            intent.putExtra("timerNumberId", 2);
            PendingIntent senderTwo = PendingIntent.getBroadcast(this, 2, intent, 0);
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(wakeUpTimeTwo, senderTwo), senderTwo);
//            Log.v(TAG, "Alarm Manager Two Set");

            if (timerWarning && millisToCountTwo > warningAlarmStart) {
                long warningTimeTwo = wakeUpTimeTwo - warningAlarmStart;
                warningIntent.putExtra("playWarningAlarm", true);
                PendingIntent warningSenderTwo = PendingIntent.getBroadcast(this, 2, warningIntent, 0);
                am.setAlarmClock(new AlarmManager.AlarmClockInfo(warningTimeTwo, warningSenderTwo), warningSenderTwo);
            }
        }

        if (millisToCountThree > 0) {
            intent.putExtra("timerNumberId", 3);
            PendingIntent senderThree = PendingIntent.getBroadcast(this, 3, intent, 0);
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(wakeUpTimeThree, senderThree), senderThree);
//            Log.v(TAG, "Alarm Manager Three Set");

            if (timerWarning && millisToCountThree > warningAlarmStart) {
                long warningTimeThree = wakeUpTimeThree - warningAlarmStart;
                warningIntent.putExtra("playWarningAlarm", true);
                PendingIntent warningSenderThree = PendingIntent.getBroadcast(this, 3, warningIntent, 0);
                am.setAlarmClock(new AlarmManager.AlarmClockInfo(warningTimeThree, warningSenderThree), warningSenderThree);
            }
        }

        if (millisToCountFour > 0) {
            intent.putExtra("timerNumberId", 4);
            PendingIntent senderFour = PendingIntent.getBroadcast(this, 4, intent, 0);
            am.setAlarmClock(new AlarmManager.AlarmClockInfo(wakeUpTimeFour, senderFour), senderFour);
//            Log.v(TAG, "Alarm Manager Four Set");

            if (timerWarning && millisToCountFour > warningAlarmStart) {
                long warningTimeFour = wakeUpTimeFour - warningAlarmStart;
                warningIntent.putExtra("playWarningAlarm", true);
                PendingIntent warningSenderFour = PendingIntent.getBroadcast(this, 4, warningIntent, 0);
                am.setAlarmClock(new AlarmManager.AlarmClockInfo(warningTimeFour, warningSenderFour), warningSenderFour);
            }
        }
    }

    private void removeAlarmManager() {
        AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(this, AlarmReceiver.class);
        Intent warningIntent = new Intent(this, WarningAlarmReceiver.class);

        intent.getIntExtra("timerNumberId", 1);
        PendingIntent sender = PendingIntent.getBroadcast(this, 1, intent, 0);

        warningIntent.getBooleanExtra("playWarningAlarm", true);
        PendingIntent warningSender = PendingIntent.getBroadcast(this, 1, warningIntent, 0);

        intent.getIntExtra("timerNumberId", 2);
        PendingIntent senderTwo = PendingIntent.getBroadcast(this, 2, intent, 0);

        warningIntent.getBooleanExtra("playWarningAlarm", true);
        PendingIntent warningSenderTwo = PendingIntent.getBroadcast(this, 2, warningIntent, 0);

        intent.getIntExtra("timerNumberId", 3);
        PendingIntent senderThree = PendingIntent.getBroadcast(this, 3, intent, 0);

        warningIntent.getBooleanExtra("playWarningAlarm", true);
        PendingIntent warningSenderThree = PendingIntent.getBroadcast(this, 3, warningIntent, 0);

        intent.getIntExtra("timerNumberId", 4);
        PendingIntent senderFour = PendingIntent.getBroadcast(this, 4, intent, 0);

        warningIntent.getBooleanExtra("playWarningAlarm", true);
        PendingIntent warningSenderFour = PendingIntent.getBroadcast(this, 4, warningIntent, 0);

        am.cancel(sender);
        am.cancel(senderTwo);
        am.cancel(senderThree);
        am.cancel(senderFour);

        am.cancel(warningSender);
        am.cancel(warningSenderTwo);
        am.cancel(warningSenderThree);
        am.cancel(warningSenderFour);

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
        final int timerNumber;

        CountTimer(long millisInFuture, int timerNumberInput) {
            super(millisInFuture, MainActivity.countdownInterval);

            timerNumber = timerNumberInput;
        }

        @Override
        public void onTick(long countTimerPausedTime) {
            if (timerNumber == 1) {
//                Log.v(TAG, "TICK  " + countTimerPausedTime);
                pausedTime = countTimerPausedTime;
                hms = timerFormat(countTimerPausedTime);
                clockView1.setText(hms);
            } else if (timerNumber == 2) {
//                Log.v(TAG, "TICK TWO " + countTimerPausedTime);
                pausedTimeTwo = countTimerPausedTime;
                hms = timerFormat(countTimerPausedTime);
                clockView2.setText(hms);
            } else if (timerNumber == 3) {
//                Log.v(TAG, "TICK THREE " + countTimerPausedTime);
                pausedTimeThree = countTimerPausedTime;
                hms = timerFormat(countTimerPausedTime);
                clockView3.setText(hms);
            } else if (timerNumber == 4) {
//                Log.v(TAG, "TICK FOUR " + countTimerPausedTime);
                pausedTimeFour = countTimerPausedTime;
                hms = timerFormat(countTimerPausedTime);
                clockView4.setText(hms);
            }

            if (timerWarning) {
                if ((pausedTime < warningAlarmStart && pausedTime > warningAlarmEnd) ||
                        (pausedTimeTwo < warningAlarmStart && pausedTimeTwo > warningAlarmEnd) ||
                        (pausedTimeThree < warningAlarmStart && pausedTimeThree > warningAlarmEnd) ||
                        (pausedTimeFour < warningAlarmStart && pausedTimeFour > warningAlarmEnd)) {
                    playWarningAlarm();
                    timerWarning = false;
                }
            }
        }

        @Override
        public void onFinish() {
            if (!timerAlarm) {
                playAlarm();
            }

            if (timerNumber == 1) {
//                Log.v(TAG, "Timer One Finished");
                countDownTimer.cancel();
                countDownTimer = null;
                timerRunning = false;
                timerPaused = false;
                millisToCount = 0;

                timerPreferences.setTimerOneRunning(false);
                timerPreferences.setStartTime(0);
                timerPreferences.setOriginalTime(0);
                timerPreferences.setTimerPaused(false);
                timerPreferences.setPausedTime(0);

            } else if (timerNumber == 2) {
//                Log.v(TAG, "Timer Two Finished");
                countDownTimerTwo.cancel();
                countDownTimerTwo = null;
                timerTwoRunning = false;
                timerTwoPaused = false;
                millisToCountTwo = 0;

                timerPreferences.setTimerTwoRunning(false);
                timerPreferences.setStartTimeTwo(0);
                timerPreferences.setOriginalTimeTwo(0);
                timerPreferences.setTimerTwoPaused(false);
                timerPreferences.setPausedTimeTwo(0);

            } else if (timerNumber == 3) {
//                Log.v(TAG, "Timer Three Finished");
                countDownTimerThree.cancel();
                countDownTimerThree = null;
                timerThreeRunning = false;
                timerThreePaused = false;
                millisToCountThree = 0;

                timerPreferences.setTimerThreeRunning(false);
                timerPreferences.setStartTimeThree(0);
                timerPreferences.setOriginalTimeThree(0);
                timerPreferences.setTimerThreePaused(false);
                timerPreferences.setPausedTimeThree(0);

            } else if (timerNumber == 4) {
//                Log.v(TAG, "Timer Four Finished");
                countDownTimerFour.cancel();
                countDownTimerFour = null;
                timerFourRunning = false;
                timerFourPaused = false;
                millisToCountFour = 0;

                timerPreferences.setTimerFourRunning(false);
                timerPreferences.setStartTimeFour(0);
                timerPreferences.setOriginalTimeFour(0);
                timerPreferences.setTimerFourPaused(false);
                timerPreferences.setPausedTimeFour(0);

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

        MainActOnAudioFocusChangeListener(int vol) {volume = vol;}

        @Override
        public void onAudioFocusChange(int focusChange) {
            if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                // Permanent loss of audio focus
                // Stop playback immediately
//                Log.v(TAG, "Audio focus lost");
                stopAlarm();
                timerAlarm = false;
            } else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT) {
                // Pause playback
//                Log.v(TAG, "Audio focus lost transient");
                alarmPlayer.pause();
            } else if (focusChange == AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
                // Lower the volume, keep playing
//                Log.v(TAG, "Audio focus lost transient can duck");
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 3, 0);
            } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                // Your app has been granted audio focus again
                // Raise volume to normal, restart playback if necessary
//                Log.v(TAG, "Audio focus gained");
                if (audioManager != null) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
                }
            }
        }
    }

    /**
     * Inner class to listen for alarm to complete and the stop alarm and return volume to previous level
     */
    private class WarningAlarmCompletionListener implements MediaPlayer.OnCompletionListener {
        @Override
        public void onCompletion(MediaPlayer mp) {
//            Log.v(TAG, "Warning alarm complete");
            stopAlarm();
            timerWarning = true;
        }
    }

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
        timerRunning = false;
        timerPaused = false;

        resetTimerInputValues();

        millisToCount = 0;
        pausedTime = 0;

        timerPreferences.setStartTime(0);
        timerPreferences.setOriginalTime(0);
        timerPreferences.setTimerOneRunning(false);
        timerPreferences.setTimerPaused(false);
        timerPreferences.setPausedTime(0);

        setInputVis();

        setInputText();
    }

    private void resetTimerTwo() {
//        Log.v(TAG, "Reset Timer Two");
        countDownTimerTwo = null;
        timerTwoRunning = false;
        timerTwoPaused = false;

        resetTimerInputValues();

        millisToCountTwo = 0;
        pausedTimeTwo = 0;

        timerPreferences.setStartTimeTwo(0);
        timerPreferences.setOriginalTimeTwo(0);
        timerPreferences.setTimerTwoRunning(false);
        timerPreferences.setTimerTwoPaused(false);
        timerPreferences.setPausedTimeTwo(0);

        setInputVis();

        setInputText();
    }

    private void resetTimerThree() {
//        Log.v(TAG, "Reset Timer Four");
        countDownTimerThree = null;
        timerThreeRunning = false;
        timerThreePaused = false;

        resetTimerInputValues();

        millisToCountThree = 0;
        pausedTimeThree = 0;

        timerPreferences.setStartTimeThree(0);
        timerPreferences.setOriginalTimeThree(0);
        timerPreferences.setTimerThreeRunning(false);
        timerPreferences.setTimerThreePaused(false);
        timerPreferences.setPausedTimeThree(0);

        setInputVis();

        setInputText();
    }

    private void resetTimerFour() {
//        Log.v(TAG, "Reset Timer Four");
        countDownTimerFour = null;
        timerFourRunning = false;
        timerFourPaused = false;

        resetTimerInputValues();

        millisToCountFour = 0;
        pausedTimeFour = 0;

        timerPreferences.setStartTimeFour(0);
        timerPreferences.setOriginalTimeFour(0);
        timerPreferences.setTimerFourRunning(false);
        timerPreferences.setTimerFourPaused(false);
        timerPreferences.setPausedTimeFour(0);

        setInputVis();

        setInputText();
    }

    public void openSettings(View view) {
        Intent intent = new Intent(this, AppPreferenceActivity.class);
        startActivity(intent);
    }
}