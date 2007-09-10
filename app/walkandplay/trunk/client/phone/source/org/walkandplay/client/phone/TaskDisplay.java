package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.Util;
import org.walkandplay.client.phone.Log;
import org.walkandplay.client.phone.TCPClientListener;

import javax.microedition.lcdui.*;

import de.enough.polish.util.Locale;

public class TaskDisplay extends DefaultDisplay implements TCPClientListener {

    private Command OK_CMD = new Command("OK", Command.OK, 1);
    private Command OUTRO_CMD = new Command("Outro", Command.CANCEL, 1);
    private String MEDIUM_BASE_URL;

    private TextField inputField;
    private int screenWidth;
    private JXElement task;
    private int taskId;
    private Image taskImage;

    public TaskDisplay(WPMidlet aMIDlet, int aTaskId, int theScreenWidth, Displayable aPrevScreen) {
        super(aMIDlet, "Task");
        screenWidth = theScreenWidth;
        prevScreen = aPrevScreen;
        midlet.getActiveApp().addTCPClientListener(this);
        MEDIUM_BASE_URL = midlet.getKWUrl() + "/media.srv?id=";
        taskId = aTaskId;

        getTask();
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("query-store-rsp")) {
                String cmd = rsp.getAttr("cmd");
                if(cmd.equals("q-task")){
                    task = rsp.getChildByTag("record");
                    if (task != null) {
                        String mediumId = task.getChildText("mediumid");
                        String url = MEDIUM_BASE_URL + mediumId + "&resize=" + (screenWidth - 13);
                        try {
                            taskImage = Util.getImage(url);
                        } catch (Throwable t) {
                            //#style alertinfo
                            append("Could not get the image for this task");
                            Log.log("Error fetching task image url: " + url);
                        }
                        drawScreen();
                    } else {
                        deleteAll();
                        addCommand(BACK_CMD);
                        //#style alertinfo
                        append("Serious error: No task found");
                    }
                }
            } else if (rsp.getTag().equals("play-answertask-rsp")) {
                deleteAll();
                addCommand(BACK_CMD);
                String answerState = rsp.getAttr("answerstate");
                String mediaState = rsp.getAttr("mediastate");
                String score = task.getChildText("score");
                String playState = task.getChildText("playstate");
                if (answerState.equals("ok") && mediaState.equals("open")) {
                    //#style alertinfo
                    append("Right answer! You still have to sent in media though. Good luck!");
                } else if (answerState.equals("ok") && mediaState.equals("done") && playState.equals("open")) {
                    //#style alertinfo
                    append("Right answer and you already sent in media!\nYou scored " + score + " points");
                } else if (answerState.equals("ok") && mediaState.equals("done") && playState.equals("done")) {
                    //#style alertinfo
                    append("Right answer and you already sent in media!\nYou scored " + score + " points\n" +
                            "You have now also finished the last task!!!\nThe Game is finished...");
                    removeCommand(BACK_CMD);
                    addCommand(OUTRO_CMD);
                } else {
                    //#style alertinfo
                    append("Oops, wrong answer! Try again...");
                }
            }else if (rsp.getTag().equals("play-answertask-nrsp")) {
                deleteAll();
                addCommand(BACK_CMD);
                //#style alertinfo
                append("Serious error: " + rsp.getAttr("details"));
            }
        }
    }

    private void drawScreen() {
        //#style labelinfo
        append(Locale.get("task.Info"));
        //#style formbox
        append(task.getChildText("name"));
        //#style formbox
        append(task.getChildText("description"));
        //<task-hit id="54232" state="open|hit|done" answerstate="open" mediastate="open"/>
        append(taskImage);
        //#style labelinfo
        append("answer");
        //#style textbox
        inputField = new TextField("", "", 1024, TextField.ANY);
        append(inputField);
        addCommand(OK_CMD);
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

    private void getTask() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-task");
        req.setAttr("id", taskId);
        midlet.getActiveApp().sendRequest(req);
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == OK_CMD) {
            if (inputField.getString() == null) {
                deleteAll();
                //#style alertinfo
                append("Don't forget to fill in your answer");
                drawScreen();
                addCommand(BACK_CMD);
            } else {
                //<play-answertask-rsp state="hit" mediastate="open|done" answerstate="notok|ok" score="75" playstate="open|done"/>
                JXElement req = new JXElement("play-answertask-req");
                req.setAttr("id", taskId);
                req.setAttr("answer", inputField.getString());
                midlet.getActiveApp().sendRequest(req);
            }
        } else if (command == OUTRO_CMD) {
            new OutroDisplay(midlet);
        } else if (command == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

}
