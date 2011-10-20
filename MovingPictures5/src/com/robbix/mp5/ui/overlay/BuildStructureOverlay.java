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
			Region outerRegion = innerRegion.stretch(1);
			LayeredMap map = panel.getMap();
			
			if (map.getBounds().contains(innerRegion) && map.canPlaceUnit(cursorPos, fp))
			{
				if (!structure.needsConnection() || map.willConnect(cursorPos, fp))
				{
					g.setColor(TRANS_GREEN);
					panel.fill(g, innerRegion);
					g.setColor(Color.GREEN);
					panel.draw(g, innerRegion);
				}
				else
				{
					g.setColor(TRANS_YELLOW);
					panel.fill(g, innerRegion);
					g.setColor(Color.YELLOW);
					panel.draw(g, innerRegion);
				}
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
