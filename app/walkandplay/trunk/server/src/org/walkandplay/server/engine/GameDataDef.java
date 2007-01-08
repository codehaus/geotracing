/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.walkandplay.server.engine;


/**
 * Constants for MobGame database.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public interface GameDataDef {

    /**
     * Table names
     */
    public static final String TABLE_ANSWER = "answer";
    public static final String TABLE_ASSIGNMENT = "assignment";
    public static final String TABLE_BOOBYTRAP = "boobytrap";
    public static final String TABLE_CLOAK = "cloak";
    public static final String TABLE_CONFRONTATION = "confrontation";
    public static final String TABLE_GAME = "game";
    public static final String TABLE_PERSONAGE = "personage";
    public static final String TABLE_LOCATION = "location";
    public static final String TABLE_MEDIUM = "base_medium";
    public static final String TABLE_PLAYER = "player";
    public static final String TABLE_TEAM = "team";
    public static final String TABLE_ZONE = "zone";

    /**
     * Common field names
     */
    public static final String FIELD_ANSWER = "answer";
    public static final String FIELD_DESCRIPTION = "description";
    public static final String FIELD_INFO = "info";
    public static final String FIELD_CONTENT = "content";
    public static final String FIELD_DATE = "date";
    public static final String FIELD_EXTRA = "extra";
    public static final String FIELD_FILENAME = "filename";
    public static final String FIELD_INITIATOR = "initiator";
    public static final String FIELD_KIND = "kind";
    public static final String FIELD_LOCATION = "location";
    public static final String FIELD_MIME = "mime";
    public static final String FIELD_NAME = "name";
    public static final String FIELD_OWNER = "owner";
    public static final String FIELD_POWER = "power";
    public static final String FIELD_STATE = "state";
    public static final String FIELD_TARGET = "target";
    public static final String FIELD_TYPE = "type";
    public static final String FIELD_TEAM = "team";
    public static final String FIELD_VICTIM = "victim";
    public static final String FIELD_WINNER = "winner";
    public static final String FIELD_DETONATIONDATE = "detonationdate";
    public static final String FIELD_TIMETOLIVE = "timetolive";

    /**
     * Table-specific field names TODO Phase out
     */

    public static final String ASSIGNMENT_ANSWER = "answer";

    public static final String GAME_NAME = FIELD_NAME;
    public static final String GAME_STATE = FIELD_STATE;

    public static final String PERSONAGE_NAME = FIELD_NAME;

    public static final String PLAYER_NAME = FIELD_NAME;
    public static final String PLAYER_TYPE = FIELD_TYPE;
    public static final String PLAYER_LASTALIVE = "lastalive";

    public static final String TEAM_NAME = FIELD_NAME;
    public static final String TEAM_LOCATION = FIELD_LOCATION;
    public static final String TEAM_SCORE = "score";
    public static final String TEAM_STATE = FIELD_STATE;
    public static final String TEAM_ACTIONS = "actions";
    public static final String TEAM_RESULTS = "results";
    public static final String TEAM_ZONES = "zones";

    public static final String ZONE_NAME = FIELD_NAME;
    public static final String ZONE_DESCRIPTION = FIELD_DESCRIPTION;

    /**
     * Standard relation tags.
     */
    public static final String REL_TAG_HQ = "hq";
    public static final String REL_TAG_LQ = "lq";
    public static final String REL_TAG_KEY = "key";
    public static final String REL_TAG_1 = "1";
    public static final String REL_TAG_2 = "2";
    public static final String REL_TAG_OWNER = "owner";
    public static final String REL_TAG_VICTIM = "victim";
    public static final String REL_TAG_WINNER = "winner";
    public static final String REL_TAG_LOSER = "loser";
    public static final String REL_TAG_DRAW = "draw";
    public static final String REL_TAG_FREE = "free";
    public static final String REL_TAG_USED = "used";
    public static final String REL_TAG_EXTRA = "extra";
    public static final String REL_TAG_REQUIRED = "required";
    public static final String REL_TAG_THUMB = "thumb";

}
