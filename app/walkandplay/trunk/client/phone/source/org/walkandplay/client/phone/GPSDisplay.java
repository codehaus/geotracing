package org.walkandplay.client.phone;

import org.geotracing.client.Preferences;

import javax.bluetooth.*;
import javax.microedition.lcdui.*;
import javax.microedition.rms.RecordStoreException;
import java.util.Hashtable;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class GPSDisplay extends DefaultDisplay implements DiscoveryListener {
    private Command SEARCH_CMD, OK_CMD;
    private ChoiceGroup deviceCG = new ChoiceGroup("", ChoiceGroup.EXCLUSIVE);
    private Hashtable devices = new Hashtable(2);
    static private Preferences preferences;
    private LocalDevice device;
    private RemoteDevice remoteDevice;
    private String connectionURL;
    private ServiceRecord serviceRecord;
    private String deviceName;
    public static final String RMS_STORE_NAME = "GPS";
    public static final String RMS_GPS_NAME = "name";
    public static final String RMS_GPS_URL = "url";

    private int msgNum;
    private int choiceNum;
    private Image logo;

    public GPSDisplay(WPMidlet theMIDlet) {
        super(theMIDlet, "Set up your GPS");
        try {
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/gps_icon_small.png");
            //#else
            logo = scheduleImage("/gps_icon_small.png");
            //#endif
        } catch (Throwable t) {
            Log.log("Could not load the images on GPSDisplay");
        }
    }

    public void start(){
        // start fresh
        deleteAll();
        
        append(logo);

        SEARCH_CMD = new Command("Search", Command.SCREEN, 1);
        OK_CMD = new Command("OK", Command.OK, 1);

        addCommand(SEARCH_CMD);
        //#style labelinfo
        append("Pairing your GPS to the program");

        //#style formbox
        append(new StringItem("", "Your Bluetooth GPS should be switched on.\nPress Search in menu to start and wait for choice-menu." + "\n"));
    }
    
    public void commandAction(Command c, Displayable d) {

        if (c == SEARCH_CMD) {
            removeCommand(SEARCH_CMD);
            searchDevices();
        } else if (c == OK_CMD) {
            if (remoteDevice == null) {
                deviceName = deviceCG.getString(deviceCG.getSelectedIndex());
                cls();
                log("you selected [" + device + "]");
                remoteDevice = (RemoteDevice) devices.get(deviceName);
                searchServices();
            } else {
                midlet.notifyDestroyed();
            }

        } else {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }
    }

    static public String getGPSName() {
        return getPreferences().get(RMS_GPS_NAME);
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
            cls();

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
            log("Start service search " + remoteDevice.getFriendlyName(false));
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
            log("found: [" + name + "]\n" + aRemoteDevice.getBluetoothAddress() + "\n\nWait, searching further...");

            // BugFix: some phones do not return friendly name (nokia 6230) ?
            if (name == null) {
                name = "unnamed";
            }
            if (devices.containsKey(name)) {
                name += "-2";
            }
            //#style formbox
            deviceCG.append(name, null);
            devices.put(name, aRemoteDevice);

            /*log("wait, searching further...");*/
        } catch (Throwable t) {
            log("ERROR getting name");
        }
    }


    public synchronized void inquiryCompleted(int complete) {
        //log("Device search complete");
        cls();
        //#style labelinfo
        append("Select a device and press Ok in menu");
        choiceNum = append(deviceCG);
        addCommand(OK_CMD);
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

            cls();
            log("OK using GPS named \n[" + deviceName + "]\nurl=[" + connectionURL + "]\n\nPress Ok or Exit and restart program.");
        }
    }

    /**
     * cls
     * <p/>
     * Clear the whole form content.
     */
    public void cls() {
        /*Log.log("# items: " + size());
        Log.log("logo: " + logoNum);
        Log.log("msg: " + msgNum);
        Log.log("choice: " + choiceNum);*/
        /*delete(msgNum);
        delete(choiceNum);*/
        deleteAll();
    }

    public void log(String message) {
        cls();
        //#style formbox
        msgNum = append(new StringItem("", message + "\n"));
        Log.log(message);
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

}