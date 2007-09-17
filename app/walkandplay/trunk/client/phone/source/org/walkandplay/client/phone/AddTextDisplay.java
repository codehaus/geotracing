package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.keyworx.mclient.Protocol;
import org.geotracing.client.Util;
import org.geotracing.client.GPSFetcher;

import javax.microedition.lcdui.*;

public class AddTextDisplay extends DefaultDisplay{

    private Command SUBMIT_CMD = new Command("OK", Command.OK, 1);

    private TextField nameField;
    private TextField textField;
    private StringItem alertField = new StringItem("", "");
    private boolean playing;
    private boolean active;

    public AddTextDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Add Text");
    }

    private void drawScreen(){
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

    public void start(Displayable aPrevScreen, boolean isPlaying){
        prevScreen = aPrevScreen;
        active = true;
        playing = isPlaying;        
        // start fresh
        deleteAll();
        drawScreen();
        
        Display.getDisplay(midlet).setCurrent(this);
    }

    public boolean isActive(){
        return active;
    }

    public void handleAddMediumRsp(JXElement aResponse){
        deleteAll();
        removeCommand(SUBMIT_CMD);
        
        //#style alertinfo
        append(alertField);

        alertField.setText("Text sent successfully");
    }

    public void handleAddMediumNrsp(JXElement aResponse){
        //#style alertinfo
        append(alertField);

        textField.setString("");
        nameField.setString("");
        alertField.setText("Error sending text - please try again.\n" + aResponse.getAttr("details"));
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
                Uploader uploader = new Uploader();
                JXElement rsp = uploader.uploadMedium(TCPClient.getInstance().getAgentKey(), midlet.getKWUrl(), name, text, "text", "text/plain", Util.getTime(), text.getBytes(), false);

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
            active = false;            
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
