package org.walkandplay.client.phone;

import nl.justobjects.mjox.JXElement;
import nl.justobjects.mjox.XMLChannel;

public interface TCPClientListener {

    public void accept(XMLChannel anXMLChannel, JXElement aResponse);

    public void onNetStatus(String aStatus);

    public void onConnected();

    public void onError(String anErrorMessage);

    public void onFatal();

}
