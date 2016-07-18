package grimesmea.gmail.com.pricklefit.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.AppStateEntry;
import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;

public class HedgehogProvider extends ContentProvider {


    static final int HEDGEHOGS = 100;
    static final int HEDGEHOG = 101;
    static final int UNLOCKED_HEDGEHOGS = 200;
    static final int SELECTED_HEDGEHOG = 300;
    static final int CURRENT_APP_STATE = 400;
    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private static final String hedgehogSelection =
            HedgehogsEntry.TABLE_NAME + "." +
                    HedgehogsEntry._ID + " = ?";
    private static final String unlockedHedgehogsSelection =
            HedgehogsEntry.TABLE_NAME +
                    "." + HedgehogsEntry.COLUMN_UNLOCK_STATUS + " = 1";
    private static final String selectedHedgehogSelection =
            HedgehogsEntry.TABLE_NAME +
                    "." + HedgehogsEntry.COLUMN_SELECTED_STATUS + " = 1";
    public final String LOG_TAG = HedgehogProvider.class.getSimpleName();
    private HedgehogDbHelper mOpenHelper;

    static UriMatcher buildUriMatcher() {
        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = HedgehogContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, HedgehogContract.PATH_HEDGEHOGS, HEDGEHOGS);
        matcher.addURI(authority, HedgehogContract.PATH_HEDGEHOGS + "/#", HEDGEHOG);
        matcher.addURI(authority, HedgehogContract.PATH_HEDGEHOGS + "/unlockedHedgehogs", UNLOCKED_HEDGEHOGS);
        matcher.addURI(authority, HedgehogContract.PATH_HEDGEHOGS + "/selectedHedgehog", SELECTED_HEDGEHOG);
        matcher.addURI(authority, HedgehogContract.PATH_APP_STATE, CURRENT_APP_STATE);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new HedgehogDbHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case HEDGEHOGS:
                return HedgehogsEntry.CONTENT_TYPE;
            case HEDGEHOG:
                return HedgehogsEntry.CONTENT_ITEM_TYPE;
            case UNLOCKED_HEDGEHOGS:
                return HedgehogsEntry.CONTENT_TYPE;
            case SELECTED_HEDGEHOG:
                return HedgehogsEntry.CONTENT_ITEM_TYPE;
            case CURRENT_APP_STATE:
                return AppStateEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            case HEDGEHOGS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        HedgehogsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case HEDGEHOG: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        HedgehogContract.HedgehogsEntry.TABLE_NAME,
                        projection,
                        hedgehogSelection,
                        new String[]{uri.getPathSegments().get(uri.getPathSegments().size() - 1)},
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case UNLOCKED_HEDGEHOGS: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        HedgehogsEntry.TABLE_NAME,
                        projection,
                        unlockedHedgehogsSelection,
                        null,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            case SELECTED_HEDGEHOG: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        HedgehogsEntry.TABLE_NAME,
                        projection,
                        selectedHedgehogSelection,
                        null,
                        null,
                        null,
                        sortOrder,
                        "1"
                );
                break;
            }
            case CURRENT_APP_STATE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        AppStateEntry.TABLE_NAME,
                        projection,
                        selection,
                        null,
                        null,
                        null,
                        sortOrder
                );
                if (retCursor.getCount() > 1) {
                    Log.e(LOG_TAG,
                            "more than 1 entry for current app state found during query.");
                }
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case HEDGEHOGS: {
                long _id = db.insert(HedgehogsEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = HedgehogsEntry.buildHedgehogUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case CURRENT_APP_STATE: {
                long _id = db.insert(AppStateEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = AppStateEntry.buildAppStateUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // Delete all rows returns the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case HEDGEHOGS: {
                rowsDeleted = db.delete(
                        HedgehogsEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            case HEDGEHOG: {
                rowsDeleted = db.delete(
                        HedgehogsEntry.TABLE_NAME,
                        hedgehogSelection,
                        new String[]{uri.getPathSegments().get(uri.getPathSegments().size() - 1)}
                );
                break;
            }
            case UNLOCKED_HEDGEHOGS: {
                rowsDeleted = db.delete(
                        HedgehogsEntry.TABLE_NAME,
                        unlockedHedgehogsSelection,
                        selectionArgs
                );
                break;
            }
            case CURRENT_APP_STATE: {
                rowsDeleted = db.delete(
                        AppStateEntry.TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case HEDGEHOGS:
                rowsUpdated = db.update(
                        HedgehogsEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case HEDGEHOG: {
                rowsUpdated = db.update(
                        HedgehogsEntry.TABLE_NAME,
                        values,
                        hedgehogSelection,
                        new String[]{uri.getPathSegments().get(uri.getPathSegments().size() - 1)}
                );
                break;
            }
            case UNLOCKED_HEDGEHOGS: {
                rowsUpdated = db.update(
                        HedgehogsEntry.TABLE_NAME,
                        values,
                        unlockedHedgehogsSelection,
                        selectionArgs
                );
                break;
            }
            case SELECTED_HEDGEHOG: {
                rowsUpdated = db.update(
                        HedgehogsEntry.TABLE_NAME,
                        values,
                        selectedHedgehogSelection,
                        selectionArgs
                );
                break;
            }
            case CURRENT_APP_STATE:
                rowsUpdated = db.update(
                        AppStateEntry.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                if (rowsUpdated > 1) {
                    Log.e(LOG_TAG,
                            "more than 1 entry for current app state found and updated during update.");
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount = 0;
        switch (match) {
            case HEDGEHOGS:
                db.beginTransaction();
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(HedgehogsEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                            Log.d("bulkInsert count", Integer.toString(returnCount));
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }
}
