package org.geotracing.client;

import nl.justobjects.mjox.JXElement;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.MIDlet;
import java.io.IOException;

/**
 * SHows moving dot on map.
 */
public class MapCanvas extends GameCanvas implements CommandListener {
	private Displayable prevScreen;
	private String tileBaseURL;
	private JXElement tileInfo, prevTileInfo;
	private Image tileImage;
	private static final long REFRESH_INTERVAL_MILLIS=10000L;
	private long lastRefreshMillis;
	private MFloat tileScale;
	private int zoom = 12;
	private Command zoomIn;
	private Command zoomOut;
	private Command back;
	private Command toggleMapType;
	private MIDlet midlet;
	private Image redDot;
	private String mapType = "map";
	private String lon = "0", lat = "0";
	private boolean active;

	public MapCanvas() {
		super(false);
		setFullScreenMode(true);

		zoomIn = new Command("Zoom In", Command.OK, 1);
		zoomOut = new Command("Zoom Out", Command.OK, 1);
		back = new Command("Back", Command.OK, 1);
		toggleMapType = new Command("Toggle Map Type", Command.OK, 1);
		addCommand(zoomIn);
		addCommand(zoomOut);
		addCommand(toggleMapType);
		addCommand(back);
		setCommandListener(this);
	}

	public void activate(MIDlet aMidlet) {
		midlet = aMidlet;
		tileBaseURL = Net.getInstance().getURL() + "/map/gmap.jsp?";
		prevScreen = Display.getDisplay(aMidlet).getCurrent();
		Display.getDisplay(midlet).setCurrent(this);
		active = true;
		fetchTileInfo();
		show();
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			active = false;
			Display.getDisplay(midlet).setCurrent(prevScreen);
		} else if (c == zoomIn) {
			zoom++;
			fetchTileInfo();
			show();
		} else if (c == zoomOut) {
			zoom--;
			fetchTileInfo();
			show();
		} else if (c == toggleMapType) {
			mapType = mapType.equals("sat") ? "map" : "sat";
			fetchTileInfo();
			tileImage = null;
			show();
		}
	}

	public boolean hasLocation() {
		return !lon.equals(("0")) && !lat.equals("0");
	}

	/**
	 * Draws the map.
	 *
	 * @param g The graphics object.
	 */
	public void paint(Graphics g) {
		int w = getWidth();
		// Defeat Nokia bug ?
		if (w == 0) {
			w = 176;
		}
		int h = getHeight();
		// Defeat Nokia bug ?
		if (h == 0) {
			h = 208;
		}

		g.setColor(4, 4, 4);
		g.fillRect(0, 0, w, h);
		g.setColor(255,255,255);

		try {

			if (tileInfo != null && tileImage == null) {
				try {
					String tileSize = w + "x" + w;
					String tileURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=image&size=" + tileSize;
					g.drawString("fetching tileImage...", 10, 10, Graphics.TOP | Graphics.LEFT);

					Image mapImage = Util.getImage(tileURL);
					tileImage = Image.createImage(mapImage.getWidth(), mapImage.getHeight());
					tileImage.getGraphics().drawImage(mapImage, 0, 0, Graphics.TOP | Graphics.LEFT);
				} catch (Throwable t) {
					g.drawString("error: " + t.getMessage(), 10, 30, Graphics.TOP | Graphics.LEFT);
					return;
				}
			}

			if (tileImage != null) {

				// Correct pixel offset with tile scale
				if (tileScale == null) {
					tileScale = new MFloat(w).Div(256L);
				}

				// x,y offset of our location in tile tileImage
				String myX = tileInfo.getAttr("x");
				String myY = tileInfo.getAttr("y");
				int x = (int) new MFloat(Integer.parseInt(myX)).Mul(tileScale).toLong();
				int y = (int) new MFloat(Integer.parseInt(myY)).Mul(tileScale).toLong();


				if (prevTileInfo != null) {
					String lmyX = prevTileInfo.getAttr("x");
					String lmyY = prevTileInfo.getAttr("y");
					int lx = (int) new MFloat(Integer.parseInt(lmyX)).Mul(tileScale).toLong();
					int ly = (int) new MFloat(Integer.parseInt(lmyY)).Mul(tileScale).toLong();
					Graphics tg = tileImage.getGraphics();
					tg.setColor(0,0,255);
					tg.drawLine(lx, ly, x, y);
				}

				// Draw map
				g.drawImage(tileImage, 0, 0, Graphics.TOP | Graphics.LEFT);

				// Draw current location
				if (redDot == null) {
					redDot = Image.createImage("/red_dot.png");
				}
				g.drawImage(redDot, x, y, Graphics.TOP | Graphics.LEFT);
			} else {
				g.setColor(100, 100, 100);
				g.drawString("No location", 10, 10, Graphics.TOP | Graphics.LEFT);
			}
		} catch (IOException ioe) {
			g.drawString("cannot get image", 10, 10, Graphics.TOP | Graphics.LEFT);
			g.drawString("try zooming out", 10, 30, Graphics.TOP | Graphics.LEFT);
		} catch (Throwable t) {
			g.drawString("ERROR", 10, 10, Graphics.TOP | Graphics.LEFT);
			g.drawString(t+"", 10, 30, Graphics.TOP | Graphics.LEFT);
		}
	}

	public void setLocation(String aLon, String aLat) {
		// Don't refresh too often (save network overhead)
		long now = Util.getTime();
		if (now - lastRefreshMillis < REFRESH_INTERVAL_MILLIS) {
			return;
		}

		lon = aLon;
		lat = aLat;
		lastRefreshMillis = now;
		fetchTileInfo();
		show();
	}

	protected void fetchTileInfo() {
		if (!hasLocation() || !active) {
			return;
		}

		try {
			// Get information on tile
			String tileInfoURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=xml";
			JXElement newTileInfo = Util.getXML(tileInfoURL);

			// Reset tileImage if first tile info or if keyhole ref changed (we moved to new tile).
			if (tileInfo == null || !tileInfo.getAttr("khref").equals(newTileInfo.getAttr("khref"))) {
				tileImage = null;
				prevTileInfo = null;
			}

			// Remember last point/tile if still on same tile
			if (tileImage != null) {
				prevTileInfo = tileInfo;
			}
			tileInfo = newTileInfo;
			// System.out.println("khref=" + tileInfo.getAttr("khref"));
		} catch (Throwable t) {
			Log.log("error: MapCanvas: t=" + t + " m=" + t.getMessage());
		}
	}

	private void show() {
		if (active) {
			repaint();
		}
	}
}
