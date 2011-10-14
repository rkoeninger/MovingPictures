package com.robbix.mp5.ui;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.robbix.mp5.ModuleEvent;
import com.robbix.mp5.ModuleListener;
import com.robbix.mp5.basics.SampleBuffer;
import com.robbix.mp5.basics.SampleStream;

public class SoundBank
{
	private static AudioFormat INCOMING_FORMAT = new AudioFormat(22050.0f, 8, 1, false, false);
	private static AudioFormat DEFAULT_FORMAT = new AudioFormat(44100.0f, 16, 2, true, false);
	
	public static SoundBank load(File rootDir, boolean lazy) throws IOException
	{
		return lazy ? loadLazy(rootDir) : preload(rootDir);
	}
	
	public static SoundBank preload(File rootDir) throws IOException
	{
		SoundBank sounds = new SoundBank();
		sounds.rootDir = rootDir;
		sounds.openLine(DEFAULT_FORMAT);
		
		for (File file : rootDir.listFiles())
		{
			if (! file.getName().endsWith(".wav"))
				continue;
			
			String rawName = file.getName().toLowerCase();
			int i = rawName.lastIndexOf(".");
			String name = rawName.substring(0, i);
			SampleBuffer buffer = SampleBuffer.load(file);
			buffer.rechannel(INCOMING_FORMAT.getChannels());
			buffer.resample(INCOMING_FORMAT.getSampleRate());
			sounds.buffers.put(name, buffer);
		}
		
		return sounds;
	}
	
	public static SoundBank loadLazy(File rootDir)
	{
		SoundBank sounds = new SoundBank();
		sounds.rootDir = rootDir;
		return sounds;
	}
	
	private void openLine(AudioFormat format) throws IOException
	{
		try
		{
			synchronized (lineLock)
			{
				line = AudioSystem.getSourceDataLine(format);
				line.open(format);
				outFormat = format;
			}
		}
		catch (LineUnavailableException lue)
		{
			throw new IOException(lue);
		}		
	}
	
	private AudioFormat outFormat;
	private SourceDataLine line;
	private Object lineLock = new Object();
	private Map<String, SampleBuffer> buffers;
	private Collection<SampleStream> playList;
	private boolean running;
	private File rootDir;
	private ModuleListener.Helper listenerHelper;
	
	private SoundBank()
	{
		running = false;
		listenerHelper = new ModuleListener.Helper();
		buffers = new HashMap<String, SampleBuffer>();
		playList = new LinkedList<SampleStream>();
	}
	
	public void addModuleListener(ModuleListener listener)
	{
		listenerHelper.add(listener);
	}
	
	public void removeModuleListener(ModuleListener listener)
	{
		listenerHelper.remove(listener);
	}
	
	public void loadModule(String name)
	{
		try
		{
			File file = new File(rootDir, name + ".wav");
			SampleBuffer buffer = SampleBuffer.load(file);
			buffer.rechannel(INCOMING_FORMAT.getChannels());
			buffer.resample(INCOMING_FORMAT.getSampleRate());
			buffers.put(name, buffer);
			listenerHelper.fireModuleLoaded(new ModuleEvent(this, name));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public boolean isLoaded(String name)
	{
		return buffers.containsKey(name);
	}
	
	public boolean unloadModule(String name)
	{
		if (buffers.containsKey(name))
		{
			buffers.remove(name);
		}
		else
		{
			return false;
		}
		
		listenerHelper.fireModuleUnloaded(new ModuleEvent(this, name));
		return true;
	}
	
	public Set<String> getLoadedModules()
	{
		return buffers.keySet();
	}
	
	public SampleBuffer getData(String name)
	{
		SampleBuffer buffer = buffers.get(name);
		
		if (buffer == null)
		{
			loadModule(name);
			buffer = buffers.get(name);
			
			if (buffer == null)
			{
				System.err.println("soundbite " + name + " not found");
				return null;
			}
		}
		
		return buffer.copy();
	}
	
	public void play(String name)
	{
		play(name, null);
	}
	
	public void play(String name, SampleStream.Callback callback)
	{
		if (name == null)
			return;
		
		SampleBuffer buffer = buffers.get(name);
		
		if (buffer == null)
		{
			loadModule(name);
			buffer = buffers.get(name);
			
			if (buffer == null)
			{
				System.err.println("soundbite " + name + " not found");
				return;
			}
		}
		
		synchronized (playList)
		{
			playList.add(new SampleStream(buffer, callback));
		}
	}
	
	public boolean isRunning()
	{
		return running;
	}
	
	public boolean isActive()
	{
		return running && ! playList.isEmpty();
	}
	
	public int getActiveStreamCount()
	{
		return playList.size();
	}
	
	public void setFormat(AudioFormat format)
	{
		stop();
		outFormat = format;
		start();
	}
	
	/**
	 * Should be called before playing any sound bites.
	 * 
	 * @return true If SoundBank is running at end of method call.
	 */
	public boolean start()
	{
		if (running)
			return true;
		
		running = true;
		
		synchronized (lineLock)
		{
			if (line == null)
			{
				try
				{
					openLine(outFormat == null ? DEFAULT_FORMAT : outFormat);
				}
				catch (IOException ioe)
				{
					return false;
				}
			}
			
			line.start();
		}
		
		Thread playThread = new Thread(new DoPlay());
		playThread.setName("MP5-Sound");
		playThread.setDaemon(true);
		playThread.start();
		return true;
	}
	
	/**
	 * Stops all playing clips at their current playback position.
	 * 
	 * @return true If SoundBank is stopped at end of method call.
	 */
	public boolean stop()
	{
		if (! running)
			return true;
		
		running = false;
		
		synchronized (lineLock)
		{
			line.stop();
		}
		
		return true;
	}
	
	/**
	 * Clears all remaining sound streams. The stop() method merely stops
	 * feeding them to the mixer, unfinished playbacks will still be there.
	 */
	public void flush()
	{
		synchronized (lineLock)
		{
			line.flush();
		}
		
		synchronized (playList)
		{
			playList.clear();
		}
	}
	
	private class DoPlay implements Runnable
	{
		public void run()
		{
			SampleBuffer buffer = new SampleBuffer(INCOMING_FORMAT, 4000);
			byte[] data = new byte[buffer.getByteSize(DEFAULT_FORMAT)];
			int numSamples; // max of num samples copied this round
			
			while (running)
			{
				if (playList.isEmpty())
				{
					trySleep(50);
					continue;
				}
				
				buffer.makeSilence();
				numSamples = 0;
				
				synchronized (playList)
				{
					Iterator<SampleStream> streamItr = playList.iterator();
					
					while (streamItr.hasNext())
					{
						SampleStream stream = streamItr.next();
						numSamples = Math.max(numSamples, stream.mix(buffer));
						
						if (stream.isDone())
							streamItr.remove();
					}
				}
				
				if (numSamples == 0)
					continue;
				
				synchronized (lineLock)
				{
					buffer.getBytes(data, outFormat, numSamples);
					int byteLen = buffer.getByteSize(outFormat, numSamples);
					int pos = 0;
					
					while (pos < byteLen)
						pos += line.write(data, pos, byteLen - pos);
				}
			}
		}
		
		private boolean trySleep(long millis)
		{
			try
			{
				Thread.sleep(millis);
				return true;
			}
			catch (InterruptedException ie)
			{
				return false;
			}
		}
	}
}
