package com.robbix.mp5.basics;

public class SampleStream
{
	private SampleBuffer buffer;
	private int pos;
	
	public SampleStream(SampleBuffer buffer)
	{
		this.buffer = buffer;
		this.pos = 0;
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
		
		return len;
	}
}
