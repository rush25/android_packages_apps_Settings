package com.android.settings.privacyguard;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class PrivacyGuardNotification extends SettingsPreferenceFragment implements
        OnPreferenceChangeListener {

    private static final String TAG = "PrivacyGuardNotification";

    private static final String KEY_PRIVACY_GUARD_NOTIFICATION = "privacy_guard_notification";
    private static final String KEY_PRIVACY_GUARD_NOTIFICATION_ICON = "privacy_guard_notification_icon";
    private static final String KEY_PRIVACY_GUARD_NOTIFICATION_DISMISS = "privacy_guard_notification_dismiss";

    private CheckBoxPreference mPrivacyGuardNotification;
    private CheckBoxPreference mPrivacyGuardNotificationIcon;
    private CheckBoxPreference mPrivacyGuardNotificationDismiss;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.privacy_guard_notification);
        PreferenceScreen prefSet = getPreferenceScreen();

        mPrivacyGuardNotification = (CheckBoxPreference) findPreference(KEY_PRIVACY_GUARD_NOTIFICATION);
        mPrivacyGuardNotification.setOnPreferenceChangeListener(this);

        mPrivacyGuardNotification.setChecked(Settings.System.getInt(getContentResolver(),
		Settings.System.PRIVACY_GUARD_NOTIFICATION, 1) == 1);

        mPrivacyGuardNotificationIcon = (CheckBoxPreference) findPreference(KEY_PRIVACY_GUARD_NOTIFICATION_ICON);
        mPrivacyGuardNotificationIcon.setOnPreferenceChangeListener(this);

        mPrivacyGuardNotificationIcon.setChecked(Settings.System.getInt(getContentResolver(),
		Settings.System.PRIVACY_GUARD_NOTIFICATION_ICON, 1) == 1);

        mPrivacyGuardNotificationDismiss = (CheckBoxPreference) findPreference(KEY_PRIVACY_GUARD_NOTIFICATION_DISMISS);
        mPrivacyGuardNotificationDismiss.setOnPreferenceChangeListener(this);

        mPrivacyGuardNotificationDismiss.setChecked(Settings.System.getInt(getContentResolver(),
		Settings.System.PRIVACY_GUARD_NOTIFICATION_DISMISS, 0) == 1);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mPrivacyGuardNotification) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PRIVACY_GUARD_NOTIFICATION, value ? 1 : 0);
		return true;
        } else if (preference == mPrivacyGuardNotificationIcon) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PRIVACY_GUARD_NOTIFICATION_ICON, value ? 1 : 0);
		return true;
        } else if (preference == mPrivacyGuardNotificationDismiss) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(getContentResolver(),
                    Settings.System.PRIVACY_GUARD_NOTIFICATION_DISMISS, value ? 1 : 0);
		return true;
        }
        return false;
    }
}
