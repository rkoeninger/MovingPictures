package com.robbix.mp5.ui;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import com.robbix.utils.Offset;
import com.robbix.utils.RColor;
import com.robbix.utils.RImage;

public class Sprite
{
	private static final Color SHADOW = new Color(0, 0, 0, 127);
	private Offset offset;
	private RColor baseColor;
	private RImage baseImage;
	private RImage shadow;
	private Map<RColor, RImage> colorMap;
	
	public Sprite(RImage image, Color baseColor, Offset offset)
	{
		this.baseImage = image;
		this.baseColor = RColor.fromColor(baseColor);
		this.offset = offset;
		
		if (this.baseColor != null)
		{
			this.colorMap = new HashMap<RColor, RImage>();
			colorMap.put(this.baseColor, image);
		}
	}
	
	public Sprite(RImage image)
	{
		this(image, null, new Offset());
	}
	
	public Sprite(RImage image, int xOff, int yOff)
	{
		this(image, null, new Offset(xOff, yOff));
	}
	
	public Sprite(RImage image, Color baseColor, int xOff, int yOff)
	{
		this(image, baseColor, new Offset(xOff, yOff));
	}
	
	public Sprite(RImage image, Offset offset)
	{
		this(image, null, offset);
	}
	
	public RColor getBaseColor()
	{
		return baseColor;
	}
	
	public RImage getImage()
	{
		return baseImage;
	}
	
	public RImage getImage(Color color, boolean cache)
	{
		if (baseColor == null || color == null || baseColor.equals(color))
			return baseImage;
		
		RColor rColor = RColor.fromColor(color);
		RImage img = colorMap.get(rColor);
		
		if (img == null)
		{
			img = baseImage.getRecoloredCopy(baseColor, color);
			
			if (cache)
				colorMap.put(rColor, img);
		}
		
		return img;
	}
	
	public RImage getImage(RColor color)
	{
		return getImage(color, true);
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
		return new Sprite(baseImage.getFadedCopy(alpha), baseColor, offset);
	}
}
