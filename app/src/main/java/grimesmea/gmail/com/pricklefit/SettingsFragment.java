package grimesmea.gmail.com.pricklefit;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsFragment extends PreferenceFragment implements
        Preference.OnPreferenceChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {


    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.shared_prefs);

        Preference stepGoalPref = findPreference(getString(R.string.pref_step_goal_key));
        Preference notificationsPref = findPreference(getString(R.string.pref_enable_notifications_key));

        stepGoalPref.setOnPreferenceChangeListener(this);
        notificationsPref.setOnPreferenceChangeListener(this);

        setPreferenceSummary(stepGoalPref, PreferenceManager
                .getDefaultSharedPreferences(stepGoalPref.getContext())
                .getString(stepGoalPref.getKey(), ""));
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

        }
    }
}
