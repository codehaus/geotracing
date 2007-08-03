package org.walkandplay.client.phone;

import org.geotracing.client.GPSInfo;
import nl.justobjects.mjox.JXElement;

public interface TracerEngineListener {

    void onGPSStatus(String aStatus);

    void onNetStatus(String aStatus);

    void setGPSInfo(GPSInfo theInfo);

    void setStatus(String aStatus);

    void setHit(JXElement anElement);

}
