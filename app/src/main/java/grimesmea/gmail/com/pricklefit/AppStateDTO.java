package grimesmea.gmail.com.pricklefit;

import android.content.ContentValues;
import android.database.Cursor;

import org.json.JSONException;
import org.json.JSONObject;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract;

/**
 * Created by comrade.marie on 7/14/2016.
 */
public class AppStateDTO {

    final static String DAILY_STEP_GOAL = "daily_step_goal";
    final static String NOTIFICATIONS_ENABLED_STATUS = "notifications_enabled_status";
    final static String CURRENT_DAILY_STEP_TOTAL = "current_daily_step_total";
    final static String HEDGEHOG_STATE_UPDATE_TIMESTAMP = "hedgehog_state_update_timestamp";
    final static String GOAL_MET_NOTIFICATION_TIMESTAMP = "goal_met_notification_timestamp";
    final static String GOAL_HALF_MET_NOTIFICATION_TIMESTAMP = "goal_half_met_notification_timestamp";
    private final String LOG_TAG = AppStateDTO.class.getSimpleName();
    int dailyStepGoal;
    boolean notificationsEnabled;
    int currentDailyStepCount;
    long hedgehogStateUpdateTimestamp;
    long goalMetNotificationTimestamp;
    long goalHalfMetNotificationTimestamp;

    public AppStateDTO(JSONObject appStateJson) throws JSONException {
        this(
                appStateJson.getInt(DAILY_STEP_GOAL),
                appStateJson.getBoolean(NOTIFICATIONS_ENABLED_STATUS),
                appStateJson.getInt(CURRENT_DAILY_STEP_TOTAL),
                appStateJson.getLong(HEDGEHOG_STATE_UPDATE_TIMESTAMP),
                appStateJson.getInt(GOAL_MET_NOTIFICATION_TIMESTAMP),
                appStateJson.getInt(GOAL_HALF_MET_NOTIFICATION_TIMESTAMP)
        );
    }

    public AppStateDTO(Cursor cursor) {
        this(
                cursor.getInt(TodayStepsFragment.COL_DAILY_STEP_GOAL),
                getBooleanValue(cursor.getInt(TodayStepsFragment.COL_NOTIFICATIONS_ENABLED_STATUS)),
                cursor.getInt(TodayStepsFragment.COL_CURRENT_DAILY_STEP_TOTAL),
                cursor.getLong(TodayStepsFragment.COL_HEDGEHOG_STATE_UPDATE_TIMESTAMP),
                cursor.getLong(TodayStepsFragment.COL_GOAL_MET_NOTIFICATION_TIMESTAMP),
                cursor.getLong(TodayStepsFragment.COL_GOAL_HALF_MET_NOTIFICATION_TIMESTAMP)
        );
    }

    public AppStateDTO(int dailyStepGoal, boolean notificationsEnabled,
                       int currentDailyStepCount, long hedgehogStateUpdateTimestamp,
                       long goalMetNotificationTimestamp, long goalHalfMetNotificationTimestamp) {
        this.dailyStepGoal = dailyStepGoal;
        this.notificationsEnabled = notificationsEnabled;
        this.currentDailyStepCount = currentDailyStepCount;
        this.hedgehogStateUpdateTimestamp = hedgehogStateUpdateTimestamp;
        this.goalMetNotificationTimestamp = goalMetNotificationTimestamp;
        this.goalHalfMetNotificationTimestamp = goalHalfMetNotificationTimestamp;
    }

    private static boolean getBooleanValue(int i) {
        return i == 1 ? true : false;
    }

    public ContentValues createContentValues() {
        ContentValues appStateValues = new ContentValues();

        appStateValues.put(HedgehogContract.AppStateEntry.COLUMN_DAILY_STEP_GOAL, dailyStepGoal);
        appStateValues.put(HedgehogContract.AppStateEntry.COLUMN_NOTIFICATIONS_ENABLED_STATUS, notificationsEnabled);
        appStateValues.put(HedgehogContract.AppStateEntry.COLUMN_CURRENT_DAILY_STEP_TOTAL, currentDailyStepCount);
        appStateValues.put(HedgehogContract.AppStateEntry.COLUMN_HEDGEHOG_STATE_UPDATE_TIMESTAMP, hedgehogStateUpdateTimestamp);
        appStateValues.put(HedgehogContract.AppStateEntry.COLUMN_GOAL_MET_NOTIFICATION_TIMESTAMP, goalMetNotificationTimestamp);
        appStateValues.put(HedgehogContract.AppStateEntry.COLUMN_GOAL_HALF_MET_NOTIFICATION_TIMESTAMP, goalHalfMetNotificationTimestamp);

        return appStateValues;
    }

    public int getDailyStepGoal() {
        return dailyStepGoal;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public int getCurrentDailyStepCount() {
        return currentDailyStepCount;
    }

    public long getHedgehogStateUpdateTimestamp() {
        return hedgehogStateUpdateTimestamp;
    }

    public long getGoalMetNotificationTimestamp() {
        return goalMetNotificationTimestamp;
    }

    public long getGoalHalfMetNotificationTimestamp() {
        return goalHalfMetNotificationTimestamp;
    }
}
