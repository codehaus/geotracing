// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   ATFpos.java

package org.geotracing.gis.proj;

;

// Referenced classes of package convert:
//            LLpos, Convert, WGS84pos

public class ATFpos extends LLpos {

	public ATFpos(double phi, double lam) {
		super.phi_ = new Double(phi);
		super.lam_ = new Double(lam);
		super.lat_ = new Double(Convert.RadToDeg(phi));
		super.lon_ = new Double(Convert.RadToDeg(lam));
	}

	public WGS84pos CalcWGS84() {
		double phiATF = super.phi_.doubleValue();
		double lamATF = super.lam_.doubleValue();
		double delX = 1118D;
		double delY = 23D;
		double delZ = 66D;
		double a1 = 6376523D;
		double f1 = 0.0032400188610999998D;
		double a2 = 6378137D;
		double f2 = 0.0033528106647474805D;
		double sinphi = Math.sin(phiATF);
		double cosphi = Math.cos(phiATF);
		double sinlam = Math.sin(lamATF);
		double coslam = Math.cos(lamATF);
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
