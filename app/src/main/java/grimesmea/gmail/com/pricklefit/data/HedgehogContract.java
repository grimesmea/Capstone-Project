package grimesmea.gmail.com.pricklefit.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Defines table and column names for the hedgehog database.
 */
public class HedgehogContract {

    public static final String CONTENT_AUTHORITY = "grimesmea.gmail.com.pricklefit.app";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_HEDGEHOG = "hedgehogs";

    /* Inner class that defines the table contents of the location table */
    public static final class HedgehogsEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_HEDGEHOG).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HEDGEHOG;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_HEDGEHOG;

        public static final String TABLE_NAME = "hedgehogs";
        public static final String COLUMN_NAME = "name";
        public static final String COLUMN_IMAGE_NAME = "image_name";
        public static final String COLUMN_SILHOUETTE_IMAGE_NAME = "silhouette_image_name";
        public static final String COLUMN_DESCRIPTION = "description";
        public static final String COLUMN_HAPPINESS_LEVEL = "happiness_level";
        public static final String COLUMN_FITNESS_LEVEL = "fitness_level";
        public static final String COLUMN_UNLOCK_STATUS = "unlock_status";
        public static final String COLUMN_SELECTED_STATUS = "selected_status";

        public static Uri buildHedgehogUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildUnlockedHedgehogsUri() {
            return CONTENT_URI.buildUpon().appendPath("unlockedHedgehogs").build();
        }

        public static Uri buildSelectedHedgehogUri() {
            return CONTENT_URI.buildUpon().appendPath("selectedHedgehog").build();
        }
    }
}
