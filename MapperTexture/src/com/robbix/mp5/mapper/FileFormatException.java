package com.robbix.mp5.mapper;

import java.io.IOException;

public class FileFormatException extends IOException
{
	private static final long serialVersionUID = -3250234777544131336L;

	public FileFormatException()
	{
	}
	
	public FileFormatException(String message)
	{
		super(message);
	}
}
