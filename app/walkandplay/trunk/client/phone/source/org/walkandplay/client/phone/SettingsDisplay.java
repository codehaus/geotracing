package org.walkandplay.client.phone;

import de.enough.polish.ui.ClockItem;
import de.enough.polish.util.Locale;
import org.geotracing.client.Util;

import javax.microedition.lcdui.*;

public class SettingsDisplay extends DefaultDisplay {
    private Command SOUND_CMD, MEDIAPLAYER_CMD, DEMO_CMD, MAP_CMD;
    private Command ACCOUNT_CMD = new Command(Locale.get("settings.Account"), Command.ITEM, 2);
    private Command VERSION_CMD = new Command(Locale.get("settings.Version"), Command.ITEM, 2);

    StringItem text = new StringItem("", "Choose settings from the menu to change");
    private Image logo;
    private AccountDisplay accountDisplay;
    private VersionDisplay versionDisplay;

    public SettingsDisplay(WPMidlet aMIDlet) {
        super(aMIDlet, "Settings");

        try {
            //#ifdef polish.images.directLoad
            logo = Image.createImage("/settings_icon_small.png");
            //#else
            logo = scheduleImage("/settings_icon_small.png");
            //#endif
        } catch (Throwable t) {
            Log.log("Could not load the images on SettingsDisplay");
        }

        append(logo);

        if (Util.hasSound()) {
            SOUND_CMD = new Command(Locale.get("settings.SoundOff"), Command.ITEM, 2);
        } else {
            SOUND_CMD = new Command(Locale.get("settings.SoundOn"), Command.ITEM, 2);
        }

        if (midlet.useExternalPlayer()) {
            MEDIAPLAYER_CMD = new Command(Locale.get("settings.MediaPlayerExtern"), Command.ITEM, 2);
        } else {
            MEDIAPLAYER_CMD = new Command(Locale.get("settings.MediaPlayerIntern"), Command.ITEM, 2);
        }

        if (midlet.isInDemoMode()) {
            DEMO_CMD = new Command(Locale.get("settings.DemoModeOff"), Command.ITEM, 2);
        } else {
            DEMO_CMD = new Command(Locale.get("settings.DemoModeOn"), Command.ITEM, 2);
        }

        if (midlet.useGoogleMaps()) {
            MAP_CMD = new Command(Locale.get("settings.SwitchToGeodan"), Command.ITEM, 2);
        } else {
            MAP_CMD = new Command(Locale.get("settings.SwitchToGoogleMaps"), Command.ITEM, 2);
        }

        //#style formbox
        append(text);

        ClockItem clock = new ClockItem("");
        //#style labelinfo
        append(clock);

        setCommands();
        Display.getDisplay(midlet).setCurrent(this);
    }

    private void setCommands() {
        addCommand(ACCOUNT_CMD);
        addCommand(SOUND_CMD);
        addCommand(MEDIAPLAYER_CMD);
        addCommand(DEMO_CMD);
        addCommand(VERSION_CMD);
        addCommand(MAP_CMD);
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == SOUND_CMD) {
            Util.toggleSound();
            if (Util.hasSound()) {
                SOUND_CMD = new Command(Locale.get("settings.SoundOff"), Command.ITEM, 2);
            } else {
                SOUND_CMD = new Command(Locale.get("settings.SoundOn"), Command.ITEM, 2);
            }
            removeAllCommands();
            setCommands();
        } else if (cmd == MAP_CMD) {
            if (midlet.useGoogleMaps()) {
                midlet.getPreferences().put(WPMidlet.WMS_URL, midlet.getAppProperty(WPMidlet.GEODAN_WMS_URL));
                MAP_CMD = new Command(Locale.get("settings.SwitchToGoogleMaps"), Command.ITEM, 2);
            } else {
                midlet.getPreferences().put(WPMidlet.WMS_URL, midlet.getAppProperty(WPMidlet.GOOGLE_WMS_URL));
                MAP_CMD = new Command(Locale.get("settings.SwitchToGeodan"), Command.ITEM, 2);
            }
            removeAllCommands();
            setCommands();
        } else if (cmd == MEDIAPLAYER_CMD) {
            removeCommand(MEDIAPLAYER_CMD);
            if (midlet.useExternalPlayer()) {
                midlet.getPreferences().put(WPMidlet.EXTERNAL_PLAYER, "false");
                MEDIAPLAYER_CMD = new Command(Locale.get("settings.MediaPlayerIntern"), Command.ITEM, 2);
            } else {
                midlet.getPreferences().put(WPMidlet.EXTERNAL_PLAYER, "true");
                MEDIAPLAYER_CMD = new Command(Locale.get("settings.MediaPlayerExtern"), Command.ITEM, 2);
            }
            removeAllCommands();
            setCommands();
        } else if (cmd == DEMO_CMD) {
            removeCommand(DEMO_CMD);
            if (midlet.isInDemoMode()) {
                midlet.getPreferences().put(WPMidlet.DEMO_MODE, "false");
                midlet.setTitle(false);
                DEMO_CMD = new Command(Locale.get("settings.DemoModeOn"), Command.ITEM, 2);
            } else {
                midlet.getPreferences().put(WPMidlet.DEMO_MODE, "true");
                midlet.setTitle(true);
                DEMO_CMD = new Command(Locale.get("settings.DemoModeOff"), Command.ITEM, 2);
            }
            removeAllCommands();
            setCommands();
        } else if (cmd == ACCOUNT_CMD) {
            if (accountDisplay == null) {
                accountDisplay = new AccountDisplay(midlet);
            }
            accountDisplay.start();
        } else if (cmd == VERSION_CMD) {
            if (versionDisplay == null) {
                versionDisplay = new VersionDisplay(midlet);
            }
            versionDisplay.start(this);
        }
    }


}
