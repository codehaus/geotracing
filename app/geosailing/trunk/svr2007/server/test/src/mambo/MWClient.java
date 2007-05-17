package mambo;

import java.io.*;
import java.net.*;

/**
 Client to simulate Mambo towards IA middleware.
 string SIGNAL_DATA_GPS = "$352021009412545*3D$GPRMC,103838.120,A,5511.9861,N,00557.0695,E,0.11,81.31,211206,,*30$<end>$<end>";   //  signal data to be sent

 string SIGNAL_DATA_GPS_START = "$352021009412545,start*3D$GPRMC,103838.120,A,5311.9861,N,00547.0695,E,0.11,81.31,211206,,*30$<end>$<end>";   //  signal data to be sent met start signaal


 string SIGNAL_DATA_GPS_STOP = "$352021009412545,stop*3D$GPRMC,103838.120,A,5711.9861,N,00577.0695,E,0.11,81.31,211206,,*30$<end>$<end>";   //  signal data to be sent met stop signaal

 string SIGNAL_DATA_DEVICE_INFORMATION = "$<MSG.Info.ServerLogin>$DeviceName=unnamed Mambo$Software=mambo_2.3.19_final2$Hardware=MAMBO-55$LastValidPosition=$GPRMC,103947.147,A,5311.9986,N,00547.0489,E,1.26,329.38,211206,,$IMEI=352021009412545$PhoneNumber=$LocalIP=10.49.13.242$CmdVersion=2$SUCCESS$<end>";


 @author Just van den Broecke
 */
public class MWClient  {
	private int port;
	private String host;
	private Socket socket;
	private PrintWriter writer;
	private OutputStream os;
	private BufferedReader reader;
	private static final String IAMW_HOST="141.252.27.97";
	private static final int IAMW_PORT=13000;
	String SIGNAL_DATA_DEVICE_INFORMATION = "$<MSG.Info.ServerLogin>$DeviceName=JustsMamboEmulator$Software=mambo_2.3.19_final2$Hardware=MAMBO-55$LastValidPosition=$GPRMC,103837.120,A,5711.9861,N,00577.0695,E,0.11,81.31,211206,,*30$IMEI=352021009412545$PhoneNumber=$LocalIP=213.84.253.107$CmdVersion=2$SUCCESS$<end>";
	String SIGNAL_DATA_GPS_START = "$352021009412545,start*3D$GPRMC,103838.120,A,5311.9861,N,00547.0695,E,0.11,81.31,211206,,*30$<end>$<end>";   //  signal data to be sent met start signaal
	String SIGNAL_DATA_GPS_1 = "$352021009412545*3D$GPRMC,103839.120,A,5711.9861,N,00577.0695,E,0.11,81.31,211206,,*30$<end>$<end>";   //  signal data to be sent
	String SIGNAL_DATA_GPS_2 = "$352021009412545*3D$GPRMC,145403.355,A,5304.5417,N,00520.0705,E,6.70,341.51,260805,,*0D$<end>$<end>";   //  signal data to be sent
	String SIGNAL_DATA_GPS_3 = "$352021009412545*3D$GPRMC,145417.354,A,5304.5661,N,00520.0575,E,6.55,335.56,260805,,*0C$<end>$<end>";   //  signal data to be sent
	String SIGNAL_DATA_GPS_STOP = "$352021009412545,stop*3D$GPRMC,103840.120,A,5711.9861,N,00577.0695,E,0.11,81.31,211206,,*30$<end>$<end>";   //  signal data to be sent met stop signaal
	char[] buf = new char[1024];

	/**
	 constructor
	 @param theHost server host
	 @param thePort server port
	 */
	public MWClient(String theHost, int thePort) {
		port = thePort;
		host = theHost;
		try {
			socket = new Socket(host, port);

			// attach the streams, so we can make use of our socket
			os = socket.getOutputStream();
			writer = new PrintWriter(os);
			reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			pr("Connection established");
		} catch (IOException ioe) {
			bailOut(ioe);
		}
	}

	/**
	 send the message to the server.
	 */
	private void sendMessage(String theMessage) {
		try {
			pr("sending " + theMessage);
			os.write(theMessage.getBytes());
			os.flush();
			pr("send OK ");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 send the message to the server.
	 */
	private String rcvMessage() {
		try {
			pr("receiving... ");
			reader.read(buf);
			pr("read OK ");
			return new String(buf);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	/**
	 send the message to the server.
	 */
	private void testBasic() {
		sendMessage(SIGNAL_DATA_DEVICE_INFORMATION);
		// pr(rcvMessage());
		sleep(1);
		sendMessage(SIGNAL_DATA_GPS_START);
		sleep(1);
		sendMessage(SIGNAL_DATA_GPS_1);
		sleep(1);
		//sendMessage(SIGNAL_DATA_GPS_2);
		//sleep(2);
		//sendMessage(SIGNAL_DATA_GPS_3);
		//sleep(2);
		sendMessage(SIGNAL_DATA_GPS_STOP);

	}

	private void sleep(long secs) {
		try {
			Thread.sleep(secs*1000);
		} catch (InterruptedException ie) {
			pr("interruped");
		}
	}

	private void bailOut(Exception theException) {
		try {
			writer.close();
			reader.close();
			socket.close();
		} catch (Exception ignore) {
			pr("error in bailout");
		}

		if (theException != null) {
			pr("an error has occurred:");
			theException.printStackTrace();
			System.exit(-1);
		} else {
			pr("disconnect OK");
			System.exit(0);
		}
	}

	private static void pr(String s) {
		if (s == null) {
			pr("null message");
		}
		System.out.println("MWCLient: " + s);
	}

	/**
	 */
	public static void main(String[] theArgs) {
		if (theArgs != null && theArgs.length == 3) {
			try {
				MWClient cc = new MWClient(theArgs[0], Integer.parseInt(theArgs[1]));
			} catch (NumberFormatException nfe) {
				System.err.println("\"" + theArgs[1] + "\" is not a valid portnumber");
			}
		} else {
			MWClient cc = new MWClient(IAMW_HOST, IAMW_PORT);
			cc.testBasic();
			cc.bailOut(null);
		}
	}
}
