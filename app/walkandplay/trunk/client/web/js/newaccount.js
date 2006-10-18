// JavaScript Document
var licenses;
function TrimString(sInString) {
  sInString = sInString.replace( /^\s+/g, "" );// strip leading
  return sInString.replace( /\s+$/g, "" );// strip trailing
}
function pwCallback(element) {
		switch(element.nodeName)
		{
	//LOGIN 		
			case 'login-rsp':
				KW.selectApp('wp','user');

			break;	
			case 'select-app-rsp':
				var doc = KW.createRequest('license-getlist-req');
				var xml = doc.documentElement;
			//	KW.utopia(doc);
			break;	
			case 'license-getlist-rsp':
				licenses = element.getElementsByTagName('record');
				var option;
				var sel = document.getElementById('licenses');
				for(var i = 0; i <licenses.length; i++) {
					option = document.createElement('option');
					option.innerHTML = licenses[i].getElementsByTagName('type')[0].firstChild.nodeValue;
					sel.appendChild(option);
					
				}			
			break;	
		}		
}
function pwNegResp(elm) {
alert(elm);

}
dojo.event.connect(window,'onload',function(evt) {

KW.url = 'wp/proto.srv';
KW.init(pwCallback, pwNegResp, 60);
KW.login('walkandplay','wp-user','user');

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
	txt = doc.createTextNode('http://test.walkandplay.com/register');
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
	password.appendChild(txt);	
	
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
	alert(dojo.dom.innerXML(xml));
	KW.utopia(doc);
	/*
	by (ja/ja)
by-sa (ja/mits)
by-nc (nee/ja)
by-nd (ja/nee)
by-sa-nc (nee/mits)
by-nc-nd (nee/nee)
*/

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