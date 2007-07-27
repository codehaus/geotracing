package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.Util;
import org.keyworx.mclient.Protocol;

import javax.microedition.lcdui.*;

public class AddTextDisplay extends DefaultDisplay {

    private Command SUBMIT_CMD = new Command("OK", Command.OK, 1);

    private Net net;
    private TextField nameField;
    private TextField textField;
    private StringItem alertField = new StringItem("", "");
    
    public AddTextDisplay(WPMidlet aMIDlet, Displayable aPrevScreen) {
        super(aMIDlet, "Add Text");
        prevScreen = aPrevScreen;

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

        append(alertField);

        addCommand(SUBMIT_CMD);
    }

    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command command, Displayable screen) {
        if (command == SUBMIT_CMD) {
            if (textField.getString() == null) {
                alertField.setText("No text typed");
            } else {
                String name = nameField.getString();
                String text = textField.getString();
                //String tags = tagsField.getString();
                if (name != null && name.length() > 0 && text != null && text.length() > 0) {
                    JXElement rsp = net.uploadMedium(name, text, "text", "text/plain", Util.getTime(), text.getBytes(), false);
                    if (Protocol.isPositiveResponse(rsp)) {
                        //now do an add medium
                        JXElement addMediumReq = new JXElement("play-add-medium-req");
                        addMediumReq.setAttr("id", rsp.getAttr("id"));
                        Log.log(new String(addMediumReq.toBytes(false)));
                        JXElement addMediumRsp = Net.getInstance().utopiaReq(addMediumReq);
                        Log.log(new String(addMediumRsp.toBytes(false)));

                        if (addMediumRsp.getTag().indexOf("-rsp") != -1) {
                            textField.setString("");
                            nameField.setString("");
                            alertField.setText("Text sent successfully");
                        } else {
                            textField.setString("");
                            nameField.setString("");
                            alertField.setText("Error sending text - please try again.");
                        }
                    }
                } else {
                    alertField.setText("Type title and text");
                }
            }
        } else if (command == BACK_CMD) {
            Log.log("prevscreen:" + prevScreen);
            if(prevScreen!=null){
                Log.log(prevScreen.getTitle());
            }            
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
