package grimesmea.gmail.com.pricklefit.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import grimesmea.gmail.com.pricklefit.sync.FitSyncAdapter;

/**
 * Provider for a horizontally expandable widget showing today's step count.
 * <p/>
 * Delegates widget updating to {@link StepCountWidgetIntentService} to ensure that
 * data retrieval is done on a background thread.
 */
public class StepCountWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        context.startService(new Intent(context, StepCountWidgetIntentService.class));
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newOptions) {
        context.startService(new Intent(context, StepCountWidgetIntentService.class));
    }

    @Override
    public void onReceive(@NonNull Context context, @NonNull Intent intent) {
        super.onReceive(context, intent);
        if (FitSyncAdapter.ACTION_DATA_UPDATED.equals(intent.getAction())) {
            context.startService(new Intent(context, StepCountWidgetIntentService.class));
        }
    }
}
