<%@ page import="nl.justobjects.jox.dom.JXElement,
				 org.geotracing.handler.EventPublisher,
				 org.geotracing.handler.QueryLogic,
				 org.geotracing.handler.Track,
				 org.geotracing.handler.TrackLogic" %>
<%@ page import="org.keyworx.amuse.core.Amuse" %>
<%@ page import="org.keyworx.common.log.Log" %>
<%@ page import="org.keyworx.common.log.Logging" %>
<%@ page import="org.keyworx.common.util.Sys" %>
<%@ page import="org.keyworx.oase.api.Record" %>
<%@ page import="org.keyworx.utopia.core.util.Oase" %>
<%@ page import="javax.servlet.ServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletRequest" %>
<%@ page import="javax.servlet.http.HttpServletResponse" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.*" %>
<%!
	public static Oase oase;
	public static TrackLogic trackLogic;
	public static final String ATTR_ID = "id";
	public static final String ATTR_USER_NAME = "loginname";
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd'.'MM'.'yy-HH:mm:ss");

	// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
	// Distributable under LGPL license. See terms of license at gnu.org.

	// This JSP implements IA MiddleWare connector for Mambo.
	// $Id$

	/**
	 Berichttype 1:
	 www.mywebsite.nl/scripts/index.php& frm_Type=1
	 & frm_dcName=naamdevice
	 & frm_imei=imeicode
	 & frm_dcLcip=0.0.0.0

	 Berichttype 2:

	 www.mywebsite.nl/scripts/index.php&frm_Type=2
	 & frm_imei=imeicode
	 & frm_Utc=
	 & frm_Timestamp = yyyymmddhhmmss
	 & frm_gpLat= decimalegraden
	 & frm_gpLng= decimalegraden
	 & frm_gpSpeed=snelheid in km
	 & frm_gpTrigger= leeg|start|stop
	 & frm_gpHead= heading in decimale graden
	 */

	public static final String RESULT_CODE = "[OK]<br>\n";
	public static final String PAR_FRAME_TYPE = "frm_Type";
	public static final String PAR_DEV_NAME = "frm_dcName";
	public static final String PAR_DEV_IP = "frm_dcLcip";
	public static final String PAR_HEADING = "frm_gpHead";
	public static final String PAR_IMEI = "frm_Imei";
	public static final String PAR_LAT = "frm_gpLat";
	public static final String PAR_LON = "frm_gpLng";
	public static final String PAR_SPEED = "frm_gpSpeed";
	public static final String PAR_TIMESTAMP = "frm_Timestamp";
	public static final String PAR_TRIGGER = "frm_gpTrigger";
	public static final String PAR_UTC = "frm_Utc";

	public static final String FRAME_TYPE_ONE = "1";
	public static final String FRAME_TYPE_TWO = "2";
	public static final float MIN_SPEED = 1.0f;
	public static final float MAX_SPEED = 200.0f;
	public static final float KM_PER_KNOT = 1.85200f;

	public static Log log = Logging.getLog("mambo-iamw.jsp");


	/** Performs command and returns XML result. */
	public synchronized String doCommand(HttpServletRequest request, HttpServletResponse response) {
		// logParms(request);

		String frameType = getParameter(request, PAR_FRAME_TYPE, "unknown");
		String imei = getParameter(request, PAR_IMEI, null);
		if (imei == null) {
			log.warn("no imei code");
			return RESULT_CODE;
		}

		try {
			setup();
			Record userInfo = getUserInfoByIMEI(imei);
			int personId = userInfo.getIntField(ATTR_ID);
			String userName = userInfo.getStringField(ATTR_USER_NAME);

			if (frameType.equals(FRAME_TYPE_ONE)) {
				// {frm_Type=1}, {frm_dcName=UNNAMED MAMBO}, {frm_dcLcip=10.49.13.242}, {frm_Imei=352021009412545}
				log.info("START imei=" + imei + " user=" + userName);
			} else if (frameType.equals(FRAME_TYPE_TWO)) {
				//  {frm_gpLat=53.1997}, {frm_gpSpeed=0.11}, {frm_gpTrigger=start}, {frm_gpLng=5.7844}, {frm_Type=2}, {frm_gpHead=81.31}, {frm_Imei=352021009412545}, {frm_Timestamp=103838.120211206}

				// 2. get active track, if not create new track using user account name
				Track track = trackLogic.getActiveTrack(personId);
				if (track == null) {
					log.info("CREATE TRACK imei=" + imei + " user=" + userName);
					track = trackLogic.create(personId, userName, Track.VAL_NORMAL_TRACK, Sys.now());
				}

				// 3. get PAR_TRIGGER, if "start" do resume
				String trigger = getParameter(request, PAR_TRIGGER, "EMPTY");
				if (trigger.equals("start")) {
					log.info("RESUME TRACK imei=" + imei + " user=" + userName);
					trackLogic.resume(personId, Track.VAL_NORMAL_TRACK, Sys.now());
				}

				// 4. get PAR_LAT/PAR_LON and write to track
				JXElement pt = new JXElement("pt");
				String lonStr = getParameter(request, PAR_LON, "0.0");
				String latStr = getParameter(request, PAR_LAT, "0.0");

				if (lonStr.startsWith("0") || latStr.startsWith("0")) {
					log.warn(userName + ": ignoring 0.0 or null lon/lat");
					return RESULT_CODE;
				}

				pt.setAttr("lon", getParameter(request, PAR_LON, null));
				pt.setAttr("lat", getParameter(request, PAR_LAT, null));


				String timestamp = getParameter(request, PAR_TIMESTAMP, null);
				if (timestamp == null) {
					log.warn("no timestamp");
					return RESULT_CODE;
				}

				// Use formatted GPS timestamp
				long t = parseTimestamp(timestamp);
				pt.setAttr("t", t);

				String speedStr = getParameter(request, PAR_SPEED, "0.0");
				float speed = Float.parseFloat(speedStr) * KM_PER_KNOT;
				if (speed < MIN_SPEED || speed > MAX_SPEED) {
					// log.warn(userName + ": discard empty, small or large speed: " + speed + " km/h");
					if (speed < MIN_SPEED) {
						pt.setAttr("speed", speed);
					}
				} else {
					Vector pts = new Vector(1);
					pts.add(pt);
					trackLogic.write(pts, personId);
				}

				// may check for: minimal distance, minimal time between samples
				// 5. publish to pushlets (live events)
				EventPublisher.tracerMove(personId, userName, track.getId(), track.getName(), pt);
			} else {
				log.warn(userName + ": unknown frametype " + frameType);
			}
		} catch (IllegalArgumentException iae) {
			log.error("illegal argument", iae);
		} catch (Throwable t) {
			log.error("Unexpected Error during command", t);
		}

		return RESULT_CODE;
	}

	/** Performs command and returns XML result. */
	public void setup() throws Exception {
		// Get global Oase (DB) session.
		try {
			// Use one Oase session
			if (oase == null) {
				// First time: create and save in app context
				String oaseContextId = Amuse.server.getPortal().getId();
				oase = Oase.createOaseSession(oaseContextId);

			}
		} catch (Exception e) {
			log.error("error creating Oase session", e);
			throw e;
		}

		trackLogic = new TrackLogic(oase);
	}


	String getParameter(ServletRequest req, String name, String defaultValue) {
		String value = req.getParameter(name);
		if (value == null || value.length() == 0) {
			return defaultValue;
		}

		return value.trim();
	}


	Map getParameters(ServletRequest req) {
		return req.getParameterMap();
	}

	long parseTimestamp(String aTimestamp) {
		// example 085558.910030605 hhmmss.sssddmmyy
		// is 3.jun.2005 08:55:58 
		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		int len = aTimestamp.length();
		int hs = Integer.parseInt(aTimestamp.substring(0, 2));
		int ms = Integer.parseInt(aTimestamp.substring(2, 4));
		int ss = Integer.parseInt(aTimestamp.substring(4, 6));
		int d = Integer.parseInt(aTimestamp.substring(len - 6, len - 4));
		int m = Integer.parseInt(aTimestamp.substring(len - 4, len - 2)) - 1;
		int y = 2000 + Integer.parseInt(aTimestamp.substring(len - 2, len));
		c.set(y, m, d, hs, ms, ss);
		// p("gregDate=" + c.getTime() + " ms=" + c.getTimeInMillis());
		return c.getTimeInMillis();
	}

	/** Get user name etc from imei. */
	public Record getUserInfoByIMEI(String anIMEI) throws Exception {
		String tables = "utopia_person,utopia_account";
		String fields = "utopia_account.loginname,utopia_person.id,utopia_person.extra";
		String where = "utopia_person.extra LIKE '%" + anIMEI + "%'";
		String relations = "utopia_account,utopia_person";
		String postCond = null;
		Record[] result = QueryLogic.queryStore(oase, tables, fields, where, relations, postCond);

		if (result.length == 0) {
			throw new IllegalArgumentException("Cannot find user for imei code: " + anIMEI);
		} else if (result.length > 1) {
			throw new IllegalArgumentException("More than 1 user for imei code: " + anIMEI);
		} else {
			return result[0];
		}
	}

	/**
	 * Throw exception when parm empty or not present.
	 */
	public void throwOnMissingParm(String aName, String aValue) throws IllegalArgumentException {
		if (aValue == null || aValue.length() == 0) {
			throw new IllegalArgumentException("Missing parameter=" + aName);
		}
	}

	/** Log incoming parms. */
	private void logParms(HttpServletRequest request) {

		Map parms = getParameters(request);
		Iterator iter = parms.keySet().iterator();
		String nvs = "MAMBO ", nextParm;
		while (iter.hasNext()) {
			nextParm = (String) iter.next();
			nvs = nvs + "{" + nextParm + "=" + getParameter(request, nextParm, "EMPTY") + "}, ";
		}
		log.info(nvs);
	}
%>
<%= doCommand(request, response) %>
