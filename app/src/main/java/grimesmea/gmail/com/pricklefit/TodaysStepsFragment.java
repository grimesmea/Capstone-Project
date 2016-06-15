package grimesmea.gmail.com.pricklefit;


import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;


/**
 * Displays daily step count and goal.
 */
public class TodaysStepsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int COL_HEDGEHOG_NAME = 1;
    static final int COL_HEDGEHOG_IMAGE_NAME = 2;
    static final int COL_HEDGEHOG_SILHOUETTE_IMAGE_NAME = 3;
    static final int COL_HEDGEHOG_DESCRIPTION = 4;
    static final int COL_HEDGEHOG_HAPPINESS_LEVEL = 5;
    static final int COL_HEDGEHOG_FITNESS_LEVEL = 6;
    static final int COL_HEDGEHOG_UNLOCK_STATUS = 7;
    static final int COL_HEDGEHOG_SELECTED_STATUS = 8;
    private static final int SELECTED_HEDGEHOG_LOADER = 100;
    private final String LOG_TAG = TodaysStepsFragment.class.getSimpleName();
    private ImageView hedgehogImageView;
    private TextView dailyStepGoalTextView;
    private Hedgehog selectedHedgehog;
    private Drawable hedgehogDrawable;

    public TodaysStepsFragment() {
        // Empty constructor required for fragment subclasses
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!getActivity().getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        ).moveToNext()) {
            Log.d(LOG_TAG, "populating content provider");
            fetchHedgehogs();
        }
    }

    private void fetchHedgehogs() {
        FetchHedgehogsDataTask hedgehogsDataTasks = new FetchHedgehogsDataTask(getActivity());
        hedgehogsDataTasks.execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_todays_steps, container, false);
        hedgehogImageView = (ImageView) rootView.findViewById(R.id.hedgehog_image);
        dailyStepGoalTextView = (TextView) rootView.findViewById(R.id.daily_step_goal);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String dailyStepGoalStr = prefs.getString(getContext().getString(R.string.pref_step_goal_key),
                getContext().getString(R.string.pref_step_goal_default));
        int dailyStepGoal = 0;

        try {
            dailyStepGoal = Integer.parseInt(dailyStepGoalStr);
        } catch (NumberFormatException nfe) {
            System.out.println("Could not parse " + nfe);
        }
        dailyStepGoalTextView.setText(String.format("%,d", dailyStepGoal));

        getLoaderManager().initLoader(SELECTED_HEDGEHOG_LOADER, null, this);

        return rootView;
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(LOG_TAG, "onCreateLoader");
        CursorLoader cursorLoader = new CursorLoader(
                getContext(),
                HedgehogsEntry.buildSelectedHedgehogUri(),
                null,
                null,
                null,
                null
        );

        return cursorLoader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            selectedHedgehog = new Hedgehog(data);
            int hedgehogImageResource = getResources().getIdentifier(selectedHedgehog.getImageName(), "drawable", getContext().getPackageName());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                hedgehogDrawable = getResources().getDrawable(hedgehogImageResource, getContext().getTheme());
            } else {
                hedgehogDrawable = getResources().getDrawable(hedgehogImageResource);
            }

            hedgehogImageView.setImageDrawable(hedgehogDrawable);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        hedgehogImageView.setImageDrawable(null);
        selectedHedgehog = null;
        hedgehogDrawable = null;
    }
}
