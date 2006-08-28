// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   WGS84pos.java

package org.geotracing.gis.proj;

import org.geotracing.gis.proj.*;


// Referenced classes of package convert:
//            LLpos, Convert, ED50pos, PARISpos,
//            CH1903pos, NLBesselpos, LUREFgeopos, ATFpos,
//            UTMpos

public class WGS84pos extends LLpos {

	public WGS84pos(double phi, double lam) {
		super.phi_ = new Double(phi);
		super.lam_ = new Double(lam);
		super.lat_ = new Double(Convert.RadToDeg(phi));
		super.lon_ = new Double(Convert.RadToDeg(lam));
	}

	public ED50pos CalcED50() {
		double phiWGS = super.phi_.doubleValue();
		double lamWGS = super.lam_.doubleValue();
		double delX = -87D;
		double delY = -98D;
		double delZ = -121D;
		double a1 = 6378137D;
		double f1 = 0.0033528106647474805D;
		double a2 = 6378388D;
		double f2 = 0.0033670033670033669D;
		double sinphi = Math.sin(phiWGS);
		double cosphi = Math.cos(phiWGS);
		double sinlam = Math.sin(lamWGS);
		double coslam = Math.cos(lamWGS);
		double e1c = Convert.ESqEllips(f1);
		double e2c = Convert.ESqEllips(f2);
		double nu1 = a1 / Math.sqrt(1.0D - e1c * Math.pow(sinphi, 2D));
		double x1 = nu1 * cosphi * coslam;
		double y1 = nu1 * cosphi * sinlam;
		double z1 = nu1 * (1.0D - e1c) * sinphi;
		double x2 = x1 - delX;
		double y2 = y1 - delY;
		double z2 = z1 - delZ;
		double lamED = Math.atan2(y2, x2);
		double p2 = Math.sqrt(Math.pow(x2, 2D) + Math.pow(y2, 2D));
		double theta = Math.atan2(z2, p2 * (1.0D - f2));
		double b2 = a2 * (1.0D - f2);
		double e2p = Convert.EprimSqEllips(f2);
		double phiED = Math.atan2(z2 + e2p * b2 * Math.pow(Math.sin(theta), 3D), p2 - e2c * a2 * Math.pow(Math.cos(theta), 3D));
		return new ED50pos(phiED, lamED);
	}

	public PARISpos CalcPARIS() {
		double phiWGS = super.phi_.doubleValue();
		double lamWGS = super.lam_.doubleValue();
		double delX = -168D;
		double delY = -60D;
		double delZ = 320D;
		double a1 = 6378137D;
		double f1 = 0.0033528106647474805D;
		double a2 = 6378249.2000000002D;
		double f2 = 0.0034075495234250643D;
		double sinphi = Math.sin(phiWGS);
		double cosphi = Math.cos(phiWGS);
		double sinlam = Math.sin(lamWGS);
		double coslam = Math.cos(lamWGS);
		double e1c = Convert.ESqEllips(f1);
		double e2c = Convert.ESqEllips(f2);
		double nu1 = a1 / Math.sqrt(1.0D - e1c * Math.pow(sinphi, 2D));
		double x1 = nu1 * cosphi * coslam;
		double y1 = nu1 * cosphi * sinlam;
		double z1 = nu1 * (1.0D - e1c) * sinphi;
		double x2 = x1 - delX;
		double y2 = y1 - delY;
		double z2 = z1 - delZ;
		double lamFR = Math.atan2(y2, x2);
		double p2 = Math.sqrt(Math.pow(x2, 2D) + Math.pow(y2, 2D));
		double theta = Math.atan2(z2, p2 * (1.0D - f2));
		double b2 = a2 * (1.0D - f2);
		double e2p = Convert.EprimSqEllips(f2);
		double phiFR = Math.atan2(z2 + e2p * b2 * Math.pow(Math.sin(theta), 3D), p2 - e2c * a2 * Math.pow(Math.cos(theta), 3D));
		lamFR -= Convert.DMSmToRad(2D, 20D, 14D, 25D);
		return new PARISpos(phiFR, lamFR);
	}

	public CH1903pos CalcCH1903() {
		double phiWGS = super.phi_.doubleValue();
		double lamWGS = super.lam_.doubleValue();
		double delX = 660.077D;
		double delY = 13.551D;
		double delZ = 369.34399999999999D;
		double a1 = 6378137D;
		double f1 = 0.0033528106647474805D;
		double a2 = 6377397.1550000003D;
		double f2 = 0.0033427731821748059D;
		double sinphi = Math.sin(phiWGS);
		double cosphi = Math.cos(phiWGS);
		double sinlam = Math.sin(lamWGS);
		double coslam = Math.cos(lamWGS);
		double e1c = Convert.ESqEllips(f1);
		double e2c = Convert.ESqEllips(f2);
		double nu1 = a1 / Math.sqrt(1.0D - e1c * Math.pow(sinphi, 2D));
		double x1 = nu1 * cosphi * coslam;
		double y1 = nu1 * cosphi * sinlam;
		double z1 = nu1 * (1.0D - e1c) * sinphi;
		double x2 = x1 - delX;
		double y2 = y1 - delY;
		double z2 = z1 - delZ;
		double lamCH = Math.atan2(y2, x2);
		double p2 = Math.sqrt(Math.pow(x2, 2D) + Math.pow(y2, 2D));
		double theta = Math.atan2(z2, p2 * (1.0D - f2));
		double b2 = a2 * (1.0D - f2);
		double e2p = Convert.EprimSqEllips(f2);
		double phiCH = Math.atan2(z2 + e2p * b2 * Math.pow(Math.sin(theta), 3D), p2 - e2c * a2 * Math.pow(Math.cos(theta), 3D));
		return new CH1903pos(phiCH, lamCH);
	}

	public NLBesselpos CalcNLBessel() {
		double phiWGS = super.phi_.doubleValue();
		double lamWGS = super.lam_.doubleValue();
		double delX = 565.03999999999996D;
		double delY = 49.909999999999997D;
		double delZ = 465.83999999999997D;
		double a1 = 6378137D;
		double f1 = 0.0033528106647474805D;
		double a2 = 6377397.1550000003D;
		double f2 = 0.0033427731821748059D;
		double sinphi = Math.sin(phiWGS);
		double cosphi = Math.cos(phiWGS);
		double sinlam = Math.sin(lamWGS);
		double coslam = Math.cos(lamWGS);
		double e1c = Convert.ESqEllips(f1);
		double e2c = Convert.ESqEllips(f2);
		double nu1 = a1 / Math.sqrt(1.0D - e1c * Math.pow(sinphi, 2D));
		double x1 = nu1 * cosphi * coslam;
		double y1 = nu1 * cosphi * sinlam;
		double z1 = nu1 * (1.0D - e1c) * sinphi;
		double x2 = x1 - delX;
		double y2 = y1 - delY;
		double z2 = z1 - delZ;
		double lamNL = Math.atan2(y2, x2);
		double p2 = Math.sqrt(Math.pow(x2, 2D) + Math.pow(y2, 2D));
		double theta = Math.atan2(z2, p2 * (1.0D - f2));
		double b2 = a2 * (1.0D - f2);
		double e2p = Convert.EprimSqEllips(f2);
		double phiNL = Math.atan2(z2 + e2p * b2 * Math.pow(Math.sin(theta), 3D), p2 - e2c * a2 * Math.pow(Math.cos(theta), 3D));
		return new NLBesselpos(phiNL, lamNL);
	}

	public LUREFgeopos CalcLUREFgeo() {
		double phiWGS = super.phi_.doubleValue();
		double lamWGS = super.lam_.doubleValue();
		double delX = -262.637D;
		double delY = 75.674999999999997D;
		double delZ = 44.686D;
		double a1 = 6378137D;
		double f1 = 0.0033528106647474805D;
		double a2 = 6378388D;
		double f2 = 0.0033670033670033669D;
		double sinphi = Math.sin(phiWGS);
		double cosphi = Math.cos(phiWGS);
		double sinlam = Math.sin(lamWGS);
		double coslam = Math.cos(lamWGS);
		double e1c = Convert.ESqEllips(f1);
		double e2c = Convert.ESqEllips(f2);
		double nu1 = a1 / Math.sqrt(1.0D - e1c * Math.pow(sinphi, 2D));
		double x1 = nu1 * cosphi * coslam;
		double y1 = nu1 * cosphi * sinlam;
		double z1 = nu1 * (1.0D - e1c) * sinphi;
		double x2 = x1 - delX;
		double y2 = y1 - delY;
		double z2 = z1 - delZ;
		double lamLUX = Math.atan2(y2, x2);
		double p2 = Math.sqrt(Math.pow(x2, 2D) + Math.pow(y2, 2D));
		double theta = Math.atan2(z2, p2 * (1.0D - f2));
		double b2 = a2 * (1.0D - f2);
		double e2p = Convert.EprimSqEllips(f2);
		double phiLUX = Math.atan2(z2 + e2p * b2 * Math.pow(Math.sin(theta), 3D), p2 - e2c * a2 * Math.pow(Math.cos(theta), 3D));
		return new LUREFgeopos(phiLUX, lamLUX);
	}

	public ATFpos CalcATF() {
		double phiWGS = super.phi_.doubleValue();
		double lamWGS = super.lam_.doubleValue();
		double delX = 1118D;
		double delY = 23D;
		double delZ = 66D;
		double a1 = 6378137D;
		double f1 = 0.0033528106647474805D;
		double a2 = 6376523D;
		double f2 = 0.0032400188610999998D;
		double sinphi = Math.sin(phiWGS);
		double cosphi = Math.cos(phiWGS);
		double sinlam = Math.sin(lamWGS);
		double coslam = Math.cos(lamWGS);
		double e1c = Convert.ESqEllips(f1);
		double e2c = Convert.ESqEllips(f2);
		double nu1 = a1 / Math.sqrt(1.0D - e1c * Math.pow(sinphi, 2D));
		double x1 = nu1 * cosphi * coslam;
		double y1 = nu1 * cosphi * sinlam;
		double z1 = nu1 * (1.0D - e1c) * sinphi;
		double x2 = x1 - delX;
		double y2 = y1 - delY;
		double z2 = z1 - delZ;
		double lamATF = Math.atan2(y2, x2);
		double p2 = Math.sqrt(Math.pow(x2, 2D) + Math.pow(y2, 2D));
		double theta = Math.atan2(z2, p2 * (1.0D - f2));
		double b2 = a2 * (1.0D - f2);
		double e2p = Convert.EprimSqEllips(f2);
		double phiATF = Math.atan2(z2 + e2p * b2 * Math.pow(Math.sin(theta), 3D), p2 - e2c * a2 * Math.pow(Math.cos(theta), 3D));
		return new ATFpos(phiATF, lamATF);
	}

	public UTMpos CalcUTM(double inZone) {
		double phi = super.phi_.doubleValue();
		double lam = super.lam_.doubleValue();
		double axis_a = 6378137D;
		double f = 0.0033528106647474805D;
		double axis_b = axis_a * (1.0D - f);
		double c = Math.pow(axis_a, 2D) / axis_b;
		double e2p = Convert.EprimSqEllips(f);
		double eta2 = e2p * Math.pow(Math.cos(phi), 2D);
		double grandV = Math.sqrt(1.0D + eta2);
		double grandR = Math.pow(axis_a, 2D) / axis_b / Math.sqrt(1.0D + eta2);
		double m0 = 0.99960000000000004D;
		double A = 6367449.1458822992D;
		double B = -16038.50861391656D;
		double C = 16.832627271237278D;
		double D = -0.02198090692030899D;
		double G = 6367449.1458822992D * phi + -16038.50861391656D * Math.sin(2D * phi) + 16.832627271237278D * Math.sin(4D * phi) + -0.02198090692030899D * Math.sin(6D * phi);
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
}
