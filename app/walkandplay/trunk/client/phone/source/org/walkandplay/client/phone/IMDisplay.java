package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannelListener;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;
import org.geotracing.client.Preferences;

import javax.microedition.lcdui.*;

public class IMDisplay extends DefaultDisplay implements XMLChannelListener {

    private Command SUBMIT_CMD = new Command("Send", Command.OK, 1);

    private StringItem inputField = new StringItem("", "");
    private TextField outputField = new TextField("", "", 32, TextField.ANY);
    private StringItem alertField = new StringItem("", "");
    private Net net;
    private String comment = "";

    public IMDisplay(WPMidlet aMIDlet, Displayable aPrevScreen) {
        super(aMIDlet, "Messaging");
        prevScreen = aPrevScreen;
        midlet.setKWClientListener(this);

        net = Net.getInstance();
        if (!net.isConnected()) {
            net.setProperties(midlet);
            net.start();
        }

        //#style labelinfo
        append("last message from webplayer");
        //#style formbox
        append(inputField);

        if (comment.length() > 0) {
            inputField.setText(comment);
        }
        //#style labelinfo
        append("send message to webplayer");
        //#style textbox
        append(outputField);
        append(alertField);

        addCommand(SUBMIT_CMD);
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if(tag.equals("utopia-rsp")){
            JXElement rsp = aResponse.getChildAt(0);
            if(rsp.getTag().equals("-rsp")){

            }
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        deleteAll();
        addCommand(BACK_CMD);
        //#style alertinfo
        append("Oops, we lost our connection. Please go back and try again.");
    }

    private void sendMsg() {
        try {
            new Thread(new Runnable() {
                public void run() {
                    try {
                        String user = new Preferences(Net.RMS_STORE_NAME).get(Net.PROP_USER, midlet.getAppProperty(Net.PROP_USER));

                        JXElement req = new JXElement("cmt-insert-req");
                        JXElement comment = new JXElement("comment");
                        req.addChild(comment);
                        JXElement targetPerson = new JXElement("targetperson");
                        JXElement author = new JXElement("author");
                        author.setText(user);
                        comment.addChild(author);
                        JXElement content = new JXElement("content");
                        content.setText(outputField.getString());
                        comment.addChild(content);
                        Log.log(new String(req.toBytes(false)));
                        JXElement rsp = net.utopiaReq(req);
                        //JXElement rsp = net.utopiaReq(req);
                        Log.log(new String(rsp.toBytes(false)));
                        if (rsp.getTag().indexOf("-rsp") != -1) {
                            alertField.setText("msg sent!");
                        } else {
                            alertField.setText("error sending msg!");
                        }
                        Log.log(new String(rsp.toBytes(false)));
                    } catch (Throwable t) {
                        alertField.setText(t.getMessage());
                    }
                }
            }).start();
        } catch (Throwable t) {
            Log.log("Exception in sendMsg:\n" + t.getMessage());
        }
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
                sendMsg();
            }
        } else if (command == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }
}