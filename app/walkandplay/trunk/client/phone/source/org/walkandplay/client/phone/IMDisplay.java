package org.walkandplay.client.phone;

import de.enough.polish.ui.Form;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;
import org.geotracing.client.Preferences;

import javax.microedition.lcdui.*;

public class IMDisplay extends DefaultDisplay implements NetListener {

    private Command SUBMIT_CMD = new Command("OK", Command.OK, 1);
    private Command CANCEL_CMD = new Command("Back", Command.CANCEL, 1);

    private StringItem inputField = new StringItem("", "");
    private TextField outputField = new TextField("", "", 32, TextField.ANY);
    private StringItem alertField = new StringItem("", "");
    private Net net;
    private String comment = "";

    public IMDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "");

        midlet = aMIDlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        //#style defaultscreen
        Form form = new Form("");
        //#style labelinfo
        form.append("last message from webplayer");
        //#style formbox
        form.append(inputField);

        if (comment.length() > 0) {
            inputField.setText(comment);
        }
        //#style labelinfo
        form.append("send message to webplayer");
        //#style textbox
        form.append(outputField);
        form.append(alertField);

        net = Net.getInstance();
        if (!net.isConnected()) {
            net.setProperties(midlet);
            net.setListener(this);
            net.start();
        }

        form.addCommand(SUBMIT_CMD);
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
        } else if (command == CANCEL_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }
}