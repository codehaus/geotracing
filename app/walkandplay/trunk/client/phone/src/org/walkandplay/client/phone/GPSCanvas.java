package org.walkandplay.client.phone;

import org.geotracing.client.Preferences;

import javax.bluetooth.*;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.rms.RecordStoreException;
import java.util.Hashtable;

public class GPSCanvas extends DefaultCanvas implements DiscoveryListener {

    private Hashtable devices = new Hashtable(2);
    private String[] discoveredDevices = new String[20];
    private LocalDevice device;
    private RemoteDevice remoteDevice;
    private String connectionURL;
    private ServiceRecord serviceRecord;
    private String deviceName;
    private int deviceCounter = 0;
    private static Preferences preferences;

    public static final String RMS_STORE_NAME = "GPS";
    public static final String RMS_GPS_NAME = "name";
    public static final String RMS_GPS_URL = "url";

    // image objects
    private Image smallLogo;

    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int MENU_STAT = 1;
    private final static int DEVICES_STAT = 2;
    private final static int DEVICE_SELECTED_STAT = 3;
    private final static int SEARCHING_SERVICES_STAT = 4;

    private int fontType = Font.FACE_MONOSPACE;

    public GPSCanvas(WPMidlet aMidlet) {
        super(aMidlet);
        try {
            setFullScreenMode(true);
            ScreenUtil.resetMenu();

            // load all images
            smallLogo = Image.createImage("/gps_icon_small.png");
        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    static public String getGPSName() {
        return getPreferences().get(RMS_GPS_NAME);
    }

    static public void clearGPSName() {
        getPreferences().put(RMS_GPS_NAME, "");
    }

    static public String getGPSURL() {
        return getPreferences().get(RMS_GPS_URL);
    }

    /**
     * Start device inquiry. Your application call this method to start inquiry.
     */
    public void searchDevices() {
        try {
            // initialize the JABWT stack
            device = LocalDevice.getLocalDevice(); // obtain reference to singleton
            device.setDiscoverable(DiscoveryAgent.GIAC); // set Discover Mode
            device.getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, this);
            log("Searching GPS Devices...");
        } catch (Throwable e) {
            log("cannot start search ex=" + e + "]");
        }
    }

    /**
     * Searches for a service from the gps device.
     */
    private void searchServices() {
        try {
            log("Start service search" + remoteDevice.getFriendlyName(false));
            // Use the serial UUID for connection
            UUID[] serviceUUIDs = new UUID[1];
            serviceUUIDs[0] = new UUID(0x1101);
            DiscoveryAgent agent = device.getDiscoveryAgent();
            agent.searchServices(null, serviceUUIDs, remoteDevice, this);

        } catch (Throwable ex) {
            log("Error searchServices() " + ex);
        }
    }

    public synchronized void deviceDiscovered(RemoteDevice aRemoteDevice, DeviceClass deviceClass) {
        try {
            String name = aRemoteDevice.getFriendlyName(false);
            log("found: [" + name + "]\n" + aRemoteDevice.getBluetoothAddress());

            // BugFix: some phones do not return friendly name (nokia 6230) ?
            if (name == null) {
                name = "unnamed";
            }
            if (devices.containsKey(name)) {
                name += "-2";
            }
            deviceCounter++;
            discoveredDevices[deviceCounter - 1] = name;
            devices.put(name, aRemoteDevice);
            log("wait, searching further...");
        } catch (Throwable t) {
            log("ERROR getting name");
        }
    }

    public synchronized void inquiryCompleted(int complete) {
        log("Device search complete");
        String[] temp = new String[deviceCounter];
        for (int i = 0; i < deviceCounter; i++) {
            temp[i] = discoveredDevices[i];
        }
        discoveredDevices = temp;
        ScreenUtil.resetMenu();
        screenStat = DEVICES_STAT;
        repaint();
    }

    public synchronized void servicesDiscovered(int transId, ServiceRecord[] records) {
        log("[srvDisc] #" + records.length);

        if (records.length > 0) {
            serviceRecord = records[0];
            connectionURL = serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            log("[SERVICE_FOUND] " + connectionURL);
        } else {
            log("[NO_RECORDS_FOUND] ");
        }
    }

    public synchronized void serviceSearchCompleted(int transId, int aStatus) {
        if (serviceRecord == null) {
            log("completed " + aStatus + " [NO_SERVICES_FOUND] ");
            log("hmm, press Ok or Exit and try again..");
        } else {
            try {
                getPreferences().put(RMS_GPS_NAME, deviceName);
                getPreferences().put(RMS_GPS_URL, connectionURL);
                getPreferences().save();
            } catch (RecordStoreException e) {
                log("RMS Error" + e);
            }

            log("OK using GPS named ");
            log("[" + deviceName + "]");
            log("url=");
            log("[" + connectionURL + "]");
            log(" ");
            screenStat = DEVICE_SELECTED_STAT;
            repaint();
        }
    }

    private static Preferences getPreferences() {
        try {
            if (preferences == null) {
                preferences = new Preferences(RMS_STORE_NAME);
			}
            return preferences;
        } catch (RecordStoreException e) {
            return null;
        }
    }

    private int drawText(Graphics aGraphics, String aText){
        ScreenUtil.drawTextArea(aGraphics, 100, (w - 2*margin - middleTextArea.getWidth())/2, margin + smallLogo.getHeight() + logo.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
        return ScreenUtil.drawText(aGraphics, aText, (w - middleTextArea.getWidth())/2, margin + smallLogo.getHeight() + logo.getHeight() + 2*margin, fh, 100);
    }

    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        super.paint(g);

        g.drawImage(smallLogo, (w - 2*margin - middleTextArea.getWidth())/2, logo.getHeight() + margin, Graphics.TOP | Graphics.LEFT);
        switch (screenStat) {
            case HOME_STAT:
                String GPS = getGPSName();
                String text;
                if (GPS != null && GPS.length() > 0) {
                    text = "You previously used a GPS with name " + GPS + ".";
                    text += "To change your GPS choose 'select gps' from the menu.";
                    ScreenUtil.drawLeftSoftKey(g, h, menuBt, margin);
                } else {
                    deviceCounter = 0;
                    searchDevices();
                    text = "Searching for a GPS device...";
                }
                drawText(g, text);
                ScreenUtil.drawRightSoftKey(g, h, w, backBt, margin);
                break;
            case MENU_STAT:
                String[] options = {"select gps"};
                ScreenUtil.drawMenu(g, h, options, menuTop, menuMiddle, menuBottom, menuSel);
                ScreenUtil.drawLeftSoftKey(g, h, menuBt, margin);
                break;
            case DEVICES_STAT:
                log("show devices in menu");
                text = "Choose your GPS device from the menu";
                drawText(g, text);
                ScreenUtil.drawMenu(g, h, discoveredDevices, menuTop, menuMiddle, menuBottom, menuSel);
                break;
            case SEARCHING_SERVICES_STAT:
                text = "Completing GPS connection...";
                drawText(g, text);
                break;
            case DEVICE_SELECTED_STAT:
                text = "Your GPS device is stored";                
                drawText(g, text);
                ScreenUtil.drawRightSoftKey(g, h, w, backBt, margin);
                new Forwarder(org.walkandplay.client.phone.WPMidlet.HOME_CANVAS, 2);
                break;
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
                    String GPS = getGPSName();
                    if (GPS != null && GPS.length() > 0) {
                        screenStat = MENU_STAT;
                    }
                    break;
                case MENU_STAT:
                    if (ScreenUtil.getSelectedMenuItem() == 1) {
                        clearGPSName();
                        screenStat = HOME_STAT;
                    }
                    break;
                case DEVICES_STAT:
                    if (ScreenUtil.getSelectedMenuItem() != 0) {
                        deviceName = discoveredDevices[(ScreenUtil.getSelectedMenuItem() - 1)];
                        log("you selected [" + device + "]");
                        remoteDevice = (RemoteDevice) devices.get(deviceName);
                        searchServices();
                        screenStat = SEARCHING_SERVICES_STAT;
                    }
                    break;
            }
            // right softkey
        } else if (key == -7) {
            switch (screenStat) {
                case HOME_STAT:
                    midlet.setScreen(org.walkandplay.client.phone.WPMidlet.HOME_CANVAS);
                    break;
                case DEVICES_STAT:
                    screenStat = HOME_STAT;
                    break;
            }
            // left
        } else if (key == -3 || getGameAction(key) == Canvas.LEFT) {
            // right
        } else if (key == -4 || getGameAction(key) == Canvas.RIGHT) {
            // up
        } else if (key == -1 || getGameAction(key) == Canvas.UP) {
            ScreenUtil.selectNextMenuItem();
            // down
        } else if (key == -2 || getGameAction(key) == Canvas.DOWN) {
            ScreenUtil.selectPrevMenuItem();
        } else if (getGameAction(key) == Canvas.KEY_STAR || key == Canvas.KEY_STAR) {
        } else if (getGameAction(key) == Canvas.KEY_POUND || key == Canvas.KEY_POUND) {
        } else if (key == -8) {
        } else {
        }

        repaint();
    }

}
