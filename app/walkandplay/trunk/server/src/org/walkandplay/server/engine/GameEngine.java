/********************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ********************************************************************/

package org.walkandplay.server.engine;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.Alarm;
import org.keyworx.common.util.AlarmListener;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.MediaFiler;
import org.keyworx.server.ServerConfig;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;

/**
 * Main engine for MobGame.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GameEngine implements GameProtocol, AlarmListener {
    /**
     * Callback for all indications out of engine.
     */
    private IndicationListener indicationListener;
    private Log log;

    /**
     * Unique engine name (=also the space name)
     */
    private String name;
    public String configDir;
    private String dataDirPath;
    private JXElement gameConfig;

    /**
     * Lock object for incoming messages.
     */
    private Object lock = new Object();

    /**
     * Clock tick.
     */
    private Alarm alarm;
    private long lastTickTimeMillis;
    private static final long ALARM_INTERVAL_MILLIS = 30000L;

    /**
     * For logging daily routes
     */
    private DayLogger routeLogger;

    /**
     * Fixed formatter date and time.
     */
    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");

    /**
     * Fixed formatter date and time.
     */
    public static final SimpleDateFormat TIME_FORMAT = new SimpleDateFormat("HH:mm:ss");

    /**
     * Fixed formatter latitude.
     */
    private static final DecimalFormat latFormat = new DecimalFormat("##.#####");

    /**
     * Fixed formatter longitude.
     */
    private static final DecimalFormat lonFormat = new DecimalFormat("#.######");

    /** Translates GPS data to map coordinates. */
    //private MapTranslator mapTranslator;

    /**
     * Tracks teams for zones, confrontations.
     */
    private LocationTracker locationTracker;

    /**
     * Tracks all XML status objects.
     */
    private GameStatus gameStatus;

    /**
     * Map of active engines, keyed by game name.
     */
    static private final Map engines = Collections.synchronizedMap(new HashMap(3));

    private GameEngine(String aName) {
        name = aName;
        log = Logging.createAppLog("wp-" + name);
        log.setLevel(Level.FINEST);
    }

    public static String formatTime(long aTimeStamp) {
        if (aTimeStamp <= 0) {
            return "";
        }
        return dateFormat.format(new Date(aTimeStamp));
    }

    /*
      * Purpose: this way each space can have it's own game engine instance
      * multiple games could be running at the same time.
      */
    public static GameEngine getInstance(String aName) {
        synchronized (engines) {
            GameEngine result = (GameEngine) engines.get(aName);
            if (result == null) {
                result = new GameEngine(aName);
                engines.put(aName, result);
            }
            return result;
        }
    }

    public static String[] getGameNames() {
        synchronized (engines) {
            return (String[]) engines.keySet().toArray(new String[0]);
        }
    }

    /**
     * Alarm callback.
     *
     * @param anAlarm the alarm performing the callback
     */
    public void alarm(Alarm anAlarm) {
        long now = Sys.now();

        // Create a clock tick message
        JXElement clockElement = new JXElement(MSG_CLOCK_TICK_IND);
        clockElement.setAttr(ATTR_TIME_PASSED, (now - lastTickTimeMillis) / 1000);
        GameMessage clockTickIndMsg = GameMessage.createIndication(clockElement);
        lastTickTimeMillis = now;

        // Send message to engine
        try {
            // TODO : disabled clock tick message because of unknown getByName exception
            //doMessage(clockTickIndMsg);
        } catch (Throwable t) {
            log.warn("Error during clock tick", t);
        }

        // Reschedule next tick
        anAlarm.reschedule();
    }

    public String getDataDirPath() {
        return dataDirPath;
    }

    public String getConfigDirPath() {
        return configDir;
    }

    public GameStatus getGameStatus() {
        return gameStatus;
    }

    public String getName() {
        return name;
    }

    public Log getLog() {
        return log;
    }

    /**
     * Interprete message.
     */
    public GameMessage doMessage(GameMessage aMsgIn) throws GameException {
        synchronized (lock) {
            GameMessage responseMessage = null;
            GameMessage indicationMessage = null;
            JXElement responseElement;
            JXElement indicationElement;
            long startTime = Sys.now();

            JXElement messageInElement = aMsgIn.getData();
            // log.info("GameEngine[" + name + "] in=" + aMsgIn);
            String inMessageTag = aMsgIn.getData().getTag();

            // Check if game is active
            String gameState = gameStatus.getGameState();
            if (!gameState.equals(STATE_GAME_ACTIVE) && ADMIN_REQUESTS.indexOf(inMessageTag) == -1) {
                // ignore other than admin requests when not active
                throw new GameException("spel is niet meer aktief state=" + gameState);
            }

            try {

                // Do action based on message tag.
                if (inMessageTag.equals(MSG_ANSWER_SUBMIT_REQ)) {
                    /*
                             <answer-submit-req assignmentid="3434" text="mallejan"/>

                             <answer-submit-rsp assignmentid="3434" text="malle jan" correct="true"/>
                          */
                    throwOnMissingAttr(messageInElement, ATTR_ASSIGNMENTID);
                    throwOnMissingAttr(messageInElement, ATTR_TEXT);

                    // Check if required media uploaded and linked
                    int assignmentId = messageInElement.getIntAttr(ATTR_ASSIGNMENTID);
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);
                    String zoneName = gameStatus.getTeamAttr(teamName, ATTR_ZONENAME);
                    JXElement teamResults = gameStatus.getTeamElement(teamName, TAG_RESULTS);
                    JXElement zoneResult = teamResults.getChildByAttr(ATTR_ZONENAME, zoneName);
                    JXElement assignmentResult = zoneResult.getChildById(assignmentId + "");

                    // Check if required assignment media were linked
                    String assignmentState = assignmentResult.getAttr(ATTR_STATE);
                    if (!assignmentState.equals(VAL_MEDIADONE)) {
                        // Create negative response
                        responseElement = new JXElement(MSG_ANSWER_SUBMIT_NRSP);
                        if (assignmentState.equals(VAL_TODO)) {
                            responseElement.setAttr(ATTR_ERROR, "eerst media linken");
                        } else if (assignmentState.equals(VAL_DONE)) {
                            responseElement.setAttr(ATTR_ERROR, "opdracht is al gedaan !");
                        }
                    } else {
                        // Media linked ok: see if answer is ok
                        String submittedAnswer = messageInElement.getAttr(ATTR_TEXT).trim().toLowerCase();

                        int answerId = assignmentResult.getIntAttr(ATTR_ANSWERID);

                        // Get possible answers
                        String[] answers = gameStatus.getAnswerTextsForAssignment(assignmentId);

                        // Match submitted anser with possible answers
                        boolean result = false;
                        for (int i = 0; i < answers.length; i++) {
                            result = answers[i].equals(submittedAnswer);
                            if (result) {
                                break;
                            }
                        }

                        // Always add result
                        gameStatus.addAnswerText(answerId, submittedAnswer, result);

                        // Always create response
                        responseElement = new JXElement(MSG_ANSWER_SUBMIT_RSP);
                        responseElement.setAttr(ATTR_ASSIGNMENTID, assignmentId);
                        responseElement.setAttr(ATTR_TEXT, submittedAnswer);
                        responseElement.setAttr(ATTR_CORRECT, result);

                        // Update score and send team-status-ind with score if answer correct
                        if (result) {
                            /*
                                   opdrachten
                                   - bewijsopdracht gedaan = 10 + 1 boobypunt
                                   - 1e poorter gedaan = 20
                                   - 2e poorter gedaan = 20
                                   - hele zone gedaan = +20 en 1 cloakpunt
                                   - team met op eind meeste bewijsopdrachten (max 6) = +36

                                   boobytraps
                                   - bom neerleggen = -1 boobypunt
                                   - bom hit = -10

                                   cloak
                                   - cloak verbruiken = -1 cloakpunt

                                   confrontations
                                   - confront win = +15
                                   - confront lose = -15

                                   */
                            JXElement assignmentInfo = gameStatus.getAssignmentInfo(assignmentId);
                            int assignmentScore = assignmentInfo.getIntAttr(ATTR_POINTS);
                            JXElement teamScore = gameStatus.getTeamElement(teamName, TAG_SCORE);

                            // Update score points
                            assignmentResult.setAttr(ATTR_STATE, VAL_DONE);
                            assignmentResult.setAttr(ATTR_SCORE, assignmentScore);

                            // Update zone result based on which assignment was done

                            JXElement zoneStatus = gameStatus.getTeamElement(teamName, TAG_ZONES);
                            JXElement zoneElement = zoneStatus.getChildByAttr(ATTR_ZONENAME, zoneName);
                            String zoneState = zoneElement.getAttr(ATTR_STATE);
                            String assignmentType = assignmentResult.getAttr(ATTR_TYPE);
                            if (assignmentType.equals(VAL_BEWIJS)) {
                                // Key assignment done

                                // Defensive check
                                if (!zoneState.equals(STATE_ZONE_KEY_TODO)) {
                                    throw new GameException("zoneState does not match key assignment type zoneState=" + zoneState);
                                }

                                // Set booby point in zone result
                                zoneResult.setAttr(ATTR_BOOBYTRAPS, 1);

                                // Add booby point to total score
                                teamScore.setAttr(ATTR_BOOBYTRAPS, teamScore.getIntAttr(ATTR_BOOBYTRAPS) + 1);

                                // Update zone state
                                zoneElement.setAttr(ATTR_STATE, STATE_ZONE_TWO_TODO);

                            } else if (assignmentType.equals(VAL_POORTER)) {
                                // Check zone state
                                if (zoneState.equals(STATE_ZONE_TWO_TODO)) {
                                    // Add zones done to total score
                                    teamScore.setAttr(ATTR_ZONES, teamScore.getIntAttr(ATTR_ZONES) + 1);

                                    // Still one assignment to do
                                    zoneElement.setAttr(ATTR_STATE, STATE_ZONE_ONE_TODO);
                                } else if (zoneState.equals(STATE_ZONE_ONE_TODO)) {
                                    // Entire zone done
                                    // 20 points extra + a cloak point

                                    // Set cloak point in zone result
                                    zoneResult.setAttr(ATTR_CLOAKS, 1);

                                    // Update zone element with score
                                    zoneElement.setAttr(ATTR_SCORE, assignmentScore);

                                    // Zone done!!
                                    zoneElement.setAttr(ATTR_STATE, STATE_ZONE_DONE);

                                    // Add cloak point to total score
                                    teamScore.setAttr(ATTR_CLOAKS, teamScore.getIntAttr(ATTR_CLOAKS) + 1);

                                    // Add bonus to total score
                                    teamScore.setAttr(ATTR_POINTS, teamScore.getIntAttr(ATTR_POINTS) + 20);


                                } else {
                                    throw new GameException("Invalid zone state for poorter assignment type=" + zoneState);
                                }

                            } else {
                                throw new GameException("Invalid assignment type=" + assignmentType);
                            }

                            // Always increment total team score
                            teamScore.setAttr(ATTR_POINTS, teamScore.getIntAttr(ATTR_POINTS) + assignmentScore);

                            // Remove current assignment from team actions element
                            JXElement teamActionsElement = gameStatus.getTeamElement(teamName, TAG_ACTIONS);
                            teamActionsElement.removeChildById(assignmentId + "");

                            // Save all statuses
                            gameStatus.setTeamElement(teamName, teamActionsElement);
                            gameStatus.setTeamElement(teamName, teamScore);
                            gameStatus.setTeamElement(teamName, zoneStatus);
                            gameStatus.setTeamElement(teamName, teamResults);

                            // Always notify score to all players
                            // <team-status-ind event="score" >
                            //	 <team teamname="blue">
                            // <score points="23" boobytraps="1" cloaks="1"/>
                            // </team>
                            // </team-status-ind>
                            JXElement statusIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                            statusIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_SCORE);
                            statusIndicationElement.addChild(gameStatus.getTeamStatus(teamName, TAG_SCORE));
                            GameMessage statusIndicationMsg = GameMessage.createIndication(statusIndicationElement);
                            sendIndication(statusIndicationMsg);

                            // Create team-status indication for changed zone state
                            indicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                            indicationElement.setAttr(ATTR_EVENT, EV_STATUS_ZONE);
                            indicationElement.addChild(gameStatus.getTeamStatus(teamName, ATTR_ZONENAME));
                            indicationElement.setAttr(ATTR_STATE, zoneElement.getAttr(ATTR_STATE));

                            indicationMessage = GameMessage.createIndication(indicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
                            sendIndication(indicationMessage);

                        }
                    }

                    // Finally create answer response
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);
                } else if (inMessageTag.equals(MSG_ANSWER_GET_REQ)) {
                    if (messageInElement.hasAttr(ATTR_ASSIGNMENTID)) {
                        // Get answer content by assignment id
                        String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);
                        Vector mediaList = gameStatus.getLinkedMediaForAssignment(teamName, messageInElement.getAttr(ATTR_ASSIGNMENTID));

                        // Create response
                        responseElement = new JXElement(MSG_ANSWER_GET_RSP);
                        responseElement.setAttr(ATTR_ASSIGNMENTID, messageInElement.getAttr(ATTR_ASSIGNMENTID));
                        if (mediaList != null) {
                            responseElement.addChildren(mediaList);
                        }

                        responseMessage = GameMessage.createResponse(aMsgIn, responseElement);
                    } else if (messageInElement.hasAttr(ATTR_ANSWERID)) {
                        // Get answer content by answer id
                        Vector mediaList = gameStatus.getLinkedMediaForAnswer(messageInElement.getIntAttr(ATTR_ANSWERID));

                        // Create response
                        responseElement = new JXElement(MSG_ANSWER_GET_RSP);
                        responseElement.setAttr(ATTR_ANSWERID, messageInElement.getAttr(ATTR_ANSWERID));
                        if (mediaList != null) {
                            responseElement.addChildren(mediaList);
                        }

                        responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                    } else {
                        throw new GameException("Need answer or assignment id");
                    }

                } else if (inMessageTag.equals(MSG_ASSIGNMENT_GET_REQ)) {
                    // Get assignment content
                    throwOnMissingAttr(messageInElement, ATTR_ID);

                    // Player type determines kind of content to be returned
                    String playerType = gameStatus.getPlayer(aMsgIn.from).getAttr(ATTR_TYPE);
                    JXElement assignmentElement = gameStatus.getAssignment(messageInElement.getIntAttr(ATTR_ID), playerType);

                    // Create response
                    responseElement = new JXElement(MSG_ASSIGNMENT_GET_RSP);
                    responseElement.addChild(assignmentElement);

                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);
                } else if (inMessageTag.equals(MSG_BOOBYTRAP_GET_REQ)) {
                    // Get boobytrap info content
                    throwOnMissingAttr(messageInElement, ATTR_ID);

                    // Get boobytrap info
                    JXElement boobytrapElement = gameStatus.getBoobytrap(messageInElement.getIntAttr(ATTR_ID));
                    String zoneName = boobytrapElement.getChildByTag(TAG_LOCATION).getAttr(ATTR_ZONENAME);
                    // TODO :  commented out zone support
                    /*JXElement resolveLocationElement = locationTracker.getReferenceLocation(zoneName);*/

                    // Create response
                    responseElement = new JXElement(MSG_BOOBYTRAP_GET_RSP);
                    responseElement.addChild(boobytrapElement);
                    /*responseElement.addChild(resolveLocationElement);*/
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                } else if (inMessageTag.equals(MSG_BOOBYTRAP_PLACE_REQ)) {
                    // Create response
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);

                    // Check if boobytrap points available
                    JXElement teamScore = gameStatus.getTeamElement(teamName, TAG_SCORE);
                    if (teamScore.getIntAttr(ATTR_BOOBYTRAPS) <= 0) {
                        responseElement = new JXElement(MSG_BOOBYTRAP_PLACE_NRSP);
                        responseElement.setAttr(ATTR_ERROR, "je hebt geen boobytrap punten");

                    } else {
                        // OK enough BT points
                        JXElement teamLocation = gameStatus.getTeamElement(teamName, TAG_LOCATION);

                        // Create the boobytrap at current team location
                        JXElement boobytrapElement = gameStatus.placeBoobytrap(teamName, teamLocation);

                        // Deduct from BT score and save
                        teamScore.setAttr(ATTR_BOOBYTRAPS, teamScore.getIntAttr(ATTR_BOOBYTRAPS) - 1);
                        gameStatus.setTeamElement(teamName, teamScore);

                        // Create response
                        responseElement = new JXElement(MSG_BOOBYTRAP_PLACE_RSP);
                        responseElement.addChild(boobytrapElement);
                        responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                        // Create indication for own team
                        JXElement btIndicationElement = new JXElement(MSG_BOOBYTRAP_PLACE_IND);
                        btIndicationElement.addChild(boobytrapElement);
                        GameMessage btIndicationMessage = GameMessage.createIndication(btIndicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
                        sendIndication(btIndicationMessage);

                        // Notify score to all players
                        // <team-status-ind event="score" >
                        //	 <team teamname="blue">
                        // <score points="23" boobytraps="1" cloaks="1"/>
                        // </team>
                        // </team-status-ind>
                        JXElement statusIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                        statusIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_SCORE);
                        statusIndicationElement.addChild(gameStatus.getTeamStatus(teamName, TAG_SCORE));
                        GameMessage statusIndicationMsg = GameMessage.createIndication(statusIndicationElement);
                        sendIndication(statusIndicationMsg);
                    }

                } else if (inMessageTag.equals(MSG_CLOAK_REQ)) {
                    // Request to cloak

                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);
                    JXElement teamScoreElement = gameStatus.getTeamElement(teamName, TAG_SCORE);

                    // Check if enough cloak points available
                    int cloakPoints = teamScoreElement.getIntAttr(ATTR_CLOAKS);
                    if (cloakPoints <= 0) {
                        responseElement = new JXElement(MSG_CLOAK_NRSP);
                        responseElement.setAttr(ATTR_ERROR, "niet voldoende cloak punten");
                        return GameMessage.createResponse(aMsgIn, responseElement);
                    }

                    // Check if not boobytrapped
                    if (gameStatus.isBoobytrapped(teamName)) {
                        responseElement = new JXElement(MSG_CLOAK_NRSP);
                        responseElement.setAttr(ATTR_ERROR, "je bent nog boobytrapped");
                        return GameMessage.createResponse(aMsgIn, responseElement);
                    }

                    // Check if not already cloaked
                    if (gameStatus.isCloaked(teamName)) {
                        responseElement = new JXElement(MSG_CLOAK_NRSP);
                        responseElement.setAttr(ATTR_ERROR, "je bent al cloaked");
                        return GameMessage.createResponse(aMsgIn, responseElement);
                    }

                    // Update cloak team state
                    gameStatus.setCloak(teamName);

                    // Update cloak points
                    cloakPoints--;
                    teamScoreElement.setAttr(ATTR_CLOAKS, cloakPoints);
                    gameStatus.setTeamElement(teamName, teamScoreElement);

                    // Create response
                    responseElement = new JXElement(MSG_CLOAK_RSP);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                    // Send team status

                    // Always notify cloak  to all players
                    // <team-status-ind event="cloaked" >
                    //	 <team teamname="blue" cloaked="true>
                    // </team-status-ind>
                    JXElement statusIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                    statusIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_CLOAKED);
                    statusIndicationElement.addChild(gameStatus.getTeamStatus(teamName, ATTR_CLOAKED));
                    GameMessage statusIndicationMsg = GameMessage.createIndication(statusIndicationElement);
                    sendIndication(statusIndicationMsg);

                    // Notify score to all players
                    // <team-status-ind event="score" >
                    //	 <team teamname="blue">
                    // <score points="23" boobytraps="1" cloaks="1"/>
                    // </team>
                    // </team-status-ind>
                    JXElement scoreStatusIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                    scoreStatusIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_SCORE);
                    scoreStatusIndicationElement.addChild(gameStatus.getTeamStatus(teamName, TAG_SCORE));
                    GameMessage scoreStatusIndicationMsg = GameMessage.createIndication(scoreStatusIndicationElement);
                    sendIndication(scoreStatusIndicationMsg);


                } else if (inMessageTag.equals(MSG_CLOCK_TICK_IND)) {
                    // System.out.println("clock " + messageInElement);

                    // TODO :  commented out to get things running!!!
                    /*// Clock tick for running timeout-based activities
                    int timePassed = messageInElement.getIntAttr(ATTR_TIME_PASSED);

                    // Check active boobytraps
                    JXElement[] boobytraps = gameStatus.getActiveBoobytraps();
                    if(boobytraps!=null){
                        for (int i = 0; i < boobytraps.length; i++) {
                            int ttl = boobytraps[i].getIntAttr(ATTR_TIMETOLIVE) - timePassed;
                            boobytraps[i].setAttr(ATTR_TIMETOLIVE, ttl);

                            // Make new ttl persistent
                            gameStatus.updateTimeToLive(boobytraps[i].getIntId(), ttl);

                            // Check if boobytrap done
                            if (ttl <= 0) {
                                // Boobytrap done
                                // Yes ! remove the boobytrap for us
                                String teamName = boobytraps[i].getAttr(ATTR_VICTIM);

                                // Clear data owning team and db
                                gameStatus.clearBoobytrap(teamName, boobytraps[i].getIntId());

                                // Always notify boobytrap clear to all players
                                // <team-status-ind event="boobytrapdone" >
                                //	 <team teamname="blue">
                                //     <actions>
                                //
                                //     </actions>
                                //   </team>
                                // </team-status-ind>
                                JXElement statusIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                                statusIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_BOOBYTRAPDONE);
                                statusIndicationElement.addChild(gameStatus.getTeamStatus(teamName, TAG_ACTIONS));
                                GameMessage statusIndicationMsg = GameMessage.createIndication(statusIndicationElement);
                                sendIndication(statusIndicationMsg);
                            }
                        }
                    }
                    // Check active cloaks
                    JXElement[] cloaks = gameStatus.getActiveCloaks();
                    if(cloaks!=null){
                        for (int i = 0; i < cloaks.length; i++) {
                            int ttl = cloaks[i].getIntAttr(ATTR_TIMETOLIVE) - timePassed;
                            cloaks[i].setAttr(ATTR_TIMETOLIVE, ttl);

                            // Make new ttl persistent
                            gameStatus.updateTimeToLive(cloaks[i].getIntId(), ttl);

                            // Check if cloak done
                            if (ttl <= 0) {

                                String teamName = cloaks[i].getAttr(ATTR_OWNER);

                                // Clear cloak in db and update team status
                                gameStatus.clearCloak(teamName, cloaks[i].getIntAttr(ATTR_ID));

                                // Always notify cloak  to all players
                                // <team-status-ind event="cloakdone" >
                                //	 <team teamname="blue" cloaked="false>
                                // </team-status-ind>
                                JXElement statusIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                                statusIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_CLOAKDONE);
                                statusIndicationElement.addChild(gameStatus.getTeamStatus(teamName, ATTR_CLOAKED));
                                GameMessage statusIndicationMsg = GameMessage.createIndication(statusIndicationElement);
                                sendIndication(statusIndicationMsg);
                            }
                        }
                    }
*/

                } else if (inMessageTag.equals(MSG_CONTENT_PUT_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_FILE);

                    responseElement = new JXElement(MSG_CONTENT_PUT_RSP);
                    gameStatus.putContent(messageInElement.getAttr(ATTR_FILE));
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                } else if (inMessageTag.equals(MSG_GAME_GET_REQ)) {
                    JXElement gameStatusElement = gameStatus.getGameStatus();
                    responseElement = new JXElement(MSG_GAME_GET_RSP);
                    responseElement.addChild(gameStatusElement);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                } else if (inMessageTag.equals(MSG_GAME_RESET_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_NAME);

                    // Reset entire game (resets all teams)
                    gameStatus.resetGame();

                    // Reset tracking for all teams
                    locationTracker.reset();

                    log.warn("====== GAME HAS BEEN RESET BY ADMIN REQUEST ======");

                    // Return response
                    responseElement = new JXElement(MSG_GAME_RESET_RSP);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);
                } else if (inMessageTag.equals(MSG_GAME_SETSTATE_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_STATE);
                    String state = messageInElement.getAttr(ATTR_STATE);
                    gameStatus.setState(state);
                    responseElement = new JXElement(MSG_GAME_SETSTATE_RSP);
                    responseElement.setAttr(ATTR_STATE, state);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                    indicationElement = new JXElement(MSG_GAME_STATUS_IND);

                    indicationElement.setAttr(ATTR_EVENT, ATTR_STATE);
                    indicationElement.addChild(gameStatus.getGameStatus(ATTR_STATE));

                    // Create indication
                    indicationMessage = GameMessage.createIndication(aMsgIn.from, indicationElement, TYPE_ALL_BUT_SENDER);

                    sendIndication(indicationMessage);
                } else if (inMessageTag.equals(MSG_GPS_DATA_IND)) {
                    // Most of the game actions are derived from location
                    // a lot of stuff happens here...

                    // 1. DERIVE AND SAVE MAP LOCATION: COORDINATES AND ZONE
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);

                    // TODO: commented out gps coordinate translation
                    /*GeoSample geoSample = null;

					// Get point on real map
					GeoPoint realPoint = null;

					// Client may have lat/lon ready
					if (messageInElement.hasAttr(ATTR_LAT)) {
						throwOnMissingAttr(messageInElement, ATTR_LON);
						geoSample = new GeoSample(
								messageInElement.getAttr(ATTR_LAT),
								messageInElement.getAttr(ATTR_LON));

						// Get point on real map from lat/lon
						realPoint = mapTranslator.getRealMapPoint(geoSample);

					} else if (messageInElement.hasAttr(ATTR_RX)) {
						// Test purpose: client sends real map point
						throwOnMissingAttr(messageInElement, ATTR_RY);
						// Create point on real map
						realPoint = new GeoPoint(messageInElement.getIntAttr(ATTR_RX),
								messageInElement.getIntAttr(ATTR_RY));
						geoSample = mapTranslator.getGeoSample(realPoint);
					} else {
						// Must be raw GPS NMEA data
						throwOnMissingAttr(messageInElement, ATTR_DATA);

						String data = messageInElement.getAttr(ATTR_DATA);
						geoSample = GPSDecoder.parseSample(data);
						if (geoSample == null) {
							log.warn("Invalid GPS data [" + data + "]");
							return null;
						}

						// Check accuracy
						if (geoSample.accuracy < gameConfig.getIntAttr("gpsMinAccuracy")) {
							log.info(teamName + " - discarding inaccurate GPS sample: " + geoSample);
							return null;
						}

						// Get point on real map
						realPoint = mapTranslator.getRealMapPoint(geoSample);
					}

					// Get point on medieval map
					GeoPoint medievalPoint = mapTranslator.getMedievalMapPoint(realPoint);*/

                    // Create new location element
                    JXElement locationElement = new JXElement(TAG_LOCATION);
//					locationElement.setAttr(ATTR_LAT, latFormat.format(geoSample.latitude));
//					locationElement.setAttr(ATTR_LON, lonFormat.format(geoSample.longitude));
//					locationElement.setAttr(ATTR_RX, realPoint.x);
//					locationElement.setAttr(ATTR_RY, realPoint.y);
//					locationElement.setAttr(ATTR_MX, medievalPoint.x);
//					locationElement.setAttr(ATTR_MY, medievalPoint.y);
//					locationElement.setAttr(ATTR_QUALITY, geoSample.accuracy);
//					locationElement.setAttr(ATTR_TIME, geoSample.timestamp);
//					locationElement.setAttr(ATTR_ZONENAME, VAL_NONE);
                    if (messageInElement.hasAttr(ATTR_LABEL)) {
                        locationElement.setAttr(ATTR_LABEL, messageInElement.getAttr(ATTR_LABEL));
                    } else {
                        locationElement.setAttr(ATTR_LABEL, aMsgIn.from);
                    }

                    // Figure out in which zone we are
                    // Location element is updated with current zone
                    String enteredZoneName = locationTracker.trackTeam(teamName, locationElement);

                    // Update team status with current location
                    gameStatus.setTeamElement(teamName, locationElement);

                    // Log to route file
                    logRoute(teamName, locationElement, messageInElement.getAttr(ATTR_LABEL));

                    // ASSERT: map/zone location determined and persisted

                    // 2. SEND LOCATION INFO TO ALL PLAYERS

                    // Create team-status indication
                    JXElement locationIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                    locationIndicationElement.setAttr(ATTR_EVENT, TAG_LOCATION);
                    JXElement teamStatusElement = gameStatus.getTeamStatus(teamName, TAG_LOCATION);
                    locationIndicationElement.addChild(teamStatusElement);

                    GameMessage locationIndicationMsg = GameMessage.createIndication(locationIndicationElement);

                    // Send team location status to all players
                    sendIndication(locationIndicationMsg);

                    // No use going on with other actions if not in any zone
                    if (locationElement.getAttr(ATTR_ZONENAME).equals(VAL_NONE)) {
                        return null;
                    }

                    // 3. CHECK IF WE ARE RESOLVING BOOBYTRAP

                    // Check boobytrap resolution
                    if (gameStatus.isBoobytrapped(teamName)) {
                        // We have an active boobytrap

                        // Check if we reached reference location
                        // TODO : commented out cause no zone support
                        /*JXElement refLocation = locationTracker.getReferenceLocation(locationElement.getAttr(ATTR_ZONENAME));*/

                        // Are we near the ref point ?
/*                        if (isNear(locationElement, refLocation)) {

                            // Clear data owning team and db
                            gameStatus.clearBoobytrap(teamName);

                            // Always notify boobytrap clear to all players
                            // <team-status-ind event="boobytrapdone" >
                            //	 <team teamname="blue">
                            //     <actions>
                            //
                            //     </actions>
                            //   </team>
                            // </team-status-ind>
                            JXElement statusIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                            statusIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_BOOBYTRAPDONE);
                            statusIndicationElement.addChild(gameStatus.getTeamStatus(teamName, TAG_ACTIONS));
                            GameMessage statusIndicationMsg = GameMessage.createIndication(statusIndicationElement);
                            sendIndication(statusIndicationMsg);
                        }*/

                        // We are either:
                        // - still boobytrapped
                        // - have just resolved a boobytrap
                        // in all cases, don't do any further location actions here
                        return null;
                    }

                    //
                    // 4. CHECK IF WE ARE HITTING PLACED BOOBYTRAP
                    //
                    JXElement[] boobytraps = gameStatus.getPlacedBoobytraps(locationElement.getAttr(ATTR_ZONENAME));
                    JXElement hitBoobytrap = null;
                    for (int i = 0; i < boobytraps.length; i++) {
                        // Can hit only under these conditions
                        // 1. we are near BT
                        // 2. it is not our own boobytrap
                        // 3. we are not cloaked
                        // 4. we are not already boobytrapped
                        if (
                                isNear(locationElement, boobytraps[i].getChildByTag(TAG_LOCATION)) &&
                                        !boobytraps[i].getAttr(ATTR_OWNER).equals(teamName) &&
                                        !gameStatus.isCloaked(teamName) &&
                                        !gameStatus.isBoobytrapped(teamName)) {
                            // Conditions for Boobytrap hit !!!
                            hitBoobytrap = boobytraps[i];
                            break;
                        }
                    }

                    // Check if hit and take actions
                    JXElement teamActionsElement = gameStatus.getTeamElement(teamName, TAG_ACTIONS);

                    // Check if we have been hit
                    if (hitBoobytrap != null) {

                        // Create and add action element
                        JXElement currentBoobytrapElement = new JXElement(TAG_ACTION);
                        currentBoobytrapElement.setAttr(ATTR_TYPE, VAL_BOOBYTRAP);
                        currentBoobytrapElement.setAttr(ATTR_ID, hitBoobytrap.getId());
                        currentBoobytrapElement.setAttr(ATTR_STATUS, VAL_ACTIVE);
                        teamActionsElement.addChild(currentBoobytrapElement);

                        // Save action status
                        gameStatus.setTeamElement(teamName, teamActionsElement);

                        // Make other status updates
                        gameStatus.detoneBoobytrap(teamName, hitBoobytrap);

                        // Send team-action-ind to team
                        JXElement actionIndicationElement = new JXElement(MSG_TEAM_ACTION_IND);
                        actionIndicationElement.setAttr(ATTR_ACTION, VAL_ACTION_BOOBYTRAPPED);
                        actionIndicationElement.setAttr(ATTR_ID, hitBoobytrap.getId());
                        GameMessage actionIndicationMsg = GameMessage.createIndication(actionIndicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
                        sendIndication(actionIndicationMsg);

                        // Always notify boobytrap hit to all players
                        // <team-status-ind event="boobytrapped" >
                        //	 <team teamname="blue">
                        //     <actions>
                        //       <action type="boobytrap" status="active" id="657"/>
                        //     </actions>
                        //   </team>
                        // </team-status-ind>
                        JXElement actionStatusIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                        actionStatusIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_BOOBYTRAPPED);
                        actionStatusIndicationElement.addChild(gameStatus.getTeamStatus(teamName, TAG_ACTIONS));
                        GameMessage actionStatusIndicationMsg = GameMessage.createIndication(actionStatusIndicationElement);
                        sendIndication(actionStatusIndicationMsg);

                        // Always notify score decrease
                        // <team-status-ind event="score" >
                        //	 <team teamname="blue">
                        //       <score points="70-10" boobytraps="2" cloaks="1">
                        //   </team>
                        // </team-status-ind>
                        JXElement teamScoreElement = gameStatus.getTeamElement(teamName, TAG_SCORE);
                        int currentScore = teamScoreElement.getIntAttr(ATTR_POINTS);

                        // Decrement and send indication if current score not already zero
                        if (currentScore > 0) {
                            // Decrement total team score
                            currentScore -= POINTS_BOOBYTRAP;
                            // Set to 0 if below zero
                            if (currentScore < 0) {
                                currentScore = 0;
                            }
                            teamScoreElement.setAttr(ATTR_POINTS, currentScore);

                            // Save score status
                            gameStatus.setTeamElement(teamName, teamScoreElement);

                            // Send score indication
                            JXElement scoreStatusIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                            scoreStatusIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_SCORE);
                            scoreStatusIndicationElement.addChild(gameStatus.getTeamStatus(teamName, TAG_SCORE));
                            GameMessage scoreStatusIndicationMsg = GameMessage.createIndication(scoreStatusIndicationElement);
                            sendIndication(scoreStatusIndicationMsg);
                        }

                        // No use proceeding
                        return null;

                    }

                    // ASSERT: we are not boobytrapped

                    // 5. CONFRONTATION ACTIONS WHEN BUMPING INTO ANOTHER TEAM
                    if (!gameStatus.isCloaked(teamName)) {
                        // Can only do confrontation when
                        // 1. we are not boobytrapped (see ASSERT) and
                        // 2. we are not cloaked

                        // Get all teams online
                        String[] onlineTeams = locationTracker.getTeams();
                        JXElement confronteeLocation = null;
                        boolean hasCollisionWithTeam = false;
                        for (int i = 0; i < onlineTeams.length; i++) {

                            // Skip our own team
                            if (onlineTeams[i].equals(teamName)) {
                                continue;
                            }

                            // Skip if other team is cloaked
                            if (gameStatus.isCloaked(onlineTeams[i])) {
                                continue;
                            }

                            // Skip if other team is boobytrapped
                            if (gameStatus.isBoobytrapped(onlineTeams[i])) {
                                continue;
                            }

                            // Check further if teams are near each other
                            confronteeLocation = locationTracker.getTeamLocation(onlineTeams[i]);
                            hasCollisionWithTeam = locationTracker.containsCollision(teamName, onlineTeams[i]);

                            if (hasCollisionWithTeam) {
                                long now = Sys.now();
                                long nonConfrontTime = gameConfig.getLongAttr("nonConfrontTime") * 60 * 1000;
                                long collisionTime = locationTracker.getCollisionTimeMillis(teamName, onlineTeams[i]);

                                // Check if collision timer still applies
                                if (collisionTime > 0) {
                                    // Check if enough time passed for a new confrontation
                                    if (now - collisionTime >= nonConfrontTime) {

                                        // Ok. Remove timing for "aged" collision.
                                        // collisionTime will be "0" the next time
                                        log.info("Collision time cleared for " + teamName + " with " + onlineTeams[i]);
                                        locationTracker.clearCollisionTimeMillis(teamName, onlineTeams[i]);
                                    } else {
                                        // Still collided and within non-confront time
                                        // Cannot do confront
                                        continue;
                                    }
                                }
                            }


                            if (isNear(locationElement, confronteeLocation)) {
                                // Skip if just confronted
                                // must first move out of each other
                                if (hasCollisionWithTeam) {
                                    continue;
                                }

                                // Do the confrontation
                                log.info("CONFRONTATION - " + teamName + " with " + onlineTeams[i]);
                                doConfrontation(teamName, onlineTeams[i]);

                                // Remember the confrontation:
                                log.info("Adding collision for " + teamName + " with " + onlineTeams[i]);
                                locationTracker.addCollision(teamName, onlineTeams[i]);

                                // Don't do too much in this round
                                return null;
                            } else if (hasCollisionWithTeam) {
                                // Not near and still collision with team
                                // remove collision pair from tracking.
                                long collisionTime = locationTracker.getCollisionTimeMillis(teamName, onlineTeams[i]);
                                // Remove only if
                                // - collision exists
                                // - not near each other
                                // - non collision time passed
                                if (collisionTime <= 0) {
                                    log.info("Removing collision for " + teamName + " with " + onlineTeams[i]);
                                    locationTracker.removeCollision(teamName, onlineTeams[i]);
                                }
                            }
                        }
                    }

                    // 3. ZONE ACTIONS

                    // Determine if a new zone was entered
                    if (enteredZoneName != null) {
                        // Set new zone name in team status
                        // gameStatus.setTeamAttr(teamName, ATTR_ZONENAME, enteredZoneName);

                        JXElement workingZoneStatus = gameStatus.getTeamWorkingZone(teamName);
                        String workingZoneName = VAL_NONE;
                        String workingZoneState = VAL_NONE;
                        if (workingZoneStatus != null) {
                            workingZoneState = workingZoneStatus.getAttr(ATTR_STATE);
                            workingZoneName = workingZoneStatus.getAttr(ATTR_ZONENAME);
                        }

                        // If team enters selected zone see if team action ind "zoneenter" needs to be sent
                        JXElement enteredZoneStatus = gameStatus.getTeamZone(teamName, enteredZoneName);
                        String enteredZoneState = enteredZoneStatus.getAttr(ATTR_STATE);

                        // Proceed if working zone has progressed enough (done or one to do)
                        // or we are entering our working zone (happens after being offline)
                        if (workingZoneState.equals(STATE_ZONE_DONE) ||
                                workingZoneState.equals(STATE_ZONE_ONE_TODO) ||
                                enteredZoneName.equals(workingZoneName)) {

                            // Send indication if entered zone state is "to do" or "selected" (happens first time)
                            if (enteredZoneState.equals(STATE_ZONE_TODO) ||
                                    enteredZoneState.equals(STATE_ZONE_SELECTED)) {
                                // Send zoneenter action to gamephone. GP decides weather a zone-select-req is allowed
                                // based on working and entered zone states
                                // <team-action-ind action="zoneenter" id="zone3" status="keytodo" />
                                JXElement actionIndicationElement = new JXElement(MSG_TEAM_ACTION_IND);
                                actionIndicationElement.setAttr(ATTR_ACTION, VAL_ACTION_ZONEENTER);
                                actionIndicationElement.setAttr(ATTR_ID, enteredZoneName);
                                actionIndicationElement.setAttr(ATTR_STATUS, enteredZoneState);
                                GameMessage actionIndicationMsg = GameMessage.createIndication(actionIndicationElement, TYPE_SINGLE, aMsgIn.from);
                                sendIndication(actionIndicationMsg);
                            } else if (enteredZoneState.equals(STATE_ZONE_ONE_TODO)) {
                                // Update team working zone status
                                gameStatus.setTeamAttr(teamName, ATTR_ZONENAME, enteredZoneName);

                                // Create team-status indication for new working zone
                                indicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                                indicationElement.setAttr(ATTR_EVENT, EV_STATUS_ZONE);
                                indicationElement.addChild(gameStatus.getTeamStatus(teamName, ATTR_ZONENAME));
                                indicationElement.setAttr(ATTR_STATE, enteredZoneState);

                                indicationMessage = GameMessage.createIndication(indicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
                                sendIndication(indicationMessage);

                            }
                        }
                    } /* else {
						// Check if selected zone has already received intro
						JXElement workingZoneStatus = gameStatus.getTeamWorkingZone(teamName);
						String locationZoneName = locationElement.getAttr(ATTR_ZONENAME);
						if (workingZoneStatus != null && !locationZoneName.equals(VAL_NONE)) {
							String workingZoneName = workingZoneStatus.getAttr(ATTR_ZONENAME);
							String zoneState = workingZoneStatus.getAttr(ATTR_STATE);

							// If we are in selected zone but have not yet heard intro
							// send the action
							if (locationZoneName.equals(workingZoneName) && zoneState.equals(STATE_ZONE_SELECTED)) {
								// Send "dozoneintro" action to team.
								// <team-action-ind action="dozoneintro" id="zone3" status="selected" />
								JXElement actionIndicationElement = new JXElement(MSG_TEAM_ACTION_IND);
								actionIndicationElement.setAttr(ATTR_ACTION, VAL_ACTION_DOZONEINTRO);
								actionIndicationElement.setAttr(ATTR_ID, workingZoneStatus.getAttr(ATTR_ZONENAME));
								actionIndicationElement.setAttr(ATTR_STATUS, zoneState);
								GameMessage actionIndicationMsg = GameMessage.createIndication(actionIndicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
								sendIndication(actionIndicationMsg);
								workingZoneStatus.setAttr(ATTR_STATE, STATE_ZONE_KEY_TODO);
								JXElement zoneStatus = gameStatus.getTeamElement(teamName, TAG_ZONES);
								gameStatus.setTeamElement(teamName, zoneStatus);
							}
						}
					} */

                    // 6. ACTIONS WHEN ASSIGNMENT LOCATION REACHED

                    // If the working zone is the zone we're in determine if assignment location is reached
                    String locationZoneName = locationElement.getAttr(ATTR_ZONENAME);
                    String workingZoneName = gameStatus.getTeamAttr(teamName, ATTR_ZONENAME);
                    if (!locationZoneName.equals(VAL_NONE) && locationZoneName.equals(workingZoneName)) {
                        int assignmentIds[] = gameStatus.getTeamOpenAssignments(teamName, workingZoneName);
                        for (int i = 0; i < assignmentIds.length; i++) {
                            JXElement assignmentLocation = gameStatus.getLocation(assignmentIds[i]);

                            // Check if team is near any open assignment
                            if (isNear(locationElement, assignmentLocation)) {
                                // YESS! Set active assignment in actions
                                teamActionsElement = gameStatus.getTeamElement(teamName, TAG_ACTIONS);

                                JXElement currentAssignmentElement = teamActionsElement.getChildByAttr(ATTR_TYPE, VAL_ASSIGNMENT);
                                if (currentAssignmentElement == null) {
                                    currentAssignmentElement = new JXElement(TAG_ACTION);
                                    teamActionsElement.addChild(currentAssignmentElement);
                                } else {
                                    // Do nothing if still near same assignment
                                    // Otherwise we would get multiple team-action-ind's
                                    if (currentAssignmentElement.getIntAttr(ATTR_ID) == assignmentIds[i]) {
                                        continue;
                                    }

                                }

                                currentAssignmentElement.setAttr(ATTR_TYPE, VAL_ASSIGNMENT);
                                currentAssignmentElement.setAttr(ATTR_ID, assignmentIds[i]);
                                currentAssignmentElement.setAttr(ATTR_STATUS, VAL_ACTIVE);

                                // Save status
                                gameStatus.setTeamElement(teamName, teamActionsElement);

                                // Send team-action-ind to team
                                JXElement actionIndicationElement = new JXElement(MSG_TEAM_ACTION_IND);
                                actionIndicationElement.setAttr(ATTR_ACTION, VAL_ACTION_DOASSIGNMENT);
                                actionIndicationElement.setAttr(ATTR_ID, assignmentIds[i]);
                                GameMessage actionIndicationMsg = GameMessage.createIndication(actionIndicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
                                sendIndication(actionIndicationMsg);
                            }
                        }

                    }
                } else if (inMessageTag.equals(MSG_GPS_STATUS_IND)) {
                    throwOnMissingAttr(messageInElement, ATTR_GPS);
                    String gpsState = messageInElement.getAttr(ATTR_GPS);
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);

                    gameStatus.setTeamAttr(teamName, ATTR_GPS, gpsState);

                    // Create indication
                    indicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                    indicationElement.setAttr(ATTR_EVENT, EV_STATUS_GPS);
                    indicationElement.addChild(gameStatus.getTeamStatus(teamName, ATTR_GPS));

                    indicationMessage = GameMessage.createIndication(indicationElement);
                    sendIndication(indicationMessage);
                } else if (inMessageTag.equals(MSG_JOIN_REQ)) {
                    // Add player and expand options element
                    gameStatus.addPlayer(aMsgIn.from, messageInElement.getChildByTag(GameProtocol.TAG_OPTIONS));

                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);
                    String playerType = gameStatus.getPlayer(aMsgIn.from).getAttr(ATTR_TYPE);
                    // If gamephone leaves there is no GPS anymore
                    if (playerType.equals(VAL_GAME_PHONE)) {
                        // If the player is the gamephone, reset team location info

                        // Reset zone tracking info for team
                        locationTracker.resetTeam(teamName);
                        // TODO: should we do this ???
                        // otherwise this could be a form of cloaking.

                        // Set GPS status to not available "na"
                        gameStatus.setTeamAttr(teamName, ATTR_GPS, VAL_NA);
                    }
                } else if (inMessageTag.equals(MSG_LEAVE_REQ)) {
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);
                    String playerType = gameStatus.getPlayer(aMsgIn.from).getAttr(ATTR_TYPE);

                    // If gamephone leaves there is no GPS anymore
                    if (playerType.equals(VAL_GAME_PHONE)) {
                        // If the player is the gamephone, reset team location info

                        // Reset zone tracking info for team
                        locationTracker.resetTeam(teamName);
                        // TODO: should we do this ???
                        // otherwise this could be a form of cloaking.

                        // Set GPS status to not available "na"
                        gameStatus.setTeamAttr(teamName, ATTR_GPS, VAL_NA);

                        // Create team-status indication that GPS is not available
                        indicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                        indicationElement.setAttr(ATTR_EVENT, EV_STATUS_GPS);
                        indicationElement.addChild(gameStatus.getTeamStatus(teamName, ATTR_GPS));

                        indicationMessage = GameMessage.createIndication(aMsgIn.from, indicationElement, TYPE_ALL_BUT_SENDER);
                        sendIndication(indicationMessage);
                    }

                    // Remove player by agent id
                    gameStatus.removePlayer(aMsgIn.from);

                } else if (inMessageTag.equals(MSG_LOCATION_GET_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_ID);

                    // Get location element
                    int id = messageInElement.getIntAttr(ATTR_ID);
                    JXElement locationElement = gameStatus.getLocation(id);
                    if (locationElement == null) {
                        throw new GameException("No location field found for id=" + id);
                    }

                    // Create response
                    responseElement = new JXElement(MSG_LOCATION_GET_RSP);
                    responseElement.addChild(locationElement);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                } else if (inMessageTag.equals(MSG_MEDIA_GET_REQ)) {
                    /* <!-- C - HQ gets free media -->

                                 <!-- S - Result of media-get-req -->
                                 <media-get-req />
                                 <media-get-rsp >
                                     <medium id="7777" type="image"/>
                                     <medium id="7778" type="image"/>
                                     <medium id="7779" type="video"/>
                                 </media-link-req>
                              */
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);
                    Vector freeMedia = gameStatus.getFreeMedia(teamName);

                    // Create positive response
                    responseElement = new JXElement(MSG_MEDIA_GET_RSP);
                    responseElement.addChildren(freeMedia);

                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                } else if (inMessageTag.equals(MSG_MEDIA_LINK_REQ)) {
                    /* <!-- C - HQ links media -->
                             <media-link-req assignmentid="3434">
                                 <medium id="7777" type="image"/>
                                 <medium id="7778" type="image"/>
                                 <medium id="7779" type="video"/>
                             </media-link-req>

                             <!-- S - Result of media-link-req -->
                             <media-link-rsp assignmentid="3434" />
                          */
                    throwOnMissingAttr(messageInElement, ATTR_ASSIGNMENTID);

                    int assignmentId = messageInElement.getIntAttr(ATTR_ASSIGNMENTID);
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);
                    JXElement assignmentResult = gameStatus.getAssignmentResult(teamName, assignmentId);
                    String resultBeforeState = assignmentResult.getAttr(ATTR_STATE);

                    // Link media and check if required number/type are linked
                    if (!gameStatus.linkMedia(teamName, assignmentId, messageInElement.getChildren())) {
                        // Insufficient right media: create negative response
                        responseElement = new JXElement(MSG_MEDIA_LINK_NRSP);
                        responseElement.setAttr(ATTR_ERROR, "onvoldoende media");
                        return GameMessage.createResponse(aMsgIn, responseElement);
                    }

                    // ASSERT sufficient and right media

                    // Create positive response
                    responseElement = new JXElement(MSG_MEDIA_LINK_RSP);
                    responseElement.setAttr(ATTR_ASSIGNMENTID, assignmentId);

                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                    // Create media-link-ind indication to entire team
                    // ONLY IF REQUIRED MEDIA WERE LINKED
                    assignmentResult = gameStatus.getAssignmentResult(teamName, assignmentId);
                    String resultAfterState = assignmentResult.getAttr(ATTR_STATE);

                    if (resultBeforeState.equals(VAL_TODO) && resultAfterState.equals(VAL_MEDIADONE)) {
                        indicationElement = new JXElement(MSG_MEDIA_LINK_IND);
                        indicationElement.setAttr(ATTR_ASSIGNMENTID, assignmentId);
                        indicationMessage = GameMessage.createIndication(indicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
                        sendIndication(indicationMessage);
                    }
                } else if (inMessageTag.equals(MSG_MEDIUM_DELETE_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_ID);
                    int mediumId = messageInElement.getIntId();
                    gameStatus.deleteMedium(mediumId);

                    // Create positive response
                    responseElement = new JXElement(MSG_MEDIUM_DELETE_RSP);
                    responseElement.setId(mediumId);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                } else if (inMessageTag.equals(MSG_MEDIUM_SEND_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_ID);
                    throwOnMissingAttr(messageInElement, ATTR_TO);

                    int mediumId = messageInElement.getIntId();
                    String playerTypeFrom = gameStatus.getPlayer(aMsgIn.from).getAttr(ATTR_TYPE);

                    // Determine player id to send indication to
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);
                    String playerTypeTo = messageInElement.getAttr(ATTR_TO);
                    String playerIdTo = gameStatus.getPlayerIdForType(teamName, playerTypeTo);
                    if (playerIdTo == null) {
                        // Create negative response
                        responseElement = new JXElement(MSG_MEDIUM_SEND_NRSP);
                        responseElement.setAttr(ATTR_ERROR, "deze speler is niet online");
                        responseMessage = GameMessage.createResponse(aMsgIn, responseElement);
                        return responseMessage;
                    }

                    // Create positive response
                    responseElement = new JXElement(MSG_MEDIUM_SEND_RSP);
                    responseElement.setId(mediumId);
                    responseElement.setAttr(ATTR_TO, playerTypeTo);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                    // Create medium-send-ind indication to addressee (to)
                    indicationElement = new JXElement(MSG_MEDIUM_SEND_IND);
                    indicationElement.setAttr(ATTR_FROM, playerTypeFrom);
                    indicationElement.setAttr(ATTR_ID, mediumId);

                    indicationMessage = GameMessage.createIndication(indicationElement, TYPE_SINGLE, playerIdTo);
                    sendIndication(indicationMessage);

                } else if (inMessageTag.equals(MSG_MEDIUM_UPLOAD_REQ)) {
                    /*
                             <medium-upload-req
                             url="http://www.amsterdam.nl/animage.gif" type="image" name="plaatje 3"/>
                         */
                    throwOnMissingAttr(messageInElement, ATTR_URL);
                    throwOnMissingAttr(messageInElement, ATTR_TYPE);

                    // Teamname player is in
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);
                    String playerType = gameStatus.getPlayer(aMsgIn.from).getAttr(ATTR_TYPE);

                    // Get url
                    String url = messageInElement.getAttr(ATTR_URL);
                    String type = messageInElement.getAttr(ATTR_TYPE);

                    // Fill in fields
                    HashMap fields = new HashMap(2);
                    String name = "noname";
                    String description = "upload by " + playerType + " from " + url;
                    if (messageInElement.hasAttr(ATTR_NAME)) {
                        name = messageInElement.getAttr(ATTR_NAME);
                    }

                    if (messageInElement.hasAttr(ATTR_DESCRIPTION)) {
                        description = messageInElement.getAttr(ATTR_DESCRIPTION);
                    }

                    // Adds medium and relates it to team
                    fields.put(ATTR_NAME, name);
                    fields.put(ATTR_DESCRIPTION, description);
                    fields.put(ATTR_OWNER, playerType);

                    int id = gameStatus.addMedium(teamName, url, type, fields);

                    // ASSERT medium inserted and linked to team

                    // Create response
                    responseElement = new JXElement(MSG_MEDIUM_UPLOAD_RSP);
                    responseElement.setAttr(ATTR_URL, url);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                    // Create medium-upload-ind indication to entire team
                    indicationElement = new JXElement(MSG_MEDIUM_UPLOAD_IND);
                    indicationElement.setAttr(ATTR_FROM, playerType);
                    indicationElement.setAttr(ATTR_ID, id);
                    indicationElement.setAttr(ATTR_TYPE, type);
                    indicationElement.setAttr(ATTR_NAME, name);
                    indicationElement.setAttr(ATTR_DESCRIPTION, description);

                    indicationMessage = GameMessage.createIndication(indicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
                    sendIndication(indicationMessage);
                } else if (inMessageTag.equals(MSG_MEDIUM_MAIL_UPLOAD_IND)) {
                    throwOnMissingAttr(messageInElement, ATTR_FROM);
                    throwOnMissingAttr(messageInElement, ATTR_SUBJECT);
                    throwOnMissingAttr(messageInElement, ATTR_TYPE);
                    throwOnMissingAttr(messageInElement, ATTR_ID);
                    throwOnMissingAttr(messageInElement, ATTR_NAME);

                    // Determine player and team
                    String playerEmail = messageInElement.getAttr(ATTR_FROM);
                    String teamName = gameStatus.getTeamNameForPlayerName(playerEmail);
                    JXElement playerElement = gameStatus.getPlayerByName(playerEmail);
                    if (playerElement == null) {
                        log.warn("medium-mail-upload: no player found for email=" + playerEmail);
                        teamName = messageInElement.getAttr(ATTR_SUBJECT);
                        JXElement teamStatusElement = gameStatus.getTeamStatus(teamName);
                        if (teamStatusElement == null) {
                            log.warn("email upload: no team found for subject=" + teamName);
                            return null;
                        }
                    }

                    String owner = (playerElement == null) ? VAL_VIDEO_PHONE : playerElement.getAttr(ATTR_TYPE);

                    gameStatus.addMedium(teamName, messageInElement.getIntId(), owner);

                    // Create medium-upload-ind indication to entire team
                    // if they are online
                    String playerIds = gameStatus.getPlayerIdsForTeam(teamName);
                    indicationElement = new JXElement(MSG_MEDIUM_UPLOAD_IND);
                    if (playerElement != null) {
                        indicationElement.setAttr(ATTR_FROM, playerElement.getAttr(ATTR_TYPE));
                    } else {
                        indicationElement.setAttr(ATTR_FROM, VAL_VIDEO_PHONE);
                    }
                    indicationElement.setAttr(ATTR_ID, messageInElement.getId());
                    indicationElement.setAttr(ATTR_TYPE, messageInElement.getAttr(ATTR_TYPE));
                    indicationElement.setAttr(ATTR_NAME, messageInElement.getAttr(ATTR_NAME));
                    indicationElement.setAttr(ATTR_DESCRIPTION, "none");

                    indicationMessage = GameMessage.createIndication(indicationElement, TYPE_MULTI, playerIds);
                    sendIndication(indicationMessage);

                } else if (inMessageTag.equals(MSG_MEDIUM_RAW_UPLOAD_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_TYPE);
                    throwOnMissingAttr(messageInElement, ATTR_MIMETYPE);

                    // Teamname player is in
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);
                    String playerType = gameStatus.getPlayer(aMsgIn.from).getAttr(ATTR_TYPE);

                    String type = messageInElement.getAttr(ATTR_TYPE);
                    String mime = messageInElement.getAttr(ATTR_MIMETYPE);

                    // Fill in fields
                    HashMap fields = new HashMap(2);
                    String name = "noname";
                    String description = "raw upload by " + playerType;
                    if (messageInElement.hasAttr(ATTR_NAME)) {
                        name = messageInElement.getAttr(ATTR_NAME);
                    }

                    if (messageInElement.hasAttr(ATTR_DESCRIPTION)) {
                        description = messageInElement.getAttr(ATTR_DESCRIPTION);
                    }

                    // Adds medium and relates it to team
                    fields.put(ATTR_NAME, name);
                    fields.put(ATTR_DESCRIPTION, description);
                    fields.put(MediaFiler.FIELD_MIME, mime);
                    fields.put(MediaFiler.FIELD_KIND, type);
                    fields.put(ATTR_OWNER, playerType);

                    // <data> element contains CDATA file bytes and
                    // how these are encoded in "encoding" attr.
                    JXElement data = messageInElement.getChildByTag(TAG_DATA);
                    throwOnMissingAttr(data, ATTR_ENCODING);

                    // Insert medium in DB and link to team
                    int id = -1; // gameStatus.addMedium(teamName, data.getCDATA(), data.getAttr(ATTR_ENCODING), type, fields);

                    // ASSERT medium inserted and linked to team

                    responseElement = new JXElement(MSG_MEDIUM_RAW_UPLOAD_RSP);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                    // Create medium-upload-ind indication to entire team
                    indicationElement = new JXElement(MSG_MEDIUM_UPLOAD_IND);
                    indicationElement.setAttr(ATTR_FROM, playerType);
                    indicationElement.setAttr(ATTR_ID, id);
                    indicationElement.setAttr(ATTR_TYPE, type);
                    indicationElement.setAttr(ATTR_NAME, name);
                    indicationElement.setAttr(ATTR_DESCRIPTION, description);

                    indicationMessage = GameMessage.createIndication(indicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
                    sendIndication(indicationMessage);
                } else if (inMessageTag.equals(MSG_TEAM_GET_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_TEAMNAME);

                    // An optional selection (attr or child tagname) may be present
                    String selection = null;
                    if (messageInElement.hasAttr(ATTR_SELECT)) {
                        selection = messageInElement.getAttr(ATTR_SELECT);
                    }

                    // Query for team status
                    JXElement teamStatusElement = gameStatus.getTeamStatus(messageInElement.getAttr(ATTR_TEAMNAME), selection);

                    // Create response
                    responseElement = new JXElement(MSG_TEAM_GET_RSP);
                    responseElement.addChild(teamStatusElement);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                } else if (inMessageTag.equals(MSG_TEAM_RESET_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_TEAMNAME);
                    String teamName = messageInElement.getAttr(ATTR_TEAMNAME);

                    // Reset team status
                    gameStatus.resetTeam(teamName);

                    // Reset zone tracking info for team
                    locationTracker.resetTeam(teamName);

                    // Create response
                    responseElement = new JXElement(MSG_TEAM_RESET_RSP);
                    responseElement.setAttr(ATTR_TEAMNAME, teamName);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);
                } else if (inMessageTag.equals(MSG_TEAM_SET_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_TEAMNAME);
                    throwOnMissingAttr(messageInElement, ATTR_SELECT);
                    String teamName = messageInElement.getAttr(ATTR_TEAMNAME);
                    String selection = messageInElement.getAttr(ATTR_SELECT);

                    // Check if we should set an attr or child element
                    if (messageInElement.hasAttr(selection)) {
                        gameStatus.setTeamAttr(teamName, selection, messageInElement.getAttr(selection));
                    } else {
                        gameStatus.setTeamElement(teamName, messageInElement.getChildByTag(selection));

                    }
                    // Create response
                    responseElement = new JXElement(MSG_TEAM_SET_RSP);
                    responseElement.setAttr(ATTR_TEAMNAME, teamName);
                    responseElement.setAttr(ATTR_SELECT, selection);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);
                } else if (inMessageTag.equals(MSG_ZONE_SELECT_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_ZONENAME);
                    // Teamname player is in
                    String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);

                    // Get target zonename info
                    String targetZoneName = messageInElement.getAttr(ATTR_ZONENAME);
                    JXElement targetZoneStatus = gameStatus.getTeamZone(teamName, targetZoneName);

                    // Check if exists
                    if (targetZoneStatus == null) {
                        throw new GameException("Onjuiste zone naam: " + targetZoneName);
                    }

                    String targetZoneState = targetZoneStatus.getAttr(ATTR_STATE);

                    // Get working zone info
                    JXElement workingZoneStatus = gameStatus.getTeamWorkingZone(teamName);
                    String workingZoneName = VAL_NONE;
                    String workingZoneState = VAL_NONE;
                    if (workingZoneStatus != null) {
                        workingZoneState = workingZoneStatus.getAttr(ATTR_STATE);
                        workingZoneName = workingZoneStatus.getAttr(ATTR_ZONENAME);
                    }
                    // Check if state of working zone is valid

                    if (!(workingZoneState.equals(STATE_ZONE_ONE_TODO) ||
                            workingZoneState.equals(STATE_ZONE_DONE) ||
                            workingZoneState.equals(VAL_NONE) ||
                            targetZoneName.equals(workingZoneName))) {
                        throw new GameException(workingZoneName + " is nog niet af state=" + workingZoneState);
                    }

                    // Check if state of target zone is valid
                    if (!(targetZoneState.equals(STATE_ZONE_TODO) ||
                            targetZoneState.equals(STATE_ZONE_SELECTED))) {
                        throw new GameException(targetZoneName + " is niet selecteerbaar in toestand " + targetZoneState);
                    }

                    // ASSERT current and new zone in right states

                    // Set new zone to selected state if still in to do state
                    targetZoneStatus.setAttr(ATTR_STATE, STATE_ZONE_KEY_TODO);

                    // Create response
                    responseElement = new JXElement(MSG_ZONE_SELECT_RSP);
                    responseElement.setAttr(ATTR_ZONENAME, targetZoneName);
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                    // Update team status
                    gameStatus.setTeamAttr(teamName, ATTR_ZONENAME, targetZoneName);
                    gameStatus.setTeamElement(teamName, targetZoneStatus);

                    // Create team-status indication for new working zone
                    indicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                    indicationElement.setAttr(ATTR_EVENT, EV_STATUS_ZONE);
                    indicationElement.addChild(gameStatus.getTeamStatus(teamName, ATTR_ZONENAME));
                    indicationElement.setAttr(ATTR_STATE, targetZoneStatus.getAttr(ATTR_STATE));

                    indicationMessage = GameMessage.createIndication(indicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
                    sendIndication(indicationMessage);

                    /*				// Teamname player is in
                                         String teamName = gameStatus.getTeamNameForPlayer(aMsgIn.from);

                                         // Zonename to be selected
                                         String newZoneName = messageInElement.getAttr(ATTR_ZONENAME);

                                         // Current zone name
                                         String currentZoneName = gameStatus.getTeamAttr(teamName, ATTR_ZONENAME);

                                         // Check if there is a current zone and it has progressed enough
                                         JXElement zoneStatusElement = gameStatus.getTeamElement(teamName, TAG_ZONES);
                                         if (!currentZoneName.equals(VAL_NONE)) {
                                             JXElement currentZoneElement = zoneStatusElement.getChildByAttr(ATTR_ZONENAME, currentZoneName);
                                             if (currentZoneElement != null) {
                                                 String currentZoneState = currentZoneElement.getAttr(ATTR_STATE);
                                                 // Allow only when current zone is
                                                 // done or one assignment to do
                                                 if (!(
                                                         currentZoneState.equals(STATE_ZONE_ONE_TODO) ||
                                                         currentZoneState.equals(STATE_ZONE_DONE))) {
                                                     throw new GameException(currentZoneName + " is nog niet af state=" + currentZoneState);
                                                 }
                                             }
                                         }

                                         // Check if new zone name exists
                                         JXElement newZoneElement = zoneStatusElement.getChildByAttr(ATTR_ZONENAME, newZoneName);
                                         if (newZoneElement == null) {
                                             throw new GameException("Onjuiste zone naam: " + newZoneName);
                                         }

                                         // Check if state of selected zone is valid
                                         String targetZoneState = newZoneElement.getAttr(ATTR_STATE);
                                         if (!(targetZoneState.equals(STATE_ZONE_TODO) ||
                                                 targetZoneState.equals(STATE_ZONE_ONE_TODO))) {
                                             throw new GameException(newZoneName + " is niet selecteerbaar in toestand " + targetZoneState);
                                         }

                                         // ASSERT current and new zone in right states

                                         // Set new zone to selected state if still in to do state
                                         if (targetZoneState.equals(STATE_ZONE_TODO)) {
                                             newZoneElement.setAttr(ATTR_STATE, STATE_ZONE_SELECTED);
                                         }

                                         // Update team status
                                         gameStatus.setTeamAttr(teamName, ATTR_ZONENAME, newZoneName);
                                         gameStatus.setTeamElement(teamName, zoneStatusElement);

                                         // Create response
                                         responseElement = new JXElement(MSG_ZONE_SELECT_RSP);
                                         responseElement.setAttr(ATTR_ZONENAME, newZoneName);
                                         responseMessage = GameMessage.createResponse(aMsgIn, responseElement);

                                         // Send action ind when zone is selected
                                         // Check if selected zone has already received intro
                                         JXElement workingZoneStatus = gameStatus.getTeamWorkingZone(teamName);
                                         if (workingZoneStatus != null) {
                                             String zoneState = workingZoneStatus.getAttr(ATTR_STATE);
                                             if (zoneState.equals(STATE_ZONE_SELECTED)) {
                                                 // Send "dozoneintro" action to team.
                                                 // <team-action-ind action="dozoneintro" id="zone3" status="selected" />
                                                 JXElement actionIndicationElement = new JXElement(MSG_TEAM_ACTION_IND);
                                                 actionIndicationElement.setAttr(ATTR_ACTION, VAL_ACTION_DOZONEINTRO);
                                                 actionIndicationElement.setAttr(ATTR_ID, workingZoneStatus.getAttr(ATTR_ZONENAME));
                                                 actionIndicationElement.setAttr(ATTR_STATUS, zoneState);
                                                 GameMessage actionIndicationMsg = GameMessage.createIndication(actionIndicationElement, TYPE_MULTI, gameStatus.getPlayerIdsForTeam(teamName));
                                                 sendIndication(actionIndicationMsg);
                                                 workingZoneStatus.setAttr(ATTR_STATE, STATE_ZONE_KEY_TODO);
                                                 JXElement zoneStatus = gameStatus.getTeamElement(teamName, TAG_ZONES);
                                                 gameStatus.setTeamElement(teamName, zoneStatus);

                                             }
                                         }

                                         // Create team-status indication
                                         indicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                                         indicationElement.setAttr(ATTR_EVENT, EV_STATUS_ZONE);
                                         indicationElement.addChild(gameStatus.getTeamStatus(teamName, ATTR_ZONENAME));

                                         indicationMessage = GameMessage.createIndication(indicationElement);
                                         sendIndication(indicationMessage);  */

                } else if (inMessageTag.equals(MSG_ZONE_GET_REQ)) {
                    throwOnMissingAttr(messageInElement, ATTR_ZONENAME);

                    responseElement = new JXElement(MSG_ZONE_GET_RSP);
                    JXElement zoneElement = gameStatus.getZone(messageInElement.getAttr(ATTR_ZONENAME));
                    responseElement.addChild(zoneElement);

                    // Create response
                    responseMessage = GameMessage.createResponse(aMsgIn, responseElement);
                } else {
                    // Hmm
                    log.warn("Unsupported message tag=" + inMessageTag);
                    throw new GameException("Unsupported message tag=" + inMessageTag);
                }
            } catch (Throwable t) {
                log.warn("Error while processing message ex=" + t + " msg=" + messageInElement);

                // Only Send negative response with request messages except join/leave
                if (inMessageTag.equals(MSG_JOIN_REQ) || inMessageTag.equals(MSG_LEAVE_REQ)) {
                    if (t instanceof GameException)
                        throw (GameException)t;
                    else
                        throw new GameException("Error processing " + inMessageTag, t);
                } else if (inMessageTag.endsWith(POSTFIX_REQ)) {
                    // Create and send negative response to player for request
                    JXElement nrspElement = new JXElement(inMessageTag.replaceAll(POSTFIX_REQ, POSTFIX_NRSP));
                    nrspElement.setAttr(ATTR_ERROR, t.toString());
                    responseMessage = GameMessage.createResponse(aMsgIn, nrspElement);
                } else {
                    if (t instanceof GameException)
                        throw (GameException)t;
                    else
                        throw new GameException("Error in GameEngine msg=" + inMessageTag, t);
                }

            }

            // log.info(inMessageTag + " done in " + (Sys.now() - startTime) + " ms");
            return responseMessage;
        }
    }

    public void addIndicationListener(IndicationListener anIndicationListener) {
        System.out.println("adding indicationlistener");
        indicationListener = anIndicationListener;
    }

    public void init() throws GameException {
        try {
            // Read MapTranslator
            //configDir = System.getProperty("mobgame.cfg.dir");
            configDir = ServerConfig.getConfigDir();

            //configDir = "/var/keyworx/webapps/mobgame/WEB-INF/cfg";
            // TODO : maptranslator commented out!!
            //String mapTransConfigFile = configDir + File.separator + "maptranslator.xml";
			//mapTranslator = new MapTranslator(mapTransConfigFile);

            String gameConfigFile = configDir + File.separator + "game-config.xml";
            gameConfig = new JXBuilder().build(new File(gameConfigFile));
            log.info("read game-config.xml: values=" + gameConfig.toString());

            gameStatus = new GameStatus(name);
            gameStatus.init(gameConfig);

            String zoneConfigFile = configDir + File.separator + "zones.xml";
            locationTracker = new LocationTracker(zoneConfigFile);

            //File oaseDataDir = new File(System.getProperty("oase.data.dir"));
            //dataDirPath = oaseDataDir.getParent() + File.separator + "mobgame" + File.separator + name;
            //dataDirPath = System.getProperty("keyworx.data.dir") + File.separator + "mobgame" + File.separator + name;
            dataDirPath = ServerConfig.getProperty(ServerConfig.KEYWORX_DATA_DIR_PROPERTY) + File.separator + "wp" + File.separator + name;
            //dataDirPath = "/var/keyworx/data" + File.separator + "mobgame" + File.separator + name;

            log.info("dataDirPath:" + dataDirPath);
            routeLogger = new DayLogger(dataDirPath + File.separator + "routes", "routes", log);
            log.info(routeLogger.toString());
            // Schedule regular clock tick
            lastTickTimeMillis = Sys.now();
            alarm = new Alarm(ALARM_INTERVAL_MILLIS, this);            
        } catch (GameException ge) {
            throw ge;
        } catch (Throwable t) {
            throw new GameException("Error in GameEngine.init()", t);
        }

        //log.info("Init OK: GameEngine game status: " + gameStatus.getGameStatus().toFormattedString());

    }

    public void exit() {
        log.info("Exit: GameEngine");
        try {

            // alarm.cancel();
            log.info("Exit OK: GameEngine ");
        } catch (Throwable t) {
            // ignored
            log.warn("Unexpected exception during GameEngine exit", t);
        }
    }

    private void sendIndication(GameMessage aMsg) {
        if (indicationListener == null) {
            return;
        }

        try {
            indicationListener.onIndication(aMsg);
        } catch (Throwable t) {
            log.warn("Error calling indicationListener", t);
        }
    }

    /**
     * Tests if two locations are near eachother.
     */
    private boolean isNear(JXElement oneLocation, JXElement anotherLocation) {
        int deltaX = oneLocation.getIntAttr(ATTR_RX) - anotherLocation.getIntAttr(ATTR_RX);
        int deltaY = oneLocation.getIntAttr(ATTR_RY) - anotherLocation.getIntAttr(ATTR_RY);
        int distance = (int) Math.round(Math.sqrt((deltaX * deltaX + deltaY * deltaY)));
        return distance < gameConfig.getIntAttr("hotspotRange");
    }

    /**
     * Calculate winner of confrontation.
     */
    private String calculateConfrontation(String aTeamName, String anotherTeamName) throws GameException {
        String teamPower = gameStatus.getTeamPower(aTeamName);
        String otherTeamPower = gameStatus.getTeamPower(anotherTeamName);

        // Determine winner
        if (teamPower.equals(otherTeamPower)) {
            // no winner when equal power
            return VAL_NONE;
        }

        // ASSERT powers are not equal

        // vuur verbrandt hout
        if (teamPower.equals(VAL_VUUR)) {
            return otherTeamPower.equals(VAL_HOUT) ? aTeamName : anotherTeamName;
        }
        // water blust vuur
        else if (teamPower.equals(VAL_WATER)) {
            return otherTeamPower.equals(VAL_VUUR) ? aTeamName : anotherTeamName;
        }
        // hout keert het water
        else if (teamPower.equals(VAL_HOUT)) {
            return otherTeamPower.equals(VAL_WATER) ? aTeamName : anotherTeamName;
        } else {
            throw new GameException("Invalid personage power");
        }
    }

    private void doConfrontation(String aTeamName, String anotherTeamName) {
        try {
            String teamName = aTeamName;
            String otherTeamName = anotherTeamName;

            // ASSERT all conditions met for confrontation

            // Perform confrontation according to personage strengths
            // vuur verbrandt hout
            // water blust vuur
            // hout keert het water
            String winner = calculateConfrontation(teamName, otherTeamName);

            // Create confrontation element
            JXElement confrontationElement = new JXElement(TAG_CONFRONTATION);
            confrontationElement.setAttr(ATTR_INITIATOR, teamName);
            confrontationElement.setAttr(ATTR_TARGET, otherTeamName);
            confrontationElement.setAttr(ATTR_WINNER, winner);
            confrontationElement.addChild(gameStatus.getTeamElement(teamName, TAG_LOCATION));

            // Update game status
            gameStatus.addConfrontation(confrontationElement);

            // Send indication to both teams
            JXElement indicationElement = new JXElement(MSG_CONFRONT_IND);
            indicationElement.addChild(confrontationElement);

            String playerIds = gameStatus.getPlayerIdsForTeam(teamName) + gameStatus.getPlayerIdsForTeam(otherTeamName);

            GameMessage indicationMessage = GameMessage.createIndication(indicationElement, TYPE_MULTI, playerIds);
            sendIndication(indicationMessage);

            // If there is a winner update score and send indication
            if (!winner.equals(VAL_NONE)) {
                // Update for winner
                JXElement winnerScoreElement = gameStatus.getTeamElement(winner, TAG_SCORE);
                winnerScoreElement.setAttr(ATTR_POINTS, winnerScoreElement.getIntAttr(ATTR_POINTS) + POINTS_CONFRONT);
                gameStatus.setTeamElement(winner, winnerScoreElement);

                // Send +score indication
                JXElement winnerIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                winnerIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_SCORE);
                winnerIndicationElement.addChild(gameStatus.getTeamStatus(winner, TAG_SCORE));
                GameMessage winnerIndicationMsg = GameMessage.createIndication(winnerIndicationElement);
                sendIndication(winnerIndicationMsg);

                // Update loser
                String loser = winner.equals(teamName) ? otherTeamName : teamName;
                JXElement loserScoreElement = gameStatus.getTeamElement(loser, TAG_SCORE);
                loserScoreElement.setAttr(ATTR_POINTS, loserScoreElement.getIntAttr(ATTR_POINTS) - POINTS_CONFRONT);

                // If below zero make zero
                if (loserScoreElement.getIntAttr(ATTR_POINTS) < 0) {
                    loserScoreElement.setAttr(ATTR_POINTS, 0);
                }

                gameStatus.setTeamElement(loser, loserScoreElement);

                // Send -score indication
                JXElement loserIndicationElement = new JXElement(MSG_TEAM_STATUS_IND);
                loserIndicationElement.setAttr(ATTR_EVENT, EV_STATUS_SCORE);
                loserIndicationElement.addChild(gameStatus.getTeamStatus(loser, TAG_SCORE));
                GameMessage loserIndicationMsg = GameMessage.createIndication(loserIndicationElement);
                sendIndication(loserIndicationMsg);
            }

        } catch (Throwable t) {
            log.warn("Error doing confrontation between team=" + aTeamName + " and team=" + anotherTeamName);
        }
    }

    private void logRoute(String aTeamName, JXElement aLocationElement, String aLabel) {
        JXElement routeElement = new JXElement("loc");
        routeElement.setAttr(ATTR_NAME, aTeamName);
        routeElement.setAttrs(aLocationElement.getAttrs());
        if (aLabel != null && aLabel.length() > 0) {
            routeElement.setAttr(ATTR_LABEL, aLabel);

        }
        routeLogger.log(routeElement);
    }

    protected void throwOnMissingAttr(JXElement aMessage, String anAttrName) throws GameException {
		if (!aMessage.hasAttr(anAttrName)) {
			throw new GameException("Required attribute missing: " + anAttrName);
		}
	}


}
