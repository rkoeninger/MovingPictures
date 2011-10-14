package com.robbix.mp5.sb;

import java.io.File;
import java.util.Random;
import java.util.Set;

import com.robbix.mp5.ui.SoundBank;

public class SoundTest
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("Loading");
		SoundBank sounds = SoundBank.preload(new File("./res/sounds"));
		System.out.println("Starting");
		sounds.start();
		
		Set<String> nameSet = sounds.getLoadedModules();
		String[] names = nameSet.toArray(new String[0]);
		Random rand = new Random();
		
		for (int t = 0; t < 100; t++)
		{
			String toPlay = names[rand.nextInt(names.length)];
			System.out.println("Playing \"" + toPlay + "\"");
			sounds.playAnyway(toPlay);
			hold();
		}
		
		System.out.println("Waiting to finish");
		while (sounds.isActive()) hold();
		System.out.println("Stoping");
		sounds.stop();
		sounds.flush();
		System.out.println("Done");
	}
	
	private static void hold()
	{
		try
		{
			Thread.sleep(500);
		}
		catch (InterruptedException ie)
		{
			
		}
	}
}
