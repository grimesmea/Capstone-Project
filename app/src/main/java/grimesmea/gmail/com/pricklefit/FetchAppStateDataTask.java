package grimesmea.gmail.com.pricklefit;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract;

/**
 * Gets app state data from JSON asset and inserts data into the PrickleFit Content Provider.
 */
public class FetchAppStateDataTask extends AsyncTask<Void, Void, AppStateDTO> {
    private final String LOG_TAG = FetchAppStateDataTask.class.getSimpleName();
    private final Context context;

    public FetchAppStateDataTask(Context context) {
        this.context = context;
    }

    @Override
    protected AppStateDTO doInBackground(Void... voids) {
        String appStateJsonStr;

        try {
            InputStream is = context.getAssets().open("app_state.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            appStateJsonStr = new String(buffer, "UTF-8");
            Log.d(LOG_TAG, appStateJsonStr);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        try {
            return getAppStateFromJson(appStateJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    private AppStateDTO getAppStateFromJson(String appStateJsonStr) throws JSONException {
        JSONObject appStateJson = new JSONObject(appStateJsonStr);
        JSONObject appState = appStateJson.getJSONObject("app_state");
        AppStateDTO appStateDTO = new AppStateDTO(appState);
        Log.d(LOG_TAG, "App state found");
        return appStateDTO;
    }

    private String insertAppStateDataIntoContentProvider(AppStateDTO appState) {
        Uri insertedRowsUri;
        ContentValues appStateContentValues = new ContentValues(appState.createContentValues());

        insertedRowsUri = context.getContentResolver().
                insert(HedgehogContract.AppStateEntry.CONTENT_URI, appStateContentValues);
        Log.d(LOG_TAG, insertedRowsUri + " row inserted");

        return insertedRowsUri.toString();
    }

    protected void onPostExecute(AppStateDTO result) {
        if (result != null) {
            insertAppStateDataIntoContentProvider(result);
        }
    }
}
