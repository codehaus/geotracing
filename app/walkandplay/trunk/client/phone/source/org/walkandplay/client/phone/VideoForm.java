package org.walkandplay.client.phone;

import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.*;
import javax.microedition.media.Player;
import javax.microedition.media.Manager;
import javax.microedition.media.control.VideoControl;

public class VideoForm extends DefaultDisplay{
    private Player player = null; // player instance
    private String url;

    private Command BACK_CMD = new Command("Back", Command.ITEM, 2);
    private Command HOME_CMD = new Command("Home", Command.ITEM, 2);

    public VideoForm(WPMidlet aMidlet, String aUrl) {
        super(aMidlet, "Video");
        url = aUrl;

        addCommand(BACK_CMD);
        addCommand(HOME_CMD);
        setCommandListener(this);
        play();
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == HOME_CMD) {
            midlet.setHome();
        } else if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(midlet.playDisplay);
        }
    }

    public void play() {

        // load in video in mpg and get ready the player
        try {
            player = Manager.createPlayer(url);
            player.realize();
            player.prefetch();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // get video control instance
        VideoControl vidc = (VideoControl) player.getControl("VideoControl");
        //Item videoDisp = (Item)vidc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, null);
        Item videoDisp = (Item)vidc.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
        append(videoDisp);

        int vW = vidc.getSourceWidth();
        int vH = vidc.getSourceHeight();

        // set video window size
        vidc.setDisplayLocation((getWidth() - vW) / 2, (getHeight() - vH) / 2);
        try {
            vidc.setDisplaySize(vidc.getSourceWidth(), vidc.getSourceHeight());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        vidc.setVisible(true);
        // watch video
        try {
            player.start();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
