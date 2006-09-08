<%@ page import="org.geotracing.gis.GeoPoint,
				 org.geotracing.gis.proj.WGS84toRD,
				 org.geotracing.gis.proj.XY,
				 org.keyworx.common.log.Log,
				 org.keyworx.common.log.Logging,
				 org.keyworx.common.net.NetUtil,
				 org.keyworx.common.util.IO,
				 org.keyworx.common.util.Sys,
				 org.keyworx.oase.api.OaseException,
				 javax.servlet.http.HttpServletResponse,
				 java.io.*,
				 java.net.URL,
				 java.net.URLConnection,
				 java.text.SimpleDateFormat,
				 java.util.Calendar,
				 java.util.Date,
				 java.util.Locale,
				 java.util.TimeZone" %>
<%@ page import="java.net.InetAddress" %>
<%!

	static Log log = Logging.getLog("eindhoven-wms.jsp");
	static SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.UK);

	/** Default IM command */
	static String IMAGE_MAGICK = "convert";
	static String CACHE_DIR = null;

	static {
		try {
			InetAddress localHost = InetAddress.getLocalHost();
			String hostName = localHost.getHostName();
			if (hostName.equals("bw-ds11")) {
				IMAGE_MAGICK = "C:\\Program Files\\ImageMagick-6.2.6-Q8\\convert";
				CACHE_DIR = "C:/mapcache/" + CACHE_NAME;
			}
			if (hostName.equals("sumatra")) {
				CACHE_DIR = "/var/keyworx/webapps/test.geotracing.com/mapcache/sotce/" + WMS_NAME + "/";
			} else if (hostName.equals("pundit")) {
				CACHE_DIR = "/home/httpd/geotracing.com/mapcache/sotce/" + WMS_NAME + "/";
			} else if (hostName.equals("test")) {
				CACHE_DIR = "/var/keyworx/webapps/senseofthecity.all/mapcache/sotce/" + WMS_NAME + "/";
			} else {
				throw new IllegalArgumentException("Host is not yet configured in eindhoven-wms.jsp");
			}

			log.info("Setting map CACHE_DIR=" + CACHE_DIR);
			if (!new File(CACHE_DIR).exists()) {
				IO.mkdir(CACHE_DIR);
			}
		} catch (Throwable t) {
			log.error("Cannot init cacheRoot: ", t);
		}


		dateFormat.setTimeZone(TimeZone.getTimeZone("GMT+0"));
	}


	/** Do HTTP request for URL and return content as String. */
	/* If resize!=null then resize image using imagemagic */
	public void store(String anURLString, String aFilePath, String resize) throws IOException {

		InputStream is = null;
		OutputStream os = null;

		File temp = null;			// Joran: remeber temp file so it can be deleted if needed

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
				log.warn("store(): Cannot get InputStream for URL=" + anURLString);
				throw new IOException("store(): Cannot create InputStream for URL=" + anURLString);
			}

			// Create temp file.
			temp = File.createTempFile("geodan", ".jpg");

			os = new FileOutputStream(temp);

			IO.copy(is, os);

			os.close();
			os = null;		// Joran: First close output stream before moving

			// rescale image if rescale parameter is given
			if (resize != null) {
				File origTemp = temp;
				temp = File.createTempFile("geodanscale", ".jpg");

				// Setup IM command line
				String[] resizeCmd = {IMAGE_MAGICK, "-resize", resize, origTemp.getAbsolutePath(), temp.getAbsolutePath()};

				StringBuffer stdout = new StringBuffer(24);
				StringBuffer stderr = new StringBuffer(24);

				int exitCode = Sys.execute(resizeCmd, stdout, stderr);
				if (exitCode != 0) {
					log.warn("ImageMagick error: " + stderr);
				}

				origTemp.delete();
			}
			Sys.move(temp, aFilePath);
			temp = null;
		} catch (IOException ioe) {
			log.warn("store(): error =" + anURLString, ioe);
			throw ioe;
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException ignore) {
				}
				if (os != null) {
					try {
						os.close();
					} catch (IOException ignore) {
					}
				}
			}
			if (temp != null) temp.delete(); // Joran: Clean up temp file
		}

	}

	public Date getFutureDate(int addHours) {
		Calendar c = Calendar.getInstance();
		c.add(Calendar.HOUR_OF_DAY, addHours);
		return c.getTime();
	}

	public String scaleIntStr(String intString, float scale) {
		if (scale == 1.0f) return intString;
		int value = Integer.parseInt(intString);
		value = (int) (((float) value) * scale);
		return Integer.toString(value);
	}

	public void sendFile(HttpServletResponse aResponse, String aFileName, String aMimeType) throws OaseException {
		InputStream is = null;
		OutputStream os = null;
		try {
			File file = new File(aFileName);
			if (!file.exists() || !file.canRead()) {
				throw new OaseException("Error sending file (does it exist?): " + aFileName);
			}

			// Set important headers
			aResponse.setContentType(aMimeType);
			aResponse.setContentLength((int) file.length());

			// Disable _any_ caching.
			//Servlets.setNoCache(aResponse);
			// No no, we like caching, so thet expire date to 2 days
			aResponse.setHeader("Expires", dateFormat.format(getFutureDate(48)));

			// Log.trace("START sending file=" + aFileName + " mime=[" + aMimeType + "] len=" + file.length());
			is = new BufferedInputStream(new FileInputStream(file));
			aResponse.setBufferSize(2048);
			os = aResponse.getOutputStream();
			byte[] buffer = new byte[4 * 1024];

			// The read/write loop
			int bytesRead = -1;
			while ((bytesRead = is.read(buffer)) != -1) {
				os.write(buffer, 0, bytesRead);
			}

			// To be sure
			aResponse.flushBuffer();
		} catch (Throwable e) {
			throw new OaseException("Error sending file: " + aFileName, e);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException ignore) {
			}
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

	// Reproject to Dutch RD coordinate system
	XY rdSW = WGS84toRD.calculate(dCoords[1], dCoords[0]);
	XY rdNE = WGS84toRD.calculate(dCoords[3], dCoords[2]);

	// Correction for Google Maps
	rdSW.x += MAP_TRANSLATE_X;
	rdNE.x += MAP_TRANSLATE_X;
	rdSW.y += MAP_TRANSLATE_Y;
	rdNE.y += MAP_TRANSLATE_Y;
	String cacheFile = CACHE_DIR + rdSW.x + "-" + rdNE.y + "-" + rdNE.x + "-" + rdSW.y + ".jpg";

	if (! new File(cacheFile).exists()) {
		String width = request.getParameter("WIDTH");
		String height = request.getParameter("HEIGHT");
		String wh = "&width=" + scaleIntStr(width, MAP_ANTIALIAS) + "&height=" + scaleIntStr(height, MAP_ANTIALIAS);
		String url = WMS_URL + wh + "&bbox=" + rdSW.x + "," + rdSW.y + "," + rdNE.x + "," + rdNE.y;
		log.info("Caching: url=" + url + " file=" + cacheFile);
		//System.out.println("url=" + url);
		if (MAP_ANTIALIAS != 1.0f) {
			store(url, cacheFile, width + "x" + height);
		} else {
			store(url, cacheFile, null);
		}
	}

	sendFile(response, cacheFile, "image/jpeg");
	//response.sendRedirect(CACHE_URL+ rdSW.x + "-" + rdNE.y + "-" + rdNE.x + "-" + rdSW.y + ".jpg");
%>

