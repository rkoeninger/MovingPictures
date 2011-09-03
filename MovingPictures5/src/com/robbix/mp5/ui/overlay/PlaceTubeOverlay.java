package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.SpriteLibrary;

public class PlaceTubeOverlay extends InputOverlay
{
	private BufferedImage tubeImage;
	
	private Position pos = null;
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		final int w = rect.width;
		g.setColor(Color.RED);
		g.setFont(Font.decode("Arial-12"));
		g.drawString("Left Click to Place", w / 2 - 35, 30);
		g.drawString("Right Click to Cancel", w / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
		if (pos == null) return;
		
		if (tubeImage == null)
		{
			SpriteLibrary sprites = getDisplay().getSpriteLibrary();
			
			BufferedImage image = (BufferedImage)
				sprites.getSequence("oTerrainFixture/tube")
					   .get(0)
					   .getImage();
			
			tubeImage = Utils.getTranslucency(image, 0.8f);
		}
		
		final int tileSize = getDisplay().getMap().getTileSize();
		
		g.drawImage(
			tubeImage,
			pos.x * tileSize,
			pos.y * tileSize,
			null
		);
	}
	
	public void onLeftClick(int x, int y)
	{
		final DisplayPanel d = getDisplay();
		
		if (d.getMap().canPlaceFixture(pos))
		{
			d.getMap().putTube(pos);
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
