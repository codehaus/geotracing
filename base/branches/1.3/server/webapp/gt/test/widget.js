var APP = {


	init: function() {
		DH.init();
		DH.debug(false, 'baseDir', DH.getBaseDir());

		var panel1 = new Panel('red', '#ff0000', '#000000');
		panel1.setXY(100, 100);
		panel1.setContent('<img src="adam-satellite.jpg" border="0" >');

		var panel2 = new Panel('blue', '#0000cc', '#000044');
		panel2.setXY(300, 100);

		panel2.setContent('<p>thecontent and such thecon tenanf dkahf</p><p>thecontent and such thecon tenanf dkahf</p><p>thecontent and such thecon tenanf dkahf</p><p>thecontent and such thecon tenanf dkahf</p><p>thecontent and such thecon tenanf dkahf</p><p>thecontent and such thecon tenanf dkahf</p><p>thecontent and such thecon tenanf dkahf</p><p>thecontent and such thecon tenanf dkahf</p><p>thecontent and such thecon tenanf dkahf</p><p>thecontent and such thecon tenanf dkahf</p><p>thecontent and such thecon tenanf dkahf</p>');

		var panel3 = new Panel('purple', '#cc00cc', '#006600');
		panel3.setXY(500, 100);

		panel3.setContent('<p>thecontent and such thecon tenanf dkahf</p>' + panel3.getBBox().toString());

		var panel4 = new Panel('DynContent', '#000000', '#dddddd');
		panel4.hideFooter();
		panel4.setXY(500, 300);
		panel4.setDimension(240, 360);
		//   panel4.loadContent('http.html');

		var selector = new Selector('Select a choice', 's1', APP.onSelect);
		selector.setXY(100, 300);
		selector.addOption('n1', 'val1', 'lab1');
		selector.addOption('n2', 'val2', 'lab2');
		selector.addOption('n3', 'val3', 'lab3');

		APP.menu = new Menu('mainmenu');
		// panel3.setContent('<p>thecontent and such thecon tenanf dkahf</p>' + APP.menu.getBBox().toString());

	},

	onSelect: function(name, value, label) {
		alert('onSelect name=' + name + ' value=' + value + ' label=' + label);
	},

	onMenuItem: function(arg) {
		alert('onMenuItem arg=' + arg);
	},

	onMenuLogin: function() {
		APP.menu.replaceItem('loginout', 'Logout', 'APP.onMenuLogout');
	},

	onMenuRemoveItem: function () {
		APP.menu.removeItem('removeme');
	},

	onMenuLogout: function() {
		APP.menu.replaceItem('loginout', 'Login', 'APP.onMenuLogin');
	}


}

DH.onReady = APP.init;
DH.include('Menu.js');
DH.include('Panel.js');
DH.include('Selector.js');

