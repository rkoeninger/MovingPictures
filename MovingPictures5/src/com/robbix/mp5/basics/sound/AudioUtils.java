package com.robbix.mp5.basics.sound;

import java.util.Iterator;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;

public class AudioUtils
{
	public static long getLengthInBytes(AudioInputStream ais)
	{
		return getLengthInBytes(ais.getFormat(), ais.getFrameLength());
	}
	
	public static long getLengthInBytes(AudioFormat format, long frameLength)
	{
		int	frameSize = format.getFrameSize();
		
		return (frameLength >= 0 && frameSize >= 1)
			? frameLength * frameSize
			: AudioSystem.NOT_SPECIFIED;
	}
	
	public static boolean containsFormat(AudioFormat format, Iterator<AudioFormat> formats)
	{
		while (formats.hasNext())
			if (AudioFormats.matches(format, formats.next()))
				return true;
		
		return false;
	}
	
    public static String NS_or_number(int number)
    {
    	return number == AudioSystem.NOT_SPECIFIED
    		? "NOT_SPECIFIED"
    		: String.valueOf(number);
    }
    
    public static String NS_or_number(float number)
    {
    	return number == AudioSystem.NOT_SPECIFIED
    		? "NOT_SPECIFIED"
    		: String.valueOf(number);
    }
    
    public static String format2ShortStr(AudioFormat format)
    {
    	return format.getEncoding() + "-" +
	    NS_or_number(format.getChannels()) + "ch-" +
	    NS_or_number(format.getSampleSizeInBits()) + "bit-" +
	    NS_or_number(((int)format.getSampleRate())) + "Hz-"+
	    (format.isBigEndian() ? "be" : "le");
    }
}
