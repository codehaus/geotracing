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
    private Command TRY_AGAIN_CMD = new Command(Locale.get("task.TryAgain"), Command.CANCEL, 1);
    private String MEDIUM_BASE_URL;

    private TextField inputField;
    private int screenWidth;
    private JXElement task;
    private String taskId;
    private Image taskImage;
    private String answer = "";
    private String state = "";
    private String answerState = "";
    private String mediaState = "";
    private boolean active;

    public TaskDisplay(WPMidlet aMIDlet, int theScreenWidth, Displayable aPrevScreen) {
        super(aMIDlet, "Task");
        screenWidth = theScreenWidth;
        prevScreen = aPrevScreen;
        MEDIUM_BASE_URL = midlet.getKWUrl() + "/media.srv?id=";
    }

    public void start(String aTaskId, String aState, String anAnswerState, String aMediaState){
        Log.log("start - taskId:" + aTaskId + ", state: " + state + ", answerState: " + anAnswerState + ", mediaState:" + aMediaState);
        // start clean
        deleteAll();
        removeAllCommands();

        // set the tcp listemer
        midlet.getActiveApp().addTCPClientListener(this);

        // only set the state for the first time - after 'start' state updates are done by play-answertask-rsp
        if(state.length() == 0) state = aState;
        if(answerState.length() == 0) answerState = anAnswerState;
        if(mediaState.length() == 0) mediaState = aMediaState;

        Log.log("Used state: " + state + ", answerState: " + anAnswerState + ", mediaState:" + aMediaState);

        // only get and display the task if it's not done yet
        if(!state.equals("done")){
            if(task == null || !taskId.equals(aTaskId)) {
                taskId = aTaskId;
                getTask();
            }else{
                drawScreen();
            }
        }else{
            drawScreen();
        }

        // display is active
        active = true;
        // now show the display
        Display.getDisplay(midlet).setCurrent(this);
    }

    public boolean isActive(){
        return active;
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
                            // TODO: do this in a separate thread??
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
                        append("No task found with id:" + taskId);
                    }
                }

            } else if (rsp.getTag().equals("play-answertask-rsp")) {
                // start fresh
                deleteAll();
                removeAllCommands();

                answerState = rsp.getAttr("answerstate");
                mediaState = rsp.getAttr("mediastate");
                state = rsp.getAttr("state");

                String score = task.getChildText("score");

                if (answerState.equals("ok") && mediaState.equals("open")) {
                    //#style alertinfo
                    append("Right answer! You still have to sent in media though. Good luck!");
                    addCommand(BACK_CMD);
                } else if (answerState.equals("ok") && mediaState.equals("done") && state.equals("open")) {
                    //#style alertinfo
                    append("Right answer and you already sent in media!\nYou scored " + score + " points");
                    answer = inputField.getString();
                    addCommand(BACK_CMD);
                } else if (answerState.equals("ok") && mediaState.equals("done") && state.equals("done")) {
                    //#style alertinfo
                    append("Right answer and you already sent in media!\nYou scored " + score + " points\n" +
                            "You have now also finished the last task!!!\nThe Game is finished...");
                    addCommand(OUTRO_CMD);
                } else {
                    //#style alertinfo
                    append("Oops, wrong answer! Try again...");
                    removeCommand(BACK_CMD);
                    removeCommand(OK_CMD);
                    addCommand(TRY_AGAIN_CMD);
                }
            }else if (rsp.getTag().equals("play-answertask-nrsp")) {
                deleteAll();
                removeAllCommands();
                addCommand(BACK_CMD);
                //#style alertinfo
                append("Serious error: " + rsp.getAttr("details"));
            }
        }
    }

    private void drawScreen() {
        Log.log("state: " + state);
        Log.log("answerState: " + answerState);
        Log.log("mediaState: " + mediaState);
        if(state.equals("done")){
            //#style alertinfo
            append(Locale.get("task.TaskDone"));
            removeCommand(OK_CMD);
        }else{
            if(answerState.equals("ok")){
                //#style alertinfo
                append(Locale.get("task.UploadMedia"));
            }else{
                //#style alertinfo
                append(Locale.get("task.Info"));
            }
            //#style formbox
            append(task.getChildText("name"));
            //#style formbox
            append(task.getChildText("description"));
            //<task-hit id="54232" state="open|hit|done" answerstate="open" mediastate="open"/>
            append(taskImage);
            //#style labelinfo
            append("answer");
            if(answerState.equals("ok")){
                //#style textbox
                inputField = new TextField("", answer, 1024, TextField.UNEDITABLE);
            }else{
                //#style textbox
                inputField = new TextField("", "", 1024, TextField.ANY);
            }
            append(inputField);

            addCommand(OK_CMD);
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

    private void getTask() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-task");
        req.setAttr("id", taskId);
        midlet.getActiveApp().sendRequest(req);
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == OK_CMD) {
            answer = inputField.getString();
            if (answer == null || answer.equals("")) {
                deleteAll();
                drawScreen();
                //#style alertinfo
                append("Don't forget to fill in your answer");
                addCommand(BACK_CMD);
            } else {
                //<play-answertask-rsp state="hit" mediastate="open|done" answerstate="notok|ok" score="75" playstate="open|done"/>
                JXElement req = new JXElement("play-answertask-req");
                req.setAttr("id", taskId);
                req.setAttr("answer", inputField.getString());
                midlet.getActiveApp().sendRequest(req);
            }
        } else if (command == OUTRO_CMD) {
            active = false;
            new OutroDisplay(midlet);
        } else if (command == BACK_CMD) {
            active = false;
            midlet.getActiveApp().removeTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }else if (command == TRY_AGAIN_CMD) {
            deleteAll();
            removeCommand(TRY_AGAIN_CMD);
            addCommand(BACK_CMD);
            addCommand(OK_CMD);
            drawScreen();
        }
    }

}
