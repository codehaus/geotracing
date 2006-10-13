package org.walkandplay.client.phone;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ScreenUtil {

    private static int selectedMenuItem = 0;
    private static int selectedIcon = 0;
    private static int menuItemLength = 0;
    private static int iconLength = 0;

    public static void createIcons(Graphics aGraphics, int aXOffSet, int aYOffset, Image[] theOffIcons, Image[] theOnIcons) {
        // TODO: support next row properly
        iconLength = theOnIcons.length;
        for (int i = 0; i < 3; i++) {
            if (i == (selectedIcon - 1)) {
                aGraphics.drawImage(theOnIcons[i], aXOffSet + i * theOnIcons[i].getWidth(), aYOffset, Graphics.TOP | Graphics.LEFT);
            } else {
                aGraphics.drawImage(theOffIcons[i], aXOffSet + i * theOffIcons[i].getWidth(), aYOffset, Graphics.TOP | Graphics.LEFT);
            }
        }
        for (int i = 3; i < 6; i++) {
            if (i == (selectedIcon - 1)) {
                aGraphics.drawImage(theOnIcons[i], aXOffSet + (i - 3) * theOnIcons[i].getWidth(), aYOffset + theOnIcons[i].getHeight() + 5, Graphics.TOP | Graphics.LEFT);
            } else {
                aGraphics.drawImage(theOffIcons[i], aXOffSet + (i - 3) * theOffIcons[i].getWidth(), aYOffset + theOnIcons[i].getHeight() + 5, Graphics.TOP | Graphics.LEFT);
            }
        }
    }

    public static void placeMsgBar(Graphics aGraphics, int aFontHeight, String aMsg, Image theBar, int theHeight) {
        aGraphics.drawImage(theBar, 0, theHeight - theBar.getHeight(), Graphics.TOP | Graphics.LEFT);
        aGraphics.setColor(0, 0, 0);
        Font f = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        aGraphics.setFont(f);
        aGraphics.drawString(aMsg, 3, theHeight - aFontHeight - 2, Graphics.TOP | Graphics.LEFT);
    }

    public static void createMenu(Graphics aGraphics, Font aFont, int theScreenHeight, int theFontHeight, String[] theMenuItems, Image theTop, Image theMiddle, Image theBottom) {
        int xMargin = 10;

        // start fresh each time we draw a menu
        //selectedMenuItem = 0;
        menuItemLength = theMenuItems.length;
        // first store the settings we have coming into this method
        //Font f = aFont;
        //int color = aGraphics.getColor();

        aGraphics.drawImage(theBottom, xMargin - 4, theScreenHeight - 26, Graphics.TOP | Graphics.LEFT);

        // now set the needed color & font
        aGraphics.setColor(0, 0, 0);
        aFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        aGraphics.setFont(aFont);
        for (int i = 0; i < theMenuItems.length; i++) {
            // first place the menu image bg
            aGraphics.drawImage(theMiddle, xMargin - 4, theScreenHeight - 20 - theBottom.getHeight() - (i + 1) * theMiddle.getHeight(), Graphics.TOP | Graphics.LEFT);
            // start from the bottom and build up
            if (i == (selectedMenuItem - 1)) {
                // highlight this menu item
                aFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL);
                aGraphics.setFont(aFont);
                aGraphics.drawString(theMenuItems[i], xMargin, theScreenHeight - 22 - theBottom.getHeight() - (i + 1) * theMiddle.getHeight() + 3, Graphics.TOP | Graphics.LEFT);
                aFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
                aGraphics.setFont(aFont);
            } else {
                aGraphics.drawString(theMenuItems[i], xMargin, theScreenHeight - 22 - theBottom.getHeight() - (i + 1) * theMiddle.getHeight() + 3, Graphics.TOP | Graphics.LEFT);
            }
        }

        aGraphics.drawImage(theTop, xMargin - 4, theScreenHeight - 30 - theMenuItems.length * theMiddle.getHeight(), Graphics.TOP | Graphics.LEFT);
        // now return the original settings
        aGraphics.setColor(0, 0, 0);
        aFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
        aGraphics.setFont(aFont);
    }

    public static void resetMenu() {
        selectedMenuItem = 0;
    }

    public static void resetIcons() {
        selectedIcon = 1;
    }

    public static void nextMenuItem() {
        if (selectedMenuItem == menuItemLength) {
            selectedMenuItem = 1;
        } else {
            selectedMenuItem++;
        }
    }

    public static void prevMenuItem() {
        if (selectedMenuItem == 1) {
            selectedMenuItem = menuItemLength;
        } else {
            selectedMenuItem--;
        }
    }

    public static int getSelectedMenuItem() {
        return selectedMenuItem;
    }

    public static void nextIcon() {
        if (selectedIcon == iconLength) {
            selectedIcon = 1;
        } else {
            selectedIcon++;
        }
    }

    public static void prevIcon() {
        if (selectedIcon == 1) {
            selectedIcon = iconLength;
        } else {
            selectedIcon--;
        }
    }

    public static void upIcon() {
        switch (selectedIcon) {
            case 4:
                selectedIcon = 1;
                break;
            case 5:
                selectedIcon = 2;
                break;
            case 6:
                selectedIcon = 3;
                break;
        }
    }

    public static void downIcon() {
        switch (selectedIcon) {
            case 1:
                selectedIcon = 4;
                break;
            case 2:
                selectedIcon = 5;
                break;
            case 3:
                selectedIcon = 6;
                break;
        }
    }

    public static int getSelectedIcon() {
        return selectedIcon;
    }

    public static void setLeftBt(Graphics aGraphics, int theScreenHeight, Image anImage) {
        aGraphics.drawImage(anImage, 0, theScreenHeight - anImage.getHeight(), Graphics.TOP | Graphics.LEFT);
    }

    public static void setRightBt(Graphics aGraphics, int theScreenHeight, int theScreenWidth, Image anImage) {
        aGraphics.drawImage(anImage, theScreenWidth - anImage.getWidth(), theScreenHeight - anImage.getHeight(), Graphics.TOP | Graphics.LEFT);
    }

    /**
     * Puts the text on the screen.
     *
     * @param g           The graphics object
     * @param aText       The text to be displayed
     * @param aXOffSet    The x screen offset
     * @param aYOffset    The y screen offset
     * @param aFontHeight The font height
     * @return y The y coordinaat of the bottom of the text.
     */
    public static int drawText(Graphics g, String aText, int aXOffSet, int aYOffset, int aFontHeight) {
        int width = 27;
        int x = aXOffSet;
        int y = aYOffset;
        String line = "";
        String lineToPrint = "";
        int printedChars = 0;
        int lineWidth = 27;

        // first remove all line endings
        while (aText.indexOf("\n") != -1) {
            int startIndex = aText.indexOf("\n");
            int length = aText.length();
            aText = aText.substring(0, startIndex) + aText.substring(startIndex + 1, length);
        }

        while (aText.indexOf("\r") != -1) {
            int startIndex = aText.indexOf("\r");
            int length = aText.length();
            aText = aText.substring(0, startIndex) + aText.substring(startIndex + 1, length);
        }

        if (aText.length() <= width) {
            g.drawString(aText, x, y, Graphics.TOP | Graphics.LEFT);
        } else {
            boolean notAtTheEnd = true;
            int startPoint = 0;
            while (notAtTheEnd) {
                if (aText.length() - startPoint < lineWidth) {
                    lineToPrint = aText.substring(startPoint, aText.length());
                    g.drawString(lineToPrint, x, y, Graphics.TOP | Graphics.LEFT);
                    notAtTheEnd = false;
                } else {
                    line = aText.substring(startPoint, printedChars + lineWidth);
                    int lastSpace = lastIndexOfASpace(line);
                    if (lastSpace == -1) {
                        notAtTheEnd = false;
                        lineToPrint = line;
                    } else {
                        lineToPrint = line.substring(0, lastSpace);
                    }

                    printedChars += lineToPrint.length() + 1;
                    g.drawString(lineToPrint, x, y, Graphics.TOP | Graphics.LEFT);

                    startPoint = printedChars;
                    y += aFontHeight;
                }
            }
        }
        return y;
    }

    /**
     * Determines the location of the last space in a text.
     *
     * @param aText The text in question
     * @return The position of the last space.
     */
    public static int lastIndexOfASpace(String aText) {
        int spaceLoc = 0;
        int startPoint = 0;
        while (spaceLoc != -1) {
            spaceLoc = aText.indexOf(" ", startPoint);
            if (spaceLoc != -1) startPoint = spaceLoc + 1;
        }
        return startPoint - 1;
    }

    public static String removeLinks(String aText) {
        while (aText.indexOf("<a") != -1) {
            int startIndex = aText.indexOf("<a");
            int endIndex = aText.indexOf(">", startIndex);
            if (startIndex > endIndex) break;
            int length = aText.length();
            aText = aText.substring(0, startIndex) + aText.substring(endIndex + 1, length);
        }

        while (aText.indexOf("<u") != -1) {
            int startIndex = aText.indexOf("<u");
            int endIndex = aText.indexOf(">", startIndex);
            if (startIndex > endIndex) break;
            int length = aText.length();
            aText = aText.substring(0, startIndex) + aText.substring(endIndex + 1, length);
        }

        while (aText.indexOf("</a") != -1) {
            int startIndex = aText.indexOf("</a");
            int endIndex = aText.indexOf(">", startIndex);
            if (startIndex > endIndex) break;
            int length = aText.length();
            aText = aText.substring(0, startIndex) + aText.substring(endIndex + 1, length);
        }

        while (aText.indexOf("</u") != -1) {
            int startIndex = aText.indexOf("</u");
            int endIndex = aText.indexOf(">", startIndex);
            if (startIndex > endIndex) break;
            int length = aText.length();
            aText = aText.substring(0, startIndex) + aText.substring(endIndex + 1, length);
        }

        return aText;
    }

    public static String unEscape(String aText) {
        int startIndex = -1;
        while ((startIndex = aText.indexOf("&lt;")) != -1) {
            int length = aText.length();
            aText = aText.substring(0, startIndex) + "<" + aText.substring(startIndex + 4, length);
        }

        while ((startIndex = aText.indexOf("&gt;")) != -1) {
            int length = aText.length();
            aText = aText.substring(0, startIndex) + ">" + aText.substring(startIndex + 4, length);
        }

        while ((startIndex = aText.indexOf("&quot;")) != -1) {
            int length = aText.length();
            aText = aText.substring(0, startIndex) + "\"" + aText.substring(startIndex + 6, length);
        }

        while ((startIndex = aText.indexOf("&#039;")) != -1) {
            int length = aText.length();
            aText = aText.substring(0, startIndex) + "\'" + aText.substring(startIndex + 6, length);
        }

        while ((startIndex = aText.indexOf("&#092;")) != -1) {
            int length = aText.length();
            aText = aText.substring(0, startIndex) + "\\" + aText.substring(startIndex + 6, length);
        }

        while ((startIndex = aText.indexOf("&amp;")) != -1) {
            int length = aText.length();
            aText = aText.substring(0, startIndex) + "&" + aText.substring(startIndex + 5, length);
        }

        return aText;
    }
}
