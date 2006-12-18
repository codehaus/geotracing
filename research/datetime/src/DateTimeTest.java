import java.util.TimeZone;

public class DateTimeTest {
	public static void main(String[] args) {
		printTimeZones();
		printMyTimeZone();

	}

	public static void printMyTimeZone() {
		TimeZone tz = TimeZone.getDefault();

		p(tz.getID());
	}

	public static void printNormalizedZones() {
		String[] ids = TimeZone.getAvailableIDs();
		for (int i = 0; i < ids.length; i++) {
			p(ids[i]);
		}
		p("we have " + ids.length + " timezones");
	}

	public static void printTimeZones() {
		String[] ids = TimeZone.getAvailableIDs();
		for (int i = 0; i < ids.length; i++) {
			p(ids[i]);
		}
		p("we have " + ids.length + " timezones");
	}

	public static void p(String s) {
		System.out.println(s);
	}
}