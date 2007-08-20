package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;
import java.util.Date;

public class VideoDisplay extends GameCanvas implements CommandListener, PlayerListener {
    private Player player = null; // player instance
    VideoControl vidc = null;
    private WPMidlet midlet;
    private String duration;
    private String contentType;
    protected Displayable prevScreen;
    private String url;
    private Image bg;

    Font f;
    int fh, w, h;

    private Command BACK_CMD = new Command("Back", Command.ITEM, 2);
    private Command HOME_CMD = new Command("Home", Command.ITEM, 2);

    public VideoDisplay(WPMidlet aMidlet, String aUrl, Displayable aPrevScreen) {
        super(false);
        midlet = aMidlet;
        prevScreen = aPrevScreen;
        setFullScreenMode(true);
        Display.getDisplay(midlet).setCurrent(this);

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

    public void playerUpdate(Player aPlayer, String anEvent, Object theDate) {
        Log.log("player update:" + anEvent);
        if (anEvent.equals(PlayerListener.END_OF_MEDIA)) {
            try {
                defPlayer();
            }
            catch (Throwable t) {
            }
            //reset();
        }
    }

    private void defPlayer() throws MediaException {
        if (player != null) {
            if (player.getState() == Player.STARTED) {
                player.stop();
            }
            if (player.getState() == Player.PREFETCHED) {
                player.deallocate();
            }
            if (player.getState() == Player.REALIZED || player.getState() == Player.UNREALIZED) {
                player.close();
            }
        }
        player = null;
    }

    private void stop() {
        try {
            if (player != null) {
                player.stop();
                player.deallocate();
                player.close();
                player = null;
            }
            if (vidc != null) {
                vidc = null;
            }
        } catch (Throwable t) {
            // nada
        }
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            stop();
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else {
            stop();
            midlet.setHome();
        }
    }

    public void play() {

        // load in video in mpg and get ready the player
        try {
            player = Manager.createPlayer(url);
            player.realize();
            player.prefetch();
            /*Calendar cal = Calendar.getInstance();
            cal.setTimeInMillis(player.getDuration());*/
            Date date = new Date(player.getDuration());
            duration = "" + date.toString();
            contentType = player.getContentType();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // get video control instance
        vidc = (VideoControl) player.getControl("VideoControl");
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
        if (f == null) {
            w = getWidth();
            h = getHeight();
            // Defeat Nokia bug ?
            if (w == 0) w = 240;
            if (h == 0) h = 320;
        }

        g.setColor(255, 255, 255);
        g.fillRect(0, 0, w, h);
        g.setColor(0, 0, 0);

        f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        fh = f.getHeight();
        g.setFont(f);

        g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);
        Font font = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(font);
        g.setColor(0);
        g.drawString("Video", (getWidth() - font.stringWidth("Video")) / 2, 2, Graphics.TOP | Graphics.LEFT);
        g.drawString(contentType, (getWidth() - font.stringWidth("Video")) / 2, 2 + fh, Graphics.TOP | Graphics.LEFT);
        g.drawString(duration, (getWidth() - font.stringWidth("Video")) / 2, 2 + 2*fh, Graphics.TOP | Graphics.LEFT);

    }
}
