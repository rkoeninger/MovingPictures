package com.robbix.mp5.ui;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import com.robbix.mp5.Modular;
import com.robbix.mp5.ModuleEvent;
import com.robbix.mp5.ModuleListener;
import com.robbix.mp5.utils.SampleBuffer;
import com.robbix.mp5.utils.SampleStream;

public class SoundBank implements Modular
{
	public static SoundBank load(File rootDir, boolean lazy) throws IOException
	{
		return lazy ? loadLazy(rootDir) : preload(rootDir);
	}
	
	public static SoundBank preload(File rootDir) throws IOException
	{
		SoundBank sounds = new SoundBank();
		sounds.rootDir = rootDir;
		
		for (File file : rootDir.listFiles())
		{
			if (! file.getName().endsWith(".wav"))
				continue;
			
			sounds.loadModule(file);
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
	
	/**
	 * Default input format as sound effects from OP2 are formatted:
	 * 22.05 kHz, 8-bit, mono, unsigned
	 */
	public static final AudioFormat DEFAULT_IN_FORMAT =
		new AudioFormat(22050.0f, 8, 2, false, false);
	
	/**
	 * Default output format used by SoundBank. Conforms to CD audio standard,
	 * as it is probably the most supported format by hardware:
	 * 44.1 kHz, 16-bit, stereo, signed, little-endian
	 */
	public static final AudioFormat DEFAULT_OUT_FORMAT =
		new AudioFormat(44100.0f, 16, 2, true, false);
	
	private AudioFormat outFormat;
	private SourceDataLine line;
	private Object lineLock = new Object();
	private Map<String, SampleBuffer> buffers;
	private Collection<SampleStream> playList;
	private File rootDir;
	private ModuleListener.Helper listenerHelper;
	private DoPlay doPlay;
	private int maxStreamCount = 10;
	private float volume = 1.0f;
	
	private SoundBank()
	{
		listenerHelper = new ModuleListener.Helper();
		buffers = new HashMap<String, SampleBuffer>();
		playList = new LinkedList<SampleStream>();
		outFormat = DEFAULT_OUT_FORMAT;
	}
	
	public void addModuleListener(ModuleListener listener)
	{
		listenerHelper.add(listener);
	}
	
	public void removeModuleListener(ModuleListener listener)
	{
		listenerHelper.remove(listener);
	}
	
	public void loadModule(String name) throws IOException
	{
		loadModule(new File(rootDir, name + ".wav"));
	}
	
	public void loadModule(File file) throws IOException
	{
		try
		{
			String name = file.getName();
			name = name.substring(0, name.lastIndexOf('.'));
			SampleBuffer buffer = SampleBuffer.load(file);
			buffer.rechannel(DEFAULT_IN_FORMAT.getChannels());
			buffer.resample(DEFAULT_IN_FORMAT.getSampleRate());
			buffers.put(name, buffer);
			listenerHelper.fireModuleLoaded(new ModuleEvent(this, name));
		}
		catch (Exception e)
		{
			throw new IOException(e);
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
			try
			{
				loadModule(name);
				buffer = buffers.get(name);
			}
			catch (IOException ioe)
			{
				System.err.println("soundbite " + name + " not found");
				return null;
			}
		}
		
		return buffer.copy();
	}
	
	public void setVolume(float volume)
	{
		if (volume < 0)
			throw new IllegalArgumentException("Illegal volume: " + volume);
		
		this.volume = volume;
	}
	
	public float getVolume()
	{
		return volume;
	}
	
	public void play(String name)
	{
		play(name, null);
	}

	public void play(String name, SampleStream.Callback callback)
	{
		play(name, 1, 0, callback);
	}
	
	public void play(String name, float volume, float spread, SampleStream.Callback callback)
	{
		if (name == null)
			return;
		
		if (!isRunning())
			return;
		
		SampleBuffer buffer = buffers.get(name);
		
		if (buffer == null)
		{
			try
			{
				loadModule(name);
				buffer = buffers.get(name);
			}
			catch (IOException ioe)
			{
				System.err.println("soundbite " + name + " not found");
				return;
			}
		}
		
		synchronized (playList)
		{
			if (playList.size() < maxStreamCount)
				playList.add(new SampleStream(buffer, volume, spread, callback));
		}
	}
	
	public boolean isRunning()
	{
		return doPlay != null;
	}
	
	public boolean isActive()
	{
		return doPlay != null && ! playList.isEmpty();
	}
	
	public int getActiveStreamCount()
	{
		return playList.size();
	}
	
	public void setFormat(AudioFormat format)
	{
		boolean wasRunning = isRunning();
		stop();
		outFormat = format;
		
		if (wasRunning)
			start();
	}
	
	public AudioFormat getFormat()
	{
		return outFormat;
	}
	
	/**
	 * Should be called before playing any sound bites.
	 * 
	 * @return true If SoundBank is running at end of method call.
	 */
	public synchronized boolean start()
	{
		if (doPlay != null)
			return true;
		
		try
		{
			synchronized (lineLock)
			{
				openLine(outFormat);
				line.start();
			}
		}
		catch (IOException ioe)
		{
			return false;
		}
		
		doPlay = new DoPlay();
		doPlay.start();
		return true;
	}
	
	/**
	 * Stops all playing clips at their current playback position.
	 * 
	 * @return true If SoundBank is stopped at end of method call.
	 */
	public synchronized boolean stop()
	{
		if (doPlay == null)
			return true;
		
		doPlay.end();
		doPlay = null;
		
		synchronized (lineLock)
		{
			line.stop();
			line = null;
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
	
	private static final AtomicInteger serial = new AtomicInteger();
	
	private class DoPlay extends Thread
	{
		private boolean running;
		
		public DoPlay()
		{
			setName("MP5-Sound-" + serial.getAndIncrement());
			setDaemon(true);
			running = true;
		}
		
		public void end()
		{
			running = false;
		}
		
		public void run()
		{
			SampleBuffer buffer = new SampleBuffer(DEFAULT_IN_FORMAT, 4000);
			byte[] data = new byte[buffer.getByteSize(outFormat)];
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
				
				if (volume == 0)
					continue;
				
				buffer.scale(volume);
				
				synchronized (lineLock)
				{
					int byteLen = buffer.getBytes(data, outFormat, numSamples);
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
