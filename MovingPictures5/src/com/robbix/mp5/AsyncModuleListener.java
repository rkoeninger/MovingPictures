package com.robbix.mp5;

import javax.swing.SwingUtilities;

public interface AsyncModuleListener extends ModuleListener
{
	/**
	 * Called when the loading of a module has started and later calls to load
	 * it will be ignored. The method {@code moduleLoaded()} will be called at
	 * some point after a call to this method to indicate the loading is
	 * complete.
	 */
	public void moduleLoadStarted(ModuleEvent e);
	
	public static class Helper extends ModuleListener.Helper
	{
		public void fireModuleLoadStarted(final ModuleEvent e)
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					for (ModuleListener listener : listeners)
						if (listener instanceof AsyncModuleListener)
							((AsyncModuleListener) listener).moduleLoadStarted(e);
				}
			});
		}
	}
}
