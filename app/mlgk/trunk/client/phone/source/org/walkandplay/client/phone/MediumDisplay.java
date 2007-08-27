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
    private String mediumName;
    private String mediumType;
    private String mediumUrl;
    private Image mediumImage;
    private MediumDisplay mediumDisplay = this;

    private StringItem mediumLabel = new StringItem("", "");

    public MediumDisplay(WPMidlet aMIDlet, String aMediumId, int theScreenWidth, Displayable aPrevScreen) {
        super(aMIDlet, "Media");
        screenWidth = theScreenWidth;
        prevScreen = aPrevScreen;
        midlet.getActiveApp().addTCPClientListener(this);
        mediumUrl = midlet.getKWUrl() + "/media.srv?id=" + aMediumId;

        mediumLabel.setText("Loading...");
        //#style labelinfo
        append(mediumLabel);

        getMedium(aMediumId);
    }

    private void drawMedium() {
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
                append("When you click on 'play audio' the audio will be " +
                        "downloaded. This might take a while.... Afterwards continue " +
                        "here by pressing 'back'");
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
                    mediumName = medium.getChildText("name");
                    if(mediumName == null || mediumName.length() == 0){
                        mediumName = "Untitled";
                    }
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
                            medium.setChildText("description", Util.getPage(mediumUrl));
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
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (command == PLAY_VIDEO_CMD) {
            if (midlet.useInternalMediaPlayer()) {
                new VideoDisplay(midlet, mediumName, mediumUrl, this);
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
                    new AudioPlayer().play(mediumName, mediumUrl);
                } else {
                    midlet.platformRequest(mediumUrl);
                }
            } catch (Throwable t) {
                //#style alertinfo
                append("Can not play audio (" + t.getMessage() + ")");
            }
        }
    }

    private class AudioPlayer implements CommandListener, PlayerListener {
		private Command BACK_CMD = new Command("Back", Command.BACK, 1);
        private Player player;
        private Gauge progressBar = new Gauge("", false, 100, 0);
        private StringItem state = new StringItem("", "");
        private Form form;
        private String name;

        public void playerUpdate(Player aPlayer, String anEvent, Object theDate) {
            form.setTitle("Audio: " + name + " [" + anEvent + "]");
            //state.setText(anEvent);
        }

        public void play(String aName, String anUrl) {
            try {
                name = aName;
                //#style defaultscreen
                form = new Form("Audio: " + name + " [downloading]");
                //#style labelinfo
                form.append(aName);
                /*//#style labelinfo
                form.append(state);*/
                //#style labelinfo
                form.append(progressBar);

                form.addCommand(BACK_CMD);
                form.setCommandListener(this);
                Display.getDisplay(midlet).setCurrent(form);

                progressBar.setValue(25);
                player = Manager.createPlayer(mediumUrl);
                player.addPlayerListener(this);
                progressBar.setValue(40);
                
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            player.prefetch();
                            progressBar.setValue(80);
                            player.start();
                            progressBar.setValue(100);
                        }catch(Throwable t){
                            Log.log(t.getMessage());
                            state.setText("Error:" + t.getMessage());
                        }
                    }
                }).start();
            } catch (Throwable t) {
                // nada
            }
		}

		public void commandAction(Command command, Displayable screen) {
            try{
                if(player!=null){
                    player.stop();
                    player.deallocate();
                    player.close();
                    player = null;
                }
            }catch(Throwable t){
                // nada
            }
            Display.getDisplay(midlet).setCurrent(mediumDisplay);
        }
	}
}
