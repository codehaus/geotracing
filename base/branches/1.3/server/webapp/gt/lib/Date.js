/*
 * DateFormat.js
 * Formats a Date object into a human-readable string.
 *
 * See http://www.gazingus.org/html/Date_Formatting_Function.html
 *
 * Copyright (C) 2001 David A. Lindquist (http://www.gazingus.org)
 */

Date.MONTHS = [
		'January', 'February', 'March', 'April', 'May', 'June', 'July',
		'August', 'September', 'October', 'November', 'December'
		];

Date.DAYS = [
		'Sunday', 'Monday', 'Tuesday', 'Wednesday',
		'Thursday', 'Friday', 'Saturday'
		];

Date.SUFFIXES = [
		'st','nd','rd','th','th','th','th','th','th','th',
		'th','th','th','th','th','th','th','th','th','th',
		'st','nd','rd','th','th','th','th','th','th','th',
		'st'
		];

Date.prototype.format = function(mask) {
	var formatted = ( mask != null ) ? mask : 'DD-MMM-YY';
	var letters = 'DMYHdhmst'.split('');
	var temp = new Array();
	var count = 0;
	var regexA;
	var regexB = /\[(\d+)\]/;

	var day = this.getDay();
	var date = this.getDate();
	var month = this.getMonth();
	var year = this.getFullYear().toString();
	var hours = this.getHours();
	var minutes = this.getMinutes();
	var seconds = this.getSeconds();
	var formats = new Object();
	formats[ 'D' ] = date;
	formats[ 'd' ] = date + Date.SUFFIXES[ date - 1 ];
	formats[ 'DD' ] = ( date < 10 ) ? '0' + date : date;
	formats[ 'DDD' ] = Date.DAYS[ day ].substring(0, 3);
	formats[ 'DDDD' ] = Date.DAYS[ day ];
	formats[ 'M' ] = month + 1;
	formats[ 'MM' ] = ( month + 1 < 10 ) ? '0' + ( month + 1 ) : month + 1;
	formats[ 'MMM' ] = Date.MONTHS[ month ].substring(0, 3);
	formats[ 'MMMM' ] = Date.MONTHS[ month ];
	formats[ 'Y' ] = ( year.charAt(2) == '0' ) ? year.charAt(3) : year.substring(2, 4);
	formats[ 'YY' ] = year.substring(2, 4);
	formats[ 'YYYY' ] = year;
	formats[ 'H' ] = hours;
	formats[ 'HH' ] = ( hours < 10 ) ? '0' + hours : hours;
	formats[ 'h' ] = ( hours > 12 || hours == 0 ) ? Math.abs(hours - 12) : hours;
	formats[ 'hh' ] = ( formats[ 'h' ] < 10 ) ? '0' + formats[ 'h' ] : formats[ 'h' ];
	formats[ 'm' ] = minutes;
	formats[ 'mm' ] = ( minutes < 10 ) ? '0' + minutes : minutes;
	formats[ 's' ] = seconds;
	formats[ 'ss' ] = ( seconds < 10 ) ? '0' + seconds : seconds;
	formats[ 't' ] = ( hours < 12 ) ?  'A' : 'P';
	formats[ 'tt' ] = ( hours < 12 ) ?  'AM' : 'PM';

	for (var i = 0; i < letters.length; i++) {
		regexA = new RegExp('(' + letters[ i ] + '+)');
		while (regexA.test(formatted)) {
			temp[ count ] = RegExp.$1;
			formatted = formatted.replace(RegExp.$1, '[' + count + ']');
			count++;
		}
	}

	while (regexB.test(formatted)) {
		formatted = formatted.replace(regexB, formats[ temp[ RegExp.$1 ] ]);
	}

	return formatted;
}

