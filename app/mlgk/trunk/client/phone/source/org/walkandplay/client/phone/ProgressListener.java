package org.walkandplay.client.phone;

public interface ProgressListener {

    void prStart();

    void prSetContentLength(int aContentLength);

    void prProgress(int anAmount);

    void prStop();

    void prError(String aMessage);

}
