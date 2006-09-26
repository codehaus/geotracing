package org.walkandplay.client.phone;

import javax.microedition.lcdui.*;
import java.util.Vector;

/**
 * A screen to display text string on screen.
 * <p>Description: This is a canvas screen to display the current messages in
 * virtual chat room. Only the latest messages are displayed. If there are  more
 * messages than those can fit into one screen, old messages are roll off from
 * the upper edge. User is not able to scroll back to see old messages, however,
 * the old messages is still available in msgs Vector until a clear command
 * is invoked. When a clear command is invoked, all message will be removed
 * from msgs vector. </p>
 * <p>Copyright: Copyright (c) 2003</p>
 *
 * @author Ben Hui (www.benhui.net)
 * @version 1.0
 */
public class LogScreen extends Canvas implements CommandListener {

    // list of available message to display
    public Vector msgs = new Vector();
    // current message idx
    int midx;
    // graphic width and height
    int w, h;
    // font height
    int fh;
    // font to write message on screen
    Font f;

    int x0, y0;

    public int bookmarkId = 1;

    public int backTo;

    public static final Command BACK = new Command("Back", Command.BACK, 1);

    public LogScreen() {
        addCommand(LogScreen.BACK);
        setCommandListener(this);
    }

    public void commandAction(Command c, Displayable d) {

    }

    /**
     * Draws the screen.
     *
     * @param g The graphics object.
     */
    protected void paint(Graphics g) {

        if (f == null) {
            // cache the font and width,height value
            // when it is used the first time
            f = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
            w = getWidth();
            h = getHeight();
            fh = f.getHeight();
        }

        int y = fh; // 1st line y value

        // message will be rendered in black color, on top of white backgound
        g.setColor(255, 255, 255);
        g.fillRect(0, 0, w, h);
        g.setColor(0, 0, 0);
        g.setFont(f);

        g.translate(-x0, -y0);

        // render the messages on screen
        for (int i = midx; i < msgs.size(); i++) {
            String s = (String) msgs.elementAt(i);
            g.drawString(s, 0, y, Graphics.BASELINE | Graphics.LEFT);
            y += fh;
        }

    }

    /**
     * Handles all key actions.
     *
     * @param key The Key that was hit.
     */
    public void keyPressed(int key) {
        if (getGameAction(key) == Canvas.RIGHT) {
            x0 += 50;
        } else if (getGameAction(key) == Canvas.LEFT) {
            x0 -= 50;
        } else if (getGameAction(key) == Canvas.UP) {
            // note: change this from 50 to 100 if you want to scroll faster
            y0 -= 50;
        } else if (getGameAction(key) == Canvas.DOWN) {
            // note: change this from 50 to 100 if you want to scroll faster
            y0 += 50;
        }
        repaint();
    }

    public void clear() {
        msgs.removeAllElements();
        midx = 0;
        x0 = 0;
        y0 = 0;
        bookmarkId = 1;
        repaint();
    }

    public void add(String s) {
        if (msgs.size() > 500) clear();
        msgs.addElement(s);
        repaint();
    }

    public void addNoRepaint(String s) {
        if (msgs.size() > 50) clear();
        msgs.addElement(s);
    }
}
