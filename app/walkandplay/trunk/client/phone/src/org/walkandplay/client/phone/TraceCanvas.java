package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;

public class TraceCanvas extends GameCanvas {

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

    private Tracer tracer;

    // image objects
    private Image logo, menuBt, textArea, backBt, traceLogo, gpsNetBar, redDot, greenDot;

    int margin = 3;

    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int MENU_STAT = 1;


    private int fontType = Font.FACE_MONOSPACE;

    public TraceCanvas(WP aMidlet) {
        super(false);
        try {
            midlet = aMidlet;
            w = getWidth();
            h = getHeight();
            setFullScreenMode(true);

            tileURL = midlet.getAppProperty("kw-url") + "/tile.jsp?";
            System.out.println("tileUrl:" + tileURL);
            tracer = new Tracer(aMidlet, this);
            tracer.start();
            System.out.println("created the tracer object");
            // load all images
            logo = Image.createImage("/logo.png");
            menuBt = Image.createImage("/menu_button.png");
            textArea = Image.createImage("/text_area.png");
            backBt = Image.createImage("/back_button.png");
            traceLogo = Image.createImage("/trace_button_off_small.png");
            gpsNetBar = Image.createImage("/gpsnet_bg.png");
            redDot = Image.createImage("/red_dot.png");
            greenDot = Image.createImage("/green_dot.png");

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
        repaint();
    }


    public void onNetStatus(String s) {
        netStatus = s;
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

        if (activeTile == null) {
            msg = "No location yet...";
            g.drawImage(textArea, 5, logo.getHeight() + 10, Graphics.TOP | Graphics.LEFT);
            String text = "No Location yet";
            ScreenUtil.drawText(g, text, 10, logo.getHeight() + traceLogo.getHeight() + 3 * margin, fh);
            return;
        }

        /*if (image != null) {
            msg = null;
        }
        */
        try {
            //msg = "Fetching map image...";
            log("getting the map tile images!!!");
            image = Util.getImage(activeTile + "&zoom=" + zoom + "&type=" + mapType);
        } catch (Throwable t) {
            String text = "Error fetching image !!";
            text += "maybe this zoom-level is not available";
            text += "try zooming further in or out";
            Log.log("error: MapScreen: t=" + t + " m=" + t.getMessage());
            g.drawImage(textArea, 5, logo.getHeight() + 10, Graphics.TOP | Graphics.LEFT);
            ScreenUtil.drawText(g, text, 10, logo.getHeight() + 15, fh);
            return;
        }

        //g.drawImage(bg, 0, 0, Graphics.TOP | Graphics.LEFT);

        if (image != null) {
            g.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);
            //String[] options = {"toggle map", "zoom out", "zoom in"};
            //ScreenUtil.createMenu(g, f, h, fh, options);
            ScreenUtil.setLeftBt(g, h, menuBt);
        }
        g.drawImage(logo, 5, 5, Graphics.TOP | Graphics.LEFT);
        g.drawString(netStatus, 10, 20, Graphics.TOP | Graphics.LEFT);
        g.drawString(gpsStatus, 10, 20 + 2 * fh, Graphics.TOP | Graphics.LEFT);
        ScreenUtil.setRightBt(g, h, w, backBt);
    }

    /**
     * Handles all key actions.
     *
     * @param key The Key that was hit.
     */
    public void keyPressed(int key) {
//        log("screenstat: " + screenStat);
//        log("key: " + key);
//        log("getGameAction(key): " + getGameAction(key));
        // left soft key & fire
        if (key == -6 || key == -5 || getGameAction(key) == Canvas.FIRE) {
            switch (screenStat) {
                case HOME_STAT:
                    screenStat = MENU_STAT;
                    break;
                case MENU_STAT:
                    if (ScreenUtil.getSelectedMenuItem() == 1) {
                        mapType = mapType.equals("sat") ? "map" : "sat";
                    } else if (ScreenUtil.getSelectedMenuItem() == 2) {
                        zoom++;
                    } else if (ScreenUtil.getSelectedMenuItem() == 3) {
                        zoom--;
                    }
                    break;
            }
            // right softkey
        } else if (key == -7) {
            switch (screenStat) {
                case HOME_STAT:
                    break;
                case MENU_STAT:
                    screenStat = HOME_STAT;
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
            switch (screenStat) {
                case MENU_STAT:
                    ScreenUtil.nextMenuItem();
                    break;
            }
            // down
        } else if (key == -2 || getGameAction(key) == Canvas.DOWN) {
            switch (screenStat) {
                case MENU_STAT:
                    ScreenUtil.prevMenuItem();
                    break;
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
