package com.robbix.mp5.map;

import java.awt.Image;
import java.util.HashMap;
import java.util.Map;

import com.robbix.mp5.Utils;

public class Tile
{
	private Map<Integer, Image> imgs;
	
	public Tile(Image img)
	{
		this.imgs = new HashMap<Integer, Image>();
		imgs.put(0, img);
	}
	
	public Image getImage()
	{
		return getImage(0);
	}
	
	public Image getImage(int scale)
	{
		Image img = imgs.get(scale);
		
		if (img == null)
		{
			img = (scale < 0)
				? Utils.shrink (getImage(scale + 1))
				: Utils.stretch(getImage(scale - 1));
		}
		
		imgs.put(scale, img);
		return img;
	}
}
