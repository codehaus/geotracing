package org.walkandplay.client.phone.util;

public interface DownloadListener {

    void dlStart();

    void dlSetContentLength(int aContentLength);

    void dlProgress();

    void dlStop();

    void dlError(String aMessage);

}
