package org.walkandplay.client.phone;

import org.geotracing.client.Util;
import org.geotracing.client.Net;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.media.Player;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.control.RecordControl;
import javax.microedition.media.control.VideoControl;
import java.util.Timer;
import java.util.TimerTask;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import nl.justobjects.mjox.JXElement;

public class MediaCanvas extends DefaultCanvas {
	private Player player;
	private VideoControl videoControl;
	private RecordControl recordControl;
	//private PhotoCamera photoCamera;
	//private AudioRecorder audioRecorder;
	private byte[] dataPhoto;
	private Image previewPhoto;
	private long time;
	private static final String mimePhoto = "image/jpeg";

	private ByteArrayOutputStream outputAudio;
	private int rate, bits;
	private byte[] dataAudio;
	private long startTime;
	private static final String mimeAudio = "audio/x-wav";
	private static final String mimeText = "text/plain";

	// Text objects
	String textType;

	private String errorMsg;

	private String inputText = "";
	private String titleText = "";
	private String tagText = "";
	public static final String[] keys = {" 0", ".,-!?@:;1", "aAbBcC2", "dDeEfF3", "gGhHiI4", "jJkKlL5", "mMnNoO6", "pPqQrRsS7", "tTuUvV8", "wWxXyYzZ9"};
	Timer keyTimer;
	int keyMajor = -1;
	int keyMinor;

	private boolean showMenu;
	private boolean captured;
	private boolean recorded;
	private boolean recording;
	private boolean playing;

	private int mediumType = 1;
	private final static int TEXT = 1;
	private final static int PHOTO = 2;
	private final static int AUDIO = 3;


	// image objects
	private Image iconOverlay, inputBox, okBt, addTagBt, shootBt, recordBt, topWhiteArea, middleWhiteArea, bottomWhiteArea;

	// icon buttons
	private Image[] icons = new Image[3];

	// screenstates
	private int screenStat = 2;
	private final static int PHOTO_STAT = 0;
	private final static int AUDIO_STAT = 1;
	private final static int TEXT_STAT = 2;
	private final static int TEXT_INPUT_STAT = 3;
	private final static int TAG_STAT = 4;

	private int fontType = Font.FACE_MONOSPACE;

	// tag canvas states
	private boolean tagCloudSelected;
	private boolean titleBoxSelected;
	private boolean tagBoxSelected;

	private int selectedTag = -1;
	private String myTags = "";
	private String[] tagCloud = {"tag1", "tag2", "tag3", "tag4"};

	public MediaCanvas(WP aMidlet) {
		super(aMidlet);
		try {
			setFullScreenMode(true);

			rate = Integer.parseInt(midlet.getAppProperty("audio-rate"));
			bits = Integer.parseInt(midlet.getAppProperty("audio-bits"));

			// load all images
			backBt = Image.createImage("/back_button.png");

			icons[0] = Image.createImage("/poi_icon_small.png");
			icons[1] = Image.createImage("/photo_icon_small.png");
			icons[2] = Image.createImage("/movie_icon_small.png");
			//icons[3] = Image.createImage("/movie_icon_small.png");
			//icons[4] = Image.createImage("/movie_icon_small.png");

			iconOverlay = Image.createImage("/icon_overlay_small.png");
			inputBox = Image.createImage("/inputbox.png");
			okBt = Image.createImage("/ok_button.png");
			shootBt = Image.createImage("/shoot_button.png");
			recordBt = Image.createImage("/record_button.png");
			addTagBt = Image.createImage("/addtag_button.png");
			topWhiteArea = Image.createImage("/whitearea_1.png");
			middleWhiteArea = Image.createImage("/whitearea_2.png");
			bottomWhiteArea = Image.createImage("/whitearea_3.png");

			//photoCamera = new PhotoCamera(player, videoControl);
			//audioRecorder = new AudioRecorder(player, recordControl, rate, bits);
		} catch (Throwable t) {
			log("could not load all images : " + t.toString());
		}
	}

	private void drawTags(Graphics aGraphics, int anXOffset, int anYOffset) {
		int lineLength = 0;
		int ypos = anYOffset;
		for (int i = 0; i < tagCloud.length; i++) {
			String txt = tagCloud[i];
			int length = lineLength + f.stringWidth(txt) + margin;
			if (length > w) {
				ypos += fh;
				// start at the beginning again
				lineLength = 0;
			}
			if (selectedTag == i) {
				f = Font.getFont(fontType, Font.STYLE_UNDERLINED, Font.SIZE_SMALL);
				aGraphics.setFont(f);
				//txt = "*" + txt + "*";
				aGraphics.drawString(txt, anXOffset + lineLength + 1, ypos, Graphics.TOP | Graphics.LEFT);
				f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
				aGraphics.setFont(f);
			} else {
				aGraphics.drawString(txt, anXOffset + lineLength, ypos, Graphics.TOP | Graphics.LEFT);
			}
			// after placing
			lineLength += f.stringWidth(txt);
		}
	}

	private void addTag() {
		// first check if it's already added
		if (tagText.indexOf(tagCloud[selectedTag]) != -1) return;

		if (tagText.length() == 0) {
			tagText += tagCloud[selectedTag];
		} else {
			tagText += " " + tagCloud[selectedTag];
		}
	}

	private void selectFirstTag() {
		selectedTag = 0;
	}

	private void deselectTagCloud() {
		selectedTag = -1;
	}

	private void selectNextTag() {
		if (tagCloud != null) {
			if (selectedTag == (tagCloud.length - 1)) {
				selectedTag = 0;
			} else {
				selectedTag++;
			}
		}
	}

	private void selectPrevTag() {
		if (tagCloud != null) {
			if (selectedTag == 0) {
				selectedTag = (tagCloud.length - 1);
			} else {
				selectedTag--;
			}
		}
	}

	/**
	 * Draws the screen.
	 *
	 * @param g The graphics object.
	 */
	public void paint(Graphics g) {
		super.paint(g);
		switch (screenStat) {
			case PHOTO_STAT:
				g.setColor(0, 0, 0);
				f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
				g.setFont(f);

				ScreenUtil.drawIcons(g, w, (w - 2 * margin - middleTextArea.getWidth()) / 2, margin + logo.getHeight() + margin, icons, iconOverlay);

				if (captured) {
					g.drawImage(getPhotoPreview(), (w - 2 * margin - middleTextArea.getWidth()) / 2, margin + logo.getHeight() + icons[0].getHeight() + 2 * margin, Graphics.TOP | Graphics.LEFT);
					ScreenUtil.drawLeftSoftKey(g, h, okBt, margin);
				} else {
					g.drawRect((w - 2 * margin - middleTextArea.getWidth()) / 2, margin + logo.getHeight() + icons[0].getHeight() + 2 * margin, 160, 120);
					showCam((w - 2 * margin - middleTextArea.getWidth()) / 2, margin + logo.getHeight() + 2 * margin, 160, 120);
					//initPhotoCam((w - 2*margin - middleTextArea.getWidth())/2, margin + logo.getHeight() + 2 * margin, 160, 120);
					ScreenUtil.drawLeftSoftKey(g, h, shootBt, margin);
				}

				break;
			case TAG_STAT:
				ScreenUtil.drawTextArea(g, 100, (w - 2 * margin - middleTextArea.getWidth()) / 2, 3 * margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);

				String keySelect = "";
				if (keyMajor != -1) {
					String all = keys[keyMajor];
					keySelect = all.substring(0, keyMinor) + "[" + all.charAt(keyMinor) + "]" + all.substring(keyMinor + 1);
				}

				g.drawString("add a title", (w - middleTextArea.getWidth()) / 2, 4 * margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
				g.drawImage(inputBox, (w - middleTextArea.getWidth()) / 2, fh + 4 * margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
				g.drawString(titleText, (w - middleTextArea.getWidth() + 4) / 2, fh + 2 + 4 * margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
				g.drawString("and some tags", (w - middleTextArea.getWidth()) / 2, fh + inputBox.getHeight() + 4 * margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
				g.drawImage(inputBox, (w - middleTextArea.getWidth()) / 2, 2 * fh + inputBox.getHeight() + 4 * margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
				g.drawString(tagText, (w - middleTextArea.getWidth() + 4) / 2, 2 * fh + 2 + inputBox.getHeight() + 4 * margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
				g.drawString(keySelect, (w - middleTextArea.getWidth()) / 2, 2 * fh + 2 * inputBox.getHeight() + 4 * margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);

				drawTags(g, (w - middleTextArea.getWidth()) / 2, 4 * fh + 2 * inputBox.getHeight() + 4 * margin + logo.getHeight() + iconOverlay.getHeight());

				if (tagCloudSelected) {
					ScreenUtil.drawLeftSoftKey(g, h, addTagBt, margin);
				} else {
					ScreenUtil.drawLeftSoftKey(g, h, okBt, margin);
				}
				break;
			case AUDIO_STAT:
				ScreenUtil.drawIcons(g, w, (w - 2 * margin - middleTextArea.getWidth()) / 2, 2 * margin + logo.getHeight(), icons, iconOverlay);
				ScreenUtil.drawTextArea(g, 100, (w - 2 * margin - middleTextArea.getWidth()) / 2, 3 * margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);

				if (recording) {
					g.drawString("recording", (w - middleTextArea.getWidth()) / 2, 4 * margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
					ScreenUtil.drawLeftSoftKey(g, h, okBt, margin);
				} else if (playing) {
					g.drawString("playing", (w - middleTextArea.getWidth()) / 2, 4 * margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
					ScreenUtil.drawLeftSoftKey(g, h, okBt, margin);
				} else if (recorded) {
					if (showMenu) {
						String[] menuItems = {"play", "tag"};
						ScreenUtil.drawMenu(g, h, menuItems, menuTop, menuMiddle, menuBottom, menuSel);
						ScreenUtil.drawLeftSoftKey(g, h, menuBt, margin);
					}
					g.drawString("recorded", (w - middleTextArea.getWidth()) / 2, 4 * margin + logo.getHeight() + iconOverlay.getHeight(), Graphics.TOP | Graphics.LEFT);
				} else {
					createRecorder();
					ScreenUtil.drawLeftSoftKey(g, h, recordBt, margin);
				}


				break;
			case TEXT_STAT:
				ScreenUtil.drawIcons(g, w, (w - 2 * margin - middleTextArea.getWidth()) / 2, 2 * margin + logo.getHeight(), icons, iconOverlay);
				ScreenUtil.drawTextArea(g, 100, (w - 2 * margin - middleTextArea.getWidth()) / 2, 3 * margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);
				ScreenUtil.drawText(g, "press OK to start writing.", (w - middleTextArea.getWidth()) / 2, 4 * margin + logo.getHeight() + iconOverlay.getHeight(), fh, 60);
				ScreenUtil.drawLeftSoftKey(g, h, okBt, margin);
				break;
			case TEXT_INPUT_STAT:
				ScreenUtil.drawIcons(g, w, (w - 2 * margin - middleTextArea.getWidth()) / 2, 2 * margin + logo.getHeight(), icons, iconOverlay);
				ScreenUtil.drawTextArea(g, 100, (w - 2 * margin - middleTextArea.getWidth()) / 2, 3 * margin + logo.getHeight() + iconOverlay.getHeight(), topTextArea, middleTextArea, bottomTextArea);
				ScreenUtil.drawTextArea(g, 80, (w - middleTextArea.getWidth()) / 2, 4 * margin + logo.getHeight() + iconOverlay.getHeight(), topWhiteArea, middleWhiteArea, bottomWhiteArea);

				// the text
				keySelect = "";
				if (keyMajor != -1) {
					String all = keys[keyMajor];
					keySelect = all.substring(0, keyMinor) + "[" + all.charAt(keyMinor) + "]" + all.substring(keyMinor + 1);
				}

				String lin1 = ("" + (f.stringWidth(inputText) / (w - 4 * margin)));
				int nrOfLines = Integer.parseInt(lin1.substring(0, 1));

				if (nrOfLines < 8) {
					for (int i = 0; i < (nrOfLines + 1); i++) {
						String txt = inputText.substring(i * 32, inputText.length());
						g.drawString(txt, (w - middleTextArea.getWidth()) / 2, 2 * margin + logo.getHeight() + iconOverlay.getHeight() + topTextArea.getHeight() + i * fh, Graphics.TOP | Graphics.LEFT);
					}
					g.drawString(keySelect, (w - middleTextArea.getWidth()) / 2, 80 + 4 * margin + logo.getHeight() + iconOverlay.getHeight() + middleTextArea.getHeight() + fh, Graphics.TOP | Graphics.LEFT);
				}

				ScreenUtil.drawLeftSoftKey(g, h, okBt, margin);
				break;
		}
	}

	/**
	 * Handles all key actions.
	 *
	 * @param key The Key that was hit.
	 */
	public void keyPressed(int key) {
		/*log("key: " + key);*/
		log("screenStat: " + screenStat);
		if (screenStat == TEXT_INPUT_STAT || screenStat == TAG_STAT) {
			if (titleBoxSelected) {
				// start clean
				if (titleText.equals("_")) titleText = "";
			} else if (tagBoxSelected) {
				// start clean
				if (tagText.equals("_")) tagText = "";
			} else {
				if (inputText.equals("_")) inputText = "";
			}

			if (key == -8) {
				if (titleBoxSelected) {
					titleText = titleText.substring(0, titleText.length() - 1);
				} else if (tagBoxSelected) {
					tagText = tagText.substring(0, tagText.length() - 1);
				} else {
					inputText = inputText.substring(0, inputText.length() - 1);
				}
				repaint();
				return;
			} else
			if (key == Canvas.KEY_NUM0 || key == Canvas.KEY_NUM1 || key == Canvas.KEY_NUM2 || key == Canvas.KEY_NUM3 ||
					key == Canvas.KEY_NUM4 || key == Canvas.KEY_NUM5 || key == Canvas.KEY_NUM6 || key == Canvas.KEY_NUM7 ||
					key == Canvas.KEY_NUM8 || key == Canvas.KEY_NUM9) {
				if (keyTimer != null) keyTimer.cancel();

				int index = key - KEY_NUM0;

				if (index < 0 || index > keys.length)
					keyMajor = -1;
				else {
					if (index != keyMajor) {
						keyMinor = 0;
						keyMajor = index;
					} else {
						keyMinor++;
						if (keyMinor >= keys[keyMajor].length())
							keyMinor = 0;
					}

					keyTimer = new Timer();
					keyTimer.schedule(new KeyConfirmer(this), 1000);
				}
				repaint();
				return;
			}
		}

		// left soft key & fire
		if (key == -6 || getGameAction(key) == Canvas.FIRE) {
			log("LEFT SOFTKEY or FIRE pressed");
			switch (screenStat) {
				case PHOTO_STAT:
					if (captured) {
						titleText = "_";
						titleBoxSelected = true;
						screenStat = TAG_STAT;
					} else {
						capture();
						captured = true;
					}
					break;
				case AUDIO_STAT:
					if (recorded) {
						if (ScreenUtil.getSelectedMenuItem() == 1) {
							playAudio();
							playing = true;
						} else if (ScreenUtil.getSelectedMenuItem() == 2) {
							titleText = "_";
							titleBoxSelected = true;
							screenStat = TAG_STAT;
						}
					} else if (recording) {
						stopRecorder();
						recording = false;
						recorded = true;
					} else if (playing) {
						stopAudio();
						playing = false;
					} else {
						startRecorder();
						recording = true;
					}
					break;
				case TEXT_STAT:
					screenStat = TEXT_INPUT_STAT;
					// indicate that the user can write...
					if (inputText.length() == 0) inputText = "_";
					break;
				case TEXT_INPUT_STAT:
					if (inputText.length() > 0) {
						titleText = "_";
						titleBoxSelected = true;
						screenStat = TAG_STAT;
					}
					break;
				case TAG_STAT:
					if (tagCloudSelected) {
						addTag();
					} else {
						if (mediumType == TEXT) {
							// create the text
							uploadText();
							//clear fields
							inputText = "";
							titleText = "";
							tagText = "";
							screenStat = TEXT_STAT;
						} else if (mediumType == AUDIO) {
							uploadAudio();
							recording = false;
							recorded = false;
							playing = false;
							titleText = "";
							tagText = "";
							screenStat = TEXT_STAT;
						} else if (mediumType == PHOTO) {
							uploadPhoto();
							captured = false;
							titleText = "";
							tagText = "";
							screenStat = TEXT_STAT;
						}

					}
			}
			// right softkey
		} else if (key == -7) {
			log("RIGHT SOFTKEY pressed");
			switch (screenStat) {
				case TEXT_INPUT_STAT:
					screenStat = TEXT_STAT;
					inputText = "";
					titleText = "";
					tagText = "";
					break;
				case TAG_STAT:
					if (mediumType == TEXT) {
						screenStat = TEXT_INPUT_STAT;
					} else if (mediumType == AUDIO) {
						screenStat = AUDIO_STAT;
					} else if (mediumType == PHOTO) {
						screenStat = PHOTO_STAT;
					}
					break;
				default:
					screenStat = TEXT_STAT;
					midlet.setScreen(org.walkandplay.client.phone.WPMidlet.TRACE_CANVAS);
			}

			// left
		} else if (getGameAction(key) == Canvas.LEFT) {
			log("LEFT pressed");
			if (screenStat == TAG_STAT) {
				selectPrevTag();
			} else {
				switch (ScreenUtil.getSelectedIcon()) {
					// text
					case 1:
						screenStat = AUDIO_STAT;
						mediumType = AUDIO;
						break;
						// photo
					case 2:
						closeCam();
						screenStat = TEXT_STAT;
						mediumType = TEXT;
						break;
						// audio
					case 3:
						closeRecorder();
						recorded = false;
						recording = false;
						playing = false;

						screenStat = PHOTO_STAT;
						mediumType = PHOTO;
						break;
				}
				ScreenUtil.selectPrevIcon();
			}
			// right
		} else if (getGameAction(key) == Canvas.RIGHT) {
			log("RIGHT pressed");
			if (screenStat == TAG_STAT) {
				selectNextTag();
			} else {
				switch (ScreenUtil.getSelectedIcon()) {
					// text
					case 1:
						screenStat = PHOTO_STAT;
						mediumType = PHOTO;
						break;
						// photo
					case 2:
						closeCam();
						screenStat = AUDIO_STAT;
						mediumType = AUDIO;
						break;
						// audio
					case 3:
						closeRecorder();
						recorded = false;
						recording = false;
						playing = false;
						screenStat = TEXT_STAT;
						mediumType = TEXT;
						break;
				}
				ScreenUtil.selectNextIcon();
			}
			// up
		} else if (getGameAction(key) == Canvas.UP) {
			log("UP pressed");
			if (screenStat == TAG_STAT) {
				if (tagCloudSelected) {
					tagBoxSelected = true;
					titleBoxSelected = false;
					tagCloudSelected = false;
					if (tagText.length() == 0) tagText = "_";
					deselectTagCloud();
				} else if (tagBoxSelected) {
					titleBoxSelected = true;
					tagBoxSelected = false;
					tagCloudSelected = false;
					if (titleText.length() == 0) titleText = "_";
				}
			} else if (screenStat == AUDIO_STAT) {
				ScreenUtil.selectNextMenuItem();
			}
			// down
		} else if (getGameAction(key) == Canvas.DOWN) {
			log("DOWN pressed");
			if (screenStat == TAG_STAT) {
				if (titleBoxSelected) {
					tagBoxSelected = true;
					titleBoxSelected = false;
					tagCloudSelected = false;
					if (tagText.length() == 0) tagText = "_";
				} else if (tagBoxSelected) {
					tagCloudSelected = true;
					tagBoxSelected = false;
					titleBoxSelected = false;
					selectFirstTag();
				}
			} else if (screenStat == AUDIO_STAT) {
				ScreenUtil.selectPrevMenuItem();
			}
		}
		repaint();
	}

	synchronized void keyConfirmed() {
		if (keyMajor != -1) {
			if (titleBoxSelected) {
				titleText += keys[keyMajor].charAt(keyMinor);
			} else if (tagBoxSelected) {
				tagText += keys[keyMajor].charAt(keyMinor);
			} else {
				inputText += keys[keyMajor].charAt(keyMinor);
			}

			keyMajor = -1;
			repaint();
		}
	}

	class KeyConfirmer extends TimerTask {
		MediaCanvas mainCanvas;

		private KeyConfirmer(MediaCanvas aCanvas) {
			mainCanvas = aCanvas;
		}

		public void run() {
			mainCanvas.keyConfirmed();
		}
	}


	////////////////////////////////////////////////////////////
	// camera code
	////////////////////////////////////////////////////////////
	public void showCam(int anXpos, int anYpos, int aDisplayHeight, int aDisplayWidth) {
		try {
			// create the player if it does not exist
			if (player == null) {
				player = Manager.createPlayer("capture://video");
				player.realize();
			}

			// create the video control if it does not exist
			if (videoControl == null) {
				videoControl = (VideoControl) player.getControl("VideoControl");
				videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
			}

			// place the camera screen
			videoControl.setDisplayLocation(anXpos, anYpos);
			videoControl.setDisplaySize(aDisplayWidth, aDisplayHeight);
			videoControl.setVisible(true);

			// show the player input
			player.start();

		} catch (Throwable ioe) {
			System.out.println("Exception initialising camera : " + ioe);
		}
	}

	private void closeCam() {
		try {
			// close the player and videocontrol
			if (player != null) {
				player.stop();
				player.close();
				player = null;
			}

			if (videoControl != null) {
				videoControl = null;
			}
		} catch (MediaException me) {
			System.out.println("Exception closing the camera: " + me.toString());
		}
	}

	public void capture() {
		try {
			// create the captured image
			try {
				dataPhoto = videoControl.getSnapshot("encoding=jpeg&width=320&height=240");
			} catch (Throwable t) {
				// Some phones don't support specific encodings
				// This should fix at least SonyEricsson K800i...
				dataPhoto = videoControl.getSnapshot(null);
			}

			time = Util.getTime();

			previewPhoto = createPhotoPreview(Image.createImage(dataPhoto, 0, dataPhoto.length));

			// Shut down the player.
			closeCam();

		} catch (Throwable me) {
			System.out.println("Exception trying to capture : " + me);
		}
	}

	public Image getPhotoPreview() {
		return previewPhoto;
	}

	// Scale down the image by skipping pixels
	public static Image createPhotoPreview(Image image) {
		int sw = image.getWidth();
		int sh = image.getHeight();

		int pw = 160;
		int ph = pw * sh / sw;

		Image temp = Image.createImage(pw, ph);
		Graphics g = temp.getGraphics();

		for (int y = 0; y < ph; y++) {
			for (int x = 0; x < pw; x++) {
				g.setClip(x, y, 1, 1);
				int dx = x * sw / pw;
				int dy = y * sh / ph;
				g.drawImage(image, x - dx, y - dy,
						Graphics.LEFT | Graphics.TOP);
			}
		}

		return Image.createImage(temp);
	}

	public boolean uploadPhoto() {
		JXElement rsp = Net.getInstance().uploadMedium(titleText, "image", mimePhoto, time, dataPhoto, false, tagText);
		if (rsp == null || rsp.getTag().indexOf("nrsp") != -1) {
			return false;
		}
		return true;
	}

	////////////////////////////////////////////////////////////
	// audio code
	////////////////////////////////////////////////////////////

	public void createRecorder() {
		try {
			player = Manager.createPlayer("capture://audio?rate=" + rate + "&bits=" + bits);
			player.realize();
			recordControl = (RecordControl) player.getControl("RecordControl");
			outputAudio = new ByteArrayOutputStream();
			recordControl.setRecordStream(outputAudio);
		} catch (Throwable t) {
			System.out.println("Exception initialising audiorecorder : " + t);
		}
	}

	private void closeRecorder() {
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

	public void playAudio() {
		if (dataAudio == null) {
			return;
		}

		try {
			Manager.createPlayer(new ByteArrayInputStream(dataAudio), mimeAudio).start();
		} catch (Throwable t) {
			System.out.println("Cannot play audio");
		}
	}

	public void stopAudio() {
		if (dataAudio == null) {
			return;
		}

		try {
			Manager.createPlayer(new ByteArrayInputStream(dataAudio), mimeAudio).stop();
		} catch (Throwable t) {
			System.out.println("Cannot stop audio");
		}
	}

	public void startRecorder() {
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

	public void stopRecorder() {
		try {
			recordControl.commit();
			player.close();
			dataAudio = outputAudio.toByteArray();
		} catch (Exception e) {
			System.out.println("Cannot stop recording");
		} finally {
			closeRecorder();
		}
	}

	public boolean uploadAudio() {
		JXElement rsp = Net.getInstance().uploadMedium(titleText, "audio", mimeAudio, startTime, dataAudio, false, tagText);
		if (rsp == null || rsp.getTag().indexOf("nrsp") != -1) {
			return false;
		}
		return true;
	}

	////////////////////////////////////////////////////////////
	// text code
	////////////////////////////////////////////////////////////


	public boolean uploadText() {
		JXElement rsp = Net.getInstance().uploadMedium(titleText, "text", mimeText, startTime, inputText.getBytes(), false, tagText);
		if (rsp == null || rsp.getTag().indexOf("nrsp") != -1) {
			return false;
		}
		return true;
	}
}
