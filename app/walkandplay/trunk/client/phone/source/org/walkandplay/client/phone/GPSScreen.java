package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;
import javax.bluetooth.*;
import java.io.IOException;
import java.util.Hashtable;

import org.geotracing.client.Preferences;
import org.geotracing.client.Log;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
//public class HomeScreen extends Form implements CommandListener {
public class GPSScreen extends Form implements CommandListener, DiscoveryListener  {
    MIDlet midlet;
    private Displayable prevScreen;
    List menuScreen;
    Command help1Cmd = new Command(Locale.get("help.Topic1"), Command.ITEM, 2);
    Command help2Cmd = new Command(Locale.get("help.Topic2"), Command.ITEM, 2);
    Command help3Cmd = new Command(Locale.get("help.Topic3"), Command.ITEM, 2);
    Command backCmd = new Command("Back", Command.BACK, 1);

    StringItem label = new StringItem("", "Help");
    StringItem text = new StringItem("", "Welcome to the help section");

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

    public GPSScreen(MIDlet aMIDlet) {
        //#style form
        super("");
        midlet = aMIDlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();

        try {
            Image logo;
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/gt_logo.png");
            //#else
            logo = scheduleImage("/gt_logo.png");
            //#endif

            //#style logo
            ImageItem logoItem = new ImageItem("", logo, ImageItem.LAYOUT_DEFAULT, "logo");
            append(logoItem);
        } catch (IOException e) {
            e.printStackTrace();
        }

        append(label);
        append(text);

        addCommand(help1Cmd);
        addCommand(help2Cmd);
        addCommand(help3Cmd);

        addCommand(backCmd);
        setCommandListener(this);

        Display.getDisplay(midlet).setCurrent(this);
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
            Log.log("Searching GPS Devices...");
        } catch (Throwable e) {
            Log.log("cannot start search ex=" + e + "]");
        }
    }

    /**
     * Searches for a service from the gps device.
     */
    private void searchServices() {
        try {
            Log.log("Start service search" + remoteDevice.getFriendlyName(false));
            // Use the serial UUID for connection
            UUID[] serviceUUIDs = new UUID[1];
            serviceUUIDs[0] = new UUID(0x1101);
            DiscoveryAgent agent = device.getDiscoveryAgent();
            agent.searchServices(null, serviceUUIDs, remoteDevice, this);

        } catch (Throwable ex) {
            Log.log("Error searchServices() " + ex);
        }
    }

    public synchronized void deviceDiscovered(RemoteDevice aRemoteDevice, DeviceClass deviceClass) {
        try {
            String name = aRemoteDevice.getFriendlyName(false);
            Log.log("found: [" + name + "]\n" + aRemoteDevice.getBluetoothAddress());

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
            Log.log("wait, searching further...");
        } catch (Throwable t) {
            Log.log("ERROR getting name");
        }
    }

    public synchronized void inquiryCompleted(int complete) {
        Log.log("Device search complete");
        String[] temp = new String[deviceCounter];
        for (int i = 0; i < deviceCounter; i++) {
            temp[i] = discoveredDevices[i];
        }
        discoveredDevices = temp;
        /*ScreenUtil.resetMenu();
        screenStat = DEVICES_STAT;
        repaint();*/
    }

    public synchronized void servicesDiscovered(int transId, ServiceRecord[] records) {
        Log.log("[srvDisc] #" + records.length);

        if (records.length > 0) {
            serviceRecord = records[0];
            connectionURL = serviceRecord.getConnectionURL(ServiceRecord.NOAUTHENTICATE_NOENCRYPT, false);
            Log.log("[SERVICE_FOUND] " + connectionURL);
        } else {
            Log.log("[NO_RECORDS_FOUND] ");
        }
    }

    public synchronized void serviceSearchCompleted(int transId, int aStatus) {
        if (serviceRecord == null) {
            Log.log("completed " + aStatus + " [NO_SERVICES_FOUND] ");
            Log.log("hmm, press Ok or Exit and try again..");
        } else {
            try {
                getPreferences().put(RMS_GPS_NAME, deviceName);
                getPreferences().put(RMS_GPS_URL, connectionURL);
                getPreferences().save();
            } catch (RecordStoreException e) {
                Log.log("RMS Error" + e);
            }

            Log.log("OK using GPS named ");
            Log.log("[" + deviceName + "]");
            Log.log("url=");
            Log.log("[" + connectionURL + "]");
            Log.log(" ");
            /*screenStat = DEVICE_SELECTED_STAT;
            repaint();*/
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

    /*private int drawText(Graphics aGraphics, String aText){
        ScreenUtil.drawTextArea(aGraphics, 100, (w - 2*margin - middleTextArea.getWidth())/2, margin + smallLogo.getHeight() + logo.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
        return ScreenUtil.drawText(aGraphics, aText, (w - middleTextArea.getWidth())/2, margin + smallLogo.getHeight() + logo.getHeight() + 2*margin, fh, 100);
    }*/

    /*
       * The commandAction method is implemented by this midlet to
       * satisfy the CommandListener interface and handle the Cancel action.
       */
    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == backCmd) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == help1Cmd) {
            label.setText(Locale.get("help.Topic1"));
            text.setText(Locale.get("help.Topic1Text"));
        } else if (cmd == help2Cmd) {
            label.setText(Locale.get("help.Topic2"));
            text.setText(Locale.get("help.Topic2Text"));
        } else if (cmd == help3Cmd) {
            label.setText(Locale.get("help.Topic3"));
            text.setText(Locale.get("help.Topic3Text"));
        }
    }


}
