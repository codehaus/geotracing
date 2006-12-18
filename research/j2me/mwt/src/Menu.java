package org.walkandplay.client.phone;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Canvas;

import mwt.Component;
import mwt.Window;
import mwt.EventListener;

public class Menu extends Component {

	private Image topImg, middleImg, bottomImg, itemSelImg;
	private int nrOfItems, screenHeight;
	private String[] items;
	private int xMargin = 10;
	private int itemIndex;
	private int selectedItem;
	private int keyPressed; // stores temporary the last keycode that pressed this button

	private final EventListener action;
	private final int actionType;

	public Menu(Image aTopImg, Image aMiddleImg, Image aBottomImg, Image anItemSelImg, int theScreenHeight, EventListener action, int actionType){
		super(0, theScreenHeight, aMiddleImg.getWidth(), theScreenHeight, false);
		topImg = aTopImg;
		middleImg = aMiddleImg;
		bottomImg = aBottomImg;
		itemSelImg = anItemSelImg;
		screenHeight = theScreenHeight;

		this.action = action;
		this.actionType = actionType;

		reset();
	}

	public void setItems(String[] theMenuItems){
		items = theMenuItems;
		nrOfItems = items.length;
	}

	public void reset() {
		itemIndex = 0;
		selectedItem = -1;
	}

	public void draw(Graphics aGraphics) {
		aGraphics.drawImage(bottomImg, xMargin - 4, screenHeight - 30, Graphics.TOP | Graphics.LEFT);
		for (int i = 0; i < items.length; i++) {
			// start from the bottom and build up
			if (i == (itemIndex - 1)) {
				// highlight this menu item
				aGraphics.drawImage(itemSelImg, xMargin - 4, screenHeight - 24 - bottomImg.getHeight() - (i + 1) * middleImg.getHeight(), Graphics.TOP | Graphics.LEFT);
			} else {
				aGraphics.drawImage(middleImg, xMargin - 4, screenHeight - 24 - bottomImg.getHeight() - (i + 1) * middleImg.getHeight(), Graphics.TOP | Graphics.LEFT);
			}
			aGraphics.drawString(items[i], xMargin, screenHeight - 22 - bottomImg.getHeight() - (i + 1) * middleImg.getHeight(), Graphics.TOP | Graphics.LEFT);
		}

		aGraphics.drawImage(topImg, xMargin - 4, screenHeight - 34 - nrOfItems * middleImg.getHeight(), Graphics.TOP | Graphics.LEFT);
	}

	public void selectNextItem() {
			if (itemIndex == nrOfItems) {
				itemIndex = 1;
			} else {
				itemIndex++;
			}
		}

	public void selectPrevItem() {
		if (itemIndex == 1) {
			itemIndex = nrOfItems;
		} else {
			itemIndex--;
		}
	}

	public int getSelectedItem() {
		return selectedItem;
	}

	// overrides
	protected boolean keyEvent(long key, Window window) {
		if (window.getFocusAction((int) key) <= Window.FOCUSACTION_PREV) {
			keyPressed = (int) key;
			if ((key >> 32) == 0) action.processEvent(EventListener.EVENT_ACTION, this, null);
			return true;
		} else return false;
	}

	public void keyPressed(int theKey, int theGameActionKey){
		// left soft key & fire
		if (theKey == -6 || theKey == -5 || theGameActionKey == Canvas.FIRE) {
			if(!isVisible()){
				reset();
			}else{
				selectedItem = itemIndex;
			}
			// right softkey
		} else if (theKey == -1 || theGameActionKey == Canvas.UP) {
			selectNextItem();
			// down
		} else if (theKey == -2 || theGameActionKey == Canvas.DOWN) {
			selectPrevItem();
		}
	}

}
