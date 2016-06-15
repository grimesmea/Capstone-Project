package grimesmea.gmail.com.pricklefit;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        OnDataPointListener {

    private static final int REQUEST_OAUTH = 1;
    private static final String SENSORS_AUTH_PENDING = "sensors_auth_state_pending";
    private static final String RECORDING_AUTH_PENDING = "recording_auth_state_pending";
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private boolean sensorsAuthInProgress = false;
    private boolean recordingAuthInProgress = false;
    private GoogleApiClient mApiClient;
    private int todaysStepCount;
    private TextView todaysStepsTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        todaysStepsTextView = (TextView) findViewById(R.id.daily_step_count);
        todaysStepsTextView.setText("0");

        if (savedInstanceState != null) {
            sensorsAuthInProgress = savedInstanceState.getBoolean(SENSORS_AUTH_PENDING);
            recordingAuthInProgress = savedInstanceState.getBoolean(RECORDING_AUTH_PENDING);
        }

        buildSensorsApiClient();
    }

    private void buildSensorsApiClient() {
        mApiClient = new GoogleApiClient.Builder(this)
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

                        // Find and list to sensors data using the Google Fit Sensors API for
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
                .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        if (!sensorsAuthInProgress) {
                            try {
                                sensorsAuthInProgress = true;
                                connectionResult.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
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
                    todaysStepCount = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                    updateStepCountTextView(todaysStepCount);
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
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LOG_TAG, "DELTA STEPS - " + Integer.toString(value.asInt()));
                    todaysStepCount += value.asInt();
                    updateStepCountTextView(todaysStepCount);
                }
            });
        }
    }

    private void updateStepCountTextView(int currentStepCount) {
        todaysStepsTextView.setText(String.format("%,d", currentStepCount));
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_weeks_stats) {
            Intent intent = new Intent(this, StatsActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_hedgie_collection) {
            Intent intent = new Intent(this, HedgehogCollectionActivity.class);
            startActivity(intent);
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(SENSORS_AUTH_PENDING, sensorsAuthInProgress);
        outState.putBoolean(RECORDING_AUTH_PENDING, recordingAuthInProgress);
    }
}
