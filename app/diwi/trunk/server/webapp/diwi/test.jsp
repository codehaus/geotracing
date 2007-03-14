<%
	//Configure querystring mapping here
	String [][] mappings = new String [][] {
		{"test", "testresponse/route1.xml"},
		{"test2", "testresponse/route2.xml"},	
		{"heide.*", "testresponse/generateroute1.xml"},		
		{"bos.*", "testresponse/generateroute2.xml"}		
	};


	//The code that reads and returns the appropriate content
	if(request.getQueryString() != null) {
		for(int i=0; i < mappings.length;i++) {
			if(java.util.regex.Pattern.matches(mappings[i][0], request.getQueryString())) {
			    response.setContentType("text/xml;charset=utf-8");
	        	pageContext.include(mappings[i][1]);	
			} 
		}
	} else {
		out.println("No query string");
	}
%>