package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

import javax.microedition.lcdui.*;
import java.util.Vector;
import java.util.Date;

import org.walkandplay.client.phone.TCPClientListener;

public class IMDisplay extends DefaultDisplay implements TCPClientListener {

    private Command SUBMIT_CMD = new Command("Send", Command.OK, 1);
    private final static String AUTHOR_TYPE_MOBILE = "mobile";

    private TextField messageField = new TextField("", "", 32, TextField.ANY);
    private JXElement message;

    private boolean active;

    public IMDisplay(WPMidlet aMIDlet, Displayable aPrevScreen) {
        super(aMIDlet, "Messaging");
        prevScreen = aPrevScreen;
        midlet.getActiveApp().addTCPClientListener(this);
    }

    public void start(JXElement aMessage){
        // start clean
        deleteAll();

        if(aMessage!=null) message = aMessage;

        drawScreen();

        active = true;
        Display.getDisplay(midlet).setCurrent(this);    
    }

    public boolean isActive() {
        return active;
    }

    private void drawScreen(){
        //#style labelinfo
        append("send message to web player");
        //#style textbox
        append(messageField);
        if(message!=null){
            //#style labelinfo
            append("last message from web player");
            //#style formbox
            append(message.getChildText("content"));
        }
        addCommand(SUBMIT_CMD);
    }

    /*<cmt-read-rsp>
        <record>
            <id>${cmtid1}</id>
            <owner/>
            <target>${trkid1}</target>
            <targettable/>
            <targetperson/>
            <author>anon</author>
            <email/>
            <url/>
            <ip/>
            <content>comments on this track</content>
            <state>1</state>
            <creationdate/>
            <modificationdate/>
            <extra/>
        </record>
    </cmt-read-rsp>
    */

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("cmt-insert-rsp")) {
                deleteAll();
                //#style alertinfo
                append("Message sent succesfully");
                addCommand(BACK_CMD);
            }else if (rsp.getTag().equals("cmt-insert-nrsp")) {
                deleteAll();

                //#style alertinfo
                append("Error sending message. Please try again");

                drawScreen();
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
        <target>gameplayid</target>
        <author>mobile</author>
        <content>message</content>
    </cmt-insert-req>
     */
    private void sendMessage(String aMessage) {
        JXElement req = new JXElement("cmt-insert-req");
        JXElement target = new JXElement("target");
        target.setText("" + midlet.getPlayApp().getGamePlayId());
        req.addChild(target);

        JXElement author = new JXElement("author");
        author.setText(AUTHOR_TYPE_MOBILE);
        req.addChild(author);

        JXElement content = new JXElement("content");
        content.setText(aMessage);
        req.addChild(content);

        midlet.getActiveApp().sendRequest(req);
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == SUBMIT_CMD) {
            String msg = messageField.getString();
            if (msg == null || msg.length() == 0) {
                deleteAll();

                //#style alertinfo
                append("Enter a message...");

                drawScreen();
            } else {
                sendMessage(msg);
            }
        } else if (command == BACK_CMD) {
            active = false;
            midlet.getActiveApp().removeTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }
}