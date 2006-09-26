// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;

import javax.microedition.lcdui.*;
import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;
import javax.microedition.midlet.MIDlet;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.keyworx.mclient.Protocol;


public class AudioCapture extends Form implements CommandListener {

	private Command back, play, start, stop, submit;
	private Player player;
	private RecordControl recordcontrol;
	private ByteArrayOutputStream output;
	private MIDlet midlet;
	private Displayable prevScreen;
	private byte[] audioData;
	private TextField name = new TextField("Enter Recording Name", null, 24, TextField.ANY);
	private static final String MIME = "audio/x-wav";
	private long startTime;
	private int rate, bits;
	final int kbPerSec;

	public AudioCapture(MIDlet aMidlet) {
		super("Audio Recorder");
		midlet = aMidlet;
		rate = Integer.parseInt(midlet.getAppProperty("audio-rate"));
		bits = Integer.parseInt(midlet.getAppProperty("audio-bits"));
		kbPerSec = (rate * bits / 8) / 1000;
		prevScreen = Display.getDisplay(midlet).getCurrent();
		back = new Command("Back", Command.BACK, 0);
		start = new Command("Start", Command.OK, 1);
		stop = new Command("Stop", Command.OK, 1);
		submit = new Command("Submit", Command.OK, 1);
		play = new Command("Play", Command.SCREEN, 1);
		addCommand(back);

		setCommandListener(this);

		try {
			player = Manager.createPlayer("capture://audio?rate=" + rate + "&bits=" + bits);
			player.realize();
			recordcontrol =
					(RecordControl) player.getControl("RecordControl");
			output = new ByteArrayOutputStream();
			recordcontrol.setRecordStream(output);
			addCommand(start);
			append("AUDIO RECORDER", " Use the Start menu to start recording");
			append("Use Stop in menu to stop recording");
			append("\nSettings: " + rate / 1000 + "kHz " + bits + " bits " + kbPerSec + " kb/sec");
		} catch (Exception e) {
			Util.showAlert(midlet, "Error", "Cannot create player. Maybe audio (MMAPI) is not supported.");
			back();
		}
	}

	public int append(String s) {
		return append(null, s);
	}

	public int append(String label, String s) {
		StringItem si = new StringItem(label, s);
		si.setLayout(Item.LAYOUT_NEWLINE_BEFORE);
		return super.append(si);
	}

	public void commandAction(Command c, Displayable d) {
		deleteAll();
		if (c == back) {
			audioData = null;
			back();

		} else if (c == start) {
			removeCommand(start);
			record();
			addCommand(stop);

		} else if (c == stop) {
			removeCommand(stop);
			stop();
			addCommand(play);
			addCommand(submit);
			append("OK Recording stopped\n duration=" + (System.currentTimeMillis() - startTime) / 1000 + " seconds\n size=" + audioData.length / 1024 + "kb\n");
			append(name);
			append("press Play to hear recording or Submit to upload");
		} else if (c == submit) {

			if (audioData == null) {
				append("no audio data recorded");
				return;
			}
			append("SUBMITTING AUDIO...", " (takes a while)");

			JXElement rsp = Net.getInstance().uploadMedium(name.getString(), "audio", MIME, audioData, false);
			if (rsp == null) {
				append("cannot submit audio !");
			} else if (Protocol.isPositiveResponse(rsp)) {
				append("submit audio OK, press Back");
			} else {
				append("submit audio failed: error is " + rsp.getAttr("error") + " press Back");
			}

			removeCommand(submit);
		} else if (c == play) {
			play();
		}
	}


	private void back() {
		Display.getDisplay(midlet).setCurrent(prevScreen);
	}


	private void play() {
		if (audioData == null) {
			append("no audio data recorded");
			return;
		}

		append("PLAYING AUDIO...", "");

		try {
			Manager.createPlayer(new ByteArrayInputStream(audioData), MIME).start();
		} catch (Throwable t) {
			Util.showAlert(midlet, "Error", "Cannot play audio");
		}
	}


	private void record() {
		try {
			recordcontrol.startRecord();
			player.start();
			startTime = System.currentTimeMillis();
			append("RECORDING AUDIO...", " press Stop to stop recording\n");

			new Thread(new Runnable() {
				public void run() {
					int seconds = 0;

					StringItem status = new StringItem("STATUS", "0 secs 0 kb");
					status.setLayout(Item.LAYOUT_CENTER | Item.LAYOUT_VCENTER);
					append(status);
					try {
						while (player != null) {
							Thread.sleep(1000);
							seconds++;
							status.setText(seconds + " secs " + (seconds * kbPerSec) + " kb");
						}
					} catch (Throwable t) {

					}
				}
			}).start();
		} catch (Exception e) {
			Util.showAlert(midlet, "Error",
					"Cannot start the player. Maybe audio recording is not supported.");
			back();
		}
	}

	private void stop() {
		append("Stopping recording...");
		try {
			recordcontrol.commit();
			player.close();
			audioData = output.toByteArray();
		} catch (Exception e) {
			Util.showAlert(midlet, "Error", "Cannot stop recording");
			back();
		} finally {
			player = null;
		}
	}
}
