// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/**
 * Represents GeoTracing user for Bliin app.
 *
 * Extends the standard Tracer class, overruling
 * standards functions.
 *
 * $Id: MyTracer.js,v 1.8 2006-08-13 19:16:36 just Exp $
 */
function MyTracer(name, color, iconURL, pt, time) {
	// Inherits from Tracer
	Tracer.apply(this, new Array(name, color, iconURL, pt, time));


	// Show static info
	this.showInfo = function() {
		DH.setHTML('tracerinfo', this.name);
		DH.setHTML('tracerdesc', 'geen beschrijving aanwezig voor ' + this.name);
		var record = this.getRecord();
		if (record != null) {
			var desc = record.getField('desc');
			if (desc != null) {
				DH.setHTML('tracerdesc', desc);
			} else {
				DH.setHTML('tracerdesc', 'beschrijving voor ' + this.name + ' is leeg');
			}
		} else {
			DH.setHTML('tracerdesc', 'geen user record voor ' + this.name);
		}

		var thumbId = this.getThumbId();
		if (thumbId != null) {
			DH.getObject("tracerimg").src = 'media.srv?id=' + thumbId + "&resize=100x100";
		}  
	}



}

