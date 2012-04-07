package com.robbix.mp5.mapper;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.KeyStroke;

public class HotKeys extends KeyAdapter
{
	private List<HotKey> hotkeys;
	
	public HotKeys()
	{
		hotkeys = new ArrayList<HotKey>();
	}
	
	public void add(HotKey hk)
	{
		hotkeys.add(hk);
	}
	
	public void keyPressed(KeyEvent e)
	{
		KeyStroke ks = KeyStroke.getKeyStrokeForEvent(e);
		
		for (HotKey hk : hotkeys)
			if (hk.respondsTo(ks))
			{
				hk.hotkeyTyped();
				e.consume();
				return;
			}
	}
}
