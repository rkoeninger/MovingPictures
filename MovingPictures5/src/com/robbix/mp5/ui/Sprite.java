package com.robbix.mp5.ui;

import java.awt.Color;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;

public class Sprite
{
	private int xOff;
	private int yOff;
	private int baseHue;
	private Map<Integer, Image> hueMap;
	
	public Sprite(Image image, int xOff, int yOff)
	{
		this(image, -1, xOff, yOff);
	}
	
	public Sprite(Image image, int baseHue, int xOff, int yOff)
	{
		this.baseHue = baseHue;
		this.xOff = xOff;
		this.yOff = yOff;
		this.hueMap = new HashMap<Integer, Image>();
		
		hueMap.put(baseHue, image);
	}
	
	public Image getImage()
	{
		return hueMap.get(baseHue);
	}
	
	public Image getImage(int hue)
	{
		if (baseHue == -1)
			return getImage();
		
		BufferedImage image = (BufferedImage) hueMap.get(hue);
		
		synchronized(this)
		{
			if (image == null)
			{
				image = recolor((BufferedImage) hueMap.get(baseHue), baseHue, hue);
				hueMap.put(hue, image);
			}
		}
		
		return image;
	}
	
	public int getXOffset()
	{
		return xOff;
	}
	
	public int getYOffset()
	{
		return yOff;
	}
	
	private static BufferedImage recolor(
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
}
