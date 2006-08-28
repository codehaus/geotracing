// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   BD72pos.java

package org.geotracing.gis.proj;


// Referenced classes of package convert:
//            LLpos, Convert, BELpos, WGS84pos

public class BD72pos extends LLpos {

	public BD72pos(double phi, double lam) {
		super.phi_ = new Double(phi);
		super.lam_ = new Double(lam);
		super.lat_ = new Double(Convert.RadToDeg(phi));
		super.lon_ = new Double(Convert.RadToDeg(lam));
	}

	public BELpos CalcBEL() {
		double n = 0.77164219280000002D;
		double K = 11565915.812935D;
		double lam0 = Convert.DMSmToRad(4D, 21D, 24D, 983D);
		double rot = Convert.DMSmToRad(0.0D, 0.0D, 29D, 298.5D);
		double lat = super.phi_.doubleValue();
		double lon = super.lam_.doubleValue();
		double eccent = Math.sqrt(Convert.ESqEllips(0.0033670033670033669D));
		double tgCoLat = Math.tan(0.78539816339744828D - lat / 2D) * Math.pow((1.0D + eccent * Math.sin(lat)) / (1.0D - eccent * Math.sin(lat)), eccent / 2D);
		double rho = 11565915.812935D * Math.pow(tgCoLat, 0.77164219280000002D);
		double theta = 0.77164219280000002D * (lon - lam0);
		double lambX = 150000.01256D + rho * Math.sin(theta - rot);
		double lambY = 5400088.4378000004D - rho * Math.cos(theta - rot);
		return new BELpos(lambX, lambY);
	}

	public WGS84pos CalcWGS84() {
		double phiBD = super.phi_.doubleValue();
		double lamBD = super.lam_.doubleValue();
		double delX = 64.888000000000005D;
		double delY = 97.039000000000001D;
		double delZ = -4.601D;
		double a1 = 6378388D;
		double f1 = 0.0033670033670033669D;
		double a2 = 6378137D;
		double f2 = 0.0033528106647474805D;
		double sinphi = Math.sin(phiBD);
		double cosphi = Math.cos(phiBD);
		double sinlam = Math.sin(lamBD);
		double coslam = Math.cos(lamBD);
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
