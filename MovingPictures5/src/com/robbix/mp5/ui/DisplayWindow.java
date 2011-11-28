package com.robbix.mp5.ui;

import com.robbix.mp5.player.Player;
import com.robbix.mp5.unit.Unit;

public interface DisplayWindow
{
	public void showStatus(Unit unit);
	public void showStatus(Player player);
	public void showFrameNumber(int frame, double rate);
}
