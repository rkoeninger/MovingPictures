package com.robbix.mp5.ui.overlay;

import java.awt.Color;
import java.awt.Graphics;

import com.robbix.mp5.Utils;
import com.robbix.mp5.basics.ColorScheme;
import com.robbix.mp5.basics.Position;
import com.robbix.mp5.map.LayeredMap;
import com.robbix.mp5.ui.Sprite;
import com.robbix.mp5.ui.SpriteSet;

public class PlaceFixtureOverlay extends InputOverlay
{
	private Sprite fixtureSprite;
	private LayeredMap.Fixture fixture;
	
	public PlaceFixtureOverlay(LayeredMap.Fixture fixture)
	{
		this.fixture = fixture;
	}
	
	public void paintOverUnits(Graphics g)
	{
		if (isCursorOnGrid())
		{
			if (fixtureSprite == null)
			{
				fixtureSprite = panel.getSpriteLibrary().getSprite(fixture);
				fixtureSprite = (fixtureSprite == SpriteSet.BLANK_SPRITE)
					? null
					: Utils.getTranslucency(fixtureSprite, -1, 0.75f);
			}
			
			Position pos = getCursorPosition();
			ColorScheme colors = panel.getMap().canPlaceFixture(fixture, pos) ? GREEN : RED;
			panel.draw(g, colors, pos);
			
			if (fixtureSprite != null)
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
