// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$
package org.geotracing.handler;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.utopia.core.data.ErrorCode;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.session.UtopiaRequest;
import org.keyworx.utopia.core.util.Oase;

/**
 * Utilities, shorthands to use in Utopia Handlers.
 * <p/>
 *
 * @author Just van den Broecke
 * @version $Id$
 */
public class HandlerUtil {

	/**
	 * Get Oase session (utopia) from request.
	 */
	public static Oase getOase(UtopiaRequest anUtopiaReq) {
		return anUtopiaReq.getUtopiaSession().getContext().getOase();
	}

	/**
	 * Get user (Person) id from request.
	 */
	public static int getUserId(UtopiaRequest anUtopiaReq)  {
		return Integer.parseInt(anUtopiaReq.getUtopiaSession().getContext().getUserId());
	}

	/**
	 * Get user (Person) name from request.
	 */
	public static String getUserName(UtopiaRequest anUtopiaReq)  {
		return anUtopiaReq.getUtopiaSession().getContext().getUserName();
	}

	/**
	 * Throw exception when attribute empty or not present.
	 */
	public static void throwOnMissingAttr(String aName, String aValue) throws UtopiaException {
		if (aValue == null || aValue.length() == 0) {
			throw new UtopiaException("Missing name=" + aName + " value=" + aValue, ErrorCode.__6002_Required_attribute_missing);
		}
	}

	/**
	 * Throw exception when child element empty not present.
	 */
	public static void throwOnMissingChildElement(JXElement aParentElement, String aChildTag) throws UtopiaException {
		if (aParentElement.getChildByTag(aChildTag) == null) {
			throw new UtopiaException("Missing element name=" + aChildTag + " in element=" + aParentElement.getTag(), ErrorCode.__6002_Required_attribute_missing);
		}
	}

	/**
	 * Throw exception when numeric attribute empty or not present.
	 */
	public static void throwOnNonNumAttr(String aName, String aValue) throws UtopiaException {
		throwOnMissingAttr(aName, aValue);
		try {
			Long.parseLong(aValue);
		} catch (Throwable t) {
			throw new UtopiaException("Invalid numvalue name=" + aName + " value=" + aValue, ErrorCode.__6004_Invalid_attribute_value);
		}
	}

	/**
	 * Throw exception when numeric attribute empty or not present.
	 */
	public static void throwOnNegNumAttr(String aName, long aValue) throws UtopiaException {
		if (aValue == -1) {
			throw new UtopiaException("Invalid numvalue name=" + aName + " value=" + aValue, ErrorCode.__6004_Invalid_attribute_value);
		}
	}


}
