// JavaScript Document
var licenses;
function TrimString(sInString) {
  sInString = sInString.replace( /^\s+/g, "" );// strip leading
  return sInString.replace( /\s+$/g, "" );// strip trailing
}

dojo.event.connect(document.getElementById('add'),'onclick',function(evt) {
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
			document.getElementById('addmediumform').style.display = 'none';
			document.getElementById('previewImage').style.display = 'block';

		} else {
				setTimeout('_checkIFrameRsp()', 2000);

		}
	}	


dojo.event.connect(window,'onload',function(evt) {
	//KW.url = '../../wp/proto.srv';
	KW.init(WPCallback, WPNegResp, 60,'/wp');
	KW.login('geoapp-user','user');
});


dojo.event.connect(document.getElementById('signupform'),'onsubmit',function(evt) {
	evt.preventDefault();
	var txt;
	var doc = KW.createRequest('profile-create-req');
	var xml = doc.documentElement;
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
	var photoid = doc.createElement('photoid');
	var keywords = doc.createElement('keywords');	
	var street = doc.createElement('street');	
	var streetnr = doc.createElement('streetnr');	
	var mobilenr = doc.createElement('mobilenr');	
	var password = doc.createElement('password');
	var license = doc.createElement('license');
	var tag; 
	var confirmationurl = doc.createElement('confirmationurl');

	xml.appendChild(person);

	xml.appendChild(confirmationurl);
	txt = doc.createTextNode('http://test.walkandplay.com/register.jsp');
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
	
	person.appendChild(password);
	txt = doc.createTextNode(document.signupform.password.value);
	password.appendChild(txt);	
	
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
	var val1 = 0;
	for( i = 0; i < document.signupform.cc1.length; i++ ) {
		if( document.signupform.cc1[i].checked == true ){
			val1 = document.signupform.cc1[i].value;
		}
	}
	var val2 = 0;
	for(var i = 0; i < document.signupform.cc2.length; i++ ) {
		if( document.signupform.cc2[i].checked == true ){
			val2 = document.signupform.cc2[i].value;
		}
	}	
	if(val1 == 'yes' && val2 == 'yes') {
		cc = 'by';
	} else if(val1 == 'yes' && val2 == 'though') {
		cc = 'by-sa';
	} else if(val1 == 'no' && val2 == 'yes') {
		cc = 'by-nc';
	} else if(val1 == 'no' && val2 == 'though') {
		cc = 'by-sa-nc';
	} else if(val1 == 'yes' && val2 == 'no') {
		cc = 'by-nd';	
	} else if(val1 == 'no' && val2 == 'no') {
		cc = 'by-nc-nd';
	}
	
	xml.appendChild(license);		
	txt = doc.createTextNode(cc);
	license.appendChild(txt);
	xml.appendChild(photoid);		
	txt = doc.createTextNode(document.signupform.photoid.value);
	photoid.appendChild(txt);
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
	User.role = 'guest';
	KW.utopia(doc);
	
});
dojo.event.connect(document.getElementById('tab1'),'onclick', function(evt) {
	document.getElementById('tab_1').style.display = 'block';
	document.getElementById('tab_2').style.display = 'none';
	document.getElementById('tab_3').style.display = 'none';
	document.getElementById('tab1').style.fontWeight = 'bold';
	document.getElementById('tab2').style.fontWeight = 'normal';
	document.getElementById('tab3').style.fontWeight = 'normal';

});
dojo.event.connect(document.getElementById('tab2'),'onclick', function(evt) {
	document.getElementById('tab_1').style.display = 'none';
	document.getElementById('tab_2').style.display = 'block';
	document.getElementById('tab_3').style.display = 'none';
	document.getElementById('tab1').style.fontWeight = 'normal';
	document.getElementById('tab2').style.fontWeight = 'bold';
	document.getElementById('tab3').style.fontWeight = 'normal';
});
dojo.event.connect(document.getElementById('tab3'),'onclick', function(evt) {
	document.getElementById('tab_1').style.display = 'none';
	document.getElementById('tab_2').style.display = 'none';
	document.getElementById('tab_3').style.display = 'block';
	document.getElementById('tab1').style.fontWeight = 'normal';
	document.getElementById('tab2').style.fontWeight = 'normal';
	document.getElementById('tab3').style.fontWeight = 'bold';
});