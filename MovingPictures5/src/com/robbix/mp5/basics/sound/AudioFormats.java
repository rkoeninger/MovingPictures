package com.robbix.mp5.basics.sound;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;

public class AudioFormats
{
	public static boolean matches(AudioFormat a, AudioFormat b)
	{
		return a.getEncoding().equals(b.getEncoding())
			&& doSampleSizeMatch(a, b)
			&& doMatch(a.getChannels(),         b.getChannels())
			&& doMatch(a.getSampleSizeInBits(), b.getSampleSizeInBits())
			&& doMatch(a.getFrameSize(),        b.getFrameSize())
			&& doMatch(a.getSampleRate(),       b.getSampleRate())
			&& doMatch(a.getFrameRate(),        b.getFrameRate());
	}
	
	private static boolean doMatch(int i1, int i2)
	{
		return i1 == AudioSystem.NOT_SPECIFIED
			|| i2 == AudioSystem.NOT_SPECIFIED
			|| i1 == i2;
	}
	
	private static boolean doMatch(float f1, float f2)
	{
		return f1 == AudioSystem.NOT_SPECIFIED
			|| f2 == AudioSystem.NOT_SPECIFIED
			|| Math.abs(f1 - f2) < 1.0e-9;
	}
	
	private static boolean doSampleSizeMatch(AudioFormat a, AudioFormat b)
	{
		return b.getSampleSizeInBits() <= 8
			|| b.getSampleSizeInBits() == AudioSystem.NOT_SPECIFIED
			|| a.isBigEndian() == b.isBigEndian();
	}
}
