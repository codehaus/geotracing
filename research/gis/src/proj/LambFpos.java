// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   LambFpos.java

package proj;

import proj.Convert;

import java.awt.Label;

// Referenced classes of package convert:
//            LambFzone, PARISpos, Convert

public class LambFpos {

	public LambFpos(double x, double y, int zone) {
		X_ = new Double(x);
		Y_ = new Double(y);
		Zone_ = new Integer(zone);
	}

	public PARISpos CalcParis() {
		double lambX = X_.doubleValue();
		double lambY = Y_.doubleValue();
		int inZone = Zone_.intValue();
		LambFzone aZone = new LambFzone(inZone);
		double x0 = aZone.x0_.doubleValue();
		double y0 = aZone.y0_.doubleValue();
		double phi0 = aZone.phi0_.doubleValue();
		double r0 = aZone.r0_.doubleValue();
		double lPhi0 = aZone.lPhi0_.doubleValue();
		double lamPar = Math.atan2(lambX - x0, (r0 + y0) - lambY) / Math.sin(phi0);
		double temp10 = Math.log((Math.pow(lambX - x0, 2D) + Math.pow((r0 + y0) - lambY, 2D)) / Math.pow(r0, 2D));
		double tempE = Math.exp(-lPhi0 + temp10 / (2D * Math.sin(phi0)));
		double temp = (1.0D - tempE) / (1.0D + tempE);
		double sinF = Math.sin(2D * Math.atan(temp));
		double tempA = (0.0034250460000000002D * sinF - 1.5707000000000001E-05D * Math.pow(sinF, 3D)) + 1.3799999999999999E-07D * Math.pow(sinF, 5D);
		double phiPar = 2D * Math.atan2(tempA + temp, 1.0D + tempA * temp);
		return new PARISpos(phiPar, lamPar);
	}

	public void DisplayPos(Label xLabel, Label yLabel) {
		xLabel.setText(Convert.ParseDec(X_.doubleValue(), 2));
		yLabel.setText("  " + Convert.ParseDec(Y_.doubleValue(), 2));
	}

	public Double X_;
	public Double Y_;
	public Integer Zone_;
}
