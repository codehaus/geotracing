/*
 * Created on 04-Apr-2004 at 16:14:27.
 *
 * Copyright (c) 2004-2005 Robert Virkus / Enough Software
 *
 * This file is part of J2ME Polish.
 *
 * J2ME Polish is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * J2ME Polish is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with J2ME Polish; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Commercial licenses are also available, please
 * refer to the accompanying LICENSE.txt or visit
 * http://www.j2mepolish.org for details.
 */
package org.walkandplay.client.phone;

import de.enough.polish.util.Locale;
import org.geotracing.client.Preferences;
import org.geotracing.client.Util;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.rms.RecordStoreException;

public class WPMidlet extends MIDlet implements CommandListener {

    private WPMidlet midlet;
    private List menuScreen;
    private SplashDisplay splashDisplay;
    private SettingsDisplay settingsDisplay;
    private HelpDisplay helpDisplay;
    private GPSDisplay gpsDisplay;
    private SelectGameDisplay selectGameDisplay;
    private CreateDisplay createDisplay;
    private AppStartDisplay activeApp;

    public static final String TITLE = "Walk and Play";
    public static final String RMS_STORE_NAME = "db";

    private static Preferences preferences;

    public final static String KW_URL = "kw-url";
    public final static String WMS_URL = "wms-url";
    public final static String GOOGLE_WMS_URL = "google-wms-url";
    public final static String GEODAN_WMS_URL = "geodan-wms-url";
    public final static String KW_USER = "kw-user";
    public final static String KW_PASSWORD = "kw-password";
    public final static String KW_SERVER = "kw-server";
    public final static String KW_PORT = "kw-port";
    public final static String KW_APP = "kw-app";
    public final static String KW_ROLE = "kw-role";
    public final static String VOLUME = "volume";
    public final static String GPS_SAMPLE_INTERVAL = "gps-sample-interval";
    public final static String GPS_SEND_INTERVAL = "gps-send-interval";
    public final static String EXTERNAL_PLAYER = "external-player";
    public final static String DEMO_MODE = "demo";
	public final static String EMULATOR = "emulator";

    public WPMidlet() {
        super();
        midlet = this;
    }

    public void setHome() {
        if (isInDemoMode()) {
            //#style mainScreen
            menuScreen = new List(TITLE + "(Demo)", List.IMPLICIT);
        } else {
            //#style mainScreen
            menuScreen = new List(TITLE, List.IMPLICIT);
        }
        //#style mainPlayCommand
        menuScreen.append(Locale.get("menu.Play"), null);
        //#style mainTraceCommand
        menuScreen.append(Locale.get("menu.Create"), null);
        //#style mainGPSCommand
        menuScreen.append(Locale.get("menu.GPS"), null);
        //#style mainSettingsCommand
        menuScreen.append(Locale.get("menu.Settings"), null);
        //#style mainHelpCommand
        menuScreen.append(Locale.get("menu.Help"), null);
        //#style mainQuitCommand
        menuScreen.append(Locale.get("menu.Quit"), null);
        if(isInDemoMode()){
            //#style mainLogCommand
            menuScreen.append(Locale.get("menu.Log"), null);
            /*//#style mainLogCommand
            menuScreen.append("test", null);*/
            /*
            //#style mainLogCommand
            menuScreen.append("video display", null);
            //#style mainLogCommand
            menuScreen.append("video form", null);
            //#style mainLogCommand
            menuScreen.append("GPS test display", null);
            //#style mainLogCommand
            menuScreen.append("Friend Finder", null);*/
        }

        menuScreen.setCommandListener(this);
        Display.getDisplay(this).setCurrent(menuScreen);
    }

    public void setTitle(boolean isDemo) {
        if (isDemo) {
            menuScreen.setTitle(TITLE + "(demo)");
        } else {
            menuScreen.setTitle(TITLE);
        }
    }

    protected void startApp() throws MIDletStateChangeException {
        Log.setDemoMode(isInDemoMode());
 		Log.setEmulator(getAppProperty(EMULATOR).equals("true"));
		if (new VersionChecker().check()) {
            /*if(splashDisplay == null){
                splashDisplay = new SplashDisplay(this);
            }
            splashDisplay.start(SplashDisplay.STATE_SPLASH_HOME);*/
            Display.getDisplay(this).setCurrent(new SplashDisplay(this, 1));
        }
    }

    protected void pauseApp() {
        // ignore
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        // just quit
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (screen == menuScreen) {
            if (cmd == List.SELECT_COMMAND) {
                goToScreen(menuScreen.getSelectedIndex());
            }
        }
    }

    public SelectGameDisplay getPlayApp() {
        return selectGameDisplay;
    }

    public CreateDisplay getCreateApp() {
        return createDisplay;
    }

    public String getKWUrl() {
        return getPreferences().get((KW_URL), getAppProperty(KW_URL));
    }

    public String getWMSUrl() {
        return getPreferences().get((WMS_URL), getAppProperty(GEODAN_WMS_URL));
    }

    public String getKWServer() {
        return getPreferences().get((KW_SERVER), getAppProperty(KW_SERVER));
    }

    public String getKWUser() {
        return getPreferences().get((KW_USER), getAppProperty(KW_USER));
    }

    public String getKWPassword() {
        return getPreferences().get((KW_PASSWORD), getAppProperty(KW_PASSWORD));
    }

    public int getKWPort() {
        return Integer.parseInt(getAppProperty(KW_PORT));
    }

    public String getKWApp() {
        return getAppProperty(KW_APP);
    }

    public String getKWRole() {
        return getAppProperty(KW_ROLE);
    }

    public int getVolume() {
        return Integer.parseInt(getAppProperty(VOLUME));
    }

    public boolean useGoogleMaps() {
        return getWMSUrl().equals(GOOGLE_WMS_URL);
    }

    public boolean useExternalPlayer() {
        String bool = getPreferences().get((EXTERNAL_PLAYER), getAppProperty(EXTERNAL_PLAYER));
        if (bool.equals("true")) {
            return true;
        }
        return false;
    }

    public long getGPSSendInterval() {
        return Long.parseLong(getPreferences().get((GPS_SEND_INTERVAL), getAppProperty(GPS_SEND_INTERVAL)));
    }

    public long getGPSSampleInterval() {
        return Long.parseLong(getPreferences().get((GPS_SAMPLE_INTERVAL), getAppProperty(GPS_SAMPLE_INTERVAL)));
    }

    public boolean isInDemoMode() {
        String bool = getPreferences().get((DEMO_MODE), getAppProperty(DEMO_MODE));
        if (bool.equals("true")) {
            return true;
        }
        return false;
    }

    public AppStartDisplay getActiveApp() {
        return activeApp;
    }

    public Preferences getPreferences() {
        try {
            if (preferences == null) {
                preferences = new Preferences(RMS_STORE_NAME);
            }
            return preferences;
        } catch (RecordStoreException e) {
            return null;
        }
    }

    private void goToScreen(int aScreenNr) {

        switch (aScreenNr) {
            case 0:
                // Play
                if(selectGameDisplay==null){
                    selectGameDisplay = new SelectGameDisplay(this);
                }
                selectGameDisplay.start();
                activeApp = selectGameDisplay;
                Display.getDisplay(this).setCurrent(activeApp);
                break;
            case 1:
                // Create
                if(createDisplay == null){
                    createDisplay = new CreateDisplay(this);
                }
                createDisplay.start();
                activeApp = createDisplay;
                Display.getDisplay(this).setCurrent(activeApp);
                break;
            case 2:
                // GPS
                /*if(gpsDisplay == null){
                    gpsDisplay = new GPSDisplay(this);
                }
                gpsDisplay.start();*/
                gpsDisplay = new GPSDisplay(this);
                Display.getDisplay(this).setCurrent(gpsDisplay);
                break;
            case 3:
                // Settings
                if (settingsDisplay == null) {
                    settingsDisplay = new SettingsDisplay(this);
                }
                Display.getDisplay(this).setCurrent(settingsDisplay);
                break;
            case 4:
                // Help
                if (helpDisplay == null) {
                    helpDisplay = new HelpDisplay(this);
                }
                Display.getDisplay(this).setCurrent(helpDisplay);
                break;
            case 5:
                // Quit
                Log.log("exit");
                /*if(splashDisplay == null){
                    splashDisplay = new SplashDisplay(this);
                }
                Display.getDisplay(this).setCurrent(splashDisplay);
                splashDisplay.start(SplashDisplay.STATE_SPLASH_EXIT);*/
                Display.getDisplay(this).setCurrent(new SplashDisplay(this, -1));
                break;
            case 6:
                if(isInDemoMode()){
                    Log.view(this);
                }
                break;
            case 7:
                /*if(isInDemoMode()){
                    Display.getDisplay(this).setCurrent(new TestDisplay(this));
                }*/
                break;
        }
    }

    private class VersionChecker implements CommandListener {
        private Command EXIT_CMD = new Command("Exit", Command.EXIT, 1);
        private Command CONTINUE_CMD = new Command("Continue at you own risk", Command.SCREEN, 1);
        private Command GET_CMD = new Command("Get new version", Command.SCREEN, 1);

        private Form form;

        public boolean check() {
            String myVersion = getAppProperty("MIDlet-Version");

            String myName = getAppProperty("MIDlet-Name");

            String versionURL = "http://" + midlet.getKWServer() + "/dist/version.html";
            try {
                String theirVersion = Util.getPage(versionURL);
                if (theirVersion != null) {
                    if (!theirVersion.trim().equals(myVersion)) {
                        //#style defaultscreen
                        form = new Form("Version check");

                        //#style alertinfo
                        form.append("Your " + myName + " version (" + myVersion + ") differs from the version (" + theirVersion + ") available for download. \nYou may want to upgrade to " + theirVersion);
                        form.addCommand(EXIT_CMD);
                        form.addCommand(GET_CMD);
                        form.addCommand(CONTINUE_CMD);

                        form.setCommandListener(this);
                        Display.getDisplay(midlet).setCurrent(form);
                        return false;
                    } else {
                        return true;
                    }
                } else {
                    return true;
                }
            } catch (Throwable t) {
                return true;
            }
        }

        public void commandAction(Command command, Displayable screen) {
            if (command == EXIT_CMD) {
                /*if(splashDisplay == null){
                    splashDisplay = new SplashDisplay(midlet);
                }
                splashDisplay.start(SplashDisplay.STATE_SPLASH_EXIT);*/
                Display.getDisplay(midlet).setCurrent(new SplashDisplay(midlet, 1));
            } else if (command == CONTINUE_CMD) {
                setHome();
            } else if (command == GET_CMD) {
                try {
                    midlet.platformRequest("http://" + midlet.getKWServer());
                    // and exit
                    /*if(splashDisplay == null){
                        splashDisplay = new SplashDisplay(midlet);
                    }
                    Display.getDisplay(midlet).setCurrent(splashDisplay);
                    splashDisplay.start(SplashDisplay.STATE_EXIT);*/
                    try {
                        midlet.destroyApp(true);
                        midlet.notifyDestroyed();
                    } catch (Throwable t) {
                        //
                    }
                } catch (Throwable t) {
                    //#style alertinfo
                    form.append("Could not get new version...sorry.");
                    Log.log(t.getMessage());
                }
            }
        }
    }
}
