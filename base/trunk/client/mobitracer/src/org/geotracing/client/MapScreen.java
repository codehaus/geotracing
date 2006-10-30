// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;


/**
 * Show map with user location.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class MapScreen extends Form implements CommandListener {

	private String tileURL;
	private int zoom = 12;
	private Command zoomIn;
	private Command zoomOut;
	private Command back;
	private Command toggleMapType;
	private MIDlet midlet;
	private String mapType = "map";
	private Displayable prevScreen;
	private String lon="0", lat="0";

	public MapScreen() {
		super("MapScreen");

		zoomIn = new Command("Zoom In", Command.OK, 1);
		zoomOut = new Command("Zoom Out", Command.OK, 1);
		back = new Command("Back", Command.OK, 1);
		toggleMapType = new Command("Toggle Map Type", Command.OK, 1);
		addCommand(zoomIn);
		addCommand(zoomOut);
		addCommand(toggleMapType);
		addCommand(back);
		setCommandListener(this);
	}

	public boolean hasLocation() {
		return !lon.equals(("0")) && !lat.equals("0");
	}

	void setLocation(String aLon, String aLat) {
		lon = aLon;
		lat = aLat;
	}

	public void activate(MIDlet aMidlet) {
		midlet = aMidlet;
		tileURL = Net.getInstance().getURL() + "/map/gmap.jsp?";
		prevScreen = Display.getDisplay(midlet).getCurrent();
		Display.getDisplay(midlet).setCurrent(this);
		show();
	}


	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			Display.getDisplay(midlet).setCurrent(prevScreen);
		} else if (c == zoomIn) {
			zoom++;
			show();
		} else if (c == zoomOut) {
			zoom--;
			show();
		} else if (c == toggleMapType) {
			mapType = mapType.equals("sat") ? "map" : "sat";
			show();
		}
	}

	private void show() {
		deleteAll();

		if (!hasLocation()) {
			append("No location (yet)");
			return;
		}


		String activeTile = tileURL + "lon=" + lon + "&lat=" + lat;

		try {
			append("Get map...");
			append("loc=" + lon + ", " + zoom);
			append("type=" + mapType + " zoom=" + zoom);
			Image image = Util.getImage(activeTile + "&zoom=" + zoom + "&type=" + mapType);
			if (image != null) {
				deleteAll();
				append(image);
			}
		} catch (Throwable t) {
			append("Error fetching map image !!");
			append("maybe this zoom-level is not available");
			append("try zooming  in or out");
			Log.log("error: MapScreen: t=" + t + " m=" + t.getMessage());
		}
	}
}