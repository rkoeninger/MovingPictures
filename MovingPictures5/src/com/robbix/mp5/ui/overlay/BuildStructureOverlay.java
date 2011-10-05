package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.BuildTask;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.ResourceDeposit;
import com.robbix.mp5.map.ResourceType;
import com.robbix.mp5.ui.Sprite;
import static com.robbix.mp5.unit.Activity.*;

import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitFactory;

public class BuildStructureOverlay extends InputOverlay
{
	private Sprite unitSprite;
	
	private Unit unit;
	
	private UnitFactory factory;
	private String type;
	
	public BuildStructureOverlay(Unit unit)
	{
		this.unit = unit;
	}
	
	public BuildStructureOverlay(UnitFactory factory, String type)
	{
		this.factory = factory;
		this.type = type;
		this.unit = factory.newUnit(type);
		this.unit.setActivity(BUILD);
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Place", rect.width / 2 - 35, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
		if (isCursorOnGrid())
		{
			if (unitSprite == null)
			{
				int hue = unit.getOwner().getColorHue();
				unitSprite = panel.getSpriteLibrary()
								  .getDefaultSprite(unit);
				unitSprite = Utils.getTranslucency(unitSprite, hue, 0.5f);
			}
			
			panel.draw(g, unitSprite, getCursorPosition());
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		Position pos = panel.getPosition(x, y);
		
		if (panel.getMap().canPlaceUnit(pos, unit.getFootprint()))
		{
			if (unit.isMine())
			{
				ResourceDeposit res =
					panel.getMap().getResourceDeposit(pos.shift(1, 0));
				
				if (res == null || res.getType() != ResourceType.COMMON_ORE)
					return;
			}
			
			int buildTime = getDisplay().getSpriteLibrary().getUnitSpriteSet(unit.getType()).get(BUILD).getFrameCount();
			panel.getMap().putUnit(unit, pos);
			unit.assignNow(new BuildTask(buildTime, 200));
			unit.setActivity(BUILD);
			Mediator.sounds.play("structureBuild");
			panel.refresh();
			
			if (factory != null)
			{
				unit = factory.newUnit(type);
			}
			else
			{
				complete();
			}
		}
	}
	
	public void onRightClick(int x, int y)
	{
		complete();
	}
}
