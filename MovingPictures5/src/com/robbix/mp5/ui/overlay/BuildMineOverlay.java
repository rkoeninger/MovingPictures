package com.robbix.mp5.ui.overlay;

import java.awt.Color;

import com.robbix.mp5.Game;
import com.robbix.mp5.ai.task.BuildMineTask;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Position;
import com.robbix.mp5.utils.RGraphics;
import com.robbix.mp5.utils.Region;

public class BuildMineOverlay extends InputOverlay
{
	private Sprite mineSprite;
	private int hue;
	
	private Unit miner;
	private Unit mine;
	
	public BuildMineOverlay(Unit miner, Unit mine)
	{
		this.miner = miner;
		this.mine = mine;
		this.hue = mine.getOwner().getColorHue();
	}
	
	public void paintImpl(RGraphics g)
	{
		if (isCursorOnGrid())
		{
			if (mineSprite == null)
				mineSprite = panel.getSpriteLibrary().getTranslucentDefault(mine, 0.5f);
			
			Position center = mine.getFootprint().getCenter();
			Position pos = getCursorPosition().subtract(center);
			String toolTip = drawUnitFootprint(g, mine.getType(), pos);
			panel.draw(g, mineSprite, hue, pos);
			
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
