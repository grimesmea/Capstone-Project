package grimesmea.gmail.com.pricklefit.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;

/**
 * Manages a local database for hedgehog data.
 */
public class HedgehogDbHelper extends SQLiteOpenHelper {
    static final String DATABASE_NAME = "hedgehogs.db";
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    public HedgehogDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create a table to hold hedgehogs.
        final String SQL_CREATE_HEDGEHOG_TABLE = "CREATE TABLE " + HedgehogContract.HedgehogsEntry.TABLE_NAME + " (" +
                HedgehogsEntry._ID + " INTEGER PRIMARY KEY," +
                HedgehogsEntry.COLUMN_NAME + " TEXT UNIQUE NOT NULL, " +
                HedgehogsEntry.COLUMN_IMAGE_NAME + " TEXT UNIQUE NOT NULL, " +
                HedgehogsEntry.COLUMN_SILHOUETTE_IMAGE_NAME + " TEXT UNIQUE NOT NULL, " +
                HedgehogsEntry.COLUMN_DESCRIPTION + " TEXT NOT NULL, " +
                HedgehogsEntry.COLUMN_HAPPINESS_LEVEL + " INTEGER NOT NULL, " +
                HedgehogsEntry.COLUMN_FITNESS_LEVEL + " INTEGER NOT NULL, " +
                HedgehogsEntry.COLUMN_UNLOCK_STATUS + " INTEGER NOT NULL, " +
                HedgehogsEntry.COLUMN_SELECTED_STATUS + " INTEGER NOT NULL " +
                " );";

        sqLiteDatabase.execSQL(SQL_CREATE_HEDGEHOG_TABLE);
    }

    /**
     * For now, drop the table if the database is update. If a future version does introduce
     * changes, this will need to be updated to preserve data currently in the table, specifically
     * COLUMN_HAPPINESS_LEVEL which is stores user progress.
     */
    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + HedgehogsEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
