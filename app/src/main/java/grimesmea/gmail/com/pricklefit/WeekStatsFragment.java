package grimesmea.gmail.com.pricklefit;


import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.Bucket;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataReadRequest;
import com.google.android.gms.fitness.result.DataReadResult;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;


/**
 * Encapsulates fetching the daily step counts for the past week as well as the average daily step
 * count and displaying it as a {@link android.support.v7.widget.RecyclerView} layout.
 */
public class WeekStatsFragment extends Fragment {

    private static final int REQUEST_OAUTH = 1;
    private static final String SENSORS_AUTH_PENDING = "sensors_auth_state_pending";
    private static final String DAILY_STEP_TOTALS = "daily_step_totals";
    private static final String DAILY_STEP_AVERAGE = "daily_step_average";
    private final String LOG_TAG = WeekStatsFragment.class.getSimpleName();
    RecyclerView mRecyclerView;
    View emptyView;
    TextView dailyAverageStepsView;
    DailyStepsAdapter mDailyStepsAdapter;
    ArrayList<DailyStepsDTO> dailyStepTotals = new ArrayList<DailyStepsDTO>();
    private boolean historyAuthInProgress = false;
    private GoogleApiClient mApiClient;
    private int dailyStepAverageSteps;

    public WeekStatsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            historyAuthInProgress = savedInstanceState.getBoolean(SENSORS_AUTH_PENDING);
            dailyStepTotals = savedInstanceState.getParcelableArrayList(DAILY_STEP_TOTALS);
            dailyStepAverageSteps = savedInstanceState.getInt(DAILY_STEP_AVERAGE);
        }

        buildHistoryApiClient();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_weeks_stats, container, false);

        dailyAverageStepsView = (TextView) rootView.findViewById(R.id.week_average_steps);
        if (dailyStepAverageSteps > 0) {
            dailyAverageStepsView.setText(String.format("%,d", dailyStepAverageSteps));
        }

        mRecyclerView = (RecyclerView) rootView.findViewById(R.id.recyclerview_daily_steps);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        emptyView = rootView.findViewById(R.id.recyclerview_daily_steps_empty);
        mRecyclerView.setHasFixedSize(true);

        SimpleDividerItemDecoration dividerItemDecoration = new SimpleDividerItemDecoration(getContext());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

        mDailyStepsAdapter = new DailyStepsAdapter(getActivity(), dailyStepTotals, emptyView);
        mRecyclerView.setAdapter(mDailyStepsAdapter);

        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(DAILY_STEP_TOTALS, dailyStepTotals);
        outState.putInt(DAILY_STEP_AVERAGE, dailyStepAverageSteps);
        super.onSaveInstanceState(outState);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != mRecyclerView) {
            mRecyclerView.clearOnScrollListeners();
        }
    }

    private void buildHistoryApiClient() {
        mApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        // Request daily step counts for the past 7 days from the Google Fit History API.
                        getWeeksStepCounts();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .enableAutoManage(getActivity(), 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        if (!historyAuthInProgress) {
                            try {
                                historyAuthInProgress = true;
                                connectionResult.startResolutionForResult(getActivity(), REQUEST_OAUTH);
                            } catch (IntentSender.SendIntentException e) {
                                Log.e(LOG_TAG, e.toString());
                            }
                        } else {
                            Log.e(LOG_TAG, "historyAuthInProgress");
                        }
                    }
                })
                .build();
    }

    private void getWeeksStepCounts() {
        // Setting a start and end date using a range of 1 week before this moment.
        Calendar cal = Calendar.getInstance();
        Date now = new Date();
        long endTime;
        long startTime;

        cal.setTime(now);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        endTime = cal.getTimeInMillis();
        cal.add(Calendar.WEEK_OF_YEAR, -1);
        startTime = cal.getTimeInMillis();

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
                .readData(mApiClient, readRequest);
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

                dailyAverageStepsView.setText(String.format("%,d", dailyStepAverageSteps));

                mDailyStepsAdapter = new DailyStepsAdapter(getActivity(), dailyStepTotals, emptyView);
                mRecyclerView.setAdapter(mDailyStepsAdapter);
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

        dailyStepAverageSteps = calculateAverageSteps(dailyStepTotals);
    }

    private int calculateAverageSteps(List<DailyStepsDTO> dailyStepTotals) {
        long totalSteps = 0;
        int totalDays = 0;

        for (DailyStepsDTO dailyStepsDTO : dailyStepTotals) {
            totalSteps += dailyStepsDTO.getSteps();
            totalDays++;
        }

        return (int) totalSteps / totalDays;
    }
}
