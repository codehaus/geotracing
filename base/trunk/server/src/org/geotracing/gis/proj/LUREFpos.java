// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   LUREFpos.java

package org.geotracing.gis.proj;

import org.geotracing.gis.proj.Convert;
import org.geotracing.gis.proj.LUREFgeopos;

import java.awt.Label;

// Referenced classes of package convert:
//            Convert, LUREFgeopos

public class LUREFpos {

	public LUREFpos(double e, double n) {
		easting_ = new Double(e);
		northing_ = new Double(n);
	}

	public LUREFgeopos CalcLUREFgeo() {
		double easting = easting_.doubleValue();
		double northing = northing_.doubleValue();
		northing += 5422420.1399999997D;
		double Eprim = easting - 80000D;
		double lam0 = Convert.DMSmToRad(6D, 10D, 0.0D, 0.0D);
		double flat = 0.0033670033670033669D;
		double axis_a = 6378388D;
		double axis_b = 6356911.9461279465D;
		double c = Math.pow(6378388D, 2D) / 6356911.9461279465D;
		double e2p = Convert.EprimSqEllips(0.0033670033670033669D);
		double m0 = 1.0D;
		double A = c * ((((1.0D - e2p * 0.75D) + Math.pow(e2p, 2D) * 0.703125D) - Math.pow(e2p, 3D) * 0.68359375D) + Math.pow(e2p, 4D) * 0.67291259765625D);
		double B = ((e2p * 0.375D - Math.pow(e2p, 2D) * 0.1875D) + Math.pow(e2p, 3D) * 0.10400390625D) - Math.pow(e2p, 4D) * 0.062255859375D;
		double C = (Math.pow(e2p, 2D) * 0.08203125D - Math.pow(e2p, 3D) * 0.08203125D) + Math.pow(e2p, 4D) * 0.0650634765625D;
		double D = Math.pow(e2p, 3D) * 0.024576822916666668D - Math.pow(e2p, 4D) * 0.036865234375D;
		double phi_f = northing / A + B * Math.sin((2D * northing) / A) + C * Math.sin((4D * northing) / A) + D * Math.sin((6D * northing) / A);
		double eta2_f = e2p * Math.pow(Math.cos(phi_f), 2D);
		double grandV_f = Math.sqrt(1.0D + eta2_f);
		double grandR_f = Math.pow(6378388D, 2D) / (6356911.9461279465D * grandV_f);
		double grandY = 2D * Math.atan(Math.exp(Eprim / grandR_f)) - 1.5707963267948966D;
		double phi_ = Math.asin(Math.cos(grandY) * Math.sin(phi_f));
		double grandH = (-eta2_f * Math.tan(phi_f)) / (2D * Math.pow(grandR_f, 2D));
		double grandG = (eta2_f * Math.tan(phi_f) * (1.0D - Math.pow(phi_f, 2D))) / (4D * Math.pow(1.0D, 4D) * Math.pow(grandR_f, 4D));
		double dLam = Math.atan2(Math.tan(grandY), Math.cos(phi_f));
		double grandI = -eta2_f / (3D * Math.pow(1.0D, 3D) * Math.pow(grandR_f, 3D) * Math.cos(phi_f));
		double phi = phi_ + grandH * Math.pow(Eprim, 2D) + grandG * Math.pow(Eprim, 4D);
		double lam = lam0 + dLam + grandI * Math.pow(Eprim, 3D);
		return new LUREFgeopos(phi, lam);
	}

	public void DisplayPos(Label eastLabel, Label northLabel) {
		eastLabel.setText("  " + Convert.ParseDec(easting_.doubleValue(), 2));
		northLabel.setText("  " + Convert.ParseDec(northing_.doubleValue(), 2));
	}

	public Double easting_;
	public Double northing_;
}
