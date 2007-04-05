package nl.diwi.util;

public interface Constants {
    // tablenames
    public final static String ROUTE_TABLE = "diwi_route";
    public final static String POI_TABLE = "diwi_poi";
    public final static String PREFS_TABLE = "diwi_prefs";
    public final static String LOCATION_TABLE = "diwi_poi";
    public final static String MEDIA_TABLE = "base_medium";
    public final static String TRAFFIC_TABLE = "diwi_traffic";

    // xml tags
    public final static String PREF_ELM = "pref";
    public final static String ROUTE_ELM = "route";
    public final static String POI_ELM = "poi";
    public final static String DESCRIPTION_ELM = "desc";
    public final static String NAME_ELM = "name";
    public final static String DISTANCE_ATTR = "distance";
    public final static String KICH_URI_ELM = "kich-uri";
    public final static String TRAFFIC_ELM = "traffic";
    public final static String THEME_ELM = "theme";
    
    // table fields
    public final static String ID_FIELD = "id";
    public final static String OWNER_FIELD = "owner";
    public final static String VERSION_FIELD = "version";
    public final static String NAME_FIELD = "name";
    public final static String DESCRIPTION_FIELD = "description";
    public final static String CATEGORY_FIELD = "category";
    public final static String X_FIELD = "x";
    public final static String Y_FIELD = "y";
    public final static String STATE_FIELD = "state";
    public final static String VALUE_FIELD = "value";
    public final static String PATH_FIELD = "path";
    public final static String TYPE_FIELD = "type";
    public final static String STARTDATE_FIELD = "startdate";
    public final static String ENDDATE_FIELD = "enddate";
    public final static String PLAYERS_FIELD = "players";
    public final static String MEDIA_FIELD = "media";
    public final static String POINT_FIELD = "point";
    public final static String KICHID_FIELD = "kichid";
    public final static String REQUEST_FIELD = "request";
    public final static String RESPONSE_FIELD = "response";
    public final static String DISTANCE_FIELD = "distance";
    public final static String BOUNDS_FIELD = "bounds";
    public final static String HEIGHT_FIELD = "height";
    public final static String WIDTH_FIELD = "width";
    public final static String URL_FIELD = "url";
    
    // poi types
    public final static int POI_BASICPOINT = 1;
    public final static int POI_STARTPOINT = 2;
    public final static int POI_ENDPOINT = 3;

    // route types
    public final static int ROUTE_TYPE_FIXED = 0;        
    public final static int ROUTE_TYPE_DIRECT = 1;    
    public final static int ROUTE_TYPE_GENERATED = 2; 
    
    //properties
    public final static String GENERATOR_URL = "routegeneratorurl";
	public final static String KICH_POST_URL = "kichposturl";
	public final static String KICH_REST_URL = "kichresturl";
	public final static String ROUTING_SERVLET_URL = "routingservleturl";
	public final static String TEST_DATA_URL = "testdataurl";
	public final static String TEST_DATA_LOCATION = "testdatalocation";
	public final static String MEDIA_URL = "mediaurl";

    //actions
    public final static String POI_INSERT_ACTION = "insert";
    public final static String POI_UPDATE_ACTION = "update";
    public final static String POI_DELETE_ACTION = "delete";

    
    public static final int EPSG_DUTCH_RD = 28992;
    public static final int EPSG_WGS84 = 4326; //google maps lat lon
    
	public static final int DEFAULT_SRID = EPSG_WGS84;

	public static final int SRID_ROUTING_API = EPSG_DUTCH_RD;
	public static final int SRID_KICH_API = EPSG_DUTCH_RD;
}
