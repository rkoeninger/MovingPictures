package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.ConVecConstructTask;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.basics.Region;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.unit.Footprint;
import com.robbix.mp5.unit.Unit;

public class BuildStructureOverlay extends InputOverlay
{
	private Sprite structSprite;
	
	private Unit conVec;
	private Unit structure;
	
	public BuildStructureOverlay(Unit conVec, Unit structure)
	{
		this.conVec = conVec;
		this.structure = structure;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		drawInstructions(g, rect, "Build", "Cancel");
		
		if (isCursorOnGrid())
		{
			if (structSprite == null)
			{
				int hue = structure.getOwner().getColorHue();
				structSprite = panel.getSpriteLibrary()
								  .getDefaultSprite(structure);
				structSprite = Utils.getTranslucency(structSprite, hue, 0.5f);
			}
			
			panel.draw(g, structSprite, getCursorPosition());
		}
	}
	
	public void paintOverTerrain(Graphics g, Rectangle rect)
	{
		if (isCursorOnGrid())
		{
			Position cursorPos = getCursorPosition();
			Footprint fp = structure.getFootprint();
			Region innerRegion = fp.getInnerRegion().move(cursorPos);
			LayeredMap map = panel.getMap();
			
			if (map.getBounds().contains(innerRegion) && map.canPlaceUnit(cursorPos, fp))
			{
				if (!structure.needsConnection() || map.willConnect(cursorPos, fp))
				{
					panel.draw(g, GREEN, innerRegion);
				}
				else
				{
					panel.draw(g, YELLOW, innerRegion);
				}
			}
			else
			{
				panel.draw(g, RED, innerRegion);
			}
						
			for (Position tubePos : fp.getTubePositions())
				panel.draw(g, WHITE.getFillOnly(), tubePos.shift(innerRegion.x, innerRegion.y));
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		LayeredMap map = panel.getMap();
		Footprint fp = structure.getFootprint();
		Position pos = panel.getPosition(x, y);
		Position conVecPos = pos.shift(fp.getWidth(), fp.getHeight());
		
		if (map.canPlaceUnit(pos, fp) && map.canPlaceUnit(conVecPos))
		{
			conVec.assignNow(new ConVecConstructTask(structure, pos));
			Mediator.doMove(conVec, conVecPos, false);
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
