<%!
	final static String WMS_LAYER_URL = "http://geoserver.nl/top25/sclmapserver.exe?REQUEST=getmap&srs=EPSG:28992&format=image/jpg&layers=layer_1";
	final static String CACHE_DIR = "/var/keyworx/webapps/n8spel.nl/map/topdag/";
    final static GeoBox VALID_AREA = new GeoBox(4.85, 52.34, 4.96, 52.39);
 %>

 <%@ include file="wms-proxy.jsp" %>
