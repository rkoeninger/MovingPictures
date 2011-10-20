package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.BuildMineTask;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.unit.Footprint;
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
		drawInstructions(g, rect, "Build", "Cancel");
		
		if (isCursorOnGrid())
		{
			if (mineSprite == null)
			{
				int hue = mine.getOwner().getColorHue();
				mineSprite = panel.getSpriteLibrary()
								  .getDefaultSprite(mine);
				mineSprite = Utils.getTranslucency(mineSprite, hue, 0.5f);
			}
			
			panel.draw(g, mineSprite, getCursorPosition());
		}
	}
	
	public void paintOverTerrain(Graphics g, Rectangle rect)
	{
		if (isCursorOnGrid())
		{
			Position cursorPos = getCursorPosition();
			Footprint fp = mine.getFootprint();
			Region innerRegion = fp.getInnerRegion().move(cursorPos);
			Region outerRegion = innerRegion.stretch(1);
			LayeredMap map = panel.getMap();
			
			if (map.getBounds().contains(innerRegion)
			 && map.canPlaceUnit(cursorPos, fp)
			 && canPlaceMine(cursorPos))
			{
				g.setColor(TRANS_GREEN);
				panel.fill(g, innerRegion);
				g.setColor(Color.GREEN);
				panel.draw(g, innerRegion);
			}
			else
			{
				g.setColor(TRANS_RED);
				panel.fill(g, innerRegion);
				g.setColor(Color.RED);
				panel.draw(g, innerRegion);
			}
			
			g.setColor(Color.WHITE);
			panel.draw(g, outerRegion);
		}
	}
	
	private boolean canPlaceMine(Position pos)
	{
		pos = pos.shift(1, 0);
		ResourceDeposit deposit = panel.getMap().getResourceDeposit(pos);
		return deposit != null && deposit.isCommon();
	}
	
	public void onLeftClick(int x, int y)
	{
		Position pos = panel.getPosition(x, y);
		Position minerPos = pos.shift(1, 0);
		
		if (canPlaceMine(pos))
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
