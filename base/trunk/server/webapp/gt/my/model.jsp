<%@ page import="org.geotracing.handler.QueryHandler"%>
<%@ page import="org.keyworx.amuse.client.web.HttpConnector"%>
<%@ page import="org.keyworx.amuse.core.Amuse"%>
<%@ page import="org.keyworx.common.log.Log"%>
<%@ page import="org.keyworx.common.log.Logging"%>
<%@ page import="org.keyworx.common.util.Sys"%>
<%@ page import="org.keyworx.oase.api.Record"%>
<%@ page import="org.keyworx.plugin.tagging.logic.TagLogic"%>
<%@ page import="org.keyworx.utopia.core.util.Oase"%>
<%@ page import="javax.servlet.ServletContext"%>
<%@ page import="javax.servlet.http.HttpSession"%>
<%@ page import="java.text.SimpleDateFormat"%>
<%@ page import="org.geotracing.handler.QueryLogic"%>
<%!
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd'.'MM'.'yy-HH:mm:ss");
	public static final SimpleDateFormat DATE_ONLY_FORMAT = new SimpleDateFormat("E' 'dd' 'MMM', 'yyyy");

	/** Model attributes. */
	public static final String ATTR_PREV_CONTENT_URL = "ATTR_PREV_CONTENT_URL";
	public static final String ATTR_CONTENT_URL = "ATTR_CONTENT_URL";
	public static final String ATTR_LEFT_MENU_URL = "ATTR_LEFT_MENU_URL";
	public static final String ATTR_MODEL_STATE = "ATTR_MODEL_STATE";
	public static final String ATTR_PAGE_URL = "ATTR_PAGE_URL";
	public static final String ATTR_RESULT_MSG = "ATTR_RESULT_MSG";
	public static final String ATTR_STATUS_MSG = "ATTR_STATUS_MSG";
	public static final String ATTR_TOP_MENU_URL = "ATTR_TOP_MENU_URL";
	public static final String ATTR_USER_NAME = "ATTR_USER_NAME";
	public static final String ATTR_PASSWORD = "ATTR_PASSWORD";

	/** Model attribute values. */
	public static final String STATUS_MSG_NULL = "not logged in";
	public static final String STATUS_MSG_LOGGED_IN = "logged in as ";

	public static int MODEL_STATE_NULL = 1;
	public static int MODEL_STATE_READY = 2;
	public static int MODEL_STATE_ERROR = 3;
	public static int INT_UNDEFINED = Integer.MIN_VALUE;

	private static class Model {
		/** All model data is held in HttpSession */
		private HttpSession session;
		private ServletContext application;
		private Log log = Logging.getLog("my");

		public Model(ServletContext anApplication, HttpSession aSession) {
			session = aSession;
			application = anApplication;
			if (getState() == INT_UNDEFINED) {
				reset();
			}

			aSession.setMaxInactiveInterval(10*3600);
			// Use one Oase session
			if (getOase() == null) {
				try {
					application.setAttribute("oase", Oase.createOaseSession(getPortalName()));
				} catch (Throwable t) {
					setState(MODEL_STATE_ERROR);
					set(ATTR_RESULT_MSG, "Cannot create Oase session");
				}
			}

		}

		public Oase getOase() {
			return (Oase) application.getAttribute("oase");
		}

		public Object getObject(String aName) {
			return session.getAttribute(aName);
		}



		public String getPersonId() {
			if (!isLoggedIn()) {
				setResultMsg("geen sessie context");
				return null;
			}
			return HttpConnector.getContextParam(session, "portal-context").getChildText("personid");
		}

		public String getResultMsg() {
			return getString(ATTR_RESULT_MSG);
		}

		public int getState() {
			return getInt(ATTR_MODEL_STATE);
		}

		public int getInt(String aName) {
			try {
				return ((Integer) getObject(aName)).intValue();
			} catch (Throwable t) {
				return INT_UNDEFINED;
			}
		}

		public String getPortalName() {
			return Amuse.server.getPortal().getId();
		}

		public String getString(String aName) {
			return (String) getObject(aName);
		}

		public String getMyTagCloud() throws Exception {
			int personId = Integer.parseInt(getPersonId());
			TagLogic tagLogic = new TagLogic(getOase().getOaseSession());
			return tags2Cloud(tagLogic.getTagsBy(new int[] { personId }, -1, -1));
		}

		public String getTagCloud() throws Exception {
			TagLogic tagLogic = new TagLogic(getOase().getOaseSession());
			return tags2Cloud(tagLogic.getTags(-1, -1));
		}

		public boolean isLoggedIn() {
			return session.getAttribute(ATTR_USER_NAME) != null && HttpConnector.getContextParam(session, "portal-context") != null;
		}

		public void logInfo(String s) {
			log.info("[" + getString(ATTR_USER_NAME) + "] " + s);
		}

		public void logWarning(String s) {
			log.warn("[" + getString(ATTR_USER_NAME) + "] " + s);
		}

		public void logWarning(String s, Throwable t) {
			log.warn("[" + getString(ATTR_USER_NAME) + "] " + s, t);
		}

		public void logError(String s) {
			log.error("[" + getString(ATTR_USER_NAME) + "] " + s);
		}

		public void logError(String s, Throwable t) {
			log.error("[" + getString(ATTR_USER_NAME) + "] " + s, t);
		}

		public void reset() {
			set(ATTR_RESULT_MSG, "");
			set(ATTR_STATUS_MSG, STATUS_MSG_NULL);
			set(ATTR_USER_NAME, null);
			setState(MODEL_STATE_NULL);
			session.setMaxInactiveInterval(Integer.MAX_VALUE);
		}

		public void set(String aName, Object aValue) {
			session.setAttribute(aName, aValue);
		}

		public void set(String aName, int aValue) {
			session.setAttribute(aName, new Integer(aValue));
		}

		public void setState(int aState) {
			set(ATTR_MODEL_STATE, aState);
		}

		public void setResultMsg(String aMsg) {
			set(ATTR_RESULT_MSG, aMsg);
		}

		public Record[] query(String tables, String fields, String where, String relations, String postCond) {
			try {
				long t1 = Sys.now();
				Record[] recs = QueryLogic.getInstance().queryStore(getOase(), tables, fields, where, relations, postCond);
				long t2 = Sys.now();
				setResultMsg("Query OK cnt=" + recs.length + " delta=" + (t2-t1) + " ms");
				return recs;
			} catch (Throwable t) {
				setResultMsg("ERROR during query: " + t);
				return new Record[0];
			}
		}

		private String tags2Cloud(Record[] tags) {
			StringBuffer sb = new StringBuffer("");
			for (int i=0; i < tags.length; i++) {
				sb.append(tags[i].getStringField("name"));
				sb.append("(");
				sb.append(tags[i].getField("usecount"));
				sb.append(") ");
			}
			return sb.toString();
		}
	}

	private Model model;
%>
<%
	model = new Model(application, session);
%>
