// JavaScript Document

	document.getElementById("wandelen").checked = true;
	
	function selectWandelen() {
		document.getElementById("wandelen").checked = true;	
		document.getElementById("fietsen").checked = false;	
	}
	function selectFietsen() {
		document.getElementById("wandelen").checked = false;	
		document.getElementById("fietsen").checked = true;	
	}
	
	
/*
	<div class="listboxes" id="startpunt-lb"> 
	<p>
	Startpunt
	</p>
	<form>
	<select id="startpunt" >
		<option value="0" selected="selected">geen voorkeur</option>
		<option value="1" >bunkerholen</option>
		<option value="2" >heeel lang</option>
		<option value="3" >snert route</option>
	</select>
	</form>
	</div>
	<div class="listboxes" id="eindpunt-lb"> 
	<p>
	Eindpunt
	</p>
	<form>
	<select id="eindpunt" >
		<option value="0" selected="selected">geen voorkeur</option>
		<option value="1" >bunkerholen</option>
		<option value="2" >heeel lang</option>
		<option value="3" >snert route</option>
	</select>
	</form>
	</div>
	<div class="listboxes" id="thema-lb"> 
	<p>
	Thema
	</p>
	<form>
	<select id="thema" >
		<option value="0" selected="selected">geen voorkeur</option>
		<option value="1" >bunkerholen</option>
		<option value="2" >heeel lang</option>
		<option value="3" >snert route</option>
	</select>
	</form>
	</div>
	<div class="listboxes" id="afstand-lb"> 
	<p>
	Afstand
	</p>
	<form>
	<select id="afstand">
		<option value="0" selected="selected">geen voorkeur</option>
		<option value="3" >3 km</option>
		<option value="5" >5 km</option>
		<option value="10" >10 km</option>
		<option value="15" >15 km</option>
		<option value="20" >20 km</option>
	</select>
	</form>
	</div>
*/	
	
	var startpuntString = '<p>Startpunt</p><form><select id="startpunt" ><option value="0" selected="selected">';
	var eindpuntString = '<p>Eindpunt</p><form><select id="eindpunt" ><option value="0" selected="selected">';
	var themesString = '<p>Thema\'s</p><form><select id="thema" ><option value="0" selected="selected">';
	var afstandString = '<p>Afstand</p><form><select id="afstand" ><option value="0" selected="selected">geen voorkeur</option><option value="3" >3 km</option><option value="5" >5 km</option><option value="10" >10 km</option><option value="15" >15 km</option><option value="20" >20 km</option></select></form>';
	
	function maakStartpuntForm(elm) {
		startpuntString += 'geen voorkeur</option> <option value="1" >bunkerholen</option><option value="2" >heeel lang</option><option value="3" >snert route';
		startpuntString += '</option></select></form>';
	
		var ni = document.getElementById('startpunt-lb');
		ni.innerHTML = startpuntString;
	
	}
	function maakEindpuntForm(elm) {
		eindpuntString += 'geen voorkeur</option><option value="1" >bunkerholen</option><option value="2" >heeel lang</option><option value="3" >snert route';
		eindpuntString += '</option></select></form>';
	
		var ni = document.getElementById('eindpunt-lb');
		ni.innerHTML = eindpuntString;
	
	}
	function maakThemasForm(elm) {
//		var themes = elm.firstChild;
		themesString += 'geen voorkeur</option><option value="1" >bunkerholen</option><option value="2" >heeel lang</option><option value="3" >snert route';
		themesString += '</option></select></form>';
	
		var ni = document.getElementById('thema-lb');
		ni.innerHTML = themesString;
	}	
	function maakAfstandForm() {

		var ni = document.getElementById('afstand-lb');
		ni.innerHTML = afstandString;
	}
	// Deze moeten weg...
	maakStartpuntForm();
	maakEindpuntForm(0);
	maakThemasForm(0);
	maakAfstandForm(0);


	function maakGenRoutesList(elm) {
		generatedString = '';
		for (i=1; i<5; i++){
			generatedString += '<p><a href=\"\">' + i +'-de route </a></p>';
		}
		var ni = document.getElementById('generated-routes');
		ni.innerHTML = generatedString;	
	}
	
	function verwerkGenRoute(elm) {
		var route = elm.firstChild;
		var name = route.getElementsByTagName('name')[0].firstChild.nodeValue;
		var description = route.getElementsByTagName('description')[0].firstChild.nodeValue;
		var routeString = '<p>Naam van de route: ' + name + '</p><p>Eigenschappen: </p> <p>' + description + '</p>';
	
		var ni = document.getElementById('generated-routes');
		ni.innerHTML = routeString;	
		
	}
	
	function verwerkPlaatje (elm) {
		imageString = '<a onClick="TST.getBigMap();" ><img src=\"' + unescape(elm.getAttribute('url')) + '\"></a>';
	//	imageString = '<img src=\"' + unescape(elm.getAttribute('url')) + '\">';
	
		var ni = document.getElementById('generated-routes-image');
		ni.innerHTML = imageString;	
	}	
	function verwerkBigPlaatje (elm) {
		imageString = '<a onClick=";" ><img src=\"' + unescape(elm.getAttribute('url')) + '\"></a>';
	//	imageString = '<img src=\"' + unescape(elm.getAttribute('url')) + '\">';
	
		var ni = document.getElementById('generated-routes-image-big');
		ni.innerHTML = imageString;	
	}

	var s1 = new Slider(document.getElementById("slider1"), document.getElementById("s1input"));
	s1.setMinimum(0);
	s1.setMaximum(100);
	s1.onchange = function () {
		document.getElementById("s1value").value = s1.getValue();
	};
	s1.setValue(0);

	var s2 = new Slider(document.getElementById("slider2"), document.getElementById("s2input"));
	s2.setMinimum(0);
	s2.setMaximum(100);
	s2.onchange = function () {
		document.getElementById("s2value").value = s2.getValue();
	};
	s2.setValue(0);
	
	var s3 = new Slider(document.getElementById("slider3"), document.getElementById("s3input"));
	s3.setMinimum(0);
	s3.setMaximum(100);
	s3.onchange = function () {
		document.getElementById("s3value").value = s3.getValue();
	};
	s3.setValue(0);

	var s4 = new Slider(document.getElementById("slider4"), document.getElementById("s4input"));
	s4.setMinimum(0);
	s4.setMaximum(100);
	s4.onchange = function () {
		document.getElementById("s4value").value = s4.getValue();
	};
	s4.setValue(0);
	
	var s5 = new Slider(document.getElementById("slider5"), document.getElementById("s5input"));
	s5.setMinimum(0);
	s5.setMaximum(100);
	s5.onchange = function () {
		document.getElementById("s5value").value = s5.getValue();
	};
	s5.setValue(0);

	var s6 = new Slider(document.getElementById("slider6"), document.getElementById("s6input"));
	s6.setMinimum(0);
	s6.setMaximum(100);
	s6.onchange = function () {
		document.getElementById("s6value").value = s6.getValue();
	};
	s6.setValue(0);
		
	var s7 = new Slider(document.getElementById("slider7"), document.getElementById("s7input"));
	s7.setMinimum(0);
	s7.setMaximum(100);
	s7.onchange = function () {
		document.getElementById("s7value").value = s7.getValue();
	};
	s7.setValue(0);

	var s8 = new Slider(document.getElementById("slider8"), document.getElementById("s8input"));
	s8.setMinimum(0);
	s8.setMaximum(100);
	s8.onchange = function () {
		document.getElementById("s8value").value = s8.getValue();
	};
	s8.setValue(0);
	
	var s9 = new Slider(document.getElementById("slider9"), document.getElementById("s9input"));
	s9.setMinimum(0);
	s9.setMaximum(100);
	s9.onchange = function () {
		document.getElementById("s9value").value = s9.getValue();
	};
	s9.setValue(0);

	var s10 = new Slider(document.getElementById("slider10"), document.getElementById("s10input"));
	s10.setMinimum(0);
	s10.setMaximum(100);
	s10.onchange = function () {
		document.getElementById("s10value").value = s10.getValue();
	};
	s10.setValue(0);
	