package com.robbix.mp5.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Offset;

public class Sprite
{
	private Offset offset;
	private int baseHue;
	private Image baseImage;
	private Map<Integer, Image> hueMap;
	
	public Sprite(Image image, int xOff, int yOff)
	{
		this(image, -1, new Offset(xOff, yOff));
	}
	
	public Sprite(Image image, int baseHue, int xOff, int yOff)
	{
		this(image, baseHue, new Offset(xOff, yOff));
	}
	
	public Sprite(Image image, Offset offset)
	{
		this(image, -1, offset);
	}
	
	public Sprite(Image image, int baseHue, Offset offset)
	{
		this.baseImage = image;
		this.baseHue = baseHue;
		this.offset = offset;
		
		if (baseHue >= 0 && baseHue < 360)
		{
			this.hueMap = new HashMap<Integer, Image>();
			hueMap.put(baseHue, image);
		}
	}
	
	public Image getImage()
	{
		return baseImage;
	}
	
	public Image getImage(int hue)
	{
		if (baseHue == -1 || hue == baseHue)
			return baseImage;
		
		BufferedImage image = (BufferedImage) hueMap.get(hue);
		
		if (image == null)
		{
			image = (BufferedImage) baseImage;
			image = Utils.recolorUnit(image, baseHue, hue);
			hueMap.put(hue, image);
		}
		
		return image;
	}
	
	public int getXOffset()
	{
		return offset.dx;
	}
	
	public int getYOffset()
	{
		return offset.dy;
	}
	
	public int getXOffset(int scale)
	{
		return offset.getDX(scale);
	}
	
	public int getYOffset(int scale)
	{
		return offset.getDY(scale);
	}
}
