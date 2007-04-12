package org.walkandplay.client.phone;

import de.enough.polish.ui.StringItem;
import de.enough.polish.ui.ImageItem;
import de.enough.polish.util.Locale;

import javax.microedition.lcdui.*;

import org.geotracing.client.*;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
/*public class TraceDisplay extends DefaultDisplay  {*/
public class DefaultTraceDisplay extends DefaultDisplay   {
    protected TracerEngine tracerEngine;
	
    public DefaultTraceDisplay(WPMidlet aMidlet, String aTitle) {
        super(aMidlet, aTitle);
    }

	TracerEngine getTracer() {
		return tracerEngine;
	}

	public void setStatus(String s) {
	}

	public void onGPSStatus(String s) {
    }


	public void onNetStatus(String s) {
    }

    public void setGPSInfo(GPSInfo theInfo) {
    }

}
