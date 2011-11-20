package com.robbix.mp5.utils;

import java.awt.Color;
import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

/**
 * Yes, it's a Utils class. I know.
 * 
 * It has commonly-used methods that don't fit anywhere in particular.
 * 
 * A shadow tint can be acquired by calling something like:
 * <code>
 *     getTranslucency(Color.BLACK, 0.5)
 * </code>
 */
public class Utils
{
	/**
	 * Doesn't like all-upper-case acronyms like "XMLDocument".
	 */
	public static String camelCaseToAllCaps(String str)
	{
		ArrayList<String> parts = new ArrayList<String>();
		
		int i0 = 0;
		
		for (int i = 0; i < str.length(); ++i)
		{
			if (Character.isUpperCase(str.charAt(i)))
			{
				parts.add(str.substring(i0, i));
				i0 = i;
			}
		}
		
		if (i0 != str.length() - 1)
		{
			parts.add(str.substring(i0, str.length()));
		}
		
		StringBuilder result = new StringBuilder();
		
		for (int p = 0; p < parts.size(); ++p)
		{
			result.append(parts.get(p).toUpperCase());
			
			if (p < parts.size() - 1 && parts.size() > 1)
				result.append('_');
		}
		
		return result.toString();
	}
	
	public static String allCapsToCamelCase(String str)
	{
		StringBuilder result = new StringBuilder();
		String[] parts = str.split("_");
		
		for (int p = 0; p < parts.length; ++p)
		{
			if (p == 0)
			{
				result.append(parts[p]);
			}
			else
			{
				result.append(Character.toUpperCase(parts[p].charAt(0)));
				
				if (parts[p].length() > 1)
					result.append(parts[p].substring(1));
			}
		}
		
		return result.toString();
	}
	
	/**
	 * Globally accessible random number generator.
	 */
	public static final Random RNG = new Random();
	
	/**
	 * Returns a random int between [low, high]
	 */
	public static int randInt(int low, int high)
	{
		return low + RNG.nextInt(high - low + 1);
	}

	/**
	 * Returns a random double between [low, high]
	 */
	public static double randFloat(double low, double high)
	{
		return low + RNG.nextDouble() * (high - low);
	}
	
	/**
	 * Regular expression specifying zero or more word characters.
	 */
	public static final String REGEX_ANY = "\\w*";
	
	public static int log2(long i)
	{
		for (int b = 0; b < 64; ++b)
			if (((i >> b) & 1) != 0)
				return b;
		
		throw new ArithmeticException();
	}
	
	public static boolean getTimeBasedSwitch(int delay, int prob)
	{
		return System.currentTimeMillis() / delay % prob == 0;
	}
	
	public static int getTimeBasedIndex(int delay, int size)
	{
		return (int) (System.currentTimeMillis() / delay % size);
	}
	
	public static final FileFilter BMP = new FileFilter()
	{
		public boolean accept(File file)
		{
			return file.isFile() && file.getName().endsWith(".bmp");
		}
	};
	
	public static final Comparator<File> FILENAME = new Comparator<File>()
	{
		public int compare(File a, File b)
		{
			return a.getName().compareToIgnoreCase(b.getName());
		}
	};
	
	/**
	 * Color object representing total transparency.
	 */
	public static final Color CLEAR = new Color(0, 0, 0, 0);
	
	/**
	 * Gets which Color is more different than the given Color: Black or White.
	 */
	public static Color getBlackWhiteComplement(Color color)
	{
		int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
		
		return avg < 128 ? Color.WHITE : Color.BLACK;
	}

	/**
	 * Gets a grayscale Color of darkness {@code gray}. {@code gray} must be
	 * between 0 and 1, mutally inclusive.
	 */
	public static Color getGrayscale(double gray)
	{
		if (gray < 0 || gray > 1)
			throw new IllegalArgumentException("Out of range: " + gray);
		
		int grayInt = (int) (gray * 255);
		return new Color(grayInt, grayInt, grayInt);
	}
	
	/**
	 * Gets the gray scale of given Color.
	 */
	public static Color getGrayscale(Color color)
	{
		int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
		
		return new Color(avg, avg, avg);
	}
	
	/**
	 * Gets the complement of the gray scale of given Color.
	 */
	public static Color getGrayscaleComplement(Color color)
	{
		int avg = (color.getRed() + color.getGreen() + color.getBlue()) / 3;
		
		return new Color(255 - avg, 255 - avg, 255 - avg);
	}
	
	/**
	 * Gets a Color with the same RGB components as the given Color,
	 * but with the specified translucentcy value in the range [0,255].
	 */
	public static Color getTranslucency(Color base, int a)
	{		
		return new Color(base.getRed(), base.getGreen(), base.getBlue(), a);
	}
	
	/**
	 * Gets a Color with the same RGB components as the given Color,
	 * but with the specified translucentcy value in the range [0.0,1.0].
	 */
	public static Color getTranslucency(Color base, double a)
	{
		float[] rgb = new float[3];
		base.getColorComponents(rgb);
		
		return new Color(rgb[0], rgb[1], rgb[2], (float)a);
	}
}
