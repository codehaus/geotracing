package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.GPSInfo;

public interface GPSEngineListener {

    public void onGPSStatus(String aStatus);

    public void onNetStatus(String aStatus);

    public void onGPSInfo(GPSInfo theInfo);

    public void onStatus(String aStatus);

    public void onHit(JXElement anElement);

}
