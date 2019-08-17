package com.robbix.utils;

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
	private static Set<FileInfo> infos = new HashSet<FileInfo>();
	
	public static void main(String[] args) throws IOException
	{
		getLineCount(new File(args.length > 0 ? args[0] : "./src"));
		System.out.println();
		System.out.println("Top 10 line counts: ");
		
		FileInfo[] infoArray = infos.toArray(new FileInfo[infos.size()]);
		
		Arrays.sort(infoArray);
		
		for (int i = 0; i < Math.min(infoArray.length, 10); ++i)
			println(infoArray[i].file, infoArray[i].lineCount);
	}
	
	private static int getLineCount(File file) throws IOException
	{
		if (file.getName().contains("LOCCounter"))
			return 0;
		
		if (file.isDirectory())
		{
			int lines = 0;
			
			File[] files = file.listFiles();
			Arrays.sort(files, Utils.FILENAME);
			
			for (File child : files)
				lines += getLineCount(child);
			
			println(file, lines);
			
			return lines;
		}
		else if (file.getName().endsWith(".java"))
		{
			BufferedReader reader = new BufferedReader(
				new InputStreamReader(new FileInputStream(file)));
			
			int lines = 0;
			
			while (reader.readLine() != null)
				lines++;
			
			println(file, lines);
			
			infos.add(new FileInfo(file, lines));
			
			reader.close();
			return lines;
		}
		else
		{
			return 0;
		}
	}
	
	private static class FileInfo implements Comparable<FileInfo>
	{
		public final File file;
		public final int lineCount;
		
		public FileInfo(File file, int lineCount)
		{
			this.file = file;
			this.lineCount = lineCount;
		}
		
		public int compareTo(FileInfo info)
		{
			return info.lineCount - this.lineCount;
		}
	}
	
	private static void println(File f, int i)
	{
		String path = f.getPath();
		System.out.println(path + nSpaces(63 - path.length()) + i);
	}
	
	private static String nSpaces(int n)
	{
		StringBuilder b = new StringBuilder(n);
		
		for (int x = 0; x < n; ++x)
			b.append(' ');
		
		return b.toString();
	}
}
