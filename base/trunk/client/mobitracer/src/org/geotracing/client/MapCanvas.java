package org.geotracing.client;

import nl.justobjects.mjox.JXElement;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.GameCanvas;
import javax.microedition.midlet.MIDlet;

/** EXPERIMENTAL (NOT YET USED) */
public class MapCanvas extends GameCanvas implements CommandListener {
	private Displayable prevScreen;
	private String tileURL;
	private int zoom = 12;
	private String currentTile;
	private String newTile;
	private String currentURL = "";
	private Command zoomIn;
	private Command zoomOut;
	private Command back;
	private Command toggleMapType;
	private MIDlet midlet;
	private Image image;
	private String mapType = "map";
	private int myX, myY;
	private String lon="0", lat="0";
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
		tileURL = Net.getInstance().getURL() + "/map/gmap.jsp?";
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
			zoom++;
			show();
		} else if (c == zoomOut) {
			zoom--;
			show();
		} else if (c == toggleMapType) {
			mapType = mapType.equals("sat") ? "map" : "sat";
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

		System.out.println("paint()");
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


		if (hasLocation()) {
			newTile = tileURL + "&lon=" + lon + "&lat=" + lat + "&zoom=" + zoom + "&type=" + mapType;
			if (currentTile == null) {
				currentTile = newTile;
			}
		}

		if (currentTile == null) {
			g.setColor(4, 4, 4);
			g.fillRect(0, 0, w, h);
			g.setColor(100, 100, 100);
			g.drawString("No location", 10, 10, Graphics.TOP | Graphics.LEFT);
		} else {

			try {
				if (newTile != null && !currentTile.equals(newTile)) {
					//msg = "Fetching map image...";
					currentTile = newTile;
					p("currentTile=" + currentTile);

					// Get google tile url and our location in pixels
					JXElement tileInfo = Util.getXML(currentTile);
					if (tileInfo != null) {
						String newURL = tileInfo.getAttr("url");

						// Only get new image if current location outside current image
						if (!newURL.equals(currentURL)) {
							p("new image: url=" + tileInfo.getAttr("url"));
							g.drawString("getImage()" + newURL, 10, 20, Graphics.TOP | Graphics.LEFT);
							image = Util.getImage(newURL);
							currentURL = newURL;
						}

						// Get my location as image pixtileInfo offsets
						myX = tileInfo.getIntAttr("x");
						myY = tileInfo.getIntAttr("y");
					}
					// draw the google map image
				}
				p("draw x=" + myX + " y=" + myY);

				if (image != null) {
					// Draw image with location superimposed
					g.drawImage(image, 0, 0, Graphics.TOP | Graphics.LEFT);
					g.setColor(200, 0, 0);
					g.fillRect(myX - 2, myY - 2, 4, 4);
				}
			} catch (Throwable t) {
				g.drawString("cannot get image", 10, 10, Graphics.TOP | Graphics.LEFT);
				g.drawString("try zooming out", 10, 30, Graphics.TOP | Graphics.LEFT);
			}

		}
	}

	public void setLocation(String aLon, String aLat) {
		lon = aLon;
		lat = aLat;
		show();
	}

	private void show() {
		if (active) {
			repaint();
		}
	}

	private void p(String s) {
		System.out.println(s);
	}
}
