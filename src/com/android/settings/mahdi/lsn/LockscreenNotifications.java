/*
* Based on: http://www.lukehorvat.com/blog/android-numberpickerdialogpreference/
* Thanks to the original author!
*/

package com.android.settings.mahdi.lsn;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.net.Uri;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.preference.Preference;
import android.preference.SeekBarPreference;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.view.WindowManager;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;
import com.android.settings.Utils;

import com.android.settings.mahdi.lsn.AppMultiSelectListPreference;
import com.android.settings.mahdi.lsn.NumberPickerPreference;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

public class LockscreenNotifications extends SettingsPreferenceFragment implements Preference.OnPreferenceChangeListener {

    private static final String KEY_CATEGORY_WAKE_UP = "category_wake_up";
    private static final String KEY_HIDE_NON_CLEARABLE = "hide_non_clearable";
    private static final String KEY_DISMISS_ALL = "dismiss_all";
    private static final String KEY_DISMISS_NOTIFICATION = "dismiss_notification";
    private static final String KEY_HIDE_LOW_PRIORITY = "hide_low_priority";
    private static final String KEY_INCLUDED_APPS = "included_apps";
    private static final String KEY_EXCLUDED_APPS = "excluded_apps";
    private static final String KEY_WAKE_ON_NOTIFICATION = "wake_on_notification";
    private static final String KEY_POCKET_MODE = "pocket_mode";
    private static final String KEY_SHOW_ALWAYS = "show_always";
    private static final String KEY_PRIVACY_MODE = "privacy_mode";
    private static final String KEY_EXPANDED_VIEW = "expanded_view";
    private static final String KEY_FORCE_EXPANDED_VIEW = "force_expanded_view";
    private static final String KEY_NOTIFICATIONS_HEIGHT = "notifications_height";
    private static final String KEY_OFFSET_TOP = "offset_top";
    private static final String KEY_NOTIFICATION_COLOR = "notification_color";
    private static final String KEY_DYNAMIC_WIDTH = "dynamic_width";

    private CheckBoxPreference mHideNonClearable;
    private CheckBoxPreference mDismissAll;
    private CheckBoxPreference mDismissNotification;
    private CheckBoxPreference mHideLowPriority;
    private AppMultiSelectListPreference mIncludedAppsPref;
    private AppMultiSelectListPreference mExcludedAppsPref;
    private CheckBoxPreference mWakeOnNotification;
    private CheckBoxPreference mPocketMode;
    private CheckBoxPreference mShowAlways;
    private CheckBoxPreference mPrivacyMode;
    private CheckBoxPreference mExpandedView;
    private CheckBoxPreference mForceExpandedView;
    private NumberPickerPreference mNotificationsHeight;
    private SeekBarPreference mOffsetTop;
    private ColorPickerPreference mNotificationColor;
    private CheckBoxPreference mDynamicWidth;

    private Switch mActionBarSwitch;
    private LockscreenNotificationsEnabler mLsnEnabler;

    private ViewGroup mPrefsContainer;
    private View mDisabledText;

    private ContentObserver mSettingsObserver = new ContentObserver(new Handler()) {
        @Override
        public void onChange(boolean selfChange, Uri uri) {
            updateEnabledState();
        }
    };

    @Override
      public void onActivityCreated(Bundle icicle) {
        // We don't call super.onActivityCreated() here, since it assumes we already set up
        // Preference (probably in onCreate()), while ProfilesSettings exceptionally set it up in
        // this method.
        // On/off switch
        Activity activity = getActivity();
        //Switch
        mActionBarSwitch = new Switch(activity);

        if (activity instanceof PreferenceActivity) {
            final int padding = activity.getResources().getDimensionPixelSize(
                    R.dimen.action_bar_switch_padding);
            mActionBarSwitch.setPaddingRelative(0, 0, padding, 0);
            activity.getActionBar().setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM,
                    ActionBar.DISPLAY_SHOW_CUSTOM);
            activity.getActionBar().setCustomView(mActionBarSwitch, new ActionBar.LayoutParams(
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    ActionBar.LayoutParams.WRAP_CONTENT,
                    Gravity.CENTER_VERTICAL | Gravity.END));
        }

        mLsnEnabler = new LockscreenNotificationsEnabler(activity, mActionBarSwitch);
        // After confirming PreferenceScreen is available, we call super.
          super.onActivityCreated(icicle);
          setHasOptionsMenu(true);
      }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.lockscreen_notifications);
        PreferenceScreen prefs = getPreferenceScreen();
        final ContentResolver cr = getActivity().getContentResolver();

        mHideNonClearable = (CheckBoxPreference) prefs.findPreference(KEY_HIDE_NON_CLEARABLE);
        mHideNonClearable.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_NON_CLEARABLE, 1, UserHandle.USER_CURRENT) == 1);

        mDismissAll = (CheckBoxPreference) prefs.findPreference(KEY_DISMISS_ALL);
        mDismissAll.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_DISMISS_ALL, 1, UserHandle.USER_CURRENT) == 1);

        mDismissNotification = (CheckBoxPreference) prefs.findPreference(KEY_DISMISS_NOTIFICATION);
        mDismissNotification.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_DISMISS_NOTIFICATION, 1, UserHandle.USER_CURRENT) == 1);

        mHideLowPriority = (CheckBoxPreference) prefs.findPreference(KEY_HIDE_LOW_PRIORITY);
        mHideLowPriority.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_LOW_PRIORITY, 0, UserHandle.USER_CURRENT) == 1);

        mIncludedAppsPref = (AppMultiSelectListPreference) prefs.findPreference(KEY_INCLUDED_APPS);
        Set<String> includedApps = getIncludedApps();
        if (includedApps != null) {
            mIncludedAppsPref.setValues(includedApps);
        }
        mIncludedAppsPref.setOnPreferenceChangeListener(this);

        mExcludedAppsPref = (AppMultiSelectListPreference) prefs.findPreference(KEY_EXCLUDED_APPS);
        Set<String> excludedApps = getExcludedApps();
        if (excludedApps != null) {
            mExcludedAppsPref.setValues(excludedApps);
        }
        mExcludedAppsPref.setOnPreferenceChangeListener(this);

        mWakeOnNotification = (CheckBoxPreference) prefs.findPreference(KEY_WAKE_ON_NOTIFICATION);
        mWakeOnNotification.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_WAKE_ON_NOTIFICATION, 0, UserHandle.USER_CURRENT) == 1);

        mPocketMode = (CheckBoxPreference) prefs.findPreference(KEY_POCKET_MODE);
        mPocketMode.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_POCKET_MODE, 0, UserHandle.USER_CURRENT) == 1);

        mShowAlways = (CheckBoxPreference) prefs.findPreference(KEY_SHOW_ALWAYS);
        mShowAlways.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_SHOW_ALWAYS, 0, UserHandle.USER_CURRENT) == 1);
        mShowAlways.setEnabled(mPocketMode.isChecked() && mPocketMode.isEnabled());

        mPrivacyMode = (CheckBoxPreference) prefs.findPreference(KEY_PRIVACY_MODE);
        mPrivacyMode.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_PRIVACY_MODE, 0, UserHandle.USER_CURRENT) == 1);

        mExpandedView = (CheckBoxPreference) prefs.findPreference(KEY_EXPANDED_VIEW);
        mExpandedView.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW, 1, UserHandle.USER_CURRENT) == 1);
        mExpandedView.setEnabled(!mPrivacyMode.isChecked());

        mForceExpandedView = (CheckBoxPreference) prefs.findPreference(KEY_FORCE_EXPANDED_VIEW);
        mForceExpandedView.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW, 0, UserHandle.USER_CURRENT) == 1);
        mForceExpandedView.setEnabled(mExpandedView.isChecked() && !mPrivacyMode.isChecked());

        mOffsetTop = (SeekBarPreference) prefs.findPreference(KEY_OFFSET_TOP);
        mOffsetTop.setProgress((int)(Settings.System.getFloat(cr,
                Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP, 0.3f) * 100));
        mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + mOffsetTop.getProgress() + "%");
        mOffsetTop.setOnPreferenceChangeListener(this);

        mNotificationsHeight = (NumberPickerPreference) prefs.findPreference(KEY_NOTIFICATIONS_HEIGHT);
        mNotificationsHeight.setValue(Settings.System.getInt(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, 4));
        Point displaySize = new Point();
        ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
        int max = Math.round((float)displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                (float)mContext.getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
        mNotificationsHeight.setMinValue(1);
        mNotificationsHeight.setMaxValue(max);
        mNotificationsHeight.setOnPreferenceChangeListener(this);

        mNotificationColor = (ColorPickerPreference) prefs.findPreference(KEY_NOTIFICATION_COLOR);
        mNotificationColor.setAlphaSliderEnabled(true);
        int color = Settings.System.getInt(cr,
                Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, 0x55555555);
        String hexColor = String.format("#%08x", (0xffffffff & color));
        mNotificationColor.setSummary(hexColor);
        mNotificationColor.setDefaultValue(color);
        mNotificationColor.setNewPreviewColor(color);
        mNotificationColor.setOnPreferenceChangeListener(this);

        mDynamicWidth = (CheckBoxPreference) prefs.findPreference(KEY_DYNAMIC_WIDTH);
        mDynamicWidth.setChecked(Settings.System.getIntForUser(cr,
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_DYNAMIC_WIDTH, 0, UserHandle.USER_CURRENT) == 1);

        boolean hasProximitySensor = getPackageManager().hasSystemFeature(PackageManager.FEATURE_SENSOR_PROXIMITY);
        if (!hasProximitySensor) {
            PreferenceCategory wakeup = (PreferenceCategory) prefs.findPreference(KEY_CATEGORY_WAKE_UP);
            wakeup.removePreference(mPocketMode);
            wakeup.removePreference(mShowAlways);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.lockscreen_notifications_fragment, container, false);
        mPrefsContainer = (ViewGroup) v.findViewById(R.id.prefs_container);
        mDisabledText = v.findViewById(R.id.disabled_text);

        View prefs = super.onCreateView(inflater, mPrefsContainer, savedInstanceState);
        mPrefsContainer.addView(prefs);

        return v;
    }

    @Override
    public void onDestroyView() {
        getActivity().getActionBar().setCustomView(null);
        super.onDestroyView();
    }

    @Override
      public void onResume() {
        super.onResume();
        if (mLsnEnabler != null) {
            mLsnEnabler.resume();
        }
        getContentResolver().registerContentObserver(
                Settings.System.getUriFor(Settings.System.LOCKSCREEN_NOTIFICATIONS),
                true, mSettingsObserver);
        updateEnabledState();

        // If running on a phone, remove padding around container
        // and the preference listview
        if (!Utils.isTablet(getActivity())) {
            mPrefsContainer.setPadding(0, 0, 0, 0);
            getListView().setPadding(0, 0, 0, 0);
        }
    }

    public void onPause() {
        super.onPause();
        if (mLsnEnabler != null) {
            mLsnEnabler.pause();
        }
        getContentResolver().unregisterContentObserver(mSettingsObserver);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        ContentResolver cr = getActivity().getContentResolver();
        if (preference == mHideLowPriority) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_LOW_PRIORITY,
                    mHideLowPriority.isChecked() ? 1 : 0);
        } else if (preference == mHideNonClearable) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_HIDE_NON_CLEARABLE,
                    mHideNonClearable.isChecked() ? 1 : 0);
            mDismissAll.setEnabled(!mHideNonClearable.isChecked());
        } else if (preference == mDismissAll) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_DISMISS_ALL,
                    mDismissAll.isChecked() ? 1 : 0);
        } else if (preference == mDismissNotification) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_DISMISS_NOTIFICATION,
                    mDismissAll.isChecked() ? 1 : 0);
        } else if (preference == mWakeOnNotification) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_WAKE_ON_NOTIFICATION,
                    mWakeOnNotification.isChecked() ? 1 : 0);
        } else if (preference == mPocketMode) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_POCKET_MODE,
                    mPocketMode.isChecked() ? 1 : 0);
            mShowAlways.setEnabled(mPocketMode.isChecked());
        } else if (preference == mShowAlways) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_SHOW_ALWAYS,
                    mShowAlways.isChecked() ? 1 : 0);
        } else if (preference == mPrivacyMode) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_PRIVACY_MODE,
                    mPrivacyMode.isChecked() ? 1 : 0);
            mForceExpandedView.setEnabled(mExpandedView.isChecked() && !mPrivacyMode.isChecked());
            mExpandedView.setEnabled(!mPrivacyMode.isChecked());
        } else if (preference == mExpandedView) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_EXPANDED_VIEW,
                    mExpandedView.isChecked() ? 1 : 0);
            mForceExpandedView.setEnabled(mExpandedView.isChecked());
        } else if (preference == mForceExpandedView) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_FORCE_EXPANDED_VIEW,
                    mForceExpandedView.isChecked() ? 1 : 0);
        } else if (preference == mDynamicWidth) {
            Settings.System.putInt(cr, Settings.System.LOCKSCREEN_NOTIFICATIONS_DYNAMIC_WIDTH,
                    mDynamicWidth.isChecked() ? 1 : 0);
        } else {
            return super.onPreferenceTreeClick(preferenceScreen, preference);
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference pref, Object value) {
        if (pref == mNotificationsHeight) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_HEIGHT, (Integer)value);
        } else if (pref == mOffsetTop) {
            Settings.System.putFloat(getContentResolver(), Settings.System.LOCKSCREEN_NOTIFICATIONS_OFFSET_TOP,
                    (Integer)value / 100f);
            mOffsetTop.setTitle(getResources().getText(R.string.offset_top) + " " + (Integer)value + "%");
            Point displaySize = new Point();
            ((WindowManager)mContext.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getSize(displaySize);
            int max = Math.round((float)displaySize.y * (1f - (mOffsetTop.getProgress() / 100f)) /
                    (float)mContext.getResources().getDimensionPixelSize(R.dimen.notification_row_min_height));
            mNotificationsHeight.setMaxValue(max);
        } else if (pref == mNotificationColor) {
            String hex = ColorPickerPreference.convertToARGB(
                    Integer.valueOf(String.valueOf(value)));
            pref.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.LOCKSCREEN_NOTIFICATIONS_COLOR, intHex);
            return true;
        } else if (pref == mIncludedAppsPref) {
            storeIncludedApps((HashSet<String>) value);
        } else if (pref == mExcludedAppsPref) {
            storeExcludedApps((HashSet<String>) value);
            return true;
        } else {
            return false;
        }
        return true;
    }

    private void updateEnabledState() {
        boolean enabled = Settings.System.getInt(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS, 0) != 0;
        mPrefsContainer.setVisibility(enabled ? View.VISIBLE : View.GONE);
        mDisabledText.setVisibility(enabled ? View.GONE : View.VISIBLE);
    }

    private HashSet<String> getIncludedApps() {
        String included = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_INCLUDED_APPS);
        if (TextUtils.isEmpty(included)) {
            return null;
        }

        return new HashSet<String>(Arrays.asList(included.split("\\|")));
    }

    private void storeIncludedApps(HashSet<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_INCLUDED_APPS, builder.toString());
    }

    private HashSet<String> getExcludedApps() {
        String excluded = Settings.System.getString(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_EXCLUDED_APPS);
        if (TextUtils.isEmpty(excluded)) {
            return null;
        }

        return new HashSet<String>(Arrays.asList(excluded.split("\\|")));
    }

    private void storeExcludedApps(HashSet<String> values) {
        StringBuilder builder = new StringBuilder();
        String delimiter = "";
        for (String value : values) {
            builder.append(delimiter);
            builder.append(value);
            delimiter = "|";
        }
        Settings.System.putString(getContentResolver(),
                Settings.System.LOCKSCREEN_NOTIFICATIONS_EXCLUDED_APPS, builder.toString());
    }
}
