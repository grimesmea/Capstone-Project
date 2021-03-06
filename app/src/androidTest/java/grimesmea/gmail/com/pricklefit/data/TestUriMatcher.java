package grimesmea.gmail.com.pricklefit.data;

import android.content.UriMatcher;
import android.net.Uri;
import android.test.AndroidTestCase;

import grimesmea.gmail.com.pricklefit.data.HedgehogContract.HedgehogsEntry;

public class TestUriMatcher extends AndroidTestCase {

    private static final Uri TEST_HEDGEHOGS_DIR = HedgehogContract.HedgehogsEntry.CONTENT_URI;
    private static final Uri TEST_HEDGEHOG_DIR = HedgehogsEntry.buildHedgehogUri(1);
    private static final Uri TEST_UNLOCKED_HEDGEHOGS_DIR = HedgehogsEntry.buildUnlockedHedgehogsUri();
    private static final Uri TEST_SELECTED_HEDGEHOG_DIR = HedgehogsEntry.buildSelectedHedgehogUri();

    public void testUriMatcher() {
        UriMatcher testUriMatcher = HedgehogProvider.buildUriMatcher();

        assertEquals("Error: The HEDGEHOGS URI was matched incorrectly.",
                HedgehogProvider.HEDGEHOGS, testUriMatcher.match(TEST_HEDGEHOGS_DIR));
        assertEquals("Error: The  URI was matched incorrectly.",
                HedgehogProvider.HEDGEHOG, testUriMatcher.match(TEST_HEDGEHOG_DIR));
        assertEquals("Error: The UNLOCKED_HEDGEHOGS URI was matched incorrectly.",
                HedgehogProvider.UNLOCKED_HEDGEHOGS, testUriMatcher.match(TEST_UNLOCKED_HEDGEHOGS_DIR));
        assertEquals("Error: The SELECTED_HEDGEHOG URI was matched incorrectly.",
                HedgehogProvider.SELECTED_HEDGEHOG, testUriMatcher.match(TEST_SELECTED_HEDGEHOG_DIR));
    }
}
