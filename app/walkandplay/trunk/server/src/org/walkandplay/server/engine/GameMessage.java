/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.walkandplay.server.engine;

import nl.justobjects.jox.dom.JXElement;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Message exchanged with GameEngine.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GameMessage implements GameProtocol {
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    // player, team or everybody
    public String fromType = TYPE_SINGLE;
    public String from = VAL_NONE;
    private JXElement data;
    public String toType = TYPE_SINGLE;
    public String to = VAL_NONE; // May be comma separated list for multiple receivers

    private GameMessage(JXElement theData) {
        data = theData;
    }


    public static GameMessage createRequest(JXElement theRequest, String aFromId) {
        GameMessage request = new GameMessage(theRequest);
        request.from = aFromId;
        return request;
    }

    public static GameMessage createResponse(GameMessage aRequest, JXElement theResponse) {
        GameMessage response = new GameMessage(theResponse);
        response.fromType = TYPE_ENGINE;
        response.from = TYPE_ENGINE;
        response.to = aRequest.from;
        return response;
    }

    public static GameMessage createNegativeResponse(GameMessage aRequest, String aReason) {
        JXElement nrspElement = new JXElement(aRequest.data.getTag().replaceAll(POSTFIX_REQ, POSTFIX_NRSP));
        nrspElement.setAttr("error", aReason);
        return createResponse(aRequest, nrspElement);
    }

    /**
     * Create indication message to all players.
     */
    public static GameMessage createIndication(JXElement theIndication) {
        return createIndication(theIndication, TYPE_ALL);
    }

    public static GameMessage createIndication(String aFromId, JXElement theIndication, String theToType) {
        return createIndication(TYPE_SINGLE, aFromId, theIndication, theToType, null);
    }

    public static GameMessage createIndication(JXElement theIndication, String theToType) {
        return createIndication(theIndication, theToType, null);
    }

    public static GameMessage createIndication(JXElement theIndication, String theToType, String theToId) {
        return createIndication(TYPE_ENGINE, TYPE_ENGINE, theIndication, theToType, theToId);
    }

    public static GameMessage createIndication(String aFromType, String aFromId, JXElement theIndication, String theToType, String theToId) {
        GameMessage indication = new GameMessage(theIndication);
        indication.fromType = aFromType;
        indication.from = aFromId;
        indication.toType = theToType;
        indication.to = theToId;
        return indication;
    }

    public JXElement getData() {
        return data;
    }

    public String toString() {
        return "from=" + fromType + "," + from + " to=" + toType + "," + to + " data=" + data;
    }

    public JXElement toXML() {
        JXElement msgElement = new JXElement("msg");
        msgElement.setAttr("fromType", fromType);
        msgElement.setAttr("from", from);
        msgElement.setAttr("toType", toType);
        msgElement.setAttr("to", to);
        msgElement.setAttr(ATTR_TIME, dateFormat.format(new Date()));
        msgElement.addChild(data);
        return msgElement;
	}
}
