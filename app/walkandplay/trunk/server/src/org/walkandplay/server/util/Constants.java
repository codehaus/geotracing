package org.walkandplay.server.util;

public interface Constants {
    // tablenames
    public final static String VERSION_TABLE = "version";
    public final static String TOUR_TABLE = "tour";
    public final static String GAMEPLAY_TABLE = "gameplay";
    public final static String ASSIGNMENT_TABLE = "assignment";
    public final static String GAME_TABLE = "game";
    public final static String TEAM_TABLE = "team";
    public final static String POI_TABLE = "g_poi";

    // table fields
    public final static String ID_FIELD = "id";
	public final static String OWNER_FIELD = "owner";
    public final static String VERSION_FIELD = "version";
    public final static String NAME_FIELD = "name";
    public final static String DESCRIPTION_FIELD = "description";
    public final static String STATE_FIELD = "state";
    public final static String TYPE_FIELD = "type";
    public final static String LOCATION_FIELD = "location";
	public final static String TAGS_FIELD = "tags";

    public final static String INVITATION = "invitation";
    public final static String INVITATION_SEND = "invitation-send";
    public final static String INVITATION_PENDING = "invitation-pending";
    public final static String INVITATION_CONFIRMED = "invitation-confirmed";

    public final static String RELTAG_INTRO = "intro";
    public final static String RELTAG_OUTRO = "outro";
	public final static String RELTAG_MEDIUM = "medium";

}
