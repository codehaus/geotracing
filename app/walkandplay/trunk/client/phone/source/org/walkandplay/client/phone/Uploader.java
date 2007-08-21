package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import org.geotracing.client.HTTPUploader;

public class Uploader {

    public JXElement uploadMedium(String anAgentKey, String aKWUrl, String aName, String aDescription, String aType, String aMime, long aTime, byte[] theData, boolean encode) {
		HTTPUploader uploader = new HTTPUploader();
		JXElement rsp = null;
		try {
			uploader.connect(aKWUrl + "/media.srv");
			if (aName == null || aName.length() == 0) {
				aName = "unnamed " + aType;
			}

            Log.log("agentkey:" + anAgentKey);

            uploader.writeField("agentkey", anAgentKey);
			uploader.writeField("name", aName);
			uploader.writeField("description", aDescription);
			uploader.writeFile(aName, aMime, "mobit-upload", theData);

            rsp = uploader.getResponse();

        } catch (Throwable t) {
			Log.log("Upload error: " + t);
		}
		return rsp;
	}
}
