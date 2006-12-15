package org.walkandplay.client.phone;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Canvas;

abstract public class CanvasElement {

    protected Canvas canvas;
    private boolean visible;
    public CanvasElement(Canvas aCanvas){
        canvas = aCanvas;
    }

    abstract void draw(Graphics aGraphics);

    abstract void keyPressed(int aKey, int theGameActionKey);

    protected void show(){
        visible = true;
    }

    protected void hide(){
        visible = false;
    }

    protected boolean isVisible(){
        return visible;
    }


}
