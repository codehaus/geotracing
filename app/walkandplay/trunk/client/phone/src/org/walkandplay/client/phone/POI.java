package org.walkandplay.client.phone;

import org.geotracing.client.Net;

public class POI {
    private String name;
    private String type;
    private String desc;

    public POI(String aPOIName, String aPOIType, String aPOIDesc){
        name = aPOIName;
        type = aPOIType;
        desc = aPOIDesc;
    }

    public void upload(){
        if(name.length()>0) Net.getInstance().addPOI(type, name, desc);
    }

}
