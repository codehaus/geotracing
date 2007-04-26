package org.walkandplay.client.phone;

import de.enough.polish.ui.*;
import de.enough.polish.ui.Form;
import de.enough.polish.ui.TextField;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.media.control.VideoControl;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.MediaException;
import javax.microedition.media.PlayerListener;

/**
 * MobiTracer main GUI.
 *
 * @author Just van den Broecke
 * @version $Id: TraceScreen.java 254 2007-01-11 17:13:03Z just $
 */
public class TestDisplay extends Form implements CommandListener, ItemCommandListener, PlayerListener, Runnable {
    private Command BACK_CMD = new Command("Back", Command.BACK, 1);
    private Command VIEW_CMD = new Command("View", Command.ITEM, 1);
    private Command START_CMD = new Command("Play", Command.SCREEN, 1);
    private Command STOP_CMD = new Command("Stop", Command.SCREEN, 2);
    protected WPMidlet midlet;
    private Display display;
    protected Displayable prevScreen;
    private Player player;
    private String url = "http://test.mlgk.nl/wp/media.srv?id=26527";

    private int w = -1, h = -1;
	private Font f, fb;
    private StringItem msg = new StringItem("", "Starting up...");

    public TestDisplay(WPMidlet aMidlet) {
        //#style defaultscreen
        super("");
        midlet = aMidlet;
        prevScreen = Display.getDisplay(midlet).getCurrent();
        display = Display.getDisplay(midlet);

        append(msg);
        addCommand(BACK_CMD);
        addCommand(VIEW_CMD);
        addCommand(START_CMD);
        addCommand(STOP_CMD);

        //#style formbox
        append(new Gauge( null, false, Gauge.INDEFINITE, Gauge.CONTINUOUS_RUNNING ));
        append(new ClockItem(""));

        //#style labelinfo
        append("labelinfo");
        //#style textbox
        append(new TextField("", "", 32, TextField.ANY));

        /*StringItem si = new StringItem("", url, Item.HYPERLINK);
        si.setDefaultCommand(VIEW_CMD);
        si.setItemCommandListener(this);
        append(si);*/

        /*try{
            midlet.platformRequest(url);
        }catch(Throwable t){
            show(t.getMessage());
        }*/

        /*try{
            if(player != null && player.getState() == Player.PREFETCHED) {
                player.start();
            } else {
                defplayer();
            }
        }catch(Throwable t){
            show(t.getMessage());
        }*/
    }

    private void show(String aMsg){
        System.out.println(aMsg);
        msg.setText(aMsg);
    }

    public void commandAction(Command command, Displayable screen) {
        if(command == BACK_CMD){
            Display.getDisplay(midlet).setCurrent(prevScreen);
        }else if(command == START_CMD){
            start();
        }else if(command == STOP_CMD){
            stopPlayer();
        }else{
            show("unknown command:" + command);
        }
    }

    public void commandAction(Command command, Item anItem) {
        show("hit the item");

    }

   public void playerUpdate(Player player,
     String event, Object data) {
      if(event == PlayerListener.END_OF_MEDIA) {
         try {
            defplayer();
         }
         catch(MediaException me) {
             show(me.getMessage());
         }
         reset();
      }
   }

   public void start() {
      Thread t = new Thread(this);
      t.start();
   }

    void play(String url) {
      try {
         VideoControl vc;
         defplayer();
         // create a player instance
         player = Manager.createPlayer(url);
         player.addPlayerListener(this);
         // realize the player
         player.realize();
         vc = (VideoControl)player.getControl("VideoControl");
         if(vc != null) {
            //Item video = (Item)vc.initDisplayMode(vc.USE_GUI_PRIMITIVE, null);
            Item video = (Item)vc.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, null);
            Form v = new Form("Playing Video...");
            StringItem si = new StringItem("Status: ", "Playing...");
            v.append(si);
            v.append(video);
            display.setCurrent(v);
         }
         player.prefetch();
         player.start();
      }
      catch(Throwable t) {
          show(t.getMessage());
          reset();
      }
   }

    public void run() {
      play(getURL());
   }

   String getURL() {
     return url;
   }
    void defplayer() throws MediaException {
      if (player != null) {
         if(player.getState() == Player.STARTED) {
            player.stop();
         }
         if(player.getState() == Player.PREFETCHED) {
            player.deallocate();
         }
         if(player.getState() == Player.REALIZED ||
		    player.getState() == Player.UNREALIZED) {
            player.close();
         }
      }
      player = null;
   }

   void reset() {
      player = null;
   }

   void stopPlayer() {
      try {
         defplayer();
      }
      catch(MediaException me) {
          show(me.getMessage());
      }
      reset();
   }



}
