package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import nl.justobjects.mjox.JXElement;
import org.geotracing.client.Util;

import javax.microedition.lcdui.*;

public class MediumDisplay extends DefaultDisplay {

    private Command PLAY_VIDEO_CMD = new Command(Locale.get("medium.playVideo"), Command.SCREEN, 2);
    private Command PLAY_AUDIO_CMD = new Command(Locale.get("medium.playAudio"), Command.SCREEN, 2);

    private int screenWidth;
    private JXElement medium;
    private String mediumId = "";
    private String mediumName = "";
    private String mediumType = "";
    private String mediumUrl = "";
    private Image mediumImage;
    private String mediumText = "";
    //private Image mediumAudio;
    private VideoDisplay videoDisplay;
    private AudioDisplay audioDisplay;
    private boolean active;
    private boolean hitPlay;

    private StringItem mediumLabel = new StringItem("", "");

    public MediumDisplay(WPMidlet aMIDlet, int theScreenWidth) {
        super(aMIDlet, "Media");
        screenWidth = theScreenWidth;
    }

    public void start(String aMediumId, Displayable aPrevScreen) {
        prevScreen = aPrevScreen;

        active = true;
        Display.getDisplay(midlet).setCurrent(this);

        if (mediumId == null || !mediumId.equals(aMediumId)) {
            mediumId = aMediumId;
            mediumLabel.setText("Loading...");
            //#style labelinfo
            append(mediumLabel);

            mediumUrl = midlet.getKWUrl() + "/media.srv?id=" + aMediumId;
            getMedium(aMediumId);
        }

    }

    public String getMediumId(){
        return mediumId;
    }

    public boolean isActive() {
        return active;
    }

    public void handleGetMediumRsp(JXElement aResponse) {

        medium = aResponse.getChildByTag("record");

        // get the name
        mediumName = medium.getChildText("name");
        if (mediumName == null || mediumName.length() == 0) {
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

    public void handleGetMediumNrsp(JXElement aResponse) {

    }

    private void getMedium(String aMediumId) {
        JXElement req = new JXElement("query-store-req");
        req.setAttr("cmd", "q-medium");
        req.setAttr("id", aMediumId);
        midlet.getActiveApp().sendRequest(req);
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
            if (midlet.useExternalPlayer()) {
                //#style formbox
                append("When you click on 'play video' the video will be " +
                        "downloaded and played in your default media player." +
                        " Afterwards close the media player and continue " +
                        "here by pressing 'back'");
            } else {
                //#style formbox
                append("When you click on 'play video' the video will be " +
                        "downloaded. This might take a while.... Afterwards continue " +
                        "here by pressing 'back'");
            }

            addCommand(PLAY_VIDEO_CMD);
        } else if (mediumType.equals("audio")) {
            setTitle("Audio");
            if (midlet.useExternalPlayer()) {
                //#style formbox
                append("When you click on 'play audio' the audio will be " +
                        "downloaded and played in your default media player. " +
                        " Afterwards close the media player and continue " +
                        "here by pressing 'back'");
            } else {
                //#style formbox
                /*append("When you click on 'play audio' the audio will be " +
                        "downloaded. This might take a while.... Afterwards continue " +
                        "here by pressing 'back'");*/
                //#style formbox
                append("The audio will be downloaded automatically - please be patient.");
                try {
                    Util.playStream(mediumUrl);
                } catch (Throwable t) {
                    //#style alertinfo
                    append("Could not play the audio:" + t.getMessage());
                }
            }

            addCommand(PLAY_AUDIO_CMD);
        }
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            active = false;
            if (hitPlay) {
                hitPlay = false;
                midlet.getPlayApp().setBypass();
                midlet.getActiveApp().connect();
            }

            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (command == PLAY_VIDEO_CMD) {
            if (midlet.useExternalPlayer()) {
                try {
                    hitPlay = true;
                    midlet.platformRequest(mediumUrl);
                } catch (Throwable t) {
                    //#style alertinfo
                    append("Can not play video (" + t.getMessage() + ")");
                }
            } else {
                new VideoDisplay(midlet, mediumName, mediumUrl, this);
                /*if(videoDisplay == null){
                    videoDisplay = new VideoDisplay(midlet);
                }
                videoDisplay.start(this, mediumName, mediumUrl);*/
            }
        } else if (command == PLAY_AUDIO_CMD) {
            try {
                if (midlet.useExternalPlayer()) {
                    hitPlay = true;
                    midlet.platformRequest(mediumUrl);
                } else {
                    if (audioDisplay == null) {
                        audioDisplay = new AudioDisplay(midlet, this);
                    }
                    audioDisplay.play(mediumName, mediumUrl);
                }
            } catch (Throwable t) {
                //#style alertinfo
                append("Can not play audio (" + t.getMessage() + ")");
            }
        }
    }

}
