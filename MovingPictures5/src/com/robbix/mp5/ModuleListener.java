package com.robbix.mp5;

import java.util.EventListener;

public interface ModuleListener extends EventListener
{
	public void moduleLoaded(ModuleEvent e);
	public void moduleUnloaded(ModuleEvent e);
}
