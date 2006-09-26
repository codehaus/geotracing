package org.walkandplay.client.phone;

import javax.bluetooth.*;
import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;
import java.util.Hashtable;

public class GPSSelector extends Form implements CommandListener, DiscoveryListener {
	private Command search, ok, cancel;
	private ChoiceGroup deviceCG = new ChoiceGroup("Devices", ChoiceGroup.EXCLUSIVE);
	private Hashtable devices = new Hashtable(2);
	static private Preferences preferences;
	private MIDlet midlet;
	private Displayable nextScreen;
	private LocalDevice device;
	private DiscoveryAgent agent;
	private RemoteDevice remoteDevice;
	private String connectionURL;
	private ServiceRecord serviceRecord;
	private String deviceName;
	public static final String RMS_STORE_NAME = "GPS";
	public static final String RMS_GPS_NAME = "name";
	public static final String RMS_GPS_URL = "url";

	public GPSSelector(MIDlet theMIDlet) {
		super("GPS Device Selector");
		midlet = theMIDlet;
		nextScreen = Display.getDisplay(midlet).getCurrent();
		search = new Command("Search", Command.SCREEN, 1);
		ok = new Command("OK", Command.OK, 1);
		cancel = new Command("Cancel", Command.CANCEL, 1);

		addCommand(search);
		addCommand(cancel);
		setCommandListener(this);
		append("Pairing your GPS to the program.");
		append("Your Bluetooth GPS should be switched on.");
		append("Press Search in menu to start and wait for choice-menu");
	}

	public void commandAction(Command c, Displayable d) {

		if (c == search) {
			removeCommand(search);
			addCommand(ok);
			searchDevices();
		} else if (c == ok) {
			if (remoteDevice == null) {
				deviceName = deviceCG.getString(deviceCG.getSelectedIndex());
				cls();
				append("you selected [" + device + "]");
				remoteDevice = (RemoteDevice) devices.get(deviceName);
				searchServices();
			} else {
				midlet.notifyDestroyed();
			}

		} else {
			Display.getDisplay(midlet).setCurrent(nextScreen);
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
			log("Start service search" + remoteDevice.getFriendlyName(false));
			// Use the serial UUID for connection
			UUID[] serviceUUIDs = new UUID[1];
			serviceUUIDs[0] = new UUID(0x1101);
			agent = device.getDiscoveryAgent();
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
			deviceCG.append(name, null);
			devices.put(name, aRemoteDevice);

			log("wait, searching further...");
		} catch (Throwable t) {
			log("ERROR getting name");
		}
	}


	public synchronized void inquiryCompleted(int complete) {
		//log("Device search complete");

		cls();
		append(deviceCG);
		log("");
		log("Select a device and press Ok in menu");
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
			log("OK using GPS named ");
			log("[" + deviceName + "]");
			log("url=");
			log("[" + connectionURL + "]");
			log(" ");
			log("press Ok or Exit and restart program");
		}
	}

	/**
	 * cls
	 *
	 * Clear the whole form content.
	 */
	public void cls() {
		deleteAll();
	}

	public void log(String message) {
		append(new StringItem("", message + "\n"));
		//System.out.println(message);
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
