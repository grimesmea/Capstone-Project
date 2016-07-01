package grimesmea.gmail.com.pricklefit;


import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DailyTotalResult;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.concurrent.TimeUnit;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;


/**
 * Displays daily step count and goal.
 */
public class TodayStepsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnDataPointListener {

    static final int COL_HEDGEHOG_ID = 0;
    static final int COL_HEDGEHOG_NAME = 1;
    static final int COL_HEDGEHOG_IMAGE_NAME = 2;
    static final int COL_HEDGEHOG_SILHOUETTE_IMAGE_NAME = 3;
    static final int COL_HEDGEHOG_DESCRIPTION = 4;
    static final int COL_HEDGEHOG_HAPPINESS_LEVEL = 5;
    static final int COL_HEDGEHOG_FITNESS_LEVEL = 6;
    static final int COL_HEDGEHOG_UNLOCK_STATUS = 7;
    static final int COL_HEDGEHOG_SELECTED_STATUS = 8;

    private static final int SELECTED_HEDGEHOG_LOADER = 100;
    private static final int REQUEST_OAUTH = 1;
    private static final String SENSORS_AUTH_PENDING = "sensors_auth_state_pending";
    private static final String RECORDING_AUTH_PENDING = "recording_auth_state_pending";
    private static final String TODAY_STEP_TOTAL = "todayStepTotal";
    private final String LOG_TAG = TodayStepsFragment.class.getSimpleName();
    private boolean sensorsAuthInProgress = false;
    private boolean recordingAuthInProgress = false;
    private GoogleApiClient mApiClient;

    private Activity activity;
    private int todayStepCount = 0;
    private int dailyStepGoal = 0;
    private ImageView hedgehogImageView;
    private TextView todayStepsTextView;
    private TextView dailyStepGoalTextView;
    private Hedgehog selectedHedgehog;
    private Drawable hedgehogDrawable;

    public TodayStepsFragment() {
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
            fetchHedgehogs();
        }

        if (savedInstanceState != null) {
            sensorsAuthInProgress = savedInstanceState.getBoolean(SENSORS_AUTH_PENDING);
            recordingAuthInProgress = savedInstanceState.getBoolean(RECORDING_AUTH_PENDING);
            todayStepCount = savedInstanceState.getInt(TODAY_STEP_TOTAL);
        }

        buildSensorsApiClient();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_todays_steps, container, false);
        hedgehogImageView = (ImageView) rootView.findViewById(R.id.hedgehog_image);
        todayStepsTextView = (TextView) rootView.findViewById(R.id.today_step_count);
        dailyStepGoalTextView = (TextView) rootView.findViewById(R.id.today_step_goal);

        if (todayStepCount > 0) {
            updateStepCountTextView(todayStepCount);
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
        String dailyStepGoalStr = prefs.getString(getContext().getString(R.string.pref_step_goal_key),
                getContext().getString(R.string.pref_step_goal_default));
        dailyStepGoal = 0;

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
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            activity = (Activity) context;
        }

    }

    private void fetchHedgehogs() {
        FetchHedgehogsDataTask hedgehogsDataTasks = new FetchHedgehogsDataTask(getActivity());
        hedgehogsDataTasks.execute();
    }

    private void buildSensorsApiClient() {
        mApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        // Request steps taken today so far from the Google Fit History API.
                        getTodaysStepCount();

                        // Subscribe to the Google Fit Recordings API for TYPE_STEP_COUNT_DELTA data.
                        subscribeToRecordingApi();

                        // Find and list sensors data sources using the Google Fit Sensors API for
                        // TYPE_STEP_COUNT_DELTA data.
                        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                                .setDataTypes(DataType.TYPE_STEP_COUNT_DELTA)
                                .setDataSourceTypes(DataSource.TYPE_RAW, DataSource.TYPE_DERIVED)
                                .build();

                        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
                            @Override
                            public void onResult(DataSourcesResult dataSourcesResult) {
                                for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                                    Log.d(LOG_TAG, "Data source: " + dataSource.getDevice());

                                    if (DataType.TYPE_STEP_COUNT_DELTA.equals(dataSource.getDataType())) {
                                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_DELTA);
                                    }
                                }
                            }
                        };

                        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
                                .setResultCallback(dataSourcesResultCallback);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .enableAutoManage(getActivity(), 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        if (!sensorsAuthInProgress) {
                            try {
                                sensorsAuthInProgress = true;
                                connectionResult.startResolutionForResult(getActivity(), REQUEST_OAUTH);
                            } catch (IntentSender.SendIntentException e) {

                            }
                        } else {
                            Log.e(LOG_TAG, "sensorsAuthInProgress");
                        }
                    }
                })
                .build();
    }

    private void getTodaysStepCount() {
        PendingResult<DailyTotalResult> stepsResult = Fitness.HistoryApi
                .readDailyTotal(mApiClient, DataType.TYPE_STEP_COUNT_DELTA);
        stepsResult.setResultCallback(new ResultCallback() {
            @Override
            public void onResult(Result result) {
                DailyTotalResult dailyTotalResult = (DailyTotalResult) result;
                if (result.getStatus().isSuccess()) {
                    DataSet totalSet = dailyTotalResult.getTotal();
                    todayStepCount = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                    Log.d(LOG_TAG, "Daily step total retrieved from history API. Today's steps = " + Integer.toString(todayStepCount));
                    updateStepCountTextView(todayStepCount);
                }
            }
        });
    }

    public void subscribeToRecordingApi() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.RecordingApi.subscribe(mApiClient, DataType.TYPE_STEP_COUNT_DELTA)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            if (status.getStatusCode()
                                    == FitnessStatusCodes.SUCCESS_ALREADY_SUBSCRIBED) {
                                Log.i(LOG_TAG, "Existing subscription for activity detected.");
                            } else {
                                Log.i(LOG_TAG, "Successfully subscribed!");
                            }
                        } else {
                            Log.i(LOG_TAG, "There was a problem subscribing.");
                        }
                    }
                });
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        SensorRequest request = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(dataType)
                .setSamplingRate(3, TimeUnit.SECONDS)
                .build();

        Fitness.SensorsApi.add(mApiClient, request, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e(LOG_TAG, "SensorApi successfully added");
                        }
                    }
                });
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
        for (final Field field : dataPoint.getDataType().getFields()) {
            final Value value = dataPoint.getValue(field);

            if (activity == null) {
                Log.e(LOG_TAG, "activity == null");
                return;
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "DELTA STEPS " + Integer.toString(value.asInt()));
                    if (value.asInt() > 0) {
                        todayStepCount += value.asInt();
                        updateStepCountTextView(todayStepCount);
                    }
                }
            });
        }
    }

    private void updateStepCountTextView(int currentStepCount) {
        todayStepsTextView.setText(String.format("%,d", currentStepCount));
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SENSORS_AUTH_PENDING, sensorsAuthInProgress);
        outState.putBoolean(RECORDING_AUTH_PENDING, recordingAuthInProgress);
        outState.putInt(TODAY_STEP_TOTAL, todayStepCount);
    }
}
