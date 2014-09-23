/*
 * Copyright (C) 2012 Slimroms
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

package com.android.settings.mahdi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.android.internal.util.mahdi.DeviceUtils;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;

public class NavbarSettings extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "NavBarSettings";

    private static final String CATEGORY_DIMENS = "dimens_cat";
    private static final String CATEGORY_ADVANCED = "advanced_cat";
    private static final String PREF_NAVIGATION_BAR_HEIGHT = "navigation_bar_height";
    private static final String PREF_NAVIGATION_BAR_HEIGHT_LANDSCAPE = "navigation_bar_height_landscape";
    private static final String PREF_NAVIGATION_BAR_WIDTH ="navigation_bar_width";
    private static final String KEY_DIMEN_OPTIONS = "navbar_dimen";
    private static final String PREF_MENU_LOCATION = "pref_navbar_menu_location";
    private static final String PREF_NAVBAR_MENU_DISPLAY = "pref_navbar_menu_display";
    private static final String ENABLE_NAVIGATION_BAR = "enable_nav_bar";
    private static final String PREF_BUTTON = "navbar_button_settings";
    private static final String PREF_RING = "navbar_targets_settings";
    private static final String PREF_STYLE_DIMEN = "navbar_style_dimen_settings";
    private static final String PREF_NAVIGATION_BAR_CAN_MOVE = "navbar_can_move";
    private static final String EMULATE_MENU_KEY = "emulate_menu_key";
    private static final String KEY_NAVIGATION_BAR_LEFT = "navigation_bar_left";

    private static final int MENU_RESET = Menu.FIRST;
    private static final int DLG_RESET = 0;

    private ContentResolver mResolver;
    private int mNavBarMenuDisplayValue;

    ListPreference mNavigationBarHeight;
    ListPreference mNavigationBarHeightLandscape;
    ListPreference mNavigationBarWidth;
    ListPreference mMenuDisplayLocation;
    ListPreference mNavBarMenuDisplay;
    CheckBoxPreference mEnableNavigationBar;
    CheckBoxPreference mNavigationBarCanMove;
    PreferenceScreen mButtonPreference;
    PreferenceScreen mRingPreference;
    PreferenceScreen mStyleDimenPreference;
    CheckBoxPreference mEmulateMenuKey;
    CheckBoxPreference mNavigationBarLeftPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.navbar_settings);

        PreferenceScreen prefs = getPreferenceScreen();
        Resources res = getResources();
        Context context = getActivity();
        mResolver = context.getContentResolver();

        mNavigationBarHeight =
            (ListPreference) findPreference(PREF_NAVIGATION_BAR_HEIGHT);
        int navigationBarHeight = Settings.System.getInt(mResolver,
                Settings.System.NAVIGATION_BAR_HEIGHT, 48);
        mNavigationBarHeight.setValue(String.valueOf(navigationBarHeight));
        mNavigationBarHeight.setSummary(mNavigationBarHeight.getEntry());
        mNavigationBarHeight.setOnPreferenceChangeListener(this);

        mNavigationBarHeightLandscape =
            (ListPreference) findPreference(PREF_NAVIGATION_BAR_HEIGHT_LANDSCAPE);
        int navigationBarHeightLandscape = Settings.System.getInt(mResolver,
                Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, 48);
        mNavigationBarHeightLandscape.setValue(String.valueOf(navigationBarHeightLandscape));
        mNavigationBarHeightLandscape.setSummary(mNavigationBarHeightLandscape.getEntry());
        mNavigationBarHeightLandscape.setOnPreferenceChangeListener(this);

        mNavigationBarWidth =
            (ListPreference) findPreference(PREF_NAVIGATION_BAR_WIDTH);
        int navigationBarWidth = Settings.System.getInt(mResolver,
                Settings.System.NAVIGATION_BAR_WIDTH, 42);
        if (!DeviceUtils.isPhone(getActivity())) {
            PreferenceCategory dimens_cat =
                (PreferenceCategory) findPreference(CATEGORY_DIMENS);
            dimens_cat.removePreference(mNavigationBarWidth);
            mNavigationBarWidth = null;
        } else {
            mNavigationBarWidth.setValue(String.valueOf(navigationBarWidth));
            mNavigationBarWidth.setSummary(mNavigationBarWidth.getEntry());
            mNavigationBarWidth.setOnPreferenceChangeListener(this);
        }

        mMenuDisplayLocation = (ListPreference) findPreference(PREF_MENU_LOCATION);
        mMenuDisplayLocation.setValue(Settings.System.getInt(mResolver,
                Settings.System.MENU_LOCATION, 0) + "");
        mMenuDisplayLocation.setSummary(mMenuDisplayLocation.getEntry());
        mMenuDisplayLocation.setOnPreferenceChangeListener(this);

        mNavBarMenuDisplay = (ListPreference) findPreference(PREF_NAVBAR_MENU_DISPLAY);
        mNavBarMenuDisplayValue = Settings.System.getInt(mResolver, 
                Settings.System.MENU_VISIBILITY, 2);
        mNavBarMenuDisplay.setValue(mNavBarMenuDisplayValue + "");
        mNavBarMenuDisplay.setSummary(mNavBarMenuDisplay.getEntry());
        mNavBarMenuDisplay.setOnPreferenceChangeListener(this);

        mButtonPreference = (PreferenceScreen) findPreference(PREF_BUTTON);
        mRingPreference = (PreferenceScreen) findPreference(PREF_RING);
        mStyleDimenPreference = (PreferenceScreen) findPreference(PREF_STYLE_DIMEN);

        boolean hasNavBarByDefault = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        boolean enableNavigationBar = Settings.System.getInt(mResolver,
                Settings.System.NAVIGATION_BAR_SHOW, hasNavBarByDefault ? 1 : 0) == 1;
        mEnableNavigationBar = (CheckBoxPreference) findPreference(ENABLE_NAVIGATION_BAR);
        mEnableNavigationBar.setChecked(enableNavigationBar);
        mEnableNavigationBar.setOnPreferenceChangeListener(this);

        mNavigationBarCanMove = (CheckBoxPreference) findPreference(PREF_NAVIGATION_BAR_CAN_MOVE);
        mNavigationBarCanMove.setChecked(Settings.System.getInt(mResolver,
                Settings.System.NAVIGATION_BAR_CAN_MOVE, 1) == 0);
        mNavigationBarCanMove.setOnPreferenceChangeListener(this);

        mEmulateMenuKey = (CheckBoxPreference) findPreference(EMULATE_MENU_KEY);
        mEmulateMenuKey.setChecked(Settings.System.getInt(mResolver,
                Settings.System.EMULATE_MENU_KEY, 0) == 1);
        mEmulateMenuKey.setOnPreferenceChangeListener(this);

        mNavigationBarLeftPref = (CheckBoxPreference) findPreference(KEY_NAVIGATION_BAR_LEFT);

        updateNavbarPreferences(enableNavigationBar);
        updateDimensionValues();
        setHasOptionsMenu(true);

        if (!Utils.isPhone(getActivity())) {
            PreferenceCategory advanced_cat =
                (PreferenceCategory) findPreference(CATEGORY_ADVANCED);
            advanced_cat.removePreference(mNavigationBarLeftPref);
            advanced_cat.removePreference(mNavigationBarCanMove);
        }
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

    private void updateDimensionValues() {
        int navigationBarHeight = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_HEIGHT, -2);
        if (navigationBarHeight == -2) {
            navigationBarHeight = (int) (getResources().getDimension(
                    com.android.internal.R.dimen.navigation_bar_height)
                    / getResources().getDisplayMetrics().density);
        }
        mNavigationBarHeight.setValue(String.valueOf(navigationBarHeight));
        mNavigationBarHeight.setSummary(mNavigationBarHeight.getEntry());

        int navigationBarHeightLandscape = Settings.System.getInt(getContentResolver(),
                            Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, -2);
        if (navigationBarHeightLandscape == -2) {
            navigationBarHeightLandscape = (int) (getResources().getDimension(
                    com.android.internal.R.dimen.navigation_bar_height_landscape)
                    / getResources().getDisplayMetrics().density);
        }
        mNavigationBarHeightLandscape.setValue(String.valueOf(navigationBarHeightLandscape));
        mNavigationBarHeightLandscape.setSummary(mNavigationBarHeightLandscape.getEntry());

        if (mNavigationBarWidth == null) {
            return;
        }
        int navigationBarWidth = Settings.System.getInt(getContentResolver(),
                            Settings.System.NAVIGATION_BAR_WIDTH, -2);
        if (navigationBarWidth == -2) {
            navigationBarWidth = (int) (getResources().getDimension(
                    com.android.internal.R.dimen.navigation_bar_width)
                    / getResources().getDisplayMetrics().density);
        }
        mNavigationBarWidth.setValue(String.valueOf(navigationBarWidth));
        mNavigationBarWidth.setSummary(mNavigationBarWidth.getEntry());

        boolean navbarCanMove = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_CAN_MOVE, 1) == 1;

        mNavigationBarHeightLandscape.setEnabled(!navbarCanMove);
        mNavigationBarWidth.setEnabled(navbarCanMove);
    }

    public void updateNavbarPreferences(boolean show) {
        mNavBarMenuDisplay.setEnabled(show);
        mButtonPreference.setEnabled(show);
        mRingPreference.setEnabled(show);
        mEmulateMenuKey.setEnabled(show);
        if (mNavigationBarCanMove != null) {
            mNavigationBarCanMove.setEnabled(show);
        }
        mMenuDisplayLocation.setEnabled(show
            && mNavBarMenuDisplayValue != 1);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mNavigationBarWidth) {
            int index = mNavigationBarWidth.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_WIDTH, Integer.parseInt((String) newValue));
            mNavigationBarWidth.setSummary(mNavigationBarWidth.getEntries()[index]);
            return true;
        } else if (preference == mNavigationBarHeight) {
            int index = mNavigationBarHeight.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT, Integer.parseInt((String) newValue));
            mNavigationBarHeight.setSummary(mNavigationBarHeight.getEntries()[index]);
            return true;
        } else if (preference == mNavigationBarHeightLandscape) {
            int index = mNavigationBarHeightLandscape.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, Integer.parseInt((String) newValue));
            mNavigationBarHeightLandscape.setSummary(mNavigationBarHeightLandscape.getEntries()[index]);
            return true;
        } else if (preference == mMenuDisplayLocation) {
            int index = mMenuDisplayLocation.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_LOCATION, Integer.parseInt((String) newValue));
            mMenuDisplayLocation.setSummary(mMenuDisplayLocation.getEntries()[index]);
            return true;
        } else if (preference == mNavBarMenuDisplay) {
            mNavBarMenuDisplayValue = Integer.parseInt((String) newValue);
            int index = mNavBarMenuDisplay.findIndexOfValue((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MENU_VISIBILITY, mNavBarMenuDisplayValue);
            mNavBarMenuDisplay.setSummary(mNavBarMenuDisplay.getEntries()[index]);
            mMenuDisplayLocation.setEnabled(mNavBarMenuDisplayValue != 1);
            return true;
        } else if (preference == mEnableNavigationBar) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_SHOW, ((Boolean) newValue) ? 1 : 0);
            updateNavbarPreferences((Boolean) newValue);
            return true;
        } else if (preference == mNavigationBarCanMove) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.NAVIGATION_BAR_CAN_MOVE, ((Boolean) newValue) ? 0 : 1);
            updateDimensionValues();
            return true;
        } else if (preference == mEmulateMenuKey) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.EMULATE_MENU_KEY, ((Boolean) newValue) ? 1 : 0);
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {

        boolean hasNavBarByDefault = getResources().getBoolean(
                com.android.internal.R.bool.config_showNavigationBar);
        boolean enableNavigationBar = Settings.System.getInt(getContentResolver(),
                Settings.System.NAVIGATION_BAR_SHOW, hasNavBarByDefault ? 1 : 0) == 1;
        updateNavbarPreferences(enableNavigationBar);

        updateDimensionValues();
        super.onResume();
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

        NavbarSettings getOwner() {
            return (NavbarSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            switch (id) {
                case DLG_RESET:
                    return new AlertDialog.Builder(getActivity())
                    .setTitle(R.string.reset)
                    .setMessage(R.string.navbar_dimensions_reset_message)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.dlg_ok,
                        new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            Settings.System.putInt(getActivity().getContentResolver(),
                                    Settings.System.NAVIGATION_BAR_HEIGHT_LANDSCAPE, -2);
                            Settings.System.putInt(getActivity().getContentResolver(),
                                    Settings.System.NAVIGATION_BAR_HEIGHT, -2);
                            Settings.System.putInt(getActivity().getContentResolver(),
                                    Settings.System.NAVIGATION_BAR_WIDTH, -2);
                            getOwner().updateDimensionValues();
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
