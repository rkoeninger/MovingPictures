package com.robbix.mp5;

import java.util.HashSet;
import java.util.Set;

import com.robbix.mp5.player.Player;

/**
 * Listener for changes to Game environment.
 */
public interface GameListener
{
	public void playerAdded(Player player);
	
	public static class Helper
	{
		private Set<GameListener> listeners;
		
		public Helper()
		{
			listeners = new HashSet<GameListener>();
		}
		
		public boolean add(GameListener listener)
		{
			return listeners.add(listener);
		}
		
		public boolean remove(GameListener listener)
		{
			return listeners.remove(listener);
		}
		
		public Set<GameListener> getAll()
		{
			return new HashSet<GameListener>(listeners);
		}
		
		public int size()
		{
			return listeners.size();
		}
		
		public void firePlayerAdded(Player player)
		{
			for (GameListener listener : listeners)
				listener.playerAdded(player);
		}
	}
}
