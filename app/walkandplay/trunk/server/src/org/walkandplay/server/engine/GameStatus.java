/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.walkandplay.server.engine;

import nl.justobjects.jox.dom.JXElement;
import nl.justobjects.jox.parser.JXBuilder;
import org.keyworx.common.util.Rand;
import org.keyworx.common.util.Sys;
import org.keyworx.oase.api.Record;

import java.io.File;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Manages actual status of all objects in game.
 *
 * @author Just van den Broecke
 * @version $Id: GameStatus.java,v 1.1.1.1 2006/04/03 09:21:36 rlenz Exp $
 */
public class GameStatus implements GameDataDef, GameProtocol {
    /**
     * Game config parms
     */
    private JXElement gameConfig;

    /**
     * Manager for persistent data.
     */
    private GameData gameData;

    /**
     * Entire game status as huge XML element.
     */
    private JXElement statusElement = new JXElement(TAG_GAME);

    /**
     * Index by agent id into player elements.
     */
    private HashMap players = new HashMap(12);

    public GameStatus(String aGameName) {
        statusElement.setAttr(ATTR_NAME, aGameName);
    }

    public GameData getGameData() {
        return gameData;
    }

    public void addAnswerText(int anAnswerId, String aText, boolean theResult) throws GameException {
        try {

            Record answerRecord = gameData.getById(anAnswerId);
            JXElement content = answerRecord.getXMLField(FIELD_CONTENT);
            if (theResult) {
                // mark right answer
                aText += "*";
            }

            // Append to existing text
            content.addText(content.hasText() ? ";" + aText : aText);

            answerRecord.setXMLField(FIELD_CONTENT, content);
            gameData.update(answerRecord);
        } catch (Throwable t) {
            throw new GameException("Error in addAnswerText() ", t);
        }
    }

    public int addConfrontation(JXElement aConfrontation) throws GameException {
        try {
            String initiatorName = aConfrontation.getAttr(ATTR_INITIATOR);
            String targetName = aConfrontation.getAttr(ATTR_TARGET);
            String winnerName = aConfrontation.getAttr(ATTR_WINNER);
            JXElement location = aConfrontation.getChildByTag(TAG_LOCATION);

            Record confrontationRecord = gameData.insertConfrontation(initiatorName, targetName, winnerName, location);
            return confrontationRecord.getId();
        } catch (Throwable t) {
            throw new GameException("Error in addConfrontation() ", t);
        }
    }


    public int addMedium(String aTeamName, byte[] theData, String theEncoding, String aType, HashMap someFields) throws GameException {
        try {

            Record mediumRecord = gameData.insertMedium(theData, theEncoding, aType, someFields);
            Record teamRecord = gameData.getTeamRecord(aTeamName);
            gameData.relate(teamRecord, mediumRecord, REL_TAG_FREE);
            return mediumRecord.getId();
        } catch (Throwable t) {
            throw new GameException("Error adding medium for " + aTeamName, t);
        }
    }

    public void addMedium(String aTeamName, int aRecordId, String anOwner) throws GameException {
        try {
            Record mediumRecord = gameData.getById(aRecordId);
            Record teamRecord = gameData.getTeamRecord(aTeamName);
            gameData.relate(teamRecord, mediumRecord, REL_TAG_FREE);
            mediumRecord.setStringField(FIELD_OWNER, anOwner);
            gameData.update(mediumRecord);
        } catch (Throwable t) {
            throw new GameException("Error adding medium for id=" + aRecordId + " team=" + aTeamName, t);
        }
    }

    public int addMedium(String aTeamName, String aURL, String aType, HashMap someFields) throws GameException {
        try {
            Record mediumRecord = gameData.insertMedium(aURL, aType, someFields);
            Record teamRecord = gameData.getTeamRecord(aTeamName);
            gameData.relate(teamRecord, mediumRecord, REL_TAG_FREE);
            return mediumRecord.getId();
        } catch (Throwable t) {
            throw new GameException("Error adding medium for " + aTeamName, t);
        }
    }

    public void deleteMedium(int aMediumId) throws GameException {
        try {
            gameData.deleteMedium(aMediumId);
        } catch (Throwable t) {
            throw new GameException("Error deleting medium id=" + aMediumId, t);
        }
    }


    /**
     * Add player and expand options element.
     */
    public void addPlayer(String aPlayerId, JXElement theOptions) throws GameException {
        String playerName = theOptions.getAttr(ATTR_NAME);
        Record playerRecord = gameData.getPlayerForName(playerName);
        Record teamRecord = gameData.getTeamForPlayer(playerRecord);

        // Get team name
        String teamName = teamRecord.getStringField(TEAM_NAME);
        theOptions.setAttr(ATTR_TEAMNAME, teamName);

        // Media URL
        theOptions.setAttr(ATTR_MEDIAURL, VAL_MEDIA_URI);

        // Time on server
        theOptions.setAttr(ATTR_TIME, Sys.now());

        // Find the player element in team element and update
        JXElement playersElement = getTeamStatus(teamName).getChildByTag(TAG_PLAYERS);
        JXElement playerElement = playersElement.getChildByAttr(ATTR_NAME, playerName);
        playerElement.setAttr(ATTR_ONLINE, VAL_YES);
        playerElement.setAttr(ATTR_AGENTID, aPlayerId);
        playerElement.setAttr(ATTR_TIME, theOptions.getAttr(ATTR_TIME));
        players.put(aPlayerId, playerElement);
    }

    public void removePlayer(String aPlayerId) throws GameException {
        JXElement playerElement = getPlayer(aPlayerId);
        playerElement.setAttr(ATTR_ONLINE, VAL_NO);
        playerElement.removeAttr(ATTR_AGENTID);
    }

    public String[] getAnswerTextsForAssignment(int anId) throws GameException {
        Record record = gameData.getById(anId);
        if (record == null) {
            throw new GameException("getAnswerForAssignment: cannot find assignment for id=" + anId);
        }
        return record.getStringField(ASSIGNMENT_ANSWER).toLowerCase().trim().split(";");
    }

    public JXElement getAssignmentResult(String aTeamName, int anAssignmentId) throws GameException {

        Record assignmentRecord = gameData.getById(anAssignmentId);
        Record zoneRecord = gameData.getRelatedRecord(assignmentRecord, TABLE_ZONE, null);

        // <results/> element
        JXElement teamResults = getTeamElement(aTeamName, TAG_RESULTS);
        JXElement zoneResults = teamResults.getChildByAttr(ATTR_ZONENAME, zoneRecord.getStringField(FIELD_NAME));
        JXElement assignmentResult = zoneResults.getChildById(anAssignmentId + "");
        int answerId = assignmentResult.getIntAttr(ATTR_ANSWERID);

        // Insert new answer record if none exists yet
        if (answerId < 0) {
            answerId = gameData.insertAnswer(aTeamName, anAssignmentId).getId();
            assignmentResult.setAttr(ATTR_ANSWERID, answerId);
            setTeamElement(aTeamName, teamResults);
        }

        return assignmentResult;
    }

    public JXElement getAssignment(int anId, String aContentTag) throws GameException {
        try {
            Record record = gameData.getById(anId);
            if (record == null) {
                throw new GameException("getAssignment: cannot find assignment for id=" + anId);
            }

            /*
              <assignment id="3434" name="zone1-bewijs" type="bewijs">

                  <info points="8" reqimages="2" reqvideos="1"/>

                  <content>
                      <part name="Intro" medium="image" id="234">
                          De Lastage is een gebied buiten de stadsmuur.
                          Er lagen scheepswerven en ook woonden er mensen.
                          Toen de Hertog van Gelre de Lastage binnenviel werden
                          alle bezittingen van deze mensen verwoest.
                          Maar het stadsbestuur wilde nog steeds geemuur
                          om de Lastage heen bouwen
                      </part>
                      <part name="Tip over de Dader" medium="image" id="234">
                          De scheepsbouwer vertelde dat hij een stukje van de Heilige
                          Hostie heeft gekocht in de hoop te genezen van zijn brandwonden.
                          De wonden werden alleen maar erger. De vrouw van de scheepsbouwer kan zich
                          alleen nog herinneren dat de man van de hostie cement aan zijn handen had zitten.
                      </part>
                      <part name="Opdracht" medium="image" id="234">
                          Verbeeld m.b.v. 3 verschillende soorten media de onverklaarbare
                          magische dingen die met de Montelbaanstoren gebeurd zouden
                      </part>
                      <part name="Wachtwoord" medium="image" id="234">
                          Welke bijnaam heeft de Montebaanstoren
                          in de loop der eeuwen gekregen? (antwoord is malle jan)
                      </part>
                  </content>

                  <!-- Location on map -->
                  <location rx="545" ry="1345" mx="343" my="868"/>

              </assignment>
              */
            JXElement result = new JXElement(TAG_ASSIGNMENT);
            result.setId(record.getId());
            result.setAttr(ATTR_NAME, record.getStringField(FIELD_NAME));
            result.setAttr(ATTR_TYPE, record.getStringField(FIELD_TYPE));
            result.addChild(record.getXMLField(FIELD_INFO));
            JXElement content = record.getXMLField(FIELD_CONTENT).getChildByTag(aContentTag);
            content.setTag(TAG_CONTENT);
            result.addChild(content);
            result.addChild(record.getXMLField(FIELD_LOCATION));


            return result;
        } catch (Throwable t) {
            throw new GameException("Error in getAssignment()", t);
        }
    }


    public JXElement getAssignmentInfo(int anId) throws GameException {
        try {
            Record record = gameData.getById(anId);
            if (record == null) {
                throw new GameException("getAssignmentInfo: cannot find assignment for id=" + anId);
            }

            return record.getXMLField(FIELD_INFO);

        } catch (Throwable t) {
            throw new GameException("Error in getAssignmentInfo()", t);
        }
    }

    public JXElement getBoobytrap(int anId) throws GameException {
        try {
            Record record = gameData.getById(anId);
            if (record == null) {
                return null;
            }
            return boobytrapRecord2Element(record);
        } catch (Throwable t) {
            throw new GameException("Error in getBoobytrap()", t);
        }
    }

    public JXElement[] getActiveBoobytraps() throws GameException {
        try {
            Record[] records = gameData.getBoobytrapRecords();
            if(records == null) return null;
            if(records.length == 0) return new JXElement[0];

            List result = new ArrayList(4);
            for (int i = 0; i < records.length; i++) {
                if (records[i].getField(FIELD_STATE).equals(VAL_ACTIVE)) {
                    result.add(boobytrapRecord2Element(records[i]));
                }
            }

            return (JXElement[]) result.toArray(new JXElement[0]);
        } catch (Throwable t) {
            throw new GameException("Error in getActiveBoobytraps()", t);
        }
    }

    public JXElement[] getActiveCloaks() throws GameException {
        try {
            Record[] records = gameData.getCloakRecords();
            if(records == null) return null;
            if(records.length == 0) return new JXElement[0];

            List result = new ArrayList(4);
            for (int i = 0; i < records.length; i++) {
                if (records[i].getField(FIELD_STATE).equals(VAL_ACTIVE)) {
                    result.add(cloakRecord2Element(records[i]));
                }
            }

            return (JXElement[]) result.toArray(new JXElement[0]);
        } catch (Throwable t) {
            throw new GameException("Error in getActiveCloaks()", t);
        }
    }

    public Vector getFreeMedia(String aTeamName) throws GameException {
        try {
            Record teamRecord = gameData.getTeamRecord(aTeamName);
            Record[] mediaRecords = gameData.getRelatedRecords(teamRecord, TABLE_MEDIUM, REL_TAG_FREE);

            Vector result = new Vector(mediaRecords.length);
            JXElement nextMediumElement = null;
            for (int i = 0; i < mediaRecords.length; i++) {
                nextMediumElement = new JXElement(TAG_MEDIUM);
                nextMediumElement.setId(mediaRecords[i].getId());
                nextMediumElement.setAttr(ATTR_TYPE, mediaRecords[i].getStringField(FIELD_KIND));
                nextMediumElement.setAttr(ATTR_OWNER, mediaRecords[i].getStringField(FIELD_OWNER));
                result.add(nextMediumElement);
            }

            return result;
        } catch (Throwable t) {
            throw new GameException("Error in getFreeMedia()", t);
        }
    }

    public String getGameState() throws GameException {
        try {
            Record gameRecord = gameData.getGameRecord();
            //if(gameRecord == null) return GameProtocol.STATE_GAME_NULL;
            return gameRecord.getStringField(GAME_STATE);
        } catch (Throwable t) {
            throw new GameException("Error in getGameState()", t);
        }
    }

    public JXElement getGameStatus() throws GameException {
        return getGameStatus(null);
    }

    public JXElement getGameStatus(String aSelect) throws GameException {
        JXElement result = null;
        try {
            Record gameRecord = gameData.getGameRecord();
            statusElement.setAttr(ATTR_STATE, gameRecord.getStringField(GAME_STATE));
            if (aSelect == null || aSelect.length() == 0) {
                result = statusElement;
            } else if (aSelect.equals(ATTR_STATE)) {
                result = new JXElement(TAG_GAME);
                result.setAttr(GAME_STATE, statusElement.getAttr(ATTR_STATE));
            }
        } catch (Throwable t) {
            throw new GameException("Error in getGameStatus()", t);
        } finally {
            return result;
        }
    }

    public JXElement[] getPlacedBoobytraps(String aZoneName) throws GameException {
        try {
            Record[] records = gameData.getBoobytrapRecords(aZoneName);
            List result = new ArrayList(4);
            for (int i = 0; i < records.length; i++) {
                if (records[i].getField(FIELD_STATE).equals(VAL_PLACED)) {
                    result.add(boobytrapRecord2Element(records[i]));
                }
            }

            return (JXElement[]) result.toArray(new JXElement[0]);
        } catch (Throwable t) {
            throw new GameException("Error in getBoobytrap()", t);
        }
    }

    public JXElement[] getPlacedBoobytraps() throws GameException {
        try {
            Record[] records = gameData.getBoobytrapRecords();
            List result = new ArrayList(4);
            for (int i = 0; i < records.length; i++) {
                if (records[i].getField(FIELD_STATE).equals(VAL_PLACED)) {
                    result.add(boobytrapRecord2Element(records[i]));
                }
            }

            return (JXElement[]) result.toArray(new JXElement[0]);
        } catch (Throwable t) {
            throw new GameException("Error in getBoobytrap()", t);
        }
    }


    public JXElement getLocation(int anId) throws GameException {
        try {
            Record record = gameData.getById(anId);
            if (record == null || !record.hasField(FIELD_LOCATION)) {
                return null;
            }

            return record.getXMLField(FIELD_LOCATION);
        } catch (Throwable t) {
            throw new GameException("Error in getLocation()", t);
        }
    }


    public JXElement getPlayer(String aPlayerId) throws GameException {
        JXElement playerElement = (JXElement) players.get(aPlayerId);
        if (playerElement == null) {
            throw new GameException("getPlayer: cannot find player for id=" + aPlayerId);
        }
        return playerElement;
    }

    public JXElement getPlayerByName(String aPlayerName) throws GameException {
        String teamName = getTeamNameForPlayerName(aPlayerName);
        if (teamName == null) {
            return null;
        }
        JXElement playersElement = getTeamElement(teamName, TAG_PLAYERS);
        return playersElement.getChildByAttr(ATTR_NAME, aPlayerName);
    }

    public String getPlayerIdsForTeam(String aTeamName) throws GameException {
        JXElement[] players = getPlayersForTeam(aTeamName);
        String ids = "";
        for (int i = 0; i < players.length; i++) {
            if (!players[i].hasAttr(ATTR_AGENTID)) {
                continue;
            }
            ids += players[i].getAttr(ATTR_AGENTID);
            if (i != players.length - 1) {
                ids += ",";
            }
        }
        return ids;
    }

    public String getPlayerIdForType(String aTeamName, String aPlayerType) throws GameException {
        JXElement[] players = getPlayersForTeam(aTeamName);
        String playerId = null;
        for (int i = 0; i < players.length; i++) {
            // No use if player not online
            if (!players[i].hasAttr(ATTR_AGENTID)) {
                continue;
            }
            if (players[i].getAttr(ATTR_TYPE).equals(aPlayerType)) {
                playerId = players[i].getAttr(ATTR_AGENTID);
            }
        }
        return playerId;
    }

    public JXElement[] getPlayersForTeam(String aTeamName) throws GameException {
        return (JXElement[]) getTeamStatus(aTeamName, TAG_PLAYERS).getChildByTag(TAG_PLAYERS).getChildren().toArray(new JXElement[0]);
    }

    public JXElement getTeamForPlayer(String aPlayerId) throws GameException {
        JXElement playerElement = getPlayer(aPlayerId);

        // The team element is the parent of the parent (players)
        JXElement teamElement = playerElement.getParent().getParent();
        if (teamElement == null) {
            throw new GameException("getTeamForPlayer: cannot find team for player id=" + aPlayerId);
        }
        return teamElement;
    }

    public String getTeamNameForPlayer(String aPlayerId) throws GameException {
        return getTeamForPlayer(aPlayerId).getAttr(ATTR_TEAMNAME);
    }


    public String getTeamNameForPlayerName(String aPlayerName) throws GameException {
        Record playerRecord = gameData.getPlayerForName(aPlayerName);
        if (playerRecord == null) {
            return null;
        }
        return gameData.getTeamForPlayer(playerRecord).getStringField(FIELD_NAME);
    }

    public String getTeamAttr(String aTeamName, String anAttrName) throws GameException {
        return getTeamStatus(aTeamName, anAttrName).getAttr(anAttrName);
    }

    public JXElement getTeamElement(String aTeamName, String anElementName) throws GameException {
        return getTeamStatus(aTeamName, anElementName).getChildAt(0);
    }

    /**
     * Get names of all teams in game.
     */
    public String[] getTeamNames() throws GameException {
        JXElement teamList = statusElement.getChildByTag(TAG_TEAMLIST);
        Vector teamElements = teamList.getChildren();
        String[] result = new String[teamElements.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = ((JXElement) teamElements.elementAt(i)).getAttr(ATTR_TEAMNAME);
        }
        return result;
    }

    /**
     * Get status element for team working zone.
     */
    public JXElement getTeamWorkingZone(String aTeamName) throws GameException {
        JXElement zoneStatus = getTeamElement(aTeamName, TAG_ZONES);
        String workingZone = getTeamAttr(aTeamName, ATTR_ZONENAME);
        if (workingZone.equals(VAL_NONE)) {
            return null;
        }

        return zoneStatus.getChildByAttr(ATTR_ZONENAME, workingZone);
    }

    /**
     * Get status element for named team zone.
     */
    public JXElement getTeamZone(String aTeamName, String aZoneName) throws GameException {
        JXElement zoneStatus = getTeamElement(aTeamName, TAG_ZONES);
        return zoneStatus.getChildByAttr(ATTR_ZONENAME, aZoneName);
    }

    /**
     * Set status element for team working zone.
     */
    public void setTeamWorkingZone(String aTeamName, JXElement aNewZoneElement) throws GameException {
        JXElement zoneStatus = getTeamElement(aTeamName, TAG_ZONES);
        String newZoneName = aNewZoneElement.getAttr(ATTR_ZONENAME);
        setTeamAttr(aTeamName, ATTR_ZONENAME, newZoneName);
        JXElement currentZoneElement = zoneStatus.getChildByAttr(ATTR_ZONENAME, newZoneName);
        currentZoneElement.setAttrs(aNewZoneElement.getAttrs());
        setTeamElement(aTeamName, zoneStatus);
    }

    public String getTeamPower(String aTeamName) throws GameException {
        Record teamRecord = gameData.getTeamRecord(aTeamName);
        Record personageRecord = gameData.getRelatedRecord(teamRecord, TABLE_PERSONAGE, null);
        return personageRecord.getStringField(FIELD_POWER);
    }

    public JXElement getTeamStatus(String aTeamName) throws GameException {
        return getTeamStatus(aTeamName, null);
    }

    public JXElement getTeamStatus(String aTeamName, String aSelection) throws GameException {
        try {
            JXElement teamList = statusElement.getChildByTag(TAG_TEAMLIST);
            JXElement teamElement = teamList.getChildByAttr(ATTR_TEAMNAME, aTeamName);
            if (teamElement == null) {
                throw new GameException("Cannot find team status element teamname=" + aTeamName);
            }

            // Return entire team element if no further
            if (aSelection == null) {
                return teamElement;
            }

            // Do selection (single attr or child of team element)
            JXElement teamSubsetElement = new JXElement(TAG_TEAM);
            teamSubsetElement.setAttr(ATTR_TEAMNAME, aTeamName);
            if (teamElement.hasAttr(aSelection)) {
                teamSubsetElement.setAttr(aSelection, teamElement.getAttr(aSelection));
            } else {
                // Must be a child
                JXElement selectionElement = teamElement.getChildByTag(aSelection);
                if (selectionElement == null) {
                    throw new GameException("getTeamStatus: invalid select value " + aSelection);
                }
                teamSubsetElement.addChild(selectionElement);
            }
            return teamSubsetElement;
        } catch (Throwable t) {
            throw new GameException("Error in getTeamStatus()", t);
        }
    }

    /**
     * Get media linked to an assignment id done by team
     */
    public Vector getLinkedMediaForAssignment(String aTeamName, String anAssignmentId) throws GameException {
        JXElement zoneResultElement = getTeamElement(aTeamName, TAG_RESULTS);
        Vector zoneResults = zoneResultElement.getChildren();

        JXElement nextResult = null;
        JXElement assignmentElement = null;
        for (int i = 0; i < zoneResults.size(); i++) {
            nextResult = (JXElement) zoneResults.elementAt(i);
            assignmentElement = nextResult.getChildById(anAssignmentId);
            if (assignmentElement != null) {
                break;
            }
        }

        if (assignmentElement == null) {
            return null;
        }

        // OK found assignment, get media
        int answerId = assignmentElement.getIntAttr(ATTR_ANSWERID);
        if (answerId == -1) {
            return null;
        }

        return getLinkedMediaForAnswer(answerId);
    }

    /**
     * Get media linked to an answer id.
     */
    public Vector getLinkedMediaForAnswer(int anAnswerId) throws GameException {

        Record answerRecord = gameData.getById(anAnswerId, TABLE_ANSWER);

        Record[] requiredMediaRecords = gameData.getRelatedRecords(answerRecord, TABLE_MEDIUM, REL_TAG_REQUIRED);

        Record[] extraMediaRecords = gameData.getRelatedRecords(answerRecord, TABLE_MEDIUM, REL_TAG_EXTRA);
        Vector result = new Vector(requiredMediaRecords.length + extraMediaRecords.length);

        JXElement nextMedium;
        for (int i = 0; i < requiredMediaRecords.length; i++) {
            nextMedium = new JXElement(TAG_MEDIUM);
            nextMedium.setId(requiredMediaRecords[i].getId());
            nextMedium.setAttr(ATTR_TYPE, requiredMediaRecords[i].getStringField(FIELD_KIND));
            result.add(nextMedium);
        }
        for (int i = 0; i < extraMediaRecords.length; i++) {
            nextMedium = new JXElement(TAG_MEDIUM);
            nextMedium.setId(extraMediaRecords[i].getId());
            nextMedium.setAttr(ATTR_TYPE, extraMediaRecords[i].getStringField(FIELD_KIND));
            nextMedium.setAttr(ATTR_EXTRA, VAL_TRUE);
            result.add(nextMedium);
        }

        return result;
    }

    /**
     * Get open assignments for team that are to be done in a zone.
     */
    public int[] getTeamOpenAssignments(String aTeamName, String aZoneName) throws GameException {
        JXElement zoneElement = getTeamElement(aTeamName, TAG_ZONES).getChildByAttr(ATTR_ZONENAME, aZoneName);
        String zoneState = zoneElement.getAttr(ATTR_STATE);
        int[] result = new int[0];
        // get open assignments from results
        JXElement zoneResultElement = getTeamElement(aTeamName, TAG_RESULTS).getChildByAttr(ATTR_ZONENAME, aZoneName);

        // Determine assignment id's from zonestate and open assignments for that zone
        if (zoneState.equals(STATE_ZONE_TODO) ||
                zoneState.equals(STATE_ZONE_SELECTED) || zoneState.equals(STATE_ZONE_DONE)) {
            // Not right state for any open assignments
            ;
        } else if (zoneState.equals(STATE_ZONE_KEY_TODO)) {
            JXElement keyAssignmentElement = zoneResultElement.getChildByAttr(ATTR_TYPE, VAL_BEWIJS);
            if (keyAssignmentElement != null) {
                result = new int[1];
                result[0] = keyAssignmentElement.getIntAttr(ATTR_ID);
            }


        } else if (zoneState.equals(STATE_ZONE_TWO_TODO)) {
            result = new int[2];
            Vector assignmentElements = zoneResultElement.getChildren();
            JXElement nextElement = null;
            int resultIndex = 0;
            for (int i = 0; i < assignmentElements.size(); i++) {
                nextElement = (JXElement) assignmentElements.elementAt(i);
                if (nextElement.getAttr(ATTR_TYPE).equals(VAL_POORTER)) {
                    result[resultIndex++] = nextElement.getIntAttr(ATTR_ID);
                }
            }
        } else if (zoneState.equals(STATE_ZONE_ONE_TODO)) {
            result = new int[1];
            Vector assignmentElements = zoneResultElement.getChildren();
            JXElement nextElement = null;
            for (int i = 0; i < assignmentElements.size(); i++) {
                nextElement = (JXElement) assignmentElements.elementAt(i);
                if (nextElement.getAttr(ATTR_TYPE).equals(VAL_POORTER) && nextElement.getAttr(ATTR_STATE).equals(VAL_TODO)) {
                    result[0] = nextElement.getIntAttr(ATTR_ID);
                }
            }
        }
        return result;
    }


    public JXElement getZone(String aZoneName) throws GameException {
        try {
            Record zoneRecord = gameData.getZoneByName(aZoneName);
            /*
               <zone zonename="zone4"
                         description="De Plaets"
                   hqmediumid="3598"
                   lqmediumid="3598"
                   keyassignmentid="3434"
                   assignment1id="3441"
                   assignment2id="3489">
                   <teaser>
                      teaser text
                   </teaser>
                </zone>
               */
            JXElement zoneElement = new JXElement(TAG_ZONE);
            zoneElement.setAttr(ATTR_ZONENAME, zoneRecord.getStringField(ZONE_NAME));
            zoneElement.setAttr(ATTR_DESCRIPTION, zoneRecord.getStringField(ZONE_DESCRIPTION));

            /** Teaser text */
            JXElement teaserElement = zoneRecord.getXMLField(FIELD_EXTRA);
            if (teaserElement == null) {
                // init if not yet in db
                teaserElement = new JXElement(TAG_TEASER);
                teaserElement.setText("hallo, deze teaser text moet nog in db !!!");
                zoneRecord.setXMLField(FIELD_EXTRA, teaserElement);
                gameData.update(zoneRecord);
            }
            zoneElement.addChild(teaserElement);

            // Media ids (hq+lq)
            Record hqMediumRecord = gameData.getRelatedRecord(zoneRecord, TABLE_MEDIUM, REL_TAG_HQ);
            if (hqMediumRecord != null) {
                zoneElement.setAttr(ATTR_HQMEDIUMID, hqMediumRecord.getId());

                // Add thumbnail image if present
                Record thumbMediumRecord = gameData.getRelatedRecord(hqMediumRecord, TABLE_MEDIUM, REL_TAG_THUMB);
                if (thumbMediumRecord != null) {
                    zoneElement.setAttr(ATTR_THUMB, thumbMediumRecord.getId());
                }
            }

            Record lqMediumRecord = gameData.getRelatedRecord(zoneRecord, TABLE_MEDIUM, REL_TAG_LQ);
            if (lqMediumRecord != null) {
                zoneElement.setAttr(ATTR_LQMEDIUMID, lqMediumRecord.getId());
            }

            // Assignment ids
            Record keyAssignmentRecord = gameData.getRelatedRecord(zoneRecord, TABLE_ASSIGNMENT, REL_TAG_KEY);
            if (keyAssignmentRecord != null) {
                zoneElement.setAttr(ATTR_KEYASSIGNMENTID, keyAssignmentRecord.getId());
            }
            Record assignment1Record = gameData.getRelatedRecord(zoneRecord, TABLE_ASSIGNMENT, REL_TAG_1);
            if (assignment1Record != null) {
                zoneElement.setAttr(ATTR_ASSIGNMENTID1, assignment1Record.getId());
            }
            Record assignment2Record = gameData.getRelatedRecord(zoneRecord, TABLE_ASSIGNMENT, REL_TAG_2);
            if (assignment2Record != null) {
                zoneElement.setAttr(ATTR_ASSIGNMENTID2, assignment2Record.getId());
            }

            return zoneElement;
        } catch (Throwable t) {
            throw new GameException("Error in getZone()", t);
        }
    }

    public void init(JXElement aGameConfig) throws GameException {
        try {
            gameConfig = aGameConfig;

            gameData = new GameData(statusElement.getAttr(ATTR_NAME));
            gameData.init();
            // Init status stuff
            JXElement teamListElement = new JXElement(TAG_TEAMLIST);

            // Get teams and create team status elements
            Record[] teamRecords = gameData.getTeamRecords();
            if(teamRecords!=null){
                System.out.println("debug 3");
                for (int i = 0; i < teamRecords.length; i++) {
                    // Create team element
                    JXElement nextTeamElement = initTeamStatus(teamRecords[i]);

                    // Add to total list of  teams
                    teamListElement.addChild(nextTeamElement);
                }

                // Finally add team list to game element
                statusElement.addChild(teamListElement);
            }

        } catch (Throwable t) {
            throw new GameException("Error in GameStatus.init()", t);
        }
    }

    /**
     * Is team resolving a boobytrap.
     */
    public boolean isBoobytrapped(String aTeamName) throws GameException {
        JXElement teamActionsElement = getTeamElement(aTeamName, TAG_ACTIONS);
        JXElement currentBoobytrapElement = teamActionsElement.getChildByAttr(ATTR_TYPE, VAL_BOOBYTRAP);
        return currentBoobytrapElement != null && currentBoobytrapElement.getAttr(ATTR_STATUS).equals(VAL_ACTIVE);
    }

    /**
     * Is team invisible.
     */
    public boolean isCloaked(String aTeamName) throws GameException {
        return getTeamAttr(aTeamName, ATTR_CLOAKED).equals(VAL_TRUE);
    }

    /**
     * Relate media to answer of assignment.
     *
     * @return false if number of required media not ok; all other cases true
     */
    public boolean linkMedia(String aTeamName, int anAssignmentId, Vector theMedia) throws GameException {
        try {
            // This may create answer record if none existed
            JXElement assignmentResult = getAssignmentResult(aTeamName, anAssignmentId);
            int answerId = assignmentResult.getIntAttr(ATTR_ANSWERID);

            Record answerRecord = gameData.getById(answerId, TABLE_ANSWER);
            Record teamRecord = gameData.getTeamRecord(aTeamName);

            /*  e.g. <medium id="7777" type="image"/> */
            JXElement nextMediumElement = null;
            int imgCount = 0, vidCount = 0;

            for (int i = 0; i < theMedia.size(); i++) {
                nextMediumElement = (JXElement) theMedia.elementAt(i);
                String mediaType = nextMediumElement.getAttr(ATTR_TYPE);
                boolean extra = nextMediumElement.getBoolAttr(ATTR_EXTRA);
                if (mediaType.equals(VAL_IMAGE)) {
                    if (!extra) {
                        imgCount++;
                    }
                } else if (mediaType.equals(VAL_VIDEO)) {
                    if (!extra) {
                        vidCount++;
                    }
                } else if (mediaType.equals(VAL_TEXT)) {
                    ; // do nothing
                } else {
                    throw new GameException("Invalid media type=" + mediaType);
                }

                // Link each medium record to the answer record
                // relation tag indicates required or extra medium
                String relationTag = extra ? REL_TAG_EXTRA : REL_TAG_REQUIRED;
                int mediumRecordId = nextMediumElement.getIntAttr(ATTR_ID);
                Record mediumRecord = gameData.getById(mediumRecordId, TABLE_MEDIUM);
                if (!gameData.isRelated(teamRecord, mediumRecord)) {
                    // For tests only
                    gameData.relate(teamRecord, mediumRecord, REL_TAG_USED);
                } else {
                    gameData.setRelationTag(teamRecord, mediumRecord, REL_TAG_USED);
                }

                if (!gameData.isRelated(answerRecord, mediumRecord)) {
                    gameData.relate(answerRecord, mediumRecordId, relationTag);
                }
            }

            String resultState = assignmentResult.getAttr(ATTR_STATE);

            // Check if required media have been linked
            // do this only when assignment is still to be done
            // and the link requests contains required media
            // Extra media may be linked at any time
            // Check is by checking if required images/videos have been sent.
            if (resultState.equals(VAL_TODO) && (imgCount > 0 || vidCount > 0)) {
                JXElement assignmentInfo = getAssignmentInfo(anAssignmentId);
                int reqImages = assignmentInfo.getIntAttr(ATTR_REQIMAGES);
                int reqVideos = assignmentInfo.getIntAttr(ATTR_REQVIDEOS);
                if (imgCount >= reqImages && vidCount >= reqVideos) {
                    // OK: update result state and save
                    assignmentResult.setAttr(ATTR_STATE, VAL_MEDIADONE);
                    saveTeamStatus(aTeamName, TAG_RESULTS);
                } else {
                    // Unlink required media
                    gameData.unrelate(answerRecord, TABLE_MEDIUM);
                    return false;
                }
            }

            return true;
        } catch (Throwable t) {
            throw new GameException("Error adding medium for " + aTeamName, t);
        }
    }

    /**
     * Reset game data to initial state.
     */
    public void resetGame() throws GameException {
        try {
            String teamNames[] = getTeamNames();
            for (int i = 0; i < teamNames.length; i++) {
                resetTeam(teamNames[i]);
            }
        } catch (Throwable t) {
            throw new GameException("Error in resetGame()", t);
        }
    }

    /**
     * Reset team data to initial state.
     */
    public void resetTeam(String aTeamName) throws GameException {
        try {
            // Reset by clearing team data in db and re-initializing team
            Record teamRecord = gameData.getTeamRecord(aTeamName);

            // Reset location
            teamRecord.setXMLField(TEAM_LOCATION, null);

            // Reset score
            teamRecord.setXMLField(TEAM_SCORE, null);

            // Reset actions
            teamRecord.setXMLField(TEAM_ACTIONS, null);

            // Reset zones
            teamRecord.setXMLField(TEAM_ZONES, null);

            // Reset results
            teamRecord.setXMLField(TEAM_RESULTS, null);

            // Remove all team-related assets: content (answers + media) and boobytraps
            gameData.deleteAssetsForTeam(aTeamName);

            // Populate new team status element
            JXElement newTeamStatus = initTeamStatus(teamRecord);

            JXElement currentTeamStatus = getTeamStatus(aTeamName);
            currentTeamStatus.clear();

            currentTeamStatus.setAttrs(newTeamStatus.getAttrs());
            currentTeamStatus.addChildren(newTeamStatus.getChildren());

        } catch (Throwable t) {
            throw new GameException("Error in resetTeam()", t);
        }
    }

    public void saveTeamStatus(String aTeamName, String aStatusTag) throws GameException {
        try {
            Record teamRecord = gameData.getTeamRecord(aTeamName);
            if (teamRecord.hasField(aStatusTag)) {
                teamRecord.setXMLField(aStatusTag, getTeamElement(aTeamName, aStatusTag));
                gameData.update(teamRecord);
            }
        } catch (Throwable t) {
            throw new GameException("Error in saveTeamStatus()", t);
        }
    }

    /**
     * Place a boobytrap at location.
     */
    public JXElement placeBoobytrap(String aTeamName, JXElement aLocation) throws GameException {
        try {
            Record boobytrapRecord = gameData.insertBoobytrap(aTeamName, aLocation, aLocation.getAttr(ATTR_ZONENAME));
            JXElement boobytrapsElement = getTeamElement(aTeamName, TAG_BOOBYTRAPS);
            JXElement result = boobytrapRecord2Element(boobytrapRecord);
            boobytrapsElement.addChild(result);
            return result;
        } catch (Throwable t) {
            throw new GameException("Error adding boobytrap for " + aTeamName, t);
        }
    }

    public void clearBoobytrap(String aVictimTeamName) throws GameException {
        try {
            // Remove the boobytrap from actions in victim
            JXElement teamActionsElement = getTeamElement(aVictimTeamName, TAG_ACTIONS);
            JXElement activeBoobytrapElement = teamActionsElement.getChildByAttr(ATTR_TYPE, VAL_BOOBYTRAP);
            if (activeBoobytrapElement == null) {
                throw new GameException("Error in clearBoobytrap() no bt found for " + aVictimTeamName);
            }

            clearBoobytrap(aVictimTeamName, activeBoobytrapElement.getIntId());
        } catch (Throwable t) {
            throw new GameException("Error clearing boobytrap for " + aVictimTeamName, t);
        }
    }

    public void clearBoobytrap(String aVictimTeamName, int aBtId) throws GameException {
        try {

            // Update boobytrap state in db
            Record boobytrapRecord = gameData.getById(aBtId);
            boobytrapRecord.setStringField(FIELD_STATE, VAL_DONE);
            gameData.update(boobytrapRecord);

            // Delete relation between boobytrap and victim
            Record teamHitRecord = gameData.getTeamRecord(aVictimTeamName);
            gameData.unrelate(boobytrapRecord, teamHitRecord);

            // Remove the boobytrap from actions in victim
            JXElement teamActionsElement = getTeamElement(aVictimTeamName, TAG_ACTIONS);
            JXElement activeBoobytrapElement = teamActionsElement.getChildByAttr(ATTR_TYPE, VAL_BOOBYTRAP);
            if (activeBoobytrapElement != null) {
                // Update boobytrap action status
                activeBoobytrapElement.setAttr(ATTR_STATUS, VAL_DONE);
                teamActionsElement.removeChildById(aBtId + "");

                // Save team action status
                setTeamElement(aVictimTeamName, teamActionsElement);
            }

            // Update BT owner state
            String ownerTeamName = boobytrapRecord.getStringField(FIELD_OWNER);
            JXElement boobyTrapsElement = getTeamElement(ownerTeamName, TAG_BOOBYTRAPS);
            JXElement boobytrapElement = boobyTrapsElement.getChildById(aBtId + "");
            if (boobytrapElement != null) {
                boobytrapElement.setAttr(ATTR_STATE, activeBoobytrapElement.getAttr(VAL_DONE));
                setTeamElement(ownerTeamName, boobyTrapsElement);
            }

        } catch (Throwable t) {
            throw new GameException("Error in clearBoobytrap()", t);
        }
    }


    public void clearCloak(String aTeamName, int aCloakId) throws GameException {
        try {
            // Delete cloak record
            gameData.delete(aCloakId);

            // Update team status
            setTeamAttr(aTeamName, ATTR_CLOAKED, VAL_FALSE);

        } catch (Throwable t) {
            throw new GameException("Error in clearCloak()", t);
        }
    }

    public void detoneBoobytrap(String aVictimTeamName, JXElement aBoobytrap) throws GameException {
        try {
            // Update boobytrap status
            aBoobytrap.setAttr(ATTR_STATUS, VAL_ACTIVE);

            // Update BT owner state
            JXElement teamElement = getTeamElement(aBoobytrap.getAttr(ATTR_OWNER), TAG_BOOBYTRAPS);
            JXElement boobytrapElement = teamElement.getChildById(aBoobytrap.getId());
            boobytrapElement.setAttr(ATTR_STATE, aBoobytrap.getAttr(VAL_ACTIVE));

            // Set in db
            Record boobytrapRecord = gameData.getById(aBoobytrap.getIntId());
            Record teamHitRecord = gameData.getTeamRecord(aVictimTeamName);

            // Update state in db
            boobytrapRecord.setStringField(FIELD_STATE, VAL_ACTIVE);
            boobytrapRecord.setTimestampField(FIELD_DETONATIONDATE, new Timestamp(Sys.now()));

            int ttl = gameConfig.getIntAttr("boobytrapTime");
            boobytrapRecord.setIntField(FIELD_TIMETOLIVE, ttl * 60);
            boobytrapRecord.setStringField(FIELD_VICTIM, aVictimTeamName);
            gameData.update(boobytrapRecord);

            // Make relation between boobytrap and victim
            gameData.relate(boobytrapRecord, teamHitRecord, REL_TAG_VICTIM);
        } catch (Throwable t) {
            throw new GameException("Error in detoneBoobytrap()", t);
        }
    }

    /**
     * Update time-to-live field in arbitrary record (bt, cloak etc).
     */
    public void updateTimeToLive(int aRecordId, int aTTL) throws GameException {
        try {
            // Set in db
            Record record = gameData.getById(aRecordId);
            record.setIntField(FIELD_TIMETOLIVE, aTTL);
            gameData.update(record);

        } catch (Throwable t) {
            throw new GameException("Error in updateBoobytrapTTL()", t);
        }
    }

    public void putContent(String aFileName) throws GameException {
        try {

            File file = new File(aFileName);
            if (!file.exists()) {
                throw new GameException("File does not exist file=" + aFileName);
            }

            String mediaFileRoot = file.getParent();

            // Parse the file
            JXElement element = new JXBuilder().build(file);

            // Check assignment or team
            if (element.getTag().equals(TAG_ASSIGNMENT)) {
                // Add or replace assignment
                String zoneName = element.getAttr("zone");
                String type = element.getAttr(ATTR_TYPE);
                String name = element.getAttr(ATTR_NAME);

                // Determine assignment type and set relation tag
                String relTag = REL_TAG_KEY;
                if (aFileName.indexOf("poorter1") != -1) {
                    relTag = REL_TAG_1;
                    //name += REL_TAG_1;
                } else if (aFileName.indexOf("poorter2") != -1) {
                    relTag = REL_TAG_2;
                    //name += REL_TAG_1;
                }

                // Replace all media
                Record zoneRecord = gameData.getZoneByName(zoneName);

                // Delete and add zone media for bewijs
                if (type.equals("bewijs")) {
                    /*
                         <intro>
                              <videophone id="movies/videophone/IntroZ1_Scheepsbouwer.mp4"/>
                              <hq id="movies/hq/IntroZ1_Scheepsbouwer.swf"/>
                          </intro>

                         */
                    // Delete media related to zone
                    gameData.deleteRelatedRecords(zoneRecord, TABLE_MEDIUM, null);

                    // Re Add zone media for bewijs
                    JXElement introElement = element.getChildByTag("intro");
                    addMediumFile(mediaFileRoot, introElement.getChildByTag("videophone"), zoneRecord, REL_TAG_LQ);
                    addMediumFile(mediaFileRoot, introElement.getChildByTag("hq"), zoneRecord, REL_TAG_HQ);
                }

                // Now do assignment

                // Get all data elements of assignment
                JXElement infoElement = element.getChildByTag("info");
                JXElement locationElement = element.getChildByTag("location");
                JXElement contentElement = element.getChildByTag("content");
                String answer = element.getChildByTag("answer").getText().trim();

                // Get existing (mainly to keep id's constant we'll update existing
                // assignment records
                Record assignmentRecord = gameData.getRelatedRecord(zoneRecord, TABLE_ASSIGNMENT, relTag);

                // First the media related to the assignment
                if (assignmentRecord != null) {
                    gameData.deleteRelatedRecords(assignmentRecord, TABLE_MEDIUM, null);
                } else {
                    // Create new (will be updated below but is quickest implementation)
                    assignmentRecord = gameData.insertAssignment(zoneRecord, relTag, name, type, infoElement, locationElement, contentElement, answer);
                }

                // Insert all media

                // get <hq/> <videophone/> etc children
                Vector specialContentElements = contentElement.getChildren();

                // Go through all <part/> elements and insert medium/expand id
                // and trim text
                for (int i = 0; i < specialContentElements.size(); i++) {
                    JXElement nextPart = null;
                    String text;
                    JXElement nextSpecial = (JXElement) specialContentElements.elementAt(i);
                    Vector parts = nextSpecial.getChildren();
                    for (int j = 0; j < parts.size(); j++) {
                        nextPart = (JXElement) parts.elementAt(j);

                        // This is to strip spaces due to xml indent
                        if (nextPart.hasText()) {
                            text = nextPart.getText().trim();
                            nextPart.setText(text);
                        }

                        // If part has medium adds and relates file and expands "id" attr
                        if (nextPart.hasAttr(ATTR_MEDIUM)) {
                            addMediumFile(mediaFileRoot, nextPart, assignmentRecord, nextPart.getAttr(ATTR_MEDIUM));
                        }
                    }
                }

                // Update existing or new assignment always
                gameData.updateAssignment(assignmentRecord, name, type, infoElement, locationElement, contentElement, answer);

            } else if (element.getTag().equals(TAG_TEAM)) {

            }


        } catch (Throwable t) {
            throw new GameException("Error putContent file=" + aFileName, t);
        }
    }

    public void setCloak(String aTeamName) throws GameException {
        try {
            // Create DB record
            gameData.insertCloak(aTeamName, gameConfig.getIntAttr("cloakTime") * 60);
            JXElement teamElement = getTeamStatus(aTeamName);
            teamElement.setAttr(ATTR_CLOAKED, VAL_TRUE);
        } catch (Throwable t) {
            throw new GameException("Error in setCloak()", t);
        }
    }

    public void setState(String aState) throws GameException {
        try {
            Record gameRecord = gameData.getGameRecord();
            gameRecord.setStringField(GAME_STATE, aState);
            gameData.update(gameRecord);
        } catch (Throwable t) {
            throw new GameException("Error in setState()", t);
        }
    }

    public void setTeamAttr(String aTeamName, String anAttr, String aValue) throws GameException {
        try {
            JXElement teamElement = getTeamStatus(aTeamName);

            // TODO Some attrs may need to become persistent
            teamElement.setAttr(anAttr, aValue);
        } catch (Throwable t) {
            throw new GameException("Error in setTeamAttr()", t);
        }
    }

    public void setTeamElement(String aTeamName, JXElement aTeamElement) throws GameException {
        try {
            String tag = aTeamElement.getTag();
            Record teamRecord = gameData.getTeamRecord(aTeamName);
            if (teamRecord.hasField(tag)) {
                teamRecord.setXMLField(tag, aTeamElement);
                gameData.update(teamRecord);
            }

            // If data update ok update status XML
            JXElement teamStatusElement = getTeamStatus(aTeamName);
            teamStatusElement.replaceChildByTag(aTeamElement);
        } catch (Throwable t) {
            throw new GameException("Error in setTeamElement()", t);
        }
    }


    /**
     * Create team element from DB record.
     */
    private JXElement initTeamStatus(Record aTeamRecord) throws GameException {
        try {

            String teamName = aTeamRecord.getStringField(TEAM_NAME);

            // Create team element
            JXElement teamElement = new JXElement(TAG_TEAM);

            // Set general attrs from record
            teamElement.setAttr(ATTR_TEAMNAME, teamName);
            teamElement.setAttr(ATTR_STATE, aTeamRecord.getStringField(TEAM_STATE));

            // GPS device status: default is not applicable
            teamElement.setAttr(ATTR_GPS, VAL_NA);

            // Cloak status
            teamElement.setAttr(ATTR_CLOAKED, VAL_FALSE);
            Record cloakRecord = gameData.getCloakForTeam(teamName);
            if (cloakRecord != null && cloakRecord.getStringField(FIELD_STATE).equals(VAL_ACTIVE)) {
                teamElement.setAttr(ATTR_CLOAKED, VAL_TRUE);
            }

            // Personage
            Record personageRecord = gameData.getPersonageForTeam(aTeamRecord);
            teamElement.setAttr(ATTR_PERSONAGENAME, personageRecord.getStringField(PERSONAGE_NAME));

            // Add Players
            JXElement playersElement = new JXElement(TAG_PLAYERS);
            Record[] playerRecords = gameData.getPlayerRecords(aTeamRecord);
            for (int j = 0; j < playerRecords.length; j++) {
                Record nextPlayerRecord = playerRecords[j];
                JXElement nextPlayerElement = new JXElement(TAG_PLAYER);
                nextPlayerElement.setAttr(ATTR_TYPE, nextPlayerRecord.getStringField(PLAYER_TYPE));
                nextPlayerElement.setAttr(ATTR_NAME, nextPlayerRecord.getStringField(PLAYER_NAME));

                playersElement.addChild(nextPlayerElement);
            }

            teamElement.addChild(playersElement);

            // Add score
            JXElement scoreElement = aTeamRecord.getXMLField(TEAM_SCORE);
            if (scoreElement == null) {
                // First time: init
                scoreElement = new JXElement(TAG_SCORE);
                scoreElement.setAttr(ATTR_POINTS, 0);
                scoreElement.setAttr(ATTR_BOOBYTRAPS, 0);
                scoreElement.setAttr(ATTR_CLOAKS, 0);
                scoreElement.setAttr(ATTR_ZONES, 0);
                aTeamRecord.setXMLField(TEAM_SCORE, scoreElement);
            }

            if (!scoreElement.hasAttr(ATTR_ZONES)) {
                scoreElement.setAttr(ATTR_ZONES, 0);
            }

            teamElement.addChild(scoreElement);

            // Add location
            JXElement locationElement = aTeamRecord.getXMLField(TEAM_LOCATION);
            if (locationElement == null) {
                // First time: init
                locationElement = new JXElement(TAG_LOCATION);
                locationElement.setAttr(ATTR_LAT, 0.0D);
                locationElement.setAttr(ATTR_LON, 0.0D);
                locationElement.setAttr(ATTR_TIME, Sys.now());
                locationElement.setAttr(ATTR_MX, 0);
                locationElement.setAttr(ATTR_MY, 0);
                locationElement.setAttr(ATTR_RX, 0);
                locationElement.setAttr(ATTR_RY, 0);
                locationElement.setAttr(ATTR_ZONENAME, VAL_NONE);
                aTeamRecord.setXMLField(TEAM_LOCATION, locationElement);
            }

            teamElement.addChild(locationElement);

            // Add actions element
            JXElement actionsElement = aTeamRecord.getXMLField(TEAM_ACTIONS);
            if (actionsElement == null) {
                // First time: init
                actionsElement = new JXElement(TAG_ACTIONS);
                aTeamRecord.setXMLField(TEAM_ACTIONS, actionsElement);
            }
            teamElement.addChild(actionsElement);

            // Add zones element

            teamElement.setAttr(ATTR_ZONENAME, VAL_NONE);
            JXElement zonesElement = aTeamRecord.getXMLField(TEAM_ZONES);
            if (zonesElement == null) {
                // First time: init
                zonesElement = new JXElement(TAG_ZONES);
                Record[] zoneRecords = gameData.getZoneRecords();
                // Random as selected
                int selZoneIndex = Rand.randomInt(0, zoneRecords.length - 1);

                // HACK HACK: fix start zones for teams
                String[] TEAM_TO_ZONE_INDEX = {"blue", "red", "green", "orange", "purple", "yellow"};
                for (int i = 0; i < TEAM_TO_ZONE_INDEX.length; i++) {
                    if (teamName.equals(TEAM_TO_ZONE_INDEX[i])) {
                        selZoneIndex = i;
                        break;
                    }
                }

                for (int i = 0; i < zoneRecords.length; i++) {
                    JXElement nextZone = new JXElement(TAG_ZONE);
                    String nextZoneName = zoneRecords[i].getStringField(ZONE_NAME);
                    nextZone.setAttr(ATTR_ZONENAME, nextZoneName);
                    if (i == selZoneIndex) {
                        nextZone.setAttr(ATTR_STATE, STATE_ZONE_SELECTED);
                    } else {
                        nextZone.setAttr(ATTR_STATE, STATE_ZONE_TODO);
                    }
                    zonesElement.addChild(nextZone);
                }

                // Update zones in DB
                aTeamRecord.setXMLField(TEAM_ZONES, zonesElement);
            }

            // Determine working zone name
            // The zone must be in state: selected, keytodo or twotodo
            Vector zoneElements = zonesElement.getChildren();
            JXElement nextZone = null;
            String nextZoneState = null;
            for (int i = 0; i < zoneElements.size(); i++) {
                nextZone = (JXElement) zoneElements.elementAt(i);
                nextZoneState = nextZone.getAttr(ATTR_STATE);
                if (nextZoneState.equals(STATE_ZONE_SELECTED) ||
                        nextZoneState.equals(STATE_ZONE_KEY_TODO) ||
                        nextZoneState.equals(STATE_ZONE_TWO_TODO)) {
                    teamElement.setAttr(ATTR_ZONENAME, nextZone.getAttr(ATTR_ZONENAME));
                    break;
                }
            }

            teamElement.addChild(zonesElement);

            // Results elements
            /*
               <results>
                   <result zonename="zone4" cloaks="1" boobytraps="1">
                       <assignment type="bewijs" id="6565" answerid="4567" score="8"/>
                       <assignment type="poorter1" id="6564" answerid="4568" score="2"/>
                       <assignment type="poorter2" id="6567" answerid="4569" score="3"/>
                   </result>
                   <result zonename="zone2" cloaks="0" boobytraps="0">
                       .
                       .
                   </result>
               </results>
               */
            JXElement resultsElement = aTeamRecord.getXMLField(TEAM_RESULTS);
            if (resultsElement == null) {
                // First time: init
                resultsElement = new JXElement(TAG_RESULTS);

                Record[] zoneRecords = gameData.getZoneRecords();
                Record nextZoneRecord = null;
                Record nextAssignmentRecord = null;
                JXElement nextResultElement = null;
                JXElement nextAssignmentElement = null;
                for (int i = 0; i < zoneRecords.length; i++) {
                    nextZoneRecord = zoneRecords[i];
                    nextResultElement = new JXElement(TAG_RESULT);
                    nextResultElement.setAttr(ATTR_ZONENAME, nextZoneRecord.getStringField(FIELD_NAME));
                    nextResultElement.setAttr(ATTR_CLOAKS, 0);
                    nextResultElement.setAttr(ATTR_BOOBYTRAPS, 0);

                    Record[] assignments = gameData.getAssignmentsForZone(nextZoneRecord);
                    for (int j = 0; j < assignments.length; j++) {
                        nextAssignmentRecord = assignments[j];
                        nextAssignmentElement = new JXElement(TAG_ASSIGNMENT);
                        nextAssignmentElement.setAttr(ATTR_TYPE, nextAssignmentRecord.getStringField(FIELD_TYPE));
                        nextAssignmentElement.setAttr(ATTR_ID, nextAssignmentRecord.getId());
                        nextAssignmentElement.setAttr(ATTR_ANSWERID, -1);
                        nextAssignmentElement.setAttr(ATTR_SCORE, 0);
                        nextAssignmentElement.setAttr(ATTR_STATE, VAL_TODO);
                        nextResultElement.addChild(nextAssignmentElement);
                    }
                    resultsElement.addChild(nextResultElement);
                }

                // Update results in DB
                aTeamRecord.setXMLField(TEAM_RESULTS, resultsElement);
            }

            // Add results element to team element
            teamElement.addChild(resultsElement);

            // Boobytrap elements
            /*
               <boobytraps>
                   <boobytrap id="657" state="placed" owner="blue" victim="none">
                           <location lat="52.3587" lon="4.9009" zonename="zone3" rx="345" ry="876" mx="1234" my="543" time="3657856575"/>
                   </boobytrap>
                   <boobytrap id="456" state="placed" owner="blue" victim="red">
                       .
                       .
                   </boobytrap>
               </boobytraps>
               */
            JXElement boobytrapsElement = new JXElement(TAG_BOOBYTRAPS);
            Record[] ownedBoobytrapRecords = gameData.getOwnedBoobyTraps(aTeamRecord);
            // TODO: make more efficient
            for (int i = 0; i < ownedBoobytrapRecords.length; i++) {
                boobytrapsElement.addChild(boobytrapRecord2Element(ownedBoobytrapRecords[i]));
            }

            // Add boobytraps element to team element
            teamElement.addChild(boobytrapsElement);

            // Update team record in DB if any of the above changed the record
            if (aTeamRecord.isModified()) {
                gameData.update(aTeamRecord);
            }

            // Always return populated team element
            return teamElement;

        } catch (Throwable t) {
            throw new GameException("Error in initTeamStatus()", t);
        }
    }

    /**
     * Convert boobytrap record to XML element.
     */
    private JXElement boobytrapRecord2Element(Record aBoobytrapRecord) throws GameException {

        try {

            /*
               <boobytrap id="657" state="placed" owner="blue" victim="none">
                       <location lat="52.3587" lon="4.9009" zonename="zone3" rx="345" ry="876" mx="1234" my="543" time="3657856575"/>
                   </boobytrap>
                */
            JXElement result = new JXElement(TAG_BOOBYTRAP);
            result.setId(aBoobytrapRecord.getId());
            String state = aBoobytrapRecord.getStringField(FIELD_STATE);
            result.setAttr(ATTR_STATE, state);
            result.setAttr(ATTR_OWNER, aBoobytrapRecord.getStringField(FIELD_OWNER));
            result.addChild(aBoobytrapRecord.getXMLField(FIELD_LOCATION));

            result.setAttr(ATTR_VICTIM, aBoobytrapRecord.getStringField(FIELD_VICTIM));
            if (state.equals(VAL_ACTIVE)) {
                result.setAttr(ATTR_TIMETOLIVE, aBoobytrapRecord.getIntField(FIELD_TIMETOLIVE));
            }

            // If someone stepped into the BT there should be a victim
            // team record.

            /* Record victimRecord = gameData.getRelatedRecord(aBoobytrapRecord, TABLE_BOOBYTRAP, REL_TAG_VICTIM);
               if (victimRecord != null) {
                   result.setAttr(ATTR_VICTIM, aBoobytrapRecord.getStringField(ATTR_NAME));
               }  */
            return result;
        } catch (Throwable t) {
            throw new GameException("Error in boobytrapRecord2Element()", t);
        }

    }

    /**
     * Convert cloak record to XML element.
     */
    private JXElement cloakRecord2Element(Record aCloakRecord) throws GameException {

        try {

            /*
               <cloak id="657" state="active" owner="blue" />
                */
            JXElement result = new JXElement(TAG_CLOAK);
            result.setId(aCloakRecord.getId());
            result.setAttr(ATTR_STATE, aCloakRecord.getStringField(FIELD_STATE));
            result.setAttr(ATTR_OWNER, aCloakRecord.getStringField(FIELD_OWNER));
            result.setAttr(ATTR_TIMETOLIVE, aCloakRecord.getIntField(FIELD_TIMETOLIVE));
            return result;
        } catch (Throwable t) {
            throw new GameException("Error in cloakRecord2Element()", t);
        }
    }

    /**
     * specialized method for adding media file and expanding "id" attr.
     */
    private JXElement addMediumFile(String aFileRoot, JXElement aMediumElement, Record aRelatedRecord, String aRelTag) throws GameException {
        try {

            // Create and check file path
            String fileName = aMediumElement.getId();
            String filePath = aFileRoot + File.separator + fileName;
            if (!new File(filePath).exists()) {
                throw new GameException("file " + filePath + " does not exist");
            }

            // Fill in fields
            HashMap fields = new HashMap(2);
            fields.put(ATTR_NAME, fileName);
            String type = null;
            if (aMediumElement.hasAttr(ATTR_MEDIUM)) {
                type = aMediumElement.getAttr(ATTR_MEDIUM);
            }

            // insert
            Record mediumRecord = gameData.insertMedium(filePath, type, fields);

            // relate
            gameData.relate(mediumRecord, aRelatedRecord, aRelTag);

            // Set "id" attr in element
            aMediumElement.setId(mediumRecord.getId());

            //  If thumbnail specified insert, relate and provide id
            if (aMediumElement.hasAttr(ATTR_THUMB)) {
                String thumbFileName = aMediumElement.getAttr(ATTR_THUMB);
                String thumbFilePath = aFileRoot + File.separator + thumbFileName;
                if (!new File(thumbFilePath).exists()) {
                    throw new GameException("thumbfile " + thumbFilePath + " does not exist");
                }

                // Fill in fields
                fields = new HashMap(2);
                fields.put(ATTR_NAME, thumbFileName);

                // insert
                Record thumbMediumRecord = gameData.insertMedium(thumbFilePath, VAL_IMAGE, fields);

                // Relate
                gameData.relate(thumbMediumRecord, aRelatedRecord, VAL_IMAGE);

                // Relate to medium as well
                gameData.relate(mediumRecord, thumbMediumRecord, REL_TAG_THUMB);

                // Set "thumb" attr in element to record id
                aMediumElement.setAttr(ATTR_THUMB, thumbMediumRecord.getId());

            }

            // Result: media file/thumb names replaced by record ids
			return aMediumElement;
		} catch (Throwable t) {
			throw new GameException("Error in addMediumFile() for file=" + aMediumElement.getId(), t);
		}
	}

}
