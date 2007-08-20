package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.*;
import org.keyworx.mclient.Protocol;
import org.walkandplay.client.phone.TCPClientListener;
import org.walkandplay.client.phone.Uploader;

import javax.microedition.lcdui.*;

public class AddTextDisplay extends DefaultDisplay implements TCPClientListener {

    private Command SUBMIT_CMD = new Command("OK", Command.OK, 1);

    private TextField nameField;
    private TextField textField;
    private StringItem alertField = new StringItem("", "");
    private boolean playing;

    public AddTextDisplay(WPMidlet aMIDlet, Displayable aPrevScreen, boolean isPlaying) {
        super(aMIDlet, "Add Text");
        prevScreen = aPrevScreen;
        playing = isPlaying;

        midlet.getActiveApp().addTCPClientListener(this);

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
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("play-add-medium-rsp") || rsp.getTag().equals("game-add-medium-rsp")) {
                deleteAll();
                addCommand(BACK_CMD);
                //#style alertinfo
                append(alertField);
                
                alertField.setText("Text sent successfully");
            } else if (rsp.getTag().indexOf("-nrsp")!=-1) {
                deleteAll();
                addCommand(BACK_CMD);
                //#style alertinfo
                append(alertField);

                textField.setString("");
                nameField.setString("");
                alertField.setText("Error sending text - please try again.\n" + rsp.getAttr("details"));
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
                Uploader uploader = new Uploader();
                JXElement rsp = uploader.uploadMedium(midlet.getKWUrl(), name, text, "text", "text/plain", Util.getTime(), text.getBytes(), false);
                /*Net net = Net.getInstance();
                net.setProperties(midlet);
                JXElement rsp = net.uploadMedium(name, text, "text", "text/plain", Util.getTime(), text.getBytes(), false);*/
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
            midlet.getActiveApp().removeTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
