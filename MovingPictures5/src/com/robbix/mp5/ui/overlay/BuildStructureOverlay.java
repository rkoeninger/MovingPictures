package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;

import com.robbix.mp5.Mediator;
import com.robbix.mp5.ai.task.ConVecConstructTask;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Footprint;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.utils.Position;
import com.robbix.mp5.utils.Region;
import com.robbix.mp5.utils.Utils;

public class BuildStructureOverlay extends InputOverlay
{
	private Sprite structSprite;
	
	private Unit conVec;
	private Unit structure;
	
	public BuildStructureOverlay(Unit conVec, Unit structure)
	{
		this.conVec = conVec;
		this.structure = structure;
		this.showTubeConnectivity = true;
	}
	
	public void paintOverUnits(Graphics g)
	{
		if (isCursorOnGrid())
		{
			if (structSprite == null)
			{
				int hue = structure.getOwner().getColorHue();
				structSprite = panel.getSpriteLibrary()
								  .getDefaultSprite(structure);
				structSprite = (structSprite == SpriteSet.BLANK_SPRITE)
					? null
					: Utils.getTranslucency(structSprite, hue, 0.5f);
			}
			
			Position center = structure.getFootprint().getCenter();
			Position pos = getCursorPosition().subtract(center);
			String toolTip = drawUnitFootprint(g, structure.getType(), pos);
			
			if (structSprite != null)
				panel.draw(g, structSprite, pos);
			
			if (toolTip != null)
			{
				g.setColor(Color.WHITE);
				Region inner = structure.getFootprint().getInnerRegion();
				
				if (inner.w == 1 && inner.h == 1) panel.draw(g, toolTip, pos);
				                             else panel.draw(g, toolTip, inner.move(pos));
			}
		}
	}
	
	public void onLeftClick()
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
}
