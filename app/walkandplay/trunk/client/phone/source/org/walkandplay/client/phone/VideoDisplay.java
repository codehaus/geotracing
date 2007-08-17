package org.walkandplay.client.phone;

import org.walkandplay.client.phone.util.Log;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

public class VideoDisplay extends GameCanvas implements CommandListener {
    private Player player = null; // player instance
    private WPMidlet midlet;
    protected Displayable prevScreen;
    private String url;
    private Image bg;

    private Command BACK_CMD = new Command("Back", Command.ITEM, 2);
    private Command HOME_CMD = new Command("Home", Command.ITEM, 2);

    public VideoDisplay(WPMidlet aMidlet, String aUrl, Displayable aPrevScreen) {
        super(false);
        midlet = aMidlet;
        prevScreen = aPrevScreen;
        setFullScreenMode(true);

        url = aUrl;

        try {
            //#ifdef polish.images.directLoad
            bg = Image.createImage("/bg.png");
            //#else
            bg = scheduleImage("/bg.png");
            //#endif
        } catch (Throwable t) {
            Log.log(t.getMessage());
        }

        addCommand(BACK_CMD);
        addCommand(HOME_CMD);
        setCommandListener(this);
        play();
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else {
            midlet.setHome();
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
        vidc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
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

    public void paint(Graphics g) {
        g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
        Font font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM);
        g.setFont(font);
        g.setColor(0);
        g.drawString("Video", (getWidth() - font.stringWidth("Video")) / 2, 2, Graphics.TOP | Graphics.LEFT);

    }
}
