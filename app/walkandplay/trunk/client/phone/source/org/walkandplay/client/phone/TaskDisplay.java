package org.walkandplay.client.phone;

import de.enough.polish.ui.Form;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Net;
import org.geotracing.client.NetListener;
import org.geotracing.client.Util;

import javax.microedition.lcdui.*;

public class TaskDisplay extends DefaultDisplay implements NetListener {

    private Net net;
    private Form form;
    private TextField textField;
    private String alert = "";
    private Command OK_CMD = new Command("OK", Command.OK, 1);
    private Command CANCEL_CMD = new Command("Back", Command.CANCEL, 1);
    private Command OUTRO_CMD = new Command("Outro", Command.CANCEL, 1);
    private String MEDIUM_BASE_URL = Net.getInstance().getURL() + "/media.srv?id=";

    private int screenWidth;
    private JXElement task;
    private int taskId;
    private Image taskImage;

    public TaskDisplay(WPMidlet aMIDlet, int aTaskId, int theScreenWidth) {
        super(aMIDlet, "");
        midlet = aMIDlet;
        taskId = aTaskId;
        screenWidth = theScreenWidth;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        net = Net.getInstance();
        if (!net.isConnected()) {
            net.setProperties(midlet);
            net.setListener(this);
            net.start();
        }

        retrieveTask();

        //#style defaultscreen
        form = new Form("");
        if (alert.length() > 0) {
            //#style formbox
            form.append(alert);
        }
        //#style formbox
        form.append(task.getChildText("name"));
        //#style formbox
        form.append(task.getChildText("description"));

        //<task-hit id="54232" state="open|hit|done" answerstate="open" mediastate="open"/>
        form.append(taskImage);

        //#style labelinfo
        form.append("answer");
        //#style textbox
        textField = new TextField("", "", 1024, TextField.ANY);
        form.append(textField);

        form.addCommand(OK_CMD);
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

    private void retrieveTask() {
        try {
            // retrieve the task
            new Thread(new Runnable() {
                public void run() {
                    Log.log("retrieving the task: " + taskId);
                    JXElement req = new JXElement("query-store-req");
                    req.setAttr("cmd", "q-task");
                    req.setAttr("id", taskId);
                    Log.log(new String(req.toBytes(false)));
                    JXElement rsp = net.utopiaReq(req);
                    Log.log(new String(rsp.toBytes(false)));
                    task = rsp.getChildByTag("record");
                    if (task != null) {
                        String mediumId = task.getChildText("mediumid");
                        String url = MEDIUM_BASE_URL + mediumId + "&resize=" + (screenWidth - 13);
                        Log.log(url);
                        try {
                            taskImage = Util.getImage(url);
                        } catch (Throwable t) {
                            Log.log("Error fetching task image url: " + url);
                        }
                    } else {
                        Log.log("No task found with id " + taskId);
                    }
                }
            }).start();
        } catch (Throwable t) {
            Log.log("Exception in retrieveTask:\n" + t.getMessage());
        }
    }


    /*
    * The commandAction method is implemented by this midlet to
    * satisfy the CommandListener interface and handle the Exit action.
    */
    public void commandAction(Command command, Displayable screen) {
        if (command == OK_CMD) {
            if (textField.getString() == null) {
                alert = "No text typed";
            } else {
                //<play-answertask-rsp state="hit" mediastate="open|done" answerstate="notok|ok" score="75" playstate="open|done"/>
                // send the answer!!
                Log.log("retrieving the task: " + taskId);
                JXElement req = new JXElement("play-answertask-req");
                req.setAttr("id", taskId);
                req.setAttr("answer", textField.getString());
                Log.log(new String(req.toBytes(false)));
                JXElement rsp = net.utopiaReq(req);
                Log.log(new String(rsp.toBytes(false)));
                if (rsp.getTag().indexOf("-rsp") != -1) {
                    String answerState = rsp.getAttr("answerstate");
                    String mediaState = rsp.getAttr("mediastate");
                    String score = task.getChildText("score");
                    String playState = task.getChildText("playstate");
                    if (answerState.equals("ok") && mediaState.equals("open")) {
                        Log.log("we've got the right answer");
                        alert = "Right answer! You still have to sent in media though. Goodluck!";
                    } else if (answerState.equals("ok") && mediaState.equals("done") && playState.equals("open")) {
                        Log.log("we've got the right answer");
                        alert = "Right answer and you already sent in media!\nYou scored " + score + " points\n";
                    } else if (answerState.equals("ok") && mediaState.equals("done") && playState.equals("done")) {
                        Log.log("last task finished!!");
                        alert = "Right answer and you already sent in media!\nYou scored " + score + " points\n" +
                                "You have now also finished the last task!!!\nThe Game is finished...";
                        form.removeCommand(OK_CMD);
                        form.removeCommand(CANCEL_CMD);
                        form.addCommand(OUTRO_CMD);
                    } else {
                        Log.log("oops wrong answer");
                        alert = "Wrong answer! Try again...";
                    }
                } else {
                    alert = "something went wrong when sending the answer.\n Please try again.";
                }
            }
        } else if (command == OUTRO_CMD) {
            new OutroDisplay(midlet);
        } else if (command == CANCEL_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
