package com.professionalkitchentimer.iinewmanii.professionalkitchentimer;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

//import android.util.Log;

/**
 * Created by IINEWMANII on 1/28/2017.
 * The settings activity.
 */

public class AppPreferenceActivity extends PreferenceActivity {
//    private static final String TAG = "NEWMAN";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    public static class SettingsFragment extends PreferenceFragment implements OnSeekBarChangeListener, CompoundButton.OnCheckedChangeListener {

        private PrefUtils timerPreferences;

        private static final int minValue = 1;
        private int alarmVolume;
        private int warningAlarmMinute;
        private TextView seekBarValue;
        private TextView warningAlarmSeekBarValue;
        private TextView versionNumberText;
        private SeekBar alarmVolumeSeekBar;
        private SeekBar warningAlarmTimeSeekBar;
        private AudioManager audioManager;
        private CheckBox keepScreenOnCheckbox;
        private CheckBox vibrateCheckbox;
        private CheckBox warningAlarmCheckbox;

        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);

            timerPreferences = new PrefUtils(getActivity());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

            View preferenceView = inflater.inflate(R.layout.preferences, container, false);

            alarmVolumeSeekBar = (SeekBar) preferenceView.findViewById(R.id.alarm_volume_seek_bar);
            warningAlarmTimeSeekBar = (SeekBar) preferenceView.findViewById(R.id.warning_alarm_volume_seek_bar);
            audioManager = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
            seekBarValue = (TextView) preferenceView.findViewById(R.id.seek_bar_value);
            warningAlarmSeekBarValue = (TextView) preferenceView.findViewById(R.id.warning_alarm_value);
            versionNumberText = (TextView) preferenceView.findViewById(R.id.version_number);
            keepScreenOnCheckbox = (CheckBox) preferenceView.findViewById(R.id.keep_screen_on_checkbox);
            vibrateCheckbox = (CheckBox) preferenceView.findViewById(R.id.vibrate_checkbox);
            warningAlarmCheckbox = (CheckBox) preferenceView.findViewById(R.id.warning_alarm_checkbox);

            alarmVolume = timerPreferences.getAlarmVolume();
            warningAlarmMinute = timerPreferences.getWarningAlarmMinute();

            alarmVolumeSeekBar.setMax(audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            alarmVolumeSeekBar.setProgress(alarmVolume);
            seekBarValue.setText(String.valueOf(alarmVolume));

            warningAlarmTimeSeekBar.setMax(30);
            warningAlarmTimeSeekBar.setProgress(warningAlarmMinute);
            warningAlarmSeekBarValue.setText(String.valueOf(warningAlarmMinute));

            alarmVolumeSeekBar.setOnSeekBarChangeListener(this);
            warningAlarmTimeSeekBar.setOnSeekBarChangeListener(this);
            keepScreenOnCheckbox.setOnCheckedChangeListener(this);
            vibrateCheckbox.setOnCheckedChangeListener(this);
            warningAlarmCheckbox.setOnCheckedChangeListener(this);

            versionNumberText.setText(String.format(" %-1s", BuildConfig.VERSION_NAME));

            return preferenceView;
        }

        @Override
        public void onStart() {

            super.onStart();
//            Log.v(TAG, "onStart AppPref");

            boolean checkKeepScreenOnCheckbox = timerPreferences.getKeepScreenOn();
            boolean checkVibrateCheckbox = timerPreferences.getVibrateSetting();
            boolean checkWarningAlarmCheckbox = timerPreferences.getWarningAlarm();

//            Log.v(TAG, "checkKeepScreenOnCheckbox = " + checkKeepScreenOnCheckbox);

            if (checkKeepScreenOnCheckbox) {
                keepScreenOnCheckbox.setChecked(true);
            } else {
                keepScreenOnCheckbox.setChecked(false);
            }

            if (checkVibrateCheckbox) {
                vibrateCheckbox.setChecked(true);
            } else {
                vibrateCheckbox.setChecked(false);
            }

            if (checkWarningAlarmCheckbox) {
                warningAlarmCheckbox.setChecked(true);
            } else {
                warningAlarmCheckbox.setChecked(false);
            }
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            Log.v(TAG, "Progress Changed");

            if (seekBar == alarmVolumeSeekBar) {
                alarmVolume = progress;
                if (alarmVolume < minValue) {
                    alarmVolume = minValue;
                }

                alarmVolumeSeekBar.setProgress(alarmVolume);
                seekBarValue.setText(String.valueOf(alarmVolume));
            }

            if (seekBar == warningAlarmTimeSeekBar) {
                warningAlarmMinute = progress;
                if (warningAlarmMinute < minValue) {
                    warningAlarmMinute = minValue;
                }

                warningAlarmTimeSeekBar.setProgress(warningAlarmMinute);
                warningAlarmSeekBarValue.setText(String.valueOf(warningAlarmMinute));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
//            Log.v(TAG, "Start Tracking Touch");
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
//            Log.v(TAG, "Stop Tracking Touch");

            if (seekBar == alarmVolumeSeekBar) {
                timerPreferences.setAlarmVolume(alarmVolume);
            }

            if (seekBar == warningAlarmTimeSeekBar) {
                timerPreferences.setWarningAlarmMinute(warningAlarmMinute);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (isChecked) {
                if (buttonView == keepScreenOnCheckbox) {
                    timerPreferences.setKeepScreenOn(true);
                }

                if (buttonView == vibrateCheckbox) {
                    timerPreferences.setVibrateSetting(true);
                }

                if (buttonView == warningAlarmCheckbox) {
                    timerPreferences.setWarningAlarm(true);
                }
            } else {
                if (buttonView == keepScreenOnCheckbox) {
                    timerPreferences.setKeepScreenOn(false);
                }

                if (buttonView == vibrateCheckbox) {
                    timerPreferences.setVibrateSetting(false);
                }

                if (buttonView == warningAlarmCheckbox) {
                    timerPreferences.setWarningAlarm(false);
                }
            }
        }
    }
}