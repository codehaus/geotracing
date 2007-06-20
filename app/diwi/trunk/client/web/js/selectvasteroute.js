// JavaScript Document

var vaste_routes_names = new Array();
var vaste_routes_description = new Array();
var vaste_routes_id = new Array();
var vaste_routes;
var n_vaste_routes = -1;
var vaste_routes_string = '';

var namesArray = new Array();
var descriptionArray = new Array();

function createFixedRoutesForm(records) {
	vaste_routes_string += '<p></p><form><select id="fixed_routes_form" >';
	if (records != null) {
		var i;
		for (i = 0; i < records.length; i++) {
			vaste_routes_names[i] = records[i].getField('name');
			vaste_routes_description[i] = records[i].getField('description');
			vaste_routes_id[i] = records[i].id;
		}
		n_vaste_routes = vaste_routes_names.length;

		for (i = 0; i < n_vaste_routes; i++) {
			vaste_routes_string += '<option value=' + vaste_routes_id[i] + ' onClick="showFixedRoute()">';
			vaste_routes_string += vaste_routes_names[i];
			vaste_routes_string += '</option>';
		}
	}
	vaste_routes_string += '</select></form>';

	var ni = document.getElementById('vasteroutes-lb');
	ni.innerHTML = vaste_routes_string;
}

function showFixedRoute() {
	var selected = document.getElementById('fixed_routes_form').value;
	var n = 0;

	for (i = 0; i < n_vaste_routes; i++) {
		if (selected == vaste_routes_id[i]) {
			n = i;
			break;
		}
	}

	var selected_route_string = '<H1><strong>' + vaste_routes_names[n];
	selected_route_string += '</strong><br></H1><p>' + vaste_routes_description[n] + '</p>';

	ni = document.getElementById('vaste_routes_content');
	ni.innerHTML = selected_route_string;
}
	
