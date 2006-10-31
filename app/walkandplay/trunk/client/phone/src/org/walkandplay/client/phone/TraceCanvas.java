package org.walkandplay.client.phone;

import org.geotracing.client.GPSInfo;
import org.geotracing.client.Log;
import org.geotracing.client.Util;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import nl.justobjects.mjox.JXElement;

public class TraceCanvas extends DefaultCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    private WP midlet;
    private String tileURL;
    private int zoom = 9;
    private String activeTile;
    private Image image;
    private String myX, myY;
    private String mapType = "map";
    private String[] msgs = new String[3];

    String gpsStatus = "disconnected";
    String netStatus = "disconnected";
    String status = "OK";
    boolean showMenu;

    private Tracer tracer;

    // image objects
    private Image msgBar;

    int margin = 3;

    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int ASSIGNMENT_STAT = 1;
    private final static int DROP_STAT = 2;

    private int fontType = Font.FACE_MONOSPACE;

    public TraceCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            midlet = aMidlet;
            w = getWidth();
            h = getHeight();
            setFullScreenMode(true);

            tileURL = midlet.getAppProperty("kw-url") + "/tile.jsp?";
            System.out.println("tileUrl:" + tileURL);
            // TODO: get this check out; just for testing purposes now... 
            if (midlet.GPS_OK()) {
                tracer = new Tracer(aMidlet, this);
                tracer.start();
                System.out.println("created the tracer object");
            }
            // load all images
            msgBar = Image.createImage("/msg_bar.png");

        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }

    }

    void start() {
        tracer.start();
    }

    Tracer getTracer() {
        return tracer;
    }

    void setLocation(String aLon, String aLat) {
        if (aLon.equals(("0")) || aLat.equals("0")) {
            msgs[2] = "No Location";
            return;
        }
        activeTile = tileURL + "lon=" + aLon + "&lat=" + aLat;
    }

    public void setGPSInfo(GPSInfo theInfo) {
        setLocation(theInfo.lon.toString(), theInfo.lat.toString());
        status = theInfo.toString();
        repaint();
    }

    public void setStatus(String s) {
        status = s;
        Log.log(s);
        repaint();
    }

    public void onGPSStatus(String s) {
        gpsStatus = s;
        if (gpsStatus.equals("connected")) {
            midlet.setGPSConnectionStat(true);
        } else {
            midlet.setGPSConnectionStat(false);
        }
        Log.log(s);
        repaint();
    }


    public void onNetStatus(String s) {
        netStatus = s;
        if (netStatus.equals("heartbeat ok")) {
            midlet.setNetConnectionStat(true);
        } else {
            midlet.setNetConnectionStat(false);
        }
        log(s);
        repaint();
    }

    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        g.setColor(0, 0, 0);
        f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(f);
        fh = f.getHeight();

        String text = "";

        if (activeTile == null) {
            text = "No location yet...";
        } else {

            try {
                //msg = "Fetching map image...";
                log("getting the map tile images!!!");
                JXElement el = Util.getXML(activeTile + "&format=xml");
                if(el!=null){
                    image = Util.getImage(el.getAttr("url"));
                    myX = el.getAttr("x");
                    myY = el.getAttr("y");

                }
            } catch (Throwable t) {
                text = "Error fetching image !!";
                text += "maybe this zoom-level is not available";
                text += "try zooming further in or out";
                Log.log("error: MapScreen: t=" + t + " m=" + t.getMessage());
            }
        }

        // draw the google map image
        if (image != null) {
            g.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);
            g.drawImage(redDot, Integer.parseInt(myX), Integer.parseInt(myY), Graphics.TOP | Graphics.LEFT);
        }

        ScreenUtil.drawLeftSoftKey(g, h, menuBt);

        if (text.length() > 0) {
            ScreenUtil.drawTextArea(g, 100, margin, margin + logo.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
            ScreenUtil.drawText(g, text, 10, logo.getHeight() + 3 * margin, fh, 100);
        }

        switch (screenStat) {
            case HOME_STAT:
                if (showMenu) {
                    String[] options = {"switch map", "zoom out", "zoom in", "drop media"};
                    ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                }
                break;
            case ASSIGNMENT_STAT:
                if (showMenu) {
                    String[] options = {"answer"};
                    ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                }
                break;
            case DROP_STAT:
                if (showMenu) {
                    String[] options = {"drop message", "drop image", "drop video", "drop audio"};
                    ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                }
                break;
        }

        // if there's a status show it in the status bar
        if(netStatus.length()>0 || gpsStatus.length()>0 || status.length()>0){
            if(netStatus.length()>0){
                msgs[0] = "Net:" + netStatus;
            }
            if(gpsStatus.length()>0){
                msgs[1] = "GPS:" + gpsStatus;
            }
            if(status.length()>0){
                msgs[2] = status;
            }

            ScreenUtil.drawMessageBar(g, fh, msgs, msgBar, h);
        }
        ScreenUtil.drawRightSoftKey(g, h, w, backBt);

        if(msgs[0].length()>0 || msgs[1].length()>0 || msgs[2].length()>0){
            msgs[0] = "";
            msgs[1] = "";
            msgs[2] = "";
            new Delayer(2);
        }
    }

    /**
     * Handles all key actions.
     *
     * @param key The Key that was hit.
     */
    public void keyPressed(int key) {
        // left soft key & fire
        if (key == -6 || key == -5 || getGameAction(key) == Canvas.FIRE) {
            switch (screenStat) {
                case HOME_STAT:
                    if (showMenu) {
                        if (ScreenUtil.getSelectedMenuItem() == 1) {
                            mapType = mapType.equals("sat") ? "map" : "sat";
                        } else if (ScreenUtil.getSelectedMenuItem() == 2) {
                            zoom++;
                        } else if (ScreenUtil.getSelectedMenuItem() == 3) {
                            zoom--;
                        } else if (ScreenUtil.getSelectedMenuItem() == 4) {
                            midlet.setScreen(WP.MEDIA_CANVAS);
                            showMenu = false;
                        }
                    } else {
                        showMenu = true;
                    }
                    break;
            }
            // right softkey
        } else if (key == -7) {
            switch (screenStat) {
                case HOME_STAT:
                    midlet.setScreen(WP.HOME_CANVAS);
                    break;
            }
            // left
        } else if (key == -3 || getGameAction(key) == Canvas.LEFT) {
            switch (screenStat) {
                case HOME_STAT:
                    break;
            }
            // right
        } else if (key == -4 || getGameAction(key) == Canvas.RIGHT) {
            switch (screenStat) {
                case HOME_STAT:
                    break;
            }
            // up
        } else if (key == -1 || getGameAction(key) == Canvas.UP) {
            // down
            if (showMenu) {
                ScreenUtil.selectNextMenuItem();
            }
        } else if (key == -2 || getGameAction(key) == Canvas.DOWN) {
            if (showMenu) {
                ScreenUtil.selectPrevMenuItem();
            }
        } else if (getGameAction(key) == Canvas.KEY_STAR || key == Canvas.KEY_STAR) {

        } else if (getGameAction(key) == Canvas.KEY_POUND || key == Canvas.KEY_POUND) {
            midlet.setScreen(-1);
        } else if (key == -8) {

        } else {

        }

        repaint();
    }

}
