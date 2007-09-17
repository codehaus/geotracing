package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

import javax.microedition.lcdui.*;
import java.util.Vector;
import java.util.Date;

import org.walkandplay.client.phone.TCPClientListener;

public class IMDisplay extends DefaultDisplay{

    private Command SUBMIT_CMD = new Command("Send", Command.OK, 1);
    private Command NEW_MSG_CMD = new Command("New message", Command.SCREEN, 1);
    private final static String AUTHOR_TYPE_MOBILE = "mobile";

    private TextField messageField = new TextField("", "", 32, TextField.ANY);
    private String myMessage = "";
    private String message = "";

    private boolean active;

    public IMDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Messaging");
    }

    public void start(Displayable aPrevScreen, String aMessage){
        prevScreen = aPrevScreen;

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

    public String getMyMessage(){
        return myMessage;
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
            append(message);
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

    public void handleCommentInsertRsp(JXElement aResponse){
        deleteAll();
        //#style alertinfo
        append("Message sent succesfully");
        removeCommand(SUBMIT_CMD);
        addCommand(NEW_MSG_CMD);
    }

    public void handleCommentInsertNrsp(JXElement aResponse){
        deleteAll();

        //#style alertinfo
        append("Error sending message. Please try again");

        drawScreen();
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
                myMessage = msg;
                sendMessage(msg);
                //clear the messagefield
                messageField.setString("");
            }
        } else if (command == BACK_CMD) {
            active = false;            
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }else if (command == NEW_MSG_CMD) {
            deleteAll();
            drawScreen();
            removeCommand(NEW_MSG_CMD);
            addCommand(SUBMIT_CMD);
        }
    }
}