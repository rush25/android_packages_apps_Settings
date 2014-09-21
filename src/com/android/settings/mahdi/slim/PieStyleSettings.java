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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SlimSeekBarPreference;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class PieStyleSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "PieStyleSettings";
    private static final String PREF_PIE_BACKGROUND_COLOR = "pie_background_color";
    private static final String PREF_PIE_SNAP_COLOR = "pie_snap_color";
    private static final String PREF_PIE_TEXT_COLOR = "pie_text_color";
    private static final String PREF_PIE_BACKGROUND_ALPHA = "pie_background_alpha";
    private static final String PREF_PIE_BACKGROUND_FADEIN_TIME = "pie_background_fadein_time";
    private static final String PREF_PIE_MIRROR_RIGHT = "pie_mirror_right";
    private static final String PREF_PIE_SHOW_TEXT = "pie_show_text";
    private static final String PREF_PIE_SHOW_BACKGROUND = "pie_show_background";
    private static final String PREF_PIE_SHOW_SNAP = "pie_show_snap";
    private static final String PREF_PIE_SLICE_GAP = "pie_slice_gap";

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    Resources mSystemUiResources;
    private boolean mCheckPreferences;

    private CheckBoxPreference mShowBackground;
    private SlimSeekBarPreference mPieBackgroundAlpha;
    private ColorPickerPreference mPieBackgroundColor;
    private SlimSeekBarPreference mPieBackgroundFadeinTime;
    private CheckBoxPreference mShowSnap;
    private CheckBoxPreference mShowText;
    private ColorPickerPreference mPieSnapColor;
    private ColorPickerPreference mPieTextColor;
    private ListPreference mPieSliceGap;
    private CheckBoxPreference mMirrorRightPie;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.pie_style);

        PreferenceScreen prefs = getPreferenceScreen();
        ContentResolver resolver = getContentResolver();
        PackageManager pm = getPackageManager();
        if (pm != null) {
            try {
                mSystemUiResources = pm.getResourcesForApplication("com.android.systemui");
            } catch (Exception e) {
                mSystemUiResources = null;
                Log.e("PIEStyle:", "can't access systemui resources",e);
            }
        }

        mShowBackground = (CheckBoxPreference) findPreference(PREF_PIE_SHOW_BACKGROUND);
        mShowBackground.setOnPreferenceChangeListener(this);

        mPieBackgroundAlpha = (SlimSeekBarPreference) findPreference(PREF_PIE_BACKGROUND_ALPHA);
        mPieBackgroundAlpha.setOnPreferenceChangeListener(this);

        mPieBackgroundColor = (ColorPickerPreference) findPreference(PREF_PIE_BACKGROUND_COLOR);
        mPieBackgroundColor.setOnPreferenceChangeListener(this);

        int pieBackgroundFadeinTime = Settings.System.getInt(resolver,
                Settings.System.PIE_TIME_FADEIN_DELAY, 500);
        mPieBackgroundFadeinTime = (SlimSeekBarPreference) findPreference(PREF_PIE_BACKGROUND_FADEIN_TIME);
        mPieBackgroundFadeinTime.setInterval(10);
        mPieBackgroundFadeinTime.setDefault(500);
        mPieBackgroundFadeinTime.minimumValue(100);
        mPieBackgroundFadeinTime.multiplyValue(10);
        mPieBackgroundFadeinTime.isMilliseconds(true);
        mPieBackgroundFadeinTime.setInitValue(pieBackgroundFadeinTime);
        mPieBackgroundFadeinTime.setOnPreferenceChangeListener(this);

        mShowSnap = (CheckBoxPreference) findPreference(PREF_PIE_SHOW_SNAP);
        mShowSnap.setOnPreferenceChangeListener(this);

        mShowText = (CheckBoxPreference) findPreference(PREF_PIE_SHOW_TEXT);
        mShowText.setOnPreferenceChangeListener(this);

        mPieSnapColor = (ColorPickerPreference) findPreference(PREF_PIE_SNAP_COLOR);
        mPieSnapColor.setOnPreferenceChangeListener(this);

        mPieTextColor = (ColorPickerPreference) findPreference(PREF_PIE_TEXT_COLOR);
        mPieTextColor.setOnPreferenceChangeListener(this);

        mPieSliceGap =
            (ListPreference) findPreference(PREF_PIE_SLICE_GAP);
        int pieSliceGap = Settings.System.getInt(resolver,
                Settings.System.PIE_SLICE_GAP, 3);
        mPieSliceGap.setValue(String.valueOf(pieSliceGap));
        mPieSliceGap.setSummary(mPieSliceGap.getEntry());
        mPieSliceGap.setOnPreferenceChangeListener(this);

        mMirrorRightPie = (CheckBoxPreference) findPreference(PREF_PIE_MIRROR_RIGHT);
        mMirrorRightPie.setOnPreferenceChangeListener(this);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, MENU_RESET, 0, R.string.reset)
                .setIcon(R.drawable.ic_settings_backup) // use the backup icon
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_RESET:
                showDialogInner(DLG_RESET);
                return true;
             default:
                return super.onContextItemSelected(item);
        }
    }

    private void resetToDefault() {
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PIE_BACKGROUND_COLOR, -2);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PIE_SNAP_COLOR, -2);
        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PIE_TEXT_COLOR, -2);

        Settings.System.putFloat(getActivity().getContentResolver(),
                Settings.System.PIE_BACKGROUND_ALPHA, 0.3f);

        Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PIE_TIME_FADEIN_DELAY, 500);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (!mCheckPreferences) {
            return false;
        }
        if (preference == mShowBackground) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_SHOW_BACKGROUND, (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mPieBackgroundAlpha) {
            float val = Float.parseFloat((String) newValue);
            Log.e("R", "value: " + val / 100);
            Settings.System.putFloat(getActivity().getContentResolver(),
                    Settings.System.PIE_BACKGROUND_ALPHA,
                    val / 100);
            return true;
        } else if (preference == mPieBackgroundColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_BACKGROUND_COLOR, intHex);
            return true;
        } else if (preference == mPieBackgroundFadeinTime) {
            int value = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_TIME_FADEIN_DELAY, value);
            return true;
        } else if (preference == mShowText) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_SHOW_TEXT, (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mShowSnap) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_SHOW_SNAP, (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mPieTextColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_TEXT_COLOR, intHex);
            return true;
        } else if (preference == mPieSnapColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.PIE_SNAP_COLOR, intHex);
            return true;
        } else if (preference == mPieSliceGap) {
            int index = mPieSliceGap.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_SLICE_GAP, Integer.parseInt((String) newValue));
            mPieSliceGap.setSummary(mPieSliceGap.getEntries()[index]);
            return true;
        } else if (preference == mMirrorRightPie) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PIE_MIRROR_RIGHT,
                    (Boolean) newValue ? 1 : 0);
           return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateStyleValues();
    }

    private void updateStyleValues() {
        mCheckPreferences = false;
        String hexColor;
        int intColor;

        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.PIE_BACKGROUND_COLOR, -2);
        if (intColor == -2) {
            intColor = mSystemUiResources.getColor(
                    mSystemUiResources.getIdentifier(
                            "pie_overlay_color", "color", "com.android.systemui"));
            mPieBackgroundColor.setSummary(getResources().getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mPieBackgroundColor.setSummary(hexColor);
        }
        mPieBackgroundColor.setNewPreviewColor(intColor);

        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.PIE_SNAP_COLOR, -2);
        if (intColor == -2) {
            intColor = mSystemUiResources.getColor(
                    mSystemUiResources.getIdentifier(
                            "pie_snap_color", "color", "com.android.systemui"));
            mPieSnapColor.setSummary(getResources().getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mPieSnapColor.setSummary(hexColor);
        }
        mPieSnapColor.setNewPreviewColor(intColor);

        intColor = Settings.System.getInt(getActivity().getContentResolver(),
                    Settings.System.PIE_TEXT_COLOR, -2);
        if (intColor == -2) {
            intColor = mSystemUiResources.getColor(
                    mSystemUiResources.getIdentifier(
                            "pie_text_color", "color", "com.android.systemui"));
            mPieTextColor.setSummary(getResources().getString(R.string.default_string));
        } else {
            hexColor = String.format("#%08x", (0xffffffff & intColor));
            mPieTextColor.setSummary(hexColor);
        }
        mPieTextColor.setNewPreviewColor(intColor);

        mShowSnap.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_SHOW_SNAP, 1) == 1);

        mShowText.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_SHOW_TEXT, 1) == 1);

        mShowBackground.setChecked(Settings.System.getInt(getContentResolver(),
                Settings.System.PIE_SHOW_BACKGROUND, 1) == 1);

        mMirrorRightPie.setChecked(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.PIE_MIRROR_RIGHT, 1) == 1);

        float defaultAlpha;
        try{
            defaultAlpha = Settings.System.getFloat(getActivity()
                     .getContentResolver(), Settings.System.PIE_BACKGROUND_ALPHA);
        } catch (Exception e) {
            defaultAlpha = 0.3f;
            Settings.System.putFloat(getActivity().getContentResolver(),
                Settings.System.PIE_BACKGROUND_ALPHA, defaultAlpha);
        }
        mPieBackgroundAlpha.setInitValue((int) (defaultAlpha * 100));

        int defaultDelay;
        try{
            defaultDelay = Settings.System.getInt(getActivity()
                     .getContentResolver(), Settings.System.PIE_TIME_FADEIN_DELAY);
        } catch (Exception e) {
            defaultDelay = 500;
            Settings.System.putInt(getActivity().getContentResolver(),
                Settings.System.PIE_TIME_FADEIN_DELAY, defaultDelay);
        }
        mPieBackgroundFadeinTime.setInitValue((int) (defaultDelay / 11) - 4);

        mCheckPreferences = true;
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        PieStyleSettings getOwner() {
            return (PieStyleSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.pie_style_reset_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            getOwner().resetToDefault();
                            getOwner().updateStyleValues();
                        }
                    })
                    .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {

        }
    }
}
