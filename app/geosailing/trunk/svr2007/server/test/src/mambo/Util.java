import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.TimeZone;
import java.util.Date;
import java.util.GregorianCalendar;

public class Util {

	static DateFormat DATE_FORMAT = new SimpleDateFormat("HHmmss.SSSddMMyy");
	static TimeZone TIME_ZONE = TimeZone.getTimeZone("GMT");

	static {
		DATE_FORMAT.setTimeZone(TIME_ZONE);
	}

	static long parseTimestamp(String aTimestamp) {
		// example 085558.910030605 hhmmss.sssddmmyy
		long result = -1;

		int len = aTimestamp.length();
		String dateStr = aTimestamp.substring(len - 6, len);
		String timeStr = aTimestamp.substring(0, 6);

		p("dateStr=" + dateStr + " timeStr=" + timeStr);

		Date date = null;
		try {
			date = DATE_FORMAT.parse(aTimestamp);
			p(date.toString() + " ms=" + date.getTime());

			GregorianCalendar c = new GregorianCalendar();
			c.setTimeZone(TimeZone.getTimeZone("GMT"));
			int hs = Integer.parseInt(aTimestamp.substring(0, 2));
			int ms = Integer.parseInt(aTimestamp.substring(2, 4));
			int ss = Integer.parseInt(aTimestamp.substring(4, 6));
			int d = Integer.parseInt(aTimestamp.substring(len - 6, len - 4));
			int m = Integer.parseInt(aTimestamp.substring(len - 4, len - 2)) - 1;
			int y = 2000 + Integer.parseInt(aTimestamp.substring(len - 2, len));
			c.set(y, m, d, hs, ms, ss);
			p("gregDate=" + c.getTime() + " ms=" + c.getTimeInMillis());
		} catch (ParseException pe) {
			p("exception: " + pe);
		}
		return result;
	}


	static long parseTimestamp2(String aTimestamp) {
		// example 085558.910030605 hhmmss.sssddmmyy

		GregorianCalendar c = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		int len = aTimestamp.length();
		int hs = Integer.parseInt(aTimestamp.substring(0, 2));
		int ms = Integer.parseInt(aTimestamp.substring(2, 4));
		int ss = Integer.parseInt(aTimestamp.substring(4, 6));
		int d = Integer.parseInt(aTimestamp.substring(len - 6, len - 4));
		int m = Integer.parseInt(aTimestamp.substring(len - 4, len - 2)) - 1;
		int y = 2000 + Integer.parseInt(aTimestamp.substring(len - 2, len));
		c.set(y, m, d, hs, ms, ss);
		p("gregDate=" + c.getTime() + " ms=" + c.getTimeInMillis());
		return c.getTimeInMillis();
	}

	private static void p(String s) {
		if (s == null) {
			p("null message");
		}
		System.out.println("Util: " + s);
	}

	public static void main(String[] args) {

		parseTimestamp("085558.910030605");
	}
}
