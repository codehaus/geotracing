package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import java.util.TimerTask;
import java.util.Timer;

public class Texter {

    private String inputText = "";
    private static final String[] keys = {" 0", ".,-!?@:;1", "aAbBcC2", "dDeEfF3", "gGhHiI4", "jJkKlL5", "mMnNoO6", "pPqQrRsS7", "tTuUvV8", "wWxXyYzZ9"};
    private Timer keyTimer;
    private int keyMajor = -1;
    private int keyMinor;
    private Canvas canvas;

    public Texter(Canvas aCanvas){
        System.out.println("Texter");
        canvas = aCanvas;
    }
    
    synchronized void keyConfirmed() {
        if (keyMajor != -1) {
            inputText += keys[keyMajor].charAt(keyMinor);
            keyMajor = -1;
            canvas.repaint();
        }
    }

    class KeyConfirmer extends TimerTask {
        public void run() {
            keyConfirmed();
        }
    }

    public String deleteChar(){
        System.out.println("Texter.deleteChar");
        return inputText.substring(0, inputText.length() - 1);
    }

    public String getSelectedKey(){
        System.out.println("Texter.getSelectedKey");
        String keySelect = "";
        if (keyMajor != -1) {
            String all = keys[keyMajor];
            keySelect = all.substring(0, keyMinor) + "[" + all.charAt(keyMinor) + "]" + all.substring(keyMinor + 1);
        }
        return keySelect;
    }

    public String write(int aKey){
        System.out.println("Texter.write()");
        if (keyTimer != null) keyTimer.cancel();

        int index = aKey - Canvas.KEY_NUM0;
        if (index < 0 || index > keys.length){
            keyMajor = -1;
        }else {
            if (index != keyMajor) {
                keyMinor = 0;
                keyMajor = index;
            } else {
                keyMinor++;
                if (keyMinor >= keys[keyMajor].length())
                    keyMinor = 0;
            }

            keyTimer = new Timer();
            keyTimer.schedule(new KeyConfirmer(), 1000);
        }
        return inputText;

    }
}
