<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ page import="org.keyworx.amuse.core.Amuse" %>
<%@ page import="org.keyworx.amuse.client.web.HttpConnector" %>
<%@ page import="org.keyworx.utopia.core.data.Role" %>
<%
    boolean success;
    String msg = "";
    String code = request.getParameter("code");
    if(code!=null && code.length() == 0){
        JXElement rsp = HttpConnector.login(session, Amuse.server.getPortal().getId(), "geoapp", Role.USER_ROLE_VALUE, "wp-user", "user", null);
        rsp = HttpConnector.selectApp(session, "walkandplay", Role.USER_ROLE_VALUE);
        if(rsp!=null && rsp.getTag().indexOf("-rsp")!=-1){
            JXElement req = new JXElement("profile-activate-req");
            req.setAttr("code", code);
            rsp = HttpConnector.executeRequest(session, req);
            if(rsp!=null && rsp.getTag().indexOf("nrsp")!=-1){
                success = false;
                msg = "Oeps activation failed!";
            }
        }else{
            success = false;
            msg = "Oeps autologin failed!";
        }
    }else{
        success = false;
        msg = "Oeps no code found!";
    }
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml">
<head>
<title>GeoTracing - walk and play</title>
<link rel="stylesheet" type="text/css" href="css/widget.css"/>
<link rel="stylesheet" type="text/css" href="css/gtwidget.css"/>
<link rel="stylesheet" type="text/css" href="css/gtapp.css"/>
<script type="text/javascript" src="js/dojo-0.3.1-ajax/dojo.js"></script>
<script type="text/javascript" src="lib/ajax-pushlet-client.js"></script>
<!--
<script src="http://maps.google.com/maps?file=api&amp;v=2.48&amp;key=ABQIAAAAAnsKeh1eS56d4nSCvWmbcBSurnAizDIuQPYrT1jN4yvuBVLWkhT_FD84oCbtAcZNS3" type="text/javascript"></script>
-->
<script src="http://maps.google.com/maps?file=api&amp;v=2.48&amp;key=ABQIAAAAgbRczGdUGFScFxOsmC_JghQHBvw4Dj8Sg5rQsz5g3RvuAbjMvRQzK0V9mdSgW9mk-dHwn7fbIh-5yA" type="text/javascript"></script>
<script type="text/javascript" src="lib/DHTML.js"></script>
<script type="text/javascript" src="lib/GTApp.js"></script>
<script type="text/javascript" src="lib/KWClient.js"></script>
<link rel="stylesheet" href="css/geotracingfs.css" />
</head>
<body>
<!-- LOGOBOX -->
<div id="logobox"> <b class="rtop"><b class="r1"></b><b class="r2"></b><b class="r3"></b><b class="r4"></b></b>
  <div class="contentblock"><br />
    <a href="index.html"><img src="img/gt_logo.gif" alt="logo" width="221" border="0"/></a> <img src="img/cpv_v_over.gif" width="265" border="0" usemap="#Map" />
    <map name="Map" id="Map">
      <area shape="rect" coords="2,-1,79,43" href="#" />
      <area shape="rect" coords="90,-1,149,42" href="#" />
      <area shape="rect" coords="199,0,258,43" href="#" />
    </map>
    <ul style="text-align:right;">
      <li> <a href="#"><img src="img/mypageBT.gif" width="58" border="0"/></a> </li>
      <li> <a href="#"><img src="img/aboutBT.gif" width="58" border="0" style="position:relative;left:7px;"/></a> </li>
      <li> <a href="#"><img src="img/signinBT.gif" width="58" border="0"/></a> </li>
      <li> <a href="#"><img src="img/help_bt.gif" width="18" border="0" style="margin-right:20px;"/></a> </li>
    </ul>
    <br />
    <br />
  </div>
  <b class="rbottom"><b class="r4"></b><b class="r3"></b><b class="r2"></b><b class="r1"></b></b> </div>
<!-- /LOGOBOX -->
<!-- CONTENT -->
<div id="formblock" style="text-align:left;z-index:15;"> <b class="rtop"><b class="r1"></b><b class="r2"></b><b class="r3"></b><b class="r4"></b></b>
  <div class="contentblock" style="background-color:#E9E9E9; height:200px;">
    <!--SIGNUP FORM-->
	<%if(success){%>
  		congratulations 
	<%}else{%>
		<%=msg%>
	<%}%>
  </div>
  <b class="rbottom"><b class="r4"></b><b class="r3"></b><b class="r2"></b><b class="r1"></b></b> </div>
<!-- / CONTENT -->
<!-- MAP-->
<div id="map"></div>
<!-- /MAP -->
<!--BORDERS-->
<div id="mapholder">
  <div class="btop"></div>
  <div class="bbottom"></div>
  <div class="bright"></div>
  <div class="bleft"></div>
</div>
<!--/BORDERS-->
<!-- EVENTSHANDLERS -->
<script type="text/javascript" src="js/events.js"></script>
<!-- /EVENTHANDLERS  -->
</body>
</html>
