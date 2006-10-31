package org.walkandplay.client.phone;

import org.geotracing.client.GPSInfo;
import org.geotracing.client.Log;
import org.geotracing.client.Util;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import nl.justobjects.mjox.JXElement;

public class TraceCanvas extends DefaultCanvas{

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    private String inputText = "";

    private WP midlet;
    private String tileURL;
    private int zoom = 9;
    private String activeTile;
    private Image image;
    private String myX, myY;
    private String mapType = "map";
    private String lon="0", lat="0";
    private String msg = "";
    private String[] statMsgs = new String[3];

    String gpsStatus = "disconnected";
    String netStatus = "disconnected";
    String status = "OK";
    boolean showMenu;

    private Tracer tracer;
    private Texter texter;

    // image objects
    private Image msgBar, inputBox, okBt;

    int margin = 3;

    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int ASSIGNMENT_STAT = 1;
    private final static int TRACK_STAT = 2;
    private final static int STATUS_STAT = 3;

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
            inputBox = Image.createImage("/inputbox.png");
            okBt = Image.createImage("/ok_button.png");

            texter = new Texter(this);
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
            statMsgs[2] = "No Location";
            return;
        }
        lon = aLon;
        lat = aLat;
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

    public boolean hasLocation() {
		return !lon.equals(("0")) && !lat.equals("0");
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

        switch (screenStat) {
            case HOME_STAT:
               // draw the google map image
                if (activeTile == null) {
                    msg = "No location yet...";
                } else {
                    try {
                        //msg = "Fetching map image...";
                        log("getting the map tile images!!!");
                        activeTile = tileURL + "&lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType;
                        JXElement el = Util.getXML(activeTile + "&format=xml");
                        if(el!=null){
                            image = Util.getImage(el.getAttr("url"));
                            myX = el.getAttr("x");
                            myY = el.getAttr("y");
                        }
                    } catch (Throwable t) {
                        msg = "Error fetching image !!";
                        msg += "maybe this zoom-level is not available";
                        msg += "try zooming further in or out";
                        Log.log("error: MapScreen: t=" + t + " m=" + t.getMessage());
                    }
                }

                if (image != null) {
                    g.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);
                    g.drawImage(redDot, Integer.parseInt(myX), Integer.parseInt(myY), Graphics.TOP | Graphics.LEFT);
                }else{
                    if (msg.length() > 0) {
                        ScreenUtil.drawTextArea(g, 100, margin, margin + logo.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
                        ScreenUtil.drawText(g, msg, 10, logo.getHeight() + 3 * margin, fh, 100);
                    }
                }

                if (showMenu) {
                    if(tracer!=null && tracer.isPaused()){
                        String[] options = {"new track", "resume track", "switch map", "zoom out", "zoom in", "drop media", "status"};
                        ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                    }else {
                        String[] options = {"new track", "suspend track", "stop track", "switch map", "zoom out", "zoom in", "drop media", "status"};
                        ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                    }
                }
                ScreenUtil.drawLeftSoftKey(g, h, menuBt);
                break;
            case ASSIGNMENT_STAT:
                if (showMenu) {
                    String[] options = {"answer"};
                    ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                }
                break;
            case TRACK_STAT:
                g.drawString("title", 2 * margin, 4 * margin + logo.getHeight(), Graphics.TOP | Graphics.LEFT);
                g.drawImage(inputBox, 2 * margin, 5 * margin + logo.getHeight() + fh, Graphics.TOP | Graphics.LEFT);
                g.drawString(inputText, 2 * margin, 5 * margin + logo.getHeight() + fh + 2, Graphics.TOP | Graphics.LEFT);
                g.drawString(texter.getSelectedKey(), 2 * margin, 6 * margin + logo.getHeight() + 2 * fh + 2, Graphics.TOP | Graphics.LEFT);
                ScreenUtil.drawLeftSoftKey(g, h, okBt);
                break;
            case STATUS_STAT:
                if (showMenu) {
                    if(tracer!=null && tracer.isPaused()){
                        String[] options = {"new track", "resume track", "switch map", "zoom out", "zoom in", "drop media", "status"};
                        ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                    }else {
                        String[] options = {"new track", "suspend track", "stop track", "switch map", "zoom out", "zoom in", "drop media", "status"};
                        ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                    }
                }
                // if there's a status show it in the status bar
                if(netStatus.length()>0 || gpsStatus.length()>0 || status.length()>0){
                    if(netStatus.length()>0 && statMsgs[0]!=null && !statMsgs[0].equals(netStatus)){
                        statMsgs[0] = "Net:" + netStatus;
                    }else{
                        statMsgs[0] = "";
                    }
                    if(gpsStatus.length()>0 && statMsgs[1]!=null && !statMsgs[1].equals(gpsStatus)){
                        statMsgs[1] = "GPS:" + gpsStatus;
                    }else{
                        statMsgs[1] = "";
                    }
                    if(status.length()>0  && statMsgs[2]!=null && !statMsgs[2].equals(status)){
                        statMsgs[2] = status;
                    }else{
                        statMsgs[2] = "";
                    }
                    ScreenUtil.drawMessageBar(g, fh, statMsgs, msgBar, h);
                }
                break;
        }

        ScreenUtil.drawRightSoftKey(g, h, w, backBt);

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
                        if(tracer!=null && tracer.isPaused()){
                            if (ScreenUtil.getSelectedMenuItem() == 1) {
                                screenStat = TRACK_STAT;
                                showMenu = false;
                            } else if (ScreenUtil.getSelectedMenuItem() == 2) {
                                tracer.resume();
                                msg += "Track resumed";
                            } else if (ScreenUtil.getSelectedMenuItem() == 3) {
                                mapType = mapType.equals("sat") ? "map" : "sat";
                            } else if (ScreenUtil.getSelectedMenuItem() == 4) {
                                zoom++;
                            } else if (ScreenUtil.getSelectedMenuItem() == 5) {
                                zoom--;
                            } else if (ScreenUtil.getSelectedMenuItem() == 6) {
                                midlet.setScreen(WP.MEDIA_CANVAS);
                                showMenu = false;
                            } else if (ScreenUtil.getSelectedMenuItem() == 7) {
                                screenStat = STATUS_STAT;
                                showMenu = false;
                            }else{
                                showMenu = false;
                            }
                        }else{
                            if (ScreenUtil.getSelectedMenuItem() == 1) {
                                screenStat = TRACK_STAT;
                                showMenu = false;
                            } else if (ScreenUtil.getSelectedMenuItem() == 2) {
                                tracer.suspend();
                                msg += "Track suspended";
                            } else if (ScreenUtil.getSelectedMenuItem() == 3) {
                                tracer.stop();
                                msg += "Track stopped";
                            } else if (ScreenUtil.getSelectedMenuItem() == 4) {
                                mapType = mapType.equals("sat") ? "map" : "sat";
                            } else if (ScreenUtil.getSelectedMenuItem() == 5) {
                                zoom++;
                            } else if (ScreenUtil.getSelectedMenuItem() == 6) {
                                zoom--;
                            } else if (ScreenUtil.getSelectedMenuItem() == 7) {
                                midlet.setScreen(WP.MEDIA_CANVAS);
                                showMenu = false;
                            } else if (ScreenUtil.getSelectedMenuItem() == 8) {
                                screenStat = STATUS_STAT;
                                showMenu = false;
                            }else{
                                showMenu = false;
                            }
                        }

                    } else {
                        showMenu = true;
                    }
                    break;
                case TRACK_STAT:
                    if(tracer!=null){
                        tracer.suspend();
                        tracer.getNet().newTrack(inputText);
                        msg += "New track created";
                    }else{
                        msg += "Could not create new track";
                    }
                    screenStat = HOME_STAT;
                    showMenu = false;
                    break;
            }
            // right softkey
        } else if (key == -7) {
            switch (screenStat) {
                case HOME_STAT:
                    if(showMenu) {
                        showMenu = false;
                        screenStat = HOME_STAT;
                    }else{
                        midlet.setScreen(WP.HOME_CANVAS);                        
                    }
                    break;
                case STATUS_STAT:
                    showMenu = false;
                    screenStat = HOME_STAT;
                    break;
                case ASSIGNMENT_STAT:
                    showMenu = false;
                    screenStat = HOME_STAT;
                    break;
                case TRACK_STAT:
                    showMenu = false;
                    screenStat = HOME_STAT;
                    break;
            }
            // left
        } else if (key == -3 || getGameAction(key) == Canvas.LEFT) {
            // right
        } else if (key == -4 || getGameAction(key) == Canvas.RIGHT) {
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
            /*inputText = inputText.substring(0, inputText.length() - 1);*/
            inputText = texter.deleteChar();
        } else {
            //inputText = texter.write(key);
        }

        repaint();
    }

}
