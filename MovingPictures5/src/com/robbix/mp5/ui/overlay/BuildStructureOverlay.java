package com.robbix.mp5.ui.overlay;

import java.awt.Color;

import com.robbix.mp5.Game;
import com.robbix.mp5.ai.task.ConVecConstructTask;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.unit.Footprint;
import com.robbix.mp5.unit.Unit;
import com.robbix.utils.Position;
import com.robbix.utils.RGraphics;
import com.robbix.utils.Region;

public class BuildStructureOverlay extends InputOverlay
{
	private Sprite structSprite;
	private int hue;
	
	private Unit conVec;
	private Unit structure;
	
	public BuildStructureOverlay(Unit conVec, Unit structure)
	{
		this.conVec = conVec;
		this.structure = structure;
		this.showTubeConnectivity = true;
		this.hue = structure.getOwner().getColorHue();
	}
	
	public void paintImpl(RGraphics g)
	{
		if (isCursorOnGrid())
		{
			if (structSprite == null)
				structSprite = panel.getSpriteLibrary().getTranslucentDefault(structure, 0.5f);
			
			Position center = structure.getFootprint().getCenter();
			Position pos = getCursorPosition().subtract(center);
			String toolTip = drawUnitFootprint(g, structure.getType(), pos);
			structSprite.paint(g, pos, hue);
			
			if (toolTip != null)
			{
				g.setColor(Color.WHITE);
				Region inner = structure.getFootprint().getInnerRegion();
				
				if (inner.w == 1 && inner.h == 1) g.drawString(toolTip, pos);
				                             else g.drawString(toolTip, inner.move(pos));
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
			Game.game.doMove(conVec, conVecPos, false);
			complete();
		}
		else
		{
			Game.game.playSound("structureError");
		}
	}
}
