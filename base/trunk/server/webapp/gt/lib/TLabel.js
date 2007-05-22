// TLabel() GMaps API extension copyright 2005-2006 Tom Mangan (tmangan@gmail.com)
// http://gmaps.tommangan.us/tlabel.html
// free for non-commercial use
//
// NOTE (Just): this version based on TLabel v2.0.1 with changes for
// 1. optional "diffusions": to randomly place colliding icons around 9 positions (diffpos)
// 2. split setPosition() into moveToLatLng() and moveToXY() to allow moving icons based on GLatLng
// 3. removed setting this.anchorLatLng in moveToXY (zoomend callback gives new zoomlevel as arg!)
// 4. replaced calculation for x,y pixels with GMap2.fromLatLngToDivPixel() (ok also in 2.0.3)
// 5. set z-index high (why?)
// 6. use this.map.getPane(G_MAP_MAP_PANE) to fetch mapTray  (ok also in 2.0.3)
// 7. keep element as object var (this.elm)
// 8. no need (afaict) to append elm to document.body (solved performance problems by leaving out)
// 9. added animated gliding to location (set TLabel.glide = true to enable)
//
// $Id$
diffpos = function(x, y) {
	this.x = x;
	this.y = y;
}

// random positions
var diffusions = new Array(new diffpos(-5, -5), new diffpos(0, -5), new diffpos(5, -5), new diffpos(-5, 0), new diffpos(0, 0), new diffpos(5, 0), new diffpos(-5, 5), new diffpos(0, 5), new diffpos(5, 5), new diffpos(10, 5));


var TLABEL = {
	SMOOTH_FACTOR: .050,
	ANIMATE_INTERVAL: 150,
	DEBUG: false,
	G_MAP_PANE: G_MAP_MARKER_PANE
}

function TLabel(diffuse) {
	this.diffuse = false;
	this.elm = null;
	this.glide = false;
	if (diffuse) {
		// to avoid collisions
		this.diffuse = diffuse;
	}
}

TLabel.prototype.debug = function(s) {
	if (TLABEL.DEBUG == true) {
		GLog.write(s);
	}
}

TLabel.prototype.initialize = function(a) {
	if (typeof(a.TLabelBugged == 'undefined')) {
		this.addTBug(a);
	}
	this.map = a;
	var b = document.createElement('span');
	b.setAttribute('id', this.id);
	b.innerHTML = this.content;
	// NOT CLEAR WHY ELM SHOULD BE APPENDED TO BODY
	// Only needed for Safari, why ??
	if (navigator.userAgent.toLowerCase().indexOf('safari') != -1) {
		document.body.appendChild(b);
	}
	b.style.position = 'absolute';

	// JUST: changed from 1 to 25000
	b.style.zIndex = 25000;
	if (this.percentOpacity) {
		this.setOpacity(this.percentOpacity);
	}

	this.elm = b;
	// document.getElementById(this.id);
	this.w = this.elm.offsetWidth;
	this.h = this.elm.offsetHeight;
	this.mapTray = this.map.getPane(TLABEL.G_MAP_PANE);
	// document.getElementById(a.getContainer().id).firstChild;
	this.mapTray.appendChild(b);
	if (!this.markerOffset) {
		this.markerOffset = new GSize(0, 0);
	}
	this.moveToXY();

	// GEvent.bind(a, "zoomend", this, this.forceToXY);
	GEvent.bind(a, "moveend", this, this.forceToXY);
}

TLabel.prototype.forceToLatLng = function(aGLatLng) {
	this.anchorLatLng = aGLatLng;
	this.forceToXY();
}

TLabel.prototype.moveToLatLng = function(aGLatLng) {
	this.anchorLatLng = aGLatLng;
	this.moveToXY();
}

TLabel.prototype.forceToXY = function() {
	this.getXY();
	if (this.glide == true) {
		this.x = this.targetX;
		this.y = this.targetY;
	}
	this.moveElm();
}

TLabel.prototype.moveToXY = function() {
	this.getXY();
	if (this.glide == true) {
		this.animate();
	} else {
		this.moveElm();
	}
}

TLabel.prototype.moveElm = function() {
	this.elm.style.left = this.x + 'px';
	this.elm.style.top = this.y + 'px';
}

TLabel.prototype.getXY = function(a, b) {
	var xy = this.map.fromLatLngToDivPixel(this.anchorLatLng)
	var x = parseInt(xy.x);
	var y = parseInt(xy.y);
	with (Math) {
		switch (this.anchorPoint) {
			case 'topLeft':break;
			case 'topCenter':x -= floor(this.w / 2);break;
			case 'topRight':x -= this.w;break;
			case 'midRight':x -= this.w;y -= floor(this.h / 2);break;
			case 'bottomRight':x -= this.w;y -= this.h;break;
			case 'bottomCenter':x -= floor(this.w / 2);y -= this.h;break;
			case 'bottomLeft':y -= this.h;break;
			case 'midLeft':y -= floor(this.h / 2);break;
			case 'center':x -= floor(this.w / 2);y -= floor(this.h / 2);break;
			default:break;
		}
	}

	if (this.diffuse == true) {
		x = x - this.markerOffset.width + diffusions[Math.floor(Math.random() * 10)].x;
		y = y - this.markerOffset.height + diffusions[Math.floor(Math.random() * 10)].y;
	} else {
		x = x - this.markerOffset.width;
		y = y - this.markerOffset.height;
	}

	if (this.glide == true) {
		this.targetX = x;
		this.targetY = y;
		if (!this.x) {
			this.x = x;
			this.y = y;
		}

	} else {
		this.x = x;
		this.y = y;
	}

	xy.x = x;
	xy.y = y;

	return xy;
}

TLabel.prototype.animate = function(show) {
	var obj = this;
	this.debug('animate enter');
	if (!this.animating || this.animating == null) {
		this.debug('start animation');
		this.animating = window.setInterval(function() { obj.glideElm() }, TLABEL.ANIMATE_INTERVAL);
	}
}

TLabel.prototype.glideElm = function() {
	//this.debug('glideElm enter x=' + this.x + ' tx=' + this.targetX + ' y=' + this.y + ' ty=' + this.targetY);

	var diffX = Math.abs(this.targetX - this.x);
	var diffY = Math.abs(this.targetY - this.y);

	if (diffX <= 1 && diffY <= 1) {
		if (this.animating) {
			this.debug('stop animation');
			window.clearTimeout(this.animating);
			this.animating = null;
		}
		this.moveElm();
		return;
	}

	// Glide by moving slowly to current value
	var deltaX = TLABEL.SMOOTH_FACTOR * (this.targetX - this.x);
	var deltaY = TLABEL.SMOOTH_FACTOR * (this.targetY - this.y);

	//this.debug('glideElm deltaX=' + deltaX + ' deltaX=' + deltaY);
	if (Math.abs(deltaX) <= 1 && Math.abs(deltaY) <= 1) {
		deltaX = deltaX < 0 ? -1.0 : 1.0;
		deltaY = deltaY < 0 ? -1.0 : 1.0;
	}
	//this.debug('glideElm NOW deltaX=' + deltaX + ' deltaX=' + deltaY);


	this.x = this.x + deltaX;
	this.y = this.y + deltaY;

	//apply to icon div
	this.x = Math.round(this.x);
	this.y = Math.round(this.y);
	//this.debug('glideElm exit x=' + this.x + ' tx=' + this.targetX + ' y=' + this.y + ' ty=' + this.targetY);
	this.moveElm();
}

/*
function normSin(a) {
	if (a > 0.9999) {
		a = 0.9999;
	}
	if (a < -0.9999) {
		a = -0.9999;
	}
	return a;
}

TLabel.prototype.getXY = function(a, b) {
	var c = a.getZoom();
	var d = a.getSize();
	var e = a.getCenter();
	with (Math) {
		var pxLng = 128 * pow(2, c) / 180;
		var pxLat = 128 * pow(2, c) / PI;
		var xDif = -(e.x - b.x) * pxLng;
		var g = normSin(sin(b.y * PI / 180));
		var h = normSin(sin(e.y * PI / 180));
		var yDif = (0.5 * log((1 + h) / (1 - h)) - 0.5 * log((1 + g) / (1 - g))) * pxLat;
		var x = round((d.width / 2) + xDif) - parseInt(this.mapTray.style.left);
		var y = round((d.height / 2) + yDif) - parseInt(this.mapTray.style.top);
	}
	return(new GPoint(x, y));
} */
TLabel.prototype.setOpacity = function(b) {
	if (b < 0) {
		b = 0;
	}
	if (b > 100) {
		b = 100;
	}
	var c = b / 100;
	var d = this.elm;
	// document.getElementById(this.id);
	if (typeof(d.style.filter) == 'string') {
		d.style.filter = 'alpha(opacity:' + b + ')';
	}
	if (typeof(d.style.KHTMLOpacity) == 'string') {
		d.style.KHTMLOpacity = c;
	}
	if (typeof(d.style.MozOpacity) == 'string') {
		d.style.MozOpacity = c;
	}
	if (typeof(d.style.opacity) == 'string') {
		d.style.opacity = c;
	}
}

TLabel.prototype.addTBug = function(a) {
	if (typeof(a.TLabelBugged) == 'undefined') {
		var b = document.createElement('div');
		b.id = 'TLabelBug';
		b.style.position = 'absolute';
		b.style.right = '0px';
		if (a.TBugged > 0) {
			b.style.bottom = '32px';
		} else {
			b.style.bottom = '20px';
		}
		b.style.backgroundColor = '#f2efe9';
		b.style.zIndex = 25500;
		b.innerHTML = '<a href="http://gmaps.tommangan.us/tlabel.html" style="font:10px verdana;text-decoration:none;margin:0px;padding:2px;color:#000;">Made with TLabel</a>';
		document.getElementById(a.getContainer().id).appendChild(b);
		var c = 0.7;
		var d = document.getElementById(b.id);
		if (typeof(d.style.filter) == 'string') {
			d.style.filter = 'alpha(opacity:' + c * 100 + ')';
		}
		if (typeof(d.style.KHTMLOpacity) == 'string') {
			d.style.KHTMLOpacity = c;
		}
		if (typeof(d.style.MozOpacity) == 'string') {
			d.style.MozOpacity = c;
		}
		if (typeof(d.style.opacity) == 'string') {
			d.style.opacity = c;
		}
		a.TLabelBugged = 1;
	}
}

GMap2.prototype.addTLabel = function(a) {
	a.initialize(this);
}

GMap2.prototype.removeTLabel = function(a) {
	//var b = document.getElementById(a.id);
	this.getPane(TLABEL.G_MAP_PANE).removeChild(a.elm);
	// 	document.getElementById(this.getContainer().id).firstChild.removeChild(b);
	delete(b);
}
