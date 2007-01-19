package org.walkandplay.client.phone;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class FindToursCanvas extends DefaultCanvas {

    int item;
    int availableHeight = 100;
    int textHeight;

    // image objects
    private Image smallLogo, upBt, upDownBt, downBt;
    boolean showMenu;

    public FindToursCanvas(WP aMidlet) {
        super(aMidlet);
        try {
            smallLogo = Image.createImage("/find_icon_small.png");
            upBt = Image.createImage("/scrollup_button.png");
            upDownBt = Image.createImage("/scroll_buttons.png");
            downBt = Image.createImage("/scrolldown_button.png");
            ScreenUtil.resetMenu();
        } catch (Throwable t) {
            log("could not load all images : " + t.toString());
        }
    }

    private int drawText(Graphics aGraphics, String aText){
        ScreenUtil.drawTextArea(aGraphics, availableHeight, (w - 2*margin - middleTextArea.getWidth())/2, margin + logo.getHeight() + smallLogo.getHeight() + margin, topTextArea, middleTextArea, bottomTextArea);
        return ScreenUtil.drawText(aGraphics, aText, (w - middleTextArea.getWidth())/2, margin + logo.getHeight() + smallLogo.getHeight() + 2*margin, fh, 100);
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
                text = "description of game 1 Lorem ipsum qui detracto appetere at, ius at omnes nemore, at tota scripserit mel. Ignota officiis te sit. Malis indoctum conceptam an pro, ad fierent delectus recusabo mei, vim eu solet sententiae. Vim omittam eloquentiam id. Labitur fabellas ex ius, his ut impedit verterem urbanitas.";
                textHeight = drawText(g, text);
                break;
            case 2:
                text = "description of game 2 Lorem ipsum qui detracto appetere at, ius at omnes nemore, at tota scripserit mel. Ignota officiis te sit. Malis indoctum conceptam an pro, ad fierent delectus recusabo mei, vim eu solet sententiae. Vim omittam eloquentiam id. Labitur fabellas ex ius, his ut impedit verterem urbanitas.";
                textHeight = drawText(g, text);
                break;
            case 3:
                text = "description of game 3 Lorem ipsum qui detracto appetere at, ius at omnes nemore, at tota scripserit mel. Ignota officiis te sit. Malis indoctum conceptam an pro, ad fierent delectus recusabo mei, vim eu solet sententiae. Vim omittam eloquentiam id. Labitur fabellas ex ius, his ut impedit verterem urbanitas.";
                textHeight = drawText(g, text);
                break;
        }

        ScreenUtil.drawScrollButtons(g, textHeight, margin + logo.getHeight() + smallLogo.getHeight() + margin + margin + availableHeight, availableHeight, w, fh, downBt, upBt, upDownBt);

        if(showMenu){
            String[] menuItems = {"game 1", "game 2", "game 3"};
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
                ScreenUtil.resetScroll();
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
            if(showMenu){
                ScreenUtil.selectNextMenuItem();
            }else{
                ScreenUtil.scrollText(true, textHeight, availableHeight, fh);
            }
            // down
        } else if (key == -2 || getGameAction(key) == Canvas.DOWN) {
            if(showMenu){
                ScreenUtil.selectPrevMenuItem();
            }else{
                ScreenUtil.scrollText(false, textHeight, availableHeight, fh);
            }
        } else if (getGameAction(key) == Canvas.KEY_STAR || key == Canvas.KEY_STAR) {
        } else if (getGameAction(key) == Canvas.KEY_POUND || key == Canvas.KEY_POUND) {
        } else if (key == -8) {
        } else {
        }

        repaint();
    }

}
