// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   LLpos.java

package org.geotracing.gis.proj;

import org.geotracing.gis.proj.Convert;

import java.awt.*;

// Referenced classes of package convert:
//            Convert

public abstract class LLpos {

	public void ReadLLpos(TextField textfield, TextField textfield1) {
		try {
			String s = textfield.getText() + "#";
			double d = Convert.StrToDeg(s);
			lat_ = new Double(d);
			phi_ = new Double(Convert.DegToRad(d));
		} catch (NumberFormatException _ex) {
			textfield.setText(String.valueOf(lat_));
		}
		try {
			String s1 = textfield1.getText() + "#";
			double d1 = Convert.StrToDeg(s1);
			lon_ = new Double(d1);
			lam_ = new Double(Convert.DegToRad(d1));
		} catch (NumberFormatException _ex) {
			textfield1.setText(String.valueOf(lon_));
		}
	}

	public void DisplayPosD(Label label, Label label1) {
		double d = lat_.doubleValue();
		double d1 = lon_.doubleValue();
		label.setText(Convert.ParseDec(d, 6));
		label1.setText("  " + Convert.ParseDec(d1, 6));
	}

	public void DisplayPosDM(Label label, Label label1) {
		double d = lat_.doubleValue();
		double d1 = lon_.doubleValue();
		label.setText(Convert.ParseDM(d, true));
		label1.setText("  " + Convert.ParseDM(d1, false));
	}

	public void DisplayPosDMS(Label label, Label label1) {
		double d = lat_.doubleValue();
		double d1 = lon_.doubleValue();
		label.setText(Convert.ParseDMS(d, true));
		label1.setText("  " + Convert.ParseDMS(d1, false));
	}

	public LLpos() {
	}

	public String toString() {
		return "lat=" + lat_ + " lon=" + lon_;
	}

	public Double phi_;
	public Double lam_;
	public Double lat_;
	public Double lon_;
}
