package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

public class AudioDisplay extends DefaultDisplay implements PlayerListener {
    private Command BACK_CMD = new Command("Back", Command.BACK, 1);
    private Command REPLAY_CMD = new Command("Replay", Command.SCREEN, 1);
    private Player player;
    private Gauge progressBar = new Gauge("", false, 100, 0);
    private StringItem state = new StringItem("", "");
    private Form form;
    private String name;
    private String audioUrl;


    public AudioDisplay(WPMidlet aMidlet, Displayable aPrevScreen) {
        super(aMidlet, "Media");
        midlet = aMidlet;
        prevScreen = aPrevScreen;
    }

    public void playerUpdate(Player aPlayer, String anEvent, Object theDate) {
        form.setTitle("Audio: " + name + " [" + anEvent + "]");
        //state.setText(anEvent);
    }

    public void play(String aName, String aUrl) {
        try {
            // if it's audio we already downloaded - just start playing
            if (audioUrl != null && audioUrl.equals(aUrl)) {
                Log.log("Already downloaded this one");
                //#style alertinfo
                form.append("Already downloaded - so play!");
                Display.getDisplay(midlet).setCurrent(form);
                player.start();
            } else {
                // start fresh
                deleteAll();

                audioUrl = aUrl;

                name = aName;
                //#style defaultscreen
                form = new Form("Audio: " + name + " [downloading]");
                //#style labelinfo
                form.append(aName);
                /*//#style labelinfo
                form.append(state);*/
                //#style labelinfo
                form.append(progressBar);

                form.addCommand(BACK_CMD);
                form.setCommandListener(this);
                Display.getDisplay(midlet).setCurrent(form);

                progressBar.setValue(25);

                //#style formbox
                form.append(audioUrl);

                player = Manager.createPlayer(audioUrl);
                player.addPlayerListener(this);
                progressBar.setValue(40);

                new Thread(new Runnable() {
                    public void run() {
                        try {
                            player.prefetch();
                            progressBar.setValue(80);
                            player.start();
                            progressBar.setValue(100);
                            form.addCommand(REPLAY_CMD);
                        } catch (Throwable t) {
                            //#style alertinfo
                            form.append(t.getMessage());
                            Log.log(t.getMessage());
                            state.setText("Error:" + t.getMessage());
                            audioUrl = null;
                            removeCommand(REPLAY_CMD);
                        }
                    }
                }).start();
            }
        } catch (Throwable t) {
            //#style alertinfo
            form.append(t.getMessage());
            Log.log(t.getMessage());
            state.setText("Error:" + t.getMessage());
            audioUrl = null;
            removeCommand(REPLAY_CMD);
        }
    }

    public void commandAction(Command command, Displayable screen) {
        if (command == BACK_CMD) {
            try {
                if (player != null) {
                    player.stop();
                    player.deallocate();
                    player.close();
                    player = null;
                }
            } catch (Throwable t) {
                // nada
            }
            Display.getDisplay(midlet).setCurrent(prevScreen);
        } else if (command == REPLAY_CMD) {
            try {
                player.start();
            } catch (Throwable t) {
                //#style alertinfo
                form.append("Could not replay audio");
            }
        }
    }
}
