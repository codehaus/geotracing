package org.walkandplay.client.phone;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class ScreenUtil {

    private static int selectedMenuItem = 0;
    private static int selectedIcon = 1;
    private static int menuItemLength = 0;
    private static int iconLength = 0;
    private static int scrollY = 0;

    public static void drawIcons(Graphics aGraphics, int theWidth, int aXOffSet, int aYOffset, Image[] theIcons, Image anIconOverlay) {
        iconLength = theIcons.length;
        int availablePixels = theWidth - 2 * aXOffSet;
        int nrOfIcons = availablePixels / theIcons[0].getWidth();

        for (int i = 0; i < theIcons.length; i++) {
            int layer = i / nrOfIcons;
            if (i == (selectedIcon - 1)) {
                aGraphics.drawImage(anIconOverlay, aXOffSet + i * theIcons[i].getWidth() - layer * nrOfIcons * theIcons[i].getWidth(), aYOffset + layer * theIcons[i].getHeight(), Graphics.TOP | Graphics.LEFT);
                aGraphics.drawImage(theIcons[i], aXOffSet + i * theIcons[i].getWidth() - layer * nrOfIcons * theIcons[i].getWidth(), aYOffset + layer * theIcons[i].getHeight(), Graphics.TOP | Graphics.LEFT);
            } else {
                aGraphics.drawImage(theIcons[i], aXOffSet + i * theIcons[i].getWidth() - layer * nrOfIcons * theIcons[i].getWidth(), aYOffset + layer * theIcons[i].getHeight(), Graphics.TOP | Graphics.LEFT);
            }
        }
    }

    public static void drawMessageBar(Graphics aGraphics, int aFontHeight, String[] theMsgs, Image theBar, int theHeight) {
        for(int i=0;i<theMsgs.length;i++){
            if(theMsgs[i].length()>0){
                aGraphics.drawImage(theBar, 0, theHeight/2 - theBar.getHeight() + i*theBar.getHeight(), Graphics.TOP | Graphics.LEFT);
                aGraphics.drawString(theMsgs[i], 3, theHeight/2 - aFontHeight + i*theBar.getHeight() - 2, Graphics.TOP | Graphics.LEFT);
            }
        }
    }

    public static void drawTextArea(Graphics aGraphics, int aHeight, int aXOffSet, int aYOffSet, Image aTopImage, Image aMiddleImage, Image aBottomImage) {
        aGraphics.drawImage(aTopImage, aXOffSet, aYOffSet, Graphics.TOP | Graphics.LEFT);
        int nrOfBars = aHeight / aMiddleImage.getHeight();
        for (int i = 0; i < nrOfBars; i++) {
            aGraphics.drawImage(aMiddleImage, aXOffSet, aYOffSet + (i + 1) * aMiddleImage.getHeight(), Graphics.TOP | Graphics.LEFT);
        }
        aGraphics.drawImage(aBottomImage, aXOffSet, aYOffSet + nrOfBars * aMiddleImage.getHeight(), Graphics.TOP | Graphics.LEFT);
    }

    public static void drawMenu(Graphics aGraphics, int theScreenHeight, String[] theMenuItems, Image theTop, Image theMiddle, Image theBottom, Image theMiddleSel) {
        int xMargin = 10;
        menuItemLength = theMenuItems.length;

        aGraphics.drawImage(theBottom, xMargin - 4, theScreenHeight - 30, Graphics.TOP | Graphics.LEFT);

        for (int i = 0; i < theMenuItems.length; i++) {
            // start from the bottom and build up
            if (i == (selectedMenuItem - 1)) {
                // highlight this menu item
                aGraphics.drawImage(theMiddleSel, xMargin - 4, theScreenHeight - 24 - theBottom.getHeight() - (i + 1) * theMiddle.getHeight(), Graphics.TOP | Graphics.LEFT);
            } else {
                aGraphics.drawImage(theMiddle, xMargin - 4, theScreenHeight - 24 - theBottom.getHeight() - (i + 1) * theMiddle.getHeight(), Graphics.TOP | Graphics.LEFT);
            }
            aGraphics.drawString(theMenuItems[i], xMargin, theScreenHeight - 22 - theBottom.getHeight() - (i + 1) * theMiddle.getHeight(), Graphics.TOP | Graphics.LEFT);
        }

        aGraphics.drawImage(theTop, xMargin - 4, theScreenHeight - 34 - theMenuItems.length * theMiddle.getHeight(), Graphics.TOP | Graphics.LEFT);
    }

    public static void drawScrollButtons(Graphics aGraphics, int aTextLength, int anYOffSet, int theAvailableHeight, int theWidth, int aFontHeight, Image aScrollDownBt, Image aScrollUpBt, Image aScrollUpAndDownBt){
        if(theAvailableHeight > aTextLength) return;
        if (scrollY == 0) {
            aGraphics.drawImage(aScrollDownBt, theWidth / 2 - aScrollDownBt.getWidth() / 2, anYOffSet, Graphics.TOP | Graphics.LEFT);
        } else if ((aTextLength) >= 3*theAvailableHeight/2) {
            aGraphics.drawImage(aScrollUpAndDownBt, theWidth / 2 - aScrollUpAndDownBt.getWidth() / 2, anYOffSet, Graphics.TOP | Graphics.LEFT);
        } else {
            aGraphics.drawImage(aScrollUpBt, theWidth / 2 - aScrollUpBt.getWidth() / 2, anYOffSet, Graphics.TOP | Graphics.LEFT);
        }
    }

    public static void scrollText(boolean up, int aTextLength, int theAvailableHeight, int aFontHeight) {
        if (up) {
            if (scrollY > 0){
                scrollY--;
            }
        } else {
            if ((aTextLength) >= 3*theAvailableHeight/2){
                scrollY++;
            }
        }
    }

    public static void resetScroll() {
        scrollY = 0;
    }

    public static void resetMenu() {
        selectedMenuItem = 0;
    }

    public static void resetIcons() {
        selectedIcon = 1;
    }

    public static void selectNextMenuItem() {
        if (selectedMenuItem == menuItemLength) {
            selectedMenuItem = 1;
        } else {
            selectedMenuItem++;
        }
    }

    public static void selectPrevMenuItem() {
        if (selectedMenuItem == 1) {
            selectedMenuItem = menuItemLength;
        } else {
            selectedMenuItem--;
        }
    }

    public static int getSelectedMenuItem() {
        return selectedMenuItem;
    }

    public static void selectNextIcon() {
        if (selectedIcon == iconLength) {
            selectedIcon = 1;
        } else {
            selectedIcon++;
        }
    }

    public static void selectPrevIcon() {
        if (selectedIcon == 1) {
            selectedIcon = iconLength;
        } else {
            selectedIcon--;
        }
    }

    public static void selectUpperIcon() {
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

    public static void selectLowerIcon() {
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

    public static void drawLeftSoftKey(Graphics aGraphics, int theScreenHeight, Image anImage, int aMargin) {
        aGraphics.drawImage(anImage, aMargin, theScreenHeight - anImage.getHeight() - aMargin, Graphics.TOP | Graphics.LEFT);
    }

    public static void drawRightSoftKey(Graphics aGraphics, int theScreenHeight, int theScreenWidth, Image anImage, int aMargin) {
        aGraphics.drawImage(anImage, theScreenWidth - anImage.getWidth() - aMargin, theScreenHeight - anImage.getHeight() - aMargin, Graphics.TOP | Graphics.LEFT);
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
    public static int drawText(Graphics g, String aText, int aXOffSet, int aYOffset, int aFontHeight, int theAvailableHeight) {
        /*System.out.println("available height:" + theAvailableHeight);
        System.out.println("scrollY:" + scrollY);*/
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

        // just drawing 1 line
        if (aText.length() <= width) {
            g.drawString(aText, x, y, Graphics.TOP | Graphics.LEFT);
        } else {
            boolean finished = false;
            int startPoint = 0;
            int lineCounter = 0;
            while (!finished) {
                if (aText.length() - startPoint < lineWidth) {
                    lineToPrint = aText.substring(startPoint, aText.length());
                    lineCounter++;
                    g.drawString(lineToPrint, x, y, Graphics.TOP | Graphics.LEFT);
                    finished = true;
                } else {
                    line = aText.substring(startPoint, printedChars + lineWidth);
                    int lastSpace = lastIndexOfASpace(line);
                    if (lastSpace == -1) {
                        finished = true;
                        lineToPrint = line;
                    } else {
                        lineToPrint = line.substring(0, lastSpace);
                    }
                    lineCounter++;
                    /*System.out.println("linecounter:" + lineCounter);
                    System.out.println("y:" + y);*/
                    // don't draw the line if user's has scrolled
                    if(scrollY < lineCounter){
                        printedChars += lineToPrint.length() + 1;
                        /*System.out.println("drawing:" + lineToPrint);*/
                        g.drawString(lineToPrint, x, y, Graphics.TOP | Graphics.LEFT);

                        startPoint = printedChars;
                        y += aFontHeight;
                    }else{
                        printedChars += lineToPrint.length() + 1;
                        /*System.out.println("not drawing:" + lineToPrint);*/
                        startPoint = printedChars;
                    }

                    // check if we have surpassed the available heigtht
                    if(y/1.5 > theAvailableHeight){
                        /*System.out.println("at the end...stop drawing");*/
                        finished = true;
                    }
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
