// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   UTMpos.java

package org.geotracing.gis.proj;

import org.geotracing.gis.proj.BELpos;
import org.geotracing.gis.proj.Convert;
import org.geotracing.gis.proj.ED50pos;

import java.awt.Label;

// Referenced classes of package convert:
//            Convert, ED50pos, BELpos

public class UTMpos {

	public UTMpos(double e, double n, double z) {
		easting_ = new Double(e);
		northing_ = new Double(n);
		zone_ = new Double(z);
	}

	public ED50pos CalcED50() {
		double easting = easting_.doubleValue();
		double northing = northing_.doubleValue();
		double zone = zone_.doubleValue();
		double axis_a = 6378388D;
		double f = 0.0033670033670033669D;
		double axis_b = 6356911.9461279465D;
		double c = Math.pow(6378388D, 2D) / axis_b;
		double e2p = Convert.EprimSqEllips(0.0033670033670033669D);
		double m0 = 0.99960000000000004D;
		if (zone < 0.0D) {
			northing -= 10000000D;
			zone = -zone;
		}
		double A = m0 * c * ((((1.0D - e2p * 0.75D) + Math.pow(e2p, 2D) * 0.703125D) - Math.pow(e2p, 3D) * 0.68359375D) + Math.pow(e2p, 4D) * 0.67291259765625D);
		double B = ((e2p * 0.375D - Math.pow(e2p, 2D) * 0.1875D) + Math.pow(e2p, 3D) * 0.10400390625D) - Math.pow(e2p, 4D) * 0.062255859375D;
		double C = (Math.pow(e2p, 2D) * 0.08203125D - Math.pow(e2p, 3D) * 0.08203125D) + Math.pow(e2p, 4D) * 0.0650634765625D;
		double D = Math.pow(e2p, 3D) * 0.024576822916666668D - Math.pow(e2p, 4D) * 0.036865234375D;
		double phi_f = northing / A + B * Math.sin((2D * northing) / A) + C * Math.sin((4D * northing) / A) + D * Math.sin((6D * northing) / A);
		double eta2_f = e2p * Math.pow(Math.cos(phi_f), 2D);
		double grandV_f = Math.sqrt(1.0D + eta2_f);
		double grandR_f = Math.pow(6378388D, 2D) / axis_b / grandV_f;
		double Eprim = easting - 500000D;
		double grandY = 2D * Math.atan(Math.exp(Eprim / (m0 * grandR_f))) - 1.5707963267948966D;
		double phi_ = Math.asin(Math.cos(grandY) * Math.sin(phi_f));
		double grandH = (-eta2_f * Math.tan(phi_f)) / (2D * m0 * Math.pow(grandR_f, 2D));
		double grandG = (eta2_f * Math.tan(phi_f) * (1.0D - Math.pow(phi_f, 2D))) / (4D * Math.pow(m0, 4D) * Math.pow(grandR_f, 4D));
		double dLam = Math.atan2(Math.tan(grandY), Math.cos(phi_f));
		double grandI = -eta2_f / (3D * Math.pow(m0, 3D) * Math.pow(grandR_f, 3D) * Math.cos(phi_f));
		double phi = phi_ + grandH * Math.pow(Eprim, 2D) + grandG * Math.pow(Eprim, 4D);
		double lam = Convert.DegToRad(zone * 6D - 183D) + dLam + grandI * Math.pow(Eprim, 3D);
		return new ED50pos(phi, lam);
	}

	public BELpos CalcLB72() {
		double Ep = easting_.doubleValue() - 500000D;
		double Np = northing_.doubleValue() - 5500000D;
		double X0 = 51066.207087529998D;
		double Y0 = 38576.016134799997D;
		double a1 = 1.0002878273719999D;
		double a2 = -0.018438235628899999D;
		double b1 = 8.6386970359969997E-12D;
		double b2 = 1.190646431148E-09D;
		double c1 = -4.0793193746530001E-15D;
		double c2 = 8.0886160348540005E-17D;
		double dE2N2 = Ep * Ep - Np * Np;
		double de3n2 = Math.pow(Ep, 3D) - 3D * Ep * Np * Np;
		double de2n3 = 3D * Ep * Ep * Np - Math.pow(Np, 3D);
		double xLam = (((((X0 + a1 * Ep) - a2 * Np) + b1 * dE2N2) - 2D * b2 * (Ep * Np)) + c1 * de3n2) - c2 * de2n3;
		double yLam = Y0 + a1 * Np + a2 * Ep + 2D * b1 * (Ep * Np) + b2 * dE2N2 + c1 * de2n3 + c2 * de3n2;
		return new BELpos(xLam, yLam);
	}

	public void DisplayPos(Label eastLabel, Label northLabel, Label zoneLabel) {
		eastLabel.setText("  " + Convert.ParseDec(easting_.doubleValue(), 2));
		northLabel.setText("  " + Convert.ParseDec(northing_.doubleValue(), 2));
		zoneLabel.setText("  " + Convert.ParseDec(zone_.doubleValue(), 0));
	}

	public Double easting_;
	public Double northing_;
	public Double zone_;
}
