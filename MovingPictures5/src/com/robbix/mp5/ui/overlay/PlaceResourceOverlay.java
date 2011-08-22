package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteLibrary;

public class PlaceResourceOverlay extends InputOverlay
{
	private BufferedImage oreImage;
	private int oreImageX;
	private int oreImageY;
	
	private Position pos = null;
	
	private ResourceDeposit res;
	
	public PlaceResourceOverlay(ResourceDeposit res)
	{
		this.res = res;
	}
	
	public void paintOverUnits(Graphics g)
	{
		final int w = getDisplay().getWidth();
		g.setColor(Color.RED);
		g.setFont(Font.decode("Arial-12"));
		g.drawString("Left Click to Place", w / 2 - 35, 30);
		g.drawString("Right Click to Cancel", w / 2 - 35, 50);
		
		if (pos == null) return;
		
		if (oreImage == null)
		{
			SpriteLibrary sprites = getDisplay().getSpriteLibrary();
			
			Sprite sprite = sprites.getDefaultSprite(res);
			
			BufferedImage image = (BufferedImage) sprite.getImage();
			
			oreImageX = sprite.getXOffset();
			oreImageY = sprite.getYOffset();
			oreImage = Utils.getTranslucency(image, 0.8f);
		}
		
		final int tileSize = getDisplay().getMap().getTileSize();
		
		g.drawImage(
			oreImage,
			pos.x * tileSize + oreImageX,
			pos.y * tileSize + oreImageY,
			null
		);
	}
	
	public void onLeftClick(int x, int y)
	{
		final DisplayPanel d = getDisplay();
		
		if (d.getMap().canPlaceResourceDeposit(pos))
		{
			d.getMap().putResourceDeposit(res, pos);
			res = (ResourceDeposit)res.clone();
			d.refresh();
		}
	}
	
	public void onRightClick(int x, int y)
	{
		getDisplay().completeOverlay(this);
	}
	
	public void mouseMoved(MouseEvent e)
	{
		pos = new Position(
			e.getX() / getDisplay().getMap().getTileSize(),
			e.getY() / getDisplay().getMap().getTileSize()
		);
	}
	
	public void mouseEntered(MouseEvent e)
	{
		pos = new Position(
			e.getX() / getDisplay().getMap().getTileSize(),
			e.getY() / getDisplay().getMap().getTileSize()
		);
	}
	
	public void mouseExited(MouseEvent e)
	{
		pos = null;
	}
}
