package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.*;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Monitor for geotagged objects around point.
 */
public class FriendFinderDisplay extends GameCanvas implements CommandListener, GPSFetcherListener {
    private GPSFetcher gpsFetcher;

    private static final int MIN_HIT_DIST = 20;
    private static final int START_RADIUS = 50;
    private static final int MAX_OBJECTS = 10;
    private static final long REFRESH_INTERVAL_MILLIS = 8000;
    private static final int AUDIO_VOLUME = 70;

    private Displayable prevScreen;
    private String queryBaseURL;
    private WPMidlet midlet;
    private int radius = START_RADIUS, max = MAX_OBJECTS;
    private int targetRadius = radius;
    private boolean active;
    private Vector detects = new Vector(0);
    private Timer timer;
    private int w = -1, h = -1;
    private Font f, fb;
    private GPSLocation myLocation;
    private String queryURL = "";
    private JXElement hitUser, showUser;
    private Image hitImage;
    private static final int STATE_IDLE = 1;
    private static final int STATE_FILTER_CHANGE = 2;
    private static final int STATE_DETECTING = 3;
    private static final int STATE_DETECTED = 4;
    private static final int STATE_OBJECT_FETCHING = 5;
    private static final int STATE_OBJECT_SHOW = 6;
    private int state = STATE_IDLE;
    private boolean emulator = false;
    private JXElement nearestUser;

    private Command BACK_CMD = new Command("Back", Command.ITEM, 2);

    public FriendFinderDisplay(WPMidlet aMidlet) {
        super(false);
        setFullScreenMode(true);
        midlet = aMidlet;
        queryBaseURL = midlet.getKWUrl() + "/srv/get.jsp?cmd=q-around";
        prevScreen = Display.getDisplay(aMidlet).getCurrent();
        startGPSFetcher();
        addCommand(BACK_CMD);
        show();
    }

    public void activate() {
        passivate();
        timer = new Timer();
        timer.schedule(new DetectTask(), 4000, REFRESH_INTERVAL_MILLIS);
        active = true;
    }

    public void passivate() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
        active = false;
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            gpsFetcher.stop();
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

    /**
     * Handles all key actions.
     *
     * @param key The Key that was hit.
     */
    public void keyPressed(int key) {
        switch (key) {
            case KEY_POUND:
                passivate();
                Display.getDisplay(midlet).setCurrent(prevScreen);
                break;
        }

        int inc = 1;
        if (targetRadius > 1000) {
            inc = 1000;
        } else if (targetRadius > 100) {
            inc = 100;
        } else if (targetRadius > 10) {
            inc = 10;
        }

        switch (getGameAction(key)) {
            case UP:
            case RIGHT:
                targetRadius += inc;
                setState(STATE_FILTER_CHANGE);
                break;
            case LEFT:
            case DOWN:
                targetRadius -= inc;
                setState(STATE_FILTER_CHANGE);
                break;
            case FIRE:
                hitImage = null;
                showUser = null;
                if (state == STATE_OBJECT_SHOW) {
                    setState(STATE_DETECTING);
                    break;
                }

                if (hitUser != null) {
                    showUser = new JXElement("record");
                    showUser.addChildren(hitUser.getChildren());
                    final String type = showUser.getChildText("type");
                    showUser.setChildText("text", "fetching " + type + "...");
                    String id = showUser.getChildText("id");
                    String time = Util.timeToString(Long.parseLong(showUser.getChildText("time")));
                    showUser.setChildText("ftime", time);
                    if (showUser.getChildText("name") == null) {
                        showUser.setChildText("name", "unnamed");
                    }
                    // uer location
                    showUser.setChildText("text", "last location of " + showUser.getChildText("name"));

                }
                break;

        }

        if (targetRadius <= 0) {
            targetRadius = 1;
        }
    }

    /**
     * Draws the radar screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        if (f == null) {
            w = getWidth();
            h = getHeight();
            if (w == 0 || h == 0) {
                w = 176;
                h = 208;
            }
            f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            fb = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
        }


        String msg = "starting...";
        try {
            // Clear screen
            g.setFont(f);
            g.setColor(4, 4, 4);
            g.fillRect(0, 0, w, h);

            // Draw header box
            int headerOffset = 18;
            g.setColor(0xCCCCCC);
            g.drawLine(0, headerOffset, w, headerOffset);

            g.setColor(0xEEEEEE);

            switch (state) {
                case STATE_IDLE:
                    msg = "FriendFinder Starting...";
                    g.drawString("Find your friends by using the radar", 5, 40, Graphics.TOP | Graphics.LEFT);
                    g.drawString("use \"#\" key to go back", 5, 65, Graphics.TOP | Graphics.LEFT);
                    activate();
                    state = STATE_DETECTING;
                    break;

                    // Radaring states
                case STATE_DETECTING:
                case STATE_DETECTED:
                case STATE_FILTER_CHANGE:

                    // Determine heading message

                    // Draw radar circle
                    int radarW = w - 10;
                    int radarH = w - 10;
                    int radarX = 5;
                    int radarY = (headerOffset - 5) + (h - (headerOffset - 5) - w) / 2;

                    int x = radarX + radarW / 2 - 1;
                    int y = radarY + radarH / 2 - 1;
                    g.setColor(0xAAAAAA);
                    g.fillArc(radarX, radarY, radarW, radarH, 0, 360);

                    // Draw detected area
                    int dist = (radius < targetRadius) ? radius : targetRadius;
                    g.setColor(0x444444);
                    int dx = radarX + radarW / 2 - (dist * radarW) / targetRadius / 2;
                    int dy = radarY + radarH / 2 - (dist * radarH) / targetRadius / 2;
                    int dw = (radarW * dist) / targetRadius;
                    g.fillArc(dx, dy, dw, dw, 0, 360);

                    g.setColor(0xFFFFFF);
                    g.fillArc(x, y, 2, 2, 0, 360);

                    g.setColor(0xCCCCCC);
                    g.drawLine(0, h - headerOffset, w, h - headerOffset);

                    // Draw detected objects on radar as concentric circles
                    nearestUser = null;
                    int visibleObjCount = 0;

                    if (detects.size() <= max) {
                        JXElement obj;
                        int objDist, objX, objY, objW, objH;
                        String objType, objId;
                        nearestUser = null;

                        for (int i = 0; i < detects.size(); i++) {
                            obj = (JXElement) detects.elementAt(i);

                            // Line color based on object type
                            objType = obj.getChildText("type");
                            g.setColor(0xFF99FF);
                            objId = obj.getChildText("id");

                            // Draw circle by distance
                            objDist = Integer.parseInt(obj.getChildText("distance"));
                            log("id=" + objId + " objType=" + objType + " dist=" + objDist);
                            if (objDist > radius) {
                                // outside visible (detected) range
                                continue;
                            }
                            visibleObjCount++;
                            objX = radarX + radarW / 2 - (objDist * radarW) / targetRadius / 2;
                            objY = radarY + radarH / 2 - (objDist * radarH) / targetRadius / 2;
                            objW = (radarW * objDist) / targetRadius;
                            objH = objW;
                            if (objW < radarW) {
                                g.drawArc(objX, objY, objW, objH, 0, 360);
                            }

                            // Remember object nearest to me
                            if (nearestUser == null || objDist < Integer.parseInt(nearestUser.getChildText("distance"))) {
                                nearestUser = obj;
                            }
                        }
                    }

                    msg = "r=" + targetRadius + "m  | ";
                    if (state == STATE_DETECTING) {
                        msg += "detecting...";
                    } else if (state == STATE_FILTER_CHANGE) {
                        msg += " ";
                    } else if (state == STATE_DETECTED) {
                        if (detects.size() >= max) {
                            visibleObjCount = detects.size();
                        }
                        msg += visibleObjCount + " ";
                        msg += "users";
                    }

                    // Show nearest object info
                    if (nearestUser != null) {
                        g.setColor(0xFFFFFF);
                        g.setFont(fb);
                        int distance = Integer.parseInt(nearestUser.getChildText("distance"));
                        int id = Integer.parseInt(nearestUser.getChildText("id"));
                        String hint = nearestUser.getChildText("user") + "/" + nearestUser.getChildText("type") + "/" + distance + "m/";
                        String name = nearestUser.getChildText("name");
                        if (name == null || name.length() == 0) {
                            name = "unnamed";
                        }

                        int nameLength = name.length();
                        if (nameLength > 8) {
                            name = name.substring(0, 4) + ".." + name.substring(nameLength - 2, nameLength);
                        }
                        hint += name;
                        // Did we bump on the nearest object ?
                        if (distance < MIN_HIT_DIST) {
                            msg += " <<HIT>>";
                            if (hitUser != null && nearestUser != hitUser && !emulator) {
                                Util.playTone(80, 50, AUDIO_VOLUME);
                                Util.playTone(90, 250, AUDIO_VOLUME);
                            }
                            hitUser = nearestUser;
                            hint = "<<" + hint + ">>";
                        }
                        g.drawString(hint, w / 2, h - 3, Graphics.BOTTOM | Graphics.HCENTER);
                    }
                    break;

                    // Object display states
                case STATE_OBJECT_FETCHING:
                case STATE_OBJECT_SHOW:
                    if (state == STATE_OBJECT_FETCHING) {
                        msg = "fetching " + showUser.getChildText("type") + " object...";
                    } else {
                        msg = showUser.getChildText("name");
                    }

                    int ty = 40;
                    if (hitImage != null) {
                        g.drawImage(hitImage, 0, headerOffset, Graphics.TOP | Graphics.LEFT);
                        ty = hitImage.getHeight() + 25;
                    } else {
                        g.drawString(showUser.getChildText("text"), 5, 25, Graphics.TOP | Graphics.LEFT);
                    }

                    g.drawString("by: " + showUser.getChildText("user"), 5, ty, Graphics.TOP | Graphics.LEFT);
                    g.drawString(showUser.getChildText("ftime"), 5, ty + 18, Graphics.TOP | Graphics.LEFT);
                    g.drawString("distance: " + showUser.getChildText("distance") + " m", 5, ty + 31, Graphics.TOP | Graphics.LEFT);

                    break;
                default:
                    msg = "unknown state";
                    break;
            }


        } catch (Throwable t) {
            log("paint() error " + t);
            msg = "paint() error " + t;
        }

        // Draw top message
        g.setColor(0xFFFFFF);
        g.drawString(msg, w / 2, 3, Graphics.TOP | Graphics.HCENTER);
    }

    protected void setState(int aState) {
        state = aState;
        log("state=" + aState);
        show();
    }

    protected void show() {
        if (active) {
            repaint();
        }
    }

    protected void log(String s) {
        // msg = s;
        if (emulator) {
			System.out.println(s);
		}
    }

    private class DetectTask extends TimerTask {
        public void run() {
            // log("run()");
            if (state == STATE_OBJECT_FETCHING || state == STATE_OBJECT_SHOW) {
                return;
            }

            log("fetching GPS location...");
            setState(STATE_DETECTING);

            GPSLocation myNewLocation = GPSFetcher.getInstance().getCurrentLocation();
            if (myNewLocation != null) {
                myLocation = myNewLocation;
            }

            if (myLocation == null) {
                log("no GPS location (yet)");
                return;
            }

            String me = midlet.getKWUser();

            String loc = Util.format(myLocation.lon, 10) + "," + Util.format(myLocation.lat, 10);
            String newQueryURL = queryBaseURL + "&types=user&loc=" + loc + "&radius=" + targetRadius + "&max=" + max + "&me=" + me;

            // No new query if identical to last query
            if (newQueryURL.equals(queryURL)) {
                setState(STATE_DETECTED);
                return;
            }

            queryURL = newQueryURL;
            JXElement result = null;
            try {
                result = Util.getXML(queryURL);
            } catch (Throwable t) {
                log("error in query: " + t.getMessage());
            }

            if (result != null) {
                detects = result.getChildren();
            } else {
                log("no objects found");
            }

            radius = targetRadius;
            setState(STATE_DETECTED);
        }
    }

    public void onGPSConnect() {
        Log.log(" connecting gps");
    }

    public void onGPSLocation(GPSLocation aLocation) {
        Log.log("gps location");
    }

    public void onGPSInfo(GPSInfo theInfo) {
        Log.log("gps info");
    }

    public void onGPSDisconnect() {
        Log.log("gps disconnect");
    }

    public void onGPSError(String aReason, Throwable anException) {
        Log.log("gps error");
    }

    public void onGPSStatus(String aStatusMsg) {
        Log.log("gps status:" + aStatusMsg);
    }

    public void onGPSTimeout() {
        Log.log("gps timeout");
    }

    private void startGPSFetcher() {
        try {
            String gpsURL = GPSSelector.getGPSURL();
            if (gpsURL == null) {
                onGPSStatus("NO GPS");
                return;
            }

            long GPS_SAMPLE_INTERVAL = Long.parseLong(midlet.getAppProperty("gps-sample-interval"));
            gpsFetcher = GPSFetcher.getInstance();
            gpsFetcher.setListener(this);
            gpsFetcher.setURL(gpsURL);
            gpsFetcher.start(GPS_SAMPLE_INTERVAL);
        } catch (Throwable t) {
            onGPSStatus("start error");
            gpsFetcher = null;
        }
    }
}
