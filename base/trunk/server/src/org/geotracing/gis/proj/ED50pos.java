// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ED50pos.java

package org.geotracing.gis.proj;

import org.geotracing.gis.proj.Convert;

// Referenced classes of package convert:
//            LLpos, Convert, UTMpos, MGRSpos,
//            WGS84pos

public class ED50pos extends LLpos {

	public ED50pos(double phi, double lam) {
		super.phi_ = new Double(phi);
		super.lam_ = new Double(lam);
		super.lat_ = new Double(Convert.RadToDeg(phi));
		super.lon_ = new Double(Convert.RadToDeg(lam));
	}

	public UTMpos CalcUTM(double inZone) {
		double phi = super.phi_.doubleValue();
		double lam = super.lam_.doubleValue();
		double axis_a = 6378388D;
		double f = 0.0033670033670033669D;
		double axis_b = axis_a * (1.0D - f);
		double c = Math.pow(axis_a, 2D) / axis_b;
		double e2p = Convert.EprimSqEllips(f);
		double eta2 = e2p * Math.pow(Math.cos(phi), 2D);
		double grandV = Math.sqrt(1.0D + eta2);
		double grandR = Math.pow(axis_a, 2D) / axis_b / Math.sqrt(1.0D + eta2);
		double m0 = 0.99960000000000004D;
		double A = 6367654.5001177313D;
		double B = -16107.034628386167D;
		double C = 16.976225262629406D;
		double D = -0.022262392837136699D;
		double G = 6367654.5001177313D * phi + -16107.034628386167D * Math.sin(2D * phi) + 16.976225262629406D * Math.sin(4D * phi) + -0.022262392837136699D * Math.sin(6D * phi);
		double Gbar = grandR * phi;
		double dLam = lam - Convert.DegToRad(inZone * 6D - 183D);
		double Nprim = m0 * grandR * Math.atan2(Math.tan(phi), Math.cos(dLam));
		double grandF = (3D * m0 * grandR * eta2 * Math.pow(Math.cos(phi), 3D) * Math.sin(phi)) / 8D;
		double Eprim = m0 * grandR * Math.log(Math.tan(0.78539816339744828D + Math.asin(Math.cos(phi) * Math.sin(dLam)) / 2D));
		double grandH = (m0 * grandR * eta2 * Math.pow(Math.cos(phi), 3D)) / 6D;
		double easting = 500000D + Eprim + grandH * Math.pow(dLam, 3D);
		double northing = Nprim + m0 * (G - Gbar) + grandF * Math.pow(dLam, 4D);
		if (phi < 0.0D)
			northing += 10000000D;
		return new UTMpos(easting, northing, inZone);
	}

	public MGRSpos CalcMGRS(double inZone) {
		double lat = super.lat_.doubleValue();
		double lon = super.lon_.doubleValue();
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
		String outMetric = "*";
		String outHecto = "*";
		String outKilo = "*";
		if (lat < -80D)
			return new MGRSpos(outMetric, outHecto, outKilo);
		if (lat > 84D)
			return new MGRSpos(outMetric, outHecto, outKilo);
		UTMpos myUTMpos = CalcUTM(inZone);
		long east = myUTMpos.easting_.longValue();
		long north = myUTMpos.northing_.longValue();
		char zone;
		int index;
		if (lat >= 80D) {
			zone = 'X';
		} else {
			index = ((int) lat + 80) / 8;
			zone = znUTM[index];
		}
		int zoneIndex = (int) inZone - 3 * ((int) inZone / 3);
		index = (int) (east / 0x0L);
		char XXUTM = xxUTM[zoneIndex][index];
		int eastVal = (int) ((double) (east - (long) (index * 0x186a0)) + 0.5D);
		int zoneParity = (int) inZone - 2 * ((int) inZone / 2);
		int temp = (int) (north / 0x0L);
		index = ((int) north - 0x1e8480 * temp) / 0x186a0;
		char YYUTM = yyUTM[zoneParity][index];
		int northVal = (int) ((double) (north - (long) ((int) (north / 0x0L) * 0x186a0)) + 0.5D);
		outMetric = String.valueOf((int) inZone) + String.valueOf(zone) + String.valueOf(XXUTM) + String.valueOf(YYUTM) + Convert.ForceDec(eastVal, 5) + Convert.ForceDec(northVal, 5);
		eastVal /= 100;
		northVal /= 100;
		outHecto = String.valueOf((int) inZone) + String.valueOf(zone) + String.valueOf(XXUTM) + String.valueOf(YYUTM) + Convert.ForceDec(eastVal, 3) + Convert.ForceDec(northVal, 3);
		eastVal /= 10;
		northVal /= 10;
		outKilo = String.valueOf((int) inZone) + String.valueOf(zone) + String.valueOf(XXUTM) + String.valueOf(YYUTM) + Convert.ForceDec(eastVal, 2) + Convert.ForceDec(northVal, 2);
		return new MGRSpos(outMetric, outHecto, outKilo);
	}

	public WGS84pos CalcWGS84() {
		double phiED = super.phi_.doubleValue();
		double lamED = super.lam_.doubleValue();
		double delX = -87D;
		double delY = -98D;
		double delZ = -121D;
		double a1 = 6378388D;
		double f1 = 0.0033670033670033669D;
		double a2 = 6378137D;
		double f2 = 0.0033528106647474805D;
		double sinphi = Math.sin(phiED);
		double cosphi = Math.cos(phiED);
		double sinlam = Math.sin(lamED);
		double coslam = Math.cos(lamED);
		double e1c = Convert.ESqEllips(f1);
		double e2c = Convert.ESqEllips(f2);
		double nu1 = a1 / Math.sqrt(1.0D - e1c * Math.pow(sinphi, 2D));
		double x1 = nu1 * cosphi * coslam;
		double y1 = nu1 * cosphi * sinlam;
		double z1 = nu1 * (1.0D - e1c) * sinphi;
		double x2 = x1 + delX;
		double y2 = y1 + delY;
		double z2 = z1 + delZ;
		double lamWGS = Math.atan2(y2, x2);
		double p2 = Math.sqrt(Math.pow(x2, 2D) + Math.pow(y2, 2D));
		double theta = Math.atan2(z2, p2 * (1.0D - f2));
		double b2 = a2 * (1.0D - f2);
		double e2p = (Math.pow(a2, 2D) - Math.pow(b2, 2D)) / Math.pow(b2, 2D);
		double phiWGS = Math.atan2(z2 + e2p * b2 * Math.pow(Math.sin(theta), 3D), p2 - e2c * a2 * Math.pow(Math.cos(theta), 3D));
		double nu2 = a2 / Math.sqrt(1.0D - e2c * Math.pow(Math.sin(phiWGS), 2D));
		return new WGS84pos(phiWGS, lamWGS);
	}
}
