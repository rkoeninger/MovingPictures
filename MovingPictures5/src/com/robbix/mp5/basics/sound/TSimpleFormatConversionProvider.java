package com.robbix.mp5.basics.sound;

import	java.util.Collection;
import	java.util.Iterator;

import	javax.sound.sampled.AudioFormat;
import	javax.sound.sampled.AudioSystem;

/**
 *	This is a base class for FormatConversionProviders that can convert
 *	from each source encoding/format to each target encoding/format.
 *	If this is not the case, use TEncodingFormatConversionProvider or
 *	TMatrixFormatConversionProvider.
 *
 *	<p>Overriding classes must implement at least
 *	<code>AudioInputStream getAudioInputStream(AudioFormat targetFormat, AudioInputStream sourceStream)</code>
 *	and provide a constructor that calls the protected constructor of this class.
 *
 * @author Matthias Pfisterer
 */
public abstract class TSimpleFormatConversionProvider extends TFormatConversionProvider
{
	private Collection	m_sourceEncodings;
	private Collection	m_targetEncodings;
	private Collection	m_sourceFormats;
	private Collection	m_targetFormats;




	protected TSimpleFormatConversionProvider(
		Collection sourceFormats,
		Collection targetFormats)
	{
		m_sourceEncodings = new ArraySet();
		m_targetEncodings = new ArraySet();
		m_sourceFormats = sourceFormats;
		m_targetFormats = targetFormats;
		collectEncodings(m_sourceFormats, m_sourceEncodings);
		collectEncodings(m_targetFormats, m_targetEncodings);
	}



	private static void collectEncodings(Collection formats,
				      Collection encodings)
	{
		Iterator	iterator = formats.iterator();
		while (iterator.hasNext())
		{
			AudioFormat	format = (AudioFormat) iterator.next();
			encodings.add(format.getEncoding());
		}
	}



	public AudioFormat.Encoding[] getSourceEncodings()
	{
		return (AudioFormat.Encoding[]) m_sourceEncodings.toArray(EMPTY_ENCODING_ARRAY);
	}



	public AudioFormat.Encoding[] getTargetEncodings()
	{
		return (AudioFormat.Encoding[]) m_targetEncodings.toArray(EMPTY_ENCODING_ARRAY);
	}



	// overwritten of FormatConversionProvider
	public boolean isSourceEncodingSupported(AudioFormat.Encoding sourceEncoding)
	{
		return m_sourceEncodings.contains(sourceEncoding);
	}



	// overwritten of FormatConversionProvider
	public boolean isTargetEncodingSupported(AudioFormat.Encoding targetEncoding)
	{
		return m_targetEncodings.contains(targetEncoding);
	}



	/**
	 *	This implementation assumes that the converter can convert
	 *	from each of its source encodings to each of its target
	 *	encodings. If this is not the case, the converter has to
	 *	override this method.
	 */
	public AudioFormat.Encoding[] getTargetEncodings(AudioFormat sourceFormat)
	{
		if (isAllowedSourceFormat(sourceFormat))
		{
			return getTargetEncodings();
		}
		else
		{
			return EMPTY_ENCODING_ARRAY;
		}
	}



	/**
	 *	This implementation assumes that the converter can convert
	 *	from each of its source formats to each of its target
	 *	formats. If this is not the case, the converter has to
	 *	override this method.
	 */
	public AudioFormat[] getTargetFormats(AudioFormat.Encoding targetEncoding, AudioFormat sourceFormat)
	{
		if (isConversionSupported(targetEncoding, sourceFormat))
		{
			return (AudioFormat[]) m_targetFormats.toArray(EMPTY_FORMAT_ARRAY);
		}
		else
		{
			return EMPTY_FORMAT_ARRAY;
		}
	}


	// TODO: check if necessary
	protected boolean isAllowedSourceEncoding(AudioFormat.Encoding sourceEncoding)
	{
		return m_sourceEncodings.contains(sourceEncoding);
	}



	protected boolean isAllowedTargetEncoding(AudioFormat.Encoding targetEncoding)
	{
		return m_targetEncodings.contains(targetEncoding);
	}



	protected boolean isAllowedSourceFormat(AudioFormat sourceFormat)
	{
		Iterator	iterator = m_sourceFormats.iterator();
		while (iterator.hasNext())
		{
			AudioFormat	format = (AudioFormat) iterator.next();
			if (AudioFormats.matches(format, sourceFormat))
			{
				return true;
			}
		}
		return false;
	}



	protected boolean isAllowedTargetFormat(AudioFormat targetFormat)
	{
		Iterator	iterator = m_targetFormats.iterator();
		while (iterator.hasNext())
		{
			AudioFormat	format = (AudioFormat) iterator.next();
			if (AudioFormats.matches(format, targetFormat))
			{
				return true;
			}
		}
		return false;
	}
	
	// $$fb 2000-04-02 added some convenience methods for overriding classes
	protected Collection getCollectionSourceEncodings() {
		return m_sourceEncodings;
	}
	
	protected Collection getCollectionTargetEncodings() {
		return m_targetEncodings;
	}
	
	protected Collection getCollectionSourceFormats() {
		return m_sourceFormats;
	}
	
	protected Collection getCollectionTargetFormats() {
		return m_targetFormats;
	}
	
	/**
	 * Utility method to check whether these values match, 
	 * taking into account AudioSystem.NOT_SPECIFIED.
	 * @return true if any of the values is AudioSystem.NOT_SPECIFIED 
	 * or both values have the same value.
	 */
	//$$fb 2000-08-16: moved from TEncodingFormatConversionProvider
	protected static boolean doMatch(int i1, int i2) {
		return i1==AudioSystem.NOT_SPECIFIED
			|| i2==AudioSystem.NOT_SPECIFIED
			|| i1==i2;
	}
    
	/**
	 * @see #doMatch(int,int)
	 */
	//$$fb 2000-08-16: moved from TEncodingFormatConversionProvider
	protected static boolean doMatch(float f1, float f2) {
		return f1==AudioSystem.NOT_SPECIFIED
			|| f2==AudioSystem.NOT_SPECIFIED
			|| Math.abs(f1 - f2) < 1.0e-9;
	}

	/**
	 * Utility method, replaces all occurences of AudioSystem.NOT_SPECIFIED
	 * in <code>targetFormat</code> with the corresponding value in <code>sourceFormat</code>.
	 * If <code>targetFormat</code> does not contain any fields with AudioSystem.NOT_SPECIFIED,
	 * it is returned unmodified. The endian-ness and encoding remains the same in all cases.
	 * <p>
	 * If any of the fields is AudioSystem.NOT_SPECIFIED in both <code>sourceFormat</code> and 
	 * <code>targetFormat</code>, it will remain not specified.
	 * <p>
	 * This method uses <code>getFrameSize(...)</code> (see below) to set the new frameSize, 
	 * if a new AudioFormat instance is created.
	 * <p>
	 * This method isn't used in TSimpleFormatConversionProvider - it is solely there
	 * for inheriting classes.
	 */
	//$$fb 2000-08-16: moved from TEncodingFormatConversionProvider
	protected AudioFormat replaceNotSpecified(AudioFormat sourceFormat, AudioFormat targetFormat) {
		boolean bSetSampleSize=false;
		boolean bSetChannels=false;
		boolean bSetSampleRate=false;
		boolean bSetFrameRate=false;
		if (targetFormat.getSampleSizeInBits()==AudioSystem.NOT_SPECIFIED 
		    && sourceFormat.getSampleSizeInBits()!=AudioSystem.NOT_SPECIFIED) {
			bSetSampleSize=true;
		}
		if (targetFormat.getChannels()==AudioSystem.NOT_SPECIFIED 
		    && sourceFormat.getChannels()!=AudioSystem.NOT_SPECIFIED) {
			bSetChannels=true;
		}
		if (targetFormat.getSampleRate()==AudioSystem.NOT_SPECIFIED 
		    && sourceFormat.getSampleRate()!=AudioSystem.NOT_SPECIFIED) {
			bSetSampleRate=true;
		}
		if (targetFormat.getFrameRate()==AudioSystem.NOT_SPECIFIED 
		    && sourceFormat.getFrameRate()!=AudioSystem.NOT_SPECIFIED) {
			bSetFrameRate=true;
		}
		if (bSetSampleSize || bSetChannels || bSetSampleRate || bSetFrameRate 
		    || (targetFormat.getFrameSize()==AudioSystem.NOT_SPECIFIED
			&& sourceFormat.getFrameSize()!=AudioSystem.NOT_SPECIFIED)) {
			// create new format in place of the original target format
			float sampleRate=bSetSampleRate?
				sourceFormat.getSampleRate():targetFormat.getSampleRate();
			float frameRate=bSetFrameRate?
				sourceFormat.getFrameRate():targetFormat.getFrameRate();
			int sampleSize=bSetSampleSize?
				sourceFormat.getSampleSizeInBits():targetFormat.getSampleSizeInBits();
			int channels=bSetChannels?
				sourceFormat.getChannels():targetFormat.getChannels();
			int frameSize=getFrameSize(
						   targetFormat.getEncoding(),
						   sampleRate,
						   sampleSize,
						   channels,
						   frameRate,
						   targetFormat.isBigEndian(),
						   targetFormat.getFrameSize());
			targetFormat= new AudioFormat(
						 targetFormat.getEncoding(),
						 sampleRate,
						 sampleSize,
						 channels,
						 frameSize,
						 frameRate,
						 targetFormat.isBigEndian());
		}
		return targetFormat;
	}

	/**
	 * Calculates the frame size for the given format description.
	 * The default implementation returns AudioSystem.NOT_SPECIFIED
	 * if either <code>sampleSize</code> or <code>channels</code> is AudioSystem.NOT_SPECIFIED,
	 * otherwise <code>sampleSize*channels/8</code> is returned.
	 * <p>
	 * If this does not reflect the way to calculate the right frame size,
	 * inheriting classes should overwrite this method if they use 
	 * replaceNotSpecified(...). It is not used elsewhere in this class.
	 */
	//$$fb 2000-08-16: added
	protected int getFrameSize(
				   AudioFormat.Encoding encoding,
				   float sampleRate,
				   int sampleSize,
				   int channels,
				   float frameRate,
				   boolean bigEndian,
				   int oldFrameSize) {
		if (sampleSize==AudioSystem.NOT_SPECIFIED || channels==AudioSystem.NOT_SPECIFIED) {
			return AudioSystem.NOT_SPECIFIED;
		}
		return sampleSize*channels/8;
	}



}

/*** TSimpleFormatConversionProvider.java ***/
