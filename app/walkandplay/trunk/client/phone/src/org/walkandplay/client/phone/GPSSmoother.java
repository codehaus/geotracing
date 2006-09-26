// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.walkandplay.client.phone;

import java.util.Vector;

/**
 * Selects best GPS location from a series of samples.
 * <p>
 * GPS samples tend to have a drift. Some GPS devices produce anomalous
 * "spikes" under shielding conditions. This class acts as a filter to
 * select the best sample from a series.
 * The algorithm is simple: take the average of a series of samples
 * and select the sample closest to the average. Samples (floats) are normalized
 * to long values for ease/efficiency of calculation.
 * </p>
 * 
 * @author Just van den Broecke
 * @version $Id: GPSSmoother.java 8 2006-08-28 15:36:01Z just $
 */
public class GPSSmoother {
	/** To convert float to long for easier calculations. */
	private static final long NORMALIZE_FACT = 1000000L;

	/** Minimum number of samples required for best location calculation. */
	private static final int MIN_SAMPLES = 10;

	/** Maximum (target) number of samples to be held for best location calculation. */
	private static final int MAX_SAMPLES = 15;

	/** Series of GPS locations. */
	private Vector gpsLocations = new Vector(MAX_SAMPLES);

	/** Normalized averages. */
	private long avgLon = 0L;
	private long avgLat = 0L;

	public GPSSmoother() {
	}

	/** Add sample to the series. */
	public void addLocation(GPSLocation aLocation) {
		gpsLocations.addElement(aLocation);

		// No umlimited number of samples
		// Only the ones at the end of the interval are of interest
		if (gpsLocations.size() > MAX_SAMPLES) {
			// Enough samples for calculation
			gpsLocations.removeElementAt(0);
		}
	}

	/** Get best location so far or null if not enough samples. */
	public GPSLocation getBestLocation() {
		int count = gpsLocations.size();
		if (count < MIN_SAMPLES) {
			// Not enough samples for calculation
			return null;
		}

		// Enough samples: calculate average.
		average();

		// Take first sample as initial best location
		GPSLocation bestLocation = (GPSLocation) gpsLocations.elementAt(0);
		long bestLonDiff = avgDiff(avgLon, bestLocation.lon);
		long bestLatDiff = avgDiff(avgLat, bestLocation.lat);

		// Go through other locations, select the best (closest to average).
		GPSLocation nextLocation;
		int bestIndex = 1;
		for (int i=1; i < count; i++) {
			nextLocation = (GPSLocation) gpsLocations.elementAt(i);

			// Calc diff from average
			long lonDiff = avgDiff(avgLon, nextLocation.lon);
			long latDiff = avgDiff(avgLat, nextLocation.lat);

			// If lat and lon closer to avg make new best location
			if (lonDiff <= bestLonDiff && latDiff <= bestLatDiff) {
				bestLonDiff = lonDiff;
				bestLatDiff = latDiff;
				bestIndex = i;
				bestLocation = nextLocation;
			}
		}
		Log.log("cnt=" + count + " bestIdx=" + bestIndex);
		return bestLocation;
	}

	/** Start new series. */
	public void reset() {
		gpsLocations.removeAllElements();
		avgLat = 0L;
		avgLat = 0L;
	}

	/** Calc absolute difference with average for a float. */
	private long avgDiff(long avg, MFloat coord) {
		return Math.abs(avg  - coord.Mul(NORMALIZE_FACT).toLong());
	}

	/** Calculate average for the series. */
	private void average() {
		GPSLocation nextLocation;
		int count = gpsLocations.size();
		long sumLon=0L, sumLat=0L;
		for (int i=0; i < count; i++) {

			nextLocation = (GPSLocation) gpsLocations.elementAt(i);

			// Normalize and add to accumulated (total) lat/lon
			sumLon += nextLocation.lon.Mul(NORMALIZE_FACT).toLong();
			sumLat += nextLocation.lat.Mul(NORMALIZE_FACT).toLong();

		}

		// Calculate (normalized) average
		avgLat = sumLat / count;
		avgLon = sumLon / count;
	}
}