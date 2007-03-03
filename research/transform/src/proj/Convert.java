// Decompiled by Jad v1.5.8c. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3)
// Source File Name:   Convert.java

package proj;


public class Convert {

	public static double RadToGrad(double d) {
		return d / 0.015707963267948967D;
	}

	public static double GradToRad(double d) {
		return d * 0.015707963267948967D;
	}

	public static double RadToDeg(double d) {
		return (d * 180D) / 3.1415926535897931D;
	}

	public static double DegToRad(double d) {
		return (d * 3.1415926535897931D) / 180D;
	}

	public static double DMSmToRad(double d, double d1, double d2, double d3) {
		double d4 = DMSmToDeg(d, d1, d2, d3);
		return DegToRad(d4);
	}

	public static double DMSmToDeg(double d, double d1, double d2, double d3) {
		return d + (d1 + (d2 + d3 / 1000D) / 60D) / 60D;
	}

	public static double ESqEllips(double d) {
		return 2D * d - Math.pow(d, 2D);
	}

	public static double EprimSqEllips(double d) {
		return 1.0D / Math.pow(1.0D - d, 2D) - 1.0D;
	}

	public static String ParseDMS(double d, boolean flag) {
		char c;
		if (d < 0.0D) {
			d = -d;
			if (flag)
				c = 'S';
			else
				c = 'W';
		} else if (flag)
			c = 'N';
		else
			c = 'E';
		int i = (short) (int) d;
		double d1 = d * 60D - (double) i * 60D;
		int j = (short) (int) d1;
		d1 = d * 3600D - (double) i * 3600D - (double) j * 60D;
		return c + " " + i + " \260  " + j + " '  " + ParseDec(d1, 2) + " ''";
	}

	public static String ParseDM(double d, boolean flag) {
		char c;
		if (d < 0.0D) {
			d = -d;
			if (flag)
				c = 'S';
			else
				c = 'W';
		} else if (flag)
			c = 'N';
		else
			c = 'E';
		int i = (short) (int) d;
		double d1 = d * 60D - (double) i * 60D;
		return c + " " + i + " \260  " + ParseDec(d1, 3) + " ' ";
	}

	public static String ParseDec(double d, int i) {
		double d1 = Math.pow(10D, i);
		long l = (long) (d * d1);
		if (i == 0)
			return String.valueOf((long) ((double) l / d1));
		else
			return String.valueOf((double) l / d1);
	}

	public static String ForceDec(int i, int j) {
		StringBuffer stringbuffer = new StringBuffer(j);
		stringbuffer.setLength(j);
		String s = String.valueOf(i);
		int k = s.length();
		for (int l = 0; l < j - k; l++)
			stringbuffer.setCharAt(l, '0');

		int i1 = j - k;
		for (int j1 = 0; i1 < j; j1++) {
			stringbuffer.setCharAt(i1, s.charAt(j1));
			i1++;
		}

		return stringbuffer.toString();
	}

	public static double StrToDeg(String s) {
		int i = s.length();
		int j = 0;
		double d = 0.0D;
		double d1 = 0.0D;
		double d2 = 0.0D;
		double d3 = 0.0D;
		double d4 = 0.0D;
		boolean flag4 = false;
		boolean flag6 = false;
		boolean flag9 = false;
		boolean flag12 = false;
		boolean flag15 = false;
		if (i == 0)
			return d;
		for (; (s.charAt(j) == ' ') & (s.charAt(j) != '#'); j++) ;
		switch (s.charAt(j)) {
			case 35: // '#'
				d = 0.0D;
				break;

			case 45: // '-'
			case 83: // 'S'
			case 87: // 'W'
			case 115: // 's'
			case 119: // 'w'
				d = -1D;
				j++;
				break;

			case 43: // '+'
			case 69: // 'E'
			case 78: // 'N'
			case 101: // 'e'
			case 110: // 'n'
				d = 1.0D;
				j++;
				break;

			default:
				d = 1.0D;
				break;
		}
		if (j >= i)
			flag6 = true;
		if (!flag6) {
			double d5 = 0.0D;
			double d9 = 1.0D;
			flag4 = false;
			boolean flag = true;
			while (flag) {
				char c = s.charAt(j++);
				switch (c) {
					case 32: // ' '
						break;

					case 48: // '0'
					case 49: // '1'
					case 50: // '2'
					case 51: // '3'
					case 52: // '4'
					case 53: // '5'
					case 54: // '6'
					case 55: // '7'
					case 56: // '8'
					case 57: // '9'
						d5 = d5 * 10D + (double) (c - 48);
						if (flag4)
							d9 *= 10D;
						break;

					case 44: // ','
					case 46: // '.'
						flag4 = true;
						break;

					case 68: // 'D'
					case 100: // 'd'
						boolean flag7 = true;
						flag = false;
						break;

					case 77: // 'M'
					case 109: // 'm'
						flag9 = true;
						flag = false;
						break;

					case 83: // 'S'
					case 115: // 's'
						flag12 = true;
						flag = false;
						break;

					default:
						boolean flag8 = true;
						flag9 = true;
						flag12 = true;
						flag15 = true;
						flag = false;
						break;
				}
			}
			d1 = d5 / d9;
		}
		if (j >= i || flag4)
			flag9 = true;
		if (!flag9) {
			double d6 = 0.0D;
			double d10 = 1.0D;
			flag4 = false;
			boolean flag1 = true;
			while (flag1) {
				char c1 = s.charAt(j++);
				switch (c1) {
					case 32: // ' '
						break;

					case 48: // '0'
					case 49: // '1'
					case 50: // '2'
					case 51: // '3'
					case 52: // '4'
					case 53: // '5'
					case 54: // '6'
					case 55: // '7'
					case 56: // '8'
					case 57: // '9'
						d6 = d6 * 10D + (double) (c1 - 48);
						if (flag4)
							d10 *= 10D;
						break;

					case 44: // ','
					case 46: // '.'
						flag4 = true;
						break;

					case 77: // 'M'
					case 109: // 'm'
						boolean flag10 = true;
						flag1 = false;
						break;

					case 83: // 'S'
					case 115: // 's'
						flag12 = true;
						flag1 = false;
						break;

					default:
						boolean flag11 = true;
						flag12 = true;
						flag15 = true;
						flag1 = false;
						break;
				}
			}
			d2 = d6 / d10;
		}
		if (j >= i || flag4)
			flag12 = true;
		if (!flag12) {
			double d7 = 0.0D;
			double d11 = 1.0D;
			flag4 = false;
			boolean flag2 = true;
			while (flag2) {
				char c2 = s.charAt(j++);
				switch (c2) {
					case 32: // ' '
						break;

					case 48: // '0'
					case 49: // '1'
					case 50: // '2'
					case 51: // '3'
					case 52: // '4'
					case 53: // '5'
					case 54: // '6'
					case 55: // '7'
					case 56: // '8'
					case 57: // '9'
						d7 = d7 * 10D + (double) (c2 - 48);
						if (flag4)
							d11 *= 10D;
						break;

					case 44: // ','
					case 46: // '.'
						flag4 = true;
						break;

					case 83: // 'S'
					case 115: // 's'
						boolean flag13 = true;
						flag2 = false;
						break;

					default:
						boolean flag14 = true;
						flag2 = false;
						break;
				}
			}
			d3 = d7 / d11;
		}
		if (j >= i || flag4)
			flag15 = true;
		if (!flag15) {
			double d8 = 0.0D;
			double d12 = 1.0D;
			boolean flag5 = false;
			boolean flag3 = true;
			while (flag3) {
				char c3 = s.charAt(j++);
				switch (c3) {
					case 32: // ' '
						break;

					case 48: // '0'
					case 49: // '1'
					case 50: // '2'
					case 51: // '3'
					case 52: // '4'
					case 53: // '5'
					case 54: // '6'
					case 55: // '7'
					case 56: // '8'
					case 57: // '9'
						d8 = d8 * 10D + (double) (c3 - 48);
						if (flag5)
							d12 *= 10D;
						break;

					case 44: // ','
					case 46: // '.'
						flag5 = true;
						break;

					case 33: // '!'
					case 34: // '"'
					case 35: // '#'
					case 36: // '$'
					case 37: // '%'
					case 38: // '&'
					case 39: // '\''
					case 40: // '('
					case 41: // ')'
					case 42: // '*'
					case 43: // '+'
					case 45: // '-'
					case 47: // '/'
					default:
						flag3 = false;
						break;
				}
			}
			d4 = d8 / d12;
		}
		d *= DMSmToDeg(d1, d2, d3, d4);
		return d;
	}

	public Convert() {
	}
}
