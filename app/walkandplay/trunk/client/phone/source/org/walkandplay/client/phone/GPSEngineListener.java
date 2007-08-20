package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.GPSInfo;

import java.util.Vector;

public interface GPSEngineListener {

    public void onGPSLocation(Vector thePoints);

    public void onGPSStatus(String aStatus);

    public void onGPSInfo(GPSInfo theInfo);

}
