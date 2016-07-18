package grimesmea.gmail.com.pricklefit;


import android.content.ContentValues;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract;

/**
 * Displays and manages changes to settings shared preferences.
 */
public class SettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private final String LOG_TAG = SettingsFragment.class.getSimpleName();

    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.pref_settings);
        Preference stepGoalPref = findPreference(getString(R.string.pref_step_goal_key));
        Preference notificationsPref = findPreference(getString(R.string.pref_notifications_enabled_key));

        stepGoalPref.setOnPreferenceChangeListener(this);
        notificationsPref.setOnPreferenceChangeListener(this);

        setPreferenceSummary(stepGoalPref, PreferenceManager
                .getDefaultSharedPreferences(stepGoalPref.getContext())
                .getString(stepGoalPref.getKey(),
                        getResources().getString(R.string.pref_step_goal_default)));
    }

    private void setPreferenceSummary(Preference preference, Object value) {
        String stringValue = value.toString();
        String key = preference.getKey();

        if (preference instanceof ListPreference) {
            // For list preferences, look up the correct display value in
            // the preference's 'entries' list (since they have separate labels/values).
            ListPreference listPreference = (ListPreference) preference;
            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else if (key.equals(getString(R.string.pref_step_goal_key))) {
            int dailyStepGoal = 0;

            try {
                dailyStepGoal = Integer.parseInt(stringValue);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            preference.setSummary(String.format("%,d", dailyStepGoal));
        }
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        setPreferenceSummary(preference, newValue);
        return true;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(getString(R.string.pref_step_goal_key))) {
            String newDailyStepGoalString = sharedPreferences.getString(getString(R.string.pref_step_goal_key),
                    getResources().getString(R.string.pref_step_goal_default));
            Log.d(LOG_TAG, "Updating daily step goal to " + newDailyStepGoalString);
            int newDailyStepGoal = 0;

            try {
                newDailyStepGoal = Integer.parseInt(newDailyStepGoalString);
            } catch (NumberFormatException nfe) {
                System.out.println("Could not parse " + nfe);
            }

            ContentValues currentDailyStepGoalValue = new ContentValues();
            currentDailyStepGoalValue.put(
                    HedgehogContract.AppStateEntry.COLUMN_DAILY_STEP_GOAL, newDailyStepGoal);

            // Update current daily step count in database.
            if (currentDailyStepGoalValue.size() > 0) {
                getActivity().getContentResolver().update(
                        HedgehogContract.AppStateEntry.CONTENT_URI,
                        currentDailyStepGoalValue,
                        null,
                        null);
            }
        }

        if (key.equals(getString(R.string.pref_notifications_enabled_key))) {
            boolean isNotificationsEnabled = sharedPreferences.getBoolean(getString(R.string.pref_notifications_enabled_key),
                    getResources().getBoolean(R.bool.pref_notifications_enabled_default));
            Log.d(LOG_TAG, "Updating notifications enabled status to " + isNotificationsEnabled);

            ContentValues notificationsEnabledValue = new ContentValues();
            notificationsEnabledValue.put(
                    HedgehogContract.AppStateEntry.COLUMN_NOTIFICATIONS_ENABLED_STATUS,
                    isNotificationsEnabled);

            // Update notifications enabled status in database.
            if (notificationsEnabledValue.size() > 0) {
                getActivity().getContentResolver().update(
                        HedgehogContract.AppStateEntry.CONTENT_URI,
                        notificationsEnabledValue,
                        null,
                        null);
            }
        }
    }
}
