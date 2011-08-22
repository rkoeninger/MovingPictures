package com.robbix.mp5.basics;

import java.io.File;
import java.io.IOException;

public class FileFormatException extends IOException
{
	private static final long serialVersionUID = 4782374583042675824L;
	
	private File file;
	
	public FileFormatException(File file, String message)
	{
		super(message);
	}
	
	public File getFile()
	{
		return file;
	}
}
