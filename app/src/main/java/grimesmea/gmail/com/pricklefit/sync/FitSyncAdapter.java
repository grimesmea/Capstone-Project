package grimesmea.gmail.com.pricklefit.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import grimesmea.gmail.com.pricklefit.AppStateDTO;
import grimesmea.gmail.com.pricklefit.MainActivity;
import grimesmea.gmail.com.pricklefit.R;
import grimesmea.gmail.com.pricklefit.data.HedgehogContract;
import grimesmea.gmail.com.pricklefit.widget.StepCountWidgetIntentService;

public class FitSyncAdapter extends AbstractThreadedSyncAdapter
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public static final String ACTION_DATA_UPDATED =
            "grimesmea.gmail.com.pricklefit.ACTION_DATA_UPDATED";
    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 3 = 3 minutes
    public static final int SYNC_INTERVAL = 60 * 3;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    public static final int NOTIFICATION_ID = 1;
    public final String LOG_TAG = FitSyncAdapter.class.getSimpleName();
    Calendar cal;
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy  HH:mm");
    private AppStateContentObserver appStateContentObserver;
    private GoogleApiClient mGoogleApiClient;
    private int todayStepCount;
    private long goalHalfMetNotificationTimestamp;
    private long goalMetNotificationTimestamp;
    private int dailyStepGoal;
    private boolean notificationsEnabled;

    public FitSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);

        cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        appStateContentObserver = new AppStateContentObserver(new Handler(), this);
        getContext().getContentResolver().registerContentObserver(
                HedgehogContract.AppStateEntry.CONTENT_URI,
                false,
                appStateContentObserver);

        Cursor appSateCursor = getContext().getContentResolver().query(
                HedgehogContract.AppStateEntry.CONTENT_URI,
                null,
                null,
                null,
                null);
        if (appSateCursor.moveToFirst()) {
            AppStateDTO appStateDTO = new AppStateDTO(appSateCursor);
            todayStepCount = appStateDTO.getCurrentDailyStepCount();
            dailyStepGoal = appStateDTO.getDailyStepGoal();
            notificationsEnabled = appStateDTO.isNotificationsEnabled();
            goalMetNotificationTimestamp = appStateDTO.getGoalMetNotificationTimestamp();
            goalHalfMetNotificationTimestamp = appStateDTO.getGoalHalfMetNotificationTimestamp();
        }
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
                    int newTodayStepCount = totalSet.isEmpty()
                            ? 0
                            : totalSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                    Log.d(LOG_TAG, "Daily step total retrieved from history API. Today's steps = "
                            + newTodayStepCount);
                    todayStepCount = newTodayStepCount;
                    updateTodayStepCount();
                    checkForNotification();
                    updateWidgets();
                }
                mGoogleApiClient.disconnect();
            }
        });
    }

    private void updateTodayStepCount() {
        Log.d(LOG_TAG, "Updating current step count to " + todayStepCount);
        ContentValues currentDailyStepCountValue = new ContentValues();
        currentDailyStepCountValue.put(
                HedgehogContract.AppStateEntry.COLUMN_CURRENT_DAILY_STEP_TOTAL, todayStepCount);

        // Update current daily step count in database.
        if (currentDailyStepCountValue.size() > 0) {
            getContext().getContentResolver().update(
                    HedgehogContract.AppStateEntry.CONTENT_URI,
                    currentDailyStepCountValue,
                    null,
                    null);
        }
    }

    private void updateWidgets() {
        StepCountWidgetIntentService.updateWidget(todayStepCount);

        Context context = getContext();
        Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                .setPackage(context.getPackageName());
        context.sendBroadcast(dataUpdatedIntent);
    }

    private void checkForNotification() {
        if (notificationsEnabled == false) {
            return;
        }
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Log.d(LOG_TAG, "checking for notification.");
        if (todayStepCount >= dailyStepGoal && cal.getTimeInMillis() != goalMetNotificationTimestamp) {
            String notificationTitle = getContext().getString(R.string.goal_met_notification_title);
            String notificationContent =
                    getContext().getString(R.string.goal_met_notification_content_start) + " " +
                            String.format("%,d", dailyStepGoal) + " " +
                            getContext().getString(R.string.goal_met_notification_content_end);
            sendNotification(notificationTitle, notificationContent);

            setGoalMetNotificationTimestamp(cal.getTimeInMillis());
        } else if (todayStepCount >= (dailyStepGoal / 2)
                && cal.getTimeInMillis() != goalMetNotificationTimestamp
                && cal.getTimeInMillis() != goalHalfMetNotificationTimestamp) {
            String notificationTitle = getContext().getString(R.string.goal_halfway_met_notification_title);
            String notificationContent =
                    getContext().getString(R.string.goal_halfway_met_notification_content_start) + " " +
                            String.format("%,d", dailyStepGoal) + " " +
                            getContext().getString(R.string.goal_halfway_met_notification_content_end);
            sendNotification(notificationTitle, notificationContent);

            setGoalHalfMetNotificationTimestamp(cal.getTimeInMillis());
        }
    }

    private void setGoalMetNotificationTimestamp(long newGoalMetNotificationTimestamp) {
        Log.d(LOG_TAG, "updating goal met timestamp to " + simpleDateFormat.format(newGoalMetNotificationTimestamp));
        goalMetNotificationTimestamp = newGoalMetNotificationTimestamp;
        ContentValues goalMetNotificationTimestampCountValue = new ContentValues();
        goalMetNotificationTimestampCountValue.put(
                HedgehogContract.AppStateEntry.COLUMN_GOAL_MET_NOTIFICATION_TIMESTAMP, goalMetNotificationTimestamp);

        // Update goal met timestamp in database.
        if (goalMetNotificationTimestampCountValue.size() > 0) {
            getContext().getContentResolver().update(
                    HedgehogContract.AppStateEntry.CONTENT_URI,
                    goalMetNotificationTimestampCountValue,
                    null,
                    null);
        }
        Log.d(LOG_TAG, "goalMetNotificationTimestamp is now " + simpleDateFormat.format(goalMetNotificationTimestamp));
    }

    private void setGoalHalfMetNotificationTimestamp(long newGoalHalfMetNotificationTimestamp) {
        Log.d(LOG_TAG, "updating goal half met timestamp to " + simpleDateFormat.format(newGoalHalfMetNotificationTimestamp));
        goalHalfMetNotificationTimestamp = newGoalHalfMetNotificationTimestamp;
        ContentValues goalHalfMetNotificationTimestampCountValue = new ContentValues();
        goalHalfMetNotificationTimestampCountValue.put(
                HedgehogContract.AppStateEntry.COLUMN_GOAL_HALF_MET_NOTIFICATION_TIMESTAMP, goalHalfMetNotificationTimestamp);

        // Update goal met timestamp in database.
        if (goalHalfMetNotificationTimestampCountValue.size() > 0) {
            getContext().getContentResolver().update(
                    HedgehogContract.AppStateEntry.CONTENT_URI,
                    goalHalfMetNotificationTimestampCountValue,
                    null,
                    null);
        }
        Log.d(LOG_TAG, "goalHalfMetNotificationTimestamp is now " + simpleDateFormat.format(goalHalfMetNotificationTimestamp));
    }

    private void sendNotification(String title, String content) {
        Log.d(LOG_TAG, "sending notification!");
        NotificationManager mNotificationManager =
                (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        PendingIntent contentIntent =
                PendingIntent.getActivity(getContext(), 0, new Intent(getContext(), MainActivity.class), 0);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getContext())
                        .setSmallIcon(R.drawable.ic_hedgehog)
                        .setContentTitle(title)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(title))
                        .setContentText(content)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    public void checkForUpdatedDailyStepGoal() {
        Cursor appSateCursor = getContext().getContentResolver().query(
                HedgehogContract.AppStateEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (appSateCursor.moveToFirst()) {
            AppStateDTO appStateDTO = new AppStateDTO(appSateCursor);
            int currentDailyStepGoal = appStateDTO.getDailyStepGoal();
            if (currentDailyStepGoal != dailyStepGoal) {
                updateDailyStepGoal(currentDailyStepGoal);
            }
        }
    }

    private void updateDailyStepGoal(int newDailyStepGoal) {
        if (todayStepCount < (newDailyStepGoal / 2)) {
            setGoalHalfMetNotificationTimestamp(0);
        }
        if (todayStepCount < newDailyStepGoal) {
            setGoalMetNotificationTimestamp(0);
        }
        dailyStepGoal = newDailyStepGoal;
        checkForNotification();
    }

    public void checkForUpdateNotificationEnabledStatus() {
        Cursor appSateCursor = getContext().getContentResolver().query(
                HedgehogContract.AppStateEntry.CONTENT_URI,
                null,
                null,
                null,
                null);

        if (appSateCursor.moveToFirst()) {
            AppStateDTO appStateDTO = new AppStateDTO(appSateCursor);
            notificationsEnabled = appStateDTO.isNotificationsEnabled();
        }
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
