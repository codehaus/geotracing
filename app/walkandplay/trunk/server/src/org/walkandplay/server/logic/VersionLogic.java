/**
 *
 *	cd:	Fri May 07 15:55:07 CEST 2004
 *	author:	ronald
 *
 *	$Id: RSSLogic.java,v 1.1.1.1 2005/08/26 13:00:15 rlenz Exp $
 *************************************************************/

package org.walkandplay.server.logic;

import org.keyworx.common.log.Log;
import org.keyworx.common.log.Logging;
import org.keyworx.oase.api.Record;
import org.keyworx.utopia.core.data.UtopiaException;
import org.keyworx.utopia.core.util.Oase;
import org.walkandplay.server.util.Constants;
import nl.justobjects.jox.dom.JXElement;

/**
 *
 *
 *
 */
public class VersionLogic {
	private Oase oase;
	private Log log = Logging.getLog("VersionLogic");

	public final static int INACTIVE_STATE = 0;
	public final static int ACTIVE_STATE = 1;

	public VersionLogic(Oase o) {
		oase = o;
        try{
            createVersion("1.0", "Initial release", ACTIVE_STATE);
        }catch(Throwable ignore){}
    }

	/**
	 * Return the current version of the mobile application.
	 *
	 * @return
	 * @throws UtopiaException
	 */
	public JXElement getVersion() throws UtopiaException {
		try {
			Record[] records = oase.getFinder().readAll(Constants.VERSION_TABLE);
			if (records == null || records.length == 0) {
				createVersion("1.0", "Initial release", ACTIVE_STATE);
			}
			return toXML(records[0]);
		} catch (Throwable t) {
			throw new UtopiaException("Exception in VersionLogic.getVersion() : " + t.toString());
		}
	}

    private JXElement toXML(Record aVersionRecord){
        JXElement version = new JXElement("vesion");
        version.setAttr(Constants.VERSION_FIELD, aVersionRecord.getStringField(Constants.VERSION_FIELD));
        version.setAttr(Constants.VERSION_FIELD, aVersionRecord.getStringField(Constants.DESCRIPTION_FIELD));
        version.setAttr(Constants.STATE_FIELD, aVersionRecord.getIntField(Constants.STATE_FIELD));
        return version;
    }

    /**
	 * Creates an application version.
	 *
	 * @param aVersionNr The version nr.
	 * @param aDescription The description.
	 * @param aState The state.
	 * @throws UtopiaException
	 */
	public void createVersion(String aVersionNr, String aDescription, int aState) throws UtopiaException {
		try {
			Record[] recs = oase.getFinder().queryTable(Constants.VERSION_TABLE, Constants.VERSION_FIELD + "='" + aVersionNr + "'", null, null);
			if (recs != null && recs.length > 0) return;

			Record record = oase.getModifier().create(Constants.VERSION_TABLE);
			record.setStringField(Constants.VERSION_FIELD, aVersionNr);
			record.setStringField(Constants.DESCRIPTION_FIELD, aDescription);
			record.setIntField(Constants.STATE_FIELD, aState);
			oase.getModifier().insert(record);
		} catch (Throwable t) {
			log.error("Exception in VersionLogic.createVersion() : " + t.toString());
			throw new UtopiaException("Exception in VersionLogic.createVersion() : " + t.toString());
		}
	}

}

/*
* $Log:
*/
