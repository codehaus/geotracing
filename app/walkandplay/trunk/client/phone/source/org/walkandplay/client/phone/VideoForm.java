package org.walkandplay.client.phone;

import org.walkandplay.client.phone.ProgressListener;

import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.MediaException;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.VideoControl;

public class VideoForm extends DefaultDisplay implements ProgressListener, PlayerListener {
    private Player player = null; // player instance
    VideoControl vidc = null;
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
        /*append("Downloading the video");*/

        //#style formbox
        /*append(progressBar);*/

        addCommand(HOME_CMD);

        //new VideoDownloader().download(this);
        play();
    }

    public void prStart() {
        progressBar.setMaxValue(progressMax);
    }

    public void prProgress(int anAmount) {
        progressBar = new Gauge(null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING);
    }

    public void prStop() {
        deleteAll();
        addCommand(BACK_CMD);
        progressBar = new Gauge("Download finished", false, 100, 100);
        play();
    }

    public void prError(String aMessage) {
        deleteAll();
        addCommand(BACK_CMD);
        append(progressBar);
        progressBar.setValue(0);

        //#style alertinfo
        append(aMessage);
    }

    public void prSetContentLength(int aContentLength) {
        progressBar.setLabel("Downloading " + aContentLength + " bytes");
    }

    public void playerUpdate(Player aPlayer, String anEvent, Object theDate) {
        //#style labelinfo
        append("player update:" + anEvent);
        
        if (anEvent.equals(PlayerListener.END_OF_MEDIA)) {
            try {
                defPlayer();
            }
            catch (Throwable t) {
            }
            //reset();
        }
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == HOME_CMD) {
            stop();
            midlet.setHome();
        } else if (cmd == BACK_CMD) {
            stop();
            midlet.setHome();
//            /Display.getDisplay(midlet).setCurrent(midlet.playDisplay);
        }
    }

    private class VideoDownloader {
        public int state = 0;

        private void download(ProgressListener aListener) {
            final ProgressListener listener = aListener;
            try {
                listener.prStart();
                listener.prProgress(-1);

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            player = Manager.createPlayer(url);
                            player.realize();
                            player.prefetch();
                            listener.prStop();

                        } catch (Throwable t) {
                            //listener.prError(t.getMessage());
                        }
                    }
                }).start();
            } catch (Throwable t) {
                listener.prError("Exception in Downloader:" + t.getMessage());
            }
        }
    }

    private void stop() {
        try {
            if (player != null) {
                player.stop();
                player = null;
            }
            if (vidc != null) {
                vidc = null;
            }
        } catch (Throwable t) {
            // nada
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

    public void play() {
        try {
            VideoControl vc;
            defPlayer();
            // create a player instance
            player = Manager.createPlayer(url);
            player.addPlayerListener(this);
            // realize the player
            //player.prefetch();
            player.realize();
            vc = (VideoControl) player.getControl("VideoControl");
            if (vc != null) {
                Item video = (Item) vc.initDisplayMode(vc.USE_GUI_PRIMITIVE, null);
                Form v = new Form("Playing Video...");
                StringItem si = new StringItem("Status: ","Playing...");
                v.append(si);
                v.append(video);
                Display.getDisplay(midlet).setCurrent(v);
            }
            player.prefetch();
            player.start();
        }
        catch (Throwable t) {

        }
        /* // get video control instance
        vidc = (VideoControl) player.getControl("VideoControl");
        //Item videoDisp = (Item)vidc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, null);
        Item videoDisp = (Item) vidc.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
        deleteAll();
        addCommand(BACK_CMD);
        addCommand(HOME_CMD);

        //#style labelinfo
        append("Playing the video");

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
        }*/
    }
}
