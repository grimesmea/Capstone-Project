package grimesmea.gmail.com.pricklefit;


import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataReadResult;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract;
import grimesmea.gmail.com.pricklefit.data.HedgehogContract.AppStateEntry;
import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;
import grimesmea.gmail.com.pricklefit.sync.FitSyncAdapter;


/**
 * Fragment displaying daily step count and goal. Updating of hedgehog "states" (unlock status,
 * happiness level) are also performed on a daily basis.
 */
public class TodayStepsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>,
        OnDataPointListener {

    static final int COL_HEDGEHOG_ID = 0;
    static final int COL_HEDGEHOG_NAME = 1;
    static final int COL_HEDGEHOG_IMAGE_NAME = 2;
    static final int COL_HEDGEHOG_SILHOUETTE_IMAGE_NAME = 3;
    static final int COL_HEDGEHOG_DESCRIPTION = 4;
    static final int COL_HEDGEHOG_HAPPINESS_LEVEL = 5;
    static final int COL_HEDGEHOG_FITNESS_LEVEL = 6;
    static final int COL_HEDGEHOG_UNLOCK_STATUS = 7;
    static final int COL_HEDGEHOG_SELECTED_STATUS = 8;

    static final int COL_DAILY_STEP_GOAL = 1;
    static final int COL_NOTIFICATIONS_ENABLED_STATUS = 2;
    static final int COL_CURRENT_DAILY_STEP_TOTAL = 3;
    static final int COL_HEDGEHOG_STATE_UPDATE_TIMESTAMP = 4;
    static final int COL_GOAL_MET_NOTIFICATION_TIMESTAMP = 5;
    static final int COL_GOAL_HALF_MET_NOTIFICATION_TIMESTAMP = 6;

    private static final int SELECTED_HEDGEHOG_LOADER = 100;
    private static final int APP_STATE_LOADER = 200;
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private static final String TODAY_STEP_TOTAL = "todayStepTotal";
    private final String LOG_TAG = TodayStepsFragment.class.getSimpleName();
    Calendar cal = Calendar.getInstance();
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm");
    SharedPreferences sharedPreferences;
    private boolean authInProgress = false;
    private GoogleApiClient mGoogleApiClient;
    private Activity activity;
    private List<Hedgehog> hedgehogs = new ArrayList<Hedgehog>();
    private List<DailyStepsDTO> dailyStepTotals = new ArrayList<DailyStepsDTO>();
    private int todayStepCount;
    private int dailyStepGoal;
    private long hedgehogStateUpdateTimestamp;
    private boolean isNotificationsEnabled;
    private boolean needsToUpdateHedgehogState = false;
    private long todayTimeStamp;
    private LinearLayout stepCountContainer;
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
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
            todayStepCount = savedInstanceState.getInt(TODAY_STEP_TOTAL);
        }

        if (!getActivity().getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        ).moveToNext()) {
            fetchHedgehogs();
        }

        if (!getActivity().getContentResolver().query(
                AppStateEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        ).moveToNext()) {
            fetchAppState();
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        buildGoogleFitApiClient();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            activity = (Activity) context;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_today_steps, container, false);
        todayStepsTextView = (TextView) rootView.findViewById(R.id.today_step_count);
        if (todayStepCount >= 0) {
            Log.d(LOG_TAG, "onCreateView todayStepCount = " + todayStepCount);
            updateStepCountTextView();
        }

        stepCountContainer = (LinearLayout) rootView.findViewById(R.id.step_count_container);
        hedgehogImageView = (ImageView) rootView.findViewById(R.id.hedgehog_image);
        dailyStepGoalTextView = (TextView) rootView.findViewById(R.id.today_step_goal);

        getLoaderManager().initLoader(SELECTED_HEDGEHOG_LOADER, null, this);
        getLoaderManager().initLoader(APP_STATE_LOADER, null, this);

        return rootView;
    }

    private void updateStepCountTextView() {
        Log.d(LOG_TAG, "updateStepCountTextView step count = " + todayStepCount);
        todayStepsTextView.setText(String.format("%,d", todayStepCount));
    }

    private void updateDailyStepGoalTextView() {
        dailyStepGoalTextView.setText(String.format("%,d", dailyStepGoal));
    }

    private void fetchHedgehogs() {
        FetchHedgehogsDataTask hedgehogsDataTasks = new FetchHedgehogsDataTask(getActivity());
        hedgehogsDataTasks.execute();
    }

    private void fetchAppState() {
        FetchAppStateDataTask appStateDataTasks = new FetchAppStateDataTask(getActivity());
        appStateDataTasks.execute();
    }

    private void buildGoogleFitApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Fitness.SENSORS_API)
                .addApi(Fitness.RECORDING_API)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        FitSyncAdapter.initializeSyncAdapter(getActivity());

                        // Subscribe to the Google Fit Recordings API for TYPE_STEP_COUNT_DELTA data.
                        subscribeToRecordingApi();

                        // Subscribe to the Google Fit History API for TYPE_STEP_COUNT_DELTA data.
                        if (needsToUpdateHedgehogState) {
                            getUnresolvedDaysStepCounts();
                        }

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

                        Fitness.SensorsApi.findDataSources(mGoogleApiClient, dataSourceRequest)
                                .setResultCallback(dataSourcesResultCallback);
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        // If your connection to the sensor gets lost at some point,
                        // you'll be able to determine the reason and react to it here.
                        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                            Log.i(LOG_TAG, "Connection lost. Cause: Network Lost.");
                        } else if (i
                                == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                            Log.i(LOG_TAG,
                                    "Connection lost. Reason: Service Disconnected");
                        }
                    }
                })
                .enableAutoManage(getActivity(), 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.i(LOG_TAG, "Google Play services connection failed. Cause: " +
                                connectionResult.toString());
                        GoogleApiAvailability googleApiAvailability =
                                GoogleApiAvailability.getInstance();
                        String googleApiErrorMessage = googleApiAvailability
                                .getErrorString(connectionResult.getErrorCode());
                        Snackbar snackbar = Snackbar.make(
                                stepCountContainer,
                                "Exception while connecting to Google Play services: " +
                                        googleApiErrorMessage,
                                Snackbar.LENGTH_INDEFINITE);
                        View view = snackbar.getView();
                        TextView textView = (TextView) view.findViewById(android.support.design.R.id.snackbar_text);
                        textView.setTextColor(ContextCompat.getColor(getContext(), R.color.light_grey));
                        snackbar.show();

                        if (!authInProgress) {
                            try {
                                authInProgress = true;
                                connectionResult.startResolutionForResult(getActivity(), REQUEST_OAUTH);
                            } catch (IntentSender.SendIntentException e) {
                                Log.e(LOG_TAG, e.getMessage());
                            }
                        } else {
                            Log.e(LOG_TAG, "authInProgress");
                        }
                    }
                })
                .build();
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        SensorRequest request = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(dataType)
                .setSamplingRate(3, TimeUnit.SECONDS)
                .build();

        Fitness.SensorsApi.add(mGoogleApiClient, request, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.d(LOG_TAG, "Sensors Api successfully added");
                        }
                    }
                });
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
        Log.d(LOG_TAG, "SensorsApi onDataPoint");
        for (final Field field : dataPoint.getDataType().getFields()) {
            final Value value = dataPoint.getValue(field);

            if (activity == null) {
                return;
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "Delta step = " + Integer.toString(value.asInt()));
                    if (value.asInt() > 0) {
                        todayStepCount += value.asInt();
                        updateStepCountTextView();
                        Log.d(LOG_TAG, "todayStepCount = " + Integer.toString(value.asInt()));
                    }
                }
            });
        }
    }

    public void subscribeToRecordingApi() {
        // To create a subscription, invoke the Recording API. As soon as the subscription is
        // active, fitness data will start recording.
        Fitness.RecordingApi.subscribe(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA)
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

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case SELECTED_HEDGEHOG_LOADER:
                return new CursorLoader(
                        getContext(),
                        HedgehogsEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            case APP_STATE_LOADER:
                return new CursorLoader(
                        getContext(),
                        AppStateEntry.CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            default:
                return null;
        }

    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(LOG_TAG, "loader.getId() = " + loader.getId());
        switch (loader.getId()) {
            case SELECTED_HEDGEHOG_LOADER:
                if (data.moveToFirst()) {
                    do {
                        Hedgehog hedgehog = new Hedgehog(data);
                        hedgehogs.add(hedgehog);

                        if (hedgehog.getIsSelected()) {
                            selectedHedgehog = hedgehog;
                            int hedgehogImageResource = getResources().getIdentifier(selectedHedgehog.getImageName(), "drawable", getContext().getPackageName());

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                                hedgehogDrawable = getResources().getDrawable(hedgehogImageResource, getContext().getTheme());
                            } else {
                                hedgehogDrawable = getResources().getDrawable(hedgehogImageResource);
                            }

                            hedgehogImageView.setImageDrawable(hedgehogDrawable);
                        }
                    } while (data.moveToNext());
                }
                break;
            case APP_STATE_LOADER:
                if (data.moveToFirst()) {
                    AppStateDTO appStateDTO = new AppStateDTO(data);

                    todayStepCount = appStateDTO.getCurrentDailyStepCount();
                    Log.d(LOG_TAG, "onLoadFinished, updating step count view to " + todayStepCount);
                    updateStepCountTextView();

                    dailyStepGoal = appStateDTO.getDailyStepGoal();
                    if (dailyStepGoal != 0) {
                        updateDailyStepGoalPref();
                        updateDailyStepGoalTextView();
                    }

                    if (isNotificationsEnabled != appStateDTO.isNotificationsEnabled()) {
                        isNotificationsEnabled = appStateDTO.isNotificationsEnabled();
                        updateNotificationsEnablePref();
                    }

                    hedgehogStateUpdateTimestamp = appStateDTO.getHedgehogStateUpdateTimestamp();
                    updateTodayTimeStamp();
                    if (hedgehogStateUpdateTimestamp != todayTimeStamp &&
                            hedgehogStateUpdateTimestamp != 0) {
                        Log.d(LOG_TAG, "Updating hedgehogStateUpdateTimestamp in Content Provider to " + simpleDateFormat.format(todayTimeStamp));
                        needsToUpdateHedgehogState = true;
                        ContentValues hedgehogStateUpdateTimestampCountValue = new ContentValues();
                        hedgehogStateUpdateTimestampCountValue.put(
                                AppStateEntry.COLUMN_HEDGEHOG_STATE_UPDATE_TIMESTAMP, todayTimeStamp);

                        // Update goal met timestamp in database.
                        if (hedgehogStateUpdateTimestampCountValue.size() > 0) {
                            getContext().getContentResolver().update(
                                    HedgehogContract.AppStateEntry.CONTENT_URI,
                                    hedgehogStateUpdateTimestampCountValue,
                                    null,
                                    null);
                        }
                        Log.d(LOG_TAG, "HedgehogStateUpdateTimestamp in Content Provider is now " + simpleDateFormat.format(todayTimeStamp));
                    }
                }
                break;
            default:
                return;
        }
    }

    private void updateDailyStepGoalPref() {
        sharedPreferences.edit().putString(getString(R.string.pref_step_goal_key),
                String.valueOf(dailyStepGoal))
                .apply();
    }

    private void updateNotificationsEnablePref() {
        Log.d(LOG_TAG, "Updating notifications enabled pref to " + isNotificationsEnabled);
        sharedPreferences.edit().putBoolean(getString(R.string.pref_notifications_enabled_key),
                isNotificationsEnabled)
                .apply();
    }

    private long updateTodayTimeStamp() {
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        todayTimeStamp = cal.getTimeInMillis();
        return todayTimeStamp;
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        hedgehogImageView.setImageDrawable(null);
        selectedHedgehog = null;
        hedgehogDrawable = null;
    }

    private void getUnresolvedDaysStepCounts() {
        long endTime;
        long startTime;

        endTime = updateTodayTimeStamp();
        startTime = hedgehogStateUpdateTimestamp;


        DataReadRequest readRequest = new DataReadRequest.Builder()
                // The data request can specify multiple data types to return, effectively
                // combining multiple data queries into one call.
                // In this example, it's very unlikely that the request is for several hundred
                // datapoints each consisting of a few steps and a timestamp.  The more likely
                // scenario is wanting to see how many steps were walked per day, for 7 days.
                .aggregate(DataType.TYPE_STEP_COUNT_DELTA, DataType.AGGREGATE_STEP_COUNT_DELTA)
                // Analogous to a "Group By" in SQL, defines how data should be aggregated.
                // bucketByTime allows for a time span, whereas bucketBySession would allow
                // bucketing by "sessions", which would need to be defined in code.
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build();


        PendingResult<DataReadResult> stepsResult = Fitness.HistoryApi
                .readData(mGoogleApiClient, readRequest);
        stepsResult.setResultCallback(new ResultCallback() {
            @Override
            public void onResult(Result result) {
                DataReadResult dataReadResult = (DataReadResult) result;
                dailyStepTotals.clear();
                if (result.getStatus().isSuccess()) {
                    if (dataReadResult.getBuckets().size() > 0) {
                        Log.i(LOG_TAG, "Number of returned buckets of DataSets is: "
                                + dataReadResult.getBuckets().size());
                        for (Bucket bucket : dataReadResult.getBuckets()) {
                            List<DataSet> dataSets = bucket.getDataSets();
                            for (DataSet dataSet : dataSets) {
                                extractDailyStepData(dataSet);
                            }
                        }
                    } else if (dataReadResult.getDataSets().size() > 0) {
                        Log.i(LOG_TAG, "Number of returned DataSets is: "
                                + dataReadResult.getDataSets().size());
                        for (DataSet dataSet : dataReadResult.getDataSets()) {
                            extractDailyStepData(dataSet);
                        }
                    }
                }
                updateHedgehogStates();
                needsToUpdateHedgehogState = false;
            }
        });
    }

    private void extractDailyStepData(DataSet dataSet) {
        int dailyStepTotal = 0;

        for (DataPoint dp : dataSet.getDataPoints()) {
            for (Field field : dp.getDataType().getFields()) {
                dailyStepTotal = dp.getValue(field).asInt();
            }
            DailyStepsDTO dailyStepsDTO = new DailyStepsDTO(dp.getStartTime(TimeUnit.MILLISECONDS), dailyStepTotal);
            dailyStepTotals.add(dailyStepsDTO);
        }
    }

    private void updateHedgehogStates() {
        if (dailyStepTotals.size() < 1) {
            return;
        }

        for (Hedgehog hedgehog : hedgehogs) {
            if (hedgehog.getIsUnlocked()) {
                List<Integer> happinessChanges = new ArrayList<Integer>();
                int newHappinessLevel;
                for (DailyStepsDTO dailySteps : dailyStepTotals) {
                    int happinessChange = hedgehog.calculateHappinessChange(dailySteps.getSteps(), dailyStepGoal);
                    happinessChanges.add(happinessChange);
                }
                newHappinessLevel = hedgehog.calculateNewHappinessLevel(happinessChanges);
                hedgehog.updateHappinessLevel(newHappinessLevel, activity);
            } else {
                hedgehog.checkForUnlock(dailyStepTotals, dailyStepGoal, activity);
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "Saving instance state");
        outState.putBoolean(AUTH_PENDING, authInProgress);
        outState.putInt(TODAY_STEP_TOTAL, todayStepCount);
        super.onSaveInstanceState(outState);
    }
}
