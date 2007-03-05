// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Widgets: some DHTML widgets.
 *
 * $Id$
 */

// See http://www.jroller.com/page/deep?entry=aop_fun_with_javascript


// WG = new Object();

/** Current z-index. */
Widget.curZ = 10000;

/** Current Selector option id.. */
Widget.curOptionId = 1;


function BBox(x, y, w, h) {
	this.x = x;
	this.y = y;
	this.w = w;
	this.h = h;

	this.toString = function() {
		return 'x=' + this.x + ' y=' + this.y + ' w=' + this.w + ' h=' + this.h;
	}
}

/**
 * Base class for widgets.
 * @constructor
 */
function Widget(id) {
	this.container = null;
	this.id = id;

	/** Get container object */
	this.getContainer = function() {
		if (this.container == null) {
			this.container = DH.getObject(this.id);
		}
		return this.container;
	}

	/** Get bounding box. */
	this.getBBox = function() {
		var cont = this.getContainer();
		return new BBox(DH.getObjectX(cont), DH.getObjectY(cont), DH.getObjectWidth(cont), DH.getObjectHeight(cont));
	}


	// Setting the visibility to hidden
	this.hide = function() {
		DH.hide(this.getContainer());
	}

	this.getId = function() {
		return this.id;
	}

	/** Is widget visible. */
	this.isVisible = function() {
		DH.isVisible(this.getContainer());
	}

	// Set w/h
	this.setOpacity = function(o) {
		DH.setOpacity(this.getContainer(), o);
	}

	// Set Panel color
	this.setXY = function(x, y) {
		DH.shiftTo(this.getContainer(), x, y);
	}

	// Set X,Y position
	this.setWidth = function(w) {
		DH.getStyleObject(this.getContainer()).width = w;
	}

	// Set Panel color
	this.setZ = function(z) {
		DH.setZIndex(this.getContainer(), z);
	}

	// Setting the visibility to hidden
	this.show = function() {
		DH.show(this.getContainer());
	}

}