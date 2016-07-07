package grimesmea.gmail.com.pricklefit.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class FitSyncService extends Service {

    private static final Object fitAdapterLock = new Object();
    private static FitSyncAdapter fitSyncAdapter = null;
    private final String LOG_TAG = FitSyncService.class.getSimpleName();

    @Override
    public void onCreate() {
        Log.d(LOG_TAG, "onCreate - FitSyncService");
        synchronized (fitAdapterLock) {
            if (fitSyncAdapter == null) {
                fitSyncAdapter = new FitSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return fitSyncAdapter.getSyncAdapterBinder();
    }

}
