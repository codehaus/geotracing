package proj;

import proj.XY;


public class XYArea {
	public XY nw;
	public XY se;

	public XYArea() {
	}

	public XYArea(long anXNW, long anYNW, long anXSE, long anYSE) {
		this(new XY(anXNW, anYNW), new XY(anXSE, anYSE));
	}

	public XYArea(XY aTopLeft, XY aBottomRight) {
		nw = aTopLeft;
		se = aBottomRight;
	}

	public double getHeight() {
		return nw.y - se.y;
	}

	public double getWidth() {
		return se.x - nw.x;
	}

	public String getId() {
		return nw.x + "_" + nw.y + "_" + se.x + "_" + se.y;

	}

	public String toString() {
		return "nw(" + nw + ") se(" + se + ")";
	}

	public boolean intersects(XYArea anXYArea) {
		return ((containsX(anXYArea.nw.x) || containsX(anXYArea.se.x)) &&
				(containsY(anXYArea.nw.y) || containsY(anXYArea.se.y))) ||
				((anXYArea.containsX(nw.x) || anXYArea.containsX(se.x)) &&
						(anXYArea.containsY(nw.y) || anXYArea.containsY(se.y)))
				;
	}

	public boolean containsX(double anX) {
		return anX >= nw.x && anX <= se.x;
	}

	public boolean containsY(double anY) {
		return anY >= se.y && anY <= nw.y;
	}

	public boolean contains(XY anXY) {
		return containsX(anXY.x) && containsY(anXY.y);
	}
}