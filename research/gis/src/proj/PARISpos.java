// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   PARISpos.java

package proj;

import proj.Convert;
import proj.LambFpos;
import proj.LambFzone;

import java.awt.Label;

// Referenced classes of package convert:
//            Convert, LambFzone, LambFpos, WGS84pos

public class PARISpos {

	public PARISpos(double phi, double lambda) {
		latRad_ = new Double(phi);
		lonRad_ = new Double(lambda);
		latGR_ = new Double(Convert.RadToGrad(phi));
		lonGR_ = new Double(Convert.RadToGrad(lambda));
		latDEG_ = new Double(Convert.RadToDeg(phi));
		lonDEG_ = new Double(Convert.RadToDeg(lambda));
	}

	public LambFpos CalcLambF(int inZoneLambert) {
		double phiPar = latRad_.doubleValue();
		double lamPar = lonRad_.doubleValue();
		LambFzone aZone = new LambFzone(inZoneLambert);
		double x0 = aZone.x0_.doubleValue();
		double y0 = aZone.y0_.doubleValue();
		double phi0 = aZone.phi0_.doubleValue();
		double r0 = aZone.r0_.doubleValue();
		double lPhi0 = aZone.lPhi0_.doubleValue();
		double e0 = 0.082483256759999998D;
		double lPhi = Math.log(Math.tan(0.78539816339744828D + phiPar / 2D)) - (e0 / 2D) * Math.log((1.0D + e0 * Math.sin(phiPar)) / (1.0D - e0 * Math.sin(phiPar)));
		double r = r0 * Math.exp((lPhi0 - lPhi) * Math.sin(phi0));
		double gamma = lamPar * Math.sin(phi0);
		double lambX = x0 + r * Math.sin(gamma);
		double lambY = (y0 + r0) - r * Math.cos(gamma);
		return new LambFpos(lambX, lambY, inZoneLambert);
	}

	public WGS84pos CalcWGS84() {
		double phiFR = latRad_.doubleValue();
		double lamFR = lonRad_.doubleValue();
		double delX = -168D;
		double delY = -60D;
		double delZ = 320D;
		double a1 = 6378249.2000000002D;
		double f1 = 0.0034075495234250643D;
		double a2 = 6378137D;
		double f2 = 0.0033528106647474805D;
		lamFR += Convert.DMSmToRad(2D, 20D, 14D, 25D);
		double sinphi = Math.sin(phiFR);
		double cosphi = Math.cos(phiFR);
		double sinlam = Math.sin(lamFR);
		double coslam = Math.cos(lamFR);
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
		double nu2 = a2 / Math.sqrt(1.0D - e2c * Math.pow(Math.sin(phiWGS), 2D));
		return new WGS84pos(phiWGS, lamWGS);
	}

	public void DisplayPos(Label latLabel, Label lonLabel) {
		latLabel.setText(Convert.ParseDec(latGR_.doubleValue(), 6));
		lonLabel.setText("  " + Convert.ParseDec(lonGR_.doubleValue(), 6));
	}

	public Double latRad_;
	public Double lonRad_;
	public Double latGR_;
	public Double lonGR_;
	public Double latDEG_;
	public Double lonDEG_;
}
