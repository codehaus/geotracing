var MOB = {
	x: 20,
	y: 20,

	go: function() {
		// Sniff required browser features
		if (!document.getElementById) {
			alert('Your browser does not support W3C DHTML, use a modern browser like FireFox');
			return;
		}
		MOB.getObject('msg').innerHTML = '(' + MOB.x + ',' + MOB.y + ')';
		MOB.setObjectXY('bb', MOB.x, MOB.y);
		setInterval('MOB.moveBall()', 3000);
	},

	moveBall: function() {
		MOB.x += 2;
		MOB.y += 2;
		MOB.getObject('msg').innerHTML = '(' + MOB.x + ',' + MOB.y + ')';
		MOB.setObjectXY('bb', MOB.x, MOB.y);
	},
		// Position an object at a specific pixel coordinate
	setObjectXY: function(obj, x, y) {
		var theObj = MOB.getStyleObject(obj);
		if (theObj != null) {
			// equalize incorrect numeric value type
			var units = (typeof theObj.left == "string") ? 'px' : 0;
			if (x) theObj.left = x + units;
			if (y) theObj.top = y + units;
		}
	}
	,
// Cross-browser add event listener to element
	addEvent: function(elm, evType, callback, useCapture) {
		var obj = MOB.getObject(elm);
		if (obj.addEventListener) {
			obj.addEventListener(evType, callback, useCapture);
			return true;
		} else if (obj.attachEvent) {
			var r = obj.attachEvent('on' + evType, callback);
			return r;
		} else {
			obj['on' + evType] = callback;
		}
	},

// Get object reference
	getObject: function(obj) {
		if (typeof obj == "string") {
			return document.getElementById(obj);
		} else {
			// pass through object reference
			return obj;
		}
	},

// Get object.style reference by id
	getStyleObject: function(obj) {
		try {
			return MOB.getObject(obj).style;
		} catch(e) {
			return null;
		}
	}

}

// Initialize when page completely loaded
MOB.addEvent(window, 'load', MOB.go, false);

