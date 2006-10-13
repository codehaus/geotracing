package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;


public class MapScreen extends Form implements CommandListener {

    private String tileURL;
    private int zoom = 9;
    private String activeTile;
    private Command zoomIn;
    private Command zoomOut;
    private Command back;
    private Command toggleMapType;
    private MIDlet midlet;
    private Image image;
    private String mapType = "map";
    private Displayable prevScreen;

    public MapScreen(MIDlet aMIDlet) {
        super("MapScreen");
        midlet = aMIDlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        zoomIn = new Command("Zoom In", Command.OK, 1);
        zoomOut = new Command("Zoom Out", Command.OK, 1);
        back = new Command("Back", Command.OK, 1);
        toggleMapType = new Command("Toggle Map Type", Command.OK, 1);
        addCommand(zoomIn);
        addCommand(zoomOut);
        addCommand(toggleMapType);
        addCommand(back);
        setCommandListener(this);
        tileURL = midlet.getAppProperty("kw-url") + "/tile.jsp?";

    }

    void setLocation(String aLon, String aLat) {
        if (aLon.equals(("0")) || aLat.equals("0")) {
            deleteAll();
            append("No Location");
            return;
        }
        activeTile = tileURL + "lon=" + aLon + "&lat=" + aLat;
    }

    public void show() {
        if (activeTile == null) {
            append("No location yet...");
            return;
        }

        if (image != null) {
            deleteAll();
        }

        try {
            append("Fetching map image...");
            image = Util.getImage(activeTile + "&zoom=" + zoom + "&type=" + mapType);
        } catch (Throwable t) {
            append("Error fetching image !!");
            append("maybe this zoom-level is not available");
            append("try zooming further in or out");
            Log.log("error: MapScreen: t=" + t + " m=" + t.getMessage());
            return;
        }
        deleteAll();
        append(image);
    }

    public void commandAction(Command c, Displayable d) {
        if (c == back) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (c == zoomIn) {
            zoom--;
            show();
        } else if (c == zoomOut) {
            zoom++;
            show();
        } else if (c == toggleMapType) {
            mapType = mapType.equals("sat") ? "map" : "sat";
            show();
        }
    }
}