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
		len = Math.min(len, available());
		
		for (int c = 0; c < buffer.getChannelCount(); ++c)
		for (int i = off; i < off + len; ++i, ++pos)
		{
			out.getChannel(c)[pos] += buffer.getChannel(c)[i];
		}
		
		return len;
	}
}
