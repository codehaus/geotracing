// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Plays Features.
 *
 * $Id$
 */

function FeaturePlayer() {
	this.featureSet = new FeatureSet();
	this.controls = DH.getObject('featurecontrols')
	var self = this;

	this.onFirst = function(e) {
		DH.cancelEvent(e);
		self.featureSet.displayFirst(true);
	}
	this.onPrev = function(e) {
		DH.cancelEvent(e);
		self.featureSet.displayPrev(true);
	}
	this.onNext = function(e) {
		DH.cancelEvent(e);
		self.featureSet.displayNext(true);
	}
	this.onLast = function(e) {
		DH.cancelEvent(e);
		self.featureSet.displayLast(true);
	}

	DH.addEvent('featfirst', 'click', self.onFirst, false);
	DH.addEvent('featprev', 'click', self.onPrev, false);
	DH.addEvent('featnext', 'click', self.onNext, false);
	DH.addEvent('featlast', 'click', self.onLast, false);

	this.setFeatureSet = function (fc) {
		this.featureSet = fc;
	}

	this.show = function() {
		DH.show(this.controls);
	}

	this.hide = function() {
		DH.hide(this.controls)
	}
}

