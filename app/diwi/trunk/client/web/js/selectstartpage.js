// JavaScript Document

	function pageQuery(q) {
		if(q.length > 1) this.q = q.substring(1, q.length);
		else this.q = null;
		this.keyValuePairs = new Array();
		if(q) {
			for(var i=0; i < this.q.split("&").length; i++) {
				this.keyValuePairs[i] = this.q.split("&")[i];
			}
		}
		this.getKeyValuePairs = function() { return this.keyValuePairs; }
		this.getValue = function(s) {
			for(var j=0; j < this.keyValuePairs.length; j++) {
				if(this.keyValuePairs[j].split("=")[0] == s)
					return this.keyValuePairs[j].split("=")[1];
			}
			return false;
		}
		this.getParameters = function() {
			var a = new Array(this.getLength());
			for(var j=0; j < this.keyValuePairs.length; j++) {
				a[j] = this.keyValuePairs[j].split("=")[0];
			}
			return a;
		}
		this.getLength = function() { return this.keyValuePairs.length; }	
	}
	
	function queryString(key){
		var page = new pageQuery(window.location.search); 
		return unescape(page.getValue(key)); 
	}
	
	function beginPagina() {
		if (queryString('mp')=='true') { 
			toonMijnPagina();
		} else {
			toonMaakRoutePagina();
		}	
	}
	
	function toonMijnPagina() {	
		document.getElementById("mijnpaginacontainer").style.display = "block";
		document.getElementById("maakroutecontainer").style.display = "none";
	}
	
	function toonMaakRoutePagina() {
		document.getElementById("mijnpaginacontainer").style.display = "none";
		document.getElementById("maakroutecontainer").style.display = "block";
	}