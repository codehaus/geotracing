<%@ page import="org.keyworx.amuse.client.web.HttpConnector" %>
<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ page import="org.keyworx.oase.api.Oase" %>
<%@ page import="org.keyworx.oase.api.OaseSession" %>
<%@ page import="org.keyworx.utopia.core.data.Person" %>
<%@ page import="org.keyworx.oase.api.Record" %>
<%@ page import="java.util.Vector" %>
<%
    System.out.println("dbg 1");
    String personId = request.getParameter("person");
    System.out.println("Person: " + personId);
    System.out.println("dbg 2");

    HttpConnector.login(session, "diwi", "geoapp", "user", "geoapp-user", "user", null);
    System.out.println("dbg 3");
    // first get the users
    OaseSession oase = Oase.createSession("diwi");
    Record[] people = oase.getFinder().readAll(Person.TABLE_NAME);
    System.out.println("dbg 4");

    JXElement rsp = new JXElement("");
    if(personId!=null && personId.length()>0){
        JXElement req = new JXElement("user-get-stats-req");
        req.setAttr("id", personId);
        rsp = HttpConnector.executeRequest(session, req);
        System.out.println(new String(rsp.toBytes(false)));
    }
%>
<html>
    <head>
        <title>Digitale Wichelroede Admin</title>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    </head>
    <body>
        <p>Selecteer een gebruiker</p>
        <p>
            <form id="userform" name="userform" method="post" action="">
                <select name="person">
                <%
                for(int i=0;i<people.length;i++){
                    Record person = people[i];
                    int id = person.getId();
                    String name = person.getStringField(Person.FIRSTNAME_FIELD);
                    name += " ";
                    name += person.getStringField(Person.LASTNAME_FIELD);
                    %><option value="<%=id%>"><%=name%></option><%
                }
                %>
                </select>
                <input type="submit" name="Submit" value="Ok" />
            </form>
        </p>
        <p>
            <%
                JXElement person = rsp.getChildByTag("person");
                if (person != null) {
                    Vector trips = person.getChildrenByTag("trip");
            %>
            <%--<%=new String(rsp.toEscapedString())%>--%>
            <table>
                <tr>
                    <td><strong>persoonsgegevens</strong></td>
                    <td></td>
                </tr>
                <tr>
                    <td>voornaam</td>
                    <td><%=person.getChildText(Person.FIRSTNAME_FIELD)%></td>
                </tr>
                <tr>
                    <td>achternaam</td>
                    <td><%=person.getChildText(Person.LASTNAME_FIELD)%></td>
                </tr>
                <tr>
                    <td>straat</td>
                    <td><%=person.getChildText(Person.STREET_FIELD)%></td>
                </tr>
                <tr>
                    <td>nr</td>
                    <td><%=person.getChildText(Person.STREETNR_FIELD)%></td>
                </tr>
                <tr>
                    <td>postcode</td>
                    <td><%=person.getChildText(Person.ZIPCODE_FIELD)%></td>
                </tr>
                <tr>
                    <td>stad</td>
                    <td><%=person.getChildText(Person.CITY_FIELD)%></td>
                </tr>
                <tr>
                    <td>mobiel</td>
                    <td><%=person.getChildText(Person.MOBILENR_FIELD)%></td>
                </tr>
                <tr>
                    <td>telefoon</td>
                    <td><%=person.getChildText(Person.PHONENR_FIELD)%></td>
                </tr>
                <tr>
                    <td>email</td>
                    <td><%=person.getChildText(Person.EMAIL_FIELD)%></td>
                </tr>
                <tr>
                    <td><strong>aantal wandelingen</strong></td>
                    <td><%=trips.size()%></td>
                </tr>
            </table>
            <%
                }
            %>

        </p>
    </body>
</html>
