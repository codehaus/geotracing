package org.walkandplay.client.phone;

import de.enough.polish.ui.Form;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;
import org.geotracing.client.Util;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Image;

public class MediumDisplay extends DefaultDisplay implements NetListener {

    private Net net;
    private Command CANCEL_CMD = new Command("Back", Command.CANCEL, 1);
    private Command VIEW_VIDEO_CMD = new Command("View video", Command.SCREEN, 2);
    private String MEDIUM_BASE_URL = Net.getInstance().getURL() + "/media.srv?id=";

    private int mediumId;
    private int screenWidth;
    private JXElement medium;
    private Image mediumImage;


    public MediumDisplay(WPMidlet aMIDlet, int aMediumId, int theScreenWidth) {
        super(aMIDlet, "");
        midlet = aMIDlet;
        mediumId = aMediumId;
        screenWidth = theScreenWidth;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        net = Net.getInstance();
        if (!net.isConnected()) {
            net.setProperties(midlet);
            net.setListener(this);
            net.start();
        }

        retrieveMedium();

        //#style defaultscreen
        Form form = new Form("");
        String type = medium.getChildText("type");
        //#style labelinfo
        form.append(medium.getChildText("name"));

        String desc = medium.getChildText("description");

        if (desc != null && desc.length() > 0) {
            //#style formbox
            form.append(desc);
        }

        if (type.equals("image")) {
            form.append(mediumImage);

        } else if (type.equals("video")) {
            //#style formbox
            /*form.append("When you click on 'view video' the video will be " +
                    "downloaded and played in your default media player like " +
                    "realplayer. Afterwards close the media player and continue " +
                    "here by pressing 'back'");*/
            //#style formbox
            form.append("When you click on 'view video' the video will be " +
                    "downloaded. This might take a while.... Afterwards continue " +
                    "here by pressing 'back'");

            form.addCommand(VIEW_VIDEO_CMD);
        }

        form.addCommand(CANCEL_CMD);
        form.setCommandListener(this);
        Display.getDisplay(midlet).setCurrent(form);

    }

    public void onNetInfo(String theInfo) {
        System.out.println(theInfo);
    }

    public void onNetError(String aReason, Throwable anException) {
        System.out.println(aReason);
    }

    public void onNetStatus(String aStatusMsg) {
        System.out.println(aStatusMsg);
    }

    private void retrieveMedium() {
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
                }
            }).start();
        } catch (Throwable t) {
            Log.log("Exception in retrieveMedium:\n" + t.getMessage());
        }
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command command, Displayable screen) {
        if (command == CANCEL_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (command == VIEW_VIDEO_CMD) {
            try {
                // now first stop the tracer engine because the media download
                // needs the internet connection too
                /*tracerEngine.suspend();
                tracerEngine.stop();
                midlet.platformRequest(MEDIUM_BASE_URL + mediumId);*/
                Display.getDisplay(midlet).setCurrent(new VideoDisplay(midlet, MEDIUM_BASE_URL + mediumId, false));
            }
            catch (Throwable t) {
                Util.showAlert(midlet, "Error", t.getMessage());
                Log.log("Exception launching the video:" + t.getMessage());
            }
        }
    }

}
