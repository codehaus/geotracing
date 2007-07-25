package org.walkandplay.client.phone;

import de.enough.polish.ui.Form;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;
import org.geotracing.client.Util;
import org.keyworx.mclient.Protocol;

import javax.microedition.lcdui.*;

public class AddTextDisplay extends DefaultDisplay implements NetListener {

    private Net net;
    private TextField nameField;
    private TextField textField;
    private StringItem alertField = new StringItem("", "");
    private Command SUBMIT_CMD = new Command("OK", Command.OK, 1);
    private Command CANCEL_CMD = new Command("Back", Command.CANCEL, 1);

    public AddTextDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "");
        midlet = aMIDlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        net = Net.getInstance();
        if (!net.isConnected()) {
            net.setProperties(midlet);
            net.setListener(this);
            net.start();
        }

        //#style defaultscreen
        Form form = new Form("");

        //#style labelinfo
        form.append("Enter Title");

        //#style textbox
        nameField = new TextField("", "", 32, TextField.ANY);
        form.append(nameField);

        //#style labelinfo
        form.append("Enter Text");

        //#style textbox
        textField = new TextField("", "", 1024, TextField.ANY);
        form.append(textField);

        /*//#style labelinfo
        form.append("Enter Tags (opt)");
        //#style textbox
        tagsField = new TextField("", "", 32, TextField.ANY);
        form.append(tagsField);*/

        form.append(alertField);

        form.addCommand(SUBMIT_CMD);
        form.addCommand(CANCEL_CMD);

        form.setCommandListener(this);

        Display.getDisplay(midlet).setCurrent(form);
    }

    public void onNetInfo(String theInfo) {
        Log.log(theInfo);
    }

    public void onNetError(String aReason, Throwable anException) {
        Log.log(aReason);
    }

    public void onNetStatus(String aStatusMsg) {
        Log.log(aStatusMsg);
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
                    }
                } else {
                    alertField.setText("Type title and text");
                }
            }
        } else if (command == CANCEL_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
