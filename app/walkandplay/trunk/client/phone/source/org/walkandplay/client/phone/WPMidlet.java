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

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import de.enough.polish.util.Locale;

//#ifdef polish.debugEnabled
import de.enough.polish.util.Debug;

import java.io.IOException;
//#endif

/**
 * <p>Shows a demonstration of the possibilities of J2ME Polish.</p>
 * <p/>
 * <p>Copyright Enough Software 2004, 2005</p>
 * <p/>
 * <pre>
 * history
 *        04-Apr-2004 - rob creation
 * </pre>
 *
 * @author Robert Virkus, j2mepolish@enough.de
 */
public class WPMidlet extends MIDlet implements CommandListener {

    List menuScreen;
    //#ifdef polish.debugEnabled
    Command showLogCmd = new Command(Locale.get("cmd.ShowLog"), Command.ITEM, 9);
    //#endif
    public Display display;

    public WPMidlet() {
        super();
        //#debug
        System.out.println("starting MenuMidlet");

        //#style mainScreen
        menuScreen = new List(null, List.IMPLICIT);

        //#style mainTraceCommand
        menuScreen.append(Locale.get("menu.Trace"), null);
        //#style mainFindCommand
        menuScreen.append(Locale.get("menu.Find"), null);
        //#style mainPlayCommand
        menuScreen.append(Locale.get("menu.Play"), null);
        //#style mainGPSCommand
        menuScreen.append(Locale.get("menu.GPS"), null);
        //#style mainSettingsCommand
        menuScreen.append(Locale.get("menu.Settings"), null);
        //#style mainHelpCommand
        menuScreen.append(Locale.get("menu.Help"), null);
        //#style mainQuitCommand
        menuScreen.append(Locale.get("menu.Quit"), null);

        menuScreen.setCommandListener(this);
        //#ifdef polish.debugEnabled
        menuScreen.addCommand(showLogCmd);
        //#endif

        // You can also use further localization features like the following:
        //System.out.println("Today is " + Locale.formatDate( System.currentTimeMillis() ));

        //#debug
        System.out.println("initialisation done.");
    }

    protected void startApp() throws MIDletStateChangeException {
        //#debug
        System.out.println("setting display.");
        display = Display.getDisplay(this);
        display.setCurrent(menuScreen);
        //#debug
        System.out.println("sample application is up and running.");
    }

    protected void pauseApp() {
        // ignore
    }

    protected void destroyApp(boolean unconditional) throws MIDletStateChangeException {
        // just quit
    }

    public void commandAction(Command cmd, Displayable screen) {
        if (screen == this.menuScreen) {
            //#ifdef polish.debugEnabled
            if (cmd == showLogCmd) {
                Debug.showLog(display);
                return;
            }
            //#endif
            if (cmd == List.SELECT_COMMAND) {
                goToScreen(menuScreen.getSelectedIndex());
            }
        }
    }

    private void goToScreen(int aScreenNr) {
        switch (aScreenNr) {
            case 0:
                // Trace
                TraceScreen traceScreen = new TraceScreen(this);
                Display.getDisplay(this).setCurrent(traceScreen);
                break;
            case 1:
                // Find
                break;
            case 2:
                // Play
                break;
            case 3:
                // GPS
                break;
            case 4:
                // Settings
                break;
            case 5:
                // Help
                Display.getDisplay(this).setCurrent(new HelpScreen(this));
                break;
            case 6:
                // Quit
                quit();
                break;
        }
    }

    private void quit() {
        notifyDestroyed();
    }

}
