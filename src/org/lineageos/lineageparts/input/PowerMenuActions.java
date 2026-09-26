/*
 * Copyright (C) 2014-2015 The CyanogenMod Project
 *               2017-2022 The LineageOS Project
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

package org.lineageos.lineageparts.input;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings;
import android.text.TextUtils;

import androidx.preference.CheckBoxPreference;
import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;

import com.android.internal.widget.LockPatternUtils;

import org.lineageos.internal.util.PowerMenuConstants;
import org.lineageos.internal.util.PowerMenuUtils;
import org.lineageos.lineageparts.R;
import org.lineageos.lineageparts.SettingsPreferenceFragment;
import org.lineageos.lineageparts.utils.TelephonyUtils;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lineageos.app.LineageGlobalActions;
import lineageos.providers.LineageSettings;

import static org.lineageos.internal.util.PowerMenuConstants.*;

public class PowerMenuActions extends SettingsPreferenceFragment {
    final static String TAG = "PowerMenuActions";

    private static final String KEY_PINNED_APPS = "pinned_apps";

    private MultiSelectListPreference mPinnedAppsPref;
    private CheckBoxPreference mHomePref;
    private CheckBoxPreference mAppSwitcherPref;
    private CheckBoxPreference mCloseAppsPref;
    private CheckBoxPreference mKillAppPref;
    private CheckBoxPreference mSettingsPref;
    private CheckBoxPreference mScreenshotPref;
    private CheckBoxPreference mAirplanePref;
    private CheckBoxPreference mUsersPref;
    private CheckBoxPreference mBugReportPref;
    private CheckBoxPreference mLockDownPref;
    private CheckBoxPreference mEmergencyPref;

    private LineageGlobalActions mLineageGlobalActions;

    Context mContext;
    private LockPatternUtils mLockPatternUtils;
    private UserManager mUserManager;
    private List<String> mLocalUserConfig = new ArrayList<String>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.power_menu_settings);
        mContext = getActivity().getApplicationContext();
        mLockPatternUtils = new LockPatternUtils(mContext);
        mUserManager = UserManager.get(mContext);
        mLineageGlobalActions = LineageGlobalActions.getInstance(mContext);

        for (String action : PowerMenuConstants.getAllActions()) {
            if (action.equals(GLOBAL_ACTION_KEY_HOME)) {
                mHomePref = findPreference(GLOBAL_ACTION_KEY_HOME);
            } else if (action.equals(GLOBAL_ACTION_KEY_APP_SWITCHER)) {
                mAppSwitcherPref = findPreference(GLOBAL_ACTION_KEY_APP_SWITCHER);
            } else if (action.equals(GLOBAL_ACTION_KEY_CLOSE_APPS)) {
                mCloseAppsPref = findPreference(GLOBAL_ACTION_KEY_CLOSE_APPS);
            } else if (action.equals(GLOBAL_ACTION_KEY_KILL_APP)) {
                mKillAppPref = findPreference(GLOBAL_ACTION_KEY_KILL_APP);
            } else if (action.equals(GLOBAL_ACTION_KEY_SETTINGS)) {
                mSettingsPref = findPreference(GLOBAL_ACTION_KEY_SETTINGS);
            } else if (action.equals(GLOBAL_ACTION_KEY_SCREENSHOT)) {
                mScreenshotPref = findPreference(GLOBAL_ACTION_KEY_SCREENSHOT);
            } else if (action.equals(GLOBAL_ACTION_KEY_AIRPLANE)) {
                mAirplanePref = findPreference(GLOBAL_ACTION_KEY_AIRPLANE);
            } else if (action.equals(GLOBAL_ACTION_KEY_USERS)) {
                mUsersPref = findPreference(GLOBAL_ACTION_KEY_USERS);
            } else if (action.equals(GLOBAL_ACTION_KEY_BUGREPORT)) {
                mBugReportPref = findPreference(GLOBAL_ACTION_KEY_BUGREPORT);
            } else if (action.equals(GLOBAL_ACTION_KEY_LOCKDOWN)) {
                mLockDownPref = findPreference(GLOBAL_ACTION_KEY_LOCKDOWN);
            } else if (action.equals(GLOBAL_ACTION_KEY_EMERGENCY)) {
                mEmergencyPref = findPreference(GLOBAL_ACTION_KEY_EMERGENCY);
            }
        }

        if (!TelephonyUtils.isVoiceCapable(getActivity())) {
            getPreferenceScreen().removePreference(mEmergencyPref);
            mEmergencyPref = null;
        }

        mLocalUserConfig = mLineageGlobalActions.getLocalUserConfig();

        mPinnedAppsPref = findPreference(KEY_PINNED_APPS);
        loadLaunchableApps();
        mPinnedAppsPref.setOnPreferenceChangeListener((preference, newValue) -> {
            @SuppressWarnings("unchecked")
            final Set<String> packages = (Set<String>) newValue;
            savePinnedApps(packages);
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        loadPinnedApps();

        if (mHomePref != null) {
            mHomePref.setChecked(mLineageGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_HOME));
        }

        if (mAppSwitcherPref != null) {
            mAppSwitcherPref.setChecked(mLineageGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_APP_SWITCHER));
        }

        if (mCloseAppsPref != null) {
            mCloseAppsPref.setChecked(mLineageGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_CLOSE_APPS));
        }

        if (mKillAppPref != null) {
            mKillAppPref.setChecked(mLineageGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_KILL_APP));
        }

        if (mSettingsPref != null) {
            mSettingsPref.setChecked(mLineageGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_SETTINGS));
        }

        if (mScreenshotPref != null) {
            mScreenshotPref.setChecked(mLineageGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_SCREENSHOT));
        }

        if (mAirplanePref != null) {
            mAirplanePref.setChecked(mLineageGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_AIRPLANE));
        }

        if (mUsersPref != null) {
            if (!UserHandle.MU_ENABLED || !UserManager.supportsMultipleUsers()) {
                getPreferenceScreen().removePreference(findPreference(GLOBAL_ACTION_KEY_USERS));
                mUsersPref = null;
            } else {
                List<UserInfo> users = mUserManager.getUsers();
                boolean enabled = (users.size() > 1);
                mUsersPref.setChecked(mLineageGlobalActions.userConfigContains(
                        GLOBAL_ACTION_KEY_USERS) && enabled);
                mUsersPref.setEnabled(enabled);
            }
        }

        if (mBugReportPref != null) {
            mBugReportPref.setChecked(mLineageGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_BUGREPORT));
        }

        if (mEmergencyPref != null) {
            mEmergencyPref.setChecked(mLineageGlobalActions.userConfigContains(
                    GLOBAL_ACTION_KEY_EMERGENCY));
        }

        updatePreferences();
    }

    @Override
    public void onResume() {
        super.onResume();
        updatePreferences();
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        boolean value;

        if (preference == mHomePref) {
            value = mHomePref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_HOME);

        } else if (preference == mAppSwitcherPref) {
            value = mAppSwitcherPref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_APP_SWITCHER);

        } else if (preference == mCloseAppsPref) {
            value = mCloseAppsPref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_CLOSE_APPS);

        } else if (preference == mKillAppPref) {
            value = mKillAppPref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_KILL_APP);

        } else if (preference == mSettingsPref) {
            value = mSettingsPref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_SETTINGS);

        } else if (preference == mScreenshotPref) {
            value = mScreenshotPref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_SCREENSHOT);

        } else if (preference == mAirplanePref) {
            value = mAirplanePref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_AIRPLANE);

        } else if (preference == mUsersPref) {
            value = mUsersPref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_USERS);

        } else if (preference == mBugReportPref) {
            value = mBugReportPref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_BUGREPORT);
            Settings.Global.putInt(getContentResolver(),
                    Settings.Global.BUGREPORT_IN_POWER_MENU, value ? 1 : 0);

        } else if (preference == mLockDownPref) {
            value = mLockDownPref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_LOCKDOWN);
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.LOCKDOWN_IN_POWER_MENU, value ? 1 : 0, UserHandle.USER_CURRENT);

        } else if (preference == mEmergencyPref) {
            value = mEmergencyPref.isChecked();
            mLineageGlobalActions.updateUserConfig(value, GLOBAL_ACTION_KEY_EMERGENCY);

        } else {
            return super.onPreferenceTreeClick(preference);
        }
        return true;
    }

    /**
     * Offers every app with a launcher icon for pinning, sorted by name.
     */
    private void loadLaunchableApps() {
        final PackageManager pm = mContext.getPackageManager();
        final Intent launcherIntent = new Intent(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER);
        final List<String[]> apps = new ArrayList<>();
        final Set<String> packages = new HashSet<>();
        for (ResolveInfo info : pm.queryIntentActivities(launcherIntent, 0)) {
            final String packageName = info.activityInfo.packageName;
            if (packages.add(packageName)) {
                apps.add(new String[] {
                        info.activityInfo.applicationInfo.loadLabel(pm).toString(),
                        packageName });
            }
        }
        final Collator collator = Collator.getInstance();
        apps.sort((a, b) -> collator.compare(a[0], b[0]));

        final CharSequence[] labels = new CharSequence[apps.size()];
        final CharSequence[] packageNames = new CharSequence[apps.size()];
        for (int i = 0; i < apps.size(); i++) {
            labels[i] = apps.get(i)[0];
            packageNames[i] = apps.get(i)[1];
        }
        mPinnedAppsPref.setEntries(labels);
        mPinnedAppsPref.setEntryValues(packageNames);
    }

    private void loadPinnedApps() {
        final String pinned = PowerMenuUtils.getPinnedApps(mContext, UserHandle.USER_CURRENT);
        final Set<String> packages = new HashSet<>();
        if (!TextUtils.isEmpty(pinned)) {
            packages.addAll(Arrays.asList(pinned.split("\\|")));
        }
        // Only keep what is still installed
        packages.retainAll(Arrays.asList(toStrings(mPinnedAppsPref.getEntryValues())));
        mPinnedAppsPref.setValues(packages);
        updatePinnedAppsSummary(packages);
    }

    private void savePinnedApps(Set<String> packages) {
        // Keep the order of the list, which is also the order in the power menu
        final List<String> ordered = new ArrayList<>();
        for (String packageName : toStrings(mPinnedAppsPref.getEntryValues())) {
            if (packages.contains(packageName)) {
                ordered.add(packageName);
            }
        }
        Settings.Secure.putStringForUser(getContentResolver(), POWER_MENU_PINNED_APPS,
                String.join("|", ordered), UserHandle.USER_CURRENT);
        updatePinnedAppsSummary(packages);
    }

    private void updatePinnedAppsSummary(Set<String> packages) {
        final CharSequence[] labels = mPinnedAppsPref.getEntries();
        final CharSequence[] packageNames = mPinnedAppsPref.getEntryValues();
        final List<CharSequence> pinned = new ArrayList<>();
        for (int i = 0; i < packageNames.length; i++) {
            if (packages.contains(packageNames[i].toString())) {
                pinned.add(labels[i]);
            }
        }
        mPinnedAppsPref.setSummary(pinned.isEmpty()
                ? getString(R.string.power_menu_pinned_apps_none)
                : TextUtils.join(", ", pinned));
    }

    private static String[] toStrings(CharSequence[] values) {
        final String[] strings = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            strings[i] = values[i].toString();
        }
        return strings;
    }

    private void updatePreferences() {
        UserInfo currentUser = mUserManager.getUserInfo(UserHandle.myUserId());
        boolean developmentSettings = Settings.Global.getInt(
                getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) == 1;
        boolean bugReport = Settings.Global.getInt(
                getContentResolver(), Settings.Global.BUGREPORT_IN_POWER_MENU, 0) == 1;
        boolean isPrimaryUser = currentUser == null || currentUser.isPrimary();
        if (mBugReportPref != null) {
            mBugReportPref.setEnabled(developmentSettings && isPrimaryUser);
            if (!developmentSettings) {
                mBugReportPref.setChecked(false);
                mBugReportPref.setSummary(R.string.power_menu_bug_report_devoptions_unavailable);
            } else if (!isPrimaryUser) {
                mBugReportPref.setChecked(false);
                mBugReportPref.setSummary(R.string.power_menu_bug_report_unavailable_for_user);
            } else {
                mBugReportPref.setChecked(bugReport);
                mBugReportPref.setSummary(null);
            }
        }

        boolean isKeyguardSecure = mLockPatternUtils.isSecure(UserHandle.myUserId());
        boolean lockdown = Settings.Secure.getIntForUser(
                getContentResolver(), Settings.Secure.LOCKDOWN_IN_POWER_MENU, 0,
                UserHandle.USER_CURRENT) == 1;
        if (mLockDownPref != null) {
            mLockDownPref.setEnabled(isKeyguardSecure);
            if (isKeyguardSecure) {
                mLockDownPref.setChecked(lockdown);
                mLockDownPref.setSummary(null);
            } else {
                mLockDownPref.setChecked(false);
                mLockDownPref.setSummary(R.string.power_menu_lockdown_unavailable);
            }
        }
    }
}
