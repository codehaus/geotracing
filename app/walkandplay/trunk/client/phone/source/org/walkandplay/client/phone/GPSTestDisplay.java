package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;
import javax.microedition.location.*;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class GPSTestDisplay extends DefaultDisplay implements CommandListener, LocationListener, ProximityListener {

    private Command startCommand;
    private Command waypointCommand, tolocationCommand, towaypointCommand;
    private Form waypointForm;
    private int lat, lon, alt, speed, course, dist, state, proximity;
    private int waylat, waylon, wayrad, wayalt;
    private double wplat, wplon, wpalt, wprad;
    private Display display;

    private Coordinates oldCoordinates = null, curCoordinates = null;
    int az = 0;
    LocationProvider lp = null;
    private float distance = 0;

    StringItem msg = new StringItem("", "");

    public GPSTestDisplay(WPMidlet theMIDlet) {
        super(theMIDlet, "");

        waypointForm = new Form("New Waypoint");

        startCommand = new Command("Start", Command.OK, 1);
        waypointCommand = new Command("Create", Command.OK, 1);
        tolocationCommand = new Command("Location", Command.EXIT, 1);
        towaypointCommand = new Command("New Waypoint", Command.OK, 1);

        lat = append(new StringItem("Latitude ", ""));
        lon = append(new StringItem("Longitude ", ""));
        alt = append(new StringItem("Altitude ", ""));
        speed = append(new StringItem("Speed ", ""));
        course = append(new StringItem("Course ", ""));
        dist = append(new StringItem("Distance ", ""));
        state = append(new StringItem("", ""));
        proximity = append(new StringItem("", ""));

        waylat = waypointForm.append(new TextField("Latitude     ", "", 9, TextField.DECIMAL));
        waylon = waypointForm.append(new TextField("Longitude    ", "", 9, TextField.DECIMAL));
        wayalt = waypointForm.append(new TextField("Altitude (m) ", "", 9, TextField.DECIMAL));
        wayrad = waypointForm.append(new TextField("Radius (m)   ", "", 9, TextField.DECIMAL));

        if (hasLocationAPI()) {
            createLocationProvider();
        } else {
            msg.setText("No location api");
        }

/*
        waypointForm.addCommand(waypointCommand);
        waypointForm.addCommand(tolocationCommand);
        waypointForm.setCommandListener(this);
*/

        append(msg);

        addCommand(startCommand);
        addCommand(towaypointCommand);
        setCommandListener(this);
        Display.getDisplay(midlet).setCurrent(this);
    }

    private void show(String aMsg) {
        msg.setText(aMsg);
    }

    public void commandAction(Command command, Displayable displayable) {

        if (command == BACK_CMD) {
            midlet.setHome();
        } else if (command == startCommand) {
            Thread getLocationThread = new Thread() {
                public void run() {
                    setListener();
                }
            };
            getLocationThread.start();
        } else if (command == towaypointCommand) {
            display.setCurrent(waypointForm);
        } else if (command == waypointCommand) {
            wplat = Double.valueOf(((TextField) waypointForm.get(waylat)).getString()).doubleValue();
            wplon = Double.valueOf(((TextField) waypointForm.get(waylon)).getString()).doubleValue();
            wpalt = Double.valueOf(((TextField) waypointForm.get(wayalt)).getString()).doubleValue();
            wprad = Double.valueOf(((TextField) waypointForm.get(wayrad)).getString()).doubleValue();

            final Coordinates waypoint = new Coordinates((float) wplat, (float) wplon, (float) wpalt);
            Thread getProximityThread = new Thread() {
                public void run() {
                    setProximity(waypoint, wprad);
                }
            };
            getProximityThread.start();
        } else if (command == tolocationCommand) {
            display.setCurrent(this);
        }
    }

    void createLocationProvider() {
        show("createLocationProvider");
        try {
            if (lp == null) {

                Criteria cr = new Criteria();
                lp = LocationProvider.getInstance(cr);
            }
        } catch (Throwable t) {
            show("createLocationProvider exception:" + t.toString());
        }
    }

    public void locationUpdated(final LocationProvider locProvider, final Location loc) {
        show("locationUpdated");
        try {
            Thread getLocationThread = new Thread() {
                public void run() {
                    getLocation(locProvider, loc);
                }
            };
            getLocationThread.start();
        } catch (Throwable t) {
            show("locationUpdated exception:" + t.toString());
        }
    }

    public void providerStateChanged(LocationProvider locProvider, int newstate) {
        show("providerStateChanged");
        try {
            (get(state)).setLabel("Location Provider State Changed. Location Provider now ");
            if (newstate == LocationProvider.AVAILABLE) {
                ((StringItem) get(state)).setText("AVAILABLE");
            } else if (newstate == LocationProvider.TEMPORARILY_UNAVAILABLE) {
                ((StringItem) get(state)).setText("TEMPORARILY_UNAVAILABLE");
            } else if (newstate == LocationProvider.OUT_OF_SERVICE) {
                ((StringItem) get(state)).setText("OUT_OF_SERVICE");
            }
        } catch (Throwable t) {
            show("providerStateChanged exception:" + t.toString());
        }
    }

    public void monitoringStateChanged(boolean arg0) {
        show("monitoringStateChanged");
        try {
            (get(state)).setLabel("Monitoring State Changed. ");
            if (!arg0) {
                ((StringItem) get(state)).setText("Not currently monitoring");
            } else {
                ((StringItem) get(state)).setText("Currently Monitoring");
            }
        } catch (Throwable t) {
            show("monitoringStateChanged exception:" + t.toString());
        }
    }

    public void proximityEvent(final Coordinates coordinates, final Location loc) {
        show("proximityEvent");
        try {
            Thread getLocationThread = new Thread() {
                public void run() {
                    getLocation(coordinates, loc);
                }
            };
            getLocationThread.start();
        } catch (Throwable t) {
            show("proximityEvent exception:" + t.toString());
        }
    }

    public boolean hasLocationAPI() {
        show("hasLocationAPI");
        if (System.getProperty("microedition.location.version") == null) {
            return false;
        } else {
            return true;
        }
    }

    private void setListener() {
        show("addListener");
        try {
            lp.setLocationListener(this, 2, -1, -1);
        } catch (Throwable t) {
            show("addListener exception:" + t.toString());
        }
    }

    private void setProximity(Coordinates waypoint, double wprad) {
        show("setProximity");
        try {
            LocationProvider.addProximityListener(this, waypoint, (float) wprad);
        } catch (Throwable t) {
            show("setProximity exception:" + t.toString());
        }
    }

    private void getLocation(LocationProvider locProvider, Location loc) {
        show("getLocation");
        try {
            getLocation(loc);
            (get(proximity)).setLabel("You are outside the " +
                    wprad + " meter radius of the defined zone");
        } catch (Throwable t) {
            show("getLocation (LocationProvider locProvider, Location loc) exception:" + t.toString());
        }
    }

    private void getLocation(Coordinates c, Location loc) {
        show("getLocation");
        try {
            getLocation(loc);
            (get(proximity)).setLabel("You are " +
                    loc.getQualifiedCoordinates().distance(c) +
                    " meters from (lat/long/alt)" +
                    c.getLatitude() + "/" + c.getLongitude() + "/" + c.getAltitude());
            setProximity(c, wprad);
        } catch (Throwable t) {
            show("getLocation(Coordinates c, Location loc) exception:" + t.toString());
        }
    }

    private void getLocation(Location loc) {
        show("getLocation");
        try {
            if (loc == null) {
                show("No location...");
                return;
            } else {
                show("location:" + loc);
            }
            QualifiedCoordinates c = loc.getQualifiedCoordinates();
            if (c == null) {
                show("No QualfifiedCoordinates");
                return;
            } else {
                show("QC:" + c);
            }

            if (oldCoordinates == null) {
                oldCoordinates = new Coordinates(c.getLatitude(), c.getLongitude(), c.getAltitude());
            } else {
                distance += c.distance(oldCoordinates);
                curCoordinates = new Coordinates(c.getLatitude(), c.getLongitude(), c.getAltitude());
                az = (int) oldCoordinates.azimuthTo(curCoordinates);
                oldCoordinates.setAltitude(c.getAltitude());
                oldCoordinates.setLatitude(c.getLatitude());
                oldCoordinates.setLongitude(c.getLongitude());
            }

            if (c != null) {
                ((StringItem) get(lat)).setText(String.valueOf(c.getLatitude()));
                ((StringItem) get(lon)).setText(String.valueOf(c.getLongitude()));
                ((StringItem) get(alt)).setText(String.valueOf(c.getAltitude()) + " m");
                ((StringItem) get(speed)).setText(String.valueOf(loc.getSpeed() / 1000 * 3600) + " km/h");
                ((StringItem) get(course)).setText(String.valueOf(az) + " " + findDirection(az));
                ((StringItem) get(dist)).setText(String.valueOf(distance / 1000) + " km");
            }

        } catch (Throwable t) {
            show("getLocation(Location loc) exception:" + t.toString());
        }
    }

    private String findDirection(int azimuth) {
        show("findDirection");
        try {
            if ((azimuth >= 337 && azimuth <= 360) || (azimuth >= 0 && azimuth < 23))
                return "N";
            if (azimuth >= 23 && azimuth < 68)
                return "NE";
            if (azimuth >= 68 && azimuth < 113)
                return "E";
            if (azimuth >= 113 && azimuth < 158)
                return "SE";
            if (azimuth >= 158 && azimuth < 203)
                return "S";
            if (azimuth >= 203 && azimuth < 248)
                return "SW";
            if (azimuth >= 248 && azimuth < 293)
                return "W";
            if (azimuth >= 293 && azimuth < 337)
                return "NW";

        } catch (Throwable t) {
            show("findDirection exception:" + t.toString());
        }
        return null;

    }
}