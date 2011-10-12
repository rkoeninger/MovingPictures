package com.robbix.mp5.basics;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class SampleBufferTest
{
	public static void main(String[] args)
	throws IOException, UnsupportedAudioFileException, LineUnavailableException
	{
		AudioInputStream ais = AudioSystem.getAudioInputStream(new File("./res/sounds/commandCenter.wav"));
		AudioFormat inFormat = ais.getFormat();
		AudioFormat outFormat = new AudioFormat(48000.0f, 16, 2, true, false);
		System.out.println(inFormat);
		System.out.println(outFormat);
		int dataSize = (int) (ais.getFrameLength() * inFormat.getFrameSize());
		byte[] data = new byte[dataSize];
		int bytesRead = 0;
		int pos = 0;
		
		while ((bytesRead = ais.read(data, pos, data.length - pos)) >= 0)
			pos += bytesRead;
		
		ais.close();
		data = new SampleBuffer(data, inFormat).getBytes(outFormat);
		dump(data, "dataOut" + System.currentTimeMillis());
		Clip clip = AudioSystem.getClip();
		clip.open(outFormat, data, 0, data.length);
		clip.setFramePosition(0);
		clip.loop(0);
		
		System.out.println("clip start");
		while (clip.isRunning());
		System.out.println("clip end");
	}
	
	private static void dump(byte[] data, String filename) throws IOException
	{
		File file = new File("C:\\" + filename);
		FileOutputStream fos = new FileOutputStream(file);
		fos.write(data);
		fos.flush();
		fos.close();
	}
}
