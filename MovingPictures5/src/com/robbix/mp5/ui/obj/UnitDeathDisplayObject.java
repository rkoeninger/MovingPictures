package com.robbix.mp5.ui.obj;

import static com.robbix.mp5.unit.Activity.COLLAPSE;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import com.robbix.mp5.Game;
import com.robbix.mp5.player.Player;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.SpriteGroup;
import com.robbix.mp5.ui.SpriteLibrary;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.mp5.unit.Unit;
import com.robbix.mp5.unit.UnitType;

public class UnitDeathDisplayObject extends DisplayObject
{
	private UnitType type;
	private Player owner;
	private Point2D point;
	private Rectangle2D bounds;
	private int timeOfDeath;
	private boolean selfDestructed;
	
	private SpriteGroup group;
	private SpriteGroup collapseGroup;
	
	public UnitDeathDisplayObject(Unit unit, int timeOfDeath, boolean selfDestructed)
	{
		this.type = unit.getType();
		this.owner = unit.getOwner();
		this.point = unit.getAbsPoint();
		this.timeOfDeath = timeOfDeath;
		this.selfDestructed = selfDestructed;
		Point2D absPoint = unit.getAbsPoint();
		this.bounds = new Rectangle2D.Double(
			absPoint.getX() - 0.5,
			absPoint.getY() - 0.5,
			unit.getWidth() + 1,
			unit.getHeight() + 1
		);
	}
	
	public UnitDeathDisplayObject(Unit unit, int timeOfDeath)
	{
		this(unit, timeOfDeath, false);
	}
	
	public DisplayLayer getDisplayLayer()
	{
		return DisplayLayer.OVER_UNIT;
	}
	
	public boolean isAlive()
	{
		int frame = Game.game.getFrame() - timeOfDeath;
		int frameCount = group != null ? group.getFrameCount() : 32;
		return frame < frameCount;
	}
	
	public void paint(DisplayGraphics g)
	{
		if (group == null || group == SpriteSet.BLANK_GROUP)
			group = getGroup();
		
		int frame = Game.game.getFrame() - timeOfDeath;
		
		if (type.isStructureType())
		{
			if (collapseGroup == null || collapseGroup == SpriteSet.BLANK_GROUP)
				collapseGroup = getCollapseGroup();
			
			if (frame < collapseGroup.getFrameCount())
				g.draw(collapseGroup.getFrame(frame), point, owner.getColor());
		}
		
		if (frame < group.getFrameCount())
			g.draw(group.getFrame(frame), point, owner.getColor());
	}
	
	public Rectangle2D getBounds()
	{
		return bounds;
	}
	
	private SpriteGroup getGroup()
	{
		SpriteLibrary lib = panel.getSpriteLibrary();
		
		if (type.isGuardPostType())
		{
			return lib.getAmbientSpriteGroup("aDeath", "guardPostKilled");
		}
		else if (type.isStructureType())
		{
			String eventName = null;
			int fpWidth = type.getFootprint().getWidth();
			int fpHeight = type.getFootprint().getHeight();
			
			if      (fpWidth == 4 && fpHeight == 3) eventName = "collapseSmoke3";
			else if (fpWidth == 3 && fpHeight == 3) eventName = "collapseSmoke2";
			else if (fpWidth == 5 && fpHeight == 4) eventName = "collapseSmoke4";
			else if (fpWidth == 3 && fpHeight == 2) eventName = "collapseSmoke1";
			else if (fpWidth == 2 && fpHeight == 2) eventName = "collapseSmoke6";
			else if (fpWidth == 2 && fpHeight == 1) eventName = "collapseSmoke5";
			else if (fpWidth == 1 && fpHeight == 2) eventName = "collapseSmoke5";
			else if (fpWidth == 1 && fpHeight == 1) eventName = "collapseSmoke5";
			
			return lib.getAmbientSpriteGroup("aDeath", eventName);
		}
		else if (selfDestructed)
		{
			if (type.is("Starflare"))
			{
				return lib.getAmbientSpriteGroup("aStarflareExplosion", "explosion");
			}
			else if (type.is("Supernova"))
			{
				return lib.getAmbientSpriteGroup("aSupernovaExplosion", "explosion");
			}
			else
			{
				return lib.getAmbientSpriteGroup("aDeath", "vehicleSelfDestruct");
			}
		}
		else if (type.is("Scorpion") || type.is("Spider"))
		{
			return lib.getAmbientSpriteGroup("aDeath", "arachnidKilled");
		}
		else
		{
			return lib.getAmbientSpriteGroup("aDeath", "vehicleKilled");
		}
	}
	
	private SpriteGroup getCollapseGroup()
	{
		return panel.getSpriteLibrary().getUnitSpriteSet(type).get(COLLAPSE);
	}
}
