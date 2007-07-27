package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.*;

import java.util.Calendar;
import java.util.TimeZone;
import java.util.Vector;

/**
 * Integrates GPS, Screen and Network interaction.
 *
 * @author Just van den Broecke
 * @version $Id: Tracer.java 222 2006-12-10 00:17:59Z just $
 */
public class TracerEngine implements GPSFetcherListener, NetListener {

    /**
     * instance of GPSFetcher
     */
    private GPSFetcher gpsFetcher;

    /**
     * Instance of KeyWorx client.
     */
    private Net net;
    private WPMidlet midlet;

    private int sampleCount;
    private boolean paused = true;
    private int VOLUME = 70;
    public DefaultTraceDisplay traceDisplay;
    public PlayDisplay playDisplay;
    private int roadRating = -1;
    private Vector points = new Vector(3);
    public static final long DEFAULT_LOC_SEND_INTERVAL_MILLIS = 25000;
    private long locSendIntervalMillis = DEFAULT_LOC_SEND_INTERVAL_MILLIS;
    private long lastTimeLocSent;

    private boolean isPlaying;
    private boolean isTracing;


    /**
     * Starts GPS fetching and KW client.
     */
    public TracerEngine(WPMidlet aMIDlet, DefaultTraceDisplay aDisplay) {
        traceDisplay = aDisplay;
        isTracing = true;
        midlet = aMIDlet;
        net = Net.getInstance();
        net.setListener(this);
    }

    public TracerEngine(WPMidlet aMIDlet, PlayDisplay aDisplay) {
        playDisplay = aDisplay;
        isPlaying = true;
        midlet = aMIDlet;
        net = Net.getInstance();
        net.setListener(this);
    }

    public Net getNet() {
        return net;
    }

    public boolean isPaused() {
        return paused;
    }

    /**
     * Set road rating.
     */
    public void setRoadRating(int aRating) {
        roadRating = aRating;
    }

    /**
     * Starts GPS fetching and KW client.
     */
    public void start() {
        net.setProperties(midlet);
        net.start();
        startGPSFetcher();
        Log.log("time=" + Calendar.getInstance().getTime() + " timezone=" + TimeZone.getDefault().getID());
    }

    /**
     * Disconnect
     */
    public void stop() {
        try {
            if (gpsFetcher != null) {
                gpsFetcher.stop();
                gpsFetcher = null;
            }
            net.stop();

        } catch (Throwable t) {
            // ignore
        }
    }

    public void resume() {
        if (!paused) {
            return;
        }
        //net.resume();
        sampleCount = 0;
        paused = false;
    }

    public void suspend() {
        if (paused) {
            return;
        }
        //net.suspend();
        paused = true;
    }

    public void suspendResume() {
        if (paused) {
            resume();
        } else {
            suspend();
        }

        if (isPlaying && !paused) {
            /*JXElement req = new JXElement("play-suspend-req");
            req.setAttr("id", midlet.getGamePlayId());
            Log.log(new String(req.toBytes(false)));
            JXElement rsp = net.utopiaReq(req);
            Log.log(new String(rsp.toBytes(false)));*/
        }
    }

    public void onGPSConnect() {
        if (isTracing) {
            traceDisplay.onGPSStatus("connected");
            traceDisplay.setStatus("GPS connected");
        } else if (isPlaying) {
            playDisplay.onGPSStatus("connected");
            playDisplay.setStatus("GPS connected");
        }

        Util.playTone(60, 250, VOLUME);
    }

    /**
     * From GPSFetcher: GPS meta NMEA sample received.
     */
    synchronized public void onGPSInfo(GPSInfo theInfo) {
        if (isTracing) {
            traceDisplay.setGPSInfo(theInfo);
        } else if (isPlaying) {
            playDisplay.setGPSInfo(theInfo);
        }
    }

    /**
     * From GPSFetcher: GPS NMEA sample received.
     */
    synchronized public void onGPSLocation(GPSLocation aLocation) {
        Util.playTone(84, 50, VOLUME);

        onGPSStatus("sample #" + (++sampleCount));
        if (paused) {
            if (isTracing) {
                traceDisplay.onNetStatus("paused");
                traceDisplay.setStatus("NOTE: not sending GPS !!\ndo ResumeTrack to send");
            } else if (isPlaying) {
                playDisplay.onNetStatus("paused");
                playDisplay.setStatus("NOTE: not sending GPS !!\ndo ResumeTrack to send");
            }
            return;
        }


        JXElement pt = new JXElement("pt");
        pt.setAttr("nmea", aLocation.data);

        pt.setAttr("t", aLocation.time);

        if (roadRating != -1) {
            pt.setAttr("rr", roadRating);
        }

        points.addElement(pt);

        // Send collected points to server if interval passed
        long now = Util.getTime();
        if (now - lastTimeLocSent > locSendIntervalMillis) {
            lastTimeLocSent = now;
            if (isTracing) {
                sendPoints(points, sampleCount, "t-trk-write-req");
            } else if (isPlaying) {
                sendPoints(points, sampleCount, "play-location-req");
            }
            points.removeAllElements();
        }
    }

    /**
     * Send GPS points.
     */
    public void sendPoints(Vector thePoints, int theCount, String aRequestName) {
        JXElement req = new JXElement(aRequestName);
        req.addChildren(thePoints);
        try {
            onNetStatus("sending #" + theCount);
            JXElement rsp = net.utopiaReq(req);
            // TODO: remove later - teskting purposes
            //rsp.removeChildren();

            /*if (System.currentTimeMillis() % 3 == 0) {
                JXElement hit = new JXElement("task-hit");
                hit.setAttr("id", 22560);
                rsp.addChild(hit);
            }*/
            
            if (System.currentTimeMillis() % 3 == 0 && !rsp.hasChildren()) {
                JXElement hit = new JXElement("medium-hit");
                hit.setAttr("id", 26527);
                rsp.addChild(hit);
            }

            if (System.currentTimeMillis() % 3 == 0 && !rsp.hasChildren()) {
                JXElement hit = new JXElement("medium-hit");
                hit.setAttr("id", 22629);
                rsp.addChild(hit);
            }

            //long lastCommandTime = Util.getTime();
            if (rsp != null) {
                onNetStatus("sent #" + theCount);
                Log.log(new String(rsp.toBytes(false)));
                Util.playTone(96, 75, VOLUME);
                JXElement e = rsp.getChildAt(0);
                if (e != null) {
                    if (e.getTag().equals("task-hit")) {
                        if (isPlaying) {
                            //<play-location-rsp><task-hit id="54232" state="open|hit|done" answerstate="open" mediastate="open"/></play-location-rsp>
                            //set current task
                            playDisplay.setTaskHit(e);
                        }
                        onNetStatus("task-" + e.getAttr("id"));
                    } else if (e.getTag().equals("medium-hit")) {
                        if (isPlaying) {
                            //<play-location-rsp><medium-hit id="54232" state="open|hit"/></play-location-rsp>
                            //set current medium
                            playDisplay.setMediumHit(e);
                        }
                        onNetStatus("medium-" + e.getAttr("id"));
                    }
                } else {
                    Log.log("No task or medium hit found");
                }
            } else {
                onNetStatus("send error");
            }

        } catch (Throwable pe) {
            onNetStatus("send error");
        }
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSDisconnect() {
        if (isTracing) {
            traceDisplay.onGPSStatus("disconnected");
            traceDisplay.setStatus("GPS disconnected");
        } else if (isPlaying) {
            playDisplay.onGPSStatus("disconnected");
            playDisplay.setStatus("GPS disconnected");
        }
    }

    /**
     * From GPSFetcher: disconnect
     */
    public void onGPSError(String aReason, Throwable anException) {
        Log.log("GPS error: " + aReason + " e=" + anException);
        if (isTracing) {
            traceDisplay.onGPSStatus("error");
        } else if (isPlaying) {
            playDisplay.onGPSStatus("error");
        }
        // traceScreen.setStatus(aReason);
        Util.playTone(72, 100, VOLUME);
        Util.playTone(71, 100, VOLUME);
        Util.playTone(70, 100, VOLUME);
    }

    /**
     * From GPSFetcher: status update
     */
    public void onGPSStatus(String aStatusMsg) {
        if (isTracing) {
            traceDisplay.onGPSStatus(aStatusMsg);
            traceDisplay.setStatus("GPS " + aStatusMsg);
        } else if (isPlaying) {
            playDisplay.onGPSStatus(aStatusMsg);
            playDisplay.setStatus("GPS " + aStatusMsg);
        }
    }

    public void onGPSTimeout() {
        if (isTracing) {
            traceDisplay.onGPSStatus("timeout");
            traceDisplay.setStatus("GPS timeout");
        } else if (isPlaying) {
            playDisplay.onGPSStatus("timeout");
            playDisplay.setStatus("GPS timeout");
        }
    }

    public void onNetInfo(String theInfo) {
        if (isTracing) {
            traceDisplay.setStatus(theInfo);
        } else if (isPlaying) {
            playDisplay.setStatus(theInfo);
        }
    }

    public void onNetError(String aReason, Throwable anException) {
        if (isTracing) {
            traceDisplay.setStatus("ERROR " + aReason + "\n" + anException);
        } else if (isPlaying) {
            playDisplay.setStatus("ERROR " + aReason + "\n" + anException);
        }
    }

    public void onNetStatus(String aStatusMsg) {
        if (isTracing) {
            traceDisplay.onNetStatus(aStatusMsg);
        } else if (isPlaying) {
            playDisplay.onNetStatus(aStatusMsg);
        }
    }

    /**
     * Check local midlet version with the one from server.
     */
    public String versionCheck() {
        String myVersion = midlet.getAppProperty("MIDlet-Version");

        String myName = midlet.getAppProperty("MIDlet-Name");
        String versionURL = Net.getInstance().getURL() + "/ota/version.html";
        String result = null;
        String theirVersion = null;
        try {
            theirVersion = Util.getPage(versionURL);
            if (theirVersion != null && !theirVersion.trim().equals(myVersion)) {
                result = "Your " + myName + " version (" + myVersion + ") differs from the version (" + theirVersion + ") available for download. \nYou may want to upgrade to " + theirVersion;
            }
        } catch (Throwable t) {
            result = "error fetching version from " + versionURL;
        }

        Log.log("versionCheck mine=" + myVersion + " theirs=" + theirVersion);
        return result;
    }

    private void startGPSFetcher() {
        try {
            String gpsURL = GPSSelector.getGPSURL();
            if (gpsURL == null) {
                if (isTracing) {
                    traceDisplay.onGPSStatus("NO GPS");
                    traceDisplay.setStatus("choose SelectGPS in menu");
                } else if (isPlaying) {
                    playDisplay.onGPSStatus("NO GPS");
                    playDisplay.setStatus("choose SelectGPS in menu");
                }
                return;
            }
            if (isTracing) {
                traceDisplay.setStatus("gpsURL=\n" + gpsURL);
            } else if (isPlaying) {
                playDisplay.setStatus("gpsURL=\n" + gpsURL);
            }
            long GPS_SAMPLE_INTERVAL = Long.parseLong(midlet.getAppProperty("gps-sample-interval"));
            gpsFetcher = GPSFetcher.getInstance();
            gpsFetcher.setListener(this);
            gpsFetcher.setURL(gpsURL);
            gpsFetcher.start(GPS_SAMPLE_INTERVAL);
        } catch (Throwable t) {
            if (isTracing) {
                traceDisplay.onGPSStatus("start error");
            } else if (isPlaying) {
                playDisplay.onGPSStatus("start error");
            }            
			gpsFetcher = null;
		}
	}


}
