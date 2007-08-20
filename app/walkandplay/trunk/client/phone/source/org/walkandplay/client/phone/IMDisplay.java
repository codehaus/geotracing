package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

import javax.microedition.lcdui.*;
import java.util.Vector;

import org.walkandplay.client.phone.TCPClientListener;

public class IMDisplay extends DefaultDisplay implements TCPClientListener {

    private Command SUBMIT_CMD = new Command("Send", Command.OK, 1);

    private StringItem inputField = new StringItem("", "");
    private TextField outputField = new TextField("", "", 32, TextField.ANY);
    private StringItem alertField = new StringItem("", "");
    private Vector messages;
    private boolean active;

    public IMDisplay(WPMidlet aMIDlet, Displayable aPrevScreen, Vector theMessages) {
        super(aMIDlet, "Messaging");
        prevScreen = aPrevScreen;
        midlet.getActiveApp().addTCPClientListener(this);

        //#style labelinfo
        append("last message from web player");
        //#style formbox
        append(inputField);

        setMessages(theMessages);

        //#style labelinfo
        append("send message to web player");
        //#style textbox
        append(outputField);
        append(alertField);

        addCommand(SUBMIT_CMD);

        active = true;
    }

    public boolean isActive() {
        return active;
    }

    public void setMessages(Vector theMessages) {
        messages = theMessages;
        if (messages != null && messages.size() > 0) {
            inputField.setText(((JXElement) messages.elementAt(0)).getChildText("content"));
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("-rsp")) {

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

    /*
    <cmt-insert-req>
        <target>${trkid1}</target>
        <content>comments on this track</content>
    </cmt-insert-req>
    <cmt-read-req>
        <target>${trkid1}</target>
    </cmt-read-req>
     */
    private void sendMessage(String aMessage) {
        JXElement req = new JXElement("cmt-insert-req");
        JXElement target = new JXElement("target");
        target.setText("" + midlet.getPlayApp().getGamePlayId());
        req.addChild(target);

        JXElement content = new JXElement("content");
        content.setText(aMessage);
        req.addChild(content);

        midlet.getActiveApp().sendRequest(req);
    }


    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command command, Displayable screen) {
        if (command == SUBMIT_CMD) {
            if (outputField.getString() == null) {
                alertField.setText("No text typed");
            } else {
                sendMessage(outputField.getString());
            }
        } else if (command == BACK_CMD) {
            active = false;
            midlet.getActiveApp().removeTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }
}