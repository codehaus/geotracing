// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Observable class in Observer/Listener pattern.
 *
 * PURPOSE
 * Author: Just van den Broecke
 * $Id$
 */
function Subject() {

	/** Array of events; each element contains an array of listeners for that event. */
	this.events = new Array();

	/**
	 * Add a listener for event.
	 *
	 * @param event the event to listen for
	 * @param fun the callback function
	 * @param obj optional object that contains callback function
	 */
	this.addListener = function(event, fun, obj) {
		if (!this.events[event]) {
			this.events[event] = new Array();
		}

		this.removeListener(event, obj, fun);

		// Create and populate new Listener object
		var listener = new Object();
		listener.obj = obj;
		listener.fun = fun;

		// Add for this event
		this.events[event].push(listener);
	}

	/**
	 * Remove a listener for event.
	 *
	 * @param event the event to listen for
	 * @param fun the callback function
	 * @param obj optional object that contains callback function
	 */
	this.removeListener = function(event, fun, obj) {
		if (!this.events[event]) {
			return;
		}

		for (var i = 0; i < this.events[event].length; i++) {
			if (this.events[event][i].fun == fun && this.events[event][i].obj == obj) {
				// Removes listener element from event array
				this.events[event].splice(i, 1);
				break;
			}
		}
	}

	/**
	 * Call all listeners registered for event.
	 *
	 * @param event the event being fired
	 * @param value optional event value
	 */
	this.callListeners = function(event, value) {
		if (!this.events[event]) {
			return;
		}

		for (var i = 0; i < this.events[event].length; i++) {
			try {
				// Do callback
				this.events[event][i].fun(this.events[event][i].obj, value);
			} catch(e) {
				alert("error while firing event: " + event)
			}
		}
	}

	/**
	 * Updates all interested listeners for supplied event.
	 *
	 * @param event the event to fire
	 * @param value optional value with the event
	 */
	this.fireEvent = function(event, value) {
		// Call all interested listeners
		this.callListeners(event, value);
	}
}
