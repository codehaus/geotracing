package org.walkandplay.server.util;

import org.geotracing.handler.Location;
import org.geotracing.handler.Track;

public interface Constants {
	// tablenames
	public final static String VERSION_TABLE = "wp_version";
	public final static String ACCOUNT_TABLE = "utopia_account";
	public final static String GAMEROUND_TABLE = "wp_gameround";
	public final static String GAMEPLAY_TABLE = "wp_gameplay";
	public final static String TASK_TABLE = "wp_task";
	public final static String GAME_TABLE = "wp_game";
	public final static String PERSON_TABLE = "utopia_person";
	public final static String TEAM_TABLE = "wp_team";
	public final static String MEDIUM_TABLE = "base_medium";
	public final static String LOCATION_TABLE = Location.TABLE_NAME;
	public final static String TRACK_TABLE = Track.TABLE_NAME;
	public final static String TASKRESULT_TABLE = "wp_taskresult";
	public final static String MEDIUMRESULT_TABLE = "wp_mediumresult";

	// tag names
	public final static String TAG_GAME = "game";
	public final static String TAG_GAMEPLAY = "gameplay";
	public final static String TAG_LOCATIONS = "locations";
	public final static String TAG_MEDIUM = "medium";
	public final static String TAG_TASK = "task";
	public final static String TAG_TASK_RESULT = "task-result";
	public final static String TAG_MEDIUM_RESULT = "medium-result";
	public final static String TAG_TASK_HIT = "task-hit";
	public final static String TAG_MEDIUM_HIT = "medium-hit";

	// xml attr names
	public final static String ATTR_TEAM = "team";
	public final static String ATTR_TRACKID = "trackid";
	public final static String ATTR_TASKID = "taskid";
	public final static String ATTR_MEDIUMID = "mediumid";

	// table fields
	public final static String ANSWER_FIELD = "answer";
	public final static String ANSWER_STATE_FIELD = "answerstate";
	public final static String END_DATE_FIELD = "enddate";
	public final static String GAME_ID_FIELD = "gameid";
	public final static String ID_FIELD = "id";
	public final static String OWNER_FIELD = "owner";
	public final static String VERSION_FIELD = "version";
	public final static String NAME_FIELD = "name";
	public final static String DESCRIPTION_FIELD = "description";
	public final static String STATE_FIELD = "state";
	public final static String TYPE_FIELD = "type";
	public final static String INTRO_FIELD = "intro";
	public final static String KIND_FIELD = "kind";
	public final static String LOGINNAME_FIELD = "loginname";
	public final static String LOCATION_FIELD = "location";
	public final static String LOCATION_ID_FIELD = "locationid";
	public final static String TAG_FIELD = "tag";
	public final static String ROUND_ID_FIELD = "roundid";
	public final static String STARTDATE_FIELD = "startdate";
	public final static String ENDDATE_FIELD = "enddate";
	public final static String POINT_FIELD = "point";
	public final static String EVENTS_FIELD = "events";
	public final static String PLAYERS_FIELD = "players";
	public final static String PLAY_STATE_FIELD = "playstate";
	public final static String MEDIA_STATE_FIELD = "mediastate";
	public final static String MEDIUM_ID_FIELD = "mediumid";
	public final static String MEDIUM_RESULT_ID_FIELD = "mediumresultid";
	// public final static String PLAYER_IDS_FIELD = "playerids";
	public final static String SCORE_FIELD = "score";
	public final static String LON_FIELD = "lon";
	public final static String LAT_FIELD = "lat";
	public final static String TEXT_FIELD = "text";
	public final static String RESULT_FIELD = "result";
	public final static String START_DATE_FIELD = "startdate";
	public final static String TIME_FIELD = "time";
	public final static String TASK_ID_FIELD = "taskid";
	public final static String TASK_RESULT_ID_FIELD = "taskresultid";
	public final static String TASK_STATE_FIELD = "taskstate";
	public final static String OUTRO_FIELD = "outro";

	public final static String INVITATION_FIELD = "invitation";
	public final static String INVITATION_SEND = "invitation-send";
	public final static String INVITATION_PENDING = "invitation-pending";
	public final static String INVITATION_CONFIRMED = "invitation-confirmed";

	public final static String RELTAG_INTRO = "intro";
	public final static String RELTAG_OUTRO = "outro";
	public final static String RELTAG_OWNER = "owner";
	public final static String RELTAG_PLAYER = "player";
	public final static String RELTAG_MEDIUM = "medium";
	public final static String RELTAG_CREATOR = "creator";
	public final static String RELTAG_RESULT = "result";
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

	// Task result states
	public final static String VAL_DONE = "done";
	public final static String VAL_OPEN = "open";
	public final static String VAL_HIT = "hit";
	public final static String VAL_OK = "ok";
	public final static String VAL_NOTOK = "notok";
	public final static String VAL_UNKNOWN = "unknown";

	// Radius in meters to hit locations
	public final static int HIT_RADIUS_METERS = 25;

}
