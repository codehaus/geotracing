// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;

import nl.justobjects.mjox.JXElement;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.MIDlet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Monitor for geotagged objects around point.
 */
public class RadarScreen extends GameCanvas {
	private static final int MIN_HIT_DIST = 20;
	private static final int START_RADIUS = 50;
	private static final int MAX_OBJECTS = 11;
	private static final String OBJECT_TYPES = "medium";
	private static final long REFRESH_INTERVAL_MILLIS = 8000;
	private static final int AUDIO_VOLUME = 70;

	private Displayable prevScreen;
	private String queryBaseURL;
	private String mediumBaseURL;
	private MIDlet midlet;
	private String loc;
	private int radius = START_RADIUS, max = MAX_OBJECTS;
	private int targetRadius = radius;
	private boolean active;
	private Vector detects = new Vector(0);
	private Timer timer;
	private int w = -1, h = -1;
	private Font f, fb;
	private GPSLocation myLocation;
	private String queryURL = "";
	private String queryTypes = OBJECT_TYPES;
	private JXElement hitObject, showObject;
	private Image hitImage;
	private static final int STATE_IDLE = 1;
	private static final int STATE_FILTER_CHANGE = 2;
	private static final int STATE_DETECTING = 3;
	private static final int STATE_DETECTED = 4;
	private static final int STATE_OBJECT_FETCHING = 5;
	private static final int STATE_OBJECT_SHOW = 6;
	private int state = STATE_IDLE;
	private boolean emulator = false;
	private boolean cheating;
	private JXElement nearestObject;

	public RadarScreen(MIDlet aMidlet) {
		super(false);
		setFullScreenMode(true);
		midlet = aMidlet;
		queryBaseURL = Net.getInstance().getURL() + "/srv/get.jsp?cmd=q-around";
		mediumBaseURL = Net.getInstance().getURL() + "/media.srv?id=";
		prevScreen = Display.getDisplay(aMidlet).getCurrent();
		show();
	}

	public void activate() {
		passivate();
		timer = new Timer();
		timer.schedule(new DetectTask(), 4000, REFRESH_INTERVAL_MILLIS);
		active = true;
	}

	public void passivate() {
		if (timer != null) {
			timer.cancel();
			timer = null;
		}
		active = false;
	}

	/**
	 * Handles all key actions.
	 *
	 * @param key The Key that was hit.
	 */
	public void keyPressed(int key) {

		switch (key) {
			case KEY_NUM0:
				// toggle query types
				queryTypes = queryTypes.equals("medium") ? "user" : "medium";
				setState(STATE_FILTER_CHANGE);
				return;

			case KEY_NUM2:
				// move in cheating mode
				if (cheating) {
					myLocation.lat = myLocation.lat.Add(MFloat.parse("0.001", 10));
				}
				return;
			case KEY_NUM4:
				// move in cheating mode
				if (cheating) {
					myLocation.lon = myLocation.lon.Sub(MFloat.parse("0.001", 10));
				}
				return;
			case KEY_NUM5:
				// move in cheating mode
				cheating = !cheating;
				if (cheating && myLocation == null) {
					myLocation = new GPSLocation();
					myLocation.lon = MFloat.parse("4.85300855", 10);
					myLocation.lat = MFloat.parse("52.3119983", 10);
				}

				log("cheating=" + cheating);
				show();
				break;
			case KEY_NUM8:
				// move in cheating mode
				if (cheating) {
					myLocation.lat = myLocation.lat.Sub(MFloat.parse("0.001", 10));
					return;
				}
			case KEY_NUM6:
				// move in cheating mode
				if (cheating) {
					myLocation.lon = myLocation.lon.Add(MFloat.parse("0.001", 10));
					return;
				}

			case KEY_POUND:
				passivate();
				Display.getDisplay(midlet).setCurrent(prevScreen);
				break;
		}

		int inc = 1;
		if (targetRadius > 1000) {
			inc = 1000;
		} else if (targetRadius > 100) {
			inc = 100;
		} else if (targetRadius > 10) {
			inc = 10;
		}

		switch (getGameAction(key)) {
			case UP:
			case RIGHT:
				targetRadius += inc;
				setState(STATE_FILTER_CHANGE);
				//show();
				break;
			case LEFT:
			case DOWN:
				targetRadius -= inc;
				setState(STATE_FILTER_CHANGE);
				//show();
				break;
			case FIRE:
				hitImage = null;
				showObject = null;
				if (state == STATE_OBJECT_SHOW) {
					setState(STATE_DETECTING);
					break;
				}

				if (cheating && nearestObject != null) {
					hitObject = nearestObject;
				}

				if (hitObject != null) {
					showObject = new JXElement("record");
					showObject.addChildren(hitObject.getChildren());
					final String type = showObject.getChildText("type");
					showObject.setChildText("text", "fetching " + type + "...");
					String id = showObject.getChildText("id");
					final String url = mediumBaseURL + id + "&resize=" + w;
					String time = Util.timeToString(Long.parseLong(showObject.getChildText("time")));
					showObject.setChildText("ftime", time);
					if (showObject.getChildText("name") == null) {
						showObject.setChildText("name", "unnamed");
					}

					setState(STATE_OBJECT_FETCHING);
					new Thread(new Runnable() {
						public void run() {
							if (type.equals("image")) {
								try {
									hitImage = Util.getImage(url);
								} catch (Throwable t) {
									log("Error fetching image url");
								}
							} else if (type.equals("audio")) {
								try {
									showObject.setChildText("text", "playing audio...");
									Util.playStream(url);
								} catch (Throwable t) {
									log("Error playing audio url");
								}
							} else if (type.equals("text")) {
								try {
									showObject.setChildText("text", Util.getPage(url));
								} catch (Throwable t) {
									log("Error fetching text url=");
								}
							} else if (type.equals("track")) {
								showObject.setChildText("text", "track start|end: " + showObject.getChildText("name"));
							} else if (type.equals("user")) {
								showObject.setChildText("text", "last location of " + showObject.getChildText("name"));
							} else {
								showObject.setChildText("text", type + " is not supported (yet)");
							}

							setState(STATE_OBJECT_SHOW);
						}
					}).start();

					// set formatted time
					hitObject = null;
				}
				break;


		}

		if (targetRadius <= 0) {
			targetRadius = 1;
		}
	}

	/**
	 * Draws the radar screen.
	 *
	 * @param g The graphics object.
	 */
	public void paint(Graphics g) {
		if (f == null) {
			w = getWidth();
			h = getHeight();
			if (w == 0 || h == 0) {
				w = 176;
				h = 208;
			}
			f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
			fb = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
		}


		String msg = "starting...";
		try {
			// Clear screen
			g.setFont(f);
			g.setColor(4, 4, 4);
			g.fillRect(0, 0, w, h);

			// Draw header box
			int headerOffset = 18;
			g.setColor(0xCCCCCC);
			g.drawLine(0, headerOffset, w, headerOffset);

			g.setColor(0xEEEEEE);

			switch (state) {
				case STATE_IDLE:
					msg = "GeoRadar Starting...";
					g.drawString("use \"0\" key to toggle users or media", 5, 40, Graphics.TOP | Graphics.LEFT);
					g.drawString("use \"#\" key to go back", 5, 65, Graphics.TOP | Graphics.LEFT);
					activate();
					state = STATE_DETECTING;
					break;

					// Radaring states
				case STATE_DETECTING:
				case STATE_DETECTED:
				case STATE_FILTER_CHANGE:

					// Determine heading message

					// Draw radar circle
					int radarW = w - 10;
					int radarH = w - 10;
					int radarX = 5;
					int radarY = (headerOffset - 5) + (h - (headerOffset - 5) - w) / 2;

					int x = radarX + radarW / 2 - 1;
					int y = radarY + radarH / 2 - 1;
					g.setColor(0xAAAAAA);
					g.fillArc(radarX, radarY, radarW, radarH, 0, 360);

					// Draw detected area
					int dist = (radius < targetRadius) ? radius : targetRadius;
					g.setColor(0x444444);
					int dx = radarX + radarW / 2 - (dist * radarW) / targetRadius / 2;
					int dy = radarY + radarH / 2 - (dist * radarH) / targetRadius / 2;
					int dw = (radarW * dist) / targetRadius;
					g.fillArc(dx, dy, dw, dw, 0, 360);

					g.setColor(0xFFFFFF);
					g.fillArc(x, y, 2, 2, 0, 360);

					g.setColor(0xCCCCCC);
					g.drawLine(0, h - headerOffset, w, h - headerOffset);

					// Draw detected objects on radar as concentric circles
					nearestObject = null;
					int visibleObjCount = 0;

					if (detects.size() < max) {
						JXElement obj;
						int objDist, objX, objY, objW, objH;
						String objType, objId;
						nearestObject = null;

						for (int i = 0; i < detects.size(); i++) {
							obj = (JXElement) detects.elementAt(i);

							// Line color based on object type
							objType = obj.getChildText("type");
							if (objType.equals("image")) {
								g.setColor(0xFF0000);
							} else if (objType.equals("video")) {
								g.setColor(0xFFFF00);
							} else if (objType.equals("audio")) {
								g.setColor(0x0000FF);
							} else if (objType.equals("text")) {
								g.setColor(0xEEEEEE);
							} else if (objType.equals("track")) {
								g.setColor(0x00FF00);
							} else if (objType.equals("user")) {
								g.setColor(0xFF99FF);
							}
							objId = obj.getChildText("id");

							// Draw circle by distance
							objDist = Integer.parseInt(obj.getChildText("distance"));
							log("id=" + objId + " objType=" + objType + " dist=" + objDist);
							if (objDist > radius) {
								// outside visible (detected) range
								continue;
							}
							visibleObjCount++;
							objX = radarX + radarW / 2 - (objDist * radarW) / targetRadius / 2;
							objY = radarY + radarH / 2 - (objDist * radarH) / targetRadius / 2;
							objW = (radarW * objDist) / targetRadius;
							objH = objW;
							if (objW < radarW) {
								g.drawArc(objX, objY, objW, objH, 0, 360);
							}

							// Remember object nearest to me
							if (nearestObject == null || objDist < Integer.parseInt(nearestObject.getChildText("distance")))
							{
								nearestObject = obj;
							}
						}
					}

					msg = "r=" + targetRadius + "m  | ";
					if (state == STATE_DETECTING) {
						msg += "detecting...";
					} else if (state == STATE_FILTER_CHANGE) {
						msg += " ";
					} else if (state == STATE_DETECTED) {
						if (detects.size() >= max) {
							visibleObjCount = detects.size();
						}
						msg += (visibleObjCount >= max) ? "too many " : visibleObjCount + " ";
						if (queryTypes.equals("medium")) {
							msg += "media";
						} else {
							msg += queryTypes + "s";
						}
					}

					// Show nearest object info
					if (nearestObject != null) {
						g.setColor(0xFFFFFF);
						g.setFont(fb);
						int distance = Integer.parseInt(nearestObject.getChildText("distance"));
						int id = Integer.parseInt(nearestObject.getChildText("id"));
						String hint = nearestObject.getChildText("user") + "/" + nearestObject.getChildText("type") + "/" + distance + "m/#" + id;

						// Did we bump on the nearest object ?
						if (distance < MIN_HIT_DIST) {
							msg += " <<HIT>>";
							if (hitObject != null && nearestObject != hitObject && !emulator) {
								Util.playTone(80, 50, AUDIO_VOLUME);
								Util.playTone(90, 250, AUDIO_VOLUME);
							}
							hitObject = nearestObject;
							hint = "<<" + hint + ">>";
						}
						g.drawString(hint, w / 2, h - 3, Graphics.BOTTOM | Graphics.HCENTER);
					}
					break;

				// Object display states
				case STATE_OBJECT_FETCHING:
				case STATE_OBJECT_SHOW:
					if (state == STATE_OBJECT_FETCHING) {
						msg = "fetching " + showObject.getChildText("type") + " object...";
					} else {
						msg = showObject.getChildText("name");
					}

					int ty = 40;
					if (hitImage != null) {
						g.drawImage(hitImage, 0, headerOffset, Graphics.TOP | Graphics.LEFT);
						ty = hitImage.getHeight() + 25;
					} else {
						g.drawString(showObject.getChildText("text"), 5, 25, Graphics.TOP | Graphics.LEFT);
					}

					g.drawString("by: " + showObject.getChildText("user"), 5, ty, Graphics.TOP | Graphics.LEFT);
					g.drawString(showObject.getChildText("ftime"), 5, ty + 18, Graphics.TOP | Graphics.LEFT);
					g.drawString("distance: " + showObject.getChildText("distance") + " m", 5, ty + 31, Graphics.TOP | Graphics.LEFT);

					break;
				default:
					msg = "unknown state";
					break;
			}


		} catch (Throwable t) {
			log("paint() error " + t);
			msg = "paint() error " + t;
		}

		// Draw top message
		g.setColor(0xFFFFFF);
		if (cheating) msg = "*" + msg;
		g.drawString(msg, w / 2, 3, Graphics.TOP | Graphics.HCENTER);
	}

	protected void setState(int aState) {
		state = aState;
		log("state=" + aState);
		show();
	}

	protected void show() {
		if (active) {
			repaint();
		}
	}

	protected void log(String s) {
		// msg = s;
		if (emulator) System.out.println(s);
	}

	private class DetectTask extends TimerTask {
		public void run() {
			// log("run()");
			if (state == STATE_OBJECT_FETCHING || state == STATE_OBJECT_SHOW) {
				return;
			}

			log("fetching GPS location...");
			setState(STATE_DETECTING);
			if (!cheating) {
				GPSLocation myNewLocation = GPSFetcher.getInstance().getCurrentLocation();
				if (myNewLocation != null) {
					myLocation = myNewLocation;
				}
			}

			if (myLocation == null) {
				log("no GPS location (yet)");
				return;
			}

			String me = Net.getInstance().getUserName();

			loc = Util.format(myLocation.lon, 10) + "," + Util.format(myLocation.lat, 10);
			String newQueryURL = queryBaseURL + "&types=" + queryTypes + "&loc=" + loc + "&radius=" + targetRadius + "&max=" + max + "&me=" + me;

			// No new query if identical to last query
			if (newQueryURL.equals(queryURL)) {
				setState(STATE_DETECTED);
				return;
			}

			queryURL = newQueryURL;
			JXElement result = null;
			try {
				result = Util.getXML(queryURL);
			} catch (Throwable t) {
				log("error in query: " + t.getMessage());
			}

			if (result != null) {
				detects = result.getChildren();
			} else {
				log("no objects found");
			}

			radius = targetRadius;
			setState(STATE_DETECTED);
		}
	}
}
