package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class PlayToursCanvas extends DefaultCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    int item;

    // image objects
    private Image menuBt, smallLogo;
    boolean showMenu;

    public PlayToursCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            w = getWidth();
            h = getHeight();
            smallLogo = Image.createImage("/play_icon_small.png");
            menuBt = Image.createImage("/menu_button.png");
            ScreenUtil.resetMenu();
        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    private void drawText(Graphics aGraphics, String aText){
        ScreenUtil.drawTextArea(aGraphics, 100, margin, logo.getHeight() + smallLogo.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
        ScreenUtil.drawText(aGraphics, aText, 2*margin, logo.getHeight() + smallLogo.getHeight() + 2*margin, fh, 100);
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

        String text;
        switch (item) {
            case 1:
                text = "description of game 1";
                drawText(g, text);
                break;
            case 2:
                text = "description of game 2";
                drawText(g, text);
                break;
            case 3:
                text = "description of game 3";
                drawText(g, text);
                break;
        }
        if(showMenu){
            String[] menuItems = {"game 1", "game 2", "game 3"};
            ScreenUtil.drawMenu(g, h, menuItems, menuTop, menuMiddle, menuBottom, menuSel);
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
