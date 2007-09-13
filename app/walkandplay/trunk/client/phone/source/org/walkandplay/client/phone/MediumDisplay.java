package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.Util;

import javax.microedition.lcdui.*;
import javax.microedition.media.Player;
import javax.microedition.media.Manager;
import javax.microedition.media.PlayerListener;
import java.util.Date;

public class MediumDisplay extends DefaultDisplay implements TCPClientListener{

    private Command PLAY_VIDEO_CMD = new Command(Locale.get("medium.playVideo"), Command.SCREEN, 2);
    private Command PLAY_AUDIO_CMD = new Command(Locale.get("medium.playAudio"), Command.SCREEN, 2);    

    private int screenWidth;
    private JXElement medium;
    private String mediumId;
    private String mediumName;
    private String mediumType;
    private String mediumUrl;
    private Image mediumImage;
    private String mediumText;
    //private Image mediumAudio;
    private MediumDisplay mediumDisplay = this;
    private VideoDisplay videoDisplay;
    private AudioDisplay audioDisplay;
    private boolean active;

    private StringItem mediumLabel = new StringItem("", "");

    public MediumDisplay(WPMidlet aMIDlet, int theScreenWidth) {
        super(aMIDlet, "Media");
        screenWidth = theScreenWidth;
    }

    public void start(String aMediumId, Displayable aPrevScreen){
        midlet.getActiveApp().addTCPClientListener(this);
        prevScreen = aPrevScreen;

        active = true;
        Display.getDisplay(midlet).setCurrent(this);

        if(mediumId==null || !mediumId.equals(aMediumId)){
            mediumId = aMediumId;
            mediumLabel.setText("Loading...");
            //#style labelinfo
            append(mediumLabel);

            mediumUrl = midlet.getKWUrl() + "/media.srv?id=" + aMediumId;
            getMedium(aMediumId);
        }

    }

    public boolean isActive(){
        return active;
    }

    private void drawMedium() {
        // start fresh for when a new medium is drawn
        deleteAll();

        if (mediumName != null && mediumName.length() > 0) {
            mediumLabel.setText(mediumName);
        } else {
            mediumLabel.setText("Untitled");
        }

        String desc = medium.getChildText("description");

        if (desc != null && desc.length() > 0) {
            //#style formbox
            append(desc);
        }

        if (mediumType.equals("image")) {
            setTitle("Image");
            append(mediumImage);

        } else if (mediumType.equals("text")) {
            setTitle("Text");
            //#style formbox    
            append(mediumText);

        } else if (mediumType.equals("video")) {
            setTitle("Video");
            if (midlet.useInternalMediaPlayer()) {
                //#style formbox
                append("When you click on 'play video' the video will be " +
                        "downloaded. This might take a while.... Afterwards continue " +
                        "here by pressing 'back'");
            } else {
                //#style formbox
                append("When you click on 'play video' the video will be " +
                "downloaded and played in your default media player." +
                " Afterwards close the media player and continue " +
                "here by pressing 'back'");
            }

            addCommand(PLAY_VIDEO_CMD);
        } else if (mediumType.equals("audio")) {
            setTitle("Audio");
            if (midlet.useInternalMediaPlayer()) {
                //#style formbox
                /*append("When you click on 'play audio' the audio will be " +
                        "downloaded. This might take a while.... Afterwards continue " +
                        "here by pressing 'back'");*/
                append("The audio will be downloaded automatically - please be patient");
                try{
                    Util.playStream(mediumUrl);
                }catch(Throwable t){
                    //#style alertinfo
                    append("Could not play the audio:" + t.getMessage());                    
                }
            } else {
                //#style formbox
                append("When you click on 'play audio' the audio will be " +
                        "downloaded and played in your default media player. " +
                        " Afterwards close the media player and continue " +
                        "here by pressing 'back'");
            }

            addCommand(PLAY_AUDIO_CMD);
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("query-store-rsp")) {
                String cmd = rsp.getAttr("cmd");
                if (cmd.equals("q-medium")) {
                    medium = rsp.getChildByTag("record");
                    
                    // get the name
                    mediumName = medium.getChildText("name");
                    if(mediumName == null || mediumName.length() == 0){
                        mediumName = "Untitled";
                    }
                    // store the mediumType
                    mediumType = medium.getChildText("type");

                    if (mediumType.equals("image")) {
                        try {
                            mediumUrl += "&resize=" + (screenWidth - 12);
                            mediumImage = Util.getImage(mediumUrl);
                        } catch (Throwable t) {
                            Log.log("Error retrieving image");
                        }
                    } else if (mediumType.equals("text")) {
                        try {
                            mediumText = Util.getPage(mediumUrl);                            
                        } catch (Throwable t) {
                            Log.log("Error fetching text url=");
                        }
                    }

                    // now draw the info
                    drawMedium();
                    
                }
            }
        }
    }

    public void onNetStatus(String aStatus) {

    }

    public void onConnected() {

    }

    public void onError(String anErrorMessage) {
        //#style alertinfo
        append(anErrorMessage);
    }

    public void onFatal() {
        midlet.getActiveApp().exit();
        Display.getDisplay(midlet).setCurrent(midlet.getActiveApp());
    }

    private void getMedium(String aMediumId) {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-medium");
        req.setAttr("id", aMediumId);
        midlet.getActiveApp().sendRequest(req);
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            midlet.getActiveApp().removeTCPClientListener(this);
            active = false;
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (command == PLAY_VIDEO_CMD) {
            if (midlet.useInternalMediaPlayer()) {
                new VideoDisplay(midlet, mediumName, mediumUrl, this);
                /*if(videoDisplay == null){
                    videoDisplay = new VideoDisplay(midlet);
                }
                videoDisplay.start(this, mediumName, mediumUrl);*/
            } else {
                try {
                    midlet.platformRequest(mediumUrl);
                } catch (Throwable t) {
                    //#style alertinfo
                    append("Can not play video (" + t.getMessage() + ")");
                }
            }
        } else if (command == PLAY_AUDIO_CMD) {
            try {
                if (midlet.useInternalMediaPlayer()) {
                    if(audioDisplay == null){
                        audioDisplay = new AudioDisplay(midlet, this);
                    }
                    audioDisplay.play(mediumName, mediumUrl);

                } else {
                    midlet.platformRequest(mediumUrl);
                }
            } catch (Throwable t) {
                //#style alertinfo
                append("Can not play audio (" + t.getMessage() + ")");
            }
        }
    }

}
