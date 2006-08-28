// Copyright (c) 2000 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.server;

import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.session.UtopiaResponse;

/**
 * Plug-in point to act on handler request/response.
 *
 * To be implemented for specific "before/after" processing.
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public interface HandlerInterceptor {

	/**
	 * Callback before request is handled.
	 *
	 * @param anUtopiaReq an UtopiaRequest
	 * @return UtopiaResponse return this response or null (go on processing)
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	public UtopiaResponse beforeRequest(UtopiaRequest anUtopiaReq) throws UtopiaException;

	/**
	 * Callback after response has been sent.
	 *
	 * @param anUtopiaReq A UtopiaRequest
	 * @return A UtopiaResponse.
	 * @exception org.keyworx.utopia.core.data.UtopiaException Standard Utopia exception
	 */
	public boolean afterResponse(UtopiaRequest anUtopiaReq, UtopiaResponse anUtopiaRsp) throws UtopiaException;
}

/*
* $Log: HandlerInterceptor.java,v $
* Revision 1.1  2005/10/13 12:55:23  just
* *** empty log message ***
*
*
*
*/



