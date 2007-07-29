package org.walkandplay.client.phone;

import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.lcdui.*;
import javax.microedition.media.Player;
import javax.microedition.media.Manager;
import javax.microedition.media.control.VideoControl;
import javax.microedition.io.HttpConnection;
import javax.microedition.io.Connector;
import java.io.DataInputStream;

public class VideoForm extends DefaultDisplay implements DownloadListener{
    private Player player = null; // player instance
    private String url;
    private Gauge progressBar = new Gauge("Download Progress", false, 100, 0);
    private int progressCounter;
    private int progressMax = 100;

    private Command BACK_CMD = new Command("Back", Command.ITEM, 2);
    private Command HOME_CMD = new Command("Home", Command.ITEM, 2);

    public VideoForm(WPMidlet aMidlet, String aUrl) {
        super(aMidlet, "Video");
        url = aUrl;

        //#style labelinfo
        append("Downloading the video");

        //#style formbox
        append(progressBar);

        addCommand(BACK_CMD);
        addCommand(HOME_CMD);
        setCommandListener(this);

        new VideoDownloader().download(this);
    }

    public void dlStart() {
        progressBar.setMaxValue(progressMax);
    }

    public void dlProgress() {
        progressBar.setValue(progressCounter);
        if (progressCounter == progressMax - 1) {
            progressCounter = 0;
        }
        progressCounter++;
    }

    public void dlStop() {
        /*progressBar.setLabel("Download finished!");*/
        play();
    }

    public void dlError(String aMessage) {
        //#style alertinfo
        append(aMessage);
    }

    public void dlSetContentLength(int aContentLength) {
        progressBar.setLabel("Downloading " + aContentLength + " bytes");
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == HOME_CMD) {
            midlet.setHome();
        } else if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(midlet.playDisplay);
        }
    }

    private class VideoDownloader {
        public int state = 0;

        private void download(DownloadListener aListener) {
            final DownloadListener listener = aListener;
            try {
                new Thread(new Runnable() {
                    public void run() {
                        try {
                            listener.dlStart();
                            listener.dlProgress();

                            player = Manager.createPlayer(url);
                            player.realize();
                            player.prefetch();

                            listener.dlStop();
                        } catch (Throwable t) {
                            listener.dlError(t.getMessage());
                        }
                    }
                }).start();
            } catch (Throwable t) {
                listener.dlError("Exception in Downloader:" + t.getMessage());
            }
        }
    }

    public void play() {
        // get video control instance
        VideoControl vidc = (VideoControl) player.getControl("VideoControl");
        //Item videoDisp = (Item)vidc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, null);
        Item videoDisp = (Item)vidc.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
        deleteAll();
        addCommand(BACK_CMD);
        addCommand(HOME_CMD);
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
