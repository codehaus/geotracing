<%@ page import="org.geotracing.gis.GeoBox" %>
<%!
	final static String WMS_URL = "http://www.geogids.info:8080/liteview6.5/servlet/MapGuideLiteView?REQUEST=GETMAP&VERSION=1.1.1&FORMAT=image/jpg&SRS=ADSK:Netherlands-RDNew&LAYERS=SENSEOFCITY_LUFO.MWF&STYLES=";
	final static String WMS_NAME = "eindhoven-sat";
	final static String CACHE_NAME = "eindhoven-sat-cache";
	final static GeoBox VALID_AREA = new GeoBox(5.30, 51.37, 5.60, 51.53);
	final static float MAP_ANTIALIAS = 2.0f;
	final static int MAP_TRANSLATE_X = 30;
	final static int MAP_TRANSLATE_Y = 15;
%>

<%@ include file="eindhoven-proxy-wms.jsp" %>

