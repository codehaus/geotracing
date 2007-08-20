package org.walkandplay.client.phone;

import de.enough.polish.ui.StringItem;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.Util;
import org.walkandplay.client.phone.Log;
import org.walkandplay.client.phone.TCPClientListener;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Player;
import javax.microedition.media.Manager;

public class MediumDisplay extends DefaultDisplay implements TCPClientListener {

    private Command VIEW_VIDEO_CMD = new Command("View video in", Command.SCREEN, 2);
    private Command VIEW_VIDEO_CMD2 = new Command("View video ext", Command.SCREEN, 2);
    private String MEDIUM_BASE_URL;

    private int mediumId;
    private int screenWidth;
    private JXElement medium;
    private Image mediumImage;
    private Player audioPlayer;

    private StringItem mediumLabel = new StringItem("", "");

    public MediumDisplay(WPMidlet aMIDlet, int aMediumId, int theScreenWidth, Displayable aPrevScreen) {
        super(aMIDlet, "Media");
        mediumId = aMediumId;
        screenWidth = theScreenWidth;
        prevScreen = aPrevScreen;
        midlet.getActiveApp().addTCPClientListener(this);
        MEDIUM_BASE_URL = midlet.getKWUrl() + "/media.srv?id=";

        mediumLabel.setText("Loading...");
        //#style labelinfo
        append(mediumLabel);

        getMedium();
    }

    private void drawMedium() {
        String type = medium.getChildText("type");
        String name = medium.getChildText("name");
        if(name!=null && name.length()>0){
            mediumLabel.setText(name);
        }else{
            mediumLabel.setText("Untitled");
        }

        String desc = medium.getChildText("description");

        if (desc != null && desc.length() > 0) {
            //#style formbox
            append(desc);
        }

        if (type.equals("image")) {
            append(mediumImage);

        } else if (type.equals("video")) {
            //#style formbox
            /*form.append("When you click on 'view video' the video will be " +
                    "downloaded and played in your default media player like " +
                    "realplayer. Afterwards close the media player and continue " +
                    "here by pressing 'back'");*/
            //#style formbox
            append("When you click on 'view video' the video will be " +
                    "downloaded. This might take a while.... Afterwards continue " +
                    "here by pressing 'back'");

            addCommand(VIEW_VIDEO_CMD);
            addCommand(VIEW_VIDEO_CMD2);
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("query-store-rsp")) {
                String cmd = rsp.getAttr("cmd");
                if (cmd.equals("q-medium")) {
                    medium = rsp.getChildByTag("record");
                    String type = medium.getChildText("type");
                    String url = MEDIUM_BASE_URL + mediumId;
                    Log.log(url);

                    if (type.equals("image")) {
                        try {
                            url += "&resize=" + (screenWidth - 10);
                            mediumImage = Util.getImage(url);
                        } catch (Throwable t) {
                            Log.log("Error retrieving image");
                        }
                    } else if (type.equals("audio")) {
                       try {
                            audioPlayer = Manager.createPlayer(url);
                            audioPlayer.prefetch();
                            audioPlayer.start();

                            //Util.playStream(url);

                            // open up real player!!!
                            //midlet.platformRequest(url);
                        } catch (Throwable t) {
                            Log.log("Error playing audio url");
                        }
                    } else if (type.equals("video")) {
                        try {
                            // open up real player!!!
                            //midlet.platformRequest(url);
                        } catch (Throwable t) {
                            Log.log("Error fetching text url=");
                        }
                    } else if (type.equals("text")) {
                        try {
                            medium.setChildText("description", Util.getPage(url));
                        } catch (Throwable t) {
                            Log.log("Error fetching text url=");
                        }
                    } else if (type.equals("user")) {
                        //showObject.setChildText("text", "last location of " + showObject.getChildText("name"));
                    } else {
                        //showObject.setChildText("text", type + " is not supported (yet)");
                    }

                    // now draw the info
                    drawMedium();
                }
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

    private void getMedium() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-medium");
        req.setAttr("id", mediumId);
        midlet.getActiveApp().sendRequest(req);
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            if(audioPlayer!=null){
                try{
                    audioPlayer.stop();
                    audioPlayer.deallocate();
                    audioPlayer.close();
                    audioPlayer = null;
                }catch(Throwable t){
                    //
                }
            }
            midlet.getActiveApp().removeTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (command == VIEW_VIDEO_CMD) {
            new VideoDisplay(midlet, MEDIUM_BASE_URL + mediumId, this);
        } else if (command == VIEW_VIDEO_CMD2) {
            try {
                // now first stop the tracer engine because the media download
                // needs the internet connection too
                /*midlet.playDisplay.getTracerEngine().suspend();
                midlet.playDisplay.getTracerEngine().stop();*/
                midlet.platformRequest(MEDIUM_BASE_URL + mediumId);
            }
            catch (Throwable t) {
                Util.showAlert(midlet, "Error", t.getMessage());
                Log.log("Exception launching the video:" + t.getMessage());
            }
        }
    }

}
