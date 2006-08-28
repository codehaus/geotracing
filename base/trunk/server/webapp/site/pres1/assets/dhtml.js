// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Cross-browser DHTML lib.
 *
 * PURPOSE
 * This library can be used for common DHTML functions
 *
 * USAGE
 * DH.init()
 *
 * Author: Just van den Broecke
 * $Id: dhtml.js,v 1.1 2005/11/30 13:22:51 just Exp $
 */
var DH = {
  initialized: false,
  isIE6CSS: (document.compatMode && document.compatMode.indexOf("CSS1") >= 0) ? true : false,
  isIE: false,

  // Initialization: must be called before anything
  init: function() {
    if (DH.initialized == true) {
      return;
    }

    // Some checks need this
    DH.isIE = !window.opera && navigator.userAgent.indexOf('MSIE') != -1;

    // Sniff required browser features
    if (!document.getElementById) {
    	alert('Your browser does not support W3C DHTML, use a modern browser like FireFox');
    	return;
    }

    // Sniff required browser features
    if (!document.getElementsByTagName) {
    	alert('No browser XML support, use a modern browser like FireFox');
    	return;
    }

   /*
     Setup Cross-Browser XMLHttpRequest v1.2
    Emulate Gecko 'XMLHttpRequest()' functionality in IE and Opera. Opera requires
    the Sun Java Runtime Environment <http://www.java.com/>.

    by Andrew Gregory
    http://www.scss.com.au/family/andrew/webdesign/xmlhttprequest/

    This work is licensed under the Creative Commons Attribution License. To view a
    copy of this license, visit http://creativecommons.org/licenses/by-sa/2.5/ or
    send a letter to Creative Commons, 559 Nathan Abbott Way, Stanford, California
    94305, USA.

    */
   // IE support
   if (window.ActiveXObject && !window.XMLHttpRequest) {
     window.XMLHttpRequest = function() {
     var msxmls = new Array(
      'Msxml2.XMLHTTP.5.0',
      'Msxml2.XMLHTTP.4.0',
      'Msxml2.XMLHTTP.3.0',
      'Msxml2.XMLHTTP',
      'Microsoft.XMLHTTP');
     for (var i = 0; i < msxmls.length; i++) {
       try {
         return new ActiveXObject(msxmls[i]);
       } catch (e) {
      }
     }
     return null;
    };
   }

   // ActiveXObject emulation
   if (!window.ActiveXObject && window.XMLHttpRequest) {
    window.ActiveXObject = function(type) {
    switch (type.toLowerCase()) {
      case 'microsoft.xmlhttp':
      case 'msxml2.xmlhttp':
      case 'msxml2.xmlhttp.3.0':
      case 'msxml2.xmlhttp.4.0':
      case 'msxml2.xmlhttp.5.0':
        return new XMLHttpRequest();
      }
     return null;
    };
   }

   DH.initialized = true;
  },

   // Cross-browser add event listener to element
  addEvent: function(elm, evType, callback, useCapture) {
    if (elm.addEventListener) {
      elm.addEventListener(evType, callback, useCapture);
      return true;
    } else if (elm.attachEvent) {
      var r = elm.attachEvent('on' + evType, callback);
      return r;
    } else {
      elm['on' + evType] = callback;
    }
  },

  // Cross-browser remove event listener to element
  // See http://www.quirksmode.org/js/events_advanced.html
  removeEvent: function(elm, evType, callback) {
    if (elm.removeEventListener) {
      elm.removeEventListener(evType, callback, false);
      return true;
    } else if (elm.detachEvent) {
      var r = elm.detachEvent('on' + evType, callback);
      return r;
    } else {
      elm['on' + evType] = null;
    }
  },

 // Get event
  getEvent: function(e) {
    // Obtain event  (cross-browser)
    return window.event ? window.event : e ;
  },

  // Get target object for event
  getEventTarget: function(e) {
    // Obtain event target (cross-browser)
    var target = window.event ? window.event.srcElement : e ? e.target : null;

    // defeat Safari bug
    // http://www.quirksmode.org/js/events_properties.html
    if (target != null && target.nodeType == 3) {
       target = target.parentNode;
    }
    return target;
  },

   // Get target object for event
  cancelEvent: function(e) {
    // Cancel event propagation (IE)
    if (window.event) {
      window.event.cancelBubble = true;
      window.event.returnValue = false;
      return;
    }

    // Cancel event propagation (others)
    if (e) {
      e.stopPropagation();
      e.preventDefault();
    }
  },

  // x-coord of event
  getEventX: function(e) {
    var x;
    if (e.pageX) {
      x = e.pageX;
    } else if (e.clientX) {
      x = e.clientX;
      if (DH.isIE) {
        x += document.body.scrollLeft;
     }
    }
    return x;
  },

 // y-coord of event
 getEventY: function(e) {
    var y;
    if (e.pageY) {
      y = e.pageY;
    } else if (e.clientY) {
      y = e.clientY;
      if (DH.isIE) {
        y += document.body.scrollTop;
     }
    }
    return y;
  },

  // Return the available content width space in browser window
  getInsideWindowWidth: function () {
    if (window.innerWidth) {
        return window.innerWidth;
    } else if (DH.isIE6CSS) {
        // measure the html element's clientWidth
        return document.body.parentElement.clientWidth
    } else if (document.body && document.body.clientWidth) {
        return document.body.clientWidth;
    }
    return 0;
  },

  // Return the available content height space in browser window
  getInsideWindowHeight: function () {

    if (window.innerHeight) {
        return window.innerHeight;
    } else if (DH.isIE6CSS) {
        // measure the html element's clientHeight
        return document.body.parentElement.clientHeight
    } else if (document.body && document.body.clientHeight) {
        return document.body.clientHeight;
    }
    return 0;
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

  // Get object in another window.
  getObjectInWindow: function(winId, objId) {
    try {
      // Get window object.
	  var winObj = eval(winId);
      return winObj.document.getElementById(objId);
    } catch(e) {
      return null;
    }
  },

  // Get object.style reference by id
  getStyleObject: function(obj) {
    try {
      return DH.getObject(obj).style;
    } catch(e) {
      return null;
    }
  },

  // Object X coordinate
  // Based on findPos*, by ppk (http://www.quirksmode.org/js/findpos.html)
  getObjectX: function(obj) {
    obj = DH.getObject(obj);
    var curLeft = 0;
    if (obj.offsetParent) {
      do {
        curLeft += obj.offsetLeft;
      } while (obj = obj.offsetParent);
    } else if (obj.x) {
      curLeft += obj.x;
    }
    return curLeft;
  },

  // Object Y coordinate
  // Based on findPos*, by ppk (http://www.quirksmode.org/js/findpos.html)
  getObjectY: function(obj) {
    obj = DH.getObject(obj);
    var curTop = 0;
    if (obj.offsetParent) {
      do {
        curTop += obj.offsetTop;
      } while (obj = obj.offsetParent);
    } else if (obj.y) {
      curTop += obj.y;
    }
    return curTop;
  },

  // Retrieve the x coordinate of a positionable object
  getObjectLeft: function (obj)  {
    var styleObj = DH.getStyleObject(obj);
    if (styleObj == null) {
      return 0;
    }
    return parseInt(styleObj.left);
  },

 // Retrieve the y coordinate of a positionable object
  getObjectTop: function (obj)  {
    var styleObj = DH.getStyleObject(obj);
    if (styleObj == null) {
      return 0;
    }
    return parseInt(styleObj.top);
  },

  // Get XML doc from server
  // On response  callback fun is called with optional user data.
  getXML: function(url, callback) {

  	// Obtain XMLHttpRequest object
    var xmlhttp = new XMLHttpRequest();
    if (!xmlhttp || xmlhttp == null) {
    	alert('No browser XMLHttpRequest (AJAX) support');
    	return;
    }

    // Setup response handling
    var cb = callback;
    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState == 4) {
          cb(xmlhttp.responseXML);
      }
    };

    // Open URL
    xmlhttp.open('GET', url, true);

    // Send XML to KW server
    xmlhttp.send(null);
  },

  // get content of URL from server
  getURL: function(url, callback) {

  	// Obtain XMLHttpRequest object
    var xmlhttp = new XMLHttpRequest();
    if (!xmlhttp || xmlhttp == null) {
    	alert('No browser XMLHttpRequest (AJAX) support');
    	return;
    }

    // Setup response handling
    var cb = callback;
    xmlhttp.onreadystatechange = function() {
      if (xmlhttp.readyState == 4) {
          var element = xmlhttp.responseText;
          cb(element);
      }
    };

    // Open URL
    xmlhttp.open('GET', url, true);

    // Send XML to KW server
    xmlhttp.send(null);
  },

  // Position an object at a specific pixel coordinate
  shiftTo: function(obj, x, y) {
    var theObj = DH.getStyleObject(obj);
    if (theObj != null) {
      // equalize incorrect numeric value type
      var units = (typeof theObj.left == "string") ? 'px' : 0;
      theObj.left = x + units;
      theObj.top = y + units;
    }
  },

  // Move an object by x and/or y pixels
  shiftBy: function(obj, deltaX, deltaY) {
    var theObj = DH.getStyleObject(obj);
    if (theObj != null) {
      // equalize incorrect numeric value type
      var units = (typeof theObj.left == "string") ? 'px' : 0;
      // TODO: does not seem to work...
      theObj.left = (parseInt(theObj.left)  + deltaX) + units;
      theObj.top = (parseInt(theObj.top)  + deltaY) + units;
    }
  },

  // Set the background color of an object
  setBGColor: function(obj, color) {
    var theObj = DH.getStyleObject(obj);
    if (theObj != null) {
      theObj.backgroundColor = color;
    }
  },


  // Write text into a layer object.
  setHTML: function(obj, html) {
    var theObj = DH.getObject(obj);
    if (theObj != null) {
       theObj.innerHTML = html;
    }
  },

  // Write text into a layer object.
  addHTML: function(obj, html) {
    var theObj = DH.getObject(obj);
    if (theObj != null) {
       theObj.innerHTML += html;
    }
  },

  // Set the z-order of an object
  setZIndex: function(obj, zOrder) {
    var theObj = DH.getStyleObject(obj);
    if (theObj != null) {
      theObj.zIndex = zOrder;
    }
  },

  // Set the visibility of an object to visible
  show: function(obj) {
    var theObj = DH.getStyleObject(obj);
    if (theObj != null) {
      theObj.visibility = "visible";
    }
  },

   // Set the visibility of an object to visible
  isVisible: function(obj) {
    var theObj = DH.getStyleObject(obj);
    if (theObj == null) {
      return false;
    }
    return theObj.visibility == "visible";
  },

  // Set the visibility of an object to visible
  toggleVisibility: function(obj) {
    var theObj = DH.getStyleObject(obj);
    if (theObj != null) {
      theObj.visibility = (theObj.visibility == "visible") ? "hidden" : "visible";
    }
  },

  // Set the visibility of an object to hidden
  hide: function(obj) {
    var theObj = DH.getStyleObject(obj);
    if (theObj != null) {
      theObj.visibility = "hidden";
    }
  },

  /** CROSS-BROWSER DRAG AND DROP SUPPORT */
  dragging: false,
  dragTarget: null,
  dragStartX: 0,
  dragStartY: 0,

  // Register object for dragging
  // @parm callback, called during dragging
  dragEnable: function (target, onStart, onDrag, onEnd) {
    // Remember client callback function
    target.onStart = onStart;
    target.onDrag = onDrag;
    target.onEnd = onEnd;
    DH.addEvent(target, 'mousedown', DH._dragStart, false);
  },

  // Unregister object for dragging
  dragDisable: function (target) {
    target.onStart = null;
    target.onDrag = null;
    target.onEnd = null;

    DH.removeEvent(target, 'mousedown', DH._dragStart);
  },

  // (internal) called when dragging starts
 _dragStart: function (e) {
    // Set dragging state
    DH.dragging = true;
    DH.dragTarget = DH.getEventTarget(e);
	e = DH.getEvent(e);
	DH.dragStartX = DH.getEventX(e);
	DH.dragStartY = DH.getEventY(e);

	// Global events during dragging
    DH.addEvent(document, 'mousemove', DH._dragMove, false);
    DH.addEvent(document, 'mouseup', DH._dragEnd, false);

    // Optional call client callback
	if (DH.dragTarget.onStart != null) {
	   DH.dragTarget.onStart(DH.dragTarget, DH.dragStartX, DH.dragStartY);
	}

  },

  // (internal) called when dragged
  _dragMove: function (e) {
	if (DH.dragging == true) {
	    var e = DH.getEvent(e);

	    // Current x,y of mouse
	    var ex = DH.getEventX(e);
	    var ey = DH.getEventY(e);

	    // Optional call client callback
	    if (DH.dragTarget.onDrag != null) {
	      DH.dragTarget.onDrag(DH.dragTarget, ex, ey, ex - DH.dragStartX,  ey - DH.dragStartY);
	    }

	    // Remember last x,y
	    DH.dragStartX = ex;
	    DH.dragStartY = ey;
		return true;
	} else {
	  return false;
	}
  },

  // (internal) called when dragging ends
  _dragEnd: function (e) {
    // Optional call client callback
    var e = DH.getEvent(e);
    if (DH.dragTarget.onEnd != null) {
	   DH.dragTarget.onEnd(DH.dragTarget, DH.getEventX(e), DH.getEventY(e));
	}

    DH.dragging = false;
    DH.dragTarget = null;
    DH.cancelEvent(e);
    DH.removeEvent(document, 'mousemove', DH._dragMove);
    DH.removeEvent(document, 'mouseup', DH._dragEnd);

 	return false;
  }

}


// BELOW: to be added later.....

/*
// Retrieve the rendered width of an element
function getObjectWidth(obj)  {
    var elem = getRawObject(obj);
    var result = 0;
    if (elem.offsetWidth) {
        result = elem.offsetWidth;
    } else if (elem.clip && elem.clip.width) {
        result = elem.clip.width;
    } else if (elem.style && elem.style.pixelWidth) {
        result = elem.style.pixelWidth;
    }
    return parseInt(result);
}

// Retrieve the rendered height of an element
function getObjectHeight(obj)  {
    var elem = getRawObject(obj);
    var result = 0;
    if (elem.offsetHeight) {
        result = elem.offsetHeight;
    } else if (elem.clip && elem.clip.height) {
        result = elem.clip.height;
    } else if (elem.style && elem.style.pixelHeight) {
        result = elem.style.pixelHeight;
    }
    return parseInt(result);
}


 */



/*
 * $Log: dhtml.js,v $
 * Revision 1.1  2005/11/30 13:22:51  just
 * *** empty log message ***
 *
 * Revision 1.12  2005/11/18 16:20:20  just
 * *** empty log message ***
 *
 * Revision 1.11  2005/11/16 19:46:56  just
 * *** empty log message ***
 *
 * Revision 1.10  2005/11/14 16:45:14  just
 * *** empty log message ***
 *
 * Revision 1.9  2005/11/14 12:48:55  just
 * *** empty log message ***
 *
 * Revision 1.8  2005/11/11 21:26:42  just
 * *** empty log message ***
 *
 * Revision 1.7  2005/10/09 20:13:20  just
 * *** empty log message ***
 *
 * Revision 1.6  2005/10/05 22:36:59  just
 * *** empty log message ***
 *
 * Revision 1.5  2005/10/05 13:23:15  just
 * *** empty log message ***
 *
 * Revision 1.4  2005/10/04 14:32:47  just
 * *** empty log message ***
 *
 * Revision 1.3  2005/10/04 09:58:41  just
 * *** empty log message ***
 *
 * Revision 1.2  2005/10/01 11:46:07  just
 * *** empty log message ***
 *
 * Revision 1.1  2005/10/01 10:49:08  just
 * *** empty log message ***
 *
 *
 */
