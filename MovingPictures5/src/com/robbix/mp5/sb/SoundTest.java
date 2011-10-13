package com.robbix.mp5.sb;

import java.io.File;

import com.robbix.mp5.ui.SoundBank;

public class SoundTest
{
	public static void main(String[] args) throws Exception
	{
		System.out.println("Loading");
		SoundBank sounds = SoundBank.preload(new File("./res/sounds"));
		System.out.println("Starting");
		sounds.start();
		hold();
		System.out.println("Playing 1");
		sounds.playAnyway("acidCloud");
		hold();
		System.out.println("Playing 2");
//		sounds.playAnyway("commandCenter");
		hold();
		System.out.println("Playing 3");
//		sounds.playAnyway("savant_meteorApproaching");
		hold();
		System.out.println("Playing 4");
//		sounds.playAnyway("savant_communicationLinkEstablished");
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
