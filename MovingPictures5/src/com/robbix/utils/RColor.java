package com.robbix.utils;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.awt.Color;

public class RColor extends Color
{
	private static final long serialVersionUID = 8976579005555051976L;
	
	public static final RColor CLEAR = new RColor(0, 0, 0, 0);
	public static final RColor BLACK = new RColor(0, 0, 0, 255);
	public static final RColor WHITE = new RColor(255, 255, 255, 255);
	
	public static RColor fromHSB(float[] hsb)
	{
		return new RColor(HSBtoRGBInt(hsb));
	}
	
	public static float[] toHSB(Color c, float[] hsb)
	{
		return Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), hsb);
	}
	
	public static float[] RGBtoHSB(int[] rgb, float[] hsb)
	{
		return Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);
	}
	
	public static int HSBtoRGBInt(float[] hsb)
	{
		return Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
	}
	
	public static RColor getGray(int value, int alpha)
	{
		return new RColor(value, value, value, alpha);
	}
	
	public static RColor getGray(int value)
	{
		return getGray(value, 255);
	}
	
	public static RColor getGray(double value, double alpha)
	{
		int iValue = floatToInt(value, 255);
		return new RColor(iValue, iValue, iValue, floatToInt(alpha, 255));
	}
	
	public static RColor getGray(double value)
	{
		return getGray(value, 1.0);
	}
	
	public static RColor getHue(int hue)
	{
		return getHue(intToFloat(hue, 360));
	}
	
	public static RColor getHue(double hue)
	{
		return new RColor(Color.getHSBColor((float)hue, 1, 1));
	}
	
	public RColor(int argb)
	{
		super(argb);
	}
	
	public RColor(int r, int g, int b)
	{
		super(r, g, b);
	}
	
	public RColor(int r, int g, int b, int a)
	{
		super(r, g, b, a);
	}
	
	public RColor(int[] i)
	{
		super(i[0], i[1], i[2], i.length > 3 ? i[3] : 255);
	}
	
	public RColor(Color c)
	{
		super(c.getRed(), c.getGreen(), c.getBlue(), c.getAlpha());
	}
	
	public int[] getRGBA()
	{
		return new int[]{getRed(), getGreen(), getBlue(), getAlpha()};
	}
	
	public void getRGB(int[] i)
	{
		i[0] = getRed();
		i[1] = getGreen();
		i[2] = getBlue();
	}
	
	public void getRGBA(int[] i)
	{
		getRGB(i);
		i[3] = getAlpha();
	}
	
	public float[] getHSB()
	{
		return Color.RGBtoHSB(getRed(), getGreen(), getBlue(), null);
	}
	
	public void getHSB(float[] f)
	{
		Color.RGBtoHSB(getRed(), getGreen(), getBlue(), f);
	}
	
	public void getHSBA(float[] f)
	{
		getHSB(f);
		f[3] = (float) intToFloat(getAlpha(), 255);
	}
	
	public RColor invert()
	{
		return new RColor(255 - getRed(), 255 - getGreen(), 255 - getBlue(), getAlpha());
	}
	
	public RColor toGray()
	{
		int avg = (getRed() + getGreen() + getBlue()) / 3;
		return getGray(avg, getAlpha());
	}
	
	public RColor toBlackWhite()
	{
		int avg = (getRed() + getGreen() + getBlue()) / 3;
		return avg > 127 ? WHITE : BLACK;
	}
	
	public RColor toOpaque()
	{
		return new RColor(getRed(), getGreen(), getBlue(), 255);
	}
	
	public RColor toTranslucent(double alpha)
	{
		return toTranslucent(floatToInt(alpha, 255));
	}
	
	public RColor toTranslucent(int alpha)
	{
		return new RColor(getRed(), getGreen(), getBlue(), alpha);
	}
	
	public boolean isOpaque()
	{
		return getAlpha() == 255;
	}
	
	public boolean isTranslucent()
	{
		return getAlpha() < 255;
	}
	
	public boolean isClear()
	{
		return getAlpha() == 0;
	}
	
	private static int floatToInt(double d, int max)
	{
		if (d < 0 || d > 1)
			throw new IllegalArgumentException("Must be between 0 and 1");
		
		return max(min((int) (max * d), max), 0);
	}
	
	private static double intToFloat(int i, int max)
	{
		if (i < 0 || i > max)
			throw new IllegalArgumentException("Must be between 0 and " + max);
		
		return i / (double)max; 
	}
}
