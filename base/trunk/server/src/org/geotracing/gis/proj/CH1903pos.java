// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   CH1903pos.java

package org.geotracing.gis.proj;


// Referenced classes of package convert:
//            LLpos, Convert, WGS84pos, SWISSpos

public class CH1903pos extends LLpos {

	public CH1903pos(double phi, double lam) {
		super.phi_ = new Double(phi);
		super.lam_ = new Double(lam);
		super.lat_ = new Double(Convert.RadToDeg(phi));
		super.lon_ = new Double(Convert.RadToDeg(lam));
	}

	public WGS84pos CalcWGS84() {
		double phiCH = super.phi_.doubleValue();
		double lamCH = super.lam_.doubleValue();
		double delX = 660.077D;
		double delY = 13.551D;
		double delZ = 369.34399999999999D;
		double a1 = 6377397.1550000003D;
		double f1 = 0.0033427731821748059D;
		double a2 = 6378137D;
		double f2 = 0.0033528106647474805D;
		double sinphi = Math.sin(phiCH);
		double cosphi = Math.cos(phiCH);
		double sinlam = Math.sin(lamCH);
		double coslam = Math.cos(lamCH);
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

	public SWISSpos CalcSWISS() {
		double phi = super.phi_.doubleValue();
		double lam = super.lam_.doubleValue();
		double a = 6377397.1550000003D;
		double f = 0.0033427731821748059D;
		double ec = Convert.ESqEllips(f);
		double ecc = Math.sqrt(ec);
		double lam0 = Convert.DMSmToRad(7D, 26D, 22D, 500D);
		double phi0 = Convert.DMSmToRad(46D, 57D, 8D, 660D);
		double c = Math.sqrt(1.0D + (ec * Math.pow(Math.cos(phi0), 4D)) / (1.0D - ec));
		double phi0Prime = Math.asin(Math.sin(phi0) / c);
		double K = Math.log(Math.tan(0.78539816339744828D + phi0Prime / 2D)) - c * (Math.log(Math.tan(0.78539816339744828D + phi0 / 2D)) - (ecc / 2D) * Math.log((1.0D + ecc * Math.sin(phi0)) / (1.0D - ecc * Math.sin(phi0))));
		double lamPrime = c * (lam - lam0);
		double w = c * (Math.log(Math.tan(0.78539816339744828D + phi / 2D)) - (ecc / 2D) * Math.log((1.0D + ecc * Math.sin(phi)) / (1.0D - ecc * Math.sin(phi)))) + K;
		double phiPrime = 2D * (Math.atan(Math.exp(w)) - 0.78539816339744828D);
		double sinPhi2Prime = Math.cos(phi0Prime) * Math.sin(phiPrime) - Math.sin(phi0Prime) * Math.cos(phiPrime) * Math.cos(lamPrime);
		double phi2Prime = Math.asin(sinPhi2Prime);
		double sinLam2Prime = (Math.cos(phiPrime) * Math.sin(lamPrime)) / Math.cos(phi2Prime);
		double lam2Prime = Math.asin(sinLam2Prime);
		double R = (a * Math.sqrt(1.0D - ec)) / (1.0D - ec * Math.pow(Math.sin(phi0), 2D));
		double YCH = R * Math.log(Math.tan(0.78539816339744828D + phi2Prime / 2D)) + 200000D;
		double XCH = R * lam2Prime + 600000D;
		return new SWISSpos(XCH, YCH);
	}
}
