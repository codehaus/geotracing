// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.client;

/**
 * Google Maps utilities.
 * <p/>
 * Original code from multiple sources:
 * by Andrew Rowbottom.
 * Released freely into the public domain, use it how you want, don't blame me.
 * No warranty for this code is taken in any way.
 * original : http://www.ponies.me.uk/maps/GoogleTileUtils.java
 * <p/>
 * Other sources:
 * http://www.cse.buffalo.edu/~ajay/googlemaps.html (for map ref)
 * http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GoogleMap {
	/**
	 * Constants for Google Map tile calcs
	 */
	final public static int I_GMAP_TILE_SIZE = 256;
	final public static MFloat F_GMAP_TILE_SIZE = new MFloat(256L);
	final public static MFloat F_GMAP_HALF_TILE_SIZE = new MFloat(128L);
	final static public MFloat MINUS_ONE = new MFloat(-1L);
	final static public MFloat ZERO = MFloat.ZERO;
	final static public MFloat HALF = new MFloat(2L, -1L);
	final static public MFloat ONE = MFloat.ONE;
	final static public MFloat TWO = new MFloat(2L);
	final static public MFloat FOUR = new MFloat(4L);
	final static public MFloat TWO_PI = TWO.Mul(MFloat.PI);
	final static public MFloat HALF_PI = MFloat.PI.Div(TWO);
	final static public MFloat QUART_PI = MFloat.PI.Div(FOUR);
	final static public MFloat DEG_180 = new MFloat(180L);
	final static public MFloat DEG_MIN_180 = new MFloat(-180L);
	final static public MFloat DEG_360 = new MFloat(360L);
	final static public MFloat DEG_PER_RAD = DEG_180.Div(MFloat.PI);

	/**
	 * Zoom resolutions in meters per pixel.
	 */
	final static public MFloat[] ZOOM_RESOS = {
			new MFloat(2L, -1L), ONE, TWO, FOUR, new MFloat(8L),
			new MFloat(16L), new MFloat(32L), new MFloat(64L), new MFloat(128L), new MFloat(256L),
			new MFloat(512L), new MFloat(1024L), new MFloat(2048L), new MFloat(4096L), new MFloat(8192L),
			new MFloat(16384L), new MFloat(65536L), new MFloat(131072L), new MFloat(262144L)
	};

	public static class XY {
		public int x;
		public int y;

		public XY(MFloat anX, MFloat anY) {
			this((int) anX.toLong(), (int) anY.toLong());
		}

		public XY(int anX, int anY) {
			x = anX;
			y = anY;
		}

		public String toString() {
			return x + "," + y;
		}
	}

	public static class LonLat {
		public MFloat lon;
		public MFloat lat;

		public LonLat(MFloat aLon, MFloat aLat) {
			lon = aLon;
			lat = aLat;
		}

		public LonLat(String aLon, String aLat) {
			this(MFloat.parse(aLon, 10), MFloat.parse(aLat, 10));
		}

		public String toString() {
			return lon +  "," + lat;
		}
	}

	public static class BBox {
		public String keyholeRef;
		public LonLat sw;
		public LonLat ne;
		public MFloat width = F_GMAP_TILE_SIZE;
		public MFloat height = F_GMAP_TILE_SIZE;
		/**
		 * Resolution in pixels/lon
		 */
		private MFloat resolX;

		/**
		 * Resolution in pixels/lat
		 */
		private MFloat resolY;

		public BBox(LonLat aSW, LonLat aNE) {
			this(aSW, aNE, F_GMAP_TILE_SIZE, F_GMAP_TILE_SIZE);
		}

		public BBox(LonLat aSW, LonLat aNE, int aWidth, int aHeight) {
			this(aSW, aNE, new MFloat(aWidth), new MFloat(aHeight));
		}

		public BBox(LonLat aSW, LonLat aNE, MFloat aWidth, MFloat aHeight) {
			sw = aSW;
			ne = aNE;
			width = aWidth;
			height = aHeight;
			System.out.println("BBOX=" + sw + " " + ne);
		}

		public MFloat getLatHeight() {
			return ne.lat.Sub(sw.lat);
		}

		public MFloat getLonWidth() {
			return ne.lon.Sub(sw.lon);
		}

		public XY getPixelXY(LonLat aLonLat) {
			return new XY(getResolX().Mul(aLonLat.lon.Sub(sw.lon)), getResolY().Mul(ne.lat.Sub(aLonLat.lat)));
		}

		public MFloat getResolX() {
			if (resolX == null) {
				resolX = width.Div(getLonWidth());
			}
			return resolX;
		}

		public MFloat getResolY() {
			if (resolY == null) {
				resolY = height.Div(getLatHeight());
			}
			return resolY;
		}

		public String toString() {
			return sw.lon + "," + sw.lat + "," + ne.lon + "," + ne.lat;
		}
	}

	/**
	 * Create bounding box.
	 */
	public static BBox createCenteredBBox(LonLat aLonLat, int aZoom) {
		return createCenteredBBox(aLonLat, aZoom, I_GMAP_TILE_SIZE, I_GMAP_TILE_SIZE);
	}

	/**
	 * Create bounding box.
	 */
	public static BBox createCenteredBBox(LonLat aLonLat, int aZoom, int aWidth, int aHeight) {
		return createCenteredBBox(aLonLat, aZoom, aWidth, aHeight, false);
	}

	/**
	 * Create OGC WMS request.
	 */
	public static BBox createCenteredBBox(LonLat aLonLat, int aZoom, int aWidth, int aHeight, boolean equalDist) {
		MFloat halfTileWidth;
		MFloat halfTileHeight;

		if (equalDist) {
			if (aZoom < 1 || aZoom > ZOOM_RESOS.length - 1) {
				throw new IllegalArgumentException("Illegal zoomlevel");
			}

			// meters per pixel
			MFloat resol = ZOOM_RESOS[ZOOM_RESOS.length - 1 - aZoom];

			// meters per degree longitude
			MFloat mPerLon = Util.metersPerDegreeLon(aLonLat.lon, aLonLat.lat);

			// meters per degree latitude
			MFloat mPerLat = Util.metersPerDegreeLat(aLonLat.lon, aLonLat.lat);

			// longitude tile width
			halfTileWidth = resol.Mul(new MFloat(aWidth)).Div(mPerLon).Div(TWO);

			// latitude width
			halfTileHeight = resol.Mul(new MFloat(aHeight)).Div(mPerLat).Div(TWO);

			System.out.println("zoom=" + aZoom + " resol=" + resol + " mPerLon=" + mPerLon + " mPerLat=" + mPerLat + " halfTileWidth=" + halfTileWidth);
		} else {
			BBox bboxStd = getBBox(getKeyholeRef(aLonLat, aZoom));
			halfTileWidth = bboxStd.getLonWidth().Div(TWO);
			halfTileHeight = bboxStd.getLatHeight().Div(TWO);
		}
		LonLat sw = new LonLat(aLonLat.lon.Sub(halfTileWidth), aLonLat.lat.Sub(halfTileHeight));
		LonLat ne = new LonLat(aLonLat.lon.Add(halfTileWidth), aLonLat.lat.Add(halfTileHeight));
		return new BBox(sw, ne, aWidth, aHeight);

	}

	/**
	 * Create OGC WMS request.
	 */
	public static String createWMSURL(String wmsBaseURL, BBox bbox, String mapType, int w, int h, String format) {
		return wmsBaseURL + "?service=WMS&version=1.1.1&request=GetMap&bbox=" + bbox + "&layers=" + mapType + "&width=" + w + "&height=" + h + "&format=" + format + "&srs=EPSG:4326&exceptions=application/vnd.ogc.inimage";
	}

	/**
	 * Get Tile with x = lon, y = lat, width=lonSpan, height=latSpan for a keyhole string.
	 */
	public static BBox getBBox(String keyholeStr) {
		// must start with "t"
		if ((keyholeStr == null) || (keyholeStr.length() == 0) || (keyholeStr.charAt(0) != 't')) {
			throw new RuntimeException("Keyhole string must start with 't'");
		}

		MFloat lon = DEG_MIN_180; // x
		MFloat lonWidth = DEG_360; // width 360

		//double lat = -90;  // y
		//double latHeight = 180; // height 180
		MFloat lat = MINUS_ONE;
		MFloat latHeight = TWO;

		for (int i = 1; i < keyholeStr.length(); i++) {
			lonWidth = lonWidth.Div(TWO);
			latHeight = latHeight.Div(TWO);

			char c = keyholeStr.charAt(i);

			switch (c) {
				case 's':

					// lat += latHeight;
					lon = lon.Add(lonWidth);

					break;

				case 'r':
					lat = lat.Add(latHeight);
					lon = lon.Add(lonWidth);

					break;

				case 'q':
					lat = lat.Add(latHeight);

					// lon += lonWidth;
					break;

				case 't':

					//lat += latHeight;
					//lon += lonWidth;
					break;

				default:
					throw new RuntimeException("unknown char '" + c + "' when decoding keyhole string.");
			}
		}

		// convert lat and latHeight to degrees in a transverse mercator projection
		// note that in fact the coordinates go from about -85 to +85 not -90 to 90!
		latHeight = latHeight.Add(lat);
		// latHeight = (2 * Math.atan(Math.exp(Math.PI * latHeight))) - (Math.PI / 2);
		latHeight = TWO.Mul(MFloat.atan(MFloat.exp(MFloat.PI.Mul(latHeight)))).Sub(HALF_PI);

		latHeight = latHeight.Mul(DEG_PER_RAD);

		// lat = (2 * Math.atan(Math.exp(Math.PI * lat))) - (Math.PI / 2);
		lat = TWO.Mul(MFloat.atan(MFloat.exp(MFloat.PI.Mul(lat)))).Sub(HALF_PI);
		lat = lat.Mul(DEG_PER_RAD);

		latHeight = latHeight.Sub(lat);

		if (lonWidth.Less(ZERO)) {
			lon = lon .Add(lonWidth);
			lonWidth = lonWidth.Neg();
		}

		if (latHeight.Less(ZERO)) {
			lat = lat.Add(latHeight);
			latHeight = latHeight.Neg();
		}

		BBox bbox = new BBox(new LonLat(lon, lat), new LonLat(lon, lat));
		bbox.ne.lon = bbox.sw.lon.Add(lonWidth);
		bbox.ne.lat = bbox.sw.lat.Add(latHeight);
		return bbox;
	}

	/**
	 * Returns pixel offset in tile for given lon,lat,zoom.
	 * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	 */
	public static GoogleMap.XY getPixelXY(String aLon, String aLat, int aZoom) {
		MFloat lon = MFloat.parse(aLon, 10);
		MFloat lat = MFloat.parse(aLat, 10);
		return getPixelXY(lon, lat, aZoom);
	}

	/**
	 * Returns pixel offset in GMap tile for given lon,lat,zoom.
	 * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	 */
	public static GoogleMap.XY getPixelXY(MFloat aLon, MFloat aLat, int aZoom) {

		// Number of tiles in each axis at zoom level
		MFloat tiles = MFloat.pow(TWO, new MFloat(aZoom));

		// Circumference in pixels
		MFloat circumference = F_GMAP_TILE_SIZE.Mul(tiles);
		MFloat radius = circumference.Div(TWO_PI);

		// Use radians
		MFloat longitude = MFloat.toRadians(aLon);
		MFloat latitude = MFloat.toRadians(aLat);

		// To correct for origin in top left but calculated values from center
		MFloat falseEasting = circumference.Div(TWO);
		MFloat falseNorthing = falseEasting; // circumference / 2.0D;

		// Do x
		MFloat xf = radius.Mul(longitude);
		// System.out.println("x1=" + x);

		// Correct for false easting
		xf = falseEasting.Add(xf);
		// System.out.println("x2=" + x);

		int x = (int) xf.toLong();
		int tilesXOffset = x / I_GMAP_TILE_SIZE;
		x = x - tilesXOffset * I_GMAP_TILE_SIZE;
		// System.out.println("x3=" + x + " tilesXOffset =" + tilesXOffset);

		// Do yf
		MFloat sinLat = MFloat.sin(latitude);
		//MFloat onePlusSinLat = ONE.Add(sinLat);
		//MFloat oneMinSinLat = ONE.Sub(sinLat);
		// MFloat log = MFloat.log((ONE.Add(sinLat).Div(ONE.Sub(sinLat))));
		// System.out.println(" radius=" + radius + " sinLat=" + sinLat + " onePlusSinLat=" + onePlusSinLat + " oneMinSinLat=" + oneMinSinLat + " log=" + log);

		// radius / 2.0 * Math.log( (1.0 + Math.sin(latitude)) / (1.0 - Math.sin(latitude)));
		MFloat yf = (radius.Div(TWO)).Mul(MFloat.log((ONE.Add(sinLat).Div(ONE.Sub(sinLat)))));
		// System.out.println("y1=" + yf);

		// Correct for false northing
		yf = (yf.Sub(falseNorthing)).Mul(MINUS_ONE);
		// System.out.println("y2=" + yf);

		// Number of pixels to subtract for tiles skipped (offset)
		int y = (int) yf.toLong();
		int tilesYOffset = y / I_GMAP_TILE_SIZE;
		y = y - tilesYOffset * I_GMAP_TILE_SIZE;
		// System.out.println("y3=" + yf + " tilesYOffset =" + tilesYOffset);

		// Construct result
		return new XY(x, y);
	}

	/**
	 * Returns keyhole ttring given lon,lat,zoom.
	 * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	 */
	public static String getKeyholeRef(LonLat aLonLat, int aZoom) {
		return getKeyholeRef(aLonLat.lon, aLonLat.lat, aZoom);
	}

	/**
	 * Returns pixel offset in tile for given lon,lat,zoom.
	 * see http://cfis.savagexi.com/articles/2006/05/03/google-maps-deconstructed
	 */
	public static String getKeyholeRef(String aLon, String aLat, int aZoom) {
		MFloat lon = MFloat.parse(aLon, 10);
		MFloat lat = MFloat.parse(aLat, 10);
		return getKeyholeRef(lon, lat, aZoom);
	}

	/**
	 * Get keyhole string for a longitude (x), latitude (y), and zoom
	 */
	public static String getKeyholeRef(MFloat aLon, MFloat aLat, int aZoom) {
		// zoom = 18 - zoom; obsolete for GMAP v2

		// first convert the lat lon to transverse mercator coordintes.
		//	if (lon > 180) lon -= 360;
		if (aLon.Great(DEG_180)) {
			aLon = aLon.Sub(DEG_360);
		}

		// 	lon /= 180;
		aLon = aLon.Div(DEG_180);

		// convert latitude to a range -1..+1
		// lat = Math.log(Math.tan((Math.PI / 4) + ((0.5 * Math.PI * lat) / 180)) ) / Math.PI;
		aLat = MFloat.log(MFloat.tan((QUART_PI).Add((HALF_PI.Mul(aLat).Div(DEG_180))))).Div(MFloat.PI);
		// System.out.println("lat=" + aLat);
		MFloat tLat = MINUS_ONE;
		MFloat tLon = MINUS_ONE;
		MFloat lonWidth = TWO;
		MFloat latHeight = TWO;

		String keyholeString = "t";

		for (int i = 0; i < aZoom; i++) {
			// lonWidth /= 2;
			lonWidth = lonWidth.Div(TWO);
			// latHeight /= 2;
			latHeight = latHeight.Div(TWO);

			if (tLat.Add(latHeight).Great(aLat)) {
				if (tLon.Add(lonWidth).Great(aLon)) {
					keyholeString += "t";
				} else {
					tLon = tLon.Add(lonWidth);
					keyholeString += "s";
				}
			} else {
				tLat = tLat.Add(latHeight);

				if (tLon.Add(lonWidth).Great(aLon)) {
					keyholeString += "q";
				} else {
					tLon = tLon.Add(lonWidth);
					keyholeString += "r";
				}
			}

		}

		return keyholeString;
	}
}
