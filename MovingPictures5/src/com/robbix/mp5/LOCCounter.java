package com.robbix.mp5;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LOCCounter
{
	private static Set<File> top10 = new HashSet<File>();
	
	public static void main(String[] args) throws IOException
	{
		getLineCount(new File("./src"), true, true);
		System.out.println();
		System.out.println("Top 10 lines: ");
		
		for (File file : top10)
			getLineCount(file, true, false);
	}
	
	private static int getLineCount(File file, boolean output, boolean tt) throws IOException
	{
		if (file.getName().contains("LOCCounter"))
			return 0;
		
		if (file.isDirectory())
		{
			int lines = 0;
			
			File[] files = file.listFiles();
			Arrays.sort(files, Utils.FILENAME);
			
			for (File child : files)
				lines += getLineCount(child, output, tt);
			
			if (output)
				System.out.println(file.getPath() + " " + lines);
			
			return lines;
		}
		else
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file)));
			
			int lines = 0;
			
			while (reader.readLine() != null)
				lines++;
			
			if (tt)
				tryTop10(file);
			
			if (output)
				System.out.println(file.getPath() + " " + lines);
			
			return lines;
		}
	}
	
	private static boolean tryTop10(File file) throws IOException
	{
		if (top10.size() < 10)
			return top10.add(file);
		
		for (File ttFile : top10)
			if (getLineCount(file, false, false) > getLineCount(ttFile, false, false))
			{
				top10.remove(ttFile);
				return top10.add(file);
			}
		
		return false;
	}
}
