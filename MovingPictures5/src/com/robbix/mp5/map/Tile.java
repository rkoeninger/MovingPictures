package com.robbix.mp5.map;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class Tile
{
	private BufferedImage img;
	private Color averageColor;
	private Color familyColor;
	
	public Tile(BufferedImage img)
	{
		this(img, null);
	}
	
	public Tile(BufferedImage img, Color familyColor)
	{
		this.img = img;
		
		Raster raster = img.getRaster();
		int area = raster.getWidth() * raster.getHeight();
		float[] avg = new float[4];
		float[] pixel = new float[4];
		
		for (int x = 0; x < raster.getWidth();  ++x)
		for (int y = 0; y < raster.getHeight(); ++y)
		{
			raster.getPixel(x, y, pixel);
			
			for (int i = 0; i < pixel.length; ++i)
				avg[i] += pixel[i];
		}
		
		for (int i = 0; i < pixel.length; ++i)
			avg[i] /= area;
		
		this.averageColor = img.getColorModel().hasAlpha()
			? new Color((int) avg[0], (int) avg[1], (int) avg[2], (int) avg[3])
			: new Color((int) avg[0], (int) avg[1], (int) avg[2]);
		
		this.familyColor = (familyColor != null) ? familyColor : averageColor;
	}
	
	public BufferedImage getImage()
	{
		return img;
	}
	
	public Color getAverageColor()
	{
		return averageColor;
	}
	
	public Color getFamilyColor()
	{
		return familyColor;
	}
}
