// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;

import nl.justobjects.mjox.JXElement;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;
import java.util.Date;

/**
 * Monitor for geotagged objects around point.
 */
public class RadarScreen extends Canvas {
	private Displayable prevScreen;
	private String queryBaseURL;
	private String mediumBaseURL;
	private MIDlet midlet;
	private String loc;
	private int radius = 50, max = 11;
	private long interval = 5000;
	private boolean active;
	private Vector detects;
	private Timer timer;
	private String msg;
	private int w = -1, h = -1;
	private Font f, fb;
	private GPSLocation myLocation;
	private String queryURL = "";
	private JXElement bumpedObject, showObject;
	private final int MIN_BUMP_DIST = 20;
	private Image bumpedImage;
	private boolean radiusChanged;

	public RadarScreen(MIDlet aMidlet) {
		midlet = aMidlet;
		queryBaseURL = Net.getInstance().getURL() + "/srv/get.jsp?cmd=q-around";
		mediumBaseURL = Net.getInstance().getURL() + "/media.srv?id=";
		prevScreen = Display.getDisplay(aMidlet).getCurrent();
		activate();
	}

	public void activate() {
		passivate();
		timer = new Timer();
		timer.schedule(new RefreshTask(), 1000, interval);
		active = true;
		msg = "Radar starting...";
		show();
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
		int inc = 1;
		if (radius > 1000) {
			inc = 1000;
		} else if (radius > 100) {
			inc = 100;
		} else if (radius > 10) {
			inc = 10;
		}

		switch (getGameAction(key)) {
			case UP:
			case RIGHT:
				radius += inc;
				radiusChanged = true;
				break;
			case LEFT:
			case DOWN:
				radius -= inc;
				radiusChanged = true;
				break;
			case FIRE:
				bumpedImage = null;
				showObject = null;
				if (bumpedObject != null) {
					showObject = new JXElement("record");
					showObject.addChildren(bumpedObject.getChildren());
					String type = showObject.getChildText("type");
					String id = showObject.getChildText("id");
					String url = mediumBaseURL + id;
					if (type.equals("image")) {
						try {
							url += "&resize=" + w;
							bumpedImage = Util.getImage(url);
						} catch (Throwable t) {
							log("Error fetching image url=");
						}
					} else if (type.equals("audio")) {
						try {
							showObject.setChildText("text", "playing audio...");
							Util.playAudioStream(url);
						} catch (Throwable t) {
							log("Error playing audio url=");
						}
					} else if (type.equals("text")) {
						try {
							showObject.setChildText("text", Util.getPage(url));
						} catch (Throwable t) {
							log("Error fetching text url=");
						}
					} else {
						showObject.setChildText("text", "unsupported media type");
					}

					if (showObject.getChildText("name") == null) {
						showObject.setChildText("name", "unnamed");
					}

					// set formatted time
					String time = new Date(Long.parseLong(showObject.getChildText("time"))).toString();
					showObject.setChildText("ftime", time);
					bumpedObject = null;
				}
				break;
			default:
				passivate();
				Display.getDisplay(midlet).setCurrent(prevScreen);
				break;

		}

		if (radius <= 0) {
			radius = 1;
		}
		show();
	}

	/**
	 * Draws the radar screen.
	 *
	 * @param g The graphics object.
	 */
	public void paint(Graphics g) {
		if (f == null) {
			setFullScreenMode(true);
			w = getWidth();
			h = getHeight();
			if (w == 0 || h == 0) {
				w = 176;
				h = 208;
			}
			f = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);
			fb = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);
		}


		try {
			g.setFont(f);
			g.setColor(4, 4, 4);
			g.fillRect(0, 0, w, h);

			g.setColor(0xCCCCCC);

			g.drawLine(0, 18, w, 18);
			g.setColor(0xEEEEEE);

			if (showObject != null) {
				g.drawString(showObject.getChildText("name"), 30, 5, Graphics.TOP | Graphics.LEFT);

				int ty = 40;
				if (bumpedImage != null) {
					g.drawImage(bumpedImage, 0, 20, Graphics.TOP | Graphics.LEFT);
					ty = bumpedImage.getHeight() + 25;
				} else {
					g.drawString(showObject.getChildText("text"), 5, 25, Graphics.TOP | Graphics.LEFT);
				}

				g.drawString("by: " + showObject.getChildText("user"), 5, ty, Graphics.TOP | Graphics.LEFT);
				g.drawString(showObject.getChildText("ftime"), 5, ty + 15, Graphics.TOP | Graphics.LEFT);
				return;
			}

			int objCount = 0;
			if (detects != null) {
				objCount = detects.size();
				msg = "r=" + radius + "m  | ";
				if (radiusChanged) {
					msg += "wait...";
				} else {
					msg += (objCount == max) ? "too many" : objCount + "";
					msg += " objects";
				}
			}

			// Draw
			g.drawString(msg, 30, 5, Graphics.TOP | Graphics.LEFT);
			int radarW = w - 10;
			int radarH = w - 10;

			int radarX = 5;
			int radarY = 18 + (h - 18 - w)/2;

			int x = radarX + radarW / 2 - 1;
			int y = radarY + radarH / 2 - 1;
			g.setColor(0x444444);
			g.fillArc(radarX, radarY, radarW, radarH, 0, 360);
			g.setColor(0xFFFFFF);
			g.fillArc(x, y, 2, 2, 0, 360);

			if (radiusChanged) {
				radiusChanged = false;
				return;
			}

			JXElement nearestObject = null;
			if (objCount < max) {
				JXElement obj;
				int objDist, objX, objY, objW, objH;
				String objType, objId;
				nearestObject = null;
				for (int i = 0; i < objCount; i++) {
					obj = (JXElement) detects.elementAt(i);
					objDist = Integer.parseInt(obj.getChildText("distance"));
					objType = obj.getChildText("type");
					if (objType.equals("image")) {
						g.setColor(0xFF0000);
					} else if (objType.equals("video")) {
						g.setColor(0xFF9900);
					} else if (objType.equals("audio")) {
						g.setColor(0x0000FF);
					} else if (objType.equals("text")) {
						g.setColor(0xEEEEEE);
					} else if (objType.equals("user")) {
						g.setColor(0xFF99FF);
					}
					objId = obj.getChildText("id");
					log("id=" + objId + " objType=" + objType + " dist=" + objDist);
					objX = radarX + radarW / 2 - (objDist * radarW) / radius / 2;
					objY = radarY + radarH / 2 - (objDist * radarH) / radius / 2;
					objW = (radarW * objDist) / radius;
					objH = objW;
					if (objW < radarW) {
						g.drawArc(objX, objY, objW, objH, 0, 360);
					}

					if (nearestObject == null || objDist < Integer.parseInt(nearestObject.getChildText("distance"))) {
						nearestObject = obj;
					}
				}
			}

			if (nearestObject != null && Integer.parseInt(nearestObject.getChildText("distance")) < MIN_BUMP_DIST) {
				bumpedObject = nearestObject;
				g.setColor(0xFFFFFF);
				g.setFont(fb);
				g.drawString("[hit!]", w - 45, 5, Graphics.TOP | Graphics.LEFT);
			}
		} catch (Throwable t) {
			g.drawString("error in paint", 10, 10, Graphics.TOP | Graphics.LEFT);
		}
	}

	private void show() {
		if (active) {
			repaint();
		}
	}

	public void log(String s) {
		msg = s;
		System.out.println(s);
	}

	private class RefreshTask extends TimerTask {
		public void run() {
			// log("run()");
			log("fetching GPS location...");
			GPSLocation myNewLocation = GPSFetcher.getInstance().getCurrentLocation();
			if (myNewLocation != null) {
				myLocation = myNewLocation;
			}

			if (myLocation == null) {
				log("no GPS location (yet)");
				show();
				return;
				//myLocation = new GPSLocation();
				//myLocation.lon = MFloat.parse("4.852911666666", 10);
				//myLocation.lat = MFloat.parse("52.31218333333", 10);
			}

			loc = myLocation.lon.toString() + "," + myLocation.lat.toString();
			String newQueryURL = queryBaseURL + "&loc=" + loc + "&radius=" + radius + "&max=" + max;
			if (newQueryURL.equals(queryURL)) {
				show();
				return;
			}

			queryURL = newQueryURL;
			JXElement result = null;
			try {
				result = Util.getXML(queryURL);
			} catch (Throwable t) {
				log("error in query: " + t.getMessage());
			}

			detects = null;
			if (result != null) {
				detects = result.getChildren();
			} else {
				log("no objects found");
			}
			show();
		}
	}
}
