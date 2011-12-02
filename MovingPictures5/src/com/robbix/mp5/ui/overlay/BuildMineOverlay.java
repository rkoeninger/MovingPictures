package com.robbix.mp5.ui.overlay;

import java.awt.Color;

import com.robbix.mp5.Game;
import com.robbix.mp5.ai.task.BuildMineTask;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.map.ResourceType;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Footprint;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitType;
import com.robbix.utils.Position;
import com.robbix.utils.RColor;
import com.robbix.utils.Region;

public class BuildMineOverlay extends InputOverlay
{
	private Sprite mineSprite;
	private ResourceType resType;
	private RColor color;
	
	private Unit miner;
	
	private Footprint fp = Footprint.STRUCT_2_BY_1_NO_W_SIDE;
	
	public BuildMineOverlay(Unit miner)
	{
		this.miner = miner;
		this.color = miner.getOwner().getColor();
	}
	
	public void paintImpl(DisplayGraphics g)
	{
		if (isCursorOnGrid())
		{
			Position center = fp.getCenter();
			Position pos = getCursorPosition().subtract(center);
			String toolTip = drawMineFootprint(g, pos);
			ResourceDeposit res = panel.getMap().getResourceDeposit(pos.shift(1, 0));
			
			if (res != null)
			{
				if (resType != res.getType())
					mineSprite = null;
				
				resType = res.getType();
				UnitType type = getMineType(res);
				
				if (mineSprite == null || mineSprite == SpriteSet.BLANK_SPRITE)
					mineSprite = panel.getSpriteLibrary().getTranslucentDefault(type, 0.5f);
				
				g.draw(mineSprite, pos, color);
			}
			
			if (toolTip != null)
			{
				g.setColor(Color.WHITE);
				Region inner = fp.getInnerRegion();
				
				if (inner.w == 1 && inner.h == 1) g.drawString(toolTip, pos);
				                             else g.drawString(toolTip, inner.move(pos));
			}
		}
	}
	
	public void onLeftClick()
	{
		Position center = fp.getCenter();
		Position pos = getCursorPosition().subtract(center);
		Position minerPos = pos.shift(1, 0);
		ResourceDeposit res = panel.getMap().getResourceDeposit(pos.shift(1, 0));
		
		if (panel.getMap().canPlaceUnit(pos) && res != null)
		{
			miner.assignNow(new BuildMineTask(getMine(res)));
			Game.game.doMove(miner, minerPos, false);
			complete();
		}
		else
		{
			Game.game.playSound("structureError");
		}
	}
	
	private Unit getMine(ResourceDeposit res)
	{
		return Game.game.getUnitFactory().newUnit(getMineType(res), miner.getOwner());
	}
	
	private UnitType getMineType(ResourceDeposit res)
	{
		String typeName = miner.getType().getName().substring(0, 1);
		typeName += res.isRare() ? "Rare" : "Common";
		typeName += "Mine";
		return Game.game.getUnitFactory().getType(typeName);
	}
}
