package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;
import java.util.Date;

public class VideoDisplay extends GameCanvas implements CommandListener, PlayerListener {
    private Player player = null; // player instance
    VideoControl vidc = null;
    private WPMidlet midlet;
    private String name;
    protected Displayable prevScreen;
    private String url;
    private Image bg;
    private String title;
    private VideoDisplay instance;

    Font f;
    int fh, w, h;

    private Command BACK_CMD = new Command("Back", Command.ITEM, 2);
    private Command REPLAY_CMD = new Command("Replay", Command.ITEM, 2);

    public VideoDisplay(WPMidlet aMidlet, String aName, String aUrl, Displayable aPrevScreen) {
        super(false);
        midlet = aMidlet;
        url = aUrl;
        name = aName;
        instance = this;
        prevScreen = aPrevScreen;
        title = "Video:" + name;
        setFullScreenMode(true);
        Display.getDisplay(midlet).setCurrent(this);

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
        setCommandListener(this);
        play();
    }

    public void playerUpdate(Player aPlayer, String anEvent, Object theDate) {
        title = "Video:" + name + " [" + anEvent + "]";
        repaint();
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
        } else if (cmd == REPLAY_CMD) {
            try{
                player.start();
            }catch(Throwable t){
                // nada
            }
        }
    }

    public void play() {
        try{
            player = Manager.createPlayer(url);
            player.realize();
            player.addPlayerListener(this);
        }catch(Throwable t){
            Log.log(t.getMessage());
            title = "Error:" + t.getMessage();
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    player.prefetch();
                    title = "Video:" + name +"(" + (new Date(player.getDuration())).toString() + ")";
                    showPlayer();
                }catch(Throwable t){
                    Log.log(t.getMessage());
                    title = "Error:" + t.getMessage();                    
                }
            }
        }).start();
    }

    private void showPlayer(){
        try{
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
            player.start();

            addCommand(REPLAY_CMD);
        }catch (Throwable t){
            Log.log(t.getMessage());
            title = "Error:" + t.getMessage();
            repaint();
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
        g.drawString(title, (getWidth() - font.stringWidth(title)) / 2, 2, Graphics.TOP | Graphics.LEFT);        
    }
}
