dojo.event.connect(window,'onload',function(evt) {
	KW.url = '../../../wp/proto.srv';
	KW.init(WPCallback, WPNegResp, 60,KW.url);
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
	var password = doc.createElement('password');
	var license = doc.createElement('license');
	var tag; 
	var confirmationurl = doc.createElement('confirmationurl');

	xml.appendChild(person);

	xml.appendChild(confirmationurl);
	txt = doc.createTextNode('http://test.walkandplay.com/wp/confirmation.jsp');
	confirmationurl.appendChild(txt);
	
	person.appendChild(nickname);
	txt = doc.createTextNode(document.signupform.nickname.value);
	nickname.appendChild(txt);
	
	person.appendChild(email);
	txt = doc.createTextNode(document.signupform.email.value);
	email.appendChild(txt);
	
	person.appendChild(emailpublic);
	txt = doc.createTextNode('false');
	emailpublic.appendChild(txt);
	
	person.appendChild(password);
	txt = doc.createTextNode(document.signupform.password.value);
	password.appendChild(txt);	
	
	var cc = 'by';
	
	xml.appendChild(license);		
	txt = doc.createTextNode(cc);
	license.appendChild(txt);

	xml.appendChild(profilepublic);
	txt = doc.createTextNode('true');
	profilepublic.appendChild(txt);
	
	User.role = 'guest';
	KW.utopia(doc);
	
});