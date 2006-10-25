package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import java.util.TimerTask;
import java.util.Timer;

/**
 * Created by IntelliJ IDEA.
 * User: ronald
 * Date: Oct 25, 2006
 * Time: 5:44:40 PM
 * To change this template use File | Settings | File Templates.
 */
public class Texter {

    private String text = "";
    public Texter(String theInputString){
        text = theInputString;
    }

    /*synchronized void keyConfirmed() {
        if (keyMajor != -1) {
            if (screenStat == DROP_TEXT_STAT || screenStat == RESPOND_TEXT_STAT) {
                inputText += keys[keyMajor].charAt(keyMinor);
            } else
            if (screenStat == TAG_TEXT_STAT || screenStat == RESPOND_TAG_TEXT_STAT || screenStat == TAG_IMAGE_STAT || screenStat == RESPOND_TAG_IMAGE_STAT)
            {
                if (tagMenuState == TITLE_SELECTED) {
                    inputTitle += keys[keyMajor].charAt(keyMinor);
                } else if (tagMenuState == TAG1_SELECTED) {
                    inputTag1 += keys[keyMajor].charAt(keyMinor);
                } else if (tagMenuState == TAG2_SELECTED) {
                    inputTag2 += keys[keyMajor].charAt(keyMinor);
                } else if (tagMenuState == TAG3_SELECTED) {
                    inputTag3 += keys[keyMajor].charAt(keyMinor);
                }
            }
            keyMajor = -1;
            repaint();
        }
    }

    class KeyConfirmer extends TimerTask {
        Canvas canvas;

        private KeyConfirmer(Canvas aCanvas) {
            canvas = aCanvas;
        }

        public void run() {
            canvas.keyConfirmed();
        }
    }

    public void write(){
        if (keyTimer != null) keyTimer.cancel();

        int index = key - KEY_NUM0;

        if (index < 0 || index > keys.length)
            keyMajor = -1;
        else {
            if (index != keyMajor) {
                keyMinor = 0;
                keyMajor = index;
            } else {
                keyMinor++;
                if (keyMinor >= keys[keyMajor].length())
                    keyMinor = 0;
            }

            keyTimer = new Timer();
            keyTimer.schedule(new KeyConfirmer(this), 500);
        }
    }*/
}
