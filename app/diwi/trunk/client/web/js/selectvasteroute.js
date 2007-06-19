// JavaScript Document

	var vaste_routes_names = new Array();
	var vaste_routes_description = new Array();
	var vaste_routes_id = new Array();
	var vaste_routes;
	var n_vaste_routes = -1;
	var vaste_routes_string = '';
	
	var namesArray = new Array();
	var descriptionArray = new Array();

	function maakVasteRoutesForm(elm) {
		vaste_routes_string += '<p></p><form><select id="fixed_routes_form" >';
		if (elm != null) {
			var size = elm.length;
			for (counter=0;counter<elm.length;counter++){
				vaste_routes_names[counter] = elm[counter].getField('name');
				vaste_routes_description[counter] = elm[counter].getField('description');
				vaste_routes_id[counter] = elm[counter].id;
			}
			n_vaste_routes = vaste_routes_names.length;
			var j;
			for (i = 0; i < n_vaste_routes; i++){ 
				j = i+1;
				vaste_routes_string += '<option value=' + vaste_routes_id[i] + ' onClick="toonSelectedVasteRoute()">';
				vaste_routes_string += vaste_routes_names[i];
				vaste_routes_string += '</option>';
			}
		}
		vaste_routes_string += '</select></form>';

		var ni = document.getElementById('vasteroutes-lb');
		ni.innerHTML = vaste_routes_string;
		toonSelectedVasteRoute();
	}


	function toonSelectedVasteRoute() {
 		if (n_vaste_routes == -1){
			TST.getFixedRoutes();
		} else {
			var selected = document.getElementById('fixed_routes_form').value;
			var n = 0;
			
			for (i=0;i<n_vaste_routes; i++) {
				if (selected == vaste_routes_id[i]) n = i;
			}
				
			var selected_route_string = '<H1><strong>' + vaste_routes_names[n];
			selected_route_string += '</strong><br></H1><p>'+ vaste_routes_description[n] + '</p>';
			
			var ni = document.getElementById('vaste_routes_content');
			ni.innerHTML = selected_route_string;
		}
	}
	
