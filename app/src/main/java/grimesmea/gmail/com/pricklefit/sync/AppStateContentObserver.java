package grimesmea.gmail.com.pricklefit.sync;

import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;

/**
 * Receives call backs for changes to content in the app state table of the Content Provider.
 */
public class AppStateContentObserver extends ContentObserver {

    FitSyncAdapter fitSyncAdapter;

    public AppStateContentObserver(Handler handler, FitSyncAdapter fitSyncAdapter) {
        super(handler);
        this.fitSyncAdapter = fitSyncAdapter;
    }

    @Override
    public void onChange(boolean selfChange) {
        this.onChange(selfChange, null);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        fitSyncAdapter.checkForUpdatedDailyStepGoal();
        fitSyncAdapter.checkForUpdateNotificationEnabledStatus();
    }
}
