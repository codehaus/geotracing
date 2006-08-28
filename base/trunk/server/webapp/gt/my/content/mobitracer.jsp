<%@ page import="org.keyworx.amuse.core.Container"%>
<%@ include file="../model.jsp" %>
<%
	String webAppURL  =  request.getRequestURL().toString().split("/my")[0];
	String mtURL = webAppURL + "/ota/mt.jsp?u=" + model.getString(ATTR_USER_NAME) + "&amp;p=&lt;your password&gt;";

	String mediaEmail = "";
	try {
		Container geoApp = (Container) Amuse.server.getPortal().getComponent("geoapp");
		mediaEmail = geoApp.getComponent("emailupload").getProperty("email");
	} catch (Throwable t) {
		mediaEmail = "cannot determine (config problem?) err=" + t;
	}
%>
<p>
	The MobiTracer is a Java (J2ME) application that runs on the phone. Note that you need a Bluetooth
	GPS module to run this application.
</p>
<p>
	You can download and install your personalized MobiTracer on your phone with the following URL:
</p>
<pre>
	<strong><%= mtURL %></strong>
</pre>
<p>
	Note that <code>&lt;your password&gt;</code> must be the password through which you logged in here. 

	Most phones have an "Application Manager" where you can enter the URL. Some phones may also
	allow installation through their web-browser. Best is to make a bookmark of the download URL
	such that you can quickly install new versions. After installation you should modify the
	default application
	settings (Network access, Connectivity and Multimedia) to prevent pop-ups while running MobiTracer.
</p>
<h3>Sending Media</h3>
<p>
	The preferred way to submit media (photos/3gp movies) is through email. Use the following email
	adress to send media to:
</p>
<pre>
	<strong><%= mediaEmail %></strong>
</pre>
<p>
	When emailing media from your mobile phone or any other system
	(e.g. photo's taken with your digital camera), your sending address must
	match one of the emails you provided when registering.
</p>
<h3>Help</h3>
<p>
	See <a href="http://www.geotracing.com/mobitracer.html">www.geotracing.com/mobitracer.html</a>
	and <a href="http://www.geotracing.com/wiki/index.php?title=GeoTracing:Apps:MobiTracer">the GeoTracing Wiki</a>
	for further instructions.
</p>
