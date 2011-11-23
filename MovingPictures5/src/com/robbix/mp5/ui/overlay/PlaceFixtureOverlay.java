package com.robbix.mp5.ui.overlay;

import java.awt.Color;

import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.DisplayGraphics;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteSet;
import com.robbix.utils.Position;

public class PlaceFixtureOverlay extends InputOverlay
{
	private Sprite fixtureSprite;
	private LayeredMap.Fixture fixture;
	
	public PlaceFixtureOverlay(LayeredMap.Fixture fixture)
	{
		this.fixture = fixture;
		this.showTubeConnectivity = (fixture == LayeredMap.Fixture.TUBE);
			
	}
	
	public void paintImpl(DisplayGraphics g)
	{
		if (isCursorOnGrid())
		{
			if (fixtureSprite == null || fixtureSprite == SpriteSet.BLANK_SPRITE)
				fixtureSprite = panel.getSpriteLibrary().getTranslucentSprite(fixture, 0.75f);
			
			Position pos = getCursorPosition();
			g.setColor(panel.getMap().canPlaceFixture(fixture, pos) ? GREEN.getFill() : RED.getFill());
			g.fill(pos);
			g.draw(fixtureSprite, pos);
			
			if (!panel.getMap().canPlaceFixture(fixture, pos))
			{
				g.setColor(Color.WHITE);
				g.drawString("Obstructed", pos);
			}
		}
	}
	
	public void onLeftClick()
	{
		Position pos = getCursorPosition();
		
		if (panel.getMap().canPlaceFixture(fixture, pos))
		{
			panel.getMap().putFixture(fixture, pos);
			panel.refresh();
		}
	}
}
