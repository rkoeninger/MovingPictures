package com.robbix.utils;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class RImage extends BufferedImage
{
	public static final int ALPHA = BufferedImage.TYPE_INT_ARGB;
	public static final int SOLID = BufferedImage.TYPE_INT_RGB;
	
	public static RImage read(File file) throws IOException
	{
		BufferedImage img = ImageIO.read(file);
		int w = img.getWidth();
		int h = img.getHeight();
		RImage rimg = new RImage(w, h, img.getColorModel().hasAlpha());
		rimg.getGraphics().drawImage(img, 0, 0, null);
		return rimg;
	}
	
	public static RImage readEnsureAlpha(File file) throws IOException
	{
		BufferedImage img = ImageIO.read(file);
		int w = img.getWidth();
		int h = img.getHeight();
		RImage rimg = new RImage(w, h, true);
		rimg.getGraphics().drawImage(img, 0, 0, null);
		return rimg;
	}
	
	public static RImage copyOf(Image img)
	{
		int w = img.getWidth(null);
		int h = img.getHeight(null);
		RImage rimg = new RImage(w, h, true);
		rimg.getGraphics().drawImage(img, 0, 0, null);
		return rimg;
	}
	
	public RImage(int w, int h, boolean hasAlpha)
	{
		super(w, h, hasAlpha ? ALPHA : SOLID);
	}
	
	public RImage(int w, int h, Color color)
	{
		super(w, h, color.getAlpha() != 255 ? ALPHA : SOLID);
		Graphics g = getGraphics();
		g.setColor(color);
		g.drawRect(0, 0, w, h);
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage copy()
	{
		RImage copy = new RImage(getWidth(), getHeight(), hasAlpha());
		copy.getGraphics().drawImage(this, 0, 0, null);
		return copy;
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage copyEnsureAlpha()
	{
		RImage copy = new RImage(getWidth(), getHeight(), true);
		copy.getGraphics().drawImage(this, 0, 0, null);
		return copy;
	}
	
	public boolean hasAlpha()
	{
		return getColorModel().hasAlpha();
	}
	
	/**
	 * Modifies this image.
	 */
	public void fade(double alpha)
	{
		if (alpha < 0 || alpha > 1)
			throw new IllegalArgumentException("Invalid alpha value: " + alpha);
		
		if (alpha == 1)
			return;
		
		WritableRaster raster = getRaster();
		int[] pixel = new int[4];
		
		for (int x = 0; x < getWidth();  ++x)
		for (int y = 0; y < getHeight(); ++y)
		{
			raster.getPixel(x, y, pixel);
			pixel[3] *= alpha;
			raster.setPixel(x, y, pixel);
		}
	}
	
	/**
	 * Modifies this image.
	 */
	public void gray()
	{
		WritableRaster raster = getRaster();
		int[] pixel = new int[4];
		
		for (int x = 0; x < getWidth();  ++x)
		for (int y = 0; y < getHeight(); ++y)
		{
			raster.getPixel(x, y, pixel);
			int sum = pixel[0] + pixel[1] + pixel[2];
			sum /= 3;
			pixel[0] = pixel[1] = pixel[2] = sum;
			raster.setPixel(x, y, pixel);
		}
	}
	
	/**
	 * Modifies this image.
	 */
	public void extract(Color bgColor)
	{
		if (!hasAlpha())
			throw new IllegalStateException("Image must have alpha");
		
		for (int y = 0; y < getHeight(); ++y)
		for (int x = 0; x < getWidth();  ++x)
		{
			if (compare(bgColor, getRGB(x, y)))
				setRGB(x, y, 0);
		}
	}
	
	/**
	 * Modifies this image.
	 */
	public void extract(Color[] bgColors)
	{
		if (!hasAlpha())
			throw new IllegalStateException("Image must have alpha");
		
		for (int y = 0; y < getHeight(); ++y)
		for (int x = 0; x < getWidth();  ++x)
		{
			if (contains(bgColors, getRGB(x, y)))
				setRGB(x, y, 0);
		}
	}
	
	/**
	 * Modifies this image.
	 */
	public void recolor(int startHue, int endHue)
	{
		WritableRaster raster = getRaster();
		int[] pixel = new int[]{0, 0, 0, 255};
		float[] hsb = new float[4];
		
		for (int x = 0; x < getWidth();  ++x)
		for (int y = 0; y < getHeight(); ++y)
		{
			if (getRGB(x, y) >> 24 != 0)
			{
				raster.getPixel(x, y, pixel);
				Color.RGBtoHSB(pixel[0], pixel[1], pixel[2], hsb);
				
				if (Math.abs((int) (hsb[0] * 360) - startHue) <= 5)
				{
					hsb[0] = endHue / 360.0f;
					int rgb = Color.HSBtoRGB(hsb[0], hsb[1], hsb[2]);
					setRGB(x, y, rgb | (pixel[3] << 24));
				}
			}
		}
	}
	
	/**
	 * Modifies this image.
	 */
	public void recolor(Color startColor, Color endColor)
	{
		WritableRaster raster = getRaster();
		int[] pixel = new int[]{0, 0, 0, 255};
		float[] hsb = new float[4];
		float[] startHSB = new RColor(startColor).getHSB();
		float[] endHSB = new RColor(endColor).getHSB();
		
		for (int x = 0; x < getWidth();  ++x)
		for (int y = 0; y < getHeight(); ++y)
		{
			if (getRGB(x, y) >> 24 != 0)
			{
				raster.getPixel(x, y, pixel);
				RColor.RGBtoHSB(pixel, hsb);
				
				if (Math.abs(hsb[0] - startHSB[0]) <= 0.015)
				{
					hsb[0] = endHSB[0] / 360.0f;
					hsb[1] *= endHSB[1];
					hsb[2] *= endHSB[2];
					int rgb = RColor.HSBtoRGBInt(hsb);
					setRGB(x, y, rgb | (pixel[3] << 24));
				}
			}
		}
	}
	
	/**
	 * Modifies this image.
	 */
	public void mask(Color fgColor)
	{
		if (!hasAlpha() && fgColor.getAlpha() != 255)
			throw new IllegalStateException("Image must have alpha");
		
		int fgColorRGB = fgColor.getRGB();
		
		for (int x = 0; x < getWidth();  ++x)
		for (int y = 0; y < getHeight(); ++y)
		{
			int rgb = getRGB(x, y);
			setRGB(x, y, (rgb >> 24 == 0) ? 0 : fgColorRGB);
		}
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage getFadedCopy(double alpha)
	{
		RImage copy = copyEnsureAlpha();
		copy.fade(alpha);
		return copy;
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage getGrayedCopy()
	{
		RImage copy = copy();
		copy.gray();
		return copy;
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage getExtractedCopy(Color bgColor)
	{
		RImage copy = copyEnsureAlpha();
		copy.extract(bgColor);
		return copy;
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage getExtractedCopy(Color[] bgColors)
	{
		RImage copy = copyEnsureAlpha();
		copy.extract(bgColors);
		return copy;
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage getRecoloredCopy(int startHue, int endHue)
	{
		RImage copy = copy();
		copy.recolor(startHue, endHue);
		return copy;
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage getRecoloredCopy(Color startColor, Color endColor)
	{
		RImage copy = copy();
		copy.recolor(startColor, endColor);
		return copy;
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage getMaskedCopy(Color fgColor)
	{
		RImage copy = copy();
		copy.mask(fgColor);
		return copy;
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage getResizedCopy(double factor)
	{
		if (factor <= 0)
			throw new IllegalArgumentException("Scale factor must be > 0");
		
		int w = (int) (getWidth()  * factor);
		int h = (int) (getHeight() * factor);
		return getResizedCopy(new Dimension(w, h));
	}
	
	/**
	 * Creates new image, does not modify this one.
	 */
	public RImage getResizedCopy(Dimension dim)
	{
		RImage resized = new RImage(dim.width, dim.height, hasAlpha());
		resized.getGraphics().drawImage(
			this,
			0, 0, dim.width, dim.height,
			0, 0, getWidth(), getHeight(),
			null
		);
		return resized;
	}
	
	private static boolean compare(Color color, int pixel)
	{
		return pixel == color.getRGB();
	}
	
	private static boolean contains(Color[] colors, int pixel)
	{
		for (Color color : colors)
			if (pixel == color.getRGB())
				return true;
		
		return false;
	}
}
