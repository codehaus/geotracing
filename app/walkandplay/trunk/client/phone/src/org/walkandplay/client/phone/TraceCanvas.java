package org.walkandplay.client.phone;

import org.geotracing.client.Util;
import org.geotracing.client.GPSInfo;
import org.geotracing.client.Log;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

public class TraceCanvas extends Canvas {

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
    private String mapType = "map";
    private String msg;

    String gpsStatus = "disconnected";
    String netStatus = "disconnected";
    String status = "OK";
    boolean showMenu;

    private Tracer tracer;

    // image objects
    private Image logo, menuBt, textArea, backBt, traceLogo, gpsNetBar, redDot, greenDot, menuTop, menuMiddle, menuBottom;

    int margin = 3;

    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int ASSIGNMENT_STAT = 1;
    private final static int DROP_STAT = 2;

    private int fontType = Font.FACE_MONOSPACE;

    public TraceCanvas(WP aMidlet) {
        //super(false);
        try {
            midlet = aMidlet;
            w = getWidth();
            h = getHeight();
            setFullScreenMode(true);

            tileURL = midlet.getAppProperty("kw-url") + "/tile.jsp?";
            System.out.println("tileUrl:" + tileURL);
            // TODO: get this check out. 
            if(midlet.GPS_OK()){
                tracer = new Tracer(aMidlet, this);
                tracer.start();
                System.out.println("created the tracer object");
            }
            // load all images
            logo = Image.createImage("/logo.png");
            menuBt = Image.createImage("/menu_button.png");
            textArea = Image.createImage("/text_area.png");
            backBt = Image.createImage("/back_button.png");
            traceLogo = Image.createImage("/trace_icon_small.png");
            gpsNetBar = Image.createImage("/gpsnet_bg.png");
            redDot = Image.createImage("/red_dot.png");
            greenDot = Image.createImage("/green_dot.png");
            menuTop = Image.createImage("/menu_top.png");
            menuMiddle = Image.createImage("/menu_middle.png");
            menuBottom = Image.createImage("/menu_bottom.png");

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

    // passes log msg to the main log method
    private void log(String aMsg) {
        midlet.log(aMsg);
    }

    void setLocation(String aLon, String aLat) {
        if (aLon.equals(("0")) || aLat.equals("0")) {
            msg = "No Location";
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
        if(gpsStatus.equals("connected")){
            midlet.setGPSConnectionStat(true);
        }else {
            midlet.setGPSConnectionStat(false);
        }
        Log.log(s);
        repaint();
    }


    public void onNetStatus(String s) {
        netStatus = s;
        if(netStatus.equals("heartbeat ok")){
            midlet.setNetConnectionStat(true);
        }else {
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
        if (f == null) {
            g.setColor(0, 0, 0);
            f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(f);
            fh = f.getHeight();
        }

        g.drawImage(gpsNetBar, margin + logo.getWidth() + margin, margin, Graphics.TOP | Graphics.LEFT);
        if (midlet.GPS_OK()) {
            g.drawImage(greenDot, margin + logo.getWidth() + margin + 4, 10, Graphics.TOP | Graphics.LEFT);
        } else {
            g.drawImage(redDot, margin + logo.getWidth() + margin + 4, 10, Graphics.TOP | Graphics.LEFT);
        }
        if (midlet.NET_OK()) {
            g.drawImage(greenDot, margin + logo.getWidth() + margin + 41, 10, Graphics.TOP | Graphics.LEFT);
        } else {
            g.drawImage(redDot, margin + logo.getWidth() + margin + 41, 10, Graphics.TOP | Graphics.LEFT);
        }

        String text = "";

        if (activeTile == null) {
            text = "No location yet...";
        }else{

            try {
                //msg = "Fetching map image...";
                log("getting the map tile images!!!");
                image = Util.getImage(activeTile + "&zoom=" + zoom + "&type=" + mapType);
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
        }

        ScreenUtil.setLeftBt(g, h, menuBt);

        if(text.length()>0){
            g.drawImage(textArea, margin, margin + logo.getHeight() + margin, Graphics.TOP | Graphics.LEFT);
            ScreenUtil.drawText(g, text, 10, logo.getHeight() + 3 * margin, fh);
        }

        switch(screenStat){
            case HOME_STAT:
                if(showMenu){
                    /*if(text.length()==0){
                        text += "Select a option from the menu.";
                    }*/
                    String[] options = {"switch map","zoom out", "zoom in", "drop media"};
                    ScreenUtil.createMenu(g, f, h, fh, options, menuTop, menuMiddle, menuBottom);
                }
                break;
            case ASSIGNMENT_STAT:
                if(showMenu){
                    String[] options = {"answer"};
                    ScreenUtil.createMenu(g, f, h, fh, options, menuTop, menuMiddle, menuBottom);
                }
                break;
            case DROP_STAT:
                if(showMenu){
                    String[] options = {"drop message","drop image", "drop video", "drop audio"};
                    ScreenUtil.createMenu(g, f, h, fh, options, menuTop, menuMiddle, menuBottom);
                }
                break;
        }

        //g.drawString(netStatus, 10, 20, Graphics.TOP | Graphics.LEFT);
        //g.drawString(gpsStatus, 10, 20 + 2 * fh, Graphics.TOP | Graphics.LEFT);
        ScreenUtil.setRightBt(g, h, w, backBt);
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
                    if(showMenu){
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
                    }else{
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
            if(showMenu){
                ScreenUtil.nextMenuItem();
            }
        } else if (key == -2 || getGameAction(key) == Canvas.DOWN) {
            if(showMenu){
                ScreenUtil.prevMenuItem();
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
