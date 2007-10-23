package org.geotracing.client;

import nl.justobjects.mjox.JXElement;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.MIDlet;
import java.io.IOException;

/**
 * SHows moving dot on map.
 */
public class MapCanvas extends GameCanvas implements CommandListener {
	private Displayable prevScreen;
	private String tileBaseURL;
	private GoogleMap.XY xy;
	private Image mapImage;
	private int zoom = 12;
	private Command zoomIn;
	private Command zoomOut;
	private Command back;
	private Command toggleMapType;
	private MIDlet midlet;
	private Image redDot;
	private String mapType = "osm";
	private GoogleMap.LonLat lonLat;
	private GoogleMap.BBox bbox;
	private boolean active;
	private int OFF_MAP_TOLERANCE = 15;

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
		tileBaseURL = Net.getInstance().getURL() + "/map.srv";
		prevScreen = Display.getDisplay(aMidlet).getCurrent();
		Display.getDisplay(midlet).setCurrent(this);
		active = true;
		show();
	}

	public void commandAction(Command c, Displayable d) {
		if (c == back) {
			active = false;
			Display.getDisplay(midlet).setCurrent(prevScreen);
		} else if (c == zoomIn) {
			zoomIn();
		} else if (c == zoomOut) {
			zoomOut();
		} else if (c == toggleMapType) {
			mapType = mapType.equals("sat") ? "osm" : "sat";
			resetMap();
			show();
		}
	}

	public boolean hasLocation() {
		return lonLat != null;
	}

	/**
	 * Handles all key actions.
	 *
	 * @param key The Key that was hit.
	 */
	public void keyPressed(int key) {

		switch (getGameAction(key)) {
			case UP:
				zoomOut();
				break;
			case DOWN:
			case FIRE:
				zoomIn();
				break;
		}
	}

	/**
	 * Draws the map.
	 *
	 * @param g The graphics object.
	 */
	public void paint(Graphics g) {
		if (!active) {
			return;
		}
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
		g.setColor(100, 100, 100);

		try {
			// No use proceeding if we don't have location
			if (!hasLocation()) {
				g.drawString("No location (yet)", 10, 10, Graphics.TOP | Graphics.LEFT);
				return;
			}

			// ASSERT: we have a valid location

			// Create bbox if not present
			if (bbox == null) {
				resetMap();

				// Create bbox around our location for given zoom and w,h
				bbox = GoogleMap.createCenteredBBox(lonLat, zoom, w, h);
				g.drawString("fetching map image...", 10, 10, Graphics.TOP | Graphics.LEFT);
				repaint();
				return;
			}

			// Should we fetch new map image ?
			if (mapImage == null) {
				try {
					// Create WMS URL and fetch image
					String mapURL = GoogleMap.createWMSURL(tileBaseURL, bbox, mapType, w, h, "image/jpeg");
					Image wmsImage = Util.getImage(mapURL);

					// Create offscreen image
					mapImage = Image.createImage(wmsImage.getWidth(), wmsImage.getHeight());
					mapImage.getGraphics().drawImage(wmsImage, 0, 0, Graphics.TOP | Graphics.LEFT);
				} catch (Throwable t) {
					g.drawString("error: " + t.getMessage(), 10, 30, Graphics.TOP | Graphics.LEFT);
					return;
				}
			}

			// Draw location and trace.
			GoogleMap.XY prevXY = xy;
			xy = bbox.getPixelXY(lonLat);

			// System.out.println("xy=" + xy);
			// If we have previous point: draw line from there to current
			if (prevXY != null) {
				// Draw trace
				Graphics mapGraphics = mapImage.getGraphics();
				mapGraphics.setColor(0, 0, 255);
				mapGraphics.drawLine(prevXY.x - 1, prevXY.y - 1, xy.x - 1, xy.y - 1);
				mapGraphics.drawLine(prevXY.x, prevXY.y, xy.x, xy.y);
			}

			// Draw background map
			g.drawImage(mapImage, 0, 0, Graphics.TOP | Graphics.LEFT);

			// Draw current location
			if (redDot == null) {
				redDot = Image.createImage("/red_dot.png");
			}

			g.drawImage(redDot, xy.x - (redDot.getWidth()) / 2, xy.y - (redDot.getHeight()) / 2, Graphics.TOP | Graphics.LEFT);

			// If moving off map refresh
			if (xy.x < OFF_MAP_TOLERANCE || w - xy.x < OFF_MAP_TOLERANCE || xy.y < OFF_MAP_TOLERANCE || h - xy.y < OFF_MAP_TOLERANCE)
			{
				resetMap();
			}
		} catch (IOException ioe) {
			g.drawString("cannot get mapimage", 10, 10, Graphics.TOP | Graphics.LEFT);
			g.drawString("try zooming out", 10, 30, Graphics.TOP | Graphics.LEFT);
		} catch (Throwable t) {
			g.drawString("ERROR", 10, 10, Graphics.TOP | Graphics.LEFT);
			g.drawString(t + "", 10, 30, Graphics.TOP | Graphics.LEFT);
		}
	}

	protected void resetMap() {
		bbox = null;
		mapImage = null;
	}

	public void setLocation(String aLon, String aLat) {
		lonLat = new GoogleMap.LonLat(aLon, aLat);
		show();
	}


	private void show() {
		if (active) {
			repaint();
		}
	}

	protected void zoomIn() {
		zoom++;
		resetMap();
		show();
	}

	protected void zoomOut() {
		zoom--;
		resetMap();
		show();
	}
}
