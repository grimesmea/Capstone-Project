package grimesmea.gmail.com.pricklefit.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;

import grimesmea.gmail.com.pricklefit.R;

public class FitSyncAdapter extends AbstractThreadedSyncAdapter
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 3 = 3 minutes
    public static final int SYNC_INTERVAL = 60 * 3;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public final String LOG_TAG = FitSyncAdapter.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;
    private SharedPreferences sharedPrefs;
    private String todayStepsStr;

    public FitSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        sharedPrefs = getContext().getSharedPreferences(
                context.getString(R.string.step_data_prefs), Context.MODE_PRIVATE);
        todayStepsStr = sharedPrefs.getString(
                context.getString(R.string.pref_today_step_count_key),
                context.getString(R.string.pref_today_step_count_default));
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        FitSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority,
                              ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");
        buildGoogleFitApiClient(getContext());
        mGoogleApiClient.connect();
    }

    private void buildGoogleFitApiClient(final Context context) {
        mGoogleApiClient = new GoogleApiClient.Builder(context)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        Log.d(LOG_TAG, "connected to google api client");
                        // Request steps taken today so far from the Google Fit History API.
                        getTodayStepCount();
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
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void getTodayStepCount() {
        PendingResult<DailyTotalResult> stepsResult = Fitness.HistoryApi
                .readDailyTotal(mGoogleApiClient, DataType.TYPE_STEP_COUNT_DELTA);
        stepsResult.setResultCallback(new ResultCallback() {
            @Override
            public void onResult(Result result) {
                DailyTotalResult dailyTotalResult = (DailyTotalResult) result;
                if (result.getStatus().isSuccess()) {
                    DataSet totalSet = dailyTotalResult.getTotal();
                    int todayStepCount = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                    Log.d(LOG_TAG, "Daily step total retrieved from history API. Today's steps = " + Integer.toString(todayStepCount));
                    if (todayStepCount != Integer.parseInt(todayStepsStr)) {
                        updateTodayStepCount(todayStepCount);
                    }
                }
                mGoogleApiClient.disconnect();
            }
        });
    }

    private void updateTodayStepCount(int stepCount) {
        Log.d(LOG_TAG, "updating today step count shared pref to " + stepCount);
        todayStepsStr = Integer.toString(stepCount);
        SharedPreferences.Editor editor = sharedPrefs.edit();
        editor.putString(getContext().getString(R.string.pref_today_step_count_key),
                Integer.toString(stepCount));
        editor.commit();
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, "onConnected: " + connectionHint);
        }
        Log.d(LOG_TAG, "connection succeeded");
    }

    @Override
    public void onConnectionSuspended(int cause) {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, "onConnectionSuspended: " + cause);
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (Log.isLoggable(LOG_TAG, Log.DEBUG)) {
            Log.d(LOG_TAG, "onConnectionFailed: " + connectionResult);
        }
        Log.d(LOG_TAG, "onConnectionFailed " + connectionResult);
    }
}
