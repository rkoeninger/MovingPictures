package com.robbix.mp5.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.robbix.mp5.utils.Offset;
import com.robbix.mp5.utils.RImage;

public class Sprite
{
	private static final Color SHADOW = new Color(0, 0, 0, 127);
	private Offset offset;
	private int baseHue;
	private RImage baseImage;
	private RImage shadow;
	private Map<Integer, RImage> hueMap;
	
	public Sprite(RImage image, int baseHue, Offset offset)
	{
		this.baseImage = image;
		this.baseHue = baseHue;
		this.offset = offset;
		
		if (baseHue >= 0 && baseHue < 360)
		{
			this.hueMap = new HashMap<Integer, RImage>();
			hueMap.put(baseHue, image);
		}
	}
	
	public Sprite(RImage image)
	{
		this(image, -1, new Offset());
	}
	
	public Sprite(RImage image, int xOff, int yOff)
	{
		this(image, -1, new Offset(xOff, yOff));
	}
	
	public Sprite(RImage image, int baseHue, int xOff, int yOff)
	{
		this(image, baseHue, new Offset(xOff, yOff));
	}
	
	public Sprite(RImage image, Offset offset)
	{
		this(image, -1, offset);
	}
	
	public int getBaseTeamHue()
	{
		return baseHue;
	}
	
	public RImage getImage()
	{
		return baseImage;
	}
	
	public RImage getImage(int hue, boolean cache)
	{
		if (baseHue == -1 || hue == baseHue)
			return baseImage;
		
		RImage img = hueMap.get(hue);
		
		if (img == null)
		{
			img = baseImage.copy();
			img.recolor(baseHue, hue);
			
			if (cache)
				hueMap.put(hue, img);
		}
		
		return img;
	}
	
	public RImage getImage(int hue)
	{
		return getImage(hue, true);
	}
	
	public Offset getOffset()
	{
		return offset;
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
	
	public RImage getShadow()
	{
		if (shadow == null)
		{
			shadow = baseImage.copy();
			shadow.mask(SHADOW);
		}
		
		return shadow;
	}
	
	public Sprite getFadedCopy(double alpha)
	{
		return new Sprite(baseImage.getFadedCopy(alpha), baseHue, offset);
	}
}
