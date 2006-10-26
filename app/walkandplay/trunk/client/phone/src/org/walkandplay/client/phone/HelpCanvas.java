package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class HelpCanvas extends DefaultCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    // image objects
    private Image menuBt, helpLogo;

    int menuItem;

    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int MENU_STAT = 1;

    public HelpCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            w = getWidth();
            h = getHeight();
            helpLogo = Image.createImage("/help_button_off_small.png");
            menuBt = Image.createImage("/menu_button.png");
            ScreenUtil.resetMenu();
        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    public void paint(Graphics g) {
        super.paint(g);
        if (f == null) {
            g.setColor(0, 0, 0);
            f = Font.getFont(fontType, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            g.setFont(f);
            fh = f.getHeight();
        }

        ScreenUtil.drawTextArea(g, 100, margin, margin + logo.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
        g.drawImage(helpLogo, margin + margin, logo.getHeight() + 10, Graphics.TOP | Graphics.LEFT);

        ScreenUtil.drawLeftSoftKey(g, h, menuBt);
        String text = "";
        switch (screenStat) {
            case HOME_STAT:
                switch (ScreenUtil.getSelectedMenuItem()) {
                    case-1:
                        text = "select a topic from the menu";
                        break;
                    case 1:
                        text = "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1";
                        break;
                    case 2:
                        text = "2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2";
                        break;
                    case 3:
                        text = "3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3";
                        break;
                    default:
                        text = "select a topic from the menu";
                }
                menuItem = ScreenUtil.getSelectedMenuItem();
                ScreenUtil.drawText(g, text, 10, logo.getHeight() + helpLogo.getHeight() + 3 * margin, fh);
                break;
            case MENU_STAT:
                switch (menuItem) {
                    case-1:
                        text = "select a topic from the menu";
                        break;
                    case 1:
                        text = "1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1 1";
                        break;
                    case 2:
                        text = "2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2 2";
                        break;
                    case 3:
                        text = "3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3 3";
                        break;
                    default:
                        text = "select a topic from the menu";
                }
                ScreenUtil.drawText(g, text, 10, logo.getHeight() + helpLogo.getHeight() + 3 * margin, fh);
                String[] menuItems = {"menu item 1", "menu item 2", "menu item 3"};
                ScreenUtil.drawMenu(g, h, menuItems, menuTop, menuMiddle, menuBottom, menuSel);
                break;
        }
    }

    /**
     * Handles all key actions.
     *
     * @param key The Key that was hit.
     */
    public void keyPressed(int key) {
        // left soft key & fire
        if (key == -6 || key == -5 || getGameAction(key) == Canvas.FIRE) {
            switch (screenStat) {
                case HOME_STAT:
                    screenStat = MENU_STAT;
                    break;
                case MENU_STAT:
                    screenStat = HOME_STAT;
                    break;
            }
            // right softkey
        } else if (key == -7) {
            switch (screenStat) {
                case HOME_STAT:
                    midlet.setScreen(WP.HOME_CANVAS);
                    break;
                case MENU_STAT:
                    midlet.setScreen(WP.HOME_CANVAS);
                    break;
            }
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
