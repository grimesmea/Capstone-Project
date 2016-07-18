package grimesmea.gmail.com.pricklefit.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.test.AndroidTestCase;

import java.util.HashSet;
import java.util.Set;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.AppStateEntry;
import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;

public class TestDb extends AndroidTestCase {
    public static final String LOG_TAG = TestDb.class.getSimpleName();

    public void setUp() {
        TestUtilities.deleteDatabase(mContext);
    }

    public void testCreateDb() throws Throwable {
        final Set<String> tableNameHashSet = new HashSet<String>();
        tableNameHashSet.add(HedgehogsEntry.TABLE_NAME);
        tableNameHashSet.add(AppStateEntry.TABLE_NAME);

        SQLiteDatabase db = new HedgehogDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        assertTrue("Error: The database has not been created correctly",
                cursor.moveToFirst());

        do {
            tableNameHashSet.remove(cursor.getString(0));
        } while (cursor.moveToNext());
        assertTrue("Error: Database was created without both the hedgehogs and app state entry tables",
                tableNameHashSet.isEmpty());

        cursor.close();
        db.close();
    }

    public void testCreateHedgehogsTable() throws Throwable {
        SQLiteDatabase db = new HedgehogDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor cursor = db.rawQuery("PRAGMA table_info(" + HedgehogsEntry.TABLE_NAME + ")",
                null);
        assertTrue("Error: Unable to query the database for hedgehogs table information",
                cursor.moveToFirst());

        final Set<String> hedgehogsColumnHashSet = new HashSet<String>();
        hedgehogsColumnHashSet.add(HedgehogsEntry._ID);
        hedgehogsColumnHashSet.add(HedgehogsEntry.COLUMN_NAME);
        hedgehogsColumnHashSet.add(HedgehogsEntry.COLUMN_IMAGE_NAME);
        hedgehogsColumnHashSet.add(HedgehogsEntry.COLUMN_SILHOUETTE_IMAGE_NAME);
        hedgehogsColumnHashSet.add(HedgehogsEntry.COLUMN_DESCRIPTION);
        hedgehogsColumnHashSet.add(HedgehogsEntry.COLUMN_HAPPINESS_LEVEL);
        hedgehogsColumnHashSet.add(HedgehogsEntry.COLUMN_FITNESS_LEVEL);
        hedgehogsColumnHashSet.add(HedgehogsEntry.COLUMN_UNLOCK_STATUS);
        hedgehogsColumnHashSet.add(HedgehogsEntry.COLUMN_SELECTED_STATUS);

        int columnNameIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnNameIndex);
            hedgehogsColumnHashSet.remove(columnName);
        } while (cursor.moveToNext());
        assertTrue("Error: The hedgehogs table does not contain all of the required columns",
                hedgehogsColumnHashSet.isEmpty());

        cursor.close();
        db.close();
    }

    public void testCreateAppStateTable() throws Throwable {
        SQLiteDatabase db = new HedgehogDbHelper(
                this.mContext).getWritableDatabase();
        assertEquals(true, db.isOpen());

        Cursor cursor = db.rawQuery("PRAGMA table_info(" + AppStateEntry.TABLE_NAME + ")",
                null);
        assertTrue("Error: Unable to query the database for app state table information",
                cursor.moveToFirst());

        final Set<String> appStateColumnHashSet = new HashSet<String>();
        appStateColumnHashSet.add(AppStateEntry._ID);
        appStateColumnHashSet.add(AppStateEntry.COLUMN_DAILY_STEP_GOAL);
        appStateColumnHashSet.add(AppStateEntry.COLUMN_NOTIFICATIONS_ENABLED_STATUS);
        appStateColumnHashSet.add(AppStateEntry.COLUMN_CURRENT_DAILY_STEP_TOTAL);
        appStateColumnHashSet.add(AppStateEntry.COLUMN_HEDGEHOG_STATE_UPDATE_TIMESTAMP);
        appStateColumnHashSet.add(AppStateEntry.COLUMN_GOAL_MET_NOTIFICATION_TIMESTAMP);
        appStateColumnHashSet.add(AppStateEntry.COLUMN_GOAL_HALF_MET_NOTIFICATION_TIMESTAMP);

        int columnNameIndex = cursor.getColumnIndex("name");
        do {
            String columnName = cursor.getString(columnNameIndex);
            appStateColumnHashSet.remove(columnName);
        } while (cursor.moveToNext());
        assertTrue("Error: The app state table does not contain all of the required columns",
                appStateColumnHashSet.isEmpty());

        cursor.close();
        db.close();
    }

    public long testInsertHedgehog() {
        HedgehogDbHelper dbHelper = new HedgehogDbHelper(mContext);
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues testHedgehogValues = TestUtilities.createHedgehogValues();

        long hedgehogRowId = TestUtilities.insertHedgehogValuesIntoDb(db, testHedgehogValues);
        assertTrue("Error: Failed to insert test hedgehog values", hedgehogRowId != -1);

        Cursor cursor = db.query(
                HedgehogsEntry.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );
        assertTrue("Error: Query returned no records", cursor.moveToFirst());


        TestUtilities.validateCurrentRecord("testInsertHedgehog",
                cursor, testHedgehogValues);
        assertFalse("Error: More than one record returned by query",
                cursor.moveToNext());

        cursor.close();
        db.close();
        return hedgehogRowId;
    }
}
