package com.robbix.mp5;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileFilter;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.Icon;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.unit.Unit;

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
	
	public static Rectangle getWindowBounds()
	{
		return GraphicsEnvironment.getLocalGraphicsEnvironment()
								  .getMaximumWindowBounds();
	}
	
	/**
	 * Returns the provided list of elements as a Set. Duplicate entries in
	 * the list will be dropped.
	 */
	public static <T> Set<T> asSet(T... elements)
	{
		Set<T> set = new HashSet<T>(elements.length);
		
		for (T element : elements)
			set.add(element);
		
		return set;
	}
	
	public static final Comparator<File> FILENAME = 
	new Comparator<File>()
	{
		public int compare(File a, File b)
		{
			return a.getName().compareToIgnoreCase(b.getName());
		}
	};
	
	/**
	 * Compares two Positions for z-order.
	 * 
	 * Positions are first sorted by y-coordinate, second by x-coordinate,
	 * top to bottom, left to right.
	 */
	public static final Comparator<Position> Z_ORDER_POS =
	new Comparator<Position>()
	{
		private final int A =  1;
		private final int B = -1;
		
		public int compare(Position a, Position b)
		{
			if      (a.y > b.y) return A;
			else if (a.y < b.y) return B;
			
			if      (a.x > b.x) return A;
			else if (a.x < b.x) return B;
			
			return 0;
		}
	};

	/**
	 * Compares two Units for z-order.
	 * 
	 * Sorted just like Z_ORDER_POS but all the structures on a row
	 * come after all the vehicles on the same row.
	 */
	public static final Comparator<Unit> Z_ORDER_UNIT =
	new Comparator<Unit>()
	{
		private final int A =  1;
		private final int B = -1;
		
		public int compare(Unit a, Unit b)
		{
			final Position posA = a.getPosition();
			final Position posB = b.getPosition();
			
			if (posA.y > posB.y) return A;
			if (posA.y < posB.y) return B;
			
			final boolean structA = a.isStructure() && !a.isMine();
			final boolean structB = b.isStructure() && !b.isMine();
			
			if ( structA && !structB) return A;
			if (!structA &&  structB) return B;
			
			if (posA.x > posB.x) return A;
			if (posA.x < posB.x) return B;
			
			if (b.isMine()) return A;
			if (a.isMine()) return B;
			
			return 0; // Should this be possible?
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
	 *     getPath(unitType, unitDir, animationFrame) -> "scout/north/3"
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
			if (parts[i] != null)
			{
				String partString = parts[i].toString();
				
				if (partString.matches("\\s"))
					throw new IllegalArgumentException(
						"Path can't have whitespace"
					);
				
				builder.append(partString);
				
				if (parts[i + 1] != null)
					builder.append('/');
			}
		
		Object lastPart = parts[parts.length - 1];
		
		if (lastPart != null)
		{
			if (builder.charAt(builder.length() - 1) != '/')
				builder.append('/');
			
			builder.append(lastPart);
		}
		
		return builder.toString();
	}

	/**
	 * Color object representing total transparency.
	 */
	public static final Color CLEAR = new Color(0, 0, 0, 0);

	/**
	 * Color object representing dark orange - #7f3f00.
	 */
	public static final Color DARK_ORANGE = new Color(127, 63, 0);

	/**
	 * Color object representing dark orange - #ff7f00.
	 */
	public static final Color ORANGE = new Color(255, 127, 0);
	
	/**
	 * A set containing ORANGE and DARK_ORANGE.
	 */
	public static final Set<Color> ORANGE_AND_DARK_ORANGE =
		Collections.unmodifiableSet(asSet(ORANGE, DARK_ORANGE));
	
	/**
	 * Returns the HTML color code (#000000) for the given Color.
	 */
	public static String getColorCode(Color color)
	{
		return "#" + Integer.toHexString(color.getRGB()).substring(2);
	}
	
	/**
	 * Gets a random color - a color whose red, green and blue components
	 * are random values 0-255.
	 */
	public static Color getRandomColor()
	{
		return new Color(RNG.nextInt(256), RNG.nextInt(256), RNG.nextInt(256));
	}
	
	/**
	 * Gets a random primary color - a color whose red, green and blue
	 * components are each either 0 or 255.
	 * 
	 * Could be one of: BLACK, RED, BLUE, GREEN, CYAN, MAGENTA, YELLOW, WHITE.
	 */
	public static Color getRandomPrimaryColor()
	{
		return new Color(RNG.nextBoolean() ? 255 : 0,
						 RNG.nextBoolean() ? 255 : 0,
						 RNG.nextBoolean() ? 255 : 0); 
	}
	
	/**
	 * Gets the RGB-component wise inversion of given Color.
	 */
	public static Color invert(Color color)
	{
		return new Color(255 - color.getRed(),
						 255 - color.getGreen(),
						 255 - color.getBlue(),
						 color.getAlpha());
	}

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
	 * Gets the gray scale of given Image.
	 */
	public static BufferedImage getGrayscale(Icon icon)
	{
		BufferedImage copy = new BufferedImage(
			icon.getIconWidth(),
			icon.getIconHeight(),
			BufferedImage.TYPE_INT_RGB
		);
		WritableRaster out = copy.getRaster();
		
		icon.paintIcon(null, copy.getGraphics(), 0, 0);
		
		int[] pixel = new int[4];
		
		for (int x = 0; x < out.getWidth();  ++x)
		for (int y = 0; y < out.getHeight(); ++y)
		{
			out.getPixel(x, y, pixel);
			pixel[0] = pixel[1] = pixel[2] = (pixel[0] + pixel[1] + pixel[2]) / 3;
			out.setPixel(x, y, pixel);
		}
		
		return copy;
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
	public static Color getTranslucency(Color base, float a)
	{
		float[] rgb = new float[3];
		base.getColorComponents(rgb);
		
		return new Color(rgb[0], rgb[1], rgb[2], a);
	}
	
	/**
	 * Returns a BufferedImage with the alpha value of all pixels scaled
	 * by the specified factor.
	 * 
	 * Unlike the other getTranslucency methods, the alpha values are not
	 * <b>set</b> to the given factor, they are <b>multiplied</b> by it.
	 * 
	 * The returned image is a new image only if the given image does
	 * not have an alpha channel.
	 */
	public static BufferedImage getTranslucency(
		BufferedImage img,
		float aFactor)
	{
		if (aFactor == 1)
			return img;
		
		if (aFactor < 0 || aFactor > 1)
			throw new IllegalArgumentException();
		
		BufferedImage copy = new BufferedImage(
			img.getWidth(),
			img.getHeight(),
			BufferedImage.TYPE_INT_ARGB
		);
		
		if (aFactor == 0)
			return copy;
		
		WritableRaster srcRaster = img.getRaster();
		WritableRaster outRaster = copy.getRaster();
		
		float[] pixel = new float[4];
		
		boolean alpha = img.getColorModel().hasAlpha();
		
		for (int y = 0; y < copy.getHeight(); ++y)
		for (int x = 0; x < copy.getWidth();  ++x)
		{
			srcRaster.getPixel(x, y, pixel);
			
			if (alpha)
			{
				pixel[3] *= aFactor;
			}
			else
			{
				pixel[3] = aFactor * 255;
			}
			
			outRaster.setPixel(x, y, pixel);
		}
		
		return copy;
	}
	
	public static Sprite getTranslucency(Sprite sprite, int hue, float aFactor)
	{
		BufferedImage img = (BufferedImage)
			(hue >= 0 ? sprite.getImage(hue) : sprite.getImage());
		
		img = getTranslucency(img, aFactor);
		
		return new Sprite(img, hue, sprite.getXOffset(), sprite.getYOffset());
	}
	
	/**
	 * Gets a new BufferedImage of the specified size with all pixels having
	 * the given color.
	 */
	public static BufferedImage getBlankImage(Color color, int w, int h)
	{
		BufferedImage img = new BufferedImage(
			w,
			h,
			BufferedImage.TYPE_INT_ARGB
		);
		
		WritableRaster raster = img.getRaster();
		
		int[] pixel = new int[]{
			color.getRed(),
			color.getGreen(),
			color.getBlue(),
			color.getAlpha()
		};
		
		for (int x = 0; x < w; ++x)
		for (int y = 0; y < h; ++y)
			raster.setPixel(x, y, pixel);
		
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
		
		int w = img.getWidth();
		int h = img.getHeight();
		
		BufferedImage copy = new BufferedImage(
			w,
			h,
			BufferedImage.TYPE_INT_ARGB
		);
		
		for (int y = 0; y < h; ++y)
		for (int x = 0; x < w; ++x)
			copy.setRGB(x, y, img.getRGB(x, y));
		
		return copy;
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
		boolean hasAlpha = img.getColorModel().hasAlpha();
		int replacementCode = replacementColor.getRGB();
		
		for (int y = 0; y < img.getHeight(); ++y)
		for (int x = 0; x < img.getWidth();  ++x)
		{
			Color color = new Color(img.getRGB(x, y), hasAlpha);
			
			if (backgroundColors.contains(color))
			{
				img.setRGB(x, y, replacementCode);
			}
		}
				
		return img;
	}
	
	public static BufferedImage shrink(Image img)
	{
		return shrink(img, true);
	}
	
	public static BufferedImage shrink(Image img, boolean alpha)
	{
		int sWidth = img.getWidth(null);
		int sHeight = img.getHeight(null);
		int dWidth = sWidth / 2;
		int dHeight = sHeight / 2;
		BufferedImage newImg = new BufferedImage(dWidth, dHeight, alpha
			? BufferedImage.TYPE_INT_ARGB
			: BufferedImage.TYPE_INT_RGB
		);
		Graphics g = newImg.getGraphics();
		g.drawImage(
			img,
			0, 0, dWidth, dHeight,
			0, 0, sWidth, sHeight,
			null
		);
		g.dispose();
		return newImg;
	}
	
	public static BufferedImage stretch(Image img)
	{
		return stretch(img, true);
	}
	
	public static BufferedImage stretch(Image img, boolean alpha)
	{
		int sWidth = img.getWidth(null);
		int sHeight = img.getHeight(null);
		int dWidth = sWidth * 2;
		int dHeight = sHeight * 2;
		BufferedImage newImg = new BufferedImage(dWidth, dHeight, alpha
			? BufferedImage.TYPE_INT_ARGB
			: BufferedImage.TYPE_INT_RGB
		);
		Graphics g = newImg.getGraphics();
		g.drawImage(
			img,
			0, 0, dWidth, dHeight,
			0, 0, sWidth, sHeight,
			null
		);
		g.dispose();
		return newImg;
	}
	
	public static BufferedImage recolorUnit(
		BufferedImage baseImage,
		int baseHue,
		int hue)
	{
		WritableRaster baseRaster = baseImage.getRaster();
		
		int w = baseRaster.getWidth();
		int h = baseRaster.getHeight();
		
		BufferedImage newImage = new BufferedImage(w, h, baseImage.getType());
		WritableRaster newRaster = newImage.getRaster();
		
		int[] rgb = new int[]{0, 0, 0, 255};
		float[] hsb = new float[4];
		
		for (int x = 0; x < w; ++x)
		for (int y = 0; y < h; ++y)
		{
			baseRaster.getPixel(x, y, rgb);
			
			if (rgb[3] > 0)
			{
				Color.RGBtoHSB(rgb[0], rgb[1], rgb[2], hsb);
				
				if (Math.abs((int) (hsb[0] * 360) - baseHue) <= 5)
				{
					hsb[0] = hue / 360.0f;
					int rgbInt = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
					
					rgb[0] = (rgbInt >> 0x10) & 0xff;
					rgb[1] = (rgbInt >> 0x08) & 0xff;
					rgb[2] = (rgbInt >> 0x00) & 0xff;
				}
			}
			
			newRaster.setPixel(x, y, rgb);
		}
		
		return newImage;
	}
	
	public static int getHueInt(Color color)
	{
		float[] hsb = new float[4];
		Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), hsb);
		return (int) (hsb[0] * 360);
	}
}
