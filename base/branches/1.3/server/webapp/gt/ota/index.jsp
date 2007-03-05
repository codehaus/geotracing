<% response.setContentType("application/xhtml+xml"); %>
<!DOCTYPE html PUBLIC "-//WAPFORUM//DTD XHTML Mobile 1.0//EN" "http://www.wapforum.org/DTD/xhtml-mobile10.dtd">
<html>
<head>
<title>Install MobiTracer</title>
<link rel="stylesheet" href="style.css" type="text/css" />
</head>
<body>
<form method="get" name="mt" action="mt.jsp">
Username
<input type="text" name="u" size="8" />
<br/>
Password
<input type="text" name="p" size="8" />
<input type="hidden" name="j" value=".jad" />
<br/>
<input type="submit" name="s" value="Install" />
</form>

</body>
</html>
