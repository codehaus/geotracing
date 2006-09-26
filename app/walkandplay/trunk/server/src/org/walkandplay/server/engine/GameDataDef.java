/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.walkandplay.server.engine;


/**
 * Constants for MobGame database.
 *
 * @author Just van den Broecke
 * @version $Id: GameDataDef.java,v 1.1.1.1 2006/04/03 09:21:35 rlenz Exp $
 */
public interface GameDataDef {
    /**
     * Portal name
     */
    public static final String PORTAL_NAME = "walkandplay";

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

/*
 * $Log: GameDataDef.java,v $
 * Revision 1.1.1.1  2006/04/03 09:21:35  rlenz
 * Import of Mobgame
 *
 * Revision 1.25  2005/01/28 13:56:21  just
 * thumb stuff zone intro
 *
 * Revision 1.24  2004/12/15 13:01:33  just
 * *** empty log message ***
 *
 * Revision 1.23  2004/12/15 11:31:22  just
 * *** empty log message ***
 *
 * Revision 1.22  2004/12/14 10:13:30  just
 * *** empty log message ***
 *
 * Revision 1.21  2004/12/09 09:09:56  just
 * *** empty log message ***
 *
 * Revision 1.20  2004/12/07 16:26:17  just
 * *** empty log message ***
 *
 * Revision 1.19  2004/12/06 16:24:46  just
 * added answer-get service
 *
 * Revision 1.18  2004/12/02 12:01:02  just
 * *** empty log message ***
 *
 * Revision 1.17  2004/11/30 10:55:05  just
 * *** empty log message ***
 *
 * Revision 1.16  2004/11/29 15:15:41  just
 * *** empty log message ***
 *
 * Revision 1.15  2004/11/22 21:23:08  just
 * *** empty log message ***
 *
 * Revision 1.14  2004/11/22 13:04:30  just
 * boobytrap placing implemented
 *
 * Revision 1.13  2004/11/16 14:07:07  just
 * *** empty log message ***
 *
 * Revision 1.12  2004/11/16 10:39:55  just
 * *** empty log message ***
 *
 * Revision 1.11  2004/11/15 15:28:54  just
 * *** empty log message ***
 *
 * Revision 1.10  2004/11/15 14:37:18  just
 * *** empty log message ***
 *
 * Revision 1.9  2004/11/10 14:15:15  just
 * *** empty log message ***
 *
 * Revision 1.8  2004/11/10 13:07:44  just
 * *** empty log message ***
 *
 * Revision 1.7  2004/11/02 15:54:58  just
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/02 13:39:34  just
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/01 16:39:17  just
 * *** empty log message ***
 *
 * Revision 1.4  2004/10/21 13:32:30  just
 * location indb
 *
 * Revision 1.3  2004/10/14 21:40:37  just
 * *** empty log message ***
 *
 * Revision 1.2  2004/10/14 15:50:55  just
 * *** empty log message ***
 *
 *
 */

