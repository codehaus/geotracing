// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

DH.include('Widget.js');

/**
 * Window-like pane.
 *
 * @extends Widget
 * @constructor
 * @version $Id: Panel.js,v 1.4 2006-05-29 13:33:32 just Exp $
 */
function Panel(id, bgColor, fgColor, onActivate, onClose) {
	Widget.apply(this, new Array(id));

	// Setting the visibility to hidden
	this.activate = function() {
		this.setZ(Widget.curZ++);
		this.show();
		if (this.onActivate != null && DH.isVisible(this.getContainer()) == true) {
			this.onActivate();
		}
	}

	// Setting the visibility to hidden
	this.clear = function() {
		if (this.content) {
			// delete this.content;
		}
		this.content.innerHTML = ' ';
		//while(this.content.hasChildNodes() == true) {
		//  this.content.removeChild(this.content.childNodes[0])
		//}
	}

	// Setting the visibility to hidden
	this.close = function() {
		this.clear();
		this.hide();
		if (this.onClose != null) {
			this.onClose();
		}
	}


	// No footer (resizer) to be shown
	this.hideFooter = function() {
		DH.hide(this.footer);
		DH.hide(this.resizer);
	}

	// Set Panel color
	this.loadContent = function(url) {
		this.clear();
		this.setContent('LOADING...');

		var panel = this;

		this.onLoadContent = function(cont) {
			panel.setContent(cont);
		}

		DH.getURL(url, this.onLoadContent);
	}

	// Set Panel color
	this.setContent = function(cont) {
		this.clear();
		if (typeof cont == "string") {
			this.content.innerHTML = cont;
		} else {
			this.content.appendChild(cont);
		}
	}

	// Set Panel color
	this.setTitle = function(title) {
		this.header.innerHTML = title;
	}

	// Set Panel color
	this.setBGColor = function(color) {
		this.bgcolor = color;
		DH.setBGColor(this.header, color);
		DH.setBGColor(this.footer, color);
	}

	// Set Panel foreground (text) header/footer color
	this.setFGColor = function(color) {
		this.fgcolor = color;
		DH.getStyleObject(this.header).color = color;
		DH.getStyleObject(this.footer).color = color;
	}

	// Set w/h
	this.setDimension = function(w, h) {
		DH.getStyleObject(this.getContainer()).width = w + 'px';
		DH.getStyleObject(this.getContainer()).height = h + 'px';
		// Damn, IE refuses overflow: auto !!
		this._fixIEScroll();
	}

	/** Fix vertical scrollbar otherwise not apearing in IE. */
	this._fixIEScroll = function() {
		if (DH.isIE == true) {
			var ch = DH.getObjectHeight(this.container) - DH.getObjectHeight(this.header) - DH.getObjectHeight(this.footer);
			DH.getStyleObject(this.content).height = ch + 'px';
			var cw = DH.getObjectWidth(this.container);
			DH.getStyleObject(this.content).width = cw + 'px';
		}
	}

	this.container = document.createElement('div');
	var container = this.container;
	container.className = 'pn-container';
	container.id = container.className + id;

	var header = document.createElement('div');
	header.className = 'pn-header';
	header.id = header.className + id;

	var closer = document.createElement('div');
	closer.className = 'pn-closer';
	closer.id = closer.className + id;

	var content = document.createElement('div');
	content.className = 'pn-content';
	content.id = content.className + id;

	var footer = document.createElement('div');
	footer.className = 'pn-footer';
	footer.id = footer.className + id;

	var resizer = document.createElement('div');
	resizer.className = 'pn-resizer';
	resizer.id = resizer.className + id;


	container.appendChild(header);
	container.appendChild(content);
	container.appendChild(footer);
	container.appendChild(resizer);
	container.appendChild(closer);
	document.body.appendChild(container);

	this.header = header;

	this.closer = closer;
	this.resizer = resizer;
	this.content = content;
	this.footer = footer;
	this.onActivate = null;
	this.onClose = null;

	if (onActivate) {
		this.onActivate = onActivate;
	}

	if (onClose) {
		this.onClose = onClose;
	}

	this.setXY((Math.round((Math.random() * 600) + 1)), 100 + (Math.round((Math.random() * 300) + 1)))
	this.setContent('');
	this.setBGColor(bgColor);
	this.setFGColor(fgColor);
	this.setTitle(id);
	// Damn, IE refuses overflow: auto !!
	this._fixIEScroll();


	var panel = this;

	this.onCloserClick = function(e) {
		panel.close();
		DH.cancelEvent(e);
	}

	this.onDragStart = function(target, x, y) {
		panel.activate();
	}

	this.onDrag = function(target, x, y, dx, dy) {
		if (target.className == 'pn-header' || target.className == 'pn-footer') {
			panel.setXY(panel.container.offsetLeft + dx, panel.container.offsetTop + dy);
		} else if (target.className == 'pn-resizer') {
			panel.setDimension(x - panel.container.offsetLeft, y - panel.container.offsetTop);
		}
	}


	DH.addEvent(this.closer, 'click', this.onCloserClick, true);
	DH.dragEnable(this.header, this.onDragStart, this.onDrag, null);
	DH.dragEnable(this.resizer, this.onDragStart, this.onDrag, null);
	DH.dragEnable(this.closer, this.onDragStart, this.onDrag, null);
	DH.dragEnable(this.footer, this.onDragStart, this.onDrag, null);
}

