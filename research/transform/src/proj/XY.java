package proj;

public class XY {
	public XY(double aX, double aY) {
		x = Math.round(aX);
		y = Math.round(aY);
	}

	public long x;
	public long y;

	public String toString() {
		return "x=" + x + " y=" + y;

	}


	public static int distance(XY xy1, XY xy2) {
		double deltaX = (xy1.x - xy2.x);
		double deltaY = (xy1.y - xy2.y);
		return (int) Math.round(Math.sqrt((deltaX * deltaX + deltaY * deltaY)));
	}

}