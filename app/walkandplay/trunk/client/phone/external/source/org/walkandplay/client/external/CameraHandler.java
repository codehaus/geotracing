package org.walkandplay.client.external;

import javax.microedition.lcdui.*;
import javax.microedition.media.Player;
import javax.microedition.media.MediaException;
import javax.microedition.media.Manager;
import javax.microedition.media.control.VideoControl;
import java.io.IOException;

/**
 * The only class that provides API to be used from outside in this camera package.
 * Handle and manage the whole package.
 *
 * @author Song Yuan
 */

public class CameraHandler {

    private static Image photo = null;
    private static byte[] photoBytes = null;

    private static Display display = null;
    private static CameraForm cameraForm = null;
    private static PhotoForm photoForm = null;
    private static Player player = null;
    private static VideoControl videoControl = null;

    private static CameraListener listener;

    /**
     * Indicate whether the user already takes the snapshot or not.
     * True if yes, otherwise false.
     */
    public static boolean isFinished = false;


    public static void addListener(CameraListener aListener){
        listener = aListener;
    }

    /**
     * Start the camera utility.
     * The realtime video will be shown.
     *
     * @param disp The Display Object of the current midlet
     */
    public static void takeSnapshot(Display disp) throws IOException, MediaException {
        display = disp;
        showVideo();
    }

    /**
     * End using the camera utility and release all resources.
     */
    public static void end() {
        isFinished = false;
        display = null;
        cameraForm = null;
        photoForm = null;
        videoControl = null;
        if (player != null) {
            player.close();
            player = null;
        }
    }

    public static void finish(){
        listener.onFinish();
    }

    public static void cancel(){
        listener.onCancel();
    }

    /**
     * Get the taken photo as Image object.
     *
     * @return the taken photo in the form of Image object
     */
    public static Image getPhoto() {
        return photo;
    }

    /**
     * Get the taken photo as byte array.
     *
     * @return the taken photo in the form of byte array
     */
    public static byte[] getPhotoBytes() {
        return photoBytes;
    }

    /**
     * Set both Image object and the byte array that represent the photo as null
     */
    public static void setPhotoNull() {
        photo = null;
        photoBytes = null;
    }

    /**
     * Get the current Display object.
     *
     * @return the current Display object
     */
    protected static Display getDisplay() {
        return display;
    }

    /**
     * Show the realtime video and wait for the user to take a snapshot.
     */
    protected static void showVideo() throws IOException, MediaException {

        //initialize the player
        if (player == null) {
            player = Manager.createPlayer("capture://video");
            player.realize();
        }

        //initialize the VideoControl
        if (videoControl == null) {
            videoControl = (VideoControl) player.getControl("VideoControl");
        }

        //start the video and display it
        if (player != null && videoControl != null) {
            if (cameraForm == null) {
                cameraForm = new CameraForm("Capture a Photo");
                Item item = (Item) videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, null);
                cameraForm.append(item);
                player.start();
            }
            display.setCurrent(cameraForm);
        } else {
            System.out.println("can't establish player or videoControl");
            photoForm.append("can't establish player or videoControl");
            photoForm.append("can't establish player or videoControl");
        }
    }

    /**
     * Confirm to take the snapshot and save it.
     */
    protected static void capturePhoto() {

        photoForm = new PhotoForm("Confirm Photo");

        //take a snapshot, use the default image format of the specific phone
        try {
            photoBytes = videoControl.getSnapshot("encoding=jpeg&width=320&height=240");
            //photoBytes = videoControl.getSnapshot(null);

            //create the image and append it to the form
            if (photoBytes != null) {
                photo = Image.createImage(photoBytes, 0, photoBytes.length);
                Image preview = createPreview(photo);
                photoForm.append(preview);
            } else {
                StringItem warnMsg = new StringItem("Camera is currently unavailable. ",
                        "Check your camera and try later.");
                photoForm.append(warnMsg);
            }
        } catch (MediaException me) {
            StringItem warnMsg = new StringItem("Camera is currently unavailable. ",
                    "Check your camera and try later.");
            photoForm.append(warnMsg);
        }

        //display the form
        display.setCurrent(photoForm);
    }

    public static Image createPreview(Image image) {
		int sw = image.getWidth();
		int sh = image.getHeight();

		int pw = 240;
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

}
