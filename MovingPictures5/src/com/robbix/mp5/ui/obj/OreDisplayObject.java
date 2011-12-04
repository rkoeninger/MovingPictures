package com.robbix.mp5.ui.obj;

import java.awt.Color;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.map.Ore;
import com.robbix.mp5.map.ResourceType;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.utils.Position;

public class OreDisplayObject extends DisplayObject
{
	private static final Color COMMON_ORE_COLOR = new Color(255, 92, 0);
	private static final Color RARE_ORE_COLOR = new Color(255, 255, 106);
	
	private Ore res;
	
	public OreDisplayObject(Ore res)
	{
		this.res = res;
	}
	
	public boolean isAlive()
	{
		return res.getPosition() != null;
	}
	
	public Rectangle2D getBounds()
	{
		Position pos = res.getPosition();
		
		if (pos == null)
			return new Rectangle2D.Double();
		
		return new Rectangle2D.Double(pos.x, pos.y, 1, 1);
	}
	
	public void paint(DisplayGraphics g)
	{
		if (panel.getScale() < -1)
		{
			Color color = res.getType() == ResourceType.COMMON_ORE
				? COMMON_ORE_COLOR
				: RARE_ORE_COLOR;
			g.setColor(color);
			g.fill(res.getPosition());
		}
		else
		{
			Sprite sprite = res.isSurveyedBy(panel.getCurrentPlayer())
				? panel.getSpriteLibrary().getSprite(res)
				: panel.getSpriteLibrary().getUnknownDepositSprite();
			g.draw(sprite, res.getPosition());
		}
	}
}
