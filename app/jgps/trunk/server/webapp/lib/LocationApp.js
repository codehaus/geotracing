// Copyright (c) 2005 Just Objects B.V. <just@justobjects.nl>
// Distributable under LGPL license. See terms of license at gnu.org.

/*
 * Test KWClientExt.js.
 *
 * $Id$
 */

var LOCAPP = {

	currentLon: null,
	currentLat: null,
	currentForm: null,
	currentId: null,
	currentDesc: null,
	currentType: null,
	currentSubtype: null,
	currentName: null,
	toUpdateUserId: null,
	UpdateUserId: null,
	gmarkers: [],
	aanmaken: null,
	currentActie: null,
	currentOplossing: null,
	

	latestoverlay:null ,
	loadForm: function(aFormFile) {
		LOCAPP.pr('load form: ' + aFormFile);
		DH.getURL(aFormFile, LOCAPP.onLoadForm);
	},

	loadFormToevoegen: function(aFormFile, lng, lat, form) {
		LOCAPP.pr('load form: ' + aFormFile);
		LOCAPP.currentLon = lng;
		LOCAPP.currentLat = lat;
		LOCAPP.currentForm = form;

		DH.getURL(aFormFile, LOCAPP.onLoadForm);

	},
	
	loadFormUpdate: function(aFormFile, lng, lat, id,name,subtype,desc,actie,oplossing, form) {
		LOCAPP.pr('load form: ' + aFormFile);
		LOCAPP.currentLon = lng;
		LOCAPP.currentLat = lat;
		LOCAPP.currentForm = form;
		LOCAPP.currentId = id;
		LOCAPP.currentDesc = desc;
		LOCAPP.currentName = name;
		LOCAPP.currentSubtype = subtype;
		LOCAPP.currentActie = actie;
		LOCAPP.currentOplossing = oplossing;
		
		DH.getURL(aFormFile, LOCAPP.onLoadForm);

	},

	onLoadForm: function(aFormText) {
		LOCAPP.pr('onLoadForm');
		DH.setHTML('session', aFormText);
		var toevoegenForm = DH.getObject(LOCAPP.currentForm);
		if (toevoegenForm) {
			//alert(lng);
			if(toevoegenForm.id=='addlocationform')
			{
				toevoegenForm.lon.value = LOCAPP.currentLon;
				toevoegenForm.lat.value = LOCAPP.currentLat;
			}
			else
			{
				toevoegenForm.lon.value = LOCAPP.currentLon;
				toevoegenForm.lat.value = LOCAPP.currentLat;
				toevoegenForm.id.value = LOCAPP.currentId;
				toevoegenForm.categories.value = LOCAPP.currentSubtype;
				toevoegenForm.description.value = LOCAPP.currentDesc;
				toevoegenForm.name.value = LOCAPP.currentName;
				toevoegenForm.actie.value = LOCAPP.currentActie;
				toevoegenForm.oplossing.value = LOCAPP.currentOplossing;
			}
		}
		GMAP.resize();
	},

	deleteMarker: function() {
		for (var i=0;i<this.gmarkers.length;i++) {
			if(this.gmarkers[i].u==latestoverlay.u)
			{
				GMAP.map.removeOverlay(this.gmarkers[i]);
				GMAP.map.closeInfoWindow();
			}
		}
	},
	
	deleteAllMarkers: function() {
		for (var i=0;i<this.gmarkers.length;i++) {
    	    GMAP.map.removeOverlay(this.gmarkers[i]);
      	}
		GMAP.map.closeInfoWindow();
		GEvent.removeListener(LOCAPP.aanmaken);
	},

	createMarker: function(point, icon) {
		// ======== Add a "directions" link ======
		var marker = new GMarker(point, icon);
		//  var htmltekst="<div style='width:20;height:20'><a href='./locationapp/Toevoegen.jsp?punt=" + point + "' target='_blank' style='color:blue'>editeren</a></div>";
		var lng = point.lng();
		var lat = point.lat();
		var htmltekst = "<label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.loadFormToevoegen('locationapp/add-location-form.html'," + lng + "," + lat + ",'addlocationform')><u>Creeër marker</u></label><br><label onmouseover=this.style.cursor='pointer' onclick=LOCAPP.deleteMarker()><u>Verwijder marker</u></label>";
		GEvent.addListener(marker, "click", function() {
			latestoverlay = marker;
			marker.openInfoWindowHtml(htmltekst);
		});
		
		LOCAPP.gmarkers.push(marker);
		return marker;
	} ,

	starteditSession: function() {
		LOCAPP.pr('start');
	//	KW.init(LOCAPP.onRsp, LOCAPP.onNegRsp, 60);
		//KW.agentkey=null;
		KW.webRoot='';
		KW.url='/proto.srv';
		DH.setHTML('session', '');
		DH.setHTML('result', '');
		DH.setHTML('featurepreview', '');
		DH.setHTML('featuredesc', '');
		DH.setHTML('featuretitle', '');
		DH.setHTML('debug','Bewerksessie');
		KW.init(LOCAPP.onRsp, LOCAPP.onNegRsp, 60, '/jgps');
		LOCAPP.loadForm('locationapp/loginedit-form.html');
	
		
	},

	startSession: function() {
		LOCAPP.pr('start');
		//KW.init(LOCAPP.onRsp, LOCAPP.onNegRsp, 60);
		KW.agentkey=null;
		KW.webRoot='';
		KW.url='/proto.srv';
		DH.setHTML('session', '');
		DH.setHTML('result', '');
		DH.setHTML('featuretitle', '');
		DH.setHTML('featurepreview', '');
		DH.setHTML('featuredesc', '');
		DH.setHTML('debug','Leessessie');
		KW.init(LOCAPP.onRsp, LOCAPP.onNegRsp, 60, '/jgps');
		LOCAPP.loadForm('locationapp/login-form.html');
	
	},
	
	login: function() {
		LOCAPP.pr('logging in...');

		var loginForm = document.getElementById('loginform');
		var punt = DH.getPageParameter('punt', null);
		if (loginForm.username.value == "") {
			alert("Please fill in you name.");
			loginForm.username.focus();
			return false;
		}

		if (loginForm.password.value == "") {
			alert("Please fill in your password.");
			loginForm.password.focus();
			return false;
		}

		// Do the login
		MYAPP.currentUser = loginForm.username.value + "e";
		//		alert(3);
		KW.login(MYAPP.currentUser, loginForm.password.value + "e");
		//alert(1);
		LOCAPP.pr('login sent');
		LOCAPP.loadForm('locationapp/ingelogd.html');
		//GTW.featureSet.dispose();
		//alert(MYAPP.currentUser);
		
		
	//	window.location="locatie-map.jsp?punt=" + punt +"&cmd=kaart&user=" + loginForm.username.value;
		SRV.get('q-locations-by-user', MYAPP.showlocations, 'user', MYAPP.currentUser );
		MYAPP.ingelogd=true;
		//LOCAPP.gmarkers=null;
		MYAPP.init();
		MYAPP.start();
		if(this.gmarkers.length!=0)
		{
			LOCAPP.deleteAllMarkers();
		}
		
		return false;
		
	},
	
	loginEdit: function() {
		LOCAPP.pr('logging in...');

		var loginForm = document.getElementById('logineditform');
		var punt = DH.getPageParameter('punt', null);
		var loginName = loginForm.username.value;
		var str=loginName.substring(loginName.length - 1,loginName.length);
		if (loginForm.username.value == "") {
			alert("Please fill in you name.");
			loginForm.username.focus();
			return false;
		}
					
		if (loginForm.password.value == "") {
			alert("Please fill in your password.");
			loginForm.password.focus();
			return false;
		}
		else
		{
			if(str!='e'){
				alert("U heeft geen edit rechten.");
				loginForm.password.focus();
				return false;
			}
			else
			{
				MYAPP.currentUser = loginForm.username.value;
				KW.login(loginForm.username.value,loginForm.password.value);
				LOCAPP.pr('login sent');
				LOCAPP.loadForm('locationapp/ingelogd.html');
				//GTW.featureSet.dispose();
				GTAPP.mode = 'media';
				GTAPP.showMode();
				
				SRV.get('q-locations-by-user', MYAPP.showMarkerlocations, 'user', MYAPP.currentUser );
				var icon = new GIcon();
				icon.image = "./images/nieuw.png";
				icon.iconSize = new GSize(20, 34);
				icon.iconAnchor = new GPoint(10, 30);
				icon.infoWindowAnchor = new GPoint(9, 2);
		
				GEvent.addListener(GMAP.map, "click", function(marker, point) {
					if (!marker) {
						GMAP.map.addOverlay(LOCAPP.createMarker(point, icon));
					}
				});
				LOCAPP.aanmaken=GEvent.addListener(GMAP.map, "click", function(marker, point) {
					if (!marker) {
						GMAP.map.addOverlay(LOCAPP.createMarker(point, icon));
					}
				});
				return false;
			}
		}
		

	},

	/*loginEditMap: function() {
		LOCAPP.pr('logging in...');

		var loginForm = document.getElementById('logineditmapform');
		
		
		if (loginForm.username.value == "") {
			alert("Please fill in you name.");
			loginForm.username.focus();
			return false;
		}
					
		if (loginForm.password.value == "") {
			alert("Please fill in your password.");
			loginForm.password.focus();
			return false;
		}

		//loginForm.username.value=loginname.substring(0,loginname.length()-1);
		// Do the login
		//loginName=loginName.substring(0,loginName.length - 1);
		//SRV.get('q-getuserid', LOCAPP.getUpdateUserId, 'user', loginName);
		KW.login(loginForm.username.value, loginForm.password.value);
		LOCAPP.pr('login sent');
		LOCAPP.loadForm('locationapp/ingelogd.html');
		//GTW.featureSet.dispose();
		GTAPP.mode = 'media';
		GTAPP.showMode();
		//MYAPP.currentUser = loginName;
		//SRV.get('q-locations-by-user', MYAPP.showlocations, 'user', loginName);
		var punt = DH.getPageParameter('punt', null);
		MYAPP.currentUser = loginForm.username.value;
		SRV.get('q-locations-by-user', MYAPP.showMarkerlocations, 'user', loginForm.username.value);
		return false;
	},*/

	getToUpdateUserId: function(records)
	{
		var record;
		record=records[0];
		LOCAPP.toUpdateUserId=record.getField('uid');
	},
	
	getUpdateUserId: function(records)
	{
		var record;
		record=records[0];
		LOCAPP.UpdateUserId=record.getField('uid');
	},

	pr: function (s) {
		/*	var elm = document.getElementById('result');
				elm.innerHTML = elm.innerHTML + '<br/>' + s;*/
	},

/** Send heartbeat. */
	sendHeartbeat: function() {
		LOCAPP.pr('sending heartbeat..');
		var req = KW.createRequest('t-hb-req');
		KW.utopia(req);
	},


	deleteMedium: function (id) {
		LOCAPP.pr('deleting medium...id=' + id);
		KW.MEDIA.del(LOCAPP.onDeleteMediumRsp, id);
	},

	onDeleteMediumRsp: function(elm) {
		LOCAPP.pr('deleted medium OK');
		DH.getObject('uploadedMedium').innerHTML = 'medium deleted';
		DH.getObject('uploadedMediumActions').innerHTML = ' ';
	},

	updateMedium: function (id) {
		LOCAPP.pr('updateMedium...id=' + id);
		KW.MEDIA.update(LOCAPP.onUpdateMediumRsp, id, 'new name', 'new description');
	},

	onUpdateMediumRsp: function(elm) {
		LOCAPP.pr('onUpdateMediumRsp  OK');
		// DH.getObject('uploadedMedium').innerHTML = 'medium updated';
	},

	addLocation: function () {
		LOCAPP.pr('uploading medium...');
		KW.MEDIA.upload(LOCAPP.onUploadMediumRsp, DH.getObject('addlocationform'));
		return false;
	},
	
	updateLocation: function () {
		LOCAPP.pr('uploading medium...');
		KW.MEDIA.upload(LOCAPP.UpdateUploadMediumRsp, DH.getObject('updatelocationform'));
		return false;
	},
	
	updateLocationReq: function(lon, lat, relateids, category, id, desc, name, actie, oplossing){
		// <add-location-req relateids="123,456,789" lon="4.99' lat="54.45/>
		if(oplossing==null || oplossing=="")
		{
			oplossing="Geen oplossing";
		}
		
		if(actie==null || actie=="")
		{
			actie="Geen actie";
		}
		
		if(desc==null || desc=="")
		{
			desc="Geen probleemstelling";
		}
		
		var req = KW.createRequest('loc-update-req');
		KW.UTIL.setOptAttr(req, 'relateids', relateids);
		KW.UTIL.setOptAttr(req, 'subtype', category);
		KW.UTIL.setAttr(req, 'lon', lon);
		KW.UTIL.setAttr(req, 'lat', lat);
		KW.UTIL.setAttr(req, 'id', id);
		KW.UTIL.setAttr(req, 'desc', desc);
		KW.UTIL.setAttr(req, 'name', name);
		KW.UTIL.setAttr(req, 'oplossing', oplossing);
		KW.UTIL.setAttr(req, 'actie', actie);
		
		KW.utopia(req);
		LOCAPP.deleteAllMarkers();
		SRV.get('q-locations-by-user', MYAPP.showMarkerlocations, 'user', MYAPP.currentUser );
	},


	addLocationReq: function(lon, lat, relateids, category, actie, oplossing, name) {
		// <add-location-req relateids="123,456,789" lon="4.99' lat="54.45/>
		if(oplossing==null || oplossing=="")
		{
			oplossing="Geen oplossing";
		}
		
		if(actie==null || actie=="")
		{
			actie="Geen actie";
		}
		
				
		var req = KW.createRequest('loc-create-req');
		KW.UTIL.setAttr(req, 'name', name);
		KW.UTIL.setAttr(req, 'lon', lon);
		KW.UTIL.setAttr(req, 'lat', lat);
		KW.UTIL.setAttr(req, 'relateids', relateids);
		KW.UTIL.setAttr(req, 'subtype', category);
		KW.UTIL.setAttr(req, 'oplossing', oplossing);
		KW.UTIL.setAttr(req, 'actie', actie);
		
//		KW.UTIL.setAttr(req, 'id', LOCAPP.UpdateUserId);
		KW.utopia(req);
		LOCAPP.deleteAllMarkers();
		SRV.get('q-locations-by-user', MYAPP.showMarkerlocations, 'user', MYAPP.currentUser );
		//LOCAPP.UpdatePersReq(LOCAPP.UpdateUserId,LOCAPP.toUpdateUserId);
	},

	UpdatePersReq: function(UpdateUserId, toUpdateUserId) {
		// <add-location-req relateids="123,456,789" lon="4.99' lat="54.45/>
		var req = KW.createRequest('loc-update-pers-req');
		KW.UTIL.setAttr(req, 'UpdateUserId', UpdateUserId);
		KW.UTIL.setAttr(req, 'toUpdateUserId', toUpdateUserId);
		KW.utopia(req);
	},

	deleteLocationReq: function(id) {
		// <add-location-req relateids="123,456,789" lon="4.99' lat="54.45/>
		/* srv.get werkt enkel voor select queries
		var loginName="Dirk";
		SRV.get('q-delete-locative-media', GTAPP.onQueryDelete, 'id', id);*/
		var req = KW.createRequest('loc-delete-req');
		KW.UTIL.setAttr(req, 'id', id);
		KW.utopia(req);
		LOCAPP.deleteMarker();
	},
	
	UpdateUploadMediumRsp: function(elm) {
		// var rsp = elm.getElementsByTagname('medium-insert-rsp');
		LOCAPP.pr('medium upload: ' + elm.tagName + ' id=' + elm.getAttribute('id'));
		var updateLocationForm = DH.getObject('updatelocationform');
		var lon = updateLocationForm.lon.value;
		var lat = updateLocationForm.lat.value;
		var subtype = updateLocationForm.categories.value;
		var names = updateLocationForm.name.value;
		var desc = updateLocationForm.description.value;
		var id = updateLocationForm.id.value;
		var actie = updateLocationForm.actie.value;
		var oplossing = updateLocationForm.oplossing.value;
		
		KW.MEDIA.update(LOCAPP.onUpdateMediumRsp, id, names, desc);
		
		var mediumDiv = DH.getObject('featurepreview');
		var relatieids = elm.getAttribute('id');
		mediumDiv.innerHTML = '<img src="media.srv?id=' + relatieids + '&resize=100" border="0" />';
		
		
		LOCAPP.updateLocationReq(lon, lat, relatieids, subtype, id, desc, names, actie, oplossing);
	},
	
	onUploadMediumRsp: function(elm) {
		// var rsp = elm.getElementsByTagname('medium-insert-rsp');
		LOCAPP.pr('medium upload: ' + elm.tagName + ' id=' + elm.getAttribute('id'));
		var mediumDiv = DH.getObject('featurepreview');
		var id = elm.getAttribute('id');
		mediumDiv.innerHTML = '<img src="media.srv?id=' + id + '&resize=100" border="0" />';
		var addLocationForm = DH.getObject('addlocationform');
		var lon = addLocationForm.lon.value;
		var lat = addLocationForm.lat.value;
		var label = addLocationForm.categories.value;
		var actie = addLocationForm.actie.value;
		var name = addLocationForm.name.value;
		var oplossing = addLocationForm.oplossing.value;
		LOCAPP.addLocationReq(lon, lat, id, label, actie, oplossing, name);
	},


	onRsp: function(elm) {
		if (!elm) {
			LOCAPP.pr('empty response');
			return;
		}
		LOCAPP.pr('server response ' + elm.tagName);
		if (elm.tagName == 'login-rsp') {
			KW.selectApp('geoapp', 'user');
			// of guest
		} else if (elm.tagName == 'select-app-rsp') {
			LOCAPP.pr('login OK');
			DH.setHTML('session', 'ingelogd');
			// LOCAPP.loadForm('locationapp/add-location-form.html')
		} else if (elm.tagName == 'logout-rsp') {
			LOCAPP.pr('logout OK');
			window.clearInterval(LOCAPP.hbTimer);
		} else if (elm.tagName == 't-hb-rsp') {
			LOCAPP.pr('heartbeat OK');
		} else {
			LOCAPP.pr('rsp tag=' + elm.tagName + ' ' + elm);
		}
		// alert('onSelect name=' + name + ' value=' + value + ' label=' + label);
	},

/** KWClient negative response handler. */
	onNegRsp: function(errorId, error, details) {
		LOCAPP.pr('negative resp:' + error + ' details=' + details);
	}
}



