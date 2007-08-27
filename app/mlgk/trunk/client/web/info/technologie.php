<a href="javascript:wpInfo()">home</a> | <a href="javascript:wpInfo('mogelijkheden.php')">mogelijkheden</a> | technologie | <a href="javascript:wpInfo('pilot.php')">pilot</a> | <a href="javascript:wpInfo('partners.php')">partners</a> | <a href="javascript:wpInfo('contact.php')">contact</a>

<p>Een kijkje onder de motorkap - hieronder volgt een kort overzicht van de verschillende technologien die in de MLGK worden gebruikt.</p>

<h2>GPS</h2>
<p>De MLGK is een software omgeving waarin lokatie centraal staat. Deze lokatie wordt bepaald mbv een <a href="http://en.wikipedia.org/wiki/Gps" target="_blank">GPS</a> ontvanger. De GPS ontvanger wordt via bluetooth verbonden met de mobiele telefoon. De mobiele software vraagt vervolgens de lokatie van de GPS uit in een vast interval.</p>

<p><img src="/info/images/gps.gif" border="0" /></p>

<h2>Mobiele telefoons</h2>
<p>De MLGK mobiele software is een online applicatie die verbindt over HTTP met de MLGK server applicatie. De software is een <a href="http://en.wikipedia.org/wiki/Gps" target="_blank">J2ME</a> applicatie en is getest op de Nokia N73. De lokatie ontvangen van de GPS wordt doorgestuurd naar de server applicatie waar alle game logica wordt bepaald. Op basis van de lokatie van de mobiele client worden alle acties bepaald en weer teruggestuurd.</p>

<p><img src="/info/images/NokiaN73-mlgk1.gif" border="0" /></p>

<h2>Web applicatie</h2>
<p>De web applicatie is een <a href="http://nl.wikipedia.org/wiki/Asynchronous_JavaScript_and_XML" target="_blank">AJAX</a> applicatie die realtime alle spelacties toont gevisualiseerd boven op een Google Maps kaart.</p> 

<h2>Server platform</h2>
<p>Het server platform is gebaseerd op het <a href="http://www.keyworx.org" target="_blank">KeyWorx</a> en <a href="http://www.geotracing.org" target="_blank">Geotracing</a> platform dat realtime multi-user communicatie mogelijk maakt die gekoppeld is aan lokatie interactie.</p>

