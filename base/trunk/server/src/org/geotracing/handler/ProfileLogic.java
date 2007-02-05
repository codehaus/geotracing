// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.$

package org.geotracing.handler;

import nl.justobjects.jox.dom.JXElement;
import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.common.util.MD5;
import org.keyworx.oase.api.OaseException;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.keyworx.plugin.tagging.logic.TagLogic;

/**
 * Performs user profile management actions.
 */
public class ProfileLogic {
	private Oase oase;
	private Log log = Logging.getLog("ProfileLogic");
	public static final String FIELD_TAGS = "tags";
	public static final String FIELD_PASSWORD = "password";
	public static final String FIELD_PHOTOID = "photoid";
	public static final String FIELD_EXTRA = "extra";
	public static final String TAG_PROFILE = "profile";
	public static final String[] PERSON_FIELDS = {"firstname", "lastname", "email", "mobilenr"};
	public static final String[] PERSON_EXTRA_FIELDS = {"desc", "profilepublic", "emailpublic", "locationpublic"};
	public static final String[] ACCOUNT_FIELDS = {FIELD_PASSWORD};
	public static final String REL_TAG_PHOTO = "thumb";
	public static final String TABLE_MEDIUM = "base_medium";
	public static final String TABLE_ACCOUNT = "utopia_account";

	public ProfileLogic(Oase o) {
		oase = o;
	}

	/**
	 * Get a user profile
	 *
	 * @param aPersonId person id for profile
	 * @throws UtopiaException
	 */
	public JXElement getProfile(int aPersonId) throws UtopiaException {
		try {

			// First get current person/account info
			Record person, thumb = null;
			person = oase.getFinder().read(aPersonId);
			Record[] thumbs = oase.getRelater().getRelated(person, TABLE_MEDIUM, REL_TAG_PHOTO);
			if (thumbs.length > 0) {
				thumb = thumbs[0];
			}

			JXElement profile = new JXElement(TAG_PROFILE);
			String value = null;

			// Person info
			for (int i = 0; i < PERSON_FIELDS.length; i++) {
				value = person.getField(PERSON_FIELDS[i]).toString();
				profile.setChildText(PERSON_FIELDS[i], value);
			}

			// Person extra field
			JXElement extra = person.getXMLField(FIELD_EXTRA);
			if (extra != null) {
				for (int i = 0; i < PERSON_EXTRA_FIELDS.length; i++) {
					value = extra.getChildText(PERSON_EXTRA_FIELDS[i]);
					profile.setChildText(PERSON_EXTRA_FIELDS[i], value);
				}
			}

			// Get tags

			// Get user icon

			return profile;

		} catch (OaseException ue) {
			throw new UtopiaException("DB Exception in getProfile()", ue);
		} catch (Throwable t) {
			throw new UtopiaException("Unexpected Exception in getProfile()", t);
		}
	}

	/**
	 * Updates a user profile
	 *
	 * @param aProfileElm XML element containing profile-related elements
	 * @throws UtopiaException
	 */
	public void updateProfile(JXElement aProfileElm) throws UtopiaException {
		try {
			int personId = aProfileElm.getIntId();
			log.info("Updating profile for id=" + personId);

			// First get current person/account info
			Record person, account, thumb = null;
			person = oase.getFinder().read(personId);
			account = oase.getRelater().getRelated(person, TABLE_ACCOUNT, null)[0];
			Record[] thumbs = oase.getRelater().getRelated(person, TABLE_MEDIUM, REL_TAG_PHOTO);
			if (thumbs.length > 0) {
				thumb = thumbs[0];
			}

			// Account updates
			String value = null;
			for (int i = 0; i < ACCOUNT_FIELDS.length; i++) {
				value = aProfileElm.getChildText(ACCOUNT_FIELDS[i]);
				if (value != null) {
					if (ACCOUNT_FIELDS[i].equals(FIELD_PASSWORD)) {
						value = MD5.createStringDigest(value);
					}
					account.setField(ACCOUNT_FIELDS[i], value);
				}
			}

			// Person updates
			for (int i = 0; i < PERSON_FIELDS.length; i++) {
				value = aProfileElm.getChildText(PERSON_FIELDS[i]);
				if (value != null) {
					person.setField(PERSON_FIELDS[i], value);
				}
			}

			// Person extra field
			JXElement extra = person.getXMLField(FIELD_EXTRA);
			if (extra == null) {
				extra = new JXElement(TAG_PROFILE);
			}

			for (int i = 0; i < PERSON_EXTRA_FIELDS.length; i++) {
				value = aProfileElm.getChildText(PERSON_EXTRA_FIELDS[i]);
				if (value != null) {
					extra.setChildText(PERSON_EXTRA_FIELDS[i], value);
				}
			}

			if (value != null) {
				person.setXMLField(FIELD_EXTRA, extra);
			}

			// Save updates
			if (person.isModified()) {
				oase.getModifier().update(person);
			}

			if (account.isModified()) {
				oase.getModifier().update(account);
			}

			// Update tags

			// Update user icon
			value = aProfileElm.getChildText(FIELD_TAGS);
			if (value != null && value.length() > 0) {
				TagLogic tagLogic = new TagLogic(oase.getOaseSession());
				int[] items = {personId};
				String[] tags = value.split(" ");
				tagLogic.tag(personId, items, tags, TagLogic.MODE_REPLACE);
			}

		} catch (OaseException ue) {
			throw new UtopiaException("DB Exception in updateProfile()", ue);
		} catch (Throwable t) {
			throw new UtopiaException("Unexpected Exception in updateProfile()", t);
		}
	}
}

