/********************************************************
 * Copyright (C)2002 - Waag Society - See license below *
 ********************************************************/

package org.walkandplay.server.engine;

/**
 * Generic exception for OASE.
 * <p/>
 * <h3>Purpose</h3>
 * <p/>
 * General explanation
 * <p/>
 * <h3>Examples</h3>
 * <p/>
 * <p/>
 * <h3>Implementation</h3>
 * <p/>
 * <p/>
 * <h3>Concurrency</h3>
 * not apllicable
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class GameException extends Exception {

    public GameException(String aMessage, Throwable t) {
        super(aMessage + "\n embedded exception=" + t.toString());
    }

    public GameException(String aMessage) {
        super(aMessage);
    }

    public GameException(Throwable t) {
        this("GameException: ", t);
    }

    public String toString() {
        return "GameException: " + getMessage();
    }
}

/*
 * $Log: GameException.java,v $
 * Revision 1.1.1.1  2006/04/03 09:21:35  rlenz
 * Import of Mobgame
 *
 * Revision 1.1  2004/10/06 09:20:18  just
 * weeer een versie
 *
 * Revision 1.1  2004/10/04 15:03:13  just
 * *** empty log message ***
 *
 *
 */