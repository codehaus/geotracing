// JavaScript Document
	User.role = 'user';

var licenses;
function TrimString(sInString) {
  sInString = sInString.replace( /^\s+/g, "" );// strip leading
  return sInString.replace( /\s+$/g, "" );// strip trailing
}
function writeToForm(elm) {
	document.signupform.firstname.value = elm.getElementsByTagName('firstname')[0].firstChild.nodeValue;
	document.signupform.lastname.value = elm.getElementsByTagName('lastname')[0].firstChild.nodeValue;
	document.signupform.nickname.value = elm.getElementsByTagName('extra')[0].getAttribute('nickname');
	document.signupform.streetnr.value = elm.getElementsByTagName('streetnr')[0].firstChild.nodeValue;
	document.signupform.address.value = elm.getElementsByTagName('street')[0].firstChild.nodeValue;
	document.signupform.zip.value = elm.getElementsByTagName('zipcode')[0].firstChild.nodeValue;
	document.signupform.place.value = elm.getElementsByTagName('city')[0].firstChild.nodeValue;
	document.signupform.country.value = elm.getElementsByTagName('country')[0].firstChild.nodeValue;
	document.signupform.phone.value = elm.getElementsByTagName('mobilenr')[0].firstChild.nodeValue;

try {
		var imageId = elm.getElementsByTagName('medium')[0].getAttribute('id');
		document.getElementById('imageId').value = imageId;
		document.getElementById('previewImage').src = 'wp/media.srv?id='+imageId+'&resize=160x120';
		document.getElementById('previewImage').style.display = 'block';
	} catch(e) {}

}
var cookieName = getCookie('name');
dojo.event.connect(document.getElementById('GOTOeditview'),'onclick',function(evt) {
		if(cookieName) {
			document.getElementById('formblock').style.display = 'block';
			document.getElementById('profileblock').style.display = 'none';
			var doc = KW.createRequest('profile-get-req');
			var xml = doc.documentElement;
			xml.setAttribute('id',KW.userId);
			KW.utopia(doc,writeToForm);
		} else {
			document.getElementById('loginbox').style.display = 'block';
		}
	});
dojo.event.connect(document.getElementById('GOTOoverview'),'onclick',function(evt){
			document.getElementById('formblock').style.display = 'none';
			document.getElementById('profileblock').style.display = 'block';
			});
dojo.event.connect(document.getElementById('file'),'onchange',function(evt) {
		addMedium();																 
		});

function addMedium () {
		document.addmediumform.agentkey.value = KW.agentKey;
		document.addmediumform.submit();
		_checkIFrameRsp();
		var form = document.getElementById('addmediumform');
		form.submit();
		return false;
	}


function _checkIFrameRsp() {
		var iframe = DH.getObject('uploadFrame');
		if (!iframe) {
			alert('cannot get uploadFrame');
			return;
		}

		var iframeDoc = null;

		if (iframe.contentDocument) {
			// For NS6
			iframeDoc = iframe.contentDocument;
		} else if (iframe.contentWindow) {
			// For IE5.5 and IE6
			iframeDoc = iframe.contentWindow.document;
		} else if (iframe.document) {
			// For IE5
			iframeDoc = iframe.document;
		}
		if (iframeDoc.getElementsByTagName('medium-insert-rsp').length > 0) {
			var imageId = iframeDoc.getElementsByTagName('medium-insert-rsp')[0].getAttribute('id');
			document.getElementById('imageId').value = imageId;
			document.getElementById('previewImage').src = 'wp/media.srv?id='+imageId+'&resize=160x120';
			document.getElementById('previewImage').style.display = 'block';
			var txt;
			var doc = KW.createRequest('profile-update-req');
			var xml = doc.documentElement;
			xml.setAttribute('id',KW.userId);
			var person = doc.createElement('person');
			var photoid = doc.createElement('photoid');
			xml.appendChild(photoid);		
			txt = doc.createTextNode(document.signupform.photoid.value);
			photoid.appendChild(txt);
			KW.utopia(doc);
		} else {
			setTimeout('_checkIFrameRsp()', 2000);
		}
	}	


dojo.event.connect(document.getElementById('signupform'),'onsubmit',function(evt) {
	evt.preventDefault();
	var txt;
	var doc = KW.createRequest('profile-update-req');
	var xml = doc.documentElement;
	xml.setAttribute('id',KW.userId);
	var person = doc.createElement('person');
	var nickname = doc.createElement('nickname');
	var email = doc.createElement('email');
	var emailpublic = doc.createElement('emailpublic');
	var profilepublic = doc.createElement('profilepublic');
	var firstname = doc.createElement('firstname');
	var lastname = doc.createElement('lastname');
	var address = doc.createElement('address');
	var zipcode = doc.createElement('zipcode');	
	var city = doc.createElement('city');
	var country = doc.createElement('country');
	var keywords = doc.createElement('keywords');	
	var street = doc.createElement('street');	
	var streetnr = doc.createElement('streetnr');	
	var mobilenr = doc.createElement('mobilenr');	
	var tag; 
	var confirmationurl = doc.createElement('confirmationurl');

	xml.appendChild(person);

	xml.appendChild(confirmationurl);
	txt = doc.createTextNode('http://test.walkandplay.com/confirmation.jsp');
	confirmationurl.appendChild(txt);
	
	person.appendChild(nickname);
	txt = doc.createTextNode(document.signupform.nickname.value);
	nickname.appendChild(txt);
	
	person.appendChild(firstname);
	txt = doc.createTextNode(document.signupform.firstname.value);
	firstname.appendChild(txt);
	
	person.appendChild(lastname);
	txt = doc.createTextNode(document.signupform.lastname.value);
	lastname.appendChild(txt);
	
	person.appendChild(email);
	txt = doc.createTextNode(document.signupform.email.value);
	email.appendChild(txt);
	
		person.appendChild(emailpublic);
	txt = doc.createTextNode(document.signupform.emailpublic.value);
	emailpublic.appendChild(txt);
	
		person.appendChild(country);
	txt = doc.createTextNode(document.signupform.country.value);
	country.appendChild(txt);
	
		person.appendChild(city);
	txt = doc.createTextNode(document.signupform.place.value);
	city.appendChild(txt);	
	
		person.appendChild(zipcode);
	txt = doc.createTextNode(document.signupform.zip.value);
	zipcode.appendChild(txt);	
	
		person.appendChild(street);
	txt = doc.createTextNode(document.signupform.address.value);
	street.appendChild(txt);	
	
		person.appendChild(streetnr);
	txt = doc.createTextNode(document.signupform.streetnr.value);
	streetnr.appendChild(txt);	
	
		person.appendChild(mobilenr);
	txt = doc.createTextNode(document.signupform.phone.value);
	mobilenr.appendChild(txt);	
	
	
	var cc;

	xml.appendChild(profilepublic);
	if(document.signupform.privacy[0].checked == true) {
		txt = doc.createTextNode('true');
	} else {
		txt = doc.createTextNode('false');
	}
	profilepublic.appendChild(txt);
	var keywords;
	keywords = document.signupform.keywords.value;
	keywords = keywords.split(',');
	for(var i = 0; i < keywords.length; i++) {
		keywords[i] = TrimString(keywords[i]);
		tag = doc.createElement('tag');
		xml.appendChild(tag);		
		txt = doc.createTextNode(keywords[i]);
		tag.appendChild(txt);
	}
	KW.utopia(doc);
});
var profileId;
var records;
var numTraces;
var nickname,email,photoid;

var mytraces = document.getElementById('mytraces');
dojo.event.connect(window,'onload', function(evt) {									 
		qs();
		if(qsParm['personid'] == null) {
			if(getCookie('personId') > 0) {
				profileId = getCookie('personId');	
			} else {
				profileId = 0;	
			}
		} else {
			profileId = qsParm['personid'];
		}
		if(profileId != 0 ) {
			SRV.get('q-tracks-by-user', getTrackData, 'user', getCookie('name'));
			SRV.get('q-user-info', getProfileData, 'id', profileId);
		}
	});


function getTrackData(records) {
	numTraces = records.length;
	document.getElementById('summNumTraces').innerHTML = numTraces;
	for(var i = 0; i < numTraces; i++) {
			var r = document.createElement('div');
			var n = document.createTextNode(records[i].getField('name'));
			var id = records[i].getField('id');			
			r.setAttribute('id',id);
			r.appendChild(n);
			mytraces.appendChild(r);
			dojo.event.connect(document.getElementById(id),'onclick',function(evt) {
				window.location ='index.html?cmd=get-track&id='+evt.target.getAttribute('id');	
			 });
		}
}

function getProfileData(record) {
	nickname = record[0].getField('nickname');
	email = record[0].getField('email');
	photoid = record[0].getField('photoid');
	document.getElementById('summNickname').innerHTML = nickname;
	document.getElementById('summEmail').innerHTML = email;
	document.getElementById('summPhotoid').src = 'wp/media.srv?id='+photoid+'&resize=160x120';

}