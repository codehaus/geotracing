package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import nl.justobjects.mjox.XMLChannelListener;
import org.geotracing.client.GPSFetcher;
import org.geotracing.client.Net;
import org.geotracing.client.Util;
import org.keyworx.mclient.Protocol;

import javax.microedition.lcdui.*;

public class AddTextDisplay extends DefaultDisplay implements XMLChannelListener {

    private Command SUBMIT_CMD = new Command("OK", Command.OK, 1);

    private Net net;
    private TextField nameField;
    private TextField textField;
    private StringItem alertField = new StringItem("", "");
    private boolean playing;

    public AddTextDisplay(WPMidlet aMIDlet, Displayable aPrevScreen, boolean isPlaying) {
        super(aMIDlet, "Add Text");
        prevScreen = aPrevScreen;
        playing = isPlaying;

        // TODO: make a better solution for this boolean
        if (isPlaying) {
            midlet.getPlayApp().setKWClientListener(this);
        } else {
            midlet.getCreateApp().setKWClientListener(this);
        }

        net = Net.getInstance();
        if (!net.isConnected()) {
            net.setProperties(midlet);
            net.start();
        }

        //#style labelinfo
        append("Enter Title");

        //#style textbox
        nameField = new TextField("", "", 32, TextField.ANY);
        append(nameField);

        //#style labelinfo
        append("Enter Text");

        //#style textbox
        textField = new TextField("", "", 1024, TextField.ANY);
        append(textField);

        /*//#style labelinfo
        form.append("Enter Tags (opt)");
        //#style textbox
        tagsField = new TextField("", "", 32, TextField.ANY);
        form.append(tagsField);*/

        addCommand(SUBMIT_CMD);
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            deleteAll();
            addCommand(BACK_CMD);
            //#style alertinfo
            append(alertField);
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("play-add-medium-rsp") || rsp.getTag().equals("game-add-medium-rsp")) {
                alertField.setText("Text sent successfully");
            } else if (rsp.getTag().equals("play-add-medium-nrsp")) {
                textField.setString("");
                nameField.setString("");
                alertField.setText("Error sending text - please try again.");
            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command command, Displayable screen) {
        if (command == SUBMIT_CMD) {
            String name = nameField.getString();
            String text = textField.getString();
            if (name == null || name.length() == 0 || text == null || text.length() == 0) {
                //#style alertinfo
                append(alertField);
                alertField.setText("Please type some text...");
            } else {
                //String tags = tagsField.getString();
                JXElement rsp = net.uploadMedium(name, text, "text", "text/plain", Util.getTime(), text.getBytes(), false);
                if (Protocol.isPositiveResponse(rsp)) {
                    //now do an add medium
                    if (playing) {
                        JXElement addMediumReq = new JXElement("play-add-medium-req");
                        addMediumReq.setAttr("id", rsp.getAttr("id"));
                        midlet.getPlayApp().sendRequest(addMediumReq);
                    } else {
                        JXElement addMediumReq = new JXElement("game-add-medium-req");
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

                        midlet.getCreateApp().sendRequest(addMediumReq);
                    }
                }
            }
        } else if (command == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
