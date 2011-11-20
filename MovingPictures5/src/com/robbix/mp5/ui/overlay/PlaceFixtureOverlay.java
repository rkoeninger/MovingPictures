package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;

import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.utils.ColorScheme;
import com.robbix.mp5.utils.Position;

public class PlaceFixtureOverlay extends InputOverlay
{
	private Sprite fixtureSprite;
	private LayeredMap.Fixture fixture;
	
	public PlaceFixtureOverlay(LayeredMap.Fixture fixture)
	{
		this.fixture = fixture;
		this.showTubeConnectivity = (fixture == LayeredMap.Fixture.TUBE);
			
	}
	
	public void paintImpl(Graphics g)
	{
		if (isCursorOnGrid())
		{
			if (fixtureSprite == null)
				fixtureSprite = panel.getSpriteLibrary().getTranslucentSprite(fixture, 0.75f);
			
			Position pos = getCursorPosition();
			ColorScheme colors = panel.getMap().canPlaceFixture(fixture, pos) ? GREEN : RED;
			panel.draw(g, colors, pos);
			panel.draw(g, fixtureSprite, pos);
			
			if (!panel.getMap().canPlaceFixture(fixture, pos))
			{
				g.setColor(Color.WHITE);
				panel.draw(g, "Obstructed", pos);
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
