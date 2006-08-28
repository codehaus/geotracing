// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   LUREFgeopos.java

package org.geotracing.gis.proj;

import org.geotracing.gis.proj.Convert;
import org.geotracing.gis.proj.LLpos;


// Referenced classes of package convert:
//            LLpos, Convert, WGS84pos, LUREFpos

public class LUREFgeopos extends LLpos {

	public LUREFgeopos(double phi, double lam) {
		super.phi_ = new Double(phi);
		super.lam_ = new Double(lam);
		super.lat_ = new Double(Convert.RadToDeg(phi));
		super.lon_ = new Double(Convert.RadToDeg(lam));
	}

	public WGS84pos CalcWGS84() {
		double phiLUX = super.phi_.doubleValue();
		double lamLUX = super.lam_.doubleValue();
		double delX = -262.637D;
		double delY = 75.674999999999997D;
		double delZ = 44.686D;
		double a1 = 6378388D;
		double f1 = 0.0033670033670033669D;
		double a2 = 6378137D;
		double f2 = 0.0033528106647474805D;
		double sinphi = Math.sin(phiLUX);
		double cosphi = Math.cos(phiLUX);
		double sinlam = Math.sin(lamLUX);
		double coslam = Math.cos(lamLUX);
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
		double e2p = Convert.EprimSqEllips(f2);
		double phiWGS = Math.atan2(z2 + e2p * b2 * Math.pow(Math.sin(theta), 3D), p2 - e2c * a2 * Math.pow(Math.cos(theta), 3D));
		return new WGS84pos(phiWGS, lamWGS);
	}

	public LUREFpos CalcLUREF() {
		double phi = super.phi_.doubleValue();
		double lam = super.lam_.doubleValue();
		double flat = 0.0033670033670033669D;
		double axis_a = 6378388D;
		double axis_b = 6356911.9461279465D;
		double c = Math.pow(6378388D, 2D) / 6356911.9461279465D;
		double e2p = Convert.EprimSqEllips(0.0033670033670033669D);
		double m0 = 1.0D;
		double eta2 = e2p * Math.pow(Math.cos(phi), 2D);
		double grandV = Math.sqrt(1.0D + eta2);
		double grandR = Math.pow(6378388D, 2D) / (6356911.9461279465D * Math.sqrt(1.0D + eta2));
		double A = 6367654.5001177313D;
		double B = -16107.034628386167D;
		double C = 16.976225262629406D;
		double D = -0.022262392837136699D;
		double G = 6367654.5001177313D * phi + -16107.034628386167D * Math.sin(2D * phi) + 16.976225262629406D * Math.sin(4D * phi) + -0.022262392837136699D * Math.sin(6D * phi);
		double Gbar = grandR * phi;
		double lam0 = Convert.DMSmToRad(6D, 10D, 0.0D, 0.0D);
		double dLam = lam - lam0;
		double Nprim = grandR * Math.atan2(Math.tan(phi), Math.cos(dLam));
		double grandF = (3D * grandR * eta2 * Math.pow(Math.cos(phi), 3D) * Math.sin(phi)) / 8D;
		double Eprim = grandR * Math.log(Math.tan(0.78539816339744828D + Math.asin(Math.cos(phi) * Math.sin(dLam)) / 2D));
		double grandH = (grandR * eta2 * Math.pow(Math.cos(phi), 3D)) / 6D;
		double easting = 80000D + Eprim + grandH * Math.pow(dLam, 3D);
		double northing = Nprim + (G - Gbar) + grandF * Math.pow(dLam, 4D);
		northing -= 5422420.1399999997D;
		return new LUREFpos(easting, northing);
	}
}
