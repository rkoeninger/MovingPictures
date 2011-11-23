package com.robbix.utils;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Random;

/**
 * Yes, it's a Utils class. I know.
 * 
 * It has commonly-used methods that don't fit anywhere in particular.
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
	private static final Random RNG = new Random();
	
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
}
