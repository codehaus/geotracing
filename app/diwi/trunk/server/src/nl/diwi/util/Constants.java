package nl.diwi.util;

public interface Constants {
    // tablenames
    public final static String ROUTE_TABLE = "diwi_route";
    public final static String POI_TABLE = "diwi_poi";
    public final static String PREFS_TABLE = "diwi_prefs";
    public final static String LOCATION_TABLE = "g_location";
    public final static String MEDIA_TABLE = "base_medium";

    // xml tags
    public final static String PREF_ELM = "pref";
    public final static String ROUTE_ELM = "route";
    public final static String POI_ELM = "poi";
    public final static String DESCRIPTION_ELM = "desc";
    public final static String NAME_ELM = "name";
    public final static String KICH_URI_ELM = "kich-uri";

    // table fields
    public final static String ID_FIELD = "id";
    public final static String OWNER_FIELD = "owner";
    public final static String VERSION_FIELD = "version";
    public final static String NAME_FIELD = "name";
    public final static String DESCRIPTION_FIELD = "description";
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

    // route types
    public final static int ROUTE_TYPE_FIXED = 0;        
    public final static int ROUTE_TYPE_DIRECT = 1;    
    public final static int ROUTE_TYPE_GENERATED = 2;
    public final static int ROUTE_TYPE_TEMP = 3;  
    
    //properties
    public final static String GENERATOR_URL = "diwi.routegenerator.url";

    //actions
    public final static String POI_INSERT_ACTION = "insert";
    public final static String POI_UPDATE_ACTION = "update";
    public final static String POI_DELETE_ACTION = "delete";

    
}
