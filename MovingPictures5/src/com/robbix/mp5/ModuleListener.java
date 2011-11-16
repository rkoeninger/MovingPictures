package com.robbix.mp5;

import java.util.EventListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

public interface ModuleListener extends EventListener
{
	/**
	 * Called when a module has been loaded and is ready to use.
	 */
	public void moduleLoaded(ModuleEvent e);
	
	/**
	 * Called when a moudle has been unloaded and will no longer be available.
	 */
	public void moduleUnloaded(ModuleEvent e);
	
	public static class Helper
	{
		protected Set<ModuleListener> listeners;
		
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
		
		public void fireModuleLoaded(final ModuleEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					for (ModuleListener listener : listeners)
						listener.moduleLoaded(e);
				}
			});
		}
		
		public void fireModuleUnloaded(final ModuleEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					for (ModuleListener listener : listeners)
						listener.moduleUnloaded(e);
				}
			});
		}
	}
}
