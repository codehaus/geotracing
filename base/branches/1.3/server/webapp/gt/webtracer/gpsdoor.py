#! /usr/bin/env python
# MeHere 0001
# Copyright 2005, Glen Murphy

import os, sys, thread, time, string, random, glob
from urllib import urlopen, urlretrieve
# from serial import Serial

from socket import *

class GPSDevice:
	def __init__(self, name):
		self.name = name
		self.pr("opening GPSDevice %s...." % name)
		self.handle = open(self.name, 'r')
		self.pr("opened GPS device")

	def pr(self, s):
		print s
		sys.stdout.flush()

	def readline(self):
		# maxsize is ignored, timeout in seconds is the max time that is way for a complete line
		# print "readgpsline2"
		line = '$GPGGA,210251.000,5218.7413,N,00451.1867,E,1,03,3.5,38.9,M,47.0,M,,0000*6A'
		try:
			# print "reading line..."
			line = self.handle.readline()
			# print "reading line OK"
			# print line
		except:
			self.pr("ERROR reading from GPS device")

		return line

   	def close(self):
   		self.handle.close()

   	def isOpen(self):
   		return self.handle != null

class EnhancedSerial:
	def __init__(self, *args, **kwargs):
		#ensure that a reasonable timeout is set
		timeout = kwargs.get('timeout',0.1)
		if timeout < 0.01: timeout = 0.1
		kwargs['timeout'] = timeout
		# Serial.__init__(self, *args, **kwargs)
		self.pr("opening GPS device %s...." % args[0])
		self.gps = open(args[0], 'r')
		self.pr ("opened GPS device")
		self.buf = ''

	def pr(self, s):
		print s
		sys.stdout.flush()

	def readgpsline(self):
		# maxsize is ignored, timeout in seconds is the max time that is way for a complete line
		# self.pr "readgpsline2"
		line = '$GPGGA,210251.000,5218.7413,N,00451.1867,E,1,03,3.5,38.9,M,47.0,M,,0000*6A'
		try:
			# self.pr "reading line..."
			line = self.gps.readline()
			# self.pr "reading line OK"
			# self.pr line
		except:
			self.pr("ERROR reading from GPS device")

		return line

	def readline(self, maxsize=None, timeout=1):
		# maxsize is ignored, timeout in seconds is the max time that is way for a complete line
		print "readline"
		return self.gps.readline();
		tries = 0
		while 1:
			self.buf += self.gps.read(512)
			pos = self.buf.find('\n')
			if pos >= 0:
				line, self.buf = self.buf[:pos+1], self.buf[pos+1:]
				return line
			tries += 1
			if tries * self.timeout > timeout:
				break
		line, self.buf = self.buf, ''
		return line

	def readlines(self, sizehint=None, timeout=1):
		"""read all lines that are available. abort after timout
		when no more data arrives."""
		lines = []
		while 1:
			line = self.readline(timeout=timeout)
			if line:
				lines.append(line)
			if not line or line[-1:] != '\n':
				break
		return lines


class mehere:
	def saveconfig(self):
		fp = open(self.configPath, "w")
		fp.write(self.username+"\n")
		fp.write(self.password+"\n")
		fp.write(self.description+"\n")
		fp.write(self.gpsport+"\n")
		fp.close()
		self.pr("config saved to %s" % self.configPath)

	def __init__(self):
		self.configPath = os.path.expanduser("~") + os.sep + "gpsdoor.ini"

		try:
			fp = open(self.configPath, "r")
			self.username = fp.readline().strip()
			self.password = fp.readline().strip()
			self.description = fp.readline().strip()
			self.gpsport = fp.readline().strip()
			fp.close()
		except:
			self.username = "You"
			self.password = ""
			self.description = ""
			self.gpsport = "GPS_PORT_NOT_SET"

		self.port = 7305
		self.longitude = 0
		self.latitude = 0
		self.running = 1
		self.ip = 0
		self.public = 0
		self.gpsdata = 0
		self.gpsconnected = 0

		# test if already running
		try:
			urlp = urlopen("http://localhost:7305/test")
			if urlp.read() == "1":
				self.pr("Instance Already Running")
				self.openbrowser()
				self.running = 0
				return
			urlp.close()
		except:
			pass

		if self.public:
			try:
				urlp = urlopen("http://glenmurphy.com/tools/ip.php")
				self.ip = urlp.read()
				urlp.close()
				self.pr("External IP is %s" % self.ip)
			except:
				self.pr ("Unable to determine IP, switching to private mode")
				self.public = 0
				self.ip = "localhost"
		else:
			self.ip = "localhost"
			self.pr("Not doing external access.")

		"""
		self.gps_thread = threading.Thread(target=self.gps)
		self.gps_thread.setDaemon(1)
		self.gps_thread.start()
		self.httpd_thread = threading.Thread(target=self.httpd)
		self.httpd_thread.setDaemon(1)
		self.httpd_thread.start()
		"""

		thread.start_new(self.gps, ())
		thread.start_new(self.httpd, ())

	'''
	def __del__(self):
		self.s.close()
		self.pr ("serial port closed")
		self.sock.close()
		self.pr ("socket server closed")
	'''

	def pr(self, s):
		print s
		sys.stdout.flush()

	def textblock(self, text):
		# UGLY, FIX ME
		output = ""
		for line in text.splitlines():
			output += line.lstrip(" \t") + "\n";
		return output

	def gps(self):
		# set up GPS connection
		while 1:
			self.gpsdata = 0
			self.gpsconnected = 0

			try:
				if self.s.isOpen():
					self.s.close()
			except:
				pass


			if self.gpsport == "DEMOMODE":
				self.pr ("Entering Demo Mode")
				self.latitude = 52.35
				self.longitude = 4.89
				self.gpsconnected = 1

				while self.gpsconnected:
					self.gpsdata = 1;
					self.latitude = self.latitude - 0.001
					self.longitude = self.longitude + 0.0001
					# sys.stdout.write('d')
					# sys.stdout.flush()
					time.sleep(1)

			else:
				while self.gpsconnected != 1:
					try:
						# self.s = EnhancedSerial(self.gpsport, baudrate=4800, xonxoff=0, rtscts=0, timeout=1)
						self.s = GPSDevice(self.gpsport)
						self.gpsconnected = 1
						self.pr("%s Connected" % self.gpsport)
					except:
						self.pr("CONNECTION ERROR: %s" % self.gpsport)
						self.gpsconnected = -1
						self.gpsdata = 0
						time.sleep(1)

				fails = 0
				while self.gpsconnected:
					# line = self.s.readline(timeout=1)
					line = self.s.readline();
					datablock = line.split(',')
					# print "datablock parsed"
					if line[0:6] == '$GPGGA' and datablock[2] and datablock[4]:
						# print line
						try:
							latitude_in = string.atof(datablock[2])
							longitude_in = string.atof(datablock[4])

							if datablock[3] == 'S':
								latitude_in = -latitude_in
							if datablock[5] == 'W':
								longitude_in = -longitude_in

							latitude_degrees = int(latitude_in/100)
							latitude_minutes = latitude_in - latitude_degrees*100

							longitude_degrees = int(longitude_in/100)
							longitude_minutes = longitude_in - longitude_degrees*100

							self.latitude = latitude_degrees + (latitude_minutes/60)
							self.longitude = longitude_degrees + (longitude_minutes/60)
							self.gpsdata = 1

							sys.stdout.write('+')
							sys.stdout.flush()
							fails = 0
						except:
							self.pr("error parsing %s" % line)
							pass
					else:
						fails = fails + 1

	def openbrowser(self):
		os.system("open http://localhost:7305")

	def outputnetlink(self):
		if self.longitude != -999:
			return self.textblock("""\
				HTTP/1.0 200
				Content-type: text/xml

				<?xml version="1.0" encoding="UTF-8"?>
				<kml xmlns="http://earth.google.com/kml/2.0">
				<Placemark>
					<name>%s</name>
					<description>%s</description>
					<LookAt>
						<longitude>%s</longitude>
						<latitude>%s</latitude>
						<range>8000</range>
						<tilt>0</tilt>
						<heading>0</heading>
					</LookAt>
					<Point>
						<coordinates>%s,%s</coordinates>
					</Point>
				</Placemark>
				</kml>
			""" % ( self.username, self.description, self.longitude, self.latitude, self.longitude, self.latitude))
		else:
			return self.textblock("""\
				HTTP/1.0 200
				Content-type: application/vnd.google-earth.kml+xml

				<?xml version="1.0" encoding="UTF-8"?>
				<kml xmlns="http://earth.google.com/kml/2.0">
				</kml>
			""")

	def outputshutdown(self):
		return self.textblock("""\
			HTTP/1.0 200
			Content-type: text/html

			<html>
			<body style="font-family:verdana; font-size:11px;">
			Cheerio old bean.
			</body>
			</html>
		""")

	def outputgmaps(self):
		return self.textblock("""\
			HTTP/1.0 200
			Content-type: application/x-javascript

			var j = {x: %s, y: %s};
			_m.map.recenterOrPanToLatLng(j);
		""" % (self.longitude, self.latitude))

	def outputjs(self):
		return self.textblock("""\
			HTTP/1.0 200
			Content-type: application/x-javascript

			mh_status = %s;
			mh_lon = %s;
			mh_lat = %s;
			if (GPS) {
			  GPS.lon = mh_lon;
			  GPS.lat = mh_lat;
			  GPS.set = true;
			}
 		""" % (self.gpsdata, self.longitude, self.latitude))

	def outputcsv(self):
		return self.textblock("""\
			HTTP/1.0 200
			Content-type: text

			%s,%s,%s
		""" % (self.longitude, self.latitude, self.gpsdata))

	def outputkmllocal(self):
		return self.textblock("""\
			HTTP/1.0 200
			Content-type: application/vnd.google-earth.kml+xml
			Content-disposition: attachment;filename=%s_googleearth.kml

			<?xml version="1.0" encoding="UTF-8"?>
			<kml xmlns="http://earth.google.com/kml/2.0">
				<NetworkLink>
					<name>%s</name>
					<description>%s</description>
					<Url>
						<href>http://localhost:%s/netlink</href>
						<refreshMode>onInterval</refreshMode>
						<refreshInverval>1</refreshInverval>
						<viewRefreshMode>onStop</viewRefreshMode>
						<viewRefreshTime>1</viewRefreshTime>
					</Url>
				</NetworkLink>
			</kml>
		"""	% (self.username, self.username, self.description, self.port))

	def outputkmlpublic(self):
		return self.textblock("""\
			HTTP/1.0 200
			Content-type: application/vnd.google-earth.kml+xml
			Content-disposition: attachment;filename=%s_googleearth.kml

			<?xml version="1.0" encoding="UTF-8"?>
			<kml xmlns="http://earth.google.com/kml/2.0">
				<NetworkLink>
					<name>%s</name>
					<description>%s</description>
					<Url>
						<href>http://%s:%s/netlink</href>
						<refreshMode>onInterval</refreshMode>
						<refreshInverval>10</refreshInverval>
						<viewRefreshMode>onStop</viewRefreshMode>
						<viewRefreshTime>1</viewRefreshTime>
					</Url>
				</NetworkLink>
			</kml>
		"""	% (self.username, self.username, self.description, self.ip, self.port))

	def xmlcoords(self):
		if self.gpsdata:
			return self.textblock('''\
				HTTP/1.0 200
				Content-type: text/xml

				<markers>
				<marker lng="'''+str(self.longitude)+'''" lat="'''+str(self.latitude)+'''" name="'''+str(self.username)+'''" />
				</markers>
			''')
		else:
			return self.textblock('''\
				HTTP/1.0 200
				Content-type: text/xml

				<markers>
				</markers>
			''')

	def xmlstatus(self):
		if self.gpsconnected != 1:
			return self.textblock("""\
				HTTP/1.0 200
				Content-type: text/html

				ERROR:GPSCONNECTION""")
		elif self.gpsconnected == 1 and self.gpsdata == 0:
			return self.textblock("""\
				HTTP/1.0 200
				Content-type: text/html

				STATUS:GPSCONNECTED""")
		elif self.gpsconnected == 1 and self.gpsdata == 1:
			return self.textblock("""\
				HTTP/1.0 200
				Content-type: text/html

				READY""")

	def xmlsetcom(self, port):
		print "xmlsetcom port=" + port
		self.gpsport = port
		self.gpsconnected = 0
		self.gpsdata = 0
		self.saveconfig()
		return self.textblock("""\
			HTTP/1.0 200
			Content-type: text/html

			OK""")

	def outputinterface(self):
		if self.public:
			kmlstring = "Google Earth Public KML:<br />http://"+self.ip+":"+str(self.port)+"/kml"
		else:
			kmlstring = "Your machine is not publicly accessible, so public KML sharing is not enabled."

		# create device/port options string (OS-dependent)
		if os.name == "posix":
			devices = glob.glob('/dev/tty.*')
			devices.append("DEMOMODE")
		else:
			# assume windows (nt)
			devices = ['DEMOMODE', 'COM1', 'COM2', 'COM3', 'COM4', 'COM5', 'COM6']

		options = ""
		# self.pr("self.gpsport=[%s]" % self.gpsport)
		for d in devices:
			# self.pr("d=[%s]" % d)
			if d == self.gpsport:
				options += "<option value=\"" + d + "\" selected=\"selected\" >" + d + "</option>\n"
			else:
				options += "<option value=\"" + d + "\" >" + d + "</option>\n"

		return self.textblock("""\
			HTTP/1.0 200
			Content-type: text/html

			<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
			<html xmlns="http://www.w3.org/1999/xhtml">
				<head>
					<title>GPSDoor</title>
					<script src="http://maps.google.com/maps?file=api&v=2&key=ABQIAAAA3eH-SAAe2Xqk-3NwFLbsNxRptbkC4WM9ulKSatSIoBxwaiDcDxQ0WcCSNbTWZ5qONyPuhvfMZPJspQ" type="text/javascript"></script>
					<style type="text/css">
					body {
						margin:0px;
					}
					#message {
						border:1px solid black;
						background-color:#ffffe0;
						padding:5px;
						display:block;
						margin-bottom:10px;
					}
					#map {
						float:left;
					}
					#menu {
						font-family:verdana;
						font-size:11px;
						line-height:19px;
						float:left;
						padding:10px 0px 0px 10px;
						width:200px;
					}
					#menu ul {
						margin:0px;
						padding-left:18px;
					}
					h1 {
						font-size:14px;
					}
					h2 {
						font-size:11px;
					}
					</style>
					<script type="text/javascript">
					//<![CDATA[

					function g(o) {return document.getElementById(o);}

					var baseIcon = new GIcon();
					baseIcon.shadow = "http://www.google.com/mapfiles/shadow50.png";
					baseIcon.iconSize = new GSize(20, 34);
					baseIcon.shadowSize = new GSize(37, 34);
					baseIcon.iconAnchor = new GPoint(9, 34);
					baseIcon.infoWindowAnchor = new GPoint(9, 2);
					baseIcon.infoShadowAnchor = new GPoint(18, 25);

					var map;
					var centeronnext = true;

					var gpsconnected = 0;
					var gpsport = '"""+self.gpsport+"""';
					var longitude = 0;
					var latitude = 0;
	                var isIE6CSS = (document.compatMode && document.compatMode.indexOf("CSS1") >= 0) ? true : false;

					function setmessage(msg) {
						if(msg) {
							g('message').innerHTML = msg;
							g('message').style.display = 'block';
						}
						else {
							g('message').style.display = 'none';
						}
					}

					function createMarker(point, name, index) {
						// Create a lettered icon for this point using our icon class from above
						var letter = String.fromCharCode("A".charCodeAt(0) + index);
						var icon = new GIcon(baseIcon);
						icon.image = "http://www.google.com/mapfiles/marker" + letter + ".png";
						var marker = new GMarker(point, icon);

						// Show this marker's index in the info window when it is clicked
						var html = "<b>" + name + "</b>";
						GEvent.addListener(marker, "click", function() {
							marker.openInfoWindowHtml(html);
						});

						return marker;
					}

					var intervalCoords;
					function getcoords() {
						var request = GXmlHttp.create();
						var n = new Date();
						request.open("GET", "/xml/coords/" + n.getTime(), true);
						request.onreadystatechange = function() {
							if (request.readyState == 4) {
								var xmlDoc = GXml.parse(request.responseText);//request.responseXML;
								var markers = xmlDoc.documentElement.getElementsByTagName("marker");

								map.clearOverlays();
								for (var i = 0; i < markers.length; i++) {
									var point = new GPoint(parseFloat(markers[i].getAttribute("lng")),
																				 parseFloat(markers[i].getAttribute("lat")));
									var marker = createMarker(point, markers[i].getAttribute("name"), i);
									map.addOverlay(marker);
									if(i == 0) {
										longitude = markers[i].getAttribute("lng");
										latitude = markers[i].getAttribute("lat");
										if(centeronnext) {
											map.recenterOrPanToLatLng(new GPoint(longitude, latitude));
											centeronnext = false;
										}
									}
								}
							}
						}
						request.send(null);
					}

					function connect() {
						centeronnext = true;
						var request = GXmlHttp.create();
						var n = new Date();
						request.open("GET", "/xml/status/" + n.getTime(), true);
						request.onreadystatechange = function() {
							if (request.readyState == 4) {
								var res = request.responseText;

								if(res == "ERROR:GPSCONNECTION\\n") {
									setmessage("There was an error connecting to your GPS device - please check your configuration.");
									gpsconnected = -1;
									setTimeout('connect()',500);
								}
								else if(res == "STATUS:GPSCONNECTED\\n") {
									setmessage("Your COM port has connected successfully, now attempting to obtain GPS coordinates...");
									gpsconnected = 1;
									setTimeout('connect()',500);
								}
								else if(res == "READY\\n") {
									setmessage("");
									gpsconnected = 1;
									intervalCoords = setInterval('getcoords()',5000);
								}
							}
						}
						request.send(null);
					}

					function load() {
						resize();
						map = new GMap(g('map'));
						map.addControl(new GLargeMapControl());
						map.addControl(new GMapTypeControl());
						map.centerAndZoom(new GPoint(-122.1419, 37.4419), 8);
						connect();
					}

					function comport(port) {
						if(gpsport != port) {
							gpsport = port;
							var request = GXmlHttp.create();
							var n = new Date();
							request.open("GET", "/xml/setcom/"+gpsport+"/" + n.getTime(), true);
							request.onreadystatechange = function() {
								if (request.readyState == 4) {
									setTimeout('connect()',1000);
								}
							}
							request.send(null);
						}
					}

					function getInsideWindowWidth() {
						if (window.innerWidth) {
							return window.innerWidth;
						} else if (isIE6CSS == true) {
							// measure the html element's clientWidth
							return document.body.parentElement.clientWidth
						} else if (document.body && document.body.clientWidth) {
							return document.body.clientWidth;
						}
						return 0;
					}

					// Return the available content height space in browser window
					function getInsideWindowHeight() {

						if (window.innerHeight) {
							return window.innerHeight;
						} else if (isIE6CSS == true) {
							// measure the html element's clientHeight
							return document.body.parentElement.clientHeight
						} else if (document.body && document.body.clientHeight) {
							return document.body.clientHeight;
						}
						return 0;
					}

					function resize() {
						var height = getInsideWindowHeight();
						var width = getInsideWindowWidth();
						g('map').style.width = parseInt(width) - 250 + 'px';
						g('map').style.height = parseInt(height) + 'px';
					}

					window.onresize = resize;
					//]]>
					</script>
				</head>
				<body onload="load();">
					<div width="500" height="500" id="map"></div>
					<div id="menu">
						<div id="message">Loading...</div>
						<h1>MeHere</h1>
						<h2>Options</h2>
						<ul>
							<li><a href="#" onclick="map.recenterOrPanToLatLng(new GPoint(longitude, latitude)); return false;">Recenter Map</a></li>
							<li><a href="/kmllocal">View in Google Earth</a></li>
							<li>"""+kmlstring+"""</li>
						</ul>
						<h2>Configuration</h2>
						<ul>
						   <li>
								GPS Ports<br />
								<select onchange="comport(this.value);">
									"""+options+"""
								</select>
							</li>
  							<li><a href="/shutdown" onclick="return confirm('Are you sure?');">Shutdown GPSDoor</a></li>
							<!-- <li><a href="http://mehere.glenmurphy.com/">Go to the MeHere website</a></li> -->
							<li><a href="http://www.geotracing.com/gt/webtracer">start WebTracer (not Safari)</a></li>
						</ul>
						<h2>Credits</h2>
						<p><a href="http://mehere.glenmurphy.com/">mehere.glenmurphy.com</a> original MeHere program</p>
					</div>
				</body>
			</html>""")


	def quit(self):
		print "QUITING..."
		self.listening = 0
		self.sock.close()
		time.sleep(4)
		self.running = 0
		print "QUIT OK"

	def handleClient(self, connection):
		data = connection.recv(2048)

		for line in data.splitlines():
			guts = line.split(' ')
			if guts[0] == "GET":
				getargs = guts[1][1:].split('/')
				page = getargs[0]
				# print guts

				if page == "test":
					connection.send( "1" )
				elif page[0:7] == "netlink":
					connection.send( self.outputnetlink() )
				elif page == "kmllocal":
					connection.send( self.outputkmllocal() )
				elif page == "kml":
					connection.send( self.outputkmlpublic() )
				elif page == "csv":
					connection.send( self.outputcsv() )
				elif page == "js":
					connection.send( self.outputjs() )
				elif page == "" and self.address[0] == '127.0.0.1':
					connection.send( self.outputinterface() )
				elif page == "xml" and self.address[0] == '127.0.0.1':
					if getargs[1] == "coords":
						connection.send( self.xmlcoords() )
					elif getargs[1] == "status":
						connection.send( self.xmlstatus() )
					elif getargs[1] == "setcom":
						# print "setcom port = %s" % getargs[2]
						port = getargs[2]

						# case where /dev/tty... was entered
						if (getargs[3] == "dev"):
							port = "/" + getargs[3] + "/" + getargs[4]
						connection.send( self.xmlsetcom(port) )
				elif page == "shutdown" and self.address[0] == '127.0.0.1':
					connection.send( self.outputshutdown() )
					self.quit()
				else:
					connection.send( " " )
		connection.close()


	def httpd(self):
		# set up webserver
		self.sock = socket( AF_INET, SOCK_STREAM )
		self.listening = 0

		while self.listening != 1:
			try:
				print "Creating server... "

				if self.public:
					self.sock.bind( ("", self.port) )
				else:
					self.sock.bind( ("localhost", self.port) )

				self.sock.listen(5)
				self.listening = 1
			except:
				print "Error binding socket (retrying in 5 secs...)"
				time.sleep(5)

		print "Created server OK"

		self.openbrowser()

		while self.listening == 1:
			self.newsock, self.address = self.sock.accept()
			# print "Incoming from:  %s" % self.address[0]
			thread.start_new(self.handleClient, (self.newsock,))

n = mehere()
while n.running == 1:
	time.sleep(1)
del n
