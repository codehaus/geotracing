<%@ page import="org.keyworx.amuse.client.web.HttpConnector" %>
<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ page import="org.keyworx.oase.api.Oase" %>
<%@ page import="org.keyworx.oase.api.OaseSession" %>
<%@ page import="org.keyworx.utopia.core.data.Person" %>
<%@ page import="org.keyworx.oase.api.Record" %>
<%@ page import="java.util.Vector" %>
<%@ page import="nl.diwi.util.Constants" %>

<%
    String personId = request.getParameter("person");
    String sync = request.getParameter("sync");
    System.out.println("Person: " + personId);
    System.out.println("Sync: " + sync);

    HttpConnector.login(session, "diwi", "geoapp", "user", "geoapp-user", "user", null);

    // first get the users

    OaseSession oase = Oase.createSession("diwi");
    String query = "SELECT * from " + Person.TABLE_NAME + " WHERE firstname NOT LIKE '%geoapp%' AND firstname NOT LIKE '%admin%'";
    //Record[] people = oase.getFinder().freeQuery(query);
    Record[] people = oase.getFinder().readAll(Person.TABLE_NAME);

    JXElement rsp = new JXElement("");
    if(personId!=null && personId.length()>0){
        JXElement req = new JXElement("user-get-stats-req");
        req.setAttr("id", personId);
        rsp = HttpConnector.executeRequest(session, req);
        System.out.println(new String(rsp.toBytes(false)));
    }
    
    if(sync!=null && sync.length()>0){
        JXElement syncReq = new JXElement("kich-sync-media-req");
        System.out.println("go and sync man!!!");
        JXElement syncRsp = HttpConnector.executeRequest(session, syncReq);
        System.out.println(new String(syncRsp.toBytes(false)));
    }
%>

<html>
    <head>
        <title>Digitale Wichelroede Admin</title>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    </head>
    <body>
        <h1>Sync Media</h1>
        <p>
            <form id="mediaform" name="mediaform" method="post" action="">
                <input type="hidden" name="sync" value="sync" />
                <input type="submit" name="Submit" value="Sync Media" />
            </form>
        </p>
        <h1>Selecteer een gebruiker</h1>
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

                    ////////////////////////////////////////////////////////////////////////
                    // Mobile log
                    ////////////////////////////////////////////////////////////////////////

                    Vector trips = person.getChildrenByTag(Constants.LOG_MOBILE_TYPE);

                    Vector roamMsgs = new Vector(0);
                    Vector activateRouteMsgs = new Vector(0);
                    Vector deactivateRouteMsgs = new Vector(0);
                    Vector addMediaMsgs = new Vector(0);
                    Vector getMapMsgs = new Vector(0);
                    Vector startNavMsgs = new Vector(0);
                    Vector stopNavMsgs = new Vector(0);
                    Vector toggleUGCMsgs = new Vector(0);
                    Vector poiHitMsgs = new Vector(0);
                    Vector poiGetMsgs = new Vector(0);
                    Vector routeGetListMsgs = new Vector(0);
                    Vector routeGetMsgs = new Vector(0);
                    Vector routeHomeMsgs = new Vector(0);


                    for (int i = 0; i < trips.size(); i++) {
                        JXElement trip = (JXElement) trips.elementAt(i);

                        roamMsgs.addAll(trip.getChildrenByTag("msg"));
                        activateRouteMsgs.addAll(trip.getChildrenByTag("nav-activate-route-req"));
                        deactivateRouteMsgs.addAll(trip.getChildrenByTag("nav-deactivate-route-req"));
                        addMediaMsgs.addAll(trip.getChildrenByTag("nav-add-medium-req"));
                        getMapMsgs.addAll(trip.getChildrenByTag("nav-get-map-req"));
                        startNavMsgs.addAll(trip.getChildrenByTag("nav-start-req"));
                        stopNavMsgs.addAll(trip.getChildrenByTag("nav-stop-req"));
                        toggleUGCMsgs.addAll(trip.getChildrenByTag("nav-toggle-ugc-req"));
                        poiHitMsgs.addAll(trip.getChildrenByTag("poi-hit"));
                        poiGetMsgs.addAll(trip.getChildrenByTag("nav-poi-get-req"));
                        routeGetListMsgs.addAll(trip.getChildrenByTag("nav-route-getlist-req"));
                        routeGetMsgs.addAll(trip.getChildrenByTag("nav-route-get-req"));
                        routeHomeMsgs.addAll(trip.getChildrenByTag("nav-route-home-req"));                        
                    }

                    ////////////////////////////////////////////////////////////////////////
                    // Web log
                    ////////////////////////////////////////////////////////////////////////

                    Vector webVistits = person.getChildrenByTag(Constants.LOG_WEB_TYPE);

                    Vector generateRouteMsgs = new Vector(0);
                    Vector routeGetListMsgs2 = new Vector(0);
                    Vector routeGetMsgs2 = new Vector(0);
                    Vector tripGetListMsgs = new Vector(0);
                    Vector tripGetMsgs = new Vector(0);

                    for (int i = 0; i < webVistits.size(); i++) {
                        JXElement webVistit = (JXElement) webVistits.elementAt(i);
                        generateRouteMsgs.addAll(webVistit.getChildrenByTag("route-generate-req"));
                        routeGetListMsgs2.addAll(webVistit.getChildrenByTag("route-get-req"));
                        routeGetMsgs2.addAll(webVistit.getChildrenByTag("route-getlist-req"));
                        tripGetListMsgs.addAll(webVistit.getChildrenByTag("route-getlist-req"));
                        tripGetMsgs.addAll(webVistit.getChildrenByTag("route-get-req"));
                    }
            %>
            <%--<%=new String(rsp.toEscapedString())%>--%>
            <table>
                <tr>
                    <td><h2>persoonsgegevens</h2></td>
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
                    <td colspan="2"><h2>Mobiel gedrag</h2></td>
                </tr>
                <tr>
                    <td><strong>aantal wandelingen</strong></td>
                    <td><%=trips.size()%></td>
                </tr>
                <tr>
                    <td><strong>aantal malen gekozen voor struinen</strong></td>
                    <td><%=roamMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<roamMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)roamMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal malen gekozen voor route</strong></td>
                    <td><%=activateRouteMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<activateRouteMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)activateRouteMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal routes afgesloten</strong></td>
                    <td><%=deactivateRouteMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<activateRouteMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)activateRouteMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>hoeveelheid media toegevoegd</strong></td>
                    <td><%=addMediaMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<addMediaMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)addMediaMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal kaarten opgehaald op pda</strong></td>
                    <td><%=getMapMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<getMapMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)getMapMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren applicatie opgestart</strong></td>
                    <td><%=startNavMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<startNavMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)startNavMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren applicatie (expliciet) afgesloten</strong></td>
                    <td><%=stopNavMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<stopNavMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stopNavMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren van 'volksmond' aan/uit gezet</strong></td>
                    <td><%=toggleUGCMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<toggleUGCMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)toggleUGCMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren op poi gestuit</strong></td>
                    <td><%=poiHitMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<poiHitMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)poiHitMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren poi gezien</strong></td>
                    <td><%=poiGetMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<poiGetMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)poiGetMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren routelijst bekijken</strong></td>
                    <td><%=routeGetListMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<routeGetListMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)routeGetListMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren route details bekijken</strong></td>
                    <td><%=routeGetMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<routeGetMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)routeGetMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren route terug gegenereerd</strong></td>
                    <td><%=routeHomeMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<routeHomeMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)routeHomeMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td colspan="2"><h2>Web gedrag</h2></td>
                </tr>
                <tr>
                    <td><strong>aantal keren routelijst bekijken</strong></td>
                    <td><%=routeGetListMsgs2.size()%></td>
                </tr>
                <%for(int i=0;i<routeGetListMsgs2.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)routeGetListMsgs2.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren route details bekijken</strong></td>
                    <td><%=routeGetMsgs2.size()%></td>
                </tr>
                <%for(int i=0;i<routeGetMsgs2.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)routeGetMsgs2.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren eigen route gegenereerd</strong></td>
                    <td><%=generateRouteMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<generateRouteMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)generateRouteMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren wandelinglijst bekijken</strong></td>
                    <td><%=tripGetListMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<tripGetListMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)tripGetListMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
                <tr>
                    <td><strong>aantal keren wandeling afspelen</strong></td>
                    <td><%=tripGetMsgs.size()%></td>
                </tr>
                <%for(int i=0;i<tripGetMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)tripGetMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>
            </table>
            <%
                }
            %>

        </p>
    <p>
        <a href="<%=new String(rsp.getChildAt(0).toBytes(false))%>" target="_new">Download the file</a>
    </p>
    </body>
</html>
