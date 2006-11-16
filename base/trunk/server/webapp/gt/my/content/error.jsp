<%@ include file="../model.jsp" %>
<h2>Error</h2>
<p>
Bogger, some kind of error occurred while processing your request. This is the last error message.
</p>
<p>
	<strong><%= model.getResultMsg() %></strong>
</p>
<p>
	Now this message could be very cryptic...What to do ? In some cases logging out and then logging in may help.
	If the error persists "contact your administrator". If you don't know him/her just
	contact me: just AT justobjects.nl.
</p>