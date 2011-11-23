package com.robbix.mp5.ui.overlay;

import java.awt.Color;

import com.robbix.mp5.Game;
import com.robbix.mp5.ai.task.BuildMineTask;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Position;
import com.robbix.utils.RColor;
import com.robbix.utils.Region;

public class BuildMineOverlay extends InputOverlay
{
	private Sprite mineSprite;
	private RColor color;
	
	private Unit miner;
	private Unit mine;
	
	public BuildMineOverlay(Unit miner, Unit mine)
	{
		this.miner = miner;
		this.mine = mine;
		this.color = mine.getOwner().getColor();
	}
	
	public void paintImpl(DisplayGraphics g)
	{
		if (isCursorOnGrid())
		{
			if (mineSprite == null || mineSprite == SpriteSet.BLANK_SPRITE)
				mineSprite = panel.getSpriteLibrary().getTranslucentDefault(mine, 0.5f);
			
			Position center = mine.getFootprint().getCenter();
			Position pos = getCursorPosition().subtract(center);
			String toolTip = drawUnitFootprint(g, mine.getType(), pos);
			g.draw(mineSprite, pos, color);
			
			if (toolTip != null)
			{
				g.setColor(Color.WHITE);
				Region inner = mine.getFootprint().getInnerRegion();
				
				if (inner.w == 1 && inner.h == 1) g.drawString(toolTip, pos);
				                             else g.drawString(toolTip, inner.move(pos));
			}
		}
	}
	
	public void onLeftClick()
	{
		Position center = mine.getFootprint().getCenter();
		Position pos = getCursorPosition().subtract(center);
		Position minerPos = pos.shift(1, 0);
		
		if (panel.getMap().canPlaceUnit(pos) && panel.getMap().canPlaceMine(pos))
		{
			miner.assignNow(new BuildMineTask(mine));
			Game.game.doMove(miner, minerPos, false);
			complete();
		}
		else
		{
			Game.game.playSound("structureError");
		}
	}
}
