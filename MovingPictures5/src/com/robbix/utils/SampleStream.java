package com.robbix.utils;

public class SampleStream
{
	public static interface Callback
	{
		public void starting();
		public void progress(double progress);
		public void complete();
	}
	
	private SampleBuffer buffer;
	private int pos;
	private Callback callback;
	private float volume;
	private float spread; // -1.0 = L, 1.0 = R
	
	public SampleStream(SampleBuffer buffer)
	{
		this(buffer, 1, 0, null);
	}
	
	public SampleStream(SampleBuffer buffer, Callback callback)
	{
		this(buffer, 1, 0, callback);
	}
	
	public SampleStream(SampleBuffer buffer, float volume, float spread)
	{
		this(buffer, volume, spread, null);
	}
	
	public SampleStream(SampleBuffer buffer, float volume, float spread, Callback callback)
	{
		if (buffer == null)
			throw new NullPointerException("buffer");
		
		if (volume <= 0)
			throw new IllegalArgumentException("Invalid volume: " + volume);
		
		if (spread < -1.0 || spread > 1.0)
			throw new IllegalArgumentException("Invalid spread: " + spread);
		
		this.buffer = buffer;
		this.volume = volume;
		this.spread = spread;
		this.callback = callback;
	}
	
	public float getVolume()
	{
		return volume;
	}
	
	public float getSpread()
	{
		return spread;
	}
	
	public int available()
	{
		return buffer.length() - pos;
	}
	
	public boolean isDone()
	{
		return pos >= buffer.length();
	}
	
	public int getPosition()
	{
		return pos;
	}
	
	public int length()
	{
		return buffer.length();
	}
	
	public synchronized void reset()
	{
		pos = 0;
	}
	
	public synchronized int mix(SampleBuffer out)
	{
		return mix(out, 0, out.length());
	}
	
	public synchronized int mix(SampleBuffer out, int off, int len)
	{
		if (! buffer.formatMatches(out))
			throw new IllegalArgumentException("Buffer formats do not match");
		
		if (off + len > out.length())
			throw new IndexOutOfBoundsException("out buffer");
		
		if (pos == 0 && callback != null)
			callback.starting();
		
		len = Math.min(len, available());
		
		for (int c = 0; c < buffer.getChannelCount(); ++c)
		{
			float[] bufferChannel = buffer.getChannel(c);
			float[] outChannel = out.getChannel(c);
			
			float spreadFactor = (buffer.getChannelCount() == 2)
				? getSpreadFactor(spread, c == 0)
				: 1.0f;
			
			for (int i = 0; i < len; ++i)
			{
				float sample = outChannel[off + i];
				sample += bufferChannel[pos + i] * volume * spreadFactor;
				sample = Math.max(-1, Math.min(1, sample));
				outChannel[off + i] = sample;
			}
		}
		
		pos += len;
		
		if (callback != null)
		{
			if (available() > 0)
			{
				callback.progress(pos / (double) buffer.length());
			}
			else
			{
				callback.complete();
			}
		}
		
		return len;
	}
	
	private float getSpreadFactor(float spread, boolean left)
	{
		if (left)
		{
			if (spread > 0) // Panned right and this is left channel
			{
				return 1 - spread;
			}
			else
			{
				return 1;
			}
		}
		else
		{
			if (spread < 0) // Panned left and this is right channel
			{
				return 1 + spread;
			}
			else
			{
				return 1;
			}
		}
	}
}
