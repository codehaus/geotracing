package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.Util;

import javax.microedition.lcdui.*;

import de.enough.polish.util.Locale;

public class TaskDisplay extends DefaultDisplay implements TCPClientListener {

    private Command OK_CMD = new Command("OK", Command.OK, 1);
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
    private TaskDisplay instance;
    private ErrorHandler errorHandler;

    public TaskDisplay(WPMidlet aMIDlet, int theScreenWidth, Displayable aPrevScreen) {
        super(aMIDlet, "Task");
        instance = this;
        screenWidth = theScreenWidth;
        prevScreen = aPrevScreen;
        MEDIUM_BASE_URL = midlet.getKWUrl() + "/media.srv?id=";
        addCommand(OK_CMD);
    }

    public void start(String aTaskId, String aState, String anAnswerState, String aMediaState){
        Log.log("start - taskId:" + aTaskId + ", state: " + aState + ", answerState: " + anAnswerState + ", mediaState:" + aMediaState);

        // set the tcp listemer
        midlet.getActiveApp().addTCPClientListener(this);

        // only set the state for the first time - after 'start' state updates are done by play-answertask-rsp
        if(state.length() == 0) state = aState;
        if(answerState.length() == 0) answerState = anAnswerState;
        if(mediaState.length() == 0) mediaState = aMediaState;

        Log.log("Used state: " + state + ", answerState: " + answerState + ", mediaState:" + mediaState);

        // only get and display the task if it's not done yet
        if(state.equals("done")){
            getErrorHandler().showGoBack("You already completed this task.");
            return;
        }else{
            if(task == null || !taskId.equals(aTaskId)){
                taskId = aTaskId;
                getTask();
                return;
            }
        }

        // a right answer was given
        if(answerState.equals("ok") && !anAnswerState.equals(answerState)){
            deleteAll();
            drawScreen();
        }

        // show the display
        active = true;
        Display.getDisplay(midlet).setCurrent(this);
    }

    public boolean isActive(){
        return active;
    }

    private void drawScreen(){
        if(answerState.equals("ok")){
            //#style alertinfo
            append(Locale.get("task.UploadMedia"));
            removeCommand(OK_CMD);
        }else{
            //#style alertinfo
            append(Locale.get("task.Info"));
        }
        //#style formbox
        append(task.getChildText("name"));
        //#style formbox
        append(task.getChildText("description"));

        append(taskImage);

        if(answerState.equals("ok")){
            if(answer.length()>0){
                //#style labelinfo
                append("your answer");
                //#style textbox
                inputField = new TextField("", answer, 1024, TextField.UNEDITABLE);
                append(inputField);
            }
        }else{
            //#style labelinfo
            append("your answer");
            //#style textbox
            inputField = new TextField("", "", 1024, TextField.ANY);
            append(inputField);
        }        
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

            } else if (rsp.getTag().equals("play-answertask-rsp")) {
                //<utopia-rsp logts="1189673784658" ><play-answertask-rsp state="open" mediastate="open" answerstate="notok" score="0" playstate="running" /></utopia-rsp>

                answerState = rsp.getAttr("answerstate");
                mediaState = rsp.getAttr("mediastate");
                state = rsp.getAttr("state");

                Log.log("state set: " + state + ", answerState: " + answerState + ", mediaState:" + mediaState);
                String score = task.getChildText("score");

                if (answerState.equals("ok") && mediaState.equals("open")) {
                    getErrorHandler().showGoBack("Right answer! You still have to sent in media though. Good luck!");
                } else if (answerState.equals("ok") && mediaState.equals("done") && state.equals("open")) {
                    answer = inputField.getString();
                    getErrorHandler().showGoBack("Right answer and you already sent in media!\nYou scored " + score + " points");
                } else if (answerState.equals("ok") && mediaState.equals("done") && state.equals("done")) {
                    getErrorHandler().showOutro(score);
                } else {
                    getErrorHandler().showTryAgain("Oops, wrong answer! Try again...");
                }
            }else if (rsp.getTag().equals("play-answertask-nrsp")) {
                getErrorHandler().showGoBack(rsp.getAttr("details"));
            }
        }
    }

    public void onNetStatus(String aStatus){

    }

    public void onConnected(){

    }

    public void onError(String anErrorMessage){
        new ErrorHandler().showGoBack(anErrorMessage);
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
            midlet.getActiveApp().removeTCPClientListener(this);
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

    private ErrorHandler getErrorHandler(){
        if(errorHandler == null){
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
            }else if (command == BACK_CMD) {
                active = false;
                midlet.getActiveApp().removeTCPClientListener(instance);
                Display.getDisplay(midlet).setCurrent(prevScreen);
            }else if (command == OUTRO_CMD) {
                active = false;
                new OutroDisplay(midlet);
            }            
		}
	}

}
