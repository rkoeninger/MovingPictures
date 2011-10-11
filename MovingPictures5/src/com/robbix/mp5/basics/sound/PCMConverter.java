package com.robbix.mp5.basics.sound;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class PCMConverter
{
	// TEST METHODS ///////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	throws IOException, UnsupportedAudioFileException, LineUnavailableException
	{
		AudioInputStream ais = AudioSystem.getAudioInputStream(new File("./res/sounds/commandCenter.wav"));
		AudioFormat inFormat = ais.getFormat();
		AudioFormat outFormat = new AudioFormat(64000.0f, 16, 2, true, false);
//		System.out.println(inFormat);
//		System.out.println(outFormat);
		int dataSize = (int) (ais.getFrameLength() * inFormat.getFrameSize());
		byte[] data = new byte[dataSize];
		int bytesRead = 0;
		int pos = 0;
		
		while ((bytesRead = ais.read(data, pos, data.length - pos)) >= 0)
			pos += bytesRead;
		
		ais.close();
		data = convert(inFormat, outFormat, data);
		dump(data, "dataOut" + System.currentTimeMillis());
		Clip clip = AudioSystem.getClip();
		clip.open(outFormat, data, 0, data.length);
		clip.setFramePosition(0);
		clip.loop(0);
		
//		System.out.println("clip start");
		while (clip.isRunning());
//		System.out.println("clip end");
	}
	
	private static void dump(byte[] data, String filename) throws IOException
	{
		File file = new File("C:\\" + filename);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(data);
		fos.flush();
		fos.close();
	}
	
	// END TEST METHODS ///////////////////////////////////////////////////////
	
	public static byte[] convert(AudioFormat source, AudioFormat dest, byte[] in)
	{
		if (AudioFormats.matches(source, dest))
			return Arrays.copyOf(in, in.length);
		
		if (! (isPCM(source) && isPCM(dest)))
			throw new IllegalArgumentException();
		
		FloatSampleBuffer buffer = new FloatSampleBuffer(in, 0, in.length, source);
		
		if (source.getChannels() != dest.getChannels())
			buffer = convertChannels(buffer, dest.getChannels());
		
		if (source.getSampleRate() != dest.getSampleRate())
			buffer = convertRate(buffer, dest.getSampleRate());
		
		int outSize = buffer.getByteArrayBufferSize(dest);
		byte[] out = new byte[outSize];
		buffer.convertToByteArray(out, 0, dest);
		return out;
	}
	
	private static FloatSampleBuffer convertChannels(FloatSampleBuffer in, int destChannels)
	{
		int sourceChannels = in.getChannelCount();
		
		if (sourceChannels == 1 && destChannels == 2)
		{
			in.addChannel(false);
			in.copyChannel(0, 1);
		}
		else if (sourceChannels == 2 && destChannels == 1)
		{
			in.makeMono();
		}
		else
		{
			throwBadChannelConversion(sourceChannels, destChannels);
		}
		
		return in;
	}
	
	private static FloatSampleBuffer convertRate(FloatSampleBuffer in, float destRate)
	{
		int channelCount      = in.getChannelCount();
		float sourceRate      = in.getSampleRate();
		float rateRatio       = destRate / sourceRate;
		int size              = (int) (in.getSampleCount() * rateRatio);
		FloatSampleBuffer out = new FloatSampleBuffer(channelCount, size, destRate);
		
		if (destRate > sourceRate)
		{
			int previousIndex = 0;
			
			for (int i = 0; i < in.getSampleCount(); ++i)
			for (int c = 0; c < channelCount; ++c)
			{
				int i2 = index(i, out.getSampleCount(), rateRatio);
				out.getChannel(c)[i2] = in.getChannel(c)[i];
				
				// Interpolate over unwritten regions
				if (i2 - previousIndex > 1)
				{
					for (int y = 0; y < channelCount; ++y)
					for (int x = previousIndex + 1; x < i2; ++x)
					{
						out.getChannel(y)[x] = out.getChannel(y)[previousIndex];
					}
				}
				
				previousIndex = i2;
			}
		}
		else
		{
			rateRatio = 1 / rateRatio;
			
			for (int i = 0; i < out.getSampleCount(); ++i)
			for (int c = 0; c < channelCount; ++c)
			{
				int i2 = index(i, in.getSampleCount(), rateRatio);
				out.getChannel(c)[i] = in.getChannel(c)[i2];
			}
		}
		
		return out;
	}
	
	private static int index(int i, int size, float ratio)
	{
		i = (int) (i * ratio);
		
		if (i < 0)     return 0;
		if (i >= size) return size - 1;
		
		return i;
	}
	
	private static boolean isPCM(AudioFormat format)
	{
		return format.getEncoding().toString().contains("PCM");
	}
	
	private static void throwBadChannelConversion(int a, int b)
	{
		throw new Error("cannot convert channels " + a + " -> " + b);
	}
}
