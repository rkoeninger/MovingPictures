package com.robbix.mp5.ui;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

public class SoundBank
{
	public static SoundBank preload(File rootDir) throws IOException
	{
		SoundBank sounds = new SoundBank();
		sounds.rootDir = rootDir;
		
		try
		{
			for (File file : rootDir.listFiles())
			{
				if (! file.getName().endsWith(".wav"))
					continue;
				
				Clip clip = AudioSystem.getClip();
				clip.open(AudioSystem.getAudioInputStream(file));
				String rawName = file.getName().toLowerCase();
				int i = rawName.lastIndexOf(".wav");
				String name = rawName.substring(0, i);
				sounds.clips.put(name, clip);
			}
			
			for (File file : new File(rootDir, "music").listFiles())
			{
				if (! file.getName().endsWith(".wav"))
					continue;
				
				Clip clip = AudioSystem.getClip();
				clip.open(AudioSystem.getAudioInputStream(file));
				String rawName = file.getName().toLowerCase();
				int i = rawName.lastIndexOf(".wav");
				String name = rawName.substring(0, i);
				sounds.musics.put(name, clip);
			}
		}
		catch (Exception e)
		{
			throw new IOException(e);
		}
		
		return sounds;
	}
	
	public static SoundBank loadLazy(File rootDir)
	{
		SoundBank sounds = new SoundBank();
		sounds.rootDir = rootDir;
		return sounds;
	}

	public void load(String name)
	{
		try
		{
			File file = new File(rootDir, name + ".wav");
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(file));
			clips.put(name, clip);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void loadMusic(String name)
	{
		try
		{
			File file = new File(rootDir, "music/" + name + ".wav");
			Clip clip = AudioSystem.getClip();
			clip.open(AudioSystem.getAudioInputStream(file));
			musics.put(name, clip);
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private Map<String, Clip> clips, musics;
	private Clip currentMusic;
	private boolean running;
	private File rootDir;
	
	private SoundBank()
	{
		clips   = new HashMap<String, Clip>();
		musics  = new HashMap<String, Clip>();
		running = false;
	}
	
	/**
	 * Starts playing clip by the given name. If clip is already in the process
	 * of being played, it will be restarted.
	 */
	public void play(String name)
	{
		if (!running || name == null)
			return;
		
		Clip clip = clips.get(name);
		
		if (clip == null)
		{
			load(name);
			clip = clips.get(name);
			
			if (clip == null)
			{
				throw new IllegalArgumentException(name + " not found");
			}
		}
		
		clip.stop();
		clip.setFramePosition(0);
		clip.start();
	}
	
	public void killMusic()
	{
		currentMusic.stop();
		currentMusic.setFramePosition(0);
		currentMusic = null;
	}
	
	public void playMusic(String name)
	{
		currentMusic = musics.get(name);
		
		if (currentMusic == null)
		{
			loadMusic(name);
			currentMusic = musics.get(name);
			
			if (currentMusic == null)
			{
				throw new IllegalArgumentException(name + " not found");
			}
		}
		
		currentMusic.loop(Clip.LOOP_CONTINUOUSLY);
	}
	
	public boolean isMusicPlaying()
	{
		return currentMusic != null && currentMusic.isActive();
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	/**
	 * Just sets running flag.
	 * 
	 * Should be called before playing any sound bites.
	 */
	public void start()
	{
		running = true;
		
		for (Clip clip : clips.values())
		{
			if (clip.getFramePosition() > 0)
			{
				clip.loop(Clip.LOOP_CONTINUOUSLY);
			}
		}
	}
	
	/**
	 * Stops all playing clips at their current playback position.
	 */
	public void stop()
	{
		running = false;
		
		for (Clip clip : clips.values())
		{
			clip.stop();
		}
	}
	
	/**
	 * Stops all playing clips and resets their playback position. Should be
	 * called after stop() to prevent left over data from being played back
	 * with SoundBank is started.
	 */
	public void flush()
	{
		for (Clip clip : clips.values())
		{
			clip.stop();
			clip.setFramePosition(0);
		}
	}
}
