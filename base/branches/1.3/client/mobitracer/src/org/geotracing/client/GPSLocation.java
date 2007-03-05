// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;

/**
 * Holds GPS location data.
 *
 * @author  Just van den Broecke
 * @version $Id$
 */
public class GPSLocation {
	public String data;
	public MFloat lat;
	public MFloat lon;
	public long time = Util.getTime();
}