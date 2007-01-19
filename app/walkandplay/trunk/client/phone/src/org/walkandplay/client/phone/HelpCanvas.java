package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class HelpCanvas extends DefaultCanvas {

    int availableHeight = 100;
    int textHeight;

    // image objects
    private Image smallLogo, upBt, upDownBt, downBt;
    boolean showMenu;

    int item;

    public HelpCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            smallLogo = Image.createImage("/help_icon_small.png");
            upBt = Image.createImage("/scrollup_button.png");
            upDownBt = Image.createImage("/scroll_buttons.png");
            downBt = Image.createImage("/scrolldown_button.png");
            ScreenUtil.resetMenu();
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
        String text;
        switch (item) {
            case 1:
                text = "description of topic 1 Lorem ipsum qui detracto appetere at, ius at omnes nemore, at tota scripserit mel. Ignota officiis te sit. Malis indoctum conceptam an pro, ad fierent delectus recusabo mei, vim eu solet sententiae. Vim omittam eloquentiam id. Labitur fabellas ex ius, his ut impedit verterem urbanitas.";
                textHeight = drawText(g, text);
                break;
            case 2:
                text = "description of topic 2 Lorem ipsum qui detracto appetere at, ius at omnes nemore, at tota scripserit mel. Ignota officiis te sit. Malis indoctum conceptam an pro, ad fierent delectus recusabo mei, vim eu solet sententiae. Vim omittam eloquentiam id. Labitur fabellas ex ius, his ut impedit verterem urbanitas.";
                textHeight = drawText(g, text);
                break;
            case 3:
                text = "description of topic 3 Lorem ipsum qui detracto appetere at, ius at omnes nemore, at tota scripserit mel. Ignota officiis te sit. Malis indoctum conceptam an pro, ad fierent delectus recusabo mei, vim eu solet sententiae. Vim omittam eloquentiam id. Labitur fabellas ex ius, his ut impedit verterem urbanitas.";
                textHeight = drawText(g, text);
                break;
        }

        ScreenUtil.drawScrollButtons(g, textHeight, margin + logo.getHeight() + smallLogo.getHeight() + margin + margin + availableHeight, availableHeight, w, fh, downBt, upBt, upDownBt);

        if(showMenu){
            String[] menuItems = {"topic 1", "topic 2", "topic 3"};
            ScreenUtil.drawMenu(g, h, menuItems, menuTop, menuMiddle, menuBottom, menuSel);
        }
        ScreenUtil.drawLeftSoftKey(g, h, menuBt, margin);
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
            midlet.setScreen(midlet.HOME_CANVAS);
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
