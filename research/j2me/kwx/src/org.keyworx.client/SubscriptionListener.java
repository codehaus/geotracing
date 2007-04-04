/****************************************************************
 * Copyright 2004 - Waag Society - www.waag.org - See license below *
 ****************************************************************/

package org.geotracing.client.kwx;

import nl.justobjects.mjox.JXElement;


/** Events from object/relation subscriptions. */
public interface SubscriptionListener {

	/** Object created, updated or deleted */
	public void onObjectIndication(JXElement anIndication);

	/** Relation between two objects created, updated or deleted */
	public void onRelationIndication(JXElement anIndication);
}

/*
 * $Log: SubscriptionListener.java,v $
 * Revision 1.1  2005/08/08 14:46:43  just
 * added subscribe services to KWClient
 *
 *
 */

