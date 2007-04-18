package org.walkandplay.server.util;

import org.geotracing.handler.Location;

public interface Constants {
	// tablenames
	public final static String VERSION_TABLE = "wp_version";
	public final static String SCHEDULE_TABLE = "wp_schedule";
	public final static String GAMEPLAY_TABLE = "wp_gameplay";
	public final static String TASK_TABLE = "wp_task";
	public final static String GAME_TABLE = "wp_game";
	public final static String PERSON_TABLE = "utopia_person";
	public final static String TEAM_TABLE = "wp_team";
	public final static String MEDIUM_TABLE = "base_medium";
	public final static String LOCATION_TABLE = Location.TABLE_NAME;

	// tag names
	public final static String TAG_GAME = "game";
	public final static String TAG_MEDIUM = "medium";
	public final static String TAG_TASK = "task";
	public final static String TAG_TASK_HIT = "task-hit";
	public final static String TAG_MEDIUM_HIT = "medium-hit";

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
	public final static String POINT_FIELD = "point";
	public final static String PLAYERS_FIELD = "players";
	public final static String MEDIUMID_FIELD = "mediumid";
	public final static String SCORE_FIELD = "score";
	public final static String ANSWER_FIELD = "answer";
	public final static String LON_FIELD = "lon";
	public final static String LAT_FIELD = "lat";

	public final static String INVITATION_FIELD = "invitation";
	public final static String INVITATION_SEND = "invitation-send";
	public final static String INVITATION_PENDING = "invitation-pending";
	public final static String INVITATION_CONFIRMED = "invitation-confirmed";

	public final static String RELTAG_INTRO = "intro";
	public final static String RELTAG_OUTRO = "outro";
	public final static String RELTAG_MEDIUM = "medium";
	public final static String RELTAG_CREATOR = "creator";
	public final static String RELTAG_TASK = "task";

	// Location types
	public static final int LOC_TYPE_MEDIUM = Location.VAL_TYPE_MEDIUM;
	public static final int LOC_TYPE_TRACK_PT = Location.VAL_TYPE_TRACK_PT;
	public static final int LOC_TYPE_USER_LOC = Location.VAL_TYPE_USER_LOC;
	public static final int LOC_TYPE_GAME_TASK = 100;
	public static final int LOC_TYPE_GAME_MEDIUM = 101;

	// Game play states
	public final static String PLAY_STATE_SCHEDULED = "scheduled";
	public final static String PLAY_STATE_RUNNING = "running";
	public final static String PLAY_STATE_SUSPENDED = "suspended";
	public final static String PLAY_STATE_DONE = "done";


}
