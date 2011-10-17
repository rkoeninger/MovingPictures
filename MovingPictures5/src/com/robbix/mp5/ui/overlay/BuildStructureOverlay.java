package com.robbix.mp5.ui.overlay;

import java.awt.Color;
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
	private Sprite unitSprite;
	
	private Unit conVec;
	private Unit structure;
	
	public BuildStructureOverlay(Unit conVec, Unit structure)
	{
		this.conVec = conVec;
		this.structure = structure;
	}
	
	public void paintOverUnits(Graphics g, Rectangle rect)
	{
		g.translate(rect.x, rect.y);
		g.setColor(Color.RED);
		g.setFont(OVERLAY_FONT);
		g.drawString("Left Click to Build", rect.width / 2 - 35, 30);
		g.drawString("Right Click to Cancel", rect.width / 2 - 35, 50);
		g.translate(-rect.x, -rect.y);
		
		if (isCursorOnGrid())
		{
			if (unitSprite == null)
			{
				int hue = structure.getOwner().getColorHue();
				unitSprite = panel.getSpriteLibrary()
								  .getDefaultSprite(structure);
				unitSprite = Utils.getTranslucency(unitSprite, hue, 0.5f);
			}
			
			panel.draw(g, unitSprite, getCursorPosition());
		}
	}
	
	public void paintOverTerrain(Graphics g, Rectangle rect)
	{
		if (isCursorOnGrid())
		{
			Position cursorPos = getCursorPosition();
			Footprint fp = structure.getFootprint();
			Region innerRegion = fp.getInnerRegion().move(cursorPos);
			Region outerRegion = innerRegion.stretch(1);
			g.setColor(TRANS_RED);
			panel.fill(g, innerRegion);
			g.setColor(Color.RED);
			panel.draw(g, innerRegion);
			g.setColor(Color.WHITE);
			panel.draw(g, outerRegion);
			g.setColor(TRANS_WHITE);
			
			for (Position tubePos : fp.getTubePositions())
				panel.fill(g, tubePos.shift(innerRegion.x, innerRegion.y));
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
