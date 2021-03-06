package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;
import org.geotracing.client.GPSFetcher;
import org.geotracing.client.Util;

import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.GUIControl;
import javax.microedition.media.control.VideoControl;

/**
 * Capture image from phone camera.
 *
 * @author Just van den Broecke
 * @version $Id: ImageCapture.java 254 2007-01-11 17:13:03Z just $
 */
public class ImageCaptureDisplayOld extends DefaultDisplay implements TCPClientListener {

    private Command CAPTURE_CMD = new Command("Capture", Command.OK, 1);

    private Player player = null;
    private VideoControl video = null;
    private Image photoPreview;
    private byte[] photoData;
    private String photoMime;
    private long photoTime;
    private Displayable prevScreen;
    private StringItem status = new StringItem("", "Photo Capture");
    private boolean playing;
    private StringItem alertField = new StringItem("", "");

    public ImageCaptureDisplayOld(WPMidlet aMIDlet, Displayable aPrevScreen, boolean isPlaying) {
        super(aMIDlet, "Take a picture");
        prevScreen = aPrevScreen;
        playing = isPlaying;
        midlet.getActiveApp().addTCPClientListener(this);

        showCamera();

        addCommand(CAPTURE_CMD);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == CAPTURE_CMD) {
            capture();
            Display.getDisplay(midlet).setCurrent(new PhotoPreview());
        } else if (c == BACK_CMD) {
            if(player!=null){
                try{
                    player.stop();
                    player.deallocate();
                    player.stop();
                    player = null;
                }catch(Throwable t){
                    //
                }
            }
            back();
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        String tag = aResponse.getTag();
        if (tag.equals("utopia-rsp")) {
            deleteAll();
            addCommand(BACK_CMD);
            //#style alertinfo
            append(alertField);
            JXElement rsp = aResponse.getChildAt(0);
            if (rsp.getTag().equals("play-add-medium-rsp") || rsp.getTag().equals("game-add-medium-rsp")) {
                alertField.setText("Image sent successfully");
            } else if (rsp.getTag().equals("play-add-medium-nrsp")) {
                alertField.setText("Error sending Image - please try again.");
            }
        }
    }

    public void onNetStatus(String aStatus){

    }

    public void onConnected(){

    }

    public void onError(String anErrorMessage){
        //#style alertinfo
        append(anErrorMessage);
    }

    public void onFatal(){
        midlet.getActiveApp().exit();
        Display.getDisplay(midlet).setCurrent(midlet.getActiveApp());
    }

    private void back() {
        midlet.getActiveApp().removeTCPClientListener(this);
        Display.getDisplay(midlet).setCurrent(prevScreen);
    }

    private void showCamera() {
        try {
            player = Manager.createPlayer("capture://video");
            player.realize();

            // Add the video playback window (item)
            video = (VideoControl) player.getControl("VideoControl");
            Item item = (Item) video.initDisplayMode(GUIControl.USE_GUI_PRIMITIVE, null);
            item.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_NEWLINE_AFTER);
            append(item);
            // Add a caption
            status.setText("Press Fire to take photo");
            status.setLayout(Item.LAYOUT_CENTER);
            //#style labelinfo
            append(status);

            player.start();

        } catch (Throwable e) {
            Util.showAlert(midlet, "cannot start camera", e.getMessage());
            back();
        }
    }

    private void capture() {
        try {
            status.setText("WAIT, taking photo...");
            photoTime = Util.getTime();

            try {
                photoData = video.getSnapshot("encoding=jpeg&width=320&height=240");
            } catch (Throwable t) {
                // Some phones don't support specific encodings
                // This should fix at least SonyEricsson K800i...
                photoData = video.getSnapshot(null);
            }

            photoMime = "image/jpeg";

            player.stop();
            player.close();

            photoPreview = createPreview(Image.createImage(photoData, 0, photoData.length));
            status.setText("OK done...");

        } catch (Throwable e) {
            Util.showAlert(midlet, "CAPTURE_CMD error", e.getMessage());
            back();
        }
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

    private class PhotoPreview extends Form implements CommandListener {

        private Command CANCEL_CMD = new Command("Back", Command.CANCEL, 1);
        private Command SUBMIT_CMD = new Command("Submit", Command.OK, 1);
        private TextField name = new TextField("Photo Name (below)", null, 24, TextField.ANY);

        public PhotoPreview() {
            super("Photo Preview");
            addCommand(CANCEL_CMD);
            addCommand(SUBMIT_CMD);
            setCommandListener(this);

            append(new ImageItem("", photoPreview, ImageItem.LAYOUT_CENTER, "image"));
            append(name);
            append("press Submit to send or Back to cancel");
        }

        public void commandAction(Command c, Displayable d) {
            if (c == CANCEL_CMD) {
                photoData = null;
                photoPreview = null;
                back();

            } else if (c == SUBMIT_CMD) {
                deleteAll();
                append("SENDING PHOTO...(takes a while)");
                Uploader uploader = new Uploader();
                //JXElement rsp = Net.getInstance().addMedium(name.getString(), "image", photoMime, photoTime, photoData, null);
                JXElement rsp = uploader.uploadMedium(TCPClient.getInstance().getAgentKey(), midlet.getKWUrl(), name.getString(), null, "image", photoMime, photoTime, photoData, false);
                JXElement addMediumReq;
                if (playing) {
                    addMediumReq = new JXElement("play-add-medium-req");
                    addMediumReq.setAttr("id", rsp.getAttr("id"));

                } else {
                    addMediumReq = new JXElement("game-add-medium-req");
                    addMediumReq.setAttr("id", midlet.getCreateApp().getGameId());
                    JXElement medium = new JXElement("medium");
                    addMediumReq.addChild(medium);

                    JXElement id = new JXElement("id");
                    id.setText(rsp.getAttr("id"));
                    medium.addChild(id);

                    JXElement lat = new JXElement("lat");
                    lat.setText("" + GPSFetcher.getInstance().getCurrentLocation().lat);
                    medium.addChild(lat);

                    JXElement lon = new JXElement("lon");
                    lon.setText("" + GPSFetcher.getInstance().getCurrentLocation().lon);
                    medium.addChild(lon);
                }
                midlet.getActiveApp().sendRequest(addMediumReq);
            }
        }
    }
}
