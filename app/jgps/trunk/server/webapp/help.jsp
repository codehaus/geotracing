<%@ include file="static-layout-header.html" %>

<tr>
<td width="170" valign="top" bgcolor="#F5F5F5" class="borderright">&nbsp;</td>
<td colspan="3" valign="top">
<table width="100%" border="0" cellspacing="0" cellpadding="0">
<tr>
<td valign="top" width="560">

<div class="nieuwsbericht">
	<div class="nieuwsberichttitel">Help</div>

	<p>
		Dit is de help pagina voor de verschillende onderdelen van
		de website Sense of Brainport.
	</p>
</div>

		<div class="nieuwsbericht">
			<div class="nieuwsberichttitel">Handleiding Mobiel/GPS</div>
			    <table border="0" cellspacing="4" cellpadding="4">
					<tr>
				<td>
					<a href="handleiding-mobiel-gps.pdf">
					<img width="50" height="69" src="images/handleiding.jpg" alt="handleiding"/>
				</a>
				</td>
				<td>
				Voor de deelnemers is hier de
				<a href="handleiding-mobiel-gps.pdf">handleiding voor de mobiele telefoon en de GPS.</a>
						</td>
					</tr>
					</table>
		</div>

		<div class="nieuwsbericht">
			<div class="nieuwsberichttitel">Deelnemers</div>
				<p>Via de knop <a href="deelnemers.jsp">Deelnemers</a> krijg je een overzicht van de deelnemers aan het
					Sense of Brainport spel.
					Klik op de foto van een deelnemer of op "Bekijk mijn Routes" om zijn/haar
					routes te bekijken. Op de kaart-pagina kun je vervolgens in de balk bovenin een
					route van deze deelnemer selecteren.
				</p>
				<p>Tevens kun je als deelnemer op deze pagina inloggen door op
					<a href="control.jsp?cmd=nav-login">[log hier in als deelnemer]</a> te klikken.
					Na inloggen kunnen titels en teksten bij routes en foto's
					aangepast worden.</p>
		</div>

		<div class="nieuwsbericht">
		<div class="nieuwsberichttitel">Routes</div>
			<p>
				De pagina <a href="map.jsp">Routes</a> toont de kaarten waarop routes
				en foto's afgebeeld kunnen worden. Ook kun je binnen deze pagina
				naar <a href="map.jsp?cmd=live">Live</a> of <a href="map.jsp?cmd=autoplay">Autoplay</a>
				gaan zonder de pagina te verlaten.
			</p>

			<p><strong>Route Kiezen</strong></p>

				<p>Via het menu "Routes" boven in rode balk kun je een route selecteren op deelnemer
					via "Routes | per deelnemer" of de meest
					recente routes via "Routes | laatste 10". Na selectie verschijnt een keuze lijst
					naast het menu waaruit je een specifieke route kunt kiezen. Iedere deelnemer maakt
					per dag een route die als naam de datum heeft. Na het kiezen van een route zal deze
					ingetekend worden, samen met de foto's (als vierkante iconen) gemaakt op deze route.
				<p><strong>Route Afspelen</strong></p>
					Wanneer de route gekozen is verschijnen afspeel-knoppen onder de route beschrijving.
					Hiermee kan de route en bijbehorende foto's afgespeeld worden. De knoppen hebben
					de volgende functies:
				</p>
				<p>
					<img border="0" style="background: #333333;" class="middle" src="img/media-playback-start.png" alt="start" onload="DH.fixPNG(this)"/>&nbsp;start of pauzeer afspelen,&nbsp;
					<img border="0"  style="background: #333333;" class="middle" class="middle" src="img/media-playback-stop.png"  alt="stop" onload="DH.fixPNG(this)"/>&nbsp;stop afspelen,&nbsp;
					<img border="0"  style="background: #333333;" class="middle" class="middle" src="img/media-seek-forward.png"  alt="stop" onload="DH.fixPNG(this)"/>&nbsp;ga naar volgende route punt.
				</p>
		   </div>
		<div class="nieuwsbericht">
		<div class="nieuwsberichttitel">Foto's</div>
			<p>
				Binnen de pagina <a href="map.jsp">Routes</a> kunnen foto's los van de routes op verschillende manieren
				opgevraagd en bekeken worden. Hiervoor kan het menu "Foto's" in de rode balk gebruikt worden. Je kunt
				de laatst gemaakte foto's kiezen of willekeurige foto's uit het gehele archief of in het zichtbare
				gebied van de kaart. De foto's worden op de kaart als vierkante iconen getoond.
				Je maakt een foto zichtbaar door je muis over een icoon te bewegen (dubbel klikken is niet nodig).
				Ook kun je door de gevonden foto's bladeren met de knoppen onder de foto:
			</p>
			<p>
				<img border="0"  style="background: #333333;" class="middle" class="middle" src="img/media-skip-backward.png"  alt="stop" onload="DH.fixPNG(this)"/>&nbsp;eerste foto,&nbsp;
				<img border="0" style="background: #333333;" class="middle" src="img/media-seek-forward.png" alt="start" onload="DH.fixPNG(this)"/>&nbsp;volgende foto,&nbsp;
				<img border="0" style="background: #333333;" class="middle" src="img/media-seek-backward.png" alt="start" onload="DH.fixPNG(this)"/>&nbsp;vorige foto,&nbsp;
				<img border="0"  style="background: #333333;" class="middle" class="middle" src="img/media-skip-forward.png"  alt="stop" onload="DH.fixPNG(this)"/>&nbsp;laatste foto.
			</p>

       </div>
<div class="nieuwsbericht">
		<div class="nieuwsberichttitel">Live</div>
			<p>
				De pagina <a href="map.jsp?cmd=live">Live</a> laat de deelnemers en hun laatste
				locaties zien. Wanneer een deelnemer live met Mobiel + GPS is zal zijn/haar blauwe icoon knipperen
				op de huidige locatie en zich verplaatsen.
			</p>
			<p>
				Je kunt een specifieke deelnemer volgen en zijn/haar route tot huidige tijdstip
				bekijken door een deelnemer te selecteren uit de keuze lijst "Follow user" naast hoofdmenu.
			</p>
		</div>
<div class="nieuwsbericht">
<div class="nieuwsberichttitel">Autoplay</div>
	<p>
		Met de pagina <a href="map.jsp?cmd=autoplay">Autoplay</a> worden routes in willekeurige
		volgorde automatisch afgespeeld. Je kunt Autoplay ook activeren door uit het menu
		linksboven "Routes | Autoplay" te selecteren. Je kunt Autoplay verlaten door een
		willekeurige andere menu optie te kiezen.
	</p>

</div>
		<div class="nieuwsbericht">

		<div class="nieuwsberichttitel">Kaart Navigatie</div>
			<p>
				De <a href="map.jsp">kaart-pagina's</a> maken gebruik van <a href="http://maps.google.com" target="_new">Google Maps</a>.
				Hieronder staat beschreven hoe deze kaarten bekeken kunnen worden.
			</p>
			<p><strong>Kaart keuze</strong></p>
			<p>Met behulp van de kaartkeuze knoppen rechtsboven
				kan een kaart-type geselecteerd worden.
			Bijvoorbeeld met de knop
				<img border="0" class="middle" src="images/help/btn_satelite.jpg" width="73" height="17" alt="satellite"/> worden
				satelliet-kaarten getoond.
			</p>

			<p><strong>In/Uitzoomen</strong></p>
				<p>In de linkerbovenhoek van de kaart vind je knoppen om de kaart in
					meer detail (inzoomen) of minder detail (uitzoomen) te zien.
				</p>
				<p>
					<img border="0" class="middle" src="images/help/zoom-plus-mini.png" width="18" height="18" alt="inzoomen"/> meer detail (inzoomen)<br/>
						<img border="0" class="middle" src="images/help/zoom-minus-mini.png" width="18" height="18" alt="uitzoomen"/> minder detail (uitzoomen)<br/>

				</p>
			<p>
				Niet alle kaart-typen zijn op elk detail-niveau beschikbaar.
			</p>


			<p><strong>Verplaatsen (Panning)</strong></p>

				<p>Met de onderstaande knoppen je de kaart naar links, rechts, boven of beneden verplaatsen.</p>
			<p>
					<img border="0" class="middle" src="images/help/north-mini.png" width="18" height="18" alt=""/> naar boven<br/>
					<img border="0" class="middle" src="images/help/east-mini.png" width="18" height="18" alt=""/> naar rechts<br/>
					<img border="0" class="middle" src="images/help/south-mini.png" width="18" height="18" alt=""/> naar beneden<br/>
					<img border="0" class="middle" src="images/help/west-mini.png" width="18" height="18" alt=""/> naar links <br/>
			</p>
			<p>
				Gemakkelijker is echter om de kaart met de muis te verslepen (dragging). Klik op de kaart en
				houd de muisknop ingedrukt. Verplaats vervolgens de muis.
			</p>
<p><strong>Centreren</strong></p>

				<p>Door op een willekeurig punt op de kaar te dubbel-klikken wordt de
					kaart
					rond dat punt gecentreerd.</p>
			</div>
		</td>
<td width="170" valign="top" bgcolor="#F5F5F5" class="kolomrechts"><img src="images/techniek.jpg"
																		alt="techniek" width="170"
																		height="100"
																		class="imageborderonder"/><br/>
	<img src="images/terras.jpg" alt="terras eersel" width="170" height="100" class="imageborderonder"/>
	<br/>
	<img src="images/piazza.jpg" alt="piazza" width="170" height="100" class="imageborderonder"/></td>
</tr>
</table>
</td>
</tr>

<%@ include file="static-layout-footer.html" %>

