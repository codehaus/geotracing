<%@ include file="model.jsp" %>
<%

	if (model.getState() == MODEL_STATE_NULL) {
		model.set(ATTR_PAGE_URL, request.getRequestURI());

		%>

	   <jsp:forward page="control.jsp" >
			  <jsp:param name="cmd" value="nav-init" />
		   </jsp:forward>
		<%
	}

%>
<%@ include file="static-layout-header.html" %>
  <tr>
	  <td width="170" valign="top" bgcolor="#F5F5F5" class="borderright"><img src="images/logo170.gif"
																			  alt="logo brainport" width="170"
																			  height="133" hspace="0" vspace="0"/>
	  </td>
    <td colspan="3" valign="top" >
    	<div class="right">
        <% if (model.isLoggedIn()) { %><p class="fragmenttekst">Ingelogd als <%= model.getString(ATTR_USER_NAME)%> <a href="control.jsp?cmd=logout">[uitloggen]</a></p><% } %>
      </div>
      <div class="fragmenttekst">
		    <!-- Content starts here -->
		    <jsp:include page="<%= model.getString(ATTR_CONTENT_URL) %>" flush="true"/>
		    <!-- Content ends here -->
      </div>
		<% if(model.getResultMsg().length() !=0) { %>
		<p style="font-size: small; font-style: italic; font-weight:bold;"><%=model.getResultMsg()%></p>
        <% } %>

		</td>
  </tr>

<%@ include file="static-layout-footer.html" %>
