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
import nl.justobjects.mjox.XMLChannelListener;
import nl.justobjects.mjox.XMLChannel;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;

import org.geotracing.client.Net;
import org.geotracing.client.Preferences;

import java.util.Vector;

public class WPMidlet extends MIDlet implements CommandListener, XMLChannelListener {

    List menuScreen;
    PlayDisplay playDisplay;
    TraceDisplay traceDisplay;
    private boolean playMode;

    private JXElement game;
    private JXElement gameRound;
    private int gamePlayId;
    private TCPClient kwClient;

    public WPMidlet() {
        super();
        setHome();
        connect();
        //Display.getDisplay(this).setCurrent(new SplashCanvas(this, 1));
    }

    private void connect(){
        try{
            Preferences prefs = new Preferences(Net.RMS_STORE_NAME);

			String user = prefs.get("kw-user", getAppProperty("kw-user"));
			String password = prefs.get("kw-password", getAppProperty("kw-password"));
			String server = prefs.get("kw-server", getAppProperty("kw-server"));
			String port = prefs.get("kw-port", getAppProperty("kw-port"));

			kwClient = new TCPClient(server, Integer.parseInt(port));
            kwClient.setListener(this);
            kwClient.login(user, password);
        }catch(Throwable t){
            Log.log("connect exception:" + t.getMessage());
        }
    }

    public void sendRequest(JXElement aRequest){
        try{
            kwClient.utopia(aRequest);
        }catch(Throwable t){
            Log.log("Exception sending " + new String(aRequest.toBytes(false)));
        }
    }

    public void accept(XMLChannel anXMLChannel, JXElement aResponse) {
        Log.log("** received:" + new String(aResponse.toBytes(false)));
        String tag = aResponse.getTag();
        if(tag.equals("login-rsp")){
            try{
                Log.log("send select app");
                kwClient.setAgentKey(aResponse);
                kwClient.selectApp("geoapp", "user");
            }catch(Throwable t){
                Log.log("Selectapp failed:" + t.getMessage());
            }
        }else if(tag.equals("select-app-rsp")){
            // now stop listening here
            // kwClient.setListener(null);
        }
    }

    public void onStop(XMLChannel anXMLChannel, String aReason) {
        Log.log("XMLChannel stopped");
        try{
            kwClient.restart();
        }catch(Throwable t){
            Log.log("Exception restarting the XMLchannel");
        }
    }

    public void setListener(XMLChannelListener aListener){
        kwClient.setListener(aListener);
    }

    public void setHome() {
        //#style mainScreen
        menuScreen = new List("Mobile Learning Game Kit", List.IMPLICIT);        
        //#style mainTraceCommand
        menuScreen.append(Locale.get("menu.Trace"), null);
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
        //#style mainLogCommand
        menuScreen.append("video display", null);
        //#style mainLogCommand
        menuScreen.append("video form", null);
        //#style mainLogCommand
        menuScreen.append("GPS test display", null);

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
                // Play
                Display.getDisplay(this).setCurrent(new SelectGameDisplay(this));
                break;
            case 2:
                // GPS
                if (traceDisplay != null) traceDisplay.stop();
                Display.getDisplay(this).setCurrent(new GPSDisplay(this));
                break;
            case 3:
                // Settings
                Display.getDisplay(this).setCurrent(new SettingsDisplay(this));
                break;
            case 4:
                // Help
                Display.getDisplay(this).setCurrent(new HelpDisplay(this));
                break;
            case 5:
                // Quit
                Display.getDisplay(this).setCurrent(new SplashCanvas(this, -1));
                //notifyDestroyed();
                break;
            case 6:
                // Log
                Log.view(this);
                break;
            case 7:
                // test display
                Display.getDisplay(this).setCurrent(new TestDisplay(this));
                break;
            case 8:
                // video canvas
                Display.getDisplay(this).setCurrent(new VideoDisplay(this, "http://test.mlgk.nl/wp/media.srv?id=54225", null));
                break;
            case 9:
                // video form
                Display.getDisplay(this).setCurrent(new VideoForm(this, "http://test.mlgk.nl/wp/media.srv?id=54225"));
                break;
            case 10:
                // gps test display
                Display.getDisplay(this).setCurrent(new GPSTestDisplay(this));
                break;
        }
    }

}
