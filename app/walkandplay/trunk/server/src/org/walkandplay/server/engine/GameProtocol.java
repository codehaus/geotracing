/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.walkandplay.server.engine;

import org.keyworx.amuse.core.Protocol;


/**
 * Constants for the MobGame protocol.
 *
 * @author Just van den Broecke
 * @version $Id: GameProtocol.java,v 1.1.1.1 2006/04/03 09:21:35 rlenz Exp $
 */
public interface GameProtocol {
    /**
     * MobGame Protocol version.
     */
    public static final String PROTOCOL_VERSION = "1.0";

    /**
     * Use Postfixes from Amuse protocol (-req/-rsp etc)
     */
    public static final String POSTFIX_REQ = Protocol.POSTFIX_REQ;
    public static final String POSTFIX_RSP = Protocol.POSTFIX_RSP;
    public static final String POSTFIX_NRSP = Protocol.POSTFIX_NRSP;
    public static final String POSTFIX_IND = Protocol.POSTFIX_IND;

    /**
     * Service id's
     */
    public static final String SERVICE_ANSWER_GET = "answer-get";
    public static final String SERVICE_ANSWER_SUBMIT = "answer-submit";
    public static final String SERVICE_ASSIGNMENT_GET = "assignment-get";
    public static final String SERVICE_BOOBYTRAP_GET = "boobytrap-get";
    public static final String SERVICE_BOOBYTRAP_PLACE = "boobytrap-place";
    public static final String SERVICE_CLOAK = "cloak";
    public static final String SERVICE_CONFRONT = "confront";
    public static final String SERVICE_CONTENT_PUT = "content-put";
    public static final String SERVICE_GPS_DATA = "gps-data";
    public static final String SERVICE_GPS_STATUS = "gps-status";
    public static final String SERVICE_GAME_GET = "game-get";
    public static final String SERVICE_GAME_RESET = "game-reset";
    public static final String SERVICE_GAME_STATUS = "game-status";
    public static final String SERVICE_GAME_SETSTATE = "game-setstate";
    public static final String SERVICE_JOIN = "join-amulet";
    public static final String SERVICE_LEAVE = "leave-amulet";
    public static final String SERVICE_LOCATION_GET = "location-get";
    public static final String SERVICE_MEDIA_GET = "media-get";
    public static final String SERVICE_MEDIA_LINK = "media-link";
    public static final String SERVICE_MEDIUM_DELETE = "medium-delete";
    public static final String SERVICE_MEDIUM_SEND = "medium-send";
    public static final String SERVICE_MEDIUM_UPLOAD = "medium-upload";
    public static final String SERVICE_MEDIUM_MAIL_UPLOAD = "medium-mail-upload";
    public static final String SERVICE_MEDIUM_RAW_UPLOAD = "medium-raw-upload";
    public static final String SERVICE_TEAM_ACTION = "team-action";
    public static final String SERVICE_TEAM_GET = "team-get";
    public static final String SERVICE_TEAM_SET = "team-set";
    public static final String SERVICE_TEAM_RESET = "team-reset";
    public static final String SERVICE_TEAM_STATUS = "team-status";
    public static final String SERVICE_CLOCK_TICK = "clock-tick";
    public static final String SERVICE_ZONE_SELECT = "zone-select";
    public static final String SERVICE_ZONE_GET = "zone-get";

    /**
     * All protocol message tags
     */
    public static final String MSG_ANSWER_GET_REQ = SERVICE_ANSWER_GET + POSTFIX_REQ;
    public static final String MSG_ANSWER_GET_RSP = SERVICE_ANSWER_GET + POSTFIX_RSP;

    public static final String MSG_ANSWER_SUBMIT_REQ = SERVICE_ANSWER_SUBMIT + POSTFIX_REQ;
    public static final String MSG_ANSWER_SUBMIT_RSP = SERVICE_ANSWER_SUBMIT + POSTFIX_RSP;
    public static final String MSG_ANSWER_SUBMIT_NRSP = SERVICE_ANSWER_SUBMIT + POSTFIX_NRSP;

    public static final String MSG_ASSIGNMENT_GET_REQ = SERVICE_ASSIGNMENT_GET + POSTFIX_REQ;
    public static final String MSG_ASSIGNMENT_GET_RSP = SERVICE_ASSIGNMENT_GET + POSTFIX_RSP;

    public static final String MSG_BOOBYTRAP_GET_REQ = SERVICE_BOOBYTRAP_GET + POSTFIX_REQ;
    public static final String MSG_BOOBYTRAP_GET_RSP = SERVICE_BOOBYTRAP_GET + POSTFIX_RSP;

    public static final String MSG_BOOBYTRAP_PLACE_REQ = SERVICE_BOOBYTRAP_PLACE + POSTFIX_REQ;
    public static final String MSG_BOOBYTRAP_PLACE_RSP = SERVICE_BOOBYTRAP_PLACE + POSTFIX_RSP;
    public static final String MSG_BOOBYTRAP_PLACE_IND = SERVICE_BOOBYTRAP_PLACE + POSTFIX_IND;
    public static final String MSG_BOOBYTRAP_PLACE_NRSP = SERVICE_BOOBYTRAP_PLACE + POSTFIX_NRSP;

    public static final String MSG_CLOAK_REQ = SERVICE_CLOAK + POSTFIX_REQ;
    public static final String MSG_CLOAK_RSP = SERVICE_CLOAK + POSTFIX_RSP;
    public static final String MSG_CLOAK_NRSP = SERVICE_CLOAK + POSTFIX_NRSP;

    public static final String MSG_CONFRONT_IND = SERVICE_CONFRONT + POSTFIX_IND;

    public static final String MSG_CONTENT_PUT_REQ = SERVICE_CONTENT_PUT + POSTFIX_REQ;
    public static final String MSG_CONTENT_PUT_RSP = SERVICE_CONTENT_PUT + POSTFIX_RSP;
    public static final String MSG_CONTENT_PUT_NRSP = SERVICE_CONTENT_PUT + POSTFIX_NRSP;

    public static final String MSG_GPS_DATA_IND = SERVICE_GPS_DATA + POSTFIX_IND;
    public static final String MSG_GPS_STATUS_IND = SERVICE_GPS_STATUS + POSTFIX_IND;

    public static final String MSG_JOIN_REQ = SERVICE_JOIN + POSTFIX_REQ;
    public static final String MSG_LEAVE_REQ = SERVICE_LEAVE + POSTFIX_REQ;

    public static final String MSG_GAME_GET_REQ = SERVICE_GAME_GET + POSTFIX_REQ;
    public static final String MSG_GAME_GET_RSP = SERVICE_GAME_GET + POSTFIX_RSP;

    public static final String MSG_GAME_RESET_REQ = SERVICE_GAME_RESET + POSTFIX_REQ;
    public static final String MSG_GAME_RESET_RSP = SERVICE_GAME_RESET + POSTFIX_RSP;
    public static final String MSG_GAME_RESET_NRSP = SERVICE_GAME_RESET + POSTFIX_NRSP;

    public static final String MSG_GAME_SETSTATE_REQ = SERVICE_GAME_SETSTATE + POSTFIX_REQ;
    public static final String MSG_GAME_SETSTATE_RSP = SERVICE_GAME_SETSTATE + POSTFIX_RSP;

    public static final String MSG_GAME_STATUS_IND = SERVICE_GAME_STATUS + POSTFIX_IND;

    public static final String MSG_LOCATION_GET_REQ = SERVICE_LOCATION_GET + POSTFIX_REQ;
    public static final String MSG_LOCATION_GET_RSP = SERVICE_LOCATION_GET + POSTFIX_RSP;

    public static final String MSG_MEDIA_GET_REQ = SERVICE_MEDIA_GET + POSTFIX_REQ;
    public static final String MSG_MEDIA_GET_RSP = SERVICE_MEDIA_GET + POSTFIX_RSP;

    public static final String MSG_MEDIA_LINK_REQ = SERVICE_MEDIA_LINK + POSTFIX_REQ;
    public static final String MSG_MEDIA_LINK_RSP = SERVICE_MEDIA_LINK + POSTFIX_RSP;
    public static final String MSG_MEDIA_LINK_IND = SERVICE_MEDIA_LINK + POSTFIX_IND;
    public static final String MSG_MEDIA_LINK_NRSP = SERVICE_MEDIA_LINK + POSTFIX_NRSP;

    public static final String MSG_MEDIUM_DELETE_REQ = SERVICE_MEDIUM_DELETE + POSTFIX_REQ;
    public static final String MSG_MEDIUM_DELETE_RSP = SERVICE_MEDIUM_DELETE + POSTFIX_RSP;

    public static final String MSG_MEDIUM_SEND_REQ = SERVICE_MEDIUM_SEND + POSTFIX_REQ;
    public static final String MSG_MEDIUM_SEND_RSP = SERVICE_MEDIUM_SEND + POSTFIX_RSP;
    public static final String MSG_MEDIUM_SEND_NRSP = SERVICE_MEDIUM_SEND + POSTFIX_NRSP;
    public static final String MSG_MEDIUM_SEND_IND = SERVICE_MEDIUM_SEND + POSTFIX_IND;

    public static final String MSG_MEDIUM_UPLOAD_REQ = SERVICE_MEDIUM_UPLOAD + POSTFIX_REQ;
    public static final String MSG_MEDIUM_UPLOAD_RSP = SERVICE_MEDIUM_UPLOAD + POSTFIX_RSP;
    public static final String MSG_MEDIUM_MAIL_UPLOAD_IND = SERVICE_MEDIUM_MAIL_UPLOAD + POSTFIX_IND;
    public static final String MSG_MEDIUM_RAW_UPLOAD_REQ = SERVICE_MEDIUM_RAW_UPLOAD + POSTFIX_REQ;
    public static final String MSG_MEDIUM_RAW_UPLOAD_RSP = SERVICE_MEDIUM_RAW_UPLOAD + POSTFIX_RSP;
    public static final String MSG_MEDIUM_UPLOAD_IND = SERVICE_MEDIUM_UPLOAD + POSTFIX_IND;

    public static final String MSG_TEAM_ACTION_IND = SERVICE_TEAM_ACTION + POSTFIX_IND;

    public static final String MSG_TEAM_GET_REQ = SERVICE_TEAM_GET + POSTFIX_REQ;
    public static final String MSG_TEAM_GET_RSP = SERVICE_TEAM_GET + POSTFIX_RSP;

    public static final String MSG_TEAM_RESET_REQ = SERVICE_TEAM_RESET + POSTFIX_REQ;
    public static final String MSG_TEAM_RESET_RSP = SERVICE_TEAM_RESET + POSTFIX_RSP;

    public static final String MSG_TEAM_SET_REQ = SERVICE_TEAM_SET + POSTFIX_REQ;
    public static final String MSG_TEAM_SET_RSP = SERVICE_TEAM_SET + POSTFIX_RSP;

    public static final String MSG_TEAM_STATUS_IND = SERVICE_TEAM_STATUS + POSTFIX_IND;

    public static final String MSG_CLOCK_TICK_IND = SERVICE_CLOCK_TICK + POSTFIX_IND;

    public static final String MSG_ZONE_SELECT_REQ = SERVICE_ZONE_SELECT + POSTFIX_REQ;
    public static final String MSG_ZONE_SELECT_RSP = SERVICE_ZONE_SELECT + POSTFIX_RSP;
    public static final String MSG_ZONE_SELECT_IND = SERVICE_ZONE_SELECT + POSTFIX_IND;

    public static final String MSG_ZONE_GET_REQ = SERVICE_ZONE_GET + POSTFIX_REQ;
    public static final String MSG_ZONE_GET_RSP = SERVICE_ZONE_GET + POSTFIX_RSP;

    /**
     * String for matching Admin requests
     */
    public static final String ADMIN_REQUESTS =
            MSG_CLOCK_TICK_IND +
                    MSG_JOIN_REQ +
                    MSG_LEAVE_REQ +
                    MSG_ANSWER_GET_REQ +
                    MSG_CONTENT_PUT_REQ +
                    MSG_GAME_GET_REQ +
                    MSG_GAME_SETSTATE_REQ +
                    MSG_TEAM_GET_REQ +
                    MSG_TEAM_RESET_REQ +
                    MSG_TEAM_SET_REQ +
                    MSG_ZONE_GET_REQ;

    /**
     * Message to/from types.
     */
    public static final String TYPE_ALL = "ALL";
    public static final String TYPE_ALL_BUT_SENDER = "ALL_BUT_SENDER";
    public static final String TYPE_ENGINE = "ENGINE";
    public static final String TYPE_SINGLE = "SINGLE";
    public static final String TYPE_MULTI = "MULTI";

    /**
     * Standard tag names
     */
    public static final String TAG_ACTIONS = "actions";
    public static final String TAG_ACTION = "action";
    public static final String TAG_ASSIGNMENT = "assignment";
    public static final String TAG_BOOBYTRAP = "boobytrap";
    public static final String TAG_BOOBYTRAPS = "boobytraps";
    public static final String TAG_CONTENT = "content";
    public static final String TAG_CLOAK = "cloak";
    public static final String TAG_CONFRONTATION = "confrontation";
    public static final String TAG_DATA = "data";
    public static final String TAG_ESCAPE = "escape";
    public static final String TAG_GAME = "game";
    public static final String TAG_LOCATION = "location";
    public static final String TAG_MEDIA = "media";
    public static final String TAG_MEDIUM = "medium";
    public static final String TAG_OPTIONS = "options";
    public static final String TAG_PLAYERS = "players";
    public static final String TAG_PLAYER = "player";
    public static final String TAG_RESULT = "result";
    public static final String TAG_RESULTS = "results";
    public static final String TAG_SCORE = "score";
    public static final String TAG_TEAM = "team";
    public static final String TAG_TEAMLIST = "teamlist";
    public static final String TAG_TEASER = "teaser";
    public static final String TAG_ZONE = "zone";
    public static final String TAG_ZONES = "zones";

    /**
     * Standard attribute names
     */
    public static final String ATTR_ACTION = "action";
    public static final String ATTR_ACTIVE = "active";
    public static final String ATTR_ANSWERID = "answerid";
    public static final String ATTR_AGENTID = "agentid";
    public static final String ATTR_ASSIGNMENTID = "assignmentid";
    public static final String ATTR_ASSIGNMENTID1 = "assignmentid1";
    public static final String ATTR_ASSIGNMENTID2 = "assignmentid2";
    public static final String ATTR_BOOBYTRAPS = "boobytraps";
    public static final String ATTR_CLOAKED = "cloaked";
    public static final String ATTR_CLOAKS = "cloaks";
    public static final String ATTR_CORRECT = "correct";
    public static final String ATTR_DATA = "data";
    public static final String ATTR_DESCRIPTION = "description";
    public static final String ATTR_ERROR = "error";
    public static final String ATTR_ENCODING = "encoding";
    public static final String ATTR_EVENT = "event";
    public static final String ATTR_EXTRA = "extra";
    public static final String ATTR_FROM = "from";
    public static final String ATTR_FILE = "file";
    public static final String ATTR_GPS = "gps";
    public static final String ATTR_HQMEDIUMID = "hqmediumid";
    public static final String ATTR_ID = "id";
    public static final String ATTR_INITIATOR = "initiator";
    public static final String ATTR_KEYASSIGNMENTID = "keyassignmentid";
    // public static final String ATTR_LASTALIVE = "lastalive";
    public static final String ATTR_LABEL = "label";
    public static final String ATTR_LQMEDIUMID = "lqmediumid";
    public static final String ATTR_MEDIAURL = "mediaurl";
    public static final String ATTR_MIMETYPE = "mimetype";
    public static final String ATTR_MEDIUM = "medium";
    public static final String ATTR_NAME = "name";
    public static final String ATTR_ONLINE = "online";
    public static final String ATTR_OWNER = "owner";
    public static final String ATTR_POINTS = "points";
    public static final String ATTR_QUALITY = "q";
    public static final String ATTR_PERSONAGENAME = "personagename";
    public static final String ATTR_REASON = "reason";
    public static final String ATTR_REQIMAGES = "reqimages";
    public static final String ATTR_REQVIDEOS = "reqvideos";
    public static final String ATTR_SCORE = "score";
    public static final String ATTR_SELECT = "select";
    public static final String ATTR_STATE = "state";
    public static final String ATTR_STATUS = "status";
    public static final String ATTR_SUBJECT = "subject";
    public static final String ATTR_TARGET = "target";
    public static final String ATTR_TEAMNAME = "teamname";
    public static final String ATTR_TEXT = "text";
    public static final String ATTR_THUMB = "thumb";
    public static final String ATTR_TIME_PASSED = "timepassed";
    public static final String ATTR_TIMETOLIVE = "timetolive";
    public static final String ATTR_TIME = "time";
    public static final String ATTR_TO = "to";
    public static final String ATTR_TYPE = "type";
    public static final String ATTR_URL = "url";
    public static final String ATTR_VICTIM = "victim";
    public static final String ATTR_WINNER = "winner";
    public static final String ATTR_ZONES = "zones";
    public static final String ATTR_ZONENAME = "zonename";

    /// Location attrs
    public static final String ATTR_LAT = "lat";
    public static final String ATTR_LON = "lon";
    public static final String ATTR_RX = "rx";
    public static final String ATTR_RY = "ry";
    public static final String ATTR_MX = "mx";
    public static final String ATTR_MY = "my";

    /**
     * Standard attr values
     */
    public static final String VAL_ASSIGNMENT = "assignment";
    public static final String VAL_ACTIVE = "active";
    public static final String VAL_BOOBYTRAP = "boobytrap";
    public static final String VAL_CONFRONT = "confront";
    public static final String VAL_DOWN = "down";
    public static final String VAL_NA = "na";
    public static final String VAL_NO = "no";
    public static final String VAL_UP = "up";
    public static final String VAL_YES = "yes";
    public static final String VAL_MEDIADONE = "mediadone";
    public static final String VAL_DONE = "done";
    public static final String VAL_TODO = "todo";
    public static final String VAL_IMAGE = "image";
    public static final String VAL_PLACED = "placed";
    public static final String VAL_TEXT = "text";
    public static final String VAL_VIDEO = "video";
    public static final String VAL_NONE = "none";
    public static final String VAL_GAME_OBSERVER = "observer";
    public static final String VAL_GAME_PHONE = "gamephone";
    public static final String VAL_VIDEO_PHONE = "videophone";
    public static final String VAL_HQ = "hq";
    public static final String VAL_MEDIA_URI = "mobgame/media.srv";
    public static final String VAL_BEWIJS = "bewijs";
    public static final String VAL_POORTER = "poorter";
    public static final String VAL_VUUR = "vuur";
    public static final String VAL_WATER = "water";
    public static final String VAL_HOUT = "hout";
    public static final String VAL_TRUE = "true";
    public static final String VAL_FALSE = "false";

    public static final String VAL_ACTION_ZONEENTER = "zoneenter";
    public static final String VAL_ACTION_DOZONEINTRO = "dozoneintro";
    public static final String VAL_ACTION_DOASSIGNMENT = "doassignment";
    public static final String VAL_ACTION_BOOBYTRAPPED = "boobytrapped";

    public static final String EV_STATUS_BOOBYTRAPPED = "boobytrapped";
    public static final String EV_STATUS_BOOBYTRAPDONE = "boobytrapdone";
    public static final String EV_STATUS_CLOAKED = "cloaked";
    public static final String EV_STATUS_CLOAKDONE = "cloakdone";
    public static final String EV_STATUS_SCORE = "score";
    public static final String EV_STATUS_ZONE = "zone";
    public static final String EV_STATUS_GPS = "gps";

    /**
     * Game states.
     */
    public static final String STATE_GAME_NULL = "null";
    public static final String STATE_GAME_ACTIVE = "active";
    public static final String STATE_GAME_PAUSED = "paused";
    public static final String STATE_GAME_STOPPED = "stopped";

    /**
     * Zone states for team
     */
    public static final String STATE_ZONE_TODO = "todo";
    /**
     * Selected
     */
    public static final String STATE_ZONE_SELECTED = "selected";
    /**
     * Going to or doing key assignment
     */
    public static final String STATE_ZONE_KEY_TODO = "keytodo";
    /**
     * Two poorters to do
     */
    public static final String STATE_ZONE_TWO_TODO = "twotodo";
    /**
     * One poorter to do
     */
    public static final String STATE_ZONE_ONE_TODO = "onetodo";
    /**
     * All done.
     */
    public static final String STATE_ZONE_DONE = "done";

    /**
     * Score values.
     */
    public static final int POINTS_CONFRONT = 15;
    public static final int POINTS_BOOBYTRAP = 10;


}

/*
 * $Log: GameProtocol.java,v $
 * Revision 1.1.1.1  2006/04/03 09:21:35  rlenz
 * Import of Mobgame
 *
 * Revision 1.27  2005/01/28 13:56:21  just
 * thumb stuff zone intro
 *
 * Revision 1.26  2005/01/26 20:51:12  just
 * added thumb adds
 *
 * Revision 1.25  2004/12/23 13:55:47  just
 * *** empty log message ***
 *
 * Revision 1.24  2004/12/15 11:31:22  just
 * *** empty log message ***
 *
 * Revision 1.23  2004/12/14 10:13:30  just
 * *** empty log message ***
 *
 * Revision 1.22  2004/12/14 09:38:45  just
 * *** empty log message ***
 *
 * Revision 1.21  2004/12/13 16:11:25  just
 * *** empty log message ***
 *
 * Revision 1.20  2004/12/13 14:15:49  just
 * *** empty log message ***
 *
 * Revision 1.19  2004/12/09 09:09:56  just
 * *** empty log message ***
 *
 * Revision 1.18  2004/12/08 12:19:58  just
 * *** empty log message ***
 *
 * Revision 1.17  2004/12/07 16:26:17  just
 * *** empty log message ***
 *
 * Revision 1.16  2004/12/06 16:24:46  just
 * added answer-get service
 *
 * Revision 1.15  2004/12/06 14:11:50  just
 * zone-select
 *
 * Revision 1.14  2004/12/02 12:01:02  just
 * *** empty log message ***
 *
 * Revision 1.13  2004/11/30 16:29:55  just
 * *** empty log message ***
 *
 * Revision 1.12  2004/11/30 10:55:05  just
 * *** empty log message ***
 *
 * Revision 1.11  2004/11/29 17:25:04  just
 * *** empty log message ***
 *
 * Revision 1.10  2004/11/29 15:15:41  just
 * *** empty log message ***
 *
 * Revision 1.9  2004/11/29 11:30:07  just
 * *** empty log message ***
 *
 * Revision 1.8  2004/11/29 10:34:11  just
 * *** empty log message ***
 *
 * Revision 1.7  2004/11/25 14:13:13  just
 * *** empty log message ***
 *
 * Revision 1.6  2004/11/24 16:07:11  just
 * *** empty log message ***
 *
 * Revision 1.5  2004/11/23 15:37:20  just
 * *** empty log message ***
 *
 * Revision 1.4  2004/11/22 21:23:08  just
 * *** empty log message ***
 *
 * Revision 1.3  2004/11/22 17:14:30  just
 * *** empty log message ***
 *
 * Revision 1.2  2004/11/22 13:04:30  just
 * boobytrap placing implemented
 *
 * Revision 1.1  2004/11/22 10:12:35  just
 * *** empty log message ***
 *
 * Revision 1.34  2004/11/18 16:07:58  just
 * *** empty log message ***
 *
 * Revision 1.33  2004/11/18 13:55:47  just
 * *** empty log message ***
 *
 * Revision 1.32  2004/11/17 17:14:40  just
 * alles met vragen gedaan
 *
 * Revision 1.31  2004/11/16 16:09:16  just
 * *** empty log message ***
 *
 * Revision 1.30  2004/11/16 14:07:07  just
 * *** empty log message ***
 *
 */

