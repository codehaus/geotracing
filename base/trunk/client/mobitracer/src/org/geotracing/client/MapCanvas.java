package org.geotracing.client;

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
	private GoogleMap.XY xy, prevXY;
	private Image mapImage;
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
			xy = prevXY = null;
			fetchTileInfo();
			show();
		} else if (c == zoomOut) {
			zoom--;
			xy = prevXY = null;
			fetchTileInfo();
			show();
		} else if (c == toggleMapType) {
			mapType = mapType.equals("sat") ? "map" : "sat";
			fetchTileInfo();
			mapImage = null;
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
		g.setColor(255, 255, 255);

		try {

			if (tileScale == null) {
				tileScale = new MFloat(w).Div(GoogleMap.I_GMAP_TILE_SIZE);
			}

			if (hasLocation() && mapImage == null) {
				try {
					String tileSize = w + "x" + w;
					String tileURL = tileBaseURL + "lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType + "&format=image&size=" + tileSize;
					g.drawString("fetching mapImage...", 10, 10, Graphics.TOP | Graphics.LEFT);


					// Get Google Tile image and draw on mapImage
					Image tileImage = Util.getImage(tileURL);
					mapImage = Image.createImage(tileImage.getWidth(), tileImage.getHeight());
					mapImage.getGraphics().drawImage(tileImage, 0, 0, Graphics.TOP | Graphics.LEFT);
					fetchTileInfo();
					repaint();
				} catch (Throwable t) {
					g.drawString("error: " + t.getMessage(), 10, 30, Graphics.TOP | Graphics.LEFT);
					return;
				}
			}

			if (xy != null) {

				// If we have previous point: draw line from there to current
				if (prevXY != null) {
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

				g.drawImage(redDot, xy.x, xy.y, Graphics.TOP | Graphics.LEFT);
			} else if (hasLocation()) {
				fetchTileInfo();
				repaint();
			} else {
				g.setColor(100, 100, 100);
				g.drawString("No location", 10, 10, Graphics.TOP | Graphics.LEFT);
			}
		} catch (IOException ioe) {
			g.drawString("cannot get image", 10, 10, Graphics.TOP | Graphics.LEFT);
			g.drawString("try zooming out", 10, 30, Graphics.TOP | Graphics.LEFT);
		} catch (Throwable t) {
			g.drawString("ERROR", 10, 10, Graphics.TOP | Graphics.LEFT);
			g.drawString(t + "", 10, 30, Graphics.TOP | Graphics.LEFT);
		}
	}

	public void setLocation(String aLon, String aLat) {
		lon = aLon;
		lat = aLat;
		fetchTileInfo();
		show();
	}

	protected void fetchTileInfo() {
		if (!hasLocation() || !active) {
			return;
		}

		try {
			GoogleMap.XY newTileXY = GoogleMap.getPixelXY(lon, lat, zoom);
			// System.out.println("MT: x=" + newTileXY.x + " y=" + newTileXY.y);

			// Reset mapImage when
			// no tile info (init)
			// OR zoom changed
			// OR we moved off screen
			if (xy == null || newTileXY.x < 0 || newTileXY.y < 0 || newTileXY.x > GoogleMap.I_GMAP_TILE_SIZE || newTileXY.y > GoogleMap.I_GMAP_TILE_SIZE)
			{
				// System.out.println("refresh");
				mapImage = null;
				prevXY = null;
			}

			// Remember last point/tile if still on same tile
			if (tileScale != null) {
				// Correct pixel offset with tile scale
				// Scale x,y offset of our location in mapImage
				newTileXY.x = (int) new MFloat(newTileXY.x).Mul(tileScale).toLong();
				newTileXY.y = (int) new MFloat(newTileXY.y).Mul(tileScale).toLong();

				prevXY = xy;
				xy = newTileXY;
			}

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
