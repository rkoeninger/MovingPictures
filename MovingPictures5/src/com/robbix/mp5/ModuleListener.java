package com.robbix.mp5;

import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

public interface ModuleListener extends EventListener
{
	public void moduleLoaded(ModuleEvent e);
	public void moduleUnloaded(ModuleEvent e);
	
	public static class Helper
	{
		private Set<ModuleListener> listeners;
		
		public Helper()
		{
			listeners = new HashSet<ModuleListener>();
		}
		
		public boolean add(ModuleListener listener)
		{
			return listeners.add(listener);
		}
		
		public boolean remove(ModuleListener listener)
		{
			return listeners.remove(listener);
		}
		
		public Set<ModuleListener> getAll()
		{
			return new HashSet<ModuleListener>(listeners);
		}
		
		public int size()
		{
			return listeners.size();
		}
		
		public void fireModuleLoaded(ModuleEvent e)
		{
			for (ModuleListener listener : listeners)
				listener.moduleLoaded(e);
		}
		
		public void fireModuleUnloaded(ModuleEvent e)
		{
			for (ModuleListener listener : listeners)
				listener.moduleUnloaded(e);
		}
	}
}
