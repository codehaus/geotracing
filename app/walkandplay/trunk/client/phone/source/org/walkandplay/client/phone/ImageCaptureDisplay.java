package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.GPSFetcher;
import org.geotracing.client.Util;
import org.walkandplay.client.external.CameraHandler;
import org.walkandplay.client.external.CameraListener;

import javax.microedition.lcdui.*;

/**
 * Capture image from phone camera.
 *
 * @author Just van den Broecke
 * @version $Id: ImageCapture.java 254 2007-01-11 17:13:03Z just $
 */
public class ImageCaptureDisplay extends DefaultDisplay implements TCPClientListener, CameraListener {

    private Command SEND_CMD = new Command("Send", Command.OK, 1);

    private Displayable prevScreen;
    private boolean playing;
    private TextField name = new TextField("", null, 24, TextField.ANY);
    private boolean active;
    private Display display;

    public ImageCaptureDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Image Capture");        
        display = Display.getDisplay(midlet);
    }

    public void camera() {
        try {
            CameraHandler.addListener(this);
            CameraHandler.takeSnapshot(display);
        } catch (Exception e) {
            //#style alertinfo
            append("Error:"+ e.getMessage());
        }

        //Create a new thread here to get notified when the photo is taken
        /*new Thread() {
            public void run() {
                while (!CameraHandler.isFinished) {
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException ie) {
                        //#style alertinfo
                        append("Error:" + ie.getMessage());
                    }
                }
                drawScreen();
                CameraHandler.end();                
            }
        }.start();*/
    }

    public void onFinish(){
        display.setCurrent(this);
        drawScreen();
        CameraHandler.end();
    }
    
    public void onCancel(){
        active = false;
        midlet.getActiveApp().removeTCPClientListener(this);
        Display.getDisplay(midlet).setCurrent(prevScreen);
    }

    private void drawScreen(){
        display.setCurrent(this);
        deleteAll();
        name.setString("");
        //append(CameraHandler.getPhoto());
        //#style labelinfo
        append("Name your photo");
        //#style textbox
        append(name);

        addCommand(SEND_CMD);
    }

    private void sendPhoto(byte[] theBytes){
        try{
            Uploader uploader = new Uploader();
			long photoTime = Util.getTime();
            JXElement rsp = uploader.uploadMedium(TCPClient.getInstance().getAgentKey(), midlet.getKWUrl(), name.getString(), null, "image", "image/jpeg", photoTime, theBytes, false);
            JXElement addMediumReq;
            if (playing) {
                addMediumReq = new JXElement("play-add-medium-req");
                addMediumReq.setAttr("id", rsp.getAttr("id"));

            } else {
                addMediumReq = new JXElement("game-add-medium-req");
                addMediumReq.setAttr("id", midlet.getCreateApp().getGameId());
                JXElement medium = new JXElement("medium");
                addMediumReq.addChild(medium);

                JXElement id = new JXElement("id");
                id.setText(rsp.getAttr("id"));
                medium.addChild(id);

                JXElement lat = new JXElement("lat");
                lat.setText("" + GPSFetcher.getInstance().getCurrentLocation().lat);
                medium.addChild(lat);

                JXElement lon = new JXElement("lon");
                lon.setText("" + GPSFetcher.getInstance().getCurrentLocation().lon);
                medium.addChild(lon);
            }
            midlet.getActiveApp().sendRequest(addMediumReq);
        }catch(Throwable t){
            //#style alertinfo
            append("Error:" + t.getMessage());
        }
    }

    public void start(Displayable aPrevScreen, boolean isPlaying){
        midlet.getActiveApp().addTCPClientListener(this);
        prevScreen = aPrevScreen;
        playing = isPlaying;
        active = true;
        display.setCurrent(this);
        camera();
    }

    public boolean isActive(){
        return active;
    }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK_CMD) {
            active = false;
            midlet.getActiveApp().removeTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }else if (c == SEND_CMD) {
            sendPhoto(CameraHandler.getPhotoBytes());
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {

            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("play-add-medium-rsp") || rsp.getTag().equals("game-add-medium-rsp")) {
                // TODO: need a notify here if we finished a task
                deleteAll();
                removeCommand(SEND_CMD);
                //#style alertinfo
                append("Image sent successfully");
            } else if (rsp.getTag().equals("play-add-medium-nrsp") || rsp.getTag().equals("game-add-medium-nrsp")) {
                deleteAll();
                //#style alertinfo
                append("Error sending Image - please try again.");
            }
        }
    }

    public void onNetStatus(String aStatus){

    }

    public void onConnected(){

    }

    public void onError(String anErrorMessage){
        //#style alertinfo
        append(anErrorMessage);
    }

    public void onFatal(){
        midlet.getActiveApp().exit();
        Display.getDisplay(midlet).setCurrent(midlet.getActiveApp());
    }

}
