<%@ page import="org.geotracing.gis.GeoPoint,
				 org.geotracing.gis.proj.WGS84toRD,
				 org.geotracing.gis.proj.XY,
				 org.keyworx.common.log.Log,
				 org.keyworx.common.log.Logging,
				 org.keyworx.common.net.NetUtil,
				 org.keyworx.common.util.IO,
				 org.keyworx.common.util.Sys,
				 org.keyworx.oase.util.Servlets,
				 java.io.*,
				 java.net.URL,
				 java.net.URLConnection" %>
<%!

	static Log log = Logging.getLog("geodan-wms.jsp");

	/* static {
		try {
	InetAddress localHost = InetAddress.getLocalHost();
	// System.out.println("ADDR=" + localHost.getHostAddress() + " NAME=" + localHost.getHostName());
	String hostName = localHost.getHostName();


	if (hostName.equals("sumatra")) {
		// test.geoskating.com/gt
		CACHE_DIR = "/var/keyworx/webapps/test.geoskating.com/map/";
	} else if (hostName.equals("pundit")) {
		CACHE_DIR = "/var/keyworx/webapps/geotracing.com/map/";
	}
		} catch (Throwable t) {

		}
	}  */
	// NoordWest:

	/** Do HTTP request for URL and return content as String. */
	public void store(String anURLString, String aFilePath) throws IOException {

		InputStream is = null;
		OutputStream os = null;

		try {
			URL url = NetUtil.createURL(anURLString);
			if (url == null) {
				throw new IOException("Could not create URL for " + anURLString);
			}

			URLConnection urlConnection;

			// Sends HTTP request. Both NS (HTTP/1.0 keep-alive) and IE (HTTP/1.1)
			// will attempt to reuse an underlying HTTP connection to the server.
			urlConnection = url.openConnection();

			// Disable any kind of caching by the applet engine (browser).
			urlConnection.setUseCaches(false);

			// Disable presenting pop-ups for cookie acceptance
			// and HTTP authorization.
			urlConnection.setAllowUserInteraction(false);

			// Receive stream for response
			is = urlConnection.getInputStream();

			if (is == null) {
				throw new IOException("store(): Cannot create InputStream for URL=" + anURLString);
			}

			// Create unique-named temp file.
			File temp = File.createTempFile("geodan", ".jpg");

			// Delete temp file when program exits.
			temp.deleteOnExit();

			os = new FileOutputStream(temp);
			
			IO.copy(is, os);

			// Close streams before moving file
			try {
				is.close();
				os.close();
			} catch (IOException ioe) {
				throw ioe;
			}

			// Move from temp to store
			Sys.move(temp, aFilePath);
		} catch (IOException ioe) {
			throw ioe;
		}

	}
%>
<%
	String bbox = request.getParameter("BBOX");
	String coords[] = bbox.split(",");
	// log.info("request bbox=" + bbox);

	double dCoords[] = new double[4];
	for (int i = 0; i < coords.length; i++) {
		dCoords[i] = Double.parseDouble(coords[i]);
	}

	if (!VALID_AREA.isInside(new GeoPoint(dCoords[0], dCoords[1])) ||
			!VALID_AREA.isInside(new GeoPoint(dCoords[2], dCoords[3]))) {
		response.sendRedirect("img/greysquare.jpg");
		return;
	}

	// Convert to NL RD-coordinates (SRS 28992)
	XY rdSW = WGS84toRD.calculate(dCoords[1], dCoords[0]);
	XY rdNE = WGS84toRD.calculate(dCoords[3], dCoords[2]);

	// Cached filename
	String cacheFile = CACHE_DIR + rdSW.x + "-" + rdNE.y + "-" + rdNE.x + "-" + rdSW.y + ".jpg";

	// Determine if file should be cached
	if (! new File(cacheFile).exists()) {
		log.info("Caching: " + cacheFile);
		String width = request.getParameter("WIDTH");
		String height = request.getParameter("HEIGHT");
		String wh = "&width=" + width + "&height=" + height;
		String url = WMS_LAYER_URL + wh + "&bbox=" + rdSW.x + "," + rdNE.y + "," + rdNE.x + "," + rdSW.y;
		// System.out.println("url=" + url);
		store(url, cacheFile);
	}

	// Always send cached map image.
	Servlets.sendFile(response, cacheFile, "image/jpeg");

%>

