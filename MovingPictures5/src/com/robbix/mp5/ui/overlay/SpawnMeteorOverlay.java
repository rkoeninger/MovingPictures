package com.robbix.mp5.ui.overlay;

import com.robbix.mp5.Game;

public class SpawnMeteorOverlay extends InputOverlay
{
	public SpawnMeteorOverlay()
	{
		super("meteor");
	}
	
	public void onLeftClick()
	{
		Game.game.doSpawnMeteor(getCursorPosition());
	}
}
