<%
    String action = request.getParameter("action");
    String xml = request.getParameter("xml");    
%>

<html>
    <head>
        <title>KICH POST page tester</title>        
    </head>
    <body>
        <p><%if(xml!=null && xml.length()>0){%><%=xml%><%}%></p>
    </body>
</html>