package org.walkandplay.client.phone;

import org.geotracing.client.Util;
import org.geotracing.client.Net;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.Player;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.control.VideoControl;

import nl.justobjects.mjox.JXElement;

public class PhotoCamera {
    private Player player;
    private VideoControl videoControl;
    private byte[] data;
    private Image preview;
	private long time;
    private String name = "untitled";
    private static final String mime = "image/jpeg";

    public PhotoCamera(Player aPlayer, VideoControl aControl){
        player = aPlayer;
        videoControl = aControl;
    }

    public void show(int anXpos, int anYpos, int aDisplayHeight, int aDisplayWidth) {
        try {
            // create the player if it does not exist
            if (player == null) {
                player = Manager.createPlayer("capture://video");
                player.realize();
            }

            // create the video control if it does not exist
            if (videoControl == null) {
                videoControl = (VideoControl) player.getControl("VideoControl");
                videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
            }

            // place the camera screen
            videoControl.setDisplayLocation(anXpos, anYpos);
            videoControl.setDisplaySize(aDisplayWidth, aDisplayHeight);
            videoControl.setVisible(true);

            // show the player input
            player.start();

        } catch (Throwable ioe) {
            System.out.println("Exception initialising camera : " + ioe);
        }
    }

    private void close() {
        try {
            // close the player and videocontrol
            if (player != null) {
                player.stop();
                player.close();
                player = null;
            }

            if (videoControl != null) {
                videoControl = null;
            }
        } catch (MediaException me) {
            System.out.println("Exception closing the camera: " + me.toString());
        }
    }

    public void capture() {
        try {
            // create the captured image
            try {
				data = videoControl.getSnapshot("encoding=jpeg&width=320&height=240");
			} catch(Throwable t) {
				// Some phones don't support specific encodings
				// This should fix at least SonyEricsson K800i...
				data = videoControl.getSnapshot(null);
			}

			time = Util.getTime();

            preview = createPreview(Image.createImage(data, 0, data.length));

            // Shut down the player.
            close();

        } catch (Throwable me) {
            System.out.println("Exception trying to capture : " + me);
        }
    }

    public Image getPreview(){
        return preview;
    }

    // Scale down the image by skipping pixels
	public static Image createPreview(Image image) {
		int sw = image.getWidth();
		int sh = image.getHeight();

		int pw = 160;
		int ph = pw * sh / sw;

		Image temp = Image.createImage(pw, ph);
		Graphics g = temp.getGraphics();

		for (int y = 0; y < ph; y++) {
			for (int x = 0; x < pw; x++) {
				g.setClip(x, y, 1, 1);
				int dx = x * sw / pw;
				int dy = y * sh / ph;
				g.drawImage(image, x - dx, y - dy,
						Graphics.LEFT | Graphics.TOP);
			}
		}

		return Image.createImage(temp);
	}

    public boolean upload(){
        JXElement rsp = Net.getInstance().uploadMedium(name, "image", mime, time, data, false);
        if(rsp == null || rsp.getTag().indexOf("nrsp")!=-1){
            return false;
        }
        return true;
    }

}
