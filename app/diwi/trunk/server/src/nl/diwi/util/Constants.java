package nl.diwi.util;

public interface Constants {

    // tablenames
    public final static String ROUTE_TABLE = "diwi_route";
    public final static String POI_TABLE = "diwi_poi";
    public final static String UGC_TABLE = "g_location";
    public final static String PREFS_TABLE = "diwi_prefs";
    public final static String LOCATION_TABLE = "diwi_poi";
    public final static String MEDIA_TABLE = "base_medium";
    public final static String LOG_TABLE = "diwi_log";
    public static final String PERSON_TABLE = "utopia_person";

    // Keyworx relationship tags
    public final static int INACTIVE_STATE = 0;
    public final static int ACTIVE_STATE = 1;
    public final static String ACTIVE_TAG = "active";

    // Keyworx services
    public final static String NAV_GET_STATE = "nav-get-state";
    public final static String NAV_GET_MAP = "nav-get-map";
    public final static String NAV_POINT = "nav-point";
    public final static String NAV_START = "nav-start";
    public final static String NAV_STOP = "nav-stop";
    public final static String NAV_ACTIVATE_ROUTE = "nav-activate-route";
    public final static String NAV_DEACTIVATE_ROUTE = "nav-deactivate-route";
    public final static String NAV_ADD_MEDIUM = "nav-add-medium";
    public final static String NAV_TOGGLE_UGC = "nav-toggle-ugc";

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
    public final static String POI_HIT_ELM = "poi-hit";
    public final static String MSG_ELM = "msg";
    public final static String UGC_HIT_ELM = "ugc-hit";
    public final static String TRIP_ELM = "trip";

    public final static String LLB_LAT_ATTR = "llbLat";
    public final static String LLB_LON_ATTR = "llbLon";
    public final static String URL_LAT_ATTR = "urtLat";
    public final static String URT_LON_ATTR = "urtLon";

    // table fields
    public final static String ID_FIELD = "id";
    public final static String OWNER_FIELD = "owner";
    public final static String VERSION_FIELD = "version";
    public final static String NAME_FIELD = "name";
    public final static String EVENTS_FIELD = "events";
    public final static String START_DATE_FIELD = "startdate";
    public final static String END_DATE_FIELD = "enddate";
    public final static String DESCRIPTION_FIELD = "description";
    public final static String CATEGORY_FIELD = "category";
    public final static String X_FIELD = "x";
    public final static String Y_FIELD = "y";
    public final static String LAT_FIELD = "lat";
    public final static String LON_FIELD = "lon";
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
    public final static String TIME_FIELD = "time";
    public final static String LOCATION_ID_FIELD = "locationid";
    public final static String INIT_FIELD = "init";

    // log types
    public final static String LOG_TRIP_TYPE = "trip";
    public final static String LOG_TRAFFIC_TYPE = "traffic";

    // trip states
    public final static String LOG_STATE_OPEN = "open";
    public final static String LOG_STATE_CLOSED = "closed";

    // poi types
    public final static int POI_BASICPOINT = 1;
    public final static int POI_STARTPOINT = 2;
    public final static int POI_ENDPOINT = 3;

    // route types
    public final static int ROUTE_TYPE_FIXED = 0;
    public final static int ROUTE_TYPE_DIRECT = 1;
    public final static int ROUTE_TYPE_GENERATED = 2;

    //properties
    public final static String KICH_POST_URL = "kichposturl";
    public final static String KICH_REST_URL = "kichresturl";
    public final static String ROUTING_SERVLET_URL = "routingservleturl";
    public final static String TEST_DATA_URL = "testdataurl";
    public final static String TEST_DATA_LOCATION = "testdatalocation";
    public final static String MEDIA_URL = "mediaurl";

    //commands for KICH service - POIServlet
    public final static String POI_INSERT_COMMAND = "insertpois";
    public final static String POI_UPDATE_COMMAND = "updatepois";
    public final static String POI_DELETE_COMMAND = "deletepois";
    public final static String RELATE_MEDIA_COMMAND = "linkmedia";
    public final static String UNRELATE_MEDIA_COMMAND = "deletelinkmedia";

    public static final int EPSG_DUTCH_RD = 28992;
    public static final int EPSG_WGS84 = 4326; //google maps lat lon

    public static final int DEFAULT_SRID = EPSG_WGS84;

    public static final int SRID_ROUTING_API = EPSG_DUTCH_RD;
    public static final int SRID_KICH_API = EPSG_DUTCH_RD;

    public static final int HIT_DISTANCE = 20;
    public static final int ROAM_DISTANCE = 20;
}
