<%@ page import="org.keyworx.oase.api.Relater" %>
<%@ page import="org.keyworx.oase.api.Finder" %>
<%@ page import="nl.justobjects.jox.dom.JXElement" %>
<%@ include file="model.jsp" %>
<%@ include file="static-layout-header.html" %>
<!-- $Id:$ -->
<tr>
<td width="170" valign="top" bgcolor="#F5F5F5" class="borderright">
	<!--		<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr>
					<td height="20" bgcolor="#B8BEBE"><div class="fragmenttekstwitbold">bekijk de routes van:</div></td>
				</tr>

				<tr>
					<td height="20" class="deelnemertekst"><a href="kentie.html">Peter Kentie </a></td>
				</tr>
				<tr>
					<td height="20" class="deelnemertekst"><a href="edelkoort.html">Lidewij Edelkoort </a></td>
				</tr>
				<tr>
					<td height="20" class="deelnemertekst"><a href="deelenemer3.html">Deelnemer 3</a></td>
				</tr>
				<tr>
					<td height="20" class="deelnemertekst"><a href="deelnemer4.html">Deelnemer 4 </a></td>
				</tr>
				<tr>
					<td height="20" class="deelnemertekst"><a href="deelnemer5.html">Deelnemer 5 </a></td>
				</tr>
				<tr>
					<td height="20" class="deelnemertekst">&nbsp;</td>
				</tr>
				<tr>
					<td height="20" class="deelnemertekst">&nbsp;</td>
				</tr>
				<tr>
					<td height="20" class="deelnemertekst">&nbsp;</td>
				</tr>
			</table> -->
</td>
<td colspan="3" valign="top">
<table width="100%" border="0" cellspacing="0" cellpadding="8">
<tr>
	<td align="left" valign="top">
		<div class="nieuwsberichttitel"><strong>Deelnemers</strong><br/>
		</div>
	</td>
	<td valign="middle" align="right">
		<div class="nieuwsberichttitel"><a href="deelnemersedit.jsp"></a></div>
	</td>
</tr>
<%
	Record[] accountRecords = model.query(/* tables: */ "utopia_account,utopia_role,utopia_person",
			/* fields: */ "utopia_account.id,utopia_person.firstname, utopia_account.loginname, utopia_account.sessionkey",
			/* where:  */ "utopia_role.name = 'user' AND utopia_account.state = 1 and substr(utopia_person.firstname,length(utopia_person.firstname),1)<>'e'",
			/* relations: */ "utopia_account,utopia_role;utopia_account,utopia_person",
			/* postCond: */ "ORDER BY utopia_person.firstname");


	Record accountRecord, personRecord, thumbRecords[];
	Finder finder = model.getOase().getFinder();
	Relater relater = model.getOase().getRelater();
	for (int i = 0; i < accountRecords.length; i++) {
		// User function name (=account.loginname)
		accountRecord = finder.read(accountRecords[i].getId(), "utopia_account");
		String loginName = accountRecord.getStringField("loginname");
//			String punt=accountRecord.getStringField("sessionkey");
//			int publiek=Integer.parseInt(punt.substring(0,1));

		// User image
		personRecord = relater.getRelated(accountRecord, "utopia_person", null)[0];
		String naam = personRecord.getStringField("firstname");
		String imageURL = "img/default-user-thumb.jpg";
		thumbRecords = relater.getRelated(personRecord, "base_medium", "thumb");
		if (thumbRecords.length > 0) {
			imageURL = "media.srv?resize=60x60&id=" + thumbRecords[0].getId();
		}

		// Zet defaults
		String toelichting = "geen toelichting";
		int publiek = 0;
		String locatie = "5.3382,50.9303";

		// Optional extra profile info in extra veld van utopia_person
		// bijv. <profile><publiek>1</publiek><locatie>5.3382,50.9303</locatie></profile>
		JXElement extra = personRecord.getXMLField("extra");
		JXElement next;
		if (extra != null) {
			// Beschrijving deelnemer
			next = extra.getChildByTag("desc");
			if (next != null) {
				toelichting = next.getText();
			}

			// Moeten locaties publiek (1) of niet (0)
			next = extra.getChildByTag("publiek");
			if (next != null) {
				publiek = Integer.parseInt(next.getText().trim());
			}

			// Locatie van deelnemer
			next = extra.getChildByTag("locatie");
			if (next != null) {
				locatie = next.getText();
			}
		}

		// Maak parameter uit publiek + punt
		String punt = publiek + "," + locatie;
		if (i % 2 == 0) { %><tr><!-- i= <%= i%> --><% } %>
	<td valign="top" class="borderleftbottom" width="320"><!-- i= <%= i%> -->
		<!--			    <table width="320" border="0" cellspacing="0" cellpadding="4">
				<tr>
					<td width="75" valign="top" class="borderbottom">
						<a href="map.jsp?cmd=staalkaart&login=<%= loginName %>">
							<img border="0" src="<%= imageURL %>"
								 alt="<%= loginName %>"/></a></td>
					<td width="245" valign="top" bgcolor="#f4f4f4" class="borderbottom">
						<div class="naam">
							<a href="map.jsp?cmd=staalkaart&login=<%= loginName %>"><%=loginName%></a>
						</div>

						<div class="voorsteltekst"><%= toelichting %></div></td>
				</tr>
			    </table>  -->
		<table width="320" border="0" cellspacing="0" cellpadding="0">
			<tr>
				<td height="20" colspan="2" bgcolor="#B8BEBE" class="naam"><%=naam%></td>
			</tr>
			<tr>
				<td width="60" height="60" align="left" valign="top" rowspan="2">
					<% if (publiek == 1) { %>
					<a href="locatie-map.jsp?punt=<%=punt%>&cmd=kaart&user=<%= loginName %>"><img src="<%= imageURL %>"
																								  alt="<%= loginName %>"
																								  border="0" width="60"
																								  height="60"/></a>
					<% } else {%>
					<a href="locatie-map.jsp?punt=<%=punt%>&user=<%= loginName %>"><img src="<%= imageURL %>"
																						alt="<%= loginName %>"
																						border="0" width="60"
																						height="60"/></a>
					<% } %>
				</td>
				<td align="left" valign="top" bgcolor="#eeeeee">
					<div class="voorsteltekst"><%= toelichting %></div>
				</td>
			</tr>
			<tr>
				<td height="20" valign="bottom" bgcolor="#eeeeee">
					<div class="voorsteltekst">
						<div align="right">
							<!-- <a href="locatie-map.jsp?punt=<%=punt%>&cmd=archive&user=<%= loginName %>">Bekijk mijn routes</a>-->
							<% if (publiek == 1) { %>
							<a href="locatie-map.jsp?punt=<%=punt%>&cmd=kaart&user=<%= loginName %>">Bekijk mijn
								punten</a><% } else {%>
							<a href="locatie-map.jsp?punt=<%=punt%>&user=<%= loginName %>">Bekijk mijn punten</a><% } %>
						</div>
					</div>
				</td>
			</tr>
		</table>

	</td>
	<% if (i == accountRecords.length - 1 && (i % 2 == 0)) { %><td>&nbsp;</td><!-- i= <%= i%> --><% } %>
	<% if (i % 2 != 0 || i == accountRecords.length - 1) { %></tr><!-- i= <%= i%> --><% } %>
<% } %>
<!--		 </table>
		</td>
				<table width="100%" border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td height="20" colspan="2" bgcolor="#B8BEBE" class="naam">PETER KENTIE </td>
					</tr>
					<tr>
						<td width="200" height="150" align="left" valign="top"><img
								src="images/deelnemers/peterkentie.jpg" alt="peter kentie" width="200" height="150"/>
						</td>
						<td align="left" valign="top" bgcolor="#eeeeee"><div class="voorsteltekst">Manager marketing &
							media bij PSV in Eindhoven. Opgeleid als grafisch vormgever en marketeer en heeft websites
							gemaakt voor Philips, VNU, DAF Trucks, e.a. Schrijft voor de publicatie Publish over
							internet en vormgeving en in Emerce een maandelijkse column over Technologie ontwikkelingen.
							<br/>

							Grootste bekendheid heeft hij verworven met het boek 'Webdesign in de Praktijk'.</div></td>
					</tr>
					<tr>
						<td height="20" colspan="2" align="left" valign="top"><div class="voorsteltekst"><div
								align="right"><a href="route.html">Bekijk route</a></div></div></td>
					</tr>
				</table>
				<br/>
				<table width="100%" border="0" cellspacing="0" cellpadding="0">
					<tr>
						<td height="20" colspan="2" bgcolor="#B8BEBE" class="naam">lidewij edelkoort </td>
					</tr>
					<tr>
						<td width="200" height="150" align="left" valign="top"><img
								src="images/deelnemers/liedelkoort.jpg" alt="peter kentie" width="200" height="150"/>
						</td>
						<td align="left" valign="top" bgcolor="#eeeeee"><div class="voorsteltekst">Trendgoeroe Lidewij
							Edelkoort (1950) startte, na haar studie mode en ontwerp aan de kunstacademie in Arnhem,
							haar carrière als inkoper van de Bijenkorf. Hier ontdekte ze al gauw haar talent voor het
							aanvoelen van opkomende trends. Inmiddels geeft Li Edelkoort leiding aan twee
							trendwatchbureaus in Parijs en worden haar voorspellingen door talloze bedrijven wereldwijd
							op de voet gevolgd.

						</div></td>
					</tr>
					<tr>
						<td height="20" colspan="2" align="left" valign="top"><div class="voorsteltekst"><div
								align="right"><a href="route.html">Bekijk route</a></div></div></td>
					</tr>
				</table>

			</td>
		</tr>
	</table>
</td>
</tr>
 -->
</table>
</td>
</tr>
<%@ include file="static-layout-footer.html" %>


