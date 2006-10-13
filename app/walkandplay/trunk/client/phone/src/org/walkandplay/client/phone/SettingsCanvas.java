package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class SettingsCanvas extends DefaultCanvas {

    // paint vars
    int w, h, fh;
    Font f;

    int x0, y0;
    int midx;

    int menuItem;

    // image objects
    private Image menuBt, helpLogo;

    // screenstates
    private int screenStat = 0;
    private final static int HOME_STAT = 0;
    private final static int MENU_STAT = 1;

    public SettingsCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            w = getWidth();
            h = getHeight();
            helpLogo = Image.createImage("/settings_button_off_small.png");
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

        g.drawImage(textArea, margin, margin + logo.getHeight() + margin, Graphics.TOP | Graphics.LEFT);
        g.drawImage(helpLogo, margin + margin, logo.getHeight() + 10, Graphics.TOP | Graphics.LEFT);

        ScreenUtil.setLeftBt(g, h, menuBt);
        String text = "";
        switch (screenStat) {
            case HOME_STAT:
                switch (ScreenUtil.getSelectedMenuItem()) {
                    case-1:
                        text = "select a setting from the menu";
                        break;
                    case 1:
                        text = "setting 1";
                        break;
                    case 2:
                        text = "setting 2";
                        break;
                    case 3:
                        text = "setting 3";
                        break;
                    default:
                        text = "select a setting from the menu";
                }
                menuItem = ScreenUtil.getSelectedMenuItem();
                ScreenUtil.drawText(g, text, 10, logo.getHeight() + helpLogo.getHeight() + 3 * margin, fh);
                break;
            case MENU_STAT:
                switch (menuItem) {
                    case-1:
                        text = "select a setting from the menu";
                        break;
                    case 1:
                        text = "setting 1";
                        break;
                    case 2:
                        text = "setting 2";
                        break;
                    case 3:
                        text = "setting 3";
                        break;
                    default:
                        text = "select a setting from the menu";
                }
                ScreenUtil.drawText(g, text, 10, logo.getHeight() + helpLogo.getHeight() + 3 * margin, fh);
                String[] menuItems = {"setting 1", "setting 2", "setting 3"};
                ScreenUtil.createMenu(g, f, h, fh, menuItems, menuTop, menuMiddle, menuBottom);                
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
                    ScreenUtil.resetMenu();
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
            switch (screenStat) {
                case MENU_STAT:
                    ScreenUtil.nextMenuItem();
                    break;
            }
            // down
        } else if (key == -2 || getGameAction(key) == Canvas.DOWN) {
            switch (screenStat) {
                case MENU_STAT:
                    ScreenUtil.prevMenuItem();
                    break;
            }
        } else if (getGameAction(key) == Canvas.KEY_STAR || key == Canvas.KEY_STAR) {
        } else if (getGameAction(key) == Canvas.KEY_POUND || key == Canvas.KEY_POUND) {
        } else if (key == -8) {
        } else {
        }

        repaint();
    }

}
