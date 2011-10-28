package com.robbix.mp5.ui.overlay;

import java.awt.Graphics;
import java.awt.Rectangle;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.Utils;
import com.robbix.mp5.ai.task.ConVecConstructTask;
import com.robbix.mp5.basics.Position;
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
		if (isCursorOnGrid())
		{
			if (structSprite == null)
			{
				int hue = structure.getOwner().getColorHue();
				structSprite = panel.getSpriteLibrary()
								  .getDefaultSprite(structure);
				structSprite = Utils.getTranslucency(structSprite, hue, 0.5f);
			}
			
			Position center = structure.getFootprint().getCenter();
			panel.draw(g, structSprite, getCursorPosition().subtract(center));
		}
	}
	
	public void paintOverTerrain(Graphics g, Rectangle rect)
	{
		if (isCursorOnGrid())
		{
			Position center = structure.getFootprint().getCenter();
			drawStructureFootprint(g, structure.getType(), getCursorPosition().subtract(center));
		}
	}
	
	public void onLeftClick(int x, int y)
	{
		LayeredMap map = panel.getMap();
		Footprint fp = structure.getFootprint();
		Position center = structure.getFootprint().getCenter();
		Position pos = getCursorPosition().subtract(center);
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
