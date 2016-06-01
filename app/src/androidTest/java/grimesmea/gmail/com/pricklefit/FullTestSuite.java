package grimesmea.gmail.com.pricklefit;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Runs all tests in all packages in this directory.
 */
public class FullTestSuite extends TestSuite {
    public FullTestSuite() {
        super();
    }

    public static Test suite() {
        return new TestSuiteBuilder(FullTestSuite.class)
                .includeAllPackagesUnderHere().build();
    }
}