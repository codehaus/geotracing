// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.gis;


public class GPSSample extends GeoPoint {

	public String rawData = "";

	/**
	 * Accuracy of coordinates. Value 1..100. 100 means perfect
	 */
	public int accuracy;

	public double speed = -1;

	public GPSSample() {
	}

	public GPSSample(String aLatitude, String aLongitude) {
		this(Double.parseDouble(aLatitude), Double.parseDouble(aLongitude));
	}


	public GPSSample(double aLatitude, double aLongitude) {
		this(aLatitude, aLongitude, System.currentTimeMillis());
	}

	public GPSSample(double aLatitude, double aLongitude, long aTimeStamp) {
		this(aLatitude, aLongitude, aTimeStamp, 0.0, 100);
	}

	public GPSSample(double aLatitude, double aLongitude, long aTimeStamp,
					 double anElevation, int anAccuracy) {
		super(aLongitude, aLatitude);
		timestamp = aTimeStamp;
		elevation = anElevation;
		accuracy = anAccuracy;
	}

	/**
	 * Create from sample string (see toString()).
	 */
	public GPSSample(String aSampleString) {
		String[] sampleArray = aSampleString.split("{,}");
		timestamp = Long.parseLong(sampleArray[0]);
		lat = Double.parseDouble(sampleArray[1]);
		lon = Double.parseDouble(sampleArray[2]);
		elevation = Double.parseDouble(sampleArray[3]);
		accuracy = Integer.parseInt(sampleArray[4]);
	}


	public String toString() {
		return "{" + timestamp + "," + lat + "," + lon + "," + elevation + "," + accuracy + "," + speed + "}";
	}


}

/*
* $Log: GPSSample.java,v $
* Revision 1.4  2005/09/29 10:46:56  just
* *** empty log message ***
*
* Revision 1.3  2005/09/28 14:09:18  just
* *** empty log message ***
*
* Revision 1.2  2005/09/27 14:24:00  just
* *** empty log message ***
*
* Revision 1.1  2005/09/27 13:20:47  just
* *** empty log message ***
*
* Revision 1.1  2005/09/27 13:12:45  just
* *** empty log message ***
*
* Revision 1.2  2005/04/18 21:11:48  just
* *** empty log message ***
*
* Revision 1.1  2005/03/24 20:26:14  just
* *** empty log message ***
*
* Revision 1.2  2005/03/24 20:24:59  just
* *** empty log message ***
*
* Revision 1.1  2005/03/20 12:55:23  just
* *** empty log message ***
*
* Revision 1.8  2005/01/20 10:18:58  just
* use nmea timestamp gps-data-ind
*
* Revision 1.7  2004/12/14 14:14:03  just
* *** empty log message ***
*
* Revision 1.6  2004/11/08 14:38:18  just
* team-action with zoneenter works
*
* Revision 1.5  2004/10/28 12:51:42  just
* *** empty log message ***
*
* Revision 1.4  2004/10/26 14:17:38  just
* *** empty log message ***
*
* Revision 1.3  2004/10/11 16:20:14  just
* *** empty log message ***
*
* Revision 1.2  2004/09/07 14:40:22  just
* first version geopos handler/data/test
*
* Revision 1.1  2004/08/24 15:24:55  just
* added GeoPos data object
*
*
*
*/

/* Waag OSS license statement.
 *
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is "Waag Society / for old and new media"
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or
 * the GNU Lesser General Public License Version 2.1 or later (the "LGPL"),
 * in which case the provisions of the GPL or the LGPL are applicable instead
 * of those above. If you wish to allow use of your version of this file only
 * under the terms of either the GPL or the LGPL, and not to allow others to
 * use your version of this file under the terms of the MPL, indicate your
 * decision by deleting the provisions above and replace them with the notice
 * and other provisions required by the GPL or the LGPL. If you do not delete
 * the provisions above, a recipient may use your version of this file under
 * the terms of any one of the MPL, the GPL or the LGPL.
 *
 * ***** END LICENSE BLOCK *****/



