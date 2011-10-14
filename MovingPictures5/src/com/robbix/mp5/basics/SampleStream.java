package com.robbix.mp5.basics;

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
	
	public SampleStream(SampleBuffer buffer)
	{
		this.buffer = buffer;
		this.pos = 0;
	}
	
	public SampleStream(SampleBuffer buffer, Callback callback)
	{
		this(buffer);
		this.callback = callback;
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
		
		if (pos == 0 && callback != null)
			callback.starting();
		
		len = Math.min(len, available());
		
		for (int c = 0; c < buffer.getChannelCount(); ++c)
		{
			float[] bufferChannel = buffer.getChannel(c);
			float[] outChannel = out.getChannel(c);
			
			for (int i = off; i < off + len; ++i, ++pos)
			{
				float sample = outChannel[i];
				sample += bufferChannel[pos];
				outChannel[i] = Math.max(-1, Math.min(1, sample));
			}
		}
		
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
}
