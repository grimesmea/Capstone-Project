package grimesmea.gmail.com.pricklefit.data;

import android.content.ComponentName;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.AppStateEntry;
import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;

public class TestProvider extends AndroidTestCase {

    public static final String LOG_TAG = TestProvider.class.getSimpleName();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        TestUtilities.deleteRecordsFromProvider(mContext);
    }

    public void testDeleteRecordsFromProvider() {
        TestUtilities.deleteRecordsFromProvider(mContext);

        Cursor cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Hedgehogs table during delete", 0, cursor.getCount());

        cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from App State table during delete", 0, cursor.getCount());
        cursor.close();
    }

    public void testProviderRegistry() {
        PackageManager pm = mContext.getPackageManager();

        ComponentName componentName = new ComponentName(mContext.getPackageName(),
                HedgehogProvider.class.getName());
        try {
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            assertEquals("Error: HedgehogProvider registered with authority: " + providerInfo.authority +
                            " instead of authority: " + HedgehogContract.CONTENT_AUTHORITY,
                    providerInfo.authority, HedgehogContract.CONTENT_AUTHORITY);
        } catch (PackageManager.NameNotFoundException e) {
            assertTrue("Error: HedgehogProvider not registered at " + mContext.getPackageName(),
                    false);
        }
    }

    public void testGetType() {
        String hedgehogsUriType = mContext.getContentResolver().getType(HedgehogsEntry.CONTENT_URI);
        assertEquals("Error: the HedgehogsEntry CONTENT_URI should return HedgehogsEntry.CONTENT_TYPE",
                HedgehogContract.HedgehogsEntry.CONTENT_TYPE, hedgehogsUriType);

        String hedgehogUriType = mContext.getContentResolver().getType(HedgehogsEntry.buildHedgehogUri(1));
        assertEquals("Error: the HedgehogsEntry HEDGEHOG_URI should return HedgehogsEntry.CONTENT_ITEM_TYPE",
                HedgehogsEntry.CONTENT_ITEM_TYPE, hedgehogUriType);

        String unlockedHedgehogsUriType = mContext.getContentResolver().getType(HedgehogsEntry.buildUnlockedHedgehogsUri());
        assertEquals("Error: the HedgehogsEntry UNLOCKED_HEDGEHOGS_URI should return HedgehogsEntry.CONTENT_TYPE",
                HedgehogsEntry.CONTENT_TYPE, unlockedHedgehogsUriType);

        String selectedHedgehogUriType = mContext.getContentResolver().getType(HedgehogsEntry.buildSelectedHedgehogUri());
        assertEquals("Error: the HedgehogsEntry SELECTED_HEDGEHOG_URI should return HedgehogsEntry.ITEM_TYPE",
                HedgehogsEntry.CONTENT_ITEM_TYPE, selectedHedgehogUriType);

        String currentAppStateUriType = mContext.getContentResolver().getType(AppStateEntry.CONTENT_URI);
        assertEquals("Error: the AppStateEntry CONTENT_URI should return AppStateEntry.ITEM_TYPE",
                AppStateEntry.CONTENT_ITEM_TYPE, currentAppStateUriType);
    }

    public void testInsertHedgehogs() {
        ContentValues testValues = TestUtilities.createHedgehogValues(2);

        TestUtilities.deleteHedgehogRecordsFromProvider(mContext);
        long rowId = TestUtilities.insertHedgehogsIntoProvider(mContext, testValues);

        assertTrue(rowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("hedgehog update ", cursor, testValues);

        cursor.close();
    }

    public void testInsertAppState() {
        ContentValues testValues = TestUtilities.createAppStateValues(1);

        TestUtilities.deleteRecordsFromProvider(mContext);
        long rowId = TestUtilities.insertAppStateIntoProvider(mContext, testValues);

        assertTrue(rowId != -1);

        Cursor cursor = mContext.getContentResolver().query(
                AppStateEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("app state update ", cursor, testValues);

        cursor.close();
    }

    public void testHedgehogsQuery() {
        HedgehogDbHelper dbHelper = new HedgehogDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        TestUtilities.deleteHedgehogRecordsFromProvider(mContext);
        ContentValues testValues = TestUtilities.createHedgehogValues();

        long rowId = db.insert(HedgehogsEntry.TABLE_NAME, null, testValues);
        assertTrue("Error: Failed to insert test hedgehog values", rowId != -1);

        // Test with HEDGEHOGS_URI
        Cursor cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("basic hedgehogs query ", cursor, testValues);

        // Test with HEDGEHOG_URI
        cursor = mContext.getContentResolver().query(
                HedgehogsEntry.buildHedgehogUri(rowId),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("hedgehog by id query ", cursor, testValues);

        // Test with UNLOCKED_HEDGEHOGS_URI
        cursor = mContext.getContentResolver().query(
                HedgehogsEntry.buildUnlockedHedgehogsUri(),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("unlocked hedgehogs query", cursor, testValues);

        // Test with SELECTED_HEDGEHOG_URI
        cursor = mContext.getContentResolver().query(
                HedgehogsEntry.buildSelectedHedgehogUri(),
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("selected hedgehog query", cursor, testValues);

        cursor.close();
        db.close();
    }

    public void testAppStateQuery() {
        HedgehogDbHelper dbHelper = new HedgehogDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        TestUtilities.deleteAppStateRecordsFromProvider(mContext);
        ContentValues testValues = TestUtilities.createAppStateValues();

        long rowId = db.insert(AppStateEntry.TABLE_NAME, null, testValues);
        assertTrue("Error: Failed to insert test app state values", rowId != -1);

        // Test with CURRENT_APP_STATE_URI
        Cursor cursor = mContext.getContentResolver().query(
                AppStateEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("basic current app state query ", cursor, testValues);

        cursor.close();
        db.close();
    }

    public void testUpdateHedgehogs() {
        ContentValues testValues = TestUtilities.createHedgehogValues();

        TestUtilities.deleteHedgehogRecordsFromProvider(mContext);
        long hedgehogRowId = TestUtilities.insertHedgehogsIntoProvider(mContext, testValues);
        assertTrue("Error: Failed to insert test hedgehog values", hedgehogRowId != -1);

        ContentValues updatedValues = new ContentValues(testValues);
        updatedValues.put(HedgehogsEntry._ID, hedgehogRowId);
        updatedValues.put(HedgehogsEntry.COLUMN_NAME, "Updated Hedgehog Name 1");

        Cursor cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.TestContentObserver testContentObserver = TestUtilities.getTestContentObserver();
        cursor.registerContentObserver(testContentObserver);

        // Test with HEDGEHOGS_URI
        int count = mContext.getContentResolver().update(
                HedgehogsEntry.CONTENT_URI,
                updatedValues,
                HedgehogsEntry._ID + "= ?",
                new String[]{Long.toString(hedgehogRowId)}
        );
        assertEquals(1, count);
        testContentObserver.waitForNotificationOrFail();
        cursor.unregisterContentObserver(testContentObserver);

        cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("hedgehogs update ", cursor, updatedValues);

        // Test with HEDGEHOG_URI
        cursor.registerContentObserver(testContentObserver);
        updatedValues.put(HedgehogsEntry.COLUMN_NAME, "Updated Test Hedgehog Name 2");

        count = mContext.getContentResolver().update(
                HedgehogsEntry.buildHedgehogUri(hedgehogRowId),
                updatedValues,
                null,
                null
        );
        assertEquals(1, count);
        testContentObserver.waitForNotificationOrFail();
        cursor.unregisterContentObserver(testContentObserver);

        cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("hedgehog update ", cursor, updatedValues);

        // Test with UNLOCKED_HEDGEHOGS_URI
        cursor.registerContentObserver(testContentObserver);
        updatedValues.put(HedgehogsEntry.COLUMN_NAME, "Updated Test Hedgehog Name 3");

        count = mContext.getContentResolver().update(
                HedgehogsEntry.buildUnlockedHedgehogsUri(),
                updatedValues,
                null,
                null
        );
        assertEquals(1, count);
        testContentObserver.waitForNotificationOrFail();
        cursor.unregisterContentObserver(testContentObserver);

        cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("unlocked hedgehogs update ", cursor, updatedValues);

        // Test with SELECTED_HEDGEHOG_URI
        cursor.registerContentObserver(testContentObserver);
        updatedValues.put(HedgehogsEntry.COLUMN_NAME, "Updated Test Hedgehog Name 3");

        count = mContext.getContentResolver().update(
                HedgehogsEntry.buildSelectedHedgehogUri(),
                updatedValues,
                null,
                null
        );
        assertEquals(1, count);
        testContentObserver.waitForNotificationOrFail();
        cursor.unregisterContentObserver(testContentObserver);

        cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("selected hedgehog update ", cursor, updatedValues);
    }

    public void testUpdateAppState() {
        ContentValues testValues = TestUtilities.createAppStateValues();

        TestUtilities.deleteAppStateRecordsFromProvider(mContext);
        long appStateRowId = TestUtilities.insertAppStateIntoProvider(mContext, testValues);
        assertTrue("Error: Failed to insert test app state values", appStateRowId != -1);

        ContentValues updatedValues = new ContentValues(testValues);
        updatedValues.put(AppStateEntry._ID, appStateRowId);
        updatedValues.put(AppStateEntry.COLUMN_DAILY_STEP_GOAL, 7777);

        Cursor cursor = mContext.getContentResolver().query(
                AppStateEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.TestContentObserver testContentObserver = TestUtilities.getTestContentObserver();
        cursor.registerContentObserver(testContentObserver);

        // Test with CURRENT_APP_STATE_URI
        int count = mContext.getContentResolver().update(
                AppStateEntry.CONTENT_URI,
                updatedValues,
                AppStateEntry._ID + "= ?",
                new String[]{Long.toString(appStateRowId)}
        );
        assertEquals(1, count);
        testContentObserver.waitForNotificationOrFail();
        cursor.unregisterContentObserver(testContentObserver);

        cursor = mContext.getContentResolver().query(
                AppStateEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        TestUtilities.validateCursor("app state update ", cursor, updatedValues);
    }

    public void testDeleteHedgehogs() {
        TestUtilities.TestContentObserver testContentObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(HedgehogsEntry.CONTENT_URI, true, testContentObserver);
        ContentValues testValues = TestUtilities.createHedgehogValues();
        long rowId;

        // Test with HEDGEHOGS_URI
        TestUtilities.deleteHedgehogRecordsFromProvider(mContext);
        rowId = TestUtilities.insertHedgehogsIntoProvider(mContext, testValues);

        mContext.getContentResolver().delete(
                HedgehogsEntry.CONTENT_URI,
                null,
                null
        );
        testContentObserver.waitForNotificationOrFail();

        Cursor cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Hedgehogs table during hedgehogs delete", 0, cursor.getCount());

        // Test with HEDGEHOG_URI
        rowId = TestUtilities.insertHedgehogsIntoProvider(mContext, testValues);

        mContext.getContentResolver().delete(
                HedgehogsEntry.buildHedgehogUri(rowId),
                null,
                null
        );
        testContentObserver.waitForNotificationOrFail();

        cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Hedgehogs table during hedgehog delete", 0, cursor.getCount());

        // Test with UNLOCKED_HEDGEHOGS_URI
        rowId = TestUtilities.insertHedgehogsIntoProvider(mContext, testValues);

        mContext.getContentResolver().delete(
                HedgehogsEntry.buildHedgehogUri(1),
                null,
                null
        );
        testContentObserver.waitForNotificationOrFail();

        cursor = mContext.getContentResolver().query(
                HedgehogsEntry.buildUnlockedHedgehogsUri(),
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from Hedgehogs table during unlocked hedgehogs delete", 0, cursor.getCount());

        mContext.getContentResolver().unregisterContentObserver(testContentObserver);
        testContentObserver.closeHandlerThread();
        cursor.close();
    }

    public void testDeleteAppState() {
        TestUtilities.TestContentObserver testContentObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(AppStateEntry.CONTENT_URI, true, testContentObserver);
        ContentValues testValues = TestUtilities.createAppStateValues();
        long rowId;

        // Test with CURRENT_APP_STATE_URI
        TestUtilities.deleteAppStateRecordsFromProvider(mContext);
        rowId = TestUtilities.insertAppStateIntoProvider(mContext, testValues);

        mContext.getContentResolver().delete(
                AppStateEntry.CONTENT_URI,
                null,
                null
        );
        testContentObserver.waitForNotificationOrFail();

        Cursor cursor = mContext.getContentResolver().query(
                AppStateEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );
        assertEquals("Error: Records not deleted from App State table during app state delete", 0, cursor.getCount());

        mContext.getContentResolver().unregisterContentObserver(testContentObserver);
        testContentObserver.closeHandlerThread();
        cursor.close();
    }

    public void testBulkInsertHedgehogs() {
        ContentValues[] bulkInsertContentValues = TestUtilities.createBulkInsertHedgehogValues();

        TestUtilities.deleteHedgehogRecordsFromProvider(mContext);
        TestUtilities.TestContentObserver testContentObserver = TestUtilities.getTestContentObserver();
        mContext.getContentResolver().registerContentObserver(HedgehogsEntry.CONTENT_URI, true, testContentObserver);

        int insertCount = mContext.getContentResolver().bulkInsert(HedgehogsEntry.CONTENT_URI, bulkInsertContentValues);

        testContentObserver.waitForNotificationOrFail();

        assertEquals(insertCount, TestUtilities.BULK_INSERT_RECORDS_TO_INSERT);

        Cursor cursor = mContext.getContentResolver().query(
                HedgehogsEntry.CONTENT_URI,
                null,
                null,
                null,
                null
        );

        assertEquals(cursor.getCount(), TestUtilities.BULK_INSERT_RECORDS_TO_INSERT);

        cursor.moveToFirst();
        for (int i = 0; i < TestUtilities.BULK_INSERT_RECORDS_TO_INSERT; i++, cursor.moveToNext()) {
            TestUtilities.validateCurrentRecord("bulk insert " + i,
                    cursor, bulkInsertContentValues[i]);
        }

        mContext.getContentResolver().unregisterContentObserver(testContentObserver);
        testContentObserver.closeHandlerThread();
        cursor.close();
    }
}
