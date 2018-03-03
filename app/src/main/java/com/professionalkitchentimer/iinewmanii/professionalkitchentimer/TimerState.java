package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by iinewmanii on 2/26/18.
 * This is a class to handle the state of the timers.
 */

class TimerState {
    /* This is an IntDef to keep track of which timer is the active timer. */
    @Retention(RetentionPolicy.SOURCE)

    @IntDef({TIMER_ONE,
            TIMER_TWO,
            TIMER_THREE,
            TIMER_FOUR,
            NO_ACTIVE_TIMER})

    @interface TimerActive {}

    static final int NO_ACTIVE_TIMER = 0;
    static final int TIMER_ONE = 1;
    static final int TIMER_TWO = 2;
    static final int TIMER_THREE = 3;
    static final int TIMER_FOUR = 4;

    private int activeTimer;

    /* These are IntDefs to keep track of timer state */
    @Retention(RetentionPolicy.SOURCE)

    @IntDef({INPUT, RUNNING, PAUSED})
    @interface TimerOneState {}

    @IntDef({INPUT, RUNNING, PAUSED})
    @interface TimerTwoState {}

    @IntDef({INPUT, RUNNING, PAUSED})
    @interface TimerThreeState {}

    @IntDef({INPUT, RUNNING, PAUSED})
    @interface TimerFourState {}

    static final int INPUT = 0;
    static final int RUNNING = 1;
    static final int PAUSED = 2;

    private int timerOneState;
    private int timerTwoState;
    private int timerThreeState;
    private int timerFourState;

    /* Methods to get and set active timer */
    @TimerActive
    int getActiveTimer() {return activeTimer;}

    void setActiveTimer(@TimerActive int activeTimer) {
        this.activeTimer = activeTimer;
    }

    /* Methods to get and set timer state */
    int getTimerOneState() {return timerOneState;}

    void setTimerOneState(@TimerOneState int timerOneState) { this.timerOneState = timerOneState; }

    int getTimerTwoState() {return timerTwoState;}

    void setTimerTwoState(@TimerTwoState int timerTwoState) { this.timerTwoState = timerTwoState; }

    int getTimerThreeState() {return timerThreeState;}

    void setTimerThreeState(@TimerThreeState int timerThreeState) { this.timerThreeState = timerThreeState; }

    int getTimerFourState() {return timerFourState;}

    void setTimerFourState(@TimerFourState int timerFourState) { this.timerFourState = timerFourState; }
}
