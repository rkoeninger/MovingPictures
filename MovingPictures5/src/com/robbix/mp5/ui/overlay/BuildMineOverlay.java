package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.BuildMineTask;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.unit.Unit;

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
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		if (isCursorOnGrid())
		{
			if (mineSprite == null)
			{
				int hue = mine.getOwner().getColorHue();
				mineSprite = panel.getSpriteLibrary()
								  .getDefaultSprite(mine);
				mineSprite = Utils.getTranslucency(mineSprite, hue, 0.5f);
			}
			
			Position center = mine.getFootprint().getCenter();
			panel.draw(g, mineSprite, getCursorPosition().subtract(center));
		}
	}
	
	public void paintOverTerrain(Graphics g, Rectangle rect)
	{
		if (isCursorOnGrid())
		{
			Position center = mine.getFootprint().getCenter();
			drawStructureFootprint(g, mine.getType(), getCursorPosition().subtract(center));
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		if (!isCursorOnGrid())
			return;

		Position center = mine.getFootprint().getCenter();
		Position pos = getCursorPosition().subtract(center);
		Position minerPos = pos.shift(1, 0);
		
		if (panel.getMap().canPlaceUnit(pos) && panel.getMap().canPlaceMine(pos))
		{
			miner.assignNow(new BuildMineTask(mine));
			Mediator.doMove(miner, minerPos, false);
			complete();
		}
		else
		{
			Mediator.playSound("structureError");
		}
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
}
