/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.walkandplay.server.engine;

/**
 * Callback for indications from GameEngine.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public interface IndicationListener {
    void onIndication(GameMessage aMsg);
}

/*
 * $Log: IndicationListener.java,v $
 * Revision 1.1.1.1  2006/04/03 09:21:36  rlenz
 * Import of Mobgame
 *
 * Revision 1.4  2004/11/22 10:12:35  just
 * *** empty log message ***
 *
 * Revision 1.3  2004/10/14 15:50:55  just
 * *** empty log message ***
 *
 *
 */

