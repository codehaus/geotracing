// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   LambFpos.java

package org.geotracing.gis.proj;

import org.geotracing.gis.proj.Convert;


// Referenced classes of package convert:
//            Convert

class LambFzone {

	LambFzone(int zone) {
		double x0;
		double y0;
		double phi0;
		double r0;
		double lPhi0;
		switch (zone) {
			case 0: // '\0'
			case 2: // '\002'
				x0 = 600000D;
				y0 = 2200000D;
				phi0 = Convert.GradToRad(52D);
				r0 = 5999695.7699999996D;
				lPhi0 = 0.92155736099999996D;
				break;

			case 1: // '\001'
				x0 = 600000D;
				y0 = 1200000D;
				phi0 = Convert.GradToRad(55D);
				r0 = 5457616.6739999996D;
				lPhi0 = 0.99199666500000006D;
				break;

			case 3: // '\003'
				x0 = 600000D;
				y0 = 3200000D;
				phi0 = Convert.GradToRad(49D);
				r0 = 6591905.0800000001D;
				lPhi0 = 0.85459109799999999D;
				break;

			case 4: // '\004'
				x0 = 234.358D;
				y0 = 4185861.3689999999D;
				phi0 = Convert.GradToRad(46.850000000000001D);
				r0 = 7053300.1799999997D;
				lPhi0 = 0.80847577299999995D;
				break;

			default:
				x0 = 600000D;
				y0 = 2200000D;
				phi0 = Convert.GradToRad(52D);
				r0 = 5999695.7699999996D;
				lPhi0 = 0.92155736099999996D;
				break;
		}
		x0_ = new Double(x0);
		y0_ = new Double(y0);
		phi0_ = new Double(phi0);
		r0_ = new Double(r0);
		lPhi0_ = new Double(lPhi0);
	}

	Double x0_;
	Double y0_;
	Double phi0_;
	Double r0_;
	Double lPhi0_;
}
