/*
 *  Copyright (C) 2013 The OmniROM Project
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package org.omnirom.omnigears.interfacesettings;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.WindowManagerGlobal;

import org.omnirom.omnigears.chameleonos.SeekBarPreference;

public class BarsSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "BarsSettings";

    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String STATUS_BAR_NOTIF_COUNT = "status_bar_notif_count";
    private static final String NETWORK_STATS = "network_stats";
    private static final String NETWORK_STATS_UPDATE_FREQUENCY = "network_stats_update_frequency";
    private static final String STATUS_BAR_NETWORK_ACTIVITY = "status_bar_network_activity";
    private static final String QUICK_PULLDOWN = "quick_pulldown";
    private static final String QUICKSETTINGS_DYNAMIC = "quicksettings_dynamic_row";
    private static final String CATEGORY_NAVBAR = "category_navigation_bar";
    private static final String SOFT_BACK_KILL_APP = "soft_back_kill_app";
    private static final String DOUBLE_TAP_TO_SLEEP = "double_tap_to_sleep";
    private static final String SMS_BREATH = "sms_breath";
    private static final String MISSED_CALL_BREATH = "missed_call_breath";
    private static final String VOICEMAIL_BREATH = "voicemail_breath";

    private CheckBoxPreference mStatusBarBrightnessControl;
    private CheckBoxPreference mStatusBarNotifCount;
    private CheckBoxPreference mNetworkStats;
    private SeekBarPreference mNetworkStatsUpdateFrequency;
    private CheckBoxPreference mStatusBarNetworkActivity;
    private CheckBoxPreference mQuickSettingsDynamic;
    private ListPreference mQuickPulldown;
    private CheckBoxPreference mSoftBackKillApp;
    private CheckBoxPreference mDTS;
    private CheckBoxPreference mSMSBreath;
    private CheckBoxPreference mMissedCallBreath;
    private CheckBoxPreference mVoicemailBreath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.bars_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mStatusBarBrightnessControl =
                (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);

        try {
            if (Settings.System.getInt(resolver, Settings.System.SCREEN_BRIGHTNESS_MODE)
                    == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                mStatusBarBrightnessControl.setEnabled(false);
                mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
            }
        } catch (SettingNotFoundException e) {
        }

        mStatusBarNotifCount = (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NOTIF_COUNT);
        mStatusBarNotifCount.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NOTIF_COUNT, 0) == 1);
        mStatusBarNotifCount.setOnPreferenceChangeListener(this);

        mNetworkStats = (CheckBoxPreference) prefSet.findPreference(NETWORK_STATS);
        mNetworkStats.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NETWORK_STATS, 0) == 1);
        mNetworkStats.setOnPreferenceChangeListener(this);

        mNetworkStatsUpdateFrequency = (SeekBarPreference)
                prefSet.findPreference(NETWORK_STATS_UPDATE_FREQUENCY);
        mNetworkStatsUpdateFrequency.setValue(Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, 500));
        mNetworkStatsUpdateFrequency.setOnPreferenceChangeListener(this);

        mStatusBarNetworkActivity =
                (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_NETWORK_ACTIVITY);
        mStatusBarNetworkActivity.setChecked(Settings.System.getInt(resolver,
                Settings.System.STATUS_BAR_NETWORK_ACTIVITY, 0) == 1);
        mStatusBarNetworkActivity.setOnPreferenceChangeListener(this);

        mQuickPulldown = (ListPreference) findPreference(QUICK_PULLDOWN);
        mQuickPulldown.setOnPreferenceChangeListener(this);
        int statusQuickPulldown =
                Settings.System.getInt(resolver, Settings.System.QS_QUICK_PULLDOWN,0);
        mQuickPulldown.setValue(String.valueOf(statusQuickPulldown));

        mQuickSettingsDynamic = (CheckBoxPreference) prefSet.findPreference(QUICKSETTINGS_DYNAMIC);
        mQuickSettingsDynamic.setChecked(Settings.System.getInt(resolver,
            Settings.System.QUICK_SETTINGS_TILES_ROW, 1) != 0);
        mQuickSettingsDynamic.setOnPreferenceChangeListener(this);

        boolean hasNavBar =
                getResources().getBoolean(com.android.internal.R.bool.config_showNavigationBar);
        // Also check, if users without navigation bar force enabled it.
        hasNavBar = hasNavBar || (SystemProperties.getInt("qemu.hw.mainkeys", 1) == 0);

        // Hide navigation bar category on devices without navigation bar
        if (!hasNavBar) {
            prefSet.removePreference(findPreference(CATEGORY_NAVBAR));
        } else {
            mSoftBackKillApp = (CheckBoxPreference) findPreference(SOFT_BACK_KILL_APP);
            mSoftBackKillApp.setChecked(Settings.System.getInt(resolver,
                    Settings.System.SOFT_BACK_KILL_APP_ENABLE, 0) == 1);
            mSoftBackKillApp.setOnPreferenceChangeListener(this);
        }

        mSMSBreath = (CheckBoxPreference) findPreference(SMS_BREATH);
        mSMSBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.KEY_SMS_BREATH, 0) == 1);
        mSMSBreath.setOnPreferenceChangeListener(this);

        mMissedCallBreath = (CheckBoxPreference) findPreference(MISSED_CALL_BREATH);
        mMissedCallBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.KEY_MISSED_CALL_BREATH, 0) == 1);
        mMissedCallBreath.setOnPreferenceChangeListener(this);

        mVoicemailBreath = (CheckBoxPreference) findPreference(VOICEMAIL_BREATH);
        mVoicemailBreath.setChecked(Settings.System.getInt(resolver,
                Settings.System.KEY_VOICEMAIL_BREATH, 0) == 1);
        mVoicemailBreath.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        // If we didn't handle it, let preferences handle it.
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mStatusBarBrightnessControl) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL,
                    value ? 1 : 0);
        } else if (preference == mStatusBarNotifCount) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_NOTIF_COUNT,
                    value ? 1 : 0);
        } else if (preference == mNetworkStats) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_NETWORK_STATS,
                    value ? 1 : 0);
        } else if (preference == mNetworkStatsUpdateFrequency) {
            int i = Integer.valueOf((Integer) objValue);
            Settings.System.putInt(resolver,
                    Settings.System.STATUS_BAR_NETWORK_STATS_UPDATE_INTERVAL, i);
            return true;
        } else if (preference == mStatusBarNetworkActivity) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_NETWORK_ACTIVITY,
                    value ? 1 : 0);
        } else if (preference == mQuickSettingsDynamic) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.QUICK_SETTINGS_TILES_ROW,
                    value ? 1 : 0);
        } else if (preference == mQuickPulldown) {
            int statusQuickPulldown = Integer.valueOf((String) objValue);
            Settings.System.putInt(resolver, Settings.System.QS_QUICK_PULLDOWN,
                    statusQuickPulldown);
        } else if (preference == mSoftBackKillApp) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                Settings.System.SOFT_BACK_KILL_APP_ENABLE, value ? 1 : 0);
        } else if (preference == mDTS) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.DOUBLE_TAP_TO_SLEEP,
                    value ? 1 : 0);
        } else if (preference == mSMSBreath) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                    Settings.System.KEY_SMS_BREATH, value ? 1 : 0);
        } else if (preference == mMissedCallBreath) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                    Settings.System.KEY_MISSED_CALL_BREATH, value ? 1 : 0);
        } else if (preference == mVoicemailBreath) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver,
                    Settings.System.KEY_VOICEMAIL_BREATH, value ? 1 : 0);
        } else {
            return false;
        }
        return true;
    }
}
