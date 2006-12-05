// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   MGRSpos.java

package org.geotracing.gis.proj;

// Referenced classes of package convert:
//            UTMpos

public class MGRSpos {

	public MGRSpos(String mgrs, char type) {
		mgrs_metric = "";
		mgrs_hecto = "";
		mgrs_kilo = "";
		switch (type) {
			case 77: // 'M'
			case 109: // 'm'
				mgrs_metric = mgrs;
				break;

			case 72: // 'H'
			case 104: // 'h'
				mgrs_hecto = mgrs;
				break;

			case 75: // 'K'
			case 107: // 'k'
				mgrs_kilo = mgrs;
				break;
		}
	}

	public MGRSpos(String mgrs_m, String mgrs_h, String mgrs_k) {
		mgrs_metric = mgrs_m;
		mgrs_hecto = mgrs_h;
		mgrs_kilo = mgrs_k;
	}

	public UTMpos CalcUTM() {
		char znUTM[] = {
				'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M',
				'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X'
		};
		char xxUTM[][] = {
				{
						'*', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z', '*'
				}, {
				'*', 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', '*'
		}, {
				'*', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', '*'
		}
		};
		char yyUTM[][] = {
				{
						'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q',
						'R', 'S', 'T', 'U', 'V', 'A', 'B', 'C', 'D', 'E'
				}, {
				'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K',
				'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V'
		}
		};
		String myStr = mgrs_metric;
		int myLen = myStr.length();
		char c[] = myStr.toCharArray();
		int utmZoneNumber = Character.digit(c[0], 10) * 10 + Character.digit(c[1], 10);
		char utmZoneLetter = c[2];
		char utmEastLetter = c[3];
		char utmNorthLetter = c[4];
		String utmEastingStr = myStr.substring(5, 10);
		String utmNorthingStr = myStr.substring(10, 15);
		Double utmEastingD = new Double(utmEastingStr);
		Double utmNorthingD = new Double(utmNorthingStr);
		double utmEasting = utmEastingD.doubleValue();
		double utmNorthing = utmNorthingD.doubleValue();
		int zoneIndex = utmZoneNumber - 3 * (utmZoneNumber / 3);
		double eastVal = 0.0D;
		for (int i = 1; i < 9; i++) {
			if (xxUTM[zoneIndex][i] != utmEastLetter)
				continue;
			eastVal = (double) i * 100000D;
			break;
		}

		eastVal += utmEasting;
		int latIndex;
		for (latIndex = 0; latIndex < 20; latIndex++)
			if (znUTM[latIndex] == utmZoneLetter)
				break;

		int zoneParity = utmZoneNumber - 2 * (utmZoneNumber / 2);
		double northVal = 0.0D;
		for (int j = 0; j < 20; j++) {
			if (yyUTM[zoneParity][j] != utmNorthLetter)
				continue;
			northVal = (double) j * 100000D;
			break;
		}

		double approx = (double) (-80 + 8 * latIndex) * 1852D * 60D;
		int coeff = (int) (approx / 2000000D);
		northVal += utmNorthing + (double) coeff * 2000000D;
		if (northVal < approx)
			northVal += 2000000D;
		return new UTMpos(eastVal, northVal, utmZoneNumber);
	}

	public String mgrs_metric;
	public String mgrs_hecto;
	public String mgrs_kilo;
}
