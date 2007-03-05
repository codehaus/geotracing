<%!
	final static String WMS_LAYER_URL = "http://geoserver.nl/geostreets/sclmapserver.exe/softtone?REQUEST=getmap&srs=EPSG:28992&format=image/jpg&layers=layer_1,layer_2,layer_3,layer_4,layer_5,layer_6";
    final static GeoBox VALID_AREA = new GeoBox(4.85, 52.34, 4.96, 52.39);
    final static String CACHE_DIR = "/var/keyworx/webapps/n8spel.nl/map/streets/";
 %>

 <%@ include file="wms-proxy.jsp" %>
