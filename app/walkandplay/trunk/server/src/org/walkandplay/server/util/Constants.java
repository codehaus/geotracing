package org.walkandplay.server.util;

public interface Constants {
    // tablenames
    public final static String VERSION_TABLE = "wp_version";
    public final static String SCHEDULE_TABLE = "wp_schedule";
    public final static String GAMEPLAY_TABLE = "wp_gameplay";
    public final static String TASK_TABLE = "wp_task";
    public final static String GAME_TABLE = "wp_game";
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
	public final static String TAG_FIELD = "tag";
	public final static String SCHEDULE_ID_FIELD = "scheduleid";
	public final static String STARTDATE_FIELD = "startdate";
	public final static String ENDDATE_FIELD = "enddate";
	public final static String PLAYERS_FIELD = "players";

    public final static String INVITATION_FIELD = "invitation";
    public final static String INVITATION_SEND = "invitation-send";
    public final static String INVITATION_PENDING = "invitation-pending";
    public final static String INVITATION_CONFIRMED = "invitation-confirmed";

    public final static String RELTAG_INTRO = "intro";
    public final static String RELTAG_OUTRO = "outro";
	public final static String RELTAG_MEDIUM = "medium";
	public final static String RELTAG_CREATOR = "creator";

}
