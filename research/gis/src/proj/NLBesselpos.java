// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   NLBesselpos.java

package proj;

import proj.Convert;
import proj.LLpos;

// Referenced classes of package convert:
//            LLpos, Convert, WGS84pos, NLRDpos

public class NLBesselpos extends LLpos {

	public NLBesselpos(double phi, double lam) {
		super.phi_ = new Double(phi);
		super.lam_ = new Double(lam);
		super.lat_ = new Double(Convert.RadToDeg(phi));
		super.lon_ = new Double(Convert.RadToDeg(lam));
	}

	public WGS84pos CalcWGS84() {
		double phiNL = super.phi_.doubleValue();
		double lamNL = super.lam_.doubleValue();
		double delX = 565.03999999999996D;
		double delY = 49.909999999999997D;
		double delZ = 465.83999999999997D;
		double a1 = 6377397.1550000003D;
		double f1 = 0.0033427731821748059D;
		double a2 = 6378137D;
		double f2 = 0.0033528106647474805D;
		double sinphi = Math.sin(phiNL);
		double cosphi = Math.cos(phiNL);
		double sinlam = Math.sin(lamNL);
		double coslam = Math.cos(lamNL);
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

	public NLRDpos CalcNLRD() {
		double latNL = super.lat_.doubleValue();
		double lonNL = super.lon_.doubleValue();
		latNL -= Convert.DMSmToDeg(52D, 9D, 22D, 178D);
		lonNL -= Convert.DMSmToDeg(5D, 23D, 15D, 500D);
		double y = (latNL * 3600D) / 10000D;
		double x = (lonNL * 3600D) / 10000D;
		double a = 190066.98999999999D;
		double b = 11830.860000000001D;
		double c = 114.2D;
		double d = 32.380000000000003D;
		double e = 2.3399999999999999D;
		double f = 0.60999999999999999D;
		double rdX = 190066.98999999999D * x - 11830.860000000001D * x * y - 114.2D * x * Math.pow(y, 2D) - 32.380000000000003D * Math.pow(x, 3D) - 2.3399999999999999D * x * Math.pow(y, 3D) - 0.60999999999999999D * Math.pow(x, 3D) * y;
		double h = 309020.32000000001D;
		double j = 3638.3600000000001D;
		double k = 72.969999999999999D;
		double l = 157.94999999999999D;
		double m = 59.799999999999997D;
		double n = 6.4299999999999997D;
		double p = 0.089999999999999997D;
		double q = 0.029999999999999999D;
		double rdY = (((((309020.32000000001D * y + 3638.3600000000001D * Math.pow(x, 2D) + 72.969999999999999D * Math.pow(y, 2D)) - 157.94999999999999D * Math.pow(x, 2D) * y) + 59.799999999999997D * Math.pow(y, 3D)) - 6.4299999999999997D * Math.pow(x * y, 2D)) + 0.089999999999999997D * Math.pow(x, 4D)) - 0.029999999999999999D * Math.pow(y, 4D);
		rdX += 155000D;
		rdY += 463000D;
		return new NLRDpos(rdX, rdY);
	}
}
