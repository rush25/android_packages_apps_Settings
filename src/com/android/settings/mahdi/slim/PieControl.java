/*
 * Copyright (C) 2014 Slimroms
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.mahdi.slim;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.preference.SlimSeekBarPreference;
import android.provider.Settings;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PieControl extends SettingsPreferenceFragment
        implements Preference.OnPreferenceChangeListener {

    private static final String PIE_CONTROL = "pie_control";
    private static final String PIE_MENU = "pie_menu";
    private static final String PREF_PIE_CONTROL_SIZE = "pie_control_size";

    private static final float PIE_CONTROL_SIZE_MIN = 0.6f;
    private static final float PIE_CONTROL_SIZE_MAX = 1.5f;
    private static final float PIE_CONTROL_SIZE_DEFAULT = 0.97f;

    // This equals EdgeGesturePosition.LEFT.FLAG
    private static final int DEFAULT_POSITION = 1 << 0;

    private static final String PREF_PIE_DISABLE_IME_TRIGGERS = "pie_disable_ime_triggers";

    private static final String[] TRIGGER = {
        "pie_control_trigger_left",
        "pie_control_trigger_bottom",
        "pie_control_trigger_right",
        "pie_control_trigger_top"
    };

    private CheckBoxPreference mPieControl;
    private ListPreference mPieMenuDisplay;
    private SlimSeekBarPreference mPieControlSize;

    private CheckBoxPreference[] mTrigger = new CheckBoxPreference[4];
    private CheckBoxPreference mDisableImeTriggers;

    private ContentObserver mPieTriggerObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange) {
            updatePieTriggers();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pie_control);

        PreferenceScreen prefSet = getPreferenceScreen();

        mPieControl = (CheckBoxPreference) prefSet.findPreference(PIE_CONTROL);
        mPieControl.setOnPreferenceChangeListener(this);

        mPieMenuDisplay = (ListPreference) prefSet.findPreference(PIE_MENU);
        mPieMenuDisplay.setSummary(mPieMenuDisplay.getEntry());
        mPieMenuDisplay.setOnPreferenceChangeListener(this);

        mPieControlSize = (SlimSeekBarPreference) findPreference(PREF_PIE_CONTROL_SIZE);
        mPieControlSize.setOnPreferenceChangeListener(this);

        for (int i = 0; i < TRIGGER.length; i++) {
            mTrigger[i] = (CheckBoxPreference) findPreference(TRIGGER[i]);
            mTrigger[i].setOnPreferenceChangeListener(this);
        }

        mDisableImeTriggers = (CheckBoxPreference) findPreference(PREF_PIE_DISABLE_IME_TRIGGERS);
        mDisableImeTriggers.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        int triggerSlots = 0;
        int counter = 0;
        if (preference == mPieControl) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_CONTROLS, (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mPieMenuDisplay) {
            int index = mPieMenuDisplay.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_MENU, Integer.parseInt((String) newValue));
            mPieMenuDisplay.setSummary(mPieMenuDisplay.getEntries()[index]);
            return true;
        } else if (preference == mPieControlSize) {
            float val = Float.parseFloat((String) newValue);
            float value = (val * ((PIE_CONTROL_SIZE_MAX - PIE_CONTROL_SIZE_MIN) /
                100)) + PIE_CONTROL_SIZE_MIN;
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_SIZE,
                    value);
            return true;
        } else if (preference == mDisableImeTriggers) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_IME_CONTROL,
                    (Boolean) newValue ? 1 : 0);
           return true;
        } else {
            for (int i = 0; i < mTrigger.length; i++) {
                boolean checked = preference == mTrigger[i]
                    ? (Boolean) newValue : mTrigger[i].isChecked();
                if (checked) {
                    if (!TRIGGER[i].equals("pie_control_trigger_top")) {
                        counter++;
                    }
                    triggerSlots |= 1 << i;
                }
            }
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_GRAVITY, triggerSlots);
            updatePieTriggers();
            return true;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mPieMenuDisplay.setValue(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_MENU,
                2) + "");
        mPieControl.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_CONTROLS, 0) == 1);

        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.PIE_GRAVITY), true,
                mPieTriggerObserver);
        updatePieTriggers();
        updateStyleValues();
    }

    @Override
    public void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(mPieTriggerObserver);
    }

    private void updatePieTriggers() {
        int triggerSlots = Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_GRAVITY, DEFAULT_POSITION);

        for (int i = 0; i < mTrigger.length; i++) {
            if ((triggerSlots & (0x01 << i)) != 0) {
                mTrigger[i].setChecked(true);
            } else {
                mTrigger[i].setChecked(false);
            }
        }

        mDisableImeTriggers.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_IME_CONTROL, 1) == 1);
    }

    private void updateStyleValues() {

        float controlSize;
        try{
            controlSize = Settings.System.getFloat(getActivity()
                    .getContentResolver(), Settings.System.PIE_SIZE);
        } catch (Exception e) {
            controlSize = PIE_CONTROL_SIZE_DEFAULT;
            Settings.System.putFloat(getActivity().getContentResolver(),
                Settings.System.PIE_SIZE, controlSize);
        }
        float controlSizeValue = ((controlSize - PIE_CONTROL_SIZE_MIN) /
                    ((PIE_CONTROL_SIZE_MAX - PIE_CONTROL_SIZE_MIN) / 100)) / 100;
        mPieControlSize.setInitValue((int) (controlSizeValue * 100));
        mPieControlSize.disableText(false);
    }
}
