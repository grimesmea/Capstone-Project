package grimesmea.gmail.com.pricklefit;

import android.content.ContentValues;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Vector;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract;

/**
 * Gets hedgehog data from JSON asset and inserts data into the Hedgehog Content Provider.
 */
public class FetchHedgehogsDataTask extends AsyncTask<Void, Void, Hedgehog[]> {
    private final String LOG_TAG = FetchHedgehogsDataTask.class.getSimpleName();
    private final Context context;

    public FetchHedgehogsDataTask(Context context) {
        this.context = context;
    }

    @Override
    protected Hedgehog[] doInBackground(Void... voids) {
        String hedgehogsJsonStr = null;

        try {
            InputStream is = context.getAssets().open("hedgehog_data.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            hedgehogsJsonStr = new String(buffer, "UTF-8");
            Log.d(LOG_TAG, hedgehogsJsonStr);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        try {
            return getHedgehogsFromJson(hedgehogsJsonStr);
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }

        return null;
    }

    private Hedgehog[] getHedgehogsFromJson(String hedgehogsJsonStr) throws JSONException {
        JSONObject hedgehogsJson = new JSONObject(hedgehogsJsonStr);
        JSONArray hedgehogsJsonArray = hedgehogsJson.getJSONArray("hedgehogs");
        Hedgehog[] hedgehogsArray = new Hedgehog[hedgehogsJsonArray.length()];

        for (int i = 0; i < hedgehogsJsonArray.length(); i++) {
            JSONObject hedgehogJson = hedgehogsJsonArray.getJSONObject(i);
            hedgehogsArray[i] = new Hedgehog(hedgehogJson);
        }

        Log.d(LOG_TAG, hedgehogsArray.length + " hedgehogs found");
        return hedgehogsArray;
    }

    private int bulkInsertHedgehogsDataIntoContentProvider(Hedgehog[] hedgehogsArray) {
        int insertedRows = 0;

        if (hedgehogsArray.length > 0) {
            List<ContentValues> hedgehogsContentValuesVector = new Vector<ContentValues>(hedgehogsArray.length);
            ContentValues[] hedgehogsContentValuesArray = new ContentValues[hedgehogsArray.length];

            for (Hedgehog hedgehog : hedgehogsArray) {
                ContentValues hedgehogContentValues = new ContentValues(hedgehog.createContentValues());
                ;
                hedgehogsContentValuesVector.add(hedgehogContentValues);
            }
            hedgehogsContentValuesVector.toArray(hedgehogsContentValuesArray);

            insertedRows = context.getContentResolver().
                    bulkInsert(HedgehogContract.HedgehogsEntry.CONTENT_URI, hedgehogsContentValuesArray);
        }

        Log.d(LOG_TAG, insertedRows + " rows inserted");

        return insertedRows;
    }

    protected void onPostExecute(Hedgehog[] result) {
        if (result != null) {
            bulkInsertHedgehogsDataIntoContentProvider(result);
        }
    }
}
