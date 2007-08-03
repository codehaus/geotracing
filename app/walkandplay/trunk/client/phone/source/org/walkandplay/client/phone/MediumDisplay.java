package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.Util;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

import de.enough.polish.ui.StringItem;

public class MediumDisplay extends DefaultDisplay{

    private Net net;
    private Command VIEW_VIDEO_CMD = new Command("View video in", Command.SCREEN, 2);
    private Command VIEW_VIDEO_CMD2 = new Command("View video ext", Command.SCREEN, 2);
    private String MEDIUM_BASE_URL = Net.getInstance().getURL() + "/media.srv?id=";

    private int mediumId;
    private int screenWidth;
    private JXElement medium;
    private Image mediumImage;

    private StringItem name = new StringItem("", "");

    public MediumDisplay(WPMidlet aMIDlet, int aMediumId, int theScreenWidth, Displayable aPrevScreen) {
        super(aMIDlet, "Media");
        mediumId = aMediumId;
        screenWidth = theScreenWidth;
        prevScreen = aPrevScreen;


        net = Net.getInstance();
        if (!net.isConnected()) {
            net.setProperties(midlet);
            net.start();
        }

        name.setText("Loading...");
        //#style formbox
        append(name);

        getMedium();
    }

    private void drawMedium(){
        String type = medium.getChildText("type");
        name.setText(medium.getChildText("name"));

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

    private void getMedium() {
        try {
            // retrieve the medium
            new Thread(new Runnable() {
                public void run() {
                    Log.log("retrieving the medium: " + mediumId);
                    JXElement req = new JXElement("query-store-req");
                    req.setAttr("cmd", "q-medium");
                    req.setAttr("id", mediumId);
                    JXElement rsp = net.utopiaReq(req);
                    medium = rsp.getChildByTag("record");
                    String type = medium.getChildText("type");
                    String url = MEDIUM_BASE_URL + mediumId;
                    Log.log(url);
                    if (type.equals("image")) {
                        try {
                            url += "&resize=" + (screenWidth - 10);
                            mediumImage = Util.getImage(url);
                        } catch (Throwable t) {
                            Log.log("Error fetching image url");
                        }
                    } else if (type.equals("audio")) {
                        try {
                            Util.playStream(url);
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
            }).start();
        } catch (Throwable t) {
            Log.log("Exception in getMedium:\n" + t.getMessage());
        }
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
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
