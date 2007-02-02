package org.geotracing.test.postgis;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.keyworx.oase.util.SuiteRunner;

/**
 * Runs all unit tests for Oase-PostGIS service API.
 *
 * @author Just van den Broecke
 */
public class AllTests extends SuiteRunner {
	public static void main(String[] args) {
		new AllTests().run();
		System.exit(0);
	}

	/** Create the test suite. */
	protected Test createSuite() {
		// Create the test suite using Class objects
		TestSuite suite = new TestSuite("PostGIS Tests");
		suite.addTestSuite(BasicsTest.class);
		suite.addTestSuite(SpatialQueryTest.class);
		return suite;
	}

}


