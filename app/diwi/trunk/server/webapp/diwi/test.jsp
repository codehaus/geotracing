<%
	String [][] mappings = new String [][] {
		{"test", "testresponse/route1.xml"},
		{"test2", "testresponse/route2.xml"},	
		{"bos=40&heide=20&bebouwing=10&theme=forts&activity=wandelaar", "testresponse/generateroutes.xml"}		
	};

	for(int i=0; i < mappings.length;i++) {
		if(mappings[i][0].equals(request.getQueryString())) {
		    response.setContentType("text/xml;charset=utf-8");
        	pageContext.include(mappings[i][1]);	
		}
	}		
%>