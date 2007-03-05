// Copyright (c) 2005+ Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.client;

/**
 * Observer for Net class clients.
 *
 * @author  Just van den Broecke
 * @version $Id$
 */
public interface NetListener {

	public void onNetInfo(String theInfo);

	public void onNetError(String aReason, Throwable anException);

	public void onNetStatus(String aStatusMsg);
}
