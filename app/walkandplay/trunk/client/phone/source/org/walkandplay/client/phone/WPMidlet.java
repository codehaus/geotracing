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
import nl.justobjects.mjox.JXElement;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

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
    PlayDisplay playDisplay;
    TraceDisplay traceDisplay;
    private boolean playMode;

    private JXElement game;
    private JXElement gameRound;
    private int gamePlayId;

    public WPMidlet() {
        super();
        setHome();
        //Display.getDisplay(this).setCurrent(new SplashCanvas(this, 1));        
    }

    public void setHome() {
        //#style mainScreen
        //menuScreen = new List("Mobile Learning Game Kit", List.IMPLICIT);
        menuScreen = new List("", List.IMPLICIT);
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
        //#style mainLogCommand
        menuScreen.append(Locale.get("menu.Log"), null);
        //#style mainLogCommand
        menuScreen.append("test display", null);
        /*//#style mainLogCommand
        menuScreen.append("video canvas", null);*/
        menuScreen.setCommandListener(this);
        Display.getDisplay(this).setCurrent(menuScreen);

    }

    public void setPlayMode(boolean aMode) {
        playMode = aMode;
    }

    public boolean getPlayMode() {
        return playMode;
    }

    public void setGame(JXElement aGame) {
        game = aGame;
    }

    public JXElement getGame() {
        return game;
    }

    public void setGameRound(JXElement aGameRound) {
        gameRound = aGameRound;
    }

    public JXElement getGameRound() {
        return gameRound;
    }

    public void setGamePlayId(int anId) {
        gamePlayId = anId;
    }

    public int getGamePlayId() {
        return gamePlayId;
    }

    protected void startApp() throws MIDletStateChangeException {
        Display.getDisplay(this).setCurrent(menuScreen);
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

    private void goToScreen(int aScreenNr) {

        switch (aScreenNr) {
            case 0:
                // Trace
                traceDisplay = new TraceDisplay(this);
                Display.getDisplay(this).setCurrent(traceDisplay);
                traceDisplay.start();
                break;
            case 1:
                // Find
                Display.getDisplay(this).setCurrent(new FindDisplay(this));
                break;
            case 2:
                // Play
                Display.getDisplay(this).setCurrent(new SelectGameDisplay(this));
                break;
            case 3:
                // GPS
                if (traceDisplay != null) traceDisplay.stop();
                Display.getDisplay(this).setCurrent(new GPSDisplay(this));
                break;
            case 4:
                // Settings
                Display.getDisplay(this).setCurrent(new SettingsDisplay(this));
                break;
            case 5:
                // Help
                Display.getDisplay(this).setCurrent(new HelpDisplay(this));
                break;
            case 6:
                // Quit
                Display.getDisplay(this).setCurrent(new SplashCanvas(this, -1));
                //notifyDestroyed();
                break;
            case 7:
                // Log
                Log.view(this);
                break;
            case 8:
                // test display
                Display.getDisplay(this).setCurrent(new TestDisplay(this));
                break;
            case 9:
                // video canvas 1
                Display.getDisplay(this).setCurrent(new VideoDisplay(this, "http://test.mlgk.nl/wp/media.srv?id=54225", true));
                break;
        }
    }

}
