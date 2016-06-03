package grimesmea.gmail.com.pricklefit.data;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.test.AndroidTestCase;

import java.util.Map;
import java.util.Set;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;
import grimesmea.gmail.com.pricklefit.utils.PollingCheck;

public class TestUtilities extends AndroidTestCase {
    static final String TEST_HEDGEHOG_NAME = "Test Hedgehog";
    static final String TEST_HEDGEHOG_IMAGE = "/test/imagepath";
    static final String TEST_HEDGEHOG_SILHOUETTE_IMAGE = "/test/silouetteimagepath";
    static final String TEST_HEDGEHOG_DESCRIPTION = "Test description";
    static final int TEST_HEDGEHOG_HAPPINESS_LEVEL = 3;
    static final int TEST_HEDGEHOG_FITNESS_LEVEL = 2;
    static final int TEST_HEDGEHOG_UNLOCK_STATUS = 1;
    static final int TEST_HEDGEHOG_SELECTED_STATUS = 1;

    static final int BULK_INSERT_RECORDS_TO_INSERT = 6;

    static void deleteDatabase(Context context) {
        context.deleteDatabase(HedgehogDbHelper.DATABASE_NAME);
    }

    static void deleteRecordsFromProvider(Context context) {
        context.getContentResolver().delete(
                HedgehogsEntry.CONTENT_URI,
                null,
                null
        );
    }

    static ContentValues createHedgehogValues(int i) {
        ContentValues hedgehogValues = new ContentValues();

        hedgehogValues.put(HedgehogsEntry.COLUMN_NAME, TEST_HEDGEHOG_NAME + i);
        hedgehogValues.put(HedgehogsEntry.COLUMN_IMAGE_NAME, TEST_HEDGEHOG_IMAGE + i);
        hedgehogValues.put(HedgehogContract.HedgehogsEntry.COLUMN_SILHOUETTE_IMAGE_NAME, TEST_HEDGEHOG_SILHOUETTE_IMAGE + i);
        hedgehogValues.put(HedgehogContract.HedgehogsEntry.COLUMN_DESCRIPTION, TEST_HEDGEHOG_DESCRIPTION + i);
        hedgehogValues.put(HedgehogContract.HedgehogsEntry.COLUMN_HAPPINESS_LEVEL, TEST_HEDGEHOG_HAPPINESS_LEVEL);
        hedgehogValues.put(HedgehogContract.HedgehogsEntry.COLUMN_FITNESS_LEVEL, TEST_HEDGEHOG_FITNESS_LEVEL);
        hedgehogValues.put(HedgehogContract.HedgehogsEntry.COLUMN_UNLOCK_STATUS, TEST_HEDGEHOG_UNLOCK_STATUS);
        hedgehogValues.put(HedgehogsEntry.COLUMN_SELECTED_STATUS, TEST_HEDGEHOG_SELECTED_STATUS);

        return hedgehogValues;
    }

    static ContentValues createHedgehogValues() {
        return createHedgehogValues(0);
    }

    static ContentValues[] createBulkInsertHedgehogValues() {
        ContentValues[] returnContentValues = new ContentValues[BULK_INSERT_RECORDS_TO_INSERT];

        for (int i = 0; i < BULK_INSERT_RECORDS_TO_INSERT; i++) {
            ContentValues hedgehogValues = createHedgehogValues(i);
            returnContentValues[i] = hedgehogValues;
        }

        return returnContentValues;
    }


    static long insertHedgehogValuesIntoDb(SQLiteDatabase db, ContentValues contentValues) {
        return db.insert(HedgehogsEntry.TABLE_NAME, null, contentValues);
    }

    static long insertHedgehogsIntoProvider(Context context, ContentValues contentValues) {
        TestUtilities.TestContentObserver testContentObserver = TestUtilities.getTestContentObserver();
        context.getContentResolver().registerContentObserver(HedgehogsEntry.CONTENT_URI, true, testContentObserver);

        Uri hedgehogUri = context.getContentResolver().insert(HedgehogsEntry.CONTENT_URI, contentValues);

        testContentObserver.waitForNotificationOrFail();

        context.getContentResolver().unregisterContentObserver(testContentObserver);
        testContentObserver.closeHandlerThread();

        return ContentUris.parseId(hedgehogUri);
    }

    static void validateCursor(String cursorSource, Cursor valueCursor, ContentValues expectedValues) {
        assertTrue("Error: Empty cursor returned by " + cursorSource, valueCursor.moveToFirst());
        validateCurrentRecord(cursorSource, valueCursor, expectedValues);
        valueCursor.close();
    }

    static void validateCurrentRecord(String cursorSource, Cursor valueCursor, ContentValues expectedValues) {
        Set<Map.Entry<String, Object>> valueSet = expectedValues.valueSet();

        for (Map.Entry<String, Object> entry : valueSet) {
            String columnName = entry.getKey();
            int idx = valueCursor.getColumnIndex(columnName);
            assertFalse("Error: Column '" + columnName + "' not found in " + cursorSource + " records", idx == -1);

            String expectedValue = entry.getValue().toString();
            assertEquals(expectedValue, valueCursor.getString(idx));
        }
    }

    static TestContentObserver getTestContentObserver() {
        return TestContentObserver.getTestContentObserver();
    }

    static class TestContentObserver extends ContentObserver {
        final HandlerThread mHandlerTread;
        boolean mContentChanged;

        private TestContentObserver(HandlerThread ht) {
            super(new Handler(ht.getLooper()));
            mHandlerTread = ht;
        }

        static TestContentObserver getTestContentObserver() {
            HandlerThread ht = new HandlerThread("ContentObserverThread");
            ht.start();
            return new TestContentObserver(ht);
        }

        @Override
        public void onChange(boolean selfChange) {
            onChange(selfChange, null);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            mContentChanged = true;
        }

        public void waitForNotificationOrFail() {
            new PollingCheck(5000) {
                @Override
                protected boolean check() {
                    return mContentChanged;
                }
            }.run();
        }

        public void closeHandlerThread() {
            mHandlerTread.quit();
        }
    }
}
