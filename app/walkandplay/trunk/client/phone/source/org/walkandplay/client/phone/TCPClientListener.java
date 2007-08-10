package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

public interface TCPClientListener {

    public void accept(XMLChannel anXMLChannel, JXElement aResponse);

    public void onStop(XMLChannel anXMLChannel, String aReason);

}
