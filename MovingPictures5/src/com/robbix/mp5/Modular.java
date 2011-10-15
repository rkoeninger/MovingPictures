package com.robbix.mp5;

import java.io.File;
import java.io.IOException;

public interface Modular
{
	public void loadModule(String name) throws IOException;
	public void loadModule(File file) throws IOException;
	public boolean isLoaded(String name);
	public boolean unloadModule(String name);
	public void addModuleListener(ModuleListener listener);
	public void removeModuleListener(ModuleListener listener);
}
