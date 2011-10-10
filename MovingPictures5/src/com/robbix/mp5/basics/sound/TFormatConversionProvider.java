package com.robbix.mp5.basics.sound;

import	javax.sound.sampled.AudioFormat;
import	javax.sound.sampled.AudioInputStream;
import	javax.sound.sampled.spi.FormatConversionProvider;


/**
 * Base class for all conversion providers.
 *
 * @author Matthias Pfisterer
 */

public abstract class TFormatConversionProvider
	extends		FormatConversionProvider
{
	protected static final AudioFormat.Encoding[]	EMPTY_ENCODING_ARRAY = new AudioFormat.Encoding[0];
	protected static final AudioFormat[]		EMPTY_FORMAT_ARRAY = new AudioFormat[0];



	// TODO: find a better solution; move out of TFormatConversionProvider
	// very primitive, not too useful
	// perhaps use some overwritable method getDefaultAudioFormat(Encoding)
	public AudioInputStream getAudioInputStream(AudioFormat.Encoding targetEncoding, AudioInputStream audioInputStream)
	{
		AudioFormat	sourceFormat = audioInputStream.getFormat();
		AudioFormat	targetFormat = new AudioFormat(
			targetEncoding,
			sourceFormat.getSampleRate(),
			sourceFormat.getSampleSizeInBits(),
			sourceFormat.getChannels(),
			sourceFormat.getFrameSize(),
			sourceFormat.getFrameRate(),
			sourceFormat.isBigEndian());
		return getAudioInputStream(targetFormat, audioInputStream);
	}
}



/*** TFormatConversionProvider.java ***/
