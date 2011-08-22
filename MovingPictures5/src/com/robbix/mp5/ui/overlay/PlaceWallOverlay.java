package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.DisplayPanel;
import com.robbix.mp5.ui.SpriteLibrary;

public class PlaceWallOverlay extends InputOverlay
{
	private BufferedImage wallImage;
	
	private Position pos = null;
	
	public void paintOverUnits(Graphics g)
	{
		final int w = getDisplay().getWidth();
		g.setColor(Color.RED);
		g.setFont(Font.decode("Arial-12"));
		g.drawString("Left Click to Place", w / 2 - 35, 30);
		g.drawString("Right Click to Cancel", w / 2 - 35, 50);

		if (pos == null) return;
		
		if (wallImage == null)
		{
			SpriteLibrary sprites = getDisplay().getSpriteLibrary();
			
			BufferedImage image = (BufferedImage)
				sprites.getSequence("oTerrainFixture/wall")
					   .get(0)
					   .getImage();
			
			wallImage = Utils.getTranslucency(image, 0.5f);
		}
		
		final int tileSize = getDisplay().getMap().getTileSize();
		
		g.drawImage(
			wallImage,
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
			d.getMap().putWall(pos);
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
