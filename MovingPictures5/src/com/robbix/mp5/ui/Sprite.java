package com.robbix.mp5.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Tuple;

public class Sprite
{
	private int xOff;
	private int yOff;
	private int baseHue;
	private Map<HSTuple, Image> hueScaleMap;
	private HSTuple baseTuple;
	
	public Sprite(Image image, int xOff, int yOff)
	{
		this(image, -1, xOff, yOff);
	}
	
	public Sprite(Image image, int baseHue, int xOff, int yOff)
	{
		this.baseHue = baseHue;
		this.xOff = xOff;
		this.yOff = yOff;
		this.hueScaleMap = new HashMap<HSTuple, Image>();
		this.baseTuple = new HSTuple(baseHue, 0);
		hueScaleMap.put(baseTuple, image);
	}
	
	public Image getImage()
	{
		return hueScaleMap.get(new HSTuple(baseHue, 0));
	}
	
	public Image getImage(int hue)
	{
		HSTuple tuple = new HSTuple(baseHue == -1 ? -1 : hue, 0);
		BufferedImage image = (BufferedImage) hueScaleMap.get(tuple);
		
		if (image == null)
		{
			image = (BufferedImage) hueScaleMap.get(new HSTuple(baseHue, 0));
			image = Utils.recolorUnit(image, baseHue, hue);
			hueScaleMap.put(tuple, image);
		}
		
		return image;
	}
	
	public Image getImage(int hue, int scale)
	{
		HSTuple tuple = new HSTuple(baseHue == -1 ? -1 : hue, scale);
		BufferedImage image = (BufferedImage) hueScaleMap.get(tuple);
		
		if (image == null)
		{
			image = (BufferedImage) hueScaleMap.get(baseTuple);
			image = Utils.recolorUnit(image, baseHue, hue);
			
			if (scale < 0)
			{
				for (int s = -1; s >= scale; s--)
				{
					image = Utils.shrink(image);
					hueScaleMap.put(new HSTuple(hue, s), image);
				}
			}
			else
			{
				for (int s = 1; s <= scale; s++)
				{
					image = Utils.stretch(image);
					hueScaleMap.put(new HSTuple(hue, s), image);
				}
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
	
	public int getXOffset(int scale)
	{
		return scale < 0 ? xOff >> -scale : xOff << scale;
	}
	
	public int getYOffset(int scale)
	{
		return scale < 0 ? yOff >> -scale : yOff << scale;
	}
	
	private static class HSTuple extends Tuple<Integer, Integer>
	{
		public HSTuple(Integer hue, Integer scale)
		{
			super(hue, scale);
		}
	}
}
