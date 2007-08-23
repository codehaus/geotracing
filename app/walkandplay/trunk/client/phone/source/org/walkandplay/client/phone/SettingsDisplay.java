package org.walkandplay.client.phone;

import de.enough.polish.ui.ClockItem;
import de.enough.polish.util.Locale;
import org.geotracing.client.Util;

import javax.microedition.lcdui.*;

public class SettingsDisplay extends DefaultDisplay {
    private Command SOUND_CMD;
    private Command MEDIAPLAYER_CMD;
    private Command ACCOUNT_CMD = new Command(Locale.get("settings.Account"), Command.ITEM, 2);
    private Command VERSION_CMD = new Command(Locale.get("settings.Version"), Command.ITEM, 2);

    StringItem text = new StringItem("", "Choose settings from the menu to change");
    private Image logo;

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

        if (midlet.useInternalMediaPlayer()) {
            MEDIAPLAYER_CMD = new Command(Locale.get("settings.MediaPlayerIntern"), Command.ITEM, 2);
        } else {
            MEDIAPLAYER_CMD = new Command(Locale.get("settings.MediaPlayerExtern"), Command.ITEM, 2);
        }

        //#style formbox
        append(text);

        ClockItem clock = new ClockItem("");
        //#style labelinfo
        append(clock);

        setCommands();
    }

    private void setCommands(){
        addCommand(ACCOUNT_CMD);
        addCommand(SOUND_CMD);
        addCommand(MEDIAPLAYER_CMD);
        addCommand(VERSION_CMD);
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (cmd == BACK_CMD) {
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (cmd == SOUND_CMD) {
            Util.toggleSound();
            if(Util.hasSound()){
                SOUND_CMD = new Command(Locale.get("settings.SoundOff"), Command.ITEM, 2);
            }else{
                SOUND_CMD = new Command(Locale.get("settings.SoundOn"), Command.ITEM, 2);
            }
            removeAllCommands();
            setCommands();
        } else if (cmd == MEDIAPLAYER_CMD) {
            removeCommand(MEDIAPLAYER_CMD);
            if (midlet.useInternalMediaPlayer()) {
                midlet.getPreferences().put(WPMidlet.MEDIA_PLAYER, WPMidlet.EXTERN);
                MEDIAPLAYER_CMD = new Command(Locale.get("settings.MediaPlayerExtern"), Command.ITEM, 2);
            }else{
                midlet.getPreferences().put(WPMidlet.MEDIA_PLAYER, WPMidlet.INTERN);
                MEDIAPLAYER_CMD = new Command(Locale.get("settings.MediaPlayerIntern"), Command.ITEM, 2);
            }
            removeAllCommands();
            setCommands();
        } else if (cmd == ACCOUNT_CMD) {
            new AccountDisplay(midlet);
        } else if (cmd == VERSION_CMD) {
            new VersionDisplay(midlet, this);
        }
    }


}
