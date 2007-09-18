package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Util;

import javax.microedition.lcdui.*;

public class TaskDisplay extends DefaultDisplay {

    private Command OK_CMD = new Command("OK", Command.OK, 1);
    private String MEDIUM_BASE_URL;

    private TextField inputField;
    private int screenWidth;
    private JXElement task;
    private String taskId;
    private String taskName = "";
    private String taskDescription = "";
    private String taskScore = "";
    private Image taskImage;
    private String answer = "";
    private String state = "";
    private String answerState = "";
    private String mediaState = "";
    private boolean active;
    private TaskDisplay instance;
    private ErrorHandler errorHandler;
    private int nrOfTasks;

    public TaskDisplay(WPMidlet aMIDlet, int theScreenWidth, int theNrOfTasks, Displayable aPrevScreen) {
        super(aMIDlet, "Task");
        instance = this;
        screenWidth = theScreenWidth;
        nrOfTasks = theNrOfTasks;
        prevScreen = aPrevScreen;
        MEDIUM_BASE_URL = midlet.getKWUrl() + "/media.srv?id=";
        addCommand(OK_CMD);
    }

    public void start(String aTaskId, String aState, String anAnswerState, String aMediaState) {
        Log.log("start - taskId:" + aTaskId + ", state: " + aState + ", answerState: " + anAnswerState + ", mediaState:" + aMediaState);

        // only set the state for the first time - after 'start' state updates are done by play-answertask-rsp
        if (state.length() == 0) state = aState;
        if (answerState.length() == 0) answerState = anAnswerState;
        if (mediaState.length() == 0) mediaState = aMediaState;

        Log.log("Used state: " + state + ", answerState: " + answerState + ", mediaState:" + mediaState);

        // only get and display the task if it's not done yet
        if (state.equals("done")) {
            getErrorHandler().showGoBack("You already completed this task.");
            return;
        } else {
            if (task == null || !taskId.equals(aTaskId)) {
                taskId = aTaskId;
                queryTask();
                return;
            }
        }

        // a right answer was given
        if (answerState.equals("ok") && !anAnswerState.equals(answerState)) {
            deleteAll();
            drawScreen();
        }

        // show the display
        active = true;
        Display.getDisplay(midlet).setCurrent(this);
    }

    public boolean isActive() {
        return active;
    }

    public String getState() {
        return state;
    }

    public String getMediaState() {
        return mediaState;
    }

    public void setState(String aState) {
        state = aState;
    }

    public void setMediaState(String aMediaState) {
        mediaState = aMediaState;
    }

    public String getAnswerState() {
        return answerState;
    }

    public void setStates(String aState, String anAnswerState, String aMediaState) {
        if (state != null) state = aState;
        if (answerState != null) answerState = anAnswerState;
        if (mediaState != null) mediaState = aMediaState;
    }

    public JXElement getTask() {
        task.setAttr("state", state);
        task.setAttr("answerstate", answerState);
        task.setAttr("mediastate", mediaState);
        return task;
    }

    private void drawScreen() {
        if (answerState.equals("ok")) {
            //#style alertinfo
            append(Locale.get("task.UploadMedia"));
            removeCommand(OK_CMD);
        } else {
            //#style alertinfo
            append(Locale.get("task.Info"));
        }
        //#style formbox
        append(taskName + "(" + taskScore + " pts)");
        //#style formbox
        append(taskDescription);

        append(taskImage);

        if (answerState.equals("ok")) {
            if (answer.length() > 0) {
                //#style labelinfo
                append("your answer");
                //#style textbox
                inputField = new TextField("", answer, 1024, TextField.UNEDITABLE);
                append(inputField);
            }
        } else {
            //#style labelinfo
            append("your answer");
            //#style textbox
            inputField = new TextField("", "", 1024, TextField.ANY);
            append(inputField);
        }
    }

    public String getTaskName() {
        return taskName;
    }

    public String getTaskScore() {
        return taskScore;
    }

    public void handleGetTaskRsp(JXElement aResponse) {
        task = aResponse.getChildByTag("record");
        if (task != null) {

            taskName = task.getChildText("name");
            taskDescription = task.getChildText("description");
            taskScore = task.getChildText("score");

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

            // start fresh for when a new task is drawn
            deleteAll();

            drawScreen();

            // show the display
            active = true;
            Display.getDisplay(midlet).setCurrent(this);

        } else {
            getErrorHandler().showGoBack("No task found with id:" + taskId);
        }
    }

    public void handleGetTaskNrsp(JXElement aResponse) {
        getErrorHandler().showGoBack("Could not get the task");
    }

    public void handleAnswerTaskRsp(JXElement aResponse) {
        //<utopia-rsp logts="1189673784658" ><play-answertask-rsp state="open" mediastate="open" answerstate="notok" score="0" playstate="running" /></utopia-rsp>
        answerState = aResponse.getAttr("answerstate");
        mediaState = aResponse.getAttr("mediastate");
        state = aResponse.getAttr("state");

        Log.log("state set: " + state + ", answerState: " + answerState + ", mediaState:" + mediaState);
        String score = task.getChildText("score");

        if (answerState.equals("ok") && mediaState.equals("open")) {
            getErrorHandler().showGoBack("Right answer! You still have to sent in media though. Good luck!");
        } else if (!answerState.equals("ok") && mediaState.equals("done")) {
            answer = inputField.getString();
            getErrorHandler().showGoBack("Ok you send in media! Now fill in the right answer");
        } else if (state.equals("done")) {
            nrOfTasks--;
            if(nrOfTasks == 0){
                getErrorHandler().showOutro(score);
            }else{
                getErrorHandler().showGoBack("Right answer and you sent in media!\nYou scored " + score + " points. Still " + nrOfTasks + " tasks to go.");
            }            
        } else {
            getErrorHandler().showTryAgain("Oops, wrong answer! Try again...");
        }


    }

    public void handleAnswerTaskNrsp(JXElement aResponse) {
        getErrorHandler().showGoBack("Problem answering the task:" + aResponse.getAttr("details"));
    }

    private void queryTask() {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-task");
        req.setAttr("id", taskId);
        midlet.getActiveApp().sendRequest(req);
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == OK_CMD) {
            answer = inputField.getString();
            if (answer == null || answer.equals("")) {
                getErrorHandler().showTryAgain("Don't forget to fill in your answer");
            } else {
                //<play-answertask-rsp state="hit" mediastate="open|done" answerstate="notok|ok" score="75" playstate="open|done"/>
                JXElement req = new JXElement("play-answertask-req");
                req.setAttr("id", taskId);
                req.setAttr("answer", inputField.getString());
                midlet.getActiveApp().sendRequest(req);
            }
        } else if (command == BACK_CMD) {
            active = false;
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

    private ErrorHandler getErrorHandler() {
        if (errorHandler == null) {
            errorHandler = new ErrorHandler();
        }
        return errorHandler;
    }

    private class ErrorHandler implements CommandListener {
        private Command BACK_CMD = new Command("Back", Command.CANCEL, 1);
        private Command OUTRO_CMD = new Command("Outro", Command.CANCEL, 1);
        private Command TRY_AGAIN_CMD = new Command(Locale.get("task.TryAgain"), Command.CANCEL, 1);

        private Form form;

        public void showTryAgain(String aMsg) {
            //#style defaultscreen
            form = new Form("TaskDisplay");

            //#style alertinfo
            form.append(aMsg);
            form.addCommand(TRY_AGAIN_CMD);

            form.setCommandListener(this);
            Display.getDisplay(midlet).setCurrent(form);
        }

        public void showGoBack(String aMsg) {
            //#style defaultscreen
            form = new Form("TaskDisplay");

            //#style alertinfo
            form.append(aMsg);
            form.addCommand(BACK_CMD);

            form.setCommandListener(this);
            Display.getDisplay(midlet).setCurrent(form);
        }

        public void showOutro(String aScore) {
            //#style defaultscreen
            form = new Form("TaskDisplay");

            //#style alertinfo
            form.append("Right answer and you already sent in media!\nYou scored " + aScore + " points\n" +
                    "You have now also finished the last task!!!\nThe Game is finished...");
            form.addCommand(OUTRO_CMD);

            form.setCommandListener(this);
            Display.getDisplay(midlet).setCurrent(form);
        }

        public void commandAction(Command command, Displayable screen) {
            if (command == TRY_AGAIN_CMD) {
                inputField.setString("");
                answer = "";
                Display.getDisplay(midlet).setCurrent(instance);
            } else if (command == BACK_CMD) {
                active = false;
                Display.getDisplay(midlet).setCurrent(prevScreen);
            } else if (command == OUTRO_CMD) {
                active = false;
                new OutroDisplay(midlet);
            }
        }
    }

}
