package org.walkandplay.client.phone;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

public class VideoForm extends DefaultDisplay implements DownloadListener {
    private Player player = null; // player instance
    private String url;
    private Gauge progressBar = new Gauge("Download Progress", false, 100, 0);
    private int progressCounter;
    private int progressMax = 100;
    private boolean run = true;


    private Command HOME_CMD = new Command("Home", Command.ITEM, 2);

    public VideoForm(WPMidlet aMidlet, String aUrl) {
        super(aMidlet, "Video");
        url = aUrl;

        //#style labelinfo
        append("Downloading the video");

        //#style formbox
        append(progressBar);

        addCommand(HOME_CMD);

        new VideoDownloader().download(this);
    }

    public void dlStart() {
        progressBar.setMaxValue(progressMax);
    }

    public void dlProgress() {
        progressBar = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
    }

    public void dlStop() {
        deleteAll();
        addCommand(BACK_CMD);
        progressBar = new Gauge("Download finished", false, 100, 100);
        play();
    }

    public void dlError(String aMessage) {
        deleteAll();
        addCommand(BACK_CMD);
        append(progressBar);
        progressBar.setValue(0);

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
            midlet.setHome();
//            /Display.getDisplay(midlet).setCurrent(midlet.playDisplay);
        }
    }

    private class VideoDownloader {
        public int state = 0;

        private void download(DownloadListener aListener) {
            final DownloadListener listener = aListener;
            try {
                listener.dlStart();
                listener.dlProgress();

                new Thread(new Runnable() {
                    public void run() {
                        try {
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
        Item videoDisp = (Item) vidc.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
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
