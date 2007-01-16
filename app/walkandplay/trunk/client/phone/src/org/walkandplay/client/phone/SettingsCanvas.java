package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import org.geotracing.client.Util;

public class SettingsCanvas extends DefaultCanvas {

    int item;
    int availableHeight = 100;
    int textHeight;

    // image objects
    private Image smallLogo;

    private int screenStat = 0;
    private final static int SOUND_STAT = 0;
    private final static int ACCOUNT_STAT = 1;



    public SettingsCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            smallLogo = Image.createImage("/settings_icon_small.png");
            //ScreenUtil.resetMenu();

        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    private int drawText(Graphics aGraphics, String aText){
        ScreenUtil.drawTextArea(aGraphics, 100, (w - 2*margin - middleTextArea.getWidth())/2, logo.getHeight() + smallLogo.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
        return ScreenUtil.drawText(aGraphics, aText, (w - middleTextArea.getWidth())/2, logo.getHeight() + smallLogo.getHeight() + 2*margin, fh, 100);
    }

    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        super.paint(g);
        g.drawImage(smallLogo, (w - 2*margin - middleTextArea.getWidth())/2, logo.getHeight() + margin, Graphics.TOP | Graphics.LEFT);
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

        if(menu.isVisible()){
            if(Util.hasSound()){
                String[] menuItems = {"sound off", "new account"};
                menu.setItems(menuItems);
                //ScreenUtil.drawMenu(g, h, menuItems, menuTop, menuMiddle, menuBottom, menuSel);
                menu.draw(g);
            }else{
                String[] menuItems = {"sound on", "new account"};
                menu.setItems(menuItems);
                //ScreenUtil.drawMenu(g, h, menuItems, menuTop, menuMiddle, menuBottom, menuSel);
                menu.draw(g);
            }
        }
        ScreenUtil.drawLeftSoftKey(g, h, menuBt, margin);
    }

    /**
     * Handles all key actions.
     *
     * @param key The Key that was hit.
     */
    public void keyPressed(int key) {
        super.keyPressed(key);
        
        int item = menu.getSelectedItem();
        switch(item){
            case 1:
                Util.toggleSound();
                screenStat = SOUND_STAT;
                break;
            case 2:
                screenStat = ACCOUNT_STAT;
                break;
        }

        // left soft key & fire
        if (key == -6 || key == -5 || getGameAction(key) == Canvas.FIRE) {
            if(menu.isVisible()){
                 item = menu.getSelectedItem();
                switch(item){
                    case 1:
                        Util.toggleSound();
                        screenStat = SOUND_STAT;
                        break;
                    case 2:
                        screenStat = ACCOUNT_STAT;
                        break;
                }
                menu.hide();
            }else{
                menu.show();
                menu.reset();
            }
            // right softkey
        } else if (key == -7) {
            midlet.setScreen(WP.HOME_CANVAS);            
            // left
        }
        repaint();
    }

}
