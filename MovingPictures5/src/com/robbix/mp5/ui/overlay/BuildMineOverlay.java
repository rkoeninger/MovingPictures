package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;

import com.robbix.mp5.Game;
import com.robbix.mp5.ai.task.BuildMineTask;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Position;
import com.robbix.mp5.utils.Region;
import com.robbix.mp5.utils.Utils;

public class BuildMineOverlay extends InputOverlay
{
	private Sprite mineSprite;
	
	private Unit miner;
	private Unit mine;
	
	public BuildMineOverlay(Unit miner, Unit mine)
	{
		this.miner = miner;
		this.mine = mine;
	}
	
	public void paintImpl(Graphics g)
	{
		if (isCursorOnGrid())
		{
			if (mineSprite == null)
			{
				int hue = mine.getOwner().getColorHue();
				mineSprite = panel.getSpriteLibrary()
								  .getDefaultSprite(mine);
				mineSprite = (mineSprite == SpriteSet.BLANK_SPRITE)
					? null
					: Utils.getTranslucency(mineSprite, hue, 0.5f);
			}
			
			Position center = mine.getFootprint().getCenter();
			Position pos = getCursorPosition().subtract(center);
			String toolTip = drawUnitFootprint(g, mine.getType(), pos);
			
			if (mineSprite != null)
				panel.draw(g, mineSprite, pos);
			
			if (toolTip != null)
			{
				g.setColor(Color.WHITE);
				Region inner = mine.getFootprint().getInnerRegion();
				
				if (inner.w == 1 && inner.h == 1) panel.draw(g, toolTip, pos);
				                             else panel.draw(g, toolTip, inner.move(pos));
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
