// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   NLRDpos.java

package org.geotracing.gis.proj;

import org.geotracing.gis.proj.Convert;
import org.geotracing.gis.proj.NLBesselpos;

import java.awt.Label;

// Referenced classes of package convert:
//            Convert, NLBesselpos
/*
NLRD position expressed as X and Y

means Rijksdriehoeksmeting Nederland.
are inputed in meters
are computed in meters
cartesian  coordinates with a false origin,
relative to the Bessel1842 datum
used on the maps edited by  Topografische Dienst (Emmen)
*/

public class NLRDpos {

	public NLRDpos(double x, double y) {
		X_ = new Double(x);
		Y_ = new Double(y);
	}

	public NLBesselpos CalcNLBessel() {
		double rdX = X_.doubleValue();
		double rdY = Y_.doubleValue();
		double x = (rdX - 155000D) / 100000D;
		double y = (rdY - 463000D) / 100000D;
		double a = 3236.0329999999999D;
		double b = 32.591999999999999D;
		double c = 0.247D;
		double d = 0.84999999999999998D;
		double e = 0.066000000000000003D;
		double f = 0.0050000000000000001D;
		double g = 0.017000000000000001D;
		double dLatSec = ((3236.0329999999999D * y - 32.591999999999999D * Math.pow(x, 2D) - 0.247D * Math.pow(y, 2D) - 0.84999999999999998D * Math.pow(x, 2D) * y - 0.066000000000000003D * Math.pow(y, 3D)) + 0.0050000000000000001D * Math.pow(x, 4D)) - 0.017000000000000001D * Math.pow(x * y, 2D);
		double p = 5261.3029999999999D;
		double q = 105.97799999999999D;
		double r = 2.4580000000000002D;
		double s = 0.81899999999999995D;
		double t = 0.056000000000000001D;
		double dLonSec = (((5261.3029999999999D * x + 105.97799999999999D * x * y + 2.4580000000000002D * x * Math.pow(y, 2D)) - 0.81899999999999995D * Math.pow(x, 3D)) + 0.056000000000000001D * x * Math.pow(y, 3D)) - 0.056000000000000001D * Math.pow(x, 3D) * y;
		double lat = Convert.DegToRad(dLatSec / 3600D) + Convert.DMSmToRad(52D, 9D, 22D, 178D);
		double lon = Convert.DegToRad(dLonSec / 3600D) + Convert.DMSmToRad(5D, 23D, 15D, 500D);
		return new NLBesselpos(lat, lon);
	}

	public void DisplayPos(Label xLabel, Label yLabel) {
		xLabel.setText(Convert.ParseDec(X_.doubleValue(), 3));
		yLabel.setText("  " + Convert.ParseDec(Y_.doubleValue(), 3));
	}

	public Double X_;
	public Double Y_;
}
