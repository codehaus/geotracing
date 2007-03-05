// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * GeoTracing presentation app.
 *
 * PURPOSE
 * Library representing the app. All starts here.
 *
 * Author: Just van den Broecke
 * $Id: app.js 8 2006-08-28 15:36:01Z just $
 */


 // The GeoTracing application functions
 var APP = {


    init: function() {

	   // dhtml lib
       DH.init();

       APP.createDyn();
     },

    // Setup callbacks
    createDyn: function() {
      var links = document.getElementsByTagName('div');
      for (var i=0; i < links.length; i++) {
        if (links[i].className != 'url') {
          continue;
        }
        DH.addEvent(links[i], 'click', APP.onWPClick, false);
        DH.addEvent(links[i], 'mouseover', APP.onWPOver, false);
        DH.addEvent(links[i], 'mouseout', APP.onWPOut, false);
       }
    },


   // waypoint clicked
   onWPClick: function (e) {
      var el = DH.getEventTarget(e).parentNode;
      if (el.id) {
        var url = el.getElementsByTagName('a')[0].href;
        window.open(url, 'url');

      }
      DH.cancelEvent(e);
    },

   // waypoint mouse-over
   onWPOver: function (e) {
      var el = DH.getEventTarget(e).parentNode;
      if (el.id) {
        var id = el.id.split('w')[1];
        DH.show('i' + id);
      }
      DH.cancelEvent(e);
    },

  // waypoint mouse-over
   onWPOut: function (e) {
      var el = DH.getEventTarget(e).parentNode;
      if (el.id) {
         var id = el.id.split('w')[1];
         DH.hide('i' + id);
      }
      DH.cancelEvent(e);
    }


   }
  // Starts it all
  DH.addEvent(window, 'load', APP.init, false);
