package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.geotracing.client.Util;

public class SettingsCanvas extends DefaultCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    int item;
    int availableHeight = 100;
    int textHeight;

    // image objects
    private Image smallLogo;
    boolean showMenu;

    private int screenStat = 0;
    private final static int SOUND_STAT = 0;
    private final static int ACCOUNT_STAT = 1;

    public SettingsCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            w = getWidth();
            h = getHeight();
            smallLogo = Image.createImage("/settings_icon_small.png");
            ScreenUtil.resetMenu();
        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    private int drawText(Graphics aGraphics, String aText){
        ScreenUtil.drawTextArea(aGraphics, 100, margin, logo.getHeight() + smallLogo.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
        return ScreenUtil.drawText(aGraphics, aText, 2*margin, logo.getHeight() + smallLogo.getHeight() + 2*margin, fh, 100);
    }

    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        super.paint(g);
        g.setColor(0, 0, 0);
        f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        g.setFont(f);
        fh = f.getHeight();
        g.drawImage(smallLogo, margin, logo.getHeight() + margin, Graphics.TOP | Graphics.LEFT);
        String text = "";
        switch (screenStat) {
            case SOUND_STAT:
                if(Util.hasSound()){
                    text = "Sound is currently turned on.";
                }else{
                    text = "Sound is currently turned off.";
                }
                drawText(g, text);
                break;
            case ACCOUNT_STAT:
                text = "Create an account to start using the application.";
                drawText(g, text);
                break;
        }

        if(showMenu){
            if(Util.hasSound()){
                String[] menuItems = {"sound off", "new account"};
                ScreenUtil.drawMenu(g, h, menuItems, menuTop, menuMiddle, menuBottom, menuSel);
            }else{
                String[] menuItems = {"sound on", "new account"};
                ScreenUtil.drawMenu(g, h, menuItems, menuTop, menuMiddle, menuBottom, menuSel);
            }
        }
        ScreenUtil.drawLeftSoftKey(g, h, menuBt);
    }

    /**
     * Handles all key actions.
     *
     * @param key The Key that was hit.
     */
    public void keyPressed(int key) {
        // left soft key & fire
        if (key == -6 || key == -5 || getGameAction(key) == Canvas.FIRE) {
            if(showMenu){
                item = ScreenUtil.getSelectedMenuItem();
                switch(item){
                    case 1:
                        Util.toggleSound();
                        screenStat = SOUND_STAT;
                        break;
                    case 2:
                        screenStat = ACCOUNT_STAT;
                        break;
                }
                showMenu = false;
            }else{
                showMenu = true;
                ScreenUtil.resetMenu();
            }
            // right softkey
        } else if (key == -7) {
            midlet.setScreen(WP.HOME_CANVAS);            
            // left
        } else if (key == -3 || getGameAction(key) == Canvas.LEFT) {
            // right
        } else if (key == -4 || getGameAction(key) == Canvas.RIGHT) {
            // up
        } else if (key == -1 || getGameAction(key) == Canvas.UP) {
            ScreenUtil.selectNextMenuItem();
            // down
        } else if (key == -2 || getGameAction(key) == Canvas.DOWN) {
            ScreenUtil.selectPrevMenuItem();
        } else if (getGameAction(key) == Canvas.KEY_STAR || key == Canvas.KEY_STAR) {
        } else if (getGameAction(key) == Canvas.KEY_POUND || key == Canvas.KEY_POUND) {
        } else if (key == -8) {
        } else {
        }

        repaint();
    }

}
