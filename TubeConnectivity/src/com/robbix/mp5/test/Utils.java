package com.robbix.mp5.test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.Comparator;
import java.util.Set;

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
	 * Compares two Positions for z-order.
	 * 
	 * Positions are first sorted by y-coordinate, second by x-coordinate,
	 * top to bottom, left to right.
	 */
	public static final Comparator<Position> Z_ORDER_POS =
	new Comparator<Position>()
	{
		public int compare(Position a, Position b)
		{
			if      (a.y > b.y) return  1;
			else if (a.y < b.y) return -1;
			
			if      (a.x > b.x) return  1;
			else if (a.x < b.x) return -1;
			
			return 0;
		}
	};

	/**
	 * Constructs a description path from the given elements.
	 * 
	 * Description paths are typically used in hash tables to
	 * identify sprites, sounds or events based on a set of
	 * criteria.
	 * 
	 * For instance, a sprite description path might look like:
	 * 
	 * <pre>
	 *     scout/north/3
	 * </pre>
	 * 
	 * Which represents the sprite for a {@code scout} unit,
	 * facing {@code north} and on the 4th (index = {@code 3})
	 * frame of its movement animation.
	 */
	public static String getPath(Object... parts)
	{
		if (parts == null || parts.length == 0)
			return "";
		
		StringBuilder builder = new StringBuilder();
		
		for (int i = 0; i < parts.length - 1; ++i)
		{
			builder.append(parts[i]);
			builder.append('/');
		}
		
		builder.append(parts[parts.length - 1]);
		
		return builder.toString();
	}

	/**
	 * Color object representing total transparency.
	 */
	public static final Color CLEAR = new Color(0, 0, 0, 0);
	
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
	public static Color getTranslucency(Color base, float a)
	{
		float[] rgb = new float[3];
		base.getColorComponents(rgb);
		
		return new Color(rgb[0], rgb[1], rgb[2], a);
	}
	
	/**
	 * Replaces all pixels with Colors in the the Set backgroundColors
	 * with replacementColor.
	 * 
	 * This method is most useful when replacing the background Color
	 * of a no-alpha sprite with zero-alpha pixels.
	 * 
	 * @see Utils.CLEAR
	 */
	public static BufferedImage replaceColors(
		BufferedImage img,
		Set<Color> backgroundColors,
		Color replacementColor)
	{
		int replacementCode = replacementColor.getRGB();
		
		for (int y = 0; y < img.getHeight(); ++y)
		for (int x = 0; x < img.getWidth();  ++x)
		{
			Color color = new Color(img.getRGB(x, y), true);
			
			if (backgroundColors.contains(color))
			{
				img.setRGB(x, y, replacementCode);
			}
		}
				
		return img;
	}
	
	/**
	 * Gets a new BufferedImage with an alpha channel that has the pixel
	 * data from the given image copied into it.
	 * 
	 * If the given BufferedImage already has an alpha channel, it is
	 * simply returned.
	 */
	public static BufferedImage getAlphaImage(BufferedImage img)
	{
		if (img.getColorModel().hasAlpha())
			return img;
		
		BufferedImage copy = new BufferedImage(
			img.getWidth(),
			img.getHeight(),
			BufferedImage.TYPE_INT_ARGB
		);
		
		copy.setData(img.getRaster());
		
		return copy;
	}
	
	// TODO: Friend methods?
}
