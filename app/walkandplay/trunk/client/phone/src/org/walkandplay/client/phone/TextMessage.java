package org.walkandplay.client.phone;

import org.geotracing.client.Net;
import org.geotracing.client.Util;
import nl.justobjects.mjox.JXElement;

public class TextMessage {
    private String msg;
    private long time;
    private static final String mime = "text/plain";

    public TextMessage(String aMsg){
        msg = aMsg;
        time = Util.getTime();
    }

    public boolean upload(){
        if(msg.length()>0){
            JXElement rsp = Net.getInstance().uploadMedium(msg, "text", mime, time, null, false);
            if(rsp == null || rsp.getTag().indexOf("nrsp")!=-1){
                return false;
            }
            return true;
        }
        return false;
    }

}
