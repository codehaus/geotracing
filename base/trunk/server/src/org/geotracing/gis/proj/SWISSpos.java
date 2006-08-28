// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   SWISSpos.java

package org.geotracing.gis.proj;

import org.geotracing.gis.proj.CH1903pos;
import org.geotracing.gis.proj.Convert;

import java.awt.Label;

// Referenced classes of package convert:
//            Convert, CH1903pos

public class SWISSpos {

	public SWISSpos(double x, double y) {
		X_ = new Double(x);
		Y_ = new Double(y);
	}

	public CH1903pos CalcCH1903() {
		double XCH = X_.doubleValue();
		double YCH = Y_.doubleValue();
		double a = 6377397.1550000003D;
		double f = 0.0033427731821748059D;
		double ec = Convert.ESqEllips(f);
		double ecc = Math.sqrt(ec);
		double lam0 = Convert.DMSmToRad(7D, 26D, 22D, 500D);
		double phi0 = Convert.DMSmToRad(46D, 57D, 8D, 660D);
		double R = (a * Math.sqrt(1.0D - ec)) / (1.0D - ec * Math.pow(Math.sin(phi0), 2D));
		double phi2Prime = 2D * (Math.atan(Math.exp((YCH - 200000D) / R)) - 0.78539816339744828D);
		double lam2Prime = (XCH - 600000D) / R;
		double c = Math.sqrt(1.0D + (ec * Math.pow(Math.cos(phi0), 4D)) / (1.0D - ec));
		double equivPhi0Prime = Math.asin(Math.sin(phi0) / c);
		double sinPhiPrime = Math.cos(equivPhi0Prime) * Math.sin(phi2Prime) + Math.sin(equivPhi0Prime) * Math.cos(phi2Prime) * Math.cos(lam2Prime);
		double phiPrime = Math.asin(sinPhiPrime);
		double sinLamPrime = (Math.cos(phi2Prime) * Math.sin(lam2Prime)) / Math.cos(phiPrime);
		double lamPrime = Math.asin(sinLamPrime);
		double lam = lamPrime / c + lam0;
		double phi = NewtonRaphson(phiPrime);
		return new CH1903pos(phi, lam);
	}

	double NewtonRaphson(double initEstimate) {
		double estimate = initEstimate;
		double tol = 1.0000000000000001E-05D;
		double a = 6377397.1550000003D;
		double f = 0.0033427731821748059D;
		double ec = Convert.ESqEllips(f);
		double ecc = Math.sqrt(ec);
		double lam0 = Convert.DMSmToRad(7D, 26D, 22D, 500D);
		double phi0 = Convert.DMSmToRad(46D, 57D, 8D, 660D);
		double c = Math.sqrt(1.0D + (ec * Math.pow(Math.cos(phi0), 4D)) / (1.0D - ec));
		double equivPhi0Prime = Math.asin(Math.sin(phi0) / c);
		double K = Math.log(Math.tan(0.78539816339744828D + equivPhi0Prime / 2D)) - c * (Math.log(Math.tan(0.78539816339744828D + phi0 / 2D)) - (ecc / 2D) * Math.log((1.0D + ecc * Math.sin(phi0)) / (1.0D - ecc * Math.sin(phi0))));
		double C = (K - Math.log(Math.tan(0.78539816339744828D + initEstimate / 2D))) / c;
		double corr;
		do {
			corr = ((C + Math.log(Math.tan(0.78539816339744828D + estimate / 2D))) - (ecc / 2D) * Math.log((1.0D + ecc * Math.sin(estimate)) / (1.0D - ecc * Math.sin(estimate)))) * (((1.0D - ec * Math.pow(Math.sin(estimate), 2D)) * Math.cos(estimate)) / (1.0D - ec));
			estimate -= corr;
		} while (Math.abs(corr) > tol);
		return estimate;
	}

	public void DisplayPos(Label xLabel, Label yLabel) {
		xLabel.setText(Convert.ParseDec(X_.doubleValue(), 3));
		yLabel.setText("  " + Convert.ParseDec(Y_.doubleValue(), 3));
	}

	public Double X_;
	public Double Y_;
}
