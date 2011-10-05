package com.robbix.mp5;

import java.util.EventObject;

public class ModuleEvent extends EventObject
{
	private static final long serialVersionUID = 1L;
	
	private String moduleName;
	
	public ModuleEvent(Object source, String moduleName)
	{
		super(source);
		this.moduleName = moduleName;
	}
	
	public String getModuleName()
	{
		return moduleName;
	}
}
