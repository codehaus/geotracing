/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.geotracing.client.kwx;

import nl.justobjects.mjox.JXElement;

/** Live events after entering Space. */
public interface SpaceListener {
	/** Called when another user enters Space. */
	public void onEnterIndication(JXElement agentFrom);

	/** Called when another user exits Space. */
	public void onExitIndication(JXElement agentFrom, String reason);

	/** Called when another user joins an Amulet. */
	public void onJoinIndication(JXElement agentFrom, JXElement joinCommand);

	/** Called when another user leaves an Amulet. */
	public void onLeaveIndication(JXElement agentFrom, String anAmuletId);

	/** Indication from Amulet. */
	public void onAmuletIndication(JXElement agentFrom, String anAmuletId, JXElement anIndication);

	/** Request from Amulet (forward compat reasons patch protocol here). */
	public void onAmuletRequest(JXElement agentFrom, String anAmuletId, JXElement aRequest);

	public void onError(String errorMessage);
}

/*
 * $Log: SpaceListener.java,v $
 * Revision 1.1  2005/08/08 14:46:43  just
 * added subscribe services to KWClient
 *
 * Revision 1.1  2005/06/17 21:15:40  just
 * KWClient moved from server branch to here
 *
 * Revision 1.1  2005/06/17 20:47:33  just
 * renamed ClientHelper* to KWClient
 *
 * Revision 1.5  2004/06/28 12:25:46  justb
 * Logging changes
 *
 * Revision 1.4  2004/06/01 18:42:51  justb
 * added/replaced copyright from Just Objects to WS
 *
 * Revision 1.3  2003/10/27 17:00:33  justb
 * first step of conversion to JO JOX
 *
 * Revision 1.2  2003/09/30 14:44:45  justb
 * many changes for new protocol (amulet-specific changes)
 *
 * Revision 1.1.1.1  2003/07/10 14:28:50  justb
 * no message
 *
 * Revision 1.4  2003/01/29 11:06:31  just
 * no msg
 *
 * Revision 1.3  2002/11/14 21:20:47  just
 * nl.justobjects.jox moved to package org.keyworx.amuse.jox
 *
 * Revision 1.2  2001/08/09 14:17:30  kstroke
 * clienthelper.onJoinIndication interface change
 *
 * Revision 1.1  2000/05/12 12:08:20  just
 * new
 *
 *
 */

/*
   Copyright (C)2000 Just A. van den Broecke <just@justobjects.nl>

   This program is free software; you can redistribute it and/or
   modify it under the terms of the GNU General Public License
   as published by the Free Software Foundation; either version 2
   of the License, or (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program; if not, write to the Free Software
   Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/