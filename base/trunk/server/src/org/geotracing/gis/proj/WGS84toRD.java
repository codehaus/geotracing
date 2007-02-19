package org.geotracing.gis.proj;

import org.geotracing.gis.proj.XY;

// http://www.tandt.be/wis/WiS/wgs84.html

public class WGS84toRD {


	public static XY calculate(String latString, String lonString) {
		String s = latString + "#";
		double lat = Convert.StrToDeg(s);

		String s1 = lonString + "#";
		double lon = Convert.StrToDeg(s1);

		return calculate(lat, lon);
	}


	public static XY calculate(double lat, double lon) {
		double phi_ = Convert.DegToRad(lat);
		double lam_ = Convert.DegToRad(lon);
		NLBesselpos nlBesselPos = calcNLBessel(phi_, lam_);
		NLRDpos aNLRD = nlBesselPos.CalcNLRD();

		return new XY(aNLRD.X_.doubleValue(), aNLRD.Y_.doubleValue());
	}

	public static void p(String s) {
		System.out.println(s);
	}

	public static NLBesselpos calcNLBessel(double phiWGS, double lamWGS) {
		//double phiWGS = super.phi_.doubleValue();
		//double lamWGS = super.lam_.doubleValue();
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

	public static void main(String[] args) {
		if (args.length != 2) {
			p("Usage: org.geotracing.gis.proj.WGS84toRD lon lat");
			System.exit(0);
		}

		XY xy = calculate(args[1], args[0]);
		p(xy.x + " " + xy.y);
	}

}