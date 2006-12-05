// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   BELpos.java

package org.geotracing.gis.proj;

import org.geotracing.gis.proj.BD72pos;

import java.awt.Label;

// Referenced classes of package convert:
//            Convert, BD72pos, UTMpos

public class BELpos {

	public BELpos(double x, double y) {
		X_ = new Double(x);
		Y_ = new Double(y);
	}

	public BD72pos CalcBD72() {
		double lambX = X_.doubleValue();
		double lambY = Y_.doubleValue();
		double epsilon = 1.0000000000000001E-15D;
		double aHayford = 6378388D;
		double fHayford = 297D;
		double k_Lambert = 11565915.812935D;
		double n_Lambert = 0.77164219280000002D;
		double r_Lambert = Convert.DMSmToRad(0.0D, 0.0D, 29D, 298.5D);
		double c_Lambert72 = Convert.DMSmToRad(4D, 21D, 24D, 983D);
		double x0_Lambert = 150000D;
		double y0_Lambert = 5400000D;
		double dX_Lambert = 0.01256D;
		double dY_Lambert = 88.437799999999996D;
		double eccent = Math.sqrt(Convert.ESqEllips(1.0D / fHayford));
		double temp = Math.atan2(lambX - x0_Lambert - dX_Lambert, (y0_Lambert + dY_Lambert) - lambY);
		temp += r_Lambert;
		double lon = c_Lambert72 + temp / n_Lambert;
		double rho = Math.sqrt(Math.pow(lambX - x0_Lambert - dX_Lambert, 2D) + Math.pow((y0_Lambert + dY_Lambert) - lambY, 2D));
		double tgCoLat = Math.pow(rho / k_Lambert, 1.0D / n_Lambert);
		double gam1 = 2D * Math.atan(tgCoLat);
		double tst;
		do {
			double gam0 = gam1;
			gam1 = Math.pow((1.0D - eccent * Math.cos(gam0)) / (1.0D + eccent * Math.cos(gam0)), eccent / 2D);
			gam1 = 2D * Math.atan(tgCoLat * gam1);
			tst = gam0 - gam1;
			if (tst < 0.0D)
				tst = -tst;
		} while (tst > epsilon);
		double lat = 1.5707963267948966D - gam1;
		return new BD72pos(lat, lon);
	}

	public UTMpos CalcUTM31_ED50() {
		double X = X_.doubleValue();
		double Y = Y_.doubleValue();
		double X0 = 449681.70199999999D;
		double Y0 = 5460505.3260000004D;
		double a1 = 0.99928625349800004D;
		double a2 = 0.018588407075999999D;
		double b1 = -5.4504182699999996E-10D;
		double b2 = -1.6968098700000001E-09D;
		double c1 = 4.0783167600000002E-15D;
		double c2 = 2.1930990199999999E-16D;
		double dX2Y2 = X * X - Y * Y;
		double dx3y2 = Math.pow(X, 3D) - 3D * X * Y * Y;
		double dx2y3 = 3D * X * X * Y - Math.pow(Y, 3D);
		double eUTM = (((((X0 + a1 * X) - a2 * Y) + b1 * dX2Y2) - 2D * b2 * (X * Y)) + c1 * dx3y2) - c2 * dx2y3;
		double nUTM = Y0 + a1 * Y + a2 * X + 2D * b1 * (X * Y) + b2 * dX2Y2 + c1 * dx2y3 + c2 * dx3y2;
		return new UTMpos(eUTM, nUTM, 31D);
	}

	public String CalculatePage() {
		double lambX = X_.doubleValue();
		double lambY = Y_.doubleValue();
		short maparray[][] = {
				{
						0, 0, 0, 0, 0, 0, -7, -8, -9, 0,
						0, 0, 0, 0
				}, {
				0, 2, 3, 4, 5, 6, 7, 8, 9, -19,
				-20, 0, 0, 0
		}, {
				10, 11, 12, 13, 14, 15, 16, 17, 18, 19,
				20, 21, 0, 0
		}, {
				22, 23, 24, 25, 26, 27, 28, 29, 30, 31,
				32, 33, 0, 0
		}, {
				34, 35, 36, 37, 38, 39, 40, 41, 42, 43,
				44, 45, -57, 0
		}, {
				0, 46, 47, 48, 49, 50, 51, 52, 53, 54,
				55, 56, 57, 0
		}, {
				0, 0, 58, 59, 60, 61, 62, 63, 64, 65,
				66, 67, 68, 69
		}, {
				0, 0, 0, 0, 70, 71, 72, 73, 74, 75,
				76, 77, 78, 79
		}, {
				0, 0, 0, 0, 0, 80, 81, 82, 83, 84,
				85, 86, 87, 0
		}, {
				0, 0, 0, 0, 0, 88, 89, 90, 91, 92,
				93, 94, 95, 0
		}, {
				0, 0, 0, 0, 0, 0, 0, 0, 96, 97,
				98, 99, 0, 0
		}, {
				0, 0, 0, 0, 0, 0, 0, 0, 100, 101,
				102, 103, 0, 0
		}
		};
		int indexX = (int) ((lambX - 12675D) / 21334D);
		int indexY = (int) ((258183D - lambY) / 20000D);
		if (indexX < 0)
			return "";
		if (indexX > 13)
			return "";
		if (indexY < 0)
			return "";
		if (indexY > 11)
			return "";
		short map = maparray[indexY][indexX];
		switch (map) {
			case 0: // '\0'
				return "";

			case -7:
				return "7 *";

			case -8:
				return "8 *";

			case -9:
				return "9 *";

			case -19:
				return "19 *";

			case -20:
				return "20 *";

			case -57:
				return "57 *";
		}
		return String.valueOf(map);
	}

	public String CalculateMap() {
		double lambX = X_.doubleValue();
		double lambY = Y_.doubleValue();
		short maparray[][] = {
				{
						0, 0, 0, 0, 1, -2, 3, 0, 0, 0
				}, {
				0, 4, 5, -6, 7, 8, 9, -10, 0, 0
		}, {
				-11, 12, 13, 14, 15, 16, 17, 18, 0, 0
		}, {
				-19, 20, 21, 22, 23, 24, 25, 26, 0, 0
		}, {
				-27, 28, 29, 30, 31, 32, 33, 34, -35, 0
		}, {
				0, -36, 37, 38, 39, 40, 41, 42, 43, 0
		}, {
				0, 0, 44, 45, 46, 47, 48, 49, 50, -50
		}, {
				0, 0, 0, 51, 52, 53, 54, 55, 56, -56
		}, {
				0, 0, 0, 0, 57, 58, 59, 60, 61, 0
		}, {
				0, 0, 0, 0, 62, 63, 64, 65, 0, 0
		}, {
				0, 0, 0, 0, 0, -66, 67, 68, -69, 0
		}, {
				0, 0, 0, 0, 0, 0, -70, 71, -72, 0
		}
		};
		String maps[] = {
				"", "Essen", "Meerle", "Maarle", "Blankenberge", "Westkapelle", "Watervliet", "Kapellen", "Turnhout", "Arendonk",
				"Beverbeek", "0ostduinkerke", "0ostende", "Brugge", "Lokeren", "Antwerpen", "Lier", "Mol", "Maaseik", "Veurne",
				"Roeselare", "Tielt", "Gent", "Mechelen", "Aarschot ", "Hasselt", "Rekem", "Proven", "Ieper", "Kortrijk",
				"Geraardsbergen", "Brussel-Bruxelles", "Leuven", "Sint-Truiden", "Tongeren", "Gemmenich", "Ploegsteert", "Tournai", "Ath", "Nivelles",
				"Wavre", "Waremme", "Li\350ge", "Limbourg", "P\351ruwelz", "Mons", "Charleroi", "Namur", "Huy", "Spa",
				"Stavelot-Losheimergraben", "Roisin", "Thuin", "Dinant", "Marche-en-Famenne", "Durbuy", "Vielsalm-Manderfeld", "Chimay", "Beauraing", "Saint-Hubert",
				"La Roche-en-Ardenne", "Limerl\351", "Cul-des-Sarts", "Gedinne", "Paliseul", "Neufchateau", "Sugny", "Bouillon", "Arlon", "Sterpenich",
				"Villers-devant-Orval", "Virton", "Houwald"
		};
		int indexX = (int) ((lambX - 2025D) / 32000D);
		int indexY = (int) ((258183D - lambY) / 20000D);
		if (indexX < 0)
			return "";
		if (indexX > 9)
			return "";
		if (indexY < 0)
			return "";
		if (indexY > 11)
			return "";
		short map = maparray[indexY][indexX];
		switch (map) {
			case 0: // '\0'
				return "";

			case -2:
				if (lambX < 172300D)
					return "";
				if (lambX > 183300D)
					return "";
				if (lambY > 243650D)
					return "";
				else
					return "2-8 " + maps[2] + '-' + maps[8];

			case -6:
				return "6-14 * " + maps[6] + '-' + maps[14];

			case 8: // '\b'
				return "2-8 " + maps[2] + '-' + maps[8];

			case -10:
				return "10-18 * " + maps[10] + '-' + maps[18];

			case -11:
				return "11-12 * " + maps[11] + '-' + maps[12];

			case 14: // '\016'
				return "6-14 " + maps[6] + '-' + maps[14];

			case 18: // '\022'
				return "10-18 " + maps[10] + '-' + maps[18];

			case -19:
				return "19-20 * " + maps[19] + '-' + maps[20];

			case 20: // '\024'
				return "19-20 " + maps[19] + '-' + maps[20];

			case -27:
				return "27-28-36 * " + maps[27] + '-' + maps[28] + '-' + maps[36];

			case 28: // '\034'
				return "27-28-36 " + maps[27] + '-' + maps[28] + '-' + maps[36];

			case -35:
				return "35-43 * " + maps[35] + '-' + maps[43];

			case -36:
				return "27-28-36 * " + maps[27] + '-' + maps[28] + '-' + maps[36];

			case 43: // '+'
				return "35-43 " + maps[35] + '-' + maps[43];

			case -50:
				if (lambX > 293000D)
					return "";
				if (lambY > 133000D)
					return "";
				else
					return "50-50A " + maps[50];

			case 50: // '2'
				return "50-50A " + maps[50];

			case -56:
				return "56-56A * " + maps[56];

			case 56: // '8'
				return "56-56A " + maps[56];

			case 63: // '?'
				return "63-66 " + maps[63] + '-' + maps[66];

			case -66:
				if (lambX < 185000D)
					return "";
				if (lambY < 53000D)
					return "";
				else
					return "63-66 " + maps[63] + '-' + maps[66];

			case 67: // 'C'
				return "67-70 " + maps[67] + '-' + maps[70];

			case 68: // 'D'
				return "68-69 " + maps[68] + '-' + maps[69];

			case -69:
				return "68-69 * " + maps[68] + '-' + maps[69];

			case -70:
				return "67-70 * " + maps[67] + '-' + maps[70];

			case 71: // 'G'
				return "71-72 " + maps[71] + '-' + maps[72];

			case -72:
				return "71-72 * " + maps[71] + '-' + maps[72];
		}
		return String.valueOf(map) + ' ' + maps[map];
	}

	public void DisplayPos(Label xLabel, Label yLabel) {
		xLabel.setText(Convert.ParseDec(X_.doubleValue(), 3));
		yLabel.setText("  " + Convert.ParseDec(Y_.doubleValue(), 3));
	}

	public Double X_;
	public Double Y_;
}
