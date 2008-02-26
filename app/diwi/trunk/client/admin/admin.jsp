<%@ page import="org.keyworx.amuse.client.web.HttpConnector" %>
<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ page import="org.keyworx.oase.api.Oase" %>
<%@ page import="org.keyworx.oase.api.OaseSession" %>
<%@ page import="org.keyworx.utopia.core.data.Person" %>
<%@ page import="org.keyworx.oase.api.Record" %>
<%@ page import="java.util.Vector" %>
<%@ page import="nl.diwi.util.Constants" %>
<%@ page import="org.keyworx.oase.util.XML" %>
<%@ page import="nl.justobjects.jox.parser.JXBuilder" %>
<%@ page import="org.keyworx.common.util.Sys" %>

<%!
    int nrOfRoamActions = 0;
    int nrOfRouteActivations = 0;
    int nrOfRouteDeactivations = 0;
    int nrOfMediaUploads = 0;
    int nrOfMapLoads = 0;
    int nrOfApplicationStarts = 0;
    int nrOfApplicationShutdowns = 0;
    int nrOfUGCOnActions = 0;
    int nrOfPOIHits = 0;
    int nrOfPOIsRequested = 0;
    int nrOfRouteListRequested = 0;
    int nrOfRouteDetailsRequested = 0;
    int nrOfRoutesHomeRequested = 0;
    int nrOfRoutesGenerated = 0;
    int nrOfRouteListRequestedWeb = 0;
    int nrOfRouteDetailsRequestedWeb = 0;
    int nrOfTripListRequested = 0;
    int nrOfTripDetailsRequestedWeb = 0;
    String allLogs = "";
    String personLog = "";

%>
<%


    String msg = "";
    String personId = request.getParameter("person");
    String xml = request.getParameter("xml");
    /*String sync = request.getParameter("sync");*/
    System.out.println("Person: " + personId);
    /*System.out.println("Sync: " + sync);*/

    String portalName = "diwi";
    Record[] people = new Record[0];
    JXElement getStatRsp = new JXElement("");
    try{
        JXElement rsp = HttpConnector.login(session, portalName, "geoapp", "user", "geoapp-user", "user", null);
        System.out.println(new String(rsp.toBytes(false)));

        // get the stats overview
        /*JXElement getAllStatReq = new JXElement("user-get-allstats-req");
        JXElement getAllStatRsp = HttpConnector.executeRequest(session, getAllStatReq);*/

        // first get the users
        OaseSession oase = Oase.createSession(portalName);
        /*String query = "SELECT * from " + Person.TABLE_NAME + " WHERE firstname NOT LIKE '%geoapp%' AND firstname NOT LIKE '%admin%'";
        Record[] people = oase.getFinder().freeQuery(query);*/
        people = oase.getFinder().readAll(Person.TABLE_NAME);
        AllStats allStats = new AllStats(people, session);

        if (xml != null && xml.length() > 0) {
            JXElement registerRsp = HttpConnector.executeRequest(session, new JXBuilder().build(xml));
            System.out.println(new String(registerRsp.toBytes(false)));
            if(registerRsp.getTag().indexOf("-nrsp")!=-1){
                msg += "Exception registering - " + registerRsp.getAttr("details") + "\n";
            }
        }
        
        if (personId != null && personId.length() > 0) {
            JXElement getStatReq = new JXElement("user-get-stats-req");
            getStatReq.setAttr("id", personId);
            getStatRsp = HttpConnector.executeRequest(session, getStatReq);
            System.out.println(new String(getStatRsp.toBytes(false)));
        }

    }catch(Throwable t){
        msg = "Exception - " + t.getMessage();
    }

%>
<%!
    private class Stat{
        Vector roamMsgs = new Vector(0);
        Vector activateRouteMsgs = new Vector(0);
        Vector deactivateRouteMsgs = new Vector(0);
        Vector addMediaMsgs = new Vector(0);
        Vector getMapMsgs = new Vector(0);
        Vector startNavMsgs = new Vector(0);
        Vector stopNavMsgs = new Vector(0);
        Vector UGCOnMsgs = new Vector(0);
        Vector poiHitMsgs = new Vector(0);
        Vector poiGetMsgs = new Vector(0);
        Vector routeGetListMsgs = new Vector(0);
        Vector routeGetMsgs = new Vector(0);
        Vector routeHomeMsgs = new Vector(0);

        Vector generateRouteMsgs = new Vector(0);
        Vector routeGetListMsgs2 = new Vector(0);
        Vector routeGetMsgs2 = new Vector(0);
        Vector tripGetListMsgs = new Vector(0);
        Vector tripGetMsgs = new Vector(0);

        Stat(JXElement aStatElement){

            ////////////////////////////////////////////////////////////////////////
            // Mobile log
            ////////////////////////////////////////////////////////////////////////

            Vector trips = aStatElement.getChildrenByTag(Constants.LOG_MOBILE_TYPE);
            personLog = new String(aStatElement.toBytes(false));
            try{
                Sys.string2File("/var/keyworx/webapps/diwi/diwi/personlog.txt", personLog);
            }catch(Throwable t){
                System.out.println("Exception creating log:" + t.toString());
            }
            for (int i = 0; i < trips.size(); i++) {

                JXElement trip = (JXElement) trips.elementAt(i);

                roamMsgs.addAll(trip.getChildrenByTag("msg"));
                activateRouteMsgs.addAll(trip.getChildrenByTag("nav-activate-route-req"));
                deactivateRouteMsgs.addAll(trip.getChildrenByTag("nav-deactivate-route-req"));
                addMediaMsgs.addAll(trip.getChildrenByTag("nav-add-medium-req"));
                getMapMsgs.addAll(trip.getChildrenByTag("nav-get-map-req"));
                startNavMsgs.addAll(trip.getChildrenByTag("nav-start-req"));
                stopNavMsgs.addAll(trip.getChildrenByTag("nav-stop-req"));
                UGCOnMsgs.addAll(trip.getChildrenByTag("nav-ugc-on-req"));
                poiHitMsgs.addAll(trip.getChildrenByTag("poi-hit"));
                poiGetMsgs.addAll(trip.getChildrenByTag("nav-poi-get-req"));
                routeGetListMsgs.addAll(trip.getChildrenByTag("nav-route-getlist-req"));
                routeGetMsgs.addAll(trip.getChildrenByTag("nav-route-get-req"));
                routeHomeMsgs.addAll(trip.getChildrenByTag("nav-route-home-req"));

            }

            ////////////////////////////////////////////////////////////////////////
            // Web log
            ////////////////////////////////////////////////////////////////////////

            Vector webVistits = aStatElement.getChildrenByTag(Constants.LOG_WEB_TYPE);

            for (int i = 0; i < webVistits.size(); i++) {
                JXElement webVistit = (JXElement) webVistits.elementAt(i);
                generateRouteMsgs.addAll(webVistit.getChildrenByTag("route-generate-req"));
                routeGetListMsgs2.addAll(webVistit.getChildrenByTag("route-get-req"));
                routeGetMsgs2.addAll(webVistit.getChildrenByTag("route-getlist-req"));
                tripGetListMsgs.addAll(webVistit.getChildrenByTag("trip-getlist-req"));
                tripGetMsgs.addAll(webVistit.getChildrenByTag("trip-get-req"));
            }
        }
    }

    private class AllStats{

        AllStats(Record[] thePeople, HttpSession aSession){

            for(int j=0;j<thePeople.length;j++){
                Record person = thePeople[j];
                JXElement getStatReq = new JXElement("user-get-stats-req");
                getStatReq.setAttr("id", person.getId());
                JXElement getStatRsp = HttpConnector.executeRequest(aSession, getStatReq);
                allLogs += new String(getStatRsp.toBytes(false));
                ////////////////////////////////////////////////////////////////////////
                // Mobile log
                ////////////////////////////////////////////////////////////////////////

                Vector trips = getStatRsp.getChildByTag("person").getChildrenByTag(Constants.LOG_MOBILE_TYPE);
                for (int i = 0; i < trips.size(); i++) {

                    JXElement trip = (JXElement) trips.elementAt(i);

                    nrOfRoamActions += trip.getChildrenByTag("msg").size();
                    nrOfRouteActivations += trip.getChildrenByTag("nav-activate-route-req").size();
                    nrOfRouteDeactivations += trip.getChildrenByTag("nav-deactivate-route-req").size();
                    nrOfMediaUploads += trip.getChildrenByTag("nav-add-medium-req").size();
                    nrOfMapLoads += trip.getChildrenByTag("nav-get-map-req").size();
                    nrOfApplicationStarts += trip.getChildrenByTag("nav-start-req").size();
                    nrOfApplicationShutdowns += trip.getChildrenByTag("nav-stop-req").size();
                    nrOfUGCOnActions += trip.getChildrenByTag("nav-ugc-on-req").size();
                    nrOfPOIHits += trip.getChildrenByTag("poi-hit").size();
                    nrOfPOIsRequested += trip.getChildrenByTag("nav-poi-get-req").size();
                    nrOfRouteListRequested += trip.getChildrenByTag("nav-route-getlist-req").size();
                    nrOfRouteDetailsRequested += trip.getChildrenByTag("nav-route-get-req").size();
                    nrOfRoutesHomeRequested += trip.getChildrenByTag("nav-route-home-req").size();
                }

                ////////////////////////////////////////////////////////////////////////
                // Web log
                ////////////////////////////////////////////////////////////////////////

                Vector webVistits = getStatRsp.getChildByTag("person").getChildrenByTag(Constants.LOG_WEB_TYPE);

                for (int i = 0; i < webVistits.size(); i++) {
                    JXElement webVistit = (JXElement) webVistits.elementAt(i);
                    nrOfRoutesGenerated += webVistit.getChildrenByTag("route-generate-req").size();
                    nrOfRouteDetailsRequestedWeb += webVistit.getChildrenByTag("route-get-req").size();
                    nrOfRouteListRequestedWeb += webVistit.getChildrenByTag("route-getlist-req").size();
                    nrOfTripListRequested += webVistit.getChildrenByTag("trip-getlist-req").size();
                    nrOfTripDetailsRequestedWeb += webVistit.getChildrenByTag("trip-get-req").size();
                }
            }
            try{
                Sys.string2File("/var/keyworx/webapps/diwi/diwi/alllogs.txt", allLogs);
            }catch(Throwable t){
                System.out.println("Exception creating log:" + t.toString());
            }
        }

    }
%>

<html>
    <head>
        <title>Digitale Wichelroede Admin</title>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
    </head>
    <body>
        <%--<h1>Sync Media</h1>
        <p>
            <form id="mediaform" name="mediaform" method="post" action="">
                <input type="hidden" name="sync" value="sync" />
                <input type="submit" name="Submit" value="Sync Media" />
            </form>
        </p>--%>
        <h1>Digitale Wichelroede statistieken</h1>
        <%if(msg.length()>0){%>
            <h1><%=msg%></h1>
        <%}%>

        <!--        
        int nrOfRoamActions = 0;
        int nrOfRouteActivations = 0;
        int nrOfRouteDeactivations = 0;
        int nrOfMediaUploads = 0;
        int nrOfMapLoads = 0;
        int nrOfApplicationStarts = 0;
        int nrOfApplicationShutdowns = 0;
        int nrOfUGCOnActions = 0;
        int nrOfPOIHits = 0;
        int nrOfPOIsRequested = 0;
        int nrOfRouteListRequested = 0;
        int nrOfRouteDetailsRequested = 0;
        int nrOfRoutesHomeRequested = 0;
        int nrOfRoutesGenerated = 0;
        int nrOfRouteListRequestedWeb = 0;
        int nrOfRouteDetailsRequestedWeb = 0;
        int nrOfTripListRequested = 0;
        int nrOfTripDetailsRequestedWeb = 0;
        -->
        <table width="400" border="0">
            <tr><td>nr Of Roam Actions</td><td><%=nrOfRoamActions%></td></tr>
            <tr><td>nr Of Route Activations</td><td><%=nrOfRouteActivations%></td></tr>
            <tr><td>nr Of Route Deactivations</td><td><%=nrOfRouteDeactivations%></td></tr>
            <tr><td>nr Of Media Uploads</td><td><%=nrOfMediaUploads%></td></tr>
            <tr><td>nr Of Map Loads</td><td><%=nrOfMapLoads%></td></tr>
            <tr><td>nr Of Application Starts</td><td><%=nrOfApplicationStarts%></td></tr>
            <tr><td>nr Of Application Shutdowns</td><td><%=nrOfApplicationShutdowns%></td></tr>
            <tr><td>nr Of UGC On Actions</td><td><%=nrOfUGCOnActions%></td></tr>
            <tr><td>nr Of POIHits</td><td><%=nrOfPOIHits%></td></tr>
            <tr><td>nr Of POIs Requested</td><td><%=nrOfPOIsRequested%></td></tr>
            <tr><td>nr Of RouteList Requested</td><td><%=nrOfRouteListRequested%></td></tr>
            <tr><td>nr Of Route Details Requested</td><td><%=nrOfRouteDetailsRequested%></td></tr>
            <tr><td>nr Of Routes Home Requested</td><td><%=nrOfRoutesHomeRequested%></td></tr>
            <tr><td>nr Of Routes Generated</td><td><%=nrOfRoutesGenerated%></td></tr>
            <tr><td>nr Of RouteList Requested Web</td><td><%=nrOfRouteListRequestedWeb%></td></tr>
            <tr><td>nr Of TripList Requested</td><td><%=nrOfTripListRequested%></td></tr>
            <tr><td>nr Of Trip Details Requested Web</td><td><%=nrOfTripDetailsRequestedWeb%></td></tr>
            <tr><td colspan="2"><a href="alllogs.txt" target="_blank">download the log file</a></td></tr> 
        </table>
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
                JXElement person = getStatRsp.getChildByTag("person");
                if (person != null) {
                    Stat stat = new Stat(person);
            %>
            <%--<%=new String(rsp.toEscapedString())%>--%>
            <table width="400" border="0">
                <tr>
                    <td colspan="2"><hr /></td>                    
                </tr>
                <tr>
                    <td colspan="2"><h2>persoonsgegevens</h2></td>
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
                    <td colspan="2"><hr /></td>
                </tr>
                <tr>
                    <td colspan="2"><h2>Mobiel gedrag</h2></td>
                </tr>
                <tr>
                    <td><strong>aantal wandelingen</strong></td>
                    <td><%=stat.tripGetListMsgs.size()%></td>
                </tr>
                <tr>
                    <td><strong>aantal malen gekozen voor struinen</strong></td>
                    <td><%=stat.roamMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.roamMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.roamMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal malen gekozen voor route</strong></td>
                    <td><%=stat.activateRouteMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.activateRouteMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.activateRouteMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal routes afgesloten</strong></td>
                    <td><%=stat.deactivateRouteMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.deactivateRouteMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.deactivateRouteMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>hoeveelheid media toegevoegd</strong></td>
                    <td><%=stat.addMediaMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.addMediaMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.addMediaMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal kaarten opgehaald op pda</strong></td>
                    <td><%=stat.getMapMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.getMapMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.getMapMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren applicatie opgestart</strong></td>
                    <td><%=stat.startNavMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.startNavMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.startNavMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren applicatie (expliciet) afgesloten</strong></td>
                    <td><%=stat.stopNavMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.stopNavMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.stopNavMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren van 'volksmond' aan/uit gezet</strong></td>
                    <td><%=stat.UGCOnMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.UGCOnMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.UGCOnMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren op poi gestuit</strong></td>
                    <td><%=stat.poiHitMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.poiHitMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.poiHitMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren poi gezien</strong></td>
                    <td><%=stat.poiGetMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.poiGetMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.poiGetMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren routelijst bekijken</strong></td>
                    <td><%=stat.routeGetListMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.routeGetListMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.routeGetListMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren route details bekijken</strong></td>
                    <td><%=stat.routeGetMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.routeGetMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.routeGetMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren route terug gegenereerd</strong></td>
                    <td><%=stat.routeHomeMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.routeHomeMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.routeHomeMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td colspan="2"><hr /></td>
                </tr>
                <tr>
                    <td colspan="2"><h2>Web gedrag</h2></td>
                </tr>
                <tr>
                    <td><strong>aantal keren routelijst bekijken</strong></td>
                    <td><%=stat.routeGetListMsgs2.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.routeGetListMsgs2.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.routeGetListMsgs2.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren route details bekijken</strong></td>
                    <td><%=stat.routeGetMsgs2.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.routeGetMsgs2.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.routeGetMsgs2.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren eigen route gegenereerd</strong></td>
                    <td><%=stat.generateRouteMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.generateRouteMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.generateRouteMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren wandelinglijst bekijken</strong></td>
                    <td><%=stat.tripGetListMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.tripGetListMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.tripGetListMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td><strong>aantal keren wandeling afspelen</strong></td>
                    <td><%=stat.tripGetMsgs.size()%></td>
                </tr>
                <%--<%for(int i=0;i<stat.tripGetMsgs.size();i++){
                    %><tr><td colspan="2"><%=((JXElement)stat.tripGetMsgs.elementAt(i)).getAttr("date")%></td></tr><%
                }%>--%>
                <tr>
                    <td colspan="2"><a href="personlog.txt" target="_blank">download the log file</a></td>
                </tr>
                <tr>
                    <td colspan="2"><hr /></td>
                </tr>
            </table>

        </p>        
        <%
        }
        %>
    </body>
</html>
