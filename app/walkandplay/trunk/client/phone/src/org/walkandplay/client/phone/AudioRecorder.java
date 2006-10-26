package org.walkandplay.client.phone;

import org.geotracing.client.Util;
import org.geotracing.client.Net;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import nl.justobjects.mjox.JXElement;

public class AudioRecorder {
    private RecordControl recordControl;
    private Player player;
    private ByteArrayOutputStream output;
    private int rate, bits;
    private String name = "untitled";
    private byte[] data;
    private long startTime;
    private static final String mime = "audio/x-wav";

    public AudioRecorder(Player aPlayer, RecordControl aControl, int theRate, int theBits){
        player = aPlayer;
        recordControl = aControl;
        rate = theRate;
        bits = theBits;
    }

    public void create() {
        try {
            player = Manager.createPlayer("capture://audio?rate=" + rate + "&bits=" + bits);
            player.realize();
            recordControl =
                    (RecordControl) player.getControl("RecordControl");
            output = new ByteArrayOutputStream();
            recordControl.setRecordStream(output);
        } catch (Throwable t) {
            System.out.println("Exception initialising audiorecorder : " + t);
        }
    }

    private void close() {
        try {
            // close the player and videocontrol
            if (player != null) {
                player.stop();
                player.close();
                player = null;
            }

            if (recordControl != null) {
                recordControl = null;
            }
        } catch (MediaException me) {
            System.out.println("Exception closing the audiorecorder: " + me.toString());
        }
    }

    public void play() {
		if (data == null) {
			return;
		}

		try {
			Manager.createPlayer(new ByteArrayInputStream(data), mime).start();
		} catch (Throwable t) {
			System.out.println("Cannot play audio");
		}
	}

    public void start() {
		try {
			recordControl.startRecord();
			player.start();
			startTime = Util.getTime();

			new Thread(new Runnable() {
				public void run() {
					try {
						while (player != null) {
							Thread.sleep(1000);
                            // TODO : try Gamecanvas write to msgbar
						}
					} catch (Throwable t) {
                        System.out.println("Problem sleeping.");
					}
				}
			}).start();
		} catch (Exception e) {
			System.out.println("Cannot start the player. Maybe audio recording is not supported.");
        }
	}

    public void stop() {
		try {
			recordControl.commit();
			player.close();
			data = output.toByteArray();
		} catch (Exception e) {
			System.out.println("Cannot stop recording");
        } finally {
			close();
		}
	}

    public boolean upload(){
        JXElement rsp = Net.getInstance().uploadMedium(name, "audio", mime, startTime, data, false);
        if(rsp == null || rsp.getTag().indexOf("nrsp")!=-1){
            return false;
        }
        return true;
    }
}
