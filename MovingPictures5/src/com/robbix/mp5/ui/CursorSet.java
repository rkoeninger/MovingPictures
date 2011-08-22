package com.robbix.mp5.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class CursorSet
{
	public static CursorSet load(File rootDir) throws IOException
	{
		CursorSet cursorSet = new CursorSet();
		
		for (File dir : rootDir.listFiles())
		{
			if (! dir.isDirectory())
				continue;
			
			cursorSet.cursors.put(dir.getName(), AnimatedCursor.load(dir));
		}
		
		return cursorSet;
	}
	
	private HashMap<String, AnimatedCursor> cursors;
	
	private CursorSet()
	{
		cursors = new HashMap<String, AnimatedCursor>();
	}
	
	public AnimatedCursor getCursor(String name)
	{
		return cursors.get(name);
	}
}
