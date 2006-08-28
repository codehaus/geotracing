$Id: readme.txt,v 1.2 2006/02/18 12:23:29 just Exp $

INLEIDING
De "otterserver" is een GeoTracing applicatie met een eigen URL/Database.
Met de standaard MobiTracer kunnen routes met annotaties worden ingevoerd.

SITE
http://www.geotracing.com/otter
"HTTP Basic Authentication" is actief wanneer gegevens uit de DB worden opgevraagd.
Er verschijnt een pop-up login scherm waarin een vast user/paswoord moet worden ingevoerd:

user: addy
password: young

MOBITRACER
Deze kan via OTA op de telefoon gedownload te worden met de volgende URL:
http://www.geotracing.com/otter/ota/mt.jsp?u=<user>&p=<password>

Het beste is om dit in "applicatie beheer te doen". Meer details in
http://www.geotracing.com/mobitracer.html
http://www.geotracing.com/wiki/index.php/GeoTracing:Apps:MobiTracer

Er is nu 1 user (addy). Dus wordt URL:
http://www.geotracing.com/otter/ota/mt.jsp?u=addy&p=young

BELANGRIJK
- MobiTracer stuurt niet automatisch GPS info naar server. Er dient altijd eerst een Track
te worden aangemaakt (NewTrack) en vervolgens een "ResumeTrack". Pauzeren gaat met SuspendTrack.
Dit creeert tracksegmenten.

- voor invoeren annotaties (POIs/foto's etc) moet er minimaal 1 punt in de track
zijn anders kunnen ze niet ge-geottagged worden

TODO
- admin scherm
- POI-typen + iconen
- meer info/help op site

VERSIE INFO

v1.0.0  18.feb.2006
- eerste versie




