package org.geotracing.gis.proj;

import org.geotracing.gis.proj.XY;

// http://www.tandt.be/wis/WiS/wgs84.html

public class RDtoWGS84 {


	public static void p(String s) {
		System.out.println(s);
	}


	public static XY middle(XY anXY1, XY anXY2) {
		return new XY((anXY1.x + anXY2.x) / 2.0f, (anXY1.y + anXY2.y) / 2.0f);
	}

	public static XY middleWGS84(XY anXY1, XY anXY2) {
		return calcWGS84(middle(anXY1, anXY2));
	}

	public static WGS84pos rd2WGS84pos(XY anXY) {
		NLRDpos rdPos = new NLRDpos(anXY.x, anXY.y);
		NLBesselpos nlBesselPos = rdPos.CalcNLBessel();
		WGS84pos wgs84Pos = nlBesselPos.CalcWGS84();
		return wgs84Pos;
	}

	public static XY calcWGS84(XY anXY) {
		NLRDpos rdPos = new NLRDpos(anXY.x, anXY.y);
		NLBesselpos nlBesselPos = rdPos.CalcNLBessel();
		WGS84pos wgs84Pos = nlBesselPos.CalcWGS84();
		return new XY(wgs84Pos.lat_.doubleValue(), wgs84Pos.lon_.doubleValue());
	}


	public static void main(String[] args) {
		// 115200_495600_153600_466800_1_sk8.png
		// NL 0_630000_307200_294000
		//	XY xy1 = new XY(0, 630000);
		//	XY xy2 = new XY(307200, 294000);
		XY xy1 = new XY(115200, 495600);
		XY xy2 = new XY(124800, 488400);
		// 115200_495600_124800_488400_2_sk8.png
		WGS84pos wgs84Pos1 = rd2WGS84pos(xy1);
		WGS84pos wgs84Pos2 = rd2WGS84pos(xy2);
		p(wgs84Pos1.toString());
		p(wgs84Pos2.toString());
		p("bbox=" + wgs84Pos1.lon_ + "," + wgs84Pos2.lat_ + "," + wgs84Pos2.lon_ + "," + wgs84Pos1.lat_);
		//XY latLonMiddle = middleWGS84(xy1, xy2);
		//p(latLonMiddle.toString());
	}

}