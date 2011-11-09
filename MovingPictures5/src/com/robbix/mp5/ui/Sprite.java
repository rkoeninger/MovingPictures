package com.robbix.mp5.ui;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.HashMap;
import java.util.Map;

import com.robbix.mp5.utils.Offset;
import com.robbix.mp5.utils.Utils;

public class Sprite
{
	private static final int[] SHADOW_COLOR = {0, 0, 0, 191};
	private static final int[] CLEAR = {0, 0, 0, 0};
	private Offset offset;
	private int baseHue;
	private Image baseImage;
	private BufferedImage shadow;
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
	
	public int getBaseTeamHue()
	{
		return baseHue;
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
	
	public Image getShadow()
	{
		if (shadow == null)
		{
			shadow = new BufferedImage(
				baseImage.getWidth(null),
				baseImage.getHeight(null),
				BufferedImage.TYPE_INT_ARGB
			);
			
			int[] pixel = new int[4];
			Raster raster = ((BufferedImage) baseImage).getRaster();
			WritableRaster out = shadow.getRaster();
			
			for (int x = 0; x < shadow.getWidth();  ++x)
			for (int y = 0; y < shadow.getHeight(); ++y)
			{
				raster.getPixel(x, y, pixel);
				out.setPixel(x, y, (pixel[3] == 0) ? CLEAR : SHADOW_COLOR);
			}
		}
		
		return shadow;
	}
}
