package proj;

// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   WGS84.java

import java.applet.Applet;
import java.applet.AppletContext;
import java.awt.*;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

public class WGS84Applet extends Applet {

	void Calculate() {
		WGS84pos aWGS84 = new WGS84pos(50D, 5D);
		aWGS84.ReadLLpos(t_lat_WGS84, t_lon_WGS84);
		aWGS84.DisplayPosD(lDEG_lat_WGS84, lDEG_lon_WGS84);
		aWGS84.DisplayPosDM(lDM__lat_WGS84, lDM__lon_WGS84);
		aWGS84.DisplayPosDMS(lDMS_lat_WGS84, lDMS_lon_WGS84);
		CH1903pos aCH1903 = aWGS84.CalcCH1903();
		aCH1903.DisplayPosD(lDEG_lat_CH1903, lDEG_lon_CH1903);
		aCH1903.DisplayPosDM(lDM__lat_CH1903, lDM__lon_CH1903);
		aCH1903.DisplayPosDMS(lDMS_lat_CH1903, lDMS_lon_CH1903);
		SWISSpos aSWISS = aCH1903.CalcSWISS();
		aSWISS.DisplayPos(l_X_CH, l_Y_CH);
		LUREFgeopos aLUREFgeo = aWGS84.CalcLUREFgeo();
		aLUREFgeo.DisplayPosD(lDEG_lat_LUREF, lDEG_lon_LUREF);
		aLUREFgeo.DisplayPosDM(lDM__lat_LUREF, lDM__lon_LUREF);
		aLUREFgeo.DisplayPosDMS(lDMS_lat_LUREF, lDMS_lon_LUREF);
		NLBesselpos aNLgeo = aWGS84.CalcNLBessel();
		aNLgeo.DisplayPosD(lDEG_lat_BESSEL, lDEG_lon_BESSEL);
		aNLgeo.DisplayPosDM(lDM__lat_BESSEL, lDM__lon_BESSEL);
		aNLgeo.DisplayPosDMS(lDMS_lat_BESSEL, lDMS_lon_BESSEL);
		NLRDpos aNLRD = aNLgeo.CalcNLRD();
		aNLRD.DisplayPos(l_NLRD_X, l_NLRD_Y);
		LUREFpos aLUREF = aLUREFgeo.CalcLUREF();
		aLUREF.DisplayPos(l_LUREF_E, l_LUREF_N);
		PARISpos aParis = aWGS84.CalcPARIS();
		aParis.DisplayPos(lGR_lat_Paris, lGR_lon_Paris);
		LambFpos aZ1 = aParis.CalcLambF(1);
		aZ1.DisplayPos(l_X_Z1, l_Y_Z1);
		LambFpos aZ2 = aParis.CalcLambF(2);
		aZ2.DisplayPos(l_X_Z2, l_Y_Z2);
		LambFpos aZ3 = aParis.CalcLambF(3);
		aZ3.DisplayPos(l_X_Z3, l_Y_Z3);
		LambFpos aZ4 = aParis.CalcLambF(4);
		aZ4.DisplayPos(l_X_Z4, l_Y_Z4);
		ED50pos aED50 = aWGS84.CalcED50();
		aED50.DisplayPosD(lDEG_lat_ED50, lDEG_lon_ED50);
		aED50.DisplayPosDM(lDM__lat_ED50, lDM__lon_ED50);
		aED50.DisplayPosDMS(lDMS_lat_ED50, lDMS_lon_ED50);
		double lat = ((LLpos) (aWGS84)).lon_.doubleValue();
		short zone = (short) (int) ((lat + 186D) / 6D);
		if (zone > 60)
			zone -= 60;
		UTMpos aUTM = aED50.CalcUTM(zone);
		aUTM.DisplayPos(l_Easting, l_Northing, l_Zone);
		if (zone != 31)
			aUTM = aED50.CalcUTM(31D);
		BELpos aBELpos = aUTM.CalcLB72();
		aBELpos.DisplayPos(l_BEL_XBD, l_BEL_YBD);
		l_Map.setText(aBELpos.CalculateMap());
		l_Page.setText(aBELpos.CalculatePage());
		MGRSpos aMGRS = aED50.CalcMGRS(zone);
		l_MGRS_M.setText(aMGRS.mgrs_metric);
		l_MGRS_H.setText(aMGRS.mgrs_hecto);
		l_MGRS_K.setText(aMGRS.mgrs_kilo);
	}

	public void init() {
		setLayout(new GridLayout(34, 3));
		b_1GeoStar = new Button("Show Map (small)");
		b_2GeoStar = new Button("Show Map (medium)");
		b_3GeoStar = new Button("Show Map (large)");
		t_lat_WGS84 = new TextField(20);
		t_lon_WGS84 = new TextField(20);
		l_X_Z1 = new Label("");
		l_Y_Z1 = new Label("");
		l_X_Z2 = new Label("");
		l_Y_Z2 = new Label("");
		l_X_Z3 = new Label("");
		l_Y_Z3 = new Label("");
		l_X_Z4 = new Label("");
		l_Y_Z4 = new Label("");
		l_X_CH = new Label("");
		l_Y_CH = new Label("");
		lGR_lat_Paris = new Label("");
		lGR_lon_Paris = new Label("");
		lDEG_lat_ED50 = new Label("");
		lDEG_lon_ED50 = new Label("");
		lDM__lat_ED50 = new Label("");
		lDM__lon_ED50 = new Label("");
		lDMS_lat_ED50 = new Label("");
		lDMS_lon_ED50 = new Label("");
		lDEG_lat_WGS84 = new Label("");
		lDEG_lon_WGS84 = new Label("");
		lDM__lat_WGS84 = new Label("");
		lDM__lon_WGS84 = new Label("");
		lDMS_lat_WGS84 = new Label("");
		lDMS_lon_WGS84 = new Label("");
		lDEG_lat_CH1903 = new Label("");
		lDEG_lon_CH1903 = new Label("");
		lDM__lat_CH1903 = new Label("");
		lDM__lon_CH1903 = new Label("");
		lDMS_lat_CH1903 = new Label("");
		lDMS_lon_CH1903 = new Label("");
		lDEG_lat_LUREF = new Label("");
		lDEG_lon_LUREF = new Label("");
		lDM__lat_LUREF = new Label("");
		lDM__lon_LUREF = new Label("");
		lDMS_lat_LUREF = new Label("");
		lDMS_lon_LUREF = new Label("");
		lDEG_lat_BESSEL = new Label("");
		lDEG_lon_BESSEL = new Label("");
		lDM__lat_BESSEL = new Label("");
		lDM__lon_BESSEL = new Label("");
		lDMS_lat_BESSEL = new Label("");
		lDMS_lon_BESSEL = new Label("");
		l_LUREF_E = new Label("");
		l_LUREF_N = new Label("");
		l_NLRD_X = new Label("");
		l_NLRD_Y = new Label("");
		l_BEL_XBD = new Label("");
		l_BEL_YBD = new Label("");
		l_Map = new Label("");
		l_Page = new Label("");
		l_Easting = new Label("");
		l_Northing = new Label("");
		l_Zone = new Label("");
		l_MGRS_M = new Label("");
		l_MGRS_H = new Label("");
		l_MGRS_K = new Label("");
		add(new Label(""));
		add(new Label("  Latitude"));
		add(new Label("  Longitude"));
		add(new Label("  WGS84 (Str)"));
		add(t_lat_WGS84);
		add(t_lon_WGS84);
		add(new Label("  WGS84 (Deg)"));
		add(lDEG_lat_WGS84);
		add(lDEG_lon_WGS84);
		add(new Label("  WGS84 (Naut.)"));
		add(lDM__lat_WGS84);
		add(lDM__lon_WGS84);
		add(new Label("  WGS84 (DMS)"));
		add(lDMS_lat_WGS84);
		add(lDMS_lon_WGS84);
		add(b_1GeoStar);
		add(b_2GeoStar);
		add(b_3GeoStar);
		add(new Label("  ED50 (Deg)"));
		add(lDEG_lat_ED50);
		add(lDEG_lon_ED50);
		add(new Label("  ED50 (Naut.)"));
		add(lDM__lat_ED50);
		add(lDM__lon_ED50);
		add(new Label("  ED50 (DMS)"));
		add(lDMS_lat_ED50);
		add(lDMS_lon_ED50);
		add(new Label("  CH1903 (Deg)"));
		add(lDEG_lat_CH1903);
		add(lDEG_lon_CH1903);
		add(new Label("  CH1903 (Naut.)"));
		add(lDM__lat_CH1903);
		add(lDM__lon_CH1903);
		add(new Label("  CH1903 (DMS)"));
		add(lDMS_lat_CH1903);
		add(lDMS_lon_CH1903);
		add(new Label("  LUREF (Deg)"));
		add(lDEG_lat_LUREF);
		add(lDEG_lon_LUREF);
		add(new Label("  LUREF (Naut.)"));
		add(lDM__lat_LUREF);
		add(lDM__lon_LUREF);
		add(new Label("  LUREF (DMS)"));
		add(lDMS_lat_LUREF);
		add(lDMS_lon_LUREF);
		add(new Label("  NL geo. (Deg)"));
		add(lDEG_lat_BESSEL);
		add(lDEG_lon_BESSEL);
		add(new Label("  NL geo. (Naut.)"));
		add(lDM__lat_BESSEL);
		add(lDM__lon_BESSEL);
		add(new Label("  NL geo. (DMS)"));
		add(lDMS_lat_BESSEL);
		add(lDMS_lon_BESSEL);
		add(new Label("  Meridian Paris (gr)"));
		add(lGR_lat_Paris);
		add(lGR_lon_Paris);
		add(new Label(" "));
		add(new Label("X"));
		add(new Label("  Y"));
		add(new Label("  Lambert I"));
		add(l_X_Z1);
		add(l_Y_Z1);
		add(new Label("  Lambert II"));
		add(l_X_Z2);
		add(l_Y_Z2);
		add(new Label("  Lambert III"));
		add(l_X_Z3);
		add(l_Y_Z3);
		add(new Label("  Lambert IV"));
		add(l_X_Z4);
		add(l_Y_Z4);
		add(new Label("  Swiss Grid"));
		add(l_X_CH);
		add(l_Y_CH);
		add(new Label("  LUREF(Easting/Northing)"));
		add(l_LUREF_E);
		add(l_LUREF_N);
		add(new Label("  NLRD "));
		add(l_NLRD_X);
		add(l_NLRD_Y);
		add(new Label("  Lambert(B)"));
		add(l_BEL_XBD);
		add(l_BEL_YBD);
		add(new Label("  IGN (B) map reference:"));
		add(l_Map);
		add(new Label("  1/50000"));
		add(new Label("  IGN (B) atlas page:"));
		add(l_Page);
		add(new Label("  1/100000"));
		add(new Label("  UTM Easting/Northing"));
		add(l_Easting);
		add(l_Northing);
		add(new Label("  UTM Zone"));
		add(l_Zone);
		add(new Label(""));
		add(new Label("  MGRS metric"));
		add(new Label("  MGRS hectometric"));
		add(new Label("  MGRS kilometric"));
		add(l_MGRS_M);
		add(l_MGRS_H);
		add(l_MGRS_K);
	}

	public boolean handleEvent(Event e) {
		if ((e.target instanceof TextField) && e.id == 1001) {
			Calculate();
			return true;
		}
		if ((e.target instanceof Button) && e.id == 1001) {
			int scale;
			if (e.arg == "Show Map (large)")
				scale = 0x186a0;
			else if (e.arg == "Show Map (medium)")
				scale = 25000;
			else if (e.arg == "Show Map (small)")
				scale = 10000;
			else
				return false;
			try {
				String lat = lDEG_lat_WGS84.getText();
				String lon = lDEG_lon_WGS84.getText();
				lon = lon.substring(2);
				AppletContext ac = getAppletContext();
				URL myURL = new URL("http://www.mapblast.com/gif?&FAM=myblast&CT=" + lat + ":" + lon + ":" + scale + "&IC=" + lat + ":" + lon + ":8:&W=640&H=480&DU=KM");
				ac.showDocument(myURL, "test");
			} catch (MalformedURLException _ex) {
				System.out.println("Bad URL");
			} catch (IOException _ex) {
				System.out.println("Bad Connection");
			} catch (IndexOutOfBoundsException _ex) {
				System.out.println("Bad lon");
			}
			return true;
		} else {
			return false;
		}
	}

	public WGS84Applet() {
	}

	Button b_1GeoStar;
	Button b_2GeoStar;
	Button b_3GeoStar;
	TextField t_lat_WGS84;
	TextField t_lon_WGS84;
	Label l_X_Z1;
	Label l_Y_Z1;
	Label l_X_Z2;
	Label l_Y_Z2;
	Label l_X_Z3;
	Label l_Y_Z3;
	Label l_X_Z4;
	Label l_Y_Z4;
	Label l_X_CH;
	Label l_Y_CH;
	Label lGR_lat_Paris;
	Label lGR_lon_Paris;
	Label lDEG_lat_ED50;
	Label lDEG_lon_ED50;
	Label lDM__lat_ED50;
	Label lDM__lon_ED50;
	Label lDMS_lat_ED50;
	Label lDMS_lon_ED50;
	Label lDEG_lat_WGS84;
	Label lDEG_lon_WGS84;
	Label lDM__lat_WGS84;
	Label lDM__lon_WGS84;
	Label lDMS_lat_WGS84;
	Label lDMS_lon_WGS84;
	Label lDEG_lat_CH1903;
	Label lDEG_lon_CH1903;
	Label lDM__lat_CH1903;
	Label lDM__lon_CH1903;
	Label lDMS_lat_CH1903;
	Label lDMS_lon_CH1903;
	Label lDEG_lat_LUREF;
	Label lDEG_lon_LUREF;
	Label lDM__lat_LUREF;
	Label lDM__lon_LUREF;
	Label lDMS_lat_LUREF;
	Label lDMS_lon_LUREF;
	Label lDEG_lat_BESSEL;
	Label lDEG_lon_BESSEL;
	Label lDM__lat_BESSEL;
	Label lDM__lon_BESSEL;
	Label lDMS_lat_BESSEL;
	Label lDMS_lon_BESSEL;
	Label l_LUREF_E;
	Label l_LUREF_N;
	Label l_NLRD_X;
	Label l_NLRD_Y;
	Label l_BEL_XBD;
	Label l_BEL_YBD;
	Label l_Map;
	Label l_Page;
	Label l_Easting;
	Label l_Northing;
	Label l_Zone;
	Label l_MGRS_M;
	Label l_MGRS_H;
	Label l_MGRS_K;
}
