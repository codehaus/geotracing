var User = {
	role: 'guest',
	signin: function() {
		var username = document.getElementById('username').value;
		var password = document.getElementById('password').value;
		KW.login(username,password);
	},
	
	
}